package com.game.dream.audio;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SoundFont2 + TinySoundFont（方案 A）：GM 音源，AudioTrack 流式输出。
 * 将 GM SoundFont 置于 assets/keyboard/gm.sf2（GeneralUser、FluidR3 等裁剪版均可）。
 */
public class Sf2MidiEngine {

    private static final String TAG = "Sf2MidiEngine";
    private static final String[] SF2_ASSET_PATHS = {
            "keyboard/gm.sf2",
            "keyboard/builtin_min.sf2"
    };
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int MIN_RENDER_FRAMES = 32;
    private static final int MAX_RENDER_FRAMES = 96;

    private static Sf2MidiEngine instance;
    private static volatile boolean nativeLibraryLoaded;
    private static volatile String lastLoadError = "";

    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final AtomicBoolean flushPending = new AtomicBoolean();
    private final Object audioLock = new Object();
    private final Set<Integer> sounding = new HashSet<>();
    private volatile int currentProgram = GmInstruments.DEFAULT_PROGRAM;
    private volatile int presetCount;
    private volatile boolean placeholderFont;
    private Thread audioThread;
    private AudioTrack audioTrack;
    private Context appContext;
    private int outputSampleRate = DEFAULT_SAMPLE_RATE;
    private int framesPerBuffer = 64;

    static {
        try {
            System.loadLibrary("keyboardsynth");
            nativeLibraryLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            nativeLibraryLoaded = false;
            lastLoadError = "本地合成库未编入安装包，需完整编译 NDK";
            Log.e(TAG, "loadLibrary keyboardsynth failed", e);
        }
    }

    public static synchronized Sf2MidiEngine getInstance() {
        if (instance == null) {
            instance = new Sf2MidiEngine();
        }
        return instance;
    }

    private static native boolean nativeLoadSoundFont(byte[] data);

    private static native void nativeSetSampleRate(int sampleRate);

    private static native void nativeSetProgram(int program);

    private static native void nativeNoteOn(int midi, int velocity);

    private static native void nativeNoteOff(int midi);

    private static native void nativeAllNotesOff();

    private static native int nativeRender(short[] buffer, int frameCount);

    private static native void nativeRelease();

    private static native boolean nativeIsReady();

    private static native int nativeGetPresetCount();

    private static native int nativeGetActiveVoices();

    public boolean isReady() {
        return ready.get() && nativeLibraryLoaded && nativeIsReady();
    }

    public static String getLastLoadError() {
        return lastLoadError != null ? lastLoadError : "";
    }

    public int getCurrentProgram() {
        return currentProgram;
    }

    public boolean isPlaceholderFont() {
        return placeholderFont;
    }

    public int getPresetCount() {
        return presetCount;
    }

  public void loadAsync(Context context, Runnable onReady, Runnable onFailed) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        new Thread(() -> {
            boolean ok = false;
            try {
                ok = loadSoundFontFromAssets();
            } catch (UnsatisfiedLinkError e) {
                lastLoadError = "本地合成库加载失败，请重新完整编译安装";
                Log.e(TAG, "native call failed", e);
            }
            if (ok) {
                startAudioThread();
                ready.set(true);
                lastLoadError = "";
                if (onReady != null) {
                    onReady.run();
                }
            } else {
                ready.set(false);
                if (onFailed != null) {
                    onFailed.run();
                }
            }
        }, "sf2-load").start();
    }

    private boolean loadSoundFontFromAssets() {
        if (!nativeLibraryLoaded) {
            return false;
        }
        if (appContext == null) {
            lastLoadError = "应用上下文未就绪";
            return false;
        }
        resolveOutputAudioParams();
        nativeSetSampleRate(outputSampleRate);
        File ext = new File(appContext.getExternalFilesDir("keyboard"), "gm.sf2");
        if (ext.isFile() && ext.length() > 20_000) {
            try {
                if (loadSoundFontBytes(readFileBytes(ext), "external:" + ext.getAbsolutePath())) {
                    return true;
                }
            } catch (IOException e) {
                Log.w(TAG, "external gm.sf2 read failed: " + e.getMessage());
            }
        }
        AssetManager am = appContext.getAssets();
        for (String path : SF2_ASSET_PATHS) {
            try (InputStream in = am.open(path)) {
                if (loadSoundFontBytes(readStream(in), "asset:" + path)) {
                    return true;
                }
                lastLoadError = "SoundFont 解析失败: " + path;
            } catch (IOException e) {
                Log.w(TAG, "skip " + path + ": " + e.getMessage());
            }
        }
        if (loadSoundFontBytes(BuiltinSoundFont.DATA, "builtin")) {
            return true;
        }
        if (lastLoadError == null || lastLoadError.isEmpty()) {
            lastLoadError = "未找到 keyboard/gm.sf2";
        }
        Log.e(TAG, "no soundfont available: " + lastLoadError);
        return false;
    }

    private static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    private static byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return readStream(in);
        }
    }

    private boolean loadSoundFontBytes(byte[] data, String source) {
        if (data == null || data.length < 12) {
            return false;
        }
        if (!nativeLoadSoundFont(data)) {
            return false;
        }
        presetCount = nativeGetPresetCount();
        placeholderFont = presetCount < 16 || data.length < 20_000;
        nativeSetProgram(GmInstruments.DEFAULT_PROGRAM);
        Log.i(TAG, "loaded " + source + " presets=" + presetCount + " bytes=" + data.length);
        lastLoadError = "";
        return true;
    }

    private void resolveOutputAudioParams() {
        outputSampleRate = DEFAULT_SAMPLE_RATE;
        framesPerBuffer = 64;
        if (appContext == null) {
            return;
        }
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            return;
        }
        int nativeRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        if (nativeRate > 0) {
            outputSampleRate = nativeRate;
        }
        try {
            String burst = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            if (burst != null) {
                int b = Integer.parseInt(burst);
                if (b > 0) {
                    framesPerBuffer = Math.max(MIN_RENDER_FRAMES, Math.min(MAX_RENDER_FRAMES, b / 2));
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void startAudioThread() {
        stopAudioThread();
        int channelMask = AudioFormat.CHANNEL_OUT_STEREO;
        int frameBytes = 2 * 2;
        int minBuf = AudioTrack.getMinBufferSize(
                outputSampleRate, channelMask, AudioFormat.ENCODING_PCM_16BIT);
        if (minBuf <= 0) {
            minBuf = framesPerBuffer * frameBytes * 2;
        }
        AudioAttributes.Builder attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attr.setFlags(AudioAttributes.FLAG_LOW_LATENCY);
        }
        AudioTrack.Builder trackBuilder = new AudioTrack.Builder()
                .setAudioAttributes(attr.build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(outputSampleRate)
                        .setChannelMask(channelMask)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build())
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY);
        }
        audioTrack = trackBuilder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "audio out rate=" + outputSampleRate
                    + " renderFrames=" + framesPerBuffer
                    + " trackBufFrames=" + audioTrack.getBufferSizeInFrames());
        }
        audioTrack.play();
        playing.set(true);
        final short[] mix = new short[MAX_RENDER_FRAMES * 2];
        final int renderFrames = framesPerBuffer;
        audioThread = new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            while (playing.get()) {
                if (flushPending.getAndSet(false)) {
                    flushPlaybackQueue();
                }
                boolean keysHeld;
                synchronized (sounding) {
                    keysHeld = !sounding.isEmpty();
                }
                if (!keysHeld && nativeGetActiveVoices() <= 0) {
                    idleWaitAudio();
                    continue;
                }
                int frames = nativeRender(mix, renderFrames);
                if (frames <= 0) {
                    waitAudioBrief();
                    continue;
                }
                int offset = 0;
                int samples = frames * 2;
                while (offset < samples && playing.get()) {
                    int wrote = writePcm(mix, offset, samples - offset);
                    if (wrote > 0) {
                        offset += wrote;
                    } else if (wrote == 0) {
                        SystemClock.sleep(1);
                    } else {
                        break;
                    }
                }
            }
        }, "sf2-audio");
        audioThread.start();
    }

    private void flushPlaybackQueue() {
        AudioTrack track = audioTrack;
        if (track == null) {
            return;
        }
        try {
            if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause();
                track.flush();
                track.play();
            }
        } catch (Exception e) {
            Log.w(TAG, "flush playback", e);
        }
    }

    private int writePcm(short[] mix, int offset, int count) {
        AudioTrack track = audioTrack;
        if (track == null) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int wrote = track.write(mix, offset, count, AudioTrack.WRITE_NON_BLOCKING);
            if (wrote == 0 && count > 0) {
                wrote = track.write(mix, offset, count);
            }
            return wrote;
        }
        return track.write(mix, offset, count);
    }

    private void waitAudioBrief() {
        synchronized (audioLock) {
            try {
                audioLock.wait(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void idleWaitAudio() {
        synchronized (audioLock) {
            try {
                audioLock.wait(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void signalAudioThread(boolean flushQueue) {
        if (flushQueue) {
            flushPending.set(true);
        }
        synchronized (audioLock) {
            audioLock.notifyAll();
        }
    }

    private void stopAudioThread() {
        playing.set(false);
        if (audioThread != null) {
            audioThread.interrupt();
            try {
                audioThread.join(800);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            audioThread = null;
        }
        if (audioTrack != null) {
            try {
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception ignored) {
            }
            audioTrack = null;
        }
    }

    public void setProgram(int program) {
        if (program < 0 || program >= GmInstruments.COUNT) {
            return;
        }
        currentProgram = program;
        if (!isReady()) {
            return;
        }
        nativeAllNotesOff();
        nativeSetProgram(program);
    }

    public void noteOn(int midi, int velocity) {
        if (!isReady()) {
            return;
        }
        boolean fromSilence;
        synchronized (sounding) {
            fromSilence = sounding.isEmpty();
            sounding.add(midi);
        }
        nativeNoteOn(midi, velocity);
        signalAudioThread(fromSilence);
    }

    public void noteOff(int midi) {
        if (!isReady()) {
            return;
        }
        synchronized (sounding) {
            sounding.remove(midi);
        }
        nativeNoteOff(midi);
    }

    public void allNotesOff() {
        synchronized (sounding) {
            sounding.clear();
        }
        nativeAllNotesOff();
        if (isReady()) {
            nativeSetProgram(currentProgram);
        }
    }

    public void release() {
        ready.set(false);
        allNotesOff();
        stopAudioThread();
        nativeRelease();
    }
}
package com.game.dream.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 古筝拨弦：预热 WAV + SoundPool 低延迟播放。
 */
public class GuzhengSoundPlayer {

    private static final String TAG = "GuzhengSound";
    private static final String ASSET_DIR = "guzheng";
    private static final int MAX_STREAMS = 24;
    /** 同弦防连触（过短会像哑音） */
    private static final long SAME_STRING_DEBOUNCE_MS = 28;

    private static GuzhengSoundPlayer instance;

    private Context appContext;
    private final List<GuzhengStringsData.StringEntry> stringTable = new ArrayList<>(22);
    private String mid55Sample;
    private String mid67Sample;

    private final Map<String, File> cachedSamples = new HashMap<>();
    /** 逻辑名 -> SoundPool sampleId */
    private final Map<String, Integer> soundIds = new HashMap<>();
    private final Random random = new Random();

    private SoundPool soundPool;
    private volatile boolean configReady;
    private volatile boolean poolReady;
    private volatile boolean warming;
    private volatile boolean released;

    private int lastStringNo;
    private long lastPlayMs;

    public static GuzhengSoundPlayer getInstance() {
        if (instance == null) {
            instance = new GuzhengSoundPlayer();
        }
        return instance;
    }

    public synchronized void init(Context context) {
        if (context == null) {
            Log.e(TAG, "init: context is null");
            return;
        }
        appContext = context.getApplicationContext();
        released = false;
        if (configReady && poolReady) {
            return;
        }
        if (!configReady) {
            loadConfig();
            configReady = hasStringConfig();
            Log.i(TAG, "init configReady=" + configReady);
        }
        if (!configReady || warming) {
            return;
        }
        warming = true;
        new Thread(() -> {
            try {
                warmCacheAndLoadPool();
            } finally {
                warming = false;
            }
        }, "guzheng-audio").start();
    }

    private boolean hasStringConfig() {
        if (stringTable.size() < 22) {
            return false;
        }
        for (int i = 1; i <= 21; i++) {
            if (stringTable.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    private void loadConfig() {
        stringTable.clear();
        try (InputStream in = appContext.getAssets().open(ASSET_DIR + "/guzheng_21_strings.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            GuzhengStringsData data = new Gson().fromJson(sb.toString(), GuzhengStringsData.class);
            if (data != null && data.strings != null) {
                for (GuzhengStringsData.StringEntry e : data.strings) {
                    if (e != null && e.no >= 1 && e.no <= 21) {
                        while (stringTable.size() <= e.no) {
                            stringTable.add(null);
                        }
                        stringTable.set(e.no, e);
                    }
                }
            }
            while (stringTable.size() < 22) {
                stringTable.add(null);
            }
            if (data != null && data.articulationMidPluck != null) {
                mid55Sample = data.articulationMidPluck.sampleMidi55Mid;
                mid67Sample = data.articulationMidPluck.sampleMidi67Mid;
            }
        } catch (Exception e) {
            Log.e(TAG, "load guzheng_21_strings.json failed", e);
        }
    }

    private void warmCacheAndLoadPool() {
        if (released) {
            return;
        }
        cachedSamples.clear();
        soundIds.clear();
        poolReady = false;

        Set<String> names = new HashSet<>();
        for (int i = 1; i < stringTable.size(); i++) {
            GuzhengStringsData.StringEntry e = stringTable.get(i);
            if (e == null) {
                continue;
            }
            if (e.sample != null) {
                names.add(e.sample);
            }
            if (e.rr != null) {
                names.addAll(e.rr);
            }
        }
        if (mid55Sample != null) {
            names.add(mid55Sample);
        }
        if (mid67Sample != null) {
            names.add(mid67Sample);
        }

        List<String> ordered = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            GuzhengStringsData.StringEntry e = stringTable.get(i);
            if (e != null && e.sample != null && names.contains(e.sample)) {
                ordered.add(e.sample);
                names.remove(e.sample);
            }
        }
        ordered.addAll(names);

        for (String name : ordered) {
            File file = GuzhengSampleCache.resolvePlayableFile(appContext, ASSET_DIR, name);
            if (file != null && file.exists()) {
                cachedSamples.put(name, file);
            }
        }

        if (cachedSamples.isEmpty()) {
            Log.e(TAG, "no cached samples");
            return;
        }
        if (released) {
            return;
        }

        releasePoolInternal();
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        SoundPool.Builder builder = new SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attrs);
        soundPool = builder.build();

        final int[] pending = {cachedSamples.size()};
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status != 0) {
                Log.w(TAG, "SoundPool load failed id=" + sampleId + " status=" + status);
            }
            synchronized (GuzhengSoundPlayer.this) {
                pending[0]--;
                if (pending[0] <= 0 && !released) {
                    poolReady = true;
                    Log.i(TAG, "SoundPool ready, samples=" + soundIds.size());
                }
            }
        });

        for (Map.Entry<String, File> e : cachedSamples.entrySet()) {
            if (released) {
                releasePoolInternal();
                soundIds.clear();
                return;
            }
            int id = soundPool.load(e.getValue().getAbsolutePath(), 1);
            if (id > 0) {
                soundIds.put(e.getKey(), id);
            } else {
                Log.w(TAG, "load queue failed: " + e.getKey());
                pending[0]--;
            }
        }
        if (pending[0] <= 0 && !released) {
            poolReady = true;
        }
    }

    /**
     * 右手拨弦区拨动。
     */
    public void playPluck(int stringNo, float pluckT, int leftStringNo, float leftPressT) {
        if (!configReady || appContext == null || released) {
            return;
        }
        if (stringNo < 1 || stringNo > 21 || stringNo >= stringTable.size()) {
            return;
        }
        GuzhengStringsData.StringEntry entry = stringTable.get(stringNo);
        if (entry == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (stringNo == lastStringNo && now - lastPlayMs < SAME_STRING_DEBOUNCE_MS) {
            return;
        }
        lastStringNo = stringNo;
        lastPlayMs = now;

        String sample = pickSample(entry, pluckT);
        Integer soundId = soundIds.get(sample);
        if (soundId == null || soundId <= 0) {
            if (!poolReady) {
                return;
            }
            File f = cachedSamples.get(sample);
            if (f == null) {
                return;
            }
            int id = soundPool.load(f.getAbsolutePath(), 1);
            if (id > 0) {
                soundIds.put(sample, id);
                soundId = id;
            } else {
                return;
            }
        }

        float rate = entry.rate;
        if (leftStringNo == stringNo && leftPressT > 0.05f) {
            float semitones = leftPressT * 5f;
            rate *= (float) Math.pow(2, semitones / 12.0);
        }
        rate = clampRate(rate);
        float volume = Math.min(1f, 0.92f + pluckT * 0.06f);

        if (soundPool != null) {
            soundPool.play(soundId, volume, volume, 1, 0, rate);
        }
    }

    private static float clampRate(float rate) {
        if (rate < 0.5f) {
            return 0.5f;
        }
        if (rate > 2.0f) {
            return 2.0f;
        }
        return rate;
    }

    private String pickSample(GuzhengStringsData.StringEntry entry, float pluckT) {
        String chosen = entry.sample;
        if (pluckT > 0.48f) {
            if (entry.sampleMidi == 55 && mid55Sample != null) {
                chosen = mid55Sample;
            } else if (entry.sampleMidi == 67 && mid67Sample != null) {
                chosen = mid67Sample;
            }
        }
        if (entry.rr != null && !entry.rr.isEmpty() && pluckT <= 0.48f) {
            List<String> rr = entry.rr;
            if (rr.size() > 1) {
                return rr.get(random.nextInt(rr.size()));
            }
            return rr.get(0);
        }
        return chosen;
    }

    public boolean isReady() {
        return configReady;
    }

    public boolean isPoolReady() {
        return poolReady;
    }

    public synchronized void release() {
        released = true;
        warming = false;
        if (soundPool != null) {
            try {
                soundPool.autoPause();
            } catch (Exception ignored) {
            }
        }
        releasePoolInternal();
        readyClear();
        Log.i(TAG, "audio released");
    }

    private void readyClear() {
        configReady = false;
        poolReady = false;
        stringTable.clear();
        cachedSamples.clear();
        soundIds.clear();
    }

    private void releasePoolInternal() {
        if (soundPool != null) {
            try {
                soundPool.release();
            } catch (Exception ignored) {
            }
            soundPool = null;
        }
    }

    public String getNoteName(int stringNo) {
        if (stringNo < 1 || stringNo > 21 || stringNo >= stringTable.size()) {
            return "";
        }
        GuzhengStringsData.StringEntry e = stringTable.get(stringNo);
        return e != null && e.note != null ? e.note : "";
    }

    public String formatStringLabel(int stringNo) {
        return String.format(Locale.CHINA, "%d弦 %s", stringNo, getNoteName(stringNo));
    }
}
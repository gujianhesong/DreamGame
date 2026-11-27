package com.game.dream.audio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * 将 assets 中的 FLAC 解压到缓存；不支持的设备上转 WAV 供 MediaPlayer 播放。
 */
final class GuzhengSampleCache {

    private static final String TAG = "GuzhengSampleCache";

    private GuzhengSampleCache() {
    }

    static File resolvePlayableFile(android.content.Context appContext, String assetDir, String sampleName) {
        File dir = new File(appContext.getCacheDir(), assetDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        File flac = new File(dir, sampleName);
        if (!flac.exists() || flac.length() == 0) {
            if (!copyAsset(appContext, assetDir + "/" + sampleName, flac)) {
                return null;
            }
        }
        String wavName = sampleName.replace(".flac", ".wav");
        File wav = new File(dir, wavName);
        if (wav.exists() && wav.length() > 44) {
            return wav;
        }
        if (transcodeToWav(flac, wav)) {
            return wav;
        }
        return flac;
    }

    private static boolean copyAsset(android.content.Context ctx, String assetPath, File out) {
        try (java.io.InputStream in = ctx.getAssets().open(assetPath);
             FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) >= 0) {
                fos.write(buf, 0, n);
            }
            return out.length() > 0;
        } catch (IOException e) {
            Log.e(TAG, "copyAsset " + assetPath, e);
            if (out.exists()) {
                //noinspection ResultOfMethodCallIgnored
                out.delete();
            }
            return false;
        }
    }

    private static boolean transcodeToWav(File src, File wavOut) {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec codec = null;
        try {
            extractor.setDataSource(src.getAbsolutePath());
            int audioTrack = -1;
            MediaFormat format = null;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat f = extractor.getTrackFormat(i);
                String mime = f.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrack = i;
                    format = f;
                    break;
                }
            }
            if (audioTrack < 0 || format == null) {
                return false;
            }
            extractor.selectTrack(audioTrack);
            String mime = format.getString(MediaFormat.KEY_MIME);
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();

            int sampleRate = format.containsKey(MediaFormat.KEY_SAMPLE_RATE)
                    ? format.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 44100;
            int channels = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)
                    ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 2;

            try (FileOutputStream fos = new FileOutputStream(wavOut)) {
                writeWavHeader(fos, 0, sampleRate, channels, 16);
                java.io.ByteArrayOutputStream pcm = new java.io.ByteArrayOutputStream();
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                boolean inputDone = false;
                boolean outputDone = false;
                long timeoutUs = 10000;

                while (!outputDone) {
                    if (!inputDone) {
                        int inIndex = codec.dequeueInputBuffer(timeoutUs);
                        if (inIndex >= 0) {
                            ByteBuffer inBuf = codec.getInputBuffer(inIndex);
                            if (inBuf != null) {
                                inBuf.clear();
                                int sampleSize = extractor.readSampleData(inBuf, 0);
                                if (sampleSize < 0) {
                                    codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                    inputDone = true;
                                } else {
                                    codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                                    extractor.advance();
                                }
                            }
                        }
                    }

                    int outIndex = codec.dequeueOutputBuffer(info, timeoutUs);
                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        continue;
                    }
                    if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat outFmt = codec.getOutputFormat();
                        if (outFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                            sampleRate = outFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                        }
                        if (outFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                            channels = outFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                        }
                        continue;
                    }
                    if (outIndex >= 0) {
                        ByteBuffer outBuf = codec.getOutputBuffer(outIndex);
                        if (outBuf != null && info.size > 0) {
                            byte[] chunk = new byte[info.size];
                            outBuf.position(info.offset);
                            outBuf.limit(info.offset + info.size);
                            outBuf.get(chunk);
                            pcm.write(chunk);
                        }
                        codec.releaseOutputBuffer(outIndex, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true;
                        }
                    }
                }

                byte[] pcmBytes = pcm.toByteArray();
                if (pcmBytes.length == 0) {
                    return false;
                }
                fos.write(pcmBytes);
                fos.flush();
            }

            patchWavHeader(wavOut, wavOut.length() - 44);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "transcodeToWav failed for " + src.getName() + ": " + e.getMessage());
            if (wavOut.exists()) {
                //noinspection ResultOfMethodCallIgnored
                wavOut.delete();
            }
            return false;
        } finally {
            try {
                extractor.release();
            } catch (Exception ignored) {
            }
            if (codec != null) {
                try {
                    codec.stop();
                    codec.release();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void writeWavHeader(FileOutputStream fos, int pcmBytes, int sampleRate, int channels, int bits)
            throws IOException {
        int byteRate = sampleRate * channels * bits / 8;
        byte[] h = new byte[44];
        h[0] = 'R';
        h[1] = 'I';
        h[2] = 'F';
        h[3] = 'F';
        int chunkSize = pcmBytes + 36;
        h[4] = (byte) (chunkSize & 0xff);
        h[5] = (byte) ((chunkSize >> 8) & 0xff);
        h[6] = (byte) ((chunkSize >> 16) & 0xff);
        h[7] = (byte) ((chunkSize >> 24) & 0xff);
        h[8] = 'W';
        h[9] = 'A';
        h[10] = 'V';
        h[11] = 'E';
        h[12] = 'f';
        h[13] = 'm';
        h[14] = 't';
        h[15] = ' ';
        h[16] = 16;
        h[17] = 0;
        h[18] = 0;
        h[19] = 0;
        h[20] = 1;
        h[21] = 0;
        h[22] = (byte) channels;
        h[23] = 0;
        h[24] = (byte) (sampleRate & 0xff);
        h[25] = (byte) ((sampleRate >> 8) & 0xff);
        h[26] = (byte) ((sampleRate >> 16) & 0xff);
        h[27] = (byte) ((sampleRate >> 24) & 0xff);
        h[28] = (byte) (byteRate & 0xff);
        h[29] = (byte) ((byteRate >> 8) & 0xff);
        h[30] = (byte) ((byteRate >> 16) & 0xff);
        h[31] = (byte) ((byteRate >> 24) & 0xff);
        int blockAlign = channels * bits / 8;
        h[32] = (byte) blockAlign;
        h[33] = 0;
        h[34] = (byte) bits;
        h[35] = 0;
        h[36] = 'd';
        h[37] = 'a';
        h[38] = 't';
        h[39] = 'a';
        h[40] = (byte) (pcmBytes & 0xff);
        h[41] = (byte) ((pcmBytes >> 8) & 0xff);
        h[42] = (byte) ((pcmBytes >> 16) & 0xff);
        h[43] = (byte) ((pcmBytes >> 24) & 0xff);
        fos.write(h);
    }

    private static void patchWavHeader(File wav, long pcmSize) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(wav, "rw")) {
            raf.seek(4);
            raf.write(intLE((int) (pcmSize + 36)));
            raf.seek(40);
            raf.write(intLE((int) pcmSize));
        }
    }

    private static byte[] intLE(int v) {
        return new byte[]{(byte) (v & 0xff), (byte) ((v >> 8) & 0xff), (byte) ((v >> 16) & 0xff), (byte) ((v >> 24) & 0xff)};
    }
}
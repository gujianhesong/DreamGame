#define TSF_IMPLEMENTATION
#define TSF_NO_STDIO
#include "tsf.h"

#include <android/log.h>
#include <jni.h>
#include <mutex>
#include <vector>
#include <cstring>

namespace {

constexpr const char *TAG = "KeyboardSynth";
std::mutex gMutex;
tsf *gTsf = nullptr;
int gSampleRate = 44100;
bool gReady = false;

} // namespace

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeLoadSoundFont(JNIEnv *env, jclass, jbyteArray sf2Bytes) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gTsf != nullptr) {
        tsf_close(gTsf);
        gTsf = nullptr;
        gReady = false;
    }
    jsize len = env->GetArrayLength(sf2Bytes);
    if (len <= 0) {
        LOGE("empty soundfont");
        return JNI_FALSE;
    }
    std::vector<unsigned char> buf(static_cast<size_t>(len));
    env->GetByteArrayRegion(sf2Bytes, 0, len, reinterpret_cast<jbyte *>(buf.data()));
    gTsf = tsf_load_memory(buf.data(), static_cast<int>(buf.size()));
    if (gTsf == nullptr) {
        LOGE("tsf_load_memory failed");
        return JNI_FALSE;
    }
    tsf_set_output(gTsf, TSF_STEREO_INTERLEAVED, gSampleRate, -3.0f);
    tsf_channel_set_presetnumber(gTsf, 0, 0, 0);
    gReady = true;
    LOGI("soundfont loaded, size=%d", len);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeSetSampleRate(JNIEnv *, jclass, jint sampleRate) {
    std::lock_guard<std::mutex> lock(gMutex);
    gSampleRate = sampleRate > 0 ? sampleRate : 44100;
    if (gTsf != nullptr) {
        tsf_set_output(gTsf, TSF_STEREO_INTERLEAVED, gSampleRate, -3.0f);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeSetProgram(JNIEnv *, jclass, jint program) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gTsf == nullptr) {
        return;
    }
    if (!tsf_channel_set_presetnumber(gTsf, 0, program, 0)) {
        int idx = tsf_get_presetindex(gTsf, 0, program);
        if (idx >= 0) {
            tsf_channel_set_presetindex(gTsf, 0, idx);
        } else {
            LOGE("GM program %d not found in soundfont", program);
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeNoteOn(JNIEnv *, jclass, jint midi, jint velocity) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (!gReady || gTsf == nullptr) {
        return;
    }
    float vel = velocity / 127.0f;
    if (vel < 0.05f) {
        vel = 0.8f;
    }
    tsf_channel_note_on(gTsf, 0, midi, vel);
}

extern "C" JNIEXPORT void JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeNoteOff(JNIEnv *, jclass, jint midi) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gTsf == nullptr) {
        return;
    }
    tsf_channel_note_off(gTsf, 0, midi);
}

extern "C" JNIEXPORT void JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeAllNotesOff(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gTsf == nullptr) {
        return;
    }
    tsf_channel_note_off_all(gTsf, 0);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeRender(JNIEnv *env, jclass, jshortArray out, jint frameCount) {
    if (frameCount <= 0 || frameCount > 512) {
        return 0;
    }
    const int samples = frameCount * 2;
    std::vector<short> pcm(static_cast<size_t>(samples));
    {
        std::lock_guard<std::mutex> lock(gMutex);
        if (!gReady || gTsf == nullptr) {
            return 0;
        }
        std::memset(pcm.data(), 0, static_cast<size_t>(samples) * sizeof(short));
        tsf_render_short(gTsf, pcm.data(), frameCount, 0);
    }
    jshort *dst = env->GetShortArrayElements(out, nullptr);
    if (dst == nullptr) {
        return 0;
    }
    std::memcpy(dst, pcm.data(), static_cast<size_t>(samples) * sizeof(short));
    env->ReleaseShortArrayElements(out, dst, 0);
    return frameCount;
}

extern "C" JNIEXPORT void JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeRelease(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gTsf != nullptr) {
        tsf_close(gTsf);
        gTsf = nullptr;
    }
    gReady = false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeIsReady(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    return gReady ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeGetPresetCount(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gTsf == nullptr) {
        return 0;
    }
    return tsf_get_presetcount(gTsf);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_game_dream_audio_Sf2MidiEngine_nativeGetActiveVoices(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (!gReady || gTsf == nullptr) {
        return 0;
    }
    return tsf_active_voice_count(gTsf);
}
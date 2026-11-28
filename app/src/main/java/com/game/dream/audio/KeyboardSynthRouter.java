package com.game.dream.audio;

import android.content.Context;

/** 蔚莱琴姬发声：内置 SF2 合成。 */
public class KeyboardSynthRouter {

    private static KeyboardSynthRouter instance;

    public static synchronized KeyboardSynthRouter getInstance() {
        if (instance == null) {
            instance = new KeyboardSynthRouter();
        }
        return instance;
    }

    public boolean isReady() {
        return Sf2MidiEngine.getInstance().isReady();
    }

    public String getStatusLine() {
        if (Sf2MidiEngine.getInstance().isPlaceholderFont()) {
            return "内置 SF2（占位音色，可替换 assets/keyboard/gm.sf2）";
        }
        return "SF2 音色包";
    }

    public static String getLastLoadError() {
        return Sf2MidiEngine.getLastLoadError();
    }

    public void connectAsync(Context context, Runnable onReady, Runnable onFailed) {
        release();
        Sf2MidiEngine.getInstance().loadAsync(context.getApplicationContext(), onReady, onFailed);
    }

    public void setProgram(int program) {
        Sf2MidiEngine.getInstance().setProgram(program);
    }

    public void noteOn(int midi, int velocity) {
        Sf2MidiEngine.getInstance().noteOn(midi, velocity);
    }

    public void noteOff(int midi) {
        Sf2MidiEngine.getInstance().noteOff(midi);
    }

    public void allNotesOff() {
        Sf2MidiEngine.getInstance().allNotesOff();
    }

    public void release() {
        Sf2MidiEngine.getInstance().release();
    }
}
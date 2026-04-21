package com.game.dream.utils;

public class Utils {

    /**
     * 获取波动值
     *
     * @param value
     * @param waveRatio
     * @return
     */
    public static int getWaveValue(int value, float waveRatio) {
        int newValue = (int) (value * ((1 - waveRatio) + Math.random() * waveRatio * 2));
        return newValue;
    }

}

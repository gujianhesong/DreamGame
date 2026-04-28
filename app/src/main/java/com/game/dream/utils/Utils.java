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

    public static float getRangeValue(float minValue, float maxValue) {
        float newMinValue = Math.min(minValue, maxValue);
        float newMaxValue = Math.min(minValue, maxValue);
        float value = (float) (newMinValue + (newMaxValue - newMinValue) * Math.random());
        return value;
    }

}

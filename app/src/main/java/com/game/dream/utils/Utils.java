package com.game.dream.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

    /**
     * 获取波动值
     *
     * @param value
     * @param waveRatio
     * @return
     */
    public static int getWaveValueInt(int value, float waveRatio) {
        int newValue = (int) (value * (1 - waveRatio) + Math.random() * waveRatio * 2);
        return newValue;
    }

    /**
     * 获取波动值
     *
     * @param value
     * @param waveRatio
     * @return
     */
    public static float getWaveValueFloat(int value, float waveRatio) {
        float newValue = (float) (value * (1 - waveRatio) + Math.random() * waveRatio * 2);
        return newValue;
    }

    public static float getRangeValue(float minValue, float maxValue) {
        float newMinValue = Math.min(minValue, maxValue);
        float newMaxValue = Math.max(minValue, maxValue);
        float value = (float) (newMinValue + (newMaxValue - newMinValue) * Math.random());
        return value;
    }

    public static <T> T getRandomItem(T[] items) {
        if (items == null || items.length == 0) {
            return null;
        }
        if (items.length == 1) {
            return items[0];
        }

        int randomIndex = (int) (Math.random() * items.length);
        return items[randomIndex];
    }

    public static <T> T getRandomItem(List<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }

        int randomIndex = (int) (Math.random() * items.size());
        return items.get(randomIndex);
    }

    /**
     * 从列表中随机获取指定数量的元素
     */
    public static <T> List<T> getRandomItems(List<T> originalList, int count) {
        if (count > originalList.size()) {
            throw new IllegalArgumentException("请求的元素数量大于列表中的元素数量");
        }

        List<T> copyList = new ArrayList<>(originalList);
        Collections.shuffle(copyList); // 打乱列表顺序

        return copyList.subList(0, count); // 返回前count个元素
    }

    public static String format100Ratio(float ratio) {
        return (int)(ratio * 10000) / 100f + "%";
    }
}

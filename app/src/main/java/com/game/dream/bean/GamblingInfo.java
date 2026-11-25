package com.game.dream.bean;

/**
 * 赌坊：单日盈亏统计（与金陵游戏日同步）。
 */
public class GamblingInfo {

    private int recordedGameDay;
    /** 当日累计净盈亏（银），负数为净输 */
    private long dailyNetSilver;

    public int getRecordedGameDay() {
        return recordedGameDay;
    }

    public void setRecordedGameDay(int recordedGameDay) {
        this.recordedGameDay = recordedGameDay;
    }

    public long getDailyNetSilver() {
        return dailyNetSilver;
    }

    public void setDailyNetSilver(long dailyNetSilver) {
        this.dailyNetSilver = dailyNetSilver;
    }
}
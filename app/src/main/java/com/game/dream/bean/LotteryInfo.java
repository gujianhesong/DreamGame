package com.game.dream.bean;

/**
 * 金陵彩：每 3 分钟一期，号码 1–10000。
 */
public class LotteryInfo {

    /** 当期已购号码，0 表示未购 */
    private int ticketNumber;
    /** 参与哪一期开奖 */
    private int ticketDrawRound;

    /** 当前售卖/开奖期号（尚未开奖的这一期） */
    private int currentDrawRound;

    /** 距本期开奖已累计毫秒（存档保留倒计时进度） */
    private long periodElapsedMs;

    private int lastWinningNumber;
    private int lastDrawRound;
    /** 已结算到哪一期（防重复发奖） */
    private int lastSettledDrawRound;

    private int lastPlayerNumber;
    private long lastPrizeSilver;

    public int getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public int getTicketDrawRound() {
        return ticketDrawRound;
    }

    public void setTicketDrawRound(int ticketDrawRound) {
        this.ticketDrawRound = ticketDrawRound;
    }

    public int getCurrentDrawRound() {
        return currentDrawRound;
    }

    public void setCurrentDrawRound(int currentDrawRound) {
        this.currentDrawRound = currentDrawRound;
    }

    public long getPeriodElapsedMs() {
        return periodElapsedMs;
    }

    public void setPeriodElapsedMs(long periodElapsedMs) {
        this.periodElapsedMs = Math.max(0, periodElapsedMs);
    }

    public int getLastWinningNumber() {
        return lastWinningNumber;
    }

    public void setLastWinningNumber(int lastWinningNumber) {
        this.lastWinningNumber = lastWinningNumber;
    }

    public int getLastDrawRound() {
        return lastDrawRound;
    }

    public void setLastDrawRound(int lastDrawRound) {
        this.lastDrawRound = lastDrawRound;
    }

    public int getLastSettledDrawRound() {
        return lastSettledDrawRound;
    }

    public void setLastSettledDrawRound(int lastSettledDrawRound) {
        this.lastSettledDrawRound = lastSettledDrawRound;
    }

    public int getLastPlayerNumber() {
        return lastPlayerNumber;
    }

    public void setLastPlayerNumber(int lastPlayerNumber) {
        this.lastPlayerNumber = lastPlayerNumber;
    }

    public long getLastPrizeSilver() {
        return lastPrizeSilver;
    }

    public void setLastPrizeSilver(long lastPrizeSilver) {
        this.lastPrizeSilver = lastPrizeSilver;
    }
}
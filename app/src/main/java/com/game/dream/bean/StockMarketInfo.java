package com.game.dream.bean;

/**
 * 存档：金陵股簿持仓与市价
 */
public class StockMarketInfo {

    public static final int STOCK_COUNT = 9;
    public static final int HISTORY_DAYS = 7;

    private int gameDay;
    private int lastDividendDay;
    private int[] prices;
    private int[] prevPrices;
    /** 当日开盘价（0.1 银），每游戏日切换时刷新 */
    private int[] dayOpenPrices;
    private int[][] priceHistory;
    private int[] shares;
    private int[] lockedShares;
    private long[] totalCost;
    private String marketNews;

    public StockMarketInfo() {
        prices = new int[STOCK_COUNT];
        prevPrices = new int[STOCK_COUNT];
        dayOpenPrices = new int[STOCK_COUNT];
        priceHistory = new int[STOCK_COUNT][HISTORY_DAYS];
        shares = new int[STOCK_COUNT];
        lockedShares = new int[STOCK_COUNT];
        totalCost = new long[STOCK_COUNT];
    }

    public int getGameDay() {
        return gameDay;
    }

    public void setGameDay(int gameDay) {
        this.gameDay = gameDay;
    }

    public int getLastDividendDay() {
        return lastDividendDay;
    }

    public void setLastDividendDay(int lastDividendDay) {
        this.lastDividendDay = lastDividendDay;
    }

    public int[] getPrices() {
        if (prices == null) {
            prices = new int[STOCK_COUNT];
        }
        return prices;
    }

    public void setPrices(int[] prices) {
        this.prices = prices;
    }

    public int[] getPrevPrices() {
        if (prevPrices == null) {
            prevPrices = new int[STOCK_COUNT];
        }
        return prevPrices;
    }

    public void setPrevPrices(int[] prevPrices) {
        this.prevPrices = prevPrices;
    }

    public int[] getDayOpenPrices() {
        if (dayOpenPrices == null) {
            dayOpenPrices = new int[STOCK_COUNT];
        }
        return dayOpenPrices;
    }

    public void setDayOpenPrices(int[] dayOpenPrices) {
        this.dayOpenPrices = dayOpenPrices;
    }

    public int[][] getPriceHistory() {
        if (priceHistory == null) {
            priceHistory = new int[STOCK_COUNT][HISTORY_DAYS];
        }
        return priceHistory;
    }

    public void setPriceHistory(int[][] priceHistory) {
        this.priceHistory = priceHistory;
    }

    public int[] getShares() {
        if (shares == null) {
            shares = new int[STOCK_COUNT];
        }
        return shares;
    }

    public void setShares(int[] shares) {
        this.shares = shares;
    }

    public int[] getLockedShares() {
        if (lockedShares == null) {
            lockedShares = new int[STOCK_COUNT];
        }
        return lockedShares;
    }

    public void setLockedShares(int[] lockedShares) {
        this.lockedShares = lockedShares;
    }

    public long[] getTotalCost() {
        if (totalCost == null) {
            totalCost = new long[STOCK_COUNT];
        }
        return totalCost;
    }

    public void setTotalCost(long[] totalCost) {
        this.totalCost = totalCost;
    }

    public String getMarketNews() {
        return marketNews;
    }

    public void setMarketNews(String marketNews) {
        this.marketNews = marketNews;
    }
}
package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SaveInfo;
import com.game.dream.bean.StockMarketInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 金陵股簿：九家字号合股，实时更价（约 3 秒）、游戏日分红。
 */
public class StockMarketSystem {

    public static final int STOCK_ZUI_XIAN = 0;
    public static final int STOCK_HUI_CHUN = 1;
    public static final int STOCK_OU_YE = 2;
    public static final int STOCK_DAO_XIANG = 3;
    public static final int STOCK_DONG_CHUAN = 4;
    public static final int STOCK_XI_YU = 5;
    public static final int STOCK_NAN_JIANG = 6;
    public static final int STOCK_LONG_QUAN = 7;
    public static final int STOCK_CUI_CAN = 8;

    public static final int DIVIDEND_EVERY_DAYS = 7;
    public static final int MIN_LEVEL = 3;
    private static final float TICK_MOVE_CAP = 0.022f;
    public static final long PRICE_UPDATE_INTERVAL_MS = 3000;

    private static StockMarketSystem instance;

    private final Random random = new Random();
    private float lastTrackedGameTime = -1f;
    private long priceTickElapsedMs = 0;

    private static final String[] STOCK_NAMES = {
            "醉仙楼",
            "回春堂",
            "欧冶铁铺",
            "稻香联庄",
            "东城船行",
            "西域商行",
            "南疆商行",
            "龙泉剑庄",
            "璀璨宝斋"
    };

    private static final String[] STOCK_BLURBS = {
            "北市旗亭，客满则利厚。",
            "灵草药贵，伤人多则市旺。",
            "铁料出入，闻风则动。",
            "四乡收粮，收成好则稳。",
            "东门通海，漕运顺则涨。",
            "丝路驼铃，货通四方则旺。",
            "瘴林边贸，奇货入市则热。",
            "神兵订单满，剑庄价昂。",
            "珠宝随富商起落，宝斋见晴雨。"
    };

    /** 股价的 0.1 银单位（82.0 → 820） */
    private static final int[] BASE_PRICES = {820, 1050, 680, 580, 920, 880, 760, 980, 1120};
    private static final int MIN_PRICE_TENTHS = 120;

    /** 每股每旬分红（固定银两） */
    private static final int[] DIVIDEND_PER_SHARE = {1, 2, 1, 1, 2, 2, 1, 2, 1};

    private static final String[][] NEWS_LINES = {
            {"醉仙楼张灯结彩，估市略扬。", "北市淡季，旗亭股价受压。", "贵客临门，酒肆姻火热。"},
            {"药草涨价，回春堂掌柜眉开眼笑。", "梅雨少病，药铺利薄。", "郎中忙活，堂口股息可期。"},
            {"铁匠订单满棚，铁价走强。", "矿脉迟开，欧冶股价低迷。", "兵甲修缮增多，铁铺受益。"},
            {"稻香屯新禾入仓，联庄利稳。", "欠收传言，粮价起伏。", "乡绅合议收粮，市价暂平。"},
            {"海风顺，东城船行载货急。", "浪大船少，船行一时受阻。", "东海货紧要，估市偏暖。"},
            {"西域商队抵金陵，商行利厚。", "丝路险阻，西域商行承压。", "胡商聚居，估市略暖。"},
            {"南疆犀革走俏，商行跟进。", "瘴气阻道，南疆贩路迟滞。", "边市开集，南疆商行受益。"},
            {"边关征订刀剑，龙泉价涨。", "淡季少客，剑庄稍落。", "名匠收徒，剑庄商誉稳。"},
            {"贵妇新制首饰，宝斋门庭若市。", "商旅稀少，珠宝欲购还休。", "贡品样入坊，宝斋行市活。"}
    };

    public static class StockQuote {
        public final int id;
        public final String name;
        public final String blurb;
        /** 当前价（0.1 银） */
        public final int price;
        /** 当日开盘价（0.1 银） */
        public final int dayOpenPrice;
        public final int shares;
        public final int sellableShares;
        public final long avgCost;
        public final long totalCost;

        StockQuote(int id, String name, String blurb, int price, int dayOpenPrice,
                   int shares, int sellableShares, long avgCost, long totalCost) {
            this.id = id;
            this.name = name;
            this.blurb = blurb;
            this.price = price;
            this.dayOpenPrice = dayOpenPrice;
            this.shares = shares;
            this.sellableShares = sellableShares;
            this.avgCost = avgCost;
            this.totalCost = totalCost;
        }

        public String getPriceText() {
            return formatPriceTenths(price);
        }

        public long getMarketValue() {
            return silverForShares(price, shares);
        }

        public long getUnrealizedProfit() {
            if (shares <= 0) {
                return 0;
            }
            return getMarketValue() - totalCost;
        }

        /** 相对当日开盘价的涨跌幅（%） */
        public float getDayChangePercent() {
            if (dayOpenPrice <= 0) {
                return 0;
            }
            return (price - dayOpenPrice) * 100f / dayOpenPrice;
        }
    }

    public static String formatPriceTenths(int priceTenths) {
        return String.format(Locale.ROOT, "%.1f", priceTenths / 10.0);
    }

    public static long silverForShares(int priceTenths, int shareQty) {
        if (shareQty <= 0 || priceTenths <= 0) {
            return 0;
        }
        return Math.round(priceTenths * (double) shareQty / 10.0);
    }

    public static StockMarketSystem getInstance() {
        if (instance == null) {
            instance = new StockMarketSystem();
        }
        return instance;
    }

    private StockMarketInfo getInfo() {
        SaveInfo save = SaveSystem.getInstance().getSaveInfo();
        if (save == null) {
            return null;
        }
        if (save.getStockMarketInfo() == null) {
            save.setStockMarketInfo(new StockMarketInfo());
        }
        StockMarketInfo info = save.getStockMarketInfo();
        ensureMarketInitialized(info);
        return info;
    }

    public void ensureMarketInitialized(StockMarketInfo info) {
        if (info == null) {
            return;
        }
        migrateStockArraysIfNeeded(info);
        migratePricesToTenthsIfNeeded(info);
        int[] prices = info.getPrices();
        int[] prev = info.getPrevPrices();
        int[][] hist = info.getPriceHistory();
        boolean fresh = prices[STOCK_ZUI_XIAN] <= 0;
        if (fresh) {
            for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
                initStockSlot(prices, prev, hist, i);
            }
            if (info.getMarketNews() == null || info.getMarketNews().isEmpty()) {
                info.setMarketNews("市道开张，九家字号并录于簿。");
            }
        } else {
            for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
                if (prices[i] <= 0) {
                    initStockSlot(prices, prev, hist, i);
                }
            }
        }
        ensureDayOpenPrices(info);
    }

    private void initStockSlot(int[] prices, int[] prev, int[][] hist, int i) {
        prices[i] = BASE_PRICES[i];
        prev[i] = BASE_PRICES[i];
        for (int d = 0; d < StockMarketInfo.HISTORY_DAYS; d++) {
            hist[i][d] = BASE_PRICES[i];
        }
    }

    /** 旧存档整银股价升级为一位小数（×10 存分位）。 */
    private void migratePricesToTenthsIfNeeded(StockMarketInfo info) {
        int[] prices = info.getPrices();
        if (prices.length == 0 || prices[STOCK_ZUI_XIAN] <= 0 || prices[STOCK_ZUI_XIAN] >= 200) {
            return;
        }
        int[] prev = info.getPrevPrices();
        int[] dayOpen = info.getDayOpenPrices();
        int[][] hist = info.getPriceHistory();
        for (int i = 0; i < prices.length && i < StockMarketInfo.STOCK_COUNT; i++) {
            if (prices[i] > 0) {
                prices[i] *= 10;
            }
            if (prev[i] > 0) {
                prev[i] *= 10;
            }
            if (dayOpen[i] > 0) {
                dayOpen[i] *= 10;
            }
            for (int d = 0; d < StockMarketInfo.HISTORY_DAYS; d++) {
                if (hist[i][d] > 0) {
                    hist[i][d] *= 10;
                }
            }
        }
    }

    private void ensureDayOpenPrices(StockMarketInfo info) {
        int[] open = info.getDayOpenPrices();
        int[] prices = info.getPrices();
        for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
            if (open[i] <= 0 && prices[i] > 0) {
                open[i] = prices[i];
            }
        }
    }

    private void snapshotDayOpenPrices(StockMarketInfo info) {
        int[] open = info.getDayOpenPrices();
        int[] prices = info.getPrices();
        for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
            if (prices[i] > 0) {
                open[i] = prices[i];
            }
        }
    }

    /** 旧存档股票数量不足时扩容数组并挂牌新股。 */
    private void migrateStockArraysIfNeeded(StockMarketInfo info) {
        int n = StockMarketInfo.STOCK_COUNT;
        int[] prices = info.getPrices();
        if (prices.length >= n) {
            return;
        }
        int oldLen = prices.length;
        prices = Arrays.copyOf(prices, n);
        int[] prev = Arrays.copyOf(info.getPrevPrices(), n);
        int[] dayOpen = Arrays.copyOf(info.getDayOpenPrices(), n);
        int[] shares = Arrays.copyOf(info.getShares(), n);
        int[] locked = Arrays.copyOf(info.getLockedShares(), n);
        long[] cost = Arrays.copyOf(info.getTotalCost(), n);
        int[][] hist = info.getPriceHistory();
        int[][] newHist = new int[n][StockMarketInfo.HISTORY_DAYS];
        for (int i = 0; i < oldLen && i < hist.length; i++) {
            System.arraycopy(hist[i], 0, newHist[i], 0, StockMarketInfo.HISTORY_DAYS);
        }
        for (int i = oldLen; i < n; i++) {
            initStockSlot(prices, prev, newHist, i);
        }
        info.setPrices(prices);
        info.setPrevPrices(prev);
        info.setDayOpenPrices(dayOpen);
        info.setShares(shares);
        info.setLockedShares(locked);
        info.setTotalCost(cost);
        info.setPriceHistory(newHist);
    }

    public boolean canUseMarket() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role == null) {
            return false;
        }
        return role.getLevel() >= MIN_LEVEL
                || role.getBankMoney() > 0
                || role.getMoney() >= 500;
    }

    public String getUnlockHint() {
        return "未满" + MIN_LEVEL + "级且未在钱庄存银者，恕不接股契。可先修炼或存钱再来。";
    }

    public List<StockQuote> getQuotes() {
        StockMarketInfo info = getInfo();
        List<StockQuote> list = new ArrayList<>();
        if (info == null) {
            return list;
        }
        int[] prices = info.getPrices();
        int[] dayOpen = info.getDayOpenPrices();
        int[] shares = info.getShares();
        long[] cost = info.getTotalCost();
        for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
            long avg = shares[i] > 0 ? cost[i] / shares[i] : 0;
            list.add(new StockQuote(i, STOCK_NAMES[i], STOCK_BLURBS[i],
                    prices[i], dayOpen[i], shares[i], shares[i], avg, cost[i]));
        }
        return list;
    }

    public int getGameDay() {
        StockMarketInfo info = getInfo();
        return info != null ? info.getGameDay() : 0;
    }

    public String getMarketNews() {
        StockMarketInfo info = getInfo();
        if (info == null || info.getMarketNews() == null) {
            return "";
        }
        return info.getMarketNews();
    }

    public int[][] getPriceHistory() {
        StockMarketInfo info = getInfo();
        return info != null ? info.getPriceHistory() : null;
    }

    /** 每约 3 秒刷新一次市价（游戏运行中持续累加）。 */
    public void tickRealtime(long deltaTimeMs) {
        if (deltaTimeMs <= 0) {
            return;
        }
        priceTickElapsedMs += deltaTimeMs;
        while (priceTickElapsedMs >= PRICE_UPDATE_INTERVAL_MS) {
            priceTickElapsedMs -= PRICE_UPDATE_INTERVAL_MS;
            StockMarketInfo info = getInfo();
            if (info != null) {
                rollPrices(info);
            }
        }
    }

    /**
     * 与昼夜同步：跨过一日（gameTime 回绕）时仅累计游戏日并检查分红。
     */
    public void tickDayNight(DayNightCycle cycle) {
        if (cycle == null) {
            return;
        }
        float t = cycle.getGameTimeSeconds();
        if (lastTrackedGameTime < 0) {
            lastTrackedGameTime = t;
            return;
        }
        if (t < lastTrackedGameTime - 3600f) {
            onNewGameDay();
        }
        lastTrackedGameTime = t;
    }

    public void resetDayTracking() {
        lastTrackedGameTime = -1f;
        priceTickElapsedMs = 0;
    }

    private void onNewGameDay() {
        StockMarketInfo info = getInfo();
        if (info == null) {
            return;
        }
        info.setGameDay(info.getGameDay() + 1);
        snapshotDayOpenPrices(info);
        payDividendsIfDue(info);
    }

    private void rollPrices(StockMarketInfo info) {
        int[] prices = info.getPrices();
        int[] prev = info.getPrevPrices();
        int[][] hist = info.getPriceHistory();

        for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
            prev[i] = prices[i];
            float bias = (random.nextFloat() - 0.5f) * 0.04f;
            float move = (random.nextFloat() * 0.06f - 0.03f) + bias;
            if (move > TICK_MOVE_CAP) {
                move = TICK_MOVE_CAP;
            } else if (move < -TICK_MOVE_CAP) {
                move = -TICK_MOVE_CAP;
            }
            float priceNow = prices[i] / 10f;
            int next = Math.round(priceNow * (1f + move) * 10f);
            next = Math.max(MIN_PRICE_TENTHS, next);
            prices[i] = next;

            shiftHistory(hist[i], next);
            String[] lines = NEWS_LINES[i];
            info.setMarketNews(lines[random.nextInt(lines.length)]);
        }
    }

    private void shiftHistory(int[] ring, int newPrice) {
        for (int d = StockMarketInfo.HISTORY_DAYS - 1; d > 0; d--) {
            ring[d] = ring[d - 1];
        }
        ring[0] = newPrice;
    }

    private void payDividendsIfDue(StockMarketInfo info) {
        int day = info.getGameDay();
        if (day <= 0 || day - info.getLastDividendDay() < DIVIDEND_EVERY_DAYS) {
            return;
        }
        info.setLastDividendDay(day);
        int[] shares = info.getShares();
        long total = 0;
        for (int i = 0; i < StockMarketInfo.STOCK_COUNT; i++) {
            if (shares[i] > 0) {
                total += (long) shares[i] * DIVIDEND_PER_SHARE[i];
            }
        }
        if (total <= 0) {
            return;
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        role.setBankMoney(role.getBankMoney() + total);
        GameEngine engine = GameEngine.getInstance();
        if (engine != null) {
            engine.showCenterToast("本旬分利 +" + total + " 银，已入钱庄。");
            engine.addMessage("股簿分利 +" + total + " 银", com.game.dream.panel.MessagePanel.MessageType.MONEY);
        }
    }

    public long getSpendableSilver() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        return role.getMoney() + role.getBankMoney();
    }

    private boolean withdrawSilver(long amount) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (amount > role.getMoney() + role.getBankMoney()) {
            return false;
        }
        long fromBank = Math.min(role.getBankMoney(), amount);
        role.setBankMoney(role.getBankMoney() - fromBank);
        long rest = amount - fromBank;
        if (rest > 0) {
            role.setMoney(role.getMoney() - rest);
        }
        return true;
    }

    private void depositSilver(long amount) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        role.setBankMoney(role.getBankMoney() + amount);
    }

    public String tryBuy(int stockId, int qty) {
        if (qty <= 0 || stockId < 0 || stockId >= StockMarketInfo.STOCK_COUNT) {
            return "股数有误。";
        }
        if (!canUseMarket()) {
            return getUnlockHint();
        }
        StockMarketInfo info = getInfo();
        int priceTenths = info.getPrices()[stockId];
        long cost = silverForShares(priceTenths, qty);
        if (!withdrawSilver(cost)) {
            return "银两不足。共需 " + cost + " 银（市价 " + formatPriceTenths(priceTenths)
                    + " × " + qty + "）。";
        }
        int[] shares = info.getShares();
        long[] totalCost = info.getTotalCost();
        shares[stockId] += qty;
        totalCost[stockId] += cost;
        return "落契成功：购入 " + STOCK_NAMES[stockId] + " " + qty + " 股，耗银 " + cost + "。";
    }

    public String trySell(int stockId, int qty) {
        if (qty <= 0 || stockId < 0 || stockId >= StockMarketInfo.STOCK_COUNT) {
            return "股数有误。";
        }
        StockMarketInfo info = getInfo();
        int[] shares = info.getShares();
        if (qty > shares[stockId]) {
            return "持股不足。";
        }
        int priceTenths = info.getPrices()[stockId];
        long gain = silverForShares(priceTenths, qty);
        long[] totalCost = info.getTotalCost();
        if (shares[stockId] > 0) {
            long costSlice = totalCost[stockId] * qty / shares[stockId];
            totalCost[stockId] -= costSlice;
        }
        shares[stockId] -= qty;
        depositSilver(gain);
        return "出让 " + STOCK_NAMES[stockId] + " " + qty + " 股，得银 " + gain + "，已入钱庄。";
    }
}
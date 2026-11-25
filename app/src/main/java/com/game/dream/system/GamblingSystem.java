package com.game.dream.system;

import com.game.dream.bean.GamblingInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SaveInfo;

/**
 * 如意赌坊：筹码档位、钱庄扣款、单日止损。
 */
public class GamblingSystem {

    public static final int STAKE_LEISURE = 500;
    public static final int STAKE_NORMAL = 2_000;
    public static final int STAKE_HIGH = 10_000;

    public static final int MIN_LEVEL_HIGH_ROLLER = 15;
    public static final long DAILY_MAX_NET_LOSS = 50_000;

    private static GamblingSystem instance;

    public static GamblingSystem getInstance() {
        if (instance == null) {
            instance = new GamblingSystem();
        }
        return instance;
    }

    private GamblingInfo getInfo() {
        SaveInfo save = SaveSystem.getInstance().getSaveInfo();
        if (save == null) {
            return null;
        }
        if (save.getGamblingInfo() == null) {
            save.setGamblingInfo(new GamblingInfo());
        }
        GamblingInfo info = save.getGamblingInfo();
        syncGameDay(info);
        return info;
    }

    private void syncGameDay(GamblingInfo info) {
        int day = StockMarketSystem.getInstance().getGameDay();
        if (info.getRecordedGameDay() != day) {
            info.setRecordedGameDay(day);
            info.setDailyNetSilver(0);
        }
    }

    public boolean isDailyLossCapped() {
        GamblingInfo info = getInfo();
        if (info == null) {
            return false;
        }
        return info.getDailyNetSilver() <= -DAILY_MAX_NET_LOSS;
    }

    /** 再输 stake 是否会突破当日止损 */
    public boolean wouldExceedDailyLossCap(int stake) {
        GamblingInfo info = getInfo();
        if (info == null) {
            return false;
        }
        return info.getDailyNetSilver() - stake < -DAILY_MAX_NET_LOSS;
    }

    public long getDailyNetSilver() {
        GamblingInfo info = getInfo();
        return info != null ? info.getDailyNetSilver() : 0;
    }

    public long getSpendableSilver() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        return role.getMoney() + role.getBankMoney();
    }

    public boolean canAfford(int stake) {
        return stake > 0 && getSpendableSilver() >= stake;
    }

    public boolean canUseHighRoller() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        return role != null && role.getLevel() >= MIN_LEVEL_HIGH_ROLLER;
    }

    /**
     * 先扣身上，不足再扣钱庄。
     */
    public boolean withdrawStake(int stake) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (stake > role.getMoney() + role.getBankMoney()) {
            return false;
        }
        long fromBank = Math.min(role.getBankMoney(), stake);
        role.setBankMoney(role.getBankMoney() - fromBank);
        long rest = stake - fromBank;
        if (rest > 0) {
            role.setMoney(role.getMoney() - rest);
        }
        return true;
    }

    public void depositWinnings(long amount) {
        if (amount <= 0) {
            return;
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        role.setMoney(role.getMoney() + amount);
    }

    /** 记录本局净盈亏（相对开局前）。 */
    public void recordNetResult(long netChange) {
        GamblingInfo info = getInfo();
        if (info == null) {
            return;
        }
        info.setDailyNetSilver(info.getDailyNetSilver() + netChange);
    }
}
package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.bean.LotteryInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SaveInfo;

import java.util.Locale;
import java.util.Random;

/**
 * 金陵彩：1000 银一注，1–10000 随机号；每 3 分钟烟花开奖一期。
 */
public class LotterySystem {

    public static final int TICKET_PRICE = 1_000;
    public static final int NUMBER_MIN = 1;
    public static final int NUMBER_MAX = 10_000;
    public static final long DRAW_INTERVAL_MS = 180_000L;

    public static final long PRIZE_EXACT = 8_000_000L;
    public static final long PRIZE_WITHIN_10 = 10_000L;
    public static final long PRIZE_WITHIN_100 = 500L;

    private static LotterySystem instance;

    private final Random random = new Random();

    public static LotterySystem getInstance() {
        if (instance == null) {
            instance = new LotterySystem();
        }
        return instance;
    }

    private LotteryInfo getInfo() {
        SaveInfo save = SaveSystem.getInstance().getSaveInfo();
        if (save == null) {
            return null;
        }
        if (save.getLotteryInfo() == null) {
            save.setLotteryInfo(new LotteryInfo());
        }
        LotteryInfo info = save.getLotteryInfo();
        ensureDrawRound(info);
        return info;
    }

    private void ensureDrawRound(LotteryInfo info) {
        if (info.getCurrentDrawRound() <= 0) {
            info.setCurrentDrawRound(1);
        }
    }

    public int getCurrentDrawRound() {
        LotteryInfo info = getInfo();
        return info != null ? info.getCurrentDrawRound() : 1;
    }

    public long getMillisUntilDraw() {
        LotteryInfo info = getInfo();
        if (info == null) {
            return DRAW_INTERVAL_MS;
        }
        long left = DRAW_INTERVAL_MS - info.getPeriodElapsedMs();
        return left > 0 ? left : 0;
    }

    public int getSecondsUntilDraw() {
        return (int) ((getMillisUntilDraw() + 999) / 1000);
    }

    public boolean hasTicketForCurrentPeriod() {
        LotteryInfo info = getInfo();
        if (info == null || info.getTicketNumber() <= 0) {
            return false;
        }
        return info.getTicketDrawRound() == info.getCurrentDrawRound();
    }

    public int getCurrentPeriodTicketNumber() {
        LotteryInfo info = getInfo();
        if (info == null || !hasTicketForCurrentPeriod()) {
            return 0;
        }
        return info.getTicketNumber();
    }

    public long getSpendableSilver() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role == null) {
            return 0;
        }
        return role.getMoney() + role.getBankMoney();
    }

    public boolean canAffordTicket() {
        return getSpendableSilver() >= TICKET_PRICE;
    }

    private boolean withdrawSilver(int amount) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role == null || amount > role.getMoney() + role.getBankMoney()) {
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

    private void depositPrize(long amount) {
        if (amount <= 0) {
            return;
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role != null) {
            role.setMoney(role.getMoney() + amount);
        }
    }

    /**
     * @return 新号码；0 表示失败
     */
    public int buyTicket() {
        LotteryInfo info = getInfo();
        if (info == null) {
            return 0;
        }
        if (hasTicketForCurrentPeriod()) {
            return 0;
        }
        if (!canAffordTicket()) {
            return 0;
        }
        if (!withdrawSilver(TICKET_PRICE)) {
            return 0;
        }
        int num = NUMBER_MIN + random.nextInt(NUMBER_MAX);
        info.setTicketNumber(num);
        info.setTicketDrawRound(info.getCurrentDrawRound());
        return num;
    }

    public static long prizeForDifference(int diff) {
        if (diff == 0) {
            return PRIZE_EXACT;
        }
        if (diff <= 10) {
            return PRIZE_WITHIN_10;
        }
        if (diff <= 100) {
            return PRIZE_WITHIN_100;
        }
        return 0;
    }

    /** 游戏运行中每帧累加，满 3 分钟开一期。 */
    public void tickRealtime(long deltaTimeMs) {
        if (deltaTimeMs <= 0) {
            return;
        }
        LotteryInfo info = getInfo();
        if (info == null) {
            return;
        }
        info.setPeriodElapsedMs(info.getPeriodElapsedMs() + deltaTimeMs);
        while (info.getPeriodElapsedMs() >= DRAW_INTERVAL_MS) {
            info.setPeriodElapsedMs(info.getPeriodElapsedMs() - DRAW_INTERVAL_MS);
            int round = info.getCurrentDrawRound();
            settleDrawForRound(round);
            info.setCurrentDrawRound(round + 1);
        }
    }

    private void settleDrawForRound(int drawRound) {
        if (drawRound < 1) {
            return;
        }
        LotteryInfo info = getInfo();
        if (info == null) {
            return;
        }
        if (info.getLastSettledDrawRound() >= drawRound) {
            return;
        }

        int winning = NUMBER_MIN + random.nextInt(NUMBER_MAX);
        info.setLastWinningNumber(winning);
        info.setLastDrawRound(drawRound);
        info.setLastSettledDrawRound(drawRound);

        boolean hadTicket = info.getTicketNumber() > 0 && info.getTicketDrawRound() == drawRound;
        if (hadTicket) {
            int player = info.getTicketNumber();
            int diff = Math.abs(player - winning);
            long prize = prizeForDifference(diff);
            info.setLastPlayerNumber(player);
            info.setLastPrizeSilver(prize);
            info.setTicketNumber(0);
            info.setTicketDrawRound(0);
            if (prize > 0) {
                depositPrize(prize);
                notifyPrize(prize, player, winning, diff);
            }
        } else {
            info.setLastPlayerNumber(0);
            info.setLastPrizeSilver(0);
        }
    }

    private void notifyPrize(long prize, int player, int winning, int diff) {
        if (prize >= PRIZE_EXACT) {
            GameEngine.getInstance().showCenterToast(
                    "金陵彩中得头彩！号码 " + player + " 与开奖 " + winning + " 相同，得 " + prize + " 银！", 4500);
        } else if (prize >= PRIZE_WITHIN_10) {
            GameEngine.getInstance().showCenterToast(
                    "金陵彩中奖！您的 " + player + " 与开奖 " + winning + " 仅差 " + diff + "，得 " + prize + " 银。", 3500);
        } else {
            GameEngine.getInstance().showCenterToast(
                    "金陵彩小奖：号码接近开奖 " + winning + "，得 " + prize + " 银。", 2500);
        }
    }

    public String formatLastDrawSummary() {
        LotteryInfo info = getInfo();
        if (info == null || info.getLastSettledDrawRound() < 1) {
            return "尚无一期开奖记录。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("第 ").append(info.getLastDrawRound()).append(" 期开奖号：")
                .append(info.getLastWinningNumber()).append("。\n");
        if (info.getLastPlayerNumber() > 0) {
            int diff = Math.abs(info.getLastPlayerNumber() - info.getLastWinningNumber());
            sb.append("您该期号码 ").append(info.getLastPlayerNumber())
                    .append("，相差 ").append(diff).append(" 点");
            if (info.getLastPrizeSilver() > 0) {
                sb.append("，已兑 ").append(info.getLastPrizeSilver()).append(" 银入袋");
            } else {
                sb.append("，未中彩");
            }
            sb.append("。");
        } else {
            sb.append("您该期未购彩或号码已随往期结算。");
        }
        return sb.toString();
    }

    public String formatRules() {
        int sec = getSecondsUntilDraw();
        return "金陵彩每银千得一注，机摇号码 "
                + NUMBER_MIN + "–" + NUMBER_MAX
                + "。满城烟花每三分钟开奖一期：完全相同得 " + PRIZE_EXACT + " 银；"
                + "相差 10 以内 " + PRIZE_WITHIN_10 + " 银；相差 100 以内 " + PRIZE_WITHIN_100 + " 银。"
                + "每期仅限一注，先扣身上银，不足扣钱庄。\n"
                + "当前第 " + getCurrentDrawRound() + " 期，约 "
                + formatCountdown(sec) + " 后放烟花。";
    }

    public static String formatCountdown(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format(Locale.CHINA, "%d 分 %02d 秒", m, s);
    }
}
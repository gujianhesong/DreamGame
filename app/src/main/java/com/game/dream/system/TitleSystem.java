package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SaveInfo;
import com.game.dream.bean.TitleProgressInfo;
import com.game.dream.title.TitleCatalog;
import com.game.dream.title.TitleDef;
import com.game.dream.title.TitleIds;
import com.game.dream.title.TitleStats;

/**
 * 称号：解锁、佩戴、条件检测。
 */
public class TitleSystem {

    private static TitleSystem instance;

    public static TitleSystem getInstance() {
        if (instance == null) {
            instance = new TitleSystem();
        }
        return instance;
    }

    private TitleProgressInfo getProgress() {
        SaveInfo save = SaveSystem.getInstance().getSaveInfo();
        if (save == null) {
            return null;
        }
        if (save.getTitleProgressInfo() == null) {
            save.setTitleProgressInfo(new TitleProgressInfo());
        }
        TitleProgressInfo p = save.getTitleProgressInfo();
        ensureStarter(p);
        return p;
    }

    private void ensureStarter(TitleProgressInfo p) {
        if (!p.isUnlocked(TitleIds.JIANGHU_NEW)) {
            p.getUnlockedTitleIds().add(TitleIds.JIANGHU_NEW);
        }
        if (!p.hasFlag("dream_start")) {
            p.setFlag("dream_start");
            tryUnlock(TitleIds.HIDDEN_DREAMER);
        }
    }

    public void addCounter(String key, long delta) {
        TitleProgressInfo p = getProgress();
        if (p == null) {
            return;
        }
        p.addCounter(key, delta);
        if (TitleStats.LEVEL_PEAK.equals(key) || TitleStats.KILLS.equals(key)) {
            // noop naming
        }
        if (TitleStats.KILLS.equals(key)) {
            RoleInfo role = RoleSystem.getInstance().getRoleInfo();
            if (role != null) {
                p.setCounter(TitleStats.LEVEL_PEAK, role.getLevel());
            }
        }
        checkCompositeFlags(p);
        evaluateRules();
    }

    public void setFlag(String flag) {
        TitleProgressInfo p = getProgress();
        if (p == null) {
            return;
        }
        p.setFlag(flag);
        checkCompositeFlags(p);
        evaluateRules();
    }

    public void onMapEntered(int mapId) {
        TitleProgressInfo p = getProgress();
        if (p == null) {
            return;
        }
        p.visitMap(mapId);
        switch (mapId) {
            case MapSystem.MAP_ID_QING_XI:
                break;
            case MapSystem.MAP_ID_JIN_LING:
                setFlag("foot_jinling");
                break;
            case MapSystem.MAP_ID_DONGHAI_BAY:
                setFlag("foot_donghai");
                break;
            case MapSystem.MAP_ID_NETHERWORLD:
                setFlag("foot_nether");
                p.setFlag("soul_prison");
                break;
            case MapSystem.MAP_ID_MONSTER_GALLERY:
                addCounter(TitleStats.GALLERY_VISIT, 1);
                break;
            case MapSystem.MAP_ID_HELL_MAZE_1:
            case MapSystem.MAP_ID_HELL_MAZE_2:
            case MapSystem.MAP_ID_HELL_MAZE_3:
            case MapSystem.MAP_ID_HELL_MAZE_4:
                p.setFlag("hell_runner");
                break;
            default:
                break;
        }
        checkCompositeFlags(p);
        evaluateRules();
    }

    private void checkCompositeFlags(TitleProgressInfo p) {
        if (p.hasFlag("used_gamble") && p.hasFlag("used_stock") && p.hasFlag("used_lottery") && p.hasFlag("used_bank")) {
            p.setFlag("jinling_four");
        }
        if (p.hasVisitedMap(MapSystem.MAP_ID_QING_XI)
                && p.hasVisitedMap(MapSystem.MAP_ID_JIN_LING)
                && p.hasVisitedMap(MapSystem.MAP_ID_DONGHAI_BAY)
                && p.hasVisitedMap(MapSystem.MAP_ID_NETHERWORLD)) {
            p.setFlag("foot_north_south");
        }
        if (p.hasVisitedMap(MapSystem.MAP_ID_QING_XI_MAZE)
                && (p.hasVisitedMap(MapSystem.MAP_ID_UNDERWATER_MAZE)
                || p.hasVisitedMap(MapSystem.MAP_ID_UNDERWATER_MAZE_2))
                && p.hasVisitedMap(MapSystem.MAP_ID_HELL_MAZE_1)) {
            p.setFlag("maze_three");
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role != null) {
            long silver = role.getMoney() + role.getBankMoney();
            if (silver >= 1_000_000) {
                p.setFlag("rich_once");
            }
            if (silver >= 10_000_000) {
                p.setFlag("tyrant_silver");
            }
            if (role.getMoney() < 50 && role.getLevel() >= 5) {
                p.setFlag("poor_brave");
            }
            if (role.getHuoli() >= role.getHuoliMax() - 1 && role.getHuoliMax() > 0) {
                p.setFlag("huoli_full");
            }
            if (role.getTili() <= 1) {
                p.setFlag("tili_empty");
            }
        }
    }

    public void onLevelUp(int level) {
        TitleProgressInfo p = getProgress();
        if (p != null) {
            p.setCounter(TitleStats.LEVEL_PEAK, level);
        }
        evaluateRules();
    }

    public void evaluateRules() {
        TitleProgressInfo p = getProgress();
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (p == null || role == null) {
            return;
        }
        checkCompositeFlags(p);
        for (TitleDef def : TitleCatalog.all()) {
            if (p.isUnlocked(def.id)) {
                continue;
            }
            if (def.rule != null && def.rule.isSatisfied(p, role)) {
                unlock(def.id, def);
            }
        }
    }

    public boolean unlock(String titleId) {
        TitleDef def = TitleCatalog.get(titleId);
        if (def == null) {
            return false;
        }
        return unlock(titleId, def);
    }

    private boolean unlock(String titleId, TitleDef def) {
        TitleProgressInfo p = getProgress();
        if (p == null || p.isUnlocked(titleId)) {
            return false;
        }
        p.getUnlockedTitleIds().add(titleId);
        if (!def.hidden) {
            GameEngine.getInstance().showAchievement("称号「" + def.displayName + "」");
        } else {
            GameEngine.getInstance().showCenterToast("隐藏称号「" + def.displayName + "」", 2800);
        }
        return true;
    }

    private void tryUnlock(String titleId) {
        TitleDef def = TitleCatalog.get(titleId);
        if (def == null) {
            return;
        }
        TitleProgressInfo p = getProgress();
        if (p != null && !p.isUnlocked(titleId)) {
            if (def.rule == null || def.rule.isSatisfied(p, RoleSystem.getInstance().getRoleInfo())) {
                unlock(titleId, def);
            }
        }
    }

    public String getEquippedTitleId() {
        TitleProgressInfo p = getProgress();
        return p != null ? p.getEquippedTitleId() : null;
    }

    public String getEquippedDisplayName() {
        String id = getEquippedTitleId();
        if (id == null || id.isEmpty()) {
            return null;
        }
        TitleDef def = TitleCatalog.get(id);
        return def != null ? def.displayName : null;
    }

    public int getEquippedColor() {
        String id = getEquippedTitleId();
        if (id == null) {
            return android.graphics.Color.WHITE;
        }
        TitleDef def = TitleCatalog.get(id);
        return def != null ? def.rarity.getColorArgb() : android.graphics.Color.rgb(255, 240, 180);
    }

    public boolean equip(String titleId) {
        TitleProgressInfo p = getProgress();
        if (p == null) {
            return false;
        }
        if (titleId == null || titleId.isEmpty()) {
            p.setEquippedTitleId(null);
            return true;
        }
        if (!p.isUnlocked(titleId)) {
            return false;
        }
        p.setEquippedTitleId(titleId);
        return true;
    }

    public void unequip() {
        equip(null);
    }

    public boolean isUnlocked(String titleId) {
        TitleProgressInfo p = getProgress();
        return p != null && p.isUnlocked(titleId);
    }

    // ----- 玩法钩子 -----

    public void onPlayerDeath() {
        addCounter(TitleStats.DEATHS, 1);
    }

    public void onSoulReturn() {
        addCounter(TitleStats.SOUL_RETURN, 1);
    }

    public void onGambleBet(boolean betBig, int sum, boolean won, int netChange, int stake) {
        TitleProgressInfo p = getProgress();
        if (p == null) {
            return;
        }
        p.setFlag("used_gamble");
        p.setFlag("gamble_visit");
        addCounter(TitleStats.GAMBLE_BETS, 1);
        if (won) {
            addCounter(TitleStats.GAMBLE_WINS, 1);
        }
        if (sum == 7) {
            addCounter(TitleStats.GAMBLE_SEVEN, 1);
            unlock(TitleIds.SEVEN_EATER);
        }
        if (betBig) {
            long streak = p.getCounter(TitleStats.GAMBLE_BIG_ONLY) + 1;
            p.setCounter(TitleStats.GAMBLE_BIG_ONLY, streak);
        } else {
            p.setCounter(TitleStats.GAMBLE_BIG_ONLY, 0);
        }
        if (netChange > 0) {
            long peak = Math.max(p.getCounter(TitleStats.GAMBLE_NET_WIN_DAY), netChange);
            p.setCounter(TitleStats.GAMBLE_NET_WIN_DAY, peak);
        }
        if (p.getCounter(TitleStats.GAMBLE_BETS) == 1 && netChange < 0) {
            p.setFlag("never_gamble");
        } else if (p.getCounter(TitleStats.GAMBLE_BETS) > 1) {
            p.getFlags().remove("never_gamble");
        }
        evaluateRules();
    }

    public void onGambleComeback(long dailyNetFromDeepLoss) {
        if (dailyNetFromDeepLoss >= 0) {
            unlock(TitleIds.GAMBLE_COMEBACK);
        }
    }

    public void onLotteryBuy() {
        setFlag("used_lottery");
        addCounter(TitleStats.LOTTERY_BUY, 1);
    }

    public void onLotteryPrize(long prize, boolean jackpot) {
        if (prize > 0) {
            addCounter(TitleStats.LOTTERY_PRIZE, 1);
        }
        if (jackpot) {
            unlock(TitleIds.EIGHT_MILLION_DREAM);
            unlock(TitleIds.LEGEND_JACKPOT);
            setFlag("after_jackpot");
            tryUnlock(TitleIds.HIDDEN_JACKPOT_AFTER);
        }
        TitleProgressInfo p = getProgress();
        if (p != null && p.hasFlag("lottery_rush_buy") && prize > 0) {
            p.getFlags().remove("lottery_rush_buy");
            unlock(TitleIds.THREE_MIN_HERO);
        }
        evaluateRules();
    }

    public void onStockTrade() {
        setFlag("used_stock");
        addCounter(TitleStats.STOCK_TRADE, 1);
    }

    public void onStockDividend() {
        addCounter(TitleStats.STOCK_DIVIDEND, 1);
    }

    public void onStockIdleUnlock() {
        setFlag("stock_idle");
        evaluateRules();
    }

    public void onBankUsed() {
        setFlag("used_bank");
        addCounter(TitleStats.BANK_DEPOSIT, 1);
    }

    public void onInnRest(boolean toDawn) {
        addCounter(TitleStats.INN_REST, 1);
        if (toDawn) {
            setFlag("inn_dawn");
        }
        evaluateRules();
    }

    public void onNpcId(int npcId) {
        switch (npcId) {
            case 100258:
            case 100283:
                addCounter(TitleStats.YAMEN_TALK, 1);
                break;
            case 100256:
                setFlag("talk_tavern");
                break;
            case 100253:
                setFlag("talk_longquan");
                break;
            case 100255:
                setFlag("talk_cuican");
                break;
            case 100263:
                setFlag("talk_cangjing");
                break;
            case 100209:
                setFlag("talk_xuanji");
                break;
            case 100210:
                setFlag("talk_peiyuan");
                break;
            case 100508:
                addCounter(TitleStats.MENGPO_TALK, 1);
                break;
            case 100207:
                setFlag("west_ghost");
                break;
            default:
                if (npcId >= 100270 && npcId <= 100277) {
                    setFlag("talk_guard");
                }
                break;
        }
        evaluateRules();
    }

    public void onHongxiang() {
        addCounter(TitleStats.HONGXIANG, 1);
    }

    public void onShuyingTalk() {
        setFlag("talk_shuying");
        evaluateRules();
    }

    public void onKillEnemy() {
        addCounter(TitleStats.KILLS, 1);
    }

    public void onPetCapture(boolean success) {
        if (success) {
            addCounter(TitleStats.PET_CAPTURE, 1);
        } else {
            setFlag("capture_fail");
            evaluateRules();
        }
    }

    public void onCoachTravel() {
        addCounter(TitleStats.COACH_TRAVEL, 1);
        addCounter(TitleStats.TELEPORT, 1);
    }

    public void onTeleport() {
        addCounter(TitleStats.TELEPORT, 1);
    }

    public void onIllusionClear() {
        addCounter(TitleStats.ILLUSION_WIN, 1);
    }

    public void onShopBuy() {
        TitleProgressInfo p = getProgress();
        if (p != null && p.getCounter(TitleStats.SHOP_BUY) == 0 && p.getCounter(TitleStats.SHOP_WINDOW) >= 20) {
            setFlag("window_shop_master");
        }
        addCounter(TitleStats.SHOP_BUY, 1);
        if (p != null && p.getCounter(TitleStats.SHOP_BUY) >= 30) {
            setFlag("shop_king_pin");
        }
        evaluateRules();
    }

    public void onShopOpen() {
        addCounter(TitleStats.SHOP_WINDOW, 1);
        TitleProgressInfo p = getProgress();
        if (p != null && p.getCounter(TitleStats.SHOP_BUY) == 0 && p.getCounter(TitleStats.SHOP_WINDOW) >= 20) {
            setFlag("window_shop");
        }
        evaluateRules();
    }

    public void onHealNpc() {
        addCounter(TitleStats.NPC_HEAL, 1);
    }

    public void onPetHeal() {
        addCounter(TitleStats.PET_HEAL, 1);
    }

    public void onWarehouseExpand() {
        addCounter(TitleStats.WAREHOUSE_EXPAND, 1);
    }

    public void onDash() {
        addCounter(TitleStats.DASH_COUNT, 1);
    }

    public void onPotion() {
        addCounter(TitleStats.POTION_USE, 1);
    }

    public void onEquipSell() {
        setFlag("equip_sell");
        evaluateRules();
    }

    public void onUiFlag(String flag) {
        setFlag(flag);
    }
}
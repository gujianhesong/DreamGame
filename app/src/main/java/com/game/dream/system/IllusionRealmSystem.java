package com.game.dream.system;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.bean.IllusionRealmInfo;
import com.game.dream.bean.SaveInfo;
import com.game.dream.enemy.Bandit;
import com.game.dream.enemy.CrabGeneral;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.FoxSpirit;
import com.game.dream.enemy.LittleGreenDragon;
import com.game.dream.enemy.ShrimpSoldier;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Viper;
import com.game.dream.enemy.WildBoar;
import com.game.dream.enemy.Wolf;
import com.game.dream.enemy.Yaksha;
import com.game.dream.utils.LogUtil;

import java.util.Random;

/**
 * 幻境挑战：凡尘 / 妖狐 / 龙宫，各 20 关
 */
public class IllusionRealmSystem {

    public static final int REALM_FANCHEN = 0;
    public static final int REALM_YAOHU = 1;
    public static final int REALM_LONGGONG = 2;
    public static final int REALM_COUNT = 3;
    public static final int STAGES_PER_REALM = 20;
    public static final int MAP_CENTER = 2000;

    public enum Phase {
        INACTIVE,
        PREPARING,
        FIGHTING,
        VICTORY
    }

    private static IllusionRealmSystem instance;

    private Phase phase = Phase.INACTIVE;
    private int currentStage = 1;
    private int currentRealm = REALM_FANCHEN;

    private int returnMapId;
    private float returnX;
    private float returnY;

    private long phaseStartTime;
    private long nextWaveTime;
    private int totalToSpawn;
    private int totalSpawned;
    private int totalKilled;
    private int wavesTotal;
    private int wavesSpawned;
    private int perWave;
    private float statMultiplier = 1f;

    private boolean stageRewardGranted;
    private boolean pendingEnterAfterTeleport;

    private final Rect abortButton = new Rect();

    private final Random random = new Random();

    public static IllusionRealmSystem getInstance() {
        if (instance == null) {
            instance = new IllusionRealmSystem();
        }
        return instance;
    }

    public void init(Context context) {
    }

    public boolean isActive() {
        return phase != Phase.INACTIVE;
    }

    public boolean isInFight() {
        return phase == Phase.PREPARING || phase == Phase.FIGHTING;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public int getCurrentRealm() {
        return currentRealm;
    }

    public static String getRealmDisplayName(int realm) {
        switch (realm) {
            case REALM_YAOHU:
                return "妖狐幻境";
            case REALM_LONGGONG:
                return "龙宫幻境";
            default:
                return "凡尘幻境";
        }
    }

    public int getClearedStage(int realm) {
        /*IllusionRealmInfo info = getIllusionRealmSave();
        if (info == null) {
            return 0;
        }
        switch (realm) {
            case REALM_YAOHU:
                return info.getYaohuCleared();
            case REALM_LONGGONG:
                return info.getLonggongCleared();
            default:
                return info.getFanchenCleared();
        }*/
        return 20;
    }

    private IllusionRealmInfo getIllusionRealmSave() {
        SaveInfo save = SaveSystem.getInstance().getSaveInfo();
        if (save == null) {
            return null;
        }
        if (save.getIllusionRealmInfo() == null) {
            save.setIllusionRealmInfo(new IllusionRealmInfo());
        }
        return save.getIllusionRealmInfo();
    }

    /** 可挑战的最高关（已通 N 关则解锁 N+1，最多 20） */
    public int getUnlockedStage(int realm) {
        if (!isRealmAccessible(realm)) {
            return 0;
        }
        return Math.min(STAGES_PER_REALM, getClearedStage(realm) + 1);
    }

    /** 是否已解锁该幻境（可看到并进入第 1 关） */
    public boolean isRealmAccessible(int realm) {
        if (realm == REALM_FANCHEN) {
            return true;
        }
        if (realm == REALM_YAOHU) {
            return getClearedStage(REALM_FANCHEN) >= STAGES_PER_REALM;
        }
        if (realm == REALM_LONGGONG) {
            return getClearedStage(REALM_YAOHU) >= STAGES_PER_REALM;
        }
        return false;
    }

    public String getRealmLockHint(int realm) {
        if (realm == REALM_YAOHU) {
            return "通关凡尘幻境 20 关后解锁";
        }
        if (realm == REALM_LONGGONG) {
            return "通关妖狐幻境 20 关后解锁";
        }
        return "";
    }

    public boolean isStageUnlocked(int realm, int stage) {
        return stage >= 1 && stage <= getUnlockedStage(realm);
    }

    public boolean isStageUnlocked(int stage) {
        return isStageUnlocked(REALM_FANCHEN, stage);
    }

    public boolean isFirstClear(int realm, int stage) {
        return stage > getClearedStage(realm);
    }

    private void saveCleared(int realm, int stage) {
        IllusionRealmInfo info = getIllusionRealmSave();
        if (info == null) {
            return;
        }
        int old = getClearedStage(realm);
        if (stage <= old) {
            return;
        }
        switch (realm) {
            case REALM_YAOHU:
                info.setYaohuCleared(stage);
                break;
            case REALM_LONGGONG:
                info.setLonggongCleared(stage);
                break;
            default:
                info.setFanchenCleared(stage);
                break;
        }
    }

    /**
     * 从选关界面进入幻境
     */
    public void requestEnterStage(int realm, int stage) {
        if (!isRealmAccessible(realm)) {
            GameEngine.getInstance().showCenterToast(getRealmLockHint(realm));
            return;
        }
        if (!isStageUnlocked(realm, stage)) {
            GameEngine.getInstance().showCenterToast("尚未解锁该关");
            return;
        }
        RoleSystem role = RoleSystem.getInstance();
        returnMapId = role.getRoleInfo().getMapId();
        if (returnMapId == MapSystem.MAP_ID_ILLUSION_REALM) {
            returnMapId = MapSystem.getInstance().getBornMap().getMapId();
        }
        if (role.getRoleInfo().getMapX() >= 0 && role.getRoleInfo().getMapY() >= 0) {
            returnX = role.getRoleInfo().getMapX();
            returnY = role.getRoleInfo().getMapY();
        } else {
            returnX = GameEngine.getInstance().getPlayer().getX();
            returnY = GameEngine.getInstance().getPlayer().getY();
        }

        currentRealm = realm;
        currentStage = stage;
        pendingEnterAfterTeleport = true;
        GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_ILLUSION_REALM);
    }

    public void requestEnterStage(int stage) {
        requestEnterStage(REALM_FANCHEN, stage);
    }

    /** 传送完成后由 GameEngine 调用 */
    public void onIllusionMapReady() {
        if (!pendingEnterAfterTeleport) {
            return;
        }
        pendingEnterAfterTeleport = false;
        beginStageInternal();
    }

    private void beginStageInternal() {
        configureStageNumbers();
        phase = Phase.PREPARING;
        phaseStartTime = System.currentTimeMillis();
        nextWaveTime = phaseStartTime + 5000;
        totalSpawned = 0;
        totalKilled = 0;
        wavesSpawned = 0;
        stageRewardGranted = false;

        GameEngine.getInstance().clearEnemies();
        GameEngine.getInstance().getPlayer().setX(MAP_CENTER);
        GameEngine.getInstance().getPlayer().setY(MAP_CENTER);
        GameEngine.getInstance().showCenterToast(
                getRealmDisplayName(currentRealm) + " 第" + currentStage + "关 — 5秒后开始", 3000);
        LogUtil.d("Illusion", getRealmDisplayName(currentRealm) + " stage " + currentStage
                + " spawn=" + totalToSpawn + " waves=" + wavesTotal);
    }

    private void configureStageNumbers() {
        totalToSpawn = 24 + currentStage * 4;
        if (currentStage == 20) {
            totalToSpawn = 80;
        }
        wavesTotal = 3 + currentStage / 4;
        if (currentStage == 20) {
            wavesTotal = 8;
        }
        perWave = Math.max(1, (int) Math.ceil((float) totalToSpawn / wavesTotal));
        statMultiplier = (1f + (currentStage - 1) * 0.07f) * getRealmStatFactor();
        if (currentStage == 20) {
            statMultiplier *= 1.35f;
        }
    }

    private float getRealmStatFactor() {
        if (currentRealm == REALM_YAOHU) {
            return 1.12f;
        }
        if (currentRealm == REALM_LONGGONG) {
            return 1.25f;
        }
        return 1f;
    }

    public void update(long deltaTime) {
        if (!MapSystem.getInstance().isIllusionRealmMap()) {
            return;
        }
        if (phase == Phase.INACTIVE) {
            return;
        }

        long now = System.currentTimeMillis();

        if (phase == Phase.PREPARING) {
            if (now >= nextWaveTime) {
                phase = Phase.FIGHTING;
                nextWaveTime = now;
                spawnNextWave();
            }
            return;
        }

        if (phase == Phase.FIGHTING) {
            if (wavesSpawned < wavesTotal && now - nextWaveTime >= getWaveIntervalMs()) {
                spawnNextWave();
                nextWaveTime = now;
            }
            checkVictory();
            return;
        }

        if (phase == Phase.VICTORY) {
            if (now - phaseStartTime >= 6000) {
                exitToWorld(false);
            }
        }
    }

    private long getWaveIntervalMs() {
        if (currentStage <= 5) return 12000;
        if (currentStage <= 10) return 10000;
        if (currentStage <= 15) return 8000;
        return 6500;
    }

    private void spawnNextWave() {
        if (wavesSpawned >= wavesTotal) {
            return;
        }
        wavesSpawned++;
        int count = perWave;
        if (wavesSpawned == wavesTotal) {
            count = totalToSpawn - totalSpawned;
        }
        count = Math.max(1, count);

        for (int i = 0; i < count; i++) {
            if (totalSpawned >= totalToSpawn) {
                break;
            }
            spawnOneEnemy();
            totalSpawned++;
        }

        GameEngine.getInstance().showCenterToast("第 " + wavesSpawned + "/" + wavesTotal + " 波", 1200);
    }

    private void spawnOneEnemy() {
        float[] pos = pickEdgeSpawn();
        Enemy enemy;
        if (currentStage == 20 && totalSpawned == 0) {
            enemy = spawnStageBoss(pos[0], pos[1]);
        } else if (currentStage >= 15 && random.nextFloat() < 0.25f) {
            enemy = spawnRealmMob(pos[0], pos[1], Enemy.EnemyLevel.LEADER);
        } else if (currentStage >= 10 && random.nextFloat() < 0.2f) {
            enemy = spawnRealmMob(pos[0], pos[1], Enemy.EnemyLevel.LEADER);
        } else {
            enemy = spawnRealmMob(pos[0], pos[1], Enemy.EnemyLevel.NORMAL);
        }
        enemy.applyIllusionChallengeStats(statMultiplier);
        enemy.setAggro(120000);
        enemy.setState(Enemy.State.CHASING);
        GameEngine.getInstance().addEnemy(enemy);
    }

    private Enemy spawnStageBoss(float x, float y) {
        switch (currentRealm) {
            case REALM_YAOHU: {
                Enemy boss = spawnFixedTier(new FoxSpirit(x, y), Enemy.EnemyLevel.ELITE, () -> new FoxSpirit(x, y));
                boss.setName("幻境狐王");
                return boss;
            }
            case REALM_LONGGONG: {
                Enemy boss = spawnFixedTier(new LittleGreenDragon(x, y), Enemy.EnemyLevel.ELITE,
                        () -> new LittleGreenDragon(x, y));
                boss.setName("幻境龙子");
                return boss;
            }
            default: {
                Enemy boss = spawnFixedTier(new Tiger(x, y), Enemy.EnemyLevel.ELITE, () -> new Tiger(x, y));
                boss.setName("幻境虎王");
                return boss;
            }
        }
    }

    private interface EnemyFactory {
        Enemy create();
    }

    private Enemy spawnFixedTier(Enemy fallback, Enemy.EnemyLevel tier, EnemyFactory factory) {
        for (int attempt = 0; attempt < 60; attempt++) {
            Enemy e = factory.create();
            if (e.getEnemyLevel() == tier) {
                return e;
            }
        }
        return fallback;
    }

    private Enemy spawnRealmMob(float x, float y, Enemy.EnemyLevel tier) {
        for (int attempt = 0; attempt < 40; attempt++) {
            Enemy e = createRealmType(x, y);
            if (e.getEnemyLevel() == tier) {
                return e;
            }
        }
        return createRealmType(x, y);
    }

    private Enemy createRealmType(float x, float y) {
        switch (currentRealm) {
            case REALM_YAOHU:
                return createYaoHuType(x, y);
            case REALM_LONGGONG:
                return createLongGongType(x, y);
            default:
                return createFanChenType(x, y);
        }
    }

    private Enemy createFanChenType(float x, float y) {
        double r = random.nextDouble();
        if (r < 0.25f) {
            Enemy e = new Wolf(x, y);
            e.setName("幻境野狼");
            return e;
        } else if (r < 0.5f) {
            Enemy e = new Viper(x, y);
            e.setName("幻境毒蛇");
            return e;
        } else if (r < 0.75f) {
            Enemy e = new WildBoar(x, y);
            e.setName("幻境野猪");
            return e;
        }
        Enemy e = new Tiger(x, y);
        e.setName("幻境猛虎");
        return e;
    }

    private Enemy createYaoHuType(float x, float y) {
        double r = random.nextDouble();
        if (r < 0.45f) {
            Enemy e = new FoxSpirit(x, y);
            e.setName("幻境狐妖");
            return e;
        } else if (r < 0.75f) {
            Enemy e = new Bandit(x, y);
            e.setName("幻境山贼");
            return e;
        }
        Enemy e = new Wolf(x, y);
        e.setName("幻境妖狼");
        return e;
    }

    private Enemy createLongGongType(float x, float y) {
        double r = random.nextDouble();
        if (r < 0.3f) {
            Enemy e = new ShrimpSoldier(x, y);
            e.setName("幻境虾兵");
            return e;
        } else if (r < 0.55f) {
            Enemy e = new CrabGeneral(x, y);
            e.setName("幻境蟹将");
            return e;
        } else if (r < 0.8f) {
            Enemy e = new Yaksha(x, y);
            e.setName("幻境夜叉");
            return e;
        }
        Enemy e = new LittleGreenDragon(x, y);
        e.setName("幻境幼龙");
        return e;
    }

    private float[] pickEdgeSpawn() {
        int mapW = MapSystem.getInstance().getCurMapInfo().getMapWidth();
        int mapH = MapSystem.getInstance().getCurMapInfo().getMapHeight();
        float margin = 120f;
        int edge = random.nextInt(4);
        float x;
        float y;
        switch (edge) {
            case 0:
                x = margin + random.nextFloat() * (mapW - margin * 2);
                y = margin;
                break;
            case 1:
                x = margin + random.nextFloat() * (mapW - margin * 2);
                y = mapH - margin;
                break;
            case 2:
                x = margin;
                y = margin + random.nextFloat() * (mapH - margin * 2);
                break;
            default:
                x = mapW - margin;
                y = margin + random.nextFloat() * (mapH - margin * 2);
                break;
        }
        return new float[]{x, y};
    }

    public void onIllusionEnemyKilled() {
        if (phase != Phase.FIGHTING && phase != Phase.PREPARING) {
            return;
        }
        totalKilled++;
        checkVictory();
    }

    private void checkVictory() {
        if (phase != Phase.FIGHTING) {
            return;
        }
        if (totalSpawned < totalToSpawn) {
            return;
        }
        if (GameEngine.getInstance().getAliveEnemyCount() > 0) {
            return;
        }
        onStageCleared();
    }

    private void onStageCleared() {
        if (stageRewardGranted) {
            return;
        }
        stageRewardGranted = true;
        phase = Phase.VICTORY;
        phaseStartTime = System.currentTimeMillis();

        int realmBonus = currentRealm * 15;
        int exp = 80 + currentStage * 45 + realmBonus * 3;
        int money = 50 + currentStage * 30 + realmBonus * 2;
        int seals = 5 + currentStage * 2 + realmBonus;

        if (isFirstClear(currentRealm, currentStage)) {
            saveCleared(currentRealm, currentStage);
            if (currentStage == 5 || currentStage == 10 || currentStage == 15) {
                money += 200 + currentStage * 20 + realmBonus * 5;
                seals += 15 + realmBonus;
            }
            if (currentStage == 20) {
                money += 800 + realmBonus * 20;
                seals += 80 + realmBonus * 4;
                exp += 500 + realmBonus * 10;
            }
        } else {
            exp = (int) (exp * 0.35f);
            money = (int) (money * 0.35f);
            seals = Math.max(1, (int) (seals * 0.35f));
        }

        RoleSystem.getInstance().addExperience(exp);
        RoleSystem.getInstance().addMoney(money);
        GameEngine.getInstance().addMessage("幻境通关 +" + exp + " 经验", com.game.dream.panel.MessagePanel.MessageType.EXPERIENCE);
        GameEngine.getInstance().addMessage("幻境印 +" + seals, com.game.dream.panel.MessagePanel.MessageType.MONEY);

        GameEngine.getInstance().showCenterToast("通关! 幻境印+" + seals + " 6秒后返回", 4000);
    }

    public void onPlayerDefeated() {
        if (!isActive()) {
            return;
        }
        // 幻境失败回满血，同时清除带入的中毒
        GameEngine.getInstance().getPlayer().clearPoisonDebuff();
        RoleSystem.getInstance().getRoleInfo().setHp(RoleSystem.getInstance().getRoleInfo().getBloodCap());
        RoleSystem.getInstance().getRoleInfo().setMp(RoleSystem.getInstance().getRoleInfo().getMagicCap());
        GameEngine.getInstance().showCenterToast("挑战失败，无惩罚", 2500);
        exitToWorld(true);
    }

    /** 主动撤离（无本关奖励） */
    public void abortChallenge() {
        if (!isInFight()) {
            return;
        }
        GameEngine.getInstance().showCenterToast("已撤离幻境", 2000);
        exitToWorld(true);
    }

    private void exitToWorld(boolean failed) {
        phase = Phase.INACTIVE;
        pendingEnterAfterTeleport = false;
        GameEngine.getInstance().clearEnemies();
        int targetMap = returnMapId > 0 ? returnMapId : MapSystem.getInstance().getBornMap().getMapId();
        RoleSystem.getInstance().getRoleInfo().setMapId(targetMap);
        RoleSystem.getInstance().getRoleInfo().setMapX((int) returnX);
        RoleSystem.getInstance().getRoleInfo().setMapY((int) returnY);
        GameEngine.getInstance().teleportToMapWithPosition(targetMap, returnX, returnY);
        if (failed) {
            RoleSystem.getInstance().getRoleInfo().setHp(RoleSystem.getInstance().getRoleInfo().getBloodCap());
            RoleSystem.getInstance().getRoleInfo().setMp(RoleSystem.getInstance().getRoleInfo().getMagicCap());
        }
    }

    public int getRemainingEnemies() {
        return Math.max(0, totalToSpawn - totalKilled);
    }

    public int getTotalToSpawn() {
        return totalToSpawn;
    }

    public int getTotalKilled() {
        return totalKilled;
    }

    public void updateHudLayout(int screenWidth) {
        abortButton.set(screenWidth - 140, 90, screenWidth - 20, 140);
    }

    public boolean handleHudTouch(float x, float y) {
        if (!isInFight()) {
            return false;
        }
        if (abortButton.contains((int) x, (int) y)) {
            abortChallenge();
            return true;
        }
        return false;
    }

    public void drawHud(Canvas canvas, int screenWidth) {
        if (!isActive()) {
            return;
        }
        updateHudLayout(screenWidth);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(210, 20, 10, 40));
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);

        String realmName = getRealmDisplayName(currentRealm);
        String phaseText;
        if (phase == Phase.PREPARING) {
            long left = Math.max(0, (nextWaveTime - System.currentTimeMillis()) / 1000);
            phaseText = realmName + " 第" + currentStage + "关 | 准备 " + left + "s";
        } else if (phase == Phase.VICTORY) {
            phaseText = "通关! 即将返回…";
        } else {
            int alive = GameEngine.getInstance().getAliveEnemyCount();
            phaseText = realmName + " 第" + currentStage + "关 | 剩余 " + alive + " | 波次 " + wavesSpawned + "/" + wavesTotal;
        }
        canvas.drawText(phaseText, screenWidth / 2f, 56, paint);

        if (isInFight()) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(20);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(180, 80, 40, 40));
            canvas.drawRoundRect(abortButton.left, abortButton.top, abortButton.right, abortButton.bottom, 8, 8, paint);
            paint.setColor(Color.WHITE);
            canvas.drawText("撤离", abortButton.centerX(), abortButton.centerY() + 7, paint);
        }
    }
}

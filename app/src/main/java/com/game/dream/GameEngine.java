package com.game.dream;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.bean.MapInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.bean.SkillStartInfo;
import com.game.dream.enemy.Bandit;
import com.game.dream.enemy.CrabGeneral;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.FoxSpirit;
import com.game.dream.enemy.GiantSeaTurtle;
import com.game.dream.enemy.LittleGreenDragon;
import com.game.dream.enemy.LonelySpirit;
import com.game.dream.enemy.SavageWraith;
import com.game.dream.enemy.SeaDragonKing;
import com.game.dream.enemy.SeaDragonPrince;
import com.game.dream.enemy.ShrimpSoldier;
import com.game.dream.enemy.TurtleChancellor;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Viper;
import com.game.dream.enemy.WildBoar;
import com.game.dream.enemy.Wolf;
import com.game.dream.enemy.Yaksha;
import com.game.dream.enemy.YellowSpringGuide;
import com.game.dream.enemy.BloodPoolDemonKing;
import com.game.dream.enemy.JudgeCuiYu;
import com.game.dream.enemy.HellGuardian;
import com.game.dream.enemy.KingYanluo;
import com.game.dream.enemy.Vampire;
import com.game.dream.enemy.ChainBoundWraith;
import com.game.dream.enemy.GhostGeneral;
import com.game.dream.enums.SkillType;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.figure.Character;
import com.game.dream.figure.Player;
import com.game.dream.item.GroundItem;
import com.game.dream.item.ItemStack;
import com.game.dream.map.HellMazeGenerator;
import com.game.dream.map.MapContentManager;
import com.game.dream.map.MazeGenerator;
import com.game.dream.map.NetherworldMapGenerator;
import com.game.dream.map.UnderwaterMazeGenerator;
import com.game.dream.npc.AnimalNpc;
import com.game.dream.npc.Npc;
import com.game.dream.npc.TreasureChest;
import com.game.dream.panel.ShopPanel;
import com.game.dream.skill.DragonLightningZone;
import com.game.dream.skill.SeaSealZone;
import com.game.dream.skill.TidePool;
import com.game.dream.skill.SkillEffect;
import com.game.dream.skill.LightningChainEffect;
import com.game.dream.system.DayNightCycle;
import com.game.dream.system.IllusionRealmSystem;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
import com.game.dream.system.Pet;
import com.game.dream.system.PetSystem;
import com.game.dream.system.MazeSystem;
import com.game.dream.system.NpcSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.system.WeatherSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DamageNumber;
import com.game.dream.ui.DialogBox;
import com.game.dream.ui.EquipSellDialog;
import com.game.dream.ui.FloatingText;
import com.game.dream.ui.GameUI;
import com.game.dream.ui.Projectile;
import com.game.dream.utils.BattleUtil;
import com.game.dream.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private Context context;
    private Context activityContext;
    private static int screenWidth;
    private static int screenHeight;

    // Map dimensions (默认值，实际使用 MapSystem 中当前地图的尺寸)
    public static final int MAP_WIDTH = 10000;
    public static final int MAP_HEIGHT = 10000;

    /** 获取当前地图实际宽度（像素） */
    private int getCurrentMapWidth() {
        return MapSystem.getInstance().getCurMapInfo().getMapWidth();
    }

    /** 获取当前地图实际高度（像素） */
    private int getCurrentMapHeight() {
        return MapSystem.getInstance().getCurMapInfo().getMapHeight();
    }

    // Camera position (top-left corner of visible area)
    private static float cameraX;
    private static float cameraY;

    // Player
    private Player player;

    // Day-night cycle
    private DayNightCycle dayNightCycle;
    private long lastUpdateTime;

    // Weather system
    private WeatherSystem weatherSystem;

    // Enemies
    private java.util.List<Enemy> enemies;

    // 迷宫出口 BOSS 封印提示的节流时间戳（避免每帧刷屏）
    private long lastBossGateToastTime = 0;

    // 战宠系统: 当前出战的战宠(通常1只) + 捕捉请求(在更新线程处理)
    private final java.util.List<Pet> activePets = new java.util.ArrayList<>();
    private volatile boolean captureRequested = false;
    private long lastCaptureTime = 0;
    private static final long CAPTURE_COOLDOWN = 1500;
    // 玩家附近是否有可捕捉目标(更新线程计算, 绘制线程读取用于捕捉按钮高亮)
    private volatile boolean capturableTargetNearby = false;

    // Pending melee attack (wait for lunge to complete before dealing damage)
    private boolean pendingMeleeAttack = false;
    private List<Projectile> pendingSwordQi = null;
    private long pendingSwordQiReleaseTime = 0;
    private static final long SWORD_QI_RELEASE_DELAY = 90;

    // 异步加载标志
    private volatile boolean isLoading = false;

    // Projectiles (magic attacks)
    private java.util.List<Projectile> projectiles;

    // Damage numbers
    private java.util.List<DamageNumber> damageNumbers;

    // Floating texts for rewards/damage
    private java.util.List<FloatingText> floatingTexts;

    private GameUI gameUI;


    // 地狱迷宫特殊机制计时器
    private long hellBloodPoolTickAcc = 0;      // 血池 DoT 累计（每 800ms 一跳）
    private long hellSafeStoneTickAcc = 0;      // 安全石台回复累计（每 1000ms 一跳）
    private static final long HELL_BLOOD_POOL_TICK = 800;
    private static final long HELL_SAFE_STONE_TICK = 1000;

    // Resource recovery tracking (every 60 seconds)
    private long accumulatedRecoveryTime = 0; // Accumulated game time in milliseconds
    private long accumulatedRecoveryTime_ZaiSheng = 0; // Accumulated game time in milliseconds
    private long accumulatedRecoveryTime_MingSi = 0; // Accumulated game time in milliseconds
    private static final long RECOVERY_INTERVAL = 60000; // 60 seconds (1 minute)

    // Active skill effects on the map
    private List<SkillEffect> activeSkillEffects = new ArrayList<>();
    private final List<DragonLightningZone> dragonLightningZones = new ArrayList<>();
    private final List<SeaSealZone> seaSealZones = new ArrayList<>();
    private final List<TidePool> tidePools = new ArrayList<>();

    // Ground items (dropped by enemies)
    private List<GroundItem> groundItems = new ArrayList<>();


    private static volatile GameEngine instance;

    public static GameEngine getInstance() {
        if (instance == null) {
            synchronized (GameEngine.class) {
                if (instance == null) {
                    // 注意：第一次调用时必须传入一个 Context
                    // 建议在 Application 类或 MainActivity.onCreate 中初始化
                    throw new IllegalStateException("GameEngine not initialized. Call init(Context) first.");
                }
            }
        }
        return instance;
    }

    public GameEngine(Context context) {
        instance = this;
        this.activityContext = context;
        this.context = context.getApplicationContext();
        this.accumulatedRecoveryTime = 0; // Initialize recovery timer
        IllusionRealmSystem.getInstance().init(this.context);

        initGame();
    }

    public static void release() {
        if (instance != null) {
            instance.cleanup(); // 停止游戏循环
            instance = null;
        }
    }

    private void initGame() {
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();

        if (roleInfo == null) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    initGame();
                }
            }, 100);
            return;
        }

        int mapId = roleInfo.getMapId();
        if (mapId <= 0 || mapId == MapSystem.MAP_ID_ILLUSION_REALM
                || mapId == MapSystem.MAP_ID_MONSTER_GALLERY) {
            mapId = MapSystem.getInstance().getBornMap().getMapId();
            roleInfo.setMapId(mapId);
        }

        // 初始化不依赖地图的组件
        dayNightCycle = new DayNightCycle();
        lastUpdateTime = System.currentTimeMillis();
        projectiles = new ArrayList<>();
        damageNumbers = new ArrayList<>();
        floatingTexts = new ArrayList<>();

        // 大地图异步加载，避免阻塞 UI
        isLoading = true;
        MapSystem.getInstance().loadMapAsync(mapId, new MapSystem.OnLoadMapCallback() {
            @Override
            public void onLoadMapFinish(int mapId, int[][] mapData) {
                finishInitGame(mapId, mapData);
                isLoading = false;
            }
        });
    }

    /**
     * 完成游戏初始化（地图数据已就绪后调用）
     * @param mapData 预生成的地图数据（仅金陵地图使用，其他为 null）
     */
    private void finishInitGame(int mapId, int[][] mapData) {
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();

        if (roleInfo.getMapX() < 0 || roleInfo.getMapY() < 0) {
            Pair<Integer, Integer> startPos = MapSystem.getInstance().getStartPosition();
            Pair<Integer, Integer> mapXY = MapSystem.getInstance().getMapXY(startPos.first, startPos.second);
            roleInfo.setMapX(mapXY.first);
            roleInfo.setMapY(mapXY.second);
        }

        player = new Player(roleInfo.getMapX(), roleInfo.getMapY());
        player.setName("剑侠客");
        player.setRespawnPoint(player.getX(), player.getY());

        updateCamera();
        initializeEnemies();

        gameUI = new GameUI();
        gameUI.initUI();
        // 补调 setScreenSize 初始化控制按钮（异步加载时 onSizeChanged 先于 gameUI 创建）
        if (screenWidth > 0 && screenHeight > 0) {
            gameUI.setScreenSize(screenWidth, screenHeight);
        }
    }

    /**
     * Spawn enemys at random locations
     */
    private void initializeEnemies() {
        enemies = new java.util.ArrayList<>();
        enemies.addAll(MapContentManager.getInstance().initializeEnemies());
        syncActivePetToWorld();
    }

    /**
     * 查找当前地图中仍存活的 BOSS 级敌人。
     * 用于普通迷宫出口封印：只要还有 BOSS 存活，玩家就无法从出口离开。
     */
    private Enemy findAliveBossInCurrentMap() {
        return findAliveEnemyOfLevel(Enemy.EnemyLevel.BOSS);
    }

    /**
     * 地狱迷宫只由关卡 BOSS 守关。该层关卡 BOSS 全部击败后，才能从出口进入下一关。
     */
    private Enemy findAliveStageBossInCurrentMap() {
        return findAliveEnemyOfLevel(Enemy.EnemyLevel.STAGE_BOSS);
    }

    private Enemy findAliveEnemyOfLevel(Enemy.EnemyLevel level) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive() && enemy.getEnemyLevel() == level) {
                return enemy;
            }
        }
        return null;
    }

    public void prepareForSave() {
        if (player == null) {
            return;
        }
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        if (roleInfo == null) {
            return;
        }
        int mapId = MapSystem.getInstance().getCurrentMapId();
        if (mapId > 0 && mapId != MapSystem.MAP_ID_ILLUSION_REALM
                && mapId != MapSystem.MAP_ID_MONSTER_GALLERY) {
            roleInfo.setMapId(mapId);
            roleInfo.setMapX((int) player.getX());
            roleInfo.setMapY((int) player.getY());
        }
    }

    public void cleanup() {
        instance = null;

        // Clean up map renderer
        MapSystem.getInstance().cleanup();

        if (gameUI != null) {
            gameUI.cleanup();
        }
    }

    public void update(boolean isFirst) {
        // 异步加载中跳过更新
        if (isLoading) return;

        // Calculate delta time first
        long currentTime = System.currentTimeMillis();
        long deltaTime;
        if (isFirst) {
            deltaTime = 0;
        } else {
            deltaTime = currentTime - lastUpdateTime;
        }
        lastUpdateTime = currentTime;

        // Update player movement (pass deltaTime)
        player.update(MapSystem.getInstance().getCurMapInfo().getMapData(),
                getCurrentMapWidth() / TILE_SIZE, getCurrentMapHeight() / TILE_SIZE, TILE_SIZE, deltaTime);

        // 中毒 DoT：跳伤飘字与毒杀
        int poisonTickDamage = player.consumePendingPoisonTickDamage();
        if (poisonTickDamage > 0) {
            damageNumbers.add(DamageNumber.poison(
                    player.getX(), player.getY() - 48, poisonTickDamage));
        }
        if (player.consumePoisonDeathFlag()) {
            handlePlayerDeath();
        }

        // Update camera to follow player
        updateCamera();

        // 检查迷宫出口传送
        if (MapSystem.getInstance().isCurrentMazaMap() && MazeSystem.getInstance().isInitialized()) {
            if (MazeSystem.getInstance().checkExitPortal(player.getX(), player.getY())) {
                // 普通迷宫由 BOSS 守关；地狱和海底迷宫只由关卡 BOSS 守关
                boolean stageBossGate = MapSystem.getInstance().isCurrentHellMazeMap()
                        || MapSystem.getInstance().isCurrentUnderwaterMazeMap();
                Enemy aliveBoss = stageBossGate
                        ? findAliveStageBossInCurrentMap()
                        : findAliveBossInCurrentMap();
                if (aliveBoss != null) {
                    long now = System.currentTimeMillis();
                    if (now - lastBossGateToastTime > 2000) {
                        lastBossGateToastTime = now;
                        showCenterToast("出口被封印，击败「" + aliveBoss.getName() + "」才能离开！", 2000);
                    }
                    return; // 封锁传送，停留在出口
                }
                // 不同迷宫出口目的地不同
                int curMapId = MapSystem.getInstance().getCurrentMapId();
                if (MapSystem.isUnderwaterMazeId(curMapId)) {
                    handleUnderwaterMazeExit(curMapId);
                } else if (curMapId >= MapSystem.MAP_ID_HELL_MAZE_1 && curMapId <= MapSystem.MAP_ID_HELL_MAZE_4) {
                    // 地狱迷宫层间传送
                    handleHellMazeExit(curMapId);
                } else {
                    teleportToMap(MapSystem.MAP_ID_QING_XI);
                }
                return; // 传送后本帧不再继续更新
            }
        }

        // 地狱迷宫特殊地形机制（血池 DoT / 安全石台 / 陷阱）
        checkHellMazeHazards(deltaTime);

        // Update day-night cycle (海底地图、海底迷宫、地府、地狱迷宫、幻境关闭昼夜系统)
        int curId = MapSystem.getInstance().getCurrentMapId();
        if (dayNightCycle != null && curId != MapSystem.MAP_ID_DONGHAI_SEABED
                && !MapSystem.isUnderwaterMazeId(curId)
                && curId != MapSystem.MAP_ID_NETHERWORLD
                && curId != MapSystem.MAP_ID_ILLUSION_REALM
                && !MapSystem.getInstance().isCurrentHellMazeMap()) {
            dayNightCycle.update(deltaTime);
        }

        // Update weather system
        if (weatherSystem != null) {
            weatherSystem.update(deltaTime, screenWidth, screenHeight);
        }

        IllusionRealmSystem.getInstance().update(deltaTime);

        // Update enemies
        checkEnemiesUpdate(deltaTime);

        // Check if pending melee attack should apply damage (after lunge completes)
        applyPendingMeleeAttack();

        // Check dash-enemy collision (撞退敌人)
        checkDashEnemyCollision();

        // Check attack lunge-enemy collision (前冲撞退敌人)
        checkAttackLungeEnemyCollision();

        releasePendingSwordQi();

        // Update projectiles
        checkProjectileUpdate(deltaTime);

        // Update ground items (auto-pickup)
        updateGroundItems();

        // Update all NPCs (idle animation + animal wandering)
        if (!MapSystem.getInstance().isCurrentMazaMap()) {
            MapInfo curMap = MapSystem.getInstance().getCurMapInfo();
            if (curMap != null) {
                List<Npc> npcList = NpcSystem.getInstance().getMapNpcList(curMap.getMapId());
                for (Npc npc : npcList) {
                    npc.update(deltaTime);
                }
            }
        }

        // Check enemy attacks on player
        checkEnemyAttacksOnPlayer();

        // 战宠系统: 处理捕捉请求 → 更新战宠AI → 战宠攻击敌人 → 敌人攻击战宠
        if (captureRequested) {
            captureRequested = false;
            processCaptureRequest();
        }
        updatePets(deltaTime);
        checkPetAttacksOnEnemies();
        checkEnemyAttacksOnPets();
        updateCapturableFlag();

        // Update GameUI
        if (gameUI != null) {
            gameUI.update();
        }

        // Update damage numbers
        if (damageNumbers != null) {
            for (int i = damageNumbers.size() - 1; i >= 0; i--) {
                DamageNumber num = damageNumbers.get(i);
                num.update(deltaTime);

                if (!num.isActive()) {
                    damageNumbers.remove(i);
                }
            }
        }

        // Update floating texts (rewards, level up, etc.)
        if (floatingTexts != null) {
            for (int i = floatingTexts.size() - 1; i >= 0; i--) {
                FloatingText text = floatingTexts.get(i);
                text.update(deltaTime);

                if (!text.isActive()) {
                    floatingTexts.remove(i);
                }
            }

            // Calculate stacking offsets so texts don't overlap
            // Each text checks how many older active texts are near its base position
            float stackSpacing = 50f;
            float proximityThreshold = 150f;
            for (int i = 0; i < floatingTexts.size(); i++) {
                FloatingText current = floatingTexts.get(i);
                int stackIndex = 0;
                for (int j = 0; j < i; j++) {
                    FloatingText older = floatingTexts.get(j);
                    if (older.isActive()
                            && Math.abs(current.getX() - older.getX()) < proximityThreshold
                            && Math.abs(current.getY() - older.getY()) < proximityThreshold) {
                        stackIndex++;
                    }
                }
                // Push upward: newer texts go higher (negative Y)
                current.setStackOffsetY(-stackIndex * stackSpacing);
            }
        }

        // Check and recover huoli/tili every minute (based on game runtime, not system time)
        checkResourceRecovery(deltaTime);

        // Update active skill effects
        for (int i = activeSkillEffects.size() - 1; i >= 0; i--) {
            SkillEffect effect = activeSkillEffects.get(i);
            effect.update(enemies);
            if (!effect.isActive()) {
                activeSkillEffects.remove(i);
            }
        }

        for (int i = dragonLightningZones.size() - 1; i >= 0; i--) {
            DragonLightningZone zone = dragonLightningZones.get(i);
            while (zone.consumePulse()) {
                applyDragonLightningPulse(zone);
            }
            if (!zone.isActive()) {
                dragonLightningZones.remove(i);
            }
        }

        for (int i = seaSealZones.size() - 1; i >= 0; i--) {
            SeaSealZone seal = seaSealZones.get(i);
            if (seal.consumeDrop()) {
                applySeaSealDrop(seal);
            }
            if (!seal.isActive()) {
                seaSealZones.remove(i);
            }
        }

        for (int i = tidePools.size() - 1; i >= 0; i--) {
            TidePool pool = tidePools.get(i);
            if (pool.consumeTick()) {
                applyTidePoolTick(pool);
            }
            if (!pool.isActive()) {
                tidePools.remove(i);
            }
        }
    }

    /**
     * Check if it's time to recover huoli and tili
     * Uses accumulated game time instead of system time to avoid counting offline time
     */
    private void checkResourceRecovery(long deltaTime) {
        accumulatedRecoveryTime += deltaTime;
        if (accumulatedRecoveryTime >= RECOVERY_INTERVAL) {
            RoleSystem.getInstance().recoverOverTime();

            // Reset accumulated time (keep remainder for accuracy)
            accumulatedRecoveryTime -= RECOVERY_INTERVAL;
        }

        if (ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_ZaiSheng)) {
            //再生
            accumulatedRecoveryTime_ZaiSheng += deltaTime;
            int limitTime = (int) (5000 + Math.random() * 2000);
            if (accumulatedRecoveryTime_ZaiSheng >= limitTime) {
                RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                int recoveryValue = (int) (roleInfo.getBloodCap() * 0.03);
                int before = player != null ? player.getHealth() : roleInfo.getHp();
                if (player != null) {
                    player.healBlood(recoveryValue);
                } else {
                    roleInfo.setHp(Math.min(roleInfo.getBloodCap(), roleInfo.getHp() + recoveryValue));
                }
                int healed = player != null ? Math.max(0, player.getHealth() - before) : recoveryValue;
                GameEngine.getInstance().showFloatText("气血+" + healed, FloatingText.Type.HEAL);

                // Reset accumulated time (keep remainder for accuracy)
                accumulatedRecoveryTime_ZaiSheng -= limitTime;
            }
        }

        if (ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_MingSi)) {
            //冥思
            accumulatedRecoveryTime_MingSi += deltaTime;
            int limitTime = (int) (5000 + Math.random() * 2000);
            if (accumulatedRecoveryTime_MingSi >= limitTime) {
                RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                int recoveryValue = (int) (roleInfo.getMagicCap() * 0.03);
                roleInfo.setMp(Math.min(roleInfo.getMagicCap(), roleInfo.getMp() + recoveryValue));
                GameEngine.getInstance().showFloatText("魔法+" + recoveryValue, FloatingText.Type.HEAL_MAGIC);

                // Reset accumulated time (keep remainder for accuracy)
                accumulatedRecoveryTime_MingSi -= limitTime;
            }
        }
    }

    /**
     * Check if enemies are attacking the player
     */
    private void checkEnemyAttacksOnPlayer() {
        if (enemies == null || enemies.isEmpty()) {
            return;
        }
        // 幽魂牢内玩家不可被怪物攻击
        if (isPlayerInPrison()) {
            return;
        }
        // 遍历副本：命中后可能 handlePlayerDeath → clearEnemies，不可直接 foreach 原列表
        java.util.List<Enemy> attackPass = new java.util.ArrayList<>(enemies);

        for (Enemy enemy : attackPass) {
            if (!enemy.isAlive() || enemy.isExhibitionMode()) continue;

            // Check if enemy is in attacking state and close to player
            if (enemy.getState() == Enemy.State.ATTACKING) {
                float dx = enemy.getX() - player.getX();
                float dy = enemy.getY() - player.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // If within attack range, deal damage
                if (distance < enemy.getAttackRange()) { // Attack range
                    // 检测敌人前摇结束刚刚攻击
                    if (enemy.consumeAttackFired()) {
                        // 攻击对抗: 玩家正面攻击时免疫普通敌人的物理攻击。BOSS 打穿这层防御。
                        if (!enemy.isBossTier() && player.isAttacking() && isEnemyInFrontOfPlayer(enemy)) {
                            // 玩家正面攻击中，免疫该次物理伤害
                            damageNumbers.add(new DamageNumber(
                                    player.getX(),
                                    player.getY() - 50,
                                    -3 // 特殊值: 招架
                            ));
                            continue;
                        }

                        if (enemy.isBossTier()) {
                            enemy.requestBossRetreat();
                        }

                        // 首领/精英/BOSS 近战时有概率改用火球（距离加权，专属法术怪物除外）
                        boolean usedMagic = false;
                        if (enemy.rollMeleeSpellSubstitution(distance)) {
                            spawnEnemyGenericFireballs(enemy, player.getX(), player.getY());
                            usedMagic = true;
                        }

                        // If didn't use magic, perform physical attack
                        if (!usedMagic) {
                            boolean died = false;
                            AttackResult attackResult = BattleUtil.caculateEnemyAttackDamage(enemy);
                            if (attackResult.isHit) {
                                int damage = attackResult.damageValue;
                                died = player.takeDamage(damage);

                                // Create floating damage number
                                damageNumbers.add(new DamageNumber(
                                        player.getX(),
                                        player.getY() - 40,
                                        damage,
                                        attackResult.isCrit
                                ));

                                // 吸血撕咬: 命中后回复敌人生命
                                int drainHeal = enemy.consumeDrainHeal();
                                if (drainHeal > 0) {
                                    enemy.heal(drainHeal);
                                    damageNumbers.add(DamageNumber.heal(
                                            enemy.getX(),
                                            enemy.getY() - 30,
                                            drainHeal
                                    ));
                                }

                                // 毒击类攻击命中：按 Enemy.getPoisonHitChance 尝试中毒，已中毒不刷新
                                if (Enemy.causesPoisonOnHit(enemy.getCurrentAttackType())) {
                                    if (player.tryApplyPoisonDebuff(enemy.getPoisonHitChance())) {
                                        showCenterToast("中毒了！", 1500);
                                    }
                                }

                                // 敌人攻击玩家击退效果: 从敌人位置推开玩家
                                float knockbackForce = 150f;
                                long knockbackDuration = 150;
                                // 老虎猛扑击退更强
                                if (enemy instanceof com.game.dream.enemy.Tiger) {
                                    knockbackForce = 280f;
                                    knockbackDuration = 250;
                                } else if (enemy instanceof TurtleChancellor
                                        && enemy.getCurrentAttackType() == Enemy.AttackType.POUNCE) {
                                    knockbackForce = 260f;
                                    knockbackDuration = 220;
                                } else if (enemy instanceof SeaDragonKing
                                        && enemy.getCurrentAttackType() == Enemy.AttackType.SPIN_ATTACK) {
                                    knockbackForce = 320f;
                                    knockbackDuration = 240;
                                }
                                player.applyKnockback(enemy.getX(), enemy.getY(), knockbackForce, knockbackDuration);

                                // BOSS跳跃砸击: 命中后眩晕1秒
                                if (enemy.isBossTier()
                                        && enemy.getCurrentAttackType() == Enemy.AttackType.LEAP_SLAM) {
                                    player.applyCC(Character.CrowdControlType.STUN, 1000);
                                    showCenterToast("你被泰山压顶眩晕了!", 1000);
                                }

                                // 孤魂怨灵哀嘆: 命中后减速30%，持续1.5秒
                                if (enemy instanceof LonelySpirit) {
                                    player.applyCC(Character.CrowdControlType.SLOW, 1500);
                                    showCenterToast("怨灵哀嘆侵蚀了你，移动速度降低!", 1200);
                                }

                                // 缚链怨魂锁链缠绕: 命中后定身1.5秒
                                if (enemy instanceof ChainBoundWraith) {
                                    player.applyCC(Character.CrowdControlType.ROOT, 1500);
                                    showCenterToast("生锈锁链缠绕了你，无法移动!", 1200);
                                }

                                // 野鬼暴怒冲锋: 命中后强力击退
                                if (enemy instanceof SavageWraith
                                        && enemy.getCurrentAttackType() == Enemy.AttackType.CHARGE) {
                                    player.applyKnockback(enemy.getX(), enemy.getY(), 450f, 350);
                                }

                                // 鬼将冲锋突刺: 命中后眩晕0.8秒
                                if (enemy instanceof GhostGeneral
                                        && enemy.getCurrentAttackType() == Enemy.AttackType.CHARGE) {
                                    player.applyCC(Character.CrowdControlType.STUN, 800);
                                    showCenterToast("鬼将冲锋将你撞晕了!", 1000);
                                }

                                // 鬼将跳劈震地: 命中后定身1秒（所有等级）
                                if (enemy instanceof GhostGeneral
                                        && enemy.getCurrentAttackType() == Enemy.AttackType.LEAP_SLAM) {
                                    player.applyCC(Character.CrowdControlType.ROOT, 1000);
                                    showCenterToast("震地碎石卡住了你!", 1000);
                                }
                            } else {
                                //未命中
                                damageNumbers.add(new DamageNumber(
                                        enemy.getX(),
                                        enemy.getY() - 30,
                                        -1
                                ));
                            }

                            if (died) {
                                handlePlayerDeath();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check attack lunge-enemy collision: push back enemies when player lunges through them
     */
    private void checkAttackLungeEnemyCollision() {
        if (!player.isAttackLunging()) return;

        java.util.Set<Enemy> hitSet = player.getLungeHitEnemies();
        float playerX = player.getX();
        float playerY = player.getY();
        int playerSize = player.getSize();

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            if (hitSet.contains(enemy)) continue; // Already knocked back this lunge

            float ex = enemy.getX();
            float ey = enemy.getY();
            int eSize = enemy.getSize();

            float dx = ex - playerX;
            float dy = ey - playerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float collisionDist = (playerSize + eSize) * 0.5f;

            if (dist < collisionDist) {
                // Mark as hit
                hitSet.add(enemy);

                if (enemy.isBossTier()) {
                    if (!enemy.isCommittedAction()) {
                        enemy.applyKnockback(playerX, playerY, 120f, 80);
                    }
                } else {
                    enemy.applyKnockback(playerX, playerY, 400f, 350);
                    enemy.applyCC(Character.CrowdControlType.STUN, 250);
                    if (enemy.isWindingUp()) {
                        enemy.setWindingUpFalse();
                    }
                }

                LogUtil.d("Attack lunge hit enemy: " + enemy.getName());
            }
        }
    }

    /**
     * Check if enemy is in front of the player (based on facing direction)
     */
    private boolean isEnemyInFrontOfPlayer(Enemy enemy) {
        float dx = enemy.getX() - player.getX();
        float dy = enemy.getY() - player.getY();
        int facing = player.getFacingDirection();

        // Check if enemy is in the general direction the player is facing
        switch (facing) {
            case 0: return dy > -20;  // Player facing down, enemy below
            case 1: return dy < 20;   // Player facing up, enemy above
            case 2: return dx < 20;   // Player facing left, enemy to the left
            case 3: return dx > -20;  // Player facing right, enemy to the right
            default: return true;
        }
    }

    /**
     * Check dash-enemy collision: knock back and stun enemies without dealing damage
     */
    private void checkDashEnemyCollision() {
        if (!player.isDashing()) return;

        java.util.Set<Enemy> hitSet = player.getDashHitEnemies();
        float playerX = player.getX();
        float playerY = player.getY();
        int playerSize = player.getSize();

        // Calculate dash direction vector
        float dirX = 0, dirY = 0;
        switch (player.getDashDirection()) {
            case 0: dirY = 1; break;  // Down
            case 1: dirY = -1; break; // Up
            case 2: dirX = -1; break; // Left
            case 3: dirX = 1; break;  // Right
        }

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            if (hitSet.contains(enemy)) continue; // Already hit this dash

            float ex = enemy.getX();
            float ey = enemy.getY();
            int eSize = enemy.getSize();

            // Circle collision detection
            float dx = ex - playerX;
            float dy = ey - playerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float collisionDist = (playerSize + eSize) * 0.5f;

            if (dist < collisionDist) {
                // Mark as hit
                hitSet.add(enemy);

                int dashDmg = 0;
                if (enemy.isBossTier()) {
                    if (!enemy.isCommittedAction()) {
                        enemy.applyKnockback(playerX, playerY, 140f, 90);
                    }
                } else {
                    enemy.applyKnockback(playerX, playerY, player.getDashKnockbackDistance(), 300);
                    enemy.applyCC(Character.CrowdControlType.STUN, player.getDashStunDuration());
                    if (enemy.isWindingUp()) {
                        enemy.setWindingUpFalse();
                    }
                    dashDmg = player.getDashHitDamage();
                    if (dashDmg > 0) {
                        enemy.takeDamage(dashDmg);
                    }
                }

                if (dashDmg > 0) {
                    damageNumbers.add(new DamageNumber(ex, ey - eSize - 10, dashDmg, false));
                } else {
                    damageNumbers.add(new DamageNumber(
                            ex,
                            ey - eSize - 10,
                            -2 // Special value for "撞退" indicator
                    ));
                }

                LogUtil.d("Dash hit enemy: " + enemy.getName());
            }
        }
    }

    private void applySeaDragonAura(float originX, float originY, float range, long duration) {
        if (player == null || isPlayerInPrison()) {
            return;
        }
        if (MapSystem.getInstance().isLocationSafe(player.getX(), player.getY())) {
            return;
        }
        float dx = player.getX() - originX;
        float dy = player.getY() - originY;
        if (dx * dx + dy * dy < range * range) {
            player.applyCC(Character.CrowdControlType.SLOW, duration);
        }
    }

    private void checkEnemiesUpdate(long deltaTime) {
        if (enemies != null) {
            boolean bossWound = false;
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy enemy = enemies.get(i);

                // Only update AI for enemies within a reasonable distance
                float dx = enemy.getX() - player.getX();
                float dy = enemy.getY() - player.getY();
                float distanceSquared = dx * dx + dy * dy;

                // 只对距离玩家 2000 像素内的怪物更新 AI
                float updateThreshold = 2000 * 2000; // 2000^2 to avoid sqrt
                if (distanceSquared < updateThreshold) {
                    if (enemy.isBossTier() && enemy.isAlive() && enemy.isBossPhaseTwo()) {
                        bossWound = true;
                    }
                    if (enemy.isPhaseTwoMinion() && enemy.isAlive() && distanceSquared < 180f * 180f) {
                        player.applyCC(Character.CrowdControlType.SLOW, 500);
                    }
                    if (enemy.isExhibitionMode()) {
                        enemy.updateDisplayOnly(deltaTime);
                    } else {
                    int[][] map = MapSystem.getInstance().getCurMapInfo().getMapData();
                    enemy.update(deltaTime, player.getX(), player.getY(), map, getCurrentMapWidth(), getCurrentMapHeight());

                    if (enemy.consumePostRetreatFireball()) {
                        float[] predicted = player.getPredictedPosition(0.4f);
                        spawnEnemyGenericFireballs(enemy, predicted[0], predicted[1]);
                        enemy.requestGapClose();
                    }

                    // Check if elite/leader enemy is casting spell while chasing
                    if (enemy.isCastingSpell()) {
                        if (enemy.getState() == Enemy.State.CHASING) {
                            spawnEnemyGenericFireballs(enemy, player.getX(), player.getY());
                        }
                        enemy.resetCastingState();
                        if (enemy.isBossTier()) {
                            enemy.requestGapClose();
                        } else {
                            enemy.requestBossRetreat();
                        }
                    }

                    // BOSS召唤小弟: 血量低于50%时触发，每只BOSS只能召唤一次
                    if (enemy.isPendingSummon()) {
                        spawnBossMinions(enemy);
                        enemy.markSummoned();
                    }

                    if (enemy.isPendingPhaseTwoMechanic()) {
                        enemy.consumePhaseTwoMechanic();
                        triggerStageBossPhaseTwo(enemy);
                    }

                    // 狐狸精狐媚法术: 向四周发射6道花瓣
                    if (enemy instanceof FoxSpirit && ((FoxSpirit) enemy).isPendingFoxCharm()) {
                        spawnFoxCharmPetals((FoxSpirit) enemy, false);
                        ((FoxSpirit) enemy).consumeFoxCharm();
                    }

                    // 孤魂幽蓝泪珠: 向玩家发射缓慢飞行的蓝色光球
                    if (enemy instanceof LonelySpirit && ((LonelySpirit) enemy).isPendingSoulTear()) {
                        spawnAimedSpell(enemy, player.getX(), player.getY(), SkillType.ENEMY_SoulTear, false, 4, 30f);
                        ((LonelySpirit) enemy).consumeSoulTear();
                    }

                    // 小青龙水龙弹: 3 颗窄扇，相邻夹角 12 度
                    if (enemy instanceof LittleGreenDragon && ((LittleGreenDragon) enemy).isPendingWaterBolt()) {
                        LittleGreenDragon dragon = (LittleGreenDragon) enemy;
                        spawnAimedSpell(enemy, player.getX(), player.getY(), SkillType.ENEMY_WaterBolt, false,
                                dragon.getWaterBoltCount(), dragon.getWaterBoltSpread());
                        dragon.consumeWaterBolt();
                        enemy.requestBossRetreat();
                    }

                    // 小青龙龙雷: 锁在玩家脚下的雷区，两段范围伤害
                    if (enemy instanceof LittleGreenDragon && ((LittleGreenDragon) enemy).isPendingLightning()) {
                        spawnDragonLightning(enemy, player.getX(), player.getY(), false);
                        ((LittleGreenDragon) enemy).consumeLightning();
                        enemy.requestBossRetreat();
                    }

                    if (enemy instanceof TurtleChancellor && ((TurtleChancellor) enemy).isPendingSeal()) {
                        seaSealZones.add(new SeaSealZone(player.getX(), player.getY(), enemy));
                        ((TurtleChancellor) enemy).consumeSeal();
                        enemy.requestBossRetreat();
                    }

                    if (enemy instanceof SeaDragonPrince) {
                        SeaDragonPrince prince = (SeaDragonPrince) enemy;
                        if (prince.isPendingWaterBolt()) {
                            spawnAimedSpell(enemy, player.getX(), player.getY(), SkillType.ENEMY_WaterBolt, false,
                                    prince.getWaterBoltCount(), prince.getWaterBoltSpread());
                            prince.consumeWaterBolt();
                            enemy.requestBossRetreat();
                        }
                        applySeaDragonAura(prince.getX(), prince.getY(), prince.getAuraRange(), prince.getAuraSlowDuration());
                    }

                    if (enemy instanceof SeaDragonKing) {
                        SeaDragonKing king = (SeaDragonKing) enemy;
                        if (king.isPendingWaterBolt()) {
                            spawnAimedSpell(enemy, player.getX(), player.getY(), SkillType.ENEMY_WaterBolt, false,
                                    king.getWaterBoltCount(), king.getWaterBoltSpread());
                            king.consumeWaterBolt();
                            enemy.requestBossRetreat();
                        }
                        if (king.isPendingLightning()) {
                            spawnDragonLightning(enemy, player.getX(), player.getY(), false);
                            king.consumeLightning();
                            enemy.requestBossRetreat();
                        }
                        applySeaDragonAura(king.getX(), king.getY(), king.getAuraRange(), king.getAuraSlowDuration());
                    }

                    // 小青龙龙威光环：减速玩家
                    if (enemy instanceof LittleGreenDragon) {
                        LittleGreenDragon dragon = (LittleGreenDragon) enemy;
                        float auraDx = player.getX() - enemy.getX();
                        float auraDy = player.getY() - enemy.getY();
                        float auraDist = (float) Math.sqrt(auraDx * auraDx + auraDy * auraDy);
                        if (auraDist < dragon.getDragonAuraRange() && !MapSystem.getInstance().isLocationSafe(player.getX(), player.getY())) {
                            player.applyCC(com.game.dream.figure.Character.CrowdControlType.SLOW,
                                    dragon.getAuraSlowDuration());
                        }
                    }
                    }
                } else {
                    // Far away enemies don't need AI updates
                    // They stay in their current state
                }

                // Remove dead enemies
                if (!enemy.isAlive()) {
                    // 野鬼怨爆: 死亡后膨胀0.5秒再爆炸，对周围玩家造成伤害
                    if (enemy instanceof SavageWraith) {
                        SavageWraith wraith = (SavageWraith) enemy;
                        if (!wraith.hasExploded()) {
                            if (!wraith.shouldExplode()) {
                                // 还在膨胀中，不移除，等待爆炸
                                continue;
                            }
                            // 触发爆炸
                            wraith.markExploded();
                            float expDx = player.getX() - enemy.getX();
                            float expDy = player.getY() - enemy.getY();
                            float expDist = (float) Math.sqrt(expDx * expDx + expDy * expDy);
                            if (expDist < wraith.getExplosionRange()) {
                                int expDamage = wraith.getExplosionDamage();
                                boolean died = player.takeDamage(expDamage);
                                damageNumbers.add(new DamageNumber(
                                        player.getX(), player.getY() - 40,
                                        expDamage, false
                                ));
                                player.applyKnockback(enemy.getX(), enemy.getY(), 350f, 300);
                                showCenterToast("野鬼怨爆!", 1000);
                                if (died) {
                                    handlePlayerDeath();
                                    enemies.remove(i);
                                    return;
                                }
                            }
                        }
                    }

                    if (MapSystem.getInstance().isIllusionRealmMap()
                            && IllusionRealmSystem.getInstance().isActive()) {
                        IllusionRealmSystem.getInstance().onIllusionEnemyKilled();
                        enemies.remove(i);
                        continue;
                    }
                    // Grant reward to player
                    int expReward = enemy.getExperienceReward();
                    int moneyReward = enemy.getMoneyReward();

                    int oldLevel = RoleSystem.getInstance().getRoleInfo().getLevel();
                    RoleSystem.getInstance().addExperience(enemy.getExperienceReward());
                    int newLevel = RoleSystem.getInstance().getRoleInfo().getLevel();
                    RoleSystem.getInstance().addMoney(enemy.getMoneyReward());

                    // 战宠参战经验: 出战且未重伤的战宠在附近时, 获得该怪一半经验
                    Pet expPet = PetSystem.getInstance().getActivePet();
                    if (expPet != null && !expPet.isDowned()) {
                        Enemy expPetBody = expPet.getEntity();
                        if (expPetBody != null) {
                            float petDx = expPetBody.getX() - enemy.getX();
                            float petDy = expPetBody.getY() - enemy.getY();
                            if (petDx * petDx + petDy * petDy <= 800f * 800f) {
                                int petExp = Math.max(1, expReward / 2);
                                boolean petLeveled = PetSystem.getInstance().addExp(expPet, petExp);
                                floatingTexts.add(new FloatingText(
                                        expPetBody.getX(), expPetBody.getY() - 140,
                                        "战宠+" + petExp + " 经验",
                                        FloatingText.Type.EXPERIENCE
                                ));
                                if (petLeveled) {
                                    floatingTexts.add(new FloatingText(
                                            expPetBody.getX(), expPetBody.getY() - 190,
                                            "战宠升级! Lv." + expPet.getLevel(),
                                            FloatingText.Type.LEVEL_UP
                                    ));
                                }
                            }
                        }
                    }

                    // Add messages to message log
                    addMessage("+" + expReward + " 经验", com.game.dream.panel.MessagePanel.MessageType.EXPERIENCE);
                    if (moneyReward > 0) {
                        addMessage("+" + moneyReward + " 金钱", com.game.dream.panel.MessagePanel.MessageType.MONEY);
                    }

                    // Create floating texts for rewards
                    floatingTexts.add(new FloatingText(
                            enemy.getX(),
                            enemy.getY() - 120,
                            "+" + expReward + " 经验",
                            FloatingText.Type.EXPERIENCE
                    ));

                    if (moneyReward > 0) {
                        floatingTexts.add(new FloatingText(
                                enemy.getX(),
                                enemy.getY() - 170,
                                "+" + moneyReward + " 金钱",
                                FloatingText.Type.MONEY
                        ));
                    }

                    // Get item drops from enemy - spawn as ground items
                    List<ItemStack> drops = enemy.getDrops();
                    for (ItemStack drop : drops) {
                        spawnGroundItem(enemy.getX(), enemy.getY(), drop.getItem(), drop.getQuantity());
                    }

                    // If player leveled up, show special notification
                    if (newLevel > oldLevel) {
                        floatingTexts.add(new FloatingText(
                                player.getX(),
                                player.getY() - 220,
                                "升级! Lv." + newLevel,
                                FloatingText.Type.LEVEL_UP
                        ));
                        addMessage("恭喜升级! 当前等级: " + newLevel, com.game.dream.panel.MessagePanel.MessageType.LEVEL_UP);
                    }

                    enemies.remove(i);
                }
            }
            player.setBossCombatWound(bossWound);
        } else {
            player.setBossCombatWound(false);
        }
    }

    /**
     * 更新地面物品 - 检测玩家靠近自动拾取
     */
    private void updateGroundItems() {
        if (groundItems == null || groundItems.isEmpty()) return;

        float playerX = player.getX();
        float playerY = player.getY();

        for (int i = groundItems.size() - 1; i >= 0; i--) {
            GroundItem groundItem = groundItems.get(i);
            if (groundItem.isPickedUp()) {
                groundItems.remove(i);
                continue;
            }

            // 检测玩家是否在拾取范围内
            if (groundItem.isPlayerInRange(playerX, playerY)) {
                // 添加到背包
                boolean added = ItemSystem.getInstance().addItem(groundItem.getItem(), groundItem.getQuantity());
                if (added) {
                    groundItem.setPickedUp(true);
                    groundItems.remove(i);

                    // 显示拾取提示
                    String itemName = groundItem.getItem().getName();
                    if (groundItem.getQuantity() > 1) {
                        itemName += " x" + groundItem.getQuantity();
                    }
                    showFloatText("获得 " + itemName, FloatingText.Type.MONEY);
                    addMessage("获得 " + itemName, com.game.dream.panel.MessagePanel.MessageType.ITEM);
                }
            }
        }
    }

    /**
     * 在地面生成掉落物品
     */
    public void spawnGroundItem(float x, float y, com.game.dream.item.Item item, int quantity) {
        groundItems.add(new GroundItem(x, y, item, quantity));
    }

    private void checkProjectileUpdate(long deltaTime) {
        if (projectiles != null) {
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile proj = projectiles.get(i);
                proj.update(deltaTime);

                // Check collisions with enemies
                if (proj.isActive()) {

                    if (proj.isEnemyProjectile()) {
                        // 幽魂牢内玩家不可被怪物攻击
                        if (isPlayerInPrison()) {
                            proj.deactivate();
                            continue;
                        }
                        // Check if this is an enemy projectile hitting the player
                        float dx = proj.getX() - player.getX();
                        float dy = proj.getY() - player.getY();
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);

                        if (distance < (proj.getSize() + player.getSize())) {
                            // Enemy projectile hit player
                            boolean died = false;
                            int skillLevel = 1;
                            Enemy enemy = proj.getFromEnemy();
                            switch (enemy.getEnemyLevel()){
                                case BOSS:
                                case STAGE_BOSS:
                                    skillLevel = 10;
                                    break;
                                case ELITE:
                                    skillLevel = 5;
                                    break;
                                case LEADER:
                                    skillLevel = 3;
                                    break;
                            }
                            AttackResult attackResult = BattleUtil.caculateEnemyCasterDamage(enemy, proj.getSkillType(), skillLevel);
                            if (attackResult != null) {
                                if (attackResult.isHit) {
                                    int damage = attackResult.damageValue;
                                    if (damage > 0) {
                                        died = player.takeDamage(damage);

                                        // Create floating damage number above enemy
                                        damageNumbers.add(new DamageNumber(
                                                player.getX(),
                                                player.getY() - 30,
                                                damage,
                                                attackResult.isCrit
                                        ));

                                        // 法术弹幕击退玩家
                                        player.applyKnockback(proj.getX(), proj.getY(), 120f, 150);
                                    }
                                } else {
                                    //未命中
                                    damageNumbers.add(new DamageNumber(
                                            player.getX(),
                                            player.getY() - 30,
                                            -1
                                    ));
                                }

                                if (died) {
                                    handlePlayerDeath();
                                }
                            }

                            // Handle Special Effects on Player
                            if (proj.getEffectType() == Projectile.EffectType.ROOT) {
                                player.applyCC(com.game.dream.figure.Character.CrowdControlType.ROOT, 2000);
                                showCenterToast("你被定身了!", 1000);
                            }

                            // 狐媚花瓣眩晕效果(30%概率眩晕1秒)
                            if (proj.getStunChance() > 0 && Math.random() < proj.getStunChance()) {
                                player.applyCC(com.game.dream.figure.Character.CrowdControlType.STUN, proj.getStunDuration());
                                showCenterToast("你被狐媚眩晕了!", 1000);
                            }

                            proj.deactivate();
                            continue;
                        }
                    } else {
                        for (Enemy enemy : enemies) {
                            if (proj.hasAlreadyHit(enemy)) {
                                continue;
                            }
                            if (proj.checkCollision(enemy)) {
                                if (proj.isFromPet()) {
                                    // 战宠火球: 用战宠攻击力结算(法术, 受宝宝法术修炼加成), 只伤敌人
                                    AttackResult petProjResult = BattleUtil.calculatePetDamage(proj.getFromEnemy(), enemy, true);
                                    if (petProjResult != null && petProjResult.isHit && petProjResult.damageValue > 0) {
                                        enemy.takeDamage(petProjResult.damageValue);
                                        damageNumbers.add(new DamageNumber(
                                                enemy.getX(), enemy.getY() - 30,
                                                petProjResult.damageValue, petProjResult.isCrit
                                        ));
                                    } else {
                                        damageNumbers.add(new DamageNumber(
                                                enemy.getX(), enemy.getY() - 30, -1
                                        ));
                                    }
                                    // 战宠狐媚: 命中后同样有概率眩晕敌人
                                    if (proj.getStunChance() > 0 && Math.random() < proj.getStunChance()) {
                                        enemy.applyCC(Character.CrowdControlType.STUN, proj.getStunDuration());
                                    }
                                } else {
                                    // Handle caster damage (with projectile info for fireball level effects)
                                    handlePlayerCasterDamageToEnemy(enemy, proj);

                                    // Handle Special Effects
                                    handlePlayerCasterEffectToEnemy(enemy, proj);
                                }

                                proj.registerHit(enemy);
                                if (!proj.isActive()) {
                                    break;
                                }
                            }
                        }
                    }

                }

                // Remove inactive projectiles
                if (!proj.isActive()) {
                    projectiles.remove(i);
                }
            }
        }
    }

    // ==================== 战宠系统 ====================

    /** 把 PetSystem 中出战的战宠放入世界(置于玩家身边)。仅在更新/加载线程调用。 */
    private void syncActivePetToWorld() {
        activePets.clear();
        if (player == null) return;
        Pet active = PetSystem.getInstance().getActivePet();
        if (active == null || active.isDowned()) return;
        Enemy body = active.getEntity();
        if (body == null) return;
        body.setPet(true);
        body.setX(player.getX() + 40);
        body.setY(player.getY() + 40);
        activePets.add(active);
    }

    /** 供 UI 调用：请求捕捉(实际在更新线程执行)。 */
    public void requestCapture() {
        captureRequested = true;
    }

    /** 弹出输入框为战宠改名(需在 UI 线程调用；触摸事件本身即在 UI 线程)。 */
    public void promptRenamePet(final Pet pet) {
        if (pet == null || !(activityContext instanceof Activity)) {
            return;
        }
        final Activity activity = (Activity) activityContext;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final EditText input = new EditText(activity);
                String cur = pet.getName() != null ? pet.getName() : "";
                input.setText(cur);
                input.setSelection(input.getText().length());
                input.setSingleLine(true);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});

                FrameLayout container = new FrameLayout(activity);
                int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(pad, pad / 2, pad, 0);
                container.addView(input, lp);

                new AlertDialog.Builder(activity)
                        .setTitle("战宠改名")
                        .setView(container)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nm = input.getText().toString().trim();
                                if (nm.isEmpty()) {
                                    showCenterToast("名字不能为空", 1200);
                                    return;
                                }
                                pet.setName(nm);
                                showCenterToast("已改名为「" + nm + "」", 1200);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }

    /** 供 UI 查询：玩家附近是否有可捕捉目标(用于捕捉按钮高亮)。 */
    public boolean hasCapturableTarget() {
        return capturableTargetNearby;
    }

    /** 每帧在更新线程计算“附近是否有可捕捉目标”，供绘制线程读取(volatile, 避免跨线程遍历 enemies)。 */
    private void updateCapturableFlag() {
        boolean found = false;
        if (player != null && enemies != null) {
            final float range = 220f;
            for (Enemy e : enemies) {
                if (e == null || !e.isAlive() || e.isPet()) continue;
                if (e.isBossTier()) continue;
                if (e.isCaptureLocked()) continue;
                if (e.getMaxHealth() > 0
                        && e.getHealth() / (float) e.getMaxHealth() > PetSystem.CAPTURE_HP_THRESHOLD) continue;
                float dx = e.getX() - player.getX();
                float dy = e.getY() - player.getY();
                if (dx * dx + dy * dy <= range * range) {
                    found = true;
                    break;
                }
            }
        }
        capturableTargetNearby = found;
    }

    /** 处理捕捉请求(更新线程)：找最近可捕捉目标 → 判定 → 成功则移除该怪并出战。 */
    private void processCaptureRequest() {
        if (player == null || enemies == null) return;
        long now = System.currentTimeMillis();
        if (now - lastCaptureTime < CAPTURE_COOLDOWN) return;

        Enemy target = null;
        Enemy lockedNearby = null;
        float best = Float.MAX_VALUE;
        float captureRange = 220f;
        for (Enemy e : enemies) {
            if (e == null || !e.isAlive() || e.isPet()) continue;
            if (e.isBossTier()) continue;
            float dx = e.getX() - player.getX();
            float dy = e.getY() - player.getY();
            float d = dx * dx + dy * dy;
            if (d > captureRange * captureRange) continue;
            if (e.isCaptureLocked()) {
                lockedNearby = e;
                continue;
            }
            if (d < best) {
                best = d;
                target = e;
            }
        }
        if (target == null) {
            if (lockedNearby != null) {
                showCenterToast("「" + lockedNearby.getName() + "」刚刚挣脱，稍后再试", 1500);
            } else {
                showCenterToast("附近没有可捕捉的怪物", 1200);
            }
            return;
        }
        lastCaptureTime = now;
        PetSystem.CaptureOutcome outcome = PetSystem.getInstance().tryCapture(target);
        switch (outcome) {
            case SUCCESS:
                enemies.remove(target);
                showCenterToast("成功收服「" + target.getName() + "」！", 1800);
                syncActivePetToWorld();
                break;
            case FAIL_NOT_WEAK:
                showCenterToast("「" + target.getName() + "」血量太高，先削弱它！", 1500);
                break;
            case FAIL_BOSS:
                showCenterToast("首领级怪物无法被捕捉！", 1500);
                break;
            case FAIL_FULL:
                showCenterToast("战宠已满(上限 " + PetSystem.getInstance().getMaxRoster() + " 只)，请先放生或提升等级！", 1500);
                break;
            case FAIL_NO_CHARM:
                showCenterToast("没有捕捉符，去杂货商人处买一些再来", 1800);
                break;
            case FAIL_LOCKED:
                showCenterToast("「" + target.getName() + "」刚刚挣脱，稍后再试", 1500);
                break;
            case FAIL_ROLL:
                showCenterToast("「" + target.getName() + "」挣脱了捕捉，恢复了部分气血！", 1800);
                break;
            default:
                showCenterToast("无法捕捉该怪物", 1500);
                break;
        }
    }

    /** 更新出战战宠：有敌人则复用 Enemy AI 追击攻击，否则跟随玩家。 */
    private void updatePets(long deltaTime) {
        Pet sysActive = PetSystem.getInstance().getActivePet();
        if (sysActive == null || sysActive.isDowned()) {
            activePets.clear();
        } else if (activePets.isEmpty() || activePets.get(0) != sysActive) {
            syncActivePetToWorld();
        }
        if (activePets.isEmpty() || player == null) return;

        int[][] map = null;
        try {
            map = MapSystem.getInstance().getCurMapInfo().getMapData();
        } catch (Exception ignored) {
        }
        int mapW = getCurrentMapWidth();
        int mapH = getCurrentMapHeight();
        float deltaSeconds = deltaTime / 1000f;
        final float petDetectRange = 520f;

        for (Pet pet : new java.util.ArrayList<>(activePets)) {
            Enemy body = pet.getEntity();
            if (body == null || !body.isAlive()) continue;

            Enemy nearest = null;
            float nearestDistSq = Float.MAX_VALUE;
            if (enemies != null) {
                for (Enemy e : enemies) {
                    if (e == null || !e.isAlive() || e.isPet()) continue;
                    float dx = e.getX() - body.getX();
                    float dy = e.getY() - body.getY();
                    float d = dx * dx + dy * dy;
                    if (d < nearestDistSq) {
                        nearestDistSq = d;
                        nearest = e;
                    }
                }
            }
            boolean hasTarget = nearest != null && nearestDistSq <= petDetectRange * petDetectRange;

            if (hasTarget) {
                if (body.getState() == Enemy.State.IDLE) {
                    body.setState(Enemy.State.CHASING);
                }
                body.update(deltaTime, nearest.getX(), nearest.getY(), map, mapW, mapH);
                if (body.isCastingSpell()) {
                    if (body.getState() == Enemy.State.CHASING) {
                        spawnPetGenericFireballs(body, nearest.getX(), nearest.getY());
                    }
                    body.resetCastingState();
                }
                spawnPetSpeciesSpells(body, nearest.getX(), nearest.getY());
            } else {
                float aimX = nearest != null ? nearest.getX() : body.getX();
                float aimY = nearest != null ? nearest.getY() : body.getY();
                if (body.getState() == Enemy.State.ATTACKING) {
                    // 目标已不在范围内: 让战宠自身攻击状态机安全收尾(处理前摇/猛扑等子状态),
                    // 传入自身坐标不产生位移, 避免卡在攻击动画
                    body.update(deltaTime, body.getX(), body.getY(), map, mapW, mapH);
                    spawnPetSpeciesSpells(body, aimX, aimY);
                } else {
                    // 脱离战斗时打断未完成的吟唱，避免 pending/前摇卡住后续施法
                    if (body.isSpellWindingUp()) {
                        body.cancelSpellCasting();
                    }
                    spawnPetSpeciesSpells(body, aimX, aimY);
                    float pdx = player.getX() - body.getX();
                    float pdy = player.getY() - body.getY();
                    float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
                    if (pDist > 600f) {
                        body.setX(player.getX() + 40);
                        body.setY(player.getY() + 40);
                        body.petIdleTick();
                    } else if (pDist > 110f) {
                        body.petMoveTowards(player.getX(), player.getY(), deltaSeconds);
                    } else {
                        body.petIdleTick();
                    }
                }
            }
        }
    }

    /**
     * 法术型战宠的专属技能弹道。不看怪物等级：普通（NORMAL）小青龙/狐狸精/孤魂出战后同样施放。
     * 弹道归属战宠，只伤敌人。
     */
    private void spawnPetSpeciesSpells(Enemy body, float targetX, float targetY) {
        if (body instanceof FoxSpirit && ((FoxSpirit) body).isPendingFoxCharm()) {
            spawnFoxCharmPetals((FoxSpirit) body, true);
            ((FoxSpirit) body).consumeFoxCharm();
        }
        if (body instanceof LonelySpirit && ((LonelySpirit) body).isPendingSoulTear()) {
            spawnAimedSpell(body, targetX, targetY, SkillType.ENEMY_SoulTear, true, 4, 30f);
            ((LonelySpirit) body).consumeSoulTear();
        }
        if (body instanceof LittleGreenDragon) {
            LittleGreenDragon dragon = (LittleGreenDragon) body;
                if (dragon.isPendingWaterBolt()) {
                    spawnAimedSpell(dragon, targetX, targetY, SkillType.ENEMY_WaterBolt, true,
                            dragon.getWaterBoltCount(), dragon.getWaterBoltSpread());
                    dragon.consumeWaterBolt();
                }
            if (dragon.isPendingLightning()) {
                spawnDragonLightning(dragon, targetX, targetY, true);
                dragon.consumeLightning();
            }
        }
    }

    /** 朝目标发射一颗法术弹。fromPet 为 true 时只伤敌人。 */
    private void spawnAimedSpell(Enemy caster, float targetX, float targetY, SkillType skillType, boolean fromPet) {
        spawnAimedSpell(caster, targetX, targetY, skillType, fromPet, 1, 0f);
    }

    /**
     * 朝目标方向发射若干法术弹。spreadDegrees 为相邻两颗的夹角，整组以瞄准方向为中心对称展开。
     */
    private void spawnAimedSpell(Enemy caster, float targetX, float targetY, SkillType skillType,
                                 boolean fromPet, int count, float spreadDegrees) {
        float ex = caster.getX();
        float ey = caster.getY();
        float dx = targetX - ex;
        float dy = targetY - ey;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float baseAngle = (float) Math.atan2(dy, dx);
        float flyDistance = Math.max(distance, 250f);
        float spreadStep = (float) Math.toRadians(spreadDegrees);
        float startAngle = baseAngle - spreadStep * (count - 1) / 2f;
        for (int i = 0; i < count; i++) {
            float angle = startAngle + spreadStep * i;
            float tx = ex + (float) Math.cos(angle) * flyDistance;
            float ty = ey + (float) Math.sin(angle) * flyDistance;
            Projectile projectile = new Projectile(ex, ey, tx, ty, skillType);
            if (fromPet) {
                projectile.setFromPet(caster);
            } else {
                projectile.setFromEnemy(caster);
            }
            projectiles.add(projectile);
        }
    }

    /** 关卡 BOSS 半血时的本层技能，每只只触发一次。 */
    private void triggerStageBossPhaseTwo(Enemy boss) {
        if (player == null || boss == null) {
            return;
        }
        if (boss instanceof TurtleChancellor) {
            ((TurtleChancellor) boss).beginPhaseShell();
            seaSealZones.add(new SeaSealZone(player.getX(), player.getY(), boss));
            showCenterToast("丞相令！镇海印落下了！", 1600);
        } else if (boss instanceof SeaDragonPrince) {
            dragonLightningZones.add(new DragonLightningZone(player.getX(), player.getY(), boss, false, true, 0));
            ((SeaDragonPrince) boss).beginRagePounce();
            showCenterToast("敖丙召来了怒涛！", 1600);
        } else if (boss instanceof SeaDragonKing) {
            float px = player.getX();
            float py = player.getY();
            for (int i = 0; i < 3; i++) {
                dragonLightningZones.add(new DragonLightningZone(px, py, boss, false, true, i * 600L));
            }
            tidePools.add(new TidePool(boss.getX(), boss.getY(), boss));
            showCenterToast("敖广掀起了龙王潮！", 1600);
        } else {
            showCenterToast("首领陷入狂暴，疗伤效果减弱！", 1600);
        }
    }

    private void applySeaSealDrop(SeaSealZone seal) {
        Enemy caster = seal.getCaster();
        if (caster == null) {
            return;
        }
        if (!isPlayerInPrison() && player != null
                && SeaSealZone.contains(seal.getX(), seal.getY(), player.getX(), player.getY())) {
            int skillLevel = caster.isBossTier() ? 10 : 1;
            AttackResult result = BattleUtil.caculateEnemyCasterDamage(caster, SkillType.ENEMY_WaterBolt, skillLevel);
            if (result != null && result.isHit && result.damageValue > 0) {
                boolean died = player.takeDamage(result.damageValue);
                damageNumbers.add(new DamageNumber(player.getX(), player.getY() - 30, result.damageValue, result.isCrit));
                if (died) {
                    handlePlayerDeath();
                }
            }
            player.applyCC(Character.CrowdControlType.ROOT, 1000);
        }
        for (Pet pet : new ArrayList<>(activePets)) {
            Enemy body = pet.getEntity();
            if (body == null || !body.isAlive() || pet.isDowned()) {
                continue;
            }
            if (!SeaSealZone.contains(seal.getX(), seal.getY(), body.getX(), body.getY())) {
                continue;
            }
            AttackResult result = BattleUtil.calculatePetDamage(caster, body, true);
            if (result != null && result.isHit && result.damageValue > 0) {
                boolean died = body.takeDamage(result.damageValue);
                damageNumbers.add(new DamageNumber(body.getX(), body.getY() - 40, result.damageValue, result.isCrit));
                if (died || !body.isAlive()) {
                    pet.setDowned(true);
                    PetSystem.getInstance().recallActive();
                    activePets.clear();
                    showCenterToast("战宠「" + pet.getName() + "」重伤，已收回！", 1800);
                    break;
                }
            }
        }
    }

    private void applyTidePoolTick(TidePool pool) {
        if (player != null && !isPlayerInPrison()
                && TidePool.contains(pool.getX(), pool.getY(), player.getX(), player.getY())) {
            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
            int dmg = Math.max(1, (int) (roleInfo.getBloodCap() * 0.03f));
            boolean died = player.takeDamage(dmg);
            damageNumbers.add(new DamageNumber(player.getX(), player.getY() - 40, dmg));
            if (died) {
                handlePlayerDeath();
            }
        }
        for (Pet pet : new ArrayList<>(activePets)) {
            Enemy body = pet.getEntity();
            if (body == null || !body.isAlive() || pet.isDowned()) {
                continue;
            }
            if (!TidePool.contains(pool.getX(), pool.getY(), body.getX(), body.getY())) {
                continue;
            }
            int dmg = Math.max(1, (int) (body.getMaxHealth() * 0.03f));
            boolean died = body.takeDamage(dmg);
            damageNumbers.add(new DamageNumber(body.getX(), body.getY() - 40, dmg, false));
            if (died || !body.isAlive()) {
                pet.setDowned(true);
                PetSystem.getInstance().recallActive();
                activePets.clear();
                showCenterToast("战宠「" + pet.getName() + "」重伤，已收回！", 1800);
                break;
            }
        }
        Enemy caster = pool.getCaster();
        if (caster != null && caster.isAlive()
                && TidePool.contains(pool.getX(), pool.getY(), caster.getX(), caster.getY())) {
            int heal = Math.max(1, (int) (caster.getMaxHealth() * 0.02f));
            caster.heal(heal);
            damageNumbers.add(DamageNumber.heal(caster.getX(), caster.getY() - 30, heal));
        }
    }

    /** 在目标当前位置落下龙雷。圈不再跟随。fromPet 时只伤敌人。 */
    private void spawnDragonLightning(Enemy caster, float targetX, float targetY, boolean fromPet) {
        dragonLightningZones.add(new DragonLightningZone(targetX, targetY, caster, fromPet));
    }

    private void applyDragonLightningPulse(DragonLightningZone zone) {
        Enemy caster = zone.getCaster();
        if (caster == null) {
            return;
        }
        if (zone.isFromPet()) {
            if (enemies == null) {
                return;
            }
            for (Enemy enemy : new ArrayList<>(enemies)) {
                if (enemy == null || !enemy.isAlive() || enemy.isPet()) {
                    continue;
                }
                if (!DragonLightningZone.contains(zone.getX(), zone.getY(), enemy.getX(), enemy.getY())) {
                    continue;
                }
                AttackResult result = BattleUtil.calculatePetDamage(caster, enemy, true);
                applyScaledHit(result, enemy.getX(), enemy.getY() - 30);
                if (result != null && result.isHit && result.damageValue > 0) {
                    enemy.takeDamage(result.damageValue);
                }
            }
            return;
        }

        if (!isPlayerInPrison() && player != null
                && DragonLightningZone.contains(zone.getX(), zone.getY(), player.getX(), player.getY())) {
            int skillLevel = 1;
            if (caster.isBossTier()) {
                skillLevel = 10;
            } else if (caster.getEnemyLevel() == Enemy.EnemyLevel.ELITE) {
                skillLevel = 5;
            }
            SkillType spell = zone.isWater() ? SkillType.ENEMY_WaterBolt : SkillType.ENEMY_DragonLightning;
            AttackResult result = BattleUtil.caculateEnemyCasterDamage(caster, spell, skillLevel);
            scaleLightningDamage(result);
            if (result != null && result.isHit && result.damageValue > 0) {
                boolean died = player.takeDamage(result.damageValue);
                damageNumbers.add(new DamageNumber(player.getX(), player.getY() - 30, result.damageValue, result.isCrit));
                if (died) {
                    handlePlayerDeath();
                }
            } else if (result != null) {
                damageNumbers.add(new DamageNumber(player.getX(), player.getY() - 30, -1));
            }
        }

        for (Pet pet : new ArrayList<>(activePets)) {
            Enemy body = pet.getEntity();
            if (body == null || !body.isAlive() || pet.isDowned()) {
                continue;
            }
            if (!DragonLightningZone.contains(zone.getX(), zone.getY(), body.getX(), body.getY())) {
                continue;
            }
            AttackResult result = BattleUtil.calculatePetDamage(caster, body, true);
            scaleLightningDamage(result);
            if (result != null && result.isHit && result.damageValue > 0) {
                boolean died = body.takeDamage(result.damageValue);
                damageNumbers.add(new DamageNumber(body.getX(), body.getY() - 40, result.damageValue, result.isCrit));
                if (died || !body.isAlive()) {
                    pet.setDowned(true);
                    PetSystem.getInstance().recallActive();
                    activePets.clear();
                    showCenterToast("战宠「" + pet.getName() + "」重伤，已收回！", 1800);
                    break;
                }
            } else if (result != null) {
                damageNumbers.add(new DamageNumber(body.getX(), body.getY() - 40, -1));
            }
        }
    }

    /** 战宠龙雷打敌人：先按八成改伤害数字，再由调用方扣血。 */
    private void applyScaledHit(AttackResult result, float numberX, float numberY) {
        scaleLightningDamage(result);
        if (result != null && result.isHit && result.damageValue > 0) {
            damageNumbers.add(new DamageNumber(numberX, numberY, result.damageValue, result.isCrit));
        } else if (result != null) {
            damageNumbers.add(new DamageNumber(numberX, numberY, -1));
        }
    }

    private void scaleLightningDamage(AttackResult result) {
        if (result != null && result.isHit && result.damageValue > 0) {
            result.damageValue = Math.max(1, Math.round(result.damageValue * DragonLightningZone.DAMAGE_RATIO));
        }
    }

    /** 战宠近战命中判定：对攻击范围内的敌对怪造成伤害。 */
    private void checkPetAttacksOnEnemies() {
        if (activePets.isEmpty() || enemies == null || enemies.isEmpty()) return;
        for (Pet pet : new java.util.ArrayList<>(activePets)) {
            Enemy body = pet.getEntity();
            if (body == null || !body.isAlive()) continue;
            if (body.getState() != Enemy.State.ATTACKING) continue;
            if (!body.consumeAttackFired()) continue;

            Enemy target = null;
            float range = body.getAttackRange();
            for (Enemy e : enemies) {
                if (e == null || !e.isAlive() || e.isPet()) continue;
                float dx = e.getX() - body.getX();
                float dy = e.getY() - body.getY();
                float reach = range + e.getSize() * 0.5f;
                if (dx * dx + dy * dy <= reach * reach) {
                    target = e;
                    break;
                }
            }
            if (target == null) continue;

            AttackResult r = BattleUtil.calculatePetDamage(body, target);
            if (r != null && r.isHit) {
                target.takeDamage(r.damageValue);
                damageNumbers.add(new DamageNumber(target.getX(), target.getY() - 40, r.damageValue, r.isCrit));
                int drain = body.consumeDrainHeal();
                if (drain > 0) {
                    body.heal(drain);
                    damageNumbers.add(DamageNumber.heal(body.getX(), body.getY() - 30, drain));
                }
            }
        }
    }

    /** 敌对怪攻击状态且范围内有战宠时，对战宠造成伤害；战宠血量归零则重伤收回。 */
    private void checkEnemyAttacksOnPets() {
        if (activePets.isEmpty() || enemies == null || enemies.isEmpty()) return;
        for (Pet pet : new java.util.ArrayList<>(activePets)) {
            Enemy body = pet.getEntity();
            if (body == null || !body.isAlive()) continue;
            for (Enemy e : enemies) {
                if (e == null || !e.isAlive() || e.isPet()) continue;
                if (e.getState() != Enemy.State.ATTACKING) continue;
                float dx = e.getX() - body.getX();
                float dy = e.getY() - body.getY();
                float reach = e.getAttackRange() + body.getSize() * 0.3f;
                if (dx * dx + dy * dy > reach * reach) continue;
                if (!e.consumeAttackFired()) continue;

                AttackResult r = BattleUtil.calculatePetDamage(e, body);
                if (r != null && r.isHit) {
                    boolean died = body.takeDamage(r.damageValue);
                    damageNumbers.add(new DamageNumber(body.getX(), body.getY() - 40, r.damageValue, r.isCrit));
                    if (died || !body.isAlive()) {
                        pet.setDowned(true);
                        PetSystem.getInstance().recallActive();
                        activePets.clear();
                        showCenterToast("战宠「" + pet.getName() + "」重伤，已收回！", 1800);
                    }
                }
                break;
            }
        }
    }

    /** 施法型战宠朝目标发射火球(归属战宠，不伤玩家)。 */
    private void spawnPetGenericFireballs(Enemy pet, float targetX, float targetY) {
        int count = pet.getGenericFireballCount();
        if (count <= 0) return;
        float ex = pet.getX();
        float ey = pet.getY();
        float dx = targetX - ex;
        float dy = targetY - ey;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float baseAngle = (float) Math.atan2(dy, dx);
        float flyDistance = Math.max(distance, 250f);
        float spreadStep = (count == 1) ? 0f : (count == 2 ? 0.18f : 0.22f);
        float startAngle = baseAngle - spreadStep * (count - 1) / 2f;
        for (int i = 0; i < count; i++) {
            float angle = startAngle + spreadStep * i;
            float tx = ex + (float) Math.cos(angle) * flyDistance;
            float ty = ey + (float) Math.sin(angle) * flyDistance;
            Projectile magicProj = new Projectile(ex, ey, tx, ty, SkillType.MAIN_FIREBALL);
            magicProj.setFromPet(pet);
            projectiles.add(magicProj);
        }
    }

    /** 绘制战宠的友方绿血条(身体上方)与名字/等级(身体下方, 参照人物 Character.drawName 位置)。 */
    private void drawPetOverlay(Canvas canvas, Enemy body, Pet pet, int offX, int offY) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        float sx = body.getX() + offX;
        float sy = body.getY() + offY;
        float size = body.getSize();
        // 友方绿血条(身体上方)
        float barW = size * 1.1f;
        float barH = 5f;
        float barX = sx - barW / 2f;
        float barY = sy - size * 0.95f;
        p.setColor(Color.argb(180, 30, 30, 30));
        canvas.drawRoundRect(barX, barY, barX + barW, barY + barH, 2, 2, p);
        float ratio = body.getMaxHealth() <= 0 ? 0f : body.getHealth() / (float) body.getMaxHealth();
        p.setColor(Color.argb(230, 80, 220, 110));
        canvas.drawRoundRect(barX, barY, barX + barW * ratio, barY + barH, 2, 2, p);

        // 名字/等级(身体下方, 与人物 Character.drawName 一致: cy + 45*scale, scale=2 → +90)
        String label = pet.getName() + " Lv." + pet.getLevel() + " ★";
        p.setTextSize(24);
        p.setTextAlign(Paint.Align.CENTER);
        float nameY = sy + 90;
        // 黑色描边提升可读性(参照 drawName 的阴影偏移)
        p.setColor(Color.BLACK);
        canvas.drawText(label, sx + 1, nameY + 1, p);
        p.setColor(Color.argb(235, 130, 255, 160));
        canvas.drawText(label, sx, nameY, p);
    }

    private void updateCamera() {
        // Center camera on player
        cameraX = player.getX() - screenWidth / 2;
        cameraY = player.getY() - screenHeight / 2;

        // Clamp camera to map bounds
        int mapW = getCurrentMapWidth();
        int mapH = getCurrentMapHeight();
        cameraX = Math.max(0, Math.min(cameraX, mapW - screenWidth));
        cameraY = Math.max(0, Math.min(cameraY, mapH - screenHeight));
    }

    // Static getters for MapRenderer
    public static float getCameraX() {
        return cameraX;
    }

    public static float getCameraY() {
        return cameraY;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public void draw(Canvas canvas) {
        // 异步加载中只绘制黑屏 + 提示文字
        if (isLoading) {
            canvas.drawColor(Color.BLACK);
            if (screenWidth > 0 && screenHeight > 0) {
                Paint loadingPaint = new Paint();
                loadingPaint.setColor(Color.WHITE);
                loadingPaint.setTextSize(40);
                loadingPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("正在加载地图...", screenWidth / 2f, screenHeight / 2f, loadingPaint);
            }
            return;
        }

        // Draw background
        canvas.drawColor(Color.BLACK);

        // Draw map using MapRenderer
        MapSystem.getInstance().render(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // Draw day-night overlay (after map, before player, 与 update() 保持一致：海底/海底迷宫/地府/幻境关闭)
        int curMapIdForDraw = MapSystem.getInstance().getCurrentMapId();
        if (dayNightCycle != null && curMapIdForDraw != MapSystem.MAP_ID_DONGHAI_SEABED
                && !MapSystem.isUnderwaterMazeId(curMapIdForDraw)
                && curMapIdForDraw != MapSystem.MAP_ID_NETHERWORLD
                && curMapIdForDraw != MapSystem.MAP_ID_ILLUSION_REALM) {
            dayNightCycle.draw(canvas, screenWidth, screenHeight);
        }

        // Draw enemies (only visible ones)
        if (enemies != null) {
            List<Enemy> enemiesCopy = new ArrayList<>(enemies);
            for (Enemy enemy : enemiesCopy) {
                // Check if enemy is within visible area (with some padding)
                float padding = 100; // Draw enemies slightly outside screen for smooth entry
                if (isEnemyVisible(enemy, padding)) {
                    enemy.draw(canvas, (int) -cameraX, (int) -cameraY);
                }
            }
        }

        // Draw active pets (友方: 精灵图 + 绿血条 + 名字/等级)
        if (!activePets.isEmpty()) {
            int petOffX = (int) -cameraX;
            int petOffY = (int) -cameraY;
            for (Pet pet : new ArrayList<>(activePets)) {
                Enemy body = pet.getEntity();
                if (body == null || !body.isAlive()) continue;
                body.onDraw(canvas, petOffX, petOffY);
                drawPetOverlay(canvas, body, pet, petOffX, petOffY);
            }
        }

        // Draw npcs
        List<Npc> npcList = NpcSystem.getInstance().getMapNpcList(MapSystem.getInstance().getCurMapInfo().getMapId());
        npcList.sort((n1, n2) -> Float.compare(n1.getY(), n2.getY()));
        boolean isNightNow = dayNightCycle != null && dayNightCycle.isNight();
        for (Npc npc : npcList) {
            if (npc.isNightOnly() && !isNightNow) {
                // 白天绘制标记（提示玩家此处夜间有NPC出现）
                drawNightNpcMarker(canvas, npc, -cameraX, -cameraY);
            } else {
                npc.draw(canvas, -cameraX, -cameraY);
            }
        }

        // Draw treasure chests (in maze)
        if (MapSystem.getInstance().isCurrentMazaMap() && MazeSystem.getInstance().isInitialized()) {
            for (TreasureChest chest : MazeSystem.getInstance().getTreasureChests()) {
                chest.draw(canvas, -cameraX, -cameraY);
            }
            com.game.dream.npc.MazeToken villageToken = MazeSystem.getInstance().getVillageToken();
            if (villageToken != null) {
                villageToken.draw(canvas, -cameraX, -cameraY);
            }
        }

        // Draw ground items (below player, above map)
        if (groundItems != null) {
            List<GroundItem> copyGroundItems = new ArrayList<>(groundItems);
            for (GroundItem groundItem : copyGroundItems) {
                groundItem.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }

        com.game.dream.utils.TreasureMapUtil.drawMarkers(canvas, cameraX, cameraY);

        // Draw projectiles
        if (projectiles != null) {
            List<Projectile> projectilesCopy = new ArrayList<>(projectiles);
            for (Projectile proj : projectilesCopy) {
                proj.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }

        // Draw player
        player.draw(canvas, (int) -cameraX, (int) -cameraY);

        // Draw damage numbers (above characters)
        if (damageNumbers != null) {
            List<DamageNumber> copyDamageNumbers = new ArrayList(damageNumbers);
            for (DamageNumber num : copyDamageNumbers) {
                num.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }

        // Draw floating texts (rewards, level up notifications)
        if (floatingTexts != null) {
            List<FloatingText> copyFloatingTexts = new ArrayList<>(floatingTexts);
            for (FloatingText text : copyFloatingTexts) {
                text.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }

        // Draw active skill effects (below UI but above map)
        if (activeSkillEffects != null) {
            List<SkillEffect> copySkillEffects = new ArrayList<>(activeSkillEffects);
            for (SkillEffect effect : copySkillEffects) {
                effect.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }
        if (!dragonLightningZones.isEmpty()) {
            List<DragonLightningZone> copyZones = new ArrayList<>(dragonLightningZones);
            for (DragonLightningZone zone : copyZones) {
                zone.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }
        if (!seaSealZones.isEmpty()) {
            for (SeaSealZone seal : new ArrayList<>(seaSealZones)) {
                seal.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }
        if (!tidePools.isEmpty()) {
            for (TidePool pool : new ArrayList<>(tidePools)) {
                pool.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }

        // Draw weather effects (海底地图、海底迷宫、地府不显示天气)
        int drawMapId = MapSystem.getInstance().getCurrentMapId();
        if (weatherSystem != null && drawMapId != MapSystem.MAP_ID_DONGHAI_SEABED
                && !MapSystem.isUnderwaterMazeId(drawMapId)
                && drawMapId != MapSystem.MAP_ID_NETHERWORLD) {
            weatherSystem.draw(canvas);
        }

        if (gameUI != null) {
            gameUI.draw(canvas);
        }
    }

    public boolean handleTouch(MotionEvent event) {
        if (isLoading) return false;

        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        boolean handled = false;

        // Get the coordinates of the pointer that triggered this event
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        if (gameUI != null) {
            handled = gameUI.handleTouch(event);
        }

        // 检查是否点击了 Npc
        if (!handled && event.getAction() == MotionEvent.ACTION_DOWN) {
            float worldX = event.getX() + cameraX;
            float worldY = event.getY() + cameraY;
            boolean isNightForNpc = dayNightCycle != null && dayNightCycle.isNight();
            List<Npc> npcList = NpcSystem.getInstance().getMapNpcList(MapSystem.getInstance().getCurMapInfo().getMapId());
            for (Npc npc : npcList) {
                // 夜间专属 NPC 白天不可交互
                if (npc.isNightOnly() && !isNightForNpc) continue;
                if (npc.isTouched(worldX, worldY)) {
                    NpcSystem.getInstance().startConversation(npc);
                    return true;
                }
            }

            // 检查是否点击了宝箱 (迷宫中)
            if (MapSystem.getInstance().isCurrentMazaMap() && MazeSystem.getInstance().isInitialized()) {
                com.game.dream.npc.MazeToken villageToken = MazeSystem.getInstance().getVillageToken();
                if (villageToken != null && villageToken.isTouched(worldX, worldY)) {
                    com.game.dream.quest.SideQuest007.tryCollectToken();
                    return true;
                }
                TreasureChest chest = MazeSystem.getInstance().checkTreasureChestClick(worldX, worldY);
                if (chest != null) {
                    chest.open();
                    return true;
                }
            }
        }

        return handled;
    }

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;

        if (gameUI != null) {
            gameUI.setScreenSize(width, height);
        }
    }

    /**
     * Check if enemy is within the visible screen area
     */
    private boolean isEnemyVisible(Enemy enemy, float padding) {
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        // Calculate visible area bounds
        float visibleLeft = cameraX - padding;
        float visibleRight = cameraX + screenWidth + padding;
        float visibleTop = cameraY - padding;
        float visibleBottom = cameraY + screenHeight + padding;

        // Check if enemy is within visible area
        return enemyX >= visibleLeft &&
                enemyX <= visibleRight &&
                enemyY >= visibleTop &&
                enemyY <= visibleBottom;
    }

    /**
     * 绘制夜间专属 NPC 的白天标记（幽绿色鬼火标记 + 文字提示）
     */
    private void drawNightNpcMarker(Canvas canvas, Npc npc, float offsetX, float offsetY) {
        float cx = npc.getX() + offsetX + npc.getSize() / 2f;
        float cy = npc.getY() + offsetY + npc.getSize() / 2f;

        // 视锥剪除
        if (cx < -100 || cx > screenWidth + 100 || cy < -100 || cy > screenHeight + 100) return;

        Paint markerPaint = new Paint();
        markerPaint.setAntiAlias(true);

        // 绘制幽绿色鬼火光球（半透明脉动效果）
        float pulse = (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.3 + 0.7);
        int alpha = (int) (120 * pulse);

        // 外层光晕
        markerPaint.setColor(Color.argb(alpha / 3, 80, 200, 120));
        canvas.drawCircle(cx, cy, 35, markerPaint);

        // 内层光球
        markerPaint.setColor(Color.argb(alpha, 100, 255, 150));
        canvas.drawCircle(cx, cy, 15, markerPaint);

        // 中心亮点
        markerPaint.setColor(Color.argb((int) (200 * pulse), 200, 255, 220));
        canvas.drawCircle(cx, cy, 6, markerPaint);

        // 文字提示
        markerPaint.setColor(Color.argb(180, 150, 255, 180));
        markerPaint.setTextSize(22);
        markerPaint.setTextAlign(Paint.Align.CENTER);
        markerPaint.setShadowLayer(3, 0, 0, Color.BLACK);
        canvas.drawText("夜晚出现", cx, cy + 50, markerPaint);
        markerPaint.clearShadowLayer();
    }

    /**
     * Show a center screen notification
     */
    public void showNotification(String title, String message, CenterNotification.Type type) {
        if (gameUI != null) {
            gameUI.showNotification(title, message, type);
        }
    }

    /**
     * Show quest completion notification
     */
    public void showQuestComplete(String questName) {
        showNotification("✅ 任务完成", questName, CenterNotification.Type.QUEST_COMPLETE);
    }

    /**
     * Show achievement unlocked notification
     */
    public void showAchievement(String achievementName) {
        showNotification("🏆 成就解锁", achievementName, CenterNotification.Type.ACHIEVEMENT);
    }

    /**
     * Show warning notification
     */
    public void showWarning(String warning) {
        showNotification("⚠️ 警告", warning, CenterNotification.Type.WARNING);
    }

    /**
     * Show center toast
     */
    public void showCenterToast(String message) {
        showCenterToast(message, 1000);
    }

    /**
     * Show center toast
     */
    public void showCenterToast(String message, long durationMillis) {
        if (gameUI != null) {
            gameUI.showCenterToast(message, durationMillis);
        }
    }

    public DialogBox showDialog(String title, String msg) {
        if (gameUI != null) {
            return gameUI.showDialog(title, msg, null, null);
        }
        return null;
    }

    public DialogBox showDialog(String title, String msg, List<String> options, DialogBox.DialogListener listener) {
        if (gameUI != null) {
            return gameUI.showDialog(title, msg, options, listener);
        }
        return null;
    }

    public EquipSellDialog getEquipSellDialog() {
        if (gameUI != null) {
            return gameUI.getEquipSellDialog();
        }
        return null;
    }

    public void showShopPanel(List<ShopPanel.ShopItem> shopItems) {
        if (gameUI != null) {
            gameUI.showShopPanel(shopItems);
        }
    }

    public void showPracticePanel() {
        if (gameUI != null) {
            gameUI.showPracticePanel();
        }
    }

    public void showAssistPanel() {
        if (gameUI != null) {
            gameUI.showAssistPanel();
        }
    }

    public void showIllusionRealmPanel() {
        if (gameUI != null) {
            gameUI.showIllusionRealmPanel();
        }
    }

    /**
     * Show float text
     */
    public void showFloatText(String text, FloatingText.Type type) {
        floatingTexts.add(new FloatingText(
                player.getX(),
                player.getY() - 160,
                text,
                type
        ));
    }

    /**
     * Show floating text at specific position
     */
    public void showFloatingText(float x, float y, String text, FloatingText.Type type) {
        floatingTexts.add(new FloatingText(x, y, text, type));
    }

    /**
     * 添加消息到消息日志
     */
    public void addMessage(String text, com.game.dream.panel.MessagePanel.MessageType type) {
        if (gameUI != null) {
            gameUI.addMessage(text, type);
        }
    }

    /**
     * 添加消息到消息日志 (自定义颜色)
     */
    public void addMessage(String text, int color) {
        if (gameUI != null) {
            gameUI.addMessage(text, color);
        }
    }

    /** 进入幻境、万妖台前记下当前世界坐标，退出重进仍落在原处，不会把临时场景坐标写入存档。 */
    private void snapshotPersistentLocationIfEnteringTransientMap(int destMapId) {
        if (destMapId != MapSystem.MAP_ID_MONSTER_GALLERY
                && destMapId != MapSystem.MAP_ID_ILLUSION_REALM) {
            return;
        }
        int currentMapId = MapSystem.getInstance().getCurrentMapId();
        if (currentMapId == MapSystem.MAP_ID_MONSTER_GALLERY
                || currentMapId == MapSystem.MAP_ID_ILLUSION_REALM) {
            return;
        }
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        if (roleInfo == null || player == null) {
            return;
        }
        if (currentMapId > 0) {
            roleInfo.setMapId(currentMapId);
        }
        roleInfo.setMapX((int) player.getX());
        roleInfo.setMapY((int) player.getY());
    }

    /**
     * 传送到指定地图
     * @param mapId
     */
    public void teleportToMap(int mapId) {
        snapshotPersistentLocationIfEnteringTransientMap(mapId);
        isLoading = true;
        dragonLightningZones.clear();
        seaSealZones.clear();
        tidePools.clear();
        pendingSwordQi = null;
        pendingSwordQiReleaseTime = 0;
        String mapName = MapSystem.getInstance().getMapName(mapId);
        showCenterToast("正在前往" + mapName + "...");

        MapSystem.getInstance().loadMapAsync(mapId, new MapSystem.OnLoadMapCallback() {
            @Override
            public void onLoadMapFinish(int mapId, int[][] mapData) {
                // 设置玩家位置
                if(MapSystem.getInstance().isCurrentHellMazeMap()){
                    // 地狱迷宫：使用专属生成器的入口坐标（入口可能在任意边，坐标已在通道内）
                    HellMazeGenerator hellGen = MapSystem.getInstance().getHellMazeGenerator();
                    if (hellGen != null) {
                        player.setX(hellGen.getEntranceX());
                        player.setY(hellGen.getEntranceY());
                    }
                } else if(MapSystem.getInstance().isCurrentMazaMap()){
                    //迷宫地图
                    //设置人物位置（入口可能在任意边，坐标已在通道内）
                    MazeGenerator mazeGen = MapSystem.getInstance().getMazeGenerator();
                    if (mazeGen != null) {
                        player.setX(mazeGen.getEntranceX());
                        player.setY(mazeGen.getEntranceY());
                    } else {
                        // 海底迷宫：入口坐标已在通道内
                        UnderwaterMazeGenerator uwGen = MapSystem.getInstance().getUnderwaterMazeGenerator();
                        if (uwGen != null) {
                            player.setX(uwGen.getEntranceX());
                            player.setY(uwGen.getEntranceY());
                        }
                    }
                } else {
                    // 重置迷宫系统
                    MazeSystem.getInstance().reset();

                    //设置人物位置
                    //Pair<Integer, Integer> startPos = MapSystem.getInstance().getStartPosition();
                    //Pair<Integer, Integer> mapXY = MapSystem.getInstance().getMapXY(startPos.first, startPos.second);
                    Pair<Integer, Integer> transPos = MapSystem.getInstance().getCurMapInfo().getTransPos();
                    if (transPos != null) {
                        player.setX(transPos.first);
                        player.setY(transPos.second);
                    }
                }

                if (MapSystem.getInstance().isIllusionRealmMap()) {
                    enemies = new java.util.ArrayList<>();
                    IllusionRealmSystem.getInstance().onIllusionMapReady();
                } else {
                    initializeEnemies();
                }

                // 刷新小地图
                if (gameUI != null) {
                    gameUI.refreshMinimap();
                }

                isLoading = false;
                if (!MapSystem.getInstance().isIllusionRealmMap()) {
                    showCenterToast("到达了" + mapName);
                }
            }
        });
    }

    public void teleportToMapWithPosition(int mapId, float x, float y) {
        isLoading = true;
        dragonLightningZones.clear();
        seaSealZones.clear();
        tidePools.clear();
        pendingSwordQi = null;
        pendingSwordQiReleaseTime = 0;
        MapSystem.getInstance().loadMapAsync(mapId, new MapSystem.OnLoadMapCallback() {
            @Override
            public void onLoadMapFinish(int mapId, int[][] mapData) {
                MazeSystem.getInstance().reset();
                player.setX(x);
                player.setY(y);
                player.setRespawnPoint(x, y);
                if (mapId != MapSystem.MAP_ID_ILLUSION_REALM
                        && mapId != MapSystem.MAP_ID_MONSTER_GALLERY) {
                    RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                    roleInfo.setMapId(mapId);
                    roleInfo.setMapX((int) x);
                    roleInfo.setMapY((int) y);
                }
                initializeEnemies();
                if (gameUI != null) {
                    gameUI.refreshMinimap();
                }
                isLoading = false;
            }
        });
    }

    public void clearEnemies() {
        if (enemies != null) {
            enemies.clear();
        }
    }

    public void addEnemy(Enemy enemy) {
        if (enemies == null) {
            enemies = new java.util.ArrayList<>();
        }
        enemies.add(enemy);
    }

    public int getAliveEnemyCount() {
        if (enemies == null) {
            return 0;
        }
        int count = 0;
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断玩家是否在地府幽魂牢区域内
     */
    private boolean isPlayerInPrison() {
        if (MapSystem.getInstance().getCurrentMapId() != MapSystem.MAP_ID_NETHERWORLD) {
            return false;
        }
        float px = player.getX();
        float py = player.getY();
        return px >= NetherworldMapGenerator.PRISON_X1
                && px <= NetherworldMapGenerator.PRISON_X2
                && py >= NetherworldMapGenerator.PRISON_Y1
                && py <= NetherworldMapGenerator.PRISON_Y2;
    }

    /**
     * 地狱迷宫各层特殊地形机制：
     *   第2层 HELL_BLOOD_POOL→ 持续伤害；HELL_SAFE_STONE → 缓慢回复
     *   第3层 HELL_TRAP → 一次性陷阱伤害，触发后转为普通地板
     */
    private void checkHellMazeHazards(long deltaTime) {
        int curMapId = MapSystem.getInstance().getCurrentMapId();
        if (curMapId < MapSystem.MAP_ID_HELL_MAZE_1 || curMapId > MapSystem.MAP_ID_HELL_MAZE_4) {
            hellBloodPoolTickAcc = 0;
            hellSafeStoneTickAcc = 0;
            return;
        }
        int[][] map = MapSystem.getInstance().getCurMapInfo().getMapData();
        if (map == null) return;
        int tx = (int) (player.getX() / TILE_SIZE);
        int ty = (int) (player.getY() / TILE_SIZE);
        if (ty < 0 || ty >= map.length || tx < 0 || tx >= map[0].length) return;
        int terrain = map[ty][tx];

        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();

        // 第2层：血池 DoT
        if (terrain == HellMazeGenerator.HELL_BLOOD_POOL) {
            hellBloodPoolTickAcc += deltaTime;
            while (hellBloodPoolTickAcc >= HELL_BLOOD_POOL_TICK) {
                hellBloodPoolTickAcc -= HELL_BLOOD_POOL_TICK;
                int dmg = Math.max(1, (int) (roleInfo.getBloodCap() * 0.05f));
                boolean died = player.takeDamage(dmg);
                damageNumbers.add(new DamageNumber(player.getX(), player.getY() - 40, dmg));
                if (died) {
                    handlePlayerDeath();
                    return;
                }
            }
            hellSafeStoneTickAcc = 0;
            return;
        }

        // 第2层：安全石台缓慢回复
        if (terrain == HellMazeGenerator.HELL_SAFE_STONE) {
            hellSafeStoneTickAcc += deltaTime;
            while (hellSafeStoneTickAcc >= HELL_SAFE_STONE_TICK) {
                hellSafeStoneTickAcc -= HELL_SAFE_STONE_TICK;
                int healHp = Math.max(1, (int) (roleInfo.getBloodCap() * 0.02f));
                int healMp = Math.max(1, (int) (roleInfo.getMagicCap() * 0.02f));
                roleInfo.setHp(Math.min(roleInfo.getBloodCap(), roleInfo.getHp() + healHp));
                roleInfo.setMp(Math.min(roleInfo.getMagicCap(), roleInfo.getMp() + healMp));
                floatingTexts.add(new FloatingText(player.getX(), player.getY() - 60,
                        "+" + healHp + " HP", FloatingText.Type.HEAL));
            }
            hellBloodPoolTickAcc = 0;
            return;
        }

        // 第3层：陷阱（一次性触发，触发后地形变为地板）
        if (terrain == HellMazeGenerator.HELL_TRAP) {
            int dmg = Math.max(1, (int) (roleInfo.getBloodCap() * 0.15f));
            boolean died = player.takeDamage(dmg);
            damageNumbers.add(new DamageNumber(player.getX(), player.getY() - 40, dmg, true));
            showCenterToast("触发陷阱！");
            map[ty][tx] = HellMazeGenerator.HELL_FLOOR;
            // 通知渲染器重建对应 chunk 缓存
            MapSystem.getInstance().invalidateHellMazeChunkAt(tx, ty);
            if (died) {
                handlePlayerDeath();
                return;
            }
        }

        hellBloodPoolTickAcc = 0;
        hellSafeStoneTickAcc = 0;
    }

    /**
     * 海底迷宫层间传送。第三层通关后回到东海海底。
     */
    private void handleUnderwaterMazeExit(int curMapId) {
        if (curMapId == MapSystem.MAP_ID_UNDERWATER_MAZE) {
            showCenterToast("你潜入了沉船铁闸……");
            teleportToMap(MapSystem.MAP_ID_UNDERWATER_MAZE_2);
        } else if (curMapId == MapSystem.MAP_ID_UNDERWATER_MAZE_2) {
            showCenterToast("龙宫内海就在前方……");
            teleportToMap(MapSystem.MAP_ID_UNDERWATER_MAZE_3);
        } else if (curMapId == MapSystem.MAP_ID_UNDERWATER_MAZE_3) {
            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
            long expReward = 25000;
            long moneyReward = 10000;
            roleInfo.setExp(roleInfo.getExp() + expReward);
            roleInfo.setMoney(roleInfo.getMoney() + moneyReward);
            addMessage("海底迷宫通关！获得 " + expReward + " 经验、" + moneyReward + " 金钱", Color.YELLOW);
            showCenterToast("海底迷宫通关！获得 " + expReward + " 经验、" + moneyReward + " 金钱");
            teleportToMap(MapSystem.MAP_ID_DONGHAI_SEABED);
        }
    }

    /**
     * 地狱迷宫层间传送逻辑
     */
    private void handleHellMazeExit(int curMapId) {
        if (curMapId == MapSystem.MAP_ID_HELL_MAZE_1) {
            showCenterToast("你踏入了血池炼狱……");
            teleportToMap(MapSystem.MAP_ID_HELL_MAZE_2);
        } else if (curMapId == MapSystem.MAP_ID_HELL_MAZE_2) {
            showCenterToast("你进入了枉死城……");
            teleportToMap(MapSystem.MAP_ID_HELL_MAZE_3);
        } else if (curMapId == MapSystem.MAP_ID_HELL_MAZE_3) {
            showCenterToast("阎罗殿的大门缓缓开启……");
            teleportToMap(MapSystem.MAP_ID_HELL_MAZE_4);
        } else if (curMapId == MapSystem.MAP_ID_HELL_MAZE_4) {
            // 通关奖励
            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
            long expReward = 50000;
            long moneyReward = 20000;
            roleInfo.setExp(roleInfo.getExp() + expReward);
            roleInfo.setMoney(roleInfo.getMoney() + moneyReward);
            showCenterToast("地狱迷宫通关！获得 " + expReward + " 经验、" + moneyReward + " 金钱");
            teleportToMap(MapSystem.MAP_ID_NETHERWORLD);
        }
    }

    private void handlePlayerDeath() {
        if (MapSystem.getInstance().isIllusionRealmMap()
                && IllusionRealmSystem.getInstance().isActive()) {
            IllusionRealmSystem.getInstance().onPlayerDefeated();
            return;
        }
        // 死亡后魂归地府幽魂牢：先扣除死亡惩罚，再恢复状态，最后传送
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        // 死亡惩罚：扣除 1% 经验与 1% 金钱
        long expLoss = roleInfo.getExp() / 100;
        long moneyLoss = roleInfo.getMoney() / 100;
        roleInfo.setExp(roleInfo.getExp() - expLoss);
        roleInfo.setMoney(roleInfo.getMoney() - moneyLoss);
        roleInfo.setHp(roleInfo.getBloodCap());
        roleInfo.setMp(roleInfo.getMagicCap());
        player.clearPoisonDebuff();
        Pair<Integer, Integer> prisonPos = NetherworldMapGenerator.getPrisonSpawnPosition();
        teleportToMapWithPosition(MapSystem.MAP_ID_NETHERWORLD, prisonPos.first, prisonPos.second);
        showCenterToast("你已身死，魂魄被拘入地府幽魂牢……失去 " + expLoss + " 经验、" + moneyLoss + " 金钱");
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Player getPlayer() {
        return player;
    }

    public MapInfo getMap() {
        return MapSystem.getInstance().getCurMapInfo();
    }

    public DayNightCycle getDayNightCycle() {
        return dayNightCycle;
    }

    public WeatherSystem getWeatherSystem() {
        return weatherSystem;
    }

    public void handlePlayerCasterDamageToEnemy(Enemy enemy, SkillType skillType) {
        doCasterDamage(enemy, skillType, null);
    }

    /**
     * Handle player caster damage with projectile info (for fireball level effects)
     */
    public void handlePlayerCasterDamageToEnemy(Enemy enemy, Projectile proj) {
        SkillType skillType = proj != null ? proj.getSkillType() : SkillType.MAIN_FIREBALL;
        doCasterDamage(enemy, skillType, proj);
    }

    /**
     * 共用法术伤害处理逻辑
     */
    private void doCasterDamage(Enemy enemy, SkillType skillType, Projectile proj) {
        AttackResult attackResult = BattleUtil.caculatePlayerCasterDamage(enemy, skillType);
        if (attackResult != null) {
            if (attackResult.isHit) {
                int damage = attackResult.damageValue;

                // 伤害倍率(寒冰术9级+: 伤害+20%)
                float damageMultiplier = (proj != null) ? proj.getDamageMultiplier() : 1.0f;
                damage = (int) (damage * damageMultiplier);

                boolean isCrit = attackResult.isCrit;
                if (proj != null && proj.getSkillLevel() >= 10) {
                    float critChance = 0.2f;
                    if (proj.getSkillType() == SkillType.MAIN_SwordQi) {
                        critChance = 0.15f;
                    }
                    if (Math.random() < critChance) {
                        isCrit = true;
                        damage *= 2;
                    }
                }

                if (damage > 0) {
                    enemy.takeDamage(damage);

                    // Create floating damage number above enemy
                    damageNumbers.add(new DamageNumber(
                            enemy.getX(),
                            enemy.getY() - 30,
                            damage,
                            isCrit
                    ));

                    // 法术击退。BOSS 硬直中的招式不被法术打断。
                    if (enemy.isBossTier()) {
                        if (!enemy.isCommittedAction()) {
                            enemy.applyKnockback(player.getX(), player.getY(), 80f, 80);
                        }
                    } else {
                        enemy.applyKnockback(player.getX(), player.getY(), 200f, 200);
                        enemy.applyCC(Character.CrowdControlType.STUN, 200);
                    }

                    if (proj != null && proj.getBurnDuration() > 0) {
                        int burnDps = Math.max(1, (int) (damage * 0.3f));
                        enemy.applyBurn(burnDps, proj.getBurnDuration());
                    }
                }
            } else {
                //未命中
                damageNumbers.add(new DamageNumber(
                        enemy.getX(),
                        enemy.getY() - 30,
                        -1
                ));
            }
        }
    }

    public void handlePlayerCasterEffectToEnemy(Enemy enemy, Projectile proj) {
        if (proj == null) return;
        Projectile.EffectType effectType = proj.getEffectType();
        
        if (effectType == Projectile.EffectType.ROOT) {
            enemy.applyCC(Character.CrowdControlType.ROOT, 2000);
        } else if (effectType == Projectile.EffectType.SLOW) {
            // 寒冷减速效果 3秒
            enemy.applyCC(Character.CrowdControlType.SLOW, 3000);
        }
        
        // 冰冻效果(概率触发)
        if (proj.getFreezeProbability() > 0 && Math.random() < proj.getFreezeProbability()) {
            // 冰冻效果 2秒
            enemy.applyCC(Character.CrowdControlType.FREEZE, 2000);
        }
    }

    public void updatePlayerDirection(boolean upPressed, boolean downPressed,
                                      boolean leftPressed, boolean rightPressed) {
        player.setMovingUp(upPressed);
        player.setMovingDown(downPressed);
        player.setMovingLeft(leftPressed);
        player.setMovingRight(rightPressed);
    }

    /**
     * Trigger player dash in the given direction
     * @param direction 0=down, 1=up, 2=left, 3=right
     */
    public void triggerPlayerDash(int direction) {
        if (player == null) {
            return;
        }
        if (!player.hasChongCiSkill()) {
            showCenterToast("尚未学会冲刺", 1200);
            return;
        }
        player.startDash(direction);
    }

    public void doAttackAction() {
        // 冷却中不触发攻击前冲
        if (player.isAttackOnCooldown()) return;

        // Trigger attack animation and lunge
        player.triggerAttackAnimation();

        // Set pending attack - damage will be applied after lunge completes
        pendingMeleeAttack = true;
    }

    /**
     * Apply melee attack damage after lunge completes
     */
    private void applyPendingMeleeAttack() {
        if (!pendingMeleeAttack) return;

        // Wait for lunge to finish (or be blocked by enemy/terrain)
        if (player.isAttackLunging()) return;

        pendingMeleeAttack = false;

        // Now apply melee damage from player's current position
        List<EnemyHitInfo> hits = player.performMeleeAttack(enemies);
        if (hits != null) {
            for (EnemyHitInfo hit : hits) {
                if (hit.damage > 0) {
                    damageNumbers.add(new DamageNumber(
                            hit.enemy.getX(),
                            hit.enemy.getY() - 30,
                            hit.damage,
                            hit.isCrit
                    ));

                    // 击退。BOSS 出招途中不受近战硬直打断。
                    if (!hit.enemy.isBossTier()) {
                        hit.enemy.applyKnockback(player.getX(), player.getY(), 300f, 200);
                        hit.enemy.applyCC(Character.CrowdControlType.STUN, 150);
                    } else if (!hit.enemy.isCommittedAction()) {
                        hit.enemy.applyKnockback(player.getX(), player.getY(), 120f, 80);
                    }

                    // 大海龟反伤: 反弹 20% 伤害给玩家
                    int reflectDmg = 0;
                    if (hit.enemy instanceof GiantSeaTurtle) {
                        reflectDmg = ((GiantSeaTurtle) hit.enemy).consumeReflectDamage();
                    } else if (hit.enemy instanceof TurtleChancellor) {
                        reflectDmg = ((TurtleChancellor) hit.enemy).consumeReflectDamage();
                    }
                    if (reflectDmg > 0) {
                        player.takeDamage(reflectDmg);
                        damageNumbers.add(new DamageNumber(
                                player.getX(),
                                player.getY() - 40,
                                reflectDmg,
                                false
                        ));
                    }
                } else {
                    // 未命中也显示
                    damageNumbers.add(new DamageNumber(
                            hit.enemy.getX(),
                            hit.enemy.getY() - 30,
                            hit.damage,
                            hit.isCrit
                    ));
                }
            }
        }
    }

    private void releasePendingSwordQi() {
        if (pendingSwordQi == null || pendingSwordQi.isEmpty()) {
            return;
        }
        if (System.currentTimeMillis() < pendingSwordQiReleaseTime) {
            return;
        }
        flushPendingSwordQi();
    }

    private void flushPendingSwordQi() {
        if (pendingSwordQi != null && !pendingSwordQi.isEmpty() && projectiles != null) {
            projectiles.addAll(pendingSwordQi);
        }
        pendingSwordQi = null;
        pendingSwordQiReleaseTime = 0;
    }

    public void doCasterAction(SkillInfo skillInfo) {
        if (skillInfo != null) {
            LogUtil.d("Casting skill: " + skillInfo.getName());
            SkillStartInfo info = SkillSystem.getInstance().castSkill(skillInfo);
            if (info != null) {
                if (info.getProjectiles() != null && !info.getProjectiles().isEmpty()
                        && skillInfo.getSkillType() == SkillType.MAIN_SwordQi) {
                    player.triggerSwordSwingAnimation();
                    flushPendingSwordQi();
                    pendingSwordQi = new ArrayList<>(info.getProjectiles());
                    pendingSwordQiReleaseTime = System.currentTimeMillis() + SWORD_QI_RELEASE_DELAY;
                } else if (info.getProjectiles() != null) {
                    projectiles.addAll(info.getProjectiles());
                }
                if (info.getSkillEffect() != null) {
                    activeSkillEffects.add(info.getSkillEffect());
                }
                // Handle LightningChainEffect specially since it's not a regular SkillEffect
                if (skillInfo.getSkillType() == com.game.dream.enums.SkillType.MAIN_LIGHTNING
                    && (info.getProjectiles() == null || info.getProjectiles().isEmpty())
                    && info.getSkillEffect() == null) {
                    // Lightning skill creates LightningChainEffect directly
                    int lightningLevel = skillInfo.getLevel();
                    LightningChainEffect lightningEffect = new LightningChainEffect(
                        enemies, player, lightningLevel, player.getX(), player.getY()
                    );
                    lightningEffect.setDamageNumbers(damageNumbers);
                    activeSkillEffects.add(lightningEffect);
                }
            }
        }
    }

    /**
     * 怪物通用火球：首领1颗、精英2颗、BOSS3颗（扇形散射）
     */
    private void spawnEnemyGenericFireballs(Enemy enemy, float targetX, float targetY) {
        int count = enemy.getGenericFireballCount();
        if (count <= 0) {
            return;
        }

        if (enemy.isBossTier() && player != null) {
            float[] predicted = player.getPredictedPosition(0.4f);
            targetX = predicted[0];
            targetY = predicted[1];
        }

        float ex = enemy.getX();
        float ey = enemy.getY();
        float dx = targetX - ex;
        float dy = targetY - ey;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float baseAngle = (float) Math.atan2(dy, dx);
        float flyDistance = Math.max(distance, 250f);

        float spreadStep = (count == 1) ? 0f : (count == 2 ? 0.18f : 0.22f);
        float startAngle = baseAngle - spreadStep * (count - 1) / 2f;

        for (int i = 0; i < count; i++) {
            float angle = startAngle + spreadStep * i;
            float tx = ex + (float) Math.cos(angle) * flyDistance;
            float ty = ey + (float) Math.sin(angle) * flyDistance;

            Projectile magicProj = new Projectile(ex, ey, tx, ty, SkillType.MAIN_FIREBALL);
            magicProj.setFromEnemy(enemy);
            projectiles.add(magicProj);
        }

        LogUtil.d("EnemySpell", enemy.getName() + " 发射了 " + count + " 颗火球");
    }

    /**
     * 狐狸精狐媚法术: 向四周发射6道花瓣，被击中后受到法术伤害，30%概率眩晕1秒
     */
    private void spawnFoxCharmPetals(FoxSpirit fox, boolean fromPet) {
        float foxX = fox.getX();
        float foxY = fox.getY();
        float petalDistance = 300f; // 花瓣飞行目标距离

        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3; // 每60度一道花瓣
            float targetX = foxX + (float) Math.cos(angle) * petalDistance;
            float targetY = foxY + (float) Math.sin(angle) * petalDistance;

            Projectile petal = new Projectile(foxX, foxY, targetX, targetY, SkillType.ENEMY_FoxCharm);
            if (fromPet) {
                petal.setFromPet(fox);
            } else {
                petal.setFromEnemy(fox);
            }
            petal.setStunChance(0.3f);      // 30%概率眩晕
            petal.setStunDuration(1000);     // 眩晕1秒
            projectiles.add(petal);
        }

        LogUtil.d("FoxCharm", fox.getName() + " 施放了狐媚法术!");
    }

    /**
     * BOSS召唤小弟: 根据BOSS类型生成同类型小弟(1精英 + 2-3首领 + 5-8普通)
     */
    private void spawnBossMinions(Enemy boss) {
        if (boss instanceof TurtleChancellor) {
            spawnThemedMinions(boss, 1 + (int) (Math.random() * 2), true);
            return;
        }
        if (boss instanceof SeaDragonPrince || boss instanceof SeaDragonKing) {
            spawnThemedMinions(boss, 2, false);
            return;
        }
        float bossX = boss.getX();
        float bossY = boss.getY();
        java.util.List<Enemy> minions = new java.util.ArrayList<>();

        // 1个精英
        minions.add(createMinionOfType(boss, Enemy.EnemyLevel.ELITE));

        // 2-3个首领
        int leaderCount = 2 + (int) (Math.random() * 2); // 2 or 3
        for (int i = 0; i < leaderCount; i++) {
            minions.add(createMinionOfType(boss, Enemy.EnemyLevel.LEADER));
        }

        // 5-8个普通
        int normalCount = 5 + (int) (Math.random() * 4); // 5 to 8
        for (int i = 0; i < normalCount; i++) {
            minions.add(createMinionOfType(boss, Enemy.EnemyLevel.NORMAL));
        }

        if (boss.isBossPhaseTwo()) {
            for (Enemy minion : minions) {
                if (minion != null) {
                    minion.markPhaseTwoMinion();
                }
            }
        }
        // 将小弟添加到敌人列表
        enemies.addAll(minions);

        // 显示召唤通知
        showCenterToast(boss.getName() + " 召唤了一群小弟!");
        LogUtil.d("BossSummon", boss.getName() + " summoned " + minions.size() + " minions");
    }

    /** 海底关卡 BOSS 的半血召唤：龟丞相召虾兵，龙族召小青龙。 */
    private void spawnThemedMinions(Enemy boss, int count, boolean shrimp) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.random() * Math.PI * 2);
            float distance = 100 + (float) (Math.random() * 150);
            float spawnX = boss.getX() + (float) Math.cos(angle) * distance;
            float spawnY = boss.getY() + (float) Math.sin(angle) * distance;
            Enemy minion = shrimp ? new ShrimpSoldier(spawnX, spawnY) : new LittleGreenDragon(spawnX, spawnY);
            minion.setName(shrimp ? "虾兵" : "小青龙");
            minion.setAggro(60000);
            if (boss.isBossPhaseTwo()) {
                minion.markPhaseTwoMinion();
            }
            enemies.add(minion);
        }
        showCenterToast(shrimp ? "龟丞相召来了虾兵！" : boss.getName() + " 召来了小青龙！", 1600);
    }

    /**
     * 根据BOSS类型创建指定等级的小弟，生成在BOSS周围随机位置
     */
    private Enemy createMinionOfType(Enemy boss, Enemy.EnemyLevel level) {
        float bossX = boss.getX();
        float bossY = boss.getY();

        // 在BOSS周围100-250像素范围内随机生成位置
        float angle = (float) (Math.random() * Math.PI * 2);
        float distance = 100 + (float) (Math.random() * 150);
        float spawnX = bossX + (float) Math.cos(angle) * distance;
        float spawnY = bossY + (float) Math.sin(angle) * distance;

        String minionName = getMinionBaseName(boss);
        Enemy minion = spawnMinionWithLevel(boss, spawnX, spawnY, level);
        minion.setName(formatMinionName(minionName, level));

        // 设置仇恨状态，让小弟主动攻击玩家(仇恨持续60秒)
        minion.setAggro(60000);

        return minion;
    }

    /**
     * 反复生成直到等级匹配，确保属性/技能与目标等级一致
     */
    private Enemy spawnMinionWithLevel(Enemy boss, float spawnX, float spawnY, Enemy.EnemyLevel level) {
        for (int attempt = 0; attempt < 200; attempt++) {
            Enemy minion = createMinionInstance(boss, spawnX, spawnY);
            if (minion.getEnemyLevel() == level) {
                return minion;
            }
        }

        LogUtil.w("BossSummon", "Failed to spawn " + level + " minion after 200 attempts, using last roll");
        return createMinionInstance(boss, spawnX, spawnY);
    }

    private Enemy createMinionInstance(Enemy boss, float spawnX, float spawnY) {
        if (boss instanceof Tiger) {
            return new Tiger(spawnX, spawnY);
        } else if (boss instanceof WildBoar) {
            return new WildBoar(spawnX, spawnY);
        } else if (boss instanceof Viper) {
            return new Viper(spawnX, spawnY);
        } else if (boss instanceof Bandit) {
            return new Bandit(spawnX, spawnY);
        } else if (boss instanceof FoxSpirit) {
            return new FoxSpirit(spawnX, spawnY);
        } else if (boss instanceof ShrimpSoldier) {
            return new ShrimpSoldier(spawnX, spawnY);
        } else if (boss instanceof CrabGeneral) {
            return new CrabGeneral(spawnX, spawnY);
        } else if (boss instanceof Yaksha) {
            return new Yaksha(spawnX, spawnY);
        } else if (boss instanceof LittleGreenDragon) {
            return new LittleGreenDragon(spawnX, spawnY);
        } else if (boss instanceof GiantSeaTurtle) {
            return new GiantSeaTurtle(spawnX, spawnY);
        } else if (boss instanceof Wolf) {
            return new Wolf(spawnX, spawnY);
        } else if (boss instanceof YellowSpringGuide) {
            // 黄泉引路人召唤吸血鬼/幽灵
            if (Math.random() < 0.5) {
                return new Vampire(spawnX, spawnY);
            }
            return new ChainBoundWraith(spawnX, spawnY);
        } else if (boss instanceof BloodPoolDemonKing) {
            // 血池鬼王召唤吸血鬼/幽灵
            if (Math.random() < 0.6) {
                return new Vampire(spawnX, spawnY);
            }
            return new ChainBoundWraith(spawnX, spawnY);
        } else if (boss instanceof JudgeCuiYu) {
            // 判官崔钰：召唤吸血鬼/幽灵/鬼将
            double r = Math.random();
            if (r < 0.35) return new Vampire(spawnX, spawnY);
            if (r < 0.70) return new ChainBoundWraith(spawnX, spawnY);
            return new GhostGeneral(spawnX, spawnY);
        } else if (boss instanceof HellGuardian) {
            // 地狱守卫：混合召唤鬼将/吸血鬼/幽灵
            double r = Math.random();
            if (r < 0.35) return new GhostGeneral(spawnX, spawnY);
            if (r < 0.70) return new Vampire(spawnX, spawnY);
            return new ChainBoundWraith(spawnX, spawnY);
        } else if (boss instanceof KingYanluo) {
            // 阎罗王：召唤鬼将/吸血鬼/幽灵
            double r = Math.random();
            if (r < 0.40) return new GhostGeneral(spawnX, spawnY);
            if (r < 0.70) return new Vampire(spawnX, spawnY);
            return new ChainBoundWraith(spawnX, spawnY);
        }
        return new Wolf(spawnX, spawnY);
    }

    private String getMinionBaseName(Enemy boss) {
        if (boss instanceof Tiger) return "猛虎";
        if (boss instanceof WildBoar) return "野猪";
        if (boss instanceof Viper) return "毒蛇";
        if (boss instanceof Bandit) return "强盗";
        if (boss instanceof FoxSpirit) return "狐狸精";
        if (boss instanceof ShrimpSoldier) return "虾兵";
        if (boss instanceof CrabGeneral) return "蟹将";
        if (boss instanceof Yaksha) return "夜叉";
        if (boss instanceof LittleGreenDragon) return "小青龙";
        if (boss instanceof GiantSeaTurtle) return "大海龟";
        if (boss instanceof YellowSpringGuide) return "亡魂";
        if (boss instanceof BloodPoolDemonKing) return "血奴";
        if (boss instanceof JudgeCuiYu) return "待审亡魂";
        if (boss instanceof HellGuardian) {
            HellGuardian g = (HellGuardian) boss;
            switch (g.getGuardType()) {
                case COW_HEAD: return "牛头小子";
                case HORSE_FACE: return "马面小子";
                case BLACK: return "黑旗亡魂";
                case WHITE: return "白旗亡魂";
            }
            return "守卫亡魂";
        }
        if (boss instanceof KingYanluo) return "轮回亡魂";
        if (boss instanceof Vampire) return "血仆";
        if (boss instanceof ChainBoundWraith) return "同因亡魂";
        if (boss instanceof GhostGeneral) return "冥界战卒";
        return "野狼";
    }

    private String formatMinionName(String baseName, Enemy.EnemyLevel level) {
        if (level == Enemy.EnemyLevel.BOSS) {
            return baseName + "BOSS";
        } else if (level == Enemy.EnemyLevel.ELITE) {
            return baseName + "精英";
        } else if (level == Enemy.EnemyLevel.LEADER) {
            return baseName + "首领";
        }
        return baseName;
    }
}

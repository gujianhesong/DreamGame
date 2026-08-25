package com.game.dream;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.MotionEvent;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.bean.MapInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.bean.SkillStartInfo;
import com.game.dream.enemy.Bandit;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.FoxSpirit;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Viper;
import com.game.dream.enemy.WildBoar;
import com.game.dream.enemy.Wolf;
import com.game.dream.enums.SkillType;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.figure.Character;
import com.game.dream.figure.Player;
import com.game.dream.item.GroundItem;
import com.game.dream.item.ItemStack;
import com.game.dream.map.MapContentManager;
import com.game.dream.map.MazeGenerator;
import com.game.dream.npc.AnimalNpc;
import com.game.dream.npc.Npc;
import com.game.dream.npc.TreasureChest;
import com.game.dream.panel.ShopPanel;
import com.game.dream.skill.SkillEffect;
import com.game.dream.skill.LightningChainEffect;
import com.game.dream.system.DayNightCycle;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
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

    // Pending melee attack (wait for lunge to complete before dealing damage)
    private boolean pendingMeleeAttack = false;

    // 异步加载标志
    private volatile boolean isLoading = false;

    // Projectiles (magic attacks)
    private java.util.List<Projectile> projectiles;

    // Damage numbers
    private java.util.List<DamageNumber> damageNumbers;

    // Floating texts for rewards/damage
    private java.util.List<FloatingText> floatingTexts;

    private GameUI gameUI;


    // Resource recovery tracking (every 60 seconds)
    private long accumulatedRecoveryTime = 0; // Accumulated game time in milliseconds
    private long accumulatedRecoveryTime_ZaiSheng = 0; // Accumulated game time in milliseconds
    private long accumulatedRecoveryTime_MingSi = 0; // Accumulated game time in milliseconds
    private static final long RECOVERY_INTERVAL = 60000; // 60 seconds (1 minute)

    // Active skill effects on the map
    private List<SkillEffect> activeSkillEffects = new ArrayList<>();

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
        this.context = context.getApplicationContext();
        this.accumulatedRecoveryTime = 0; // Initialize recovery timer

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
        if (mapId <= 0) {
            mapId = MapSystem.getInstance().getBornMap().getMapId();
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

        // Update camera to follow player
        updateCamera();

        // 检查迷宫出口传送
        if (MapSystem.getInstance().isCurrentMazaMap() && MazeSystem.getInstance().isInitialized()) {
            if (MazeSystem.getInstance().checkExitPortal(player.getX(), player.getY())) {
                teleportToMap(MapSystem.MAP_ID_QING_XI);
                return; // 传送后本帧不再继续更新
            }
        }

        // Update day-night cycle
        if (dayNightCycle != null) {
            dayNightCycle.update(deltaTime);
        }

        // Update weather system
        if (weatherSystem != null) {
            weatherSystem.update(deltaTime, screenWidth, screenHeight);
        }

        // Update enemies
        checkEnemiesUpdate(deltaTime);

        // Check if pending melee attack should apply damage (after lunge completes)
        applyPendingMeleeAttack();

        // Check dash-enemy collision (撞退敌人)
        checkDashEnemyCollision();

        // Check attack lunge-enemy collision (前冲撞退敌人)
        checkAttackLungeEnemyCollision();

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
                roleInfo.setHp(Math.min(roleInfo.getBloodCap(), roleInfo.getHp() + recoveryValue));
                GameEngine.getInstance().showFloatText("气血+" + recoveryValue, FloatingText.Type.HEAL);

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
        long currentTime = System.currentTimeMillis();

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            // Check if enemy is in attacking state and close to player
            if (enemy.getState() == Enemy.State.ATTACKING) {
                float dx = enemy.getX() - player.getX();
                float dy = enemy.getY() - player.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // If within attack range, deal damage
                if (distance < enemy.getAttackRange()) { // Attack range
                    // 检测敌人前摇结束刚刚攻击
                    if (enemy.consumeAttackFired()) {
                        // 攻击对抗: 玩家正面攻击时免疫敌人物理攻击
                        if (player.isAttacking() && isEnemyInFrontOfPlayer(enemy)) {
                            // 玩家正面攻击中，免疫该次物理伤害
                            damageNumbers.add(new DamageNumber(
                                    player.getX(),
                                    player.getY() - 50,
                                    -3 // 特殊值: 招架
                            ));
                            continue;
                        }

                        // Elite/Leader enemys have chance to use magic attacks
                        boolean usedMagic = false;
                        if (enemy.canCastSpell() && Math.random() < 0.9f) {
                            // Cast magic spell - create projectile
                            float[] targetPos = enemy.castMagicSpell(player.getX(), player.getY());
                            if (targetPos != null) {
                                Projectile magicProj = new Projectile(
                                        enemy.getX(),
                                        enemy.getY(),
                                        targetPos[0],
                                        targetPos[1],
                                        SkillType.MAIN_FIREBALL
                                );
                                magicProj.setFromEnemy(enemy);
                                projectiles.add(magicProj);
                                usedMagic = true;
                                LogUtil.d("Elite Enemy casts fireball!");
                            }
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

                                // 敌人攻击玩家击退效果: 从敌人位置推开玩家
                                float knockbackForce = 150f;
                                long knockbackDuration = 150;
                                // 老虎猛扑击退更强
                                if (enemy instanceof com.game.dream.enemy.Tiger) {
                                    knockbackForce = 280f;
                                    knockbackDuration = 250;
                                }
                                player.applyKnockback(enemy.getX(), enemy.getY(), knockbackForce, knockbackDuration);
                            } else {
                                //未命中
                                damageNumbers.add(new DamageNumber(
                                        enemy.getX(),
                                        enemy.getY() - 30,
                                        -1
                                ));
                            }

                            if (died) {
                                // Player died - respawn
                                player.respawn();
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

                // Knockback enemy away from player
                float knockbackForce = 400f;
                long knockbackDuration = 350;
                enemy.applyKnockback(playerX, playerY, knockbackForce, knockbackDuration);

                // Brief stagger (stun) so enemy doesn't immediately act
                enemy.applyCC(Character.CrowdControlType.STUN, 250);

                // Cancel enemy's wind-up if active
                if (enemy.isWindingUp()) {
                    enemy.setWindingUpFalse();
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

                // Knockback enemy in dash direction
                float knockbackForce = 500f;
                long knockbackDuration = 300;
                // Apply knockback from player position
                enemy.applyKnockback(playerX, playerY, knockbackForce, knockbackDuration);

                // Stun enemy (cannot move or attack)
                enemy.applyCC(Character.CrowdControlType.STUN, 500);

                // Cancel enemy's wind-up if active
                if (enemy.isWindingUp()) {
                    enemy.setWindingUpFalse();
                }

                // Show visual feedback (no damage number, just a text indicator)
                damageNumbers.add(new DamageNumber(
                        ex,
                        ey - eSize - 10,
                        -2 // Special value for "撞退" indicator
                ));

                LogUtil.d("Dash hit enemy: " + enemy.getName());
            }
        }
    }

    private void checkEnemiesUpdate(long deltaTime) {
        if (enemies != null) {
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy enemy = enemies.get(i);

                // Only update AI for enemies within a reasonable distance
                float dx = enemy.getX() - player.getX();
                float dy = enemy.getY() - player.getY();
                float distanceSquared = dx * dx + dy * dy;

                // 只对距离玩家 2000 像素内的怪物更新 AI
                float updateThreshold = 2000 * 2000; // 2000^2 to avoid sqrt
                if (distanceSquared < updateThreshold) {
                    int[][] map = MapSystem.getInstance().getCurMapInfo().getMapData();
                    enemy.update(deltaTime, player.getX(), player.getY(), map, getCurrentMapWidth(), getCurrentMapHeight());

                    // Check if elite/leader enemy is casting spell while chasing
                    if (enemy.isCastingSpell()) {
                        if (enemy.getState() == Enemy.State.CHASING) {
                            // Cast magic spell - create projectile
                            float[] targetPos = enemy.castMagicSpell(player.getX(), player.getY());
                            if (targetPos != null) {
                                Projectile magicProj = new Projectile(
                                        enemy.getX(),
                                        enemy.getY(),
                                        targetPos[0],
                                        targetPos[1],
                                        SkillType.MAIN_FIREBALL
                                );
                                magicProj.setFromEnemy(enemy);
                                projectiles.add(magicProj);
                                LogUtil.d("Elite Enemy casts fireball!");
                            }
                        }
                        // Reset casting state
                        enemy.resetCastingState();
                    }

                    // BOSS召唤小弟: 血量低于50%时触发，每只BOSS只能召唤一次
                    if (enemy.isPendingSummon()) {
                        spawnBossMinions(enemy);
                        enemy.markSummoned();
                    }

                    // 狐狸精狐媚法术: 向四周发射6道花瓣
                    if (enemy instanceof FoxSpirit && ((FoxSpirit) enemy).isPendingFoxCharm()) {
                        spawnFoxCharmPetals((FoxSpirit) enemy);
                        ((FoxSpirit) enemy).consumeFoxCharm();
                    }
                } else {
                    // Far away enemies don't need AI updates
                    // They stay in their current state
                }

                // Remove dead enemies
                if (!enemy.isAlive()) {
                    // Grant reward to player
                    int expReward = enemy.getExperienceReward();
                    int moneyReward = enemy.getMoneyReward();

                    int oldLevel = RoleSystem.getInstance().getRoleInfo().getLevel();
                    RoleSystem.getInstance().addExperience(enemy.getExperienceReward());
                    int newLevel = RoleSystem.getInstance().getRoleInfo().getLevel();
                    RoleSystem.getInstance().addMoney(enemy.getMoneyReward());

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
                        // Check if this is an enemy projectile hitting the player
                        float dx = proj.getX() - player.getX();
                        float dy = proj.getY() - player.getY();
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);

                        if (distance < (proj.getSize() + player.getSize())) {
                            // Enemy projectile hit player
                            boolean died = false;
                            int skillLevel = 1;
                            AttackResult attackResult = BattleUtil.caculateEnemyCasterDamage(proj.getFromEnemy(), proj.getSkillType(), skillLevel);
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
                                    player.respawn();
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
                            if (proj.checkCollision(enemy)) {
                                // Handle caster damage (with projectile info for fireball level effects)
                                handlePlayerCasterDamageToEnemy(enemy, proj);

                                // Handle Special Effects
                                handlePlayerCasterEffectToEnemy(enemy, proj);

                                proj.deactivate();
                                break;
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

        // Draw day-night overlay (after map, before player)
        if (dayNightCycle != null) {
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

        // Draw npcs
        List<Npc> npcList = NpcSystem.getInstance().getMapNpcList(MapSystem.getInstance().getCurMapInfo().getMapId());
        npcList.sort((n1, n2) -> Float.compare(n1.getY(), n2.getY()));
        for (Npc npc : npcList) {
            npc.draw(canvas, -cameraX, -cameraY);
        }

        // Draw treasure chests (in maze)
        if (MapSystem.getInstance().isCurrentMazaMap() && MazeSystem.getInstance().isInitialized()) {
            for (TreasureChest chest : MazeSystem.getInstance().getTreasureChests()) {
                chest.draw(canvas, -cameraX, -cameraY);
            }
        }

        // Draw ground items (below player, above map)
        if (groundItems != null) {
            for (GroundItem groundItem : groundItems) {
                groundItem.draw(canvas, (int) -cameraX, (int) -cameraY);
            }
        }

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
        for (SkillEffect effect : activeSkillEffects) {
            effect.draw(canvas, (int) -cameraX, (int) -cameraY);
        }

        // Draw weather effects
        if (weatherSystem != null) {
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
            List<Npc> npcList = NpcSystem.getInstance().getMapNpcList(MapSystem.getInstance().getCurMapInfo().getMapId());
            for (Npc npc : npcList) {
                if (npc.isTouched(worldX, worldY)) {
                    NpcSystem.getInstance().startConversation(npc);
                    return true;
                }
            }

            // 检查是否点击了宝箱 (迷宫中)
            if (MapSystem.getInstance().isCurrentMazaMap() && MazeSystem.getInstance().isInitialized()) {
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

    /**
     * 传送到指定地图
     * @param mapId
     */
    public void teleportToMap(int mapId) {
        isLoading = true;
        String mapName = MapSystem.getInstance().getMapName(mapId);
        showCenterToast("正在前往" + mapName + "...");

        MapSystem.getInstance().loadMapAsync(mapId, new MapSystem.OnLoadMapCallback() {
            @Override
            public void onLoadMapFinish(int mapId, int[][] mapData) {
                // 设置玩家位置
                if(MapSystem.getInstance().isCurrentMazaMap()){
                    //迷宫地图
                    //设置人物位置
                    MazeGenerator mazeGen = MapSystem.getInstance().getMazeGenerator();
                    if (mazeGen != null) {
                        player.setX(mazeGen.getEntranceX());
                        player.setY(mazeGen.getEntranceY() + 100); // 入口下方一点
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

                // 重新初始化敌人
                initializeEnemies();

                // 刷新小地图
                if (gameUI != null) {
                    gameUI.refreshMinimap();
                }

                isLoading = false;
                showCenterToast("到达了" + mapName);
            }
        });
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

                // 伤害倍率(火云术/寒冰术9级+: 伤害+20%)
                float damageMultiplier = (proj != null) ? proj.getDamageMultiplier() : 1.0f;
                damage = (int) (damage * damageMultiplier);

                // 20%概率暴击(火云术/寒冰术10级)
                boolean isCrit = attackResult.isCrit;
                if (proj != null && proj.getSkillLevel() >= 10) {
                    if (Math.random() < 0.2f) {
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

                    // 法术击退效果: 从玩家位置推开敌人
                    enemy.applyKnockback(player.getX(), player.getY(), 200f, 200);
                    // 法术受击硬直
                    enemy.applyCC(Character.CrowdControlType.STUN, 200);

                    // 火云术9级+: 灼烧效果 (3秒, 每秒伤害=基础伤害的30%)
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
        if (player != null) {
            player.startDash(direction);
        }
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

                    // 击退效果: 从玩家位置推开敌人
                    hit.enemy.applyKnockback(player.getX(), player.getY(), 300f, 200);
                    // 受击硬直: 短暂停顿
                    hit.enemy.applyCC(Character.CrowdControlType.STUN, 150);
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

    public void doCasterAction(SkillInfo skillInfo) {
        if (skillInfo != null) {
            LogUtil.d("Casting skill: " + skillInfo.getName());
            SkillStartInfo info = SkillSystem.getInstance().castSkill(skillInfo);
            if (info != null) {
                if (info.getProjectiles() != null) {
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
     * 狐狸精狐媚法术: 向四周发射6道花瓣，被击中后受到法术伤害，30%概率眩晕1秒
     */
    private void spawnFoxCharmPetals(FoxSpirit fox) {
        float foxX = fox.getX();
        float foxY = fox.getY();
        float petalDistance = 300f; // 花瓣飞行目标距离

        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3; // 每60度一道花瓣
            float targetX = foxX + (float) Math.cos(angle) * petalDistance;
            float targetY = foxY + (float) Math.sin(angle) * petalDistance;

            Projectile petal = new Projectile(foxX, foxY, targetX, targetY, SkillType.ENEMY_FoxCharm);
            petal.setFromEnemy(fox);
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

        // 将小弟添加到敌人列表
        enemies.addAll(minions);

        // 显示召唤通知
        showCenterToast(boss.getName() + " 召唤了一群小弟!");
        LogUtil.d("BossSummon", boss.getName() + " summoned " + minions.size() + " minions");
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

        Enemy minion;
        String minionName;

        if (boss instanceof Tiger) {
            minion = new Tiger(spawnX, spawnY);
            minionName = "猛虎";
        } else if (boss instanceof WildBoar) {
            minion = new WildBoar(spawnX, spawnY);
            minionName = "野猪";
        } else if (boss instanceof Viper) {
            minion = new Viper(spawnX, spawnY);
            minionName = "毒蛇";
        } else if (boss instanceof Bandit) {
            minion = new Bandit(spawnX, spawnY);
            minionName = "强盗";
        } else if (boss instanceof FoxSpirit) {
            minion = new FoxSpirit(spawnX, spawnY);
            minionName = "狐狸精";
        } else {
            // Default to Wolf
            minion = new Wolf(spawnX, spawnY);
            minionName = "野狼";
        }

        // 强制设置等级(覆盖构造函数中的随机等级)
        setEnemyLevel(minion, level, minionName);

        // 设置仇恨状态，让小弟主动攻击玩家(仇恨持续60秒)
        minion.setAggro(60000);

        return minion;
    }

    /**
     * 强制设置怪物等级和属性
     */
    private void setEnemyLevel(Enemy enemy, Enemy.EnemyLevel level, String baseName) {
        enemy.setEnemyLevel(level);

        if (level == Enemy.EnemyLevel.BOSS) {
            enemy.setSize(enemy.getSize() * 3);
            enemy.setName(baseName + "BOSS");
        } else if (level == Enemy.EnemyLevel.ELITE) {
            enemy.setSize(enemy.getSize() * 2);
            enemy.setName(baseName + "精英");
        } else if (level == Enemy.EnemyLevel.LEADER) {
            enemy.setSize((int) (enemy.getSize() * 1.3f));
            enemy.setName(baseName + "首领");
        } else {
            enemy.setName(baseName);
        }
    }
}

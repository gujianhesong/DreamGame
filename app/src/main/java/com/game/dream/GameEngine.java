package com.game.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.bean.SkillStartInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Wolf;
import com.game.dream.enums.SkillType;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.figure.Character;
import com.game.dream.figure.Player;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.ItemStack;
import com.game.dream.map.MapGenerator;
import com.game.dream.map.MapRenderer;
import com.game.dream.skill.SkillEffect;
import com.game.dream.system.DayNightCycle;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.system.WeatherSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DamageNumber;
import com.game.dream.ui.DialogBox;
import com.game.dream.ui.FloatingText;
import com.game.dream.ui.GameUI;
import com.game.dream.ui.Projectile;
import com.game.dream.utils.BattleUtil;
import com.game.dream.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private Context context;
    private static int screenWidth;
    private static int screenHeight;

    // Map dimensions
    public static final int MAP_WIDTH = 10000;
    public static final int MAP_HEIGHT = 10000;
    public static final int TILE_SIZE = 20;

    // Camera position (top-left corner of visible area)
    private static float cameraX;
    private static float cameraY;

    // Player
    private Player player;

    // Map data
    private int[][] map; // 0=plain, 1=grassland, 2=forest, 3=lake, 4=snow, 5=swamp, 6=lava
    // Map generator
    private MapGenerator mapGenerator;
    // Map renderer (extracted to separate class)
    private MapRenderer mapRenderer;


    // Day-night cycle
    private DayNightCycle dayNightCycle;
    private long lastUpdateTime;

    // Weather system
    private WeatherSystem weatherSystem;

    // Enemies
    private java.util.List<Enemy> enemies;

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
        // Initialize map generator and generate map
        mapGenerator = new MapGenerator(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);
        map = mapGenerator.generateMap();

        // Find a valid starting position (not on lake or lava)
        int startX = MAP_WIDTH / TILE_SIZE / 2;
        int startY = MAP_HEIGHT / TILE_SIZE / 2;

        // Search for a valid position near the center
        boolean foundValidPosition = false;
        for (int radius = 0; radius < 50 && !foundValidPosition; radius++) {
            for (int dy = -radius; dy <= radius && !foundValidPosition; dy++) {
                for (int dx = -radius; dx <= radius && !foundValidPosition; dx++) {
                    int checkX = startX + dx;
                    int checkY = startY + dy;

                    if (checkX >= 0 && checkX < map[0].length && checkY >= 0 && checkY < map.length) {
                        int terrain = map[checkY][checkX];
                        if (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA) {
                            startX = checkX;
                            startY = checkY;
                            foundValidPosition = true;
                        }
                    }
                }
            }
        }

        // Create player at center of map
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        if (roleInfo.getMapX() < 0 || roleInfo.getMapY() < 0) {
            roleInfo.setMapX(startX * TILE_SIZE + TILE_SIZE / 2);
            roleInfo.setMapY(startY * TILE_SIZE + TILE_SIZE / 2);
        }
        player = new Player(roleInfo.getMapX(), roleInfo.getMapY());
        player.setName("剑侠客");
        // Set initial respawn point
        player.setRespawnPoint(player.getX(), player.getY());

        // Initialize camera to center on player
        updateCamera();

        // Initialize map renderer
        mapRenderer = new MapRenderer(map, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);

        // Initialize day-night cycle
        dayNightCycle = new DayNightCycle();
        lastUpdateTime = System.currentTimeMillis();

        // Initialize weather system
        //weatherSystem = new WeatherSystem();

        // Initialize enemys
        initializeEnemies();

        // Initialize projectiles
        projectiles = new ArrayList<>();

        // Initialize damage numbers
        damageNumbers = new ArrayList<>();

        // Initialize floating texts list
        floatingTexts = new ArrayList<>();

        // Initialize GameUI
        gameUI = new GameUI();
        gameUI.initUI();
    }

    /**
     * Spawn enemys at random locations
     */
    private void initializeEnemies() {
        enemies = new java.util.ArrayList<>();

        // Spawn 10 wolves at random positions
        Random random = new Random(67890);
        int enemyCount = 100;

        for (int i = 0; i < enemyCount; i++) {
            boolean foundValidSpawn = false;
            float spawnX = 0, spawnY = 0;

            // Try to find a valid spawn position
            for (int attempts = 0; attempts < 50 && !foundValidSpawn; attempts++) {
                int gridX = random.nextInt(map[0].length);
                int gridY = random.nextInt(map.length);

                int terrain = map[gridY][gridX];

                // Spawn on land (not lake/lava) and not too close to player start
                if (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA) {
                    spawnX = gridX * TILE_SIZE + TILE_SIZE / 2;
                    spawnY = gridY * TILE_SIZE + TILE_SIZE / 2;

                    // Check distance from player
                    float dx = spawnX - player.getX();
                    float dy = spawnY - player.getY();
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance > 500) { // At least 500 pixels away from player
                        foundValidSpawn = true;
                    }
                }
            }

            if (foundValidSpawn) {
                double rand = Math.random();
                if (rand < 0.4) {
                    Enemy enemy = new Tiger(spawnX, spawnY);
                    enemy.setName("猛虎");
                    enemies.add(enemy);
                } else {
                    Enemy enemy = new Wolf(spawnX, spawnY);
                    enemy.setName("野狼");
                    enemies.add(enemy);
                }
            }
        }
    }

    public void cleanup() {
        instance = null;
        // Clean up map renderer
        if (mapRenderer != null) {
            mapRenderer.cleanup();
        }

        if (gameUI != null) {
            gameUI.cleanup();
        }
    }

    public void update(boolean isFirst) {
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
        player.update(map, MAP_WIDTH / TILE_SIZE, MAP_HEIGHT / TILE_SIZE, TILE_SIZE, deltaTime);

        // Update camera to follow player
        updateCamera();

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

        // Update projectiles
        checkProjectileUpdate(deltaTime);

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
                    if (enemy.canAttack()) {
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

                        enemy.setLastAttackTime(currentTime);

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
                    enemy.update(deltaTime, player.getX(), player.getY(), map, MAP_WIDTH, MAP_HEIGHT);

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

                    // Get item drops from enemy
                    List<ItemStack> drops = enemy.getDrops();
                    StringBuilder dropMessage = new StringBuilder();
                    for (ItemStack drop : drops) {
                        if (ItemSystem.getInstance().addItem(drop.getItem(), drop.getQuantity())) {
                            if (dropMessage.length() > 0) dropMessage.append(", ");
                            if (drop.getItem() instanceof EquipmentItem) {
                                dropMessage.append(drop.getItem().getName());
                            } else {
                                dropMessage.append(drop.getItem().getName())
                                        .append(" x").append(drop.getQuantity());
                            }

                            // Show floating text for item drop
                            floatingTexts.add(new FloatingText(
                                    enemy.getX() + (float) (Math.random() * 40 - 20),
                                    enemy.getY() - 70 - (float) (Math.random() * 20),
                                    drop.getItem().getName(),
                                    FloatingText.Type.EXPERIENCE
                            ));
                        }
                    }

                    // If player leveled up, show special notification
                    if (newLevel > oldLevel) {
                        floatingTexts.add(new FloatingText(
                                player.getX(),
                                player.getY() - 220,
                                "升级! Lv." + newLevel,
                                FloatingText.Type.LEVEL_UP
                        ));
                    }

                    enemies.remove(i);
                }
            }
        }
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

                            proj.deactivate();
                            continue;
                        }
                    } else {
                        for (Enemy enemy : enemies) {
                            if (proj.checkCollision(enemy)) {
                                // Handle caster damage
                                handlePlayerCasterDamageToEnemy(enemy, proj.getSkillType());

                                // Handle Special Effects
                                handlePlayerCasterEffectToEnemy(enemy, proj.getEffectType());

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
        cameraX = Math.max(0, Math.min(cameraX, MAP_WIDTH - screenWidth));
        cameraY = Math.max(0, Math.min(cameraY, MAP_HEIGHT - screenHeight));
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
        // Draw background
        canvas.drawColor(Color.BLACK);

        // Draw map using MapRenderer
        mapRenderer.draw(canvas, cameraX, cameraY, screenWidth, screenHeight);

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
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        boolean handled = false;

        // Get the coordinates of the pointer that triggered this event
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        if (gameUI != null) {
            handled = gameUI.handleTouch(event);
            if (handled) {
                return true;
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

    public DialogBox showDialog(String title, String msg, List<String> options, DialogBox.DialogListener listener) {
        if (gameUI != null) {
            return gameUI.showDialog(title, msg, options, listener);
        }
        return null;
    }

    /**
     * Show float text
     */
    public void showFloatText(String text, FloatingText.Type type) {
        floatingTexts.add(new FloatingText(
                player.getX(),
                player.getY() - 220,
                text,
                type
        ));
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Player getPlayer() {
        return player;
    }

    public int[][] getMap() {
        return map;
    }

    public DayNightCycle getDayNightCycle() {
        return dayNightCycle;
    }

    public WeatherSystem getWeatherSystem() {
        return weatherSystem;
    }

    public void handlePlayerCasterDamageToEnemy(Enemy enemy, SkillType skillType) {
        AttackResult attackResult = BattleUtil.caculatePlayerCasterDamage(enemy, skillType);
        if (attackResult != null) {
            if (attackResult.isHit) {
                int damage = attackResult.damageValue;
                if (damage > 0) {
                    enemy.takeDamage(damage);

                    // Create floating damage number above enemy
                    damageNumbers.add(new DamageNumber(
                            enemy.getX(),
                            enemy.getY() - 30,
                            damage,
                            attackResult.isCrit
                    ));
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

    public void handlePlayerCasterEffectToEnemy(Enemy enemy, Projectile.EffectType effectType) {
        if (effectType == Projectile.EffectType.ROOT) {
            enemy.applyCC(Character.CrowdControlType.ROOT, 2000);
        }
    }

    public void updatePlayerDirection(boolean upPressed, boolean downPressed,
                                      boolean leftPressed, boolean rightPressed) {
        player.setMovingUp(upPressed);
        player.setMovingDown(downPressed);
        player.setMovingLeft(leftPressed);
        player.setMovingRight(rightPressed);
    }

    public void doAttackAction() {
        // Trigger attack animation
        player.triggerAttackAnimation();

        // Trigger melee attack
        List<EnemyHitInfo> hits = player.performMeleeAttack(enemies);
        if (hits != null) {
            for (EnemyHitInfo hit : hits) {
                damageNumbers.add(new DamageNumber(
                        hit.enemy.getX(),
                        hit.enemy.getY() - 30,
                        hit.damage,
                        hit.isCrit
                ));
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
            }
        }
    }
}

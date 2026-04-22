package com.game.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Wolf;
import com.game.dream.enums.SkillType;
import com.game.dream.panel.RoleInfoPanel;
import com.game.dream.system.DayNightCycle;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.WeatherSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private Context context;
    private static int screenWidth;
    private static int screenHeight;

    // Map dimensions
    private static final int MAP_WIDTH = 10000;
    private static final int MAP_HEIGHT = 10000;
    private static final int TILE_SIZE = 20;

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

    // Minimap
    private Minimap minimap;

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

    private RoleInfoPanel roleInfoPanel;

    // Attack buttons
    private Rect meleeAttackButton;
    private Rect magicAttackButton1; // Top-left spell
    private Rect magicAttackButton2; // Top spell
    private Rect magicAttackButton3; // Top-right spell
    private Rect roleInfoButton;
    private boolean meleeAttackPressed;
    private boolean magicAttack1Pressed, magicAttack2Pressed, magicAttack3Pressed;

    // Track which pointer IDs are controlling the D-pad
    private Integer dpadPointerId = null;

    // Control buttons
    private Rect dpadBounds;
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // FPS tracking
    private long lastFrameTime;
    private int frameCount;
    private float currentFPS;
    private long fpsUpdateTime;

    // Memory tracking (updated every 3 seconds)
    private float cachedMemoryMB = 0;
    private long lastMemoryUpdateTime = 0;
    private static final long MEMORY_UPDATE_INTERVAL = 3000; // 3 seconds

    public GameEngine(Context context) {
        this.context = context;
        this.lastFrameTime = System.currentTimeMillis();
        this.frameCount = 0;
        this.currentFPS = 0;
        this.fpsUpdateTime = System.currentTimeMillis();
        initGame();
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

        // Initialize minimap
        minimap = new Minimap(map, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);
        minimap.initialize();

        // Initialize day-night cycle
        dayNightCycle = new DayNightCycle();
        lastUpdateTime = System.currentTimeMillis();

        // Initialize weather system
        //weatherSystem = new WeatherSystem();

        // Initialize enemys
        initializeEnemies();

        // Initialize projectiles
        projectiles = new java.util.ArrayList<>();

        // Initialize damage numbers
        damageNumbers = new java.util.ArrayList<>();

        // Initialize floating texts list
        floatingTexts = new java.util.ArrayList<>();

        // Initialize role info panel
        roleInfoPanel = new RoleInfoPanel(player);
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
        // Clean up map renderer
        if (mapRenderer != null) {
            mapRenderer.cleanup();
        }

        // Clean up minimap
        if (minimap != null) {
            minimap.cleanup();
        }
    }

    public void update() {
        // Calculate delta time first
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Set player movement flags based on button states
        player.setMovingUp(upPressed);
        player.setMovingDown(downPressed);
        player.setMovingLeft(leftPressed);
        player.setMovingRight(rightPressed);

        // Debug: Log movement state
        if (upPressed || downPressed || leftPressed || rightPressed) {
            android.util.Log.d("GameEngine", "Player moving: U=" + upPressed + " D=" + downPressed +
                    " L=" + leftPressed + " R=" + rightPressed);
        }

        // Update player movement (pass deltaTime)
        player.update(map, MAP_WIDTH / TILE_SIZE, MAP_HEIGHT / TILE_SIZE, TILE_SIZE, deltaTime);

        // Update camera to follow player
        updateCamera();

        // Update FPS counter
        updateFPS();

        // Update day-night cycle
        dayNightCycle.update(deltaTime);

        // Update weather system
        if (weatherSystem != null) {
            weatherSystem.update(deltaTime, screenWidth, screenHeight);
        }

        // Update enemies
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

        // Update projectiles
        if (projectiles != null) {
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile proj = projectiles.get(i);
                proj.update(deltaTime);

                // Check collisions with enemies
                if (proj.isActive()) {
                    for (Enemy enemy : enemies) {
                        if (proj.checkCollision(enemy)) {
                            AttackResult attackResult = BattleUtil.caculatePlayerCasterDamage(enemy, proj.getSkillType());
                            if (attackResult != null) {
                                if (attackResult.isHit) {
                                    int damage = attackResult.damageValue;
                                    enemy.takeDamage(damage);

                                    // Create floating damage number above enemy
                                    damageNumbers.add(new DamageNumber(
                                            enemy.getX(),
                                            enemy.getY() - 30,
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
                            }

                            proj.deactivate();
                            break;
                        }
                    }
                }

                // Remove inactive projectiles
                if (!proj.isActive()) {
                    projectiles.remove(i);
                }
            }
        }

        // Check enemy attacks on player
        checkEnemyAttacksOnPlayer();

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
                if (distance < 60) { // Attack range
                    if (enemy.canAttack()) {
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

                        enemy.setLastAttackTime(currentTime);

                        if (died) {
                            // Player died - respawn
                            player.respawn();

                            // Clear nearby enemies to prevent spawn kill
                            for (Enemy e : enemies) {
                                float ex = e.getX() - player.getX();
                                float ey = e.getY() - player.getY();
                                float edist = (float) Math.sqrt(ex * ex + ey * ey);
                                if (edist < 300) {
                                    e.takeDamage(1000); // Kill nearby enemies
                                }
                            }
                        }
                    }
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

    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - fpsUpdateTime;

        // Update FPS every second
        if (elapsed >= 1000) {
            currentFPS = (frameCount * 1000f) / elapsed;
            frameCount = 0;
            fpsUpdateTime = currentTime;
        }
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

        // Draw weather effects
        if (weatherSystem != null) {
            weatherSystem.draw(canvas);
        }

        // Draw UI
        drawUI(canvas);

        // Draw minimap
        if (minimap != null) {
            minimap.draw(canvas, player.getX(), player.getY(), screenWidth, screenHeight);
        }

        // Draw controls
        drawControls(canvas);

        // Draw role info panel (on top of everything)
        if (roleInfoPanel != null) {
            roleInfoPanel.draw(canvas);
        }
    }

    private void drawUI(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);

        /*// Draw player health bar (top-left, below other info)
        float healthBarWidth = 200;
        float healthBarHeight = 20;
        float healthBarX = 10;
        float healthBarY = 110;

        // Background
        paint.setColor(Color.BLACK);
        canvas.drawRect(healthBarX - 2, healthBarY - 2,
                healthBarX + healthBarWidth + 2,
                healthBarY + healthBarHeight + 2, paint);

        // Health fill
        float healthPercent = player.getHealthPercent();
        int healthColor;
        if (healthPercent > 0.6f) {
            healthColor = Color.GREEN;
        } else if (healthPercent > 0.3f) {
            healthColor = Color.YELLOW;
        } else {
            healthColor = Color.RED;
        }

        paint.setColor(healthColor);
        canvas.drawRect(healthBarX, healthBarY,
                healthBarX + healthBarWidth * healthPercent,
                healthBarY + healthBarHeight, paint);

        // Health text
        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(player.getHealth() + " / " + player.getMaxHealth(),
                healthBarX + healthBarWidth / 2,
                healthBarY + 15, paint);

        // Invincibility indicator
        if (player.isCurrentlyInvincible()) {
            paint.setColor(Color.YELLOW);
            paint.setTextSize(16);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("✨ INVINCIBLE", healthBarX, healthBarY + 40, paint);
        }*/

        // Draw player level and experience (top-left)
        int expBarWidth = 200;
        int expBarHeight = 20;
        int expBarX = 10;
        int expBarY = 35;

        // Level text
        paint.setColor(Color.rgb(255, 215, 0)); // Gold color
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Lv." + RoleSystem.getInstance().getRoleInfo().getLevel(), expBarX, expBarY + 7, paint);

        // Experience bar background
        paint.setColor(Color.BLACK);
        canvas.drawRect(expBarX + 70, expBarY - 12,
                expBarX + 70 + expBarWidth + 2,
                expBarY - 12 + expBarHeight + 2, paint);

        // Experience bar fill
        float expProgress = RoleSystem.getInstance().getRoleInfo().getExp() * 1f / RoleSystem.getInstance().getExpForNextLevel();
        paint.setColor(Color.rgb(100, 181, 246)); // Blue
        canvas.drawRect(expBarX + 70, expBarY - 12,
                expBarX + 70 + expBarWidth * expProgress,
                expBarY - 12 + expBarHeight, paint);

        // EXP text
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(RoleSystem.getInstance().getRoleInfo().getExp() + "/" + RoleSystem.getInstance().getExpForNextLevel(),
                expBarX + 70 + expBarWidth / 2,
                expBarY + 5, paint);

        paint.setTextSize(30);
        // Draw FPS (top-right corner)
        paint.setTextAlign(Paint.Align.LEFT);
        if (currentFPS >= 55) {
            paint.setColor(Color.GREEN);
        } else if (currentFPS >= 30) {
            paint.setColor(Color.YELLOW);
        } else {
            paint.setColor(Color.RED);
        }
        canvas.drawText("FPS: " + String.format("%.1f", currentFPS), 10, 80, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Memory: " + String.format("%.1f", getUsedMemoryMB()) + " MB", 10, 120, paint);

        // Draw coordinates (top-left)
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        canvas.drawText("Position: (" + (int) player.getX() + ", " + (int) player.getY() + ")", 10, 160, paint);

//        // Draw terrain info
//        int playerGridX = (int) (player.getX() / TILE_SIZE);
//        int playerGridY = (int) (player.getY() / TILE_SIZE);
//        String terrainName = MapGenerator.getTerrainName(map[playerGridY][playerGridX]);
//        canvas.drawText("Terrain: " + terrainName, 10, 160, paint);
//
//        // Draw chunk cache info (for debugging)
//        paint.setColor(Color.CYAN);
//        canvas.drawText("Chunks: " + mapRenderer.getCachedChunkCount(), 10, 200, paint);
//        canvas.drawText("Active: " + mapRenderer.getActiveChunkCount(), 10, 240, paint);

        // Draw time info
        if (dayNightCycle != null) {
            paint.setColor(Color.rgb(255, 255, 200)); // Light yellow
            canvas.drawText(dayNightCycle.getTimePhase(), 10, 200, paint);
        }

        // Draw weather info
        if (weatherSystem != null) {
            paint.setColor(Color.rgb(200, 220, 255)); // Light blue
            canvas.drawText("Weather: " + weatherSystem.getWeatherDescription(), 10, 240, paint);
        }
    }

    private void drawControls(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw D-pad background
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawCircle(dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width() / 2, paint);

        // Draw D-pad buttons
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("▲", dpadBounds.centerX(), dpadBounds.centerY() - dpadBounds.height() / 4 + 15, paint);
        canvas.drawText("▼", dpadBounds.centerX(), dpadBounds.centerY() + dpadBounds.height() / 4 + 15, paint);
        canvas.drawText("◀", dpadBounds.centerX() - dpadBounds.width() / 4, dpadBounds.centerY() + 15, paint);
        canvas.drawText("▶", dpadBounds.centerX() + dpadBounds.width() / 4, dpadBounds.centerY() + 15, paint);

        // Draw attack buttons cluster
        if (meleeAttackButton != null && magicAttackButton1 != null &&
                magicAttackButton2 != null && magicAttackButton3 != null) {

            // Draw connection lines from magic buttons to physical button
            paint.setColor(Color.argb(60, 255, 255, 255));
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawLine(magicAttackButton1.centerX(), magicAttackButton1.centerY(),
                    meleeAttackButton.centerX(), meleeAttackButton.centerY(), paint);
            canvas.drawLine(magicAttackButton2.centerX(), magicAttackButton2.centerY(),
                    meleeAttackButton.centerX(), meleeAttackButton.centerY(), paint);
            canvas.drawLine(magicAttackButton3.centerX(), magicAttackButton3.centerY(),
                    meleeAttackButton.centerX(), meleeAttackButton.centerY(), paint);

            // Draw magic attack buttons (circular spells)
            drawCircularMagicAttackButton(canvas, magicAttackButton1, magicAttack1Pressed, "❄️", Color.rgb(100, 200, 255));
            drawCircularMagicAttackButton(canvas, magicAttackButton2, magicAttack2Pressed, "🔥", Color.rgb(255, 150, 100));
            drawCircularMagicAttackButton(canvas, magicAttackButton3, magicAttack3Pressed, "⚡", Color.rgb(255, 255, 100));

            // Draw physical attack button (circular, larger and more prominent)
            drawCircularPhysicalAttackButton(canvas, meleeAttackButton, meleeAttackPressed);
        }

        // Draw role info button
        if (roleInfoButton != null) {
            drawInfoButton(canvas, roleInfoButton, roleInfoPanel.isVisible());
        }
    }

    private void drawDpadButton(Canvas canvas, Rect button, boolean pressed, String label) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Calculate center and radius
        float centerX = button.centerX();
        float centerY = button.centerY();
        float radius = button.width() / 2;

        // Button background (circular)
        if (pressed) {
            paint.setColor(Color.argb(200, 100, 100, 100));
        } else {
            paint.setColor(Color.argb(150, 80, 80, 80));
        }
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Border (circular)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Label
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(25);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = centerY + 8;
        canvas.drawText(label, centerX, textY, paint);
    }

    /**
     * Draw circular magic attack button
     */
    private void drawCircularMagicAttackButton(Canvas canvas, Rect button, boolean pressed, String label, int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float centerX = button.centerX();
        float centerY = button.centerY();
        float radius = button.width() / 2;

        // Button background (circular)
        if (pressed) {
            paint.setColor(Color.argb(120, Color.red(color), Color.green(color), Color.blue(color)));
        } else {
            paint.setColor(Color.argb(80, Color.red(color), Color.green(color), Color.blue(color)));
        }
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Border (circular)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Label
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(35);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = centerY + 12;
        canvas.drawText(label, centerX, textY, paint);

        // Draw cooldown overlay
        float cooldownProgress = player.getMagicCooldownProgress();
        if (cooldownProgress < 1.0f) {
            drawCircularCooldown(canvas, button, cooldownProgress);
        }
    }

    /**
     * Draw circular physical attack button (larger and more prominent)
     */
    private void drawCircularPhysicalAttackButton(Canvas canvas, Rect button, boolean pressed) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float centerX = button.centerX();
        float centerY = button.centerY();
        float radius = button.width() / 2;

        // Button background (circular, red gradient effect)
        if (pressed) {
            paint.setColor(Color.argb(120, 255, 80, 80));
        } else {
            paint.setColor(Color.argb(80, 220, 60, 60));
        }
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Outer glow effect
        paint.setColor(Color.argb(80, 255, 100, 100));
        canvas.drawCircle(centerX, centerY, radius + 5, paint);

        // Border (circular, thicker)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Inner circle for depth
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 255, 255, 255));
        canvas.drawCircle(centerX, centerY, radius * 0.7f, paint);

        // Label (larger icon)
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = centerY + 17;
        canvas.drawText("⚔️", centerX, textY, paint);

        // Draw cooldown overlay
        float cooldownProgress = player.getAttackCooldownProgress();
        if (cooldownProgress < 1.0f) {
            drawCircularCooldown(canvas, button, cooldownProgress);
        }
    }

    /**
     * Draw circular cooldown overlay
     */
    private void drawCircularCooldown(Canvas canvas, Rect button, float progress) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float centerX = button.centerX();
        float centerY = button.centerY();
        float radius = button.width() / 2;

        // Dark overlay based on cooldown progress
        int alpha = (int)(180 * (1 - progress)); // More opaque when cooling down
        paint.setColor(Color.argb(alpha, 0, 0, 0));

        // Draw arc from top, clockwise
        RectF oval = new RectF(button.left, button.top, button.right, button.bottom);
        float sweepAngle = 360 * (1 - progress); // Remaining cooldown

        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(oval, -90, sweepAngle, true, paint);

        // Optional: Draw border for the cooldown arc
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(100, 255, 255, 255));
        canvas.drawArc(oval, -90, sweepAngle, false, paint);
    }

    /**
     * Draw role info button
     */
    private void drawInfoButton(Canvas canvas, Rect button, boolean isActive) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Button background
        if (isActive) {
            paint.setColor(Color.argb(200, 100, 150, 255));
        } else {
            paint.setColor(Color.argb(150, 80, 80, 80));
        }
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 10, 10, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 10, 10, paint);

        // Icon (ℹ️ or 👤)
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textX = button.centerX();
        float textY = button.centerY() + 10;
        canvas.drawText("👤", textX, textY, paint);
    }

    private void drawCooldownIndicator(Canvas canvas, Rect button, float progress, int color) {
        if (progress >= 1.0f) return; // No cooldown

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(150, 0, 0, 0));

        // Draw cooldown overlay
        float height = button.height() * (1 - progress);
        canvas.drawRect(button.left, button.top, button.right, button.top + height, paint);
    }

    public boolean handleTouch(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        boolean handled = false;
        // Get the coordinates of the pointer that triggered this event
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        // If role info panel is visible, check if touching it first
        if (roleInfoPanel != null && roleInfoPanel.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN && roleInfoPanel.handleTouch(x, y)) {
                return true; // Panel handled the touch (closed itself)
            }
        }
        // Check role info button
        if (roleInfoButton != null && roleInfoButton.contains((int)x, (int)y)) {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                roleInfoPanel.toggleVisibility();
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                return true;
            }
        }
        // Handle D-pad with pointer tracking
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Check if this pointer started on D-pad
                if (isInCircle(x, y, dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width() / 2)) {
                    dpadPointerId = pointerId; // Lock this pointer to D-pad
                    handled = true;

                    // Calculate initial direction
                    float dx = x - dpadBounds.centerX();
                    float dy = y - dpadBounds.centerY();

                    upPressed = false;
                    downPressed = false;
                    leftPressed = false;
                    rightPressed = false;

                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) rightPressed = true;
                        else leftPressed = true;
                    } else {
                        if (dy > 0) downPressed = true;
                        else upPressed = true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // If this is the D-pad pointer, update direction regardless of position
                if (dpadPointerId != null && pointerId == dpadPointerId) {
                    handled = true;

                    float dx = x - dpadBounds.centerX();
                    float dy = y - dpadBounds.centerY();

                    upPressed = false;
                    downPressed = false;
                    leftPressed = false;
                    rightPressed = false;

                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) rightPressed = true;
                        else leftPressed = true;
                    } else {
                        if (dy > 0) downPressed = true;
                        else upPressed = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // If this is the D-pad pointer, release it
                if (dpadPointerId != null && pointerId == dpadPointerId) {
                    dpadPointerId = null; // Release the lock
                    upPressed = false;
                    downPressed = false;
                    leftPressed = false;
                    rightPressed = false;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                // Cancel all input
                dpadPointerId = null;
                upPressed = false;
                downPressed = false;
                leftPressed = false;
                rightPressed = false;
                break;
        }

        // Update player movement
        player.setMovingUp(upPressed);
        player.setMovingDown(downPressed);
        player.setMovingLeft(leftPressed);
        player.setMovingRight(rightPressed);

        // Handle attack buttons based on event type
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Check if this pointer is on any attack button
                if (meleeAttackButton != null &&
                        isPointInCircle(x, y, meleeAttackButton.centerX(), meleeAttackButton.centerY(), meleeAttackButton.width() / 2)) {
                    meleeAttackPressed = true;
                    handled = true;

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

                if (magicAttackButton1 != null &&
                        isPointInCircle(x, y, magicAttackButton1.centerX(), magicAttackButton1.centerY(), magicAttackButton1.width() / 2)) {
                    magicAttack1Pressed = true;
                    handled = true;

                    // Cast ice bolt
                    List<Projectile> list = player.castTripleSpell(SkillType.ICE_BOLT);
                    if (list != null) {
                        projectiles.addAll(list);
                    }
                }

                if (magicAttackButton2 != null &&
                        isPointInCircle(x, y, magicAttackButton2.centerX(), magicAttackButton2.centerY(), magicAttackButton2.width() / 2)) {
                    magicAttack2Pressed = true;
                    handled = true;

                    // Cast fireball
                    List<Projectile> list = player.castTripleSpell(SkillType.FIREBALL);
                    if (list != null) {
                        projectiles.addAll(list);
                    }
                }

                if (magicAttackButton3 != null &&
                        isPointInCircle(x, y, magicAttackButton3.centerX(), magicAttackButton3.centerY(), magicAttackButton3.width() / 2)) {
                    magicAttack3Pressed = true;
                    handled = true;

                    // Cast lightning
                    List<Projectile> list = player.castTripleSpell(SkillType.LIGHTNING);
                    if (list != null) {
                        projectiles.addAll(list);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // Clear the state for the pointer that lifted
                // Check which button this pointer was on
                if (meleeAttackButton != null &&
                        isPointInCircle(x, y, meleeAttackButton.centerX(), meleeAttackButton.centerY(), meleeAttackButton.width() / 2)) {
                    meleeAttackPressed = false;
                    handled = true;
                }

                if (magicAttackButton1 != null &&
                        isPointInCircle(x, y, magicAttackButton1.centerX(), magicAttackButton1.centerY(), magicAttackButton1.width() / 2)) {
                    magicAttack1Pressed = false;
                    handled = true;
                }

                if (magicAttackButton2 != null &&
                        isPointInCircle(x, y, magicAttackButton2.centerX(), magicAttackButton2.centerY(), magicAttackButton2.width() / 2)) {
                    magicAttack2Pressed = false;
                    handled = true;
                }

                if (magicAttackButton3 != null &&
                        isPointInCircle(x, y, magicAttackButton3.centerX(), magicAttackButton3.centerY(), magicAttackButton3.width() / 2)) {
                    magicAttack3Pressed = false;
                    handled = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // For MOVE events, we still need to track if pointers are on buttons
                // to maintain correct visual state when finger moves in/out of button area
                boolean anyOnButton = false;

                for (int i = 0; i < event.getPointerCount(); i++) {
                    float px = event.getX(i);
                    float py = event.getY(i);

                    if (meleeAttackButton != null &&
                            isPointInCircle(px, py, meleeAttackButton.centerX(), meleeAttackButton.centerY(), meleeAttackButton.width() / 2)) {
                        anyOnButton = true;
                        if (!meleeAttackPressed) {
                            meleeAttackPressed = true;
                            handled = true;
                        }
                    }

                    if (magicAttackButton1 != null &&
                            isPointInCircle(px, py, magicAttackButton1.centerX(), magicAttackButton1.centerY(), magicAttackButton1.width() / 2)) {
                        anyOnButton = true;
                        if (!magicAttack1Pressed) {
                            magicAttack1Pressed = true;
                            handled = true;
                        }
                    }

                    if (magicAttackButton2 != null &&
                            isPointInCircle(px, py, magicAttackButton2.centerX(), magicAttackButton2.centerY(), magicAttackButton2.width() / 2)) {
                        anyOnButton = true;
                        if (!magicAttack2Pressed) {
                            magicAttack2Pressed = true;
                            handled = true;
                        }
                    }

                    if (magicAttackButton3 != null &&
                            isPointInCircle(px, py, magicAttackButton3.centerX(), magicAttackButton3.centerY(), magicAttackButton3.width() / 2)) {
                        anyOnButton = true;
                        if (!magicAttack3Pressed) {
                            magicAttack3Pressed = true;
                            handled = true;
                        }
                    }
                }

                // If no pointers are on any button, clear all states
                if (!anyOnButton) {
                    if (meleeAttackPressed || magicAttack1Pressed || magicAttack2Pressed || magicAttack3Pressed) {
                        meleeAttackPressed = false;
                        magicAttack1Pressed = false;
                        magicAttack2Pressed = false;
                        magicAttack3Pressed = false;
                        handled = true;
                    }
                }
                break;
        }

        return handled;
    }

    /**
     * Check if a point is within a circle
     */
    private boolean isPointInCircle(float px, float py, float centerX, float centerY, float radius) {
        float dx = px - centerX;
        float dy = py - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    private boolean isInCircle(float x, float y, float centerX, float centerY, float radius) {
        float dx = x - centerX;
        float dy = y - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;

        // Initialize control buttons
        initControlButtons();

        // Initialize role info panel (center of screen)
        int panelWidth = Math.min(600, width - 40);
        int panelHeight = Math.min(700, height - 100);
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;
        roleInfoPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
    }

    private void initControlButtons() {
        // D-pad buttons (bottom-left) - Smaller size
        int buttonSize = screenHeight / 5;
        int padding = 20;

        // Calculate the center of the D-pad cross
        int dpadCenterX = (int) (padding + buttonSize * 1.5);
        int dpadCenterY = (int) (screenHeight - padding - buttonSize * 1.5);

        // D-pad bounds (entire control area)
        dpadBounds = new Rect(
                dpadCenterX - buttonSize,
                dpadCenterY - buttonSize,
                dpadCenterX + buttonSize,
                dpadCenterY + buttonSize
        );

        // Attack buttons cluster (bottom-right)
        int magicButtonSize = (int)(buttonSize * 0.8); // Magic buttons size
        int physicalButtonSize = (int)(buttonSize * 1.2); // Physical button is 40% larger
        int attackPadding = 30;

        // Physical attack button center (bottom-right position)
        int physicalCenterX = screenWidth - attackPadding - physicalButtonSize;
        int physicalCenterY = screenHeight - attackPadding - physicalButtonSize;

        // Physical attack button (melee) - circular, larger
        meleeAttackButton = new Rect(
                physicalCenterX - physicalButtonSize / 2,
                physicalCenterY - physicalButtonSize / 2,
                physicalCenterX + physicalButtonSize / 2,
                physicalCenterY + physicalButtonSize / 2
        );

        // Magic attack buttons arranged in fan shape around physical button
        // Layout: Left, Top-Left, Top (forming an arc in the upper-left quadrant)
        int spacing = (int)(physicalButtonSize * 1.5);

        // Button 1: Left of physical button
        int magic1X = physicalCenterX - spacing;
        int magic1Y = physicalCenterY;
        magicAttackButton1 = new Rect(
                magic1X - magicButtonSize / 2,
                magic1Y - magicButtonSize / 2,
                magic1X + magicButtonSize / 2,
                magic1Y + magicButtonSize / 2
        );

        // Button 2: Top-Left of physical button (diagonal)
        int magic2X = physicalCenterX - (int)(spacing * 0.7);
        int magic2Y = physicalCenterY - (int)(spacing * 0.7);
        magicAttackButton2 = new Rect(
                magic2X - magicButtonSize / 2,
                magic2Y - magicButtonSize / 2,
                magic2X + magicButtonSize / 2,
                magic2Y + magicButtonSize / 2
        );

        // Button 3: Top of physical button
        int magic3X = physicalCenterX;
        int magic3Y = physicalCenterY - spacing;
        magicAttackButton3 = new Rect(
                magic3X - magicButtonSize / 2,
                magic3Y - magicButtonSize / 2,
                magic3X + magicButtonSize / 2,
                magic3Y + magicButtonSize / 2
        );

        // role info button (top-right corner)
        int infoButtonSize = screenHeight / 10;
        int infoPadding = 20;
        roleInfoButton = new Rect(
                screenWidth / 2 + infoPadding,
                screenHeight - infoPadding - infoButtonSize,
                screenWidth / 2 + infoPadding + infoButtonSize,
                screenHeight - infoPadding
        );
    }

    private float getUsedMemoryMB() {
        long currentTime = System.currentTimeMillis();

        // Only update every 3 seconds
        if (currentTime - lastMemoryUpdateTime >= MEMORY_UPDATE_INTERVAL) {
            android.os.Debug.MemoryInfo memoryInfo = new android.os.Debug.MemoryInfo();
            android.os.Debug.getMemoryInfo(memoryInfo);

            // getTotalPss() returns memory in KB
            long totalPssKB = memoryInfo.getTotalPss();

            // Convert to MB and cache
            cachedMemoryMB = totalPssKB / 1024f;
            lastMemoryUpdateTime = currentTime;
        }

        return cachedMemoryMB;
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
}

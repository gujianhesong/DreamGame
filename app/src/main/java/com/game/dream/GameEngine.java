package com.game.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Wolf;
import com.game.dream.panel.RoleInfoPanel;
import com.game.dream.system.DayNightCycle;
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

    private RoleInfoPanel roleInfoPanel;

    // Attack buttons
    private Rect meleeAttackButton;
    private Rect magicAttackButton;
    private Rect roleInfoButton;
    private boolean meleeAttackPressed;
    private boolean magicAttackPressed;

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
        player = new Player(startX * TILE_SIZE + TILE_SIZE / 2, startY * TILE_SIZE + TILE_SIZE / 2);

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
                if (rand < 0.7) {
                    enemies.add(new Tiger(spawnX, spawnY));
                } else {
                    enemies.add(new Wolf(spawnX, spawnY));
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
                            int damage = proj.getDamage();
                            enemy.takeDamage(damage);

                            // Create floating damage number above enemy
                            damageNumbers.add(new DamageNumber(
                                    enemy.getX(),
                                    enemy.getY() - 30,
                                    damage
                            ));

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
                                    damage
                            ));

                            //是否暴击
                        } else {
                            //未命中
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
            for (Enemy enemy : enemies) {
                // Check if enemy is within visible area (with some padding)
                float padding = 100; // Draw enemies slightly outside screen for smooth entry
                if (isEnemyVisible(enemy, padding)) {
                    enemy.draw(canvas, (int) -cameraX, (int) -cameraY);
                }
            }
        }

        // Draw projectiles
        if (projectiles != null) {
            for (Projectile proj : projectiles) {
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
        canvas.drawText("FPS: " + String.format("%.1f", currentFPS), 10, 40, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Memory: " + String.format("%.1f", getUsedMemoryMB()) + " MB", 10, 80, paint);

        // Draw coordinates (top-left)
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        canvas.drawText("Position: (" + (int) player.getX() + ", " + (int) player.getY() + ")", 10, 120, paint);

        // Draw terrain info
        int playerGridX = (int) (player.getX() / TILE_SIZE);
        int playerGridY = (int) (player.getY() / TILE_SIZE);
        String terrainName = MapGenerator.getTerrainName(map[playerGridY][playerGridX]);
        canvas.drawText("Terrain: " + terrainName, 10, 160, paint);

        // Draw chunk cache info (for debugging)
        paint.setColor(Color.CYAN);
        canvas.drawText("Chunks: " + mapRenderer.getCachedChunkCount(), 10, 200, paint);
        canvas.drawText("Active: " + mapRenderer.getActiveChunkCount(), 10, 240, paint);

        // Draw time info
        if (dayNightCycle != null) {
            paint.setColor(Color.rgb(255, 255, 200)); // Light yellow
            canvas.drawText(dayNightCycle.getTimePhase(), 10, 280, paint);
        }

        // Draw weather info
        if (weatherSystem != null) {
            paint.setColor(Color.rgb(200, 220, 255)); // Light blue
            canvas.drawText("Weather: " + weatherSystem.getWeatherDescription(), 10, 320, paint);
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

        // Draw attack buttons
        if (meleeAttackButton != null && magicAttackButton != null) {
            drawAttackButton(canvas, meleeAttackButton, meleeAttackPressed, "⚔️", Color.rgb(255, 100, 100));
            drawAttackButton(canvas, magicAttackButton, magicAttackPressed, "✨", Color.rgb(100, 150, 255));

            // Draw cooldown indicators
            drawCooldownIndicator(canvas, meleeAttackButton, player.getAttackCooldownProgress(), Color.RED);
            drawCooldownIndicator(canvas, magicAttackButton, player.getMagicCooldownProgress(), Color.BLUE);
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

    private void drawAttackButton(Canvas canvas, Rect button, boolean pressed, String label, int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Button background
        if (pressed) {
            paint.setColor(Color.argb(200, Color.red(color), Color.green(color), Color.blue(color)));
        } else {
            paint.setColor(Color.argb(150, Color.red(color), Color.green(color), Color.blue(color)));
        }
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 20, 20, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 20, 20, paint);

        // Label
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textX = button.centerX();
        float textY = button.centerY() + 15;
        canvas.drawText(label, textX, textY, paint);
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
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        boolean handled = false;

        // If role info panel is visible, check if touching it first
        if (roleInfoPanel != null && roleInfoPanel.isVisible()) {
            if (roleInfoPanel.handleTouch(x, y)) {
                return true; // Panel handled the touch (closed itself)
            }
        }

        // Check role info button first (independent of other controls)
        if (roleInfoButton != null && roleInfoButton.contains((int) x, (int) y)) {
            if (action == MotionEvent.ACTION_DOWN) {
                roleInfoPanel.toggleVisibility();
                return true; // Handled by info button
            }
            // For ACTION_UP on info button, just return true without toggling again
            if (action == MotionEvent.ACTION_UP) {
                return true;
            }
        }

        // Check if touching D-pad
        if (isInCircle(x, y, dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width() / 2)) {
            handled = true;

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                upPressed = false;
                downPressed = false;
                leftPressed = false;
                rightPressed = false;
                player.setMovingUp(false);
                player.setMovingDown(false);
                player.setMovingLeft(false);
                player.setMovingRight(false);
            } else {
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

                player.setMovingUp(upPressed);
                player.setMovingDown(downPressed);
                player.setMovingLeft(leftPressed);
                player.setMovingRight(rightPressed);
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:

                // Check attack buttons (still use rectangular detection)
                meleeAttackPressed = meleeAttackButton != null && meleeAttackButton.contains((int) x, (int) y);
                magicAttackPressed = magicAttackButton != null && magicAttackButton.contains((int) x, (int) y);

                // Perform attacks when buttons are pressed
                if (meleeAttackPressed) {
                    List<EnemyHitInfo> hits = player.performMeleeAttack(enemies);
                    if (hits != null) {
                        // Create damage numbers for each hit enemy
                        for (EnemyHitInfo hit : hits) {
                            damageNumbers.add(new DamageNumber(
                                    hit.enemy.getX(),
                                    hit.enemy.getY() - 30,
                                    hit.damage
                            ));
                        }
                    }
                }

                if (magicAttackPressed) {
                    // Cast spell in the direction player is facing
                    List<Projectile> list = player.castTripleSpell(Projectile.Type.FIREBALL);
                    if (list != null) {
                        projectiles.addAll(list);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                meleeAttackPressed = false;
                magicAttackPressed = false;

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

        // Attack buttons (bottom-right) - Also smaller
        int attackButtonSize = (int) (buttonSize * 0.8);
        int attackPadding = (int) (buttonSize * 0.5);

        // Melee attack button (left)
        meleeAttackButton = new Rect(
                screenWidth - attackPadding * 2 - attackButtonSize * 2,
                screenHeight - attackPadding - attackButtonSize,
                screenWidth - attackPadding * 2 - attackButtonSize,
                screenHeight - attackPadding
        );

        // Magic attack button (right)
        magicAttackButton = new Rect(
                screenWidth - attackPadding - attackButtonSize,
                screenHeight - attackPadding - attackButtonSize,
                screenWidth - attackPadding,
                screenHeight - attackPadding
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

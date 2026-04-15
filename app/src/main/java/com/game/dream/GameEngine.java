package com.game.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

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
    // Map renderer (extracted to separate class)
    private MapRenderer mapRenderer;

    // Minimap
    private Minimap minimap;

    // Day-night cycle
    private DayNightCycle dayNightCycle;
    private long lastUpdateTime;

    // Weather system
    private WeatherSystem weatherSystem;

    // Control buttons
    private Rect upButton, downButton, leftButton, rightButton;
    private Rect dpadBounds;
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // Terrain types
    private static final int PLAIN = 0;
    private static final int GRASSLAND = 1;
    private static final int FOREST = 2;
    private static final int LAKE = 3;
    private static final int SNOW = 4;
    private static final int SWAMP = 5;
    private static final int LAVA = 6;

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
        generateMap();

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
                        if (terrain != LAKE && terrain != LAVA) {
                            startX = checkX;
                            startY = checkY;
                            foundValidPosition = true;
                        }
                    }
                }
            }
        }

        // Create player at center of map
        player = new Player(startX * TILE_SIZE + TILE_SIZE/2, startY * TILE_SIZE + TILE_SIZE/2);
        player.setSize(TILE_SIZE);

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

    private void generateMap() {
        map = new int[MAP_HEIGHT / TILE_SIZE][MAP_WIDTH / TILE_SIZE];

        // Generate terrain using simple noise-like algorithm
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                map[y][x] = generateTerrainType(x, y);
            }
        }
    }

    private int generateTerrainType(int gridX, int gridY) {
        // Use coordinates to create interesting terrain patterns
        double noise = Math.sin(gridX * 0.05) * Math.cos(gridY * 0.05) +
                Math.sin(gridX * 0.02 + gridY * 0.03) * 0.5;

        if (noise > 0.8) {
            return SNOW;
        } else if (noise > 0.5) {
            return FOREST;
        } else if (noise > 0.2) {
            return GRASSLAND;
        } else if (noise > -0.2) {
            return PLAIN;
        } else if (noise > -0.5) {
            return SWAMP;
        } else if (noise > -0.8) {
            return LAKE;
        } else {
            return LAVA;
        }
    }

    public void update() {
        // Update player movement
        player.update(map, MAP_WIDTH / TILE_SIZE, MAP_HEIGHT / TILE_SIZE, TILE_SIZE);

        // Update camera to follow player
        updateCamera();

        // Update FPS counter
        updateFPS();

        // Update day-night cycle
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        dayNightCycle.update(deltaTime);
        lastUpdateTime = currentTime;

        // Update weather system
        if (weatherSystem != null) {
            weatherSystem.update(deltaTime, screenWidth, screenHeight);
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

        // Draw weather effects
        if (weatherSystem != null) {
            weatherSystem.draw(canvas);
        }

        // Draw player
        player.draw(canvas, (int)-cameraX, (int)-cameraY);

        // Draw UI
        drawUI(canvas);

        // Draw minimap
        if (minimap != null) {
            minimap.draw(canvas, player.getX(), player.getY(), screenWidth, screenHeight);
        }

        // Draw controls
        drawControls(canvas);
    }

    private void drawUI(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);

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
        canvas.drawText("Position: (" + (int)player.getX() + ", " + (int)player.getY() + ")", 10, 120, paint);

        // Draw terrain info
        int playerGridX = (int)(player.getX() / TILE_SIZE);
        int playerGridY = (int)(player.getY() / TILE_SIZE);
        String terrainName = getTerrainName(map[playerGridY][playerGridX]);
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

    private String getTerrainName(int terrainType) {
        switch (terrainType) {
            case PLAIN: return "Plain";
            case GRASSLAND: return "Grassland";
            case FOREST: return "Forest";
            case LAKE: return "Lake";
            case SNOW: return "Snow";
            case SWAMP: return "Swamp";
            case LAVA: return "Lava";
            default: return "Unknown";
        }
    }

    private void drawControls(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAlpha(150);

        // Draw D-pad
        paint.setColor(Color.rgb(40, 80, 160));
        canvas.drawCircle(dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width()/2, paint);

        // Draw directional arrows
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("↑", dpadBounds.centerX(), dpadBounds.centerY() - dpadBounds.height()/4 + 15, paint);
        canvas.drawText("↓", dpadBounds.centerX(), dpadBounds.centerY() + dpadBounds.height()/4 + 15, paint);
        canvas.drawText("←", dpadBounds.centerX() - dpadBounds.width()/4, dpadBounds.centerY() + 15, paint);
        canvas.drawText("→", dpadBounds.centerX() + dpadBounds.width()/4, dpadBounds.centerY() + 15, paint);

        paint.setAlpha(255);
    }

    public boolean handleTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        boolean handled = false;

        // Check if touching D-pad
        if (isInCircle(x, y, dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width()/2)) {
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

        return handled;
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
    }

    private void initControlButtons() {
        int buttonSize = Math.min(screenWidth, screenHeight) / 8;

        int leftAreaCenterX = screenWidth / 8;
        int leftAreaCenterY = screenHeight / 2;

        int dpadRadius = (int)(buttonSize * 1.8f);
        dpadBounds = new Rect(
                leftAreaCenterX - dpadRadius,
                leftAreaCenterY - dpadRadius,
                leftAreaCenterX + dpadRadius,
                leftAreaCenterY + dpadRadius
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
}

package com.game.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

public class GameEngine {
    private Context context;
    private int screenWidth;
    private int screenHeight;

    // Map dimensions
    private static final int MAP_WIDTH = 10000;
    private static final int MAP_HEIGHT = 10000;
    private static final int TILE_SIZE = 50;

    // Camera position (top-left corner of visible area)
    private float cameraX;
    private float cameraY;

    // Player
    private Player player;

    // Map data
    private int[][] map; // 0=plain, 1=grassland, 2=forest, 3=lake, 4=snow, 5=swamp, 6=lava

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

    public GameEngine(Context context) {
        this.context = context;
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
    }

    private void updateCamera() {
        // Center camera on player
        cameraX = player.getX() - screenWidth / 2;
        cameraY = player.getY() - screenHeight / 2;

        // Clamp camera to map bounds
        cameraX = Math.max(0, Math.min(cameraX, MAP_WIDTH - screenWidth));
        cameraY = Math.max(0, Math.min(cameraY, MAP_HEIGHT - screenHeight));
    }

    public void draw(Canvas canvas) {
        // Draw background
        canvas.drawColor(Color.BLACK);

        // Draw visible portion of map
        drawMap(canvas);

        // Draw player
        player.draw(canvas, (int)-cameraX, (int)-cameraY);

        // Draw UI
        drawUI(canvas);

        // Draw controls
        drawControls(canvas);
    }

    private void drawMap(Canvas canvas) {
        Paint paint = new Paint();

        // Calculate visible tile range
        int startTileX = (int)(cameraX / TILE_SIZE);
        int endTileX = (int)((cameraX + screenWidth) / TILE_SIZE) + 1;
        int startTileY = (int)(cameraY / TILE_SIZE);
        int endTileY = (int)((cameraY + screenHeight) / TILE_SIZE) + 1;

        // Clamp to map bounds
        startTileX = Math.max(0, startTileX);
        startTileY = Math.max(0, startTileY);
        endTileX = Math.min(map[0].length, endTileX);
        endTileY = Math.min(map.length, endTileY);

        // Draw only visible tiles
        for (int y = startTileY; y < endTileY; y++) {
            for (int x = startTileX; x < endTileX; x++) {
                int screenX = (int)(x * TILE_SIZE - cameraX);
                int screenY = (int)(y * TILE_SIZE - cameraY);

                switch (map[y][x]) {
                    case PLAIN:
                        paint.setColor(Color.rgb(210, 180, 140)); // Tan
                        break;
                    case GRASSLAND:
                        paint.setColor(Color.rgb(34, 139, 34)); // Forest green
                        break;
                    case FOREST:
                        paint.setColor(Color.rgb(0, 100, 0)); // Dark green
                        break;
                    case LAKE:
                        paint.setColor(Color.rgb(30, 144, 255)); // Dodger blue
                        break;
                    case SNOW:
                        paint.setColor(Color.rgb(255, 250, 250)); // Snow white
                        break;
                    case SWAMP:
                        paint.setColor(Color.rgb(85, 107, 47)); // Dark olive green
                        break;
                    case LAVA:
                        paint.setColor(Color.rgb(255, 69, 0)); // Red orange
                        break;
                    default:
                        paint.setColor(Color.GRAY);
                        break;
                }

                canvas.drawRect(screenX, screenY, screenX + TILE_SIZE, screenY + TILE_SIZE, paint);

                // Draw grid lines
                // Draw grid lines
                paint.setColor(Color.argb(50, 0, 0, 0));
                paint.setStrokeWidth(1);
                canvas.drawLine(screenX, screenY, screenX + TILE_SIZE, screenY, paint);
                canvas.drawLine(screenX, screenY, screenX, screenY + TILE_SIZE, paint);
            }
        }
    }

    private void drawUI(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);

        // Draw coordinates
        canvas.drawText("Position: (" + (int)player.getX() + ", " + (int)player.getY() + ")", 10, 40, paint);

        // Draw terrain info
        int playerGridX = (int)(player.getX() / TILE_SIZE);
        int playerGridY = (int)(player.getY() / TILE_SIZE);
        String terrainName = getTerrainName(map[playerGridY][playerGridX]);
        canvas.drawText("Terrain: " + terrainName, 10, 80, paint);
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
        this.screenWidth = width;
        this.screenHeight = height;

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
}

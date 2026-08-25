package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.game.dream.bean.MapInfo;

/**
 * Renders a minimap showing the player's position in the world
 */
public class Minimap {
    private MapInfo map;
    private int mapWidth;
    private int mapHeight;
    private int tileSize;

    // Minimap dimensions
    private int minimapSize = 200; // Size in pixels
    private float scale;

    // Pre-rendered minimap bitmap
    private Bitmap minimapBitmap;
    private boolean isInitialized = false;

    // Position indicator
    private Paint playerIndicatorPaint;
    private Paint borderPaint;
    private Paint backgroundPaint;

    public Minimap(MapInfo mapInfo, int mapWidth, int mapHeight, int tileSize) {
        this.map = mapInfo;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;

        // Calculate scale to fit entire map in minimap
        this.scale = (float)minimapSize / Math.max(mapWidth, mapHeight);

        // Initialize paints
        playerIndicatorPaint = new Paint();
        playerIndicatorPaint.setColor(Color.RED);
        playerIndicatorPaint.setAntiAlias(true);
        playerIndicatorPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(180, 0, 0, 0)); // Semi-transparent black
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Initialize the minimap by rendering the entire map at reduced scale
     */
    public void initialize() {
        if (isInitialized && minimapBitmap != null && !minimapBitmap.isRecycled()) {
            return;
        }

        // 超大地图跳过小地图渲染（避免主线程卡顿）
        if (mapWidth > 20000 || mapHeight > 20000) {
            isInitialized = true;
            return;
        }

        // Create minimap bitmap
        minimapBitmap = Bitmap.createBitmap(minimapSize, minimapSize, Bitmap.Config.RGB_565);
        Canvas minimapCanvas = new Canvas(minimapBitmap);

        // Fill background
        minimapCanvas.drawColor(Color.BLACK);

        Paint paint = new Paint();
        paint.setAntiAlias(false);

        // Draw each tile scaled down
        int mapTilesX = mapWidth / tileSize;
        int mapTilesY = mapHeight / tileSize;

        // 优化: 对于超大地图，每隔 step 采样一次
        int step = Math.max(1, Math.max(mapTilesX, mapTilesY) / 200);
        float minimapTileSize = tileSize * scale * step;

        for (int y = 0; y < mapTilesY; y += step) {
            for (int x = 0; x < mapTilesX; x += step) {
                int terrain = map.getMapData()[y][x];

                // Set color based on terrain
                switch (terrain) {
                    case 0: // PLAIN
                        paint.setColor(Color.rgb(210, 180, 140));
                        break;
                    case 1: // GRASSLAND
                        paint.setColor(Color.rgb(34, 139, 34));
                        break;
                    case 2: // FOREST
                        paint.setColor(Color.rgb(0, 100, 0));
                        break;
                    case 3: // LAKE
                        paint.setColor(Color.rgb(30, 144, 255));
                        break;
                    case 4: // SNOW
                        paint.setColor(Color.rgb(255, 250, 250));
                        break;
                    case 5: // SWAMP
                        paint.setColor(Color.rgb(85, 107, 47));
                        break;
                    case 6: // LAVA
                        paint.setColor(Color.rgb(255, 69, 0));
                        break;
                    case 7: // RIVER
                        paint.setColor(Color.rgb(50, 120, 220));
                        break;
                    case 8: // MOUNTAIN
                        paint.setColor(Color.rgb(110, 100, 90));
                        break;
                    case 9: // BRIDGE
                        paint.setColor(Color.rgb(160, 140, 100));
                        break;
                    case 10: // FARMLAND
                        paint.setColor(Color.rgb(180, 200, 100));
                        break;
                    case 11: // CITY_ROAD
                        paint.setColor(Color.rgb(190, 180, 160));
                        break;
                    case 12: // CITY_WALL
                        paint.setColor(Color.rgb(100, 95, 90));
                        break;
                    case 13: // SAND
                        paint.setColor(Color.rgb(235, 215, 160));
                        break;
                    case 14: // SEA
                        paint.setColor(Color.rgb(20, 80, 170));
                        break;
                    case 100: // VILLAGE_CAN_PASS
                        paint.setColor(Color.rgb(215, 162, 109));
                        break;
                    case 200: // MAZE_WALL
                        paint.setColor(Color.rgb(85, 80, 95));
                        break;
                    case 201: // MAZE_FLOOR
                        paint.setColor(Color.rgb(160, 150, 130));
                        break;
                    case 202: // MAZE_ENTRANCE
                        paint.setColor(Color.rgb(60, 120, 255));
                        break;
                    case 203: // MAZE_EXIT
                        paint.setColor(Color.rgb(255, 200, 50));
                        break;
                    default:
                        paint.setColor(Color.GRAY);
                        break;
                }

                // Calculate position and size on minimap
                float minimapX = x * tileSize * scale;
                float minimapY = y * tileSize * scale;

                // Ensure we cover the entire minimap (avoid gaps)
                float drawWidth = minimapTileSize + 1;
                float drawHeight = minimapTileSize + 1;

                minimapCanvas.drawRect(minimapX, minimapY,
                        minimapX + drawWidth,
                        minimapY + drawHeight, paint);
            }
        }

        isInitialized = true;
    }

    /**
     * Draw the minimap on the screen (top-right corner)
     */
    public void draw(Canvas canvas, float playerX, float playerY, int screenWidth, int screenHeight) {
        if (!isInitialized || minimapBitmap == null || minimapBitmap.isRecycled()) {
            initialize();
        }

        // 超大地图跳过了小地图渲染，直接返回
        if (minimapBitmap == null || minimapBitmap.isRecycled()) {
            return;
        }

        // Position minimap in top-right corner with padding
        int padding = 10;
        int minimapX = screenWidth - minimapSize - padding;
        int minimapY = padding;

        // Draw semi-transparent background
        RectF backgroundRect = new RectF(minimapX - 5, minimapY - 5,
                minimapX + minimapSize + 5,
                minimapY + minimapSize + 5);
        canvas.drawRoundRect(backgroundRect, 10, 10, backgroundPaint);

        // Draw minimap bitmap
        Rect srcRect = new Rect(0, 0, minimapSize, minimapSize);
        Rect dstRect = new Rect(minimapX, minimapY,
                minimapX + minimapSize,
                minimapY + minimapSize);
        canvas.drawBitmap(minimapBitmap, srcRect, dstRect, null);

        // Draw border
        canvas.drawRect(dstRect, borderPaint);

        // Calculate player position on minimap
        float playerMinimapX = minimapX + (playerX * scale);
        float playerMinimapY = minimapY + (playerY * scale);

        // Clamp to minimap bounds
        playerMinimapX = Math.max(minimapX, Math.min(playerMinimapX, minimapX + minimapSize));
        playerMinimapY = Math.max(minimapY, Math.min(playerMinimapY, minimapY + minimapSize));

        // Draw player indicator (red dot)
        float indicatorRadius = 4;
        canvas.drawCircle(playerMinimapX, playerMinimapY, indicatorRadius, playerIndicatorPaint);

        // Draw white outline around player indicator
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setStrokeWidth(1.5f);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setAntiAlias(true);
        canvas.drawCircle(playerMinimapX, playerMinimapY, indicatorRadius, outlinePaint);

        // Draw "Minimap" label
        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(16);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Minimap", minimapX + minimapSize / 2, minimapY - 8, labelPaint);
    }

    /**
     * Set minimap size
     */
    public void setMinimapSize(int size) {
        this.minimapSize = size;
        this.scale = (float)minimapSize / Math.max(mapWidth, mapHeight);
        isInitialized = false; // Force re-initialization
    }

    /**
     * Get minimap size
     */
    public int getMinimapSize() {
        return minimapSize;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (minimapBitmap != null && !minimapBitmap.isRecycled()) {
            minimapBitmap.recycle();
            minimapBitmap = null;
        }
        isInitialized = false;
    }
}

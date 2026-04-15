package com.game.dream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Renders a minimap showing the player's position in the world
 */
public class Minimap {
    private int[][] map;
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

    public Minimap(int[][] map, int mapWidth, int mapHeight, int tileSize) {
        this.map = map;
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

        for (int y = 0; y < mapTilesY; y++) {
            for (int x = 0; x < mapTilesX; x++) {
                int terrain = map[y][x];

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
                    default:
                        paint.setColor(Color.GRAY);
                        break;
                }

                // Calculate position and size on minimap
                float minimapX = x * tileSize * scale;
                float minimapY = y * tileSize * scale;
                float minimapTileSize = tileSize * scale;

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

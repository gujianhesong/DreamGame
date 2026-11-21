package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.game.dream.bean.MapInfo;

/**
 * 小地图：显示玩家周围局部区域，玩家固定在中心。
 */
public class Minimap {
    /** 小地图视口半径（世界像素），只显示该范围内的地形 */
    private static final float VIEW_WORLD_RADIUS = 2400f;

    private MapInfo map;
    private int mapWidth;
    private int mapHeight;
    private int tileSize;

    private int minimapSize = 200;
    private float viewScale;

    private Bitmap minimapBitmap;
    private boolean isInitialized = false;
    private boolean skipBitmapForHugeMap;

    private Paint playerIndicatorPaint;
    private Paint borderPaint;
    private Paint backgroundPaint;

    public Minimap(MapInfo mapInfo, int mapWidth, int mapHeight, int tileSize) {
        this.map = mapInfo;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        updateViewScale();

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
        backgroundPaint.setColor(Color.argb(180, 0, 0, 0));
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    private void updateViewScale() {
        viewScale = minimapSize / (2f * VIEW_WORLD_RADIUS);
    }

    public void initialize() {
        if (isInitialized && minimapBitmap != null && !minimapBitmap.isRecycled()) {
            return;
        }

        skipBitmapForHugeMap = mapWidth > 20000 || mapHeight > 20000;

        if (skipBitmapForHugeMap) {
            isInitialized = true;
            return;
        }

        minimapBitmap = MapOverviewBitmap.render(map, mapWidth, mapHeight, tileSize, minimapSize);
        isInitialized = true;
    }

    public Rect getScreenBounds(int screenWidth, int screenHeight) {
        int padding = 10;
        int minimapX = screenWidth - minimapSize - padding;
        int minimapY = padding;
        return new Rect(minimapX - 5, minimapY - 5,
                minimapX + minimapSize + 5, minimapY + minimapSize + 5);
    }

    public boolean hitTest(float x, float y, int screenWidth, int screenHeight) {
        return getScreenBounds(screenWidth, screenHeight).contains((int) x, (int) y);
    }

    public void draw(Canvas canvas, float playerX, float playerY, int screenWidth, int screenHeight) {
        if (!isInitialized) {
            initialize();
        }

        int padding = 10;
        int minimapX = screenWidth - minimapSize - padding;
        int minimapY = padding;

        RectF backgroundRect = new RectF(minimapX - 5, minimapY - 5,
                minimapX + minimapSize + 5,
                minimapY + minimapSize + 5);
        canvas.drawRoundRect(backgroundRect, 10, 10, backgroundPaint);

        Rect dstRect = new Rect(minimapX, minimapY,
                minimapX + minimapSize,
                minimapY + minimapSize);

        Paint fill = new Paint();
        fill.setColor(Color.rgb(20, 22, 28));
        canvas.drawRect(dstRect, fill);

        int save = canvas.save();
        canvas.clipRect(dstRect);

        float centerX = minimapX + minimapSize / 2f;
        float centerY = minimapY + minimapSize / 2f;

        if (minimapBitmap != null && !minimapBitmap.isRecycled()) {
            RectF mapDst = new RectF(
                    centerX - playerX * viewScale,
                    centerY - playerY * viewScale,
                    centerX + (mapWidth - playerX) * viewScale,
                    centerY + (mapHeight - playerY) * viewScale
            );
            Rect src = new Rect(0, 0, minimapBitmap.getWidth(), minimapBitmap.getHeight());
            canvas.drawBitmap(minimapBitmap, src, mapDst, null);
        } else if (map != null && map.getMapData() != null) {
            drawLocalTerrain(canvas, playerX, playerY, centerX, centerY);
        } else {
            Paint placeholder = new Paint();
            placeholder.setColor(Color.rgb(35, 40, 50));
            canvas.drawRect(dstRect, placeholder);
            placeholder.setColor(Color.rgb(140, 170, 200));
            placeholder.setTextSize(16);
            placeholder.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("点击查看大地图", dstRect.centerX(), dstRect.centerY() + 6, placeholder);
        }

        canvas.restoreToCount(save);
        canvas.drawRect(dstRect, borderPaint);

        float indicatorRadius = 5f;
        canvas.drawCircle(centerX, centerY, indicatorRadius, playerIndicatorPaint);

        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setStrokeWidth(1.5f);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, indicatorRadius, outlinePaint);

        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(16);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("小地图", minimapX + minimapSize / 2, minimapY - 8, labelPaint);
    }

    private void drawLocalTerrain(Canvas canvas, float playerX, float playerY,
                                  float centerX, float centerY) {
        int[][] data = map.getMapData();
        int mapTilesX = mapWidth / tileSize;
        int mapTilesY = mapHeight / tileSize;

        float half = VIEW_WORLD_RADIUS;
        int worldLeft = (int) Math.max(0, playerX - half);
        int worldRight = (int) Math.min(mapWidth, playerX + half);
        int worldTop = (int) Math.max(0, playerY - half);
        int worldBottom = (int) Math.min(mapHeight, playerY + half);

        int tx0 = Math.max(0, worldLeft / tileSize);
        int tx1 = Math.min(mapTilesX - 1, worldRight / tileSize);
        int ty0 = Math.max(0, worldTop / tileSize);
        int ty1 = Math.min(mapTilesY - 1, worldBottom / tileSize);

        int step = skipBitmapForHugeMap ? 2 : 1;
        float tilePx = tileSize * viewScale;

        Paint paint = new Paint();
        paint.setAntiAlias(false);

        for (int ty = ty0; ty <= ty1; ty += step) {
            for (int tx = tx0; tx <= tx1; tx += step) {
                float wx = tx * tileSize;
                float wy = ty * tileSize;
                float sx = centerX + (wx - playerX) * viewScale;
                float sy = centerY + (wy - playerY) * viewScale;
                float drawSize = tilePx * step + 1;
                paint.setColor(MapOverviewBitmap.colorForTerrain(data[ty][tx]));
                canvas.drawRect(sx, sy, sx + drawSize, sy + drawSize, paint);
            }
        }
    }

    public void setMinimapSize(int size) {
        this.minimapSize = size;
        updateViewScale();
        isInitialized = false;
    }

    public int getMinimapSize() {
        return minimapSize;
    }

    public void cleanup() {
        if (minimapBitmap != null && !minimapBitmap.isRecycled()) {
            minimapBitmap.recycle();
            minimapBitmap = null;
        }
        isInitialized = false;
        skipBitmapForHugeMap = false;
    }
}
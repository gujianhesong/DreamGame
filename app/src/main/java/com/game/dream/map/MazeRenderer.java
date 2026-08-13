package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.LruCache;

/**
 * 迷宫地图渲染器 - 使用 chunk 缓存优化渲染性能
 * 将静态地形预渲染到 Bitmap chunk 中, 每帧只做 bitmap blit
 */
public class MazeRenderer {

    private int[][] map;
    private int mapWidth;
    private int mapHeight;
    private int tileSize;

    private Paint wallPaint;
    private Paint floorPaint;
    private Paint wallBorderPaint;
    private Paint entrancePaint;
    private Paint exitPaint;
    private Paint entranceGlowPaint;
    private Paint exitGlowPaint;

    // Chunk 缓存
    private static final int CHUNK_TILES = 16; // 每个 chunk = 16x16 tiles
    private int chunkPixelSize;
    private LruCache<String, Bitmap> chunkCache;
    private Paint chunkPaint;

    // 入口/出口位置 (用于每帧绘制动态效果)
    private int entranceTileCol = -1, entranceTileRow = -1;
    private int exitTileCol = -1, exitTileRow = -1;

    public MazeRenderer(int[][] map, int mapWidth, int mapHeight, int tileSize) {
        this.map = map;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.chunkPixelSize = CHUNK_TILES * tileSize;

        // 缓存最多 200 个 chunk (约 200 * 320 * 320 * 2 bytes ≈ 40MB)
        chunkCache = new LruCache<String, Bitmap>(200) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };

        chunkPaint = new Paint();
        chunkPaint.setAntiAlias(false);
        chunkPaint.setFilterBitmap(false);

        initPaints();
        // 预查找入口/出口 tile 位置
        findEntranceAndExit();
    }

    private void initPaints() {
        wallPaint = new Paint();
        wallPaint.setColor(Color.rgb(85, 80, 95));
        wallPaint.setAntiAlias(false);

        floorPaint = new Paint();
        floorPaint.setColor(Color.rgb(160, 150, 130));
        floorPaint.setAntiAlias(false);

        wallBorderPaint = new Paint();
        wallBorderPaint.setColor(Color.rgb(65, 60, 75));
        wallBorderPaint.setStyle(Paint.Style.STROKE);
        wallBorderPaint.setStrokeWidth(1);
        wallBorderPaint.setAntiAlias(false);

        entrancePaint = new Paint();
        entrancePaint.setColor(Color.argb(180, 60, 120, 255));
        entrancePaint.setAntiAlias(true);

        exitPaint = new Paint();
        exitPaint.setColor(Color.argb(200, 255, 200, 50));
        exitPaint.setAntiAlias(true);

        entranceGlowPaint = new Paint();
        entranceGlowPaint.setColor(Color.argb(60, 60, 120, 255));
        entranceGlowPaint.setAntiAlias(true);

        exitGlowPaint = new Paint();
        exitGlowPaint.setColor(Color.argb(60, 255, 200, 50));
        exitGlowPaint.setAntiAlias(true);
    }

    private void findEntranceAndExit() {
        for (int row = 0; row < Math.min(10, map.length); row++) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col] == MazeGenerator.MAZE_ENTRANCE) {
                    entranceTileCol = col;
                    entranceTileRow = row;
                    return;
                }
            }
        }
        for (int row = map.length - 1; row > map.length - 10; row--) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col] == MazeGenerator.MAZE_EXIT) {
                    exitTileCol = col;
                    exitTileRow = row;
                    return;
                }
            }
        }
    }

    /**
     * 绘制迷宫 - 使用 chunk 缓存
     */
    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        // 计算可见 chunk 范围
        int startChunkX = Math.max(0, (int) (cameraX / chunkPixelSize) - 1);
        int endChunkX = (int) ((cameraX + screenWidth) / chunkPixelSize) + 1;
        int startChunkY = Math.max(0, (int) (cameraY / chunkPixelSize) - 1);
        int endChunkY = (int) ((cameraY + screenHeight) / chunkPixelSize) + 1;

        int maxChunkX = (map[0].length + CHUNK_TILES - 1) / CHUNK_TILES;
        int maxChunkY = (map.length + CHUNK_TILES - 1) / CHUNK_TILES;
        endChunkX = Math.min(endChunkX, maxChunkX - 1);
        endChunkY = Math.min(endChunkY, maxChunkY - 1);

        // 绘制缓存的 chunk
        for (int cy = startChunkY; cy <= endChunkY; cy++) {
            for (int cx = startChunkX; cx <= endChunkX; cx++) {
                Bitmap chunk = getChunk(cx, cy);
                if (chunk != null) {
                    int dstX = (int) (cx * chunkPixelSize - cameraX);
                    int dstY = (int) (cy * chunkPixelSize - cameraY);
                    canvas.drawBitmap(chunk, dstX, dstY, chunkPaint);
                }
            }
        }

        // 每帧绘制入口/出口动态效果 (光圈 + 文字)
        drawEntranceExit(canvas, cameraX, cameraY, screenWidth, screenHeight);
    }

    /**
     * 获取或创建 chunk bitmap
     */
    private Bitmap getChunk(int chunkX, int chunkY) {
        String key = chunkX + "_" + chunkY;
        Bitmap cached = chunkCache.get(key);
        if (cached != null && !cached.isRecycled()) {
            return cached;
        }

        // 创建新 chunk
        int startCol = chunkX * CHUNK_TILES;
        int startRow = chunkY * CHUNK_TILES;
        int endCol = Math.min(startCol + CHUNK_TILES, map[0].length);
        int endRow = Math.min(startRow + CHUNK_TILES, map.length);
        int w = (endCol - startCol) * tileSize;
        int h = (endRow - startRow) * tileSize;
        if (w <= 0 || h <= 0) return null;

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas chunkCanvas = new Canvas(bitmap);

        // 渲染 chunk 内的每个 tile
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                float x = (col - startCol) * tileSize;
                float y = (row - startRow) * tileSize;
                int terrain = map[row][col];

                switch (terrain) {
                    case MazeGenerator.MAZE_WALL:
                        drawWall(chunkCanvas, x, y, col, row);
                        break;
                    case MazeGenerator.MAZE_FLOOR:
                        drawFloor(chunkCanvas, x, y, col, row);
                        break;
                    case MazeGenerator.MAZE_ENTRANCE:
                        drawFloor(chunkCanvas, x, y, col, row);
                        break;
                    case MazeGenerator.MAZE_EXIT:
                        drawFloor(chunkCanvas, x, y, col, row);
                        break;
                }
            }
        }

        chunkCache.put(key, bitmap);
        return bitmap;
    }

    /**
     * 绘制入口/出口动态效果
     */
    private void drawEntranceExit(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        // 入口
        if (entranceTileCol >= 0) {
            float sx = entranceTileCol * tileSize - cameraX;
            float sy = entranceTileRow * tileSize - cameraY;
            if (sx > -tileSize * 3 && sx < screenWidth + tileSize * 3
                    && sy > -tileSize * 3 && sy < screenHeight + tileSize * 3) {
                float cx = sx + tileSize / 2f;
                float cy = sy + tileSize / 2f;
                canvas.drawCircle(cx, cy, tileSize * 1.5f, entranceGlowPaint);
                canvas.drawCircle(cx, cy, tileSize * 0.6f, entrancePaint);

                Paint textPaint = new Paint();
                textPaint.setColor(Color.argb(220, 100, 180, 255));
                textPaint.setTextSize(24);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setAntiAlias(true);
                canvas.drawText("入口", cx, cy - tileSize, textPaint);
            }
        }

        // 出口
        if (exitTileCol >= 0) {
            float sx = exitTileCol * tileSize - cameraX;
            float sy = exitTileRow * tileSize - cameraY;
            if (sx > -tileSize * 3 && sx < screenWidth + tileSize * 3
                    && sy > -tileSize * 3 && sy < screenHeight + tileSize * 3) {
                float cx = sx + tileSize / 2f;
                float cy = sy + tileSize / 2f;
                canvas.drawCircle(cx, cy, tileSize * 1.5f, exitGlowPaint);
                canvas.drawCircle(cx, cy, tileSize * 0.6f, exitPaint);

                Paint textPaint = new Paint();
                textPaint.setColor(Color.argb(220, 255, 215, 0));
                textPaint.setTextSize(24);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setAntiAlias(true);
                canvas.drawText("出口", cx, cy - tileSize, textPaint);
            }
        }
    }

    /**
     * 绘制墙壁 - 石砖纹理 (预渲染到 chunk)
     */
    private void drawWall(Canvas canvas, float x, float y, int col, int row) {
        canvas.drawRect(x, y, x + tileSize, y + tileSize, wallPaint);

        if ((row + col) % 2 == 0) {
            canvas.drawLine(x, y + tileSize / 2f, x + tileSize, y + tileSize / 2f, wallBorderPaint);
            canvas.drawLine(x + tileSize / 2f, y, x + tileSize / 2f, y + tileSize / 2f, wallBorderPaint);
            canvas.drawLine(x, y + tileSize / 2f, x, y + tileSize, wallBorderPaint);
        } else {
            canvas.drawLine(x, y + tileSize / 2f, x + tileSize, y + tileSize / 2f, wallBorderPaint);
            canvas.drawLine(x + tileSize / 3f, y + tileSize / 2f, x + tileSize / 3f, y + tileSize, wallBorderPaint);
            canvas.drawLine(x + tileSize * 2 / 3f, y, x + tileSize * 2 / 3f, y + tileSize / 2f, wallBorderPaint);
        }

        if ((row * 7 + col * 13) % 5 == 0) {
            wallPaint.setColor(Color.rgb(95, 90, 105));
            canvas.drawRect(x + 1, y + 1, x + tileSize - 1, y + tileSize - 1, wallPaint);
            wallPaint.setColor(Color.rgb(85, 80, 95));
        }
    }

    /**
     * 绘制地板 - 石板效果 (预渲染到 chunk)
     */
    private void drawFloor(Canvas canvas, float x, float y, int col, int row) {
        canvas.drawRect(x, y, x + tileSize, y + tileSize, floorPaint);

        wallBorderPaint.setColor(Color.argb(40, 120, 110, 100));
        canvas.drawRect(x, y, x + tileSize, y + tileSize, wallBorderPaint);
        wallBorderPaint.setColor(Color.rgb(65, 60, 75));

        if ((row * 11 + col * 7) % 17 == 0) {
            floorPaint.setColor(Color.rgb(175, 165, 145));
            canvas.drawCircle(x + tileSize / 3f, y + tileSize / 3f, 1.5f, floorPaint);
            floorPaint.setColor(Color.rgb(160, 150, 130));
        }
    }

    public void cleanup() {
        if (chunkCache != null) {
            chunkCache.evictAll();
        }
    }
}

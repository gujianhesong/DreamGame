package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.LruCache;

/**
 * 地狱迷宫渲染器 - 4层不同视觉主题，chunk缓存优化
 *
 * 第1层 黄泉迷径：土黄岩壁 + 黄泉水流地面
 * 第2层 血池炼狱：暗红血痂壁 + 翻涌血池 + 灰色石台
 * 第3层 枉死城：灰白石城 + 锁链装饰 + 铁门
 * 第4层 阎罗殿：黑金宫殿 + 幽绿鬼火
 */
public class HellMazeRenderer {

    private int[][] map;
    private int mapWidth, mapHeight, tileSize;
    private int floor; // 1-4

    // Chunk 缓存
    private static final int CHUNK_TILES = 16;
    private int chunkPixelSize;
    private LruCache<String, Bitmap> chunkCache;
    private Paint chunkPaint;

    // 入口/出口 tile 位置
    private int entranceTileCol = -1, entranceTileRow = -1;
    private int exitTileCol = -1, exitTileRow = -1;

    // 各层配色
    private int wallColor, wallBorderColor, floorColor, floorAccentColor;
    private int specialColor1, specialColor2; // 血池/陷阱/BOSS区域

    public HellMazeRenderer(int[][] map, int mapWidth, int mapHeight, int tileSize, int floor) {
        this.map = map;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.floor = floor;
        this.chunkPixelSize = CHUNK_TILES * tileSize;

        chunkCache = new LruCache<String, Bitmap>(200) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null && !oldValue.isRecycled()) oldValue.recycle();
            }
        };
        chunkPaint = new Paint();
        chunkPaint.setAntiAlias(false);
        chunkPaint.setFilterBitmap(false);

        initColors();
        findEntranceAndExit();
    }

    private void initColors() {
        switch (floor) {
            case 1: // 黄泉迷径 - 土黄/暗褐
                wallColor = Color.rgb(90, 70, 40);
                wallBorderColor = Color.rgb(65, 50, 28);
                floorColor = Color.rgb(140, 120, 80);
                floorAccentColor = Color.rgb(160, 140, 95);
                specialColor1 = Color.argb(180, 200, 180, 60); // 入口光
                specialColor2 = Color.argb(200, 255, 200, 50); // 出口光
                break;
            case 2: // 血池炼狱 - 暗红/黑
                wallColor = Color.rgb(80, 25, 25);
                wallBorderColor = Color.rgb(55, 15, 15);
                floorColor = Color.rgb(100, 60, 50);
                floorAccentColor = Color.rgb(120, 70, 55);
                specialColor1 = Color.rgb(180, 20, 20);  // 血池
                specialColor2 = Color.rgb(130, 130, 140); // 安全石台
                break;
            case 3: // 枉死城 - 灰白/铁灰
                wallColor = Color.rgb(100, 100, 110);
                wallBorderColor = Color.rgb(70, 70, 80);
                floorColor = Color.rgb(150, 148, 145);
                floorAccentColor = Color.rgb(170, 168, 162);
                specialColor1 = Color.rgb(200, 50, 50);  // 陷阱标记
                specialColor2 = Color.rgb(80, 80, 90);   // 铁门
                break;
            case 4: // 阎罗殿 - 黑金
                wallColor = Color.rgb(20, 20, 30);
                wallBorderColor = Color.rgb(10, 10, 18);
                floorColor = Color.rgb(35, 35, 45);
                floorAccentColor = Color.rgb(45, 42, 55);
                specialColor1 = Color.rgb(255, 200, 50); // 金色装饰
                specialColor2 = Color.rgb(50, 200, 80);  // 幽绿鬼火
                break;
        }
    }

    private void findEntranceAndExit() {
        for (int row = 0; row < Math.min(10, map.length); row++) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col] == HellMazeGenerator.HELL_ENTRANCE) {
                    entranceTileCol = col;
                    entranceTileRow = row;
                    break;
                }
            }
            if (entranceTileCol >= 0) break;
        }
        for (int row = map.length - 1; row > map.length - 10; row--) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col] == HellMazeGenerator.HELL_EXIT) {
                    exitTileCol = col;
                    exitTileRow = row;
                    break;
                }
            }
            if (exitTileCol >= 0) break;
        }
    }

    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        int startChunkX = Math.max(0, (int) (cameraX / chunkPixelSize) - 1);
        int endChunkX = (int) ((cameraX + screenWidth) / chunkPixelSize) + 1;
        int startChunkY = Math.max(0, (int) (cameraY / chunkPixelSize) - 1);
        int endChunkY = (int) ((cameraY + screenHeight) / chunkPixelSize) + 1;

        int maxChunkX = (map[0].length + CHUNK_TILES - 1) / CHUNK_TILES;
        int maxChunkY = (map.length + CHUNK_TILES - 1) / CHUNK_TILES;
        endChunkX = Math.min(endChunkX, maxChunkX - 1);
        endChunkY = Math.min(endChunkY, maxChunkY - 1);

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

        // 动态效果：入口/出口光圈
        drawPortals(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 第1层迷魂雾：屏幕边缘黟雾遵挡，降低可视距离
        if (floor == 1) {
            drawSoulMist(canvas, screenWidth, screenHeight);
        }
    }

    /**
     * 迷魂雾（第1层专属）：以屏幕中心为圆心的径向渐变黑雾，边缘透明度逐渐升高
     */
    private void drawSoulMist(Canvas canvas, int screenWidth, int screenHeight) {
        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;
        float inner = Math.min(screenWidth, screenHeight) * 0.22f;
        float outer = Math.max(screenWidth, screenHeight) * 0.68f;
        android.graphics.RadialGradient gradient = new android.graphics.RadialGradient(
                cx, cy, outer,
                new int[]{Color.argb(0, 20, 15, 25), Color.argb(110, 25, 20, 32), Color.argb(210, 10, 8, 15)},
                new float[]{inner / outer, (inner + outer) / 2f / outer, 1f},
                android.graphics.Shader.TileMode.CLAMP);
        Paint fogPaint = new Paint();
        fogPaint.setShader(gradient);
        fogPaint.setAntiAlias(true);
        canvas.drawRect(0, 0, screenWidth, screenHeight, fogPaint);
    }

    /**
     * 失效包含指定 tile 坐标的 chunk（当地形发生变化时调用）
     */
    public void invalidateChunkAt(int tileX, int tileY) {
        int cx = tileX / CHUNK_TILES;
        int cy = tileY / CHUNK_TILES;
        chunkCache.remove(cx + "_" + cy);
    }

    private Bitmap getChunk(int chunkX, int chunkY) {
        String key = chunkX + "_" + chunkY;
        Bitmap cached = chunkCache.get(key);
        if (cached != null && !cached.isRecycled()) return cached;

        int startCol = chunkX * CHUNK_TILES;
        int startRow = chunkY * CHUNK_TILES;
        int endCol = Math.min(startCol + CHUNK_TILES, map[0].length);
        int endRow = Math.min(startRow + CHUNK_TILES, map.length);
        int w = (endCol - startCol) * tileSize;
        int h = (endRow - startRow) * tileSize;
        if (w <= 0 || h <= 0) return null;

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint();
        p.setAntiAlias(false);

        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                float x = (col - startCol) * tileSize;
                float y = (row - startRow) * tileSize;
                int terrain = map[row][col];
                drawTile(c, p, x, y, col, row, terrain);
            }
        }

        chunkCache.put(key, bitmap);
        return bitmap;
    }

    private void drawTile(Canvas c, Paint p, float x, float y, int col, int row, int terrain) {
        switch (terrain) {
            case HellMazeGenerator.HELL_WALL:
                drawWall(c, p, x, y, col, row);
                break;
            case HellMazeGenerator.HELL_FLOOR:
            case HellMazeGenerator.HELL_ENTRANCE:
            case HellMazeGenerator.HELL_EXIT:
                drawFloor(c, p, x, y, col, row);
                break;
            case HellMazeGenerator.HELL_BLOOD_POOL:
                drawBloodPool(c, p, x, y, col, row);
                break;
            case HellMazeGenerator.HELL_SAFE_STONE:
                drawSafeStone(c, p, x, y, col, row);
                break;
            case HellMazeGenerator.HELL_TRAP:
                drawTrapFloor(c, p, x, y, col, row);
                break;
            case HellMazeGenerator.HELL_BOSS_ARENA:
                drawBossArenaFloor(c, p, x, y, col, row);
                break;
            default:
                drawFloor(c, p, x, y, col, row);
                break;
        }
    }

    private void drawWall(Canvas c, Paint p, float x, float y, int col, int row) {
        p.setColor(wallColor);
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);

        // 砖缝纹理
        p.setColor(wallBorderColor);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        if ((row + col) % 2 == 0) {
            c.drawLine(x, y + tileSize / 2f, x + tileSize, y + tileSize / 2f, p);
            c.drawLine(x + tileSize / 2f, y, x + tileSize / 2f, y + tileSize / 2f, p);
        } else {
            c.drawLine(x, y + tileSize / 2f, x + tileSize, y + tileSize / 2f, p);
            c.drawLine(x + tileSize / 3f, y + tileSize / 2f, x + tileSize / 3f, y + tileSize, p);
        }

        // 第4层：金色装饰线
        if (floor == 4 && (row * 7 + col * 13) % 11 == 0) {
            p.setColor(Color.argb(100, 255, 200, 50));
            p.setStyle(Paint.Style.FILL);
            c.drawRect(x + 2, y + 2, x + tileSize - 2, y + tileSize - 2, p);
        }
        // 第3层：锁链装饰
        if (floor == 3 && (row * 3 + col * 7) % 19 == 0) {
            p.setColor(Color.argb(120, 80, 80, 90));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            c.drawLine(x + tileSize / 2f, y, x + tileSize / 2f, y + tileSize, p);
            c.drawCircle(x + tileSize / 2f, y + tileSize / 3f, 3, p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void drawFloor(Canvas c, Paint p, float x, float y, int col, int row) {
        p.setColor(floorColor);
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);

        // 石板缝隙
        p.setColor(Color.argb(30, 0, 0, 0));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);
        p.setStyle(Paint.Style.FILL);

        // 随机色差
        if ((row * 11 + col * 7) % 13 == 0) {
            p.setColor(floorAccentColor);
            c.drawRect(x + 1, y + 1, x + tileSize - 1, y + tileSize - 1, p);
        }

        // 第1层：黄泉水流纹理
        if (floor == 1 && (row * 5 + col * 3) % 17 == 0) {
            p.setColor(Color.argb(60, 200, 180, 60));
            c.drawOval(x + 2, y + tileSize / 3f, x + tileSize - 2, y + tileSize * 2 / 3f, p);
        }
    }

    private void drawBloodPool(Canvas c, Paint p, float x, float y, int col, int row) {
        // 暗红底色
        p.setColor(Color.rgb(100, 15, 15));
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);
        // 血池表面
        p.setColor(specialColor1);
        c.drawOval(x - 2, y - 2, x + tileSize + 2, y + tileSize + 2, p);
        // 气泡
        if ((row * 13 + col * 7) % 5 == 0) {
            p.setColor(Color.argb(150, 255, 80, 80));
            c.drawCircle(x + tileSize * 0.3f, y + tileSize * 0.4f, 2.5f, p);
            c.drawCircle(x + tileSize * 0.7f, y + tileSize * 0.6f, 1.8f, p);
        }
    }

    private void drawSafeStone(Canvas c, Paint p, float x, float y, int col, int row) {
        p.setColor(specialColor2);
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);
        // 石台边缘高光
        p.setColor(Color.argb(80, 200, 200, 210));
        c.drawRect(x + 1, y + 1, x + tileSize - 1, y + 3, p);
        // 裂纹
        p.setColor(Color.argb(60, 60, 60, 70));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        if ((row + col) % 3 == 0) {
            c.drawLine(x + 3, y + tileSize / 2f, x + tileSize - 3, y + tileSize / 2f + 2, p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void drawTrapFloor(Canvas c, Paint p, float x, float y, int col, int row) {
        // 看起来像普通地板，但有微弱标记
        p.setColor(floorColor);
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);
        // 细微的红色警告纹（玩家需要仔细观察）
        p.setColor(Color.argb(40, 200, 50, 50));
        c.drawRect(x + tileSize / 4f, y + tileSize / 4f, x + tileSize * 3 / 4f, y + tileSize * 3 / 4f, p);
        // 边缘小孔
        p.setColor(Color.argb(80, 40, 40, 40));
        c.drawCircle(x + 3, y + 3, 1.5f, p);
        c.drawCircle(x + tileSize - 3, y + 3, 1.5f, p);
        c.drawCircle(x + 3, y + tileSize - 3, 1.5f, p);
        c.drawCircle(x + tileSize - 3, y + tileSize - 3, 1.5f, p);
    }

    private void drawBossArenaFloor(Canvas c, Paint p, float x, float y, int col, int row) {
        // 黑色大理石
        p.setColor(Color.rgb(30, 28, 38));
        p.setStyle(Paint.Style.FILL);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);
        // 金色网格线
        p.setColor(Color.argb(50, 255, 200, 50));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        c.drawRect(x, y, x + tileSize, y + tileSize, p);
        p.setStyle(Paint.Style.FILL);
        // 随机金色斑点（大理石纹）
        if ((row * 7 + col * 11) % 9 == 0) {
            p.setColor(Color.argb(40, 255, 215, 80));
            c.drawCircle(x + tileSize / 2f, y + tileSize / 2f, 3, p);
        }
    }

    /**
     * 绘制入口/出口动态光圈
     */
    private void drawPortals(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        Paint p = new Paint();
        p.setAntiAlias(true);

        // 入口
        if (entranceTileCol >= 0) {
            float sx = entranceTileCol * tileSize - cameraX;
            float sy = entranceTileRow * tileSize - cameraY;
            if (sx > -tileSize * 4 && sx < screenWidth + tileSize * 4
                    && sy > -tileSize * 4 && sy < screenHeight + tileSize * 4) {
                float cx = sx + tileSize / 2f, cy = sy + tileSize / 2f;
                p.setColor(Color.argb(50, 100, 180, 255));
                canvas.drawCircle(cx, cy, tileSize * 2f, p);
                p.setColor(Color.argb(180, 80, 150, 255));
                canvas.drawCircle(cx, cy, tileSize * 0.7f, p);
                p.setColor(Color.WHITE);
                p.setTextSize(22);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("入口", cx, cy - tileSize * 1.2f, p);
            }
        }

        // 出口（传送门）
        if (exitTileCol >= 0) {
            float sx = exitTileCol * tileSize - cameraX;
            float sy = exitTileRow * tileSize - cameraY;
            if (sx > -tileSize * 4 && sx < screenWidth + tileSize * 4
                    && sy > -tileSize * 4 && sy < screenHeight + tileSize * 4) {
                float cx = sx + tileSize / 2f, cy = sy + tileSize / 2f;
                long t = System.currentTimeMillis();
                float pulse = (float) (1.0 + 0.2 * Math.sin(t / 300.0));
                p.setColor(Color.argb(50, 255, 180, 50));
                canvas.drawCircle(cx, cy, tileSize * 2f * pulse, p);
                p.setColor(Color.argb(200, 255, 200, 50));
                canvas.drawCircle(cx, cy, tileSize * 0.7f, p);
                p.setColor(Color.WHITE);
                p.setTextSize(22);
                p.setTextAlign(Paint.Align.CENTER);
                String label = (floor < 4) ? "下一层" : "出口";
                canvas.drawText(label, cx, cy - tileSize * 1.2f, p);
            }
        }
    }

    public void cleanup() {
        if (chunkCache != null) {
            chunkCache.evictAll();
        }
    }
}

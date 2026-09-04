package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.LruCache;

/**
 * 海底迷宫渲染器 - 东海海底背景 + 珊瑚墙壁
 *
 * 地面采用东海海底地形配色（SEA_FLOOR / PALACE_GROUND 色系），
 * 迷宫墙壁渲染为珊瑚礁墙，叠加水下视觉效果（焦散/气泡/蓝绿滤镜）。
 * 使用 chunk 缓存优化渲染性能。
 */
public class UnderwaterMazeRenderer {

    private int[][] map;
    private int mapWidth;
    private int mapHeight;
    private int tileSize;

    // 入口/出口传送门
    private Paint entrancePaint;
    private Paint exitPaint;
    private Paint entranceGlowPaint;
    private Paint exitGlowPaint;
    private Paint bubblePaint;

    // Chunk 缓存
    private static final int CHUNK_TILES = 16;
    private int chunkPixelSize;
    private LruCache<String, Bitmap> chunkCache;
    private Paint chunkPaint;

    // 入口/出口位置
    private int entranceTileCol = -1, entranceTileRow = -1;
    private int exitTileCol = -1, exitTileRow = -1;

    // 动画计时基准
    private long animBaseTime = 0;

    // 珊瑚颜色表（复用东海海底风格）
    private static final int[][] CORAL_COLORS = {
        {220, 70, 70},   // 红珊瑚
        {255, 130, 50},  // 橙珊瑚
        {180, 70, 190},  // 紫珊瑚
        {255, 190, 70},  // 金珊瑚
        {80, 200, 150},  // 绿珊瑚
        {255, 120, 160}, // 粉珊瑚
    };

    public UnderwaterMazeRenderer(int[][] map, int mapWidth, int mapHeight, int tileSize) {
        this.map = map;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.chunkPixelSize = CHUNK_TILES * tileSize;

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
        findEntranceAndExit();
    }

    private void initPaints() {
        entrancePaint = new Paint();
        entrancePaint.setColor(Color.argb(200, 60, 150, 220));
        entrancePaint.setAntiAlias(true);

        exitPaint = new Paint();
        exitPaint.setColor(Color.argb(220, 220, 200, 80));
        exitPaint.setAntiAlias(true);

        entranceGlowPaint = new Paint();
        entranceGlowPaint.setColor(Color.argb(70, 60, 150, 220));
        entranceGlowPaint.setAntiAlias(true);

        exitGlowPaint = new Paint();
        exitGlowPaint.setColor(Color.argb(70, 220, 200, 80));
        exitGlowPaint.setAntiAlias(true);

        bubblePaint = new Paint();
        bubblePaint.setColor(Color.argb(80, 150, 220, 255));
        bubblePaint.setAntiAlias(true);
    }

    private void findEntranceAndExit() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < Math.min(10, map[0].length); col++) {
                if (map[row][col] == MazeGenerator.MAZE_ENTRANCE) {
                    entranceTileCol = col;
                    entranceTileRow = row;
                    return;
                }
            }
        }
    }

    private void findExit() {
        for (int row = 0; row < map.length; row++) {
            for (int col = map[0].length - 1; col > map[0].length - 10; col--) {
                if (map[row][col] == MazeGenerator.MAZE_EXIT) {
                    exitTileCol = col;
                    exitTileRow = row;
                    return;
                }
            }
        }
    }

    // ==================== 主绘制入口 ====================

    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        long time = System.currentTimeMillis();
        if (animBaseTime == 0) animBaseTime = time;
        float t = (time - animBaseTime) / 1000f;

        // 1. 绘制迷宫 chunk（海底地面 + 珊瑚墙壁）
        drawChunks(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 2. 入口/出口动态传送门
        drawEntranceExit(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 3. 水下氛围（气泡、荧光生物、海藻）
        drawUnderwaterAmbience(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 4. 焦散光斑
        drawCaustics(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 5. 水下滤镜（蓝绿遮罩 + 顶部光柱渐变）
        drawUnderwaterOverlay(canvas, screenWidth, screenHeight);
    }

    private void drawChunks(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
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
    }

    // ==================== Chunk 预渲染 ====================

    private Bitmap getChunk(int chunkX, int chunkY) {
        String key = chunkX + "_" + chunkY;
        Bitmap cached = chunkCache.get(key);
        if (cached != null && !cached.isRecycled()) {
            return cached;
        }

        int startCol = chunkX * CHUNK_TILES;
        int startRow = chunkY * CHUNK_TILES;
        int endCol = Math.min(startCol + CHUNK_TILES, map[0].length);
        int endRow = Math.min(startRow + CHUNK_TILES, map.length);
        int w = (endCol - startCol) * tileSize;
        int h = (endRow - startRow) * tileSize;
        if (w <= 0 || h <= 0) return null;

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas chunkCanvas = new Canvas(bitmap);

        // 先画地板层（所有格子都画海底地面）
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                float x = (col - startCol) * tileSize;
                float y = (row - startRow) * tileSize;
                int terrain = map[row][col];
                if (terrain != MazeGenerator.MAZE_WALL) {
                    drawSeaFloor(chunkCanvas, x, y, col, row);
                } else {
                    // 墙壁底下也画海底地面（防止缝隙）
                    drawSeaFloor(chunkCanvas, x, y, col, row);
                }
            }
        }

        // 再画墙壁层（珊瑚墙覆盖在地面上）
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                float x = (col - startCol) * tileSize;
                float y = (row - startRow) * tileSize;
                if (map[row][col] == MazeGenerator.MAZE_WALL) {
                    drawCoralWall(chunkCanvas, x, y, col, row);
                }
            }
        }

        chunkCache.put(key, bitmap);
        return bitmap;
    }

    // ==================== 海底地面（同心环带地形分布） ====================

    // 地图中心与最大距离（与 DonghaiSeabedMapGenerator 一致）
    private static final float MAP_CENTER_X = 5000f;  // mapWidth / 2
    private static final float MAP_CENTER_Y = 5000f;  // mapHeight / 2
    private static final float MAP_MAX_DIST = (float) Math.sqrt(MAP_CENTER_X * MAP_CENTER_X + MAP_CENTER_Y * MAP_CENTER_Y);

    /**
     * 简易噪声（与 DonghaiSeabedMapGenerator 一致）
     */
    private float getSimpleNoise(int tileX, int tileY) {
        return (float) (Math.sin(tileX * 0.05) * Math.cos(tileY * 0.05) * 0.5
                + Math.sin(tileX * 0.12 + tileY * 0.08) * 0.3
                + Math.cos(tileX * 0.03 - tileY * 0.11) * 0.2);
    }

    /**
     * 获取 tile 对应的海底地形类型（完全复用 DonghaiSeabedMapGenerator 的同心环带分布）
     * 返回: 0=SEA_FLOOR, 1=CORAL_REEF, 2=KELP_FOREST, 3=DEEP_SEA, 4=HYDROTHERMAL
     */
    private int getSeabedZone(int tileCol, int tileRow) {
        float worldX = tileCol * tileSize + tileSize / 2f;
        float worldY = tileRow * tileSize + tileSize / 2f;
        float dx = worldX - MAP_CENTER_X;
        float dy = worldY - MAP_CENTER_Y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float normalizedDist = dist / MAP_MAX_DIST;
        float noise = getSimpleNoise(tileCol, tileRow);

        if (normalizedDist < 0.30f + noise * 0.03f) {
            return 0; // SEA_FLOOR
        } else if (normalizedDist < 0.40f + noise * 0.03f) {
            // 珊瑚礁带中散布海底沙地
            return (noise > 0.3f) ? 0 : 1; // SEA_FLOOR or CORAL_REEF
        } else if (normalizedDist < 0.55f + noise * 0.04f) {
            // 海藻森林中散布珊瑚礁
            return (noise > 0.4f) ? 1 : 2; // CORAL_REEF or KELP_FOREST
        } else if (normalizedDist < 0.75f + noise * 0.04f) {
            // 深海中散布海藻森林
            return (noise > 0.5f) ? 2 : 3; // KELP_FOREST or DEEP_SEA
        } else {
            // 热液区中散布深海
            return (noise > 0.2f && normalizedDist < 0.85f) ? 3 : 4; // DEEP_SEA or HYDROTHERMAL
        }
    }

    /**
     * 绘制海底地面 - 完全参考东海海底地形分布
     */
    private void drawSeaFloor(Canvas canvas, float x, float y, int col, int row) {
        int zone = getSeabedZone(col, row);
        // 用 tile 坐标做确定性随机
        java.util.Random rng = new java.util.Random(row * 500 + col);

        int baseR, baseG, baseB;
        switch (zone) {
            case 0: // SEA_FLOOR: rgb(45, 130, 150)
                baseR = 45; baseG = 130; baseB = 150;
                break;
            case 1: // CORAL_REEF: rgb(25, 100, 150)
                baseR = 25; baseG = 100; baseB = 150;
                break;
            case 2: // KELP_FOREST: rgb(15, 70, 95)
                baseR = 15; baseG = 70; baseB = 95;
                break;
            case 3: // DEEP_SEA: rgb(8, 30, 80)
                baseR = 8; baseG = 30; baseB = 80;
                break;
            default: // HYDROTHERMAL: rgb(55, 25, 25)
                baseR = 55; baseG = 25; baseB = 25;
                break;
        }
        // 微调让相邻 tile 有细微色差
        int noise = (rng.nextInt(11) - 5);
        baseR = Math.max(0, Math.min(255, baseR + noise));
        baseG = Math.max(0, Math.min(255, baseG + noise));
        baseB = Math.max(0, Math.min(255, baseB + noise));

        Paint floorP = new Paint();
        floorP.setColor(Color.rgb(baseR, baseG, baseB));
        floorP.setAntiAlias(false);
        canvas.drawRect(x, y, x + tileSize, y + tileSize, floorP);

        // 各地形专属装饰（参考 MapRenderer 的装饰风格）
        float cx = x + tileSize / 2f;
        float cy = y + tileSize / 2f;
        Paint decoP = new Paint();
        decoP.setAntiAlias(true);

        switch (zone) {
            case 0: // SEA_FLOOR: 沙纹 + 小石子
                if (rng.nextInt(2) == 0) {
                    float ry = y + 4 + rng.nextInt(tileSize - 8);
                    decoP.setColor(Color.argb(25, 80, 160, 180));
                    decoP.setStrokeWidth(1);
                    canvas.drawLine(x + 3, ry, x + tileSize - 3, ry + (rng.nextFloat() - 0.5f) * 2, decoP);
                }
                if (rng.nextInt(3) == 0) {
                    float rx = x + 4 + rng.nextInt(tileSize - 8);
                    float ry2 = y + 4 + rng.nextInt(tileSize - 8);
                    int gray = 80 + rng.nextInt(40);
                    decoP.setColor(Color.rgb(gray, gray + 10, gray + 20));
                    canvas.drawCircle(rx, ry2, 1 + rng.nextFloat(), decoP);
                }
                break;

            case 1: // CORAL_REEF: 珊瑚碎片 + 贝壳
                int corals = 1 + rng.nextInt(2);
                for (int i = 0; i < corals; i++) {
                    float px = x + 3 + rng.nextInt(tileSize - 6);
                    float py = y + 3 + rng.nextInt(tileSize - 6);
                    switch (rng.nextInt(4)) {
                        case 0: decoP.setColor(Color.rgb(220, 80, 80)); break;
                        case 1: decoP.setColor(Color.rgb(255, 140, 60)); break;
                        case 2: decoP.setColor(Color.rgb(180, 80, 200)); break;
                        default: decoP.setColor(Color.rgb(255, 200, 80)); break;
                    }
                    canvas.drawCircle(px, py, 1.2f + rng.nextFloat() * 1.5f, decoP);
                }
                if (rng.nextInt(4) == 0) {
                    float bx = x + 4 + rng.nextInt(tileSize - 8);
                    float by = y + 4 + rng.nextInt(tileSize - 8);
                    decoP.setColor(Color.rgb(230, 210, 180));
                    canvas.drawCircle(bx, by, 1.5f, decoP);
                }
                break;

            case 2: // KELP_FOREST: 海藻叶片 + 暗色阴影
                int blades = 1 + rng.nextInt(2);
                for (int i = 0; i < blades; i++) {
                    float bx = x + 3 + rng.nextInt(tileSize - 6);
                    float by = y + 3 + rng.nextInt(tileSize - 6);
                    decoP.setColor(Color.argb(50, 20, 100, 60));
                    decoP.setStrokeWidth(1.5f);
                    canvas.drawLine(bx, by, bx + (rng.nextFloat() - 0.5f) * 3, by - 3 - rng.nextFloat() * 3, decoP);
                }
                if (rng.nextInt(3) == 0) {
                    decoP.setColor(Color.argb(20, 0, 30, 40));
                    decoP.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(cx, cy, tileSize * 0.3f, decoP);
                    decoP.setStyle(Paint.Style.FILL);
                }
                break;

            case 3: // DEEP_SEA: 生物荧光 + 暗流纹
                if (rng.nextInt(3) == 0) {
                    float gx = x + 3 + rng.nextInt(tileSize - 6);
                    float gy = y + 3 + rng.nextInt(tileSize - 6);
                    switch (rng.nextInt(3)) {
                        case 0: decoP.setColor(Color.argb(35, 80, 150, 255)); break;
                        case 1: decoP.setColor(Color.argb(30, 100, 255, 200)); break;
                        default: decoP.setColor(Color.argb(25, 200, 100, 255)); break;
                    }
                    canvas.drawCircle(gx, gy, 1 + rng.nextFloat(), decoP);
                }
                if (rng.nextInt(4) == 0) {
                    float ddx = x + 2 + rng.nextInt(tileSize - 4);
                    float ddy = y + 4 + rng.nextInt(tileSize - 8);
                    decoP.setColor(Color.argb(20, 40, 80, 150));
                    decoP.setStrokeWidth(1);
                    canvas.drawLine(ddx, ddy, ddx + 5 + rng.nextFloat() * 4, ddy + (rng.nextFloat() - 0.5f) * 2, decoP);
                }
                break;

            default: // HYDROTHERMAL: 暗红发光 + 烟雾
                if (rng.nextInt(2) == 0) {
                    float hx = x + 3 + rng.nextInt(tileSize - 6);
                    float hy = y + 3 + rng.nextInt(tileSize - 6);
                    decoP.setColor(Color.argb(60, 200, 60, 20));
                    canvas.drawCircle(hx, hy, 1.5f + rng.nextFloat(), decoP);
                }
                decoP.setColor(Color.argb(15, 100, 80, 60));
                canvas.drawCircle(cx, cy, tileSize * 0.35f, decoP);
                break;
        }
    }

    // ==================== 珊瑚墙壁 ====================

    /**
     * 绘制珊瑚墙 - 海底珊瑚礁石墙
     *
     * 基色采用 CORAL_REEF 深色，表面附着各色珊瑚装饰
     */
    private void drawCoralWall(Canvas canvas, float x, float y, int col, int row) {
        // 墙壁基色 - 深色珊瑚礁岩 (接近 CORAL_REEF 暗色)
        Paint basePaint = new Paint();
        basePaint.setColor(Color.rgb(30, 65, 85));
        basePaint.setAntiAlias(false);
        canvas.drawRect(x, y, x + tileSize, y + tileSize, basePaint);

        // 礁石纹理 - 暗缝
        Paint gapP = new Paint();
        gapP.setColor(Color.argb(60, 10, 30, 45));
        gapP.setAntiAlias(false);
        if ((row + col) % 2 == 0) {
            canvas.drawLine(x, y + tileSize * 0.5f, x + tileSize, y + tileSize * 0.5f, gapP);
        } else {
            canvas.drawLine(x + tileSize * 0.5f, y, x + tileSize * 0.5f, y + tileSize, gapP);
        }

        // 珊瑚装饰（根据位置变化）
        int variant = (row * 7 + col * 13) % 9;
        int[] cc = CORAL_COLORS[(row * 3 + col * 5) % CORAL_COLORS.length];
        Paint coralP = new Paint();
        coralP.setAntiAlias(true);

        switch (variant) {
            case 0: // 鹿角珊瑚 - 分枝
                coralP.setColor(Color.argb(160, cc[0], cc[1], cc[2]));
                coralP.setStrokeWidth(2);
                coralP.setStyle(Paint.Style.STROKE);
                canvas.drawLine(x + tileSize * 0.3f, y + tileSize * 0.9f,
                        x + tileSize * 0.4f, y + tileSize * 0.2f, coralP);
                canvas.drawLine(x + tileSize * 0.4f, y + tileSize * 0.4f,
                        x + tileSize * 0.65f, y + tileSize * 0.15f, coralP);
                canvas.drawLine(x + tileSize * 0.4f, y + tileSize * 0.55f,
                        x + tileSize * 0.2f, y + tileSize * 0.3f, coralP);
                break;
            case 1: // 脑珊瑚 - 圆团
                coralP.setColor(Color.argb(150, cc[0], cc[1], cc[2]));
                canvas.drawCircle(x + tileSize * 0.5f, y + tileSize * 0.5f, tileSize * 0.3f, coralP);
                coralP.setColor(Color.argb(40, 0, 0, 0));
                coralP.setStyle(Paint.Style.STROKE);
                coralP.setStrokeWidth(0.8f);
                canvas.drawArc(x + tileSize * 0.25f, y + tileSize * 0.35f,
                        x + tileSize * 0.75f, y + tileSize * 0.55f, 30, 120, false, coralP);
                break;
            case 2: // 管状珊瑚 - 多根
                for (int i = 0; i < 3; i++) {
                    float tx = x + tileSize * (0.25f + i * 0.25f);
                    float th = tileSize * (0.3f + ((i + row) % 2) * 0.2f);
                    coralP.setColor(Color.argb(140, cc[0], cc[1], cc[2]));
                    coralP.setStyle(Paint.Style.FILL);
                    canvas.drawRect(tx - 2, y + tileSize - th, tx + 2, y + tileSize, coralP);
                    coralP.setColor(Color.argb(120, Math.max(0, cc[0] - 30), Math.max(0, cc[1] - 30), Math.max(0, cc[2] - 30)));
                    canvas.drawCircle(tx, y + tileSize - th, 3, coralP);
                }
                break;
            case 3: // 扇形珊瑚
                coralP.setColor(Color.argb(140, cc[0], cc[1], cc[2]));
                android.graphics.Path fan = new android.graphics.Path();
                float fcx = x + tileSize * 0.5f;
                float fcy = y + tileSize * 0.8f;
                fan.moveTo(fcx, fcy);
                fan.quadTo(fcx - tileSize * 0.35f, fcy - tileSize * 0.5f, fcx, fcy - tileSize * 0.65f);
                fan.quadTo(fcx + tileSize * 0.35f, fcy - tileSize * 0.5f, fcx, fcy);
                fan.close();
                canvas.drawPath(fan, coralP);
                break;
            case 4: // 蘑菇珊瑚
                coralP.setColor(Color.argb(130, Math.min(255, cc[0] + 20), Math.min(255, cc[1] + 20), Math.min(255, cc[2] + 20)));
                canvas.drawRect(x + tileSize * 0.45f, y + tileSize * 0.5f, x + tileSize * 0.55f, y + tileSize * 0.9f, coralP);
                coralP.setColor(Color.argb(150, cc[0], cc[1], cc[2]));
                canvas.drawOval(x + tileSize * 0.2f, y + tileSize * 0.25f, x + tileSize * 0.8f, y + tileSize * 0.55f, coralP);
                break;
            case 5: // 海藻丛
                coralP.setColor(Color.argb(120, 25, 100, 45));
                coralP.setStrokeWidth(2);
                coralP.setStyle(Paint.Style.STROKE);
                for (int i = 0; i < 3; i++) {
                    float sx = x + tileSize * (0.3f + i * 0.2f);
                    canvas.drawLine(sx, y + tileSize * 0.9f, sx + (i - 1) * 3, y + tileSize * 0.2f, coralP);
                }
                break;
            case 6: // 海葵 - 放射触手
                coralP.setColor(Color.argb(130, cc[0], cc[1], cc[2]));
                coralP.setStrokeWidth(1.5f);
                coralP.setStyle(Paint.Style.STROKE);
                float acx = x + tileSize * 0.5f;
                float acy = y + tileSize * 0.5f;
                for (int a = 0; a < 6; a++) {
                    float angle = (float) (a * Math.PI * 2 / 6);
                    float ex = acx + (float) Math.cos(angle) * tileSize * 0.35f;
                    float ey = acy + (float) Math.sin(angle) * tileSize * 0.35f;
                    canvas.drawLine(acx, acy, ex, ey, coralP);
                }
                // 中心
                coralP.setStyle(Paint.Style.FILL);
                canvas.drawCircle(acx, acy, tileSize * 0.1f, coralP);
                break;
            case 7: // 密集小珊瑚点
                for (int i = 0; i < 4; i++) {
                    int pci = (row + col + i) % CORAL_COLORS.length;
                    coralP.setColor(Color.argb(120, CORAL_COLORS[pci][0], CORAL_COLORS[pci][1], CORAL_COLORS[pci][2]));
                    float px = x + tileSize * (0.2f + (i % 2) * 0.4f + ((i / 2) * 0.15f));
                    float py = y + tileSize * (0.25f + (i / 2) * 0.35f);
                    canvas.drawCircle(px, py, tileSize * 0.08f, coralP);
                }
                break;
            default: // 海绵 + 缝隙
                coralP.setColor(Color.argb(100, 50, 80, 100));
                canvas.drawCircle(x + tileSize * 0.5f, y + tileSize * 0.5f, tileSize * 0.25f, coralP);
                break;
        }

        // 墙壁顶部边缘高亮（给墙一个立体感）
        Paint edgeP = new Paint();
        edgeP.setColor(Color.argb(35, 100, 180, 200));
        edgeP.setAntiAlias(false);
        // 检查上方是否是地板（如果是，顶部画亮边）
        if (row > 0 && map[row - 1][col] != MazeGenerator.MAZE_WALL) {
            canvas.drawRect(x, y, x + tileSize, y + 2, edgeP);
        }
        // 检查左方是否是地板
        if (col > 0 && map[row][col - 1] != MazeGenerator.MAZE_WALL) {
            canvas.drawRect(x, y, x + 2, y + tileSize, edgeP);
        }
    }

    // ==================== 入口/出口传送门 ====================

    private void drawEntranceExit(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        long now = System.currentTimeMillis();

        if (entranceTileCol >= 0) {
            float sx = entranceTileCol * tileSize - cameraX;
            float sy = entranceTileRow * tileSize - cameraY;
            if (sx > -tileSize * 3 && sx < screenWidth + tileSize * 3
                    && sy > -tileSize * 3 && sy < screenHeight + tileSize * 3) {
                float cx = sx + tileSize / 2f;
                float cy = sy + tileSize / 2f;
                float pulse = (float) Math.sin(now / 400.0) * 0.3f + 1.0f;
                canvas.drawCircle(cx, cy, tileSize * 1.8f * pulse, entranceGlowPaint);
                canvas.drawCircle(cx, cy, tileSize * 0.8f, entrancePaint);
                for (int i = 0; i < 5; i++) {
                    float bx = cx + (float) Math.sin(now / 300.0 + i * 1.3) * tileSize * 0.6f;
                    float by = cy - (float) ((now / 10.0 + i * 40) % (tileSize * 3)) + tileSize;
                    canvas.drawCircle(bx, by, 2 + i * 0.8f, bubblePaint);
                }
                Paint textPaint = new Paint();
                textPaint.setColor(Color.argb(220, 80, 180, 255));
                textPaint.setTextSize(24);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setAntiAlias(true);
                canvas.drawText("入口", cx, cy - tileSize * 1.5f, textPaint);
            }
        }

        if (exitTileCol >= 0) {
            float sx = exitTileCol * tileSize - cameraX;
            float sy = exitTileRow * tileSize - cameraY;
            if (sx > -tileSize * 3 && sx < screenWidth + tileSize * 3
                    && sy > -tileSize * 3 && sy < screenHeight + tileSize * 3) {
                float cx = sx + tileSize / 2f;
                float cy = sy + tileSize / 2f;
                float pulse = (float) Math.sin(now / 350.0) * 0.3f + 1.0f;
                canvas.drawCircle(cx, cy, tileSize * 1.8f * pulse, exitGlowPaint);
                canvas.drawCircle(cx, cy, tileSize * 0.8f, exitPaint);
                for (int i = 0; i < 4; i++) {
                    float angle = (float) (now / 500.0 + i * Math.PI / 2);
                    float ox = cx + (float) Math.cos(angle) * tileSize;
                    float oy = cy + (float) Math.sin(angle) * tileSize;
                    Paint dotPaint = new Paint();
                    dotPaint.setColor(Color.argb(150, 255, 230, 100));
                    dotPaint.setAntiAlias(true);
                    canvas.drawCircle(ox, oy, 3, dotPaint);
                }
                Paint textPaint = new Paint();
                textPaint.setColor(Color.argb(220, 255, 220, 80));
                textPaint.setTextSize(24);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setAntiAlias(true);
                canvas.drawText("出口", cx, cy - tileSize * 1.5f, textPaint);
            }
        }
    }

    // ==================== 水下氛围 ====================

    private void drawUnderwaterAmbience(Canvas canvas, float cameraX, float cameraY,
                                         int screenWidth, int screenHeight, float t) {
        // 上升气泡
        for (int i = 0; i < 20; i++) {
            float seed = i * 1337.5f;
            float bx = ((float) ((seed * 7.3) % screenWidth) + (System.currentTimeMillis() / (20 + i * 3)) % screenWidth) % screenWidth;
            float by = (float) (screenHeight - ((System.currentTimeMillis() / (8.0 + i * 2) + seed * 3) % (screenHeight + 200)));
            float br = 2.0f + (i % 5) * 1.0f;
            int alpha = 40 + (i % 4) * 15;

            Paint bp = new Paint();
            bp.setColor(Color.argb(alpha, 150, 220, 255));
            bp.setAntiAlias(true);
            canvas.drawCircle(bx, by, br, bp);
            if (br > 3) {
                Paint hp = new Paint();
                hp.setColor(Color.argb(Math.min(255, alpha + 30), 230, 250, 255));
                hp.setAntiAlias(true);
                canvas.drawCircle(bx - br * 0.25f, by - br * 0.3f, br * 0.3f, hp);
            }
        }

        // 荧光生物
        for (int i = 0; i < 10; i++) {
            float seed = i * 2741.3f;
            float worldX = (float) ((seed * 13.7) % mapWidth);
            float worldY = (float) ((seed * 17.3) % mapHeight);
            float sx = worldX - cameraX;
            float sy = worldY - cameraY;
            if (sx > -50 && sx < screenWidth + 50 && sy > -50 && sy < screenHeight + 50) {
                float glow = (float) Math.sin(t * 1.5 + i) * 0.5f + 0.5f;
                int colorIdx = i % 4;
                Paint gp = new Paint();
                if (colorIdx == 0) gp.setColor(Color.argb((int) (50 * glow), 60, 180, 255));
                else if (colorIdx == 1) gp.setColor(Color.argb((int) (50 * glow), 80, 255, 160));
                else if (colorIdx == 2) gp.setColor(Color.argb((int) (45 * glow), 220, 100, 255));
                else gp.setColor(Color.argb((int) (40 * glow), 255, 180, 80));
                gp.setAntiAlias(true);
                float gr = 8 + glow * 8;
                canvas.drawCircle(sx, sy, gr, gp);
                Paint coreP = new Paint();
                coreP.setColor(Color.argb((int) (70 * glow), 200, 240, 255));
                coreP.setAntiAlias(true);
                canvas.drawCircle(sx, sy, gr * 0.3f, coreP);
            }
        }

        // 飘动海藻（在通道中）
        for (int i = 0; i < 8; i++) {
            float seed = i * 4219.7f;
            float worldX = (float) ((seed * 11.3) % mapWidth);
            float worldY = (float) ((seed * 19.1) % mapHeight);
            float sx = worldX - cameraX;
            float sy = worldY - cameraY;
            if (sx > -30 && sx < screenWidth + 30 && sy > -50 && sy < screenHeight + 30) {
                float sway = (float) Math.sin(t * 0.8 + i * 1.5) * 8;
                Paint swP = new Paint();
                swP.setColor(Color.argb(80, 25, 110, 50));
                swP.setAntiAlias(true);
                swP.setStrokeWidth(2.5f);
                swP.setStyle(Paint.Style.STROKE);
                canvas.drawLine(sx, sy + 10, sx + sway, sy - 15, swP);
                canvas.drawLine(sx + sway, sy - 15, sx + sway * 0.5f, sy - 30, swP);
            }
        }
    }

    // ==================== 焦散光斑 ====================

    private void drawCaustics(Canvas canvas, float cameraX, float cameraY, int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        for (int i = 0; i < 15; i++) {
            float phase1 = t * 0.3f + i * 2.1f;
            float phase2 = t * 0.2f + i * 1.7f;
            float worldX = (float) (Math.sin(phase1) * 3000 + Math.cos(phase2) * 2000);
            float worldY = (float) (Math.cos(phase1) * 3000 + Math.sin(phase2) * 2000);
            float sx = worldX - (cameraX % 2000) + 1000;
            float sy = worldY - (cameraY % 2000) + 1000;
            if (sx < -200 || sx > sw + 200 || sy < -200 || sy > sh + 200) continue;

            float size = 50 + (float) Math.sin(t * 0.5 + i) * 25;
            int alpha = 12 + (int) (Math.sin(t * 0.8 + i * 1.3) * 8);
            paint.setColor(Color.argb(alpha, 180, 230, 255));
            canvas.drawOval(sx - size, sy - size * 0.6f, sx + size, sy + size * 0.6f, paint);
        }
    }

    // ==================== 水下滤镜 ====================

    private void drawUnderwaterOverlay(Canvas canvas, int sw, int sh) {
        Paint paint = new Paint();
        // 全局蓝绿色遮罩（模拟水下色偏，与东海海底一致）
        paint.setColor(Color.argb(35, 10, 60, 100));
        canvas.drawRect(0, 0, sw, sh, paint);

        // 从上方打下的渐变光柱（模拟水面透光）
        float lightIntensity = 0.12f;
        paint.setShader(new LinearGradient(0, 0, 0, sh,
                Color.argb((int) (lightIntensity * 255), 80, 180, 220),
                Color.argb(0, 20, 60, 100),
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, sw, sh, paint);
        paint.setShader(null);
    }

    // ==================== 公共方法 ====================

    public void cleanup() {
        if (chunkCache != null) {
            chunkCache.evictAll();
        }
    }

    public void findExitAndEntrance() {
        findEntranceAndExit();
        findExit();
    }
}

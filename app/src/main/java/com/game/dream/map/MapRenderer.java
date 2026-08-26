package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles all map rendering operations with chunk-based caching for performance
 */
public class MapRenderer {
    private int[][] map;
    private int mapWidth;
    private int mapHeight;
    private int tileSize;

    // Chunk-based rendering
    private static final int CHUNK_SIZE = 500;
    private Map<String, Bitmap> chunkCache = new HashMap<>();
    private Set<String> activeChunks = new HashSet<>();
    private Paint chunkPaint;
    private int maxCachedChunks = 60;

    // Async loading
    private ExecutorService chunkLoaderExecutor;
    private Map<String, Bitmap> pendingChunks = new HashMap<>();

    // Independent map decorations (trees drawn as objects, not tile-level)
    private List<float[]> decoTrees = new ArrayList<>();  // {worldX, worldY, size, style}
    private boolean decorationsGenerated = false;
    private Paint decoPaint;

    // Exclusion zones where decorations should not be placed (e.g., village areas)
    private List<Rect> decorationExclusions = new ArrayList<>();

    // Terrain types
    private static final int PLAIN = 0;
    private static final int GRASSLAND = 1;
    private static final int FOREST = 2;
    private static final int LAKE = 3;
    private static final int SNOW = 4;
    private static final int SWAMP = 5;
    private static final int LAVA = 6;
    private static final int RIVER = 7;
    private static final int MOUNTAIN = 8;
    private static final int BRIDGE = 9;
    private static final int FARMLAND = 10;
    private static final int CITY_ROAD = 11;
    private static final int CITY_WALL = 12;
    private static final int SAND = 13;
    private static final int SEA = 14;

    public MapRenderer(int[][] map, int mapWidth, int mapHeight, int tileSize) {
        this.map = map;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;

        // Initialize rendering resources
        chunkPaint = new Paint();
        chunkPaint.setFilterBitmap(false);
        chunkPaint.setDither(false);

        // Initialize thread pool for async loading (2 threads)
        chunkLoaderExecutor = Executors.newFixedThreadPool(2);

        // Decoration paint
        decoPaint = new Paint();
        decoPaint.setAntiAlias(true);
    }

    /**
     * Add an exclusion zone where decorations should not be placed
     */
    public void addDecorationExclusion(Rect bounds) {
        decorationExclusions.add(bounds);
    }

    /**
     * Get chunk key based on position
     */
    private String getChunkKey(int chunkX, int chunkY) {
        return chunkX + "," + chunkY;
    }

    /**
     * Render a single chunk to bitmap - Enhanced with decorations and texture
     */
    private Bitmap renderChunk(int chunkX, int chunkY) {
        int pixelX = chunkX * CHUNK_SIZE;
        int pixelY = chunkY * CHUNK_SIZE;

        // Calculate tile range - include extra tiles to cover edges
        int startTileX = pixelX / tileSize;
        int endTileX = Math.min(((pixelX + CHUNK_SIZE) / tileSize) + 1, map[0].length);
        int startTileY = pixelY / tileSize;
        int endTileY = Math.min(((pixelY + CHUNK_SIZE) / tileSize) + 1, map.length);

        // Create bitmap
        Bitmap chunkBitmap = Bitmap.createBitmap(CHUNK_SIZE, CHUNK_SIZE, Bitmap.Config.RGB_565);
        Canvas chunkCanvas = new Canvas(chunkBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setDither(false);

        // Pass 1: Draw base terrain with color variation
        for (int y = startTileY; y < endTileY; y++) {
            for (int x = startTileX; x < endTileX; x++) {
                int terrain = map[y][x];

                // Calculate tile position in world space
                int tileWorldX = x * tileSize;
                int tileWorldY = y * tileSize;

                // Calculate position relative to chunk
                int screenX = tileWorldX - pixelX;
                int screenY = tileWorldY - pixelY;

                // Calculate the intersection of this tile with the chunk
                int drawX = Math.max(0, screenX);
                int drawY = Math.max(0, screenY);
                int drawXEnd = Math.min(CHUNK_SIZE, screenX + tileSize);
                int drawYEnd = Math.min(CHUNK_SIZE, screenY + tileSize);

                if (drawX >= drawXEnd || drawY >= drawYEnd) continue;

                // Base terrain color with variation
                drawTerrainTile(chunkCanvas, paint, terrain, x, y, drawX, drawY, drawXEnd, drawYEnd);

                // Draw terrain edge transitions
                drawTerrainEdges(chunkCanvas, paint, x, y, drawX, drawY, drawXEnd, drawYEnd, screenX, screenY);
            }
        }

        // Pass 2: Draw decorations on top
        Paint decoPaint = new Paint();
        decoPaint.setAntiAlias(true);
        decoPaint.setFilterBitmap(false);
        for (int y = startTileY; y < endTileY; y++) {
            for (int x = startTileX; x < endTileX; x++) {
                int terrain = map[y][x];
                int tileWorldX = x * tileSize;
                int tileWorldY = y * tileSize;
                int screenX = tileWorldX - pixelX;
                int screenY = tileWorldY - pixelY;

                drawTerrainDecorations(chunkCanvas, decoPaint, terrain, x, y, screenX, screenY);
            }
        }

        return chunkBitmap;
    }

    /**
     * Draw a single terrain tile with color variation for texture
     */
    private void drawTerrainTile(Canvas canvas, Paint paint, int terrain, int tileX, int tileY,
                                 int drawX, int drawY, int drawXEnd, int drawYEnd) {
        // Base color
        int baseR, baseG, baseB;
        switch (terrain) {
            case PLAIN:     baseR = 210; baseG = 180; baseB = 140; break;
            case GRASSLAND: baseR = 34;  baseG = 139; baseB = 34;  break;
            case FOREST:    baseR = 0;   baseG = 100; baseB = 0;   break;
            case LAKE:      baseR = 30;  baseG = 144; baseB = 255; break;
            case SNOW:      baseR = 245; baseG = 245; baseB = 250; break;
            case SWAMP:     baseR = 85;  baseG = 107; baseB = 47;  break;
            case LAVA:      baseR = 255; baseG = 69;  baseB = 0;   break;
            case RIVER:     baseR = 50;  baseG = 120; baseB = 220; break;
            case MOUNTAIN:  baseR = 110; baseG = 100; baseB = 90;  break;
            case BRIDGE:    baseR = 160; baseG = 140; baseB = 100; break;
            case FARMLAND:  baseR = 180; baseG = 200; baseB = 100; break;
            case CITY_ROAD: baseR = 190; baseG = 180; baseB = 160; break;
            case CITY_WALL: baseR = 100; baseG = 95;  baseB = 90;  break;
            case SAND:      baseR = 235; baseG = 215; baseB = 160; break;
            case SEA:       baseR = 20;  baseG = 80;  baseB = 170; break;
            default:        baseR = 128; baseG = 128; baseB = 128; break;
        }

        // Draw the base tile
        paint.setColor(Color.rgb(baseR, baseG, baseB));
        canvas.drawRect(drawX, drawY, drawXEnd, drawYEnd, paint);

        // Add subtle color variation within the tile (deterministic based on position)
        int hash = hashTile(tileX, tileY);
        int variation = ((hash % 20) - 10); // -10 to +10

        int vr = clamp(baseR + variation, 0, 255);
        int vg = clamp(baseG + variation, 0, 255);
        int vb = clamp(baseB + variation, 0, 255);

        // Draw a slightly varied sub-region for texture
        int subW = drawXEnd - drawX;
        int subH = drawYEnd - drawY;
        if (subW > 4 && subH > 4) {
            paint.setColor(Color.rgb(vr, vg, vb));
            // Draw 2-3 sub-regions with different shades
            int offset1 = (hash % 5) + 2;
            canvas.drawRect(drawX, drawY, drawX + subW / 2, drawY + subH / 2 + offset1, paint);

            int variation2 = ((hash >> 4) % 16) - 8;
            vr = clamp(baseR + variation2, 0, 255);
            vg = clamp(baseG + variation2, 0, 255);
            vb = clamp(baseB + variation2, 0, 255);
            paint.setColor(Color.rgb(vr, vg, vb));
            canvas.drawRect(drawX + subW / 2 - offset1, drawY + subH / 2, drawXEnd, drawYEnd, paint);
        }
    }

    /**
     * Draw terrain edge transitions for smoother borders
     */
    private void drawTerrainEdges(Canvas canvas, Paint paint, int tileX, int tileY,
                                  int drawX, int drawY, int drawXEnd, int drawYEnd,
                                  int screenX, int screenY) {
        int mapH = map.length;
        int mapW = map[0].length;
        int currentTerrain = map[tileY][tileX];
        int edgeWidth = 3; // pixels for edge blending

        // Check each neighbor and draw edge blend
        // Right neighbor
        if (tileX + 1 < mapW) {
            int rightTerrain = map[tileY][tileX + 1];
            if (rightTerrain != currentTerrain) {
                int edgeX = screenX + tileSize;
                int blendX = Math.min(edgeX, drawXEnd);
                int startX = Math.max(drawX, edgeX - edgeWidth);
                if (startX < blendX) {
                    paint.setColor(getBlendedColor(currentTerrain, rightTerrain, 0.5f));
                    paint.setAlpha(80);
                    canvas.drawRect(startX, drawY, blendX, drawYEnd, paint);
                    paint.setAlpha(255);
                }
            }
        }
        // Bottom neighbor
        if (tileY + 1 < mapH) {
            int bottomTerrain = map[tileY + 1][tileX];
            if (bottomTerrain != currentTerrain) {
                int edgeY = screenY + tileSize;
                int blendY = Math.min(edgeY, drawYEnd);
                int startY = Math.max(drawY, edgeY - edgeWidth);
                if (startY < blendY) {
                    paint.setColor(getBlendedColor(currentTerrain, bottomTerrain, 0.5f));
                    paint.setAlpha(80);
                    canvas.drawRect(drawX, startY, drawXEnd, blendY, paint);
                    paint.setAlpha(255);
                }
            }
        }
    }

    /**
     * Draw terrain-specific decorations
     */
    private void drawTerrainDecorations(Canvas canvas, Paint paint, int terrain, int tileX, int tileY,
                                        int screenX, int screenY) {
        // Use deterministic random based on tile position
        int seed = hashTile(tileX, tileY);
        java.util.Random rng = new java.util.Random(seed);

        float cx = screenX + tileSize / 2f;
        float cy = screenY + tileSize / 2f;

        switch (terrain) {
            case GRASSLAND:
                drawGrasslandDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case FOREST:
                drawForestDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case PLAIN:
                drawPlainDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case LAKE:
                drawLakeDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case SWAMP:
                drawSwampDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case SNOW:
                drawSnowDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case LAVA:
                drawLavaDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case RIVER:
                drawRiverDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case MOUNTAIN:
                drawMountainDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case FARMLAND:
                drawFarmlandDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case CITY_ROAD:
                drawCityRoadDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case SAND:
                drawSandDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
            case SEA:
                drawSeaDeco(canvas, paint, rng, screenX, screenY, cx, cy);
                break;
        }
    }

    /**
     * Grassland: 密集草丛、多花朵、小灌木
     */
    private void drawGrasslandDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                   int sx, int sy, float cx, float cy) {
        // 少量草丛（2~3 簇）
        int tufts = 2 + rng.nextInt(2);
        for (int i = 0; i < tufts; i++) {
            float gx = sx + 3 + rng.nextInt(tileSize - 6);
            float gy = sy + 3 + rng.nextInt(tileSize - 6);
            int shade = 15 + rng.nextInt(35);
            paint.setColor(Color.rgb(15 + shade, 110 + shade, 15 + shade));
            paint.setStrokeWidth(1);
            float h = 3 + rng.nextFloat() * 2.5f;
            canvas.drawLine(gx, gy, gx - 1.5f, gy - h, paint);
            canvas.drawLine(gx, gy, gx, gy - h - 0.5f, paint);
            canvas.drawLine(gx, gy, gx + 1.5f, gy - h, paint);
        }

        // 偶尔一朵花（25% 概率）
        if (rng.nextFloat() < 0.25f) {
            float fx = sx + 4 + rng.nextInt(tileSize - 8);
            float fy = sy + 4 + rng.nextInt(tileSize - 8);
            switch (rng.nextInt(3)) {
                case 0: paint.setColor(Color.rgb(255, 255, 90)); break;
                case 1: paint.setColor(Color.rgb(255, 150, 200)); break;
                case 2: paint.setColor(Color.rgb(255, 255, 250)); break;
            }
            canvas.drawCircle(fx, fy, 1.5f, paint);
        }

        // 偶尔一个小灌木（12% 概率）
        if (rng.nextFloat() < 0.12f) {
            float bx = sx + 4 + rng.nextInt(tileSize - 8);
            float by = sy + 4 + rng.nextInt(tileSize - 8);
            float br = 2 + rng.nextFloat() * 1.5f;
            paint.setColor(Color.rgb(35, 100 + rng.nextInt(25), 30));
            canvas.drawCircle(bx, by, br, paint);
        }
    }

    /**
     * Forest: 密集大树冠、灌木丛、落叶、蘑菇
     */
    private void drawForestDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                int sx, int sy, float cx, float cy) {
        // 大树冠（3~5 个，更大更密）
        int trees = 3 + rng.nextInt(3);
        for (int i = 0; i < trees; i++) {
            float tx = sx + 3 + rng.nextInt(tileSize - 6);
            float ty = sy + 3 + rng.nextInt(tileSize - 6);
            float radius = 5 + rng.nextFloat() * 5;

            // 阴影
            paint.setColor(Color.argb(70, 0, 30, 0));
            canvas.drawCircle(tx, ty + 1.5f, radius + 1.5f, paint);

            // 树冠（深绿，带色差）
            int g = 50 + rng.nextInt(50);
            paint.setColor(Color.rgb(0, g, 0));
            canvas.drawCircle(tx, ty, radius, paint);

            // 高光
            paint.setColor(Color.argb(50, 70, 170, 70));
            canvas.drawCircle(tx - 1.5f, ty - 1.5f, radius * 0.45f, paint);
        }

        // 灌木丛（2~3 个小绿团）
        int bushes = 2 + rng.nextInt(2);
        for (int i = 0; i < bushes; i++) {
            float bx = sx + 2 + rng.nextInt(tileSize - 4);
            float by = sy + 2 + rng.nextInt(tileSize - 4);
            float br = 2 + rng.nextFloat() * 2.5f;
            paint.setColor(Color.rgb(25, 80 + rng.nextInt(35), 20));
            canvas.drawCircle(bx, by, br, paint);
            paint.setColor(Color.argb(35, 60, 150, 50));
            canvas.drawCircle(bx - 0.5f, by - 0.5f, br * 0.4f, paint);
        }

        // 落叶（3~5 个彩色小点）
        int leaves = 3 + rng.nextInt(3);
        for (int i = 0; i < leaves; i++) {
            float lx = sx + 2 + rng.nextInt(tileSize - 4);
            float ly = sy + 2 + rng.nextInt(tileSize - 4);
            int leafType = rng.nextInt(4);
            switch (leafType) {
                case 0: paint.setColor(Color.argb(100, 160, 130, 40)); break; // 黄叶
                case 1: paint.setColor(Color.argb(100, 140, 80, 20)); break;  // 棕叶
                case 2: paint.setColor(Color.argb(90, 50, 110, 30)); break;   // 绿叶
                case 3: paint.setColor(Color.argb(80, 180, 100, 30)); break;  // 橙叶
            }
            canvas.drawCircle(lx, ly, 1 + rng.nextFloat(), paint);
        }

        // 蘑菇（25% 概率，1~2 个）
        if (rng.nextFloat() < 0.25f) {
            int mushrooms = 1 + rng.nextInt(2);
            for (int i = 0; i < mushrooms; i++) {
                float mx = sx + 3 + rng.nextInt(tileSize - 6);
                float my = sy + 3 + rng.nextInt(tileSize - 6);
                // 菌柄
                paint.setColor(Color.rgb(220, 210, 190));
                canvas.drawCircle(mx, my + 1, 0.8f, paint);
                // 菌盖
                paint.setColor(Color.rgb(180 + rng.nextInt(60), 40 + rng.nextInt(30), 30));
                canvas.drawCircle(mx, my, 1.5f, paint);
            }
        }
    }

    /**
     * Plain: 草丛、野花、石头（树木由独立装饰系统绘制）
     */
    private void drawPlainDeco(Canvas canvas, Paint paint, java.util.Random rng,
                               int sx, int sy, float cx, float cy) {
        // 草丛（1~2 簇）
        int tufts = 1 + rng.nextInt(2);
        for (int i = 0; i < tufts; i++) {
            float gx = sx + 2 + rng.nextInt(tileSize - 4);
            float gy = sy + 2 + rng.nextInt(tileSize - 4);
            int shade = 15 + rng.nextInt(30);
            paint.setColor(Color.rgb(100 + shade, 140 + shade, 60 + shade));
            paint.setStrokeWidth(1.5f);
            float h = 3 + rng.nextFloat() * 3;
            canvas.drawLine(gx, gy, gx - 1.5f, gy - h, paint);
            canvas.drawLine(gx, gy, gx + 1.5f, gy - h, paint);
        }

        // 野花（18% 概率，放大到 2~3px）
        if (rng.nextFloat() < 0.18f) {
            float fx = sx + 3 + rng.nextInt(tileSize - 6);
            float fy = sy + 3 + rng.nextInt(tileSize - 6);
            switch (rng.nextInt(3)) {
                case 0: paint.setColor(Color.rgb(255, 255, 100)); break;
                case 1: paint.setColor(Color.rgb(255, 150, 200)); break;
                case 2: paint.setColor(Color.rgb(255, 255, 240)); break;
            }
            canvas.drawCircle(fx, fy, 2f + rng.nextFloat(), paint);
        }

        // 小石头（15% 概率，2~3px）
        if (rng.nextFloat() < 0.15f) {
            float rx = sx + 3 + rng.nextInt(tileSize - 6);
            float ry = sy + 3 + rng.nextInt(tileSize - 6);
            int rockSize = 2 + rng.nextInt(2);
            int gray = 135 + rng.nextInt(55);
            paint.setColor(Color.rgb(gray, gray - 5, gray - 15));
            canvas.drawCircle(rx, ry, rockSize, paint);
        }
    }

    /**
     * Lake: 芦苇、睡莲、涟漪、水波
     */
    private void drawLakeDeco(Canvas canvas, Paint paint, java.util.Random rng,
                              int sx, int sy, float cx, float cy) {
        // 水波线（2~4 条）
        int waves = 2 + rng.nextInt(3);
        for (int i = 0; i < waves; i++) {
            float wy = sy + 3 + rng.nextInt(tileSize - 6);
            float wx = sx + 2 + rng.nextInt(tileSize - 4);
            float waveLen = 4 + rng.nextFloat() * 7;
            paint.setColor(Color.argb(55, 90, 170, 255));
            paint.setStrokeWidth(1);
            canvas.drawLine(wx, wy, wx + waveLen, wy + (rng.nextFloat() - 0.5f) * 2.5f, paint);
        }

        // 涟漪（40% 概率，同心圆）
        if (rng.nextFloat() < 0.4f) {
            float rx = sx + 4 + rng.nextInt(tileSize - 8);
            float ry = sy + 4 + rng.nextInt(tileSize - 8);
            float rr = 2 + rng.nextFloat() * 2;
            paint.setColor(Color.argb(35, 150, 210, 255));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.8f);
            canvas.drawCircle(rx, ry, rr, paint);
            canvas.drawCircle(rx, ry, rr * 1.6f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }

        // 睡莲（35% 概率，1~2 片）
        if (rng.nextFloat() < 0.35f) {
            int lilies = 1 + rng.nextInt(2);
            for (int i = 0; i < lilies; i++) {
                float lx = sx + 3 + rng.nextInt(tileSize - 6);
                float ly = sy + 3 + rng.nextInt(tileSize - 6);
                float lr = 2 + rng.nextFloat() * 1.5f;
                // 莲叶
                paint.setColor(Color.rgb(30, 120 + rng.nextInt(30), 40));
                canvas.drawCircle(lx, ly, lr, paint);
                // 莲花（50% 概率）
                if (rng.nextFloat() < 0.5f) {
                    paint.setColor(Color.rgb(255, 180 + rng.nextInt(50), 200));
                    canvas.drawCircle(lx, ly - 0.5f, lr * 0.5f, paint);
                }
            }
        }

        // 岸边芦苇（50% 概率，2~4 根）
        if (rng.nextFloat() < 0.5f) {
            int reeds = 2 + rng.nextInt(3);
            for (int i = 0; i < reeds; i++) {
                float rx = sx + 2 + rng.nextInt(tileSize - 4);
                float ry = sy + 2 + rng.nextInt(tileSize - 4);
                float rh = 4 + rng.nextFloat() * 3;
                paint.setColor(Color.rgb(55 + rng.nextInt(25), 90 + rng.nextInt(30), 30));
                paint.setStrokeWidth(1);
                canvas.drawLine(rx, ry, rx + (rng.nextFloat() - 0.5f) * 1.5f, ry - rh, paint);
                // 芦苇穗
                paint.setColor(Color.argb(80, 160, 140, 90));
                canvas.drawCircle(rx + (rng.nextFloat() - 0.5f), ry - rh, 1, paint);
            }
        }

        // 光斑闪烁
        if (rng.nextFloat() < 0.3f) {
            float shimX = sx + 3 + rng.nextInt(tileSize - 6);
            float shimY = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(45, 210, 240, 255));
            canvas.drawCircle(shimX, shimY, 1.5f + rng.nextFloat(), paint);
        }
    }

    /**
     * Swamp: dark patches, reed marks, bubbles
     */
    private void drawSwampDeco(Canvas canvas, Paint paint, java.util.Random rng,
                               int sx, int sy, float cx, float cy) {
        // Dark murky patches
        if (rng.nextInt(2) == 0) {
            float px = sx + 3 + rng.nextInt(tileSize - 6);
            float py = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(40, 30, 50, 20));
            canvas.drawCircle(px, py, 2 + rng.nextFloat() * 2, paint);
        }

        // Reed marks (tall grass)
        int reeds = 1 + rng.nextInt(2);
        for (int i = 0; i < reeds; i++) {
            float rx = sx + 3 + rng.nextInt(tileSize - 6);
            float ry = sy + 2 + rng.nextInt(tileSize - 4);
            paint.setColor(Color.rgb(60, 80 + rng.nextInt(30), 30));
            paint.setStrokeWidth(1);
            canvas.drawLine(rx, ry, rx + (rng.nextFloat() - 0.5f) * 2, ry - 4 - rng.nextFloat() * 3, paint);
        }

        // Occasional bubble
        if (rng.nextInt(5) == 0) {
            float bx = sx + 4 + rng.nextInt(tileSize - 8);
            float by = sy + 4 + rng.nextInt(tileSize - 8);
            paint.setColor(Color.argb(50, 120, 150, 80));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(bx, by, 1.5f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    /**
     * Snow: ice crystals, blue shadows, sparkle
     */
    private void drawSnowDeco(Canvas canvas, Paint paint, java.util.Random rng,
                              int sx, int sy, float cx, float cy) {
        // Blue shadow patches
        if (rng.nextInt(2) == 0) {
            float px = sx + 3 + rng.nextInt(tileSize - 6);
            float py = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(25, 150, 180, 220));
            canvas.drawCircle(px, py, 2 + rng.nextFloat() * 3, paint);
        }

        // Ice crystal (small star shape)
        if (rng.nextInt(3) == 0) {
            float ix = sx + 4 + rng.nextInt(tileSize - 8);
            float iy = sy + 4 + rng.nextInt(tileSize - 8);
            paint.setColor(Color.argb(100, 200, 220, 255));
            paint.setStrokeWidth(1);
            // Tiny cross
            canvas.drawLine(ix - 2, iy, ix + 2, iy, paint);
            canvas.drawLine(ix, iy - 2, ix, iy + 2, paint);
            // Diagonals
            canvas.drawLine(ix - 1.2f, iy - 1.2f, ix + 1.2f, iy + 1.2f, paint);
            canvas.drawLine(ix - 1.2f, iy + 1.2f, ix + 1.2f, iy - 1.2f, paint);
        }

        // Sparkle dots
        if (rng.nextInt(4) == 0) {
            float spx = sx + 2 + rng.nextInt(tileSize - 4);
            float spy = sy + 2 + rng.nextInt(tileSize - 4);
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawCircle(spx, spy, 0.8f, paint);
        }
    }

    /**
     * Lava: bright spots, dark cracks
     */
    private void drawLavaDeco(Canvas canvas, Paint paint, java.util.Random rng,
                              int sx, int sy, float cx, float cy) {
        // Bright yellow-orange spots (hot areas)
        int hotSpots = 1 + rng.nextInt(2);
        for (int i = 0; i < hotSpots; i++) {
            float hx = sx + 3 + rng.nextInt(tileSize - 6);
            float hy = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(80, 255, 200 + rng.nextInt(55), 0));
            canvas.drawCircle(hx, hy, 1.5f + rng.nextFloat() * 2, paint);
        }

        // Dark cracks
        int cracks = 1 + rng.nextInt(2);
        for (int i = 0; i < cracks; i++) {
            float cx1 = sx + 2 + rng.nextInt(tileSize - 4);
            float cy1 = sy + 2 + rng.nextInt(tileSize - 4);
            float cx2 = cx1 + (rng.nextFloat() - 0.5f) * 8;
            float cy2 = cy1 + (rng.nextFloat() - 0.5f) * 8;
            paint.setColor(Color.argb(100, 80, 20, 0));
            paint.setStrokeWidth(1);
            canvas.drawLine(cx1, cy1, cx2, cy2, paint);
        }

        // Glow
        paint.setColor(Color.argb(30, 255, 100, 0));
        canvas.drawCircle(cx, cy, tileSize * 0.4f, paint);
    }

    /**
     * River: wave lines, flow direction marks
     */
    private void drawRiverDeco(Canvas canvas, Paint paint, java.util.Random rng,
                               int sx, int sy, float cx, float cy) {
        // Flow lines
        int lines = 1 + rng.nextInt(2);
        for (int i = 0; i < lines; i++) {
            float wy = sy + 4 + rng.nextInt(tileSize - 8);
            float wx = sx + 2 + rng.nextInt(tileSize - 4);
            float waveLen = 5 + rng.nextFloat() * 8;
            paint.setColor(Color.argb(70, 100, 180, 255));
            paint.setStrokeWidth(1);
            canvas.drawLine(wx, wy, wx + waveLen, wy + (rng.nextFloat() - 0.5f) * 2, paint);
        }
        // Shimmer
        if (rng.nextInt(3) == 0) {
            paint.setColor(Color.argb(50, 200, 230, 255));
            canvas.drawCircle(cx + rng.nextFloat() * 4 - 2, cy + rng.nextFloat() * 4 - 2, 1.5f, paint);
        }
    }

    /**
     * Mountain: rock cracks, snow patches
     */
    private void drawMountainDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                  int sx, int sy, float cx, float cy) {
        // Rock cracks
        int cracks = 1 + rng.nextInt(3);
        for (int i = 0; i < cracks; i++) {
            float x1 = sx + 2 + rng.nextInt(tileSize - 4);
            float y1 = sy + 2 + rng.nextInt(tileSize - 4);
            float x2 = x1 + (rng.nextFloat() - 0.5f) * 10;
            float y2 = y1 + (rng.nextFloat() - 0.5f) * 10;
            paint.setColor(Color.argb(80, 70, 65, 60));
            paint.setStrokeWidth(1);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
        // Snow patches on peaks
        if (rng.nextInt(3) == 0) {
            paint.setColor(Color.argb(40, 230, 230, 240));
            canvas.drawCircle(sx + 4 + rng.nextInt(tileSize - 8), sy + 3 + rng.nextInt(tileSize / 2), 2, paint);
        }
        // Rock highlights
        if (rng.nextInt(2) == 0) {
            paint.setColor(Color.argb(30, 160, 150, 140));
            canvas.drawCircle(cx, cy, 3, paint);
        }
    }

    /**
     * Farmland: crop rows, scarecrow marks
     */
    private void drawFarmlandDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                  int sx, int sy, float cx, float cy) {
        // Crop rows (horizontal lines)
        int rows = 2 + rng.nextInt(2);
        for (int i = 0; i < rows; i++) {
            float ry = sy + 3 + (tileSize - 6) * (i + 1) / (rows + 1);
            paint.setColor(Color.argb(60, 120, 160, 50));
            paint.setStrokeWidth(1);
            canvas.drawLine(sx + 2, ry, sx + tileSize - 2, ry, paint);
        }
        // Small crop dots
        int dots = 2 + rng.nextInt(3);
        for (int i = 0; i < dots; i++) {
            float dx = sx + 3 + rng.nextInt(tileSize - 6);
            float dy = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(80, 180, 200, 60));
            canvas.drawCircle(dx, dy, 1, paint);
        }
    }

    /**
     * CityRoad: stone tile pattern
     */
    private void drawCityRoadDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                  int sx, int sy, float cx, float cy) {
        // Stone tile grid lines
        paint.setColor(Color.argb(30, 140, 130, 120));
        paint.setStrokeWidth(1);
        // Horizontal line
        canvas.drawLine(sx, sy + tileSize / 2, sx + tileSize, sy + tileSize / 2, paint);
        // Vertical line
        canvas.drawLine(sx + tileSize / 2, sy, sx + tileSize / 2, sy + tileSize, paint);
        // Occasional wear mark
        if (rng.nextInt(4) == 0) {
            paint.setColor(Color.argb(20, 160, 150, 140));
            canvas.drawCircle(cx + rng.nextFloat() * 4 - 2, cy + rng.nextFloat() * 4 - 2, 2, paint);
        }
    }

    /**
     * Sand: small shells, ripple marks
     */
    private void drawSandDeco(Canvas canvas, Paint paint, java.util.Random rng,
                              int sx, int sy, float cx, float cy) {
        // Sand ripple lines
        if (rng.nextInt(2) == 0) {
            float ry = sy + 4 + rng.nextInt(tileSize - 8);
            paint.setColor(Color.argb(30, 200, 180, 130));
            paint.setStrokeWidth(1);
            canvas.drawLine(sx + 3, ry, sx + tileSize - 3, ry + (rng.nextFloat() - 0.5f) * 2, paint);
        }
        // Small shell or pebble
        if (rng.nextInt(4) == 0) {
            float px = sx + 4 + rng.nextInt(tileSize - 8);
            float py = sy + 4 + rng.nextInt(tileSize - 8);
            paint.setColor(Color.rgb(220, 200, 170));
            canvas.drawCircle(px, py, 1.2f, paint);
        }
    }

    /**
     * Sea: wave lines, foam spots
     */
    private void drawSeaDeco(Canvas canvas, Paint paint, java.util.Random rng,
                             int sx, int sy, float cx, float cy) {
        // Wave lines
        int waves = 1 + rng.nextInt(2);
        for (int i = 0; i < waves; i++) {
            float wy = sy + 3 + rng.nextInt(tileSize - 6);
            float wx = sx + 2 + rng.nextInt(tileSize - 4);
            float waveLen = 5 + rng.nextFloat() * 8;
            paint.setColor(Color.argb(50, 80, 160, 255));
            paint.setStrokeWidth(1);
            canvas.drawLine(wx, wy, wx + waveLen, wy + (rng.nextFloat() - 0.5f) * 3, paint);
        }
        // Foam spots
        if (rng.nextInt(3) == 0) {
            float fx = sx + 3 + rng.nextInt(tileSize - 6);
            float fy = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(40, 200, 230, 255));
            canvas.drawCircle(fx, fy, 1.5f, paint);
        }
    }

    /**
     * Deterministic hash for tile position
     */
    private int hashTile(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    /**
     * Clamp value between min and max
     */
    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Generate independent decoration objects (trees) on the map.
     * Trees are placed as standalone objects, similar to village trees.
     */
    private void generateDecorations() {
        decoTrees.clear();
        java.util.Random decoRand = new java.util.Random(12345);
        // 根据地图面积动态计算树数量（约每 720,000 平方像素一棵树）
        float mapArea = (float) mapWidth * (float) mapHeight;
        int treeCount = Math.max(100, (int) (mapArea / 720000f));

        for (int i = 0; i < treeCount; i++) {
            int tileX = decoRand.nextInt(mapWidth / tileSize);
            int tileY = decoRand.nextInt(mapHeight / tileSize);
            int terrain = map[tileY][tileX];

            // Only place on walkable terrain
            if (terrain == PLAIN || terrain == GRASSLAND || terrain == FOREST) {
                float worldX = tileX * tileSize + decoRand.nextFloat() * tileSize;
                float worldY = tileY * tileSize + decoRand.nextFloat() * tileSize;

                // Skip if inside an exclusion zone (village area)
                boolean excluded = false;
                for (Rect ex : decorationExclusions) {
                    if (worldX >= ex.left && worldX <= ex.right
                            && worldY >= ex.top && worldY <= ex.bottom) {
                        excluded = true;
                        break;
                    }
                }
                if (excluded) continue;

                float size = 150 + decoRand.nextFloat() * 100;  // 150~250px，约为角色的 2~3 倍
                int style = decoRand.nextInt(5);  // 0=阔叶树, 1=松树, 2=灌木, 3=金秋树, 4=红枫树
                decoTrees.add(new float[]{worldX, worldY, size, style});
            }
        }
        decorationsGenerated = true;
    }

    /**
     * Draw all independent decoration trees with frustum culling
     */
    private void drawDecorationTrees(Canvas canvas, float cameraX, float cameraY,
                                     int screenWidth, int screenHeight) {
        if (!decorationsGenerated) {
            generateDecorations();
        }

        float margin = 200; // max tree extent (trees are 2~3x character size)
        float visLeft = cameraX - margin;
        float visRight = cameraX + screenWidth + margin;
        float visTop = cameraY - margin;
        float visBottom = cameraY + screenHeight + margin;

        for (int i = 0; i < decoTrees.size(); i++) {
            float[] tree = decoTrees.get(i);
            float wx = tree[0], wy = tree[1], size = tree[2];
            int style = (int) tree[3];

            // Frustum culling
            if (wx < visLeft || wx > visRight || wy < visTop || wy > visBottom) continue;

            float sx = wx - cameraX;
            float sy = wy - cameraY;

            switch (style) {
                case 0: drawDecoBroadleafTree(canvas, sx, sy, size); break;
                case 1: drawDecoPineTree(canvas, sx, sy, size); break;
                case 2: drawDecoBush(canvas, sx, sy, size); break;
                case 3: drawDecoAutumnGoldTree(canvas, sx, sy, size); break;
                case 4: drawDecoAutumnRedTree(canvas, sx, sy, size); break;
            }
        }
    }

    /**
     * 阔叶树：多圆叠加蓬松树冠（村庄风格）
     */
    private void drawDecoBroadleafTree(Canvas canvas, float cx, float cy, float size) {
        float canopyR = size * 0.42f;
        float trunkW = size * 0.06f;
        float trunkH = size * 0.55f;
        // 先画树冠
        // 阴影
        decoPaint.setColor(Color.argb(40, 0, 30, 0));
        canvas.drawCircle(cx + 2, cy + 2, canopyR + 3, decoPaint);
        // 左下圆
        decoPaint.setColor(Color.rgb(25, 105, 25));
        canvas.drawCircle(cx - canopyR * 0.4f, cy + canopyR * 0.15f, canopyR * 0.75f, decoPaint);
        // 右下圆
        canvas.drawCircle(cx + canopyR * 0.4f, cy + canopyR * 0.1f, canopyR * 0.7f, decoPaint);
        // 顶部圆
        decoPaint.setColor(Color.rgb(38, 125, 35));
        canvas.drawCircle(cx, cy - canopyR * 0.5f, canopyR * 0.8f, decoPaint);
        // 高光
        decoPaint.setColor(Color.argb(50, 100, 210, 70));
        canvas.drawCircle(cx - canopyR * 0.2f, cy - canopyR * 0.7f, canopyR * 0.35f, decoPaint);
        // 再画树干（从树冠底部伸出）
        decoPaint.setColor(Color.rgb(90, 60, 30));
        canvas.drawRect(cx - trunkW * 1.3f, cy + canopyR * 0.5f,
                        cx + trunkW * 1.3f, cy + canopyR * 0.5f + trunkH, decoPaint);
    }

    /**
     * 松树：三层三角树冠
     */
    private void drawDecoPineTree(Canvas canvas, float cx, float cy, float size) {
        float treeH = size * 1.4f;
        float trunkW = size * 0.05f;
        float trunkH = size * 0.4f;
        // 先画阴影
        decoPaint.setColor(Color.argb(30, 0, 25, 0));
        canvas.drawCircle(cx + 1, cy + 1, size * 0.45f, decoPaint);
        // 再画三层三角树冠
        for (int i = 0; i < 3; i++) {
            float layerY = cy + size * 0.35f - i * treeH * 0.3f;
            float layerW = size * (0.45f - i * 0.1f);
            float layerH = treeH * 0.38f;
            decoPaint.setColor(i == 0 ? Color.rgb(20, 88, 20) : Color.rgb(32, 112, 30));
            android.graphics.Path tri = new android.graphics.Path();
            tri.moveTo(cx - layerW, layerY + layerH);
            tri.lineTo(cx, layerY);
            tri.lineTo(cx + layerW, layerY + layerH);
            tri.close();
            canvas.drawPath(tri, decoPaint);
        }
        // 最后画树干（从最底层三角下方伸出）
        decoPaint.setColor(Color.rgb(80, 55, 28));
        canvas.drawRect(cx - trunkW, cy + size * 0.55f,
                        cx + trunkW, cy + size * 0.55f + trunkH, decoPaint);
    }

    /**
     * 灌木丛：扁圆蓬松
     */
    private void drawDecoBush(Canvas canvas, float cx, float cy, float size) {
        float bushR = size * 0.35f;
        float trunkW = size * 0.07f;
        float trunkH = size * 0.3f;
        // 先画树冠
        // 阴影
        decoPaint.setColor(Color.argb(30, 0, 20, 0));
        canvas.drawCircle(cx + 1.5f, cy + 1.5f, bushR + 2, decoPaint);
        // 左圆
        decoPaint.setColor(Color.rgb(38, 115, 32));
        canvas.drawCircle(cx - bushR * 0.5f, cy + bushR * 0.2f, bushR * 0.7f, decoPaint);
        // 右圆
        canvas.drawCircle(cx + bushR * 0.5f, cy + bushR * 0.1f, bushR * 0.65f, decoPaint);
        // 顶圆
        decoPaint.setColor(Color.rgb(50, 135, 42));
        canvas.drawCircle(cx, cy - bushR * 0.25f, bushR * 0.75f, decoPaint);
        // 高光
        decoPaint.setColor(Color.argb(40, 130, 215, 80));
        canvas.drawCircle(cx - bushR * 0.15f, cy - bushR * 0.5f, bushR * 0.3f, decoPaint);
        // 再画短粗树干（从树冠底部伸出）
        decoPaint.setColor(Color.rgb(95, 70, 38));
        canvas.drawRect(cx - trunkW * 1.5f, cy + bushR * 0.5f,
                        cx + trunkW * 1.5f, cy + bushR * 0.5f + trunkH, decoPaint);
    }

    /**
     * 金秋树：金黄色树冠（与绿色明显区分）
     */
    private void drawDecoAutumnGoldTree(Canvas canvas, float cx, float cy, float size) {
        float canopyR = size * 0.42f;
        float trunkW = size * 0.06f;
        float trunkH = size * 0.55f;
        // 先画树冠
        decoPaint.setColor(Color.argb(40, 30, 20, 0));
        canvas.drawCircle(cx + 2, cy + 2, canopyR + 3, decoPaint);
        decoPaint.setColor(Color.rgb(200, 170, 30));
        canvas.drawCircle(cx - canopyR * 0.4f, cy + canopyR * 0.15f, canopyR * 0.75f, decoPaint);
        canvas.drawCircle(cx + canopyR * 0.4f, cy + canopyR * 0.1f, canopyR * 0.7f, decoPaint);
        decoPaint.setColor(Color.rgb(230, 195, 40));
        canvas.drawCircle(cx, cy - canopyR * 0.5f, canopyR * 0.8f, decoPaint);
        decoPaint.setColor(Color.argb(55, 255, 240, 100));
        canvas.drawCircle(cx - canopyR * 0.2f, cy - canopyR * 0.7f, canopyR * 0.35f, decoPaint);
        // 再画树干
        decoPaint.setColor(Color.rgb(90, 60, 30));
        canvas.drawRect(cx - trunkW * 1.3f, cy + canopyR * 0.5f,
                        cx + trunkW * 1.3f, cy + canopyR * 0.5f + trunkH, decoPaint);
    }

    /**
     * 红枫树：橙红色树冠（与绿色明显区分）
     */
    private void drawDecoAutumnRedTree(Canvas canvas, float cx, float cy, float size) {
        float canopyR = size * 0.42f;
        float trunkW = size * 0.06f;
        float trunkH = size * 0.55f;
        // 先画树冠
        decoPaint.setColor(Color.argb(40, 30, 10, 0));
        canvas.drawCircle(cx + 2, cy + 2, canopyR + 3, decoPaint);
        decoPaint.setColor(Color.rgb(190, 70, 25));
        canvas.drawCircle(cx - canopyR * 0.4f, cy + canopyR * 0.15f, canopyR * 0.75f, decoPaint);
        canvas.drawCircle(cx + canopyR * 0.4f, cy + canopyR * 0.1f, canopyR * 0.7f, decoPaint);
        decoPaint.setColor(Color.rgb(220, 90, 35));
        canvas.drawCircle(cx, cy - canopyR * 0.5f, canopyR * 0.8f, decoPaint);
        decoPaint.setColor(Color.argb(50, 255, 160, 80));
        canvas.drawCircle(cx - canopyR * 0.2f, cy - canopyR * 0.7f, canopyR * 0.35f, decoPaint);
        // 再画树干
        decoPaint.setColor(Color.rgb(85, 55, 28));
        canvas.drawRect(cx - trunkW * 1.3f, cy + canopyR * 0.5f,
                        cx + trunkW * 1.3f, cy + canopyR * 0.5f + trunkH, decoPaint);
    }

    /**
     * Get blended color between two terrain types
     */
    private int getBlendedColor(int terrain1, int terrain2, float ratio) {
        int[] c1 = getTerrainRGB(terrain1);
        int[] c2 = getTerrainRGB(terrain2);
        int r = (int)(c1[0] * (1 - ratio) + c2[0] * ratio);
        int g = (int)(c1[1] * (1 - ratio) + c2[1] * ratio);
        int b = (int)(c1[2] * (1 - ratio) + c2[2] * ratio);
        return Color.rgb(r, g, b);
    }

    /**
     * Get RGB values for a terrain type
     */
    private int[] getTerrainRGB(int terrain) {
        switch (terrain) {
            case PLAIN:     return new int[]{210, 180, 140};
            case GRASSLAND: return new int[]{34, 139, 34};
            case FOREST:    return new int[]{0, 100, 0};
            case LAKE:      return new int[]{30, 144, 255};
            case SNOW:      return new int[]{245, 245, 250};
            case SWAMP:     return new int[]{85, 107, 47};
            case LAVA:      return new int[]{255, 69, 0};
            case RIVER:     return new int[]{50, 120, 220};
            case MOUNTAIN:  return new int[]{110, 100, 90};
            case BRIDGE:    return new int[]{160, 140, 100};
            case FARMLAND:  return new int[]{180, 200, 100};
            case CITY_ROAD: return new int[]{190, 180, 160};
            case CITY_WALL: return new int[]{100, 95, 90};
            case SAND:      return new int[]{235, 215, 160};
            case SEA:       return new int[]{20, 80, 170};
            default:        return new int[]{128, 128, 128};
        }
    }

    /**
     * Async render a chunk
     */
    private void asyncRenderChunk(int chunkX, int chunkY) {
        String key = getChunkKey(chunkX, chunkY);

        if (chunkCache.containsKey(key) || pendingChunks.containsKey(key)) {
            return; // Already loaded or loading
        }

        // Mark as pending
        pendingChunks.put(key, null);

        // Submit to thread pool
        chunkLoaderExecutor.submit(() -> {
            Bitmap chunk = renderChunk(chunkX, chunkY);

            // Add to cache
            synchronized (chunkCache) {
                chunkCache.put(key, chunk);
                pendingChunks.remove(key);

                // Evict if needed
                if (chunkCache.size() > maxCachedChunks) {
                    evictOldestChunk(activeChunks);
                }
            }
        });
    }

    /**
     * Remove least recently used chunk from cache
     */
    private void evictOldestChunk(Set<String> activeKeys) {
        for (Map.Entry<String, Bitmap> entry : chunkCache.entrySet()) {
            if (!activeKeys.contains(entry.getKey())) {
                Bitmap bmp = entry.getValue();
                if (bmp != null && !bmp.isRecycled()) {
                    bmp.recycle();
                }
                chunkCache.remove(entry.getKey());
                break;
            }
        }
    }

    /**
     * Update chunk cache based on camera position
     */
    public void updateChunkCache(float cameraX, float cameraY, int screenWidth, int screenHeight) {
        int preloadRadius = 2;
        int startChunkX = (int)(cameraX / CHUNK_SIZE) - preloadRadius;
        int endChunkX = (int)((cameraX + screenWidth) / CHUNK_SIZE) + preloadRadius;
        int startChunkY = (int)(cameraY / CHUNK_SIZE) - preloadRadius;
        int endChunkY = (int)((cameraY + screenHeight) / CHUNK_SIZE) + preloadRadius;

        int maxChunkX = (mapWidth + CHUNK_SIZE - 1) / CHUNK_SIZE;
        int maxChunkY = (mapHeight + CHUNK_SIZE - 1) / CHUNK_SIZE;

        startChunkX = Math.max(0, startChunkX);
        startChunkY = Math.max(0, startChunkY);
        endChunkX = Math.min(maxChunkX, endChunkX);
        endChunkY = Math.min(maxChunkY, endChunkY);

        Set<String> newActiveChunks = new HashSet<>();

        for (int cy = startChunkY; cy < endChunkY; cy++) {
            for (int cx = startChunkX; cx < endChunkX; cx++) {
                String key = getChunkKey(cx, cy);
                newActiveChunks.add(key);

                // Async load if not cached
                if (!chunkCache.containsKey(key)) {
                    asyncRenderChunk(cx, cy);
                }
            }
        }

        activeChunks = newActiveChunks;
    }

    /**
     * Draw the visible portion of the map
     */
    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        // Update chunk cache
        updateChunkCache(cameraX, cameraY, screenWidth, screenHeight);

        // Calculate visible chunk range
        int startChunkX = (int)(cameraX / CHUNK_SIZE) - 1;
        int endChunkX = (int)((cameraX + screenWidth) / CHUNK_SIZE) + 1;
        int startChunkY = (int)(cameraY / CHUNK_SIZE) - 1;
        int endChunkY = (int)((cameraY + screenHeight) / CHUNK_SIZE) + 1;

        int maxChunkX = (mapWidth + CHUNK_SIZE - 1) / CHUNK_SIZE;
        int maxChunkY = (mapHeight + CHUNK_SIZE - 1) / CHUNK_SIZE;

        startChunkX = Math.max(0, startChunkX);
        startChunkY = Math.max(0, startChunkY);
        endChunkX = Math.min(maxChunkX, endChunkX);
        endChunkY = Math.min(maxChunkY, endChunkY);

        // Draw visible chunks
        for (int cy = startChunkY; cy < endChunkY; cy++) {
            for (int cx = startChunkX; cx < endChunkX; cx++) {
                String key = getChunkKey(cx, cy);
                Bitmap chunk = chunkCache.get(key);

                if (chunk != null && !chunk.isRecycled()) {
                    int dstX = cx * CHUNK_SIZE - (int)cameraX;
                    int dstY = cy * CHUNK_SIZE - (int)cameraY;

                    // Handle edge chunks that may be partially off-screen
                    int drawWidth = Math.min(CHUNK_SIZE, screenWidth - dstX);
                    int drawHeight = Math.min(CHUNK_SIZE, screenHeight - dstY);

                    if (drawWidth > 0 && drawHeight > 0) {
                        Rect srcRect = new Rect(0, 0, drawWidth, drawHeight);
                        Rect dstRect = new Rect(dstX, dstY, dstX + drawWidth, dstY + drawHeight);
                        canvas.drawBitmap(chunk, srcRect, dstRect, chunkPaint);
                    }
                }
            }
        }

        // Draw independent decoration trees on top of terrain
        drawDecorationTrees(canvas, cameraX, cameraY, screenWidth, screenHeight);
    }

    /**
     * Get the number of cached chunks (for debugging)
     */
    public int getCachedChunkCount() {
        return chunkCache.size();
    }

    /**
     * Get the number of active chunks (for debugging)
     */
    public int getActiveChunkCount() {
        return activeChunks.size();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        // Shutdown thread pool
        if (chunkLoaderExecutor != null && !chunkLoaderExecutor.isShutdown()) {
            chunkLoaderExecutor.shutdownNow();
        }

        // Clean up chunk cache
        synchronized (chunkCache) {
            for (Bitmap chunk : chunkCache.values()) {
                if (chunk != null && !chunk.isRecycled()) {
                    chunk.recycle();
                }
            }
            chunkCache.clear();
        }

        pendingChunks.clear();
        activeChunks.clear();
    }
}

package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.HashSet;
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
     * Grassland: grass tufts, small flowers
     */
    private void drawGrasslandDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                   int sx, int sy, float cx, float cy) {
        // Grass tufts (small V shapes)
        int tufts = 2 + rng.nextInt(3);
        for (int i = 0; i < tufts; i++) {
            float gx = sx + 3 + rng.nextInt(tileSize - 6);
            float gy = sy + 3 + rng.nextInt(tileSize - 6);
            int shade = 20 + rng.nextInt(40);
            paint.setColor(Color.rgb(20 + shade, 120 + shade, 20 + shade));
            paint.setStrokeWidth(1);
            // Two small blades
            canvas.drawLine(gx, gy, gx - 1.5f, gy - 3, paint);
            canvas.drawLine(gx, gy, gx + 1.5f, gy - 3, paint);
        }

        // Occasional small flower
        if (rng.nextInt(4) == 0) {
            float fx = sx + 4 + rng.nextInt(tileSize - 8);
            float fy = sy + 4 + rng.nextInt(tileSize - 8);
            int flowerType = rng.nextInt(4);
            switch (flowerType) {
                case 0: paint.setColor(Color.rgb(255, 255, 100)); break; // Yellow
                case 1: paint.setColor(Color.rgb(255, 150, 200)); break; // Pink
                case 2: paint.setColor(Color.rgb(200, 150, 255)); break; // Purple
                case 3: paint.setColor(Color.rgb(255, 255, 255)); break; // White
            }
            canvas.drawCircle(fx, fy, 1.5f, paint);
            // Center
            paint.setColor(Color.rgb(255, 200, 0));
            canvas.drawCircle(fx, fy, 0.7f, paint);
        }
    }

    /**
     * Forest: tree canopy shadows, undergrowth
     */
    private void drawForestDeco(Canvas canvas, Paint paint, java.util.Random rng,
                                int sx, int sy, float cx, float cy) {
        // Dark canopy circles (tree tops seen from above)
        int trees = 1 + rng.nextInt(2);
        for (int i = 0; i < trees; i++) {
            float tx = sx + 4 + rng.nextInt(tileSize - 8);
            float ty = sy + 4 + rng.nextInt(tileSize - 8);
            float radius = 3 + rng.nextFloat() * 4;

            // Dark shadow
            paint.setColor(Color.argb(60, 0, 40, 0));
            canvas.drawCircle(tx, ty + 1, radius + 1, paint);

            // Canopy (dark green)
            int g = 60 + rng.nextInt(40);
            paint.setColor(Color.rgb(0, g, 0));
            canvas.drawCircle(tx, ty, radius, paint);

            // Highlight
            paint.setColor(Color.argb(40, 80, 180, 80));
            canvas.drawCircle(tx - 1, ty - 1, radius * 0.5f, paint);
        }

        // Undergrowth dots
        int dots = 2 + rng.nextInt(3);
        for (int i = 0; i < dots; i++) {
            float dx = sx + 2 + rng.nextInt(tileSize - 4);
            float dy = sy + 2 + rng.nextInt(tileSize - 4);
            paint.setColor(Color.argb(80, 30, 80 + rng.nextInt(40), 20));
            canvas.drawCircle(dx, dy, 1, paint);
        }
    }

    /**
     * Plain: small rocks, dried grass
     */
    private void drawPlainDeco(Canvas canvas, Paint paint, java.util.Random rng,
                               int sx, int sy, float cx, float cy) {
        // Small rocks
        if (rng.nextInt(3) == 0) {
            float rx = sx + 4 + rng.nextInt(tileSize - 8);
            float ry = sy + 4 + rng.nextInt(tileSize - 8);
            int rockSize = 1 + rng.nextInt(2);
            int gray = 140 + rng.nextInt(60);
            paint.setColor(Color.rgb(gray, gray - 10, gray - 20));
            canvas.drawCircle(rx, ry, rockSize, paint);
            // Highlight
            paint.setColor(Color.argb(60, 255, 255, 255));
            canvas.drawCircle(rx - 0.5f, ry - 0.5f, rockSize * 0.5f, paint);
        }

        // Dried grass marks
        int marks = 1 + rng.nextInt(3);
        for (int i = 0; i < marks; i++) {
            float gx = sx + 3 + rng.nextInt(tileSize - 6);
            float gy = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(100, 180, 160, 100));
            paint.setStrokeWidth(1);
            canvas.drawLine(gx, gy, gx + rng.nextFloat() * 3 - 1.5f, gy - 2 - rng.nextFloat() * 2, paint);
        }
    }

    /**
     * Lake: wave lines, shimmer spots
     */
    private void drawLakeDeco(Canvas canvas, Paint paint, java.util.Random rng,
                              int sx, int sy, float cx, float cy) {
        // Wave lines
        int waves = 1 + rng.nextInt(2);
        for (int i = 0; i < waves; i++) {
            float wy = sy + 4 + rng.nextInt(tileSize - 8);
            float wx = sx + 2 + rng.nextInt(tileSize - 4);
            float waveLen = 4 + rng.nextFloat() * 6;
            paint.setColor(Color.argb(60, 100, 180, 255));
            paint.setStrokeWidth(1);
            canvas.drawLine(wx, wy, wx + waveLen, wy + (rng.nextFloat() - 0.5f) * 2, paint);
        }

        // Shimmer spots (light reflections)
        if (rng.nextInt(3) == 0) {
            float shimX = sx + 3 + rng.nextInt(tileSize - 6);
            float shimY = sy + 3 + rng.nextInt(tileSize - 6);
            paint.setColor(Color.argb(50, 200, 230, 255));
            canvas.drawCircle(shimX, shimY, 1.5f, paint);
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

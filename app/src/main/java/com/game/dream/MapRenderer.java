package com.game.dream;

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
    private int maxCachedChunks = 20;

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
     * Render a single chunk to bitmap - Final fixed version
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

        // Draw all tiles that intersect with this chunk
        for (int y = startTileY; y < endTileY; y++) {
            for (int x = startTileX; x < endTileX; x++) {
                int terrain = map[y][x];
                setColorForTerrain(paint, terrain);

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

                // Only draw if there's an intersection
                if (drawX < drawXEnd && drawY < drawYEnd) {
                    chunkCanvas.drawRect(drawX, drawY, drawXEnd, drawYEnd, paint);
                }
            }
        }

        return chunkBitmap;
    }

    /**
     * Set color based on terrain type
     */
    private void setColorForTerrain(Paint paint, int terrain) {
        switch (terrain) {
            case PLAIN:
                paint.setColor(Color.rgb(210, 180, 140));
                break;
            case GRASSLAND:
                paint.setColor(Color.rgb(34, 139, 34));
                break;
            case FOREST:
                paint.setColor(Color.rgb(0, 100, 0));
                break;
            case LAKE:
                paint.setColor(Color.rgb(30, 144, 255));
                break;
            case SNOW:
                paint.setColor(Color.rgb(255, 250, 250));
                break;
            case SWAMP:
                paint.setColor(Color.rgb(85, 107, 47));
                break;
            case LAVA:
                paint.setColor(Color.rgb(255, 69, 0));
                break;
            default:
                paint.setColor(Color.GRAY);
                break;
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

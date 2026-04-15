package com.game.dream;

import java.util.Random;

/**
 * Handles procedural terrain generation with multi-octave noise and biome logic
 */
public class MapGenerator {
    private int mapWidth;
    private int mapHeight;
    private int tileSize;
    private Random random;

    // Terrain type constants
    public static final int PLAIN = 0;
    public static final int GRASSLAND = 1;
    public static final int FOREST = 2;
    public static final int LAKE = 3;
    public static final int SNOW = 4;
    public static final int SWAMP = 5;
    public static final int LAVA = 6;

    /**
     * Create a new terrain generator
     */
    public MapGenerator(int mapWidth, int mapHeight, int tileSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.random = new Random(12345); // Fixed seed for reproducible maps
    }

    /**
     * Generate the complete terrain map
     */
    public int[][] generateMap() {
        int mapWidthInTiles = mapWidth / tileSize;
        int mapHeightInTiles = mapHeight / tileSize;

        int[][] map = new int[mapHeightInTiles][mapWidthInTiles];

        // Generate elevation and moisture maps
        double[][] elevationMap = new double[mapHeightInTiles][mapWidthInTiles];
        double[][] moistureMap = new double[mapHeightInTiles][mapWidthInTiles];

        generateElevationMap(elevationMap, mapWidthInTiles, mapHeightInTiles);
        generateMoistureMap(moistureMap, mapWidthInTiles, mapHeightInTiles);

        // Convert to terrain types
        convertToTerrain(map, elevationMap, moistureMap, mapWidthInTiles, mapHeightInTiles);

        // Smooth for natural transitions
        smoothMap(map, 2);

        return map;
    }

    /**
     * Generate elevation map using multi-octave noise
     */
    private void generateElevationMap(double[][] elevationMap, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Multi-octave noise for elevation
                double elevation = 0;
                elevation += sampleNoise(x, y, 0.02) * 1.0;      // Base layer
                elevation += sampleNoise(x, y, 0.05) * 0.5;      // Detail layer
                elevation += sampleNoise(x, y, 0.1) * 0.25;      // Fine detail
                elevation /= 1.75; // Normalize

                // Add large-scale features (mountains, valleys)
                double largeFeature = Math.sin(x * 0.005) * Math.cos(y * 0.005);
                elevation = elevation * 0.7 + largeFeature * 0.3;

                elevationMap[y][x] = elevation;
            }
        }
    }

    /**
     * Generate moisture map
     */
    private void generateMoistureMap(double[][] moistureMap, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double moisture = 0;
                moisture += sampleNoise(x + 1000, y + 1000, 0.03) * 1.0;
                moisture += sampleNoise(x + 1000, y + 1000, 0.08) * 0.5;
                moisture /= 1.5;

                moistureMap[y][x] = moisture;
            }
        }
    }

    /**
     * Sample pseudo-noise at given coordinates and frequency
     */
    private double sampleNoise(int x, int y, double frequency) {
        double nx = x * frequency;
        double ny = y * frequency;

        // Combine multiple sine/cosine waves
        return (Math.sin(nx) * Math.cos(ny) +
                Math.sin(nx * 1.5 + ny * 0.7) * 0.5 +
                Math.cos(nx * 0.8 - ny * 1.2) * 0.3) / 1.8;
    }

    /**
     * Convert elevation and moisture values to terrain types
     */
    private void convertToTerrain(int[][] map, double[][] elevationMap,
                                  double[][] moistureMap, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double elevation = elevationMap[y][x];
                double moisture = moistureMap[y][x];

                map[y][x] = getTerrainType(elevation, moisture);
            }
        }
    }

    /**
     * Determine terrain type based on elevation and moisture
     */
    private int getTerrainType(double elevation, double moisture) {
        // High elevation - mountains/snow
        if (elevation > 0.6) {
            if (elevation > 0.8) {
                return SNOW; // Snow-capped peaks
            } else {
                return FOREST; // Mountain forests
            }
        }
        // Medium-high elevation
        else if (elevation > 0.2) {
            if (moisture > 0.3) {
                return FOREST; // Wet hills = forest
            } else if (moisture < -0.3) {
                return PLAIN; // Dry hills = plains
            } else {
                return GRASSLAND; // Moderate = grassland
            }
        }
        // Low elevation
        else if (elevation > -0.2) {
            if (moisture > 0.5) {
                return SWAMP; // Very wet lowlands = swamp
            } else if (moisture > 0.1) {
                return GRASSLAND; // Wet lowlands = grassland
            } else {
                return PLAIN; // Dry/moderate = plain
            }
        }
        // Very low elevation - water bodies
        else {
            if (elevation < -0.6) {
                return LAVA; // Deep pits = lava (rare)
            } else if (moisture > 0.2) {
                return LAKE; // Wet depressions = lakes
            } else {
                return SWAMP; // Shallow wet areas = swamp
            }
        }
    }

    /**
     * Smooth the map to create natural transitions between terrain types
     */
    private void smoothMap(int[][] map, int iterations) {
        int height = map.length;
        int width = map[0].length;

        for (int iter = 0; iter < iterations; iter++) {
            int[][] newMap = new int[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Count neighboring terrain types
                    int[] terrainCount = new int[7];

                    // Check 3x3 neighborhood
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;

                            int nx = x + dx;
                            int ny = y + dy;

                            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                terrainCount[map[ny][nx]]++;
                            }
                        }
                    }

                    // Find most common neighbor terrain
                    int mostCommonTerrain = map[y][x];
                    int maxCount = 0;

                    for (int i = 0; i < 7; i++) {
                        if (terrainCount[i] > maxCount) {
                            maxCount = terrainCount[i];
                            mostCommonTerrain = i;
                        }
                    }

                    // Only change if there's a strong majority (at least 5 out of 8)
                    if (maxCount >= 5) {
                        newMap[y][x] = mostCommonTerrain;
                    } else {
                        newMap[y][x] = map[y][x];
                    }
                }
            }

            // Copy new map back
            for (int y = 0; y < height; y++) {
                System.arraycopy(newMap[y], 0, map[y], 0, width);
            }
        }
    }

    /**
     * Get terrain name for display
     */
    public static String getTerrainName(int terrainType) {
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
}

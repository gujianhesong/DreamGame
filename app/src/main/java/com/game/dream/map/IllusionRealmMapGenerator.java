package com.game.dream.map;

/**
 * 幻境挑战专用地图：开阔平原，便于四边涌怪
 */
public class IllusionRealmMapGenerator {

    /** @see com.game.dream.system.IllusionRealmSystem#REALM_FANCHEN */
    public static final int THEME_FANCHEN = 0;
    public static final int THEME_YAOHU = 1;
    public static final int THEME_LONGGONG = 2;

    public static int[][] generate(int mapWidth, int mapHeight, int tileSize) {
        return generate(mapWidth, mapHeight, tileSize, THEME_FANCHEN);
    }

    public static int[][] generate(int mapWidth, int mapHeight, int tileSize, int realmTheme) {
        int cols = mapWidth / tileSize;
        int rows = mapHeight / tileSize;
        int[][] map = new int[rows][cols];
        java.util.Random rnd = new java.util.Random(9000 + realmTheme);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map[r][c] = pickTile(realmTheme, rnd);
            }
        }
        return map;
    }

    private static int pickTile(int realmTheme, java.util.Random rnd) {
        if (realmTheme == THEME_YAOHU) {
            return rnd.nextFloat() < 0.42f ? MapGenerator.FOREST : MapGenerator.GRASSLAND;
        }
        if (realmTheme == THEME_LONGGONG) {
            if (rnd.nextFloat() < 0.08f) {
                return MapGenerator.CORAL_REEF;
            }
            if (rnd.nextFloat() < 0.12f) {
                return MapGenerator.KELP_FOREST;
            }
            return MapGenerator.SEA_FLOOR;
        }
        return MapGenerator.GRASSLAND;
    }
}

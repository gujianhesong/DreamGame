package com.game.dream.map;

import android.graphics.Rect;

/**
 * 东海湾地图生成器
 * 地图尺寸: 10000x10000
 * 左侧: 草地(6000宽) + 村庄(3000宽)，右侧: 沙滩(2000) + 海(2000)
 * 中间从上到下有一道围墙，围墙上平均分布 3 道门
 */
public class DonghaiBayMapGenerator {

    public static final int MAP_WIDTH = 10000;
    public static final int MAP_HEIGHT = 10000;

    // 围墙参数（左侧占 6000）
    private static final int WALL_X = 6000;        // 围墙左边缘 x
    private static final int WALL_WIDTH = 100;      // 围墙厚度
    private static final int GATE_HEIGHT = 400;   // 每道门的高度
    // 3 道门平均分布：将围墙 4 等分，门在 1/4、2/4、3/4 处
    private static final int[] GATE_CENTER_YS = {
            MAP_HEIGHT / 4,           // 2500
            MAP_HEIGHT / 2,           // 5000
            MAP_HEIGHT * 3 / 4        // 7500
    };

    // 左侧村庄区域（居中于 6000 宽草地，宽 3000）
    private static final int VILLAGE_X1 = 1500;
    private static final int VILLAGE_Y1 = 3500;
    private static final int VILLAGE_X2 = 4500;
    private static final int VILLAGE_Y2 = 6500;

    private int tileSize;

    public DonghaiBayMapGenerator(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * 生成东海湾地图数据
     */
    public int[][] generateMap() {
        int w = MAP_WIDTH / tileSize;
        int h = MAP_HEIGHT / tileSize;
        int[][] map = new int[h][w];

        // 1. 左侧: 草地（村庄区域基础地形）
        int wallStartCol = WALL_X / tileSize;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < wallStartCol; x++) {
                map[y][x] = MapGenerator.GRASSLAND;
            }
        }

        // 2. 右侧: 沙滩 2000 + 海 2000
        int rightStartCol = (WALL_X + WALL_WIDTH) / tileSize;
        int beachEndCol = rightStartCol + 2000 / tileSize; // 沙滩右边界（列）
        for (int y = 0; y < h; y++) {
            for (int x = rightStartCol; x < w; x++) {
                if (x < beachEndCol) {
                    map[y][x] = MapGenerator.SAND;
                } else {
                    map[y][x] = MapGenerator.SEA;
                }
            }
        }

        // 3. 村庄区域设为可通行
        int vx1 = VILLAGE_X1 / tileSize, vy1 = VILLAGE_Y1 / tileSize;
        int vx2 = VILLAGE_X2 / tileSize, vy2 = VILLAGE_Y2 / tileSize;
        for (int y = vy1; y <= vy2 && y < h; y++) {
            for (int x = vx1; x <= vx2 && x < w; x++) {
                if (y >= 0 && x >= 0) {
                    map[y][x] = MapGenerator.VILLAGE_CAN_PASS;
                }
            }
        }

        // 4. 围墙设为 CITY_WALL（不可通行），跳过 3 道门口区域
        int wallEndCol = (WALL_X + WALL_WIDTH) / tileSize;
        for (int y = 0; y < h; y++) {
            // 检查是否在任何门口区域内
            boolean inGate = false;
            for (int centerY : GATE_CENTER_YS) {
                int gateTop = centerY - GATE_HEIGHT / 2;
                int gateBottom = centerY + GATE_HEIGHT / 2;
                if (y >= gateTop / tileSize && y < gateBottom / tileSize) {
                    inGate = true;
                    break;
                }
            }
            if (inGate) continue;
            for (int x = wallStartCol; x <= wallEndCol && x < w; x++) {
                if (y >= 0 && x >= 0) {
                    map[y][x] = MapGenerator.CITY_WALL;
                }
            }
        }

        return map;
    }

    /**
     * 获取围墙不可通行的矩形区域（用于碰撞检测）
     */
    public Rect getWallBounds() {
        return new Rect(WALL_X, 0, WALL_X + WALL_WIDTH, MAP_HEIGHT);
    }

    /**
     * 获取 3 道门的中心 y 坐标数组
     */
    public static int[] getGateCenterYs() { return GATE_CENTER_YS; }
    public static int getGateHeight() { return GATE_HEIGHT; }

    /**
     * 获取左侧村庄的世界坐标范围
     */
    public static int[] getVillageBounds() {
        return new int[]{VILLAGE_X1, VILLAGE_Y1, VILLAGE_X2, VILLAGE_Y2};
    }
}

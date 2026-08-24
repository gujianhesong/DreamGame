package com.game.dream.map;

import java.util.Random;

/**
 * 金陵大地图地形生成器
 * 地图尺寸: 60000x60000，包含主城、四角村庄、河流、山岭、湖泊、草地、平原
 */
public class JinlingMapGenerator {

    public static final int MAP_WIDTH = 60000;
    public static final int MAP_HEIGHT = 60000;
    private int tileSize;
    private Random random;

    // 主城区域参数
    public static final int CITY_X1 = 25000;
    public static final int CITY_Y1 = 25000;
    public static final int CITY_X2 = 35000;
    public static final int CITY_Y2 = 35000;
    public static final int CITY_CENTER_X = 30000;
    public static final int CITY_CENTER_Y = 30000;

    // 四角村庄区域
    public static final int VILLAGE_SIZE = 2000;

    // 道路宽度（像素）
    private static final int ROAD_WIDTH = 300;

    // 河流宽度参数
    private static final int RIVER_WIDTH = 160;

    public JinlingMapGenerator(int tileSize) {
        this.tileSize = tileSize;
        this.random = new Random(54321);
    }

    /**
     * 生成金陵大地图（算法优化版：哈希噪声替代三角函数，避免线程开销）
     */
    public int[][] generateMap() {
        int w = MAP_WIDTH / tileSize;
        int h = MAP_HEIGHT / tileSize;
        int[][] map = new int[h][w];

        // 1. 逐 tile 计算噪声 + 区域修正 + 地形转换
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                map[y][x] = computeTerrainAt(x, y, w, h);
            }
        }

        // 2. 平滑处理（1 次迭代）
        smoothMap(map);

        // 3. 刻划主城、道路、村庄（河流已移除，避免阻挡城门道路）
        // carveRivers(map, w, h);
        stampCityArea(map, w, h);
        stampRoads(map, w, h);
        stampVillages(map, w, h);

        return map;
    }

    // ==================== 逐 tile 地形计算（内存优化） ====================

    /**
     * 计算单个 tile 的地形类型（合并噪声 + 区域修正 + 地形转换）
     */
    private int computeTerrainAt(int x, int y, int w, int h) {
        // 基础噪声
        double elevation = (sampleNoise(x, y, 0.015) * 1.0
                + sampleNoise(x, y, 0.04) * 0.5
                + sampleNoise(x, y, 0.08) * 0.25) / 1.75;
        double moisture = (sampleNoise(x + 5000, y + 5000, 0.02) * 1.0
                + sampleNoise(x + 5000, y + 5000, 0.06) * 0.5) / 1.5;

        // 区域修正
        float px = (float) x / w;
        float py = (float) y / h;

        if (py < 0.22f) {
            float factor = 1.0f - py / 0.22f;
            elevation += 0.35 * factor * factor;
        }
        float neDist = dist(px, py, 0.82f, 0.18f);
        if (neDist < 0.18f) {
            float factor = 1.0f - neDist / 0.18f;
            elevation -= 0.6 * factor;
            moisture += 0.5 * factor;
        }
        float seDist = dist(px, py, 0.82f, 0.82f);
        if (seDist < 0.2f) {
            float factor = 1.0f - seDist / 0.2f;
            elevation += 0.3 * factor;
        }
        float nwDist = dist(px, py, 0.18f, 0.18f);
        if (nwDist < 0.15f) {
            float factor = 1.0f - nwDist / 0.15f;
            elevation += 0.15 * factor;
            moisture += 0.1 * factor;
        }
        float swDist = dist(px, py, 0.2f, 0.8f);
        if (swDist < 0.25f) {
            float factor = 1.0f - swDist / 0.25f;
            elevation -= 0.1 * factor;
            moisture -= 0.15 * factor;
        }
        float centerDist = dist(px, py, 0.5f, 0.5f);
        if (centerDist < 0.12f) {
            float factor = 1.0f - centerDist / 0.12f;
            elevation *= (1.0 - 0.3 * factor);
        }

        return getTerrainType(elevation, moisture);
    }

    // ==================== 噪声生成（整数哈希，比 sin/cos 快 ~10x）====================

    /**
     * 整数哈希噪声函数，输出范围 [-1, 1]
     * 替代 Math.sin/Math.cos，纯整数运算，无浮点三角函数开销
     */
    private double sampleNoise(int x, int y, double frequency) {
        int ix = (int)(x * frequency);
        int iy = (int)(y * frequency);
        int n = ix * 374761393 + iy * 668265263;
        n = (n ^ (n >> 13)) * 1274126177;
        n = n ^ (n >> 16);
        return (double)(n & 0x7fffffff) / 1073741824.0 - 1.0;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // ==================== 地形转换 ====================

    private int getTerrainType(double elevation, double moisture) {
        if (elevation > 0.65) {
            return MapGenerator.MOUNTAIN; // 裸岩高山
        } else if (elevation > 0.45) {
            if (moisture > 0.2) {
                return MapGenerator.FOREST;
            } else {
                return MapGenerator.GRASSLAND;
            }
        } else if (elevation > 0.1) {
            if (moisture > 0.35) {
                return MapGenerator.FOREST;
            } else if (moisture > 0.0) {
                return MapGenerator.GRASSLAND;
            } else {
                return MapGenerator.PLAIN;
            }
        } else if (elevation > -0.15) {
            if (moisture > 0.3) {
                return MapGenerator.SWAMP;
            } else if (moisture > 0.0) {
                return MapGenerator.PLAIN;
            } else {
                return MapGenerator.PLAIN;
            }
        } else {
            if (moisture > 0.15) {
                return MapGenerator.LAKE;
            } else {
                return MapGenerator.SWAMP;
            }
        }
    }

    // ==================== 平滑 ====================

    /**
     * 顺序平滑处理（1 次迭代，双缓冲）
     */
    private void smoothMap(int[][] map) {
        int h = map.length;
        int w = map[0].length;
        int[][] newMap = new int[h][w];
        int[] counts = new int[13];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                for (int i = 0; i < 13; i++) counts[i] = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            counts[map[ny][nx]]++;
                        }
                    }
                }
                int maxCount = 0, mostCommon = map[y][x];
                for (int i = 0; i < 13; i++) {
                    if (counts[i] > maxCount) {
                        maxCount = counts[i];
                        mostCommon = i;
                    }
                }
                newMap[y][x] = (maxCount >= 5) ? mostCommon : map[y][x];
            }
        }
        for (int y = 0; y < h; y++) {
            System.arraycopy(newMap[y], 0, map[y], 0, w);
        }
    }

    // ==================== 河流刻划 ====================

    private void carveRivers(int[][] map, int w, int h) {
        // 主河流1: 从东北湖泊区域流向南方，沿主城东侧
        // 路径点 (像素坐标)
        int[][] river1 = {
                {49200, 10800}, {48000, 15000}, {42000, 20000},
                {36000, 24000}, {35500, 28000}, {35200, 32000},
                {34000, 38000}, {32000, 44000}, {30000, 50000},
                {28000, 56000}
        };

        // 主河流2: 从西北流向南方，沿主城西侧
        int[][] river2 = {
                {10800, 10800}, {12000, 16000}, {16000, 20000},
                {20000, 24000}, {24500, 28000}, {24800, 32000},
                {26000, 38000}, {28000, 44000}, {30000, 50000}
        };

        // 支流: 从西方汇入主河流2
        int[][] tributary1 = {
                {2000, 30000}, {8000, 29500}, {14000, 28000},
                {20000, 26500}, {24800, 25000}
        };

        // 支流: 从东方汇入主河流1
        int[][] tributary2 = {
                {58000, 30000}, {52000, 29000}, {46000, 27500},
                {40000, 26000}, {35500, 25000}
        };

        carveRiverPath(map, w, h, river1, RIVER_WIDTH);
        carveRiverPath(map, w, h, river2, RIVER_WIDTH);
        carveRiverPath(map, w, h, tributary1, RIVER_WIDTH - 40);
        carveRiverPath(map, w, h, tributary2, RIVER_WIDTH - 40);
    }

    /**
     * 沿路径点刻划河流，带自然宽度变化
     */
    private void carveRiverPath(int[][] map, int w, int h, int[][] waypoints, int baseWidth) {
        for (int i = 0; i < waypoints.length - 1; i++) {
            int steps = Math.max(
                    Math.abs(waypoints[i + 1][0] - waypoints[i][0]),
                    Math.abs(waypoints[i + 1][1] - waypoints[i][1])
            ) / 10;
            if (steps < 1) steps = 1;

            for (int s = 0; s <= steps; s++) {
                float t = (float) s / steps;
                float cx = waypoints[i][0] + (waypoints[i + 1][0] - waypoints[i][0]) * t;
                float cy = waypoints[i][1] + (waypoints[i + 1][1] - waypoints[i][1]) * t;

                // 宽度随路径变化（正弦波动）
                float widthMod = 0.8f + 0.4f * (float) Math.sin(s * 0.15);
                int radius = (int) ((baseWidth / 2f) * widthMod);

                int tileX = (int) (cx / tileSize);
                int tileY = (int) (cy / tileSize);
                int tileR = radius / tileSize + 1;

                for (int dy = -tileR; dy <= tileR; dy++) {
                    for (int dx = -tileR; dx <= tileR; dx++) {
                        int tx = tileX + dx;
                        int ty = tileY + dy;
                        if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
                            float dist = (float) Math.sqrt(dx * dx + dy * dy) * tileSize;
                            if (dist < radius) {
                                map[ty][tx] = MapGenerator.RIVER;
                            }
                        }
                    }
                }
            }
        }
    }

    // ==================== 主城区域Stamp ====================

    /**
     * 将主城 10000x10000 区域设为 CITY_ROAD（可通行基础）
     */
    private void stampCityArea(int[][] map, int w, int h) {
        int tx1 = CITY_X1 / tileSize;
        int ty1 = CITY_Y1 / tileSize;
        int tx2 = CITY_X2 / tileSize;
        int ty2 = CITY_Y2 / tileSize;

        for (int y = ty1; y <= ty2 && y < h; y++) {
            for (int x = tx1; x <= tx2 && x < w; x++) {
                if (y >= 0 && x >= 0) {
                    map[y][x] = MapGenerator.CITY_ROAD;
                }
            }
        }
    }

    // ==================== 连接道路Stamp ====================

    /**
     * 从主城四面城门向外延伸道路，连接四角村庄
     */
    private void stampRoads(int[][] map, int w, int h) {
        int halfRoad = ROAD_WIDTH / 2 / tileSize;

        // 北门道路: 从 (30000, 25000) 向北方延伸到 (30000, 0)
        int centerCol = CITY_CENTER_X / tileSize;
        stampRoadVertical(map, w, h, centerCol, 0, CITY_Y1 / tileSize, halfRoad);

        // 南门道路: 从 (30000, 35000) 向南方延伸到 (30000, 60000)
        stampRoadVertical(map, w, h, centerCol, CITY_Y2 / tileSize, MAP_HEIGHT / tileSize, halfRoad);

        // 西门道路: 从 (25000, 30000) 向西方延伸
        int centerRow = CITY_CENTER_Y / tileSize;
        stampRoadHorizontal(map, w, h, centerRow, 0, CITY_X1 / tileSize, halfRoad);

        // 东门道路: 从 (35000, 30000) 向东方延伸
        stampRoadHorizontal(map, w, h, centerRow, CITY_X2 / tileSize, MAP_WIDTH / tileSize, halfRoad);

        // 对角线道路：连接主城到四角村庄（斜向道路）
        // 东北路
        stampDiagonalRoad(map, w, h, CITY_X2, CITY_Y1, MAP_WIDTH - 1000, 1000, halfRoad);
        // 东南路
        stampDiagonalRoad(map, w, h, CITY_X2, CITY_Y2, MAP_WIDTH - 1000, MAP_HEIGHT - 1000, halfRoad);
        // 西南路
        stampDiagonalRoad(map, w, h, CITY_X1, CITY_Y2, 1000, MAP_HEIGHT - 1000, halfRoad);
        // 西北路
        stampDiagonalRoad(map, w, h, CITY_X1, CITY_Y1, 1000, 1000, halfRoad);
    }

    private void stampRoadVertical(int[][] map, int w, int h, int centerCol, int startY, int endY, int halfWidth) {
        for (int y = startY; y < endY && y < h; y++) {
            for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                int x = centerCol + dx;
                if (x >= 0 && x < w && y >= 0) {
                    if (map[y][x] != MapGenerator.RIVER && map[y][x] != MapGenerator.LAKE) {
                        map[y][x] = MapGenerator.CITY_ROAD;
                    }
                }
            }
        }
    }

    private void stampRoadHorizontal(int[][] map, int w, int h, int centerRow, int startX, int endX, int halfWidth) {
        for (int x = startX; x < endX && x < w; x++) {
            for (int dy = -halfWidth; dy <= halfWidth; dy++) {
                int y = centerRow + dy;
                if (y >= 0 && y < h && x >= 0) {
                    if (map[y][x] != MapGenerator.RIVER && map[y][x] != MapGenerator.LAKE) {
                        map[y][x] = MapGenerator.CITY_ROAD;
                    }
                }
            }
        }
    }

    private void stampDiagonalRoad(int[][] map, int w, int h, int fromX, int fromY, int toX, int toY, int halfWidth) {
        int steps = Math.max(Math.abs(toX - fromX), Math.abs(toY - fromY)) / tileSize;
        if (steps < 1) return;
        for (int s = 0; s <= steps; s++) {
            float t = (float) s / steps;
            float px = fromX + (toX - fromX) * t;
            float py = fromY + (toY - fromY) * t;
            int tx = (int) (px / tileSize);
            int ty = (int) (py / tileSize);
            for (int dy = -halfWidth; dy <= halfWidth; dy++) {
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    int nx = tx + dx, ny = ty + dy;
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                        if (map[ny][nx] != MapGenerator.RIVER && map[ny][nx] != MapGenerator.LAKE) {
                            map[ny][nx] = MapGenerator.CITY_ROAD;
                        }
                    }
                }
            }
        }
    }

    // ==================== 四角村庄Stamp ====================

    private void stampVillages(int[][] map, int w, int h) {
        int vs = VILLAGE_SIZE / tileSize;

        // 东北村庄 - 碧波渡（渔村）
        stampVillageArea(map, w, h, (MAP_WIDTH - 1000) / tileSize - vs, 1000 / tileSize, vs, MapGenerator.FARMLAND);

        // 东南村庄 - 云岩寨（山村）
        stampVillageArea(map, w, h, (MAP_WIDTH - 1000) / tileSize - vs, (MAP_HEIGHT - 1000) / tileSize - vs, vs, MapGenerator.GRASSLAND);

        // 西南村庄 - 稻香屯（田园村）
        stampVillageArea(map, w, h, 1000 / tileSize, (MAP_HEIGHT - 1000) / tileSize - vs, vs, MapGenerator.FARMLAND);

        // 西北村庄 - 翠微庄（林村）
        stampVillageArea(map, w, h, 1000 / tileSize, 1000 / tileSize, vs, MapGenerator.GRASSLAND);
    }

    private void stampVillageArea(int[][] map, int w, int h, int startX, int startY, int size, int surroundTerrain) {
        // 先Stamp周围地形为适合村庄的地形
        int pad = 4; // 额外padding
        for (int y = startY - pad; y < startY + size + pad; y++) {
            for (int x = startX - pad; x < startX + size + pad; x++) {
                if (x >= 0 && x < w && y >= 0 && y < h) {
                    if (map[y][x] != MapGenerator.RIVER && map[y][x] != MapGenerator.LAKE) {
                        map[y][x] = surroundTerrain;
                    }
                }
            }
        }
        // 村庄内部设为 VILLAGE_CAN_PASS（后续由 VillageRenderer 标记建筑碰撞）
        for (int y = startY; y < startY + size && y < h; y++) {
            for (int x = startX; x < startX + size && x < w; x++) {
                if (x >= 0 && y >= 0) {
                    map[y][x] = MapGenerator.VILLAGE_CAN_PASS;
                }
            }
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取四角村庄的世界坐标范围
     * @param villageIndex 0=东北, 1=东南, 2=西南, 3=西北
     */
    public static int[] getVillageBounds(int villageIndex) {
        switch (villageIndex) {
            case 0: // 东北 - 碧波渡
                return new int[]{MAP_WIDTH - 1000 - VILLAGE_SIZE, 1000,
                        MAP_WIDTH - 1000, 1000 + VILLAGE_SIZE};
            case 1: // 东南 - 云岩寨
                return new int[]{MAP_WIDTH - 1000 - VILLAGE_SIZE, MAP_HEIGHT - 1000 - VILLAGE_SIZE,
                        MAP_WIDTH - 1000, MAP_HEIGHT - 1000};
            case 2: // 西南 - 稻香屯
                return new int[]{1000, MAP_HEIGHT - 1000 - VILLAGE_SIZE,
                        1000 + VILLAGE_SIZE, MAP_HEIGHT - 1000};
            case 3: // 西北 - 翠微庄
                return new int[]{1000, 1000,
                        1000 + VILLAGE_SIZE, 1000 + VILLAGE_SIZE};
            default:
                return new int[]{0, 0, VILLAGE_SIZE, VILLAGE_SIZE};
        }
    }

    /**
     * 获取村庄名称
     */
    public static String getVillageName(int villageIndex) {
        switch (villageIndex) {
            case 0: return "碧波渡";
            case 1: return "云岩寨";
            case 2: return "稻香屯";
            case 3: return "翠微庄";
            default: return "未知";
        }
    }
}

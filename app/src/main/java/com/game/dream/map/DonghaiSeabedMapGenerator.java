package com.game.dream.map;

/**
 * 东海海底地图生成器
 * 地图尺寸: 20000x20000
 * 中心: 东海龙宫 (6000x6000, 位于 7000~13000)
 * 地形分层: 龙宫 → 海底沙地 → 海藻森林 → 深海/珊瑚礁 → 海底火山
 */
public class DonghaiSeabedMapGenerator {

    public static final int MAP_WIDTH = 20000;
    public static final int MAP_HEIGHT = 20000;

    // 龙宫区域
    public static final int PALACE_X1 = 7000;
    public static final int PALACE_Y1 = 7000;
    public static final int PALACE_X2 = 13000;
    public static final int PALACE_Y2 = 13000;

    // 地图中心
    private static final float CENTER_X = MAP_WIDTH / 2f;
    private static final float CENTER_Y = MAP_HEIGHT / 2f;
    private static final float MAX_DIST = (float) Math.sqrt(CENTER_X * CENTER_X + CENTER_Y * CENTER_Y);

    // 龙宫围墙参数
    public static final int WALL_THICKNESS = 80;
    public static final int GATE_WIDTH = 500;  // 南门宽度
    public static final int GATE_HEIGHT = 400; // 南门高度（视觉上）
    public static final int SIDE_GATE_WIDTH = 400; // 左右门宽度

    // 左右门中心 Y 坐标
    public static final int SIDE_GATE_CENTER_Y = (PALACE_Y1 + PALACE_Y2) / 2; // 10000

    private int tileSize;

    public DonghaiSeabedMapGenerator(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * 生成海底地图地形数据
     */
    public int[][] generateMap() {
        int w = MAP_WIDTH / tileSize;
        int h = MAP_HEIGHT / tileSize;
        int[][] map = new int[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float worldX = x * tileSize + tileSize / 2f;
                float worldY = y * tileSize + tileSize / 2f;

                // 到地图中心的距离
                float dx = worldX - CENTER_X;
                float dy = worldY - CENTER_Y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float normalizedDist = dist / MAX_DIST;

                // 添加噪声使边界自然
                float noise = getSimpleNoise(x, y);

                // 龙宫区域内
                if (worldX >= PALACE_X1 && worldX <= PALACE_X2
                        && worldY >= PALACE_Y1 && worldY <= PALACE_Y2) {
                    map[y][x] = MapGenerator.PALACE_GROUND;
                }
                // 龙宫外围区域（距离中心 < 4500）→ 海底沙地
                else if (normalizedDist < 0.30f + noise * 0.03f) {
                    map[y][x] = MapGenerator.SEA_FLOOR;
                }
                // 珊瑚礁带（0.30~0.40）
                else if (normalizedDist < 0.40f + noise * 0.03f) {
                    // 珊瑚礁中散布海底沙地
                    if (noise > 0.3f) {
                        map[y][x] = MapGenerator.SEA_FLOOR;
                    } else {
                        map[y][x] = MapGenerator.CORAL_REEF;
                    }
                }
                // 海藻森林（0.40~0.55）
                else if (normalizedDist < 0.55f + noise * 0.04f) {
                    // 海藻森林中散布珊瑚礁
                    if (noise > 0.4f) {
                        map[y][x] = MapGenerator.CORAL_REEF;
                    } else {
                        map[y][x] = MapGenerator.KELP_FOREST;
                    }
                }
                // 深海区域（0.55~0.75）
                else if (normalizedDist < 0.75f + noise * 0.04f) {
                    // 深海中散布海藻森林
                    if (noise > 0.5f) {
                        map[y][x] = MapGenerator.KELP_FOREST;
                    } else {
                        map[y][x] = MapGenerator.DEEP_SEA;
                    }
                }
                // 海底火山/岩壁（0.75+）→ 不可通行边界
                else {
                    // 火山区域中散布热液喷口
                    if (noise > 0.2f && normalizedDist < 0.85f) {
                        map[y][x] = MapGenerator.DEEP_SEA;
                    } else {
                        map[y][x] = MapGenerator.HYDROTHERMAL;
                    }
                }
            }
        }

        // 龙宫围墙标记为不可通行（留出南门）
        markPalaceWalls(map, w, h);

        return map;
    }

    /**
     * 标记龙宫围墙为 CITY_WALL，留出南门、左门、右门
     */
    private void markPalaceWalls(int[][] map, int w, int h) {
        int t = tileSize;
        int wallT = WALL_THICKNESS;

        // 南门位置：南墙中央
        int gateCenterX = (PALACE_X1 + PALACE_X2) / 2;
        int southGateLeft = gateCenterX - GATE_WIDTH / 2;
        int southGateRight = gateCenterX + GATE_WIDTH / 2;

        // 左右门位置：西墙/东墙中央
        int sideGateTop = SIDE_GATE_CENTER_Y - SIDE_GATE_WIDTH / 2;
        int sideGateBottom = SIDE_GATE_CENTER_Y + SIDE_GATE_WIDTH / 2;

        // 西墙（左墙）—— 留左门
        for (int y = PALACE_Y1; y <= PALACE_Y2; y += t) {
            // 跳过左门区域
            if (y >= sideGateTop && y <= sideGateBottom) continue;
            for (int x = PALACE_X1; x < PALACE_X1 + wallT; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.CITY_WALL;
                }
            }
        }

        // 东墙（右墙）—— 留右门
        for (int y = PALACE_Y1; y <= PALACE_Y2; y += t) {
            // 跳过右门区域
            if (y >= sideGateTop && y <= sideGateBottom) continue;
            for (int x = PALACE_X2 - wallT; x <= PALACE_X2; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.CITY_WALL;
                }
            }
        }

        // 北墙（上墙）
        for (int y = PALACE_Y1; y < PALACE_Y1 + wallT; y += t) {
            for (int x = PALACE_X1; x <= PALACE_X2; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.CITY_WALL;
                }
            }
        }

        // 南墙（下墙）—— 留南门
        for (int y = PALACE_Y2 - wallT; y <= PALACE_Y2; y += t) {
            for (int x = PALACE_X1; x <= PALACE_X2; x += t) {
                // 跳过南门区域
                if (x >= southGateLeft && x <= southGateRight) continue;
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.CITY_WALL;
                }
            }
        }
    }

    /**
     * 简易噪声函数（基于正弦叠加）
     */
    private float getSimpleNoise(int x, int y) {
        double n = Math.sin(x * 0.05) * Math.cos(y * 0.05) * 0.5
                + Math.sin(x * 0.12 + y * 0.08) * 0.3
                + Math.cos(x * 0.03 - y * 0.11) * 0.2;
        return (float) n;
    }

    /**
     * 获取龙宫围墙障碍物矩形（供碰撞检测）
     */
    public static java.util.List<android.graphics.Rect> getPalaceWallObstacles() {
        java.util.List<android.graphics.Rect> obstacles = new java.util.ArrayList<>();
        int wallT = WALL_THICKNESS;
        int gateCenterX = (PALACE_X1 + PALACE_X2) / 2;
        int southGateLeft = gateCenterX - GATE_WIDTH / 2;
        int southGateRight = gateCenterX + GATE_WIDTH / 2;

        int sideGateTop = SIDE_GATE_CENTER_Y - SIDE_GATE_WIDTH / 2;
        int sideGateBottom = SIDE_GATE_CENTER_Y + SIDE_GATE_WIDTH / 2;

        // 西墙上段（北半部分）
        obstacles.add(new android.graphics.Rect(PALACE_X1, PALACE_Y1,
                PALACE_X1 + wallT, sideGateTop));
        // 西墙下段（南半部分）
        obstacles.add(new android.graphics.Rect(PALACE_X1, sideGateBottom,
                PALACE_X1 + wallT, PALACE_Y2));
        // 东墙上段（北半部分）
        obstacles.add(new android.graphics.Rect(PALACE_X2 - wallT, PALACE_Y1,
                PALACE_X2, sideGateTop));
        // 东墙下段（南半部分）
        obstacles.add(new android.graphics.Rect(PALACE_X2 - wallT, sideGateBottom,
                PALACE_X2, PALACE_Y2));
        // 北墙
        obstacles.add(new android.graphics.Rect(PALACE_X1, PALACE_Y1,
                PALACE_X2, PALACE_Y1 + wallT));
        // 南墙左段
        obstacles.add(new android.graphics.Rect(PALACE_X1, PALACE_Y2 - wallT,
                southGateLeft, PALACE_Y2));
        // 南墙右段
        obstacles.add(new android.graphics.Rect(southGateRight, PALACE_Y2 - wallT,
                PALACE_X2, PALACE_Y2));

        return obstacles;
    }

    /**
     * 获取传送出生点（龙宫南门外）
     */
    public static android.util.Pair<Integer, Integer> getSpawnPosition() {
        int gateCenterX = (PALACE_X1 + PALACE_X2) / 2;
        return new android.util.Pair<>(gateCenterX, PALACE_Y2 + 300);
    }
}

package com.game.dream.map;

/**
 * 地府地图生成器
 * 地图尺寸: 20000x20000
 * 中心: 阎罗殿 (6000x6000, 位于 7000~13000)
 * 地形分层: 阎罗殿 → 彼岸花田 → 黄泉河(奈何桥) → 幽冥荒原 → 白骨堆 → 地狱深渊
 */
public class NetherworldMapGenerator {

    public static final int MAP_WIDTH = 20000;
    public static final int MAP_HEIGHT = 20000;

    // 阎罗殿区域
    public static final int PALACE_X1 = 7000;
    public static final int PALACE_Y1 = 7000;
    public static final int PALACE_X2 = 13000;
    public static final int PALACE_Y2 = 13000;

    // 地图中心
    private static final float CENTER_X = MAP_WIDTH / 2f;
    private static final float CENTER_Y = MAP_HEIGHT / 2f;
    private static final float MAX_DIST = (float) Math.sqrt(CENTER_X * CENTER_X + CENTER_Y * CENTER_Y);

    // 鬼门关围墙参数
    public static final int WALL_THICKNESS = 80;
    public static final int GATE_WIDTH = 500;   // 南门宽度（鬼门关）
    public static final int SIDE_GATE_WIDTH = 400; // 左右侧门宽度

    // 左右门中心 Y 坐标
    public static final int SIDE_GATE_CENTER_Y = (PALACE_Y1 + PALACE_Y2) / 2; // 10000

    // 奈何桥参数（横跨黄泉河，位于南门正南方）
    public static final int BRIDGE_WIDTH = 500;
    public static final int BRIDGE_CENTER_X = (PALACE_X1 + PALACE_X2) / 2;

    // 幽魂牢（主角死亡后囚禁之地，位于地图东南角 HELL_PIT 深渊中）
    public static final int PRISON_X1 = 18200;
    public static final int PRISON_Y1 = 18200;
    public static final int PRISON_X2 = 19400;
    public static final int PRISON_Y2 = 19400;
    public static final int PRISON_WALL_THICKNESS = 80;
    // 牢内中心（传送出生点）
    public static final int PRISON_CENTER_X = (PRISON_X1 + PRISON_X2) / 2;
    public static final int PRISON_CENTER_Y = (PRISON_Y1 + PRISON_Y2) / 2;

    private int tileSize;

    public NetherworldMapGenerator(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * 生成地府地图地形数据
     */
    public int[][] generateMap() {
        int w = MAP_WIDTH / tileSize;
        int h = MAP_HEIGHT / tileSize;
        int[][] map = new int[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float worldX = x * tileSize + tileSize / 2f;
                float worldY = y * tileSize + tileSize / 2f;

                float dx = worldX - CENTER_X;
                float dy = worldY - CENTER_Y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float normalizedDist = dist / MAX_DIST;

                float noise = getSimpleNoise(x, y);

                // 1. 阎罗殿区域
                if (worldX >= PALACE_X1 && worldX <= PALACE_X2
                        && worldY >= PALACE_Y1 && worldY <= PALACE_Y2) {
                    map[y][x] = MapGenerator.JUDGE_HALL_GROUND;
                }
                // 2. 彼岸花田（0.28~0.36）——紧邻宫墙外
                else if (normalizedDist < 0.36f + noise * 0.02f) {
                    map[y][x] = MapGenerator.SPIDER_LILY_FIELD;
                }
                // 3. 黄泉河（0.36~0.44）——环形水道
                else if (normalizedDist < 0.44f + noise * 0.02f) {
                    map[y][x] = MapGenerator.YELLOW_SPRING_RIVER;
                }
                // 4. 幽冥荒原（0.44~0.72）——散布白骨堆
                else if (normalizedDist < 0.72f + noise * 0.03f) {
                    if (noise > 0.55f) {
                        map[y][x] = MapGenerator.BONE_PILE;
                    } else {
                        map[y][x] = MapGenerator.NETHER_WASTELAND;
                    }
                }
                // 5. 外围白骨荒原（0.72~0.86）——白骨堆密集
                else if (normalizedDist < 0.86f + noise * 0.03f) {
                    if (noise > 0.25f) {
                        map[y][x] = MapGenerator.BONE_PILE;
                    } else {
                        map[y][x] = MapGenerator.NETHER_WASTELAND;
                    }
                }
                // 6. 边界地狱深渊
                else {
                    map[y][x] = MapGenerator.HELL_PIT;
                }
            }
        }

        // 标记阎罗殿围墙为不可通行（留鬼门关南门 + 左右侧门）
        markPalaceWalls(map, w, h);

        // 在黄泉河上开凿奈何桥（南门正南方）
        carveNaiheBridge(map, w, h);

        // 开凿幽魂牢（东南角封闭囚室）
        carvePrison(map, w, h);

        return map;
    }

    /**
     * 标记阎罗殿围墙为 GHOST_GATE_WALL，留出鬼门关（南门）、左门、右门
     */
    private void markPalaceWalls(int[][] map, int w, int h) {
        int t = tileSize;
        int wallT = WALL_THICKNESS;

        int gateCenterX = (PALACE_X1 + PALACE_X2) / 2;
        int southGateLeft = gateCenterX - GATE_WIDTH / 2;
        int southGateRight = gateCenterX + GATE_WIDTH / 2;

        int sideGateTop = SIDE_GATE_CENTER_Y - SIDE_GATE_WIDTH / 2;
        int sideGateBottom = SIDE_GATE_CENTER_Y + SIDE_GATE_WIDTH / 2;

        // 西墙（留左门）
        for (int y = PALACE_Y1; y <= PALACE_Y2; y += t) {
            if (y >= sideGateTop && y <= sideGateBottom) continue;
            for (int x = PALACE_X1; x < PALACE_X1 + wallT; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.GHOST_GATE_WALL;
                }
            }
        }

        // 东墙（留右门）
        for (int y = PALACE_Y1; y <= PALACE_Y2; y += t) {
            if (y >= sideGateTop && y <= sideGateBottom) continue;
            for (int x = PALACE_X2 - wallT; x <= PALACE_X2; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.GHOST_GATE_WALL;
                }
            }
        }

        // 北墙
        for (int y = PALACE_Y1; y < PALACE_Y1 + wallT; y += t) {
            for (int x = PALACE_X1; x <= PALACE_X2; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.GHOST_GATE_WALL;
                }
            }
        }

        // 南墙（留鬼门关）
        for (int y = PALACE_Y2 - wallT; y <= PALACE_Y2; y += t) {
            for (int x = PALACE_X1; x <= PALACE_X2; x += t) {
                if (x >= southGateLeft && x <= southGateRight) continue;
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.GHOST_GATE_WALL;
                }
            }
        }
    }

    /**
     * 开凿奈何桥：从鬼门关往南延伸，跨越整个黄泉河环带
     * 桥面为 STONE_BRIDGE，可通行
     */
    private void carveNaiheBridge(int[][] map, int w, int h) {
        int t = tileSize;
        int bridgeLeft = BRIDGE_CENTER_X - BRIDGE_WIDTH / 2;
        int bridgeRight = BRIDGE_CENTER_X + BRIDGE_WIDTH / 2;

        // 从宫门外一直延伸到荒原内
        int yStart = PALACE_Y2;
        int yEnd = (int) (CENTER_Y + 0.46f * MAX_DIST);

        for (int y = yStart; y <= yEnd; y += t) {
            for (int x = bridgeLeft; x <= bridgeRight; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    int cur = map[ty][tx];
                    // 只覆盖黄泉河与彼岸花田，不动围墙
                    if (cur == MapGenerator.YELLOW_SPRING_RIVER
                            || cur == MapGenerator.SPIDER_LILY_FIELD) {
                        map[ty][tx] = MapGenerator.STONE_BRIDGE;
                    }
                }
            }
        }
    }

    /**
     * 开凿幽魂牢：地图东南角 HELL_PIT 深渊中的封闭囚室
     * 内部铺 JUDGE_HALL_GROUND（可通行），四周 80 厚 GHOST_GATE_WALL（不可通行）
     */
    private void carvePrison(int[][] map, int w, int h) {
        int t = tileSize;
        int wallT = PRISON_WALL_THICKNESS;

        // 1. 先把整个牢区（含墙）清成墙体
        for (int y = PRISON_Y1; y <= PRISON_Y2; y += t) {
            for (int x = PRISON_X1; x <= PRISON_X2; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.GHOST_GATE_WALL;
                }
            }
        }

        // 2. 把内部（去掉墙厚）铺成可通行地面
        for (int y = PRISON_Y1 + wallT; y <= PRISON_Y2 - wallT; y += t) {
            for (int x = PRISON_X1 + wallT; x <= PRISON_X2 - wallT; x += t) {
                int tx = x / t, ty = y / t;
                if (ty >= 0 && ty < h && tx >= 0 && tx < w) {
                    map[ty][tx] = MapGenerator.JUDGE_HALL_GROUND;
                }
            }
        }
    }

    /**
     * 获取幽魂牢墙体障碍物矩形（供碰撞检测）
     */
    public static java.util.List<android.graphics.Rect> getPrisonWallObstacles() {
        java.util.List<android.graphics.Rect> obstacles = new java.util.ArrayList<>();
        int wallT = PRISON_WALL_THICKNESS;
        // 四面墙各一个矩形
        obstacles.add(new android.graphics.Rect(PRISON_X1, PRISON_Y1, PRISON_X2, PRISON_Y1 + wallT)); // 北墙
        obstacles.add(new android.graphics.Rect(PRISON_X1, PRISON_Y2 - wallT, PRISON_X2, PRISON_Y2)); // 南墙
        obstacles.add(new android.graphics.Rect(PRISON_X1, PRISON_Y1, PRISON_X1 + wallT, PRISON_Y2)); // 西墙
        obstacles.add(new android.graphics.Rect(PRISON_X2 - wallT, PRISON_Y1, PRISON_X2, PRISON_Y2)); // 东墙
        return obstacles;
    }

    /**
     * 获取幽魂牢传送出生点（牢内中心）
     */
    public static android.util.Pair<Integer, Integer> getPrisonSpawnPosition() {
        return new android.util.Pair<>(PRISON_CENTER_X, PRISON_CENTER_Y);
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
     * 获取阎罗殿围墙障碍物矩形（供碰撞检测）
     */
    public static java.util.List<android.graphics.Rect> getPalaceWallObstacles() {
        java.util.List<android.graphics.Rect> obstacles = new java.util.ArrayList<>();
        int wallT = WALL_THICKNESS;
        int gateCenterX = (PALACE_X1 + PALACE_X2) / 2;
        int southGateLeft = gateCenterX - GATE_WIDTH / 2;
        int southGateRight = gateCenterX + GATE_WIDTH / 2;

        int sideGateTop = SIDE_GATE_CENTER_Y - SIDE_GATE_WIDTH / 2;
        int sideGateBottom = SIDE_GATE_CENTER_Y + SIDE_GATE_WIDTH / 2;

        // 西墙上段
        obstacles.add(new android.graphics.Rect(PALACE_X1, PALACE_Y1,
                PALACE_X1 + wallT, sideGateTop));
        // 西墙下段
        obstacles.add(new android.graphics.Rect(PALACE_X1, sideGateBottom,
                PALACE_X1 + wallT, PALACE_Y2));
        // 东墙上段
        obstacles.add(new android.graphics.Rect(PALACE_X2 - wallT, PALACE_Y1,
                PALACE_X2, sideGateTop));
        // 东墙下段
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
     * 获取传送出生点（鬼门关外、奈何桥北端）
     */
    public static android.util.Pair<Integer, Integer> getSpawnPosition() {
        int gateCenterX = (PALACE_X1 + PALACE_X2) / 2;
        return new android.util.Pair<>(gateCenterX, PALACE_Y2 + 300);
    }
}

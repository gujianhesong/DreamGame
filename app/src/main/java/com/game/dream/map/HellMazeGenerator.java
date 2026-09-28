package com.game.dream.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 地狱迷宫生成器 - 4层不同主题
 *
 * 第1层 黄泉迷径：标准DFS迷宫，黄泉雾气主题
 * 第2层 血池炼狱：迷宫+血池地形（持续伤害）+安全石台
 * 第3层 枉死城：宽巷道迷宫+陷阱地形
 * 第4层 阎罗殿：线性BOSS竞技场（3个区域）
 */
public class HellMazeGenerator {

    // 地狱迷宫地形常量（墙/地板/入口/出口复用 MazeGenerator 常量，保证碰撞检测兼容）
    public static final int HELL_WALL = MazeGenerator.MAZE_WALL;       // 200
    public static final int HELL_FLOOR = MazeGenerator.MAZE_FLOOR;     // 201
    public static final int HELL_ENTRANCE = MazeGenerator.MAZE_ENTRANCE; // 202
    public static final int HELL_EXIT = MazeGenerator.MAZE_EXIT;       // 203
    // 特殊地形（可通行但有特殊效果）
    public static final int HELL_BLOOD_POOL = 214;  // 第2层：血池（持续伤害）
    public static final int HELL_SAFE_STONE = 215;  // 第2层：安全石台
    public static final int HELL_TRAP = 216;        // 第3层：陷阱
    public static final int HELL_BOSS_ARENA = 217;  // 第4层：BOSS战区域

    private int mapWidth;
    private int mapHeight;
    private int tileSize;
    private int floor; // 1-4
    private Random random;

    private int cellSize = 8; // 通道宽度 = cellSize * tileSize

    // 入口/出口坐标 (像素坐标)
    private int entranceX, entranceY;
    private int exitX, exitY;
    // 出口房间中心坐标（迷宫内部，保证可通行，用于 BOSS 守出口）
    private int exitRoomX, exitRoomY;

    // 第4层BOSS区域中心坐标
    private int[] bossArenaCenters = new int[6]; // 3个BOSS区域的 x,y

    public HellMazeGenerator(int mapWidth, int mapHeight, int tileSize, int floor) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.floor = floor;
        // 随机种子：每次生成不同的迷宫布局
        this.random = new Random();

        // 第3层巷道更宽
        if (floor == 3) {
            cellSize = 10;
        }
    }

    // 边方向常量: 0=上, 1=下, 2=左, 3=右
    private static final int EDGE_TOP = 0;
    private static final int EDGE_BOTTOM = 1;
    private static final int EDGE_LEFT = 2;
    private static final int EDGE_RIGHT = 3;

    public int[][] generateMap() {
        if (floor == 4) {
            return generateBossArena();
        }
        return generateMazeFloor();
    }

    /**
     * 生成第1-3层迷宫
     */
    private int[][] generateMazeFloor() {
        int cols = mapWidth / tileSize;
        int rows = mapHeight / tileSize;

        // 初始化全部为墙壁
        int[][] map = new int[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                map[y][x] = HELL_WALL;
            }
        }

        // 迷宫网格
        int gridCols = cols / cellSize;
        int gridRows = rows / cellSize;
        if (gridCols % 2 == 0) gridCols--;
        if (gridRows % 2 == 0) gridRows--;

        // === 随机选择入口/出口所在的边（不同边） ===
        int entranceEdge = random.nextInt(4);
        int exitEdge;
        do {
            exitEdge = random.nextInt(4);
        } while (exitEdge == entranceEdge);

        // 入口房间（贴着入口边的随机房间）
        int startRow, startCol;
        switch (entranceEdge) {
            case EDGE_TOP:
                startRow = 1;
                startCol = randomOdd(1, gridCols - 2);
                break;
            case EDGE_BOTTOM:
                startRow = gridRows - 2;
                startCol = randomOdd(1, gridCols - 2);
                break;
            case EDGE_LEFT:
                startRow = randomOdd(1, gridRows - 2);
                startCol = 1;
                break;
            default: // EDGE_RIGHT
                startRow = randomOdd(1, gridRows - 2);
                startCol = gridCols - 2;
                break;
        }

        // 出口房间（贴着出口边的随机房间）
        int endRow, endCol;
        switch (exitEdge) {
            case EDGE_TOP:
                endRow = 1;
                endCol = randomOdd(1, gridCols - 2);
                break;
            case EDGE_BOTTOM:
                endRow = gridRows - 2;
                endCol = randomOdd(1, gridCols - 2);
                break;
            case EDGE_LEFT:
                endRow = randomOdd(1, gridRows - 2);
                endCol = 1;
                break;
            default: // EDGE_RIGHT
                endRow = randomOdd(1, gridRows - 2);
                endCol = gridCols - 2;
                break;
        }

        // DFS 递归回溯生成迷宫
        boolean[][] visited = new boolean[gridRows][gridCols];
        List<int[]> stack = new ArrayList<>();

        // 从入口房间开始
        visited[startRow][startCol] = true;
        carveArea(map, startRow, startCol, rows, cols);
        stack.add(new int[]{startRow, startCol});

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!stack.isEmpty()) {
            int[] current = stack.get(stack.size() - 1);
            int cr = current[0];
            int cc = current[1];

            List<int[]> neighbors = new ArrayList<>();
            for (int[] dir : directions) {
                int nr = cr + dir[0] * 2;
                int nc = cc + dir[1] * 2;
                if (nr > 0 && nr < gridRows - 1 && nc > 0 && nc < gridCols - 1 && !visited[nr][nc]) {
                    neighbors.add(new int[]{nr, nc, dir[0], dir[1]});
                }
            }

            if (neighbors.isEmpty()) {
                stack.remove(stack.size() - 1);
            } else {
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                int nr = next[0], nc = next[1], dr = next[2], dc = next[3];

                // 挖掉墙壁
                carveArea(map, cr + dr, cc + dc, rows, cols);
                // 挖掉目标房间
                carveArea(map, nr, nc, rows, cols);

                visited[nr][nc] = true;
                stack.add(new int[]{nr, nc});
            }
        }

        // 额外通道（第1层少，第3层多）
        int extraRatio = (floor == 1) ? 8 : (floor == 2) ? 6 : 5;
        addExtraPassages(map, gridRows, gridCols, rows, cols, extraRatio);

        // === 挖出入口通道并标记（根据随机边） ===
        carveEdgePassage(map, entranceEdge, startRow, startCol, rows, cols, HELL_ENTRANCE);
        setEdgePortalCoords(entranceEdge, startRow, startCol, rows, cols, true);

        // === 挖出出口通道并标记 ===
        carveEdgePassage(map, exitEdge, endRow, endCol, rows, cols, HELL_EXIT);
        setEdgePortalCoords(exitEdge, endRow, endCol, rows, cols, false);

        // === 各层特殊地形 ===
        if (floor == 2) {
            addBloodPools(map, rows, cols);
        } else if (floor == 3) {
            addTraps(map, rows, cols);
        }

        return map;
    }

    /**
     * 返回 [min, max] 范围内的随机奇数
     */
    private int randomOdd(int min, int max) {
        if (max < min) return min;
        int v = min + random.nextInt(max - min + 1);
        if (v % 2 == 0) {
            v = (v + 1 <= max) ? v + 1 : v - 1;
        }
        return Math.max(min, v);
    }

    /**
     * 从地图边缘到指定房间挖一条通道，并在边缘标记入口/出口地形
     */
    private void carveEdgePassage(int[][] map, int edge, int roomRow, int roomCol,
                                  int rows, int cols, int markerTerrain) {
        int roomTileR = roomRow * cellSize;
        int roomTileC = roomCol * cellSize;
        switch (edge) {
            case EDGE_TOP: {
                for (int r = 0; r <= roomTileR; r++) {
                    for (int c = 0; c < cellSize; c++) {
                        int tx = roomTileC + c;
                        if (r < rows && tx < cols) map[r][tx] = HELL_FLOOR;
                    }
                }
                for (int c = 0; c < cellSize; c++) {
                    int tx = roomTileC + c;
                    if (tx < cols) map[0][tx] = markerTerrain;
                }
                break;
            }
            case EDGE_BOTTOM: {
                for (int r = roomTileR; r < rows; r++) {
                    for (int c = 0; c < cellSize; c++) {
                        int tx = roomTileC + c;
                        if (r >= 0 && tx < cols) map[r][tx] = HELL_FLOOR;
                    }
                }
                for (int c = 0; c < cellSize; c++) {
                    int tx = roomTileC + c;
                    if (tx < cols) map[rows - 1][tx] = markerTerrain;
                }
                break;
            }
            case EDGE_LEFT: {
                for (int c = 0; c <= roomTileC; c++) {
                    for (int r = 0; r < cellSize; r++) {
                        int ty = roomTileR + r;
                        if (ty < rows && c < cols) map[ty][c] = HELL_FLOOR;
                    }
                }
                for (int r = 0; r < cellSize; r++) {
                    int ty = roomTileR + r;
                    if (ty < rows) map[ty][0] = markerTerrain;
                }
                break;
            }
            default: { // EDGE_RIGHT
                for (int c = roomTileC; c < cols; c++) {
                    for (int r = 0; r < cellSize; r++) {
                        int ty = roomTileR + r;
                        if (ty < rows && c >= 0) map[ty][c] = HELL_FLOOR;
                    }
                }
                for (int r = 0; r < cellSize; r++) {
                    int ty = roomTileR + r;
                    if (ty < rows) map[ty][cols - 1] = markerTerrain;
                }
                break;
            }
        }
    }

    /**
     * 计算入口/出口的像素坐标（位于边缘通道中心）
     */
    private void setEdgePortalCoords(int edge, int roomRow, int roomCol,
                                     int rows, int cols, boolean isEntrance) {
        int centerX = (roomCol * cellSize + cellSize / 2) * tileSize;
        int centerY = (roomRow * cellSize + cellSize / 2) * tileSize;
        int px, py;
        switch (edge) {
            case EDGE_TOP:
                px = centerX; py = tileSize * 2;
                break;
            case EDGE_BOTTOM:
                px = centerX; py = (rows - 3) * tileSize;
                break;
            case EDGE_LEFT:
                px = tileSize * 2; py = centerY;
                break;
            default: // EDGE_RIGHT
                px = (cols - 3) * tileSize; py = centerY;
                break;
        }
        if (isEntrance) {
            entranceX = px;
            entranceY = py;
        } else {
            exitX = px;
            exitY = py;
            // 记录出口房间中心（迷宫内部）
            exitRoomX = centerX;
            exitRoomY = centerY;
        }
    }

    /**
     * 第2层：在通道中随机生成血池区域 + 安全石台
     */
    private void addBloodPools(int[][] map, int rows, int cols) {
        int poolCount = 25 + random.nextInt(10); // 25-34个血池区域
        for (int i = 0; i < poolCount; i++) {
            // 随机选一个通道位置
            for (int attempt = 0; attempt < 50; attempt++) {
                int cx = cellSize + random.nextInt(cols - cellSize * 2);
                int cy = cellSize + random.nextInt(rows - cellSize * 2);
                if (map[cy][cx] != HELL_FLOOR) continue;
                // 不在入口/出口附近
                float px = cx * tileSize, py = cy * tileSize;
                if (dist(px, py, entranceX, entranceY) < 600) continue;
                if (dist(px, py, exitX, exitY) < 600) continue;

                // 生成椭圆形血池 (半径 3-6 tiles)
                int rx = 3 + random.nextInt(4);
                int ry = 3 + random.nextInt(4);
                for (int dy = -ry; dy <= ry; dy++) {
                    for (int dx = -rx; dx <= rx; dx++) {
                        if (dx * dx * ry * ry + dy * dy * rx * rx <= rx * rx * ry * ry) {
                            int ty = cy + dy, tx = cx + dx;
                            if (ty > 0 && ty < rows - 1 && tx > 0 && tx < cols - 1
                                    && map[ty][tx] == HELL_FLOOR) {
                                map[ty][tx] = HELL_BLOOD_POOL;
                            }
                        }
                    }
                }

                // 血池中心放一个安全石台
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int ty = cy + dy, tx = cx + dx;
                        if (ty > 0 && ty < rows - 1 && tx > 0 && tx < cols - 1) {
                            map[ty][tx] = HELL_SAFE_STONE;
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * 第3层：在通道中随机放置陷阱
     */
    private void addTraps(int[][] map, int rows, int cols) {
        int trapCount = 60 + random.nextInt(20); // 60-79个陷阱
        for (int i = 0; i < trapCount; i++) {
            for (int attempt = 0; attempt < 50; attempt++) {
                int tx = cellSize + random.nextInt(cols - cellSize * 2);
                int ty = cellSize + random.nextInt(rows - cellSize * 2);
                if (map[ty][tx] != HELL_FLOOR) continue;
                float px = tx * tileSize, py = ty * tileSize;
                if (dist(px, py, entranceX, entranceY) < 500) continue;
                if (dist(px, py, exitX, exitY) < 500) continue;
                map[ty][tx] = HELL_TRAP;
                break;
            }
        }
    }

    /**
     * 第4层：线性BOSS竞技场 - 3个大房间由走廊连接
     */
    private int[][] generateBossArena() {
        int cols = mapWidth / tileSize;
        int rows = mapHeight / tileSize;

        int[][] map = new int[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                map[y][x] = HELL_WALL;
            }
        }

        // 入口在顶部中间
        int entranceCol = cols / 2;
        entranceX = entranceCol * tileSize;
        entranceY = tileSize * 2;

        // 3个BOSS房间，垂直排列
        int roomSize = 30; // 30x30 tiles = 600x600 px
        int corridorWidth = 6;
        int startY = 10;
        int roomGap = (rows - startY - roomSize * 3) / 4;

        for (int i = 0; i < 3; i++) {
            int roomTop = startY + roomGap * (i + 1) + roomSize * i;
            int roomLeft = (cols - roomSize) / 2;
            int roomCenterX = (roomLeft + roomSize / 2) * tileSize;
            int roomCenterY = (roomTop + roomSize / 2) * tileSize;
            bossArenaCenters[i * 2] = roomCenterX;
            bossArenaCenters[i * 2 + 1] = roomCenterY;

            // 挖出房间
            for (int r = roomTop; r < roomTop + roomSize && r < rows; r++) {
                for (int c = roomLeft; c < roomLeft + roomSize && c < cols; c++) {
                    if (r >= 0 && c >= 0) {
                        map[r][c] = HELL_BOSS_ARENA;
                    }
                }
            }

            // 连接走廊（从入口或上一个房间到当前房间）
            int corridorTop;
            if (i == 0) {
                corridorTop = 0;
            } else {
                corridorTop = startY + roomGap * i + roomSize * (i - 1);
            }
            int corridorLeft = (cols - corridorWidth) / 2;
            for (int r = corridorTop; r < roomTop && r < rows; r++) {
                for (int c = corridorLeft; c < corridorLeft + corridorWidth && c < cols; c++) {
                    if (r >= 0 && c >= 0) {
                        map[r][c] = HELL_FLOOR;
                    }
                }
            }
        }

        // 标记入口
        int entCol = (cols - corridorWidth) / 2;
        for (int c = entCol; c < entCol + corridorWidth && c < cols; c++) {
            map[0][c] = HELL_ENTRANCE;
        }

        // 出口在最后一个房间底部
        int lastRoomBottom = startY + roomGap * 3 + roomSize * 3;
        exitX = (cols / 2) * tileSize;
        exitY = (lastRoomBottom - 2) * tileSize;
        int exitCol = (cols - corridorWidth) / 2;
        for (int r = lastRoomBottom; r < rows && r < lastRoomBottom + 5; r++) {
            for (int c = exitCol; c < exitCol + corridorWidth && c < cols; c++) {
                if (r >= 0 && c >= 0) map[r][c] = HELL_FLOOR;
            }
        }
        if (lastRoomBottom + 4 < rows) {
            for (int c = exitCol; c < exitCol + corridorWidth && c < cols; c++) {
                map[lastRoomBottom + 4][c] = HELL_EXIT;
            }
        }

        return map;
    }

    private void carveArea(int[][] map, int gridRow, int gridCol, int rows, int cols) {
        int startR = gridRow * cellSize;
        int startC = gridCol * cellSize;
        for (int r = 0; r < cellSize; r++) {
            for (int c = 0; c < cellSize; c++) {
                int ty = startR + r, tx = startC + c;
                if (ty >= 0 && ty < rows && tx >= 0 && tx < cols) {
                    map[ty][tx] = HELL_FLOOR;
                }
            }
        }
    }

    private void addExtraPassages(int[][] map, int gridRows, int gridCols, int rows, int cols, int divisor) {
        int extraPassages = (gridRows * gridCols) / divisor;
        for (int i = 0; i < extraPassages; i++) {
            int gr = random.nextInt(gridRows - 2) + 1;
            int gc = random.nextInt(gridCols - 2) + 1;
            if (gr % 2 == 1 && gc % 2 == 1) continue;
            if (gr % 2 == 0 && gc % 2 == 0) continue;
            int tileR = gr * cellSize, tileC = gc * cellSize;
            if (tileR >= rows || tileC >= cols) continue;
            if (map[tileR][tileC] != HELL_WALL) continue;
            carveArea(map, gr, gc, rows, cols);
        }
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 检查地狱迷宫中某地形是否可通行
     */
    public static boolean checkCanPass(int terrain) {
        return terrain != HELL_WALL;
    }

    public int getEntranceX() { return entranceX; }
    public int getEntranceY() { return entranceY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }
    /** 出口房间中心坐标（迷宫内部，用于 BOSS 守出口） */
    public int getExitRoomX() { return exitRoomX; }
    public int getExitRoomY() { return exitRoomY; }
    public int getFloor() { return floor; }

    /** 获取第4层BOSS区域中心坐标 [x0,y0, x1,y1, x2,y2] */
    public int[] getBossArenaCenters() { return bossArenaCenters; }
}

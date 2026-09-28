package com.game.dream.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 迷宫地图生成器 - 使用递归回溯算法(DFS)生成迷宫
 *
 * 网格模型:
 *   - grid 中奇数位置 (1,3,5...) = 房间, 偶数位置 (0,2,4...) = 墙壁
 *   - 每个 grid 位置映射到 cellSize x cellSize 个 tile
 *   - 打通墙壁时只挖掉该 grid 位置对应的 tile 区域, 保证通道连续
 */
public class MazeGenerator {

    // 迷宫专用地形常量
    public static final int MAZE_WALL = 200;
    public static final int MAZE_FLOOR = 201;
    public static final int MAZE_ENTRANCE = 202;
    public static final int MAZE_EXIT = 203;

    private int mapWidth;   // 像素宽度
    private int mapHeight;  // 像素高度
    private int tileSize;   // 每格像素
    private Random random;

    // 迷宫格子尺寸: 通道宽度 = cellSize * tileSize
    private int cellSize = 8; // 8 tiles * 20px = 160px 通道宽度

    // 入口/出口坐标 (像素坐标)
    private int entranceX;
    private int entranceY;
    private int exitX;
    private int exitY;

    public MazeGenerator(int mapWidth, int mapHeight, int tileSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        // 随机种子：每次生成不同的迷宫布局
        this.random = new Random();
    }

    // 边方向常量: 0=上, 1=下, 2=左, 3=右
    private static final int EDGE_TOP = 0;
    private static final int EDGE_BOTTOM = 1;
    private static final int EDGE_LEFT = 2;
    private static final int EDGE_RIGHT = 3;

    /**
     * 生成迷宫地图
     */
    public int[][] generateMap() {
        int cols = mapWidth / tileSize;   // 500
        int rows = mapHeight / tileSize;  // 500

        // 初始化全部为墙壁
        int[][] map = new int[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                map[y][x] = MAZE_WALL;
            }
        }

        // 迷宫网格:
        // 奇数 grid 位置 = 房间, 偶数 grid 位置 = 墙壁/柱子
        // grid 尺寸: 需要奇数个位置, 且首尾为墙壁(偶数边界)
        int gridCols = cols / cellSize;
        int gridRows = rows / cellSize;
        if (gridCols % 2 == 0) gridCols--;
        if (gridRows % 2 == 0) gridRows--;

        // === 随机选择入口/出口所在的边（不同边，增加难度） ===
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
        carveArea(map, startRow, startCol, cellSize);
        stack.add(new int[]{startRow, startCol});

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!stack.isEmpty()) {
            int[] current = stack.get(stack.size() - 1);
            int cr = current[0];
            int cc = current[1];

            // 找未访问的房间邻居 (跳2格: 越过中间的墙壁)
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
                int nr = next[0];
                int nc = next[1];
                int dr = next[2];
                int dc = next[3];

                // 挖掉两个房间之间的墙壁 (墙壁grid位置 = 中间格)
                int wallRow = cr + dr;
                int wallCol = cc + dc;
                carveArea(map, wallRow, wallCol, cellSize);

                // 挖掉目标房间
                carveArea(map, nr, nc, cellSize);

                visited[nr][nc] = true;
                stack.add(new int[]{nr, nc});
            }
        }

        // 随机打通额外墙壁, 创造多条路径
        addExtraPassages(map, gridRows, gridCols);

        // === 挖出入口通道并标记（根据随机边） ===
        carveEdgePassage(map, entranceEdge, startRow, startCol, rows, cols, MAZE_ENTRANCE);
        setEdgePortalCoords(entranceEdge, startRow, startCol, rows, cols, true);

        // === 挖出出口通道并标记 ===
        carveEdgePassage(map, exitEdge, endRow, endCol, rows, cols, MAZE_EXIT);
        setEdgePortalCoords(exitEdge, endRow, endCol, rows, cols, false);

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
                // 从顶部到房间
                for (int r = 0; r <= roomTileR; r++) {
                    for (int c = 0; c < cellSize; c++) {
                        int tx = roomTileC + c;
                        if (r < rows && tx < cols) map[r][tx] = MAZE_FLOOR;
                    }
                }
                for (int c = 0; c < cellSize; c++) {
                    int tx = roomTileC + c;
                    if (tx < cols) map[0][tx] = markerTerrain;
                }
                break;
            }
            case EDGE_BOTTOM: {
                // 从房间到底部
                for (int r = roomTileR; r < rows; r++) {
                    for (int c = 0; c < cellSize; c++) {
                        int tx = roomTileC + c;
                        if (r >= 0 && tx < cols) map[r][tx] = MAZE_FLOOR;
                    }
                }
                for (int c = 0; c < cellSize; c++) {
                    int tx = roomTileC + c;
                    if (tx < cols) map[rows - 1][tx] = markerTerrain;
                }
                break;
            }
            case EDGE_LEFT: {
                // 从左侧到房间
                for (int c = 0; c <= roomTileC; c++) {
                    for (int r = 0; r < cellSize; r++) {
                        int ty = roomTileR + r;
                        if (ty < rows && c < cols) map[ty][c] = MAZE_FLOOR;
                    }
                }
                for (int r = 0; r < cellSize; r++) {
                    int ty = roomTileR + r;
                    if (ty < rows) map[ty][0] = markerTerrain;
                }
                break;
            }
            default: { // EDGE_RIGHT
                // 从房间到右侧
                for (int c = roomTileC; c < cols; c++) {
                    for (int r = 0; r < cellSize; r++) {
                        int ty = roomTileR + r;
                        if (ty < rows && c >= 0) map[ty][c] = MAZE_FLOOR;
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
        }
    }

    /**
     * 将一个 grid 位置对应的 tile 区域全部挖空为地板
     * gridPos (gr, gc) -> tile rows [gr*cellSize .. (gr+1)*cellSize-1]
     *                    -> tile cols [gc*cellSize .. (gc+1)*cellSize-1]
     */
    private void carveArea(int[][] map, int gridRow, int gridCol, int cellSize) {
        int rows = map.length;
        int cols = map[0].length;
        int startR = gridRow * cellSize;
        int startC = gridCol * cellSize;
        for (int r = 0; r < cellSize; r++) {
            for (int c = 0; c < cellSize; c++) {
                int ty = startR + r;
                int tx = startC + c;
                if (ty >= 0 && ty < rows && tx >= 0 && tx < cols) {
                    map[ty][tx] = MAZE_FLOOR;
                }
            }
        }
    }

    /**
     * 随机打通额外墙壁, 创造多条路径
     * 只针对确实是墙壁的 grid 位置, 且只挖墙壁区域本身
     */
    private void addExtraPassages(int[][] map, int gridRows, int gridCols) {
        int rows = map.length;
        int cols = map[0].length;
        int extraPassages = (gridRows * gridCols) / 6; // 约 16.7% 的额外通道

        for (int i = 0; i < extraPassages; i++) {
            // 随机选一个 grid 位置
            int gr = random.nextInt(gridRows - 2) + 1;
            int gc = random.nextInt(gridCols - 2) + 1;

            // 只处理偶数位置 (墙壁), 奇数位置是房间(已经是地板)
            if (gr % 2 == 1 && gc % 2 == 1) continue; // 跳过房间
            if (gr % 2 == 0 && gc % 2 == 0) continue; // 跳过柱子(交叉点)

            // 检查这面墙壁是否还是 WALL (连接两个已通的房间才有意义)
            int tileR = gr * cellSize;
            int tileC = gc * cellSize;
            if (tileR < 0 || tileR >= rows || tileC < 0 || tileC >= cols) continue;
            if (map[tileR][tileC] != MAZE_WALL) continue;

            // 只挖墙壁本身的 tile 区域, 不扩展到相邻房间
            carveArea(map, gr, gc, cellSize);
        }
    }

    public int getEntranceX() { return entranceX; }
    public int getEntranceY() { return entranceY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }

    /**
     * 检查迷宫中某坐标是否可通行
     */
    public static boolean checkCanPass(int terrain) {
        return terrain != MAZE_WALL;
    }
}

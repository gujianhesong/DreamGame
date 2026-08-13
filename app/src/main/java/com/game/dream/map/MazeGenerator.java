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
    private int cellSize = 6; // 6 tiles * 20px = 120px 通道宽度

    // 入口/出口坐标 (像素坐标)
    private int entranceX;
    private int entranceY;
    private int exitX;
    private int exitY;

    public MazeGenerator(int mapWidth, int mapHeight, int tileSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.random = new Random(54321);
    }

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

        // DFS 递归回溯生成迷宫
        boolean[][] visited = new boolean[gridRows][gridCols];
        List<int[]> stack = new ArrayList<>();

        // 从房间 (1,1) 开始
        int startCol = 1;
        int startRow = 1;
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

        // === 设置入口 (顶部, 与起始房间对齐) ===
        // 起始房间 grid(1,1) -> tile col = cellSize
        int entranceCol = startCol * cellSize;
        entranceX = (entranceCol + cellSize / 2) * tileSize;
        entranceY = tileSize;
        // 从地图顶部到起始房间挖一条入口通道
        for (int r = 0; r < startRow * cellSize; r++) {
            for (int c = 0; c < cellSize; c++) {
                int tx = entranceCol + c;
                if (r >= 0 && r < rows && tx >= 0 && tx < cols) {
                    map[r][tx] = MAZE_FLOOR;
                }
            }
        }
        // 标记入口
        for (int c = 0; c < cellSize; c++) {
            int tx = entranceCol + c;
            if (tx < cols) {
                map[0][tx] = MAZE_ENTRANCE;
            }
        }

        // === 设置出口 (底部, 与最后可达房间对齐) ===
        int lastGridCol = gridCols - 2; // 最后一个奇数grid列
        int exitCol = lastGridCol * cellSize;
        exitX = (exitCol + cellSize / 2) * tileSize;
        exitY = (rows - 2) * tileSize;
        // 从最后房间到底部挖一条出口通道
        int lastRoomRow = (gridRows - 2); // 最后一个奇数grid行
        int exitStartRow = (lastRoomRow + 1) * cellSize; // 房间下方的墙壁边界
        for (int r = exitStartRow; r < rows; r++) {
            for (int c = 0; c < cellSize; c++) {
                int tx = exitCol + c;
                if (r >= 0 && r < rows && tx >= 0 && tx < cols) {
                    map[r][tx] = MAZE_FLOOR;
                }
            }
        }
        // 标记出口
        for (int c = 0; c < cellSize; c++) {
            int tx = exitCol + c;
            if (tx < cols) {
                map[rows - 1][tx] = MAZE_EXIT;
            }
        }

        return map;
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

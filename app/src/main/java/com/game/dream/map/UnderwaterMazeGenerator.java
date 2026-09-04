package com.game.dream.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 海底迷宫生成器 - 入口在左侧，出口在右侧
 *
 * 基于 MazeGenerator 的 DFS 递归回溯算法，修改入口/出口方向为左右分布
 * 地图尺寸: 10000x10000
 *
 * 网格模型:
 *   - grid 中奇数位置 (1,3,5...) = 房间, 偶数位置 (0,2,4...) = 墙壁
 *   - 每个 grid 位置映射到 cellSize x cellSize 个 tile
 *   - 打通墙壁时只挖掉该 grid 位置对应的 tile 区域, 保证通道连续
 */
public class UnderwaterMazeGenerator {

    // 海底迷宫专用地形常量 (复用迷宫地形)
    // 使用 MazeGenerator 中已定义的 MAZE_WALL/MAZE_FLOOR/MAZE_ENTRANCE/MAZE_EXIT

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

    public UnderwaterMazeGenerator(int mapWidth, int mapHeight, int tileSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileSize = tileSize;
        this.random = new Random(12345);
    }

    /**
     * 生成海底迷宫地图
     * 入口在左侧中间，出口在右侧中间
     */
    public int[][] generateMap() {
        int cols = mapWidth / tileSize;   // 500
        int rows = mapHeight / tileSize;  // 500

        // 初始化全部为墙壁
        int[][] map = new int[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                map[y][x] = MazeGenerator.MAZE_WALL;
            }
        }

        // 迷宫网格
        int gridCols = cols / cellSize;
        int gridRows = rows / cellSize;
        if (gridCols % 2 == 0) gridCols--;
        if (gridRows % 2 == 0) gridRows--;

        // DFS 递归回溯生成迷宫
        boolean[][] visited = new boolean[gridRows][gridCols];
        List<int[]> stack = new ArrayList<>();

        // 从左侧中间房间开始 (grid row = 中间奇数行, col = 1)
        int startRow = (gridRows / 2);
        if (startRow % 2 == 0) startRow++; // 确保是奇数(房间)
        int startCol = 1;

        visited[startRow][startCol] = true;
        carveArea(map, startRow, startCol, cellSize, rows, cols);
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

                // 挖掉两个房间之间的墙壁
                int wallRow = cr + dr;
                int wallCol = cc + dc;
                carveArea(map, wallRow, wallCol, cellSize, rows, cols);

                // 挖掉目标房间
                carveArea(map, nr, nc, cellSize, rows, cols);

                visited[nr][nc] = true;
                stack.add(new int[]{nr, nc});
            }
        }

        // 随机打通额外墙壁, 创造多条路径
        addExtraPassages(map, gridRows, gridCols, rows, cols);

        // === 设置入口 (左侧中间) ===
        int entranceRow = startRow * cellSize; // 与起始房间对齐
        entranceX = tileSize; // 左侧边缘
        entranceY = (entranceRow + cellSize / 2) * tileSize;

        // 从地图左边缘到起始房间挖一条入口通道
        for (int c = 0; c < startCol * cellSize; c++) {
            for (int r = 0; r < cellSize; r++) {
                int ty = entranceRow + r;
                int tx = c;
                if (ty >= 0 && ty < rows && tx >= 0 && tx < cols) {
                    map[ty][tx] = MazeGenerator.MAZE_FLOOR;
                }
            }
        }
        // 标记入口
        for (int r = 0; r < cellSize; r++) {
            int ty = entranceRow + r;
            if (ty < rows) {
                map[ty][0] = MazeGenerator.MAZE_ENTRANCE;
            }
        }

        // === 设置出口 (右侧中间) ===
        int lastGridCol = gridCols - 2; // 最后一个奇数grid列
        int exitGridRow = startRow;     // 与入口同一行
        // 确保出口行在有效范围内
        if (exitGridRow >= gridRows - 1) exitGridRow = gridRows - 2;
        if (exitGridRow % 2 == 0) exitGridRow--;

        int exitTileRow = exitGridRow * cellSize;
        exitX = (cols - 2) * tileSize; // 右侧边缘
        exitY = (exitTileRow + cellSize / 2) * tileSize;

        // 从最后房间到右边缘挖一条出口通道
        int exitStartCol = (lastGridCol + 1) * cellSize;
        for (int c = exitStartCol; c < cols; c++) {
            for (int r = 0; r < cellSize; r++) {
                int ty = exitTileRow + r;
                int tx = c;
                if (ty >= 0 && ty < rows && tx >= 0 && tx < cols) {
                    map[ty][tx] = MazeGenerator.MAZE_FLOOR;
                }
            }
        }
        // 标记出口
        for (int r = 0; r < cellSize; r++) {
            int ty = exitTileRow + r;
            if (ty < rows) {
                map[ty][cols - 1] = MazeGenerator.MAZE_EXIT;
            }
        }

        return map;
    }

    /**
     * 将一个 grid 位置对应的 tile 区域全部挖空为地板
     */
    private void carveArea(int[][] map, int gridRow, int gridCol, int cellSize, int rows, int cols) {
        int startR = gridRow * cellSize;
        int startC = gridCol * cellSize;
        for (int r = 0; r < cellSize; r++) {
            for (int c = 0; c < cellSize; c++) {
                int ty = startR + r;
                int tx = startC + c;
                if (ty >= 0 && ty < rows && tx >= 0 && tx < cols) {
                    map[ty][tx] = MazeGenerator.MAZE_FLOOR;
                }
            }
        }
    }

    /**
     * 随机打通额外墙壁, 创造多条路径
     */
    private void addExtraPassages(int[][] map, int gridRows, int gridCols, int rows, int cols) {
        int extraPassages = (gridRows * gridCols) / 6;

        for (int i = 0; i < extraPassages; i++) {
            int gr = random.nextInt(gridRows - 2) + 1;
            int gc = random.nextInt(gridCols - 2) + 1;

            if (gr % 2 == 1 && gc % 2 == 1) continue; // 跳过房间
            if (gr % 2 == 0 && gc % 2 == 0) continue; // 跳过柱子

            int tileR = gr * cellSize;
            int tileC = gc * cellSize;
            if (tileR < 0 || tileR >= rows || tileC < 0 || tileC >= cols) continue;
            if (map[tileR][tileC] != MazeGenerator.MAZE_WALL) continue;

            carveArea(map, gr, gc, cellSize, rows, cols);
        }
    }

    public int getEntranceX() { return entranceX; }
    public int getEntranceY() { return entranceY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }
}

package com.game.dream.utils;

import com.game.dream.GameEngine;
import com.game.dream.bean.MapInfo;
import com.game.dream.common.Constants;
import com.game.dream.figure.Player;
import com.game.dream.system.MapSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 大地图自动导航用的粗网格 A*，与玩家移动的地形阻挡一致。
 */
public final class MapPathfinder {

    /** 寻路用格子步长（单位：地图瓦片数，与玩家踩格一致） */
    private static final int STRIDE = 1;
    private static final int MAX_EXPAND = 450_000;

    private static final int[] NB_DX = {1, -1, 0, 0, 1, 1, -1, -1};
    private static final int[] NB_DY = {0, 0, 1, -1, 1, -1, 1, -1};
    private static final float[] NB_COST = {1f, 1f, 1f, 1f, 1.414f, 1.414f, 1.414f, 1.414f};

    private MapPathfinder() {
    }

    private static long blockKey(int bx, int by) {
        return ((long) bx << 32) | (by & 0xffffffffL);
    }

    private static int keyX(long key) {
        return (int) (key >> 32);
    }

    private static int keyY(long key) {
        return (int) key;
    }

    private static int playerSize() {
        Player player = GameEngine.getInstance().getPlayer();
        return player != null ? player.getSize() : 32;
    }

    private static boolean isBlockWalkable(int[][] map, int bx, int by, int blocksW, int blocksH) {
        if (bx < 0 || by < 0 || bx >= blocksW || by >= blocksH) {
            return false;
        }
        int ps = playerSize();
        float wx = MapWalkUtil.tileToPlayerCenterX(bx * STRIDE);
        float wy = MapWalkUtil.tileToPlayerCenterY(by * STRIDE, ps);
        return MapWalkUtil.isWorldPositionReachable(wx, wy);
    }

    private static int worldToBlockX(float worldX) {
        return (int) (worldX / Constants.TILE_SIZE) / STRIDE;
    }

    private static int worldToBlockY(float worldY) {
        int ps = playerSize();
        int footTileY = (int) ((worldY + ps / 2f) / Constants.TILE_SIZE);
        return footTileY / STRIDE;
    }

    /**
     * @return 世界坐标路点列表（含终点）；不可达返回 null
     */
    public static List<float[]> findPath(float startWx, float startWy, float endWx, float endWy) {
        MapInfo info = MapSystem.getInstance().getCurMapInfo();
        if (info == null || info.getMapData() == null) {
            return null;
        }
        int[][] map = info.getMapData();
        int tilesW = map[0].length;
        int tilesH = map.length;
        int blocksW = (tilesW + STRIDE - 1) / STRIDE;
        int blocksH = (tilesH + STRIDE - 1) / STRIDE;

        int startBx = worldToBlockX(startWx);
        int startBy = worldToBlockY(startWy);
        int endBx = worldToBlockX(endWx);
        int endBy = worldToBlockY(endWy);

        if (!MapWalkUtil.isWorldPositionReachable(endWx, endWy)) {
            return null;
        }
        if (!isBlockWalkable(map, endBx, endBy, blocksW, blocksH)) {
            return null;
        }

        if (startBx == endBx && startBy == endBy) {
            List<float[]> single = new ArrayList<>(1);
            single.add(new float[]{endWx, endWy});
            return single;
        }

        if (!isBlockWalkable(map, startBx, startBy, blocksW, blocksH)) {
            int[] near = findNearestWalkableBlock(map, startBx, startBy, blocksW, blocksH, 8);
            if (near == null) {
                return null;
            }
            startBx = near[0];
            startBy = near[1];
        }

        final int goalBx = endBx;
        final int goalBy = endBy;

        HashMap<Long, Integer> gScore = new HashMap<>();
        HashMap<Long, Long> cameFrom = new HashMap<>();
        PriorityQueue<long[]> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n[2]));

        long startKey = blockKey(startBx, startBy);
        gScore.put(startKey, 0);
        open.add(new long[]{startBx, startBy, (long) (heuristic(startBx, startBy, goalBx, goalBy) * 1000)});

        int expanded = 0;
        while (!open.isEmpty() && expanded < MAX_EXPAND) {
            long[] current = open.poll();
            int cx = (int) current[0];
            int cy = (int) current[1];
            if (cx == goalBx && cy == goalBy) {
                return buildPath(cameFrom, startBx, startBy, goalBx, goalBy, endWx, endWy);
            }
            long cKey = blockKey(cx, cy);
            int gCur = gScore.getOrDefault(cKey, Integer.MAX_VALUE);
            expanded++;

            for (int i = 0; i < NB_DX.length; i++) {
                int nx = cx + NB_DX[i];
                int ny = cy + NB_DY[i];
                if (!isBlockWalkable(map, nx, ny, blocksW, blocksH)) {
                    continue;
                }
                if (NB_DX[i] != 0 && NB_DY[i] != 0) {
                    if (!isBlockWalkable(map, cx + NB_DX[i], cy, blocksW, blocksH)
                            || !isBlockWalkable(map, cx, cy + NB_DY[i], blocksW, blocksH)) {
                        continue;
                    }
                }
                long nKey = blockKey(nx, ny);
                int stepCost = (int) (NB_COST[i] * 1000);
                int tentative = gCur + stepCost;
                if (tentative >= gScore.getOrDefault(nKey, Integer.MAX_VALUE)) {
                    continue;
                }
                cameFrom.put(nKey, cKey);
                gScore.put(nKey, tentative);
                float f = tentative + heuristic(nx, ny, goalBx, goalBy) * 1000;
                open.add(new long[]{nx, ny, (long) f});
            }
        }
        return null;
    }

    private static float heuristic(int bx, int by, int gx, int gy) {
        int dx = Math.abs(gx - bx);
        int dy = Math.abs(gy - by);
        return (dx + dy) * STRIDE * Constants.TILE_SIZE;
    }

    private static int[] findNearestWalkableBlock(int[][] map, int bx, int by, int blocksW, int blocksH, int maxRadius) {
        if (isBlockWalkable(map, bx, by, blocksW, blocksH)) {
            return new int[]{bx, by};
        }
        for (int r = 1; r <= maxRadius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) {
                        continue;
                    }
                    int nx = bx + dx;
                    int ny = by + dy;
                    if (isBlockWalkable(map, nx, ny, blocksW, blocksH)) {
                        return new int[]{nx, ny};
                    }
                }
            }
        }
        return null;
    }

    private static List<float[]> buildPath(HashMap<Long, Long> cameFrom,
                                           int startBx, int startBy, int goalBx, int goalBy,
                                           float endWx, float endWy) {
        ArrayList<int[]> blocks = new ArrayList<>();
        long cur = blockKey(goalBx, goalBy);
        blocks.add(new int[]{goalBx, goalBy});
        while (cur != blockKey(startBx, startBy)) {
            Long prev = cameFrom.get(cur);
            if (prev == null) {
                return null;
            }
            blocks.add(new int[]{keyX(prev), keyY(prev)});
            cur = prev;
        }
        Collections.reverse(blocks);

        int ps = playerSize();
        List<float[]> waypoints = new ArrayList<>(blocks.size());
        for (int i = 0; i < blocks.size() - 1; i++) {
            int[] b = blocks.get(i);
            waypoints.add(new float[]{
                    MapWalkUtil.tileToPlayerCenterX(b[0]),
                    MapWalkUtil.tileToPlayerCenterY(b[1], ps)
            });
        }
        waypoints.add(new float[]{endWx, endWy});
        return simplifyCollinear(waypoints);
    }

    private static List<float[]> simplifyCollinear(List<float[]> path) {
        if (path.size() <= 2) {
            return path;
        }
        ArrayList<float[]> out = new ArrayList<>();
        out.add(path.get(0));
        for (int i = 1; i < path.size() - 1; i++) {
            float[] a = out.get(out.size() - 1);
            float[] b = path.get(i);
            float[] c = path.get(i + 1);
            float abx = b[0] - a[0];
            float aby = b[1] - a[1];
            float bcx = c[0] - b[0];
            float bcy = c[1] - b[1];
            float cross = abx * bcy - aby * bcx;
            if (Math.abs(cross) > 4f) {
                out.add(b);
            }
        }
        out.add(path.get(path.size() - 1));
        return out;
    }
}
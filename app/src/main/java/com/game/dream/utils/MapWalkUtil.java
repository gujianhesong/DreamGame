package com.game.dream.utils;

import com.game.dream.GameEngine;
import com.game.dream.bean.MapInfo;
import com.game.dream.common.Constants;
import com.game.dream.figure.Player;
import com.game.dream.map.MapGenerator;
import com.game.dream.map.MazeGenerator;
import com.game.dream.system.MapSystem;

/**
 * 判断世界坐标是否处于可行走区域（与 {@link Player} 脚底格子判定一致）。
 */
public final class MapWalkUtil {

    private MapWalkUtil() {
    }

    public static boolean isWorldPositionReachable(float worldX, float worldY) {
        MapInfo info = MapSystem.getInstance().getCurMapInfo();
        if (info == null || info.getMapData() == null) {
            return false;
        }
        int[][] map = info.getMapData();
        int mapW = info.getMapWidth();
        int mapH = info.getMapHeight();
        int tile = Constants.TILE_SIZE;

        Player player = GameEngine.getInstance().getPlayer();
        int size = player != null ? player.getSize() : 32;
        float half = size / 2f;

        if (worldX < half || worldY < half || worldX > mapW - half || worldY > mapH - half) {
            return false;
        }

        int gridX = (int) (worldX / tile);
        int gridY = (int) ((worldY + half) / tile);
        return isTilePassable(map, gridX, gridY);
    }

    public static boolean isTilePassable(int[][] map, int gridX, int gridY) {
        if (gridY < 0 || gridX < 0 || gridY >= map.length || gridX >= map[0].length) {
            return false;
        }
        int terrain = map[gridY][gridX];
        if (terrain == MazeGenerator.MAZE_WALL) {
            return false;
        }
        return MapGenerator.checkCanPass(map, gridX, gridY);
    }

    /** 将该地形格转换为角色中心点世界坐标（脚底落在格心）。 */
    public static float tileToPlayerCenterX(int tileX) {
        return tileX * Constants.TILE_SIZE + Constants.TILE_SIZE / 2f;
    }

    public static float tileToPlayerCenterY(int tileY, int playerSize) {
        return tileY * Constants.TILE_SIZE + Constants.TILE_SIZE / 2f - playerSize / 2f;
    }
}
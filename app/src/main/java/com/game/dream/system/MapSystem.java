package com.game.dream.system;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Pair;

import com.game.dream.bean.MapInfo;
import com.game.dream.map.MapGenerator;
import com.game.dream.map.MapRenderer;
import com.game.dream.map.MazeGenerator;
import com.game.dream.map.MazeRenderer;
import com.game.dream.map.VillageRenderer;
import com.game.dream.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MapSystem {

    private static MapSystem instance = new MapSystem();

    public static MapSystem getInstance() {
        return instance;
    }

    private MapSystem() {
        initMapList();
    }

    // Map dimensions
    public static final int MAP_WIDTH = 10000;
    public static final int MAP_HEIGHT = 10000;

    private static final int BORN_MAP_ID = 1001;
    private static final int MAZE_MAP_ID = 1002;

    // Map data
    private int[][] mapData; // 0=plain, 1=grassland, 2=forest, 3=lake, 4=snow, 5=swamp, 6=lava
    // Map generator
    private MapGenerator mapGenerator;
    private MazeGenerator mazeGenerator;
    // Map renderer (extracted to separate class)
    private MapRenderer mapRenderer;
    private MazeRenderer mazeRenderer;
    private VillageRenderer villageRenderer;

    private List<MapInfo> mapInfoList = new ArrayList<>();
    private MapInfo curMapInfo;
    private int currentMapId = BORN_MAP_ID;

    public void loadMap(int mapId) {
        MapInfo findMap = null;
        for (MapInfo mapInfo : mapInfoList) {
            if (mapInfo.getMapId() == mapId) {
                findMap = mapInfo;
            }
        }
        if (findMap != null) {
            curMapInfo = findMap;
            currentMapId = mapId;
            // 同步更新角色记录的地图ID, 确保存档能保存当前地图
            RoleSystem.getInstance().getRoleInfo().setMapId(mapId);

            if (mapId == MAZE_MAP_ID) {
                // 迷宫地图
                mazeGenerator = new MazeGenerator(findMap.getMapWidth(), findMap.getMapHeight(), TILE_SIZE);
                mapData = mazeGenerator.generateMap();
                curMapInfo.setMapData(mapData);
                mazeRenderer = new MazeRenderer(mapData, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);
                // 清除村庄渲染器
                mapRenderer = null;
                villageRenderer = null;
                // 初始化迷宫对象
                MazeSystem.getInstance().initMazeObjects(mapData, mazeGenerator);
            } else {
                // 普通地图 (清溪村)
                mapGenerator = new MapGenerator(findMap.getMapWidth(), findMap.getMapHeight(), TILE_SIZE);
                mapData = mapGenerator.generateMap();
                curMapInfo.setMapData(mapData);
                mapRenderer = new MapRenderer(mapData, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);
                villageRenderer = new VillageRenderer();
                villageRenderer.initVillage(MAP_WIDTH / 2, MAP_WIDTH / 2, MAP_WIDTH, MAP_HEIGHT);
                // 清除迷宫渲染器
                mazeRenderer = null;
                mazeGenerator = null;

                // Mark village houses as non-walkable in the map array
                Rect villageBounds = villageRenderer.getVillageBounds();
                for (int i = villageBounds.left; i <= villageBounds.right; i += TILE_SIZE) {
                    for (int j = villageBounds.top; j <= villageBounds.bottom; j += TILE_SIZE) {
                        mapData[i/TILE_SIZE][j/TILE_SIZE] = MapGenerator.VILLAGE_CAN_PASS;
                    }
                }
                int tileSize = TILE_SIZE;
                for (Rect obs : villageRenderer.getObstacles()) {
                    int startCol = obs.left / tileSize;
                    int endCol = obs.right / tileSize;
                    int startRow = obs.top / tileSize;
                    int endRow = obs.bottom / tileSize;

                    for (int r = startRow; r <= endRow; r++) {
                        for (int c = startCol; c <= endCol; c++) {
                            if (r >= 0 && r < curMapInfo.getMapHeight() && c >= 0 && c < curMapInfo.getMapWidth()) {
                                mapData[r][c] = MapGenerator.VILLAGE_NO_PASS;
                            }
                        }
                    }
                }
            }
        }
    }

    private void initMapList() {
        mapInfoList.add(new MapInfo(1001, "清溪", 10000, 10000));
        mapInfoList.add(new MapInfo(1002, "迷雾迷宫", 10000, 10000));
    }

    public MapInfo getBornMap() {
        MapInfo bornMap = null;
        for (MapInfo mapInfo : mapInfoList) {
            if (mapInfo.getMapId() == BORN_MAP_ID) {
                bornMap = mapInfo;
            }
        }
        return bornMap;
    }

    private List<MapInfo> getMapList() {
        return mapInfoList;
    }

    public Pair<Integer, Integer> getStartPosition() {
        // Find a valid starting position (not on lake or lava)
        int startX = MAP_WIDTH / TILE_SIZE / 2;
        int startY = MAP_HEIGHT / TILE_SIZE / 2;

        // Search for a valid position near the center
        boolean foundValidPosition = false;
        for (int radius = 0; radius < 50 && !foundValidPosition; radius++) {
            for (int dy = -radius; dy <= radius && !foundValidPosition; dy++) {
                for (int dx = -radius; dx <= radius && !foundValidPosition; dx++) {
                    int checkX = startX + dx;
                    int checkY = startY + dy;

                    if (checkX >= 0 && checkX < mapData[0].length && checkY >= 0 && checkY < mapData.length) {
                        int terrain = mapData[checkY][checkX];
                        if (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA && terrain != MapGenerator.VILLAGE_NO_PASS) {
                            startX = checkX;
                            startY = checkY;
                            foundValidPosition = true;
                        }
                    }
                }
            }
        }
        return new Pair<>(startX, startY);
    }

    public Pair<Integer, Integer> getMapXY(int posX, int posY) {
        int mapX = posX * TILE_SIZE + TILE_SIZE / 2;
        int mapY = posY * TILE_SIZE + TILE_SIZE / 2;
        return new Pair<>(mapX, mapY);
    }

    public MapInfo getCurMapInfo() {
        return curMapInfo;
    }

    public void cleanup() {
        if (mapRenderer != null) {
            mapRenderer.cleanup();
        }
        if (mazeRenderer != null) {
            mazeRenderer.cleanup();
        }
    }

    public void render(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        if (currentMapId == MAZE_MAP_ID) {
            // 迷宫地图渲染
            if (mazeRenderer != null) {
                mazeRenderer.draw(canvas, cameraX, cameraY, screenWidth, screenHeight);
            }
        } else {
            // 普通地图渲染
            if (mapRenderer != null) {
                mapRenderer.draw(canvas, cameraX, cameraY, screenWidth, screenHeight);
            }
            if (villageRenderer != null) {
                villageRenderer.draw(canvas, cameraX, cameraY);
            }
        }
    }

    /**
     * 检查坐标是否在村庄安全区内（供 Enemy 调用）
     */
    public boolean isLocationSafe(float x, float y) {
        // 迷宫中没有安全区
        if (currentMapId == MAZE_MAP_ID) {
            return false;
        }
        if (villageRenderer == null || villageRenderer.getVillageBounds() == null) {
            return false;
        }
        Rect bounds = villageRenderer.getVillageBounds();
        int padding = 20; // 留一点缓冲距离
        return x >= bounds.left - padding && x <= bounds.right + padding &&
                y >= bounds.top - padding && y <= bounds.bottom + padding;
    }

    public int getCurrentMapId() {
        return currentMapId;
    }

    public MazeGenerator getMazeGenerator() {
        return mazeGenerator;
    }
}

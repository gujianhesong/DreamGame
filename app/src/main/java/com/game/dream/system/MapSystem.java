package com.game.dream.system;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.game.dream.bean.MapInfo;
import com.game.dream.map.JinlingCityRenderer;
import com.game.dream.map.JinlingMapGenerator;
import com.game.dream.map.MapGenerator;
import com.game.dream.map.MapRenderer;
import com.game.dream.map.MazeGenerator;
import com.game.dream.map.MazeRenderer;
import com.game.dream.map.VillageRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapSystem {
    public static interface OnLoadMapCallback {
        void onLoadMapFinish(int mapId, int[][] mapData);
    }

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

    // 金陵地图尺寸
    public static final int JINLING_MAP_WIDTH = 60000;
    public static final int JINLING_MAP_HEIGHT = 60000;

    public static final int MAP_ID_QING_XI = 1001; //清溪
    public static final int MAP_ID_JIN_LING = 1002; //金陵

    public static final int MAP_ID_QING_XI_MAZE = 2001; //清溪迷宫

    // Map data
    private int[][] mapData; // 0=plain, 1=grassland, 2=forest, 3=lake, 4=snow, 5=swamp, 6=lava
    // Map generator
    private MapGenerator mapGenerator;
    private MazeGenerator mazeGenerator;
    // Map renderer (extracted to separate class)
    private MapRenderer mapRenderer;
    private MazeRenderer mazeRenderer;
    private VillageRenderer villageRenderer;

    // 金陵地图专用渲染器
    private JinlingCityRenderer jinlingCityRenderer;
    private List<VillageRenderer> additionalVillageRenderers = new ArrayList<>();

    private List<MapInfo> mapInfoList = new ArrayList<>();
    private MapInfo curMapInfo;
    private int currentMapId = MAP_ID_QING_XI;

    public void loadMapAsync(int mapId, OnLoadMapCallback callback) {
        new Thread(() -> {
            int[][] mapData;

            if (mapId == MAP_ID_JIN_LING) {
                //生成金陵地图数据
                mapData = MapSystem.getInstance().generateJinlingMapData();
            } else {
                loadMap(mapId);
                mapData = this.mapData;
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if(callback != null){
                    // 如果是异步加载的金陵地图，应用预生成的数据
                    if (mapId == MAP_ID_JIN_LING && mapData != null) {
                        MapSystem.getInstance().loadJinlingFromData(mapData);
                    }

                    callback.onLoadMapFinish(mapId, mapData);
                }
            });
        }).start();
    }

    private void loadMap(int mapId) {
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

            if (mapId == MAP_ID_QING_XI_MAZE) {
                // 迷宫地图
                mazeGenerator = new MazeGenerator(findMap.getMapWidth(), findMap.getMapHeight(), TILE_SIZE);
                mapData = mazeGenerator.generateMap();
                curMapInfo.setMapData(mapData);
                mazeRenderer = new MazeRenderer(mapData, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);
                // 清除村庄渲染器
                mapRenderer = null;
                villageRenderer = null;
                jinlingCityRenderer = null;
                additionalVillageRenderers.clear();
                // 初始化迷宫对象
                MazeSystem.getInstance().initMazeObjects(mapData, mazeGenerator);
            } else if (mapId == MAP_ID_JIN_LING) {
                // 金陵大地图
                JinlingMapGenerator jinlingGen = new JinlingMapGenerator(TILE_SIZE);
                mapData = jinlingGen.generateMap();
                curMapInfo.setMapData(mapData);
                mapRenderer = new MapRenderer(mapData, JINLING_MAP_WIDTH, JINLING_MAP_HEIGHT, TILE_SIZE);

                // 初始化金陵主城渲染器
                jinlingCityRenderer = new JinlingCityRenderer();
                jinlingCityRenderer.initCity();

                // 初始化四角村庄渲染器
                additionalVillageRenderers.clear();
                for (int i = 0; i < 4; i++) {
                    int[] bounds = JinlingMapGenerator.getVillageBounds(i);
                    VillageRenderer vr = new VillageRenderer();
                    vr.initVillageWithBounds(bounds[0], bounds[1],
                            bounds[2] - bounds[0], bounds[3] - bounds[1]);
                    additionalVillageRenderers.add(vr);
                }

                // 标记村庄建筑为不可通行
                for (VillageRenderer vr : additionalVillageRenderers) {
                    Rect vBounds = vr.getVillageBounds();
                    if (vBounds != null) {
                        for (int i = vBounds.left; i <= vBounds.right; i += TILE_SIZE) {
                            for (int j = vBounds.top; j <= vBounds.bottom; j += TILE_SIZE) {
                                int tx = i / TILE_SIZE, ty = j / TILE_SIZE;
                                if (ty >= 0 && ty < mapData.length && tx >= 0 && tx < mapData[0].length) {
                                    mapData[ty][tx] = MapGenerator.VILLAGE_CAN_PASS;
                                }
                            }
                        }
                        for (Rect obs : vr.getObstacles()) {
                            int startCol = obs.left / TILE_SIZE;
                            int endCol = obs.right / TILE_SIZE;
                            int startRow = obs.top / TILE_SIZE;
                            int endRow = obs.bottom / TILE_SIZE;
                            for (int r = startRow; r <= endRow; r++) {
                                for (int c = startCol; c <= endCol; c++) {
                                    if (r >= 0 && r < curMapInfo.getMapHeight() / TILE_SIZE
                                            && c >= 0 && c < curMapInfo.getMapWidth() / TILE_SIZE) {
                                        mapData[r][c] = MapGenerator.VILLAGE_NO_PASS;
                                    }
                                }
                            }
                        }
                    }
                }

                // 标记主城建筑为不可通行
                for (Rect obs : jinlingCityRenderer.getObstacles()) {
                    int startCol = obs.left / TILE_SIZE;
                    int endCol = obs.right / TILE_SIZE;
                    int startRow = obs.top / TILE_SIZE;
                    int endRow = obs.bottom / TILE_SIZE;
                    for (int r = startRow; r <= endRow; r++) {
                        for (int c = startCol; c <= endCol; c++) {
                            int maxTiles = JINLING_MAP_WIDTH / TILE_SIZE;
                            if (r >= 0 && r < JINLING_MAP_HEIGHT / TILE_SIZE
                                    && c >= 0 && c < maxTiles) {
                                mapData[r][c] = MapGenerator.VILLAGE_NO_PASS;
                            }
                        }
                    }
                }

                // 清除迷宫渲染器
                mazeRenderer = null;
                mazeGenerator = null;
                villageRenderer = null;
            } else {
                // 普通地图 (清溪村)
                mapGenerator = new MapGenerator(findMap.getMapWidth(), findMap.getMapHeight(), TILE_SIZE);
                mapData = mapGenerator.generateMap();
                curMapInfo.setMapData(mapData);
                mapRenderer = new MapRenderer(mapData, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE);
                villageRenderer = new VillageRenderer();
                villageRenderer.initVillage(MAP_WIDTH / 2, MAP_WIDTH / 2, MAP_WIDTH, MAP_HEIGHT);
                // 清除迷宫渲染器和金陵渲染器
                mazeRenderer = null;
                mazeGenerator = null;
                jinlingCityRenderer = null;
                additionalVillageRenderers.clear();

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
        mapInfoList.add(new MapInfo(MAP_ID_QING_XI, "清溪", 10000, 10000, new Pair<>(3520, 5430)));
        mapInfoList.add(new MapInfo(MAP_ID_JIN_LING, "金陵", JINLING_MAP_WIDTH, JINLING_MAP_HEIGHT, new Pair<>(30000, 35430)));

        mapInfoList.add(new MapInfo(MAP_ID_QING_XI_MAZE, "清溪地下迷宫", 10000, 10000, null));
    }

    public MapInfo getBornMap() {
        MapInfo bornMap = null;
        for (MapInfo mapInfo : mapInfoList) {
            if (mapInfo.getMapId() == MAP_ID_QING_XI) {
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
        if (currentMapId == MAP_ID_QING_XI_MAZE) {
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
            // 金陵主城渲染
            if (jinlingCityRenderer != null) {
                jinlingCityRenderer.draw(canvas, cameraX, cameraY, screenWidth, screenHeight);
            }
            // 金陵四角村庄渲染
            for (VillageRenderer vr : additionalVillageRenderers) {
                vr.draw(canvas, cameraX, cameraY);
            }
        }
    }

    /**
     * 检查坐标是否在村庄安全区内（供 Enemy 调用）
     */
    public boolean isLocationSafe(float x, float y) {
        // 迷宫中没有安全区
        if (currentMapId == MAP_ID_QING_XI_MAZE) {
            return false;
        }
        // 清溪村安全区
        if (villageRenderer != null && villageRenderer.getVillageBounds() != null) {
            Rect bounds = villageRenderer.getVillageBounds();
            int padding = 20;
            if (x >= bounds.left - padding && x <= bounds.right + padding &&
                    y >= bounds.top - padding && y <= bounds.bottom + padding) {
                return true;
            }
        }
        // 金陵主城安全区
        if (jinlingCityRenderer != null && jinlingCityRenderer.getCityBounds() != null) {
            Rect bounds = jinlingCityRenderer.getCityBounds();
            int padding = 20;
            if (x >= bounds.left - padding && x <= bounds.right + padding &&
                    y >= bounds.top - padding && y <= bounds.bottom + padding) {
                return true;
            }
        }
        // 金陵四角村庄安全区
        for (VillageRenderer vr : additionalVillageRenderers) {
            if (vr.getVillageBounds() != null) {
                Rect bounds = vr.getVillageBounds();
                int padding = 20;
                if (x >= bounds.left - padding && x <= bounds.right + padding &&
                        y >= bounds.top - padding && y <= bounds.bottom + padding) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getCurrentMapId() {
        return currentMapId;
    }

    /**
     * 是否迷宫地图
     * @return
     */
    public boolean isCurrentMazaMap() {
        return currentMapId >= 2000 && currentMapId < 3000;
    }

    /**
     * 获取地图名称
     * @param mapId
     * @return
     */
    public String getMapName(int mapId){
        for(MapInfo mapInfo : mapInfoList){
            if(mapInfo.getMapId() == mapId){
                return mapInfo.getMapName();
            }
        }
        return "未知地图";
    }

    public MazeGenerator getMazeGenerator() {
        return mazeGenerator;
    }

    /**
     * 后台生成金陵地图数据（耗时操作，在后台线程调用）
     */
    public int[][] generateJinlingMapData() {
        JinlingMapGenerator jinlingGen = new JinlingMapGenerator(TILE_SIZE);
        return jinlingGen.generateMap();
    }

    /**
     * 使用预生成的地图数据加载金陵地图（快速操作，可在主线程调用）
     */
    public void loadJinlingFromData(int[][] generatedMapData) {
        currentMapId = MAP_ID_JIN_LING;
        RoleSystem.getInstance().getRoleInfo().setMapId(MAP_ID_JIN_LING);

        // 切换 curMapInfo 到金陵地图对象（确保宽高为 60000x60000）
        for (MapInfo mapInfo : mapInfoList) {
            if (mapInfo.getMapId() == MAP_ID_JIN_LING) {
                curMapInfo = mapInfo;
                break;
            }
        }

        mapData = generatedMapData;
        curMapInfo.setMapData(mapData);
        mapRenderer = new MapRenderer(mapData, JINLING_MAP_WIDTH, JINLING_MAP_HEIGHT, TILE_SIZE);

        // 初始化金陵主城渲染器
        jinlingCityRenderer = new JinlingCityRenderer();
        jinlingCityRenderer.initCity();

        // 初始化四角村庄渲染器
        additionalVillageRenderers.clear();
        for (int i = 0; i < 4; i++) {
            int[] bounds = JinlingMapGenerator.getVillageBounds(i);
            VillageRenderer vr = new VillageRenderer();
            vr.initVillageWithBounds(bounds[0], bounds[1],
                    bounds[2] - bounds[0], bounds[3] - bounds[1]);
            additionalVillageRenderers.add(vr);
        }

        // 并行标记村庄和主城建筑为不可通行（5个独立区域，各一个线程）
        int numWorkers = additionalVillageRenderers.size() + 1;
        ExecutorService obstacleExecutor = Executors.newFixedThreadPool(numWorkers);
        List<Callable<Void>> obstacleTasks = new ArrayList<>();

        // 每个村庄一个线程（区域互不重叠，安全并行）
        for (VillageRenderer vr : additionalVillageRenderers) {
            obstacleTasks.add(() -> {
                Rect vBounds = vr.getVillageBounds();
                if (vBounds != null) {
                    for (int i = vBounds.left; i <= vBounds.right; i += TILE_SIZE) {
                        for (int j = vBounds.top; j <= vBounds.bottom; j += TILE_SIZE) {
                            int tx = i / TILE_SIZE, ty = j / TILE_SIZE;
                            if (ty >= 0 && ty < mapData.length && tx >= 0 && tx < mapData[0].length) {
                                mapData[ty][tx] = MapGenerator.VILLAGE_CAN_PASS;
                            }
                        }
                    }
                    for (Rect obs : vr.getObstacles()) {
                        int startCol = obs.left / TILE_SIZE;
                        int endCol = obs.right / TILE_SIZE;
                        int startRow = obs.top / TILE_SIZE;
                        int endRow = obs.bottom / TILE_SIZE;
                        for (int r = startRow; r <= endRow; r++) {
                            for (int c = startCol; c <= endCol; c++) {
                                if (r >= 0 && r < JINLING_MAP_HEIGHT / TILE_SIZE
                                        && c >= 0 && c < JINLING_MAP_WIDTH / TILE_SIZE) {
                                    mapData[r][c] = MapGenerator.VILLAGE_NO_PASS;
                                }
                            }
                        }
                    }
                }
                return null;
            });
        }

        // 主城建筑碰撞标记（单独一个线程）
        obstacleTasks.add(() -> {
            for (Rect obs : jinlingCityRenderer.getObstacles()) {
                int startCol = obs.left / TILE_SIZE;
                int endCol = obs.right / TILE_SIZE;
                int startRow = obs.top / TILE_SIZE;
                int endRow = obs.bottom / TILE_SIZE;
                for (int r = startRow; r <= endRow; r++) {
                    for (int c = startCol; c <= endCol; c++) {
                        if (r >= 0 && r < JINLING_MAP_HEIGHT / TILE_SIZE
                                && c >= 0 && c < JINLING_MAP_WIDTH / TILE_SIZE) {
                            mapData[r][c] = MapGenerator.VILLAGE_NO_PASS;
                        }
                    }
                }
            }
            return null;
        });

        try { obstacleExecutor.invokeAll(obstacleTasks); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        obstacleExecutor.shutdown();

        // 清除其他渲染器
        mazeRenderer = null;
        mazeGenerator = null;
        villageRenderer = null;
    }

    /**
     * 获取当前地图所有障碍物（村庄房屋、城市建筑、城墙等）
     */
    public List<Rect> getAllVillageObstacles() {
        List<Rect> allObs = new ArrayList<>();
        if (villageRenderer != null) {
            allObs.addAll(villageRenderer.getObstacles());
        }
        for (VillageRenderer vr : additionalVillageRenderers) {
            allObs.addAll(vr.getObstacles());
        }
        // 城市建筑也纳入避障范围（城里动物需要避开房屋和城墙）
        if (jinlingCityRenderer != null && currentMapId == MAP_ID_JIN_LING) {
            allObs.addAll(jinlingCityRenderer.getObstacles());
        }
        return allObs;
    }
}

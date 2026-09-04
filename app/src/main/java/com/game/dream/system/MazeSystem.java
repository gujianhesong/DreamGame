package com.game.dream.system;

import com.game.dream.map.MazeGenerator;
import com.game.dream.npc.TreasureChest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 迷宫系统 - 管理迷宫中的宝箱、出口传送门等对象
 */
public class MazeSystem {

    private static MazeSystem instance = new MazeSystem();

    public static MazeSystem getInstance() {
        return instance;
    }

    private List<TreasureChest> treasureChests = new ArrayList<>();
    private float exitPortalX;
    private float exitPortalY;
    private float entranceX;
    private float entranceY;
    private boolean isInitialized = false;

    private MazeSystem() {
    }

    /**
     * 初始化迷宫对象 - 在迷宫地图中放置宝箱和记录出口位置
     */
    public void initMazeObjects(int[][] mapData, MazeGenerator mazeGen) {
        treasureChests.clear();
        isInitialized = true;

        // 记录入口/出口坐标
        entranceX = mazeGen.getEntranceX();
        entranceY = mazeGen.getEntranceY();
        exitPortalX = mazeGen.getExitX();
        exitPortalY = mazeGen.getExitY();

        placeTreasureChests(mapData);
    }

    /**
     * 初始化迷宫对象 - 直接传入入口/出口坐标（用于海底迷宫等非 MazeGenerator 生成的迷宫）
     */
    public void initMazeObjects(int[][] mapData, int entranceX, int entranceY, int exitX, int exitY) {
        treasureChests.clear();
        isInitialized = true;

        this.entranceX = entranceX;
        this.entranceY = entranceY;
        this.exitPortalX = exitX;
        this.exitPortalY = exitY;

        placeTreasureChests(mapData);
    }

    /**
     * 在迷宫中随机放置宝箱
     */
    private void placeTreasureChests(int[][] mapData) {

        // 在迷宫中随机放置宝箱
        Random random = new Random(99999);
        int chestCount = 15 + random.nextInt(6); // 15-20 个宝箱
        int rows = mapData.length;
        int cols = mapData[0].length;
        int tileSize = 20;
        int chestId = 0;

        for (int i = 0; i < chestCount; i++) {
            // 随机找一个可通行的位置
            for (int attempt = 0; attempt < 100; attempt++) {
                int gridX = 5 + random.nextInt(cols - 10);
                int gridY = 5 + random.nextInt(rows - 10);

                if (MazeGenerator.checkCanPass(mapData[gridY][gridX])) {
                    // 确保不在入口/出口附近
                    float px = gridX * tileSize;
                    float py = gridY * tileSize;
                    float distToEntrance = (float) Math.sqrt(Math.pow(px - entranceX, 2) + Math.pow(py - entranceY, 2));
                    float distToExit = (float) Math.sqrt(Math.pow(px - exitPortalX, 2) + Math.pow(py - exitPortalY, 2));

                    if (distToEntrance > 500 && distToExit > 500) {
                        treasureChests.add(new TreasureChest(chestId++, px, py));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 检查玩家是否走到出口传送门
     */
    public boolean checkExitPortal(float playerX, float playerY) {
        if (!isInitialized) return false;
        float dx = playerX - exitPortalX;
        float dy = playerY - exitPortalY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist < 60; // 60px 内触发传送
    }

    /**
     * 检查是否点击了宝箱
     */
    public TreasureChest checkTreasureChestClick(float worldX, float worldY) {
        for (TreasureChest chest : treasureChests) {
            if (chest.isTouched(worldX, worldY)) {
                return chest;
            }
        }
        return null;
    }

    public List<TreasureChest> getTreasureChests() {
        return treasureChests;
    }

    public float getExitPortalX() { return exitPortalX; }
    public float getExitPortalY() { return exitPortalY; }
    public float getEntranceX() { return entranceX; }
    public float getEntranceY() { return entranceY; }
    public boolean isInitialized() { return isInitialized; }

    public void reset() {
        treasureChests.clear();
        isInitialized = false;
    }
}

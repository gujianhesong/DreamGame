package com.game.dream.map;

import static com.game.dream.common.Constants.TILE_SIZE;

import com.game.dream.GameEngine;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Viper;
import com.game.dream.enemy.WildBoar;
import com.game.dream.enemy.Wolf;
import com.game.dream.system.MapSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapContentManager {

    private static MapContentManager instance = new MapContentManager();

    public static MapContentManager getInstance() {
        return instance;
    }

    private MapContentManager() {
    }

    /**
     * Spawn enemys at random locations
     */
    public List<Enemy> initializeEnemies() {
        int currentMapId = MapSystem.getInstance().getCurrentMapId();
        return initializeEnemies(currentMapId);
    }

    /**
     * Spawn enemys at random locations
     *
     * @param mapId
     * @return
     */
    public List<Enemy> initializeEnemies(int mapId) {
        List<Enemy> enemies = new ArrayList<>();

        int enemyCount = (mapId == 1002) ? 120 : 100; // 迷宫中怪物少一些

        Random random = new Random(67890);
        int[][] map = MapSystem.getInstance().getCurMapInfo().getMapData();
        for (int i = 0; i < enemyCount; i++) {
            boolean foundValidSpawn = false;
            float spawnX = 0, spawnY = 0;

            // Try to find a valid spawn position
            for (int attempts = 0; attempts < 50 && !foundValidSpawn; attempts++) {
                int gridX = random.nextInt(map[0].length);
                int gridY = random.nextInt(map.length);

                int terrain = map[gridY][gridX];

                // Spawn on passable terrain
                boolean canSpawn = false;
                if (mapId == 1002) {
                    // 迷宫: 只能在地板上生成
                    canSpawn = (terrain == MazeGenerator.MAZE_FLOOR || terrain == MazeGenerator.MAZE_ENTRANCE || terrain == MazeGenerator.MAZE_EXIT);
                } else {
                    // 普通地图: 不能在水/岩浆/村庄建筑上生成
                    canSpawn = (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA
                            && terrain != MapGenerator.VILLAGE_CAN_PASS && terrain != MapGenerator.VILLAGE_NO_PASS);
                }

                if (canSpawn) {
                    spawnX = gridX * TILE_SIZE + TILE_SIZE / 2;
                    spawnY = gridY * TILE_SIZE + TILE_SIZE / 2;

                    // Check distance from player
                    float dx = spawnX - GameEngine.getInstance().getPlayer().getX();
                    float dy = spawnY - GameEngine.getInstance().getPlayer().getY();
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance > 500) { // At least 500 pixels away from player
                        foundValidSpawn = true;
                    }
                }
            }

            if (foundValidSpawn) {
                Enemy enemy = generateEnemyOnMap(mapId, spawnX, spawnY);
                if (enemy != null) {
                    enemies.add(enemy);
                }
            }
        }
        return enemies;
    }

    private Enemy generateEnemyOnMap(int mapId, float spawnX, float spawnY) {
        Enemy enemy = null;
        double rand = Math.random();
        switch (mapId) {
            case 1001: {
                //清溪村
                if (rand < 0.25) {
                    enemy = new Tiger(spawnX, spawnY);
                    enemy.setName("猛虎");
                } else if (rand < 0.5) {
                    enemy = new WildBoar(spawnX, spawnY);
                    enemy.setName("野猪");
                } else if (rand < 0.75) {
                    enemy = new Viper(spawnX, spawnY);
                    enemy.setName("毒蛇");
                } else {
                    enemy = new Wolf(spawnX, spawnY);
                    enemy.setName("野狼");
                }
            }
            case 1002: {
                //清溪村-迷宫
                if (rand < 0.25) {
                    enemy = new Tiger(spawnX, spawnY);
                    enemy.setName("猛虎");
                } else if (rand < 0.5) {
                    enemy = new WildBoar(spawnX, spawnY);
                    enemy.setName("野猪");
                } else if (rand < 0.75) {
                    enemy = new Viper(spawnX, spawnY);
                    enemy.setName("毒蛇");
                } else {
                    enemy = new Wolf(spawnX, spawnY);
                    enemy.setName("野狼");
                }
            }
        }
        return enemy;
    }
}

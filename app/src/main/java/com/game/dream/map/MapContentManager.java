package com.game.dream.map;

import static com.game.dream.common.Constants.TILE_SIZE;

import com.game.dream.GameEngine;
import com.game.dream.enemy.Enemy;
import com.game.dream.enemy.Bandit;
import com.game.dream.enemy.CrabGeneral;
import com.game.dream.enemy.FoxSpirit;
import com.game.dream.enemy.GiantSeaTurtle;
import com.game.dream.enemy.LittleGreenDragon;
import com.game.dream.enemy.LonelySpirit;
import com.game.dream.enemy.SavageWraith;
import com.game.dream.enemy.ShrimpSoldier;
import com.game.dream.enemy.Tiger;
import com.game.dream.enemy.Viper;
import com.game.dream.enemy.WildBoar;
import com.game.dream.enemy.Wolf;
import com.game.dream.enemy.Yaksha;
import com.game.dream.enemy.YellowSpringGuide;
import com.game.dream.enemy.BloodPoolDemonKing;
import com.game.dream.enemy.JudgeCuiYu;
import com.game.dream.enemy.HellGuardian;
import com.game.dream.enemy.KingYanluo;
import com.game.dream.enemy.Vampire;
import com.game.dream.enemy.ChainBoundWraith;
import com.game.dream.enemy.GhostGeneral;
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

        if (mapId == MapSystem.MAP_ID_ILLUSION_REALM) {
            return enemies;
        }

        int enemyCount;
        if (mapId >= MapSystem.MAP_ID_HELL_MAZE_1 && mapId <= MapSystem.MAP_ID_HELL_MAZE_4) {
            // 地狱迷宫：第4层无小怪（纯BOSS战）
            if (mapId == MapSystem.MAP_ID_HELL_MAZE_4) {
                return enemies;
            }
            enemyCount = 80 + (mapId - MapSystem.MAP_ID_HELL_MAZE_1) * 30; // 80/110/140
        } else if (mapId >= 2000 && mapId < 3000) {
            enemyCount = 120; // 迷宫中怪物少一些
        } else if (mapId == MapSystem.MAP_ID_JIN_LING) {
            enemyCount = 3000; // 金陵大地图怪物多一些
        } else if (mapId == MapSystem.MAP_ID_NETHERWORLD) {
            enemyCount = 400; // 地府危险区域，怪物较多
        } else {
            enemyCount = 180;
        }

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
                if (mapId == MapSystem.MAP_ID_NETHERWORLD) {
                    // 地府: 孤魂在彼岸花田/黄泉河两岸，野鬼在幽冥荒原/白骨堆
                    // 阎罗殿/奈何桥/幽魂牢/地狱深渊/鬼门关城墙不刷怪
                    canSpawn = (terrain == MapGenerator.SPIDER_LILY_FIELD
                            || terrain == MapGenerator.NETHER_WASTELAND
                            || terrain == MapGenerator.BONE_PILE);
                    // 幽魂牢区域绝对不刷怪
                    if (canSpawn) {
                        float wx = gridX * TILE_SIZE + TILE_SIZE / 2f;
                        float wy = gridY * TILE_SIZE + TILE_SIZE / 2f;
                        if (wx >= NetherworldMapGenerator.PRISON_X1 - 200
                                && wx <= NetherworldMapGenerator.PRISON_X2 + 200
                                && wy >= NetherworldMapGenerator.PRISON_Y1 - 200
                                && wy <= NetherworldMapGenerator.PRISON_Y2 + 200) {
                            canSpawn = false;
                        }
                    }
                } else if (mapId > 1000 && mapId < 2000) {
                    // 普通大地图: 不能在水/岩浆/村庄建筑/河流/山脉/城墙上生成
                    canSpawn = (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA
                            && terrain != MapGenerator.VILLAGE_CAN_PASS && terrain != MapGenerator.VILLAGE_NO_PASS
                            && terrain != MapGenerator.RIVER && terrain != MapGenerator.MOUNTAIN
                            && terrain != MapGenerator.CITY_WALL && terrain != MapGenerator.CITY_ROAD
                            && terrain != MapGenerator.DEEP_SEA && terrain != MapGenerator.HYDROTHERMAL
                            && terrain != MapGenerator.SEA && terrain != MapGenerator.PALACE_GROUND);
                } else if (mapId >= MapSystem.MAP_ID_HELL_MAZE_1 && mapId <= MapSystem.MAP_ID_HELL_MAZE_4) {
                    // 地狱迷宫: 地板+特殊地形均可刷怪
                    canSpawn = (terrain == MazeGenerator.MAZE_FLOOR
                            || terrain == MazeGenerator.MAZE_ENTRANCE
                            || terrain == MazeGenerator.MAZE_EXIT
                            || terrain == HellMazeGenerator.HELL_BLOOD_POOL
                            || terrain == HellMazeGenerator.HELL_SAFE_STONE
                            || terrain == HellMazeGenerator.HELL_TRAP
                            || terrain == HellMazeGenerator.HELL_BOSS_ARENA);
                } else if (mapId > 2000 && mapId < 3000) {
                    // 迷宫: 只能在地板上生成
                    canSpawn = (terrain == MazeGenerator.MAZE_FLOOR || terrain == MazeGenerator.MAZE_ENTRANCE || terrain == MazeGenerator.MAZE_EXIT);
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

        // 地狱迷宫：在出口前放置守护BOSS
        if (mapId >= MapSystem.MAP_ID_HELL_MAZE_1 && mapId <= MapSystem.MAP_ID_HELL_MAZE_4) {
            spawnHellMazeBoss(mapId, enemies);
        }
        return enemies;
    }

    /**
     * 在地狱迷宫出口前方生成层BOSS
     */
    private void spawnHellMazeBoss(int mapId, List<Enemy> enemies) {
        HellMazeGenerator gen = MapSystem.getInstance().getHellMazeGenerator();
        if (gen == null) return;

        // 第4层：在 3 个 BOSS 房间中心分别生成守卫与阎罗王
        if (mapId == MapSystem.MAP_ID_HELL_MAZE_4) {
            int[] centers = gen.getBossArenaCenters();
            if (centers != null && centers.length >= 6) {
                // 房间1：牛头 + 马面
                Enemy cow = new HellGuardian(centers[0] - 120, centers[1], HellGuardian.GuardType.COW_HEAD);
                cow.setName("牛头");
                enemies.add(cow);
                Enemy horse = new HellGuardian(centers[0] + 120, centers[1], HellGuardian.GuardType.HORSE_FACE);
                horse.setName("马面");
                enemies.add(horse);

                // 房间2：黑无常 + 白无常
                Enemy black = new HellGuardian(centers[2] - 120, centers[3], HellGuardian.GuardType.BLACK);
                black.setName("黑无常");
                enemies.add(black);
                Enemy white = new HellGuardian(centers[2] + 120, centers[3], HellGuardian.GuardType.WHITE);
                white.setName("白无常");
                enemies.add(white);

                // 房间3：阎罗王
                Enemy yanluo = new KingYanluo(centers[4], centers[5]);
                yanluo.setName("阎罗王");
                enemies.add(yanluo);
            }
            return;
        }

        // 第1~3层：BOSS 守在出口前约 200px，防止一入层就碰到传送门
        float bx = gen.getExitX();
        float by = Math.max(200, gen.getExitY() - 200);
        Enemy boss = null;
        switch (mapId) {
            case MapSystem.MAP_ID_HELL_MAZE_1:
                boss = new YellowSpringGuide(bx, by);
                boss.setName("黄泉引路人");
                break;
            case MapSystem.MAP_ID_HELL_MAZE_2:
                boss = new BloodPoolDemonKing(bx, by);
                boss.setName("血池鬼王");
                break;
            case MapSystem.MAP_ID_HELL_MAZE_3:
                boss = new JudgeCuiYu(bx, by);
                boss.setName("判官崔钰");
                break;
        }
        if (boss != null) {
            boss.setAggro(0); // 不主动仇恨，靠近时才反应
            enemies.add(boss);
        }
    }

    private Enemy generateEnemyOnMap(int mapId, float spawnX, float spawnY) {
        Enemy enemy = null;
        double rand = Math.random();
        switch (mapId) {
            case MapSystem.MAP_ID_QING_XI: {
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
                break;
            }
            case MapSystem.MAP_ID_QING_XI_MAZE: {
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
                break;
            }
            case MapSystem.MAP_ID_JIN_LING: {
                // 金陵野外
                if (rand < 0.5) {
                    enemy = new Bandit(spawnX, spawnY);
                    enemy.setName("强盗");
                } else {
                    enemy = new FoxSpirit(spawnX, spawnY);
                    enemy.setName("狐狸精");
                }
                break;
            }
            case MapSystem.MAP_ID_DONGHAI_SEABED: {
                // 东海海底
                if (rand < 0.30) {
                    enemy = new ShrimpSoldier(spawnX, spawnY);
                    enemy.setName("虾兵");
                } else if (rand < 0.55) {
                    enemy = new CrabGeneral(spawnX, spawnY);
                    enemy.setName("蟹将");
                } else if (rand < 0.80) {
                    enemy = new Yaksha(spawnX, spawnY);
                    enemy.setName("夜叉");
                } else {
                    enemy = new LittleGreenDragon(spawnX, spawnY);
                    enemy.setName("小青龙");
                }
                break;
            }
            case MapSystem.MAP_ID_DONGHAI_BAY: {
                // 东海湾
                enemy = new GiantSeaTurtle(spawnX, spawnY);
                enemy.setName("大海龟");
                break;
            }
            case MapSystem.MAP_ID_UNDERWATER_MAZE: {
                // 海底迷宫
                if (rand < 0.30) {
                    enemy = new Yaksha(spawnX, spawnY);
                    enemy.setName("夜叉");
                } else if (rand < 0.55) {
                    enemy = new LittleGreenDragon(spawnX, spawnY);
                    enemy.setName("小青龙");
                } else if (rand < 0.80) {
                    enemy = new ShrimpSoldier(spawnX, spawnY);
                    enemy.setName("虾兵");
                } else {
                    enemy = new CrabGeneral(spawnX, spawnY);
                    enemy.setName("蟹将");
                }
                break;
            }
            case MapSystem.MAP_ID_NETHERWORLD: {
                // 地府: 孤魂(彼岸花田/黄泉河畔) + 野鬼(幽冥荒原/白骨堆)
                // 根据刷新点地形决定怪物类型
                int[][] netherMap = MapSystem.getInstance().getCurMapInfo().getMapData();
                int gx = (int) (spawnX / TILE_SIZE);
                int gy = (int) (spawnY / TILE_SIZE);
                int terrain = 0;
                if (netherMap != null && gy >= 0 && gy < netherMap.length && gx >= 0 && gx < netherMap[0].length) {
                    terrain = netherMap[gy][gx];
                }
                if (terrain == MapGenerator.SPIDER_LILY_FIELD) {
                    // 彼岸花田 → 孤魂为主
                    if (rand < 0.70) {
                        enemy = new LonelySpirit(spawnX, spawnY);
                        enemy.setName("孤魂");
                    } else {
                        enemy = new SavageWraith(spawnX, spawnY);
                        enemy.setName("野鬼");
                    }
                } else {
                    // 幽冥荒原/白骨堆 → 野鬼为主
                    if (rand < 0.70) {
                        enemy = new SavageWraith(spawnX, spawnY);
                        enemy.setName("野鬼");
                    } else {
                        enemy = new LonelySpirit(spawnX, spawnY);
                        enemy.setName("孤魂");
                    }
                }
                break;
            }
            case MapSystem.MAP_ID_HELL_MAZE_1: {
                // 黄泉迷径：吸血鬼50% + 幽灵50%
                if (rand < 0.50) {
                    enemy = new Vampire(spawnX, spawnY);
                    enemy.setName("吸血鬼");
                } else {
                    enemy = new ChainBoundWraith(spawnX, spawnY);
                    enemy.setName("幽灵");
                }
                break;
            }
            case MapSystem.MAP_ID_HELL_MAZE_2: {
                // 血池炼狱：吸血鬼50% + 幽灵50%
                if (rand < 0.50) {
                    enemy = new Vampire(spawnX, spawnY);
                    enemy.setName("吸血鬼");
                } else {
                    enemy = new ChainBoundWraith(spawnX, spawnY);
                    enemy.setName("幽灵");
                }
                break;
            }
            case MapSystem.MAP_ID_HELL_MAZE_3: {
                // 枉死城：吸血鬼35% + 幽灵35% + 鬼将30%
                if (rand < 0.35) {
                    enemy = new Vampire(spawnX, spawnY);
                    enemy.setName("吸血鬼");
                } else if (rand < 0.70) {
                    enemy = new ChainBoundWraith(spawnX, spawnY);
                    enemy.setName("幽灵");
                } else {
                    enemy = new GhostGeneral(spawnX, spawnY);
                    enemy.setName("鬼将");
                }
                break;
            }
        }
        return enemy;
    }
}

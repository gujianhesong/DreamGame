package com.game.dream.utils;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.GameEngine;
import com.game.dream.bean.MapInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.figure.Player;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.item.ItemStack;
import com.game.dream.item.TreasureMapItem;
import com.game.dream.map.MapGenerator;
import com.game.dream.map.MazeGenerator;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.FloatingText;

import java.util.ArrayList;
import java.util.List;

public class TreasureMapUtil {

    public static TreasureMapItem createRandomMap() {
        MapInfo target = pickTargetMap();
        if (target == null || target.getMapData() == null) {
            return null;
        }
        float[] pos = findWalkableWorldPos(target);
        if (pos == null) {
            return null;
        }
        return new TreasureMapItem(target.getMapId(), pos[0], pos[1]);
    }

    public static boolean tryUse(TreasureMapItem mapItem) {
        MapSystem mapSystem = MapSystem.getInstance();
        if (mapSystem.getCurrentMapId() != mapItem.getTargetMapId()) {
            GameEngine.getInstance().showCenterToast(
                    "宝藏在「" + mapSystem.getMapName(mapItem.getTargetMapId()) + "」，先去那张地图", 2200);
            return false;
        }

        Player player = GameEngine.getInstance().getPlayer();
        if (player == null) {
            return false;
        }
        float dx = player.getX() - mapItem.getTargetX();
        float dy = player.getY() - mapItem.getTargetY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > TreasureMapItem.USE_RANGE) {
            GameEngine.getInstance().showCenterToast(
                    "还没到标记地点，大约还差 " + Math.round(dist) + " 步", 2000);
            return false;
        }

        grantTreasure();
        return true;
    }

    public static void drawMarkers(Canvas canvas, float cameraX, float cameraY) {
        int currentMapId = MapSystem.getInstance().getCurrentMapId();
        List<ItemStack> items = ItemSystem.getInstance().getItems();
        if (items == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        long now = System.currentTimeMillis();
        float pulse = 0.65f + 0.35f * (float) Math.sin(now / 220.0);

        for (ItemStack stack : items) {
            if (stack == null || !(stack.getItem() instanceof TreasureMapItem)) {
                continue;
            }
            TreasureMapItem mapItem = (TreasureMapItem) stack.getItem();
            if (mapItem.getTargetMapId() != currentMapId) {
                continue;
            }
            float sx = mapItem.getTargetX() - cameraX;
            float sy = mapItem.getTargetY() - cameraY;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.argb((int) (180 * pulse), 255, 210, 70));
            canvas.drawCircle(sx, sy, 18 * pulse, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(220, 210, 160, 40));
            canvas.drawCircle(sx, sy, 6, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            paint.setColor(Color.rgb(255, 230, 140));
            canvas.drawText("宝", sx, sy - 22, paint);
        }
    }

    private static MapInfo pickTargetMap() {
        MapSystem mapSystem = MapSystem.getInstance();
        List<MapInfo> candidates = new ArrayList<>();
        int[] allowedIds = {
                MapSystem.MAP_ID_QING_XI,
                MapSystem.MAP_ID_JIN_LING,
                MapSystem.MAP_ID_DONGHAI_BAY
        };
        for (int mapId : allowedIds) {
            MapInfo info = mapSystem.getMapInfo(mapId);
            if (info != null && info.getMapData() != null) {
                candidates.add(info);
            }
        }
        MapInfo current = mapSystem.getCurMapInfo();
        if (current != null && current.getMapData() != null && isAllowedTreasureMap(current.getMapId())) {
            if (Math.random() < 0.55 || candidates.isEmpty()) {
                return current;
            }
        }
        if (candidates.isEmpty()) {
            return current != null && current.getMapData() != null && isAllowedTreasureMap(current.getMapId())
                    ? current : null;
        }
        return candidates.get((int) (Math.random() * candidates.size()));
    }

    public static boolean isAllowedTreasureMap(int mapId) {
        return mapId == MapSystem.MAP_ID_QING_XI
                || mapId == MapSystem.MAP_ID_JIN_LING
                || mapId == MapSystem.MAP_ID_DONGHAI_BAY;
    }

    private static float[] findWalkableWorldPos(MapInfo mapInfo) {
        int[][] data = mapInfo.getMapData();
        if (data == null || data.length == 0 || data[0].length == 0) {
            return null;
        }
        int rows = data.length;
        int cols = data[0].length;
        int margin = 4;
        for (int i = 0; i < 250; i++) {
            int gx = margin + (int) (Math.random() * Math.max(1, cols - margin * 2));
            int gy = margin + (int) (Math.random() * Math.max(1, rows - margin * 2));
            if (isTreasureWalkable(data, gx, gy)) {
                return new float[]{
                        gx * TILE_SIZE + TILE_SIZE / 2f,
                        gy * TILE_SIZE + TILE_SIZE / 2f
                };
            }
        }
        int startX = margin + (int) (Math.random() * Math.max(1, cols - margin * 2));
        int startY = margin + (int) (Math.random() * Math.max(1, rows - margin * 2));
        for (int d = 0; d < rows * cols; d++) {
            int gx = margin + (startX + d) % Math.max(1, cols - margin * 2);
            int gy = margin + ((startY + d / Math.max(1, cols - margin * 2)) % Math.max(1, rows - margin * 2));
            if (isTreasureWalkable(data, gx, gy)) {
                return new float[]{
                        gx * TILE_SIZE + TILE_SIZE / 2f,
                        gy * TILE_SIZE + TILE_SIZE / 2f
                };
            }
        }
        return null;
    }

    private static boolean isTreasureWalkable(int[][] mapData, int gx, int gy) {
        if (gy < 0 || gx < 0 || gy >= mapData.length || gx >= mapData[0].length) {
            return false;
        }
        int terrain = mapData[gy][gx];
        if (terrain == MazeGenerator.MAZE_WALL) {
            return false;
        }
        if (!MapGenerator.checkCanPass(mapData, gx, gy)) {
            return false;
        }
        // 村庄街道、城内路面、龙宫内院偏安全，宝藏埋在野外
        if (terrain == MapGenerator.VILLAGE_CAN_PASS
                || terrain == MapGenerator.CITY_ROAD
                || terrain == MapGenerator.PALACE_GROUND
                || terrain == MapGenerator.JUDGE_HALL_GROUND
                || terrain == MapGenerator.BRIDGE
                || terrain == MapGenerator.STONE_BRIDGE) {
            return false;
        }
        return true;
    }

    private static void grantTreasure() {
        if (Math.random() < 0.20) {
            RoleInfo role = RoleSystem.getInstance().getRoleInfo();
            int money = 5000 + (int) (Math.random() * 25001);
            role.setMoney(role.getMoney() + money);
            GameEngine.getInstance().showFloatText("金币+" + money, FloatingText.Type.MONEY);
            GameEngine.getInstance().showCenterToast("挖出金币 " + money, 2500);
            return;
        }

        int level = Math.max(1, RoleSystem.getInstance().getRoleInfo().getLevel());
        Item item = rollRewardItem(level);
        if (item == null) {
            GameEngine.getInstance().showCenterToast("这里什么也没有", 2000);
            return;
        }
        boolean added = ItemSystem.getInstance().addItem(item, 1);
        if (!added) {
            Player player = GameEngine.getInstance().getPlayer();
            if (player != null) {
                GameEngine.getInstance().spawnGroundItem(player.getX(), player.getY(), item, 1);
            }
        }
        GameEngine.getInstance().showCenterToast("你挖出了：" + item.getName(), 2500);
    }

    private static Item rollRewardItem(int playerLevel) {
        int gearLevel = Math.max(10, Math.min(100, (playerLevel / 10) * 10));
        int gemLevel = Math.max(1, Math.min(4, 1 + playerLevel / 25));
        double roll = Math.random();
        if (roll < 0.32) {
            return ItemCreator.createBuildEquipIron(gearLevel);
        }
        if (roll < 0.55) {
            return ItemCreator.createBuildEquipBook(gearLevel, null);
        }
        if (roll < 0.74) {
            return ItemCreator.createGemstone(gemLevel);
        }
        if (roll < 0.90) {
            return ItemCreator.createXiLianStone(gearLevel);
        }
        return EquipCreator.createEquip(gearLevel, null);
    }
}

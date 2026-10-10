package com.game.dream.item;

import com.game.dream.bean.TreasureMapItemInfo;
import com.game.dream.system.MapSystem;

/**
 * 藏宝图：记录固定大地图上的一处可通行坐标，抵达附近后使用可挖出随机物品。
 */
public class TreasureMapItem extends Item {
    public static final int ITEM_ID = 260001;
    public static final float USE_RANGE = 160f;

    private final int targetMapId;
    private final float targetX;
    private final float targetY;

    public TreasureMapItem(int targetMapId, float targetX, float targetY) {
        super(ITEM_ID, "藏宝图", buildDescription(targetMapId, targetX, targetY),
                Type.SPECIAL, Rarity.Rarity_4, 1, 800);
        this.targetMapId = targetMapId;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    private static String buildDescription(int mapId, float x, float y) {
        String mapName = MapSystem.getInstance().getMapName(mapId);
        return "记载着宝藏埋藏点。前往「" + mapName + "」坐标("
                + Math.round(x) + ", " + Math.round(y) + ")附近使用。";
    }

    public int getTargetMapId() {
        return targetMapId;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public TreasureMapItemInfo toItemInfo() {
        return new TreasureMapItemInfo(getId(), getName(), 1, targetMapId, targetX, targetY);
    }

    @Override
    public boolean use() {
        return true;
    }
}

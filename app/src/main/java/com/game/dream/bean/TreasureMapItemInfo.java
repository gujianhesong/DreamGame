package com.game.dream.bean;

import com.game.dream.item.Item;

public class TreasureMapItemInfo extends ItemInfo {
    private int targetMapId;
    private float targetX;
    private float targetY;

    public TreasureMapItemInfo() {
        super(0, "藏宝图", Item.Type.SPECIAL.name(), 1);
    }

    public TreasureMapItemInfo(int id, String name, int amount, int targetMapId, float targetX, float targetY) {
        super(id, name, Item.Type.SPECIAL.name(), amount);
        this.targetMapId = targetMapId;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public int getTargetMapId() {
        return targetMapId;
    }

    public void setTargetMapId(int targetMapId) {
        this.targetMapId = targetMapId;
    }

    public float getTargetX() {
        return targetX;
    }

    public void setTargetX(float targetX) {
        this.targetX = targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }
}

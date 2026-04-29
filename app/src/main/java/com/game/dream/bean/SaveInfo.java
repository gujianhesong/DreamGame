package com.game.dream.bean;

import java.util.List;

public class SaveInfo {
    private RoleInfo roleInfo;
    private List<ItemInfo> itemInfos;
    private List<EquipItemInfo> equipInfos;

    public RoleInfo getRoleInfo() {
        return roleInfo;
    }

    public void setRoleInfo(RoleInfo roleInfo) {
        this.roleInfo = roleInfo;
    }

    public List<ItemInfo> getItemInfos() {
        return itemInfos;
    }

    public void setItemInfos(List<ItemInfo> itemInfos) {
        this.itemInfos = itemInfos;
    }

    public List<EquipItemInfo> getEquipInfos() {
        return equipInfos;
    }

    public void setEquipInfos(List<EquipItemInfo> equipInfos) {
        this.equipInfos = equipInfos;
    }
}

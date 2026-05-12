package com.game.dream.bean;

import java.util.List;

public class SaveInfo {
    private RoleInfo roleInfo;
    private List<ItemInfo> itemInfos;
    private List<EquipItemInfo> equipInfos;
    private List<SkillInfo> mainSkillInfos;
    private List<SkillInfo> assistSkillInfos;
    private List<SkillInfo> practiseSkillInfos;

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

    public List<SkillInfo> getMainSkillInfos() {
        return mainSkillInfos;
    }

    public void setMainSkillInfos(List<SkillInfo> skillInfos) {
        this.mainSkillInfos = skillInfos;
    }

    public List<SkillInfo> getAssistSkillInfos() {
        return assistSkillInfos;
    }

    public void setAssistSkillInfos(List<SkillInfo> skillInfos) {
        this.assistSkillInfos = skillInfos;
    }

    public List<SkillInfo> getPractiseSkillInfos() {
        return practiseSkillInfos;
    }

    public void setPractiseSkillInfos(List<SkillInfo> skillInfos) {
        this.practiseSkillInfos = skillInfos;
    }
}

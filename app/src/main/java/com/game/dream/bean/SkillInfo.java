package com.game.dream.bean;

import com.game.dream.enums.SkillType;

public class SkillInfo {
    private SkillType skillType;
    private int level;
    private String name;
    private String desc;

    public SkillInfo(SkillType skillType, int level, String name, String desc) {
        this.skillType = skillType;
        this.level = level;
        this.name = name;
        this.desc = desc;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public void setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

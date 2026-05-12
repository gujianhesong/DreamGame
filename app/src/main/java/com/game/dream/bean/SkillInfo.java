package com.game.dream.bean;

import com.game.dream.enums.SkillType;

public class SkillInfo {
    private SkillType skillType;
    private int level;
    private transient int maxLevel;
    private String name;
    private transient String desc;

    public SkillInfo(SkillType skillType, int level, int maxLevel, String name, String desc) {
        this.skillType = skillType;
        this.level = level;
        this.maxLevel = maxLevel;
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

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
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


    public boolean canUpgrade() {
        return level < maxLevel;
    }

    public boolean canDowngrade() {
        return level > 1;
    }
}

package com.game.dream.bean;

import com.game.dream.enums.SkillType;

import java.security.PublicKey;

public class SkillInfo {
    private SkillType skillType;
    private int level;
    private transient int maxLevel;
    private transient int cooldownSeconds;
    private String name;
    private transient String desc;


    public SkillInfo(SkillType skillType, int level, int maxLevel, String name, String desc) {
        this.skillType = skillType;
        this.level = level;
        this.maxLevel = maxLevel;
        this.name = name;
        this.desc = desc;
    }

    public SkillInfo(SkillType skillType, int level, int maxLevel, int cooldownSeconds, String name, String desc) {
        this.skillType = skillType;
        this.level = level;
        this.maxLevel = maxLevel;
        this.cooldownSeconds = cooldownSeconds;
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

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /** 实际冷却毫秒。剑气随等级从 1 秒降到不低于 0.6 秒。 */
    public long getCooldownMillis() {
        if (skillType == SkillType.MAIN_SwordQi) {
            int lv = Math.max(1, level);
            return Math.max(600L, 1000L - (lv - 1) * 400L / 9L);
        }
        return Math.max(0, cooldownSeconds) * 1000L;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
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

    public boolean isMainSkill(){
        if (getSkillType().name().startsWith("MAIN_")) {
            return true;
        }
        return false;
    }

    public boolean isAssistSkill(){
        if (getSkillType().name().startsWith("AST_")) {
            return true;
        }
        return false;
    }

    public boolean isPractiseSkill(){
        if (getSkillType().name().startsWith("PST_")) {
            return true;
        }
        return false;
    }

    /**
     * 是否为“使用”类技能（不可装配，点击直接使用）
     */
    public boolean isUseSkill(){
        return getSkillType() == SkillType.MAIN_DunXing;
    }

    /** 学会即可用，不占技能栏（如冲刺）。 */
    public boolean isPassiveUnlock() {
        return getSkillType() == SkillType.MAIN_ChongCi;
    }
}

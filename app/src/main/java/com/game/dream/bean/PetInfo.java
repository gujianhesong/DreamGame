package com.game.dream.bean;

/**
 * 战宠持久化数据。
 * speciesClass 保存被捕捉怪物的 Enemy 子类全限定名，读档时用反射重建同种战宠。
 */
public class PetInfo {
    private String speciesClass;
    private String name;
    private int level;
    private int exp;
    private float hpRatio;
    private boolean active;

    public PetInfo() {
    }

    public PetInfo(String speciesClass, String name, int level, int exp, float hpRatio, boolean active) {
        this.speciesClass = speciesClass;
        this.name = name;
        this.level = level;
        this.exp = exp;
        this.hpRatio = hpRatio;
        this.active = active;
    }

    public String getSpeciesClass() {
        return speciesClass;
    }

    public void setSpeciesClass(String speciesClass) {
        this.speciesClass = speciesClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public float getHpRatio() {
        return hpRatio;
    }

    public void setHpRatio(float hpRatio) {
        this.hpRatio = hpRatio;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

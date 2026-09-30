package com.game.dream.bean;

/**
 * 战宠持久化数据。
 * speciesClass 保存被捕捉怪物的 Enemy 子类全限定名，读档时用反射重建同种战宠。
 * 属性模型(方案B): 战斗属性由 体/魔/力/耐/敏 派生，故需持久化当前属性、初始模板与未分配点数。
 * 旧存档缺失这些字段时默认为 0，读档侧会自动迁移(按怪物基线反推初始属性)。
 */
public class PetInfo {
    private String speciesClass;
    private String name;
    private int level;
    private int exp;
    private float hpRatio;
    private boolean active;

    // 当前属性点
    private int propTi;
    private int propMo;
    private int propLi;
    private int propNai;
    private int propMin;
    // 初始属性模板(洗点用)
    private int startTi;
    private int startMo;
    private int startLi;
    private int startNai;
    private int startMin;
    // 未分配点数
    private int remainPoints;

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

    public int getPropTi() {
        return propTi;
    }

    public void setPropTi(int propTi) {
        this.propTi = propTi;
    }

    public int getPropMo() {
        return propMo;
    }

    public void setPropMo(int propMo) {
        this.propMo = propMo;
    }

    public int getPropLi() {
        return propLi;
    }

    public void setPropLi(int propLi) {
        this.propLi = propLi;
    }

    public int getPropNai() {
        return propNai;
    }

    public void setPropNai(int propNai) {
        this.propNai = propNai;
    }

    public int getPropMin() {
        return propMin;
    }

    public void setPropMin(int propMin) {
        this.propMin = propMin;
    }

    public int getStartTi() {
        return startTi;
    }

    public void setStartTi(int startTi) {
        this.startTi = startTi;
    }

    public int getStartMo() {
        return startMo;
    }

    public void setStartMo(int startMo) {
        this.startMo = startMo;
    }

    public int getStartLi() {
        return startLi;
    }

    public void setStartLi(int startLi) {
        this.startLi = startLi;
    }

    public int getStartNai() {
        return startNai;
    }

    public void setStartNai(int startNai) {
        this.startNai = startNai;
    }

    public int getStartMin() {
        return startMin;
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public int getRemainPoints() {
        return remainPoints;
    }

    public void setRemainPoints(int remainPoints) {
        this.remainPoints = remainPoints;
    }
}

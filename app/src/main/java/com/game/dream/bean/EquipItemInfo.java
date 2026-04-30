package com.game.dream.bean;

import com.game.dream.item.Item;

public class EquipItemInfo extends ItemInfo {
    //装备类型，1:头盔,2:项链,3:武器,4:衣服,5:腰带,6:鞋子
    private int equipType;
    //装备级别
    private int level;

    //属性
    private int hp;
    private int mp;
    private int hit;
    private int attack;
    private int defense;
    private int speed;
    private int dodge;
    private int mana;

    //原始属性
    private int propTi;
    private int propMo;
    private int propLi;
    private int propNai;
    private int propMin;

    //宝石
    private int hpStoneLevel;
    private int mpStoneLevel;
    private int hitStoneLevel;
    private int attackStoneLevel;
    private int defenseStoneLevel;
    private int speedStoneLevel;
    private int manaStoneLevel;
    private int dodgeStoneLevel;

    //附加属性
    private float attackCritRatio; //物理暴击几率
    private float magicCritRatio; //法术暴击几率
    private float attackSpeedRatio; //攻击速度增幅
    private float magicSpeedRatio; //法术速度增幅
    private float attackValueRatio; //攻击伤害增幅
    private float magicValueRatio; //法术伤害增幅
    private float beAttackedValueRatio; //被攻击伤害减伤
    private float beMagicedValueRatio; //被法术伤害减伤


    public EquipItemInfo(int id, String name) {
        super(id, name, Item.Type.EQUIPMENT.name());
    }

    public int getEquipType() {
        return equipType;
    }

    public void setEquipType(int equipType) {
        this.equipType = equipType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDodge() {
        return dodge;
    }

    public void setDodge(int dodge) {
        this.dodge = dodge;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
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

    public int getHpStoneLevel() {
        return hpStoneLevel;
    }

    public void setHpStoneLevel(int hpStoneLevel) {
        this.hpStoneLevel = hpStoneLevel;
    }

    public int getMpStoneLevel() {
        return mpStoneLevel;
    }

    public void setMpStoneLevel(int mpStoneLevel) {
        this.mpStoneLevel = mpStoneLevel;
    }

    public int getHitStoneLevel() {
        return hitStoneLevel;
    }

    public void setHitStoneLevel(int hitStoneLevel) {
        this.hitStoneLevel = hitStoneLevel;
    }

    public int getAttackStoneLevel() {
        return attackStoneLevel;
    }

    public void setAttackStoneLevel(int attackStoneLevel) {
        this.attackStoneLevel = attackStoneLevel;
    }

    public int getDefenseStoneLevel() {
        return defenseStoneLevel;
    }

    public void setDefenseStoneLevel(int defenseStoneLevel) {
        this.defenseStoneLevel = defenseStoneLevel;
    }

    public int getSpeedStoneLevel() {
        return speedStoneLevel;
    }

    public void setSpeedStoneLevel(int speedStoneLevel) {
        this.speedStoneLevel = speedStoneLevel;
    }

    public int getManaStoneLevel() {
        return manaStoneLevel;
    }

    public void setManaStoneLevel(int manaStoneLevel) {
        this.manaStoneLevel = manaStoneLevel;
    }

    public int getDodgeStoneLevel() {
        return dodgeStoneLevel;
    }

    public void setDodgeStoneLevel(int dodgeStoneLevel) {
        this.dodgeStoneLevel = dodgeStoneLevel;
    }

    public float getAttackCritRatio() {
        return attackCritRatio;
    }

    public void setAttackCritRatio(float attackCritRatio) {
        this.attackCritRatio = attackCritRatio;
    }

    public float getMagicCritRatio() {
        return magicCritRatio;
    }

    public void setMagicCritRatio(float magicCritRatio) {
        this.magicCritRatio = magicCritRatio;
    }

    public float getAttackSpeedRatio() {
        return attackSpeedRatio;
    }

    public void setAttackSpeedRatio(float attackSpeedRatio) {
        this.attackSpeedRatio = attackSpeedRatio;
    }

    public float getMagicSpeedRatio() {
        return magicSpeedRatio;
    }

    public void setMagicSpeedRatio(float magicSpeedRatio) {
        this.magicSpeedRatio = magicSpeedRatio;
    }

    public float getAttackValueRatio() {
        return attackValueRatio;
    }

    public void setAttackValueRatio(float attackValueRatio) {
        this.attackValueRatio = attackValueRatio;
    }

    public float getMagicValueRatio() {
        return magicValueRatio;
    }

    public void setMagicValueRatio(float magicValueRatio) {
        this.magicValueRatio = magicValueRatio;
    }

    public float getBeAttackedValueRatio() {
        return beAttackedValueRatio;
    }

    public void setBeAttackedValueRatio(float beAttackedValueRatio) {
        this.beAttackedValueRatio = beAttackedValueRatio;
    }

    public float getBeMagicedValueRatio() {
        return beMagicedValueRatio;
    }

    public void setBeMagicedValueRatio(float beMagicedValueRatio) {
        this.beMagicedValueRatio = beMagicedValueRatio;
    }
}

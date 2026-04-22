package com.game.dream.bean;

public class AddPointResult {
    private int blood;
    private int magic;
    private int hit;
    private int attack;
    private int defense;
    private int speed;
    private int dodge;
    private int mana;

    public AddPointResult(int blood, int magic, int hit, int attack, int defense, int speed, int dodge, int mana) {
        this.blood = blood;
        this.magic = magic;
        this.hit = hit;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.dodge = dodge;
        this.mana = mana;
    }

    public int getBlood() {
        return blood;
    }

    public int getMagic() {
        return magic;
    }

    public int getHit() {
        return hit;
    }

    public int getAttack() {
        return attack;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDefense() {
        return defense;
    }

    public int getDodge() {
        return dodge;
    }

    public int getMana() {
        return mana;
    }
}

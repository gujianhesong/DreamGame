package com.game.dream.bean;

import com.game.dream.enemy.Enemy;

public class EnemyHitInfo {
    public Enemy enemy;
    public int damage;
    public boolean isCrit;

    public EnemyHitInfo(Enemy enemy, int damage, boolean isCrit) {
        this.enemy = enemy;
        this.damage = damage;
        this.isCrit = isCrit;
    }
}

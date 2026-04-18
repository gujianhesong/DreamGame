package com.game.dream.bean;

import com.game.dream.enemy.Enemy;

public class EnemyHitInfo {
    public Enemy enemy;
    public int damage;

    public EnemyHitInfo(Enemy enemy, int damage) {
        this.enemy = enemy;
        this.damage = damage;
    }
}

package com.game.dream.system;

import com.game.dream.enemy.Enemy;

/**
 * 战宠数据容器。
 * 包裹一个真实的 {@link Enemy} 子类实例(body)作为战宠的实体与美术，
 * 并附加养成元数据(种类、名字、等级、经验、重伤状态)。
 */
public class Pet {

    private final Enemy body;
    private final String speciesClass;
    private String name;
    private int level;
    private int exp;
    private boolean downed; // 重伤(战斗中被击败, 非永久死亡)

    public Pet(Enemy body, String speciesClass, String name) {
        this.body = body;
        this.speciesClass = speciesClass;
        this.name = name;
        this.level = 1;
        this.exp = 0;
        this.downed = false;
    }

    public Enemy getEntity() {
        return body;
    }

    public String getSpeciesClass() {
        return speciesClass;
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

    public boolean isDowned() {
        return downed;
    }

    public void setDowned(boolean downed) {
        this.downed = downed;
    }

    /** 当前血量(0-1)，用于面板与存档显示。 */
    public float getHpRatio() {
        if (body == null || body.getMaxHealth() <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, body.getHealth() / (float) body.getMaxHealth()));
    }

    /** 疗伤: 回满血并解除重伤状态。 */
    public void heal() {
        downed = false;
        if (body != null) {
            body.applyPetLevelStats(level);
        }
    }
}

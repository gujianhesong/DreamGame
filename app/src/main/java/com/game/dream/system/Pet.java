package com.game.dream.system;

import com.game.dream.enemy.Enemy;

/**
 * 战宠数据容器。
 * 包裹一个真实的 {@link Enemy} 子类实例(body)作为战宠的实体与美术，
 * 并附加养成元数据(种类、名字、等级、经验、重伤状态)。
 *
 * 属性模型(方案A): 战宠战斗属性直接复用其物种野生基线，捕捉时不放大(steps=0)，
 * 之后按“相对捕捉等级”的步数线性成长(主属性每级+6%、速度每级+1.2%，
 * 见 {@link Enemy#applyPetLevelStats(int)})，无需手动加点。
 * 战斗属性(气血/攻击/防御/速度/灵力)直接从 body 读取，面板只读展示。
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

    // ==================== 战斗属性(只读, 取自 body) ====================

    public int getDerivedMaxHealth() {
        return body != null ? body.getMaxHealth() : 0;
    }

    public int getDerivedAttack() {
        return body != null ? body.getAttackDamage() : 0;
    }

    public int getDerivedDefense() {
        return body != null ? body.getDefense() : 0;
    }

    public int getDerivedSpeed() {
        return body != null ? Math.round(body.getSpeed()) : 0;
    }

    public int getDerivedMana() {
        return body != null ? body.getMana() : 0;
    }

    /**
     * 依据当前等级重算战斗属性并写入 body(按物种基线线性缩放, 每级 +12%)。
     * @param fullHeal true=回满血(升级/疗伤); false=按当前血量比例保留。
     */
    public void recomputeStats(boolean fullHeal) {
        if (body == null) {
            return;
        }
        float ratio = getHpRatio();
        body.applyPetLevelStats(level); // 按等级缩放并回满血
        if (!fullHeal) {
            body.setHealth(Math.max(1, Math.round(body.getMaxHealth() * ratio)));
        }
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
        recomputeStats(true);
    }
}

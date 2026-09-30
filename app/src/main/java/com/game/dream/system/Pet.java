package com.game.dream.system;

import com.game.dream.bean.AddPointResult;
import com.game.dream.enemy.Enemy;

/**
 * 战宠数据容器。
 * 包裹一个真实的 {@link Enemy} 子类实例(body)作为战宠的实体与美术，
 * 并附加养成元数据(种类、名字、等级、经验、重伤状态)。
 *
 * 属性模型(方案B): 战宠战斗属性由 体/魔/力/耐/敏 五项属性点派生，
 * 复用与玩家相同的 {@link RoleSystem#caculateAddPoints} 公式，再叠加基础值，
 * 最终写入 body 的 气血/攻击/防御/速度/法力。物种差异通过"初始属性模板"体现
 * (捕捉时按怪物基线属性反推得到)。每升 1 级为五项属性各固定 +1，并额外获得
 * {@link #POINTS_PER_LEVEL} 点自由分配。
 */
public class Pet {

    /** 战宠派生属性的基础值(与玩家 RoleInfo 的 baseValue 保持一致风格)。 */
    public static final int BASE_HP = 100;
    public static final int BASE_ATK = 40;
    public static final int BASE_DEF = 40;
    public static final int BASE_SPD = 20;
    public static final int BASE_MANA = 20;
    /** 每升 1 级获得的自由分配属性点。 */
    public static final int POINTS_PER_LEVEL = 5;
    /** 每升 1 级为 体/魔/力/耐/敏 各固定增加的属性点(共 5 点)。 */
    public static final int FIXED_POINTS_PER_ATTR_PER_LEVEL = 1;

    // 属性索引
    public static final int ATTR_TI = 0;   // 体
    public static final int ATTR_MO = 1;   // 魔
    public static final int ATTR_LI = 2;   // 力
    public static final int ATTR_NAI = 3;  // 耐
    public static final int ATTR_MIN = 4;  // 敏
    public static final int ATTR_COUNT = 5;

    private final Enemy body;
    private final String speciesClass;
    private String name;
    private int level;
    private int exp;
    private boolean downed; // 重伤(战斗中被击败, 非永久死亡)

    // 当前属性点
    private int propTi;
    private int propMo;
    private int propLi;
    private int propNai;
    private int propMin;
    // 初始属性模板(捕捉时反推, 洗点用)
    private int startTi;
    private int startMo;
    private int startLi;
    private int startNai;
    private int startMin;
    // 未分配点数
    private int remainPoints;

    public Pet(Enemy body, String speciesClass, String name) {
        this.body = body;
        this.speciesClass = speciesClass;
        this.name = name;
        this.level = 1;
        this.exp = 0;
        this.downed = false;
        this.remainPoints = 0;
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

    // ==================== 属性点 ====================

    public int getRemainPoints() {
        return remainPoints;
    }

    public void setRemainPoints(int remainPoints) {
        this.remainPoints = Math.max(0, remainPoints);
    }

    /** 读取当前某项属性(索引见 ATTR_*)。 */
    public int getAttr(int index) {
        switch (index) {
            case ATTR_TI: return propTi;
            case ATTR_MO: return propMo;
            case ATTR_LI: return propLi;
            case ATTR_NAI: return propNai;
            case ATTR_MIN: return propMin;
            default: return 0;
        }
    }

    /** 读取初始模板某项属性(洗点后回到此值)。 */
    public int getStartAttr(int index) {
        switch (index) {
            case ATTR_TI: return startTi;
            case ATTR_MO: return startMo;
            case ATTR_LI: return startLi;
            case ATTR_NAI: return startNai;
            case ATTR_MIN: return startMin;
            default: return 0;
        }
    }

    public void setAttr(int index, int value) {
        value = Math.max(0, value);
        switch (index) {
            case ATTR_TI: propTi = value; break;
            case ATTR_MO: propMo = value; break;
            case ATTR_LI: propLi = value; break;
            case ATTR_NAI: propNai = value; break;
            case ATTR_MIN: propMin = value; break;
            default: break;
        }
    }

    /** 设置初始属性模板(捕捉/迁移时一次性写入)。 */
    public void setStartAttrs(int ti, int mo, int li, int nai, int min) {
        this.startTi = Math.max(0, ti);
        this.startMo = Math.max(0, mo);
        this.startLi = Math.max(0, li);
        this.startNai = Math.max(0, nai);
        this.startMin = Math.max(0, min);
    }

    /** 设置当前属性(读档/初始化)。 */
    public void setAttrs(int ti, int mo, int li, int nai, int min) {
        setAttr(ATTR_TI, ti);
        setAttr(ATTR_MO, mo);
        setAttr(ATTR_LI, li);
        setAttr(ATTR_NAI, nai);
        setAttr(ATTR_MIN, min);
    }

    /** 消耗 1 点加到指定属性; 成功返回 true。 */
    public boolean allocatePoint(int index) {
        if (remainPoints <= 0) {
            return false;
        }
        if (index < 0 || index >= ATTR_COUNT) {
            return false;
        }
        setAttr(index, getAttr(index) + 1);
        remainPoints--;
        return true;
    }

    /**
     * 某项属性可退回的下限 = 初始模板 + 升级累积的固定加点。
     * 自由加点可退到此下限，但固定成长部分不可退。
     */
    public int getAttrFloor(int index) {
        return getStartAttr(index) + (Math.max(1, level) - 1) * FIXED_POINTS_PER_ATTR_PER_LEVEL;
    }

    /** 从指定属性退回 1 点(不得低于退点下限); 成功返回 true。 */
    public boolean deallocatePoint(int index) {
        if (index < 0 || index >= ATTR_COUNT) {
            return false;
        }
        if (getAttr(index) <= getAttrFloor(index)) {
            return false;
        }
        setAttr(index, getAttr(index) - 1);
        remainPoints++;
        return true;
    }

    // ==================== 派生战斗属性 ====================

    private AddPointResult calc() {
        return RoleSystem.getInstance().caculateAddPoints(propTi, propMo, propLi, propNai, propMin);
    }

    public int getDerivedMaxHealth() {
        return BASE_HP + calc().getBlood();
    }

    public int getDerivedAttack() {
        return BASE_ATK + calc().getAttack();
    }

    public int getDerivedDefense() {
        return BASE_DEF + calc().getDefense();
    }

    public int getDerivedSpeed() {
        return BASE_SPD + calc().getSpeed();
    }

    public int getDerivedMana() {
        return BASE_MANA + calc().getMana();
    }

    /**
     * 依据当前属性重算战斗属性并写入 body。
     * @param fullHeal true=回满血(升级/疗伤); false=按当前血量比例保留。
     */
    public void recomputeStats(boolean fullHeal) {
        if (body == null) {
            return;
        }
        body.setPetCombatStats(getDerivedMaxHealth(), getDerivedAttack(), getDerivedDefense(),
                getDerivedSpeed(), getDerivedMana(), fullHeal);
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

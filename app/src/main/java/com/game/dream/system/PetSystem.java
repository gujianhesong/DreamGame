package com.game.dream.system;

import com.game.dream.bean.PetInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 战宠系统总管(单例)。
 * 负责花名册管理、削弱后概率捕捉、经验升级、出战/收回/放生/疗伤，以及与存档互转。
 * 战宠实体复用真实 {@link Enemy} 子类(保留原画与专属招式)，通过反射按种类重建。
 */
public class PetSystem {

    private static final PetSystem instance = new PetSystem();

    public static PetSystem getInstance() {
        return instance;
    }

    /** 捕捉结果码，供 GameEngine 决定提示文案。 */
    public enum CaptureOutcome {
        SUCCESS,          // 捕捉成功
        FAIL_INVALID,     // 目标无效/已死亡
        FAIL_BOSS,        // BOSS 不可捕捉
        FAIL_FULL,        // 花名册已满
        FAIL_NOT_WEAK,    // 目标血量过高，需先削弱
        FAIL_ROLL,        // 概率判定失败，怪物挣脱
        FAIL_NO_SPECIES   // 无法重建该种类(缺少 (float,float) 构造器)
    }

    /** 战宠花名册起始上限。 */
    public static final int BASE_MAX_ROSTER = 6;
    /** 每达到一个等级阶梯增加的槽位数。 */
    public static final int ROSTER_BONUS_PER_TIER = 2;
    /** 每多少玩家等级提升一个阶梯。 */
    public static final int ROSTER_LEVEL_STEP = 10;
    public static final float CAPTURE_HP_THRESHOLD = 0.40f;

    private final List<Pet> roster = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile Pet activePet = null;

    private PetSystem() {
    }

    public List<Pet> getRoster() {
        return roster;
    }

    public Pet getActivePet() {
        return activePet;
    }

    /**
     * 当前花名册上限: 起始 {@link #BASE_MAX_ROSTER} 只，玩家每 {@link #ROSTER_LEVEL_STEP} 级
     * 额外增加 {@link #ROSTER_BONUS_PER_TIER} 只(如 10 级=8, 20 级=10, 依此类推)。
     */
    public int getMaxRoster() {
        int playerLevel = 1;
        RoleInfo info = RoleSystem.getInstance().getRoleInfo();
        if (info != null) {
            playerLevel = Math.max(1, info.getLevel());
        }
        int tiers = playerLevel / ROSTER_LEVEL_STEP;
        return BASE_MAX_ROSTER + ROSTER_BONUS_PER_TIER * tiers;
    }

    public boolean isRosterFull() {
        return roster.size() >= getMaxRoster();
    }

    /** 出战指定战宠；若处于重伤状态则以 50% 血复活。 */
    public void setActive(Pet pet) {
        if (pet == null || !roster.contains(pet)) {
            return;
        }
        if (pet.isDowned()) {
            pet.setDowned(false);
            Enemy body = pet.getEntity();
            body.applyPetLevelStats(pet.getLevel());
            body.setHealth(Math.max(1, body.getMaxHealth() / 2));
        }
        activePet = pet;
    }

    /** 收回当前出战战宠(不离队)。 */
    public void recallActive() {
        activePet = null;
    }

    /** 放生战宠(永久离队)。 */
    public void release(Pet pet) {
        if (pet == null) {
            return;
        }
        roster.remove(pet);
        if (activePet == pet) {
            activePet = null;
        }
    }

    /** 疗伤: 回满血并解除重伤。 */
    public void heal(Pet pet) {
        if (pet != null) {
            pet.heal();
        }
    }

    /**
     * 削弱后概率捕捉。
     * 规则: BOSS 不可捕捉; 目标血量需低于 {@link #CAPTURE_HP_THRESHOLD};
     * 成功率 = 0.15 + (1 - 血量比例) * 0.55，上限 0.90; ELITE 结果再乘 0.4。
     */
    public CaptureOutcome tryCapture(Enemy target) {
        if (target == null || !target.isAlive() || target.isPet()) {
            return CaptureOutcome.FAIL_INVALID;
        }
        if (target.getEnemyLevel() == Enemy.EnemyLevel.BOSS) {
            return CaptureOutcome.FAIL_BOSS;
        }
        if (isRosterFull()) {
            return CaptureOutcome.FAIL_FULL;
        }
        float hpRatio = target.getMaxHealth() <= 0
                ? 1f : target.getHealth() / (float) target.getMaxHealth();
        if (hpRatio > CAPTURE_HP_THRESHOLD) {
            return CaptureOutcome.FAIL_NOT_WEAK;
        }

        float chance = 0.15f + (1f - hpRatio) * 0.55f;
        if (chance > 0.90f) {
            chance = 0.90f;
        }
        if (target.getEnemyLevel() == Enemy.EnemyLevel.ELITE) {
            chance *= 0.4f;
        }
        if (Math.random() >= chance) {
            return CaptureOutcome.FAIL_ROLL;
        }

        Enemy body = instantiateSpecies(target.getClass(), target.getX(), target.getY());
        if (body == null) {
            return CaptureOutcome.FAIL_NO_SPECIES;
        }
        body.setPet(true);
        body.resetAsPet(1);
        body.setName(target.getName());

        Pet pet = new Pet(body, target.getClass().getName(), target.getName());
        roster.add(pet);
        if (activePet == null) {
            activePet = pet;
        }
        LogUtil.i("PetSystem 捕捉成功: " + pet.getName());
        return CaptureOutcome.SUCCESS;
    }

    /** 升级到下一级所需经验。 */
    public int expToNext(int level) {
        return (int) (100 * Math.pow(Math.max(1, level), 1.5));
    }

    /**
     * 增加战宠经验，返回是否发生升级(可能连升多级)。
     */
    public boolean addExp(Pet pet, int amount) {
        if (pet == null || amount <= 0) {
            return false;
        }
        pet.setExp(pet.getExp() + amount);
        boolean leveled = false;
        while (pet.getExp() >= expToNext(pet.getLevel())) {
            pet.setExp(pet.getExp() - expToNext(pet.getLevel()));
            pet.setLevel(pet.getLevel() + 1);
            pet.getEntity().applyPetLevelStats(pet.getLevel());
            leveled = true;
        }
        return leveled;
    }

    // ==================== 存档互转 ====================

    public List<PetInfo> toSaveInfos() {
        List<PetInfo> list = new ArrayList<>();
        for (Pet pet : roster) {
            list.add(new PetInfo(
                    pet.getSpeciesClass(),
                    pet.getName(),
                    pet.getLevel(),
                    pet.getExp(),
                    pet.getHpRatio(),
                    pet == activePet
            ));
        }
        return list;
    }

    public void loadFrom(List<PetInfo> infos) {
        roster.clear();
        activePet = null;
        if (infos == null) {
            return;
        }
        for (PetInfo info : infos) {
            if (info == null || info.getSpeciesClass() == null) {
                continue;
            }
            Class<?> clazz;
            try {
                clazz = Class.forName(info.getSpeciesClass());
            } catch (Exception e) {
                LogUtil.i("PetSystem 未知战宠种类: " + info.getSpeciesClass());
                continue;
            }
            Enemy body = instantiateSpecies(clazz, 0, 0);
            if (body == null) {
                continue;
            }
            body.setPet(true);
            int level = Math.max(1, info.getLevel());
            body.resetAsPet(level);
            body.setName(info.getName());

            float ratio = Math.max(0.05f, Math.min(1f, info.getHpRatio()));
            body.setHealth((int) (body.getMaxHealth() * ratio));

            Pet pet = new Pet(body, info.getSpeciesClass(), info.getName());
            pet.setLevel(level);
            pet.setExp(Math.max(0, info.getExp()));
            roster.add(pet);
            if (info.isActive()) {
                activePet = pet;
            }
        }
    }

    /** 反射按种类重建 Enemy 实例(要求存在 (float, float) 构造器)。 */
    private Enemy instantiateSpecies(Class<?> clazz, float x, float y) {
        try {
            return (Enemy) clazz.getConstructor(float.class, float.class).newInstance(x, y);
        } catch (Exception e) {
            LogUtil.i("PetSystem 重建战宠失败: " + clazz.getName() + " " + e.getMessage());
            return null;
        }
    }
}

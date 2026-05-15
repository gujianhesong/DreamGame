package com.game.dream;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;

import java.util.List;

public class BattleUtil {

    /**
     * 计算人物输出的物理伤害
     *
     * @param enemy
     * @return
     */
    public static AttackResult caculatePlayerAttackDamage(Enemy enemy) {
        boolean isCrit;
        boolean isHit;
        int damageValue;

        //计算命中
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        int playerHit = roleInfo.getHit();
        int enemyDodge = (int) (enemy.getSpeed() * 1.2);
        float value = (playerHit - enemyDodge * 5) / 20000f;
        value = Math.max(-0.1f, Math.min(0.1f, value));
        float dodgeRatio = 0.1f - value;
        if (Math.random() < dodgeRatio) {
            //未命中
            isHit = false;
            isCrit = false;
            damageValue = 0;
        } else {
            //命中
            isHit = true;
            isCrit = false;

            int playerAttack = roleInfo.getAttack();
            int enemyDefense = enemy.getDefense();

            //计算修炼加成
            if (roleInfo.getPracticeAttack() > 0) {
                for (int i = 0; i < roleInfo.getPracticeAttack(); i++) {
                    playerAttack = (int) (playerAttack * 1.02 + 5);
                }
            }

            //计算伤害
            damageValue = calculateAttackDamage(playerAttack, enemyDefense);
            damageValue = (int) (damageValue * (0.9 + Math.random() * 0.2));

            float critRatio = 0.05f;
            if (Math.random() < critRatio) {
                //暴击几率
                isCrit = true;
                damageValue *= 2;
            }

            damageValue = Math.max(damageValue, 1);
        }

        AttackResult attackResult = new AttackResult();
        attackResult.damageValue = damageValue;
        attackResult.isCrit = isCrit;
        attackResult.isHit = isHit;

        LogUtil.i("aaaaaaaaaaaaaaa 角色物理输出伤害 " + damageValue + ", 暴击:" + isCrit + ", 命中:" + isHit);

        return attackResult;
    }

    private static int calculateAttackDamage(float attack, int defense) {
        int damageValue = (int) (attack * 0.2 + (attack - defense) * 1.1);
        damageValue = Math.max(0, damageValue);
        damageValue += (int) (attack * 0.05);
        return damageValue;
    }

    /**
     * 计算人物输出的法术伤害
     *
     * @param enemy
     * @param skillType
     * @return
     */
    public static AttackResult caculatePlayerCasterDamage(Enemy enemy, SkillType skillType) {
        List<SkillInfo> playerSkills = SkillSystem.getInstance().getPlayerSkills();
        SkillInfo findSkillInfo = null;
        for (SkillInfo item : playerSkills) {
            if (item.getSkillType() == skillType) {
                findSkillInfo = item;
                break;
            }
        }
        if (findSkillInfo == null || findSkillInfo.getLevel() <= 0) {
            return null;
        }

        boolean isCrit;
        boolean isHit;
        int damageValue = 0;

        //计算命中
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        int playerHit = roleInfo.getHit();
        int enemyDodge = (int) (enemy.getSpeed() * 1.2);
        float value = (playerHit - enemyDodge * 5) / 20000f;
        value = Math.max(-0.1f, Math.min(0.1f, value));
        float dodgeRatio = 0.1f - value;
        if (Math.random() < dodgeRatio) {
            //未命中
            isHit = false;
            isCrit = false;
        } else {
            //命中
            isHit = true;
            isCrit = false;

            float castBaseValue = 0f;
            switch (skillType){
                case MAIN_FIREBALL:
                    castBaseValue = 20f;
                    break;
                case MAIN_ICE_BOLT:
                    castBaseValue = 15f;
                    break;
                case MAIN_LIGHTNING:
                    castBaseValue = 30f;
                    break;
                case MAIN_WanJianGuiZong:
                    castBaseValue = 25f;
                    break;
            }

            if(castBaseValue > 0){
                //计算法术伤害
                damageValue = calculateMagicDamage(castBaseValue, roleInfo.getMana(), enemy.getMana(), findSkillInfo.getLevel());

                //计算修炼加成
                if (roleInfo.getPracticeMagic() > 0) {
                    for (int i = 0; i < roleInfo.getPracticeMagic(); i++) {
                        damageValue = (int) (damageValue * 1.02 + 5);
                    }
                }

                //浮动伤害
                damageValue = (int) (damageValue * (0.9 + Math.random() * 0.2));

                float critRatio = 0.01f;
                if (Math.random() < critRatio) {
                    //暴击几率
                    isCrit = true;
                    damageValue *= 2;
                }

                damageValue = Math.max(damageValue, 1);
            }
        }

        AttackResult attackResult = new AttackResult();
        attackResult.damageValue = damageValue;
        attackResult.isCrit = isCrit;
        attackResult.isHit = isHit;

        LogUtil.i("aaaaaaaaaaaaaaa 角色法术输出伤害 " + damageValue + ", 暴击:" + isCrit + ", 命中:" + isHit
                + ", " + roleInfo.getMana() + ", " + enemy.getMana());

        return attackResult;
    }

    private static int calculateMagicDamage(float baseDamage, int casterSpirit,
                                           int acceptSpirit, int skillLevel) {
        float spiritDelta = casterSpirit - acceptSpirit;
        float playerSpiritMultiplier = (float)Math.pow(casterSpirit, 0.5f); // 平方根软化

        // 3. Skill level multiplier
        float skillMultiplier = 1.0f + (skillLevel - 1) * 0.15f;
        skillMultiplier = Math.min(skillMultiplier, 3.0f);

        // 4. Calculate final damage
        float finalDamage = spiritDelta * 0.3f + baseDamage * skillMultiplier * (0.45f + playerSpiritMultiplier * 0.55f);

//        LogUtil.i("baseDamage  spiritDelta:" + spiritDelta + ",playerSpiritMultiplier:" + playerSpiritMultiplier);
//        LogUtil.i("baseDamage:" + baseDamage + ",playerSpirit:" + playerSpirit + ",enemySpirit:" + enemySpirit
//                + ",skillLevel:" + skillLevel + ",result:" + finalDamage);

        // Random variance ±10%
        float variance = 0.9f + (float)(Math.random() * 0.2);
        finalDamage *= variance;

        int result = Math.max(1, (int)finalDamage);

        LogUtil.i("baseDamage:" + baseDamage + ",casterSpirit:" + casterSpirit + ",acceptSpirit:" + acceptSpirit
                + ",skillLevel:" + skillLevel + ",result:" + result);
        return result;
    }

    public static void testMagicDamage(){
        for (int playerMana = 30; playerMana <= 1000; playerMana += 30) {
            for (int enemyMana = 30; enemyMana <= 1000; enemyMana += 30) {
                BattleUtil.calculateMagicDamage(20, playerMana, enemyMana, 1);
            }
        }
    }

    public static int calculateMagicDamage2(float baseDamage, int playerSpirit,
                                            int enemySpirit, int skillLevel) {
        // 1. Spirit ratio (player vs enemy)
        float spiritRatio = (float)playerSpirit / (playerSpirit + enemySpirit);
        float spiritMultiplier2 = (float) Math.pow(Math.abs(playerSpirit), 0.5f); // 平方根软化

        // 2. Damage multiplier based on ratio
        // Ratio 0.5 → 0.6x damage (被压制)
        // Ratio 1.0 → 1.0x damage (平等)
        // Ratio 2.0 → 1.5x damage (压制)
        // Ratio 3.0 → 1.8x damage (强力压制)
        float spiritMultiplier = (float)Math.pow(spiritRatio, 0.5f); // 平方根软化
        spiritMultiplier = Math.max(0.02f, Math.min(spiritMultiplier, 10f));

        // 3. Skill level multiplier
        float skillMultiplier = 1.0f + (skillLevel - 1) * 0.15f;
        skillMultiplier = Math.min(skillMultiplier, 3.0f);

        // 4. Calculate final damage
        float finalDamage = baseDamage * (spiritMultiplier2 * 0.05f + spiritMultiplier * skillMultiplier * 0.5f);

        LogUtil.i("baseDamage:" + baseDamage + ",playerSpirit:" + playerSpirit + ",enemySpirit:" + enemySpirit
                + ",skillLevel:" + skillLevel + ",result:" + finalDamage + ",spiritMultiplier:" + spiritMultiplier);

        // Random variance ±10%
//        float variance = 0.9f + (float)(Math.random() * 0.2);
//        finalDamage *= variance;
//
//        int result = Math.max(1, (int)finalDamage);

        //LogUtil.i("baseDamage=====spiritMultiplier1:" + spiritMultiplier1 + ",spiritMultiplier2:" + spiritMultiplier2);
//        LogUtil.i("baseDamage:" + baseDamage + ",playerSpirit:" + playerSpirit + ",enemySpirit:" + enemySpirit
//                + ",skillLevel:" + skillLevel + ",result:" + result);
//        return result;
        return 0;
    }

    /**
     * 计算敌人输出的物理伤害
     *
     * @param enemy
     * @return
     */
    public static AttackResult caculateEnemyAttackDamage(Enemy enemy) {
        boolean isCrit;
        boolean isHit;
        int damageValue;

        //计算命中
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        int enemyHit = (int) (enemy.getSpeed() * 1.2);
        int playerDodge = roleInfo.getDodge();
        float value = (enemyHit - playerDodge) / 2000f;
        value = Math.max(-0.1f, Math.min(0.1f, value));
        float dodgeRatio = 0.1f - value;

        if (Math.random() < dodgeRatio) {
            //未命中
            isHit = false;
            isCrit = false;
            damageValue = 0;
        } else {
            //命中
            isHit = true;
            isCrit = false;

            int enemyAttack = enemy.getAttackDamage();
            int playerDefense = roleInfo.getDefense();

            //计算修炼加成
            if (roleInfo.getPracticeDefense() > 0) {
                for (int i = 0; i < roleInfo.getPracticeDefense(); i++) {
                    playerDefense = (int) (playerDefense * 1.02 + 5);
                }
            }

            //计算伤害
            damageValue = calculateAttackDamage(enemyAttack, playerDefense);
            damageValue = (int) (damageValue * (0.9 + Math.random() * 0.2));

            float critRatio = 0.05f;
            if (Math.random() < critRatio) {
                //暴击几率
                isCrit = true;
                damageValue *= 2;
            }

            damageValue = Math.max(damageValue, 1);
        }

        AttackResult attackResult = new AttackResult();
        attackResult.damageValue = damageValue;
        attackResult.isCrit = isCrit;
        attackResult.isHit = isHit;

        LogUtil.i("aaaaaaaaaaaaaaa 怪物物理输出伤害 " + damageValue + ", 暴击:" + isCrit + ", 命中:" + isHit);

        return attackResult;
    }

    /**
     * 计算怪物输出的法术伤害
     *
     * @param skillType
     * @return
     */
    public static AttackResult caculateEnemyCasterDamage(Enemy enemy, SkillType skillType, int skillLevel) {
        boolean isCrit = false;
        boolean isHit = false;
        int damageValue = 0;

        //计算命中
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        int enemyHit = (int) (enemy.getSpeed() * 1.2);
        int playerDodge = roleInfo.getDodge();
        float value = (enemyHit - playerDodge) / 2000f;
        value = Math.max(-0.1f, Math.min(0.1f, value));
        float dodgeRatio = 0.1f - value;
        if (Math.random() < dodgeRatio) {
            //未命中
            isHit = false;
            isCrit = false;
            damageValue = 0;
        } else {
            //命中
            isHit = true;
            isCrit = false;

            float castBaseValue = 0f;
            switch (skillType){
                case MAIN_FIREBALL:
                    castBaseValue = 10f;
                    break;
                case MAIN_ICE_BOLT:
                    castBaseValue = 7f;
                    break;
                case MAIN_LIGHTNING:
                    castBaseValue = 14f;
                    break;
            }

            if(castBaseValue > 0){
                //计算法术伤害
                damageValue = calculateEnemyMagicDamage(castBaseValue, enemy.getMana(), roleInfo.getMana(), skillLevel);
                LogUtil.i("aaaaaaaaaaaaaaa 怪物法术输出伤害 " + damageValue);
                //计算修炼加成
                if (roleInfo.getPracticeMagicDefense() > 0) {
                    for (int i = 0; i < roleInfo.getPracticeMagicDefense(); i++) {
                        damageValue = (int) (damageValue * 0.98 - 5);
                    }
                }

                //浮动伤害
                damageValue = (int) (damageValue * (0.9 + Math.random() * 0.2));

                float critRatio = 0.1f;
                if (Math.random() < critRatio) {
                    //暴击几率
                    isCrit = true;
                    damageValue *= 2;
                }

                damageValue = Math.max(damageValue, 1);
            }
        }

        AttackResult attackResult = new AttackResult();
        attackResult.damageValue = damageValue;
        attackResult.isCrit = isCrit;
        attackResult.isHit = isHit;

        LogUtil.i("aaaaaaaaaaaaaaa 怪物法术输出伤害 " + damageValue + ", 暴击:" + isCrit + ", 命中:" + isHit
                + ", " + roleInfo.getMana() + ", " + enemy.getMana());

        return attackResult;
    }

    private static int calculateEnemyMagicDamage(float baseDamage, int casterSpirit,
                                            int acceptSpirit, int skillLevel) {
        float spiritDelta = casterSpirit - acceptSpirit;
        float playerSpiritMultiplier = (float)Math.pow(casterSpirit, 0.5f); // 平方根软化

        // 3. Skill level multiplier
        float skillMultiplier = 1.0f + (skillLevel - 1) * 0.15f;
        skillMultiplier = Math.min(skillMultiplier, 3.0f);

        // 4. Calculate final damage
        float finalDamage = spiritDelta * 0.3f + baseDamage * skillMultiplier * (0.45f + playerSpiritMultiplier * 0.4f);

//        LogUtil.i("baseDamage  spiritDelta:" + spiritDelta + ",playerSpiritMultiplier:" + playerSpiritMultiplier);
//        LogUtil.i("baseDamage:" + baseDamage + ",playerSpirit:" + playerSpirit + ",enemySpirit:" + enemySpirit
//                + ",skillLevel:" + skillLevel + ",result:" + finalDamage);

        // Random variance ±10%
        float variance = 0.9f + (float)(Math.random() * 0.2);
        finalDamage *= variance;

        int result = Math.max(1, (int)finalDamage);

        LogUtil.i("baseDamage:" + baseDamage + ",casterSpirit:" + casterSpirit + ",acceptSpirit:" + acceptSpirit
                + ",skillLevel:" + skillLevel + ",result:" + result);
        return result;
    }
}

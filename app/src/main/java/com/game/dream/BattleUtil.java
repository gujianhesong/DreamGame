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
            damageValue = (int) ((playerAttack - enemyDefense) * 1.2);
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
            damageValue = (int) ((enemyAttack - playerDefense) * 1.2);
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

            //计算法术伤害，灵力差 * 技能等级 * 法术基础伤害
            float manaRatioValue = (20 + (roleInfo.getMana() - enemy.getMana()) * 0.3f);
            manaRatioValue = Math.max(manaRatioValue, 1);
            float leveRatioValue = 1 + findSkillInfo.getLevel() * 0.1f;
            float castBaseValue = 4f;
            switch (skillType){
                case FIREBALL:
                    castBaseValue = 4f;
                    break;
                case ICE_BOLT:
                    castBaseValue = 4.5f;
                    break;
                case LIGHTNING:
                    castBaseValue = 5f;
                    break;
            }
            damageValue = (int) (manaRatioValue * leveRatioValue * castBaseValue);

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

        AttackResult attackResult = new AttackResult();
        attackResult.damageValue = damageValue;
        attackResult.isCrit = isCrit;
        attackResult.isHit = isHit;

        LogUtil.i("aaaaaaaaaaaaaaa 角色法术输出伤害 " + damageValue + ", 暴击:" + isCrit + ", 命中:" + isHit
                + ", " + roleInfo.getMana() + ", " + enemy.getMana());

        return attackResult;
    }
}

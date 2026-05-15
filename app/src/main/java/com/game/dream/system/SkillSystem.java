package com.game.dream.system;

import com.game.dream.FloatingText;
import com.game.dream.GameEngine;
import com.game.dream.LogUtil;
import com.game.dream.Player;
import com.game.dream.Projectile;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.bean.SkillStartInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;
import com.game.dream.skill.SkillEffect;

import java.util.ArrayList;
import java.util.List;

public class SkillSystem {

    private static SkillSystem instance = new SkillSystem();

    public static SkillSystem getInstance() {
        return instance;
    }

    private SkillSystem() {
        initMainSkills();
        initAssistSkills();
        initPractiseSkills();

        equippedActiveSkills = new ArrayList<>();
    }

    private List<SkillInfo> playerMainSkills = new ArrayList<>();
    private List<SkillInfo> playerAssistSkills = new ArrayList<>();
    private List<SkillInfo> playerPractiseSkills = new ArrayList<>();

    private List<SkillInfo> equippedActiveSkills;
    private int currentSkillPage = 0; // 0 for first page, 1 for second page
    public static final int SKILLS_PER_PAGE = 5;
    private static final int MAX_EQUIPED_ACTIVE_SKILLS = 15;

    private void initMainSkills() {
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_FIREBALL, 1, 10, 3, "火云术", "发射火焰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_ICE_BOLT, 1, 10, 4, "寒冰术", "发射寒冰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_LIGHTNING, 1, 10, 5, "雷击术", "发射几道闪电对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_ROOT, 1, 10, 8, "定身术", "发射符咒定住敌人，使其无法移动"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_WanJianGuiZong, 1, 10, 10, "万剑归宗", "发动剑阵对范围内的敌人造成多次伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_JinGangHuTi, 1, 10, 10, "金刚护体", "获得金刚护体效果后大幅降低受到的伤害，期间不会死亡，持续5秒"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_MiaoShouHuiChun, 1, 10, 15, "妙手回春", "消耗法力值恢复大量气血"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_LianQiHuaShen, 1, 10, 20, "炼气化神", "消耗气血值恢复大量魔法"));
    }

    private void initAssistSkills() {
        playerAssistSkills.add(new SkillInfo(SkillType.AST_QiangShen, 0, 100, "强身术", "强身之术，学习后可以提高人物的气血上限"));
        playerAssistSkills.add(new SkillInfo(SkillType.AST_MingXiang, 0, 100, "冥想", "冥想之法，学习后可以提高人物的魔法上限"));
        playerAssistSkills.add(new SkillInfo(SkillType.AST_ShenSu, 0, 100, "神速", "千里神速，学习后可以提高人物的速度"));
        playerAssistSkills.add(new SkillInfo(SkillType.AST_JianShen, 0, 100, "健身术", "健身之术，学习后可以提高人物的体力上限"));
        playerAssistSkills.add(new SkillInfo(SkillType.AST_YangShengZhiDao, 0, 100, "养生之道", "养生之道，学习后可以提高人物的活力上限"));
        playerAssistSkills.add(new SkillInfo(SkillType.AST_ZhuangbeiZhiZao, 0, 100, "装备制造", "装备制造之术，学习后可以制造相应等级的装备"));
    }

    private void initPractiseSkills() {
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_AttackPractise, 0, 25, "攻击修炼", "提升人物的攻击伤害"));
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_MagicPractise, 0, 25, "法术修炼", "提升人物的法术伤害"));
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_DefensePractise, 0, 25, "防御修炼", "降低人物受到的攻击伤害"));
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_MagicDefensePractise, 0, 25, "法术防御修炼", "降低人物受到的法术伤害"));

        playerPractiseSkills.add(new SkillInfo(SkillType.PST_BB_AttackPractise, 0, 25, "宝宝攻击修炼", "提升宝宝的攻击伤害"));
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_BB_MagicPractise, 0, 25, "宝宝法术修炼", "提升宝宝的法术伤害"));
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_BB_DefensePractise, 0, 25, "宝宝防御修炼", "降低宝宝受到的攻击伤害"));
        playerPractiseSkills.add(new SkillInfo(SkillType.PST_BB_MagicDefensePractise, 0, 25, "宝宝法术防御修炼", "降低宝宝受到的法术伤害"));
    }

    public List<SkillInfo> getPlayerSkills() {
        return playerMainSkills;
    }

    public SkillInfo getPlayerSkill(SkillType skillType) {
        for (SkillInfo skillInfo : playerMainSkills) {
            if (skillInfo.getSkillType() == skillType) {
                return skillInfo;
            }
        }
        return null;
    }

    public void setMainSkillInfos(List<SkillInfo> skillInfos) {
        if (skillInfos != null) {
            for (SkillInfo skillInfo : playerMainSkills) {
                for (SkillInfo item : skillInfos) {
                    if (item.getSkillType() == skillInfo.getSkillType()) {
                        skillInfo.setLevel(item.getLevel());
                    }
                }
            }
        }
    }

    public void setAssistSkillInfos(List<SkillInfo> skillInfos) {
        if (skillInfos != null) {
            for (SkillInfo skillInfo : playerAssistSkills) {
                for (SkillInfo item : skillInfos) {
                    if (item.getSkillType() == skillInfo.getSkillType()) {
                        skillInfo.setLevel(item.getLevel());
                    }
                }
            }
        }
    }

    public void setPractiseSkillInfos(List<SkillInfo> skillInfos) {
        if (skillInfos != null) {
            for (SkillInfo skillInfo : playerPractiseSkills) {
                for (SkillInfo item : skillInfos) {
                    if (item.getSkillType() == skillInfo.getSkillType()) {
                        skillInfo.setLevel(item.getLevel());
                    }
                }
            }
        }
    }

    public List<SkillInfo> getMainSkillInfos() {
        return new ArrayList<>(playerMainSkills);
    }

    public List<SkillInfo> getAssistSkillInfos() {
        return new ArrayList<>(playerAssistSkills);
    }

    public List<SkillInfo> getPractiseSkillInfos() {
        return new ArrayList<>(playerPractiseSkills);
    }

    public List<SkillInfo> getEquippedActiveSkills() {
        return equippedActiveSkills;
    }

    public void setEquippedActiveSkills(List<SkillInfo> skillInfos) {
        equippedActiveSkills.clear();
        if (skillInfos != null) {
            for (SkillInfo skillInfo : skillInfos) {
                for (SkillInfo skillItem : playerMainSkills) {
                    if (skillInfo.getSkillType() == skillItem.getSkillType()) {
                        equipSkill(skillItem);
                        break;
                    }
                }
            }
        }
    }

    public boolean equipSkill(SkillInfo skill) {
        // Only main skills for now
        if (!skill.isMainSkill()) return false;
        if (equippedActiveSkills.contains(skill)) return false;
        if (equippedActiveSkills.size() >= MAX_EQUIPED_ACTIVE_SKILLS) return false;

        equippedActiveSkills.add(skill);
        return true;
    }

    public void unequipSkill(SkillInfo skill) {
        equippedActiveSkills.remove(skill);
    }

    public List<SkillInfo> getCurrentPageSkills() {
        int start = currentSkillPage * SKILLS_PER_PAGE;
        int end = Math.min(start + SKILLS_PER_PAGE, equippedActiveSkills.size());

        if (start >= equippedActiveSkills.size()) {
            return new ArrayList<>(); // Return empty list if page is out of bounds
        }

        return new ArrayList<>(equippedActiveSkills.subList(start, end));
    }

    public void nextPage() {
        // Check if there are more skills to show on the next page
        if ((currentSkillPage + 1) * SKILLS_PER_PAGE < equippedActiveSkills.size()) {
            currentSkillPage++;
        } else {
            currentSkillPage = 0; // Loop back to first page
        }
    }

    public int getCurrentPageIndex() {
        return currentSkillPage;
    }

    public int getSkillIndex(int buttonIndex) {
        return currentSkillPage * SKILLS_PER_PAGE + buttonIndex;
    }

    public SkillStartInfo castSkill(SkillInfo skill) {
        LogUtil.d("Casting skill: " + skill.getName());

        Player player = GameEngine.getInstance().getPlayer();
        long currentTime = System.currentTimeMillis();
        if (currentTime - player.getLastMagicTime(skill.getSkillType()) < skill.getCooldownSeconds() * 1000L) {
            return null; // Still on cooldown
        }

        if (skill.getSkillType() == SkillType.MAIN_LianQiHuaShen) {
            int costBlood = 50;
            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
            if (roleInfo.getHp() < costBlood * 3) {
                GameEngine.getInstance().showCenterToast("气血不足", 1000);
                return null;
            }
            roleInfo.setHp(roleInfo.getHp() - costBlood);
        } else {
            int costMagic = 20;
            if (skill.getSkillType() == SkillType.MAIN_WanJianGuiZong) {
                costMagic = 40;
            } else if (skill.getSkillType() == SkillType.MAIN_MiaoShouHuiChun) {
                costMagic = 30;
            }
            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
            if (roleInfo.getMp() < costMagic) {
                GameEngine.getInstance().showCenterToast("魔法不足", 1000);
                return null;
            }
            roleInfo.setMp(roleInfo.getMp() - costMagic);
        }

        player.setLastMagicTime(skill.getSkillType(), currentTime);

        SkillStartInfo skillStartInfo = null;
        switch (skill.getSkillType()) {
            case MAIN_FIREBALL:
            case MAIN_ICE_BOLT:
            case MAIN_LIGHTNING:
            case MAIN_ROOT: {
                skillStartInfo = castTripleSpell(skill.getSkillType());
            }
            break;
            case MAIN_WanJianGuiZong: {
                skillStartInfo = castSwordStorm();
            }
            break;
            case MAIN_JinGangHuTi: {
                // Activate buff for 5 seconds
                player.activateDiamondBody(5000);
                GameEngine.getInstance().showCenterToast("获得金刚护体!", 1000);
            }
            break;
            case MAIN_MiaoShouHuiChun: {
                float ratio = 0.1f + skill.getLevel() * 0.02f;
                int healAmount = (int) (RoleSystem.getInstance().getRoleInfo().getBloodCap() * ratio) + 50;
                player.healBlood(healAmount);
                GameEngine.getInstance().showFloatText("恢复气血 +" + healAmount, FloatingText.Type.HEAL);
            }
            break;
            case MAIN_LianQiHuaShen: {
                float ratio = 0.1f + skill.getLevel() * 0.02f;
                int healAmount = (int) (RoleSystem.getInstance().getRoleInfo().getMagicCap() * ratio) + 50;
                player.healMagic(healAmount);
                GameEngine.getInstance().showFloatText("恢复魔法 +" + healAmount, FloatingText.Type.HEAL_MAGIC);
            }
            break;
        }
        return skillStartInfo;
    }

    /**
     * Cast triple spell
     */
    public SkillStartInfo castTripleSpell(SkillType skillType) {
        SkillStartInfo skillStartInfo = new SkillStartInfo();
        List<Projectile> spells = new ArrayList<>();
        skillStartInfo.setProjectiles(spells);

        if (skillType == SkillType.MAIN_ROOT) {
            spells.addAll(castRootSpell());
        } else {
            float baseAngle = 0;

            // Determine base angle from facing direction
            Player player = GameEngine.getInstance().getPlayer();
            switch (player.getFacingDirection()) {
                case 0:
                    baseAngle = 90;
                    break;  // Down
                case 1:
                    baseAngle = -90;
                    break; // Up
                case 2:
                    baseAngle = 180;
                    break; // Left
                case 3:
                    baseAngle = 0;
                    break;   // Right
            }

            // Create 3 projectiles with 30 degree separation
            float[] angles = null;
            float range = 300;
            switch (skillType) {
                case MAIN_FIREBALL:
                    angles = new float[]{baseAngle - 60, baseAngle - 40, baseAngle - 20, baseAngle,
                            baseAngle + 20, baseAngle + 40, baseAngle + 60};
                    range = 400;
                    break;
                case MAIN_ICE_BOLT:
                    angles = new float[12];
                    for (int i = 0; i < 12; i++) {
                        angles[i] = baseAngle + 30 * i;
                    }
                    range = 300;
                    break;
                case MAIN_LIGHTNING:
                    angles = new float[]{baseAngle - 90, baseAngle, baseAngle + 90, baseAngle + 180};
                    range = 1000;
                    break;
            }

            for (float angle : angles) {
                // Convert angle to radians
                double rad = Math.toRadians(angle);

                // Calculate target position
                float spellTargetX = player.getX() + (float) (Math.cos(rad) * range);
                float spellTargetY = player.getY() + (float) (Math.sin(rad) * range);

                // Cast triple spell (returns list of 3 projectiles)
                spells.add(new Projectile(player.getX(), player.getY(), spellTargetX, spellTargetY, skillType));
            }
        }

        return skillStartInfo;
    }

    private List<Projectile> castRootSpell() {
        // Find the nearest enemy within range
        Enemy target = null;
        float minDist = Float.MAX_VALUE;
        float spellRange = 300f;
        List<Projectile> projectiles = new ArrayList<>();

        Player player = GameEngine.getInstance().getPlayer();
        List<Enemy> enemies = GameEngine.getInstance().getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                float dx = enemy.getX() - player.getX();
                float dy = enemy.getY() - player.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < spellRange && dist < minDist) {
                    minDist = dist;
                    target = enemy;
                }
            }
        }

        if (target != null) {
            Projectile rootProj = new Projectile(
                    player.getX(),
                    player.getY(),
                    target.getX(),
                    target.getY(),
                    SkillType.MAIN_ROOT // Use an existing visual type or add a TALISMAN type
            );

            // Set the effect type to ROOT
            rootProj.setEffectType(Projectile.EffectType.ROOT);
            projectiles.add(rootProj);
        } else {
            GameEngine.getInstance().showCenterToast("范围内没有目标", 1000);
        }
        return projectiles;
    }

    private SkillStartInfo castSwordStorm() {
        SkillStartInfo skillStartInfo = new SkillStartInfo();
        // Center the storm on the player
        float radius = 800; // Large range
        long duration = 4000;

        Player player = GameEngine.getInstance().getPlayer();
        SkillEffect skillEffect = new SkillEffect(
                SkillEffect.Type.SWORD_STORM,
                player.getX(),
                player.getY(),
                radius,
                duration,
                800, // Damage every 0.3s
                5    // Total 5 hits
        );
        skillStartInfo.setSkillEffect(skillEffect);

        GameEngine.getInstance().showCenterToast("万剑归宗", 1000);

        return skillStartInfo;
    }

}

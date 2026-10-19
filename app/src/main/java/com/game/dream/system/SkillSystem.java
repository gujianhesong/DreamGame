package com.game.dream.system;

import com.game.dream.ui.FloatingText;
import com.game.dream.GameEngine;
import com.game.dream.utils.LogUtil;
import com.game.dream.figure.Player;
import com.game.dream.ui.Projectile;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.bean.SkillStartInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.item.EquipmentItem;
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
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_DunXing, 1, 1, 30, "遁形", "施展遁形之术，瞬间返回清溪村"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_SwordQi, 0, 10, 1, "剑气", "挥剑发出一道剑气。冷却短、耗蓝少，适合中距离点射。升级加快飞行速度并略微缩短冷却，高等级可少量穿透。"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_FIREBALL, 0, 10, 3, "火云术", "发射火焰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_ICE_BOLT, 0, 10, 4, "寒冰术", "发射寒冰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_LIGHTNING, 0, 10, 5, "雷击术", "发射几道闪电对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_FeiShaZouShi, 0, 10, 5, "飞沙走石", "召唤沙暴席卷周围，对范围内敌人造成持续伤害并减速"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_ROOT, 0, 10, 8, "定身术", "发射符咒定住敌人，使其无法移动"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_WanJianGuiZong, 0, 10, 10, "万剑归宗", "发动剑阵对范围内的敌人造成多次伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_JinGangHuTi, 0, 10, 10, "金刚护体", "获得金刚护体效果后大幅降低受到的伤害，期间不会死亡，持续5秒"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_MiaoShouHuiChun, 0, 10, 15, "妙手回春", "消耗法力值恢复大量气血"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_LianQiHuaShen, 0, 10, 20, "炼气化神", "消耗气血值恢复大量魔法"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_DuWuZhen, 0, 10, 15, "毒雾阵", "释放一片剧毒雾气，对范围内敌人造成持续伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_ChongCi, 0, 10, 0, "冲刺",
                "双击方向键冲刺。升级增加距离与集气，5级起有无敌，满级冲撞带伤。"));
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

    /** 冲刺技能等级，0 表示尚未学会。 */
    public int getChongCiLevel() {
        SkillInfo skill = getPlayerSkill(SkillType.MAIN_ChongCi);
        return skill == null ? 0 : Math.max(0, skill.getLevel());
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
        clampAssistLevelsToCap();
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
        clampPracticeLevelsToCap();
    }

    /** 升到 N 级的金币：1000 × N × N */
    public static int getPracticeUpgradeCost(int nextLevel) {
        if (nextLevel <= 0) {
            return 0;
        }
        return 1000 * nextLevel * nextLevel;
    }

    /** 修炼上限 = min(25, 人物等级 / 4) */
    public int getPracticeLevelCap() {
        int playerLevel = Math.max(1, RoleSystem.getInstance().getRoleInfo().getLevel());
        return Math.min(25, playerLevel / 4);
    }

    public void clampPracticeLevelsToCap() {
        int cap = getPracticeLevelCap();
        for (SkillInfo skill : playerPractiseSkills) {
            if (skill.getLevel() > cap) {
                skill.setLevel(cap);
            }
        }
    }

    public boolean tryUpgradePractice(SkillInfo skill) {
        if (skill == null || !skill.isPractiseSkill()) {
            return false;
        }
        int nextLevel = skill.getLevel() + 1;
        if (nextLevel > skill.getMaxLevel()) {
            GameEngine.getInstance().showCenterToast(skill.getName() + " 已达满级", 1800);
            return false;
        }
        int cap = getPracticeLevelCap();
        if (nextLevel > cap) {
            int needRoleLevel = nextLevel * 4;
            GameEngine.getInstance().showCenterToast(
                    "人物需达到 " + needRoleLevel + " 级才能修炼到 " + nextLevel + " 级（当前上限 " + cap + "）", 2500);
            return false;
        }
        int cost = getPracticeUpgradeCost(nextLevel);
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        if (roleInfo.getMoney() < cost) {
            GameEngine.getInstance().showCenterToast("金钱不足，需要 " + cost, 2000);
            return false;
        }
        roleInfo.setMoney(roleInfo.getMoney() - cost);
        skill.setLevel(nextLevel);
        String action = nextLevel == 1 ? "学会了" : ("升至 " + nextLevel + " 级");
        GameEngine.getInstance().showCenterToast(skill.getName() + " " + action, 2000);
        return true;
    }

    /** 升到 N 级的金币：30 × N × N */
    public static int getAssistUpgradeCost(int nextLevel) {
        if (nextLevel <= 0) {
            return 0;
        }
        return 30 * nextLevel * nextLevel;
    }

    /** 升到 N 级的经验：与金币相同，30 × N² */
    public static int getAssistUpgradeExpCost(int nextLevel) {
        return getAssistUpgradeCost(nextLevel);
    }

    /** 辅助上限 = min(100, 人物等级) */
    public int getAssistLevelCap() {
        int playerLevel = Math.max(1, RoleSystem.getInstance().getRoleInfo().getLevel());
        return Math.min(100, playerLevel);
    }

    public void clampAssistLevelsToCap() {
        int cap = getAssistLevelCap();
        for (SkillInfo skill : playerAssistSkills) {
            if (skill.getLevel() > cap) {
                skill.setLevel(cap);
            }
        }
    }

    public boolean tryUpgradeAssist(SkillInfo skill) {
        if (skill == null || !skill.isAssistSkill()) {
            return false;
        }
        int nextLevel = skill.getLevel() + 1;
        if (nextLevel > skill.getMaxLevel()) {
            GameEngine.getInstance().showCenterToast(skill.getName() + " 已达满级", 1800);
            return false;
        }
        int cap = getAssistLevelCap();
        if (nextLevel > cap) {
            GameEngine.getInstance().showCenterToast(
                    "人物需达到 " + nextLevel + " 级才能学习到 " + nextLevel + " 级（当前上限 " + cap + "）", 2500);
            return false;
        }
        int moneyCost = getAssistUpgradeCost(nextLevel);
        int expCost = getAssistUpgradeExpCost(nextLevel);
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        if (roleInfo.getMoney() < moneyCost) {
            GameEngine.getInstance().showCenterToast("金钱不足，需要 " + moneyCost, 2000);
            return false;
        }
        if (roleInfo.getExp() < expCost) {
            GameEngine.getInstance().showCenterToast("经验不足，需要 " + expCost, 2000);
            return false;
        }
        roleInfo.setMoney(roleInfo.getMoney() - moneyCost);
        roleInfo.setExp(roleInfo.getExp() - expCost);
        skill.setLevel(nextLevel);
        String action = nextLevel == 1 ? "学会了" : ("升至 " + nextLevel + " 级");
        GameEngine.getInstance().showCenterToast(skill.getName() + " " + action, 2000);
        return true;
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
        // Only main skills for now, and not use-type skills
        if (!skill.isMainSkill()) return false;
        if (skill.getLevel() < 1) return false;
        if (skill.isUseSkill() || skill.isPassiveUnlock()) return false;
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

        if (skill == null || skill.getLevel() < 1) {
            GameEngine.getInstance().showCenterToast("尚未学会该技能", 1200);
            return null;
        }

        Player player = GameEngine.getInstance().getPlayer();
        long currentTime = System.currentTimeMillis();

        // 引导锁: 持续型领域法术(如飞沙走石)释放过程中, 禁止施放任何法术
        if (player.isChanneling()) {
            GameEngine.getInstance().showCenterToast("施法中", 800);
            return null;
        }

        if (currentTime - player.getLastMagicTime(skill.getSkillType()) < skill.getCooldownMillis()) {
            return null; // Still on cooldown
        }

        if (skill.getSkillType() == SkillType.MAIN_SwordQi
                && ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON) == null) {
            GameEngine.getInstance().showCenterToast("未装备武器，无法使用剑气", 1500);
            return null;
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
            if (skill.getSkillType() == SkillType.MAIN_SwordQi) {
                costMagic = 8;
            } else if (skill.getSkillType() == SkillType.MAIN_WanJianGuiZong) {
                costMagic = 40;
            } else if (skill.getSkillType() == SkillType.MAIN_MiaoShouHuiChun) {
                costMagic = 30;
            }

            if (ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_ZhuanZhu)) {
                //专注
                if (Math.random() < 0.2) {
                    costMagic = 0;
                }
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
            case MAIN_SwordQi: {
                skillStartInfo = castSwordQi();
            }
            break;
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
            case MAIN_DuWuZhen: {
                skillStartInfo = castPoisonCloud();
            }
            break;
            case MAIN_FeiShaZouShi: {
                skillStartInfo = castSandstorm(skill.getLevel());
            }
            break;
        }

        // 持续型领域法术(飞沙走石/万剑归宗/毒雾阵): 冷却不在引导期间计时,
        // 把 lastMagicTime 推迟到引导结束, 使冷却从法术释放完毕后才开始倒计时
        if (skillStartInfo != null && skillStartInfo.getSkillEffect() != null) {
            long channelDuration = skillStartInfo.getSkillEffect().getDuration();
            long channelEnd = currentTime + channelDuration;
            player.setLastMagicTime(skill.getSkillType(), channelEnd);
            player.setChannelLockUntil(channelEnd); // 引导期间锁住施法, 结束后才可再施放
        }

        return skillStartInfo;
    }

    /** 剑气：单发、飞行快。面朝锥形内优先锁最近的敌人。 */
    private SkillStartInfo castSwordQi() {
        SkillStartInfo skillStartInfo = new SkillStartInfo();
        List<Projectile> spells = new ArrayList<>();
        skillStartInfo.setProjectiles(spells);

        Player player = GameEngine.getInstance().getPlayer();
        int fireLevel = 1;
        SkillInfo swordQi = getPlayerSkill(SkillType.MAIN_SwordQi);
        if (swordQi != null) {
            fireLevel = swordQi.getLevel();
        }

        float range = 520f;
        float[] aim = aimSwordQi(player, range);
        Projectile proj = new Projectile(player.getX(), player.getY(), aim[0], aim[1], SkillType.MAIN_SwordQi);
        proj.applySwordQiLook(ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON));
        proj.setSkillLevel(fireLevel);
        float moveAxis = 200f + RoleSystem.getInstance().getRoleInfo().getSpeed() * 0.7f;
        float playerTop = Math.max(1000f, moveAxis * 1.42f);
        float flight = Math.max(1400f + (fireLevel - 1) * 60f, playerTop * 1.35f);
        proj.scaleSpeed(flight / 300f);
        if (fireLevel >= 10) {
            proj.setPierceCount(2);
        } else if (fireLevel >= 9) {
            proj.setPierceCount(1);
        }
        spells.add(proj);
        return skillStartInfo;
    }

    private float[] aimSwordQi(Player player, float range) {
        float baseAngle;
        switch (player.getFacingDirection()) {
            case 1:
                baseAngle = (float) -Math.PI / 2f;
                break;
            case 2:
                baseAngle = (float) Math.PI;
                break;
            case 3:
                baseAngle = 0f;
                break;
            default:
                baseAngle = (float) Math.PI / 2f;
                break;
        }
        float fx = (float) Math.cos(baseAngle);
        float fy = (float) Math.sin(baseAngle);
        float coneCos = (float) Math.cos(Math.toRadians(40));

        Enemy best = null;
        float bestDist = range;
        List<Enemy> enemies = GameEngine.getInstance().getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy == null || !enemy.isAlive() || enemy.isPet()) {
                    continue;
                }
                float dx = enemy.getX() - player.getX();
                float dy = enemy.getY() - player.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 1f || dist > range) {
                    continue;
                }
                float dot = (dx * fx + dy * fy) / dist;
                if (dot >= coneCos && dist < bestDist) {
                    best = enemy;
                    bestDist = dist;
                }
            }
        }
        if (best != null) {
            return new float[]{best.getX(), best.getY()};
        }
        return new float[]{
                player.getX() + fx * range,
                player.getY() + fy * range
        };
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
                case MAIN_FIREBALL: {
                    int fireLevel = 1;
                    List<SkillInfo> playerSkills = getPlayerSkills();
                    for (SkillInfo si : playerSkills) {
                        if (si.getSkillType() == SkillType.MAIN_FIREBALL) {
                            fireLevel = si.getLevel();
                            break;
                        }
                    }

                    int fireCount = Math.min(6, 3 + (fireLevel - 1) / 2);
                    float fireSize = 15 + ((fireLevel - 1) / 2) * 3f;

                    if (fireCount == 1) {
                        angles = new float[]{baseAngle};
                    } else {
                        float spreadAngle = Math.min(120f, fireCount * 20f);
                        angles = new float[fireCount];
                        for (int i = 0; i < fireCount; i++) {
                            angles[i] = baseAngle - spreadAngle / 2 + spreadAngle * i / (fireCount - 1);
                        }
                    }
                    range = 400;

                    for (float angle : angles) {
                        double rad = Math.toRadians(angle);
                        float spellTargetX = player.getX() + (float) (Math.cos(rad) * range);
                        float spellTargetY = player.getY() + (float) (Math.sin(rad) * range);

                        Projectile proj = new Projectile(player.getX(), player.getY(), spellTargetX, spellTargetY, skillType);
                        proj.setSkillLevel(fireLevel);
                        proj.setSize(fireSize);
                        float speedMultiplier = 1.0f + (fireLevel - 1) * 0.06f;
                        proj.scaleSpeed(speedMultiplier);

                        if (fireLevel >= 8) {
                            proj.setBurnDuration(3000);
                        }
                        if (fireLevel >= 9) {
                            proj.setDamageMultiplier(1.2f);
                        }

                        spells.add(proj);
                    }
                    angles = new float[0];
                }
                break;
                case MAIN_ICE_BOLT: {
                    // 获取寒冰术等级
                    int iceLevel = 1;
                    List<SkillInfo> playerSkills = getPlayerSkills();
                    for (SkillInfo si : playerSkills) {
                        if (si.getSkillType() == SkillType.MAIN_ICE_BOLT) {
                            iceLevel = si.getLevel();
                            break;
                        }
                    }

                    // 寒冰粒子大小: 每2级增加一次
                    float iceSize = 8 + ((iceLevel - 1) / 2) * 2f;

                    // 生成12个寒冰粒子
                    angles = new float[12];
                    for (int i = 0; i < 12; i++) {
                        angles[i] = baseAngle + 30 * i;
                    }
                    range = 300;

                    // 创建寒冰粒子并设置等级属性
                    for (float angle : angles) {
                        double rad = Math.toRadians(angle);
                        float spellTargetX = player.getX() + (float) (Math.cos(rad) * range);
                        float spellTargetY = player.getY() + (float) (Math.sin(rad) * range);

                        Projectile proj = new Projectile(player.getX(), player.getY(), spellTargetX, spellTargetY, skillType);
                        proj.setSkillLevel(iceLevel);
                        // 设置寒冰粒子大小
                        proj.setSize(iceSize);
                        // 每级增加寒冰粒子移动速度(每级+10%)
                        float speedMultiplier = 1.0f + (iceLevel - 1) * 0.1f;
                        proj.scaleSpeed(speedMultiplier);

                        // 5级+: 寒冷减速效果
                        if (iceLevel >= 5) {
                            proj.setEffectType(Projectile.EffectType.SLOW);
                        }
                        // 7级+: 30%概率冰冻效果
                        if (iceLevel >= 7) {
                            proj.setFreezeProbability(0.3f);
                        }
                        // 9级: 伤害+20%
                        if (iceLevel >= 9) {
                            proj.setDamageMultiplier(1.2f);
                        }

                        spells.add(proj);
                    }
                    angles = new float[0]; // 寒冰已在上方创建，跳过公共循环
                }
                break;
                case MAIN_LIGHTNING:
                    // Lightning skill is handled directly by GameEngine as LightningChainEffect
                    // Don't create projectiles here, return empty lists
                    angles = new float[0]; // Skip the common projectile creation loop
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

    private SkillStartInfo castPoisonCloud() {
        // 毒雾持续 6 秒，每 1 秒造成一次伤害，共 6 次
        SkillStartInfo skillStartInfo = new SkillStartInfo();
        // Center the storm on the player
        float radius = 500; // Large range
        long duration = 6000;

        Player player = GameEngine.getInstance().getPlayer();
        SkillEffect skillEffect = new SkillEffect(
                SkillEffect.Type.POISON_CLOUD,
                player.getX(),
                player.getY(),
                radius,
                duration,
                1000, // Damage every 1s
                6    // Total 6 hits
        );
        skillStartInfo.setSkillEffect(skillEffect);

        GameEngine.getInstance().showCenterToast("毒雾阵", 1000);

        return skillStartInfo;
    }

    /**
     * 飞沙走石: 持续沙暴领域，伤害+减速，高等级概率眩晕
     * 等级成长:
     *   伤害跳数: 1级2跳 → 10级5跳(每3级+1跳), 跳数=totalHits, 由 SkillEffect 精确停止
     *   固定每 1 秒跳一次伤害; 持续时间 = 跳数 × 1秒(2跳2秒 → 5跳5秒), 低跳数不再空转到5秒
     *   奇数级(1,3,5,7,9): 扩大范围(上限500)
     *   5级+: 减速效果增强(40%→60%)
     *   7级+: 15%概率眩晕
     */
    private SkillStartInfo castSandstorm(int skillLevel) {
        SkillStartInfo skillStartInfo = new SkillStartInfo();
        Player player = GameEngine.getInstance().getPlayer();

        // 基础参数
        float baseRadius = 300f;

        // 等级成长: 奇数级扩大范围(上限500)
        float radiusBonus = ((skillLevel - 1) / 2) * 40f;
        float radius = Math.min(500f, baseRadius + radiusBonus);

        // 伤害跳数: 1级2跳 → 10级5跳(每3级+1跳)
        int totalHits = Math.min(5, 2 + skillLevel / 3);
        // 固定每 1 秒跳一次伤害; 持续时间 = 跳数 × 1秒, 避免低跳数时领域空转
        int damageInterval = 1000;
        long duration = (long) totalHits * damageInterval;

        SkillEffect skillEffect = new SkillEffect(
                SkillEffect.Type.SANDSTORM,
                player.getX(),
                player.getY(),
                radius,
                duration,
                damageInterval,
                totalHits
        );

        // 设置等级相关属性
        skillEffect.setSkillLevel(skillLevel);

        // 5级+: 减速效果增强
        if (skillLevel >= 5) {
            skillEffect.setSlowRatio(0.6f); // 60%减速
        } else {
            skillEffect.setSlowRatio(0.4f); // 40%减速
        }

        // 7级+: 15%概率眩晕
        if (skillLevel >= 7) {
            skillEffect.setStunChance(0.15f);
        }

        skillStartInfo.setSkillEffect(skillEffect);

        GameEngine.getInstance().showCenterToast("飞沙走石", 1000);

        return skillStartInfo;
    }

}

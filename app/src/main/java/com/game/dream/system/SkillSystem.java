package com.game.dream.system;

import com.game.dream.bean.SkillInfo;
import com.game.dream.enums.SkillType;

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
    private static final int SKILLS_PER_PAGE = 5;
    private static final int MAX_EQUIPED_ACTIVE_SKILLS = 15;

    private void initMainSkills() {
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_FIREBALL, 1, 10, "火云术", "发射火焰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_ICE_BOLT, 1, 10, "寒冰术", "发射寒冰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.MAIN_LIGHTNING, 1, 10, "雷击术", "发射几道闪电对敌人造成伤害"));
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

        return equippedActiveSkills.subList(start, end);
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

}

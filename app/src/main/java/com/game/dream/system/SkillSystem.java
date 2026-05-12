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
    }

    private List<SkillInfo> playerMainSkills = new ArrayList<>();
    private List<SkillInfo> playerAssistSkills = new ArrayList<>();
    private List<SkillInfo> playerPractiseSkills = new ArrayList<>();

    private void initMainSkills() {
        playerMainSkills.add(new SkillInfo(SkillType.FIREBALL, 1, 10, "火云术", "发射火焰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.ICE_BOLT, 1, 10, "寒冰术", "发射寒冰对敌人造成伤害"));
        playerMainSkills.add(new SkillInfo(SkillType.LIGHTNING, 1, 10, "极光术", "发射几道极光对敌人造成伤害"));
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
}

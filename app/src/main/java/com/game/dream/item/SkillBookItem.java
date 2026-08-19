package com.game.dream.item;

import com.game.dream.enums.SkillType;

/**
 * 技能书物品 - 使用后学习或升级对应技能
 */
public class SkillBookItem extends Item {

    private SkillType skillType;

    public SkillBookItem(int id, String name, String description, Rarity rarity,
                         int maxStack, int value, SkillType skillType) {
        super(id, name, description, Type.SKILL_BOOK, rarity, maxStack, value);
        this.skillType = skillType;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    @Override
    public boolean use() {
        return true; // 使用后消耗
    }
}

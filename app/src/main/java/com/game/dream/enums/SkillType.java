package com.game.dream.enums;

import android.text.TextUtils;

public enum SkillType {

    //主技能
    FIREBALL,
    ICE_BOLT,
    LIGHTNING,

    //辅助技能
    AST_QiangShen,
    AST_MingXiang,
    AST_ShenSu,
    AST_JianShen,
    AST_YangShengZhiDao,
    AST_ZhuangbeiZhiZao,

    //修炼技能
    PST_AttackPractise,
    PST_MagicPractise,
    PST_DefensePractise,
    PST_MagicDefensePractise,
    PST_BB_AttackPractise,
    PST_BB_MagicPractise,
    PST_BB_DefensePractise,
    PST_BB_MagicDefensePractise,

    ;

    public static SkillType getSkillType(String value) {
        for (SkillType skillType : SkillType.values()) {
            if (TextUtils.equals(skillType.name(), value)) {
                return skillType;
            }
        }
        return null;
    }
}

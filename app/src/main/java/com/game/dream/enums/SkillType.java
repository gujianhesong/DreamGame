package com.game.dream.enums;

import android.text.TextUtils;

public enum SkillType {

    //主技能
    MAIN_DunXing, //遁形
    MAIN_FIREBALL, //火云术
    MAIN_ICE_BOLT, //寒冰术
    MAIN_LIGHTNING, //雷击术
    MAIN_FeiShaZouShi, //飞沙走石
    MAIN_ROOT, //定身术
    MAIN_WanJianGuiZong, //万剑归宗
    MAIN_JinGangHuTi, //金刚护体
    MAIN_MiaoShouHuiChun, //妙手回春
    MAIN_LianQiHuaShen, //炼气化神
    MAIN_DuWuZhen, //毒雾阵

    //辅助技能
    AST_QiangShen, //强身
    AST_MingXiang, //冥想
    AST_ShenSu, //神速
    AST_JianShen, //健身
    AST_YangShengZhiDao, //养身之道
    AST_ZhuangbeiZhiZao, //装备制造

    //修炼技能
    PST_AttackPractise, //攻击修炼
    PST_MagicPractise, //法术修炼
    PST_DefensePractise, //防御修炼
    PST_MagicDefensePractise, //法术防御修炼
    PST_BB_AttackPractise, //宝宝攻击修炼
    PST_BB_MagicPractise, //宝宝法术修炼
    PST_BB_DefensePractise, //宝宝防御修炼
    PST_BB_MagicDefensePractise, //宝宝法术防御修炼

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

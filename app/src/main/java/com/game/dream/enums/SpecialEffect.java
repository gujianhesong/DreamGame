package com.game.dream.enums;

import android.text.TextUtils;

public enum SpecialEffect {
    SE_WuJiBieXianZhi("无级别限制"), //无级别限制
    SE_Xixue("吸血"), //吸血（输出伤害的10%）
    SE_ShenYou("神佑"), //神佑（20%自动复活）
    SE_ZhuanZhu("专注"), //专注（施法20%不消耗蓝）
    SE_ShenNong("神农"), //神农（使用药物恢复血蓝增加20%）
    SE_ZaiSheng("再生"), //再生（每隔5秒恢复3%气血）
    SE_MingSi("冥思"), //冥思（每隔5秒恢复3%魔法）
    SE_BiZhong("必中"), //必中（武器,攻击施法必定命中）

    ;

    private String desc;

    SpecialEffect(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static SpecialEffect getSpecialEffectWithName(String name) {
        for (SpecialEffect item : SpecialEffect.values()) {
            if (TextUtils.equals(item.name(), name)) {
                return item;
            }
        }
        return null;
    }
}

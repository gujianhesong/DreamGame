package com.game.dream.enums;

import android.text.TextUtils;

public enum XiLianType {
    XL_attackCritRatio("物理暴击几率"), //物理暴击几率
    XL_magicCritRatio("法术暴击几率"), //法术暴击几率
    XL_attackSpeedRatio("攻击速度增加"), //攻击速度增加
    XL_magicSpeedRatio("法术速度增加"), //法术速度增加
    XL_attackValueRatio("攻击伤害增加"), //攻击伤害增加
    XL_magicValueRatio("法术伤害增加"), //法术伤害增加
    XL_beAttackedValueRatio("被攻击伤害减少"), //被攻击伤害减少
    XL_beMagicedValueRatio("被法术伤害减少"), //被法术伤害减少


    ;

    private String desc;

    XiLianType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static XiLianType getXiLianTypeWithName(String name) {
        for (XiLianType item : XiLianType.values()) {
            if (TextUtils.equals(item.name(), name)) {
                return item;
            }
        }
        return null;
    }
}

package com.game.dream.enums;

import android.text.TextUtils;

public enum GemtoneType {
    GT_TaiYangShi("太阳石"),
    GT_HoneMaNao("红玛瑙"),
    GT_SheLiZi("舍利子"),
    GT_YueLiangShi("月亮石"),
    GT_GuangMangShi("光芒石"),
    GT_HeiBaoShi("黑宝石"),
    GT_LanBaoShi("蓝宝石"),
    GT_ShenMiShi("神秘石"),

    ;

    private String desc;

    GemtoneType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static GemtoneType getGemtoneTypeWithName(String name) {
        for (GemtoneType item : GemtoneType.values()) {
            if (TextUtils.equals(item.name(), name)) {
                return item;
            }
        }
        return null;
    }

    public static GemtoneType getGemtoneTypeWithIndex(int index) {
        GemtoneType[] arr = GemtoneType.values();
        if (index < arr.length) {
            return arr[index];
        }
        return null;
    }
}

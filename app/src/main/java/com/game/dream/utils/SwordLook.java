package com.game.dream.utils;

import android.graphics.Color;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.item.EquipmentItem;

/**
 * 持剑与剑气主色：按武器等级分档，再用攻击/命中微调色相。剑气外形仍用月牙弧。
 */
public final class SwordLook {

    private SwordLook() {
    }

    public static int bladeColor(int weaponLevel) {
        if (weaponLevel >= 100) {
            return Color.rgb(255, 215, 0);
        }
        if (weaponLevel >= 80) {
            return Color.rgb(255, 165, 0);
        }
        if (weaponLevel >= 60) {
            return Color.rgb(200, 150, 255);
        }
        if (weaponLevel >= 40) {
            return Color.rgb(100, 200, 255);
        }
        if (weaponLevel >= 20) {
            return Color.rgb(180, 230, 200);
        }
        return Color.rgb(220, 220, 240);
    }

    public static int glowArgb(int weaponLevel) {
        if (weaponLevel >= 100) {
            return Color.argb(120, 255, 200, 0);
        }
        if (weaponLevel >= 80) {
            return Color.argb(110, 255, 180, 0);
        }
        if (weaponLevel >= 60) {
            return Color.argb(100, 180, 100, 255);
        }
        if (weaponLevel >= 40) {
            return Color.argb(90, 80, 180, 255);
        }
        if (weaponLevel >= 20) {
            return Color.argb(80, 150, 220, 180);
        }
        return Color.argb(70, 200, 200, 255);
    }

    public static int qiColor(EquipmentItem weapon) {
        int level = 1;
        int seed = 0;
        if (weapon != null && weapon.getEquipItemInfo() != null) {
            EquipItemInfo info = weapon.getEquipItemInfo();
            level = Math.max(1, info.getLevel());
            seed = info.getAttack() * 31 + info.getHit() * 17 + info.getId();
        }
        return tint(bladeColor(level), seed);
    }

    private static int tint(int base, int seed) {
        int shift = (seed % 41) - 20;
        int r = clamp(Color.red(base) + shift);
        int g = clamp(Color.green(base) + (shift / 2));
        int b = clamp(Color.blue(base) - shift);
        return Color.rgb(r, g, b);
    }

    private static int clamp(int v) {
        return Math.max(40, Math.min(255, v));
    }
}

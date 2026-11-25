package com.game.dream.enums;

/** 称号品质：凡 / 名 / 奇 / 传说 / 隐藏 */
public enum TitleRarity {
    FAN,
    MING,
    QI,
    LEGEND,
    HIDDEN;

    public int getColorArgb() {
        switch (this) {
            case FAN:
                return android.graphics.Color.rgb(255, 244, 200);
            case MING:
                return android.graphics.Color.rgb(130, 255, 170);
            case QI:
                return android.graphics.Color.rgb(150, 210, 255);
            case LEGEND:
                return android.graphics.Color.rgb(255, 230, 80);
            case HIDDEN:
            default:
                return android.graphics.Color.rgb(240, 180, 255);
        }
    }
}
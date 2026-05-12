package com.game.dream.utils;

import android.graphics.Rect;

public class TouchUtil {
    private static final int expand = 10;

    public static boolean checkIsInTouchRectInt(Rect rect, int x, int y) {
        Rect hitRect = new Rect(
                rect.left - expand,
                rect.top - expand,
                rect.right + expand,
                rect.bottom + expand
        );
        if (hitRect.contains(x, y)) {
            return true;
        }
        return false;
    }

    public static boolean checkIsInTouchRectFloat(Rect rect, float x, float y) {
        return checkIsInTouchRectInt(rect, (int) x, (int) y);
    }
}

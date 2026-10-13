package com.game.dream.npc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 清溪迷宫中的镇村令石台，点击拾取任务信物。
 */
public class MazeToken {
    private final float x;
    private final float y;
    private final int size = 64;
    private boolean collected;
    private final Paint paint = new Paint();

    public MazeToken(float x, float y) {
        this.x = x;
        this.y = y;
        paint.setAntiAlias(true);
    }

    public void draw(Canvas canvas, float offsetX, float offsetY) {
        if (collected) {
            return;
        }
        float cx = x + offsetX;
        float cy = y + offsetY;
        float half = size / 2f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(90, 90, 100));
        canvas.drawRect(cx - half * 0.7f, cy - half * 0.2f, cx + half * 0.7f, cy + half * 0.85f, paint);

        paint.setColor(Color.rgb(150, 140, 120));
        canvas.drawRoundRect(cx - half * 0.55f, cy - half * 0.95f, cx + half * 0.55f, cy + half * 0.15f, 6, 6, paint);

        paint.setColor(Color.rgb(180, 40, 40));
        canvas.drawCircle(cx, cy - half * 0.4f, 8, paint);

        paint.setColor(Color.YELLOW);
        paint.setTextSize(34);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("!", cx, cy - half - 8, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setShadowLayer(3, 0, 0, Color.BLACK);
        canvas.drawText("镇村令", cx, cy + half + 22, paint);
        paint.clearShadowLayer();
    }

    public boolean isTouched(float touchWorldX, float touchWorldY) {
        if (collected) {
            return false;
        }
        float half = size / 2f;
        return touchWorldX >= x - half && touchWorldX <= x + half
                && touchWorldY >= y - half && touchWorldY <= y + half;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}

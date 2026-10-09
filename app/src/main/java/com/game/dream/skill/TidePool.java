package com.game.dream.skill;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.enemy.Enemy;

/**
 * 龙王潮：铺在龙王脚下的潮池，持续伤害站在里面的玩家和战宠，龙王本人站在里面会回血。
 */
public class TidePool {

    public static final float RADIUS = 180f;
    private static final long DURATION_MS = 6000;
    private static final long TICK_MS = 800;

    private final float x;
    private final float y;
    private final Enemy caster;
    private final long startTime;
    private long lastTickTime;

    public TidePool(float x, float y, Enemy caster) {
        this.x = x;
        this.y = y;
        this.caster = caster;
        this.startTime = System.currentTimeMillis();
        this.lastTickTime = this.startTime - TICK_MS;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Enemy getCaster() {
        return caster;
    }

    public boolean isActive() {
        return System.currentTimeMillis() - startTime < DURATION_MS;
    }

    public boolean consumeTick() {
        long now = System.currentTimeMillis();
        if (!isActive() || now - lastTickTime < TICK_MS) {
            return false;
        }
        lastTickTime = now;
        return true;
    }

    public static boolean contains(float cx, float cy, float px, float py) {
        float dx = px - cx;
        float dy = py - cy;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        float sx = x + offsetX;
        float sy = y + offsetY;
        float pulse = 1f + 0.04f * (float) Math.sin(System.currentTimeMillis() / 180.0);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 20, 80, 150));
        canvas.drawCircle(sx, sy, RADIUS * pulse, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.argb(190, 80, 180, 230));
        canvas.drawCircle(sx, sy, RADIUS, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(140, 160, 220, 255));
        for (int i = 0; i < 5; i++) {
            double angle = System.currentTimeMillis() / 650.0 + i * 1.25;
            float dist = RADIUS * (0.22f + 0.12f * i);
            canvas.drawCircle(
                    sx + (float) Math.cos(angle) * dist,
                    sy + (float) Math.sin(angle) * dist,
                    7f + i, paint);
        }
    }
}

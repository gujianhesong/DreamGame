package com.game.dream.skill;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.enemy.Enemy;

/**
 * 龟丞相镇海印：圈锁在施放瞬间的落点，延迟后落印一次。
 */
public class SeaSealZone {

    public static final float RADIUS = 120f;
    public static final long DROP_DELAY = 700;
    private static final long LINGER_MS = 280;
    private static final long FLASH_MS = 180;

    private final float x;
    private final float y;
    private final Enemy caster;
    private final long startTime;
    private boolean dropped;
    private long dropTime;

    public SeaSealZone(float x, float y, Enemy caster) {
        this.x = x;
        this.y = y;
        this.caster = caster;
        this.startTime = System.currentTimeMillis();
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
        long end = dropped ? dropTime + LINGER_MS : startTime + DROP_DELAY + LINGER_MS;
        return System.currentTimeMillis() < end;
    }

    /** 到点落印一次。 */
    public boolean consumeDrop() {
        if (dropped) {
            return false;
        }
        if (System.currentTimeMillis() - startTime < DROP_DELAY) {
            return false;
        }
        dropped = true;
        dropTime = System.currentTimeMillis();
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
        long now = System.currentTimeMillis();
        boolean flashing = dropped && now - dropTime < FLASH_MS;
        float wait = dropped ? 1f : Math.min(1f, (now - startTime) / (float) DROP_DELAY);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(flashing ? 90 : (int) (24 + wait * 30), 40, 160, 130));
        canvas.drawCircle(sx, sy, RADIUS, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(flashing ? 5f : 3f);
        paint.setColor(Color.argb(flashing ? 230 : 150, 120, 230, 190));
        canvas.drawCircle(sx, sy, RADIUS, paint);

        paint.setStrokeWidth(2f);
        canvas.drawCircle(sx, sy, RADIUS * (0.35f + wait * 0.45f), paint);

        if (!flashing) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(220, 210, 255, 230));
        canvas.drawCircle(sx, sy, 14f, paint);
    }
}

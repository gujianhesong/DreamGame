package com.game.dream.skill;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 小青龙龙雷：落在目标脚下的固定雷区。
 * 出现时打第一段，{@link #PULSE_INTERVAL} 后再判一次圈内目标，随后短暂停留再消失。
 */
public class DragonLightningZone {

    public static final float RADIUS = 130f;
    /** 两段伤害的间隔 */
    public static final long PULSE_INTERVAL = 450;
    /** 单段伤害相对原来那一发龙雷的比例 */
    public static final float DAMAGE_RATIO = 0.8f;

    private static final int PULSE_COUNT = 2;
    /** 第二段之后雷区还亮一小会儿，方便看见落点 */
    private static final long LINGER_MS = 220;
    private static final long FLASH_MS = 160;

    private final float x;
    private final float y;
    private final com.game.dream.enemy.Enemy caster;
    private final boolean fromPet;
    private final long startTime;

    private int pulsesDone;
    private long lastPulseTime;

    public DragonLightningZone(float x, float y, com.game.dream.enemy.Enemy caster, boolean fromPet) {
        this.x = x;
        this.y = y;
        this.caster = caster;
        this.fromPet = fromPet;
        this.startTime = System.currentTimeMillis();
        this.pulsesDone = 0;
        this.lastPulseTime = 0;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public com.game.dream.enemy.Enemy getCaster() {
        return caster;
    }

    public boolean isFromPet() {
        return fromPet;
    }

    public boolean isActive() {
        long life = (PULSE_COUNT - 1) * PULSE_INTERVAL + LINGER_MS;
        return System.currentTimeMillis() - startTime < life;
    }

    /** 到点则记一次落雷并返回 true。同一帧若落后多段会连续返回。 */
    public boolean consumePulse() {
        if (pulsesDone >= PULSE_COUNT) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed < pulsesDone * PULSE_INTERVAL) {
            return false;
        }
        pulsesDone++;
        lastPulseTime = System.currentTimeMillis();
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
        boolean flashing = lastPulseTime > 0 && now - lastPulseTime < FLASH_MS;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int fillAlpha = flashing ? 90 : 36;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(fillAlpha, 190, 255, 80));
        canvas.drawCircle(sx, sy, RADIUS, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(flashing ? 5f : 3f);
        paint.setColor(Color.argb(flashing ? 230 : 150, 220, 255, 120));
        canvas.drawCircle(sx, sy, RADIUS, paint);

        if (!flashing) {
            return;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(230, 255, 255, 210));
        float flicker = (now / 30f);
        for (int i = 0; i < 6; i++) {
            float angle = flicker * 0.15f + i * (float) (Math.PI * 2 / 6);
            float ex = sx + (float) Math.cos(angle) * RADIUS * 0.92f;
            float ey = sy + (float) Math.sin(angle) * RADIUS * 0.92f;
            float bend = ((i % 2 == 0) ? 1f : -1f) * 18f;
            float mx = (sx + ex) / 2f + (float) Math.cos(angle + 1.2f) * bend;
            float my = (sy + ey) / 2f + (float) Math.sin(angle + 1.2f) * bend;
            canvas.drawLine(sx, sy, mx, my, paint);
            canvas.drawLine(mx, my, ex, ey, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(200, 255, 255, 240));
        canvas.drawCircle(sx, sy, 10f, paint);
    }
}

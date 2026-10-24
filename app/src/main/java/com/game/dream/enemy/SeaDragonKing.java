package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 海底迷宫三层关卡 BOSS：东海龙王敖广。独立怪物，不是小青龙。
 * 五发水龙弹、龙雷、大范围龙威，龙尾横扫附带击退。半血连续三道潮柱，并在脚下铺潮池。
 */
public class SeaDragonKing extends Enemy {

    private static final float AURA_RANGE = 400f;
    private static final long AURA_SLOW = 1100;
    private static final float PREFERRED_DISTANCE = 340f;
    private static final float MIN_DISTANCE = 200f;
    private static final int BOLT_COUNT = 5;
    private static final float BOLT_SPREAD = 12f;
    private static final long BOLT_COOLDOWN = 2400;
    private static final long LIGHTNING_COOLDOWN = 4000;

    private long lastWaterBoltTime = 0;
    private long lastLightningTime = 0;
    private boolean pendingWaterBolt = false;
    private boolean pendingLightning = false;

    public SeaDragonKing(float x, float y) {
        super(x, y, 600);
        becomeUniqueBoss();
        attackCooldown = 2000;
        setAttackShape(AttackShape.CIRCLE);
        addAvailableAttackType(AttackType.SPIN_ATTACK);
        spinDuration = 1400;
        spinHitInterval = 380;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 720;
        prop.attackRange = 440;
        prop.rewardExp = 10000;
        prop.rewardMoney = 5000;
        setPropertyExtra(prop);
        setProperty(3000*40, 3000, 3000, 600, 4000);
    }

    @Override
    public boolean usesGenericFireball() {
        return false;
    }

    public int getWaterBoltCount() {
        return BOLT_COUNT;
    }

    public float getWaterBoltSpread() {
        return BOLT_SPREAD;
    }

    public float getAuraRange() {
        return AURA_RANGE;
    }

    public long getAuraSlowDuration() {
        return AURA_SLOW;
    }

    public boolean isPendingWaterBolt() {
        return pendingWaterBolt;
    }

    public void consumeWaterBolt() {
        pendingWaterBolt = false;
    }

    public boolean isPendingLightning() {
        return pendingLightning;
    }

    public void consumeLightning() {
        pendingLightning = false;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive() || isStunned() || isFrozen()) {
            return;
        }
        long now = System.currentTimeMillis();
        tickSpellWindUp(now, playerX, playerY);
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (currentState == State.CHASING) {
            tryCastSpells(dist, now);
        }
    }

    @Override
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (isRooted() || isSpellWindingUp || dist < 1f) {
            return;
        }
        if (dist < MIN_DISTANCE) {
            targetX = x - dx / dist * 220;
            targetY = y - dy / dist * 220;
        } else if (dist > PREFERRED_DISTANCE * 1.4f) {
            targetX = playerX;
            targetY = playerY;
        } else {
            float perpX = -dy / dist;
            float perpY = dx / dist;
            float circleDir = (Math.sin(System.currentTimeMillis() / 1800.0) > 0) ? 1 : -1;
            targetX = x + perpX * 120 * circleDir;
            targetY = y + perpY * 120 * circleDir;
        }
        moveToTargetWithSpeed(deltaSeconds, speed * 1.05f);
    }

    private void tryCastSpells(float dist, long now) {
        if (isSpellWindingUp || pendingWaterBolt || pendingLightning || dist >= propertyExtra.detectionRange) {
            return;
        }
        if (now - lastSpellCastRollTime < SPELL_CAST_CHECK_INTERVAL) {
            return;
        }
        boolean waterReady = now - lastWaterBoltTime >= BOLT_COOLDOWN;
        boolean lightningReady = now - lastLightningTime >= LIGHTNING_COOLDOWN;
        if (!waterReady && !lightningReady) {
            return;
        }
        lastSpellCastRollTime = now;

        float lightningChance = dist <= MIN_DISTANCE * 1.2f ? 0.35f : 0.25f;
        float waterChance = dist < MIN_DISTANCE ? 0.08f : (dist <= PREFERRED_DISTANCE ? 0.42f : 0.26f);
        if (isBossPhaseTwo()) {
            lightningChance = Math.min(0.70f, lightningChance * 1.6f);
            waterChance = Math.min(0.70f, waterChance * 1.6f);
        }
        if (lightningReady && dist <= propertyExtra.detectionRange * 0.85f && Math.random() < lightningChance) {
            beginSpellWindUp(SPELL_DRAGON_LIGHTNING);
            return;
        }
        if (waterReady && Math.random() < waterChance) {
            beginSpellWindUp(SPELL_WATER_BOLT);
        }
    }

    @Override
    protected void onSpecialSpellWindUpComplete(int spellId, long currentTime) {
        if (spellId == SPELL_WATER_BOLT) {
            pendingWaterBolt = true;
            lastWaterBoltTime = currentTime;
        } else if (spellId == SPELL_DRAGON_LIGHTNING) {
            pendingLightning = true;
            lastLightningTime = currentTime;
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel4(this);
        }
        return possibleDrops;
    }

    @Override
    protected void performAttack() {
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) {
            return;
        }
        long now = System.currentTimeMillis();
        float cx = x + offsetX;
        float cy = y + offsetY;
        boolean facingRight = targetX >= x;
        float dir = facingRight ? 1f : -1f;
        float s = size;
        boolean aggressive = currentState == State.CHASING || currentState == State.ATTACKING
                || pendingWaterBolt || pendingLightning;
        paint.setAntiAlias(true);

        float auraPulse = 1f + 0.04f * (float) Math.sin(now / 620.0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(40, 200, 150, 40));
        canvas.drawCircle(cx, cy, AURA_RANGE * 0.18f * auraPulse, paint);
        paint.setColor(Color.argb(28, 255, 210, 80));
        canvas.drawCircle(cx, cy, AURA_RANGE * 0.10f * auraPulse, paint);

        int segments = 14;
        float[] bx = new float[segments];
        float[] by = new float[segments];
        float[] thick = new float[segments];
        for (int i = 0; i < segments; i++) {
            float t = i / (float) (segments - 1);
            float sway = (float) Math.sin(now / 480.0 + t * 3.1f) * s * 0.045f;
            bx[i] = cx + dir * s * (0.36f - t * 0.92f);
            by[i] = cy - s * 0.04f + t * s * 0.08f + sway;
            thick[i] = s * 0.075f;
            if (t > 0.85f) {
                thick[i] = s * (0.075f - (t - 0.85f) / 0.15f * 0.018f);
            }
        }

        drawCarpTail(canvas, bx[segments - 1], by[segments - 1], dir, s, now);
        drawLegs(canvas, bx, by, dir, s, now, true);
        drawBody(canvas, bx, by, thick, dir, s);
        drawDorsalSpines(canvas, bx, by, thick, dir, s, now);
        drawLegs(canvas, bx, by, dir, s, now, false);
        drawHead(canvas, bx[0], by[0], dir, s, now, aggressive);
    }

    private void drawBody(Canvas canvas, float[] bx, float[] by, float[] thick, float dir, float s) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < bx.length - 1; i++) {
            paint.setStrokeWidth(thick[i] * 2f);
            paint.setColor(Color.rgb(120, 78, 18));
            canvas.drawLine(bx[i], by[i], bx[i + 1], by[i + 1], paint);
            paint.setStrokeWidth(thick[i] * 1.5f);
            paint.setColor(Color.rgb(212, 162, 42));
            canvas.drawLine(bx[i], by[i] - thick[i] * 0.06f, bx[i + 1], by[i + 1] - thick[i] * 0.06f, paint);
            paint.setStrokeWidth(thick[i] * 0.7f);
            paint.setColor(Color.rgb(255, 214, 110));
            canvas.drawLine(bx[i], by[i] + thick[i] * 0.22f, bx[i + 1], by[i + 1] + thick[i] * 0.22f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        for (int i = 1; i < bx.length - 2; i++) {
            float sc = thick[i] * 0.16f;
            paint.setColor(Color.argb(100, 255, 230, 140));
            canvas.drawCircle(bx[i] + dir * thick[i] * 0.1f, by[i] - thick[i] * 0.1f, sc, paint);
            paint.setColor(Color.argb(70, 160, 100, 20));
            canvas.drawCircle(bx[i] - dir * thick[i] * 0.04f, by[i] + thick[i] * 0.08f, sc * 0.55f, paint);
        }
        paint.setStrokeWidth(1f);
    }

    private void drawDorsalSpines(Canvas canvas, float[] bx, float[] by, float[] thick, float dir, float s, long now) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 1; i < bx.length - 3; i++) {
            float wave = (float) Math.sin(now / 280.0 + i * 0.7) * s * 0.012f;
            Path spine = new Path();
            float mx = bx[i];
            float my = by[i] - thick[i] * 0.55f;
            spine.moveTo(mx - dir * thick[i] * 0.12f, my);
            spine.lineTo(mx + dir * s * 0.006f, my - thick[i] * 1.15f - wave);
            spine.lineTo(mx + dir * thick[i] * 0.14f, my + thick[i] * 0.04f);
            spine.close();
            paint.setColor(i % 2 == 0 ? Color.rgb(90, 50, 10) : Color.rgb(196, 140, 36));
            canvas.drawPath(spine, paint);
        }
    }

    private void drawLegs(Canvas canvas, float[] bx, float[] by, float dir, float s, long now, boolean backLayer) {
        int[] joints = backLayer ? new int[]{3, 8} : new int[]{2, 7};
        float side = backLayer ? -dir : dir;
        float step = (float) Math.sin(now / 260.0) * s * 0.02f;
        for (int idx = 0; idx < joints.length; idx++) {
            int i = joints[idx];
            if (i >= bx.length) {
                continue;
            }
            float hipX = bx[i] + side * s * 0.04f;
            float hipY = by[i] + s * 0.02f;
            float pawX = hipX + side * s * 0.06f + (idx == 0 ? step : -step);
            float pawY = hipY + s * 0.16f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setColor(Color.rgb(150, 100, 28));
            paint.setStrokeWidth(s * 0.032f);
            canvas.drawLine(hipX, hipY, pawX, pawY - s * 0.05f, paint);
            paint.setStrokeWidth(s * 0.024f);
            canvas.drawLine(pawX, pawY - s * 0.05f, pawX + side * s * 0.02f, pawY, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(110, 72, 18));
            canvas.drawRect(pawX - s * 0.028f, pawY - s * 0.012f, pawX + s * 0.03f, pawY + s * 0.018f, paint);
            paint.setColor(Color.rgb(210, 190, 150));
            paint.setStrokeWidth(s * 0.01f);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(pawX, pawY, pawX + dir * s * 0.055f, pawY + s * 0.008f, paint);
            canvas.drawLine(pawX, pawY, pawX + dir * s * 0.05f, pawY + s * 0.03f, paint);
            canvas.drawLine(pawX, pawY, pawX + dir * s * 0.032f, pawY + s * 0.048f, paint);
            canvas.drawLine(pawX, pawY, pawX - dir * s * 0.018f, pawY + s * 0.044f, paint);
            canvas.drawLine(pawX, pawY, pawX - dir * s * 0.048f, pawY + s * 0.006f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
    }

    private void drawCarpTail(Canvas canvas, float tx, float ty, float dir, float s, long now) {
        float flap = (float) Math.sin(now / 240.0) * s * 0.04f;
        Path fin = new Path();
        fin.moveTo(tx, ty);
        fin.lineTo(tx - dir * s * 0.18f + flap, ty - s * 0.17f);
        fin.lineTo(tx - dir * s * 0.07f, ty - s * 0.02f);
        fin.lineTo(tx - dir * s * 0.20f - flap, ty + s * 0.16f);
        fin.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(170, 118, 28));
        canvas.drawPath(fin, paint);
        paint.setColor(Color.rgb(255, 210, 90));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.012f);
        canvas.drawPath(fin, paint);
        canvas.drawLine(tx, ty, tx - dir * s * 0.16f + flap, ty - s * 0.14f, paint);
        canvas.drawLine(tx, ty, tx - dir * s * 0.18f - flap, ty + s * 0.13f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
    }

    private void drawHead(Canvas canvas, float hx, float hy, float dir, float s, long now, boolean aggressive) {
        float snout = hx + dir * s * 0.19f;
        float brow = hy - s * 0.06f;

        drawMane(canvas, hx - dir * s * 0.06f, hy, dir, s, now);
        drawAntlers(canvas, hx, brow, dir, s);
        drawEars(canvas, hx - dir * s * 0.03f, hy - s * 0.01f, dir, s);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(196, 148, 36));
        Path head = new Path();
        head.moveTo(hx - dir * s * 0.09f, hy - s * 0.05f);
        head.lineTo(hx + dir * s * 0.04f, hy - s * 0.11f);
        head.lineTo(hx + dir * s * 0.12f, hy - s * 0.07f);
        head.lineTo(snout, hy - s * 0.012f);
        head.lineTo(snout + dir * s * 0.02f, hy + s * 0.018f);
        head.lineTo(hx + dir * s * 0.10f, hy + s * 0.055f);
        head.lineTo(hx - dir * s * 0.08f, hy + s * 0.04f);
        head.close();
        canvas.drawPath(head, paint);

        paint.setColor(Color.rgb(140, 90, 22));
        Path jaw = new Path();
        jaw.moveTo(hx + dir * s * 0.02f, hy + s * 0.02f);
        jaw.lineTo(snout - dir * s * 0.02f, hy + s * 0.03f);
        jaw.lineTo(snout - dir * s * 0.01f, hy + s * 0.07f);
        jaw.lineTo(hx + dir * s * 0.01f, hy + s * 0.08f);
        jaw.close();
        canvas.drawPath(jaw, paint);

        paint.setColor(Color.rgb(230, 220, 200));
        paint.setStrokeWidth(s * 0.01f);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(hx + dir * s * 0.11f, hy + s * 0.012f, hx + dir * s * 0.15f, hy + s * 0.038f, paint);
        canvas.drawLine(hx + dir * s * 0.08f, hy + s * 0.018f, hx + dir * s * 0.12f, hy + s * 0.048f, paint);
        canvas.drawLine(snout - dir * s * 0.02f, hy + s * 0.045f, snout - dir * s * 0.05f, hy + s * 0.018f, paint);
        canvas.drawLine(snout - dir * s * 0.05f, hy + s * 0.05f, snout - dir * s * 0.08f, hy + s * 0.022f, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(8, 18, 16));
        canvas.drawCircle(snout - dir * s * 0.01f, hy - s * 0.004f, s * 0.01f, paint);

        paint.setColor(Color.rgb(120, 70, 16));
        Path browRidge = new Path();
        browRidge.moveTo(hx - dir * s * 0.02f, hy - s * 0.04f);
        browRidge.lineTo(hx + dir * s * 0.10f, hy - s * 0.08f);
        browRidge.lineTo(hx + dir * s * 0.12f, hy - s * 0.05f);
        browRidge.lineTo(hx + dir * s * 0.02f, hy - s * 0.02f);
        browRidge.close();
        canvas.drawPath(browRidge, paint);

        float eyeX = hx + dir * s * 0.07f;
        float eyeY = hy - s * 0.028f;
        paint.setColor(aggressive ? Color.rgb(255, 90, 20) : Color.rgb(220, 140, 30));
        canvas.drawOval(eyeX - s * 0.032f, eyeY - s * 0.012f, eyeX + s * 0.034f, eyeY + s * 0.014f, paint);
        paint.setColor(Color.rgb(8, 4, 2));
        canvas.drawRect(eyeX - s * 0.004f, eyeY - s * 0.014f, eyeX + s * 0.004f, eyeY + s * 0.016f, paint);

        paint.setColor(Color.rgb(255, 220, 90));
        canvas.drawCircle(hx + dir * s * 0.02f, hy + s * 0.09f, s * 0.02f, paint);
        paint.setColor(Color.argb(150, 255, 255, 200));
        canvas.drawCircle(hx + dir * s * 0.02f, hy + s * 0.09f, s * 0.01f, paint);

        drawWhiskers(canvas, snout, hy, dir, s, now);

        if (aggressive) {
            paint.setColor(Color.argb(150, 40, 90, 140));
            canvas.drawCircle(snout + dir * s * 0.03f, hy + s * 0.01f, s * 0.028f, paint);
        }
    }

    private void drawMane(Canvas canvas, float nx, float ny, float dir, float s, long now) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 5; i++) {
            float wave = (float) Math.sin(now / 360.0 + i) * s * 0.02f;
            Path hair = new Path();
            float ox = nx - dir * s * (0.02f + i * 0.018f);
            hair.moveTo(nx, ny - s * 0.04f + i * s * 0.018f);
            hair.lineTo(ox - dir * s * 0.08f, ny - s * 0.12f + i * s * 0.02f + wave);
            hair.lineTo(ox, ny + s * 0.02f + i * s * 0.016f);
            hair.close();
            paint.setColor(i % 2 == 0 ? Color.rgb(240, 228, 190) : Color.rgb(210, 170, 70));
            canvas.drawPath(hair, paint);
        }
    }

    private void drawAntlers(Canvas canvas, float hx, float brow, float dir, float s) {
        paint.setColor(Color.rgb(232, 198, 96));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeWidth(s * 0.016f);
        float baseY = brow - s * 0.01f;
        float innerX = hx - dir * s * 0.01f;
        float outerX = hx + dir * s * 0.05f;
        canvas.drawLine(innerX, baseY, innerX - dir * s * 0.06f, baseY - s * 0.20f, paint);
        canvas.drawLine(innerX - dir * s * 0.025f, baseY - s * 0.10f, innerX - dir * s * 0.12f, baseY - s * 0.14f, paint);
        canvas.drawLine(innerX - dir * s * 0.04f, baseY - s * 0.16f, innerX + dir * s * 0.02f, baseY - s * 0.24f, paint);
        canvas.drawLine(innerX - dir * s * 0.05f, baseY - s * 0.18f, innerX - dir * s * 0.11f, baseY - s * 0.22f, paint);
        canvas.drawLine(outerX, baseY, outerX + dir * s * 0.07f, baseY - s * 0.19f, paint);
        canvas.drawLine(outerX + dir * s * 0.025f, baseY - s * 0.09f, outerX + dir * s * 0.12f, baseY - s * 0.12f, paint);
        canvas.drawLine(outerX + dir * s * 0.04f, baseY - s * 0.15f, outerX + dir * s * 0.02f, baseY - s * 0.24f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
    }

    private void drawEars(Canvas canvas, float ex, float ey, float dir, float s) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(170, 120, 32));
        Path ear = new Path();
        ear.moveTo(ex, ey);
        ear.lineTo(ex - dir * s * 0.06f, ey - s * 0.08f);
        ear.lineTo(ex - dir * s * 0.01f, ey - s * 0.03f);
        ear.close();
        canvas.drawPath(ear, paint);
        paint.setColor(Color.rgb(210, 160, 60));
        canvas.drawLine(ex - dir * s * 0.01f, ey - s * 0.01f, ex - dir * s * 0.045f, ey - s * 0.06f, paint);
    }

    private void drawWhiskers(Canvas canvas, float snout, float hy, float dir, float s, long now) {
        float w1 = (float) Math.sin(now / 420.0) * s * 0.025f;
        float w2 = (float) Math.sin(now / 420.0 + 1.1) * s * 0.025f;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setColor(Color.rgb(245, 238, 210));
        paint.setStrokeWidth(s * 0.008f);
        Path a = new Path();
        a.moveTo(snout, hy - s * 0.008f);
        a.quadTo(snout + dir * s * 0.14f, hy - s * 0.07f + w1, snout + dir * s * 0.28f, hy - s * 0.03f + w1);
        canvas.drawPath(a, paint);
        Path b = new Path();
        b.moveTo(snout, hy + s * 0.02f);
        b.quadTo(snout + dir * s * 0.13f, hy + s * 0.08f + w2, snout + dir * s * 0.26f, hy + s * 0.10f + w2);
        canvas.drawPath(b, paint);
        Path c = new Path();
        c.moveTo(snout - dir * s * 0.02f, hy + s * 0.01f);
        c.quadTo(snout + dir * s * 0.08f, hy + s * 0.04f, snout + dir * s * 0.16f, hy + s * 0.03f);
        canvas.drawPath(c, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
    }
}

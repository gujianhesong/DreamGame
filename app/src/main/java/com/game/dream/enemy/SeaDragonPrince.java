package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 海底迷宫二层关卡 BOSS：东海龙太子敖丙。独立怪物，不是小青龙。
 * 三发窄扇水龙弹、龙尾横扫、龙威。半血落一道两段水龙卷，再猛扑一次。
 */
public class SeaDragonPrince extends Enemy {

    private static final float AURA_RANGE = 320f;
    private static final long AURA_SLOW = 700;
    private static final float PREFERRED_DISTANCE = 300f;
    private static final float MIN_DISTANCE = 180f;
    private static final int BOLT_COUNT = 3;
    private static final float BOLT_SPREAD = 12f;
    private static final long BOLT_COOLDOWN = 2000;

    private long lastWaterBoltTime = 0;
    private boolean pendingWaterBolt = false;
    private boolean forcedPounce = false;
    private boolean pounceArmed = false;
    private long forcedPounceDeadline = 0;

    public SeaDragonPrince(float x, float y) {
        super(x, y, 480);
        becomeUniqueBoss();
        attackCooldown = 2200;
        pounceFixedSpeed = 780f;
        setAttackShape(AttackShape.CIRCLE);
        addAvailableAttackType(AttackType.SPIN_ATTACK);
        spinDuration = 1200;
        spinHitInterval = 400;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 640;
        prop.attackRange = 400;
        prop.rewardExp = 7000;
        prop.rewardMoney = 3500;
        setPropertyExtra(prop);
        setProperty(3000*30, 2500, 2500, 400, 3000);
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

    public void beginRagePounce() {
        forcedPounce = true;
        pounceArmed = false;
        interruptBossRetreat();
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        long now = System.currentTimeMillis();
        if (forcedPounce && !pounceArmed && isAlive() && !isStunned() && !isFrozen()) {
            interruptBossRetreat();
            currentState = State.ATTACKING;
            currentAttackType = AttackType.POUNCE;
            isWindingUp = true;
            isPounceRetreating = false;
            windUpStartTime = now;
            pounceArmed = true;
            forcedPounceDeadline = now + 8000;
        }
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (pounceArmed && (isPounceRetreating || now > forcedPounceDeadline)) {
            forcedPounce = false;
            pounceArmed = false;
        }
        if (!isAlive() || isStunned() || isFrozen()) {
            return;
        }
        tickSpellWindUp(now, playerX, playerY);
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (currentState == State.CHASING) {
            tryCastWaterBolt(dist, now);
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
            targetX = x - dx / dist * 200;
            targetY = y - dy / dist * 200;
        } else if (dist > PREFERRED_DISTANCE * 1.5f) {
            targetX = playerX;
            targetY = playerY;
        } else {
            float perpX = -dy / dist;
            float perpY = dx / dist;
            float circleDir = (Math.sin(System.currentTimeMillis() / 2000.0) > 0) ? 1 : -1;
            targetX = x + perpX * 100 * circleDir;
            targetY = y + perpY * 100 * circleDir;
        }
        moveToTargetWithSpeed(deltaSeconds, speed * 1.1f);
    }

    private void tryCastWaterBolt(float dist, long now) {
        if (isSpellWindingUp || pendingWaterBolt || dist >= propertyExtra.detectionRange) {
            return;
        }
        if (now - lastWaterBoltTime < BOLT_COOLDOWN) {
            return;
        }
        if (now - lastSpellCastRollTime < SPELL_CAST_CHECK_INTERVAL) {
            return;
        }
        lastSpellCastRollTime = now;
        float chance = dist < MIN_DISTANCE ? 0.06f : (dist <= PREFERRED_DISTANCE ? 0.45f : 0.28f);
        if (isBossPhaseTwo()) {
            chance = Math.min(0.70f, chance * 1.6f);
        }
        if (Math.random() < chance) {
            beginSpellWindUp(SPELL_WATER_BOLT);
        }
    }

    @Override
    protected void onSpecialSpellWindUpComplete(int spellId, long currentTime) {
        if (spellId == SPELL_WATER_BOLT) {
            pendingWaterBolt = true;
            lastWaterBoltTime = currentTime;
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel3(this);
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
        float cy = y + offsetY + bobOffset * 0.6f;
        boolean facingRight = targetX >= x;
        float dir = facingRight ? 1f : -1f;
        float s = size;
        boolean aggressive = currentState == State.CHASING || currentState == State.ATTACKING || pendingWaterBolt;
        paint.setAntiAlias(true);

        float auraPulse = 1f + 0.06f * (float) Math.sin(now / 420.0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(36, 120, 210, 255));
        canvas.drawCircle(cx, cy, AURA_RANGE * 0.20f * auraPulse, paint);
        paint.setColor(Color.argb(20, 230, 250, 255));
        canvas.drawCircle(cx, cy, AURA_RANGE * 0.11f * auraPulse, paint);

        int segments = 13;
        float[] bx = new float[segments];
        float[] by = new float[segments];
        float[] thick = new float[segments];
        for (int i = 0; i < segments; i++) {
            float t = i / (float) (segments - 1);
            float sway = (float) Math.sin(now / 280.0 + t * 4.4f) * s * 0.06f;
            bx[i] = cx + dir * s * (0.34f - t * 0.84f);
            by[i] = cy - s * 0.05f + t * s * 0.07f + sway;
            thick[i] = s * 0.072f;
            if (t > 0.85f) {
                thick[i] = s * (0.072f - (t - 0.85f) / 0.15f * 0.016f);
            }
        }

        drawTail(canvas, bx[segments - 1], by[segments - 1], dir, s, now);
        drawLegs(canvas, bx, by, dir, s, now, true);
        drawBody(canvas, bx, by, thick, dir);
        drawFins(canvas, bx, by, thick, dir, s, now);
        drawLegs(canvas, bx, by, dir, s, now, false);
        drawHead(canvas, bx[0], by[0], dir, s, now, aggressive);
    }

    private void drawBody(Canvas canvas, float[] bx, float[] by, float[] thick, float dir) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < bx.length - 1; i++) {
            paint.setStrokeWidth(thick[i] * 2f);
            paint.setColor(Color.rgb(70, 170, 210));
            canvas.drawLine(bx[i], by[i], bx[i + 1], by[i + 1], paint);
            paint.setStrokeWidth(thick[i] * 1.5f);
            paint.setColor(Color.rgb(140, 220, 245));
            canvas.drawLine(bx[i], by[i] - thick[i] * 0.06f, bx[i + 1], by[i + 1] - thick[i] * 0.06f, paint);
            paint.setStrokeWidth(thick[i] * 0.75f);
            paint.setColor(Color.rgb(245, 252, 255));
            canvas.drawLine(bx[i], by[i] + thick[i] * 0.22f, bx[i + 1], by[i + 1] + thick[i] * 0.22f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        for (int i = 1; i < bx.length - 2; i++) {
            paint.setColor(Color.argb(90, 255, 255, 255));
            canvas.drawCircle(bx[i] + dir * thick[i] * 0.1f, by[i] - thick[i] * 0.1f, thick[i] * 0.18f, paint);
        }
        paint.setStrokeWidth(1f);
    }

    private void drawFins(Canvas canvas, float[] bx, float[] by, float[] thick, float dir, float s, long now) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 1; i < bx.length - 3; i++) {
            float wave = (float) Math.sin(now / 240.0 + i * 0.8) * s * 0.01f;
            Path fin = new Path();
            float mx = bx[i];
            float my = by[i] - thick[i] * 0.5f;
            fin.moveTo(mx - dir * thick[i] * 0.16f, my);
            fin.quadTo(mx, my - thick[i] * 0.7f - wave, mx + dir * thick[i] * 0.16f, my);
            fin.close();
            paint.setColor(i % 2 == 0 ? Color.rgb(90, 190, 230) : Color.rgb(180, 235, 250));
            canvas.drawPath(fin, paint);
        }
    }

    private void drawLegs(Canvas canvas, float[] bx, float[] by, float dir, float s, long now, boolean backLayer) {
        int[] joints = backLayer ? new int[]{3, 8} : new int[]{2, 7};
        float side = backLayer ? -dir : dir;
        float step = (float) Math.sin(now / 220.0) * s * 0.018f;
        for (int idx = 0; idx < joints.length; idx++) {
            int i = joints[idx];
            if (i >= bx.length) {
                continue;
            }
            float hipX = bx[i] + side * s * 0.035f;
            float hipY = by[i] + s * 0.015f;
            float pawX = hipX + side * s * 0.05f + (idx == 0 ? step : -step);
            float pawY = hipY + s * 0.13f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.rgb(80, 180, 215));
            paint.setStrokeWidth(s * 0.028f);
            canvas.drawLine(hipX, hipY, pawX, pawY - s * 0.03f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(160, 220, 240));
            canvas.drawCircle(pawX, pawY, s * 0.022f, paint);
            paint.setColor(Color.rgb(245, 250, 255));
            paint.setStrokeWidth(s * 0.009f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(pawX, pawY, pawX + dir * s * 0.03f, pawY + s * 0.012f, paint);
            canvas.drawLine(pawX, pawY, pawX + dir * s * 0.018f, pawY + s * 0.026f, paint);
            canvas.drawLine(pawX, pawY, pawX - dir * s * 0.016f, pawY + s * 0.022f, paint);
            canvas.drawLine(pawX, pawY, pawX - dir * s * 0.028f, pawY + s * 0.006f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
    }

    private void drawTail(Canvas canvas, float tx, float ty, float dir, float s, long now) {
        float flap = (float) Math.sin(now / 200.0) * s * 0.035f;
        Path fin = new Path();
        fin.moveTo(tx, ty);
        fin.quadTo(tx - dir * s * 0.12f + flap, ty - s * 0.12f, tx - dir * s * 0.05f, ty);
        fin.quadTo(tx - dir * s * 0.13f - flap, ty + s * 0.11f, tx, ty);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(110, 200, 235));
        canvas.drawPath(fin, paint);
        paint.setColor(Color.rgb(230, 248, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.01f);
        canvas.drawPath(fin, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
    }

    private void drawHead(Canvas canvas, float hx, float hy, float dir, float s, long now, boolean aggressive) {
        float snout = hx + dir * s * 0.13f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(150, 220, 245));
        canvas.drawCircle(hx, hy, s * 0.078f, paint);
        canvas.drawOval(snout - s * 0.05f, hy - s * 0.03f, snout + s * 0.05f, hy + s * 0.04f, paint);
        paint.setColor(Color.rgb(245, 252, 255));
        canvas.drawOval(hx - s * 0.04f, hy + s * 0.01f, hx + s * 0.05f, hy + s * 0.07f, paint);

        paint.setColor(Color.rgb(220, 200, 90));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(s * 0.014f);
        canvas.drawLine(hx - dir * s * 0.01f, hy - s * 0.07f, hx - dir * s * 0.03f, hy - s * 0.16f, paint);
        canvas.drawLine(hx - dir * s * 0.02f, hy - s * 0.12f, hx - dir * s * 0.07f, hy - s * 0.14f, paint);
        canvas.drawLine(hx + dir * s * 0.03f, hy - s * 0.07f, hx + dir * s * 0.05f, hy - s * 0.15f, paint);
        canvas.drawLine(hx + dir * s * 0.04f, hy - s * 0.11f, hx + dir * s * 0.08f, hy - s * 0.13f, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(120, 200, 230));
        Path ear = new Path();
        ear.moveTo(hx - dir * s * 0.03f, hy - s * 0.03f);
        ear.quadTo(hx - dir * s * 0.06f, hy - s * 0.08f, hx - dir * s * 0.02f, hy - s * 0.06f);
        ear.close();
        canvas.drawPath(ear, paint);

        float eyeX = hx + dir * s * 0.045f;
        float eyeY = hy - s * 0.012f;
        paint.setColor(Color.WHITE);
        canvas.drawCircle(eyeX, eyeY, s * 0.028f, paint);
        paint.setColor(aggressive ? Color.rgb(80, 160, 220) : Color.rgb(70, 140, 200));
        canvas.drawCircle(eyeX + dir * s * 0.004f, eyeY + s * 0.002f, s * 0.016f, paint);
        paint.setColor(Color.rgb(20, 30, 50));
        canvas.drawCircle(eyeX + dir * s * 0.006f, eyeY + s * 0.002f, s * 0.008f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(eyeX - dir * s * 0.008f, eyeY - s * 0.008f, s * 0.007f, paint);

        paint.setColor(Color.argb(80, 255, 140, 150));
        canvas.drawCircle(hx + dir * s * 0.02f, hy + s * 0.028f, s * 0.016f, paint);

        paint.setColor(Color.rgb(40, 90, 120));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(s * 0.008f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        Path smile = new Path();
        smile.moveTo(snout - dir * s * 0.03f, hy + s * 0.02f);
        smile.quadTo(snout, hy + s * 0.032f, snout + dir * s * 0.02f, hy + s * 0.018f);
        canvas.drawPath(smile, paint);

        paint.setColor(Color.rgb(230, 245, 255));
        float w1 = (float) Math.sin(now / 260.0) * s * 0.03f;
        float w2 = (float) Math.sin(now / 260.0 + 1.2) * s * 0.03f;
        Path a = new Path();
        a.moveTo(snout, hy);
        a.quadTo(snout + dir * s * 0.08f, hy - s * 0.04f + w1, snout + dir * s * 0.16f, hy - s * 0.01f + w1);
        canvas.drawPath(a, paint);
        Path b = new Path();
        b.moveTo(snout, hy + s * 0.016f);
        b.quadTo(snout + dir * s * 0.07f, hy + s * 0.05f + w2, snout + dir * s * 0.14f, hy + s * 0.05f + w2);
        canvas.drawPath(b, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);

        if (aggressive) {
            paint.setColor(Color.argb(140, 160, 230, 255));
            canvas.drawCircle(snout + dir * s * 0.03f, hy + s * 0.006f, s * 0.022f, paint);
        }
    }
}

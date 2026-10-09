package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 海底迷宫一层关卡 BOSS：龟丞相。独立怪物，不是大海龟。
 * 慢速两连爪、龟壳冲撞，读条后在玩家脚下落镇海印。半血缩壳并再落一印。
 */
public class TurtleChancellor extends Enemy {

    private static final long SEAL_COOLDOWN = 4000;
    private static final long PHASE_SHELL_MS = 4000;

    private long lastSealTime = 0;
    private boolean pendingSeal = false;
    private boolean phaseShell = false;
    private long phaseShellEnd = 0;
    private int pendingReflectDamage = 0;

    public TurtleChancellor(float x, float y) {
        super(x, y, 400);
        enemyLevel = EnemyLevel.STAGE_BOSS;
        attackCooldown = 2400;
        windUpDuration = 560;
        pounceFixedSpeed = 520f;
        comboHitCount = 2;
        comboHitInterval = 340;
        setAttackShape(AttackShape.ARC);

        addAvailableAttackType(AttackType.COMBO);
        addAvailableAttackType(AttackType.POUNCE);

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 520;
        prop.attackRange = 220;
        prop.rewardExp = 5000;
        prop.rewardMoney = 2500;
        setPropertyExtra(prop);
        setProperty(3000*30, 2200, 2200, 300, 2500);
    }

    @Override
    public boolean usesGenericFireball() {
        return false;
    }

    @Override
    public boolean takeDamage(int damage) {
        if (phaseShell && System.currentTimeMillis() < phaseShellEnd) {
            pendingReflectDamage += Math.round(damage * 0.3f);
            return super.takeDamage(Math.max(1, Math.round(damage * 0.2f)));
        }
        return super.takeDamage(damage);
    }

    public int consumeReflectDamage() {
        int dmg = pendingReflectDamage;
        pendingReflectDamage = 0;
        return dmg;
    }

    public void beginPhaseShell() {
        phaseShell = true;
        phaseShellEnd = System.currentTimeMillis() + PHASE_SHELL_MS;
    }

    public boolean isPendingSeal() {
        return pendingSeal;
    }

    public void consumeSeal() {
        pendingSeal = false;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive() || isStunned() || isFrozen()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (phaseShell && now >= phaseShellEnd) {
            phaseShell = false;
        }
        tickSpellWindUp(now, playerX, playerY);
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        tryCastSeal(dist, now);
    }

    private void tryCastSeal(float dist, long now) {
        if (isSpellWindingUp || pendingSeal || phaseShell) {
            return;
        }
        if (currentState != State.CHASING && currentState != State.ATTACKING) {
            return;
        }
        if (dist >= propertyExtra.detectionRange || dist < 80f) {
            return;
        }
        if (now - lastSealTime < SEAL_COOLDOWN) {
            return;
        }
        if (now - lastSpellCastRollTime < SPELL_CAST_CHECK_INTERVAL) {
            return;
        }
        lastSpellCastRollTime = now;
        float chance = dist < propertyExtra.attackRange ? 0.22f : 0.38f;
        if (isBossPhaseTwo()) {
            chance = Math.min(0.70f, chance * 1.6f);
        }
        if (Math.random() >= chance) {
            return;
        }
        currentState = State.CHASING;
        beginSpellWindUp(SPELL_SEA_SEAL);
    }

    @Override
    protected void onSpecialSpellWindUpComplete(int spellId, long currentTime) {
        if (spellId == SPELL_SEA_SEAL) {
            pendingSeal = true;
            lastSealTime = currentTime;
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
        paint.setAntiAlias(true);
        float scale = size / 30.0f;
        long now = System.currentTimeMillis();

        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = now - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 3 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY + bobOffset * 0.25f;
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        if (phaseShell && now < phaseShellEnd) {
            drawSealedShell(canvas, paint, cx, cy, scale, now);
            return;
        }

        drawShadow(canvas, paint, cx, cy, scale);
        drawLegs(canvas, paint, cx, cy, scale, dir);
        drawRobe(canvas, paint, cx, cy, scale, now);
        drawShell(canvas, paint, cx, cy, scale, now);
        drawHead(canvas, paint, cx, cy, scale, now, dir);
        drawJadeTablet(canvas, paint, cx, cy, scale, now, dir);

        if (currentState == State.ATTACKING && isWindingUp) {
            float progress = getWindUpProgress();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.2f * scale);
            paint.setColor(Color.argb((int) (80 + progress * 140), 90, 220, 180));
            float r = (12 + progress * 10) * scale;
            canvas.drawCircle(cx, cy, r, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawShadow(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(55, 0, 0, 0));
        canvas.drawOval(cx - 18 * scale, cy + 10 * scale, cx + 18 * scale, cy + 16 * scale, paint);
    }

    private void drawLegs(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        float swingA = (float) Math.sin(animFrame * Math.PI / 2) * 1.6f * scale;
        float swingB = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 1.6f * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(48, 86, 72));
        canvas.drawOval(cx - 16 * scale + dir * 3 * scale, cy + 4 * scale + swingA,
                cx - 8 * scale + dir * 3 * scale, cy + 13 * scale + swingA, paint);
        canvas.drawOval(cx + 8 * scale + dir * 3 * scale, cy + 4 * scale + swingB,
                cx + 16 * scale + dir * 3 * scale, cy + 13 * scale + swingB, paint);
        canvas.drawOval(cx - 14 * scale - dir * 5 * scale, cy + 6 * scale + swingB,
                cx - 6 * scale - dir * 5 * scale, cy + 14 * scale + swingB, paint);
        canvas.drawOval(cx + 6 * scale - dir * 5 * scale, cy + 6 * scale + swingA,
                cx + 14 * scale - dir * 5 * scale, cy + 14 * scale + swingA, paint);

        paint.setColor(Color.rgb(214, 176, 72));
        canvas.drawOval(cx - 16 * scale + dir * 3 * scale, cy + 11 * scale + swingA,
                cx - 8 * scale + dir * 3 * scale, cy + 13.4f * scale + swingA, paint);
        canvas.drawOval(cx + 8 * scale + dir * 3 * scale, cy + 11 * scale + swingB,
                cx + 16 * scale + dir * 3 * scale, cy + 13.4f * scale + swingB, paint);
    }

    private void drawRobe(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float wave = (float) Math.sin(now / 520.0) * 1.4f * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(118, 24, 28));
        Path robe = new Path();
        robe.moveTo(cx - 11 * scale, cy - 2 * scale);
        robe.lineTo(cx + 11 * scale, cy - 2 * scale);
        robe.lineTo(cx + 17 * scale + wave, cy + 12 * scale);
        robe.lineTo(cx + 8 * scale, cy + 11 * scale);
        robe.lineTo(cx - 8 * scale, cy + 11 * scale);
        robe.lineTo(cx - 17 * scale - wave, cy + 12 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        paint.setColor(Color.rgb(236, 196, 86));
        canvas.drawRect(cx - 12 * scale, cy + 1.2f * scale, cx + 12 * scale, cy + 4.6f * scale, paint);
        paint.setColor(Color.rgb(164, 28, 32));
        canvas.drawRect(cx - 2.2f * scale, cy + 0.6f * scale, cx + 2.2f * scale, cy + 5.2f * scale, paint);
        paint.setColor(Color.rgb(255, 232, 140));
        canvas.drawCircle(cx, cy + 2.9f * scale, 1.6f * scale, paint);
    }

    private void drawShell(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 0, 0, 0));
        canvas.drawOval(cx - 16 * scale, cy - 7 * scale, cx + 16 * scale, cy + 9 * scale, paint);

        paint.setColor(Color.rgb(22, 58, 52));
        canvas.drawOval(cx - 15.5f * scale, cy - 10 * scale, cx + 15.5f * scale, cy + 8 * scale, paint);
        paint.setColor(Color.rgb(34, 86, 74));
        canvas.drawOval(cx - 13.5f * scale, cy - 8.5f * scale, cx + 13.5f * scale, cy + 6.5f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f * scale);
        paint.setColor(Color.rgb(214, 176, 78));
        canvas.drawOval(cx - 15.5f * scale, cy - 10 * scale, cx + 15.5f * scale, cy + 8 * scale, paint);

        paint.setStrokeWidth(1.1f * scale);
        canvas.drawCircle(cx, cy - 1.5f * scale, 5.2f * scale, paint);
        for (int i = 0; i < 6; i++) {
            float a = i * (float) Math.PI / 3f + 0.2f;
            float hx = cx + (float) Math.cos(a) * 9.2f * scale;
            float hy = cy - 1.2f * scale + (float) Math.sin(a) * 5.2f * scale;
            canvas.drawCircle(hx, hy, 3.1f * scale, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        float pulse = 0.75f + 0.25f * (float) Math.sin(now / 380.0);
        paint.setColor(Color.argb((int) (160 + 80 * pulse), 255, 220, 110));
        canvas.drawCircle(cx, cy - 1.5f * scale, 2.4f * scale, paint);
        paint.setColor(Color.rgb(36, 28, 12));
        paint.setStrokeWidth(1.3f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(cx - 1.3f * scale, cy - 3.1f * scale, cx + 1.3f * scale, cy + 0.1f * scale, paint);
        canvas.drawLine(cx - 1.3f * scale, cy - 1.5f * scale, cx + 1.3f * scale, cy - 1.5f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(55, 190, 230, 200));
        canvas.drawOval(cx - 7 * scale, cy - 7 * scale, cx + 1 * scale, cy - 1 * scale, paint);
        paint.setStrokeWidth(1);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        float headX = cx + dir * 15.5f * scale;
        float headY = cy - 4.5f * scale;
        float nod = (float) Math.sin(now / 640.0) * 0.8f * scale;
        float sway = (float) Math.sin(now / 420.0) * 0.9f * scale;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(118, 148, 108));
        canvas.drawOval(headX - 6.2f * scale, headY - 5.4f * scale + nod,
                headX + 7.4f * scale, headY + 5.8f * scale + nod, paint);
        paint.setColor(Color.rgb(96, 126, 90));
        canvas.drawOval(headX + dir * 2.2f * scale, headY - 1.6f * scale + nod,
                headX + dir * 7.6f * scale, headY + 3.4f * scale + nod, paint);

        paint.setColor(Color.rgb(40, 52, 36));
        paint.setStrokeWidth(0.7f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(headX - 1 * scale, headY - 2.4f * scale + nod,
                headX + 4 * scale, headY - 2.4f * scale + nod, paint);
        canvas.drawLine(headX - 0.4f * scale, headY - 0.6f * scale + nod,
                headX + 4.6f * scale, headY - 0.6f * scale + nod, paint);
        paint.setStyle(Paint.Style.FILL);

        float eyeX = headX + dir * 3.4f * scale;
        float eyeY = headY - 1.4f * scale + nod;
        paint.setColor(Color.rgb(18, 22, 16));
        canvas.drawOval(eyeX - 1.6f * scale, eyeY - 1.1f * scale, eyeX + 1.6f * scale, eyeY + 0.9f * scale, paint);
        paint.setColor(Color.rgb(255, 214, 90));
        canvas.drawCircle(eyeX + dir * 0.2f * scale, eyeY - 0.15f * scale, 0.7f * scale, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(eyeX - dir * 0.35f * scale, eyeY - 0.4f * scale, 0.28f * scale, paint);

        paint.setColor(Color.rgb(236, 232, 214));
        paint.setStrokeWidth(1.15f * scale);
        paint.setStyle(Paint.Style.STROKE);
        Path beardL = new Path();
        beardL.moveTo(headX - dir * 0.4f * scale, headY + 3.4f * scale + nod);
        beardL.quadTo(headX - dir * 3.5f * scale + sway, headY + 7.5f * scale + nod,
                headX - dir * 1.2f * scale, headY + 11.5f * scale + nod);
        canvas.drawPath(beardL, paint);
        Path beardR = new Path();
        beardR.moveTo(headX + dir * 1.4f * scale, headY + 3.6f * scale + nod);
        beardR.quadTo(headX + dir * 4.2f * scale - sway, headY + 8.2f * scale + nod,
                headX + dir * 2.4f * scale, headY + 12.2f * scale + nod);
        canvas.drawPath(beardR, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(headX + dir * 0.6f * scale, headY + 4.2f * scale + nod, 1.5f * scale, paint);

        drawOfficialHat(canvas, paint, headX, headY + nod, scale, now);
        paint.setStrokeWidth(1);
    }

    private void drawOfficialHat(Canvas canvas, Paint paint, float hx, float hy, float scale, long now) {
        float flap = (float) Math.sin(now / 480.0) * 0.8f * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(18, 18, 24));
        canvas.drawRoundRect(hx - 8.4f * scale, hy - 10.4f * scale,
                hx + 8.4f * scale, hy - 5.2f * scale, 1.2f * scale, 1.2f * scale, paint);
        canvas.drawRect(hx - 6.2f * scale, hy - 5.2f * scale, hx + 6.2f * scale, hy - 3.6f * scale, paint);

        paint.setColor(Color.rgb(214, 176, 78));
        canvas.drawRect(hx - 8.4f * scale, hy - 10.4f * scale, hx + 8.4f * scale, hy - 9.5f * scale, paint);
        canvas.drawCircle(hx, hy - 11.1f * scale, 1.5f * scale, paint);

        paint.setColor(Color.rgb(12, 12, 18));
        Path leftWing = new Path();
        leftWing.moveTo(hx - 8.2f * scale, hy - 8.4f * scale);
        leftWing.lineTo(hx - 15.5f * scale, hy - 9.8f * scale + flap);
        leftWing.lineTo(hx - 15.2f * scale, hy - 7.2f * scale + flap);
        leftWing.lineTo(hx - 8.2f * scale, hy - 6.6f * scale);
        leftWing.close();
        canvas.drawPath(leftWing, paint);
        Path rightWing = new Path();
        rightWing.moveTo(hx + 8.2f * scale, hy - 8.4f * scale);
        rightWing.lineTo(hx + 15.5f * scale, hy - 9.8f * scale - flap);
        rightWing.lineTo(hx + 15.2f * scale, hy - 7.2f * scale - flap);
        rightWing.lineTo(hx + 8.2f * scale, hy - 6.6f * scale);
        rightWing.close();
        canvas.drawPath(rightWing, paint);

        paint.setColor(Color.rgb(236, 196, 86));
        canvas.drawCircle(hx - 15.4f * scale, hy - 8.5f * scale + flap, 0.7f * scale, paint);
        canvas.drawCircle(hx + 15.4f * scale, hy - 8.5f * scale - flap, 0.7f * scale, paint);
    }

    private void drawJadeTablet(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        float tx = cx + dir * 10 * scale;
        float ty = cy + 1.5f * scale;
        float glow = 0.7f + 0.3f * (float) Math.sin(now / 320.0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(88, 148, 124));
        canvas.drawRoundRect(tx - 1.6f * scale, ty - 8.8f * scale,
                tx + 1.6f * scale, ty + 6.6f * scale, 0.7f * scale, 0.7f * scale, paint);
        paint.setColor(Color.argb((int) (90 + 80 * glow), 180, 255, 220));
        canvas.drawRoundRect(tx - 1.1f * scale, ty - 8.1f * scale,
                tx + 1.1f * scale, ty + 5.8f * scale, 0.5f * scale, 0.5f * scale, paint);
        paint.setColor(Color.rgb(214, 176, 78));
        canvas.drawCircle(tx, ty - 6.4f * scale, 0.7f * scale, paint);
        paint.setColor(Color.rgb(118, 148, 108));
        canvas.drawCircle(tx - dir * 1.8f * scale, ty + 5.4f * scale, 1.6f * scale, paint);
    }

    private void drawSealedShell(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float pulse = 0.94f + 0.06f * (float) Math.sin(now / 160.0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 0, 0, 0));
        canvas.drawOval(cx - 18 * scale, cy + 6 * scale, cx + 18 * scale, cy + 14 * scale, paint);

        paint.setColor(Color.argb(40, 80, 220, 160));
        canvas.drawCircle(cx, cy - 1 * scale, 22 * scale * pulse, paint);

        paint.setColor(Color.rgb(16, 52, 46));
        canvas.drawOval(cx - 16.5f * scale * pulse, cy - 11 * scale * pulse,
                cx + 16.5f * scale * pulse, cy + 9 * scale * pulse, paint);
        paint.setColor(Color.rgb(28, 78, 66));
        canvas.drawOval(cx - 14 * scale * pulse, cy - 9 * scale * pulse,
                cx + 14 * scale * pulse, cy + 7 * scale * pulse, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f * scale);
        paint.setColor(Color.rgb(255, 214, 90));
        canvas.drawOval(cx - 16.5f * scale * pulse, cy - 11 * scale * pulse,
                cx + 16.5f * scale * pulse, cy + 9 * scale * pulse, paint);
        canvas.drawCircle(cx, cy - 1 * scale, 6.2f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 228, 120));
        canvas.drawCircle(cx, cy - 1 * scale, 3.4f * scale, paint);
        paint.setColor(Color.rgb(36, 24, 8));
        paint.setStrokeWidth(1.6f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(cx - 1.8f * scale, cy - 3.2f * scale, cx + 1.8f * scale, cy + 1.2f * scale, paint);
        canvas.drawLine(cx - 1.8f * scale, cy - 1 * scale, cx + 1.8f * scale, cy - 1 * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(18, 18, 24));
        canvas.drawRoundRect(cx - 6 * scale, cy - 16.5f * scale,
                cx + 6 * scale, cy - 12.4f * scale, 1 * scale, 1 * scale, paint);
        paint.setColor(Color.rgb(214, 176, 78));
        canvas.drawCircle(cx, cy - 17.2f * scale, 1.3f * scale, paint);
        paint.setStrokeWidth(1);
    }
}

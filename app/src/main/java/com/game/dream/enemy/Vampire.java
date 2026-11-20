package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.common.Constants;
import com.game.dream.item.Item;
import com.game.dream.map.HellMazeGenerator;
import com.game.dream.system.MapSystem;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 吸血鬼 · 血色贵公子 - 地狱迷宫第2层血池炼狱主题怪物
 * 生前为堕落的血色贵族，死后沉眠于血池深处，以阳人鲜血维系不死之身。
 *
 * 视觉(参照梦幻西游吸血鬼): 圆形竹编斗笠，帽檐四周垂下一圈帘子把脸
 *       完全挡住；深紫黑窄袍微驼；袖口从帘下伸出苍白细腕与短黑爪；
 *       脚下悬浮，拖出血雾尾迹。首领/精英帘更长更红，BOSS斗笠更大。
 *
 * 攻击:
 *  1. 血之吮吸 - DRAIN_BITE 近战咬击，命中回复自身伤害量 60% HP
 *  2. 血雾遁形 - BLINK_STRIKE（LEADER+），瞬移到玩家身边背刺
 *  3. 血池共鸣 - 被动，站在 HELL_BLOOD_POOL tile 上时每秒回复 3% HP、
 *     移速 +30%、攻速 +25%；离开血池 5 秒后失效
 *
 * 属性: HP 中偏高、物理攻击高、物理防御中、魔抗低（弱火/雷）、移速快
 * 出没: 血池炼狱的血池边缘
 */
public class Vampire extends Enemy {

    // 血池共鸣状态
    private boolean bloodPoolBuffActive = false;
    private long lastOnBloodPoolTime = 0;
    private long lastBloodRegenTime = 0;
    private static final long BLOOD_REGEN_INTERVAL = 1000;
    private static final long BUFF_LINGER_DURATION = 5000; // 离开血池后Buff残留时长

    // 原始属性缓存（用于Buff切换）
    private int baseSpeed;
    private long baseAttackCooldown;

    // 视觉动画
    private float capePhase = 0f;
    private float floatPhase = (float) (Math.random() * Math.PI * 2);
    private float eyeGlowPhase = 0f;

    public Vampire(float x, float y) {
        super(x, y, 82);
        attackCooldown = 1800;
        setAttackShape(AttackShape.ARC);      // 獠牙撕咬 - 扇形
        addAvailableAttackType(AttackType.MELEE);
        addAvailableAttackType(AttackType.DRAIN_BITE);
        windUpDuration = 450;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 460;
        prop.attackRange = 140;
        prop.rewardExp = 620;
        prop.rewardMoney = 320;
        setPropertyExtra(prop);

        // HP中偏高、攻击高、防御中、速度快、法力低(魔抗低)
        setProperty(2300, 850, 600, 300, 300);

        // 等级分布
        resetPropertyWithLevel();

        // 首领以上追加血雾遁形
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.BLINK_STRIKE);
        }

        // 吸血比例提升（默认 50%）
        drainHealPercent = 0.6f;

        // 缓存基础值
        baseSpeed = speed;
        baseAttackCooldown = attackCooldown;
    }

    @Override
    public boolean usesGenericFireball() {
        return false; // 吸血鬼使用纯近战 + 血池共鸣
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;

        tickVisual(deltaTime);

        // 血池共鸣：检测所站地形
        long now = System.currentTimeMillis();
        boolean onBloodPool = isStandingOnBloodPool();
        if (onBloodPool) {
            lastOnBloodPoolTime = now;
            if (!bloodPoolBuffActive) {
                bloodPoolBuffActive = true;
                applyBloodPoolBuff();
            }
            // 每秒回复 3% 最大 HP
            if (now - lastBloodRegenTime >= BLOOD_REGEN_INTERVAL) {
                lastBloodRegenTime = now;
                int heal = Math.max(1, (int) (maxHealth * 0.03f));
                health = Math.min(maxHealth, health + heal);
            }
        } else if (bloodPoolBuffActive && now - lastOnBloodPoolTime > BUFF_LINGER_DURATION) {
            // 离开血池超过5秒，Buff失效
            bloodPoolBuffActive = false;
            removeBloodPoolBuff();
        }
    }

    private boolean isStandingOnBloodPool() {
        try {
            int[][] map = MapSystem.getInstance().getCurMapInfo().getMapData();
            if (map == null) return false;
            int tx = (int) (x / Constants.TILE_SIZE);
            int ty = (int) (y / Constants.TILE_SIZE);
            if (ty < 0 || ty >= map.length || tx < 0 || tx >= map[0].length) return false;
            return map[ty][tx] == HellMazeGenerator.HELL_BLOOD_POOL;
        } catch (Exception e) {
            return false;
        }
    }

    private void applyBloodPoolBuff() {
        speed = (int) (baseSpeed * 1.3f);
        attackCooldown = (long) (baseAttackCooldown * 0.8f);
    }

    private void removeBloodPoolBuff() {
        speed = baseSpeed;
        attackCooldown = baseAttackCooldown;
    }

    public boolean isBloodPoolBuffActive() {
        return bloodPoolBuffActive;
    }

    @Override
    public void updateDisplayOnly(long deltaTime) {
        super.updateDisplayOnly(deltaTime);
        tickVisual(deltaTime);
    }

    private void tickVisual(long deltaTime) {
        capePhase += 0.0055f * deltaTime;
        floatPhase += 0.0042f * deltaTime;
        eyeGlowPhase += 0.006f * deltaTime;
    }

    @Override
    protected void performAttack() {
        // 由 GameEngine 处理
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel4(this);
        }
        return possibleDrops;
    }

    // ==================== 绘制 ====================

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        paint.setAntiAlias(true);
        float scale = size / 30.0f;
        long now = System.currentTimeMillis();

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = now - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 3 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        float t = now / 1000f;
        float floatY = (float) Math.sin(t * 2.35f + floatPhase) * 5.8f * scale;
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY + floatY - 7 * scale;
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;
        float brimY = hatBrimY(cy, scale);
        float hatTilt = (float) Math.sin(t * 1.65f + floatPhase) * 3.2f;
        if (currentState == State.ATTACKING && isWindingUp) {
            hatTilt += dir * getWindUpProgress() * 8f;
        }

        drawBloodMist(canvas, paint, cx, cy, scale, now);
        drawHatVeil(canvas, paint, cx, cy, scale, true);
        drawRobe(canvas, paint, cx, cy, scale, now);
        drawArms(canvas, paint, cx, cy, scale);
        drawHatVeil(canvas, paint, cx, cy, scale, false);

        canvas.save();
        canvas.rotate(hatTilt, cx, brimY);
        drawRoundHat(canvas, paint, cx, cy, scale, now);
        canvas.restore();
        if (currentState == State.ATTACKING && isWindingUp) {
            drawFangSlashEffect(canvas, paint, cx, cy, scale, dir);
        }
        if (bloodPoolBuffActive) {
            drawBloodPoolAura(canvas, paint, cx, cy, scale, now);
        }
    }

    private boolean isHatElite() {
        return enemyLevel == EnemyLevel.LEADER
                || enemyLevel == EnemyLevel.ELITE
                || enemyLevel == EnemyLevel.BOSS
                || enemyLevel == EnemyLevel.STAGE_BOSS;
    }

    private boolean isHatBoss() {
        return enemyLevel == EnemyLevel.BOSS || enemyLevel == EnemyLevel.STAGE_BOSS;
    }

    private void drawBloodMist(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        for (int i = 0; i < 5; i++) {
            float angle = now / 520f + i * (float) (Math.PI * 2 / 5);
            float mx = cx + (float) Math.cos(angle) * 13 * scale;
            float my = cy + 23 * scale + (float) Math.sin(angle * 1.4f) * 4.5f * scale;
            float pulse = 0.45f + 0.35f * (float) Math.sin(now / 380.0 + i);
            paint.setColor(Color.argb((int) (85 * pulse), 170, 30, 40));
            canvas.drawCircle(mx, my, 6.5f * scale, paint);
            paint.setColor(Color.argb((int) (140 * pulse), 220, 50, 55));
            canvas.drawCircle(mx, my, 2.8f * scale, paint);
        }
    }

    private void drawRobe(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float waveL = (float) Math.sin(now / 430.0) * 2.8f * scale;
        float waveR = (float) Math.sin(now / 430.0 + 1.3f) * 2.8f * scale;
        paint.setColor(Color.argb(isHatElite() ? 220 : 170, 128, 28, 42));
        Path lining = new Path();
        lining.moveTo(cx - 6 * scale, cy - 2 * scale);
        lining.lineTo(cx + 6 * scale, cy - 2 * scale);
        lining.lineTo(cx + 10 * scale + waveR, cy + 21 * scale);
        lining.lineTo(cx - 10 * scale + waveL, cy + 21 * scale);
        lining.close();
        canvas.drawPath(lining, paint);

        paint.setColor(Color.argb(250, 36, 24, 48));
        Path robe = new Path();
        robe.moveTo(cx - 5.5f * scale, cy - 6 * scale);
        robe.lineTo(cx + 5.5f * scale, cy - 6 * scale);
        robe.lineTo(cx + 8.5f * scale, cy + 6 * scale);
        robe.lineTo(cx + 7.5f * scale + waveR, cy + 20 * scale);
        robe.lineTo(cx, cy + 17.5f * scale);
        robe.lineTo(cx - 7.5f * scale + waveL, cy + 20 * scale);
        robe.lineTo(cx - 8.5f * scale, cy + 6 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        paint.setColor(Color.argb(240, 96, 22, 36));
        canvas.drawRect(cx - 6.5f * scale, cy + 6 * scale, cx + 6.5f * scale, cy + 7.6f * scale, paint);

        float pulse = 0.6f + 0.4f * (float) Math.sin(now / 420.0);
        paint.setColor(Color.argb((int) (230 * pulse), 220, 36, 48));
        canvas.drawCircle(cx, cy + 2.2f * scale, 1.4f * scale, paint);
    }

    private float hatBrimY(float cy, float scale) {
        return cy - 12.5f * scale;
    }

    private float hatRx(float scale) {
        return 18.5f * scale * (isHatBoss() ? 1.1f : 1f);
    }

    private float hatRy(float scale) {
        return 6.4f * scale * (isHatBoss() ? 1.1f : 1f);
    }

    private float hatCurtainLen(float scale) {
        return (isHatElite() ? 15.5f : 13.8f) * scale;
    }

    /** backLayer=true 画身后垂帘，false 画身前垂帘挡住脸。 */
    private void drawHatVeil(Canvas canvas, Paint paint, float cx, float cy, float scale, boolean backLayer) {
        float brimCy = hatBrimY(cy, scale);
        float rx = hatRx(scale);
        float ry = hatRy(scale);
        float curtainLen = hatCurtainLen(scale);
        if (!backLayer) {
            paint.setColor(Color.argb(185, 24, 10, 18));
            canvas.drawOval(cx - rx * 0.42f, brimCy + 1.2f * scale,
                    cx + rx * 0.42f, brimCy + curtainLen * 0.55f, paint);

            float glow = 0.1f + 0.08f * (float) Math.sin(System.currentTimeMillis() / 320.0);
            if (currentState == State.ATTACKING && isWindingUp) {
                glow = 0.3f + 0.5f * getWindUpProgress();
            } else if (isHatBoss()) {
                glow += 0.12f;
            }
            int eyeA = (int) (255 * glow);
            if (eyeA > 22) {
                paint.setColor(Color.argb(eyeA, 210, 28, 36));
                canvas.drawCircle(cx - 2.3f * scale, brimCy + 6.2f * scale, 1.15f * scale, paint);
                canvas.drawCircle(cx + 2.3f * scale, brimCy + 6.2f * scale, 1.15f * scale, paint);
            }
        }
        drawHatCurtains(canvas, paint, cx, brimCy, rx, ry, curtainLen, scale, backLayer);
    }

    private void drawRoundHat(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float brimCy = hatBrimY(cy, scale);
        float rx = hatRx(scale);
        float ry = hatRy(scale);
        float domeH = 5.2f * scale * (isHatBoss() ? 1.1f : 1f);

        paint.setColor(Color.argb(255, 118, 78, 42));
        canvas.drawOval(cx - rx, brimCy - ry * 0.15f, cx + rx, brimCy + ry * 1.12f, paint);

        paint.setColor(Color.argb(255, 214, 176, 108));
        canvas.drawOval(cx - rx * 0.97f, brimCy - ry * 0.62f, cx + rx * 0.97f, brimCy + ry * 0.78f, paint);

        paint.setColor(Color.argb(255, 232, 198, 128));
        canvas.drawOval(cx - rx * 0.62f, brimCy - ry * 0.55f - domeH * 0.55f,
                cx + rx * 0.62f, brimCy + ry * 0.22f, paint);

        paint.setColor(Color.argb(90, 255, 236, 190));
        canvas.drawOval(cx - rx * 0.28f, brimCy - ry * 0.4f - domeH * 0.72f,
                cx + rx * 0.2f, brimCy - ry * 0.08f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.85f * scale);
        paint.setColor(Color.argb(170, 150, 108, 58));
        canvas.drawOval(cx - rx * 0.82f, brimCy - ry * 0.38f, cx + rx * 0.82f, brimCy + ry * 0.58f, paint);
        canvas.drawOval(cx - rx * 0.5f, brimCy - ry * 0.42f - domeH * 0.28f,
                cx + rx * 0.5f, brimCy + ry * 0.28f, paint);
        canvas.drawLine(cx - rx * 0.9f, brimCy, cx + rx * 0.9f, brimCy, paint);

        paint.setStrokeWidth(1.7f * scale);
        paint.setColor(Color.argb(240, 96, 62, 32));
        canvas.drawOval(cx - rx, brimCy - ry * 0.08f, cx + rx, brimCy + ry * 1.02f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        float beadPulse = 0.75f + 0.25f * (float) Math.sin(now / 380.0);
        paint.setColor(Color.argb((int) (255 * beadPulse), isHatElite() ? 196 : 140, 32, 40));
        canvas.drawCircle(cx, brimCy - domeH * 0.55f, 1.55f * scale, paint);
    }

    /**
     * 帽檐四周垂帘。前帘沿帽檐一排挂下，邻片错相大幅摆动。
     */
    private void drawHatCurtains(Canvas canvas, Paint paint, float cx, float brimCy,
                                 float rx, float ry, float curtainLen, float scale, boolean backLayer) {
        long now = System.currentTimeMillis();
        int strips = backLayer ? 6 : 9;
        float wind = (float) Math.sin(now / 480.0 + floatPhase) * 0.9f * scale;
        for (int i = 0; i < strips; i++) {
            float u = strips == 1 ? 0.5f : (i / (float) (strips - 1));
            float nx = (u - 0.5f) * 2f;
            float ax = cx + nx * rx * (backLayer ? 0.78f : 0.9f);
            float ay = brimCy + ry * 0.22f + (1f - nx * nx) * ry * (backLayer ? -0.35f : 0.55f);
            float phase = (float) (now / 220.0 + i * 0.85f + floatPhase);
            float midSway = wind * 0.4f + (float) Math.sin(phase) * 1.8f * scale;
            float botSway = wind + (float) Math.sin(phase + 0.7f) * 3.0f * scale;
            if (backLayer) {
                midSway *= 0.6f;
                botSway *= 0.6f;
            }
            float halfW = (1.25f + (1f - Math.abs(nx)) * 0.35f) * scale;
            float len = curtainLen * (backLayer ? 0.82f : 1f)
                    * (1f + 0.045f * (float) Math.sin(phase * 0.7f));
            float mx = ax + midSway;
            float my = ay + len * 0.45f;
            float bx = ax + botSway;
            float by = ay + len + (float) Math.sin(phase + 0.4f) * 1.2f * scale;

            boolean warm = i % 2 == 0;
            int cr = warm ? (isHatElite() ? 98 : 76) : (isHatElite() ? 68 : 50);
            int cg = warm ? 28 : 18;
            int cb = warm ? 56 : 42;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(backLayer ? 200 : 240, cr, cg, cb));
            Path strip = new Path();
            strip.moveTo(ax - halfW, ay);
            strip.lineTo(ax + halfW, ay);
            strip.quadTo(mx + halfW, my, bx + halfW * 0.75f, by);
            strip.quadTo(bx, by + halfW * 0.85f, bx - halfW * 0.75f, by);
            strip.quadTo(mx - halfW, my, ax - halfW, ay);
            strip.close();
            canvas.drawPath(strip, paint);

            paint.setColor(Color.argb(120, 220, 150, 160));
            paint.setStrokeWidth(1.1f * scale);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            Path edge = new Path();
            edge.moveTo(ax, ay + scale);
            edge.quadTo(mx, my, bx, by);
            canvas.drawPath(edge, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);

            paint.setColor(Color.argb(245, isHatElite() ? 196 : 148, 40, 48));
            canvas.drawCircle(bx, by + 0.8f * scale, 1.35f * scale, paint);
        }
    }

    private void drawArms(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        long now = System.currentTimeMillis();
        float idle = (float) Math.sin(now / 360.0) * 2.4f * scale;
        float swing = idle;
        if (currentState == State.ATTACKING && isWindingUp) {
            swing = -getWindUpProgress() * 9 * scale;
        }

        paint.setColor(Color.argb(250, 32, 22, 42));
        paint.setStrokeWidth(4.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int s = -1; s <= 1; s += 2) {
            Path sleeve = new Path();
            sleeve.moveTo(cx + s * 6.2f * scale, cy - 2 * scale);
            sleeve.quadTo(cx + s * 11.5f * scale, cy + 3 * scale,
                    cx + s * 13.2f * scale, cy + 9 * scale + swing * 0.35f);
            canvas.drawPath(sleeve, paint);
        }

        paint.setColor(Color.argb(245, 220, 214, 208));
        paint.setStrokeWidth(2.3f * scale);
        for (int s = -1; s <= 1; s += 2) {
            canvas.drawLine(cx + s * 13.2f * scale, cy + 9 * scale + swing * 0.35f,
                    cx + s * 14.2f * scale, cy + 12.2f * scale + swing * 0.35f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        for (int s = -1; s <= 1; s += 2) {
            float hx = cx + s * 14.2f * scale;
            float hy = cy + 12.2f * scale + swing * 0.35f;
            paint.setColor(Color.argb(245, 224, 218, 212));
            canvas.drawCircle(hx, hy, 1.65f * scale, paint);
            paint.setColor(Color.argb(252, 18, 12, 16));
            for (int i = 0; i < 3; i++) {
                float off = (i - 1) * 1.05f * scale;
                Path claw = new Path();
                claw.moveTo(hx + off * 0.3f, hy + 0.5f * scale);
                claw.lineTo(hx + off + s * 1.35f * scale, hy + 4f * scale);
                claw.lineTo(hx + off * 0.3f + s * 0.35f * scale, hy + 1.1f * scale);
                claw.close();
                canvas.drawPath(claw, paint);
            }
        }
    }

    private void drawFangSlashEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 獠牙撕咬：扇形血红爪痕
        float progress = getWindUpProgress();
        float arcRadius = propertyExtra.attackRange * progress;
        float angle = dir > 0 ? 0 : (float) Math.PI;

        paint.setColor(Color.argb((int) (progress * 90), 220, 30, 40));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                (float) Math.toDegrees(angle) - 45, 90, true, paint);

        // 2道爪痕
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f * scale * progress);
        for (int i = 0; i < 2; i++) {
            paint.setColor(Color.argb((int) (progress * (200 - i * 50)), 255, 60 - i * 20, 60));
            float r = arcRadius * (0.5f + i * 0.4f);
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r,
                    (float) Math.toDegrees(angle) - 40, 80, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBloodPoolAura(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 血池共鸣激活时：环绕身体的红色光环
        float pulse = 0.6f + 0.4f * (float) Math.sin(now / 300.0);
        paint.setColor(Color.argb((int) (70 * pulse), 220, 40, 40));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * scale);
        canvas.drawCircle(cx, cy + 4 * scale, 20 * scale, paint);

        paint.setColor(Color.argb((int) (110 * pulse), 255, 80, 80));
        paint.setStrokeWidth(1 * scale);
        canvas.drawCircle(cx, cy + 4 * scale, 24 * scale, paint);

        // 上升的血色粒子
        for (int i = 0; i < 4; i++) {
            float t = ((now / 900f + i * 0.25f) % 1f);
            float angle = (float) (i * Math.PI / 2 + now / 700.0);
            float px = cx + (float) Math.cos(angle) * 18 * scale;
            float py = cy + 12 * scale - t * 20 * scale;
            paint.setColor(Color.argb((int) (200 * (1 - t)), 255, 60, 60));
            canvas.drawCircle(px, py, 1.5f * scale * (1 - t * 0.5f), paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

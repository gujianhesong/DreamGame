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
 * 血池鬼王 - 地狱迷宫第2层BOSS
 * 由万千罪魂鲜血凝聚而成的鬼王，长年沉眠于血池深处，凡闯入血池炼狱者皆被其视为祭品。
 *
 * 视觉: 血红肌肉虬结的巨大身躯，头顶弯曲双角，双肩披挂罪魂颅骨，
 *       双目燃烧血色火焰，胸腹裂口不断滴落鲜血，双手化为巨大利爪，
 *       下半身沉浸血雾中若隐若现。
 *
 * 攻击:
 *  1. 血爪撕裂 - 三连击（COMBO），矩形前方范围
 *  2. 沸血弹 - 中距离投射血色火球（复用通用火球）
 *  3. 血池滋养 - 被动，站在血池地形上时每秒回复 1% 最大HP
 *  4. 血怒召唤 - HP<50% 时召唤亡魂援军（父类机制）
 *
 * 属性: HP极高、攻击极高、防御中、魔抗低、移速中
 */
public class BloodPoolDemonKing extends Enemy {

    // 血池回复计时
    private long lastBloodRegenTime = 0;
    private static final long BLOOD_REGEN_INTERVAL = 1000;

    // 视觉动画相位
    private float breathPhase = 0f;
    private float bloodDripPhase = (float) (Math.random() * Math.PI * 2);
    private float eyeFlarePhase = 0f;

    public BloodPoolDemonKing(float x, float y) {
        super(x, y, 100);
        attackCooldown = 1800;
        setAttackShape(AttackShape.RECT);      // 血爪撕裂 - 矩形
        addAvailableAttackType(AttackType.COMBO);
        addAvailableAttackType(AttackType.CHARGE);

        // COMBO 参数（三连爪击）
        comboHitCount = 3;
        comboHitInterval = 260;

        // CHARGE 参数（血怒冲锋）
        chargeSpeedMultiplier = 4.5f;
        windUpDuration = 550;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 550;
        prop.attackRange = 200;
        prop.rewardExp = 5000;
        prop.rewardMoney = 2500;
        setPropertyExtra(prop);

        // 基础属性：HP极高、攻击高、防御中、速度中、法力低(魔抗低)
        setProperty(2800, 800, 420, 180, 260);

        // 强制 BOSS 等级
        enemyLevel = EnemyLevel.BOSS;
        size = size * 3;
        setProperty(maxHealth * 30, attackDamage * 4, defense * 5, speed * 4, mana * 4);
    }

    @Override
    public boolean usesGenericFireball() {
        return true; // 沸血弹
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;

        breathPhase += 0.0025f * deltaTime;
        bloodDripPhase += 0.003f * deltaTime;
        eyeFlarePhase += 0.006f * deltaTime;

        // 血池滋养：站在血池地形上每秒回复 1% 最大HP
        long now = System.currentTimeMillis();
        if (now - lastBloodRegenTime >= BLOOD_REGEN_INTERVAL) {
            lastBloodRegenTime = now;
            if (isStandingOnBloodPool()) {
                int heal = Math.max(1, (int) (maxHealth * 0.01f));
                health = Math.min(maxHealth, health + heal);
            }
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

    @Override
    protected void performAttack() {
        // 由 GameEngine 处理
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel5(this);
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

        // 呼吸起伏
        float breath = (float) Math.sin(breathPhase) * 1.5f * scale;
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY + breath;
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // 1. 下半身血雾
        drawBloodMist(canvas, paint, cx, cy, scale, now);
        // 2. 躯干
        drawTorso(canvas, paint, cx, cy, scale, now);
        // 3. 肩甲颅骨
        drawSkullShoulders(canvas, paint, cx, cy, scale);
        // 4. 头部与双角
        drawHead(canvas, paint, cx, cy, scale, now);
        // 5. 双臂利爪
        drawClaws(canvas, paint, cx, cy, scale, now, dir);
        // 6. 攻击特效
        if (currentState == State.ATTACKING && isWindingUp) {
            drawClawSlashEffect(canvas, paint, cx, cy, scale, dir);
        }
    }

    private void drawBloodMist(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 下半身翻涌血雾
        for (int i = 0; i < 5; i++) {
            float angle = bloodDripPhase * 0.5f + i * (float) (Math.PI * 2 / 5);
            float mx = cx + (float) Math.cos(angle) * 16 * scale;
            float my = cy + 20 * scale + (float) Math.sin(angle) * 4 * scale;
            float pulse = 0.4f + 0.3f * (float) Math.sin(now / 500.0 + i);
            paint.setColor(Color.argb((int) (110 * pulse), 140, 20, 20));
            canvas.drawCircle(mx, my, 9 * scale, paint);
            paint.setColor(Color.argb((int) (160 * pulse), 190, 40, 40));
            canvas.drawCircle(mx, my, 5 * scale, paint);
        }
    }

    private void drawTorso(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 血红肌肉躯干
        paint.setColor(Color.argb(240, 130, 25, 25));
        Path torso = new Path();
        torso.moveTo(cx - 16 * scale, cy - 10 * scale);
        torso.quadTo(cx - 20 * scale, cy + 4 * scale, cx - 14 * scale, cy + 18 * scale);
        torso.lineTo(cx + 14 * scale, cy + 18 * scale);
        torso.quadTo(cx + 20 * scale, cy + 4 * scale, cx + 16 * scale, cy - 10 * scale);
        torso.quadTo(cx, cy - 14 * scale, cx - 16 * scale, cy - 10 * scale);
        torso.close();
        canvas.drawPath(torso, paint);

        // 肌肉纹理
        paint.setColor(Color.argb(180, 90, 15, 15));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 6 * scale, cy - 8 * scale, cx - 8 * scale, cy + 12 * scale, paint);
        canvas.drawLine(cx + 6 * scale, cy - 8 * scale, cx + 8 * scale, cy + 12 * scale, paint);
        canvas.drawLine(cx, cy - 6 * scale, cx, cy + 14 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 胸腹裂口（滴血）
        paint.setColor(Color.argb(230, 40, 5, 5));
        canvas.drawOval(cx - 5 * scale, cy - 2 * scale,
                cx + 5 * scale, cy + 8 * scale, paint);
        // 裂口内发光
        float glow = 0.6f + 0.4f * (float) Math.sin(now / 400.0);
        paint.setColor(Color.argb((int) (180 * glow), 255, 80, 40));
        canvas.drawOval(cx - 3.5f * scale, cy, cx + 3.5f * scale, cy + 6 * scale, paint);

        // 滴落的血珠
        for (int i = 0; i < 2; i++) {
            float t = ((now / 900f + i * 0.5f) % 1f);
            float dx = cx + (i == 0 ? -3 : 3) * scale;
            float dy = cy + 8 * scale + t * 20 * scale;
            paint.setColor(Color.argb((int) (220 * (1 - t)), 180, 20, 20));
            canvas.drawCircle(dx, dy, 1.5f * scale * (1 - t * 0.5f), paint);
        }
    }

    private void drawSkullShoulders(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        // 左右肩甲各挂一个罪魂颅骨
        for (int s = -1; s <= 1; s += 2) {
            float sx = cx + s * 18 * scale;
            float sy = cy - 10 * scale;
            // 颅骨主体
            paint.setColor(Color.argb(235, 220, 210, 190));
            canvas.drawCircle(sx, sy, 4 * scale, paint);
            // 眼窝
            paint.setColor(Color.argb(240, 30, 20, 20));
            canvas.drawCircle(sx - 1.5f * scale, sy - 0.5f * scale, 1 * scale, paint);
            canvas.drawCircle(sx + 1.5f * scale, sy - 0.5f * scale, 1 * scale, paint);
            // 牙缝
            paint.setStrokeWidth(0.6f * scale);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(sx - 2 * scale, sy + 2 * scale, sx + 2 * scale, sy + 2 * scale, paint);
            for (int i = -1; i <= 1; i++) {
                canvas.drawLine(sx + i * 1.2f * scale, sy + 1.5f * scale,
                        sx + i * 1.2f * scale, sy + 3 * scale, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float headY = cy - 20 * scale;

        // 头部（血红）
        paint.setColor(Color.argb(240, 140, 30, 30));
        canvas.drawOval(cx - 8 * scale, headY - 7 * scale,
                cx + 8 * scale, headY + 7 * scale, paint);

        // 弯曲双角（黑褐）
        paint.setColor(Color.argb(245, 45, 30, 22));
        for (int s = -1; s <= 1; s += 2) {
            Path horn = new Path();
            horn.moveTo(cx + s * 5 * scale, headY - 6 * scale);
            horn.quadTo(cx + s * 12 * scale, headY - 12 * scale,
                    cx + s * 8 * scale, headY - 18 * scale);
            horn.quadTo(cx + s * 10 * scale, headY - 12 * scale,
                    cx + s * 3 * scale, headY - 6 * scale);
            horn.close();
            canvas.drawPath(horn, paint);
        }

        // 燃烧血色双目
        float flare = 0.7f + 0.3f * (float) Math.sin(eyeFlarePhase);
        paint.setColor(Color.argb((int) (255 * flare), 255, 60, 30));
        canvas.drawCircle(cx - 3 * scale, headY - 1 * scale, 1.6f * scale, paint);
        canvas.drawCircle(cx + 3 * scale, headY - 1 * scale, 1.6f * scale, paint);
        // 外焰
        paint.setColor(Color.argb((int) (120 * flare), 255, 100, 40));
        canvas.drawCircle(cx - 3 * scale, headY - 1 * scale, 3.2f * scale, paint);
        canvas.drawCircle(cx + 3 * scale, headY - 1 * scale, 3.2f * scale, paint);

        // 獠牙巨口
        paint.setColor(Color.argb(240, 25, 10, 10));
        canvas.drawOval(cx - 4 * scale, headY + 2 * scale,
                cx + 4 * scale, headY + 6 * scale, paint);
        // 上獠牙
        paint.setColor(Color.argb(245, 240, 235, 220));
        Path fang1 = new Path();
        fang1.moveTo(cx - 3 * scale, headY + 2 * scale);
        fang1.lineTo(cx - 2 * scale, headY + 5 * scale);
        fang1.lineTo(cx - 1 * scale, headY + 2 * scale);
        fang1.close();
        canvas.drawPath(fang1, paint);
        Path fang2 = new Path();
        fang2.moveTo(cx + 1 * scale, headY + 2 * scale);
        fang2.lineTo(cx + 2 * scale, headY + 5 * scale);
        fang2.lineTo(cx + 3 * scale, headY + 2 * scale);
        fang2.close();
        canvas.drawPath(fang2, paint);
    }

    private void drawClaws(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 双臂（血红肌肉）
        paint.setColor(Color.argb(240, 130, 25, 25));
        paint.setStrokeWidth(5 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float swing = 0;
        if (currentState == State.ATTACKING && isWindingUp) {
            swing = -getWindUpProgress() * 12 * scale;
        }

        // 左臂
        Path armL = new Path();
        armL.moveTo(cx - 14 * scale, cy - 6 * scale);
        armL.quadTo(cx - 20 * scale, cy + 2 * scale,
                cx - 22 * scale, cy + 12 * scale + swing * 0.5f);
        canvas.drawPath(armL, paint);
        // 右臂
        Path armR = new Path();
        armR.moveTo(cx + 14 * scale, cy - 6 * scale);
        armR.quadTo(cx + 20 * scale, cy + 2 * scale,
                cx + 22 * scale, cy + 12 * scale + swing * 0.5f);
        canvas.drawPath(armR, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 利爪（黑褐色尖爪 3根/手）
        paint.setColor(Color.argb(250, 30, 20, 18));
        for (int s = -1; s <= 1; s += 2) {
            float handX = cx + s * 22 * scale;
            float handY = cy + 12 * scale + swing * 0.5f;
            for (int i = 0; i < 3; i++) {
                float off = (i - 1) * 2.5f * scale;
                Path claw = new Path();
                claw.moveTo(handX + off * 0.4f, handY);
                claw.lineTo(handX + off + s * 3 * scale, handY + 5 * scale);
                claw.lineTo(handX + off * 0.4f + s * 0.5f * scale, handY + 2 * scale);
                claw.close();
                canvas.drawPath(claw, paint);
            }
            // 爪上血迹
            paint.setColor(Color.argb(200, 180, 20, 20));
            canvas.drawCircle(handX, handY + 3 * scale, 1.2f * scale, paint);
            paint.setColor(Color.argb(250, 30, 20, 18));
        }
    }

    private void drawClawSlashEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 矩形爪痕（前方扇形血光）
        float progress = getWindUpProgress();
        float rectW = propertyExtra.attackRange * 1.1f;
        float rectH = 80 * scale * progress;

        paint.setColor(Color.argb((int) (progress * 80), 200, 30, 30));
        canvas.save();
        canvas.translate(cx, cy);
        if (dir < 0) canvas.scale(-1, 1);
        canvas.drawRect(0, -rectH / 2, rectW * progress, rectH / 2, paint);

        // 3道血爪痕
        paint.setColor(Color.argb((int) (progress * 160), 255, 80, 60));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f * scale * progress);
        for (int i = -1; i <= 1; i++) {
            Path slash = new Path();
            slash.moveTo(0, i * 15 * scale);
            slash.quadTo(rectW * 0.5f * progress, i * 15 * scale + 8 * scale * progress,
                    rectW * progress, i * 15 * scale);
            canvas.drawPath(slash, paint);
        }
        canvas.restore();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

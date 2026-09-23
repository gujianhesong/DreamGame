package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 判官崔钰 - 地狱迷宫第3层BOSS（枉死城之主）
 * 阴司判官之首，掌生死簿、执朱砂笔，专审枉死之魂。凡闯入枉死城的阳人，
 * 皆被其视为"逃避轮回的罪魂"，须以朱笔勾决、押入轮回。
 *
 * 视觉: 高大清瘦，青色官袍金线云纹，红色内衬，腰系玉带；
 *       头戴方巾冠，两翼长翅向后飘扬；面容苍白，长须及胸，双目幽蓝无瞳；
 *       右手执巨大朱砂笔（笔尖常燃朱红光），左手托生死簿（浮空自翻页）。
 *
 * 攻击:
 *  1. 朱笔勾决 - 扇形挥笔（ARC近战），笔锋带朱砂拖尾
 *  2. 生死符咒 - 中距离投掷血色符纸（复用通用火球）
 *  3. 判官召魂 - HP<50% 时召唤亡魂援军（父类机制）
 *
 * 属性: HP高、攻击高、防御中偏高、魔抗高、移速偏慢
 */
public class JudgeCuiYu extends Enemy {

    // 视觉动画相位
    private float robePhase = 0f;
    private float ledgerPhase = 0f;
    private float penGlowPhase = 0f;

    public JudgeCuiYu(float x, float y) {
        super(x, y, 92);
        attackCooldown = 2200;
        setAttackShape(AttackShape.ARC);      // 朱笔勾决 - 扇形
        addAvailableAttackType(AttackType.MELEE);
        windUpDuration = 650;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 540;
        prop.attackRange = 280;
        prop.rewardExp = 7500;
        prop.rewardMoney = 3800;
        setPropertyExtra(prop);

        // 基础属性：HP高、攻击高、防御中偏高、速度慢、法力高(魔抗高)
        setProperty(3200, 720, 460, 130, 620);

        // 强制 BOSS
        enemyLevel = EnemyLevel.BOSS;
        size = size * 3;
        setProperty(maxHealth * 30, attackDamage * 4, defense * 5, speed * 4, mana * 4);
    }

    @Override
    public boolean usesGenericFireball() {
        return true; // 生死符咒
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;
        robePhase += 0.002f * deltaTime;
        ledgerPhase += 0.003f * deltaTime;
        penGlowPhase += 0.005f * deltaTime;
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

        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY;
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // 1. 官袍
        drawRobe(canvas, paint, cx, cy, scale, now);
        // 2. 玉带
        drawBelt(canvas, paint, cx, cy, scale);
        // 3. 头部与冠冕
        drawHead(canvas, paint, cx, cy, scale, now);
        // 4. 生死簿（左手托浮空）
        drawLedger(canvas, paint, cx, cy, scale, now, dir);
        // 5. 朱砂笔（右手执）
        drawPen(canvas, paint, cx, cy, scale, now, dir);
        // 6. 攻击特效
        if (currentState == State.ATTACKING && isWindingUp) {
            drawInkSlashEffect(canvas, paint, cx, cy, scale, dir);
        }
    }

    private void drawRobe(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 青色官袍（长及脚踝，宽大袖口）
        float wave = (float) Math.sin(robePhase) * 1.5f * scale;
        paint.setColor(Color.argb(240, 40, 60, 100));
        Path robe = new Path();
        robe.moveTo(cx - 12 * scale, cy - 10 * scale);
        robe.lineTo(cx + 12 * scale, cy - 10 * scale);
        robe.lineTo(cx + 18 * scale, cy + 8 * scale);
        robe.lineTo(cx + 16 * scale + wave, cy + 22 * scale);
        robe.lineTo(cx - 16 * scale - wave, cy + 22 * scale);
        robe.lineTo(cx - 18 * scale, cy + 8 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        // 红色内衬（前襟 V 领）
        paint.setColor(Color.argb(235, 150, 30, 30));
        Path inner = new Path();
        inner.moveTo(cx - 4 * scale, cy - 10 * scale);
        inner.lineTo(cx + 4 * scale, cy - 10 * scale);
        inner.lineTo(cx + 6 * scale, cy + 14 * scale);
        inner.lineTo(cx, cy + 18 * scale);
        inner.lineTo(cx - 6 * scale, cy + 14 * scale);
        inner.close();
        canvas.drawPath(inner, paint);

        // 金线云纹（袖口与下摆）
        paint.setColor(Color.argb(200, 220, 180, 70));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        // 下摆金边
        canvas.drawLine(cx - 16 * scale - wave, cy + 21 * scale,
                cx + 16 * scale + wave, cy + 21 * scale, paint);
        // 云纹（3个圆弧）
        for (int i = -1; i <= 1; i++) {
            float ox = cx + i * 8 * scale;
            canvas.drawArc(ox - 3 * scale, cy + 4 * scale,
                    ox + 3 * scale, cy + 10 * scale, 0, 180, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBelt(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        // 玉带（金色带扣）
        paint.setColor(Color.argb(240, 30, 30, 45));
        canvas.drawRect(cx - 13 * scale, cy + 2 * scale, cx + 13 * scale, cy + 6 * scale, paint);
        // 带扣
        paint.setColor(Color.argb(245, 240, 210, 100));
        canvas.drawRect(cx - 3 * scale, cy + 1.5f * scale, cx + 3 * scale, cy + 6.5f * scale, paint);
        paint.setColor(Color.argb(240, 180, 140, 40));
        canvas.drawRect(cx - 2 * scale, cy + 2.5f * scale, cx + 2 * scale, cy + 5.5f * scale, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float headY = cy - 18 * scale;

        // 面部（苍白）
        paint.setColor(Color.argb(240, 210, 205, 195));
        canvas.drawOval(cx - 6 * scale, headY - 6 * scale,
                cx + 6 * scale, headY + 7 * scale, paint);

        // 长须（三缕，胸前）
        paint.setColor(Color.argb(230, 220, 220, 225));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = -1; i <= 1; i++) {
            Path beard = new Path();
            beard.moveTo(cx + i * 2 * scale, headY + 5 * scale);
            beard.quadTo(cx + i * 3 * scale, headY + 14 * scale,
                    cx + i * 4 * scale, headY + 22 * scale);
            canvas.drawPath(beard, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 幽蓝无瞳双目
        float glow = 0.7f + 0.3f * (float) Math.sin(now / 500.0);
        paint.setColor(Color.argb((int) (230 * glow), 100, 180, 255));
        canvas.drawOval(cx - 4 * scale, headY - 2 * scale,
                cx - 1 * scale, headY + 1 * scale, paint);
        canvas.drawOval(cx + 1 * scale, headY - 2 * scale,
                cx + 4 * scale, headY + 1 * scale, paint);

        // 方巾冠（黑色高冠 + 金色饰边）
        paint.setColor(Color.argb(245, 20, 20, 30));
        Path crown = new Path();
        crown.moveTo(cx - 7 * scale, headY - 6 * scale);
        crown.lineTo(cx + 7 * scale, headY - 6 * scale);
        crown.lineTo(cx + 6 * scale, headY - 14 * scale);
        crown.lineTo(cx - 6 * scale, headY - 14 * scale);
        crown.close();
        canvas.drawPath(crown, paint);
        // 金边
        paint.setColor(Color.argb(230, 220, 180, 70));
        canvas.drawRect(cx - 7 * scale, headY - 7 * scale, cx + 7 * scale, headY - 5.5f * scale, paint);
        // 冠顶金饰
        paint.setColor(Color.argb(240, 240, 210, 100));
        canvas.drawCircle(cx, headY - 14 * scale, 1.5f * scale, paint);

        // 两翼长翅（向后飘扬）
        paint.setColor(Color.argb(235, 25, 25, 38));
        for (int s = -1; s <= 1; s += 2) {
            Path wing = new Path();
            wing.moveTo(cx + s * 6 * scale, headY - 11 * scale);
            wing.quadTo(cx + s * 18 * scale, headY - 14 * scale,
                    cx + s * 26 * scale, headY - 8 * scale);
            wing.quadTo(cx + s * 18 * scale, headY - 10 * scale,
                    cx + s * 6 * scale, headY - 8 * scale);
            wing.close();
            canvas.drawPath(wing, paint);
        }
        // 翅上金线
        paint.setColor(Color.argb(200, 200, 160, 60));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int s = -1; s <= 1; s += 2) {
            canvas.drawLine(cx + s * 8 * scale, headY - 10 * scale,
                    cx + s * 24 * scale, headY - 9 * scale, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawLedger(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 生死簿（浮空于左手侧）
        float lx = cx - dir * 20 * scale;
        float ly = cy - 4 * scale + (float) Math.sin(ledgerPhase) * 2 * scale;

        // 簿下方幽光
        float glow = 0.5f + 0.3f * (float) Math.sin(now / 600.0);
        paint.setColor(Color.argb((int) (80 * glow), 180, 220, 255));
        canvas.drawCircle(lx, ly, 8 * scale, paint);

        // 簿主体（暗黄纸页 + 黑褐封皮）
        paint.setColor(Color.argb(240, 60, 40, 25));
        canvas.drawRect(lx - 6 * scale, ly - 4 * scale, lx + 6 * scale, ly + 4 * scale, paint);
        paint.setColor(Color.argb(235, 220, 200, 160));
        canvas.drawRect(lx - 5 * scale, ly - 3 * scale, lx + 5 * scale, ly + 3 * scale, paint);

        // 自翻页（3条弧线）
        paint.setColor(Color.argb(200, 180, 160, 130));
        paint.setStrokeWidth(0.7f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 3; i++) {
            float t = ((now / 1200f + i * 0.33f) % 1f);
            float arc = (float) Math.sin(t * Math.PI);
            canvas.drawArc(lx - 5 * scale, ly - 3 * scale - arc * 3 * scale,
                    lx + 5 * scale, ly + 3 * scale - arc * 3 * scale, 0, 180, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 簿上朱砂字（几个小红点）
        paint.setColor(Color.argb(220, 200, 40, 40));
        canvas.drawCircle(lx - 2 * scale, ly - 1 * scale, 0.7f * scale, paint);
        canvas.drawCircle(lx + 1 * scale, ly, 0.7f * scale, paint);
        canvas.drawCircle(lx + 3 * scale, ly + 1 * scale, 0.7f * scale, paint);
    }

    private void drawPen(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 朱砂笔（右手高举，笔尖向下）
        float swing = 0;
        if (currentState == State.ATTACKING && isWindingUp) {
            swing = -getWindUpProgress() * 25 * scale;
        }
        float px = cx + dir * 16 * scale;
        float py = cy - 6 * scale + swing * 0.4f;

        // 笔杆（深褐竹木）
        paint.setColor(Color.argb(245, 70, 45, 25));
        paint.setStrokeWidth(2.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(px, py - 12 * scale, px + dir * 3 * scale, py + 14 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 笔尖（朱红毛锥）
        paint.setColor(Color.argb(245, 200, 40, 40));
        Path tip = new Path();
        tip.moveTo(px + dir * 2 * scale, py + 12 * scale);
        tip.lineTo(px + dir * 5 * scale, py + 20 * scale);
        tip.lineTo(px + dir * 1 * scale, py + 18 * scale);
        tip.close();
        canvas.drawPath(tip, paint);

        // 笔尖朱红光晕
        float pulse = 0.6f + 0.4f * (float) Math.sin(penGlowPhase);
        paint.setColor(Color.argb((int) (140 * pulse), 255, 80, 60));
        canvas.drawCircle(px + dir * 3 * scale, py + 17 * scale, 4 * scale, paint);
        paint.setColor(Color.argb((int) (200 * pulse), 255, 140, 100));
        canvas.drawCircle(px + dir * 3 * scale, py + 17 * scale, 2 * scale, paint);

        // 笔杆金环
        paint.setColor(Color.argb(240, 220, 180, 70));
        canvas.drawRect(px - 1.5f * scale, py - 4 * scale, px + dir * 3 * scale + 1.5f * scale, py - 2 * scale, paint);
    }

    private void drawInkSlashEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 扇形朱砂笔锋
        float progress = getWindUpProgress();
        float arcRadius = propertyExtra.attackRange * progress;
        float angle = dir > 0 ? 0 : (float) Math.PI;

        // 朱砂扇形
        paint.setColor(Color.argb((int) (progress * 90), 220, 50, 40));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                (float) Math.toDegrees(angle) - 55, 110, true, paint);

        // 墨痕（3道弧线，朱红→暗红渐变）
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i <= 3; i++) {
            float r = arcRadius * (i / 3f);
            int alpha = (int) (progress * (180 - i * 40));
            paint.setColor(Color.argb(alpha, 255 - i * 30, 60 - i * 10, 40));
            paint.setStrokeWidth((3.5f - i * 0.6f) * scale * progress);
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r,
                    (float) Math.toDegrees(angle) - 50, 100, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 朱砂飞溅（几个小红点）
        paint.setColor(Color.argb((int) (progress * 220), 240, 60, 50));
        for (int i = 0; i < 4; i++) {
            float t = i / 3f;
            float sx = cx + dir * arcRadius * (0.4f + t * 0.6f);
            float sy = cy + (float) Math.sin(t * Math.PI * 2) * arcRadius * 0.3f;
            canvas.drawCircle(sx, sy, 1.5f * scale * progress, paint);
        }
    }
}

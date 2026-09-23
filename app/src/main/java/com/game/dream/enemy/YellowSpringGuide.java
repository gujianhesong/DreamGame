package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 黄泉引路人 - 地狱迷宫第1层BOSS
 * 生前为黄泉河上的摆渡人，死后仍执着引渡亡魂，凡闯入黄泉迷径的阳人皆被视为"该渡之魂"。
 *
 * 视觉: 身披蓑衣、头戴斗笠的高大枯瘦身影，一手撑竹篙，一手提引魂灯，
 *       面容隐于斗笠阴影下，只见两点幽黄灯火般的眼眸，脚下常年缭绕黄泉雾气。
 *
 * 攻击:
 *  1. 黄泉篙击 - 扇形近战（ARC），前摇长但伤害高
 *  2. 引魂灯 - 中距离投掷幽黄火球（复用通用火球），冷却短
 *  3. 摆渡召唤 - HP<50% 时召唤孤魂/野鬼援军（父类机制）
 *
 * 属性: HP极高、防御高、魔抗中、移速慢、攻击间隔长
 */
public class YellowSpringGuide extends Enemy {

    // 引魂灯光晕动画相位
    private float lanternPhase = 0f;
    // 脚下雾气动画相位
    private float mistPhase = (float) (Math.random() * Math.PI * 2);

    public YellowSpringGuide(float x, float y) {
        super(x, y, 90);
        attackCooldown = 2400;
        setAttackShape(AttackShape.ARC);      // 黄泉篙击 - 扇形
        addAvailableAttackType(AttackType.MELEE);
        windUpDuration = 700;                 // 篙击前摇长（大幅挥动）

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 520;
        prop.attackRange = 260;
        prop.rewardExp = 3000;
        prop.rewardMoney = 1500;
        setPropertyExtra(prop);

        // 基础属性（BOSS 加成前）：HP高、攻击高、防御高、速度慢、法力中
        setProperty(2200, 550, 380, 110, 500);

        // 强制 BOSS 等级（跳过随机）
        enemyLevel = EnemyLevel.BOSS;
        size = size * 3;
        setProperty(maxHealth * 30, attackDamage * 4, defense * 5, speed * 4, mana * 4);
    }

    @Override
    public boolean usesGenericFireball() {
        return true; // 使用通用火球表现"引魂灯"投掷
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;
        lanternPhase += 0.004f * deltaTime;
        mistPhase += 0.002f * deltaTime;
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

        // 1. 脚下黄泉雾
        drawMist(canvas, paint, cx, cy, scale, now);
        // 2. 蓑衣身体
        drawBody(canvas, paint, cx, cy, scale, now);
        // 3. 斗笠头部
        drawHead(canvas, paint, cx, cy, scale, now);
        // 4. 竹篙（攻击时挥动）
        drawPole(canvas, paint, cx, cy, scale, now, dir);
        // 5. 引魂灯
        drawLantern(canvas, paint, cx, cy, scale, now, dir);
        // 6. 攻击特效：扇形篙影
        if (currentState == State.ATTACKING && isWindingUp) {
            drawPoleSwingEffect(canvas, paint, cx, cy, scale, dir);
        }
    }

    private void drawMist(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 脚下黄泉雾气（3团缓慢漂移的暗黄光斑）
        for (int i = 0; i < 3; i++) {
            float angle = mistPhase + i * (float) (Math.PI * 2 / 3);
            float mx = cx + (float) Math.cos(angle) * 14 * scale;
            float my = cy + 18 * scale + (float) Math.sin(angle) * 4 * scale;
            float pulse = 0.4f + 0.3f * (float) Math.sin(now / 700.0 + i);
            paint.setColor(Color.argb((int) (60 * pulse), 200, 180, 90));
            canvas.drawCircle(mx, my, 8 * scale, paint);
            paint.setColor(Color.argb((int) (100 * pulse), 230, 210, 130));
            canvas.drawCircle(mx, my, 4 * scale, paint);
        }
    }

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 蓑衣（暗黄褐色，层叠下垂）
        paint.setColor(Color.argb(235, 110, 88, 45));
        Path robe = new Path();
        robe.moveTo(cx - 12 * scale, cy - 8 * scale);
        robe.lineTo(cx + 12 * scale, cy - 8 * scale);
        robe.lineTo(cx + 15 * scale, cy + 6 * scale);
        // 层叠下摆（3层波浪）
        for (int i = 0; i < 3; i++) {
            float wave = (float) Math.sin(now / 500.0 + i) * 1.5f * scale;
            robe.lineTo(cx + (12 - i * 6) * scale, cy + (14 + i * 4) * scale + wave);
            robe.lineTo(cx + (6 - i * 6) * scale, cy + (10 + i * 4) * scale + wave);
        }
        robe.lineTo(cx - 15 * scale, cy + 6 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        // 蓑衣纹理（纵向草茎）
        paint.setColor(Color.argb(140, 80, 60, 30));
        paint.setStrokeWidth(1f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = -3; i <= 3; i++) {
            float sx = cx + i * 3 * scale;
            canvas.drawLine(sx, cy - 6 * scale, sx + i * 0.6f * scale, cy + 14 * scale, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 腰带（深褐麻绳）
        paint.setColor(Color.argb(230, 70, 50, 25));
        canvas.drawRect(cx - 12 * scale, cy - 2 * scale, cx + 12 * scale, cy + 1 * scale, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float headY = cy - 16 * scale;

        // 阴影中的面孔（暗色椭圆）
        paint.setColor(Color.argb(230, 40, 32, 25));
        canvas.drawOval(cx - 6 * scale, headY - 5 * scale,
                cx + 6 * scale, headY + 6 * scale, paint);

        // 幽黄眼眸（两点灯火）
        float glow = 0.7f + 0.3f * (float) Math.sin(now / 400.0);
        paint.setColor(Color.argb((int) (230 * glow), 255, 210, 90));
        canvas.drawCircle(cx - 2.5f * scale, headY, 1.5f * scale, paint);
        canvas.drawCircle(cx + 2.5f * scale, headY, 1.5f * scale, paint);
        // 外晕
        paint.setColor(Color.argb((int) (100 * glow), 255, 220, 120));
        canvas.drawCircle(cx - 2.5f * scale, headY, 3 * scale, paint);
        canvas.drawCircle(cx + 2.5f * scale, headY, 3 * scale, paint);

        // 斗笠（大圆盘 + 顶尖）
        paint.setColor(Color.argb(240, 140, 110, 55));
        Path hat = new Path();
        hat.moveTo(cx - 14 * scale, headY - 3 * scale);
        hat.lineTo(cx + 14 * scale, headY - 3 * scale);
        hat.lineTo(cx + 9 * scale, headY - 7 * scale);
        hat.lineTo(cx, headY - 13 * scale);
        hat.lineTo(cx - 9 * scale, headY - 7 * scale);
        hat.close();
        canvas.drawPath(hat, paint);

        // 斗笠纹理（放射竹篾）
        paint.setColor(Color.argb(180, 90, 68, 32));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = -3; i <= 3; i++) {
            canvas.drawLine(cx + i * 3 * scale, headY - 3 * scale,
                    cx + i * 1.5f * scale, headY - 11 * scale, paint);
        }
        // 笠沿阴影
        paint.setColor(Color.argb(200, 60, 45, 22));
        paint.setStrokeWidth(1.5f * scale);
        canvas.drawLine(cx - 14 * scale, headY - 3 * scale, cx + 14 * scale, headY - 3 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawPole(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 竹篙（长杆斜握于身侧）
        float swing = 0f;
        if (currentState == State.ATTACKING && isWindingUp) {
            // 攻击前摇时竹篙抬起
            swing = -getWindUpProgress() * 20 * scale;
        } else {
            swing = (float) Math.sin(now / 900.0) * 1.5f * scale;
        }

        paint.setColor(Color.argb(240, 130, 100, 55));
        paint.setStrokeWidth(2.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        Path pole = new Path();
        pole.moveTo(cx + dir * 10 * scale, cy + 18 * scale);
        pole.lineTo(cx + dir * 14 * scale + swing, cy - 22 * scale);
        canvas.drawPath(pole, paint);

        // 竹节
        paint.setColor(Color.argb(200, 90, 70, 35));
        paint.setStrokeWidth(1.2f * scale);
        for (int i = 0; i < 4; i++) {
            float t = 0.2f + i * 0.2f;
            float nx = cx + dir * (10 + 4 * t) * scale + swing * t;
            float ny = cy + (18 - 40 * t) * scale;
            canvas.drawLine(nx - 1.5f * scale, ny, nx + 1.5f * scale, ny, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawLantern(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 引魂灯（悬挂于另一侧手臂下）
        float bob = (float) Math.sin(lanternPhase) * 2 * scale;
        float lx = cx - dir * 14 * scale;
        float ly = cy - 2 * scale + bob;

        // 提手绳
        paint.setColor(Color.argb(220, 90, 70, 40));
        paint.setStrokeWidth(1f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - dir * 10 * scale, cy - 6 * scale, lx, ly - 5 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 灯笼外晕（脉冲黄光）
        float pulse = 0.6f + 0.4f * (float) Math.sin(now / 350.0);
        paint.setColor(Color.argb((int) (90 * pulse), 255, 200, 90));
        canvas.drawCircle(lx, ly, 8 * scale, paint);
        paint.setColor(Color.argb((int) (140 * pulse), 255, 220, 130));
        canvas.drawCircle(lx, ly, 5 * scale, paint);

        // 灯笼本体（暖橙纸面 + 骨架）
        paint.setColor(Color.argb(235, 240, 180, 90));
        canvas.drawOval(lx - 4 * scale, ly - 5 * scale,
                lx + 4 * scale, ly + 5 * scale, paint);
        paint.setColor(Color.argb(200, 130, 70, 30));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(lx - 4 * scale, ly - 2 * scale, lx + 4 * scale, ly - 2 * scale, paint);
        canvas.drawLine(lx - 4 * scale, ly + 2 * scale, lx + 4 * scale, ly + 2 * scale, paint);
        canvas.drawLine(lx, ly - 5 * scale, lx, ly + 5 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 顶盖 / 底托
        paint.setColor(Color.argb(240, 80, 55, 25));
        canvas.drawRect(lx - 3 * scale, ly - 6 * scale, lx + 3 * scale, ly - 5 * scale, paint);
        canvas.drawRect(lx - 3 * scale, ly + 5 * scale, lx + 3 * scale, ly + 6 * scale, paint);

        // 灯芯（一点幽黄火焰）
        paint.setColor(Color.argb(255, 255, 240, 160));
        canvas.drawCircle(lx, ly, 1.5f * scale, paint);
    }

    private void drawPoleSwingEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 扇形篙影（土黄弧光）
        float progress = getWindUpProgress();
        float arcRadius = propertyExtra.attackRange * progress * 0.9f;
        float angle = dir > 0 ? 0 : (float) Math.PI;

        paint.setColor(Color.argb((int) (progress * 70), 220, 190, 100));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                (float) Math.toDegrees(angle) - 50, 100, true, paint);

        paint.setColor(Color.argb((int) (progress * 130), 255, 230, 150));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * scale * progress);
        for (int i = 1; i <= 3; i++) {
            float r = arcRadius * (i / 3f);
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r,
                    (float) Math.toDegrees(angle) - 45, 90, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

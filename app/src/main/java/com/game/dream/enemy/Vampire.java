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
 * 视觉: 高瘦人形，苍白俊美面孔，血红双眸，獠牙外露；
 *       深红丝绒立领披风（内衬黑色），胸前佩血色宝石胸针，
 *       白色手套露出细长黑爪，脚下悬浮不接地，移动拖出血雾尾迹。
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
        setProperty(1700, 480, 300, 240, 200);

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

        capePhase += 0.0025f * deltaTime;
        floatPhase += 0.003f * deltaTime;
        eyeGlowPhase += 0.004f * deltaTime;

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

        // 悬浮上下起伏（不接地）
        float floatY = (float) Math.sin(floatPhase) * 3 * scale;
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY + floatY - 6 * scale; // 上浮 6px 表示悬浮
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // 1. 血雾尾迹（脚下）
        drawBloodMist(canvas, paint, cx, cy, scale, now);
        // 2. 披风
        drawCape(canvas, paint, cx, cy, scale, now, dir);
        // 3. 身体（贵族礼服）
        drawBody(canvas, paint, cx, cy, scale, now);
        // 4. 头部（苍白面孔 + 血红双眸 + 獠牙）
        drawHead(canvas, paint, cx, cy, scale, now);
        // 5. 手臂（白手套 + 黑爪）
        drawArms(canvas, paint, cx, cy, scale, now, dir);
        // 6. 攻击特效：獠牙撕咬
        if (currentState == State.ATTACKING && isWindingUp) {
            drawFangSlashEffect(canvas, paint, cx, cy, scale, dir);
        }
        // 7. 血池共鸣激活视觉（红色光环）
        if (bloodPoolBuffActive) {
            drawBloodPoolAura(canvas, paint, cx, cy, scale, now);
        }
    }

    private void drawBloodMist(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 脚下血雾尾迹（3团缓慢漂移的暗红雾气）
        for (int i = 0; i < 3; i++) {
            float angle = capePhase * 0.6f + i * (float) (Math.PI * 2 / 3);
            float mx = cx + (float) Math.cos(angle) * 12 * scale;
            float my = cy + 22 * scale + (float) Math.sin(angle) * 3 * scale;
            float pulse = 0.4f + 0.3f * (float) Math.sin(now / 600.0 + i);
            paint.setColor(Color.argb((int) (90 * pulse), 160, 25, 25));
            canvas.drawCircle(mx, my, 7 * scale, paint);
            paint.setColor(Color.argb((int) (130 * pulse), 210, 45, 45));
            canvas.drawCircle(mx, my, 3.5f * scale, paint);
        }
    }

    private void drawCape(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 深红丝绒披风（外层深红 + 内衬黑色）
        float wave = (float) Math.sin(capePhase) * 2 * scale;

        // 黑色内衬（先画，作为底层）
        paint.setColor(Color.argb(240, 20, 15, 20));
        Path innerCape = new Path();
        innerCape.moveTo(cx - 10 * scale, cy - 8 * scale);
        innerCape.lineTo(cx + 10 * scale, cy - 8 * scale);
        innerCape.lineTo(cx + 14 * scale + wave, cy + 20 * scale);
        innerCape.lineTo(cx - 14 * scale - wave, cy + 20 * scale);
        innerCape.close();
        canvas.drawPath(innerCape, paint);

        // 深红外层（左右两片，中间开口露出黑色内衬）
        paint.setColor(Color.argb(240, 130, 20, 30));
        Path leftCape = new Path();
        leftCape.moveTo(cx - 10 * scale, cy - 8 * scale);
        leftCape.lineTo(cx - 3 * scale, cy - 6 * scale);
        leftCape.lineTo(cx - 6 * scale - wave, cy + 22 * scale);
        leftCape.lineTo(cx - 16 * scale - wave, cy + 18 * scale);
        leftCape.close();
        canvas.drawPath(leftCape, paint);

        Path rightCape = new Path();
        rightCape.moveTo(cx + 10 * scale, cy - 8 * scale);
        rightCape.lineTo(cx + 3 * scale, cy - 6 * scale);
        rightCape.lineTo(cx + 6 * scale + wave, cy + 22 * scale);
        rightCape.lineTo(cx + 16 * scale + wave, cy + 18 * scale);
        rightCape.close();
        canvas.drawPath(rightCape, paint);

        // 立领（高耸的黑色尖领）
        paint.setColor(Color.argb(245, 25, 18, 22));
        Path collar = new Path();
        collar.moveTo(cx - 8 * scale, cy - 8 * scale);
        collar.lineTo(cx - 12 * scale, cy - 22 * scale);
        collar.lineTo(cx - 5 * scale, cy - 12 * scale);
        collar.lineTo(cx + 5 * scale, cy - 12 * scale);
        collar.lineTo(cx + 12 * scale, cy - 22 * scale);
        collar.lineTo(cx + 8 * scale, cy - 8 * scale);
        collar.close();
        canvas.drawPath(collar, paint);

        // 披风金色滚边
        paint.setColor(Color.argb(200, 200, 160, 60));
        paint.setStrokeWidth(0.9f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 10 * scale, cy - 8 * scale, cx - 16 * scale - wave, cy + 18 * scale, paint);
        canvas.drawLine(cx + 10 * scale, cy - 8 * scale, cx + 16 * scale + wave, cy + 18 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 黑色贵族礼服（燕尾服风格）
        paint.setColor(Color.argb(245, 30, 25, 35));
        Path body = new Path();
        body.moveTo(cx - 8 * scale, cy - 8 * scale);
        body.lineTo(cx + 8 * scale, cy - 8 * scale);
        body.lineTo(cx + 10 * scale, cy + 6 * scale);
        body.lineTo(cx + 7 * scale, cy + 18 * scale);
        body.lineTo(cx - 7 * scale, cy + 18 * scale);
        body.lineTo(cx - 10 * scale, cy + 6 * scale);
        body.close();
        canvas.drawPath(body, paint);

        // 白色衬衫 V 领
        paint.setColor(Color.argb(240, 230, 225, 220));
        Path shirt = new Path();
        shirt.moveTo(cx - 3 * scale, cy - 8 * scale);
        shirt.lineTo(cx + 3 * scale, cy - 8 * scale);
        shirt.lineTo(cx + 2 * scale, cy + 8 * scale);
        shirt.lineTo(cx, cy + 10 * scale);
        shirt.lineTo(cx - 2 * scale, cy + 8 * scale);
        shirt.close();
        canvas.drawPath(shirt, paint);

        // 血色宝石胸针（胸前一点红光）
        float pulse = 0.7f + 0.3f * (float) Math.sin(now / 500.0);
        paint.setColor(Color.argb((int) (255 * pulse), 220, 30, 40));
        canvas.drawCircle(cx, cy - 4 * scale, 1.6f * scale, paint);
        paint.setColor(Color.argb((int) (120 * pulse), 255, 80, 90));
        canvas.drawCircle(cx, cy - 4 * scale, 3 * scale, paint);

        // 腰带（暗红丝带）
        paint.setColor(Color.argb(240, 120, 20, 30));
        canvas.drawRect(cx - 8 * scale, cy + 4 * scale, cx + 8 * scale, cy + 6 * scale, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float headY = cy - 16 * scale;

        // 苍白面孔
        paint.setColor(Color.argb(245, 225, 215, 210));
        canvas.drawOval(cx - 6 * scale, headY - 7 * scale,
                cx + 6 * scale, headY + 7 * scale, paint);

        // 黑色短发（向后梳）
        paint.setColor(Color.argb(245, 20, 15, 20));
        Path hair = new Path();
        hair.moveTo(cx - 6 * scale, headY - 3 * scale);
        hair.quadTo(cx - 7 * scale, headY - 9 * scale, cx, headY - 8 * scale);
        hair.quadTo(cx + 7 * scale, headY - 9 * scale, cx + 6 * scale, headY - 3 * scale);
        hair.quadTo(cx + 5 * scale, headY - 6 * scale, cx, headY - 5 * scale);
        hair.quadTo(cx - 5 * scale, headY - 6 * scale, cx - 6 * scale, headY - 3 * scale);
        hair.close();
        canvas.drawPath(hair, paint);

        // 血红双眸（发光）
        float glow = 0.75f + 0.25f * (float) Math.sin(eyeGlowPhase);
        paint.setColor(Color.argb((int) (255 * glow), 220, 25, 30));
        canvas.drawCircle(cx - 2.5f * scale, headY - 1 * scale, 1.3f * scale, paint);
        canvas.drawCircle(cx + 2.5f * scale, headY - 1 * scale, 1.3f * scale, paint);
        // 外晕
        paint.setColor(Color.argb((int) (110 * glow), 255, 60, 60));
        canvas.drawCircle(cx - 2.5f * scale, headY - 1 * scale, 2.8f * scale, paint);
        canvas.drawCircle(cx + 2.5f * scale, headY - 1 * scale, 2.8f * scale, paint);

        // 嘴（微笑弧线）+ 两颗獠牙
        paint.setColor(Color.argb(220, 80, 30, 30));
        paint.setStrokeWidth(0.9f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(cx - 2.5f * scale, headY + 2 * scale,
                cx + 2.5f * scale, headY + 5 * scale, 0, 180, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 獠牙（两颗白色尖牙从唇下露出）
        paint.setColor(Color.argb(250, 250, 250, 245));
        Path fang1 = new Path();
        fang1.moveTo(cx - 2 * scale, headY + 3.5f * scale);
        fang1.lineTo(cx - 1.5f * scale, headY + 6.5f * scale);
        fang1.lineTo(cx - 1 * scale, headY + 3.5f * scale);
        fang1.close();
        canvas.drawPath(fang1, paint);
        Path fang2 = new Path();
        fang2.moveTo(cx + 1 * scale, headY + 3.5f * scale);
        fang2.lineTo(cx + 1.5f * scale, headY + 6.5f * scale);
        fang2.lineTo(cx + 2 * scale, headY + 3.5f * scale);
        fang2.close();
        canvas.drawPath(fang2, paint);

        // 獠牙血迹（攻击后）
        if (currentState == State.ATTACKING) {
            paint.setColor(Color.argb(200, 190, 20, 25));
            canvas.drawCircle(cx - 1.5f * scale, headY + 6 * scale, 0.8f * scale, paint);
            canvas.drawCircle(cx + 1.5f * scale, headY + 6 * scale, 0.8f * scale, paint);
        }
    }

    private void drawArms(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 双臂（黑色礼服袖 + 白色手套 + 黑爪）
        float swing = 0;
        if (currentState == State.ATTACKING && isWindingUp) {
            swing = -getWindUpProgress() * 10 * scale;
        }

        // 袖子
        paint.setColor(Color.argb(245, 30, 25, 35));
        paint.setStrokeWidth(4 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int s = -1; s <= 1; s += 2) {
            Path arm = new Path();
            arm.moveTo(cx + s * 8 * scale, cy - 5 * scale);
            arm.quadTo(cx + s * 13 * scale, cy + 2 * scale,
                    cx + s * 14 * scale, cy + 10 * scale + swing * 0.3f);
            canvas.drawPath(arm, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 白手套 + 黑爪尖
        for (int s = -1; s <= 1; s += 2) {
            float hx = cx + s * 14 * scale;
            float hy = cy + 10 * scale + swing * 0.3f;
            paint.setColor(Color.argb(245, 240, 235, 230));
            canvas.drawCircle(hx, hy, 2 * scale, paint);
            // 3根黑爪
            paint.setColor(Color.argb(250, 15, 10, 15));
            for (int i = 0; i < 3; i++) {
                float off = (i - 1) * 1.5f * scale;
                Path claw = new Path();
                claw.moveTo(hx + off * 0.4f, hy + 1 * scale);
                claw.lineTo(hx + off + s * 2 * scale, hy + 5 * scale);
                claw.lineTo(hx + off * 0.4f + s * 0.5f * scale, hy + 2 * scale);
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

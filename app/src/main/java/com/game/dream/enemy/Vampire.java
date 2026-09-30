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
 * 视觉(参照梦幻西游吸血鬼): 狂野长发、蝙蝠式大尖耳、青灰苍白面孔、
 *       血红斜眼、血盆大口外露两颗长獠牙并滴血；残破暗紫黑高领披风
 *       （血红内衬），敞开胸口血迹斑斑，细长苍白手臂末端生四根黑爪，
 *       脚下悬浮不接地，移动拖出血雾尾迹。
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
        // 残破的暗紫黑披风（底部锯齿状破损），内衬血红
        float wave = (float) Math.sin(capePhase) * 2 * scale;

        // 血红内衬（底层）
        paint.setColor(Color.argb(240, 120, 15, 25));
        Path lining = new Path();
        lining.moveTo(cx - 10 * scale, cy - 8 * scale);
        lining.lineTo(cx + 10 * scale, cy - 8 * scale);
        lining.lineTo(cx + 13 * scale + wave, cy + 18 * scale);
        lining.lineTo(cx - 13 * scale - wave, cy + 18 * scale);
        lining.close();
        canvas.drawPath(lining, paint);

        // 暗紫黑披风外层（残破锯齿下摆，中间开口露出血红内衬）
        paint.setColor(Color.argb(245, 32, 20, 40));
        Path leftCape = new Path();
        leftCape.moveTo(cx - 10 * scale, cy - 9 * scale);
        leftCape.lineTo(cx - 3 * scale, cy - 6 * scale);
        leftCape.lineTo(cx - 5 * scale - wave, cy + 20 * scale);
        leftCape.lineTo(cx - 9 * scale - wave, cy + 14 * scale);
        leftCape.lineTo(cx - 12 * scale - wave, cy + 22 * scale);
        leftCape.lineTo(cx - 16 * scale - wave, cy + 15 * scale);
        leftCape.close();
        canvas.drawPath(leftCape, paint);

        Path rightCape = new Path();
        rightCape.moveTo(cx + 10 * scale, cy - 9 * scale);
        rightCape.lineTo(cx + 3 * scale, cy - 6 * scale);
        rightCape.lineTo(cx + 5 * scale + wave, cy + 20 * scale);
        rightCape.lineTo(cx + 9 * scale + wave, cy + 14 * scale);
        rightCape.lineTo(cx + 12 * scale + wave, cy + 22 * scale);
        rightCape.lineTo(cx + 16 * scale + wave, cy + 15 * scale);
        rightCape.close();
        canvas.drawPath(rightCape, paint);

        // 高耸尖立领（吸血鬼标志性高领，两片尖角）
        paint.setColor(Color.argb(250, 24, 16, 30));
        Path collar = new Path();
        collar.moveTo(cx - 9 * scale, cy - 8 * scale);
        collar.lineTo(cx - 13 * scale, cy - 24 * scale);
        collar.lineTo(cx - 4 * scale, cy - 12 * scale);
        collar.lineTo(cx + 4 * scale, cy - 12 * scale);
        collar.lineTo(cx + 13 * scale, cy - 24 * scale);
        collar.lineTo(cx + 9 * scale, cy - 8 * scale);
        collar.close();
        canvas.drawPath(collar, paint);

        // 立领血红内缘
        paint.setColor(Color.argb(200, 130, 20, 30));
        paint.setStrokeWidth(0.9f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 12 * scale, cy - 22 * scale, cx - 4 * scale, cy - 12 * scale, paint);
        canvas.drawLine(cx + 12 * scale, cy - 22 * scale, cx + 4 * scale, cy - 12 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 破旧的暗色贵族外衣
        paint.setColor(Color.argb(245, 28, 22, 34));
        Path body = new Path();
        body.moveTo(cx - 8 * scale, cy - 9 * scale);
        body.lineTo(cx + 8 * scale, cy - 9 * scale);
        body.lineTo(cx + 9 * scale, cy + 5 * scale);
        body.lineTo(cx + 6 * scale, cy + 17 * scale);
        body.lineTo(cx - 6 * scale, cy + 17 * scale);
        body.lineTo(cx - 9 * scale, cy + 5 * scale);
        body.close();
        canvas.drawPath(body, paint);

        // 敞开的胸口（苍白皮肤 V 形）
        paint.setColor(Color.argb(240, 200, 208, 199));
        Path chest = new Path();
        chest.moveTo(cx - 3.5f * scale, cy - 9 * scale);
        chest.lineTo(cx + 3.5f * scale, cy - 9 * scale);
        chest.lineTo(cx + 2 * scale, cy + 6 * scale);
        chest.lineTo(cx, cy + 9 * scale);
        chest.lineTo(cx - 2 * scale, cy + 6 * scale);
        chest.close();
        canvas.drawPath(chest, paint);

        // 胸前血迹斑斑
        paint.setColor(Color.argb(160, 140, 20, 28));
        canvas.drawCircle(cx - 1 * scale, cy - 1 * scale, 1.4f * scale, paint);
        canvas.drawCircle(cx + 1.4f * scale, cy + 3 * scale, 1 * scale, paint);

        // 血色宝石胸针（胸前一点红光）
        float pulse = 0.7f + 0.3f * (float) Math.sin(now / 500.0);
        paint.setColor(Color.argb((int) (255 * pulse), 220, 30, 40));
        canvas.drawCircle(cx, cy - 5 * scale, 1.6f * scale, paint);
        paint.setColor(Color.argb((int) (120 * pulse), 255, 80, 90));
        canvas.drawCircle(cx, cy - 5 * scale, 3 * scale, paint);

        // 残破腰带（暗红丝带）
        paint.setColor(Color.argb(240, 90, 15, 25));
        canvas.drawRect(cx - 8 * scale, cy + 4 * scale, cx + 8 * scale, cy + 6 * scale, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float headY = cy - 16 * scale;

        // 1. 狂野长发（后层，垂至肩部，发梢残破）—— 深蓝黑
        paint.setColor(Color.argb(248, 22, 18, 30));
        Path backHair = new Path();
        backHair.moveTo(cx - 7 * scale, headY - 6 * scale);
        backHair.quadTo(cx - 12 * scale, headY + 2 * scale, cx - 9 * scale, headY + 14 * scale);
        backHair.lineTo(cx - 6 * scale, headY + 9 * scale);
        backHair.lineTo(cx - 4 * scale, headY + 15 * scale);
        backHair.lineTo(cx - 1 * scale, headY + 9 * scale);
        backHair.lineTo(cx + 2 * scale, headY + 15 * scale);
        backHair.lineTo(cx + 5 * scale, headY + 9 * scale);
        backHair.lineTo(cx + 9 * scale, headY + 14 * scale);
        backHair.quadTo(cx + 12 * scale, headY + 2 * scale, cx + 7 * scale, headY - 6 * scale);
        backHair.close();
        canvas.drawPath(backHair, paint);

        // 2. 蝙蝠式大尖耳（梦幻西游吸血鬼标志性特征）
        drawPointedEar(canvas, paint, cx, headY, scale, -1);
        drawPointedEar(canvas, paint, cx, headY, scale, 1);

        // 3. 青灰苍白病态面孔
        paint.setColor(Color.argb(245, 206, 214, 205));
        canvas.drawOval(cx - 6 * scale, headY - 7 * scale,
                cx + 6 * scale, headY + 7.5f * scale, paint);
        // 消瘦的下颌阴影
        paint.setColor(Color.argb(55, 90, 110, 95));
        canvas.drawOval(cx - 4.5f * scale, headY + 0.5f * scale,
                cx + 4.5f * scale, headY + 7.5f * scale, paint);

        // 4. 前额乱发（锯齿状刘海）
        paint.setColor(Color.argb(248, 26, 20, 34));
        Path fringe = new Path();
        fringe.moveTo(cx - 6.5f * scale, headY - 2.5f * scale);
        fringe.quadTo(cx - 6 * scale, headY - 9 * scale, cx, headY - 8 * scale);
        fringe.quadTo(cx + 6 * scale, headY - 9 * scale, cx + 6.5f * scale, headY - 2.5f * scale);
        fringe.lineTo(cx + 4 * scale, headY - 5 * scale);
        fringe.lineTo(cx + 2.5f * scale, headY - 0.5f * scale);
        fringe.lineTo(cx + 1 * scale, headY - 5 * scale);
        fringe.lineTo(cx - 1 * scale, headY - 1.5f * scale);
        fringe.lineTo(cx - 3 * scale, headY - 5.5f * scale);
        fringe.lineTo(cx - 4.5f * scale, headY - 1 * scale);
        fringe.close();
        canvas.drawPath(fringe, paint);

        // 5. 血红斜眼（发光）+ 愤怒斜眉
        float glow = 0.75f + 0.25f * (float) Math.sin(eyeGlowPhase);
        paint.setColor(Color.argb(110, 40, 20, 30));
        canvas.drawOval(cx - 4.6f * scale, headY - 3 * scale, cx - 0.4f * scale, headY + 0.6f * scale, paint);
        canvas.drawOval(cx + 0.4f * scale, headY - 3 * scale, cx + 4.6f * scale, headY + 0.6f * scale, paint);
        paint.setColor(Color.argb((int) (255 * glow), 230, 25, 30));
        canvas.drawCircle(cx - 2.5f * scale, headY - 1.2f * scale, 1.4f * scale, paint);
        canvas.drawCircle(cx + 2.5f * scale, headY - 1.2f * scale, 1.4f * scale, paint);
        paint.setColor(Color.argb((int) (110 * glow), 255, 60, 60));
        canvas.drawCircle(cx - 2.5f * scale, headY - 1.2f * scale, 3 * scale, paint);
        canvas.drawCircle(cx + 2.5f * scale, headY - 1.2f * scale, 3 * scale, paint);
        paint.setColor(Color.argb(230, 20, 15, 22));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 4.6f * scale, headY - 3.8f * scale, cx - 1 * scale, headY - 2 * scale, paint);
        canvas.drawLine(cx + 4.6f * scale, headY - 3.8f * scale, cx + 1 * scale, headY - 2 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 6. 张开的血盆大口
        paint.setColor(Color.argb(235, 60, 12, 18));
        canvas.drawOval(cx - 3.2f * scale, headY + 1.5f * scale,
                cx + 3.2f * scale, headY + 6.5f * scale, paint);
        paint.setColor(Color.argb(235, 25, 5, 8));
        canvas.drawOval(cx - 2.2f * scale, headY + 2.5f * scale,
                cx + 2.2f * scale, headY + 6 * scale, paint);

        // 7. 两颗长上獠牙（从上前唇垂下）
        paint.setColor(Color.argb(252, 250, 248, 240));
        Path fangL = new Path();
        fangL.moveTo(cx - 2.7f * scale, headY + 2 * scale);
        fangL.lineTo(cx - 1.5f * scale, headY + 2 * scale);
        fangL.lineTo(cx - 2.1f * scale, headY + 8 * scale);
        fangL.close();
        canvas.drawPath(fangL, paint);
        Path fangR = new Path();
        fangR.moveTo(cx + 1.5f * scale, headY + 2 * scale);
        fangR.lineTo(cx + 2.7f * scale, headY + 2 * scale);
        fangR.lineTo(cx + 2.1f * scale, headY + 8 * scale);
        fangR.close();
        canvas.drawPath(fangR, paint);

        // 8. 獠牙滴血（持续滴落动画）
        float drip = (float) ((now / 500.0) % 1.0);
        paint.setColor(Color.argb(210, 190, 20, 30));
        canvas.drawCircle(cx - 2.1f * scale, headY + 8 * scale + drip * 3 * scale, 0.9f * scale * (1 - drip * 0.4f), paint);
        canvas.drawCircle(cx + 2.1f * scale, headY + 8 * scale + drip * 3 * scale, 0.9f * scale * (1 - drip * 0.4f), paint);
    }

    /**
     * 蝙蝠式大尖耳（梦幻西游吸血鬼标志性造型）
     * @param s -1=左耳, 1=右耳
     */
    private void drawPointedEar(Canvas canvas, Paint paint, float cx, float headY, float scale, int s) {
        // 外耳（苍白皮肤，尖角朝外上方）
        paint.setColor(Color.argb(245, 198, 206, 197));
        Path ear = new Path();
        ear.moveTo(cx + s * 4.5f * scale, headY - 3 * scale);
        ear.lineTo(cx + s * 13 * scale, headY - 10 * scale);
        ear.lineTo(cx + s * 11 * scale, headY - 1 * scale);
        ear.lineTo(cx + s * 5.5f * scale, headY + 3 * scale);
        ear.close();
        canvas.drawPath(ear, paint);
        // 内耳阴影（暗红）
        paint.setColor(Color.argb(150, 150, 55, 65));
        Path inner = new Path();
        inner.moveTo(cx + s * 6 * scale, headY - 2.5f * scale);
        inner.lineTo(cx + s * 11.5f * scale, headY - 8 * scale);
        inner.lineTo(cx + s * 9.5f * scale, headY - 1 * scale);
        inner.close();
        canvas.drawPath(inner, paint);
    }

    private void drawArms(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 细长苍白手臂，前伸，末端生四根长黑爪（凶残）
        float swing = 0;
        if (currentState == State.ATTACKING && isWindingUp) {
            swing = -getWindUpProgress() * 12 * scale;
        }

        // 手臂（苍白带青灰）
        paint.setColor(Color.argb(245, 198, 206, 197));
        paint.setStrokeWidth(3.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int s = -1; s <= 1; s += 2) {
            Path arm = new Path();
            arm.moveTo(cx + s * 7 * scale, cy - 5 * scale);
            arm.quadTo(cx + s * 13 * scale, cy + 1 * scale,
                    cx + s * 15 * scale, cy + 9 * scale + swing * 0.3f);
            canvas.drawPath(arm, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 手掌 + 4根细长黑爪
        for (int s = -1; s <= 1; s += 2) {
            float hx = cx + s * 15 * scale;
            float hy = cy + 9 * scale + swing * 0.3f;
            paint.setColor(Color.argb(245, 205, 212, 203));
            canvas.drawCircle(hx, hy, 2.2f * scale, paint);
            paint.setColor(Color.argb(252, 18, 12, 18));
            for (int i = 0; i < 4; i++) {
                float off = (i - 1.5f) * 1.3f * scale;
                Path claw = new Path();
                claw.moveTo(hx + off * 0.4f, hy + 1 * scale);
                claw.lineTo(hx + off + s * 2.5f * scale, hy + 7 * scale);
                claw.lineTo(hx + off * 0.4f + s * 0.6f * scale, hy + 2 * scale);
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

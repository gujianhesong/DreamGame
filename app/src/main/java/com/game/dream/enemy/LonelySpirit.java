package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 孤魂 - 地府中远程法术型敌人
 * 生前含冤而死、执念未消的魂魄，在黄泉路畔漫无目的地游荡。
 *
 * 视觉: 半透明冷灰蓝色，无脚，下半身化为雾状尾迹，眼窝流幽蓝泪痕，
 *       灰白长发向上飘散，残破白色殓衣，身周环绕鬼火。
 *
 * 攻击:
 *  1. 怨灵哀嚎 - 扇形音波（ARC近战），命中减速
 *  2. 幽蓝泪珠 - 缓慢飞行的蓝色光球（远程法术弹幕）
 *  3. 相位闪避 - 被动，受击40%概率虚化免伤并瞬移，冷却3秒
 *
 * 属性: HP中低、物理防御极低、魔抗高、移速慢、攻击间隔长但单次伤害不低
 * 出没: 彼岸花田、黄泉河两岸
 */
public class LonelySpirit extends Enemy {

    // 幽蓝泪珠法术
    private long lastSoulTearTime = 0;
    private boolean pendingSoulTear = false;
    private static final long SOUL_TEAR_COOLDOWN = 4000;

    // 相位闪避
    private long lastPhaseDodgeTime = 0;
    private static final long PHASE_DODGE_COOLDOWN = 3000;
    private static final float PHASE_DODGE_CHANCE = 0.4f;
    private boolean isPhaseShifting = false;
    private long phaseShiftStartTime = 0;
    private static final long PHASE_SHIFT_DURATION = 400; // 虚化动画时长

    // 飘行动画相位
    private float floatPhase = (float) (Math.random() * Math.PI * 2);

    public LonelySpirit(float x, float y) {
        super(x, y, 75);
        attackCooldown = 2800; // 攻击间隔长
        setAttackShape(AttackShape.ARC); // 怨灵哀嚎 - 扇形
        addAvailableAttackType(AttackType.MELEE);
        windUpDuration = 600; // 哀嚎前摇较长（张嘴蓄力）

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 420;
        prop.attackRange = 300; // 中远程
        prop.rewardExp = 500;
        prop.rewardMoney = 250;
        setPropertyExtra(prop);

        // HP中低、攻击(物理)低、防御极低、速度慢、法力高(魔抗高)
        setProperty(1500, 200, 200, 220, 600);

        // 等级分布
        resetPropertyWithLevel();
    }

    @Override
    public boolean usesGenericFireball() {
        return false; // 使用专属法术"幽蓝泪珠"
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive() || isStunned() || isFrozen()) return;

        long now = System.currentTimeMillis();

        // 更新飘行相位
        floatPhase += 0.003f * deltaTime;

        // 相位闪避动画结束
        if (isPhaseShifting && now - phaseShiftStartTime > PHASE_SHIFT_DURATION) {
            isPhaseShifting = false;
        }

        // 尝试施放幽蓝泪珠
        tickSpellWindUp(now, playerX, playerY);
        tryCastSoulTear(playerX, playerY, now);
    }

    /**
     * 幽蓝泪珠: 向玩家发射一颗缓慢飞行的蓝色光球
     */
    private void tryCastSoulTear(float playerX, float playerY, long now) {
        if (isSpellWindingUp || pendingSoulTear) return;
        if (now - lastSoulTearTime < SOUL_TEAR_COOLDOWN) return;
        if (now - lastSpellCastRollTime < SPELL_CAST_CHECK_INTERVAL) return;

        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > propertyExtra.detectionRange * 1.2f) return;

        lastSpellCastRollTime = now;

        // 距离越远越倾向施法（保持距离输出）
        float chance;
        if (dist > propertyExtra.attackRange) {
            chance = 0.5f;
        } else {
            chance = 0.2f;
        }
        if (enemyLevel == EnemyLevel.BOSS) chance *= 1.3f;
        else if (enemyLevel == EnemyLevel.ELITE) chance *= 1.15f;

        if (Math.random() < chance) {
            beginSpellWindUp(SPELL_WATER_BOLT); // 复用 WATER_BOLT 的前摇ID
            // 实际在 onSpecialSpellWindUpComplete 中改为 SoulTear
        }
    }

    @Override
    protected void onSpecialSpellWindUpComplete(int spellId, long currentTime) {
        // 孤魂的法术前摇完成后生成幽蓝泪珠
        pendingSoulTear = true;
        lastSoulTearTime = currentTime;
    }

    public boolean isPendingSoulTear() {
        return pendingSoulTear;
    }

    public void consumeSoulTear() {
        pendingSoulTear = false;
    }

    /**
     * 相位闪避: 受到攻击时40%概率虚化免伤并瞬移150px
     */
    @Override
    public boolean takeDamage(int damage) {
        long now = System.currentTimeMillis();

        // 相位闪避判定（冷却中不触发）
        if (now - lastPhaseDodgeTime >= PHASE_DODGE_COOLDOWN && Math.random() < PHASE_DODGE_CHANCE) {
            lastPhaseDodgeTime = now;
            isPhaseShifting = true;
            phaseShiftStartTime = now;

            // 瞬移150px随机方向
            float angle = (float) (Math.random() * Math.PI * 2);
            float newX = x + (float) Math.cos(angle) * 150;
            float newY = y + (float) Math.sin(angle) * 150;

            // 边界限制
            com.game.dream.bean.MapInfo mapInfo = com.game.dream.system.MapSystem.getInstance().getCurMapInfo();
            if (mapInfo != null) {
                int[][] map = mapInfo.getMapData();
                if (map != null) {
                    float mapW = map[0].length * com.game.dream.common.Constants.TILE_SIZE;
                    float mapH = map.length * com.game.dream.common.Constants.TILE_SIZE;
                    newX = Math.max(size, Math.min(newX, mapW - size));
                    newY = Math.max(size, Math.min(newY, mapH - size));
                }
            }
            x = newX;
            y = newY;

            // 短暂无敌（避免连续触发）
            isInvincible = true;
            invincibleEndTime = now + 500;

            return false; // 未受伤
        }

        return super.takeDamage(damage);
    }

    /**
     * 覆盖追击行为: 保持中远距离飘行，不贴脸
     */
    @Override
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (isRooted()) return;
        if (isSpellWindingUp) return;

        float preferredDist = propertyExtra.attackRange * 0.8f; // 240px

        if (dist < preferredDist * 0.6f) {
            // 太近，后退
            targetX = x - dx / dist * 200;
            targetY = y - dy / dist * 200;
        } else if (dist > propertyExtra.detectionRange) {
            // 太远，靠近
            targetX = playerX;
            targetY = playerY;
        } else {
            // 理想距离，横向飘行（绕圈）+ 上下浮动
            float perpX = -dy / dist;
            float perpY = dx / dist;
            float circleDir = (float) Math.sin(System.currentTimeMillis() / 3000.0) > 0 ? 1 : -1;
            targetX = x + perpX * 80 * circleDir;
            targetY = y + perpY * 80 * circleDir;
        }

        // 飘行速度较慢
        moveToTargetWithSpeed(deltaSeconds, speed * 0.9f);
    }

    @Override
    protected void performAttack() {
        // 由 GameEngine 处理
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel3(this);
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
        // 飘行上下浮动
        float floatY = (float) Math.sin(floatPhase) * 4 * scale;
        float cy = y + offsetY + vibY + floatY;

        // 相位闪避时半透明闪烁
        int baseAlpha = 140;
        if (isPhaseShifting) {
            long elapsed = now - phaseShiftStartTime;
            float t = (float) elapsed / PHASE_SHIFT_DURATION;
            baseAlpha = (int) (40 + 30 * Math.sin(t * Math.PI * 4));
        }

        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // === 1. 身周鬼火（Wisp）===
        drawWisps(canvas, paint, cx, cy, scale, now, baseAlpha);

        // === 2. 雾状尾迹（下半身）===
        drawMistTail(canvas, paint, cx, cy, scale, now, baseAlpha);

        // === 3. 殓衣/身体 ===
        drawBody(canvas, paint, cx, cy, scale, now, baseAlpha, dir);

        // === 4. 头部 ===
        drawHead(canvas, paint, cx, cy, scale, now, baseAlpha, dir);

        // === 5. 手臂（前伸索求姿态）===
        drawArms(canvas, paint, cx, cy, scale, now, baseAlpha, dir);

        // === 6. 攻击特效: 哀嚎音波 ===
        if (currentState == State.ATTACKING && isWindingUp) {
            drawWailingEffect(canvas, paint, cx, cy, scale, dir);
        }
    }

    private void drawWisps(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 3团幽蓝鬼火环绕
        for (int i = 0; i < 3; i++) {
            float angle = (float) (now / 1500.0 + i * Math.PI * 2 / 3);
            float radius = 18 * scale + (float) Math.sin(now / 600.0 + i * 2) * 4 * scale;
            float wx = cx + (float) Math.cos(angle) * radius;
            float wy = cy - 5 * scale + (float) Math.sin(angle) * radius * 0.5f;
            float wispAlpha = 0.5f + 0.5f * (float) Math.sin(now / 300.0 + i * 1.5);

            // 外层光晕
            paint.setColor(Color.argb((int) (alpha * 0.3f * wispAlpha), 80, 160, 255));
            canvas.drawCircle(wx, wy, 5 * scale, paint);
            // 核心
            paint.setColor(Color.argb((int) (alpha * 0.8f * wispAlpha), 150, 210, 255));
            canvas.drawCircle(wx, wy, 2.5f * scale, paint);
        }
    }

    private void drawMistTail(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 3~4条飘散的雾状尾迹
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (int i = 0; i < 4; i++) {
            float offsetX = (i - 1.5f) * 5 * scale;
            float wave = (float) Math.sin(now / 500.0 + i * 1.2) * 6 * scale;
            float tailAlpha = alpha * (0.6f - i * 0.1f);

            paint.setColor(Color.argb((int) tailAlpha, 180, 200, 220));
            paint.setStrokeWidth((3 - i * 0.5f) * scale);

            Path tail = new Path();
            tail.moveTo(cx + offsetX, cy + 8 * scale);
            tail.quadTo(cx + offsetX + wave, cy + 18 * scale,
                    cx + offsetX + wave * 1.5f, cy + 28 * scale);
            canvas.drawPath(tail, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha, float dir) {
        // 残破白色殓衣
        paint.setColor(Color.argb(alpha, 220, 225, 235));
        Path robe = new Path();
        robe.moveTo(cx - 9 * scale, cy - 6 * scale);
        robe.lineTo(cx + 9 * scale, cy - 6 * scale);
        robe.lineTo(cx + 11 * scale, cy + 6 * scale);
        // 撕裂的衣摆
        float tear1 = (float) Math.sin(now / 400.0) * 2 * scale;
        float tear2 = (float) Math.sin(now / 400.0 + 1.5) * 2 * scale;
        robe.lineTo(cx + 7 * scale, cy + 12 * scale + tear1);
        robe.lineTo(cx + 3 * scale, cy + 9 * scale);
        robe.lineTo(cx - 1 * scale, cy + 14 * scale + tear2);
        robe.lineTo(cx - 5 * scale, cy + 10 * scale);
        robe.lineTo(cx - 9 * scale, cy + 13 * scale + tear1);
        robe.lineTo(cx - 11 * scale, cy + 6 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        // 隐约可见的肋骨
        paint.setColor(Color.argb((int) (alpha * 0.4f), 160, 170, 185));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 3; i++) {
            float ribY = cy - 2 * scale + i * 3.5f * scale;
            canvas.drawLine(cx - 5 * scale, ribY, cx + 5 * scale, ribY, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 殓衣上的暗色污渍
        paint.setColor(Color.argb((int) (alpha * 0.25f), 100, 110, 130));
        canvas.drawCircle(cx - 4 * scale, cy + 2 * scale, 2 * scale, paint);
        canvas.drawCircle(cx + 5 * scale, cy - 1 * scale, 1.5f * scale, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha, float dir) {
        float headY = cy - 14 * scale;

        // 模糊椭圆面孔
        paint.setColor(Color.argb(alpha, 200, 210, 225));
        canvas.drawOval(cx - 6 * scale, headY - 7 * scale,
                cx + 6 * scale, headY + 7 * scale, paint);

        // 灰白长发向上飘散
        paint.setColor(Color.argb((int) (alpha * 0.8f), 190, 195, 205));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < 7; i++) {
            float hairX = cx + (i - 3) * 2.2f * scale;
            float hairWave = (float) Math.sin(now / 600.0 + i * 0.9) * 3 * scale;
            float hairLen = (10 + (3 - Math.abs(i - 3)) * 3) * scale;
            Path hair = new Path();
            hair.moveTo(hairX, headY - 5 * scale);
            hair.quadTo(hairX + hairWave, headY - 5 * scale - hairLen * 0.6f,
                    hairX + hairWave * 1.5f, headY - 5 * scale - hairLen);
            canvas.drawPath(hair, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 空洞眼窝
        paint.setColor(Color.argb(alpha, 30, 40, 60));
        canvas.drawOval(cx - 4.5f * scale, headY - 3 * scale,
                cx - 1.5f * scale, headY + 0.5f * scale, paint);
        canvas.drawOval(cx + 1.5f * scale, headY - 3 * scale,
                cx + 4.5f * scale, headY + 0.5f * scale, paint);

        // 幽蓝泪痕（从眼窝向下延伸的光带）
        float tearWave = (float) Math.sin(now / 400.0) * 1 * scale;
        paint.setColor(Color.argb((int) (alpha * 0.9f), 100, 180, 255));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        // 左泪
        Path leftTear = new Path();
        leftTear.moveTo(cx - 3 * scale, headY + 0.5f * scale);
        leftTear.quadTo(cx - 3.5f * scale + tearWave, headY + 5 * scale,
                cx - 3 * scale + tearWave, headY + 10 * scale);
        canvas.drawPath(leftTear, paint);
        // 右泪
        Path rightTear = new Path();
        rightTear.moveTo(cx + 3 * scale, headY + 0.5f * scale);
        rightTear.quadTo(cx + 3.5f * scale - tearWave, headY + 5 * scale,
                cx + 3 * scale - tearWave, headY + 10 * scale);
        canvas.drawPath(rightTear, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 嘴（攻击时张开 - 黑色椭圆）
        if (currentState == State.ATTACKING) {
            float openRatio = isWindingUp ? getWindUpProgress() : 0.6f;
            paint.setColor(Color.argb(alpha, 15, 20, 35));
            canvas.drawOval(cx - 2.5f * scale, headY + 3 * scale,
                    cx + 2.5f * scale, headY + 3 * scale + 4 * scale * openRatio, paint);
        }
    }

    private void drawArms(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha, float dir) {
        // 枯瘦双手向前伸出（索求姿态）
        paint.setColor(Color.argb(alpha, 195, 205, 220));
        paint.setStrokeWidth(2 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float armWave = (float) Math.sin(now / 700.0) * 2 * scale;
        float reach = (currentState == State.ATTACKING) ? 4 * scale : 0;

        // 左臂
        Path leftArm = new Path();
        leftArm.moveTo(cx - 8 * scale, cy - 3 * scale);
        leftArm.quadTo(cx - 12 * scale, cy - 6 * scale + armWave,
                cx - 14 * scale - reach, cy - 10 * scale + armWave);
        canvas.drawPath(leftArm, paint);

        // 右臂
        Path rightArm = new Path();
        rightArm.moveTo(cx + 8 * scale, cy - 3 * scale);
        rightArm.quadTo(cx + 12 * scale, cy - 6 * scale - armWave,
                cx + 14 * scale + reach, cy - 10 * scale - armWave);
        canvas.drawPath(rightArm, paint);

        // 指尖幽蓝光点
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        float tipGlow = 0.5f + 0.5f * (float) Math.sin(now / 250.0);
        paint.setColor(Color.argb((int) (alpha * tipGlow), 120, 200, 255));
        canvas.drawCircle(cx - 14 * scale - reach, cy - 10 * scale + armWave, 2 * scale, paint);
        canvas.drawCircle(cx + 14 * scale + reach, cy - 10 * scale - armWave, 2 * scale, paint);
    }

    private void drawWailingEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        float progress = getWindUpProgress();
        // 扇形音波扩散
        paint.setColor(Color.argb((int) (progress * 60), 100, 180, 255));
        paint.setStyle(Paint.Style.FILL);
        float arcRadius = propertyExtra.attackRange * progress * 0.8f;
        float angle = facingRightAngle(dir);
        canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                (float) Math.toDegrees(angle) - 45, 90, true, paint);

        // 音波纹
        paint.setColor(Color.argb((int) (progress * 100), 150, 210, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * scale * progress);
        for (int i = 1; i <= 3; i++) {
            float r = arcRadius * (i / 3f);
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r,
                    (float) Math.toDegrees(angle) - 40, 80, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private float facingRightAngle(float dir) {
        return dir > 0 ? 0 : (float) Math.PI;
    }
}

package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 野鬼 - 地府中近战爆发型敌人
 * 生前穷凶极恶之徒，死后拒绝接受阎罗审判，打伤鬼差逃出鬼门关，堕入荒原化为厉鬼。
 *
 * 视觉: 实体化暗红/灰青色，体型壮硕驼背，血红双眼发光，额头短角，
 *       黑红短刺发，赤裸上身皮肤裂纹透暗红光，利爪，脚踝断裂铁镣，
 *       脊椎外露带暗红符文。
 *
 * 攻击:
 *  1. 裂魂爪 - 三连击（COMBO），第三击附带击退
 *  2. 暴怒冲锋 - 距离>400px时高速直线冲锋（CHARGE），大伤害+击飞
 *  3. 怨爆 - 死亡时爆炸（GameEngine处理），对周围造成最大HP 15%伤害
 *  4. 不屈 - 被动，HP<20%时狂暴：移速+50%、攻速+50%、体型膨胀15%
 *
 * 属性: HP高、物理防御中等、魔抗低、移速中等（狂暴时极快）
 * 出没: 幽冥荒原、白骨堆密集带
 */
public class SavageWraith extends Enemy {

    // 不屈狂暴
    private boolean isEnraged = false;
    private int originalSpeed;
    private int originalSize;
    private long originalAttackCooldown;

    // 怨爆（死亡爆炸）
    private boolean hasExploded = false;
    private long deathTime = 0;
    private static final long EXPLOSION_DELAY = 500; // 膨胀0.5秒后爆炸
    public static final float EXPLOSION_RANGE = 200f;

    // 冲锋火焰痕迹视觉
    private long lastChargeTime = 0;

    public SavageWraith(float x, float y) {
        super(x, y, 95);
        attackCooldown = 1600;
        setAttackShape(AttackShape.RECT); // 裂魂爪 - 矩形前方
        addAvailableAttackType(AttackType.COMBO);  // 裂魂爪三连击
        addAvailableAttackType(AttackType.CHARGE); // 暴怒冲锋

        // COMBO 参数
        comboHitCount = 3;
        comboHitInterval = 300; // 每击间隔0.3s

        // CHARGE 参数
        chargeSpeedMultiplier = 4.0f; // 冲锋速度×4
        windUpDuration = 500; // 冲锋蓄力0.5s

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 500;
        prop.attackRange = 160; // 近战
        prop.rewardExp = 600;
        prop.rewardMoney = 300;
        setPropertyExtra(prop);

        // HP高、攻击高、防御中等、速度中等、法力低(魔抗低)
        setProperty(1300, 500, 400, 200, 250);

        // 等级分布
        if (Math.random() < 0.02) {
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 50, attackDamage * 8, defense * 8, speed * 7, mana * 4);
        } else if (Math.random() < 0.07) {
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 10, attackDamage * 4, defense * 4, speed * 4, mana * 2);
        } else if (Math.random() < 0.30) {
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, attackDamage * 2, defense * 2, speed * 2, mana * 2);
        }

        // 精英/BOSS 可额外使用猛扑
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.POUNCE);
        }

        // 记录原始值（狂暴时对比用）
        originalSpeed = speed;
        originalSize = size;
        originalAttackCooldown = attackCooldown;
    }

    @Override
    public boolean usesGenericFireball() {
        return false; // 纯物理近战，不使用法术
    }

    /**
     * 覆盖 takeDamage: 死亡时立即记录死亡时间（确保远距离击杀也能触发怨爆）
     */
    @Override
    public boolean takeDamage(int damage) {
        boolean died = super.takeDamage(damage);
        if (died && deathTime == 0) {
            deathTime = System.currentTimeMillis();
        }
        return died;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        // 不屈被动: HP < 20% 时进入狂暴
        checkEnrage();

        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive()) {
            // 记录死亡时间（用于怨爆延迟）
            if (deathTime == 0) {
                deathTime = System.currentTimeMillis();
            }
            return;
        }
    }

    /**
     * 不屈: HP低于20%时狂暴 - 移速+50%、攻速+50%、体型膨胀15%
     */
    private void checkEnrage() {
        if (isEnraged) return;
        if (maxHealth <= 0) return;

        float hpRatio = (float) health / maxHealth;
        if (hpRatio <= 0.2f) {
            isEnraged = true;
            speed = (int) (originalSpeed * 1.5f);
            size = (int) (originalSize * 1.15f);
            attackCooldown = (long) (originalAttackCooldown * 0.65f); // 攻速+50% ≈ 冷却×0.65
        }
    }

    public boolean isEnraged() {
        return isEnraged;
    }

    /**
     * 怨爆: 死亡后是否应该爆炸（由GameEngine调用）
     */
    public boolean shouldExplode() {
        if (hasExploded) return false;
        if (deathTime == 0) return false;
        return System.currentTimeMillis() - deathTime >= EXPLOSION_DELAY;
    }

    public void markExploded() {
        hasExploded = true;
    }

    public boolean hasExploded() {
        return hasExploded;
    }

    public float getExplosionRange() {
        return EXPLOSION_RANGE * (size / (float) originalSize);
    }

    /**
     * 爆炸伤害 = 最大HP的15%
     */
    public int getExplosionDamage() {
        return (int) (maxHealth * 0.15f);
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
        if (!isAlive() && !shouldExplode()) {
            // 死亡膨胀动画（爆炸前）
            if (deathTime > 0) {
                drawDeathSwell(canvas, offsetX, offsetY);
            }
            return;
        }
        if (!isAlive()) return;

        paint.setAntiAlias(true);
        float scale = size / 30.0f;
        long now = System.currentTimeMillis();

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = now - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 4 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY;

        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // 狂暴时全身冒黑红烟气
        if (isEnraged) {
            drawEnrageAura(canvas, paint, cx, cy, scale, now);
        }

        // === 1. 脚踝铁镣链环 ===
        drawShackles(canvas, paint, cx, cy, scale, now);

        // === 2. 身体（赤裸上身+裂纹）===
        drawTorso(canvas, paint, cx, cy, scale, now, dir);

        // === 3. 脊椎外露 + 符文 ===
        drawSpine(canvas, paint, cx, cy, scale, now);

        // === 4. 手臂 + 利爪 ===
        drawArmsAndClaws(canvas, paint, cx, cy, scale, now, dir);

        // === 5. 头部 ===
        drawHead(canvas, paint, cx, cy, scale, now, dir);

        // === 6. 焦黑脚印（移动时）===
        if (currentState == State.CHASING || isCharging) {
            drawScorchedFootprints(canvas, paint, cx, cy, scale, now);
        }

        // === 7. 冲锋蓄力特效 ===
        if (currentState == State.ATTACKING && isWindingUp
                && currentAttackType == AttackType.CHARGE) {
            drawChargeWindUp(canvas, paint, cx, cy, scale, now);
        }
    }

    private void drawDeathSwell(Canvas canvas, int offsetX, int offsetY) {
        long elapsed = System.currentTimeMillis() - deathTime;
        if (elapsed > EXPLOSION_DELAY) return;

        float t = (float) elapsed / EXPLOSION_DELAY;
        float swellScale = size * (1.0f + t * 0.6f);
        float cx = x + offsetX;
        float cy = y + offsetY;

        Paint p = new Paint();
        p.setAntiAlias(true);
        // 膨胀的暗红身体
        p.setColor(Color.argb((int) (200 * (1 - t)), 180, 40, 30));
        canvas.drawCircle(cx, cy, swellScale * 0.6f, p);
        // 裂纹爆亮
        p.setColor(Color.argb((int) (255 * t), 255, 80, 30));
        p.setStrokeWidth(3 * t);
        p.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 6; i++) {
            float angle = i * (float) Math.PI / 3 + t * 2;
            float r = swellScale * 0.5f;
            canvas.drawLine(cx, cy,
                    cx + (float) Math.cos(angle) * r,
                    cy + (float) Math.sin(angle) * r, p);
        }
    }

    private void drawEnrageAura(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 黑红色烟气
        for (int i = 0; i < 5; i++) {
            float angle = (float) (now / 800.0 + i * Math.PI * 2 / 5);
            float r = 16 * scale + (float) Math.sin(now / 300.0 + i) * 4 * scale;
            float sx = cx + (float) Math.cos(angle) * r;
            float sy = cy + (float) Math.sin(angle) * r * 0.7f;
            paint.setColor(Color.argb(40, 60, 10, 10));
            canvas.drawCircle(sx, sy, 5 * scale, paint);
            paint.setColor(Color.argb(25, 150, 30, 20));
            canvas.drawCircle(sx, sy, 8 * scale, paint);
        }
    }

    private void drawShackles(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 脚踝断裂铁镣
        paint.setColor(Color.rgb(60, 55, 50));
        paint.setStrokeWidth(2.5f * scale);
        paint.setStyle(Paint.Style.STROKE);

        float legY = cy + 12 * scale;
        // 左镣
        canvas.drawArc(cx - 7 * scale, legY - 2 * scale,
                cx - 3 * scale, legY + 3 * scale, 0, 300, false, paint);
        // 右镣
        canvas.drawArc(cx + 3 * scale, legY - 2 * scale,
                cx + 7 * scale, legY + 3 * scale, 0, 300, false, paint);

        // 断裂的链环拖行
        float chainSwing = (float) Math.sin(now / 300.0) * 3 * scale;
        paint.setStrokeWidth(1.5f * scale);
        paint.setColor(Color.rgb(80, 75, 65));
        canvas.drawLine(cx - 5 * scale, legY + 2 * scale,
                cx - 8 * scale + chainSwing, legY + 8 * scale, paint);
        canvas.drawLine(cx + 5 * scale, legY + 2 * scale,
                cx + 8 * scale - chainSwing, legY + 8 * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawTorso(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 灰青色壮硕上身（略驼背）
        paint.setColor(Color.rgb(90, 100, 95));
        Path torso = new Path();
        // 肩部宽，腰部略窄，驼背前倾
        torso.moveTo(cx - 11 * scale, cy - 8 * scale); // 左肩
        torso.quadTo(cx - 13 * scale, cy - 2 * scale, cx - 9 * scale, cy + 8 * scale); // 左腰
        torso.lineTo(cx + 9 * scale, cy + 8 * scale); // 右腰
        torso.quadTo(cx + 13 * scale, cy - 2 * scale, cx + 11 * scale, cy - 8 * scale); // 右肩
        torso.quadTo(cx + 4 * scale, cy - 12 * scale, cx, cy - 11 * scale); // 驼背颈
        torso.quadTo(cx - 4 * scale, cy - 12 * scale, cx - 11 * scale, cy - 8 * scale);
        torso.close();
        canvas.drawPath(torso, paint);

        // 肌肉隆起（深色阴影线）
        paint.setColor(Color.argb(80, 50, 60, 55));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 6 * scale, cy - 5 * scale, cx - 4 * scale, cy + 3 * scale, paint);
        canvas.drawLine(cx + 6 * scale, cy - 5 * scale, cx + 4 * scale, cy + 3 * scale, paint);
        // 胸肌线
        canvas.drawLine(cx - 5 * scale, cy - 4 * scale, cx, cy - 2 * scale, paint);
        canvas.drawLine(cx + 5 * scale, cy - 4 * scale, cx, cy - 2 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 裂纹（透出暗红色光）
        float crackGlow = isEnraged ? 1.0f : 0.6f;
        float pulse = 0.7f + 0.3f * (float) Math.sin(now / 300.0);
        paint.setColor(Color.argb((int) (180 * crackGlow * pulse), 200, 40, 30));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);

        // 左侧裂纹
        Path crack1 = new Path();
        crack1.moveTo(cx - 8 * scale, cy - 6 * scale);
        crack1.lineTo(cx - 5 * scale, cy - 1 * scale);
        crack1.lineTo(cx - 7 * scale, cy + 4 * scale);
        canvas.drawPath(crack1, paint);

        // 右侧裂纹
        Path crack2 = new Path();
        crack2.moveTo(cx + 7 * scale, cy - 7 * scale);
        crack2.lineTo(cx + 4 * scale, cy - 2 * scale);
        crack2.lineTo(cx + 8 * scale, cy + 3 * scale);
        canvas.drawPath(crack2, paint);

        // 中央裂纹
        Path crack3 = new Path();
        crack3.moveTo(cx, cy - 9 * scale);
        crack3.lineTo(cx - 2 * scale, cy - 3 * scale);
        crack3.lineTo(cx + 1 * scale, cy + 5 * scale);
        canvas.drawPath(crack3, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 破旧囚裤（灰黑色）
        paint.setColor(Color.rgb(45, 42, 48));
        Path pants = new Path();
        pants.moveTo(cx - 9 * scale, cy + 7 * scale);
        pants.lineTo(cx + 9 * scale, cy + 7 * scale);
        pants.lineTo(cx + 7 * scale, cy + 14 * scale);
        pants.lineTo(cx + 2 * scale, cy + 14 * scale);
        pants.lineTo(cx, cy + 10 * scale);
        pants.lineTo(cx - 2 * scale, cy + 14 * scale);
        pants.lineTo(cx - 7 * scale, cy + 14 * scale);
        pants.close();
        canvas.drawPath(pants, paint);
    }

    private void drawSpine(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 脊椎骨外露（从后颈延伸到腰）
        paint.setColor(Color.rgb(200, 195, 180));
        float spineX = cx - 1 * scale; // 略偏后
        for (int i = 0; i < 6; i++) {
            float sy = cy - 9 * scale + i * 3.2f * scale;
            float boneSize = (2.0f - i * 0.15f) * scale;
            canvas.drawCircle(spineX, sy, boneSize, paint);
        }

        // 脊椎两侧暗红符文
        float runeGlow = 0.5f + 0.5f * (float) Math.sin(now / 500.0);
        int runeAlpha = isEnraged ? (int) (200 * runeGlow) : (int) (100 * runeGlow);
        paint.setColor(Color.argb(runeAlpha, 180, 30, 25));
        for (int i = 0; i < 4; i++) {
            float sy = cy - 7 * scale + i * 3.5f * scale;
            // 左符文
            canvas.drawRect(spineX - 5 * scale, sy, spineX - 3.5f * scale, sy + 2 * scale, paint);
            // 右符文
            canvas.drawRect(spineX + 3.5f * scale, sy, spineX + 5 * scale, sy + 2 * scale, paint);
        }
    }

    private void drawArmsAndClaws(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 粗壮手臂
        paint.setColor(Color.rgb(85, 95, 90));
        paint.setStrokeWidth(4 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float armSwing = (float) Math.sin(now / 250.0) * 3 * scale;
        boolean attacking = currentState == State.ATTACKING;
        float reach = attacking ? 6 * scale : 0;

        // 左臂
        canvas.drawLine(cx - 11 * scale, cy - 6 * scale,
                cx - 16 * scale - reach, cy + 2 * scale + armSwing, paint);
        // 右臂
        canvas.drawLine(cx + 11 * scale, cy - 6 * scale,
                cx + 16 * scale + reach, cy + 2 * scale - armSwing, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 利爪（5根黑色尖爪）
        paint.setColor(Color.rgb(20, 15, 20));
        drawClaw(canvas, paint, cx - 16 * scale - reach, cy + 2 * scale + armSwing, scale, -1, attacking, now);
        drawClaw(canvas, paint, cx + 16 * scale + reach, cy + 2 * scale - armSwing, scale, 1, attacking, now);
    }

    private void drawClaw(Canvas canvas, Paint paint, float baseX, float baseY, float scale, float dir, boolean attacking, long now) {
        float clawLen = attacking ? 6 * scale : 4.5f * scale;
        float spread = 0.4f;

        for (int i = 0; i < 5; i++) {
            float angle = (float) (-Math.PI / 2 + (i - 2) * spread);
            float tipX = baseX + dir * (float) Math.cos(angle) * clawLen;
            float tipY = baseY + (float) Math.sin(angle) * clawLen;

            paint.setStrokeWidth(1.5f * scale);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(baseX, baseY, tipX, tipY, paint);

            // 攻击时红色弧光拖尾
            if (attacking) {
                paint.setColor(Color.argb(120, 255, 50, 30));
                paint.setStrokeWidth(2.5f * scale);
                canvas.drawLine(baseX + dir * 2 * scale, baseY, tipX + dir * 3 * scale, tipY, paint);
                paint.setColor(Color.rgb(20, 15, 20));
            }
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        float headY = cy - 16 * scale;

        // 头部（狰狞面孔）
        paint.setColor(Color.rgb(85, 95, 90));
        Path head = new Path();
        head.moveTo(cx - 7 * scale, headY);
        head.quadTo(cx - 8 * scale, headY - 6 * scale, cx - 4 * scale, headY - 8 * scale);
        head.quadTo(cx, headY - 10 * scale, cx + 4 * scale, headY - 8 * scale);
        head.quadTo(cx + 8 * scale, headY - 6 * scale, cx + 7 * scale, headY);
        head.quadTo(cx + 5 * scale, headY + 5 * scale, cx, headY + 6 * scale);
        head.quadTo(cx - 5 * scale, headY + 5 * scale, cx - 7 * scale, headY);
        head.close();
        canvas.drawPath(head, paint);

        // 黑红色短刺发（向上竖立）
        paint.setColor(Color.rgb(40, 20, 25));
        for (int i = 0; i < 7; i++) {
            float hairX = cx + (i - 3) * 2.5f * scale;
            float hairHeight = (6 + (3 - Math.abs(i - 3)) * 2) * scale;
            float hairWave = (float) Math.sin(now / 200.0 + i) * 1 * scale;
            Path spike = new Path();
            spike.moveTo(hairX - 1.2f * scale, headY - 7 * scale);
            spike.lineTo(hairX + hairWave, headY - 7 * scale - hairHeight);
            spike.lineTo(hairX + 1.2f * scale, headY - 7 * scale);
            spike.close();
            canvas.drawPath(spike, paint);
        }

        // 发尖暗红火星
        paint.setColor(Color.argb(150, 255, 80, 30));
        for (int i = 0; i < 3; i++) {
            float sparkX = cx + (float) Math.sin(now / 150.0 + i * 2.5) * 6 * scale;
            float sparkY = headY - 14 * scale - (float) Math.cos(now / 200.0 + i * 1.8) * 4 * scale;
            canvas.drawCircle(sparkX, sparkY, 1 * scale, paint);
        }

        // 额头短角（暗红色，干涸血凝成）
        paint.setColor(Color.rgb(120, 35, 30));
        // 左角
        Path leftHorn = new Path();
        leftHorn.moveTo(cx - 5 * scale, headY - 7 * scale);
        leftHorn.lineTo(cx - 7 * scale, headY - 13 * scale);
        leftHorn.lineTo(cx - 3.5f * scale, headY - 8 * scale);
        leftHorn.close();
        canvas.drawPath(leftHorn, paint);
        // 右角
        Path rightHorn = new Path();
        rightHorn.moveTo(cx + 5 * scale, headY - 7 * scale);
        rightHorn.lineTo(cx + 7 * scale, headY - 13 * scale);
        rightHorn.lineTo(cx + 3.5f * scale, headY - 8 * scale);
        rightHorn.close();
        canvas.drawPath(rightHorn, paint);

        // 血红双眼（发光）
        float eyeGlow = 0.7f + 0.3f * (float) Math.sin(now / 200.0);
        int eyeAlpha = isEnraged ? 255 : (int) (200 * eyeGlow);
        paint.setColor(Color.argb(eyeAlpha, 255, 30, 20));
        canvas.drawCircle(cx - 3.5f * scale, headY - 2 * scale, 2 * scale, paint);
        canvas.drawCircle(cx + 3.5f * scale, headY - 2 * scale, 2 * scale, paint);
        // 眼睛光晕
        paint.setColor(Color.argb(60, 255, 50, 30));
        canvas.drawCircle(cx - 3.5f * scale, headY - 2 * scale, 3.5f * scale, paint);
        canvas.drawCircle(cx + 3.5f * scale, headY - 2 * scale, 3.5f * scale, paint);

        // 咧嘴黑牙
        paint.setColor(Color.rgb(15, 10, 12));
        Path mouth = new Path();
        mouth.moveTo(cx - 5 * scale, headY + 2 * scale);
        mouth.quadTo(cx, headY + 6 * scale, cx + 5 * scale, headY + 2 * scale);
        mouth.quadTo(cx, headY + 4 * scale, cx - 5 * scale, headY + 2 * scale);
        mouth.close();
        canvas.drawPath(mouth, paint);

        // 参差黑牙
        paint.setColor(Color.rgb(30, 25, 28));
        for (int i = 0; i < 5; i++) {
            float toothX = cx + (i - 2) * 2.2f * scale;
            float toothH = (1.5f + (i % 2) * 1.2f) * scale;
            Path tooth = new Path();
            tooth.moveTo(toothX - 0.8f * scale, headY + 2.5f * scale);
            tooth.lineTo(toothX, headY + 2.5f * scale + toothH);
            tooth.lineTo(toothX + 0.8f * scale, headY + 2.5f * scale);
            tooth.close();
            canvas.drawPath(tooth, paint);
        }
    }

    private void drawScorchedFootprints(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 脚下焦黑印记（短暂）
        paint.setColor(Color.argb(50, 20, 15, 10));
        float offset = (float) Math.sin(now / 200.0) * 3 * scale;
        canvas.drawOval(cx - 5 * scale + offset, cy + 14 * scale,
                cx - 2 * scale + offset, cy + 16 * scale, paint);
        canvas.drawOval(cx + 2 * scale - offset, cy + 14 * scale,
                cx + 5 * scale - offset, cy + 16 * scale, paint);
    }

    private void drawChargeWindUp(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float progress = getWindUpProgress();
        // 蓄力: 身体后仰 + 全身裂纹爆亮
        paint.setColor(Color.argb((int) (progress * 150), 255, 60, 20));
        paint.setStrokeWidth(2 * scale * progress);
        paint.setStyle(Paint.Style.STROKE);

        // 爆裂纹
        for (int i = 0; i < 8; i++) {
            float angle = i * (float) Math.PI / 4 + progress * 2;
            float r = 12 * scale * progress;
            canvas.drawLine(cx, cy,
                    cx + (float) Math.cos(angle) * r,
                    cy + (float) Math.sin(angle) * r, paint);
        }

        // 红色蓄力圈
        paint.setColor(Color.argb((int) (progress * 80), 255, 30, 20));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, size * 0.5f * progress, paint);
    }
}

package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 鬼将 · 幽冥战将 - 地狱迷宫精英级重装突击怪
 * 生前为沙场猛将，战死后亡魂被阎罗收编为冥界战将，
 * 披挂阴铁战甲、手持幽冥长戟，镇压地狱中不安分的亡魂。
 *
 * 视觉: 封闭式兜鍪（暗铁色），面甲下透出幽蓝双目光焰，盔顶折断翎羽；
 *       黑蓝色阴铁重甲，兽头吞肩，甲缝渗出幽蓝鬼火；
 *       破烂暗红战袍从肩后垂下；右手持幽冥长戟，左手持碎裂圆盾；
 *       下半身化为翻涌黑蓝战雾（飘行）。
 *
 * 攻击:
 *  1. 长戟横扫 - ARC 近战，扇形120°，范围200px，命中击退
 *  2. 冲锋突刺 - CHARGE，直线冲击500px，命中眩晕0.8秒
 *  3. 跳劈震地 - LEAP_SLAM，跃起猛刺地面，250px AOE，命中定身1秒
 *  4. 幽冥戟光 - 远程法术（通用火球），蓝黑色能量弹
 *  5. 战将之怒 - 被动，HP<40%时攻速+50%、移速+30%、鬼火变赤红
 *
 * 属性: 全属性极高，接近层间小BOSS水平
 * 等级: LEADER 50% / ELITE 40% / BOSS 10%（不出NORMAL）
 * 出没: 第2~4层地狱迷宫（低概率精英刷新）
 */
public class GhostGeneral extends Enemy {

    // 战将之怒
    private boolean enraged = false;
    private int baseSpeed;
    private long baseAttackCooldown;

    // 视觉动画
    private float capePhase = 0f;
    private float floatPhase = (float) (Math.random() * Math.PI * 2);
    private float eyeFlamePhase = 0f;
    private float mistPhase = 0f;
    private float halberdGlowPhase = 0f;

    public GhostGeneral(float x, float y) {
        super(x, y, 100);
        attackCooldown = 1500;
        setAttackShape(AttackShape.ARC);      // 长戟横扫 - 扇形
        addAvailableAttackType(AttackType.MELEE);
        addAvailableAttackType(AttackType.CHARGE);
        addAvailableAttackType(AttackType.LEAP_SLAM);
        windUpDuration = 400;

        // CHARGE 参数
        chargeSpeedMultiplier = 4.5f;  // 冲锋速度×4.5（约1260px/s）

        // LEAP_SLAM 参数
        slamLeapSpeed = 1800f;         // 跳跃速度极快
        slamLandRange = 250f;          // 落地AOE范围250px

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 520;
        prop.attackRange = 200;        // 长戟攻击范围远
        prop.rewardExp = 900;
        prop.rewardMoney = 500;
        setPropertyExtra(prop);

        // 全属性极高
        setProperty(3000, 1000, 800, 350, 700);

        // 强制等级分布（不出NORMAL）
        forcedLevelReset();

        // 缓存基础值
        baseSpeed = speed;
        baseAttackCooldown = attackCooldown;
    }

    /**
     * 强制等级分布: LEADER 50% / ELITE 40% / BOSS 10%
     */
    private void forcedLevelReset() {
        double roll = Math.random();
        if (roll < 0.10) {
            // BOSS 10%
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 40, attackDamage * 5, defense * 6, speed * 6, mana * 5);
        } else if (roll < 0.50) {
            // ELITE 40%
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 8, attackDamage * 3, defense * 4, speed * 4, mana * 3);
        } else {
            // LEADER 50%
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, (int) (attackDamage * 1.5), (int) (defense * 2), (int) (speed * 2), (int) (mana * 1.5));
        }
        // 更新缓存
        baseSpeed = speed;
        baseAttackCooldown = attackCooldown;
    }

    @Override
    public boolean usesGenericFireball() {
        return true; // 幽冥戟光（远程能量弹）
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;

        capePhase += 0.003f * deltaTime;
        floatPhase += 0.0025f * deltaTime;
        eyeFlamePhase += 0.005f * deltaTime;
        mistPhase += 0.004f * deltaTime;
        halberdGlowPhase += 0.003f * deltaTime;

        // 战将之怒：HP < 40% 时进入狂化
        float hpRatio = (float) health / maxHealth;
        if (!enraged && hpRatio <= 0.4f) {
            enraged = true;
            attackCooldown = (long) (baseAttackCooldown * 0.5f);  // 攻速+50%
            speed = (int) (baseSpeed * 1.3f);                      // 移速+30%
        } else if (enraged && hpRatio > 0.4f) {
            // 理论上不会发生（HP不会回复到40%以上），但保险起见
            enraged = false;
            attackCooldown = baseAttackCooldown;
            speed = baseSpeed;
        }
    }

    public boolean isEnraged() {
        return enraged;
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
                float intensity = (1f - elapsed / 300f) * 4 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        // 飘行浮动
        float floatY = (float) Math.sin(floatPhase) * 3 * scale;
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY + floatY;
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // 鬼火颜色（正常幽蓝，狂化赤红）
        int fireR = enraged ? 255 : 80;
        int fireG = enraged ? 60 : 180;
        int fireB = enraged ? 40 : 255;

        // 1. 战雾（下半身）
        drawWarMist(canvas, paint, cx, cy, scale, now, fireR, fireG, fireB);
        // 2. 披风
        drawCape(canvas, paint, cx, cy, scale, now);
        // 3. 战甲身体
        drawArmor(canvas, paint, cx, cy, scale, now, fireR, fireG, fireB);
        // 4. 头部（兜鍪 + 面甲 + 光焰双目）
        drawHelm(canvas, paint, cx, cy, scale, now, fireR, fireG, fireB);
        // 5. 武器（长戟 + 碎盾）
        drawWeapons(canvas, paint, cx, cy, scale, now, dir, fireR, fireG, fireB);
        // 6. 肩甲兽头
        drawShoulderGuards(canvas, paint, cx, cy, scale, dir);
        // 7. 攻击特效
        if (currentState == State.ATTACKING && isWindingUp) {
            drawHalberdSlashEffect(canvas, paint, cx, cy, scale, dir);
        }
        // 8. 冲锋残影
        if (isCharging) {
            drawChargeTrail(canvas, paint, cx, cy, scale, fireR, fireG, fireB);
        }
        // 9. 跳跃滞空
        if (isSlamLeaping) {
            drawLeapEffect(canvas, paint, cx, cy, scale);
        }
        // 10. 狂化光环
        if (enraged) {
            drawEnrageAura(canvas, paint, cx, cy, scale, now);
        }
        // 11. ELITE+杀气光环
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            drawKillingIntentAura(canvas, paint, cx, cy, scale, now, fireR, fireG, fireB);
        }
    }

    private void drawWarMist(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int fr, int fg, int fb) {
        // 下半身黑蓝战雾（翻涌）+ 偶尔铁蹄虚影
        for (int i = 0; i < 5; i++) {
            float angle = mistPhase + i * (float) (Math.PI * 2 / 5);
            float mx = cx + (float) Math.cos(angle) * 14 * scale;
            float my = cy + 18 * scale + (float) Math.sin(angle * 1.3f) * 4 * scale;
            float pulse = 0.4f + 0.3f * (float) Math.sin(now / 500.0 + i * 1.2f);

            // 黑蓝雾团
            paint.setColor(Color.argb((int) (120 * pulse), 20, 30, 60));
            canvas.drawCircle(mx, my, 9 * scale, paint);
            paint.setColor(Color.argb((int) (80 * pulse), fr / 3, fg / 3, fb / 3));
            canvas.drawCircle(mx, my, 5 * scale, paint);
        }

        // 铁蹄虚影（偶尔闪过）
        float hoofT = (now % 3000) / 3000f;
        if (hoofT < 0.15f) {
            float alpha = (1 - hoofT / 0.15f) * 100;
            paint.setColor(Color.argb((int) alpha, 100, 110, 130));
            float hx = cx + (float) Math.sin(hoofT * 20) * 8 * scale;
            canvas.drawOval(hx - 3 * scale, cy + 22 * scale,
                    hx + 3 * scale, cy + 26 * scale, paint);
        }
    }

    private void drawCape(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 破烂暗红战袍（从肩后垂下，下摆撕裂如旗）
        float wave = (float) Math.sin(capePhase) * 3 * scale;
        float wave2 = (float) Math.sin(capePhase * 1.3f + 1) * 2 * scale;

        paint.setColor(Color.argb(220, 100, 20, 25));
        Path cape = new Path();
        cape.moveTo(cx - 10 * scale, cy - 10 * scale);
        cape.lineTo(cx + 10 * scale, cy - 10 * scale);
        cape.lineTo(cx + 13 * scale + wave, cy + 8 * scale);
        // 撕裂下摆（锯齿旗状）
        cape.lineTo(cx + 10 * scale + wave2, cy + 16 * scale);
        cape.lineTo(cx + 6 * scale + wave, cy + 10 * scale);
        cape.lineTo(cx + 2 * scale + wave2, cy + 18 * scale);
        cape.lineTo(cx - 2 * scale + wave, cy + 11 * scale);
        cape.lineTo(cx - 6 * scale + wave2, cy + 17 * scale);
        cape.lineTo(cx - 10 * scale + wave, cy + 9 * scale);
        cape.lineTo(cx - 13 * scale + wave2, cy + 15 * scale);
        cape.close();
        canvas.drawPath(cape, paint);

        // 战袍金色边缘（残存）
        paint.setColor(Color.argb(140, 180, 140, 50));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 10 * scale, cy - 10 * scale, cx - 13 * scale + wave2, cy + 15 * scale, paint);
        canvas.drawLine(cx + 10 * scale, cy - 10 * scale, cx + 13 * scale + wave, cy + 8 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawArmor(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int fr, int fg, int fb) {
        // 黑蓝阴铁重甲（胸甲 + 腹甲）
        paint.setColor(Color.argb(250, 35, 45, 65));
        Path chest = new Path();
        chest.moveTo(cx - 11 * scale, cy - 10 * scale);
        chest.lineTo(cx + 11 * scale, cy - 10 * scale);
        chest.lineTo(cx + 12 * scale, cy + 2 * scale);
        chest.lineTo(cx + 9 * scale, cy + 12 * scale);
        chest.lineTo(cx - 9 * scale, cy + 12 * scale);
        chest.lineTo(cx - 12 * scale, cy + 2 * scale);
        chest.close();
        canvas.drawPath(chest, paint);

        // 甲片纹理（横向3道）
        paint.setColor(Color.argb(180, 50, 60, 85));
        for (int i = 0; i < 3; i++) {
            float ly = cy + (-6 + i * 6) * scale;
            canvas.drawRect(cx - 10 * scale, ly, cx + 10 * scale, ly + 1.5f * scale, paint);
        }

        // 甲缝鬼火（3处渗出）
        float glow = 0.6f + 0.4f * (float) Math.sin(now / 400.0);
        paint.setColor(Color.argb((int) (180 * glow), fr, fg, fb));
        canvas.drawCircle(cx - 6 * scale, cy - 4 * scale, 1.5f * scale, paint);
        canvas.drawCircle(cx + 7 * scale, cy + 1 * scale, 1.2f * scale, paint);
        canvas.drawCircle(cx, cy + 8 * scale, 1.3f * scale, paint);
        // 外晕
        paint.setColor(Color.argb((int) (60 * glow), fr, fg, fb));
        canvas.drawCircle(cx - 6 * scale, cy - 4 * scale, 4 * scale, paint);
        canvas.drawCircle(cx + 7 * scale, cy + 1 * scale, 3.5f * scale, paint);
        canvas.drawCircle(cx, cy + 8 * scale, 3.5f * scale, paint);

        // 腰带（铁扣）
        paint.setColor(Color.argb(240, 60, 55, 45));
        canvas.drawRect(cx - 10 * scale, cy + 10 * scale, cx + 10 * scale, cy + 13 * scale, paint);
        paint.setColor(Color.argb(220, 160, 130, 50));
        canvas.drawRect(cx - 2 * scale, cy + 10 * scale, cx + 2 * scale, cy + 13 * scale, paint);
    }

    private void drawHelm(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int fr, int fg, int fb) {
        float headY = cy - 20 * scale;

        // 兜鍪主体（暗铁色圆顶）
        paint.setColor(Color.argb(250, 45, 50, 65));
        canvas.drawOval(cx - 8 * scale, headY - 9 * scale,
                cx + 8 * scale, headY + 7 * scale, paint);

        // 面甲（V形缝隙）
        paint.setColor(Color.argb(250, 25, 30, 40));
        Path visor = new Path();
        visor.moveTo(cx - 6 * scale, headY - 2 * scale);
        visor.lineTo(cx, headY + 2 * scale);
        visor.lineTo(cx + 6 * scale, headY - 2 * scale);
        visor.lineTo(cx + 5 * scale, headY + 4 * scale);
        visor.lineTo(cx, headY + 6 * scale);
        visor.lineTo(cx - 5 * scale, headY + 4 * scale);
        visor.close();
        canvas.drawPath(visor, paint);

        // 双目光焰（从面甲缝隙透出）
        float flare = 0.7f + 0.3f * (float) Math.sin(eyeFlamePhase);
        paint.setColor(Color.argb((int) (255 * flare), fr, fg, fb));
        canvas.drawCircle(cx - 3 * scale, headY, 1.8f * scale, paint);
        canvas.drawCircle(cx + 3 * scale, headY, 1.8f * scale, paint);
        // 外焰
        paint.setColor(Color.argb((int) (100 * flare), fr, fg, fb));
        canvas.drawCircle(cx - 3 * scale, headY, 4 * scale, paint);
        canvas.drawCircle(cx + 3 * scale, headY, 4 * scale, paint);
        // 火焰上窜
        paint.setColor(Color.argb((int) (140 * flare), fr, fg, fb));
        Path flame1 = new Path();
        flame1.moveTo(cx - 4 * scale, headY - 1 * scale);
        flame1.quadTo(cx - 3 * scale, headY - 6 * scale - flare * 3 * scale,
                cx - 2 * scale, headY - 2 * scale);
        canvas.drawPath(flame1, paint);
        Path flame2 = new Path();
        flame2.moveTo(cx + 2 * scale, headY - 1 * scale);
        flame2.quadTo(cx + 3 * scale, headY - 6 * scale - flare * 3 * scale,
                cx + 4 * scale, headY - 2 * scale);
        canvas.drawPath(flame2, paint);

        // 盔顶折断翎羽
        paint.setColor(Color.argb(200, 140, 30, 30));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        Path plume = new Path();
        plume.moveTo(cx, headY - 9 * scale);
        plume.quadTo(cx + 4 * scale, headY - 16 * scale,
                cx + 2 * scale, headY - 18 * scale); // 折断
        canvas.drawPath(plume, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 狂化时面甲碎裂露出骷髅
        if (enraged) {
            paint.setColor(Color.argb(200, 220, 210, 200));
            // 骷髅面孔轮廓
            canvas.drawOval(cx - 4 * scale, headY - 1 * scale,
                    cx + 4 * scale, headY + 5 * scale, paint);
            // 黑色眼洞
            paint.setColor(Color.argb(240, 10, 5, 5));
            canvas.drawCircle(cx - 2 * scale, headY + 1 * scale, 1.5f * scale, paint);
            canvas.drawCircle(cx + 2 * scale, headY + 1 * scale, 1.5f * scale, paint);
            // 裂缝
            paint.setColor(Color.argb(180, 80, 80, 90));
            paint.setStrokeWidth(0.8f * scale);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(cx - 6 * scale, headY - 3 * scale, cx - 2 * scale, headY + 1 * scale, paint);
            canvas.drawLine(cx + 5 * scale, headY - 4 * scale, cx + 3 * scale, headY, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }

    private void drawWeapons(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir, int fr, int fg, int fb) {
        // 右手：幽冥长戟
        float halberdAngle = 0;
        if (currentState == State.ATTACKING && isWindingUp) {
            halberdAngle = -getWindUpProgress() * 1.2f * dir;
        } else if (isCharging) {
            halberdAngle = -0.3f * dir; // 冲锋时戟前指
        }

        canvas.save();
        canvas.translate(cx + dir * 14 * scale, cy - 5 * scale);
        canvas.rotate(halberdAngle);

        // 戟杆（深铁色）
        paint.setColor(Color.argb(250, 60, 55, 50));
        paint.setStrokeWidth(2.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(0, -20 * scale, 0, 22 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 戟刃（冷光月牙形）
        paint.setColor(Color.argb(250, 180, 195, 210));
        Path blade = new Path();
        blade.moveTo(0, -20 * scale);
        blade.quadTo(dir * 8 * scale, -24 * scale, dir * 10 * scale, -18 * scale);
        blade.quadTo(dir * 6 * scale, -16 * scale, 0, -14 * scale);
        blade.close();
        canvas.drawPath(blade, paint);
        // 刃光
        float bladeGlow = 0.5f + 0.5f * (float) Math.sin(halberdGlowPhase);
        paint.setColor(Color.argb((int) (120 * bladeGlow), fr, fg, fb));
        canvas.drawPath(blade, paint);

        // 杆身缠绕锁链碎片
        paint.setColor(Color.argb(200, 90, 80, 65));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 3; i++) {
            float ly = (-8 + i * 10) * scale;
            canvas.drawLine(-1.5f * scale, ly, 1.5f * scale, ly + 4 * scale, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        canvas.restore();

        // 左手：碎裂圆盾
        float shieldX = cx - dir * 13 * scale;
        float shieldY = cy - 2 * scale;
        paint.setColor(Color.argb(240, 50, 55, 70));
        canvas.drawCircle(shieldX, shieldY, 7 * scale, paint);
        // 盾面纹（十字）
        paint.setColor(Color.argb(180, 80, 90, 110));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(shieldX - 4 * scale, shieldY, shieldX + 4 * scale, shieldY, paint);
        canvas.drawLine(shieldX, shieldY - 4 * scale, shieldX, shieldY + 4 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        // 裂痕
        paint.setColor(Color.argb(200, 30, 30, 35));
        paint.setStrokeWidth(1 * scale);
        paint.setStyle(Paint.Style.STROKE);
        Path crack = new Path();
        crack.moveTo(shieldX - 5 * scale, shieldY - 3 * scale);
        crack.lineTo(shieldX - 1 * scale, shieldY + 1 * scale);
        crack.lineTo(shieldX + 3 * scale, shieldY + 5 * scale);
        canvas.drawPath(crack, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawShoulderGuards(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 兽头吞肩（左右各一）
        for (int s = -1; s <= 1; s += 2) {
            float sx = cx + s * 13 * scale;
            float sy = cy - 10 * scale;

            // 肩甲主体
            paint.setColor(Color.argb(250, 40, 48, 62));
            Path guard = new Path();
            guard.moveTo(sx - 5 * scale, sy - 2 * scale);
            guard.quadTo(sx, sy - 7 * scale, sx + 5 * scale, sy - 2 * scale);
            guard.lineTo(sx + 4 * scale, sy + 4 * scale);
            guard.lineTo(sx - 4 * scale, sy + 4 * scale);
            guard.close();
            canvas.drawPath(guard, paint);

            // 兽头（虎/蛟 - 简化为圆眼+尖牙）
            paint.setColor(Color.argb(230, 160, 130, 50));
            canvas.drawCircle(sx - 1.5f * scale, sy - 2 * scale, 1.2f * scale, paint);
            canvas.drawCircle(sx + 1.5f * scale, sy - 2 * scale, 1.2f * scale, paint);
            // 尖牙
            paint.setColor(Color.argb(240, 200, 195, 180));
            Path fang = new Path();
            fang.moveTo(sx - 2 * scale, sy + 1 * scale);
            fang.lineTo(sx - 1 * scale, sy + 4 * scale);
            fang.lineTo(sx, sy + 1 * scale);
            fang.close();
            canvas.drawPath(fang, paint);
            Path fang2 = new Path();
            fang2.moveTo(sx, sy + 1 * scale);
            fang2.lineTo(sx + 1 * scale, sy + 4 * scale);
            fang2.lineTo(sx + 2 * scale, sy + 1 * scale);
            fang2.close();
            canvas.drawPath(fang2, paint);
        }
    }

    private void drawHalberdSlashEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 长戟横扫：扇形弧光
        float progress = getWindUpProgress();
        float arcRadius = propertyExtra.attackRange * progress;
        float angle = dir > 0 ? 0 : (float) Math.PI;

        // 弧形斩击轨迹
        paint.setColor(Color.argb((int) (progress * 100), 150, 200, 255));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                (float) Math.toDegrees(angle) - 60, 120, true, paint);

        // 3道戟光弧线
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 3; i++) {
            float r = arcRadius * (0.6f + i * 0.2f);
            paint.setStrokeWidth((3 - i) * scale * progress);
            paint.setColor(Color.argb((int) (progress * (220 - i * 60)), 180, 220, 255));
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r,
                    (float) Math.toDegrees(angle) - 55 + i * 5, 110 - i * 10, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawChargeTrail(Canvas canvas, Paint paint, float cx, float cy, float scale, int fr, int fg, int fb) {
        // 冲锋残影：身后拖出3道半透明残影
        for (int i = 1; i <= 3; i++) {
            float alpha = (4 - i) * 40;
            float trailX = cx - chargeDirectionX * i * 20 * scale;
            float trailY = cy - chargeDirectionY * i * 20 * scale;
            paint.setColor(Color.argb((int) alpha, fr, fg, fb));
            canvas.drawOval(trailX - 8 * scale, trailY - 14 * scale,
                    trailX + 8 * scale, trailY + 10 * scale, paint);
        }
    }

    private void drawLeapEffect(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        // 跳跃滞空：脚下收缩阴影 + 周身气流
        paint.setColor(Color.argb(80, 20, 25, 40));
        canvas.drawOval(cx - 12 * scale, cy + 20 * scale,
                cx + 12 * scale, cy + 26 * scale, paint);

        // 上升气流线
        paint.setColor(Color.argb(120, 100, 150, 220));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 4; i++) {
            float lx = cx + (i - 1.5f) * 6 * scale;
            canvas.drawLine(lx, cy + 15 * scale, lx, cy + 25 * scale, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawEnrageAura(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 战将之怒：赤红色火焰环绕全身
        float pulse = 0.6f + 0.4f * (float) Math.sin(now / 250.0);
        paint.setColor(Color.argb((int) (90 * pulse), 255, 50, 30));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3 * scale);
        canvas.drawCircle(cx, cy, 22 * scale, paint);

        // 上升火焰粒子
        for (int i = 0; i < 6; i++) {
            float t = ((now / 700f + i * 0.166f) % 1f);
            float angle = (float) (i * Math.PI / 3 + now / 500.0);
            float px = cx + (float) Math.cos(angle) * 16 * scale;
            float py = cy + 10 * scale - t * 30 * scale;
            paint.setColor(Color.argb((int) (200 * (1 - t)), 255, 80 - (int)(t * 50), 20));
            canvas.drawCircle(px, py, 2 * scale * (1 - t * 0.6f), paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawKillingIntentAura(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int fr, int fg, int fb) {
        // ELITE+杀气光环：淡蓝色斗气外溢
        float pulse = 0.4f + 0.3f * (float) Math.sin(now / 600.0);
        paint.setColor(Color.argb((int) (50 * pulse), fr, fg, fb));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * scale);
        canvas.drawCircle(cx, cy, 26 * scale, paint);

        paint.setColor(Color.argb((int) (30 * pulse), fr, fg, fb));
        paint.setStrokeWidth(1 * scale);
        canvas.drawCircle(cx, cy, 32 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

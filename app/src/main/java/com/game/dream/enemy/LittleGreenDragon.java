package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.EquipCreator;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;

import java.util.List;

/**
 * 小青龙 - 东海龙族的幼龙，远程法师型怪物
 * 特点: 远程法术攻击、保持距离游斗、龙威光环（减速玩家）
 * 视觉: 翠绿蛇形龙身、鹿角、鬃毛、四小爪、龙须、鳞片光泽
 */
public class LittleGreenDragon extends Enemy {

    // 龙威光环参数
    private static final float DRAGON_AURA_RANGE = 250; // 龙威范围
    private static final long AURA_SLOW_DURATION = 500; // 减速持续 500ms（持续刷新）
    private long lastAuraApplyTime = 0;

    // 法术冷却
    private long lastSpellTime = 0;
    private boolean pendingWaterBolt = false;
    private boolean pendingLightning = false;

    // 理想距离（保持这个距离输出）
    private static final float PREFERRED_DISTANCE = 300;
    private static final float MIN_DISTANCE = 180;

    public LittleGreenDragon(float x, float y) {
        super(x, y, 120);
        attackCooldown = 2500;
        setAttackShape(AttackShape.CIRCLE);

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 600; // 感知范围大（远程怪）
        prop.attackRange = 400;    // 攻击范围远
        prop.rewardExp = 1200;
        prop.rewardMoney = 600;
        setPropertyExtra(prop);

        // 小青龙：血量低、防御低、速度中等、魔法伤害高
        setProperty(1200, 200, 200, 150, 400);

        // 等级分布
        if (Math.random() < 0.02) {
            // BOSS - 东海龙子
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 50, attackDamage * 6, defense * 5, speed * 7, mana * 8);
        } else if (Math.random() < 0.07) {
            // 精英 - 青龙太子
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 10, attackDamage * 4, defense * 3, speed * 4, mana * 5);
        } else if (Math.random() < 0.30) {
            // 首领 - 青龙侍从
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, attackDamage * 2, defense * 2, speed * 2, mana * 3);
        }

        // 近战时使用环绕斩击（龙尾横扫）作为最后的挣扎
        addAvailableAttackType(AttackType.SPIN_ATTACK);
        spinDuration = 1200;
        spinHitInterval = 400;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        // 先调用父类 update 处理 CC/灼烧/AI 状态机
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive() || isStunned() || isFrozen()) return;

        long now = System.currentTimeMillis();

        // === 龙威光环：持续减速附近玩家 ===
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < DRAGON_AURA_RANGE && now - lastAuraApplyTime > 300) {
            // 通知 GameEngine 对玩家施加减速
            pendingWaterBolt = false; // 不冲突
            lastAuraApplyTime = now;
        }

        // === 远程法术攻击 ===
        if (currentState == State.CHASING || currentState == State.IDLE) {
            // 水龙弹冷却
            long waterBoltCd = (enemyLevel == EnemyLevel.BOSS) ? 2000 : 
                               (enemyLevel == EnemyLevel.ELITE) ? 2500 : 3500;
            if (dist < propertyExtra.detectionRange && now - lastSpellTime > waterBoltCd) {
                pendingWaterBolt = true;
                lastSpellTime = now;
            }

            // 闪电（精英以上）
            if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
                long lightningCd = 4000;
                if (dist < propertyExtra.detectionRange * 0.8f && now - lastSpellTime > lightningCd) {
                    pendingLightning = true;
                    lastSpellTime = now;
                }
            }
        }
    }

    /**
     * 覆盖追击行为：保持距离而非靠近玩家
     */
    @Override
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (isRooted()) return;

        if (dist < MIN_DISTANCE) {
            // 太近了，后退拉开距离
            targetX = x - dx / dist * 200;
            targetY = y - dy / dist * 200;
        } else if (dist > PREFERRED_DISTANCE * 1.5f) {
            // 太远了，适当靠近
            targetX = playerX;
            targetY = playerY;
        } else {
            // 理想距离，横向游动（绕圈）
            float perpX = -dy / dist;
            float perpY = dx / dist;
            float circleDir = (Math.sin(System.currentTimeMillis() / 2000.0) > 0) ? 1 : -1;
            targetX = x + perpX * 100 * circleDir;
            targetY = y + perpY * 100 * circleDir;
        }

        float chaseSpeed = speed * 1.1f;
        moveToTargetWithSpeed(deltaSeconds, chaseSpeed);
    }

    /**
     * 检查是否有待处理的水龙弹
     */
    public boolean isPendingWaterBolt() {
        return pendingWaterBolt;
    }

    public void consumeWaterBolt() {
        pendingWaterBolt = false;
    }

    /**
     * 检查是否有待处理的闪电
     */
    public boolean isPendingLightning() {
        return pendingLightning;
    }

    public void consumeLightning() {
        pendingLightning = false;
    }

    /**
     * 获取龙威光环范围
     */
    public float getDragonAuraRange() {
        return DRAGON_AURA_RANGE;
    }

    /**
     * 获取龙威减速持续时长
     */
    public long getAuraSlowDuration() {
        return AURA_SLOW_DURATION;
    }

    @Override
    public List<Item> getPossibleDropList() {
        possibleDrops.clear();

        if (enemyLevel == EnemyLevel.BOSS) {
            addPossibleDrop(EquipCreator.createEquip(60, null));
            addPossibleDrop(EquipCreator.createEquip(70, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(60, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(70, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(60));
            addPossibleDrop(ItemCreator.createBuildEquipIron(70));
            addPossibleDrop(ItemCreator.createGain_hp_3());
            addPossibleDrop(ItemCreator.createGain_attack_3());
            addPossibleDrop(ItemCreator.createGain_defense_3());
            addPossibleDrop(ItemCreator.createGain_mana_3());
            addPossibleDrop(ItemCreator.createGain_speed_3());
            addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
        } else if (enemyLevel == EnemyLevel.ELITE) {
            addPossibleDrop(EquipCreator.createEquip(50, null));
            addPossibleDrop(EquipCreator.createEquip(60, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(60, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            addPossibleDrop(ItemCreator.createBuildEquipIron(60));
            addPossibleDrop(ItemCreator.createGain_hp_2());
            addPossibleDrop(ItemCreator.createGain_attack_2());
            addPossibleDrop(ItemCreator.createGain_defense_2());
            addPossibleDrop(ItemCreator.createGain_mana_2());
            addPossibleDrop(ItemCreator.createGain_speed_2());
            addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
        } else if (enemyLevel == EnemyLevel.LEADER) {
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(EquipCreator.createEquip(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        } else {
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            addPossibleDrop(ItemCreator.createHp1_2_QiyeLian());
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createMp1_2_Xiangye());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
        }

        return possibleDrops;
    }

    @Override
    protected void performAttack() {
        // LittleGreenDragon attack - handled by GameEngine
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        paint.setAntiAlias(true);

        float scale = size / 30.0f;

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = System.currentTimeMillis() - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 4 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY;

        boolean facingRight = targetX > x;

        if (facingRight) {
            drawDragon(canvas, paint, cx, cy, scale, facingRight);
        } else {
            drawDragon(canvas, paint, cx, cy, scale, facingRight);
        }
    }

    private void drawDragon(Canvas canvas, Paint paint, float cx, float cy, float scale, boolean facingRight) {
        long now = System.currentTimeMillis();
        boolean isAggressive = currentState == State.CHASING || currentState == State.ATTACKING;
        float dir = facingRight ? 1f : -1f;

        // === 1. 龙威光环（淡绿色半透明光圈）===
        float auraPulse = 1.0f + 0.05f * (float) Math.sin(now / 500.0);
        paint.setColor(Color.argb(20, 80, 220, 120));
        canvas.drawCircle(cx, cy + bobOffset, DRAGON_AURA_RANGE * 0.3f * auraPulse, paint);
        paint.setColor(Color.argb(10, 100, 240, 140));
        canvas.drawCircle(cx, cy + bobOffset, DRAGON_AURA_RANGE * 0.45f * auraPulse, paint);

        // === 2. 龙身（蛇形曲线身体）===
        float bodySwing = (float) Math.sin(now / 500.0) * 3 * scale;
        float bodySwing2 = (float) Math.sin(now / 500.0 + 1.5) * 3 * scale;

        // 身体分节位置（从头到尾）
        float headX = cx + dir * 10 * scale;
        float headY = cy - 8 * scale + bobOffset;

        // 身体曲线控制点
        float[] bodyX = new float[8];
        float[] bodyY = new float[8];
        for (int i = 0; i < 8; i++) {
            float t = i / 7f;
            bodyX[i] = cx + dir * (10 - t * 25) * scale + (float) Math.sin(t * Math.PI * 2 + now / 400.0) * 4 * scale;
            bodyY[i] = cy + (-8 + t * 22) * scale + bobOffset;
        }

        // 绘制龙身（粗到细）
        for (int i = 0; i < 7; i++) {
            float thickness = (6 - i * 0.7f) * scale;
            paint.setColor(Color.rgb(50 + i * 5, 170 - i * 8, 90 + i * 5));
            paint.setStrokeWidth(thickness);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(bodyX[i], bodyY[i], bodyX[i + 1], bodyY[i + 1], paint);
        }

        // 鳞片光泽（沿身体分布）
        paint.setColor(Color.argb(50, 180, 255, 200));
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 6; i++) {
            float sx = bodyX[i] + dir * 1 * scale;
            float sy = bodyY[i] - 1 * scale;
            canvas.drawCircle(sx, sy, (2.5f - i * 0.3f) * scale, paint);
        }

        // === 3. 脊背鬃毛 ===
        paint.setColor(Color.rgb(40, 140, 70));
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 5; i++) {
            float mx = bodyX[i + 1];
            float my = bodyY[i + 1];
            float maneWave = (float) Math.sin(now / 350.0 + i * 0.8) * 1.5f * scale;
            Path mane = new Path();
            mane.moveTo(mx - 2 * scale, my);
            mane.lineTo(mx + maneWave, my - 5 * scale);
            mane.lineTo(mx + 2 * scale, my);
            mane.close();
            canvas.drawPath(mane, paint);
        }

        // === 4. 四只小爪 ===
        paint.setColor(Color.rgb(55, 160, 85));
        paint.setStrokeWidth(2 * scale);
        paint.setStyle(Paint.Style.STROKE);
        // 前左爪
        float claw1X = bodyX[1] + dir * 4 * scale;
        float claw1Y = bodyY[1] + 4 * scale;
        canvas.drawLine(claw1X, claw1Y, claw1X + dir * 5 * scale, claw1Y + 5 * scale, paint);
        // 前右爪
        float claw2X = bodyX[2] - dir * 2 * scale;
        float claw2Y = bodyY[2] + 4 * scale;
        canvas.drawLine(claw2X, claw2Y, claw2X - dir * 3 * scale, claw2Y + 5 * scale, paint);
        // 后左爪
        float claw3X = bodyX[4] + dir * 3 * scale;
        float claw3Y = bodyY[4] + 3 * scale;
        canvas.drawLine(claw3X, claw3Y, claw3X + dir * 4 * scale, claw3Y + 4 * scale, paint);
        // 后右爪
        float claw4X = bodyX[5] - dir * 2 * scale;
        float claw4Y = bodyY[5] + 3 * scale;
        canvas.drawLine(claw4X, claw4Y, claw4X - dir * 3 * scale, claw4Y + 4 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // === 5. 头部 ===
        // 龙头（椭圆形，前端略尖）
        paint.setColor(Color.rgb(55, 175, 90));
        Path dragonHead = new Path();
        dragonHead.moveTo(headX - 6 * scale * dir, headY - 4 * scale);
        dragonHead.quadTo(headX + 2 * scale * dir, headY - 7 * scale,
                headX + 8 * scale * dir, headY - 3 * scale);
        dragonHead.quadTo(headX + 10 * scale * dir, headY,
                headX + 8 * scale * dir, headY + 3 * scale);
        dragonHead.quadTo(headX + 2 * scale * dir, headY + 5 * scale,
                headX - 6 * scale * dir, headY + 3 * scale);
        dragonHead.close();
        canvas.drawPath(dragonHead, paint);

        // 龙角（鹿角状，根据等级大小不同）
        float antlerSize = (enemyLevel == EnemyLevel.BOSS) ? 1.5f :
                           (enemyLevel == EnemyLevel.ELITE) ? 1.2f :
                           (enemyLevel == EnemyLevel.LEADER) ? 1.0f : 0.7f;
        paint.setColor(Color.rgb(180, 160, 100));
        paint.setStrokeWidth(1.8f * scale * antlerSize);
        paint.setStyle(Paint.Style.STROKE);
        // 左角
        float antlerBaseX = headX;
        float antlerBaseY = headY - 5 * scale;
        canvas.drawLine(antlerBaseX, antlerBaseY, antlerBaseX - 3 * scale * antlerSize, antlerBaseY - 8 * scale * antlerSize, paint);
        canvas.drawLine(antlerBaseX - 3 * scale * antlerSize, antlerBaseY - 8 * scale * antlerSize,
                antlerBaseX - 5 * scale * antlerSize, antlerBaseY - 6 * scale * antlerSize, paint);
        canvas.drawLine(antlerBaseX - 3 * scale * antlerSize, antlerBaseY - 8 * scale * antlerSize,
                antlerBaseX - 1 * scale * antlerSize, antlerBaseY - 11 * scale * antlerSize, paint);
        // 右角
        canvas.drawLine(antlerBaseX + 4 * scale * dir, antlerBaseY,
                antlerBaseX + 4 * scale * dir + 2 * scale * antlerSize, antlerBaseY - 8 * scale * antlerSize, paint);
        canvas.drawLine(antlerBaseX + 4 * scale * dir + 2 * scale * antlerSize, antlerBaseY - 8 * scale * antlerSize,
                antlerBaseX + 4 * scale * dir + 4 * scale * antlerSize, antlerBaseY - 6 * scale * antlerSize, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 龙眼（金色，攻击时泛红）
        if (isAggressive) {
            paint.setColor(Color.rgb(255, 80, 40));
        } else {
            paint.setColor(Color.rgb(255, 200, 50));
        }
        float eyeX = headX + 4 * scale * dir;
        float eyeY = headY - 2 * scale;
        canvas.drawCircle(eyeX, eyeY, 2 * scale, paint);
        // 瞳孔
        paint.setColor(Color.rgb(20, 15, 10));
        canvas.drawOval(eyeX - 0.5f * scale, eyeY - 1.5f * scale, eyeX + 0.5f * scale, eyeY + 1.5f * scale, paint);
        // 眼睛高光
        paint.setColor(Color.WHITE);
        canvas.drawCircle(eyeX + 0.5f * scale * dir, eyeY - 0.8f * scale, 0.6f * scale, paint);

        // 龙须（两根飘动）
        paint.setColor(Color.rgb(70, 190, 100));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        float whiskerWave1 = (float) Math.sin(now / 350.0) * 3 * scale;
        float whiskerWave2 = (float) Math.sin(now / 350.0 + 1.2) * 3 * scale;
        // 上须
        Path whisker1 = new Path();
        whisker1.moveTo(headX + 7 * scale * dir, headY - 2 * scale);
        whisker1.quadTo(headX + 14 * scale * dir, headY - 4 * scale + whiskerWave1,
                headX + 18 * scale * dir, headY - 2 * scale + whiskerWave1);
        canvas.drawPath(whisker1, paint);
        // 下须
        Path whisker2 = new Path();
        whisker2.moveTo(headX + 7 * scale * dir, headY + 1 * scale);
        whisker2.quadTo(headX + 13 * scale * dir, headY + 3 * scale + whiskerWave2,
                headX + 16 * scale * dir, headY + 5 * scale + whiskerWave2);
        canvas.drawPath(whisker2, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 龙口（攻击时张开吐水/雷）
        if (isAggressive && (pendingWaterBolt || pendingLightning)) {
            paint.setColor(Color.argb(150, 100, 220, 255));
            canvas.drawCircle(headX + 9 * scale * dir, headY, 3 * scale, paint);
        }

        // === 6. 尾部（渐细 + 尾鳍）===
        float tailX = bodyX[7];
        float tailY = bodyY[7];
        float tailWave = (float) Math.sin(now / 300.0) * 4 * scale;
        paint.setColor(Color.rgb(45, 155, 80));
        Path tailFin = new Path();
        tailFin.moveTo(tailX, tailY);
        tailFin.lineTo(tailX - dir * 4 * scale + tailWave, tailY - 5 * scale);
        tailFin.lineTo(tailX - dir * 2 * scale, tailY + 2 * scale);
        tailFin.lineTo(tailX - dir * 6 * scale + tailWave, tailY + 4 * scale);
        tailFin.lineTo(tailX, tailY + 1 * scale);
        tailFin.close();
        canvas.drawPath(tailFin, paint);
    }
}

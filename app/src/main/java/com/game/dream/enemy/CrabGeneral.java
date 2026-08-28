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
 * 蟹将 - 东海龙宫的蟹将，重装坦克型怪物
 * 特点: 高防御、高血量、速度慢、铁壳防御（受击概率减伤）
 * 视觉: 青蓝色甲壳、金色纹路、大螯、柄状眼
 */
public class CrabGeneral extends Enemy {

    // 铁壳防御状态
    private boolean isShellGuard = false;
    private long shellGuardEndTime = 0;
    private static final float SHELL_GUARD_CHANCE = 0.30f; // 30% 触发概率
    private static final float SHELL_GUARD_REDUCTION = 0.70f; // 减伤 70%
    private static final long SHELL_GUARD_DURATION = 1000; // 持续 1 秒

    public CrabGeneral(float x, float y) {
        super(x, y, 90);
        attackCooldown = 2500;
        setAttackShape(AttackShape.ARC); // 蟹螯夹击 - 扇形

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 300;
        prop.attackRange = 200;
        prop.rewardExp = 800;
        prop.rewardMoney = 400;
        setPropertyExtra(prop);

        // 蟹将：血量高、防御高、速度慢、攻击高
        setProperty(1000, 400, 350, 100, 200);

        // 等级分布
        if (Math.random() < 0.02) {
            // BOSS - 蟹帅
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 50, attackDamage * 8, defense * 10, speed * 6, mana * 6);
        } else if (Math.random() < 0.07) {
            // 精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 10, attackDamage * 4, defense * 5, speed * 3, mana * 3);
        } else if (Math.random() < 0.30) {
            // 首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, attackDamage * 2, defense * 3, speed * 2, mana * 2);
        }

        // 所有蟹将使用连续爪击（双螯交替夹击）
        addAvailableAttackType(AttackType.COMBO);
        comboHitCount = (enemyLevel == EnemyLevel.BOSS) ? 4 : (enemyLevel == EnemyLevel.ELITE) ? 3 : 2;
        comboHitInterval = 250; // 蟹将连击较慢

        // 首领以上可使用环绕斩击（蟹旋）
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
            spinDuration = 1800;
            spinHitInterval = 350;
        }

        // 精英以上可使用猛扑（巨螯扑击）
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.POUNCE);
            pounceFixedSpeed = 700f; // 蟹将扑击较慢
        }

        // BOSS可使用跳跃砸击（泰山压顶）
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
    }

    /**
     * 铁壳防御: 受击时 30% 概率触发，减伤 70%，持续 1 秒
     */
    @Override
    public boolean takeDamage(int damage) {
        // 铁壳防御激活时，减伤 70%
        if (isShellGuard) {
            damage = (int) (damage * (1 - SHELL_GUARD_REDUCTION));
        } else if (Math.random() < SHELL_GUARD_CHANCE) {
            // 触发铁壳防御
            isShellGuard = true;
            shellGuardEndTime = System.currentTimeMillis() + SHELL_GUARD_DURATION;
            damage = (int) (damage * (1 - SHELL_GUARD_REDUCTION));
        }

        return super.takeDamage(damage);
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        // 更新铁壳防御状态
        if (isShellGuard && System.currentTimeMillis() > shellGuardEndTime) {
            isShellGuard = false;
        }
    }

    /**
     * 检查是否处于铁壳防御状态
     */
    public boolean isShellGuard() {
        return isShellGuard;
    }

    @Override
    public List<Item> getPossibleDropList() {
        possibleDrops.clear();

        if (enemyLevel == EnemyLevel.BOSS) {
            addPossibleDrop(EquipCreator.createEquip(50, null));
            addPossibleDrop(EquipCreator.createEquip(60, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(60, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            addPossibleDrop(ItemCreator.createBuildEquipIron(60));
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
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(EquipCreator.createEquip(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            addPossibleDrop(ItemCreator.createBuildEquipIron(50));
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
            addPossibleDrop(EquipCreator.createEquip(30, null));
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        } else {
            addPossibleDrop(EquipCreator.createEquip(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            addPossibleDrop(ItemCreator.createHp1_2_QiyeLian());
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createMp1_2_Xiangye());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
        }

        return possibleDrops;
    }

    @Override
    protected void performAttack() {
        // CrabGeneral attack - handled by GameEngine
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
            drawFacingRight(canvas, paint, cx, cy, scale);
        } else {
            drawFacingLeft(canvas, paint, cx, cy, scale);
        }
    }

    private void drawFacingRight(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        long now = System.currentTimeMillis();
        boolean isAggressive = currentState == State.CHASING || currentState == State.ATTACKING;

        // 铁壳防御视觉效果：壳合拢，发出金属光泽
        boolean shellActive = isShellGuard;
        int shellAlpha = shellActive ? 255 : 220;

        // 攻击动画：螯前伸
        float clawExtend = 0;
        if (currentState == State.ATTACKING) {
            if (isWindingUp) {
                clawExtend = -getWindUpProgress() * 3 * scale;
            } else {
                float p = Math.min(1.0f, (now - getLastAttackTime()) / 250f);
                clawExtend = (1 - p) * 6 * scale;
            }
        }

        // 蟹腿摆动
        float legSwing1 = (float) Math.sin(now / 300.0) * 2 * scale;
        float legSwing2 = (float) Math.sin(now / 300.0 + Math.PI) * 2 * scale;

        // === 1. 蟹腿（8条，两侧各4条）===
        paint.setColor(Color.rgb(60, 120, 110));
        paint.setStrokeWidth(2 * scale);
        // 右侧腿
        for (int i = 0; i < 4; i++) {
            float legX = cx + (4 + i * 3) * scale;
            float legY = cy + (2 + i * 2) * scale + bobOffset;
            float swing = (i % 2 == 0) ? legSwing1 : legSwing2;
            paint.setStyle(Paint.Style.STROKE);
            Path leg = new Path();
            leg.moveTo(legX, legY);
            leg.lineTo(legX + 6 * scale, legY + 6 * scale + swing);
            leg.lineTo(legX + 8 * scale, legY + 10 * scale + swing);
            canvas.drawPath(leg, paint);
        }
        // 左侧腿
        for (int i = 0; i < 4; i++) {
            float legX = cx - (4 + i * 3) * scale;
            float legY = cy + (2 + i * 2) * scale + bobOffset;
            float swing = (i % 2 == 0) ? legSwing2 : legSwing1;
            paint.setStyle(Paint.Style.STROKE);
            Path leg = new Path();
            leg.moveTo(legX, legY);
            leg.lineTo(legX - 6 * scale, legY + 6 * scale + swing);
            leg.lineTo(legX - 8 * scale, legY + 10 * scale + swing);
            canvas.drawPath(leg, paint);
        }
        paint.setStyle(Paint.Style.FILL);

        // === 2. 蟹壳主体（椭圆形，青蓝色）===
        float shellW = 16 * scale;
        float shellH = 12 * scale;

        // 壳底色
        paint.setColor(Color.argb(shellAlpha, 50, 130, 120));
        Path shell = new Path();
        shell.addOval(cx - shellW, cy - shellH + bobOffset, cx + shellW, cy + shellH + bobOffset, Path.Direction.CW);
        canvas.drawPath(shell, paint);

        // 壳纹路（金色花纹）
        paint.setColor(Color.argb(150, 200, 170, 60));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * scale);
        // 中心圆纹
        canvas.drawCircle(cx, cy + bobOffset, 6 * scale, paint);
        // 放射纹
        for (int i = 0; i < 6; i++) {
            float angle = (float) (i * Math.PI / 3);
            float rx = (float) Math.cos(angle) * 12 * scale;
            float ry = (float) Math.sin(angle) * 8 * scale;
            canvas.drawLine(cx, cy + bobOffset, cx + rx, cy + ry + bobOffset, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 壳高光（铁壳防御时更亮）
        int highlightAlpha = shellActive ? 120 : 50;
        paint.setColor(Color.argb(highlightAlpha, 200, 230, 220));
        canvas.drawOval(cx - 8 * scale, cy - 8 * scale + bobOffset,
                cx + 2 * scale, cy - 2 * scale + bobOffset, paint);

        // 铁壳防御特效：金属光泽脉冲
        if (shellActive) {
            float pulse = (float) Math.sin(now / 150.0) * 0.3f + 0.7f;
            paint.setColor(Color.argb((int) (80 * pulse), 180, 220, 255));
            paint.setStrokeWidth(3 * scale);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawOval(cx - shellW - 2 * scale, cy - shellH - 2 * scale + bobOffset,
                    cx + shellW + 2 * scale, cy + shellH + 2 * scale + bobOffset, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }

        // === 3. 大螯（两只，一大一小）===
        // 右螯（大螯，攻击螯）
        float rightClawX = cx + shellW + clawExtend;
        float rightClawY = cy - 4 * scale + bobOffset;
        drawClaw(canvas, paint, rightClawX, rightClawY, scale, true, isAggressive, 1.2f);

        // 左螯（小螯）
        float leftClawX = cx - shellW - clawExtend * 0.5f;
        float leftClawY = cy - 2 * scale + bobOffset;
        drawClaw(canvas, paint, leftClawX, leftClawY, scale, false, isAggressive, 0.9f);

        // === 4. 柄状眼（两只竖起的眼睛）===
        float eyeBaseY = cy - shellH + bobOffset;
        // 右眼柄
        paint.setColor(Color.rgb(60, 130, 120));
        paint.setStrokeWidth(2.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx + 5 * scale, eyeBaseY, cx + 7 * scale, eyeBaseY - 8 * scale, paint);
        // 左眼柄
        canvas.drawLine(cx - 5 * scale, eyeBaseY, cx - 7 * scale, eyeBaseY - 8 * scale, paint);
        paint.setStyle(Paint.Style.FILL);

        // 眼球
        if (isAggressive) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.rgb(30, 20, 15));
        }
        canvas.drawCircle(cx + 7 * scale, eyeBaseY - 9 * scale, 2 * scale, paint);
        canvas.drawCircle(cx - 7 * scale, eyeBaseY - 9 * scale, 2 * scale, paint);
        // 眼睛高光
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx + 6.5f * scale, eyeBaseY - 9.5f * scale, 0.7f * scale, paint);
        canvas.drawCircle(cx - 7.5f * scale, eyeBaseY - 9.5f * scale, 0.7f * scale, paint);

        // === 5. 嘴部（小圆口器）===
        paint.setColor(Color.rgb(40, 80, 75));
        canvas.drawCircle(cx + 12 * scale, cy + 2 * scale + bobOffset, 1.5f * scale, paint);

        // === 6. 攻击特效（蓄力时水泡）===
        if (currentState == State.ATTACKING && isWindingUp) {
            float progress = getWindUpProgress();
            paint.setColor(Color.argb((int) (progress * 100), 80, 180, 220));
            paint.setStrokeWidth(2.5f * scale);
            paint.setStyle(Paint.Style.STROKE);
            float arcRadius = 14 * scale * progress;
            canvas.drawCircle(cx, cy + bobOffset, arcRadius, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }

    /**
     * 绘制蟹螯
     */
    private void drawClaw(Canvas canvas, Paint paint, float clawX, float clawY, float scale,
                          boolean isLarge, boolean isAggressive, float sizeMultiplier) {
        float clawSize = isLarge ? 8 * scale * sizeMultiplier : 6 * scale * sizeMultiplier;

        // 螯臂
        paint.setColor(Color.rgb(70, 140, 130));
        paint.setStrokeWidth(3 * scale * sizeMultiplier);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(clawX - 4 * scale, clawY + 2 * scale, clawX, clawY, paint);
        paint.setStyle(Paint.Style.FILL);

        // 螯主体（椭圆形）
        paint.setColor(Color.rgb(55, 125, 115));
        canvas.drawOval(clawX - clawSize, clawY - clawSize * 0.6f,
                clawX + clawSize, clawY + clawSize * 0.6f, paint);

        // 螯尖（两个三角形，攻击时张开）
        float openAngle = isAggressive ? 0.4f : 0.15f;
        paint.setColor(Color.rgb(45, 110, 100));
        // 上螯尖
        Path upperPincer = new Path();
        upperPincer.moveTo(clawX + clawSize * 0.5f, clawY);
        upperPincer.lineTo(clawX + clawSize * 1.3f, clawY - clawSize * openAngle);
        upperPincer.lineTo(clawX + clawSize * 0.8f, clawY + clawSize * 0.1f);
        upperPincer.close();
        canvas.drawPath(upperPincer, paint);
        // 下螯尖
        Path lowerPincer = new Path();
        lowerPincer.moveTo(clawX + clawSize * 0.5f, clawY);
        lowerPincer.lineTo(clawX + clawSize * 1.3f, clawY + clawSize * openAngle);
        lowerPincer.lineTo(clawX + clawSize * 0.8f, clawY - clawSize * 0.1f);
        lowerPincer.close();
        canvas.drawPath(lowerPincer, paint);

        // 螯纹路
        paint.setColor(Color.argb(100, 200, 170, 60));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(clawX, clawY, clawSize * 0.4f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        canvas.save();
        canvas.scale(-1, 1, cx, cy);
        drawFacingRight(canvas, paint, cx, cy, scale);
        canvas.restore();
    }
}

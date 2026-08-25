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
 * 强盗敌人 - 人形怪物，手持大刀，攻击凶猛
 */
public class Bandit extends Enemy {

    private long slashAnimStartTime = 0; // 挥砍动画开始时间

    public Bandit(float x, float y) {
        super(x, y, 85);
        attackCooldown = 2000;
        setAttackShape(AttackShape.ARC); // 刀光斩击 - 扇形

        EnemyPropertyExtra enemyPropertyExtra = new EnemyPropertyExtra();
        enemyPropertyExtra.detectionRange = 300;
        enemyPropertyExtra.attackRange = 130;
        enemyPropertyExtra.rewardExp = 360;
        enemyPropertyExtra.rewardMoney = 180;
        setPropertyExtra(enemyPropertyExtra);

        setProperty(600, 300, 200, 100, 100);

        if (Math.random() < 0.02) {
            //BOSS
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;

            setProperty(maxHealth * 50, attackDamage * 8, defense * 8, speed * 8, mana * 8);
        } else if (Math.random() < 0.07) {
            //精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;

            setProperty(maxHealth * 10, attackDamage * 4, defense * 4, speed * 4, mana * 4);
        } else if (Math.random() < 0.30) {
            //首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);

            setProperty(maxHealth * 3, attackDamage * 2, defense * 2, speed * 2, mana * 2);
        }

        // 首领/精英/BOSS强盗可以使用连斩
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.COMBO);
            comboHitCount = (enemyLevel == EnemyLevel.BOSS) ? 4 : (enemyLevel == EnemyLevel.ELITE) ? 3 : 2;
        }

        // 精英/BOSS强盗可以使用环绕斩击（旋转刀光）
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
        }

        // BOSS强盗可以使用跳跃砸击
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        possibleDrops.clear();

        if (enemyLevel == EnemyLevel.BOSS) {
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(EquipCreator.createEquip(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            addPossibleDrop(ItemCreator.createBuildEquipIron(50));
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
            addPossibleDrop(EquipCreator.createEquip(30, null));
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
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
            addPossibleDrop(EquipCreator.createEquip(20, null));
            addPossibleDrop(EquipCreator.createEquip(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        } else {
            addPossibleDrop(EquipCreator.createEquip(20, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(20, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(20));
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        }

        return possibleDrops;
    }

    @Override
    protected void performAttack() {
        // Bandit attack logic - handled by GameEngine
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
        float screenX = x + offsetX + vibX;
        float screenY = y + offsetY + vibY;

        // Determine facing direction
        boolean facingRight = targetX > x;

        if (facingRight) {
            drawFacingRight(canvas, paint, screenX, screenY, scale);
        } else {
            drawFacingLeft(canvas, paint, screenX, screenY, scale);
        }
    }

    /**
     * 绘制面朝右的强盗（人形 + 手持大刀）
     */
    private void drawFacingRight(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        float legSwing1 = (float) Math.sin(animFrame * Math.PI / 2) * 3 * scale;
        float legSwing2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 3 * scale;

        // === 计算挥刀动画角度 ===
        long now = System.currentTimeMillis();
        float armSwingAngle = -20; // 默认持刀微抬（度，负=逆时针=抬手）
        boolean isAttacking = currentState == State.ATTACKING;

        if (isAttacking) {
            if (isSpinning) {
                // 环绕斩击：手臂持续旋转
                armSwingAngle = ((now - spinStartTime) / 4f) % 360;
            } else if (isCharging) {
                // 冲击：手臂后摆
                armSwingAngle = -50;
            } else if (isSlamLeaping) {
                // 跳跃砸击：手臂高举
                armSwingAngle = -140;
            } else if (isWindingUp) {
                // 蓄力阶段：手臂随蓄力逐渐高举
                float progress = getWindUpProgress();
                armSwingAngle = -20 - progress * 110; // -20° → -130°
                slashAnimStartTime = 0;
            } else if (slashAnimStartTime > 0) {
                // 挥砍阶段：手臂从上劈到下
                float slashProgress = Math.min(1.0f, (now - slashAnimStartTime) / 200f);
                armSwingAngle = -130 + slashProgress * 180; // -130° → 50°
            } else {
                // 前摇刚结束，开始挥砍动画
                slashAnimStartTime = now;
                armSwingAngle = -130;
            }
        } else {
            slashAnimStartTime = 0;
        }

        // === 1. 后腿（先画，在身体后面）===
        paint.setColor(Color.rgb(60, 50, 40));
        canvas.drawRect(cx - 6 * scale, cy + 5 * scale + bobOffset + legSwing2,
                cx - 2 * scale, cy + 14 * scale + bobOffset, paint);

        // === 2. 身体（深褐色短褂）===
        paint.setColor(Color.rgb(80, 60, 45));
        Path body = new Path();
        body.moveTo(cx - 8 * scale, cy - 6 * scale + bobOffset);
        body.lineTo(cx + 8 * scale, cy - 6 * scale + bobOffset);
        body.lineTo(cx + 6 * scale, cy + 7 * scale + bobOffset);
        body.lineTo(cx - 6 * scale, cy + 7 * scale + bobOffset);
        body.close();
        canvas.drawPath(body, paint);

        // 腰带
        paint.setColor(Color.rgb(120, 90, 50));
        canvas.drawRect(cx - 7 * scale, cy + 3 * scale + bobOffset,
                cx + 7 * scale, cy + 5.5f * scale + bobOffset, paint);

        // === 3. 前腿 ===
        paint.setColor(Color.rgb(60, 50, 40));
        canvas.drawRect(cx + 2 * scale, cy + 5 * scale + bobOffset + legSwing1,
                cx + 6 * scale, cy + 14 * scale + bobOffset, paint);

        // 靴子
        paint.setColor(Color.rgb(50, 35, 20));
        canvas.drawRect(cx - 7 * scale, cy + 12 * scale + bobOffset + legSwing2,
                cx - 1 * scale, cy + 15 * scale + bobOffset, paint);
        canvas.drawRect(cx + 1 * scale, cy + 12 * scale + bobOffset + legSwing1,
                cx + 7 * scale, cy + 15 * scale + bobOffset, paint);

        // === 4. 左臂（攻击时配合摆动）===
        paint.setColor(Color.rgb(80, 60, 45));
        paint.setStrokeWidth(3.5f * scale);
        if (isAttacking && !isSpinning) {
            // 攻击时左臂后摆
            canvas.drawLine(cx - 7 * scale, cy - 3 * scale + bobOffset,
                    cx - 13 * scale, cy + 1 * scale + bobOffset, paint);
        } else {
            canvas.drawLine(cx - 7 * scale, cy - 3 * scale + bobOffset,
                    cx - 11 * scale, cy + 4 * scale + bobOffset, paint);
        }

        // === 5. 头部 ===
        paint.setColor(Color.rgb(230, 200, 160));
        canvas.drawCircle(cx, cy - 11 * scale + bobOffset, 6 * scale, paint);

        // 头巾（深红色）
        paint.setColor(Color.rgb(140, 30, 30));
        Path hood = new Path();
        hood.moveTo(cx - 6.5f * scale, cy - 10 * scale + bobOffset);
        hood.lineTo(cx - 5 * scale, cy - 17 * scale + bobOffset);
        hood.lineTo(cx + 5 * scale, cy - 17 * scale + bobOffset);
        hood.lineTo(cx + 6.5f * scale, cy - 10 * scale + bobOffset);
        hood.close();
        canvas.drawPath(hood, paint);

        // 面罩（遮住口鼻）
        paint.setColor(Color.rgb(50, 50, 50));
        canvas.drawRect(cx - 4 * scale, cy - 9 * scale + bobOffset,
                cx + 5 * scale, cy - 6 * scale + bobOffset, paint);

        // 眼睛（凶狠的红色）
        if (currentState == State.CHASING || currentState == State.ATTACKING) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.rgb(200, 50, 50));
        }
        canvas.drawCircle(cx + 2 * scale, cy - 12 * scale + bobOffset, 1.5f * scale, paint);

        // === 6. 右臂 + 大刀（以肩膀为轴心旋转，实现挥砍动画）===
        float shoulderX = cx + 7 * scale;
        float shoulderY = cy - 3 * scale + bobOffset;

        canvas.save();
        canvas.rotate(armSwingAngle, shoulderX, shoulderY);

        // 上臂（从肩膀向下延伸）
        paint.setColor(Color.rgb(80, 60, 45));
        paint.setStrokeWidth(3.5f * scale);
        canvas.drawLine(shoulderX, shoulderY,
                shoulderX + 6 * scale, shoulderY + 4 * scale, paint);

        // 手
        paint.setColor(Color.rgb(230, 200, 160));
        canvas.drawCircle(shoulderX + 6 * scale, shoulderY + 4 * scale, 2 * scale, paint);

        // === 7. 大刀（跟随手臂旋转）===
        float knifeBaseX = shoulderX + 6 * scale;
        float knifeBaseY = shoulderY + 4 * scale;

        // 刀柄（棕色缠绕）
        paint.setColor(Color.rgb(100, 70, 40));
        paint.setStrokeWidth(2.5f * scale);
        canvas.drawLine(knifeBaseX - 1 * scale, knifeBaseY + 2 * scale,
                knifeBaseX + 2 * scale, knifeBaseY - 2 * scale, paint);

        // 护手（金色横档）
        paint.setColor(Color.rgb(200, 170, 50));
        paint.setStrokeWidth(2.5f * scale);
        canvas.drawLine(knifeBaseX + 0.5f * scale, knifeBaseY - 1.5f * scale,
                knifeBaseX + 3.5f * scale, knifeBaseY + 0.5f * scale, paint);

        // 刀身（银白色，宽刃，从护手向前延伸）
        paint.setColor(Color.rgb(210, 220, 235));
        Path blade = new Path();
        blade.moveTo(knifeBaseX + 1 * scale, knifeBaseY - 2 * scale);
        blade.lineTo(knifeBaseX + 3 * scale, knifeBaseY - 2 * scale);
        blade.lineTo(knifeBaseX + 9 * scale, knifeBaseY - 9 * scale);
        blade.lineTo(knifeBaseX + 7 * scale, knifeBaseY - 10 * scale);
        blade.close();
        canvas.drawPath(blade, paint);

        // 刀刃高光
        paint.setColor(Color.argb(180, 255, 255, 255));
        paint.setStrokeWidth(0.8f * scale);
        canvas.drawLine(knifeBaseX + 2 * scale, knifeBaseY - 2.5f * scale,
                knifeBaseX + 8 * scale, knifeBaseY - 9.5f * scale, paint);

        canvas.restore();

        // === 8. 攻击挥砍特效 ===
        if (isWindingUp) {
            float progress = getWindUpProgress();
            paint.setColor(Color.argb((int) (progress * 120), 255, 255, 200));
            paint.setStrokeWidth(2 * scale);
            Path slashArc = new Path();
            slashArc.addArc(
                    cx + 10 * scale - 18 * scale, cy - 5 * scale + bobOffset - 18 * scale,
                    cx + 10 * scale + 18 * scale, cy - 5 * scale + bobOffset + 18 * scale,
                    -60, 120 * progress);
            canvas.drawPath(slashArc, paint);
        }
    }

    /**
     * 绘制面朝左的强盗（镜像翻转）
     */
    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        canvas.save();
        canvas.scale(-1, 1, cx, cy);
        drawFacingRight(canvas, paint, cx, cy, scale);
        canvas.restore();
    }
}

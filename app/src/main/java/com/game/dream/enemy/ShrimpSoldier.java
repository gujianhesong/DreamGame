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
 * 虾兵 - 东海龙宫的虾兵，高速突袭型怪物
 * 特点: 速度快、血量低、猛扑攻击
 * 视觉: 红褐色半透明身体、弯曲腰身、长触须、扇形尾片、手持珊瑚长矛
 */
public class ShrimpSoldier extends Enemy {

    public ShrimpSoldier(float x, float y) {
        super(x, y, 70);
        attackCooldown = 2000;
        setAttackShape(AttackShape.ARC); // 珊瑚矛刺击 - 扇形

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 500;
        prop.attackRange = 300;
        prop.rewardExp = 600;
        prop.rewardMoney = 300;
        setPropertyExtra(prop);

        // 虾兵：速度高、血量低、攻击中等
        setProperty(800, 320, 250, 200, 150);

        // 等级分布
        if (Math.random() < 0.02) {
            // BOSS - 虾帅
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 50, attackDamage * 8, defense * 6, speed * 7, mana * 6);
        } else if (Math.random() < 0.07) {
            // 精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 10, attackDamage * 4, defense * 3, speed * 4, mana * 3);
        } else if (Math.random() < 0.30) {
            // 首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, attackDamage * 2, defense * 2, speed * 2, mana * 2);
        }

        // 所有虾兵使用猛扑（虾弹跳射）
        addAvailableAttackType(AttackType.POUNCE);
        pounceFixedSpeed = 900f; // 虾兵弹射速度略低于狼

        // 首领以上可使用连续爪击（珊瑚矛连刺）
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.COMBO);
            comboHitCount = (enemyLevel == EnemyLevel.BOSS) ? 4 : (enemyLevel == EnemyLevel.ELITE) ? 3 : 2;
            comboHitInterval = 180; // 虾兵连刺更快
        }

        // 精英以上可使用闪现突击（水遁突刺）
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.BLINK_STRIKE);
        }

        // BOSS可使用跳跃砸击（泰山压顶）
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
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
        // ShrimpSoldier attack - handled by GameEngine
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

        // 攻击动画：前冲
        float lunge = 0;
        if (currentState == State.ATTACKING) {
            if (isWindingUp) {
                lunge = -getWindUpProgress() * 4 * scale;
            } else {
                float p = Math.min(1.0f, (now - getLastAttackTime()) / 200f);
                lunge = (1 - p) * 8 * scale;
            }
        }

        // 身体摆动（虾的游动感）
        float bodySwing = (float) Math.sin(now / 300.0) * 1.5f * scale;

        // === 1. 尾扇（身体后方）===
        paint.setColor(Color.rgb(200, 80, 50));
        Path tailFan = new Path();
        float tailBaseX = cx - 14 * scale + bodySwing;
        float tailBaseY = cy + 2 * scale + bobOffset;
        // 扇形尾片（5片）
        for (int i = 0; i < 5; i++) {
            float angle = (float) (i * Math.PI / 6 - Math.PI / 3);
            float fanLen = 10 * scale;
            float tipX = tailBaseX + (float) Math.cos(angle + Math.PI) * fanLen;
            float tipY = tailBaseY + (float) Math.sin(angle + Math.PI) * fanLen * 0.6f;
            paint.setColor(Color.argb(200, 200 + i * 8, 70 + i * 10, 40 + i * 5));
            Path fan = new Path();
            fan.moveTo(tailBaseX, tailBaseY);
            fan.lineTo(tipX - 2 * scale, tipY - 1 * scale);
            fan.lineTo(tipX + 2 * scale, tipY + 1 * scale);
            fan.close();
            canvas.drawPath(fan, paint);
        }

        // === 2. 身体（弯曲的虾身，分节）===
        // 虾身从尾到头呈弧形
        float[] segX = new float[6];
        float[] segY = new float[6];
        for (int i = 0; i < 6; i++) {
            float t = i / 5f;
            // 弧形路径：尾部低，中间高，头部略低
            segX[i] = tailBaseX + (16 * scale + lunge) * t + bodySwing * (1 - t);
            segY[i] = tailBaseY + (-8 * scale) * (float) Math.sin(t * Math.PI) * 0.6f + bobOffset;
        }

        // 身体主体（粗曲线）
        paint.setColor(Color.rgb(210, 90, 55));
        paint.setStrokeWidth(8 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        Path bodyPath = new Path();
        bodyPath.moveTo(segX[0], segY[0]);
        for (int i = 1; i < 6; i++) {
            bodyPath.lineTo(segX[i], segY[i]);
        }
        canvas.drawPath(bodyPath, paint);

        // 身体分节纹路
        paint.setColor(Color.rgb(180, 70, 40));
        paint.setStrokeWidth(1 * scale);
        for (int i = 1; i < 5; i++) {
            float nodeX = segX[i];
            float nodeY = segY[i];
            canvas.drawLine(nodeX - 3 * scale, nodeY - 3 * scale,
                    nodeX + 3 * scale, nodeY + 3 * scale, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // === 3. 腿部（多对小腿）===
        paint.setColor(Color.rgb(190, 80, 50));
        paint.setStrokeWidth(1.2f * scale);
        for (int i = 1; i < 5; i++) {
            float legX = segX[i];
            float legY = segY[i];
            float legSwing = (float) Math.sin(now / 250.0 + i * 0.8) * 2 * scale;
            // 每节一对小腿
            canvas.drawLine(legX - 2 * scale, legY + 3 * scale,
                    legX - 4 * scale, legY + 8 * scale + legSwing, paint);
            canvas.drawLine(legX + 2 * scale, legY + 3 * scale,
                    legX + 4 * scale, legY + 8 * scale - legSwing, paint);
        }

        // === 4. 头部 ===
        float headX = segX[5] + lunge;
        float headY = segY[5];

        // 虾头（三角形，前端尖）
        paint.setColor(Color.rgb(220, 95, 60));
        Path head = new Path();
        head.moveTo(headX - 5 * scale, headY - 5 * scale);
        head.lineTo(headX + 10 * scale, headY - 2 * scale);
        head.lineTo(headX + 8 * scale, headY + 3 * scale);
        head.lineTo(headX - 5 * scale, headY + 4 * scale);
        head.close();
        canvas.drawPath(head, paint);

        // 虾眼（黑色圆点，攻击时红色）
        if (isAggressive) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.rgb(30, 20, 15));
        }
        canvas.drawCircle(headX + 3 * scale, headY - 4 * scale, 1.8f * scale, paint);
        // 眼睛高光
        paint.setColor(Color.WHITE);
        canvas.drawCircle(headX + 2.5f * scale, headY - 4.5f * scale, 0.6f * scale, paint);

        // === 5. 触须（两根长触须飘动）===
        paint.setColor(Color.rgb(230, 120, 70));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        float antennaWave1 = (float) Math.sin(now / 400.0) * 4 * scale;
        float antennaWave2 = (float) Math.sin(now / 400.0 + 1.5) * 4 * scale;
        // 上触须
        Path antenna1 = new Path();
        antenna1.moveTo(headX + 8 * scale, headY - 3 * scale);
        antenna1.quadTo(headX + 16 * scale, headY - 10 * scale + antennaWave1,
                headX + 22 * scale, headY - 14 * scale + antennaWave1);
        canvas.drawPath(antenna1, paint);
        // 下触须
        Path antenna2 = new Path();
        antenna2.moveTo(headX + 8 * scale, headY - 1 * scale);
        antenna2.quadTo(headX + 15 * scale, headY - 6 * scale + antennaWave2,
                headX + 20 * scale, headY - 8 * scale + antennaWave2);
        canvas.drawPath(antenna2, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // === 6. 珊瑚长矛（武器）===
        float spearX = headX + 6 * scale;
        float spearY = headY - 1 * scale;
        // 矛杆
        paint.setColor(Color.rgb(255, 100, 80));
        paint.setStrokeWidth(2 * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(spearX, spearY + 6 * scale, spearX + 14 * scale, spearY - 10 * scale, paint);
        // 矛尖（珊瑚色三角）
        paint.setColor(Color.rgb(255, 70, 50));
        paint.setStyle(Paint.Style.FILL);
        Path spearTip = new Path();
        spearTip.moveTo(spearX + 14 * scale, spearY - 10 * scale);
        spearTip.lineTo(spearX + 12 * scale, spearY - 14 * scale);
        spearTip.lineTo(spearX + 16 * scale, spearY - 13 * scale);
        spearTip.close();
        canvas.drawPath(spearTip, paint);
        paint.setStrokeWidth(1);

        // === 7. 攻击特效（蓄力时水花）===
        if (currentState == State.ATTACKING && isWindingUp) {
            float progress = getWindUpProgress();
            paint.setColor(Color.argb((int) (progress * 120), 100, 200, 255));
            paint.setStrokeWidth(2 * scale);
            paint.setStyle(Paint.Style.STROKE);
            float arcRadius = 12 * scale * progress;
            canvas.drawCircle(headX + 5 * scale, headY, arcRadius, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }

    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        canvas.save();
        canvas.scale(-1, 1, cx, cy);
        drawFacingRight(canvas, paint, cx, cy, scale);
        canvas.restore();
    }
}

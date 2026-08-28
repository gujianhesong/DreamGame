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
 * 夜叉 - 东海龙宫的护法恶鬼，近战爆发型怪物
 * 特点: 攻击高、血量中等、速度中等、狂暴机制（血量越低攻击越高）
 * 视觉: 深蓝皮肤、肌肉虬结、獠牙、红眼、三叉戟
 */
public class Yaksha extends Enemy {

    // 狂暴状态
    private boolean isEnraged = false;
    private static final float RAGE_THRESHOLD = 0.50f; // 50% 血量触发
    private static final float RAGE_ATTACK_BONUS = 1.5f; // 攻击 +50%
    private static final float RAGE_SPEED_BONUS = 1.3f; // 速度 +30%

    // 基础属性缓存（用于狂暴计算）
    private int baseAttackDamage;
    private int baseSpeed;

    public Yaksha(float x, float y) {
        super(x, y, 100);
        attackCooldown = 2200;
        setAttackShape(AttackShape.ARC); // 三叉戟横扫 - 扇形

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 320;
        prop.attackRange = 150;
        prop.rewardExp = 1000;
        prop.rewardMoney = 500;
        setPropertyExtra(prop);

        // 夜叉：攻击高、血量中等、速度中等
        setProperty(800, 400, 200, 150, 150);
        baseAttackDamage = attackDamage;
        baseSpeed = speed;

        // 等级分布
        if (Math.random() < 0.02) {
            // BOSS - 九幽夜叉王
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 50, attackDamage * 8, defense * 7, speed * 7, mana * 6);
            baseAttackDamage = attackDamage;
            baseSpeed = speed;
        } else if (Math.random() < 0.07) {
            // 精英 - 噬魂夜叉
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 10, attackDamage * 4, defense * 3, speed * 4, mana * 3);
            baseAttackDamage = attackDamage;
            baseSpeed = speed;
        } else if (Math.random() < 0.30) {
            // 首领 - 夜叉头目
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, attackDamage * 2, defense * 2, speed * 2, mana * 2);
            baseAttackDamage = attackDamage;
            baseSpeed = speed;
        }

        // 所有夜叉使用猛扑（夜叉扑杀）
        addAvailableAttackType(AttackType.POUNCE);
        pounceFixedSpeed = 850f;

        // 首领以上可使用连续爪击（三叉戟连刺）
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.COMBO);
            comboHitCount = (enemyLevel == EnemyLevel.BOSS) ? 4 : (enemyLevel == EnemyLevel.ELITE) ? 3 : 2;
            comboHitInterval = 220;
        }

        // 精英以上可使用闪现突击（鬼影闪）
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.BLINK_STRIKE);
        }

        // BOSS可使用环绕斩击（夜叉旋风斩）
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
            spinDuration = 2000;
            spinHitInterval = 300;
        }
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive()) return;

        // 更新狂暴状态
        boolean shouldRage = (health <= maxHealth * RAGE_THRESHOLD);
        if (shouldRage && !isEnraged) {
            isEnraged = true;
            attackDamage = (int) (baseAttackDamage * RAGE_ATTACK_BONUS);
            speed = (int) (baseSpeed * RAGE_SPEED_BONUS);
        } else if (!shouldRage && isEnraged) {
            // 如果治疗回到阈值以上，退出狂暴
            isEnraged = false;
            attackDamage = baseAttackDamage;
            speed = baseSpeed;
        }
    }

    /**
     * 检查是否处于狂暴状态
     */
    public boolean isEnraged() {
        return isEnraged;
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
        // Yaksha attack - handled by GameEngine
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

        // 攻击动画
        float lunge = 0;
        if (currentState == State.ATTACKING) {
            if (isWindingUp) {
                lunge = -getWindUpProgress() * 5 * scale;
            } else {
                float p = Math.min(1.0f, (now - getLastAttackTime()) / 200f);
                lunge = (1 - p) * 10 * scale;
            }
        }

        // 狂暴光效（全身红色脉冲光环）
        if (isEnraged) {
            float ragePulse = (float) Math.sin(now / 200.0) * 0.3f + 0.7f;
            paint.setColor(Color.argb((int) (60 * ragePulse), 255, 50, 30));
            canvas.drawCircle(cx, cy + bobOffset, size * 0.9f, paint);
        }

        // === 1. 腿部（粗壮双腿）===
        float legSwing1 = (float) Math.sin(animFrame * Math.PI / 2) * 3 * scale;
        float legSwing2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 3 * scale;

        paint.setColor(Color.rgb(40, 55, 80));
        canvas.drawRect(cx - 7 * scale, cy + 8 * scale + bobOffset + legSwing1,
                cx - 2 * scale, cy + 18 * scale + bobOffset, paint);
        canvas.drawRect(cx + 2 * scale, cy + 8 * scale + bobOffset + legSwing2,
                cx + 7 * scale, cy + 18 * scale + bobOffset, paint);

        // 脚（宽厚）
        paint.setColor(Color.rgb(35, 48, 70));
        canvas.drawOval(cx - 8 * scale, cy + 16 * scale + bobOffset + legSwing1,
                cx - 1 * scale, cy + 20 * scale + bobOffset + legSwing1, paint);
        canvas.drawOval(cx + 1 * scale, cy + 16 * scale + bobOffset + legSwing2,
                cx + 8 * scale, cy + 20 * scale + bobOffset + legSwing2, paint);

        // === 2. 身体（肌肉虬结的躯干）===
        paint.setColor(Color.rgb(50, 70, 100));
        Path torso = new Path();
        torso.moveTo(cx - 12 * scale, cy - 8 * scale + bobOffset);
        torso.lineTo(cx + 12 * scale, cy - 8 * scale + bobOffset);
        torso.lineTo(cx + 10 * scale, cy + 10 * scale + bobOffset);
        torso.lineTo(cx - 10 * scale, cy + 10 * scale + bobOffset);
        torso.close();
        canvas.drawPath(torso, paint);

        // 肌肉纹路（胸肌 + 腹肌线条）
        paint.setColor(Color.argb(80, 70, 95, 130));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        // 胸肌分界线
        canvas.drawLine(cx, cy - 6 * scale + bobOffset, cx, cy + 2 * scale + bobOffset, paint);
        canvas.drawLine(cx - 8 * scale, cy - 3 * scale + bobOffset, cx + 8 * scale, cy - 3 * scale + bobOffset, paint);
        // 腹肌
        for (int i = 0; i < 3; i++) {
            float ay = cy + (3 + i * 3) * scale + bobOffset;
            canvas.drawLine(cx - 5 * scale, ay, cx + 5 * scale, ay, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 鳞片暗纹（身上隐约的鳞纹）
        paint.setColor(Color.argb(40, 80, 110, 150));
        for (int i = 0; i < 4; i++) {
            float sx = cx + (-6 + i * 4) * scale;
            float sy = cy + (-5 + i * 2) * scale + bobOffset;
            canvas.drawCircle(sx, sy, 1.5f * scale, paint);
        }

        // === 3. 手臂（粗壮）===
        paint.setColor(Color.rgb(50, 70, 100));
        paint.setStrokeWidth(4 * scale);
        paint.setStyle(Paint.Style.STROKE);
        // 左臂
        canvas.drawLine(cx - 12 * scale, cy - 5 * scale + bobOffset,
                cx - 16 * scale, cy + 4 * scale + bobOffset, paint);
        // 右臂（持武器，攻击时前伸）
        canvas.drawLine(cx + 12 * scale, cy - 5 * scale + bobOffset,
                cx + 16 * scale + lunge, cy + 2 * scale + bobOffset, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // === 4. 头部 ===
        float headX = cx + lunge * 0.3f;
        float headY = cy - 14 * scale + bobOffset;

        // 头部（方形下颌，凶恶）
        paint.setColor(Color.rgb(55, 75, 105));
        Path head = new Path();
        head.moveTo(headX - 7 * scale, headY - 6 * scale);
        head.lineTo(headX + 7 * scale, headY - 6 * scale);
        head.lineTo(headX + 8 * scale, headY + 2 * scale);
        head.lineTo(headX + 5 * scale, headY + 6 * scale);
        head.lineTo(headX - 5 * scale, headY + 6 * scale);
        head.lineTo(headX - 8 * scale, headY + 2 * scale);
        head.close();
        canvas.drawPath(head, paint);

        // 蓬乱长发（飘散）
        paint.setColor(Color.rgb(20, 25, 40));
        float hairWave = (float) Math.sin(now / 400.0) * 2 * scale;
        Path hair = new Path();
        hair.moveTo(headX - 8 * scale, headY - 5 * scale);
        hair.quadTo(headX - 10 * scale + hairWave, headY - 12 * scale, headX, headY - 10 * scale);
        hair.quadTo(headX + 10 * scale - hairWave, headY - 12 * scale, headX + 8 * scale, headY - 5 * scale);
        hair.lineTo(headX + 10 * scale, headY + 4 * scale);
        hair.quadTo(headX + 12 * scale + hairWave, headY + 10 * scale, headX + 8 * scale, headY + 14 * scale);
        hair.lineTo(headX - 8 * scale, headY + 14 * scale);
        hair.quadTo(headX - 12 * scale - hairWave, headY + 10 * scale, headX - 10 * scale, headY + 4 * scale);
        hair.close();
        canvas.drawPath(hair, paint);

        // 眼睛（红色，狂暴时更亮）
        if (isEnraged) {
            paint.setColor(Color.rgb(255, 30, 20));
            // 狂暴时眼睛发光
            paint.setColor(Color.argb(100, 255, 50, 30));
            canvas.drawCircle(headX - 3.5f * scale, headY - 1 * scale, 3 * scale, paint);
            canvas.drawCircle(headX + 3.5f * scale, headY - 1 * scale, 3 * scale, paint);
            paint.setColor(Color.rgb(255, 40, 20));
        } else if (isAggressive) {
            paint.setColor(Color.rgb(255, 60, 40));
        } else {
            paint.setColor(Color.rgb(200, 50, 40));
        }
        canvas.drawCircle(headX - 3.5f * scale, headY - 1 * scale, 1.8f * scale, paint);
        canvas.drawCircle(headX + 3.5f * scale, headY - 1 * scale, 1.8f * scale, paint);

        // 獠牙（两根尖牙从下颌伸出）
        paint.setColor(Color.rgb(230, 225, 200));
        Path fangL = new Path();
        fangL.moveTo(headX - 4 * scale, headY + 4 * scale);
        fangL.lineTo(headX - 3 * scale, headY + 4 * scale);
        fangL.lineTo(headX - 3.5f * scale, headY + 8 * scale);
        fangL.close();
        canvas.drawPath(fangL, paint);
        Path fangR = new Path();
        fangR.moveTo(headX + 3 * scale, headY + 4 * scale);
        fangR.lineTo(headX + 4 * scale, headY + 4 * scale);
        fangR.lineTo(headX + 3.5f * scale, headY + 8 * scale);
        fangR.close();
        canvas.drawPath(fangR, paint);

        // === 5. 三叉戟（武器）===
        float tridentX = cx + 16 * scale + lunge;
        float tridentY = cy - 2 * scale + bobOffset;
        // 戟杆
        paint.setColor(Color.rgb(100, 100, 110));
        paint.setStrokeWidth(2.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(tridentX, tridentY + 14 * scale, tridentX + 4 * scale, tridentY - 16 * scale, paint);
        // 三叉戟头
        paint.setColor(Color.rgb(80, 160, 200));
        paint.setStyle(Paint.Style.FILL);
        // 中间尖
        Path tip1 = new Path();
        tip1.moveTo(tridentX + 4 * scale, tridentY - 16 * scale);
        tip1.lineTo(tridentX + 3 * scale, tridentY - 22 * scale);
        tip1.lineTo(tridentX + 5 * scale, tridentY - 22 * scale);
        tip1.close();
        canvas.drawPath(tip1, paint);
        // 左叉
        Path tip2 = new Path();
        tip2.moveTo(tridentX + 2 * scale, tridentY - 15 * scale);
        tip2.lineTo(tridentX, tridentY - 20 * scale);
        tip2.lineTo(tridentX + 2.5f * scale, tridentY - 18 * scale);
        tip2.close();
        canvas.drawPath(tip2, paint);
        // 右叉
        Path tip3 = new Path();
        tip3.moveTo(tridentX + 6 * scale, tridentY - 15 * scale);
        tip3.lineTo(tridentX + 8 * scale, tridentY - 20 * scale);
        tip3.lineTo(tridentX + 5.5f * scale, tridentY - 18 * scale);
        tip3.close();
        canvas.drawPath(tip3, paint);

        // 狂暴时叉尖幽蓝光芒
        if (isEnraged) {
            float glow = (float) Math.sin(now / 150.0) * 0.3f + 0.7f;
            paint.setColor(Color.argb((int) (80 * glow), 100, 200, 255));
            canvas.drawCircle(tridentX + 4 * scale, tridentY - 19 * scale, 5 * scale, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // === 6. 攻击特效 ===
        if (currentState == State.ATTACKING && isWindingUp) {
            float progress = getWindUpProgress();
            int effectColor = isEnraged ? Color.argb((int) (progress * 150), 255, 60, 30)
                    : Color.argb((int) (progress * 120), 100, 180, 255);
            paint.setColor(effectColor);
            paint.setStrokeWidth(2 * scale);
            paint.setStyle(Paint.Style.STROKE);
            float arcRadius = 14 * scale * progress;
            canvas.drawCircle(cx + 5 * scale, cy + bobOffset, arcRadius, paint);
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

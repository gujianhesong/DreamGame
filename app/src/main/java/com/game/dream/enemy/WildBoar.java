package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.EquipCreator;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.utils.Utils;

import java.util.List;

/**
 * 野猪敌人 - 具有冲撞技能和奔跑动画
 */
public class WildBoar extends Enemy {

    // 动画相关
    private float legAngle; // 腿部摆动角度
    private float bodyBob;  // 身体起伏

    public WildBoar(float x, float y) {
        super(x, y, 100);
        setAttackShape(AttackShape.RECT); // 野猪冲锋 - 矩形
        addAvailableAttackType(AttackType.CHARGE); // 冲击攻击

        int health = Utils.getWaveValueInt(300, 0.2f);
        this.maxHealth = health;
        this.health = health;
        this.attackDamage = 40;
        this.defense = 50;
        this.speed = 70;
        this.mana = 40;

        this.legAngle = 0;
        this.bodyBob = 0;

        EnemyPropertyExtra enemyPropertyExtra = new EnemyPropertyExtra();
        enemyPropertyExtra.detectionRange = 200f;
        enemyPropertyExtra.attackRange = 100;
        enemyPropertyExtra.rewardExp = 240;
        enemyPropertyExtra.rewardMoney = 120;
        setPropertyExtra(enemyPropertyExtra);

        setProperty(400, 70, 50, 70, 40);

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

        // 精英/BOSS野猪可以使用环绕斩击
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
        }

        // BOSS野猪可以使用跳跃砸击
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive()) return;

        // 更新野猪特有的动画
        updateBoarAnimation(deltaTime);

        // 处理冲撞逻辑
        if (isCharging) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - chargeStartTime < 800) { // 冲撞持续 0.8 秒
                // 高速直线移动
                float chargeSpeed = speed * 3.5f;
                float moveX = chargeDirectionX * chargeSpeed * (deltaTime / 1000f);
                float moveY = chargeDirectionY * chargeSpeed * (deltaTime / 1000f);

                x += moveX;
                y += moveY;

                // 边界检查
                x = Math.max(size, Math.min(x, mapWidth - size));
                y = Math.max(size, Math.min(y, mapHeight - size));
            } else {
                isCharging = false;
                currentState = State.IDLE; // 冲撞结束后进入待机
            }
        }
    }

    /**
     * 重写追逐逻辑，增加随机冲撞
     */
    @Override
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        // 5% 的概率触发冲撞
        if (!isCharging && Math.random() < 0.05f) {
            startCharge(playerX, playerY);
            return;
        }

        super.updateChasing(deltaSeconds, playerX, playerY, map, mapWidth, mapHeight);
    }

    /**
     * 开始冲撞
     */
    private void startCharge(float targetX, float targetY) {
        isCharging = true;
        chargeStartTime = System.currentTimeMillis();

        // 计算冲撞方向
        float dx = targetX - this.x;
        float dy = targetY - this.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            chargeDirectionX = dx / dist;
            chargeDirectionY = dy / dist;
        }
    }

    /**
     * 野猪特有的动画：腿部摆动和身体起伏
     */
    private void updateBoarAnimation(long deltaTime) {
        float animSpeed = 0.01f;
        if (currentState == State.CHASING || isCharging) {
            animSpeed = 0.02f; // 奔跑时动画更快
        }

        legAngle += animSpeed * deltaTime;
        bodyBob = (float) Math.sin(legAngle) * 3; // 身体上下起伏 3 像素
    }

    @Override
    public void onDraw(Canvas canvas, int cameraX, int cameraY) {
        if (!isAlive()) return;

        paint.setAntiAlias(true);

        // 【修正】引入一个缩放系数，让野猪视觉上比 size 定义的要小一些
        float visualScale = 0.85f;

        // === 攻击动画：低头冲撞 ===
        float lunge = 0;
        float headDip = 0;
        if (currentState == State.ATTACKING) {
            long now = System.currentTimeMillis();
            if (isWindingUp) {
                lunge = -getWindUpProgress() * 5 * visualScale;
                headDip = getWindUpProgress() * 4 * visualScale;
            } else {
                float p = Math.min(1.0f, (now - getLastAttackTime()) / 200f);
                lunge = (1 - p) * 12 * visualScale;
            }
        }

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = System.currentTimeMillis() - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 4 * visualScale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        float cx = x + cameraX;
        float cy = y + cameraY + bodyBob * visualScale; // 应用身体起伏

        boolean facingRight = (targetX > x) || (isCharging && chargeDirectionX > 0);
        float scaleX = facingRight ? -1.0f : 1.0f;

        // 应用攻击偏移（朝向前方）
        float lungeX = facingRight ? -lunge : lunge;
        cx += lungeX + vibX;
        cy += headDip + vibY;

        canvas.save();
        canvas.scale(scaleX, 1.0f, cx, cy);

        // --- 所有绘制尺寸都乘以 visualScale ---

        // 2. 身体
        paint.setColor(Color.rgb(90, 60, 40));
        canvas.drawOval(cx - size / 2.2f * visualScale, cy - size / 3.5f * visualScale,
                cx + size / 2.2f * visualScale, cy + size / 3.5f * visualScale, paint);

        // 3. 鬃毛
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4 * visualScale);
        for (int i = 0; i < 5; i++) {
            float hx = cx - size / 3f * visualScale + i * (size / 4f * visualScale);
            float hy = cy - size / 3.5f * visualScale - 5 * visualScale;
            canvas.drawLine(hx, hy, hx, hy - 8 * visualScale, paint);
        }

        // 4. 头部
        paint.setColor(Color.rgb(90, 60, 40));
        float headR = size / 3.2f * visualScale;
        canvas.drawCircle(cx - size / 2.5f * visualScale, cy - size / 8f * visualScale, headR, paint);

        // 5. 獠牙
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3 * visualScale);
        Path tusk1 = new Path();
        tusk1.moveTo(cx - size / 2.2f * visualScale, cy - size / 10f * visualScale);
        tusk1.quadTo(cx - size / 2.2f * visualScale - 12 * visualScale, cy - size / 10f * visualScale - 10 * visualScale,
                cx - size / 2.2f * visualScale - 8 * visualScale, cy - size / 10f * visualScale - 18 * visualScale);
        canvas.drawPath(tusk1, paint);

        Path tusk2 = new Path();
        tusk2.moveTo(cx - size / 2.2f * visualScale, cy - size / 10f * visualScale + 5 * visualScale);
        tusk2.quadTo(cx - size / 2.2f * visualScale - 12 * visualScale, cy - size / 10f * visualScale - 5 * visualScale,
                cx - size / 2.2f * visualScale - 8 * visualScale, cy - size / 10f * visualScale - 13 * visualScale);
        canvas.drawPath(tusk2, paint);

        // 6. 眼睛
        paint.setColor(Color.RED);
        canvas.drawCircle(cx - size / 2.5f * visualScale, cy - size / 5f * visualScale, 3 * visualScale, paint);

        // 7. 耳朵
        paint.setColor(Color.rgb(90, 60, 40));
        paint.setStyle(Paint.Style.FILL);
        Path ear = new Path();
        ear.moveTo(cx - size / 2.5f * visualScale, cy - size / 2.5f * visualScale);
        ear.lineTo(cx - size / 2.5f * visualScale - 5 * visualScale, cy - size / 2.5f * visualScale - 12 * visualScale);
        ear.lineTo(cx - size / 2.5f * visualScale + 8 * visualScale, cy - size / 2.5f * visualScale - 2 * visualScale);
        canvas.drawPath(ear, paint);

        // 8. 四条腿
        paint.setColor(Color.rgb(70, 50, 30));
        paint.setStrokeWidth(6 * visualScale);

        float legLength = size / 2.5f * visualScale;
        float legOffset = (float) Math.sin(legAngle) * 8 * visualScale;

        canvas.drawLine(cx - size / 3f * visualScale, cy + size / 4f * visualScale,
                cx - size / 3f * visualScale - legOffset, cy + size / 4f * visualScale + legLength, paint);
        canvas.drawLine(cx - size / 6f * visualScale, cy + size / 4f * visualScale,
                cx - size / 6f * visualScale + legOffset, cy + size / 4f * visualScale + legLength, paint);
        canvas.drawLine(cx + size / 6f * visualScale, cy + size / 4f * visualScale,
                cx + size / 6f * visualScale - legOffset, cy + size / 4f * visualScale + legLength, paint);
        canvas.drawLine(cx + size / 3f * visualScale, cy + size / 4f * visualScale,
                cx + size / 3f * visualScale + legOffset, cy + size / 4f * visualScale + legLength, paint);

        // 9. 尾巴
        paint.setStrokeWidth(3 * visualScale);
        canvas.drawArc(cx + size / 2.2f * visualScale - 5 * visualScale, cy - 5 * visualScale,
                cx + size / 2.2f * visualScale + 5 * visualScale, cy + 5 * visualScale, 0, 180, false, paint);

        // 10. 冲撞特效
        if (isCharging) {
            paint.setColor(Color.argb(80, 255, 100, 100));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2 * visualScale);
            canvas.drawCircle(cx, cy, size / 1.5f * visualScale, paint);
        }

        canvas.restore();
    }

    @Override
    public List<Item> getPossibleDropList() {
        possibleDrops.clear();

        if (enemyLevel == EnemyLevel.BOSS) {
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
        } else if (enemyLevel == EnemyLevel.ELITE) {
            addPossibleDrop(EquipCreator.createEquip(10, null));
            addPossibleDrop(EquipCreator.createEquip(20, null));

            addPossibleDrop(ItemCreator.createBuildEquipBook(10, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(20, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(10));
            addPossibleDrop(ItemCreator.createBuildEquipIron(20));

            addPossibleDrop(ItemCreator.createGain_hp_1());
            addPossibleDrop(ItemCreator.createGain_attack_1());
            addPossibleDrop(ItemCreator.createGain_defense_1());
            addPossibleDrop(ItemCreator.createGain_mana_1());
            addPossibleDrop(ItemCreator.createGain_speed_1());

            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        } else if (enemyLevel == EnemyLevel.LEADER) {
            addPossibleDrop(EquipCreator.createEquip(0, null));
            addPossibleDrop(EquipCreator.createEquip(10, null));

            addPossibleDrop(ItemCreator.createBuildEquipBook(10, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(10));

            addPossibleDrop(ItemCreator.createHp1_2_QiyeLian());
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createMp1_2_Xiangye());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
        } else {
            addPossibleDrop(EquipCreator.createEquip(0, null));

            addPossibleDrop(ItemCreator.createHp1_1_Siyehua());
            addPossibleDrop(ItemCreator.createHp1_2_QiyeLian());
            addPossibleDrop(ItemCreator.createMp1_1_Foshou());
            addPossibleDrop(ItemCreator.createMp1_2_Xiangye());
        }

        return possibleDrops;
    }
}
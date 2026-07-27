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
    private boolean isCharging;
    private long chargeStartTime;
    private float chargeDirectionX;
    private float chargeDirectionY;

    // 动画相关
    private float legAngle; // 腿部摆动角度
    private float bodyBob;  // 身体起伏

    public WildBoar(float x, float y) {
        super(x, y, 100, 200f, 45f, 40, 150); // size=55, detection=200, attack=45, exp=30, money=15
        setAttackShape(AttackShape.RECT); // 野猪冲锋 - 矩形

        int health = Utils.getWaveValueInt(300, 0.2f);
        this.maxHealth = health;
        this.health = health;
        this.attackDamage = 40;
        this.defense = 50;
        this.speed = 70;
        this.mana = 40;

        this.isCharging = false;
        this.legAngle = 0;
        this.bodyBob = 0;

        if (Math.random() < 0.05) {
            //精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;

            health = Utils.getWaveValueInt(300 * 10, 0.2f);
            this.maxHealth = health;
            this.health = health;
            this.attackDamage = 80;
            this.defense = 80;
            this.speed = 140;
            this.mana = 80;
        } else if (Math.random() < 0.25) {
            //首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);

            health = Utils.getWaveValueInt(300 * 3, 0.2f);
            this.maxHealth = health;
            this.health = health;
            this.attackDamage = 60;
            this.defense = 60;
            this.speed = 100;
            this.mana = 60;
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

        float cx = x + cameraX;
        float cy = y + cameraY + bodyBob * visualScale; // 应用身体起伏

        boolean facingRight = (targetX > x) || (isCharging && chargeDirectionX > 0);
        float scaleX = facingRight ? -1.0f : 1.0f;

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

        if (enemyLevel == EnemyLevel.ELITE) {
            addPossibleDrop(EquipCreator.createEquip(10, null));
            addPossibleDrop(EquipCreator.createEquip(20, null));

            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        } else if (enemyLevel == EnemyLevel.LEADER) {
            addPossibleDrop(EquipCreator.createEquip(0, null));
            addPossibleDrop(EquipCreator.createEquip(10, null));

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
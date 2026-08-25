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
 * 毒蛇敌人 - 具有毒素伤害和 S 型游走动画
 */
public class Viper extends Enemy {
    private float waveOffset; // 用于 S 型游走的相位

    public Viper(float x, float y) {
        super(x, y, 60);
        setAttackShape(AttackShape.RECT); // 蛇头前咬 - 矩形
        addAvailableAttackType(AttackType.DRAIN_BITE); // 吸血撕咬
        windUpDuration = 300; // 蛇咬前摇较短，快速出击
        this.waveOffset = 0;

        EnemyPropertyExtra enemyPropertyExtra = new EnemyPropertyExtra();
        enemyPropertyExtra.detectionRange = 180;
        enemyPropertyExtra.attackRange = 100;
        enemyPropertyExtra.rewardExp = 200;
        enemyPropertyExtra.rewardMoney = 100;
        setPropertyExtra(enemyPropertyExtra);

        setProperty(280, 50, 40, 50, 40);

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

        // 精英/BOSS蛇可以使用闪现突击
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.BLINK_STRIKE);
        }

        // BOSS蛇可以使用跳跃砸击
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive()) return;

        // 更新游走动画相位
        waveOffset += 0.005f * deltaTime;
    }

    /**
     * 重写绘制方法，实现蛇的形态
     */
    @Override
    public void onDraw(Canvas canvas, int cameraX, int cameraY) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float cx = x + cameraX;
        float cy = y + cameraY;

        boolean facingRight = (targetX > x);
        float scaleX = facingRight ? 1.0f : -1.0f;

        // === 攻击动画：蛇头弹射 ===
        float lunge = 0;
        if (currentState == State.ATTACKING) {
            long now = System.currentTimeMillis();
            if (isWindingUp) {
                lunge = -getWindUpProgress() * (size / 4f);
            } else {
                float p = Math.min(1.0f, (now - getLastAttackTime()) / 180f);
                lunge = (1 - p) * (size / 2.5f);
            }
        }

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = System.currentTimeMillis() - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 3f;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }
        cx += vibX;
        cy += vibY;

        canvas.save();
        canvas.scale(scaleX, 1.0f, cx, cy);

        // --- 第三段：长长的尾巴 (从身体后方向后延伸) ---
        paint.setColor(Color.rgb(50, 180, 50));
        paint.setStrokeWidth(size / 10f); // 尾巴非常细
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        Path tail = new Path();
        // 尾巴起点在身体后下方
        float tailBaseX = cx - size/4f;
        float tailBaseY = cy + size/6f;
        tail.moveTo(tailBaseX, tailBaseY);
        // 尾巴向后拖行，末端尖细
        tail.quadTo(cx - size/1.2f, cy + size/4f, cx - size/1.6f, cy + size/3f);
        canvas.drawPath(tail, paint);

        // --- 第二段：修长的身体 (S型连接尾巴和脖子) ---
        paint.setStrokeWidth(size / 5f); // 身体变粗
        Path body = new Path();
        body.moveTo(tailBaseX, tailBaseY); // 接尾巴
        // S型向上弯曲
        body.quadTo(cx - size/8f, cy - size/8f, cx, cy - size/3f);
        canvas.drawPath(body, paint);

        // --- 第一段：颈部与头部 (攻击时前探) ---
        paint.setStrokeWidth(size / 4f); // 颈部最粗
        Path neck = new Path();
        neck.moveTo(cx, cy - size/3f); // 接身体
        neck.quadTo(cx + size/4f + lunge * 0.5f, cy - size/1.2f, cx + size/2.5f + lunge, cy - size/1.5f);
        canvas.drawPath(neck, paint);

        // 2. 背部花纹（点缀在身体上）
        paint.setColor(Color.rgb(20, 100, 20));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx - size/2f, cy + size/4f, size/18f, paint); // 尾部斑纹
        canvas.drawCircle(cx - size/10f, cy, size/14f, paint); // 身体中段斑纹
        canvas.drawCircle(cx + size/10f + lunge * 0.5f, cy - size/2.5f, size/12f, paint); // 颈部斑纹

        // 3. 头部（位于顶端，攻击时前探）
        paint.setColor(Color.rgb(40, 150, 40));
        paint.setStyle(Paint.Style.FILL);
        float headX = cx + size/2.5f + lunge;
        float headY = cy - size/1.5f;

        Path head = new Path();
        head.moveTo(headX - size/10f, headY + size/15f);
        head.lineTo(headX - size/12f, headY - size/15f);
        head.lineTo(headX + size/12f, headY - size/12f);  // 鼻尖
        head.lineTo(headX + size/10f, headY + size/15f);
        head.close();
        canvas.drawPath(head, paint);

        // 4. 眼睛
        paint.setColor(Color.YELLOW);
        canvas.drawOval(headX - size/18f, headY - size/25f, headX - size/28f, headY - size/50f, paint);
        paint.setColor(Color.BLACK);
        canvas.drawRect(headX - size/20f, headY - size/22f, headX - size/22f, headY - size/45f, paint);

        // 5. 动态信子（攻击时更频繁吐出）
        long time = System.currentTimeMillis();
        long tongueInterval = (currentState == State.ATTACKING) ? 80 : 150;
        if ((time / tongueInterval) % 4 == 0) {
            paint.setStrokeWidth(1.5f);
            paint.setColor(Color.RED);
            float tipX = headX + size/12f;
            float tipY = headY - size/20f;
            canvas.drawLine(tipX, tipY, tipX + 15, tipY - 8, paint);
            canvas.drawLine(tipX + 15, tipY - 8, tipX + 19, tipY - 11, paint);
            canvas.drawLine(tipX + 15, tipY - 8, tipX + 19, tipY - 5, paint);
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

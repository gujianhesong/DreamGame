package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.system.MapSystem;
import com.game.dream.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Tiger enemy - stronger and more aggressive than wolf
 */
public class Tiger extends Enemy {

    public Tiger(float x, float y) {
        super(x, y, 120);
        attackCooldown = 2000; // Faster attacks than wolf
        windUpDuration = 500; // 老虎扑击前摇，需要足够时间冲到玩家面前
        addAvailableAttackType(AttackType.POUNCE); // 老虎会猛扑
        setAttackShape(AttackShape.ARC); // 虎掌拍击 - 扇形

        EnemyPropertyExtra enemyPropertyExtra = new EnemyPropertyExtra();
        enemyPropertyExtra.detectionRange = 400;
        enemyPropertyExtra.attackRange = 250;
        enemyPropertyExtra.rewardExp = 100;
        enemyPropertyExtra.rewardMoney = 100;
        setPropertyExtra(enemyPropertyExtra);

        setProperty(500, 80, 60, 80, 40);

        if (Math.random() < 0.02) {
            //BOSS
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;

            setProperty(500 * 50, 640, 480, 350, 350);
        } else if (Math.random() < 0.07) {
            //精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;

            setProperty(500 * 10, 320, 240, 180, 180);
        } else if (Math.random() < 0.30) {
            //首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);

            setProperty(500 * 3, 160, 120, 100, 80);
        }

        // 精英/BOSS老虎可以使用环绕斩击
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
        }

        // BOSS老虎可以使用跳跃砸击
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
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

    @Override
    protected void performAttack() {
        // Tiger has a powerful attack
        // Damage will be handled by GameEngine
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        paint.setAntiAlias(true);

        float screenX = x + offsetX;
        float screenY = y + offsetY;
        float scale = size / 40.0f;

        // Determine facing direction
        boolean facingRight = targetX > x;

        if (facingRight) {
            drawFacingRight(canvas, paint, screenX, screenY, scale);
        } else {
            drawFacingLeft(canvas, paint, screenX, screenY, scale);
        }
    }

    /**
     * Draw tiger facing right
     */
    private void drawFacingRight(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        // Body (orange)
        paint.setColor(Color.rgb(255, 165, 0));
        Path body = new Path();
        body.moveTo(cx - 15 * scale, cy - 8 * scale + bobOffset);
        body.lineTo(cx + 15 * scale, cy - 8 * scale + bobOffset);
        body.lineTo(cx + 12 * scale, cy + 10 * scale + bobOffset);
        body.lineTo(cx - 12 * scale, cy + 10 * scale + bobOffset);
        body.close();
        canvas.drawPath(body, paint);

        // Black stripes on body
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2 * scale);
        canvas.drawLine(cx - 5 * scale, cy - 5 * scale + bobOffset,
                cx - 5 * scale, cy + 5 * scale + bobOffset, paint);
        canvas.drawLine(cx, cy - 6 * scale + bobOffset,
                cx, cy + 6 * scale + bobOffset, paint);
        canvas.drawLine(cx + 5 * scale, cy - 5 * scale + bobOffset,
                cx + 5 * scale, cy + 5 * scale + bobOffset, paint);

        // Head
        paint.setColor(Color.rgb(255, 165, 0));
        canvas.drawCircle(cx + 16 * scale, cy - 10 * scale + bobOffset, 9 * scale, paint);

        // Face markings (white)
        paint.setColor(Color.rgb(255, 240, 200));
        Path faceWhite = new Path();
        faceWhite.moveTo(cx + 12 * scale, cy - 8 * scale + bobOffset);
        faceWhite.lineTo(cx + 20 * scale, cy - 8 * scale + bobOffset);
        faceWhite.lineTo(cx + 16 * scale, cy - 4 * scale + bobOffset);
        faceWhite.close();
        canvas.drawPath(faceWhite, paint);

        // Eyes (intense yellow/orange)
        if (currentState == State.CHASING || currentState == State.ATTACKING) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.rgb(255, 200, 0));
        }
        canvas.drawCircle(cx + 14 * scale, cy - 11 * scale + bobOffset, 2 * scale, paint);
        canvas.drawCircle(cx + 19 * scale, cy - 11 * scale + bobOffset, 2 * scale, paint);

        // Nose
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx + 16 * scale, cy - 7 * scale + bobOffset, 2 * scale, paint);

        // Ears
        paint.setColor(Color.rgb(255, 165, 0));
        Path ear1 = new Path();
        ear1.moveTo(cx + 10 * scale, cy - 16 * scale + bobOffset);
        ear1.lineTo(cx + 12 * scale, cy - 22 * scale + bobOffset);
        ear1.lineTo(cx + 15 * scale, cy - 16 * scale + bobOffset);
        ear1.close();
        canvas.drawPath(ear1, paint);

        Path ear2 = new Path();
        ear2.moveTo(cx + 17 * scale, cy - 16 * scale + bobOffset);
        ear2.lineTo(cx + 20 * scale, cy - 22 * scale + bobOffset);
        ear2.lineTo(cx + 22 * scale, cy - 15 * scale + bobOffset);
        ear2.close();
        canvas.drawPath(ear2, paint);

        // Inner ears (pink)
        paint.setColor(Color.rgb(255, 180, 180));
        canvas.drawCircle(cx + 12 * scale, cy - 18 * scale + bobOffset, 2 * scale, paint);
        canvas.drawCircle(cx + 20 * scale, cy - 18 * scale + bobOffset, 2 * scale, paint);

        // Legs (thicker than wolf)
        paint.setColor(Color.rgb(255, 165, 0));
        float legOffset1 = (float) Math.sin(animFrame * Math.PI / 2) * 4 * scale;
        float legOffset2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 4 * scale;

        // Front legs
        canvas.drawRect(cx + 8 * scale, cy + 6 * scale + bobOffset + legOffset1,
                cx + 12 * scale, cy + 15 * scale + bobOffset, paint);
        canvas.drawRect(cx + 13 * scale, cy + 6 * scale + bobOffset + legOffset2,
                cx + 17 * scale, cy + 15 * scale + bobOffset, paint);

        // Back legs
        canvas.drawRect(cx - 10 * scale, cy + 6 * scale + bobOffset + legOffset2,
                cx - 6 * scale, cy + 15 * scale + bobOffset, paint);
        canvas.drawRect(cx - 5 * scale, cy + 6 * scale + bobOffset + legOffset1,
                cx - 1 * scale, cy + 15 * scale + bobOffset, paint);

        // Stripes on legs
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1.5f * scale);
        canvas.drawLine(cx + 9 * scale, cy + 8 * scale + bobOffset + legOffset1,
                cx + 11 * scale, cy + 8 * scale + bobOffset + legOffset1, paint);
        canvas.drawLine(cx + 14 * scale, cy + 8 * scale + bobOffset + legOffset2,
                cx + 16 * scale, cy + 8 * scale + bobOffset + legOffset2, paint);

        // Tail (long with stripes)
        paint.setColor(Color.rgb(255, 165, 0));
        Path tail = new Path();
        tail.moveTo(cx - 15 * scale, cy - 5 * scale + bobOffset);
        tail.quadTo(cx - 22 * scale, cy - 10 * scale + bobOffset,
                cx - 20 * scale, cy - 16 * scale + bobOffset);
        tail.lineTo(cx - 17 * scale, cy - 15 * scale + bobOffset);
        tail.quadTo(cx - 18 * scale, cy - 8 * scale + bobOffset,
                cx - 12 * scale, cy - 3 * scale + bobOffset);
        tail.close();
        canvas.drawPath(tail, paint);

        // Tail stripes
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2 * scale);
        canvas.drawLine(cx - 18 * scale, cy - 8 * scale + bobOffset,
                cx - 16 * scale, cy - 9 * scale + bobOffset, paint);
        canvas.drawLine(cx - 20 * scale, cy - 12 * scale + bobOffset,
                cx - 18 * scale, cy - 13 * scale + bobOffset, paint);
    }

    /**
     * Draw tiger facing left (mirror of right)
     */
    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        canvas.save();
        canvas.scale(-1, 1, cx, cy);
        drawFacingRight(canvas, paint, cx, cy, scale);
        canvas.restore();
    }

}

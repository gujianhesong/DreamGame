package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.utils.Utils;

import java.util.List;

/**
 * Wolf enemy that attacks player when nearby
 */
public class Wolf extends Enemy {

    public Wolf(float x, float y) {
        super(x, y, 80);
        attackCooldown = 2300;
        setAttackShape(AttackShape.ARC); // 狼爪横扫 - 扇形

        EnemyPropertyExtra enemyPropertyExtra = new EnemyPropertyExtra();
        enemyPropertyExtra.detectionRange = 250;
        enemyPropertyExtra.attackRange = 110;
        enemyPropertyExtra.rewardExp = 100;
        enemyPropertyExtra.rewardMoney = 100;
        setPropertyExtra(enemyPropertyExtra);

        setProperty(350, 60, 50, 60, 30);

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

        // 首领/精英/BOSS狼可以使用连续爪击
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.COMBO);
            comboHitCount = (enemyLevel == EnemyLevel.BOSS) ? 4 : (enemyLevel == EnemyLevel.ELITE) ? 3 : 2;
        }

        // 精英/BOSS狼可以使用闪现突击
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.BLINK_STRIKE);
        }

        // BOSS狼可以使用跳跃砸击
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
        // Wolf attack logic - deal damage to player
        // This will be handled by GameEngine
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
     * Draw wolf facing right
     */
    private void drawFacingRight(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        // === 攻击动画：前扑撕咬 ===
        float lunge = 0;
        if (currentState == State.ATTACKING) {
            long now = System.currentTimeMillis();
            if (isWindingUp) {
                lunge = -getWindUpProgress() * 5 * scale;
            } else {
                float p = Math.min(1.0f, (now - getLastAttackTime()) / 200f);
                lunge = (1 - p) * 10 * scale;
            }
        }

        // Body (gray-brown)
        paint.setColor(Color.rgb(139, 119, 101));
        Path body = new Path();
        body.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
        body.lineTo(cx + 10 * scale, cy - 5 * scale + bobOffset);
        body.lineTo(cx + 8 * scale, cy + 8 * scale + bobOffset);
        body.lineTo(cx - 10 * scale, cy + 8 * scale + bobOffset);
        body.close();
        canvas.drawPath(body, paint);

        // Head (攻击时前伸)
        paint.setColor(Color.rgb(160, 140, 120));
        canvas.drawCircle(cx + 12 * scale + lunge, cy - 8 * scale + bobOffset, 7 * scale, paint);

        // Snout
        paint.setColor(Color.rgb(180, 160, 140));
        Path snout = new Path();
        snout.moveTo(cx + 16 * scale + lunge, cy - 6 * scale + bobOffset);
        snout.lineTo(cx + 22 * scale + lunge, cy - 5 * scale + bobOffset);
        snout.lineTo(cx + 16 * scale + lunge, cy - 4 * scale + bobOffset);
        snout.close();
        canvas.drawPath(snout, paint);

        // Nose
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx + 21 * scale + lunge, cy - 5 * scale + bobOffset, 1.5f * scale, paint);

        // Eyes (red when aggressive)
        if (currentState == State.CHASING || currentState == State.ATTACKING) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.YELLOW);
        }
        canvas.drawCircle(cx + 13 * scale + lunge, cy - 9 * scale + bobOffset, 1.5f * scale, paint);

        // Ears (攻击时前伸)
        paint.setColor(Color.rgb(139, 119, 101));
        Path ear1 = new Path();
        ear1.moveTo(cx + 8 * scale + lunge, cy - 12 * scale + bobOffset);
        ear1.lineTo(cx + 10 * scale + lunge, cy - 18 * scale + bobOffset);
        ear1.lineTo(cx + 13 * scale + lunge, cy - 12 * scale + bobOffset);
        ear1.close();
        canvas.drawPath(ear1, paint);

        Path ear2 = new Path();
        ear2.moveTo(cx + 14 * scale + lunge, cy - 12 * scale + bobOffset);
        ear2.lineTo(cx + 16 * scale + lunge, cy - 18 * scale + bobOffset);
        ear2.lineTo(cx + 18 * scale + lunge, cy - 11 * scale + bobOffset);
        ear2.close();
        canvas.drawPath(ear2, paint);

        // Legs
        paint.setColor(Color.rgb(120, 100, 80));
        float legOffset1 = (float) Math.sin(animFrame * Math.PI / 2) * 3 * scale;
        float legOffset2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 3 * scale;

        // Front legs (攻击时前伸)
        canvas.drawRect(cx + 6 * scale + lunge, cy + 5 * scale + bobOffset + legOffset1,
                cx + 9 * scale + lunge, cy + 12 * scale + bobOffset, paint);
        canvas.drawRect(cx + 10 * scale + lunge, cy + 5 * scale + bobOffset + legOffset2,
                cx + 13 * scale + lunge, cy + 12 * scale + bobOffset, paint);

        // Back legs
        canvas.drawRect(cx - 8 * scale, cy + 5 * scale + bobOffset + legOffset2,
                cx - 5 * scale, cy + 12 * scale + bobOffset, paint);
        canvas.drawRect(cx - 4 * scale, cy + 5 * scale + bobOffset + legOffset1,
                cx - 1 * scale, cy + 12 * scale + bobOffset, paint);

        // Tail
        paint.setColor(Color.rgb(139, 119, 101));
        Path tail = new Path();
        tail.moveTo(cx - 12 * scale, cy - 3 * scale + bobOffset);
        tail.quadTo(cx - 18 * scale, cy - 8 * scale + bobOffset,
                cx - 16 * scale, cy - 12 * scale + bobOffset);
        tail.lineTo(cx - 14 * scale, cy - 11 * scale + bobOffset);
        tail.quadTo(cx - 15 * scale, cy - 6 * scale + bobOffset,
                cx - 10 * scale, cy - 2 * scale + bobOffset);
        tail.close();
        canvas.drawPath(tail, paint);
    }

    /**
     * Draw wolf facing left (mirror of right)
     */
    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        canvas.save();
        canvas.scale(-1, 1, cx, cy);
        drawFacingRight(canvas, paint, cx, cy, scale);
        canvas.restore();
    }

}
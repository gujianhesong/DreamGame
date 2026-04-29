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
        super(x, y, 70, 200, 50, 30, 150);
        attackCooldown = 1500;

        int health = Utils.getWaveValueInt(200, 0.2f);
        this.maxHealth = health;
        this.health = health;
        this.attackDamage = 30;
        this.defense = 30;
        this.speed = 30;
        this.mana = 30;

        if (Math.random() < 0.05) {
            //精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;

            health = Utils.getWaveValueInt(200 * 10, 0.2f);
            this.maxHealth = health;
            this.health = health;
            this.attackDamage = 60;
            this.defense = 60;
            this.speed = 60;
            this.mana = 60;
        } else if (Math.random() < 0.25) {
            //首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);

            health = Utils.getWaveValueInt(200 * 3, 0.2f);
            this.maxHealth = health;
            this.health = health;
            this.attackDamage = 40;
            this.defense = 40;
            this.speed = 40;
            this.mana = 40;
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        possibleDrops.clear();

        addPossibleDrop(ItemCreator.createHp1_1_Siyehua());
        addPossibleDrop(ItemCreator.createHp1_2_QiyeLian());
        addPossibleDrop(ItemCreator.createMp1_1_Foshou());
        addPossibleDrop(ItemCreator.createMp1_2_Xiangye());

        addPossibleDrop(EquipCreator.createEquip(0, null));

        return possibleDrops;
    }

    @Override
    protected void updateIdle(long deltaTime, float deltaSeconds, int[][] map, int mapWidth, int mapHeight) {
        long currentTime = System.currentTimeMillis();

        // Change direction every 2-4 seconds
        if (currentTime - stateTimer > 2000 + (int) (Math.random() * 2000)) {
            // Pick a random nearby position
            float angle = (float) (Math.random() * Math.PI * 2);
            float distance = 50 + (float) (Math.random() * 100);

            targetX = x + (float) Math.cos(angle) * distance;
            targetY = y + (float) Math.sin(angle) * distance;

            // Clamp to map bounds
            targetX = Math.max(size, Math.min(targetX, mapWidth - size));
            targetY = Math.max(size, Math.min(targetY, mapHeight - size));

            stateTimer = currentTime;
        }

        // Move towards target
        moveToTarget(deltaSeconds);
    }

    @Override
    protected void performAttack() {
        // Wolf attack logic - deal damage to player
        // This will be handled by GameEngine
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float screenX = x + offsetX;
        float screenY = y + offsetY;
        float scale = size / 30.0f;

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
        // Body (gray-brown)
        paint.setColor(Color.rgb(139, 119, 101));
        Path body = new Path();
        body.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
        body.lineTo(cx + 10 * scale, cy - 5 * scale + bobOffset);
        body.lineTo(cx + 8 * scale, cy + 8 * scale + bobOffset);
        body.lineTo(cx - 10 * scale, cy + 8 * scale + bobOffset);
        body.close();
        canvas.drawPath(body, paint);

        // Head
        paint.setColor(Color.rgb(160, 140, 120));
        canvas.drawCircle(cx + 12 * scale, cy - 8 * scale + bobOffset, 7 * scale, paint);

        // Snout
        paint.setColor(Color.rgb(180, 160, 140));
        Path snout = new Path();
        snout.moveTo(cx + 16 * scale, cy - 6 * scale + bobOffset);
        snout.lineTo(cx + 22 * scale, cy - 5 * scale + bobOffset);
        snout.lineTo(cx + 16 * scale, cy - 4 * scale + bobOffset);
        snout.close();
        canvas.drawPath(snout, paint);

        // Nose
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx + 21 * scale, cy - 5 * scale + bobOffset, 1.5f * scale, paint);

        // Eyes (red when aggressive)
        if (currentState == State.CHASING || currentState == State.ATTACKING) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.YELLOW);
        }
        canvas.drawCircle(cx + 13 * scale, cy - 9 * scale + bobOffset, 1.5f * scale, paint);

        // Ears
        paint.setColor(Color.rgb(139, 119, 101));
        Path ear1 = new Path();
        ear1.moveTo(cx + 8 * scale, cy - 12 * scale + bobOffset);
        ear1.lineTo(cx + 10 * scale, cy - 18 * scale + bobOffset);
        ear1.lineTo(cx + 13 * scale, cy - 12 * scale + bobOffset);
        ear1.close();
        canvas.drawPath(ear1, paint);

        Path ear2 = new Path();
        ear2.moveTo(cx + 14 * scale, cy - 12 * scale + bobOffset);
        ear2.lineTo(cx + 16 * scale, cy - 18 * scale + bobOffset);
        ear2.lineTo(cx + 18 * scale, cy - 11 * scale + bobOffset);
        ear2.close();
        canvas.drawPath(ear2, paint);

        // Legs
        paint.setColor(Color.rgb(120, 100, 80));
        float legOffset1 = (float) Math.sin(animFrame * Math.PI / 2) * 3 * scale;
        float legOffset2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 3 * scale;

        // Front legs
        canvas.drawRect(cx + 6 * scale, cy + 5 * scale + bobOffset + legOffset1,
                cx + 9 * scale, cy + 12 * scale + bobOffset, paint);
        canvas.drawRect(cx + 10 * scale, cy + 5 * scale + bobOffset + legOffset2,
                cx + 13 * scale, cy + 12 * scale + bobOffset, paint);

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
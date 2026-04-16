package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Tiger enemy - stronger and more aggressive than wolf
 */
public class Tiger extends Enemy {

    public Tiger(float x, float y) {
        super(x, y, 100, 100, 120, 250, 60); // health=50, speed=100, detection=250, attack=60
        attackCooldown = 1200; // Faster attacks than wolf
    }

    @Override
    protected void updateIdle(long deltaTime, float deltaSeconds, int[][] map, int mapWidth, int mapHeight) {
        long currentTime = System.currentTimeMillis();

        // Tigers are more active - change direction every 1-3 seconds
        if (currentTime - stateTimer > 1000 + (int)(Math.random() * 2000)) {
            // Pick a random nearby position
            float angle = (float)(Math.random() * Math.PI * 2);
            float distance = 80 + (float)(Math.random() * 150);

            targetX = x + (float)Math.cos(angle) * distance;
            targetY = y + (float)Math.sin(angle) * distance;

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
        // Tiger has a powerful attack
        // Damage will be handled by GameEngine
    }

    @Override
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        Paint paint = new Paint();
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

        // Draw health bar
        drawHealthBar(canvas, screenX, screenY, scale);
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
        float legOffset1 = (float)Math.sin(animFrame * Math.PI / 2) * 4 * scale;
        float legOffset2 = (float)Math.sin((animFrame + 2) * Math.PI / 2) * 4 * scale;

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

package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Renderer for player character - handles all drawing logic
 */
public class PlayerRenderer {

    private Player player;

    public PlayerRenderer(Player player) {
        this.player = player;
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        // Check if should blink when invincible
        if (player.isCurrentlyInvincible()) {
            long currentTime = System.currentTimeMillis();
            long timeLeft = player.getInvincibleEndTime() - currentTime;

            // Blink rapidly in the first second, then slower
            int blinkInterval = timeLeft > 1000 ? 100 : 50;
            if ((currentTime / blinkInterval) % 2 == 0) {
                return; // Skip drawing this frame (blink effect)
            }
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float screenX = player.getX() + offsetX;
        float screenY = player.getY() + offsetY;
        float scale = player.getSize() / 40f;

        // Calculate animation parameters based on state
        boolean isMoving = player.isMoving();;
        int walkCycle = player.getWalkCycle();
        int facingDirection = player.getFacingDirection();
        int size = player.getSize();
        float bobOffset, legOffset, armSwing, breathScale;

        if (isMoving) {
            // Walking animation
            bobOffset = (float) Math.sin(walkCycle * Math.PI / 30) * 2 * scale;
            legOffset = (float) Math.sin(walkCycle * Math.PI / 15) * 3 * scale;
            armSwing = (float) Math.sin(walkCycle * Math.PI / 15) * 4 * scale;
            breathScale = 1.0f;
        } else {
            // Idle animation - breathing effect
            bobOffset = (float) Math.sin(walkCycle * Math.PI / 60) * 1 * scale;
            legOffset = 0;
            armSwing = (float) Math.sin(walkCycle * Math.PI / 60) * 1.5f * scale;
            breathScale = 1.0f + (float) Math.sin(walkCycle * Math.PI / 60) * 0.02f;
        }

        // Draw mystical aura/glow around character (pulsing effect)
        float auraPulse = (float) Math.sin(walkCycle * Math.PI / 40) * 0.1f + 0.4f;
        paint.setColor(Color.argb((int) (auraPulse * 100), 100, 181, 246));
        canvas.drawCircle(screenX, screenY - 5 * scale + bobOffset * 0.5f, size * 0.65f * breathScale, paint);

        switch (facingDirection) {
            case 0: // Facing DOWN
                drawFacingDown(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving);
                break;
            case 1: // Facing UP
                drawFacingUp(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving);
                break;
            case 2: // Facing LEFT
                drawFacingLeft(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving);
                break;
            case 3: // Facing RIGHT
                drawFacingRight(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving);
                break;
        }

        // Floating energy particles (always animated)
        drawEnergyParticles(canvas, paint, screenX, screenY, scale, isMoving);
    }

    private void drawFacingDown(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                float bobOffset, float legOffset, float armSwing, float breathScale, boolean isMoving) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // Sword on back
        Path scabbard = new Path();
        scabbard.moveTo(cx + 7 * scale, cy - 18 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx + 10 * scale, cy - 16 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx + 5 * scale, cy + 8 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx + 2 * scale, cy + 6 * scale + bobOffset * 0.3f);
        scabbard.close();

        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawPath(scabbard, paint);

        // Scabbard decoration
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx + 5.5f * scale, cy - 10 * scale + bobOffset * 0.3f,
                cx + 7.5f * scale, cy - 8 * scale + bobOffset * 0.3f, paint);

        // Sword hilt
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx + 6.5f * scale, cy - 19 * scale + bobOffset * 0.3f,
                cx + 11 * scale, cy - 17.5f * scale + bobOffset * 0.3f, paint);

        // === LEGS ===
        if (isMoving) {
            // Walking - legs move
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 7 * scale, cy + 15 * scale + bobOffset,
                    cx - 1 * scale, cy + 22 * scale + bobOffset + legOffset, paint);
            canvas.drawRect(cx + 1 * scale, cy + 15 * scale + bobOffset,
                    cx + 7 * scale, cy + 22 * scale + bobOffset - legOffset, paint);

            // Shoes
            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 8 * scale, cy + 20 * scale + bobOffset + legOffset,
                    cx, cy + 23 * scale + bobOffset + legOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx, cy + 20 * scale + bobOffset - legOffset,
                    cx + 8 * scale, cy + 23 * scale + bobOffset - legOffset,
                    2 * scale, 2 * scale, paint);
        } else {
            // Idle - standing still with slight weight shift
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 7 * scale, cy + 15 * scale + bobOffset,
                    cx - 1 * scale, cy + 22 * scale + bobOffset, paint);
            canvas.drawRect(cx + 1 * scale, cy + 15 * scale + bobOffset,
                    cx + 7 * scale, cy + 22 * scale + bobOffset, paint);

            // Shoes
            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 8 * scale, cy + 20 * scale + bobOffset,
                    cx, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx, cy + 20 * scale + bobOffset,
                    cx + 8 * scale, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
        }

        // === BODY (Robes) with breathing effect ===
        Path robeBody = new Path();
        robeBody.moveTo(cx - 12 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 12 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 14 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.quadTo(cx, cy + 18 * scale + bobOffset, cx - 14 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.close();

        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawPath(robeBody, paint);

        // Robe shadow
        Path robeShadow = new Path();
        robeShadow.moveTo(cx - 12 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeShadow.lineTo(cx, cy - 8 * scale + bobOffset);
        robeShadow.lineTo(cx, cy + 17 * scale + bobOffset);
        robeShadow.lineTo(cx - 14 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeShadow.close();

        paint.setColor(Color.argb(30, 0, 0, 100));
        canvas.drawPath(robeShadow, paint);

        // Blue sash/belt
        paint.setColor(Color.rgb(30, 136, 229));
        canvas.drawRect(cx - 13 * scale * breathScale, cy + 2 * scale + bobOffset,
                cx + 13 * scale * breathScale, cy + 5 * scale + bobOffset, paint);

        // Sash knot
        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawCircle(cx, cy + 3.5f * scale + bobOffset, 2.5f * scale, paint);

        // === ARMS ===
        if (isMoving) {
            // Walking - arms swing
            Path leftArm = new Path();
            leftArm.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 18 * scale + armSwing, cy + 5 * scale + bobOffset,
                    cx - 15 * scale + armSwing * 0.5f, cy + 12 * scale + bobOffset);
            leftArm.lineTo(cx - 12 * scale + armSwing * 0.5f, cy + 11 * scale + bobOffset);
            leftArm.quadTo(cx - 13 * scale, cy + 5 * scale + bobOffset,
                    cx - 10 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 12 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 18 * scale - armSwing, cy + 5 * scale + bobOffset,
                    cx + 15 * scale - armSwing * 0.5f, cy + 12 * scale + bobOffset);
            rightArm.lineTo(cx + 12 * scale - armSwing * 0.5f, cy + 11 * scale + bobOffset);
            rightArm.quadTo(cx + 13 * scale, cy + 5 * scale + bobOffset,
                    cx + 10 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);
        } else {
            // Idle - arms at sides with gentle sway
            Path leftArm = new Path();
            leftArm.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 16 * scale, cy + 2 * scale + bobOffset + armSwing * 0.3f,
                    cx - 14 * scale, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 11 * scale, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale, cy + 2 * scale + bobOffset,
                    cx - 10 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 12 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 16 * scale, cy + 2 * scale + bobOffset - armSwing * 0.3f,
                    cx + 14 * scale, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 11 * scale, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 12 * scale, cy + 2 * scale + bobOffset,
                    cx + 10 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);
        }

        // === HEAD ===
        // Draw hair FIRST (behind face)
        Path hair = new Path();
        hair.moveTo(cx - 9 * scale * breathScale, cy - 17 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 11 * scale, cy - 23 * scale + bobOffset * 0.3f, cx - 7 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx, cy - 27 * scale + bobOffset * 0.3f, cx + 7 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx + 11 * scale, cy - 23 * scale + bobOffset * 0.3f, cx + 9 * scale * breathScale, cy - 17 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 8 * scale, cy - 14 * scale + bobOffset * 0.5f, cx + 7 * scale, cy - 12 * scale + bobOffset * 0.5f);
        hair.lineTo(cx + 6 * scale, cy - 14 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 4 * scale, cy - 16 * scale + bobOffset * 0.3f, cx, cy - 16 * scale + bobOffset * 0.3f);
        hair.quadTo(cx - 4 * scale, cy - 16 * scale + bobOffset * 0.3f, cx - 6 * scale, cy - 14 * scale + bobOffset * 0.5f);
        hair.lineTo(cx - 7 * scale, cy - 12 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 8 * scale, cy - 14 * scale + bobOffset * 0.5f, cx - 9 * scale * breathScale, cy - 17 * scale + bobOffset * 0.5f);
        hair.close();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hair, paint);

        // Draw face SECOND (on top of hair)
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(255, 224, 178));
        canvas.drawCircle(cx, cy - 15 * scale + bobOffset * 0.5f, 9 * scale * breathScale, paint);

        // Hair highlight (on top)
        Path hairHighlight = new Path();
        hairHighlight.moveTo(cx - 5 * scale, cy - 23 * scale + bobOffset * 0.3f);
        hairHighlight.quadTo(cx, cy - 25 * scale + bobOffset * 0.3f, cx + 5 * scale, cy - 23 * scale + bobOffset * 0.3f);
        hairHighlight.quadTo(cx, cy - 22 * scale + bobOffset * 0.3f, cx - 5 * scale, cy - 23 * scale + bobOffset * 0.3f);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(80, 255, 255, 255));
        canvas.drawPath(hairHighlight, paint);

        int walkCycle = player.getWalkCycle();
        // Eyes with blinking effect (idle only)
        if (!isMoving && (walkCycle % 120 < 115)) {
            // Eyes open
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawCircle(cx - 3.5f * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);
            canvas.drawCircle(cx + 3.5f * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);

            // Eye highlights
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx - 3 * scale, cy - 15.5f * scale + bobOffset * 0.5f, 0.7f * scale, paint);
            canvas.drawCircle(cx + 4 * scale, cy - 15.5f * scale + bobOffset * 0.5f, 0.7f * scale, paint);
        } else if (!isMoving) {
            // Blinking - eyes closed
            paint.setStrokeWidth(1.5f * scale);
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawLine(cx - 5 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx - 2 * scale, cy - 15 * scale + bobOffset * 0.5f, paint);
            canvas.drawLine(cx + 2 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx + 5 * scale, cy - 15 * scale + bobOffset * 0.5f, paint);
            paint.setStrokeWidth(1);
        } else {
            // Walking - eyes always open
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawCircle(cx - 3.5f * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);
            canvas.drawCircle(cx + 3.5f * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx - 3 * scale, cy - 15.5f * scale + bobOffset * 0.5f, 0.7f * scale, paint);
            canvas.drawCircle(cx + 4 * scale, cy - 15.5f * scale + bobOffset * 0.5f, 0.7f * scale, paint);
        }

        // Eyebrows
        paint.setStrokeWidth(1.5f * scale);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawLine(cx - 5 * scale, cy - 17.5f * scale + bobOffset * 0.5f,
                cx - 2 * scale, cy - 18 * scale + bobOffset * 0.5f, paint);
        canvas.drawLine(cx + 2 * scale, cy - 18 * scale + bobOffset * 0.5f,
                cx + 5 * scale, cy - 17.5f * scale + bobOffset * 0.5f, paint);
        paint.setStrokeWidth(1);

        // Mouth
        paint.setStrokeWidth(1.2f * scale);
        paint.setColor(Color.rgb(200, 100, 100));
        canvas.drawArc(cx - 3 * scale, cy - 12 * scale + bobOffset * 0.5f,
                cx + 3 * scale, cy - 9 * scale + bobOffset * 0.5f,
                200, 140, false, paint);
        paint.setStrokeWidth(1);

        // Jade ornament
        paint.setColor(Color.rgb(76, 175, 80));
        canvas.drawCircle(cx + 8.5f * scale, cy - 18.5f * scale + bobOffset * 0.3f, 1.5f * scale, paint);
    }

    private void drawFacingUp(Canvas canvas, Paint paint, float cx, float cy, float scale,
                              float bobOffset, float legOffset, float armSwing, float breathScale, boolean isMoving) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // Similar structure - implement idle animations for back view
        if (isMoving) {
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 7 * scale, cy + 15 * scale + bobOffset,
                    cx - 1 * scale, cy + 22 * scale + bobOffset + legOffset, paint);
            canvas.drawRect(cx + 1 * scale, cy + 15 * scale + bobOffset,
                    cx + 7 * scale, cy + 22 * scale + bobOffset - legOffset, paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 8 * scale, cy + 20 * scale + bobOffset + legOffset,
                    cx, cy + 23 * scale + bobOffset + legOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx, cy + 20 * scale + bobOffset - legOffset,
                    cx + 8 * scale, cy + 23 * scale + bobOffset - legOffset,
                    2 * scale, 2 * scale, paint);
        } else {
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 7 * scale, cy + 15 * scale + bobOffset,
                    cx - 1 * scale, cy + 22 * scale + bobOffset, paint);
            canvas.drawRect(cx + 1 * scale, cy + 15 * scale + bobOffset,
                    cx + 7 * scale, cy + 22 * scale + bobOffset, paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 8 * scale, cy + 20 * scale + bobOffset,
                    cx, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx, cy + 20 * scale + bobOffset,
                    cx + 8 * scale, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
        }

        // Body with breathing
        Path robeBody = new Path();
        robeBody.moveTo(cx - 12 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 12 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 14 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.quadTo(cx, cy + 18 * scale + bobOffset, cx - 14 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.close();

        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawPath(robeBody, paint);

        // Back detail
        paint.setColor(Color.argb(20, 0, 0, 100));
        canvas.drawLine(cx, cy - 8 * scale + bobOffset, cx, cy + 16 * scale + bobOffset, paint);

        // Blue sash
        paint.setColor(Color.rgb(30, 136, 229));
        canvas.drawRect(cx - 13 * scale * breathScale, cy + 2 * scale + bobOffset,
                cx + 13 * scale * breathScale, cy + 5 * scale + bobOffset, paint);

        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawCircle(cx, cy + 3.5f * scale + bobOffset, 2.5f * scale, paint);

        // Arms
        if (isMoving) {
            Path leftArm = new Path();
            leftArm.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 18 * scale + armSwing, cy + 5 * scale + bobOffset,
                    cx - 15 * scale + armSwing * 0.5f, cy + 12 * scale + bobOffset);
            leftArm.lineTo(cx - 12 * scale + armSwing * 0.5f, cy + 11 * scale + bobOffset);
            leftArm.quadTo(cx - 13 * scale, cy + 5 * scale + bobOffset,
                    cx - 10 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 12 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 18 * scale - armSwing, cy + 5 * scale + bobOffset,
                    cx + 15 * scale - armSwing * 0.5f, cy + 12 * scale + bobOffset);
            rightArm.lineTo(cx + 12 * scale - armSwing * 0.5f, cy + 11 * scale + bobOffset);
            rightArm.quadTo(cx + 13 * scale, cy + 5 * scale + bobOffset,
                    cx + 10 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);
        } else {
            Path leftArm = new Path();
            leftArm.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 16 * scale, cy + 2 * scale + bobOffset + armSwing * 0.3f,
                    cx - 14 * scale, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 11 * scale, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale, cy + 2 * scale + bobOffset,
                    cx - 10 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 12 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 16 * scale, cy + 2 * scale + bobOffset - armSwing * 0.3f,
                    cx + 14 * scale, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 11 * scale, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 12 * scale, cy + 2 * scale + bobOffset,
                    cx + 10 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);
        }

        // Hair (back view) with gentle movement
        Path hair = new Path();
        hair.moveTo(cx - 10 * scale * breathScale, cy - 15 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 12 * scale, cy - 22 * scale + bobOffset * 0.3f, cx - 8 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx, cy - 28 * scale + bobOffset * 0.3f, cx + 8 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx + 12 * scale, cy - 22 * scale + bobOffset * 0.3f, cx + 10 * scale * breathScale, cy - 15 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 11 * scale, cy - 8 * scale + bobOffset * 0.5f, cx + 9 * scale, cy - 5 * scale + bobOffset * 0.5f);
        hair.quadTo(cx, cy - 3 * scale + bobOffset * 0.5f, cx - 9 * scale, cy - 5 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 11 * scale, cy - 8 * scale + bobOffset * 0.5f, cx - 10 * scale * breathScale, cy - 15 * scale + bobOffset * 0.5f);
        hair.close();

        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hair, paint);

        // Hair flowing down
        Path hairBack = new Path();
        hairBack.moveTo(cx - 8 * scale, cy - 10 * scale + bobOffset * 0.5f);
        hairBack.quadTo(cx - 9 * scale, cy, cx - 7 * scale, cy + 8 * scale + bobOffset * 0.5f);
        hairBack.lineTo(cx + 7 * scale, cy + 8 * scale + bobOffset * 0.5f);
        hairBack.quadTo(cx + 9 * scale, cy, cx + 8 * scale, cy - 10 * scale + bobOffset * 0.5f);
        hairBack.close();

        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hairBack, paint);

        // Hair highlights
        paint.setColor(Color.argb(60, 255, 255, 255));
        canvas.drawCircle(cx - 4 * scale, cy - 20 * scale + bobOffset * 0.3f, 2 * scale, paint);
        canvas.drawCircle(cx + 4 * scale, cy - 20 * scale + bobOffset * 0.3f, 2 * scale, paint);

        // Sword on back
        Path scabbard = new Path();
        scabbard.moveTo(cx + 2 * scale, cy - 20 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx + 5 * scale, cy - 18 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx, cy + 10 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx - 3 * scale, cy + 8 * scale + bobOffset * 0.3f);
        scabbard.close();

        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawPath(scabbard, paint);

        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx - 0.5f * scale, cy - 12 * scale + bobOffset * 0.3f,
                cx + 1.5f * scale, cy - 10 * scale + bobOffset * 0.3f, paint);
        canvas.drawRect(cx - 1 * scale, cy - 2 * scale + bobOffset * 0.3f,
                cx + 1 * scale, cy + bobOffset * 0.3f, paint);

        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx + 1.5f * scale, cy - 21 * scale + bobOffset * 0.3f,
                cx + 6 * scale, cy - 19.5f * scale + bobOffset * 0.3f, paint);

        paint.setColor(Color.rgb(76, 175, 80));
        canvas.drawCircle(cx + 3.5f * scale, cy - 20.5f * scale + bobOffset * 0.3f, 2 * scale, paint);
    }

    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                float bobOffset, float legOffset, float armSwing, float breathScale, boolean isMoving) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // Implement similar idle animations for side view
        if (isMoving) {
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 6 * scale, cy + 15 * scale + bobOffset,
                    cx, cy + 22 * scale + bobOffset + legOffset, paint);
            canvas.drawRect(cx, cy + 15 * scale + bobOffset,
                    cx + 6 * scale, cy + 22 * scale + bobOffset - legOffset, paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 7 * scale, cy + 20 * scale + bobOffset + legOffset,
                    cx + 1 * scale, cy + 23 * scale + bobOffset + legOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx - 1 * scale, cy + 20 * scale + bobOffset - legOffset,
                    cx + 7 * scale, cy + 23 * scale + bobOffset - legOffset,
                    2 * scale, 2 * scale, paint);
        } else {
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 6 * scale, cy + 15 * scale + bobOffset,
                    cx, cy + 22 * scale + bobOffset, paint);
            canvas.drawRect(cx, cy + 15 * scale + bobOffset,
                    cx + 6 * scale, cy + 22 * scale + bobOffset, paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 7 * scale, cy + 20 * scale + bobOffset,
                    cx + 1 * scale, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx - 1 * scale, cy + 20 * scale + bobOffset,
                    cx + 7 * scale, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
        }

        // Body with breathing
        Path robeBody = new Path();
        robeBody.moveTo(cx - 8 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 8 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 10 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.quadTo(cx, cy + 18 * scale + bobOffset, cx - 10 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.close();

        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawPath(robeBody, paint);

        Path robeShadow = new Path();
        robeShadow.moveTo(cx - 8 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeShadow.lineTo(cx, cy - 8 * scale + bobOffset);
        robeShadow.lineTo(cx, cy + 17 * scale + bobOffset);
        robeShadow.lineTo(cx - 10 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeShadow.close();

        paint.setColor(Color.argb(30, 0, 0, 100));
        canvas.drawPath(robeShadow, paint);

        paint.setColor(Color.rgb(30, 136, 229));
        canvas.drawRect(cx - 9 * scale * breathScale, cy + 2 * scale + bobOffset,
                cx + 9 * scale * breathScale, cy + 5 * scale + bobOffset, paint);

        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawCircle(cx, cy + 3.5f * scale + bobOffset, 2 * scale, paint);

        // Arms
        if (isMoving) {
            Path frontArm = new Path();
            frontArm.moveTo(cx + 2 * scale, cy - 5 * scale + bobOffset);
            frontArm.quadTo(cx + 8 * scale - armSwing, cy + 5 * scale + bobOffset,
                    cx + 6 * scale - armSwing * 0.5f, cy + 12 * scale + bobOffset);
            frontArm.lineTo(cx + 3 * scale - armSwing * 0.5f, cy + 11 * scale + bobOffset);
            frontArm.quadTo(cx + 4 * scale, cy + 5 * scale + bobOffset,
                    cx + 2 * scale, cy - 3 * scale + bobOffset);
            frontArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(frontArm, paint);

            Path backArm = new Path();
            backArm.moveTo(cx - 6 * scale, cy - 5 * scale + bobOffset);
            backArm.quadTo(cx - 10 * scale + armSwing * 0.3f, cy + 5 * scale + bobOffset,
                    cx - 8 * scale + armSwing * 0.2f, cy + 10 * scale + bobOffset);
            backArm.lineTo(cx - 5 * scale + armSwing * 0.2f, cy + 9 * scale + bobOffset);
            backArm.quadTo(cx - 6 * scale, cy + 5 * scale + bobOffset,
                    cx - 6 * scale, cy - 3 * scale + bobOffset);
            backArm.close();

            paint.setColor(Color.rgb(245, 245, 245));
            canvas.drawPath(backArm, paint);
        } else {
            Path frontArm = new Path();
            frontArm.moveTo(cx + 2 * scale, cy - 5 * scale + bobOffset);
            frontArm.quadTo(cx + 7 * scale, cy + 3 * scale + bobOffset + armSwing * 0.2f,
                    cx + 5 * scale, cy + 11 * scale + bobOffset);
            frontArm.lineTo(cx + 2.5f * scale, cy + 10 * scale + bobOffset);
            frontArm.quadTo(cx + 3.5f * scale, cy + 3 * scale + bobOffset,
                    cx + 2 * scale, cy - 3 * scale + bobOffset);
            frontArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(frontArm, paint);

            Path backArm = new Path();
            backArm.moveTo(cx - 6 * scale, cy - 5 * scale + bobOffset);
            backArm.quadTo(cx - 9 * scale, cy + 3 * scale + bobOffset - armSwing * 0.2f,
                    cx - 7 * scale, cy + 10 * scale + bobOffset);
            backArm.lineTo(cx - 4.5f * scale, cy + 9 * scale + bobOffset);
            backArm.quadTo(cx - 5.5f * scale, cy + 3 * scale + bobOffset,
                    cx - 6 * scale, cy - 3 * scale + bobOffset);
            backArm.close();

            paint.setColor(Color.rgb(245, 245, 245));
            canvas.drawPath(backArm, paint);
        }

        // Head profile
        paint.setColor(Color.rgb(255, 224, 178));
        Path faceProfile = new Path();
        faceProfile.moveTo(cx - 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx - 8 * scale, cy - 15 * scale + bobOffset * 0.5f, cx - 7 * scale, cy - 12 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx - 6 * scale, cy - 10 * scale + bobOffset * 0.5f, cx - 4 * scale, cy - 10 * scale + bobOffset * 0.5f);
        faceProfile.lineTo(cx + 2 * scale, cy - 10 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx + 5 * scale, cy - 10 * scale + bobOffset * 0.5f, cx + 6 * scale, cy - 12 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx + 7 * scale, cy - 15 * scale + bobOffset * 0.5f, cx + 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx + 4 * scale, cy - 21 * scale + bobOffset * 0.3f, cx, cy - 21 * scale + bobOffset * 0.3f);
        faceProfile.quadTo(cx - 4 * scale, cy - 21 * scale + bobOffset * 0.3f, cx - 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        faceProfile.close();
        canvas.drawPath(faceProfile, paint);

        paint.setColor(Color.rgb(240, 210, 160));
        Path nose = new Path();
        nose.moveTo(cx + 5 * scale, cy - 14 * scale + bobOffset * 0.5f);
        nose.lineTo(cx + 7 * scale, cy - 13 * scale + bobOffset * 0.5f);
        nose.lineTo(cx + 5 * scale, cy - 12 * scale + bobOffset * 0.5f);
        nose.close();
        canvas.drawPath(nose, paint);

        // Hair
        Path hair = new Path();
        hair.moveTo(cx - 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 8 * scale, cy - 23 * scale + bobOffset * 0.3f, cx - 4 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx, cy - 26 * scale + bobOffset * 0.3f, cx + 4 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx + 7 * scale, cy - 23 * scale + bobOffset * 0.3f, cx + 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 7 * scale, cy - 12 * scale + bobOffset * 0.5f, cx + 8 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hair.lineTo(cx + 6 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 5 * scale, cy - 12 * scale + bobOffset * 0.5f, cx + 4 * scale, cy - 15 * scale + bobOffset * 0.5f);
        hair.quadTo(cx, cy - 16 * scale + bobOffset * 0.5f, cx - 4 * scale, cy - 15 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 6 * scale, cy - 14 * scale + bobOffset * 0.5f, cx - 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        hair.close();

        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hair, paint);

        Path hairFlow = new Path();
        hairFlow.moveTo(cx - 4 * scale, cy - 20 * scale + bobOffset * 0.3f);
        hairFlow.quadTo(cx - 8 * scale, cy - 15 * scale + bobOffset * 0.3f, cx - 7 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hairFlow.lineTo(cx - 5 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hairFlow.quadTo(cx - 6 * scale, cy - 15 * scale + bobOffset * 0.3f, cx - 4 * scale, cy - 20 * scale + bobOffset * 0.3f);
        hairFlow.close();

        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hairFlow, paint);

        int walkCycle = player.getWalkCycle();
        // Eye with blinking
        if (!isMoving && (walkCycle % 120 < 115)) {
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawOval(cx + 2 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx + 4 * scale, cy - 13.5f * scale + bobOffset * 0.5f, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx + 3 * scale, cy - 14.5f * scale + bobOffset * 0.5f, 0.6f * scale, paint);
        } else if (!isMoving) {
            paint.setStrokeWidth(1.5f * scale);
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawLine(cx + 2 * scale, cy - 14.5f * scale + bobOffset * 0.5f,
                    cx + 4 * scale, cy - 14.5f * scale + bobOffset * 0.5f, paint);
            paint.setStrokeWidth(1);
        } else {
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawOval(cx + 2 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx + 4 * scale, cy - 13.5f * scale + bobOffset * 0.5f, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx + 3 * scale, cy - 14.5f * scale + bobOffset * 0.5f, 0.6f * scale, paint);
        }

        paint.setStrokeWidth(1.5f * scale);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawLine(cx + 1 * scale, cy - 16.5f * scale + bobOffset * 0.5f,
                cx + 4 * scale, cy - 17 * scale + bobOffset * 0.5f, paint);
        paint.setStrokeWidth(1);

        paint.setStrokeWidth(1.2f * scale);
        paint.setColor(Color.rgb(200, 100, 100));
        canvas.drawLine(cx + 3 * scale, cy - 10 * scale + bobOffset * 0.5f,
                cx + 5 * scale, cy - 10 * scale + bobOffset * 0.5f, paint);
        paint.setStrokeWidth(1);

        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx - 5 * scale, cy - 10 * scale + bobOffset * 0.3f,
                cx - 3 * scale, cy - 8 * scale + bobOffset * 0.3f, paint);

        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx - 3 * scale, cy - 23 * scale + bobOffset * 0.3f,
                cx + 2 * scale, cy - 21.5f * scale + bobOffset * 0.3f, paint);

        paint.setColor(Color.rgb(76, 175, 80));
        canvas.drawCircle(cx - 0.5f * scale, cy - 22.5f * scale + bobOffset * 0.3f, 1.8f * scale, paint);
    }

    private void drawFacingRight(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                 float bobOffset, float legOffset, float armSwing, float breathScale, boolean isMoving) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // === LEGS ===
        if (isMoving) {
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 6 * scale, cy + 15 * scale + bobOffset,
                    cx, cy + 22 * scale + bobOffset + legOffset, paint);
            canvas.drawRect(cx, cy + 15 * scale + bobOffset,
                    cx + 6 * scale, cy + 22 * scale + bobOffset - legOffset, paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 7 * scale, cy + 20 * scale + bobOffset + legOffset,
                    cx + 1 * scale, cy + 23 * scale + bobOffset + legOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx - 1 * scale, cy + 20 * scale + bobOffset - legOffset,
                    cx + 7 * scale, cy + 23 * scale + bobOffset - legOffset,
                    2 * scale, 2 * scale, paint);
        } else {
            paint.setColor(Color.rgb(240, 240, 240));
            canvas.drawRect(cx - 6 * scale, cy + 15 * scale + bobOffset,
                    cx, cy + 22 * scale + bobOffset, paint);
            canvas.drawRect(cx, cy + 15 * scale + bobOffset,
                    cx + 6 * scale, cy + 22 * scale + bobOffset, paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRoundRect(cx - 7 * scale, cy + 20 * scale + bobOffset,
                    cx + 1 * scale, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
            canvas.drawRoundRect(cx - 1 * scale, cy + 20 * scale + bobOffset,
                    cx + 7 * scale, cy + 23 * scale + bobOffset,
                    2 * scale, 2 * scale, paint);
        }

        // === BODY (Side view - mirrored from left) ===
        Path robeBody = new Path();
        robeBody.moveTo(cx - 8 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 8 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 10 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.quadTo(cx, cy + 18 * scale + bobOffset, cx - 10 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.close();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawPath(robeBody, paint);

        // Side shadow (opposite side from left-facing)
        Path robeShadow = new Path();
        robeShadow.moveTo(cx, cy - 8 * scale + bobOffset);
        robeShadow.lineTo(cx + 8 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeShadow.lineTo(cx + 10 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeShadow.lineTo(cx, cy + 17 * scale + bobOffset);
        robeShadow.close();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(30, 0, 0, 100));
        canvas.drawPath(robeShadow, paint);

        // Blue sash
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(30, 136, 229));
        canvas.drawRect(cx - 9 * scale * breathScale, cy + 2 * scale + bobOffset,
                cx + 9 * scale * breathScale, cy + 5 * scale + bobOffset, paint);

        // Sash knot
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawCircle(cx, cy + 3.5f * scale + bobOffset, 2 * scale, paint);

        // === ARMS ===
        if (isMoving) {
            // Front arm (left arm visible when facing right)
            Path frontArm = new Path();
            frontArm.moveTo(cx - 2 * scale, cy - 5 * scale + bobOffset);
            frontArm.quadTo(cx - 8 * scale + armSwing, cy + 5 * scale + bobOffset,
                    cx - 6 * scale + armSwing * 0.5f, cy + 12 * scale + bobOffset);
            frontArm.lineTo(cx - 3 * scale + armSwing * 0.5f, cy + 11 * scale + bobOffset);
            frontArm.quadTo(cx - 4 * scale, cy + 5 * scale + bobOffset,
                    cx - 2 * scale, cy - 3 * scale + bobOffset);
            frontArm.close();

            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(frontArm, paint);

            // Back arm (right arm, partially visible)
            Path backArm = new Path();
            backArm.moveTo(cx + 6 * scale, cy - 5 * scale + bobOffset);
            backArm.quadTo(cx + 10 * scale - armSwing * 0.3f, cy + 5 * scale + bobOffset,
                    cx + 8 * scale - armSwing * 0.2f, cy + 10 * scale + bobOffset);
            backArm.lineTo(cx + 5 * scale - armSwing * 0.2f, cy + 9 * scale + bobOffset);
            backArm.quadTo(cx + 6 * scale, cy + 5 * scale + bobOffset,
                    cx + 6 * scale, cy - 3 * scale + bobOffset);
            backArm.close();

            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.rgb(245, 245, 245));
            canvas.drawPath(backArm, paint);
        } else {
            // Idle arms
            Path frontArm = new Path();
            frontArm.moveTo(cx - 2 * scale, cy - 5 * scale + bobOffset);
            frontArm.quadTo(cx - 7 * scale, cy + 3 * scale + bobOffset + armSwing * 0.2f,
                    cx - 5 * scale, cy + 11 * scale + bobOffset);
            frontArm.lineTo(cx - 2.5f * scale, cy + 10 * scale + bobOffset);
            frontArm.quadTo(cx - 3.5f * scale, cy + 3 * scale + bobOffset,
                    cx - 2 * scale, cy - 3 * scale + bobOffset);
            frontArm.close();

            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(frontArm, paint);

            Path backArm = new Path();
            backArm.moveTo(cx + 6 * scale, cy - 5 * scale + bobOffset);
            backArm.quadTo(cx + 9 * scale, cy + 3 * scale + bobOffset - armSwing * 0.2f,
                    cx + 7 * scale, cy + 10 * scale + bobOffset);
            backArm.lineTo(cx + 4.5f * scale, cy + 9 * scale + bobOffset);
            backArm.quadTo(cx + 5.5f * scale, cy + 3 * scale + bobOffset,
                    cx + 6 * scale, cy - 3 * scale + bobOffset);
            backArm.close();

            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.rgb(245, 245, 245));
            canvas.drawPath(backArm, paint);
        }

        // === HEAD (Profile view - facing right) ===
        // Face profile
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(255, 224, 178));
        Path faceProfile = new Path();
        faceProfile.moveTo(cx + 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx + 8 * scale, cy - 15 * scale + bobOffset * 0.5f, cx + 7 * scale, cy - 12 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx + 6 * scale, cy - 10 * scale + bobOffset * 0.5f, cx + 4 * scale, cy - 10 * scale + bobOffset * 0.5f);
        faceProfile.lineTo(cx - 2 * scale, cy - 10 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx - 5 * scale, cy - 10 * scale + bobOffset * 0.5f, cx - 6 * scale, cy - 12 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx - 7 * scale, cy - 15 * scale + bobOffset * 0.5f, cx - 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        faceProfile.quadTo(cx - 4 * scale, cy - 21 * scale + bobOffset * 0.3f, cx, cy - 21 * scale + bobOffset * 0.3f);
        faceProfile.quadTo(cx + 4 * scale, cy - 21 * scale + bobOffset * 0.3f, cx + 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        faceProfile.close();
        canvas.drawPath(faceProfile, paint);

        // Nose (pointing right)
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(240, 210, 160));
        Path nose = new Path();
        nose.moveTo(cx - 5 * scale, cy - 14 * scale + bobOffset * 0.5f);
        nose.lineTo(cx - 7 * scale, cy - 13 * scale + bobOffset * 0.5f);
        nose.lineTo(cx - 5 * scale, cy - 12 * scale + bobOffset * 0.5f);
        nose.close();
        canvas.drawPath(nose, paint);

        // Hair (side view - flowing opposite direction)
        Path hair = new Path();
        hair.moveTo(cx + 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 8 * scale, cy - 23 * scale + bobOffset * 0.3f, cx + 4 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx, cy - 26 * scale + bobOffset * 0.3f, cx - 4 * scale, cy - 25 * scale + bobOffset * 0.3f);
        hair.quadTo(cx - 7 * scale, cy - 23 * scale + bobOffset * 0.3f, cx - 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 7 * scale, cy - 12 * scale + bobOffset * 0.5f, cx - 8 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hair.lineTo(cx - 6 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hair.quadTo(cx - 5 * scale, cy - 12 * scale + bobOffset * 0.5f, cx - 4 * scale, cy - 15 * scale + bobOffset * 0.5f);
        hair.quadTo(cx, cy - 16 * scale + bobOffset * 0.5f, cx + 4 * scale, cy - 15 * scale + bobOffset * 0.5f);
        hair.quadTo(cx + 6 * scale, cy - 14 * scale + bobOffset * 0.5f, cx + 6 * scale, cy - 18 * scale + bobOffset * 0.5f);
        hair.close();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hair, paint);

        // Hair flowing back (to the left when facing right)
        Path hairFlow = new Path();
        hairFlow.moveTo(cx + 4 * scale, cy - 20 * scale + bobOffset * 0.3f);
        hairFlow.quadTo(cx + 8 * scale, cy - 15 * scale + bobOffset * 0.3f, cx + 7 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hairFlow.lineTo(cx + 5 * scale, cy - 8 * scale + bobOffset * 0.5f);
        hairFlow.quadTo(cx + 6 * scale, cy - 15 * scale + bobOffset * 0.3f, cx + 4 * scale, cy - 20 * scale + bobOffset * 0.3f);
        hairFlow.close();

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawPath(hairFlow, paint);

        // Eye (profile - looking right)
        paint.reset();
        paint.setAntiAlias(true);
        int walkCycle = player.getWalkCycle();
        if (!isMoving && (walkCycle % 120 < 115)) {
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawOval(cx - 4 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx - 2 * scale, cy - 13.5f * scale + bobOffset * 0.5f, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx - 3 * scale, cy - 14.5f * scale + bobOffset * 0.5f, 0.6f * scale, paint);
        } else if (!isMoving) {
            paint.setStrokeWidth(1.5f * scale);
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawLine(cx - 4 * scale, cy - 14.5f * scale + bobOffset * 0.5f,
                    cx - 2 * scale, cy - 14.5f * scale + bobOffset * 0.5f, paint);
            paint.setStrokeWidth(1);
        } else {
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawOval(cx - 4 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx - 2 * scale, cy - 13.5f * scale + bobOffset * 0.5f, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx - 3 * scale, cy - 14.5f * scale + bobOffset * 0.5f, 0.6f * scale, paint);
        }

        // Eyebrow
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1.5f * scale);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawLine(cx - 1 * scale, cy - 16.5f * scale + bobOffset * 0.5f,
                cx - 4 * scale, cy - 17 * scale + bobOffset * 0.5f, paint);
        paint.setStrokeWidth(1);

        // Mouth
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1.2f * scale);
        paint.setColor(Color.rgb(200, 100, 100));
        canvas.drawLine(cx - 3 * scale, cy - 10 * scale + bobOffset * 0.5f,
                cx - 5 * scale, cy - 10 * scale + bobOffset * 0.5f, paint);
        paint.setStrokeWidth(1);

        // Jade pommel
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(76, 175, 80));
        canvas.drawCircle(cx + 0.5f * scale, cy - 22.5f * scale + bobOffset * 0.3f, 1.8f * scale, paint);
    }

    private void drawEnergyParticles(Canvas canvas, Paint paint, float cx, float cy, float scale, boolean isMoving) {
        // More active particles when moving, calmer when idle
        int particleCount = isMoving ? 5 : 3;
        float particleSpeed = isMoving ? 4 : 3;

        int walkCycle = player.getWalkCycle();
        int size = player.getSize();

        paint.setColor(Color.argb(150, 255, 235, 59));
        for (int i = 0; i < particleCount; i++) {
            float angle = (walkCycle * particleSpeed + i * 72) * (float) Math.PI / 180;
            float radius = isMoving ? size * 0.6f : size * 0.5f;
            float particleX = cx + (float) Math.cos(angle) * radius;
            float particleY = cy - 5 * scale + (float) Math.sin(angle) * (radius * 0.8f);
            float particleSize = isMoving ? 1.5f * scale : 1.2f * scale;
            canvas.drawCircle(particleX, particleY, particleSize, paint);
        }

        // Qi/energy aura lines
        paint.setStrokeWidth(1.5f * scale);
        paint.setColor(Color.argb(80, 66, 165, 245));
        int lineCount = isMoving ? 3 : 2;
        float lineSpeed = isMoving ? 3 : 1.5f;

        for (int i = 0; i < lineCount; i++) {
            float angle = (walkCycle * lineSpeed + i * 120) * (float) Math.PI / 180;
            float startRadius = isMoving ? size * 0.5f : size * 0.45f;
            float endRadius = isMoving ? size * 0.8f : size * 0.65f;
            float startX = cx + (float) Math.cos(angle) * startRadius;
            float startY = cy - 5 * scale + (float) Math.sin(angle) * startRadius;
            float endX = cx + (float) Math.cos(angle) * endRadius;
            float endY = cy - 5 * scale + (float) Math.sin(angle) * endRadius;
            canvas.drawLine(startX, startY, endX, endY, paint);
        }
        paint.setStrokeWidth(1);
    }
}

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
        boolean isMoving = player.isMoving();
        boolean isAttacking = player.isAttacking();
        int walkCycle = player.getWalkCycle();
        int facingDirection = player.getFacingDirection();
        int size = player.getSize();
        float attackProgress = player.getAttackAnimationProgress();

        float bobOffset, legOffset, armSwing, breathScale;

        if (isAttacking) {
            // Attack animation overrides walking
            bobOffset = 0;
            legOffset = 0;
            breathScale = 1.0f;

            // Arm swing based on attack phase
            if (attackProgress < 0.3f) {
                // Wind-up phase: pull arm back
                armSwing = -20 * scale * (attackProgress / 0.3f);
            } else if (attackProgress < 0.5f) {
                // Strike phase: swing forward quickly
                float strikeProgress = (attackProgress - 0.3f) / 0.2f;
                armSwing = -20 * scale + 60 * scale * strikeProgress;
            } else {
                // Recovery phase: return to normal
                float recoveryProgress = (attackProgress - 0.5f) / 0.5f;
                armSwing = 40 * scale * (1 - recoveryProgress);
            }
        } else if (isMoving) {
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
                drawFacingDown(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving, isAttacking, attackProgress);
                break;
            case 1: // Facing UP
                drawFacingUp(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving, isAttacking, attackProgress);
                break;
            case 2: // Facing LEFT
                drawFacingLeft(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving, isAttacking, attackProgress);
                break;
            case 3: // Facing RIGHT
                drawFacingRight(canvas, paint, screenX, screenY, scale, bobOffset, legOffset, armSwing, breathScale, isMoving, isAttacking, attackProgress);
                break;
        }

        // Draw attack effect (sword slash)
        if (isAttacking) {
            drawAttackEffect(canvas, paint, screenX, screenY, scale, facingDirection, attackProgress);
        }

        // Floating energy particles (always animated)
        drawEnergyParticles(canvas, paint, screenX, screenY, scale, isMoving);
    }

// ... existing code ...

    private void drawFacingDown(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                float bobOffset, float legOffset, float armSwing, float breathScale,
                                boolean isMoving, boolean isAttacking, float attackProgress) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // Sword handling - show in hand during attack, on back otherwise
        if (!isAttacking || attackProgress < 0.2f || attackProgress > 0.6f) {
            // Sword on back (normal state)
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
        }

        // === LEGS ===
        if (isMoving && !isAttacking) {
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
            // Idle or attacking - standing still
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

        // === ARMS with attack animation ===
        if (isAttacking) {
            // Attack pose - arms extended forward
            float rightArmExtend = 0;
            if (attackProgress >= 0.3f && attackProgress <= 0.5f) {
                // Strike phase - extend arm forward
                float strikeProgress = (attackProgress - 0.3f) / 0.2f;
                rightArmExtend = 15 * scale * strikeProgress;
            } else if (attackProgress > 0.5f) {
                // Recovery - return arm
                float recoveryProgress = (attackProgress - 0.5f) / 0.5f;
                rightArmExtend = 15 * scale * (1 - recoveryProgress);
            }

            // Left arm (slightly back for balance)
            Path leftArm = new Path();
            leftArm.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 16 * scale, cy + 2 * scale + bobOffset,
                    cx - 14 * scale, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 11 * scale, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale, cy + 2 * scale + bobOffset,
                    cx - 10 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            // Right arm (attacking arm)
            Path rightArm = new Path();
            rightArm.moveTo(cx + 12 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 18 * scale + rightArmExtend, cy + 2 * scale + bobOffset,
                    cx + 16 * scale + rightArmExtend, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 13 * scale + rightArmExtend, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 14 * scale, cy + 2 * scale + bobOffset,
                    cx + 10 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);

            // Draw sword in hand during attack
            if (attackProgress >= 0.2f && attackProgress <= 0.6f) {
                drawSwordInHand(canvas, paint, cx + 14 * scale + rightArmExtend, cy + 8 * scale + bobOffset, scale, attackProgress);
            }
        } else if (isMoving) {
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
        if (!isMoving && !isAttacking && (walkCycle % 120 < 115)) {
            // Eyes open
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawCircle(cx - 3.5f * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);
            canvas.drawCircle(cx + 3.5f * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);

            // Eye highlights
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx - 3 * scale, cy - 15.5f * scale + bobOffset * 0.5f, 0.7f * scale, paint);
            canvas.drawCircle(cx + 4 * scale, cy - 15.5f * scale + bobOffset * 0.5f, 0.7f * scale, paint);
        } else if (!isMoving && !isAttacking) {
            // Blinking - eyes closed
            paint.setStrokeWidth(1.5f * scale);
            paint.setColor(Color.rgb(33, 33, 33));
            canvas.drawLine(cx - 5 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx - 2 * scale, cy - 15 * scale + bobOffset * 0.5f, paint);
            canvas.drawLine(cx + 2 * scale, cy - 15 * scale + bobOffset * 0.5f,
                    cx + 5 * scale, cy - 15 * scale + bobOffset * 0.5f, paint);
            paint.setStrokeWidth(1);
        } else {
            // Walking or attacking - eyes always open
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

// ... existing code ...

    private void drawFacingUp(Canvas canvas, Paint paint, float cx, float cy, float scale,
                              float bobOffset, float legOffset, float armSwing, float breathScale,
                              boolean isMoving, boolean isAttacking, float attackProgress) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // Similar structure - implement idle animations for back view
        if (isMoving && !isAttacking) {
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

        // Arms with attack animation
        if (isAttacking) {
            float rightArmExtend = 0;
            if (attackProgress >= 0.3f && attackProgress <= 0.5f) {
                float strikeProgress = (attackProgress - 0.3f) / 0.2f;
                rightArmExtend = 15 * scale * strikeProgress;
            } else if (attackProgress > 0.5f) {
                float recoveryProgress = (attackProgress - 0.5f) / 0.5f;
                rightArmExtend = 15 * scale * (1 - recoveryProgress);
            }

            Path leftArm = new Path();
            leftArm.moveTo(cx - 12 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 16 * scale, cy + 2 * scale + bobOffset,
                    cx - 14 * scale, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 11 * scale, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale, cy + 2 * scale + bobOffset,
                    cx - 10 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 12 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 18 * scale + rightArmExtend, cy + 2 * scale + bobOffset,
                    cx + 16 * scale + rightArmExtend, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 13 * scale + rightArmExtend, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 14 * scale, cy + 2 * scale + bobOffset,
                    cx + 10 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);

            // Draw sword in hand during attack
            if (attackProgress >= 0.2f && attackProgress <= 0.6f) {
                drawSwordInHand(canvas, paint, cx + 14 * scale + rightArmExtend, cy + 8 * scale + bobOffset, scale, attackProgress);
            }
        } else if (isMoving) {
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

        // Head (back view)
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawCircle(cx, cy - 15 * scale + bobOffset * 0.5f, 9 * scale * breathScale, paint);

        // Long flowing hair (back view)
        Path hair = new Path();
        hair.moveTo(cx - 9 * scale, cy - 18 * scale + bobOffset * 0.3f);
        hair.quadTo(cx, cy - 26 * scale + bobOffset * 0.3f, cx + 9 * scale, cy - 18 * scale + bobOffset * 0.3f);
        hair.lineTo(cx + 11 * scale, cy - 8 * scale + bobOffset * 0.3f);
        hair.quadTo(cx, cy - 10 * scale + bobOffset * 0.3f, cx - 11 * scale, cy - 8 * scale + bobOffset * 0.3f);
        hair.close();
        canvas.drawPath(hair, paint);

        // Hair strands flowing down
        paint.setStrokeWidth(3 * scale);
        canvas.drawLine(cx - 5 * scale, cy - 10 * scale + bobOffset * 0.3f,
                cx - 6 * scale, cy + 5 * scale + bobOffset * 0.3f, paint);
        canvas.drawLine(cx, cy - 10 * scale + bobOffset * 0.3f,
                cx, cy + 7 * scale + bobOffset * 0.3f, paint);
        canvas.drawLine(cx + 5 * scale, cy - 10 * scale + bobOffset * 0.3f,
                cx + 6 * scale, cy + 5 * scale + bobOffset * 0.3f, paint);
        paint.setStrokeWidth(1);

        // Sword on back (prominently displayed)
        if (!isAttacking || attackProgress < 0.2f || attackProgress > 0.6f) {
            drawSwordOnBack(canvas, paint, cx, cy, scale, bobOffset);
        }
    }

// ... existing code ...

    private void drawFacingLeft(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                float bobOffset, float legOffset, float armSwing, float breathScale,
                                boolean isMoving, boolean isAttacking, float attackProgress) {
        canvas.save();
        canvas.scale(-1, 1, cx, cy);
        drawFacingRight(canvas, paint, cx, cy, scale, bobOffset, legOffset, armSwing, breathScale, isMoving, isAttacking, attackProgress);
        canvas.restore();
    }

    private void drawFacingRight(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                 float bobOffset, float legOffset, float armSwing, float breathScale,
                                 boolean isMoving, boolean isAttacking, float attackProgress) {
        // Reset paint to ensure clean state
        paint.reset();
        paint.setAntiAlias(true);

        // Legs
        if (isMoving && !isAttacking) {
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

        // Body
        Path robeBody = new Path();
        robeBody.moveTo(cx - 10 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 10 * scale * breathScale, cy - 8 * scale + bobOffset);
        robeBody.lineTo(cx + 12 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.quadTo(cx, cy + 18 * scale + bobOffset, cx - 12 * scale * breathScale, cy + 16 * scale + bobOffset);
        robeBody.close();

        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawPath(robeBody, paint);

        // Side shadow
        paint.setColor(Color.argb(20, 0, 0, 100));
        canvas.drawLine(cx, cy - 8 * scale + bobOffset, cx, cy + 16 * scale + bobOffset, paint);

        // Blue sash
        paint.setColor(Color.rgb(30, 136, 229));
        canvas.drawRect(cx - 11 * scale * breathScale, cy + 2 * scale + bobOffset,
                cx + 11 * scale * breathScale, cy + 5 * scale + bobOffset, paint);

        paint.setColor(Color.rgb(25, 118, 210));
        canvas.drawCircle(cx, cy + 3.5f * scale + bobOffset, 2.5f * scale, paint);

        // Arms with attack animation
        if (isAttacking) {
            float rightArmExtend = 0;
            if (attackProgress >= 0.3f && attackProgress <= 0.5f) {
                float strikeProgress = (attackProgress - 0.3f) / 0.2f;
                rightArmExtend = 15 * scale * strikeProgress;
            } else if (attackProgress > 0.5f) {
                float recoveryProgress = (attackProgress - 0.5f) / 0.5f;
                rightArmExtend = 15 * scale * (1 - recoveryProgress);
            }

            Path leftArm = new Path();
            leftArm.moveTo(cx - 8 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale, cy + 2 * scale + bobOffset,
                    cx - 10 * scale, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 7 * scale, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 8 * scale, cy + 2 * scale + bobOffset,
                    cx - 6 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 10 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 16 * scale + rightArmExtend, cy + 2 * scale + bobOffset,
                    cx + 14 * scale + rightArmExtend, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 11 * scale + rightArmExtend, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 12 * scale, cy + 2 * scale + bobOffset,
                    cx + 8 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);

            // Draw sword in hand during attack
            if (attackProgress >= 0.2f && attackProgress <= 0.6f) {
                drawSwordInHand(canvas, paint, cx + 12 * scale + rightArmExtend, cy + 8 * scale + bobOffset, scale, attackProgress);
            }
        } else if (isMoving) {
            Path leftArm = new Path();
            leftArm.moveTo(cx - 8 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale + armSwing * 0.5f, cy + 2 * scale + bobOffset,
                    cx - 10 * scale + armSwing * 0.3f, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 7 * scale + armSwing * 0.3f, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 8 * scale, cy + 2 * scale + bobOffset,
                    cx - 6 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 10 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 14 * scale - armSwing * 0.5f, cy + 2 * scale + bobOffset,
                    cx + 12 * scale - armSwing * 0.3f, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 9 * scale - armSwing * 0.3f, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 10 * scale, cy + 2 * scale + bobOffset,
                    cx + 8 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);
        } else {
            Path leftArm = new Path();
            leftArm.moveTo(cx - 8 * scale, cy - 5 * scale + bobOffset);
            leftArm.quadTo(cx - 12 * scale, cy + 2 * scale + bobOffset + armSwing * 0.3f,
                    cx - 10 * scale, cy + 10 * scale + bobOffset);
            leftArm.lineTo(cx - 7 * scale, cy + 9 * scale + bobOffset);
            leftArm.quadTo(cx - 8 * scale, cy + 2 * scale + bobOffset,
                    cx - 6 * scale, cy - 3 * scale + bobOffset);
            leftArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(leftArm, paint);

            Path rightArm = new Path();
            rightArm.moveTo(cx + 10 * scale, cy - 5 * scale + bobOffset);
            rightArm.quadTo(cx + 14 * scale, cy + 2 * scale + bobOffset - armSwing * 0.3f,
                    cx + 12 * scale, cy + 10 * scale + bobOffset);
            rightArm.lineTo(cx + 9 * scale, cy + 9 * scale + bobOffset);
            rightArm.quadTo(cx + 10 * scale, cy + 2 * scale + bobOffset,
                    cx + 8 * scale, cy - 3 * scale + bobOffset);
            rightArm.close();

            paint.setColor(Color.rgb(255, 255, 255));
            canvas.drawPath(rightArm, paint);
        }

        // Head (side profile)
        paint.setColor(Color.rgb(255, 224, 178));
        canvas.drawCircle(cx + 2 * scale, cy - 15 * scale + bobOffset * 0.5f, 9 * scale * breathScale, paint);

        // Hair (side view)
        Path hair = new Path();
        hair.moveTo(cx - 5 * scale, cy - 18 * scale + bobOffset * 0.3f);
        hair.quadTo(cx + 2 * scale, cy - 26 * scale + bobOffset * 0.3f, cx + 10 * scale, cy - 18 * scale + bobOffset * 0.3f);
        hair.lineTo(cx + 11 * scale, cy - 10 * scale + bobOffset * 0.3f);
        hair.quadTo(cx + 2 * scale, cy - 12 * scale + bobOffset * 0.3f, cx - 6 * scale, cy - 10 * scale + bobOffset * 0.3f);
        hair.close();
        canvas.drawPath(hair, paint);

        // Hair flowing back
        paint.setStrokeWidth(2.5f * scale);
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawLine(cx - 4 * scale, cy - 12 * scale + bobOffset * 0.3f,
                cx - 10 * scale, cy - 5 * scale + bobOffset * 0.3f, paint);
        canvas.drawLine(cx - 3 * scale, cy - 11 * scale + bobOffset * 0.3f,
                cx - 9 * scale, cy - 3 * scale + bobOffset * 0.3f, paint);
        paint.setStrokeWidth(1);

        // Face details
        paint.setColor(Color.rgb(33, 33, 33));
        canvas.drawCircle(cx + 5 * scale, cy - 15 * scale + bobOffset * 0.5f, 1.5f * scale, paint);

        paint.setStrokeWidth(1.5f * scale);
        canvas.drawLine(cx + 3 * scale, cy - 17.5f * scale + bobOffset * 0.5f,
                cx + 7 * scale, cy - 17 * scale + bobOffset * 0.5f, paint);
        paint.setStrokeWidth(1);

        // Sword on back (side view)
        if (!isAttacking || attackProgress < 0.2f || attackProgress > 0.6f) {
            //drawSwordOnSide(canvas, paint, cx, cy, scale, bobOffset);
        }
    }

    /**
     * Draw sword in hand during attack animation
     */
    private void drawSwordInHand(Canvas canvas, Paint paint, float handX, float handY,
                                 float scale, float attackProgress) {
        // Calculate sword angle based on attack progress
        float angle;
        if (attackProgress < 0.35f) {
            // Wind-up: sword behind
            angle = -60;
        } else if (attackProgress < 0.45f) {
            // Strike: sword swings through
            float strikeProgress = (attackProgress - 0.35f) / 0.1f;
            angle = -60 + 150 * strikeProgress; // -60 to +90 degrees
        } else {
            // Recovery: sword returns
            float recoveryProgress = (attackProgress - 0.45f) / 0.15f;
            angle = 90 - 60 * recoveryProgress;
        }

        canvas.save();
        canvas.rotate(angle, handX, handY);

        // Sword blade
        paint.setColor(Color.rgb(220, 220, 240));
        canvas.drawRect(handX - 2 * scale, handY - 25 * scale,
                handX + 2 * scale, handY + 5 * scale, paint);

        // Sword edge highlight
        paint.setColor(Color.WHITE);
        canvas.drawRect(handX - 1 * scale, handY - 25 * scale,
                handX + 0 * scale, handY + 5 * scale, paint);

        // Sword hilt
        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawRect(handX - 4 * scale, handY + 3 * scale,
                handX + 4 * scale, handY + 6 * scale, paint);

        canvas.restore();
    }

    /**
     * Draw sword on back (for up/back view)
     */
    private void drawSwordOnBack(Canvas canvas, Paint paint, float cx, float cy, float scale, float bobOffset) {
        Path scabbard = new Path();
        scabbard.moveTo(cx + 2 * scale, cy - 18 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx + 5 * scale, cy - 16 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx, cy + 8 * scale + bobOffset * 0.3f);
        scabbard.lineTo(cx - 3 * scale, cy + 6 * scale + bobOffset * 0.3f);
        scabbard.close();

        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawPath(scabbard, paint);

        // Scabbard decoration
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx + 0.5f * scale, cy - 10 * scale + bobOffset * 0.3f,
                cx + 2.5f * scale, cy - 8 * scale + bobOffset * 0.3f, paint);

        // Sword hilt
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRect(cx + 1.5f * scale, cy - 19 * scale + bobOffset * 0.3f,
                cx + 6 * scale, cy - 17.5f * scale + bobOffset * 0.3f, paint);
    }

    /**
     * Draw sword on side (for left/right view)
     */
    private void drawSwordOnSide(Canvas canvas, Paint paint, float cx, float cy, float scale, float bobOffset) {
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
    }

    /**
     * Draw attack effect (sword slash)
     */
    private void drawAttackEffect(Canvas canvas, Paint paint, float cx, float cy, float scale,
                                  int facingDirection, float progress) {
        // Only show effect during strike phase (30%-50%)
        if (progress < 0.3f || progress > 0.6f) return;

        float effectAlpha = 1.0f;
        if (progress < 0.35f) {
            effectAlpha = (progress - 0.3f) / 0.05f; // Fade in
        } else if (progress > 0.5f) {
            effectAlpha = 1.0f - (progress - 0.5f) / 0.1f; // Fade out
        }

        int alpha = (int)(effectAlpha * 200);

        // Slash color (white with blue tint)
        paint.setColor(Color.argb(alpha, 200, 230, 255));
        paint.setStrokeWidth(4 * scale);
        paint.setStyle(Paint.Style.STROKE);

        float slashLength = 50 * scale;
        float startX, startY, endX, endY;

        switch (facingDirection) {
            case 0: // Down - horizontal slash
                startX = cx - slashLength / 2;
                startY = cy + 20 * scale;
                endX = cx + slashLength / 2;
                endY = cy + 20 * scale;
                break;
            case 1: // Up - horizontal slash above
                startX = cx - slashLength / 2;
                startY = cy - 30 * scale;
                endX = cx + slashLength / 2;
                endY = cy - 30 * scale;
                break;
            case 2: // Left - vertical slash
                startX = cx - 25 * scale;
                startY = cy - slashLength / 2;
                endX = cx - 25 * scale;
                endY = cy + slashLength / 2;
                break;
            case 3: // Right - vertical slash
                startX = cx + 25 * scale;
                startY = cy - slashLength / 2;
                endX = cx + 25 * scale;
                endY = cy + slashLength / 2;
                break;
            default:
                return;
        }

        // Draw slash line with arc effect
        Path slashPath = new Path();
        slashPath.moveTo(startX, startY);

        // Add slight curve to the slash
        float controlX = (startX + endX) / 2 + (facingDirection == 0 || facingDirection == 1 ? 0 : 15 * scale);
        float controlY = (startY + endY) / 2 + (facingDirection == 2 || facingDirection == 3 ? 0 : 15 * scale);
        slashPath.quadTo(controlX, controlY, endX, endY);

        canvas.drawPath(slashPath, paint);

        // Draw glow effect
        paint.setColor(Color.argb(alpha / 2, 100, 200, 255));
        paint.setStrokeWidth(8 * scale);
        canvas.drawPath(slashPath, paint);

        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
    }

// ... existing code ...

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

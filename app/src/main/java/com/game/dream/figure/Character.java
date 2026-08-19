package com.game.dream.figure;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Base class for all living characters (player and enemies)
 */
public abstract class Character {
    public enum CrowdControlType {
        NONE,
        ROOT,   // Cannot move, can attack
        STUN,   // Cannot move, cannot attack
        SLOW,   // Move speed reduced
        FREEZE  // Frozen: cannot move, cannot attack (冰冻)
    }

    protected float x, y;
    protected int size;

    protected String name;

    protected Paint paint = new Paint();

    protected boolean isInvincible;
    protected long invincibleEndTime;
    protected long lastDamageTime;

    protected long lastAttackTime;
    protected long attackCooldown;

    // Animation
    protected int animFrame;
    protected long lastAnimUpdate;
    protected float bobOffset;

    protected CrowdControlType currentCC = CrowdControlType.NONE;
    protected long ccEndTime = 0;

    Paint ccPaint = new Paint();

    protected long jinGangStateEndTime = 0;
    protected boolean isJinGangState = false;

    protected long lastHealBloodTime = 0;
    protected long lastHealMagicTime = 0;

    // Knockback (击退)
    protected float knockbackVelX = 0;
    protected float knockbackVelY = 0;
    protected long knockbackEndTime = 0;

    // Hit flash (受击闪红)
    protected long lastHitFlashTime = 0;

    public Character(float x, float y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.name = "";
        this.isInvincible = false;
        this.invincibleEndTime = 0;
        this.lastDamageTime = 0;
        this.lastAttackTime = 0;
        this.attackCooldown = 500;

        this.animFrame = 0;
        this.lastAnimUpdate = System.currentTimeMillis();
        this.bobOffset = 0;
    }

    public void draw(Canvas canvas, int offsetX, int offsetY){
        float screenX = getX() + offsetX;
        float screenY = getY() + offsetY;

        // 1. 阴影
        float cx = screenX;
        float cy = screenY;
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawOval(cx - size * 0.5f, cy + size * 0.2f, cx + size * 0.5f, cy + size * 0.8f, paint);

        onDraw(canvas, offsetX, offsetY);

        // Draw health bar above player (using inherited method)

        //float scale = getSize() / 40f;
        float scale = 2f;
        drawHealthBar(canvas, screenX, screenY, scale);

        // Draw name below player
        drawName(canvas, screenX, screenY, scale);

        drawCCEffects(canvas, screenX, screenY, scale);

        drawFreezeOverlay(canvas, screenX, screenY, scale);

        drawJinGangEffect(canvas, screenX, screenY, scale);

        drawHealBloodEffect(canvas, screenX, screenY, scale);
        drawHealMagicEffect(canvas, screenX, screenY, scale);

        // Draw hit flash overlay (red tint when recently hit)
        drawHitFlash(canvas, screenX, screenY, scale);
    }

    public abstract void onDraw(Canvas canvas, int offsetX, int offsetY);

    /**
     * Draw character name below the character
     */
    protected void drawName(Canvas canvas, float cx, float cy, float scale) {
        if (name == null || name.isEmpty()) {
            return;
        }

        Paint namePaint = new Paint();
        namePaint.setAntiAlias(true);
        namePaint.setTextSize(14 * scale);
        namePaint.setTextAlign(Paint.Align.CENTER);

        // Add shadow for better visibility
        namePaint.setColor(Color.BLACK);
        namePaint.setStyle(Paint.Style.FILL);
        canvas.drawText(name, cx + 1, cy + 45 * scale + 1, namePaint);

        // Draw name with color
        namePaint.setColor(Color.WHITE);
        canvas.drawText(name, cx, cy + 45 * scale, namePaint);
    }

    /**
     * Check if currently invincible
     */
    public boolean isCurrentlyInvincible() {
        if (!isInvincible) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime >= invincibleEndTime) {
            isInvincible = false;
            return false;
        }
        return true;
    }

    /**
     * Get health as percentage (0-1)
     */
    public float getHealthPercent() {
        return (float)getHealth() / getMaxHealth();
    }

    /**
     * Draw health bar above character
     */
    protected void drawHealthBar(Canvas canvas, float cx, float cy, float scale) {
        scale = 2f;

        float barWidth = 50 * scale;
        float barHeight = 6 * scale;
        float barX = cx - barWidth / 2;
        float barY = cy - 45 * scale;

        // Background
        android.graphics.Paint bgPaint = new android.graphics.Paint();
        bgPaint.setColor(android.graphics.Color.BLACK);
        bgPaint.setStyle(android.graphics.Paint.Style.FILL);
        canvas.drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, bgPaint);

        // Health fill
        android.graphics.Paint healthPaint = new android.graphics.Paint();
        healthPaint.setStyle(android.graphics.Paint.Style.FILL);

        float healthPercent = getHealthPercent();

        if (healthPercent > 0.6f) {
            healthPaint.setColor(android.graphics.Color.GREEN);
        } else if (healthPercent > 0.3f) {
            healthPaint.setColor(android.graphics.Color.YELLOW);
        } else {
            healthPaint.setColor(android.graphics.Color.RED);
        }

        canvas.drawRect(barX, barY, barX + barWidth * healthPercent, barY + barHeight, healthPaint);

        // Flash red when low health
        if (healthPercent <= 0.3f) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime / 200) % 2 == 0) {
                android.graphics.Paint flashPaint = new android.graphics.Paint();
                flashPaint.setColor(android.graphics.Color.argb(100, 255, 0, 0));
                canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, flashPaint);
            }
        }

        // Draw HP text
        android.graphics.Paint textPaint = new android.graphics.Paint();
        textPaint.setColor(android.graphics.Color.WHITE);
        textPaint.setTextSize(12 * scale);
        textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        canvas.drawText(getHealth() + "/" + getMaxHealth(), cx, barY - 2, textPaint);
    }

    /**
     * Update animation frame
     */
    protected void updateAnimation(long currentTime) {
        if (currentTime - lastAnimUpdate > 150) {
            animFrame = (animFrame + 1) % 4;
            lastAnimUpdate = currentTime;

            bobOffset = (float)Math.sin(animFrame * Math.PI / 2) * 3;
        }
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }

    public boolean isAlive() { return getHealth() > 0; }

    public String getName() { return name; }

    // Setters
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setSize(int size) { this.size = size; }
    public void setName(String name) { this.name = name; }

    public abstract int getHealth();
    public abstract int getMaxHealth();

    /**
     * Take damage
     */
    public abstract boolean takeDamage(int damage);

    /**
     * Apply a crowd control effect
     */
    public void applyCC(CrowdControlType type, long durationMillis) {
        this.currentCC = type;
        this.ccEndTime = System.currentTimeMillis() + durationMillis;
    }

    /**
     * Update and clear expired CC effects
     */
    public void updateCCState() {
        if (currentCC != CrowdControlType.NONE && System.currentTimeMillis() > ccEndTime) {
            currentCC = CrowdControlType.NONE;
        }
    }

    public boolean isRooted() { return currentCC == CrowdControlType.ROOT; }
    public boolean isStunned() { return currentCC == CrowdControlType.STUN; }
    public boolean isSlowed() { return currentCC == CrowdControlType.SLOW; }
    public boolean isFrozen() { return currentCC == CrowdControlType.FREEZE; }

    /**
     * Apply knockback to this character
     * @param fromX X position of the knockback source
     * @param fromY Y position of the knockback source
     * @param force knockback strength (pixels/second)
     * @param durationMillis how long the knockback lasts
     */
    public void applyKnockback(float fromX, float fromY, float force, long durationMillis) {
        float dx = this.x - fromX;
        float dy = this.y - fromY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            knockbackVelX = (dx / dist) * force;
            knockbackVelY = (dy / dist) * force;
            knockbackEndTime = System.currentTimeMillis() + durationMillis;
        }
    }

    /**
     * Update knockback movement. Returns the delta movement applied.
     */
    protected float[] updateKnockback(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > knockbackEndTime) {
            knockbackVelX = 0;
            knockbackVelY = 0;
            return null;
        }
        float deltaSeconds = deltaTime / 1000f;
        float moveX = knockbackVelX * deltaSeconds;
        float moveY = knockbackVelY * deltaSeconds;
        // Apply friction (knockback decays)
        knockbackVelX *= 0.9f;
        knockbackVelY *= 0.9f;
        return new float[]{moveX, moveY};
    }

    public boolean isBeingKnockedBack() {
        return System.currentTimeMillis() < knockbackEndTime;
    }

    /**
     * Trigger hit flash effect (red tint)
     */
    public void triggerHitFlash() {
        lastHitFlashTime = System.currentTimeMillis();
    }

    /**
     * Draw red flash overlay when character was recently hit
     */
    protected void drawHitFlash(Canvas canvas, float cx, float cy, float scale) {
        if (lastHitFlashTime <= 0) return;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastHitFlashTime;
        if (elapsed > 300) {
            lastHitFlashTime = 0;
            return;
        }
        float alpha = 1.0f - (elapsed / 300f);
        Paint flashPaint = new Paint();
        flashPaint.setAntiAlias(true);
        flashPaint.setColor(Color.argb((int)(alpha * 120), 255, 50, 50));
        canvas.drawCircle(cx, cy, size * 0.8f, flashPaint);
    }

    public boolean isJinGangState(){
        return isJinGangState;
    }

    /**
     * Draw crowd control effects (like root, stun) above the character
     */
    protected void drawCCEffects(Canvas canvas, float cx, float cy, float scale) {
        if (currentCC == CrowdControlType.NONE) return;

        float centerX = cx;
        float centerY = cy - 60; // Position slightly above the character

        ccPaint.setAntiAlias(true);
        ccPaint.setStrokeWidth(4);
        ccPaint.setTextSize(18 * scale);
        ccPaint.setColor(Color.YELLOW);
        ccPaint.setTextAlign(Paint.Align.CENTER);
        if (currentCC == CrowdControlType.ROOT) {
            // Draw text for Root effect
            canvas.drawText("定身", centerX, centerY + 7, ccPaint);
        } else if (currentCC == CrowdControlType.STUN) {
            // Draw text for Stun effect
            canvas.drawText("眩晕", centerX, centerY + 7, ccPaint);
        } else if (currentCC == CrowdControlType.FREEZE) {
            // Draw text for Freeze effect
            ccPaint.setColor(Color.rgb(100, 200, 255));
            canvas.drawText("冰冻", centerX, centerY + 7, ccPaint);
        }
    }

    /**
     * Draw freeze overlay effect on the character body (ice crystals + blue tint)
     */
    protected void drawFreezeOverlay(Canvas canvas, float cx, float cy, float scale) {
        if (currentCC != CrowdControlType.FREEZE) return;

        float overlayRadius = size * 0.6f * scale;
        long time = System.currentTimeMillis();
        
        // Blue semi-transparent overlay
        Paint overlayPaint = new Paint();
        overlayPaint.setAntiAlias(true);
        overlayPaint.setColor(Color.argb(60, 100, 200, 255));
        canvas.drawCircle(cx, cy - 5 * scale, overlayRadius, overlayPaint);
        
        // Ice crystal spikes around the character
        Paint icePaint = new Paint();
        icePaint.setAntiAlias(true);
        icePaint.setColor(Color.argb(180, 180, 230, 255));
        icePaint.setStrokeWidth(2 * scale);
        
        // Draw 6 ice spikes with rotation animation
        float rotation = (time % 3000) / 3000f * 360f;
        for (int i = 0; i < 6; i++) {
            float angle = (float) Math.toRadians(rotation + i * 60);
            float spikeLength = overlayRadius * 0.5f;
            float startX = cx + (float) Math.cos(angle) * overlayRadius * 0.5f;
            float startY = cy - 5 * scale + (float) Math.sin(angle) * overlayRadius * 0.5f;
            float endX = cx + (float) Math.cos(angle) * (overlayRadius * 0.5f + spikeLength);
            float endY = cy - 5 * scale + (float) Math.sin(angle) * (overlayRadius * 0.5f + spikeLength);
            canvas.drawLine(startX, startY, endX, endY, icePaint);
        }
    }

    protected void drawJinGangEffect(Canvas canvas, float cx, float cy, float scale) {
        float centerX = cx;
        float centerY = cy; // Position slightly above the character
        int radius = (int) ((size/2 + 10) * scale);
        if (isJinGangState) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.argb(80, 255, 215, 0)); // Golden semi-transparent

            // Pulsing circle around player
            long time = System.currentTimeMillis();
            float pulse = 1.0f + 0.1f * (float)Math.sin(time / 100.0);

            canvas.drawCircle(centerX, centerY, radius * pulse, paint);

            // Golden border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.rgb(255, 215, 0));
            canvas.drawCircle(centerX, centerY, radius, paint);
        }
    }

    /**
     * Activate Diamond Body (金刚护体)
     *
     * @param durationMillis  How long the buff lasts
     */
    public void activateDiamondBody(long durationMillis) {
        this.jinGangStateEndTime = System.currentTimeMillis() + durationMillis;
        this.isJinGangState = true; // Mark as having the buff
        // You can store the reduction percentage in a field if you want variable reduction
    }

    /**
     * Update shield state and clear expired buffs
     */
    public void updateBuffs() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > jinGangStateEndTime) {
            jinGangStateEndTime = 0;
            isJinGangState = false;
        }
    }

    protected void drawHealBloodEffect(Canvas canvas, float cx, float cy, float scale) {
        float centerX = cx;
        float centerY = cy; // Position slightly above the character

        // Draw healing flash effect
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHealBloodTime < 500) { // Show for 0.5 seconds
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            float alpha = 1.0f - ((float)(currentTime - lastHealBloodTime) / 500f);
            paint.setColor(Color.argb((int)(150 * alpha), 0, 255, 0)); // Green

            canvas.drawCircle(centerX, centerY, size, paint);
        }
    }

    protected void drawHealMagicEffect(Canvas canvas, float cx, float cy, float scale) {
        float centerX = cx;
        float centerY = cy; // Position slightly above the character

        // Draw healing flash effect
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHealMagicTime < 500) { // Show for 0.5 seconds
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            float alpha = 1.0f - ((float)(currentTime - lastHealMagicTime) / 500f);
            paint.setColor(Color.argb((int)(150 * alpha), 0, 0, 200)); // Blue

            canvas.drawCircle(centerX, centerY, size, paint);
        }
    }
}

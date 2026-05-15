package com.game.dream;

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
        SLOW    // Move speed reduced
    }

    protected float x, y;
    protected int size;

    protected String name;

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

    public final void draw(Canvas canvas, int offsetX, int offsetY){
        onDraw(canvas, offsetX, offsetY);

        // Draw health bar above player (using inherited method)
        float screenX = getX() + offsetX;
        float screenY = getY() + offsetY;
        //float scale = getSize() / 40f;
        float scale = 2f;
        drawHealthBar(canvas, screenX, screenY, scale);

        // Draw name below player
        drawName(canvas, screenX, screenY, scale);

        drawCCEffects(canvas, screenX, screenY, scale);
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
            // Draw stars for Root effect
            canvas.drawText("定身", centerX, centerY + 7, ccPaint);
        } else if (currentCC == CrowdControlType.STUN) {
            // Draw stars for Stun effect
            canvas.drawText("眩晕", centerX, centerY + 7, ccPaint);
        }
    }
}

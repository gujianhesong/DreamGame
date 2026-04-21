package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Base class for all living characters (player and enemies)
 */
public abstract class Character {
    protected float x, y;
    protected float speed;
    protected int size;

    // Name
    protected String name;

    // Health system
    protected int health;
    protected int maxHealth;
    protected boolean isInvincible;
    protected long invincibleEndTime;
    protected long lastDamageTime;

    // Combat stats
    protected int attackDamage;

    protected int magicDamage;
    protected int defense;
    protected int mana;

    protected long lastAttackTime;
    protected long attackCooldown;

    // Animation
    protected int animFrame;
    protected long lastAnimUpdate;
    protected float bobOffset;

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
     * Take damage
     */
    public boolean takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();

        // Check if invincible
        if (isInvincible && currentTime < invincibleEndTime) {
            return false; // No damage taken
        }

        // Apply damage
        health -= damage;
        lastDamageTime = currentTime;

        // Check if dead
        if (health <= 0) {
            health = 0;
            return true; // Died
        }

        return false; // Still alive
    }

    /**
     * Heal character
     */
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
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
        return (float)health / maxHealth;
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

        float healthPercent = (float)health / maxHealth;

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
        canvas.drawText(health + "/" + maxHealth, cx, barY - 2, textPaint);
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
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getAttackDamage() { return attackDamage; }

    public int getMagicDamage() { return magicDamage; }
    public int getDefense() { return defense; }
    public float getSpeed() { return speed; }
    public int getMana() { return mana; }
    public boolean isAlive() { return health > 0; }

    public String getName() { return name; }

    // Setters
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setHealth(int health) { this.health = health; }
    public void setSize(int size) { this.size = size; }

    public void setName(String name) { this.name = name; }
}

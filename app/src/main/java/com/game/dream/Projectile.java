package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;

/**
 * Projectile for magic attacks (fireballs, ice bolts, etc.)
 */
public class Projectile {

    private float x, y;
    private float vx, vy;
    private SkillType skillType;
    private float size;
    private boolean isActive;
    private long lifetime;
    private long createdTime;

    // Visual effects
    private int color;
    private float rotation;

    public Projectile(float x, float y, float targetX, float targetY, SkillType skillType) {
        this.x = x;
        this.y = y;
        this.skillType = skillType;
        this.isActive = true;
        this.createdTime = System.currentTimeMillis();
        this.rotation = 0;

        // Calculate velocity towards target
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float speed;
            switch (skillType) {
                case FIREBALL:
                    speed = 300;
                    this.size = 20;
                    this.color = Color.rgb(255, 100, 50);
                    this.lifetime = 1500;
                    break;
                case ICE_BOLT:
                    speed = 400;
                    this.size = 8;
                    this.color = Color.rgb(100, 200, 255);
                    this.lifetime = 1000;
                    break;
                case LIGHTNING:
                    speed = 600;
                    this.size = 10;
                    this.color = Color.rgb(255, 255, 100);
                    this.lifetime = 2000;
                    break;
                default:
                    speed = 300;
                    this.size = 10;
                    this.color = Color.WHITE;
                    this.lifetime = 2000;
                    break;
            }

            this.vx = (dx / distance) * speed;
            this.vy = (dy / distance) * speed;
        }
    }

    /**
     * Update projectile position
     */
    public void update(long deltaTime) {
        if (!isActive) return;

        float deltaSeconds = deltaTime / 1000f;

        // Move projectile
        x += vx * deltaSeconds;
        y += vy * deltaSeconds;

        // Rotate for visual effect
        rotation += 10 * deltaSeconds;

        // Check lifetime
        long currentTime = System.currentTimeMillis();
        if (currentTime - createdTime > lifetime) {
            isActive = false;
        }
    }

    /**
     * Check collision with enemy
     */
    public boolean checkCollision(Enemy enemy) {
        if (!isActive || !enemy.isAlive()) return false;

        float dx = x - enemy.getX();
        float dy = y - enemy.getY();
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        return distance < (size + enemy.getSize());
    }

    /**
     * Draw the projectile
     */
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (!isActive) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float screenX = x + offsetX;
        float screenY = y + offsetY;

        switch (skillType) {
            case FIREBALL:
                drawFireball(canvas, paint, screenX, screenY);
                break;
            case ICE_BOLT:
                drawIceBolt(canvas, paint, screenX, screenY);
                break;
            case LIGHTNING:
                drawLightning(canvas, paint, screenX, screenY);
                break;
        }
    }

    /**
     * Draw fireball effect
     */
    private void drawFireball(Canvas canvas, Paint paint, float cx, float cy) {
        // Outer glow
        paint.setColor(Color.argb(100, 255, 150, 50));
        canvas.drawCircle(cx, cy, size * 1.5f, paint);

        // Main fireball
        paint.setColor(color);
        canvas.drawCircle(cx, cy, size, paint);

        // Core
        paint.setColor(Color.rgb(255, 255, 200));
        canvas.drawCircle(cx, cy, size * 0.5f, paint);
    }

    /**
     * Draw ice bolt effect
     */
    private void drawIceBolt(Canvas canvas, Paint paint, float cx, float cy) {
        // Outer glow
        paint.setColor(Color.argb(100, 150, 220, 255));
        canvas.drawCircle(cx, cy, size * 1.3f, paint);

        // Main bolt
        paint.setColor(color);
        canvas.drawCircle(cx, cy, size, paint);

        // Sparkles
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) {
            float angle = rotation + i * (float)(Math.PI * 2 / 3);
            float sx = cx + (float)Math.cos(angle) * size * 0.7f;
            float sy = cy + (float)Math.sin(angle) * size * 0.7f;
            canvas.drawCircle(sx, sy, 2, paint);
        }
    }

    /**
     * Draw lightning effect
     */
    private void drawLightning(Canvas canvas, Paint paint, float cx, float cy) {
        // Glow
        paint.setColor(Color.argb(150, 255, 255, 150));
        canvas.drawCircle(cx, cy, size * 1.5f, paint);

        // Main bolt
        paint.setStrokeWidth(3);
        paint.setColor(color);

        // Draw zigzag lightning
        float length = size * 2;
        float dx = vx / Math.abs(vx + vy + 0.001f);
        float dy = vy / Math.abs(vx + vy + 0.001f);

        canvas.drawLine(cx - dx * length, cy - dy * length,
                cx + dx * length, cy + dy * length, paint);
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return isActive; }
    public SkillType getSkillType() { return skillType; }

    // Setters
    public void deactivate() { isActive = false; }
}

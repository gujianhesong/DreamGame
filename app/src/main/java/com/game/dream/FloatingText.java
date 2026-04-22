package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Floating text that appears and fades out
 */
public class FloatingText {
    public enum Type {
        EXPERIENCE,   // Blue text for EXP
        MONEY,        // Gold text for money
        DAMAGE,       // Red text for damage
        HEAL,         // Green text for healing
        LEVEL_UP      // Yellow text for level up
    }

    private float x, y;
    private String text;
    private int color;
    private long createdTime;
    private long lifetime;
    private boolean isActive;

    // Animation phases
    private static final long RISE_DURATION = 1000; // 1.0 seconds to rise
    private static final long FADE_DURATION = 1500; // 1.5 seconds to fade

    // Animation state
    private float offsetY;
    private float alpha;
    private float maxRiseDistance = 120; // Maximum rise distance

    public FloatingText(float x, float y, String text, Type type) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.createdTime = System.currentTimeMillis();
        this.lifetime = RISE_DURATION + FADE_DURATION; // Total 1 second
        this.isActive = true;
        this.offsetY = 0;
        this.alpha = 1.0f;

        // Set color based on type
        switch (type) {
            case EXPERIENCE:
                this.color = Color.rgb(100, 181, 246); // Light blue
                break;
            case MONEY:
                this.color = Color.rgb(255, 215, 0); // Gold
                break;
            case DAMAGE:
                this.color = Color.rgb(255, 80, 80); // Red
                break;
            case HEAL:
                this.color = Color.rgb(100, 255, 100); // Green
                break;
            case LEVEL_UP:
                this.color = Color.rgb(255, 255, 100); // Bright yellow
                break;
            default:
                this.color = Color.WHITE;
        }
    }

    /**
     * Update damage number animation
     */
    public void update(long deltaTime) {
        if (!isActive) return;

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - createdTime;

        if (elapsed >= lifetime) {
            isActive = false;
            return;
        }

        if (elapsed < RISE_DURATION) {
            // Phase 1: Rising up with easing
            float progress = (float)elapsed / RISE_DURATION;

            // Ease-out cubic for smooth rising
            float easedProgress = 1 - (float)Math.pow(1 - progress, 3);
            offsetY = -easedProgress * maxRiseDistance;

            // Keep full opacity during rise
            alpha = 1.0f;
        } else {
            // Phase 2: Fading out at top position
            float fadeElapsed = elapsed - RISE_DURATION;
            float fadeProgress = fadeElapsed / FADE_DURATION;

            // Stay at max height
            offsetY = -maxRiseDistance;

            // Linear fade out
            alpha = 1.0f - fadeProgress;
        }
    }

    /**
     * Draw damage number
     */
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (!isActive) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        // Set color based on damage type
        int textColor = color;

        // Apply alpha for fade effect
        int alphaInt = (int)(alpha * 255);
        int r = Color.red(textColor);
        int g = Color.green(textColor);
        int b = Color.blue(textColor);

        // Draw outline for better visibility
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(50);

        String showText = text;

        paint.setColor(Color.argb(alphaInt, 0, 0, 0)); // Black outline
        canvas.drawText(showText, x + offsetX, y + offsetY + this.offsetY, paint);

        // Draw fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(alphaInt, r, g, b));
        canvas.drawText(showText, x + offsetX, y + offsetY + this.offsetY, paint);

        // Add white highlight for extra visibility
        if (alpha > 0.5f) {
            paint.setColor(Color.argb((int)(alphaInt * 0.3), 255, 255, 255));
            paint.setTextSize(50);
            canvas.drawText(showText, x + offsetX, y + offsetY + this.offsetY - 1, paint);
        }
    }

    /**
     * Check if damage number is still active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get X position
     */
    public float getX() {
        return x;
    }

    /**
     * Get Y position
     */
    public float getY() {
        return y;
    }
}
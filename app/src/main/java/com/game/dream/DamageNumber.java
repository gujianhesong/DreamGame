package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Floating damage number that appears above characters when taking damage
 */
public class DamageNumber {
    private float x, y;
    private int damage;
    private boolean isCritical;
    private long createdTime;
    private long lifetime;
    private boolean isActive;

    // Animation phases
    private static final long RISE_DURATION = 800; // 0.4 seconds to rise
    private static final long FADE_DURATION = 600; // 0.6 seconds to fade

    // Animation state
    private float offsetY;
    private float alpha;
    private float maxRiseDistance = 80; // Maximum rise distance

    public DamageNumber(float x, float y, int damage) {
        this(x, y, damage, false);
    }

    public DamageNumber(float x, float y, int damage, boolean isCritical) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.isCritical = isCritical;
        this.createdTime = System.currentTimeMillis();
        this.lifetime = RISE_DURATION + FADE_DURATION; // Total 1 second
        this.isActive = true;
        this.offsetY = 0;
        this.alpha = 1.0f;
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
        int textColor;
        if (isCritical) {
            // Critical hit - bright red/orange
            textColor = Color.rgb(255, 50, 50);
        } else {
            // Normal hit - red
            textColor = Color.rgb(255, 180, 180);
        }

        // Apply alpha for fade effect
        int alphaInt = (int)(alpha * 255);
        int r = Color.red(textColor);
        int g = Color.green(textColor);
        int b = Color.blue(textColor);

        // Draw outline for better visibility
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        if (isCritical) {
            paint.setTextSize(60);
        } else {
            paint.setTextSize(50);
        }

        String showText;
        if (damage >= 0) {
            showText = "-" + damage;
        } else {
            showText = "未命中";
        }

        paint.setColor(Color.argb(alphaInt, 0, 0, 0)); // Black outline
        canvas.drawText(showText, x + offsetX, y + offsetY + this.offsetY, paint);

        // Draw fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(alphaInt, r, g, b));
        canvas.drawText(showText, x + offsetX, y + offsetY + this.offsetY, paint);

        // Add white highlight for extra visibility
        if (alpha > 0.5f) {
            paint.setColor(Color.argb((int)(alphaInt * 0.3), 255, 255, 255));
            if (isCritical) {
                paint.setTextSize(60);
            } else {
                paint.setTextSize(50);
            }
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
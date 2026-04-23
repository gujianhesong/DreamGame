package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Center screen notification for important game events
 */
public class CenterNotification {
    private String title;
    private String message;
    private long createdTime;
    private long duration;
    private long fadeDuration;

    public enum Type {
        LEVEL_UP,       // Level up notification
        QUEST_COMPLETE, // Quest completed
        ACHIEVEMENT,    // Achievement unlocked
        WARNING,        // Warning message
        INFO            // General info
    }

    private Type type;

    public CenterNotification(String title, String message, Type type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdTime = System.currentTimeMillis();
        this.duration = 3000; // Display for 3 seconds
        this.fadeDuration = 500; // Fade in/out for 0.5 seconds
    }

    /**
     * Update notification state
     */
    public void update() {
        // Just track time, rendering handles the animation
    }

    /**
     * Draw notification in center of screen
     */
    public void draw(Canvas canvas, int screenWidth, int screenHeight) {
        long elapsed = System.currentTimeMillis() - createdTime;

        if (elapsed >= duration) return;

        // Calculate alpha for fade in/out
        int alpha = 255;
        if (elapsed < fadeDuration) {
            // Fade in
            alpha = (int)(255 * ((float)elapsed / fadeDuration));
        } else if (elapsed > duration - fadeDuration) {
            // Fade out
            long fadeOutElapsed = elapsed - (duration - fadeDuration);
            alpha = (int)(255 * (1 - (float)fadeOutElapsed / fadeDuration));
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Background box
        float boxWidth = Math.min(screenWidth * 0.7f, 500);
        float boxHeight = 120;
        float boxX = (screenWidth - boxWidth) / 2;
        float boxY = (screenHeight - boxHeight) / 2 - 50;

        // Semi-transparent background
        paint.setColor(Color.argb((int)(alpha * 0.85f), 20, 20, 30));
        RectF bgRect = new RectF(boxX, boxY, boxX + boxWidth, boxY + boxHeight);
        canvas.drawRoundRect(bgRect, 15, 15, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        // Border color based on type
        switch (type) {
            case LEVEL_UP:
                paint.setColor(Color.argb(alpha, 255, 215, 0)); // Gold
                break;
            case QUEST_COMPLETE:
                paint.setColor(Color.argb(alpha, 100, 200, 100)); // Green
                break;
            case ACHIEVEMENT:
                paint.setColor(Color.argb(alpha, 200, 100, 255)); // Purple
                break;
            case WARNING:
                paint.setColor(Color.argb(alpha, 255, 100, 100)); // Red
                break;
            default:
                paint.setColor(Color.argb(alpha, 100, 150, 255)); // Blue
                break;
        }

        canvas.drawRoundRect(bgRect, 15, 15, paint);
        paint.setStyle(Paint.Style.FILL);

        // Title text
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb(alpha, 255, 255, 255));
        float titleY = boxY + 45;
        canvas.drawText(title, screenWidth / 2f, titleY, paint);

        // Message text
        paint.setTextSize(24);
        float messageY = boxY + 85;
        canvas.drawText(message, screenWidth / 2f, messageY, paint);

        // Decorative particles for special notifications
        if (type == Type.LEVEL_UP || type == Type.ACHIEVEMENT) {
            drawDecorations(canvas, paint, boxX, boxY, boxWidth, boxHeight, alpha);
        }
    }

    /**
     * Draw decorative elements for special notifications
     */
    private void drawDecorations(Canvas canvas, Paint paint, float boxX, float boxY,
                                 float boxWidth, float boxHeight, int alpha) {
        long elapsed = System.currentTimeMillis() - createdTime;

        // Sparkle effect
        paint.setStrokeWidth(2);
        for (int i = 0; i < 8; i++) {
            float angle = (elapsed / 500f + i) * (float)Math.PI / 4;
            float radius = 30 + (float)Math.sin(elapsed / 200f) * 10;

            float x = boxX + boxWidth / 2 + (float)Math.cos(angle) * radius;
            float y = boxY + boxHeight / 2 + (float)Math.sin(angle) * radius;

            paint.setColor(Color.argb(alpha, 255, 255, 200));
            canvas.drawCircle(x, y, 3, paint);
        }
        paint.setStrokeWidth(1);
    }

    /**
     * Check if notification has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createdTime >= duration;
    }

    public Type getType() { return type; }
}

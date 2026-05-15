package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * A simple notification that appears in the center of the screen
 */
public class CenterToast {
    private String message;
    private long duration; // How long to show in ms
    private long startTime;
    private float alpha;

    public CenterToast(String message, long duration) {
        this.message = message;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.alpha = 1.0f;
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed < duration) {
            // Fade out in the last 500ms
            if (elapsed > duration - 500) {
                alpha = 1.0f - ((float)(elapsed - (duration - 500)) / 500f);
            } else {
                alpha = 1.0f;
            }
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime >= duration;
    }

    public void draw(Canvas canvas, int screenWidth, int screenHeight) {
        if (isExpired()) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        // Calculate text size based on screen width
        float textSize = screenWidth / 40f;
        paint.setTextSize(textSize);

        // Measure text to draw background box
        float textWidth = paint.measureText(message);
        float padding = 30;
        RectF bgRect = new RectF(
                screenWidth / 2f - textWidth / 2f - padding,
                screenHeight / 2f - textSize - padding,
                screenWidth / 2f + textWidth / 2f + padding,
                screenHeight / 2f + padding
        );

        // Draw semi-transparent background
        paint.setColor(Color.argb((int)(180 * alpha), 0, 0, 0));
        canvas.drawRoundRect(bgRect, 15, 15, paint);

        // Draw border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
        canvas.drawRoundRect(bgRect, 15, 15, paint);

        // Draw text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
        canvas.drawText(message, screenWidth / 2f, screenHeight / 2f, paint);
    }
}

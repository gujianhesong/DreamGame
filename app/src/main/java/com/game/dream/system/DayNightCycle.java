package com.game.dream.system;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Handles day-night cycle with smooth transitions
 */
public class DayNightCycle {
    // Time system (in game seconds)
    private float gameTime; // 0-86400 seconds (24 hours)
    private float timeSpeed; // How fast time passes (1.0 = real time, 60.0 = 1 min = 1 hour)

    // Cycle phases (in seconds)
    private static final int DAY_START = 6 * 3600;      // 6:00 AM
    private static final int DAY_END = 18 * 3600;       // 6:00 PM
    private static final int NIGHT_START = 18 * 3600;   // 6:00 PM
    private static final int NIGHT_END = 6 * 3600;      // 6:00 AM (next day)

    // Overlay paint
    private Paint overlayPaint;

    // Current overlay color and alpha
    private int currentColor;
    private int currentAlpha;

    public DayNightCycle() {
        this.gameTime = 12 * 3600; // Start at noon
        this.timeSpeed = 24 * 6;

        overlayPaint = new Paint();
        overlayPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Update the day-night cycle
     */
    public void update(long deltaTime) {
        // Convert deltaTime from milliseconds to seconds
        float deltaSeconds = deltaTime / 1000.0f;

        // Advance game time
        gameTime += deltaSeconds * timeSpeed;

        // Wrap around at 24 hours
        if (gameTime >= 86400) {
            gameTime -= 86400;
        }

        // Calculate overlay based on current time
        calculateOverlay();
    }

    /**
     * Calculate the overlay color and alpha based on current time
     */
    private void calculateOverlay() {
        int hour = (int)(gameTime / 3600);
        int minute = (int)((gameTime % 3600) / 60);

        // Determine phase and interpolation factor
        if (gameTime >= DAY_START && gameTime <= DAY_END) {
            // Daytime transitions
            if (gameTime < 8 * 3600) {
                // Dawn (6:00 - 8:00)
                float progress = (gameTime - DAY_START) / (2.0f * 3600);
                currentAlpha = (int)(150 * (1 - progress));
                currentColor = Color.rgb(255, 200, 150); // Warm orange
            } else if (gameTime > 16 * 3600) {
                // Dusk (16:00 - 18:00)
                float progress = (gameTime - 16 * 3600) / (2.0f * 3600);
                currentAlpha = (int)(150 * progress);
                currentColor = Color.rgb(255, 150, 100); // Orange-red
            } else {
                // Midday (8:00 - 16:00) - clear sky
                currentAlpha = 0;
                currentColor = Color.TRANSPARENT;
            }
        } else {
            // Nighttime (18:00 - 6:00)
            // Normalize time to a 0-1 range where 0.5 = midnight (0:00)
            float normalizedTime;

            if (gameTime >= NIGHT_START) {
                // 18:00 - 24:00: map to 0.0 - 0.5
                normalizedTime = (gameTime - NIGHT_START) / (12.0f * 3600);
            } else {
                // 0:00 - 6:00: map to 0.5 - 1.0
                normalizedTime = 0.5f + gameTime / (12.0f * 3600);
            }

            // Calculate darkness based on distance from midnight (0.5)
            float distanceFromMidnight = Math.abs(normalizedTime - 0.5f) * 2;
            float darknessFactor = 1 - distanceFromMidnight;

            // Smooth transition at dawn/dusk boundaries
            // Check if we're in the transition period (5:00-6:00 or 17:00-18:00)
            float transitionAlpha;
            int transitionColor;

            if (gameTime >= 5 * 3600 && gameTime < DAY_START) {
                // Pre-dawn transition (5:00 - 6:00): gradually lighten from night to dawn
                float preDawnProgress = (gameTime - 5 * 3600) / 3600; // 0 to 1

                // Interpolate between night color and dawn color
                float nightAlpha = 220 * darknessFactor;
                float dawnAlpha = 150;
                currentAlpha = (int)(nightAlpha * (1 - preDawnProgress) + dawnAlpha * preDawnProgress);

                // Interpolate color from dark blue to warm orange
                int nightR = 10, nightG = 10, nightB = 40;
                int dawnR = 255, dawnG = 200, dawnB = 150;

                int r = (int)(nightR * (1 - preDawnProgress) + dawnR * preDawnProgress);
                int g = (int)(nightG * (1 - preDawnProgress) + dawnG * preDawnProgress);
                int b = (int)(nightB * (1 - preDawnProgress) + dawnB * preDawnProgress);

                currentColor = Color.rgb(r, g, b);

            } else if (gameTime >= NIGHT_END && gameTime < (NIGHT_END + 3600)) {
                // This shouldn't happen with current logic, but kept for safety
                currentAlpha = (int)(220 * darknessFactor);
                currentColor = Color.rgb(10, 10, 40);

            } else if (gameTime >= 17 * 3600 && gameTime < NIGHT_START) {
                // Pre-dusk transition (17:00 - 18:00): gradually darken from day to night
                float preDuskProgress = (gameTime - 17 * 3600) / 3600; // 0 to 1

                // At 17:00, should be transitioning from dusk ending
                float duskEndAlpha = 150;
                float nightStartAlpha = 220 * darknessFactor;
                currentAlpha = (int)(duskEndAlpha * (1 - preDuskProgress) + nightStartAlpha * preDuskProgress);

                // Interpolate color
                int duskR = 255, duskG = 150, duskB = 100;
                int nightR = 10, nightG = 10, nightB = 40;

                int r = (int)(duskR * (1 - preDuskProgress) + nightR * preDuskProgress);
                int g = (int)(duskG * (1 - preDuskProgress) + nightG * preDuskProgress);
                int b = (int)(duskB * (1 - preDuskProgress) + nightB * preDuskProgress);

                currentColor = Color.rgb(r, g, b);

            } else {
                // Normal nighttime
                currentAlpha = (int)(220 * darknessFactor);
                currentColor = Color.rgb(10, 10, 40);
            }
        }
    }

    /**
     * Draw the day-night overlay
     */
    public void draw(Canvas canvas, int screenWidth, int screenHeight) {
        if (currentAlpha > 0) {
            overlayPaint.setColor(currentColor);
            overlayPaint.setAlpha(currentAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, overlayPaint);
        }
    }

    /**
     * Get current game time as formatted string
     */
    public String getFormattedTime() {
        int hours = (int)(gameTime / 3600);
        int minutes = (int)((gameTime % 3600) / 60);
        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * Get current time phase description
     */
    public String getTimePhase() {
        int hour = (int)(gameTime / 3600);

        if (hour >= 5 && hour < 8) {
            return "Dawn " + hour;
        } else if (hour >= 8 && hour < 12) {
            return "Morning " + hour;
        } else if (hour >= 12 && hour < 14) {
            return "Noon " + hour;
        } else if (hour >= 14 && hour < 17) {
            return "Afternoon " + hour;
        } else if (hour >= 17 && hour < 19) {
            return "Dusk " + hour;
        } else if (hour >= 19 || hour < 5) {
            return "Night " + hour;
        }
        return "Unknown";
    }

    /**
     * Set time speed multiplier
     */
    public void setTimeSpeed(float speed) {
        this.timeSpeed = speed;
    }

    /**
     * Get current alpha value (for debugging)
     */
    public int getCurrentAlpha() {
        return currentAlpha;
    }
}

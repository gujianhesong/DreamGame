package com.game.dream.system;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dynamic weather system with rain, snow, fog, and clear weather
 */
public class WeatherSystem {
    public enum WeatherType {
        CLEAR,      // Clear sky
        RAIN,       // Rain
        HEAVY_RAIN, // Heavy rain
        SNOW,       // Light snow
        HEAVY_SNOW, // Heavy snow
        FOG         // Foggy
    }

    private WeatherType currentWeather;
    private WeatherType targetWeather;
    private float weatherTransition; // 0-1 for smooth transitions
    private long lastWeatherChange;
    private long weatherDuration;
    private Random random;

    // Weather particles
    private List<WeatherParticle> particles;
    private int maxParticles;

    // Fog overlay
    private Paint fogPaint;
    private float fogDensity;

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    public WeatherSystem() {
        this.currentWeather = WeatherType.CLEAR;
        this.targetWeather = WeatherType.CLEAR;
        this.weatherTransition = 0;
        this.random = new Random();
        this.particles = new ArrayList<>();
        this.maxParticles = 200;
        this.fogDensity = 0;
        this.lastWeatherChange = System.currentTimeMillis();
        this.weatherDuration = 30000 + random.nextInt(60000); // 30-90 seconds

        fogPaint = new Paint();
        fogPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Update weather system
     */
    public void update(long deltaTime, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Check if it's time to change weather
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeatherChange > weatherDuration) {
            changeWeather();
            lastWeatherChange = currentTime;
            weatherDuration = 30000 + random.nextInt(90000); // 30-120 seconds
        }

        // Smooth transition between weather types
        if (currentWeather != targetWeather) {
            weatherTransition += deltaTime / 3000f; // 3 seconds transition
            if (weatherTransition >= 1.0f) {
                weatherTransition = 1.0f;
                currentWeather = targetWeather;
            }
        }

        // Update particles
        updateParticles(deltaTime);

        // Update fog density
        updateFog();
    }

    /**
     * Change to a new random weather type
     */
    private void changeWeather() {
        WeatherType[] possibleWeathers = WeatherType.values();
        WeatherType newWeather;

        // Don't change to the same weather
        do {
            newWeather = possibleWeathers[random.nextInt(possibleWeathers.length)];
        } while (newWeather == currentWeather);

        targetWeather = newWeather;
        weatherTransition = 0;

        // Initialize particles for new weather
        initializeParticles();
    }

    /**
     * Initialize particles based on weather type
     */
    private void initializeParticles() {
        particles.clear();

        int particleCount = 0;
        switch (targetWeather) {
            case RAIN:
                particleCount = 100;
                break;
            case HEAVY_RAIN:
                particleCount = 200;
                break;
            case SNOW:
                particleCount = 80;
                break;
            case HEAVY_SNOW:
                particleCount = 150;
                break;
            default:
                particleCount = 0;
                break;
        }

        for (int i = 0; i < particleCount; i++) {
            particles.add(createParticle(targetWeather));
        }
    }

    /**
     * Create a weather particle
     */
    private WeatherParticle createParticle(WeatherType weather) {
        WeatherParticle particle = new WeatherParticle();
        particle.x = random.nextInt(screenWidth + 200) - 100;
        particle.y = random.nextInt(screenHeight);

        switch (weather) {
            case RAIN:
            case HEAVY_RAIN:
                particle.vx = -2 + random.nextFloat() * 4; // Slight wind
                particle.vy = 10 + random.nextFloat() * 5; // Fast falling
                particle.size = 2 + random.nextFloat() * 2;
                particle.color = Color.argb(150, 174, 194, 224); // Light blue
                break;
            case SNOW:
            case HEAVY_SNOW:
                particle.vx = -1 + random.nextFloat() * 2; // Gentle drift
                particle.vy = 2 + random.nextFloat() * 2; // Slow falling
                particle.size = 2 + random.nextFloat() * 3;
                particle.color = Color.argb(200, 255, 255, 255); // White
                break;
        }

        return particle;
    }

    /**
     * Update all particles
     */
    private void updateParticles(long deltaTime) {
        float deltaSeconds = deltaTime / 1000f;

        for (int i = particles.size() - 1; i >= 0; i--) {
            WeatherParticle p = particles.get(i);

            // Move particle
            p.x += p.vx * deltaSeconds * 60;
            p.y += p.vy * deltaSeconds * 60;

            // Reset if out of bounds
            if (p.y > screenHeight || p.x < -100 || p.x > screenWidth + 100) {
                p.x = random.nextInt(screenWidth + 200) - 100;
                p.y = -10;

                // Add some randomness to respawn position
                if (currentWeather == WeatherType.RAIN || currentWeather == WeatherType.HEAVY_RAIN) {
                    p.vx = -2 + random.nextFloat() * 4;
                    p.vy = 10 + random.nextFloat() * 5;
                } else if (currentWeather == WeatherType.SNOW || currentWeather == WeatherType.HEAVY_SNOW) {
                    p.vx = -1 + random.nextFloat() * 2;
                    p.vy = 2 + random.nextFloat() * 2;
                }
            }
        }
    }

    /**
     * Update fog density based on weather
     */
    private void updateFog() {
        float targetFog = 0;

        switch (currentWeather) {
            case FOG:
                targetFog = 0.6f;
                break;
            case HEAVY_RAIN:
                targetFog = 0.3f;
                break;
            case HEAVY_SNOW:
                targetFog = 0.4f;
                break;
            default:
                targetFog = 0;
                break;
        }

        // Smooth fog transition
        fogDensity += (targetFog - fogDensity) * 0.02f;
    }

    /**
     * Draw weather effects
     */
    public void draw(Canvas canvas) {
        // Draw particles (rain/snow)
        if (!particles.isEmpty()) {
            Paint particlePaint = new Paint();
            particlePaint.setAntiAlias(true);

            // Apply transition alpha
            int baseAlpha = getCurrentParticleAlpha();

            for (WeatherParticle p : particles) {
                int alpha = (int)(baseAlpha * weatherTransition);
                particlePaint.setColor(setAlpha(p.color, alpha));
                particlePaint.setStrokeWidth(p.size);

                if (currentWeather == WeatherType.RAIN || currentWeather == WeatherType.HEAVY_RAIN) {
                    // Draw rain as lines
                    canvas.drawLine(p.x, p.y, p.x + p.vx * 0.5f, p.y + p.vy * 0.5f, particlePaint);
                } else {
                    // Draw snow as circles
                    canvas.drawCircle(p.x, p.y, p.size, particlePaint);
                }
            }
        }

        // Draw fog overlay
        if (fogDensity > 0.01f) {
            fogPaint.setColor(Color.argb((int)(fogDensity * 255), 200, 200, 210));
            canvas.drawRect(0, 0, screenWidth, screenHeight, fogPaint);
        }
    }

    /**
     * Get current particle alpha based on weather intensity
     */
    private int getCurrentParticleAlpha() {
        switch (currentWeather) {
            case RAIN:
                return 150;
            case HEAVY_RAIN:
                return 200;
            case SNOW:
                return 180;
            case HEAVY_SNOW:
                return 220;
            default:
                return 0;
        }
    }

    /**
     * Set alpha value of a color
     */
    private int setAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Get current weather type
     */
    public WeatherType getCurrentWeather() {
        return currentWeather;
    }

    /**
     * Get weather description
     */
    public String getWeatherDescription() {
        switch (currentWeather) {
            case CLEAR:
                return "Clear";
            case RAIN:
                return "Rain";
            case HEAVY_RAIN:
                return "Heavy Rain";
            case SNOW:
                return "Snow";
            case HEAVY_SNOW:
                return "Heavy Snow";
            case FOG:
                return "Fog";
            default:
                return "Unknown";
        }
    }

    /**
     * Force a specific weather type (for testing)
     */
    public void setWeather(WeatherType weather) {
        targetWeather = weather;
        currentWeather = weather;
        weatherTransition = 1.0f;
        initializeParticles();
    }

    /**
     * Weather particle data
     */
    private static class WeatherParticle {
        float x, y;
        float vx, vy;
        float size;
        int color;
    }
}

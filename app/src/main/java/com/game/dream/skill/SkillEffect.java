package com.game.dream.skill;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.GameEngine;
import com.game.dream.LogUtil;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;

import java.util.List;

/**
 * Represents a persistent skill effect on the map (like "Ten Thousand Swords")
 */
public class SkillEffect {
    public enum Type {
        SWORD_STORM, // 万剑归宗
        POISON_CLOUD
    }

    private Type type;
    private float x, y;
    private float radius;
    private long duration;
    private long startTime;
    private long lastDamageTime;
    private int damageInterval; // e.g., 500ms
    private int totalHits;
    private int currentHits;
    private boolean isActive;

    public SkillEffect(Type type, float x, float y, float radius, long duration,
                       int damageInterval, int totalHits) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.lastDamageTime = 0;
        this.damageInterval = damageInterval;
        this.totalHits = totalHits;
        this.currentHits = 0;
        this.isActive = true;
    }

    public void update(List<Enemy> enemies) {
        if (!isActive) return;
        long currentTime = System.currentTimeMillis();

        // Check if duration expired or max hits reached
        if (currentTime - startTime > duration || currentHits >= totalHits) {
            isActive = false;
            return;
        }

        // Check damage interval
        if (currentTime - lastDamageTime >= damageInterval) {
            applyDamage(enemies);
            lastDamageTime = currentTime;
        }
    }

    private void applyDamage(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist <= radius) {
                GameEngine.getInstance().handlePlayerCasterDamageToEnemy(enemy, SkillType.MAIN_WanJianGuiZong);

                currentHits++;
            }
        }
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (!isActive) return;

        float screenX = getX() + offsetX;
        float screenY = getY() + offsetY;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw the sword storm area
        // Pulsing effect
        long time = System.currentTimeMillis();
        float pulse = 1.0f + 0.1f * (float)Math.sin(time / 50.0);
        // Background glow
        paint.setColor(Color.argb(50, 200, 200, 255));
        canvas.drawCircle(screenX, screenY, radius * pulse, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.argb(150, 100, 150, 255));
        canvas.drawCircle(screenX, screenY, radius, paint);

        // 3. 绘制旋转的“剑气”
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(5, 0, 0, Color.CYAN); // 增加发光效果

        // 关键：加快旋转速度，使用 time / 50.0f
        float baseAngle = (time / 15) % 360;

        // 绘制 12 把飞剑（简化为线段）
        for (int i = 0; i < 12; i++) {
            // 每把剑之间间隔 30 度
            float angleDeg = baseAngle + (i * 30);
            float rad = angleDeg * (float)Math.PI / 180;

            // 剑的内端点（在圆环内部）
            float innerR = radius * 0.6f;
            float sx = screenX + (float)Math.cos(rad) * innerR;
            float sy = screenY + (float)Math.sin(rad) * innerR;

            // 剑的外端点（稍微超出圆环）
            float outerR = radius * 1.1f;
            float ex = screenX + (float)Math.cos(rad) * outerR;
            float ey = screenY + (float)Math.sin(rad) * outerR;

            canvas.drawLine(sx, sy, ex, ey, paint);

            // 在剑尖画一个小亮点
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(ex, ey, 3, paint);
            paint.setStyle(Paint.Style.STROKE); // 恢复
        }

        paint.clearShadowLayer(); // 清除阴影，避免影响后续绘制
    }

    public boolean isActive() { return isActive; }
    public float getX() { return x; }
    public float getY() { return y; }
}
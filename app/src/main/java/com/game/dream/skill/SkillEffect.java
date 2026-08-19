package com.game.dream.skill;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.GameEngine;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;

import java.util.List;

/**
 * Represents a persistent skill effect on the map (like "Ten Thousand Swords")
 */
public class SkillEffect {
    public enum Type {
        SWORD_STORM, // 万剑归宗
        POISON_CLOUD,  // 毒雾阵
        HEALING_ZONE,  // 治疗法阵
        SANDSTORM      // 飞沙走石
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

    // 沙暴等级相关
    private int skillLevel = 1;
    private float slowRatio = 0.4f;   // 减速比例
    private float stunChance = 0f;    // 眩晕概率

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
        if (currentTime - startTime > duration/* || currentHits >= totalHits*/) {
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
                if (type == Type.SWORD_STORM) {
                    GameEngine.getInstance().handlePlayerCasterDamageToEnemy(enemy, SkillType.MAIN_WanJianGuiZong);
                } else if (type == Type.POISON_CLOUD) {
                    GameEngine.getInstance().handlePlayerCasterDamageToEnemy(enemy, SkillType.MAIN_DuWuZhen);
                } else if (type == Type.SANDSTORM) {
                    GameEngine.getInstance().handlePlayerCasterDamageToEnemy(enemy, SkillType.MAIN_FeiShaZouShi);
                    // 沙暴减速效果
                    if (slowRatio > 0) {
                        enemy.applyCC(com.game.dream.figure.Character.CrowdControlType.SLOW, (long)(damageInterval * 1.5f));
                    }
                    // 沙暴眩晕效果(概率触发)
                    if (stunChance > 0 && Math.random() < stunChance) {
                        enemy.applyCC(com.game.dream.figure.Character.CrowdControlType.STUN, 800);
                    }
                }

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

        if (type == Type.SWORD_STORM) {
            // Draw the sword storm area
            // Pulsing effect
            long time = System.currentTimeMillis();
            float pulse = 1.0f + 0.1f * (float) Math.sin(time / 50.0);
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
                float rad = angleDeg * (float) Math.PI / 180;

                // 剑的内端点（在圆环内部）
                float innerR = radius * 0.6f;
                float sx = screenX + (float) Math.cos(rad) * innerR;
                float sy = screenY + (float) Math.sin(rad) * innerR;

                // 剑的外端点（稍微超出圆环）
                float outerR = radius * 1.1f;
                float ex = screenX + (float) Math.cos(rad) * outerR;
                float ey = screenY + (float) Math.sin(rad) * outerR;

                canvas.drawLine(sx, sy, ex, ey, paint);

                // 在剑尖画一个小亮点
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(ex, ey, 3, paint);
                paint.setStyle(Paint.Style.STROKE); // 恢复
            }

            paint.clearShadowLayer(); // 清除阴影，避免影响后续绘制
        } else if (type == Type.POISON_CLOUD) {
            long time = System.currentTimeMillis();
            // 1. 绘制多层半透明绿色圆圈，模拟烟雾缭绕
            for (int i = 3; i > 0; i--) {
                float offset = (time / 500.0f + i) % 2; // 动态偏移
                float r = radius * (0.8f + 0.2f * offset);
                int alpha = (int) (40 / i); // 越往外越淡

                paint.setColor(Color.argb(alpha, 50, 200, 50)); // 毒绿色
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(screenX, screenY, r, paint);
            }

            // 2. 绘制一些随机的小气泡
            paint.setColor(Color.argb(100, 100, 255, 100));
            for (int i = 0; i < 5; i++) {
                double angle = (time / 1000.0 + i * 72) * Math.PI * 2 / 360;
                float dist = radius * 0.5f;
                float bx = screenX + (float) Math.cos(angle) * dist;
                float by = screenY + (float) Math.sin(angle) * dist;
                canvas.drawCircle(bx, by, 5, paint);
            }
        } else if (type == Type.SANDSTORM) {
            long time = System.currentTimeMillis();

            // 1. 绘制沙暴底层(黄褐色旋转沙雾)
            for (int i = 3; i > 0; i--) {
                float pulse = 1.0f + 0.08f * (float) Math.sin(time / 200.0 + i);
                float r = radius * (0.7f + 0.1f * i) * pulse;
                int alpha = (int) (100 / i);

                paint.setColor(Color.argb(alpha, 180, 150, 80)); // 沙土色
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(screenX, screenY, r, paint);
            }

            // 2. 绘制旋转的沙粒(快速旋转)
            float rotAngle = (time / 8) % 360; // 快速旋转
            paint.setColor(Color.argb(120, 210, 180, 100)); // 亮沙色
            for (int i = 0; i < 16; i++) {
                float angleDeg = rotAngle + (i * 22.5f);
                float rad = angleDeg * (float) Math.PI / 180;
                float dist = radius * (0.3f + 0.4f * ((i % 3) / 3.0f));
                float px = screenX + (float) Math.cos(rad) * dist;
                float py = screenY + (float) Math.sin(rad) * dist;
                float pSize = 2 + (i % 3);
                canvas.drawCircle(px, py, pSize, paint);
            }

            // 3. 绘制飞溅的碎石(中等大小,旋转方向不同)
            float rockAngle = (360 - (time / 12) % 360); // 反向旋转
            paint.setColor(Color.argb(150, 140, 120, 90)); // 石灰色
            for (int i = 0; i < 8; i++) {
                float angleDeg = rockAngle + (i * 45);
                float rad = angleDeg * (float) Math.PI / 180;
                float dist = radius * (0.4f + 0.2f * ((i % 2)));
                float rx = screenX + (float) Math.cos(rad) * dist;
                float ry = screenY + (float) Math.sin(rad) * dist;
                // 绘制小石块(不规则圆形)
                canvas.drawCircle(rx, ry, 3 + (i % 3), paint);
            }

            // 4. 沙暴边界模糊圈
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.argb(40, 200, 170, 90));
            canvas.drawCircle(screenX, screenY, radius, paint);
            paint.setStyle(Paint.Style.FILL);
        }

    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setSkillLevel(int level) { this.skillLevel = level; }
    public int getSkillLevel() { return skillLevel; }

    public void setSlowRatio(float ratio) { this.slowRatio = ratio; }
    public float getSlowRatio() { return slowRatio; }

    public void setStunChance(float chance) { this.stunChance = chance; }
    public float getStunChance() { return stunChance; }
}
package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.Player;

/**
 * Role information panel - displays player stats and equipment
 */
public class RoleInfoPanel {
    private Player player;
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    public RoleInfoPanel(Player player) {
        this.player = player;
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
    }

    /**
     * Toggle panel visibility
     */
    public void toggleVisibility() {
        isVisible = !isVisible;
    }

    /**
     * Show panel
     */
    public void show() {
        isVisible = true;
    }

    /**
     * Hide panel
     */
    public void hide() {
        isVisible = false;
    }

    /**
     * Check if panel is visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set panel bounds
     */
    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        // Calculate close button position (top-right corner)
        int buttonSize = 50;
        int padding = 10;
        closeButton.set(
                panelBounds.right - buttonSize - padding,
                panelBounds.top + padding,
                panelBounds.right - padding,
                panelBounds.top + padding + buttonSize
        );
    }

    /**
     * Draw the character info panel
     */
    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Panel background (semi-transparent dark)
        paint.setColor(Color.argb(230, 20, 20, 30));
        canvas.drawRect(panelBounds, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(100, 150, 255));
        canvas.drawRect(panelBounds, paint);

        paint.setStyle(Paint.Style.FILL);

        // Draw close button
        drawCloseButton(canvas, paint);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("角色信息", panelBounds.centerX(), panelBounds.top + 45, paint);

        // Divider line
        paint.setStrokeWidth(2);
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 60,
                panelBounds.right - 20, panelBounds.top + 60, paint);

        // Draw stats
        int startY = panelBounds.top + 90;
        int lineHeight = 45;

        // Health
        drawStatRow(canvas, paint, "生命值", player.getHealth() + " / " + player.getMaxHealth(),
                startY, player.getHealthPercent());

        // Speed
        drawStatRow(canvas, paint, "移动速度", String.valueOf((int)player.getSpeed()),
                startY + lineHeight, player.getSpeed() / 200f);

        // Attack
        drawStatRow(canvas, paint, "物理攻击", String.valueOf(player.getAttackDamage()),
                startY + lineHeight * 2, player.getAttackDamage() / 50f);

        // Magic Damage
        drawStatRow(canvas, paint, "法术伤害", String.valueOf(player.getMagicDamage()),
                startY + lineHeight * 3, player.getMagicDamage() / 50f);

        // Defense
        drawStatRow(canvas, paint, "防御力", String.valueOf(player.getDefense()),
                startY + lineHeight * 4, player.getDefense() / 30f);

        // Position
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("位置: (" + (int)player.getX() + ", " + (int)player.getY() + ")",
                panelBounds.left + 20, startY + lineHeight * 5 + 30, paint);

        // Facing direction
        String direction = getDirectionName(player.getFacingDirection());
        canvas.drawText("朝向: " + direction, panelBounds.left + 20, startY + lineHeight * 6 + 30, paint);

        // Status
        String status = player.isCurrentlyInvincible() ? "✨ 无敌状态" : "正常";
        paint.setColor(player.isCurrentlyInvincible() ? Color.YELLOW : Color.GREEN);
        canvas.drawText("状态: " + status, panelBounds.left + 20, startY + lineHeight * 7 + 30, paint);

        // Close hint
        paint.setColor(Color.GRAY);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("点击任意处关闭", panelBounds.centerX(), panelBounds.bottom - 20, paint);
    }

    /**
     * Draw close button
     */
    private void drawCloseButton(Canvas canvas, Paint paint) {
        // Button background
        paint.setColor(Color.argb(180, 255, 80, 80));
        canvas.drawRoundRect(closeButton.left, closeButton.top,
                closeButton.right, closeButton.bottom, 8, 8, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(closeButton.left, closeButton.top,
                closeButton.right, closeButton.bottom, 8, 8, paint);

        // X symbol
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);

        float padding = 12;
        // Draw X
        canvas.drawLine(closeButton.left + padding, closeButton.top + padding,
                closeButton.right - padding, closeButton.bottom - padding, paint);
        canvas.drawLine(closeButton.right - padding, closeButton.top + padding,
                closeButton.left + padding, closeButton.bottom - padding, paint);
    }

    /**
     * Draw a stat row with bar
     */
    private void drawStatRow(Canvas canvas, Paint paint, String label, String value,
                             int y, float percent) {
        int leftMargin = 20;
        int rightMargin = 20;
        int barWidth = 150;
        int barHeight = 16;

        // Label
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, panelBounds.left + leftMargin, y + 20, paint);

        // Value
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value, panelBounds.right - rightMargin, y + 20, paint);

        // Bar background
        int barX = panelBounds.centerX() - barWidth / 2;
        int barY = y + 8;
        paint.setColor(Color.BLACK);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);

        // Bar fill
        percent = Math.max(0, Math.min(1, percent));
        int barColor;
        if (percent > 0.6f) {
            barColor = Color.rgb(100, 200, 100);
        } else if (percent > 0.3f) {
            barColor = Color.rgb(255, 200, 50);
        } else {
            barColor = Color.rgb(255, 100, 100);
        }
        paint.setColor(barColor);
        canvas.drawRect(barX, barY, barX + barWidth * percent, barY + barHeight, paint);
    }

    /**
     * Get direction name
     */
    private String getDirectionName(int direction) {
        switch (direction) {
            case 0: return "↓ 下";
            case 1: return "↑ 上";
            case 2: return "← 左";
            case 3: return "→ 右";
            default: return "未知";
        }
    }

    /**
     * Handle touch event
     */
    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        // Check if touched close button
        if (closeButton.contains((int)x, (int)y)) {
            hide();
            return true;
        }
        return false;
    }
}

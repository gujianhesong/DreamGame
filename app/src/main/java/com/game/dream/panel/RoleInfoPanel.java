package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.Player;
import com.game.dream.bean.AddPointResult;
import com.game.dream.bean.RoleInfo;
import com.game.dream.system.RoleSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Role information panel - displays player stats and equipment
 */
public class RoleInfoPanel {
    private Player player;
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    // Attribute buttons for +/- operations
    private List<Rect> plusButtons;
    private List<Rect> minusButtons;
    private Rect confirmButton;

    // Attribute names mapping
    private static final String[] ATTR_NAMES = {
            "命中", "伤害", "防御", "速度", "躲避", "灵力"
    };
    private static final String[] BASE_ATTR_NAMES = {
            "体质", "魔力", "力量", "耐力", "敏捷", "潜力"
    };

    private boolean isInitAttributeButtons = false;

    private int tempAddTi, tempAddMo, tempAddLi, tempAddNai, tempAddMin;
    private int tempRemainPoints;

    private AddPointResult addPointResult;

    public RoleInfoPanel(Player player) {
        this.player = player;
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.plusButtons = new ArrayList<>();
        this.minusButtons = new ArrayList<>();
        this.confirmButton = new Rect();
    }

    /**
     * Toggle panel visibility
     */
    public void toggleVisibility() {
        isVisible = !isVisible;
        tempRemainPoints = RoleSystem.getInstance().getRoleInfo().getRemainPoints();
    }

    /**
     * Show panel
     */
    public void show() {
        isVisible = true;
        tempRemainPoints = RoleSystem.getInstance().getRoleInfo().getRemainPoints();
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
     * Initialize attribute button positions
     */
    private void initAttributeButtons(int startY) {
        plusButtons.clear();
        minusButtons.clear();

        int lineHeight = 45;
        int buttonSize = 35;
        int buttonGap = 10;
        int deltaY = 8;

        for (int i = 0; i < 6; i++) {
            int y = startY + i * lineHeight;

            // Plus button (right side of derived attr)
            int plusX = panelBounds.centerX() + 160;
            plusButtons.add(new Rect(plusX, y - deltaY,
                    plusX + buttonSize, y + buttonSize - deltaY));

            // Minus button (left of plus)
            int minusX = plusX - buttonSize - buttonGap;
            minusButtons.add(new Rect(minusX, y - deltaY,
                    minusX + buttonSize, y + buttonSize - deltaY));
        }

        // Confirm button at bottom
        int btnWidth = 120;
        int btnHeight = 50;
        confirmButton.set(
                panelBounds.centerX() - btnWidth / 2,
                panelBounds.bottom - btnHeight - 20,
                panelBounds.centerX() + btnWidth / 2,
                panelBounds.bottom - 20
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

        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();

        // Character Name and Level
        int startY = panelBounds.top + 90;
        int lineHeight = 40;

        // Name
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("名称", panelBounds.left + 20, startY + 20, paint);

        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(roleInfo.getName(), panelBounds.right - 20, startY + 20, paint);
        startY += lineHeight;

        // Level
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("等级", panelBounds.left + 20, startY + 20, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Lv." + roleInfo.getLevel(), panelBounds.right - 20, startY + 20, paint);
        startY += lineHeight;

        // Label
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("称谓", panelBounds.left + 20, startY + 20, paint);

        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(roleInfo.getLabel(), panelBounds.right - 20, startY + 20, paint);
        startY += lineHeight;

        // Experience bar
        drawExpBar(canvas, paint, "经验", roleInfo.getExp(),
                RoleSystem.getInstance().getExpForNextLevel(), startY);
        startY += lineHeight;

        // Health
        drawStatRow(canvas, paint, "生命值", roleInfo.getBlood() + " / " + roleInfo.getBloodCap(),
                startY, roleInfo.getBlood() * 1f / roleInfo.getBloodCap());
        if (addPointResult != null && addPointResult.getBlood() > 0) {
            paint.setColor(Color.WHITE);
            canvas.drawText("+" + addPointResult.getBlood(), panelBounds.right - 160, startY + 20, paint);
        }

        startY += lineHeight;

        // Magic
        drawStatRow(canvas, paint, "魔法值", roleInfo.getMagic() + " / " + roleInfo.getMagicCap(),
                startY, roleInfo.getMagic() * 1f / roleInfo.getMagicCap());
        if (addPointResult != null && addPointResult.getMagic() > 0) {
            paint.setColor(Color.WHITE);
            canvas.drawText("+" + addPointResult.getMagic(), panelBounds.right - 160, startY + 20, paint);
        }
        startY += lineHeight;

        // Draw attribute rows
        if (!isInitAttributeButtons) {
            initAttributeButtons(startY);
            isInitAttributeButtons = true;
        }
        drawAttributeRows(canvas, paint, roleInfo, startY);

        // Draw confirm button
        drawConfirmButton(canvas, paint);
    }

    /**
     * Calculate exp needed for next level
     */
    private long getExpToNextLevel(int level) {
        return (long) (100 * Math.pow(level, 1.5));
    }

    /**
     * Draw attribute rows with +/- buttons
     */
    private void drawAttributeRows(Canvas canvas, Paint paint, RoleInfo roleInfo, int startY) {
        int lineHeight = 45;
        int leftMargin = 20;
        int rightMargin = 20;

        // Get current attribute values
        int[] derivedAttrs = {
                roleInfo.getHit(),      // 命中
                roleInfo.getAttack(),   // 伤害
                roleInfo.getDefense(),  // 防御
                roleInfo.getSpeed(),    // 速度
                roleInfo.getDodge(),    // 躲避
                roleInfo.getMana()      // 灵力
        };

        int[] baseAttrs = {
                roleInfo.getPropTi(),   // 体质
                roleInfo.getPropMo(),   // 魔力
                roleInfo.getPropLi(),   // 力量
                roleInfo.getPropNai(),  // 耐力
                roleInfo.getPropMin(),  // 敏捷
                tempRemainPoints // 潜力 (show remaining points)
        };

        for (int i = 0; i < 6; i++) {
            int y = startY + i * lineHeight;

            // Derived attribute name and value (left side)
            paint.setColor(Color.WHITE);
            paint.setTextSize(28);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(ATTR_NAMES[i], panelBounds.left + leftMargin, y + 20, paint);

            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(derivedAttrs[i]),
                    panelBounds.left + leftMargin + 90, y + 20, paint);

            if (addPointResult != null) {
                switch (i) {
                    case 0:
                        if (addPointResult.getHit() > 0) {
                            canvas.drawText("+" + addPointResult.getHit(), panelBounds.left + 150, y + 20, paint);
                        }
                        break;
                    case 1:
                        if (addPointResult.getAttack() > 0) {
                            canvas.drawText("+" + addPointResult.getAttack(), panelBounds.left + 150, y + 20, paint);
                        }
                        break;
                    case 2:
                        if (addPointResult.getDefense() > 0) {
                            canvas.drawText("+" + addPointResult.getDefense(), panelBounds.left + 150, y + 20, paint);
                        }
                        break;
                    case 3:
                        if (addPointResult.getSpeed() > 0) {
                            canvas.drawText("+" + addPointResult.getSpeed(), panelBounds.left + 150, y + 20, paint);
                        }
                        break;
                    case 4:
                        if (addPointResult.getDodge() > 0) {
                            canvas.drawText("+" + addPointResult.getDodge(), panelBounds.left + 150, y + 20, paint);
                        }
                        break;
                    case 5:
                        if (addPointResult.getMana() > 0) {
                            canvas.drawText("+" + addPointResult.getMana(), panelBounds.left + 150, y + 20, paint);
                        }
                        break;
                }
            }

            // Base attribute name and value (center)
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(BASE_ATTR_NAMES[i], panelBounds.centerX() - 60, y + 20, paint);

            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(baseAttrs[i]),
                    panelBounds.centerX() + 30, y + 20, paint);

            if (addPointResult != null) {
                switch (i) {
                    case 0:
                        if (tempAddTi > 0) {
                            canvas.drawText("+" + tempAddTi, panelBounds.centerX() + 70, y + 20, paint);
                        }
                        break;
                    case 1:
                        if (tempAddMo > 0) {
                            canvas.drawText("+" + tempAddMo, panelBounds.centerX() + 70, y + 20, paint);
                        }
                        break;
                    case 2:
                        if (tempAddLi > 0) {
                            canvas.drawText("+" + tempAddLi, panelBounds.centerX() + 70, y + 20, paint);
                        }
                        break;
                    case 3:
                        if (tempAddNai > 0) {
                            canvas.drawText("+" + tempAddNai, panelBounds.centerX() + 70, y + 20, paint);
                        }
                        break;
                    case 4:
                        if (tempAddMin > 0) {
                            canvas.drawText("+" + tempAddMin, panelBounds.centerX() + 70, y + 20, paint);
                        }
                        break;
                }
            }

            // Draw +/- buttons (except for last row - potential)
            if (i < 5) {
                drawPlusButton(canvas, paint, plusButtons.get(i));
                drawMinusButton(canvas, paint, minusButtons.get(i));
            }
        }
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
     * Draw plus button
     */
    private void drawPlusButton(Canvas canvas, Paint paint, Rect button) {
        // Button background
        paint.setColor(Color.argb(180, 100, 200, 100));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Plus symbol
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);

        float centerX = (button.left + button.right) / 2f;
        float centerY = (button.top + button.bottom) / 2f + 10;
        canvas.drawText("+", centerX, centerY, paint);

        paint.setStrokeWidth(1);
    }

    /**
     * Draw minus button
     */
    private void drawMinusButton(Canvas canvas, Paint paint, Rect button) {
        // Button background
        paint.setColor(Color.argb(180, 200, 100, 100));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Minus symbol
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);

        float centerX = (button.left + button.right) / 2f;
        float centerY = (button.top + button.bottom) / 2f + 10;
        canvas.drawText("-", centerX, centerY, paint);

        paint.setStrokeWidth(1);
    }

    /**
     * Draw confirm button
     */
    private void drawConfirmButton(Canvas canvas, Paint paint) {
        // Button background
        paint.setColor(Color.argb(200, 50, 150, 255));
        canvas.drawRoundRect(confirmButton.left, confirmButton.top,
                confirmButton.right, confirmButton.bottom, 8, 8, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(confirmButton.left, confirmButton.top,
                confirmButton.right, confirmButton.bottom, 8, 8, paint);

        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.CENTER);

        float centerX = (confirmButton.left + confirmButton.right) / 2f;
        float centerY = (confirmButton.top + confirmButton.bottom) / 2f + 9;
        canvas.drawText("确认", centerX, centerY, paint);
    }

    /**
     * Handle touch event
     */
    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        // Check if touched close button
        if (closeButton.contains((int) x, (int) y)) {
            hide();
            return true;
        }

        // Check if touched confirm button
        if (confirmButton.contains((int) x, (int) y)) {
            // Save changes here
            confirmAttributes();
            return true;
        }

        // Check if touched +/- buttons
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        for (int i = 0; i < 5; i++) { // Only first 5 attributes have buttons
            if (plusButtons.get(i).contains((int) x, (int) y)) {
                increaseAttribute(i, roleInfo);
                return true;
            } else if (minusButtons.get(i).contains((int) x, (int) y)) {
                decreaseAttribute(i, roleInfo);
                return true;
            }
        }

        return false;
    }

    /**
     * Increase attribute by 1 point
     */
    private void increaseAttribute(int index, RoleInfo roleInfo) {
        if (tempRemainPoints <= 0) return;

        switch (index) {
            case 0:
                tempAddTi += 1;
                tempRemainPoints -= 1;
                break;
            case 1:
                tempAddMo += 1;
                tempRemainPoints -= 1;
                break;
            case 2:
                tempAddLi += 1;
                tempRemainPoints -= 1;
                break;
            case 3:
                tempAddNai += 1;
                tempRemainPoints -= 1;
                break;
            case 4:
                tempAddMin += 1;
                tempRemainPoints -= 1;
                break;
        }
        addPointResult = RoleSystem.getInstance().caculateAddPoints(tempAddTi, tempAddMo, tempAddLi, tempAddNai, tempAddMin);
    }

    /**
     * Decrease attribute by 1 point
     */
    private void decreaseAttribute(int index, RoleInfo roleInfo) {
        switch (index) {
            case 0:
                if (tempAddTi > 0) {
                    tempAddTi -= 1;
                    tempRemainPoints += 1;
                }
                break;
            case 1:
                if (tempAddMo > 0) {
                    tempAddMo -= 1;
                    tempRemainPoints += 1;
                }
                break;
            case 2:
                if (tempAddLi > 0) {
                    tempAddLi -= 1;
                    tempRemainPoints += 1;
                }
                break;
            case 3:
                if (tempAddNai > 0) {
                    tempAddNai -= 1;
                    tempRemainPoints += 1;
                }
                break;
            case 4:
                if (tempAddMin > 0) {
                    tempAddMin -= 1;
                    tempRemainPoints += 1;
                }
                break;
        }
        addPointResult = RoleSystem.getInstance().caculateAddPoints(tempAddTi, tempAddMo, tempAddLi, tempAddNai, tempAddMin);
    }

    private void confirmAttributes() {
        if (tempAddTi > 0 || tempAddMo > 0 || tempAddLi > 0 || tempAddNai > 0 || tempAddMin > 0) {
            addPointResult = RoleSystem.getInstance().caculateAddPoints(tempAddTi, tempAddMo, tempAddLi, tempAddNai, tempAddMin);
            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
            roleInfo.setBloodCap(roleInfo.getBlood() + addPointResult.getBlood());
            roleInfo.setMagicCap(roleInfo.getMagicCap() + addPointResult.getMagic());
            roleInfo.setHit(roleInfo.getHit() + addPointResult.getHit());
            roleInfo.setAttack(roleInfo.getAttack() + addPointResult.getAttack());
            roleInfo.setDefense(roleInfo.getDefense() + addPointResult.getDefense());
            roleInfo.setSpeed(roleInfo.getSpeed() + addPointResult.getSpeed());
            roleInfo.setDodge(roleInfo.getDodge() + addPointResult.getDodge());
            roleInfo.setMana(roleInfo.getMana() + addPointResult.getMana());

            roleInfo.setPropTi(roleInfo.getPropTi() + tempAddTi);
            roleInfo.setPropMo(roleInfo.getPropMo() + tempAddMo);
            roleInfo.setPropLi(roleInfo.getPropLi() + tempAddLi);
            roleInfo.setPropNai(roleInfo.getPropNai() + tempAddNai);
            roleInfo.setPropMin(roleInfo.getPropMin() + tempAddMin);

            tempAddTi = 0;
            tempAddMo = 0;
            tempAddLi = 0;
            tempAddNai = 0;
            tempAddMin = 0;

            addPointResult = null;

            roleInfo.setRemainPoints(tempRemainPoints);
        }
    }

    /**
     * Draw experience bar with current/max values
     */
    private void drawExpBar(Canvas canvas, Paint paint, String label, long current, long max, int y) {
        int leftMargin = 20;
        int rightMargin = 20;
        int barWidth = 150;
        int barHeight = 16;

        // Label
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, panelBounds.left + leftMargin, y + 20, paint);

        // Value text
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(current + " / " + max, panelBounds.right - rightMargin, y + 20, paint);

        // Bar background
        int barX = panelBounds.centerX() - barWidth / 2;
        int barY = y + 8;
        paint.setColor(Color.BLACK);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);

        // Experience bar fill (blue color)
        float percent = max > 0 ? (float) current / max : 0;
        percent = Math.max(0, Math.min(1, percent));
        paint.setColor(Color.rgb(100, 181, 246)); // Light blue
        canvas.drawRect(barX, barY, barX + barWidth * percent, barY + barHeight, paint);
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
        int barY = y + 2;
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
}
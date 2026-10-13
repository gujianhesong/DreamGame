package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.figure.Player;
import com.game.dream.bean.AddPointResult;
import com.game.dream.bean.RoleInfo;
import com.game.dream.system.RoleSystem;
import com.game.dream.utils.TouchUtil;

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
    private Rect levelUpButton;

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
        this.levelUpButton = new Rect();
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
        int buttonSize = 40;
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
        int lineHeight = 50;
        int buttonSize = 40;
        int buttonGap = 20;
        int deltaY = 8;

        List<Rect> nextPlus = new ArrayList<>(6);
        List<Rect> nextMinus = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            int y = startY + i * lineHeight;

            // Plus button (right side of derived attr)
            int plusX = panelBounds.centerX() + 160;
            nextPlus.add(new Rect(plusX, y - deltaY,
                    plusX + buttonSize, y + buttonSize - deltaY));

            // Minus button (left of plus)
            int minusX = plusX - buttonSize - buttonGap;
            nextMinus.add(new Rect(minusX, y - deltaY,
                    minusX + buttonSize, y + buttonSize - deltaY));
        }
        plusButtons = nextPlus;
        minusButtons = nextMinus;

        // Confirm button at bottom
        int btnWidth = 100;
        int btnHeight = 40;
        confirmButton.set(
                panelBounds.centerX() - btnWidth / 2 + 150,
                panelBounds.bottom - btnHeight - 203,
                panelBounds.centerX() + btnWidth / 2  + 150,
                panelBounds.bottom - 203
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
        int lineHeight = 45;

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

        // Huoli
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("活力", panelBounds.left + 20, startY + 20, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        String huoliStr = roleInfo.getHuoli() + "/" + roleInfo.getHuoliMax();
        canvas.drawText(huoliStr, panelBounds.right - 20, startY + 20, paint);
        startY += lineHeight;

        // Tili
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("体力", panelBounds.left + 20, startY + 20, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        String tiliStr = roleInfo.getTili() + "/" + roleInfo.getTiliMax();
        canvas.drawText(tiliStr, panelBounds.right - 20, startY + 20, paint);
        startY += lineHeight;

        // Health
        drawHpStatRow(canvas, paint, roleInfo.getHp() + " / " + roleInfo.getBloodCap(),
                startY, roleInfo.getHp() * 1f / roleInfo.getBloodCap());
        if (addPointResult != null && addPointResult.getBlood() > 0) {
            paint.setColor(Color.WHITE);
            canvas.drawText("+" + addPointResult.getBlood(), panelBounds.right - 160, startY + 20, paint);
        }

        startY += lineHeight;

        // Magic
        drawMpStatRow(canvas, paint, roleInfo.getMp() + " / " + roleInfo.getMagicCap(),
                startY, roleInfo.getMp() * 1f / roleInfo.getMagicCap());
        if (addPointResult != null && addPointResult.getMagic() > 0) {
            paint.setColor(Color.WHITE);
            canvas.drawText("+" + addPointResult.getMagic(), panelBounds.right - 160, startY + 20, paint);
        }
        startY += lineHeight + 10;

        // Draw attribute rows
        if (!isInitAttributeButtons) {
            initAttributeButtons(startY);
            isInitAttributeButtons = true;
        }
        drawAttributeRows(canvas, paint, roleInfo, startY);

        // Draw confirm button
        drawConfirmButton(canvas, paint);

        // Experience bar
        startY = 880;
        boolean manualLevel = RoleSystem.getInstance().isManualLevelMode();
        layoutLevelUpButton(startY, manualLevel);
        drawExpBar(canvas, paint, "经验", roleInfo.getExp(),
                RoleSystem.getInstance().getExpForNextLevel(), startY, manualLevel);
        if (manualLevel) {
            drawLevelUpButton(canvas, paint, roleInfo);
        }
    }

    private void layoutLevelUpButton(int expRowY, boolean visible) {
        if (!visible) {
            levelUpButton.setEmpty();
            return;
        }
        int btnWidth = 90;
        int btnHeight = 40;
        levelUpButton.set(
                panelBounds.right - 20 - btnWidth,
                expRowY - 6,
                panelBounds.right - 20,
                expRowY - 6 + btnHeight
        );
    }

    /**
     * Calculate exp needed for next level
     */
    private long getExpToNextLevel(int level) {
        return 500L * level * level;
    }

    /**
     * Draw attribute rows with +/- buttons
     */
    private void drawAttributeRows(Canvas canvas, Paint paint, RoleInfo roleInfo, int startY) {
        int lineHeight = 50;
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
            List<Rect> plus = plusButtons;
            List<Rect> minus = minusButtons;
            if (i < 5 && i < plus.size() && i < minus.size()) {
                // Determine if buttons should be enabled
                boolean plusEnabled = tempRemainPoints > 0;
                boolean minusEnabled = false;

                switch (i) {
                    case 0:
                        minusEnabled = tempAddTi > 0;
                        break;
                    case 1:
                        minusEnabled = tempAddMo > 0;
                        break;
                    case 2:
                        minusEnabled = tempAddLi > 0;
                        break;
                    case 3:
                        minusEnabled = tempAddNai > 0;
                        break;
                    case 4:
                        minusEnabled = tempAddMin > 0;
                        break;
                }

                drawPlusButton(canvas, paint, plus.get(i), plusEnabled);
                drawMinusButton(canvas, paint, minus.get(i), minusEnabled);
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
    private void drawPlusButton(Canvas canvas, Paint paint, Rect button, boolean enabled) {
        // Button background
        if (enabled) {
            paint.setColor(Color.argb(180, 100, 200, 100));
        } else {
            paint.setColor(Color.argb(80, 100, 100, 100)); // Gray when disabled
        }
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(enabled ? Color.WHITE : Color.rgb(150, 150, 150));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Plus symbol
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(enabled ? Color.WHITE : Color.rgb(150, 150, 150));
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
    private void drawMinusButton(Canvas canvas, Paint paint, Rect button, boolean enabled) {
        // Button background
        if (enabled) {
            paint.setColor(Color.argb(180, 200, 100, 100));
        } else {
            paint.setColor(Color.argb(80, 100, 100, 100)); // Gray when disabled
        }
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(enabled ? Color.WHITE : Color.rgb(150, 150, 150));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 5, 5, paint);

        // Minus symbol
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(enabled ? Color.WHITE : Color.rgb(150, 150, 150));
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
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        if (!levelUpButton.isEmpty() && TouchUtil.checkIsInTouchRectFloat(levelUpButton, x, y)) {
            handleLevelUpClick();
            return true;
        }

        // Check if touched confirm button
        if (TouchUtil.checkIsInTouchRectFloat(confirmButton, x, y)) {
            // Save changes here
            confirmAttributes();
            return true;
        }

        // Check if touched +/- buttons
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        List<Rect> plus = plusButtons;
        List<Rect> minus = minusButtons;
        int attrCount = Math.min(5, Math.min(plus.size(), minus.size()));
        for (int i = 0; i < attrCount; i++) { // Only first 5 attributes have buttons
            if (TouchUtil.checkIsInTouchRectFloat(plus.get(i), x, y)) {
                increaseAttribute(i, roleInfo);
                return true;
            } else if (TouchUtil.checkIsInTouchRectFloat(minus.get(i), x, y)) {
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

            roleInfo.getSelfProperty().propTi += tempAddTi;
            roleInfo.getSelfProperty().propMo += tempAddMo;
            roleInfo.getSelfProperty().propLi += tempAddLi;
            roleInfo.getSelfProperty().propNai += tempAddNai;
            roleInfo.getSelfProperty().propMin += tempAddMin;

            tempAddTi = 0;
            tempAddMo = 0;
            tempAddLi = 0;
            tempAddNai = 0;
            tempAddMin = 0;

            addPointResult = null;

            roleInfo.setRemainPoints(tempRemainPoints);
        }
    }

    private void handleLevelUpClick() {
        RoleSystem roleSystem = RoleSystem.getInstance();
        int need = roleSystem.getExpForNextLevel();
        if (roleSystem.tryManualLevelUp()) {
            tempRemainPoints += 5;
            GameEngine.getInstance().showCenterToast(
                    "升级! Lv." + roleSystem.getRoleInfo().getLevel(), 1800);
        } else {
            GameEngine.getInstance().showCenterToast("经验不足，需要 " + need, 2000);
        }
    }

    private void drawLevelUpButton(Canvas canvas, Paint paint, RoleInfo roleInfo) {
        boolean canLevel = roleInfo.getExp() >= RoleSystem.getInstance().getExpForNextLevel();
        if (canLevel) {
            paint.setColor(Color.argb(220, 220, 160, 40));
        } else {
            paint.setColor(Color.argb(80, 100, 100, 100));
        }
        canvas.drawRoundRect(levelUpButton.left, levelUpButton.top,
                levelUpButton.right, levelUpButton.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(canLevel ? Color.WHITE : Color.rgb(150, 150, 150));
        canvas.drawRoundRect(levelUpButton.left, levelUpButton.top,
                levelUpButton.right, levelUpButton.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(canLevel ? Color.WHITE : Color.rgb(150, 150, 150));
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        float centerX = (levelUpButton.left + levelUpButton.right) / 2f;
        float centerY = (levelUpButton.top + levelUpButton.bottom) / 2f + 8;
        canvas.drawText("升级", centerX, centerY, paint);
    }

    /**
     * 上一行：经验数值（及升级按钮预留）；下一行：整宽进度条。
     */
    private void drawExpBar(Canvas canvas, Paint paint, String label, long current, long max, int y,
                            boolean reserveLevelUpButton) {
        int leftMargin = 20;
        int rightMargin = reserveLevelUpButton ? 120 : 20;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, panelBounds.left + leftMargin, y + 20, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(current + " / " + max, panelBounds.right - rightMargin, y + 20, paint);

        int barLeft = panelBounds.left + leftMargin;
        int barRight = panelBounds.right - 20;
        int barY = y + 50;
        int barHeight = 14;

        paint.setColor(Color.GRAY);
        canvas.drawRoundRect(barLeft, barY, barRight, barY + barHeight, 4, 4, paint);

        float percent = max > 0 ? (float) current / max : 0;
        percent = Math.max(0, Math.min(1, percent));
        int fillRight = barLeft + (int) ((barRight - barLeft) * percent);
        if (fillRight > barLeft) {
            paint.setColor(Color.rgb(255, 200, 50));
            canvas.drawRoundRect(barLeft, barY, fillRight, barY + barHeight, 4, 4, paint);
        }
    }


    private void drawHpStatRow(Canvas canvas, Paint paint, String value,
                             int y, float percent) {
        int leftMargin = 20;
        int rightMargin = 20;
        int barWidth = 150;
        int barHeight = 16;

        // Label
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("气血", panelBounds.left + leftMargin, y + 20, paint);

        // Value
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value, panelBounds.right - rightMargin, y + 20, paint);

        // Bar background
        int barX = panelBounds.centerX() - barWidth / 2;
        int barY = y + 2;
        paint.setColor(Color.GRAY);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);

        // Bar fill
        percent = Math.max(0, Math.min(1, percent));
        int barColor = Color.rgb(100, 200, 100);
        paint.setColor(barColor);
        canvas.drawRect(barX, barY, barX + barWidth * percent, barY + barHeight, paint);
    }

    private void drawMpStatRow(Canvas canvas, Paint paint, String value,
                               int y, float percent) {
        int leftMargin = 20;
        int rightMargin = 20;
        int barWidth = 150;
        int barHeight = 16;

        // Label
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("魔法", panelBounds.left + leftMargin, y + 20, paint);

        // Value
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value, panelBounds.right - rightMargin, y + 20, paint);

        // Bar background
        int barX = panelBounds.centerX() - barWidth / 2;
        int barY = y + 2;
        paint.setColor(Color.GRAY);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);

        // Bar fill
        percent = Math.max(0, Math.min(1, percent));
        int barColor = Color.rgb(100, 181, 246);
        paint.setColor(barColor);
        canvas.drawRect(barX, barY, barX + barWidth * percent, barY + barHeight, paint);
    }
}
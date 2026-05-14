package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.bean.SkillInfo;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Skills panel with 3 tabs: Main, Assist, Practise
 */
public class SkillsPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    // Tabs
    private Rect activeTab;
    private Rect passiveTab;
    private Rect cultivationTab;
    private int currentTab; // 0=Active, 1=Passive, 2=Cultivation

    // Skill list display area
    private Rect skillListArea;

    // Scrolling support
    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;

    // Buttons for each skill (upgrade/downgrade)
    private List<SkillButtonPair> skillButtons;

    // Callback interface
    public interface SkillActionListener {
        void onUpgradeSkill(SkillInfo skill);

        void onDowngradeSkill(SkillInfo skill);

        void onEquipSkill(SkillInfo skill);

        void onUnequipSkill(SkillInfo skill);
    }

    private SkillActionListener listener;

    private static class SkillButtonPair {
        Rect upgradeButton;
        Rect downgradeButton;
        Rect equipButton;
        SkillInfo skill;

        SkillButtonPair(SkillInfo skill, Rect upgrade, Rect downgrade, Rect equip) {
            this.skill = skill;
            this.upgradeButton = upgrade;
            this.downgradeButton = downgrade;
            this.equipButton = equip;
        }
    }

    public SkillsPanel() {
        this.isVisible = false;
        this.currentTab = 0;
        this.skillButtons = new ArrayList<>();

        // Initialize all Rect objects
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.activeTab = new Rect();
        this.passiveTab = new Rect();
        this.cultivationTab = new Rect();
        this.skillListArea = new Rect();

        setSkillActionListener(new SkillsPanel.SkillActionListener() {
            @Override
            public void onUpgradeSkill(SkillInfo skill) {
                if (skill.getLevel() < skill.getMaxLevel()) {
                    skill.setLevel(skill.getLevel() + 1);
                }
//                long money = RoleSystem.getInstance().getRoleInfo().getMoney();
//                if (money >= skill.getCostPerLevel()) {
//                    skill.upgrade();
//                    RoleSystem.getInstance().getRoleInfo().setMoney(money - skill.getCostPerLevel());
//                    LogUtil.d("Upgraded skill: " + skill.getName() + " to level " + skill.getLevel());
//                } else {
//                    LogUtil.d("Not enough money to upgrade skill!");
//                }
            }

            @Override
            public void onDowngradeSkill(SkillInfo skill) {
                if (skill.getLevel() > 1) {
                    skill.setLevel(skill.getLevel() - 1);
                }
//                skill.downgrade();
//                long money = RoleSystem.getInstance().getRoleInfo().getMoney();
//                RoleSystem.getInstance().getRoleInfo().setMoney(money + skill.getCostPerLevel());
//                LogUtil.d("Downgraded skill: " + skill.getName() + " to level " + skill.getLevel());
            }

            @Override
            public void onEquipSkill(SkillInfo skill) {
                SkillSystem.getInstance().equipSkill(skill);
            }

            @Override
            public void onUnequipSkill(SkillInfo skill) {
                SkillSystem.getInstance().unequipSkill(skill);
            }
        });
    }

    /**
     * Toggle panel visibility
     */
    public void toggleVisibility() {
        isVisible = !isVisible;
    }

    public void show() {
        isVisible = true;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set panel bounds
     */
    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        // Close button (top-right)
        int buttonSize = 40;
        int padding = 10;
        closeButton.set(
                panelBounds.right - buttonSize - padding,
                panelBounds.top + padding,
                panelBounds.right - padding,
                panelBounds.top + padding + buttonSize
        );

        // Tabs (top)
        int tabHeight = 50;
        int tabWidth = width / 3;
        int tabY = panelBounds.top + 60;

        activeTab = new Rect(panelBounds.left, tabY, panelBounds.left + tabWidth, tabY + tabHeight);
        passiveTab = new Rect(panelBounds.left + tabWidth, tabY, panelBounds.left + tabWidth * 2, tabY + tabHeight);
        cultivationTab = new Rect(panelBounds.left + tabWidth * 2, tabY, panelBounds.right, tabY + tabHeight);

        // Skill list area (below tabs)
        skillListArea = new Rect(
                panelBounds.left + 20,
                tabY + tabHeight + 20,
                panelBounds.right - 20,
                panelBounds.bottom - 30
        );

        updateSkillButtons();
    }

    /**
     * Set SkillActionListener
     */
    private void setSkillActionListener(SkillActionListener listener) {
        this.listener = listener;
    }

    /**
     * Update button positions based on current tab
     */
    private void updateSkillButtons() {
        skillButtons.clear();

        // Filter skills by current tab
        List<SkillInfo> filteredSkills = new ArrayList<>();
        if (currentTab == 0) {
            filteredSkills.addAll(SkillSystem.getInstance().getMainSkillInfos());
        } else if (currentTab == 1) {
            filteredSkills.addAll(SkillSystem.getInstance().getAssistSkillInfos());
        } else if (currentTab == 2) {
            filteredSkills.addAll(SkillSystem.getInstance().getPractiseSkillInfos());
        }

        // Grid layout: 3 columns
        int cols = 3;
        int gap = 15;
        int itemWidth = (skillListArea.width() - gap * (cols - 1)) / cols;
        int itemHeight = 200;

        // Calculate total content height
        int rows = (int) Math.ceil((double) filteredSkills.size() / cols);
        float totalContentHeight = rows * (itemHeight + gap);

        // Calculate max scroll offset
        maxScrollOffset = Math.max(0, totalContentHeight - skillListArea.height());

        // Clamp scroll offset
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        for (int i = 0; i < filteredSkills.size(); i++) {
            SkillInfo skill = filteredSkills.get(i);

            int col = i % cols;
            int row = i / cols;

            int xPos = skillListArea.left + col * (itemWidth + gap);
            int yPos = skillListArea.top + row * (itemHeight + gap) - (int) scrollOffset;

            // Upgrade button (bottom-right of item)
            Rect upgradeBtn = new Rect(
                    xPos + itemWidth - 55,
                    yPos + itemHeight - 45,
                    xPos + itemWidth - 10,
                    yPos + itemHeight - 10
            );

            // Downgrade button (bottom-left of item)
            Rect downgradeBtn = new Rect(
                    xPos + 10,
                    yPos + itemHeight - 45,
                    xPos + 65,
                    yPos + itemHeight - 10
            );

            // Equip button (bottom-middle of item)
            Rect equipBtn = new Rect(
                    xPos + itemWidth / 2 - 45,
                    yPos + itemHeight - 45,
                    xPos + itemWidth / 2 + 45,
                    yPos + itemHeight - 10
            );

            skillButtons.add(new SkillButtonPair(skill, upgradeBtn, downgradeBtn, equipBtn));
        }
    }

    /**
     * Draw the skills panel
     */
    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Panel background
        paint.setColor(Color.argb(240, 20, 25, 35));
        canvas.drawRect(panelBounds, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(100, 180, 255));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("技能修炼", panelBounds.centerX(), panelBounds.top + 45, paint);

        // Close button
        drawCloseButton(canvas, paint);

        // Draw tabs
        drawTabs(canvas, paint);

        // Draw skill list
        drawSkillList(canvas, paint);
    }

    /**
     * Draw tabs
     */
    private void drawTabs(Canvas canvas, Paint paint) {
        int tabHeight = 50;

        // Active tab
        drawTab(canvas, paint, activeTab, "主技能", currentTab == 0);

        // Passive tab
        drawTab(canvas, paint, passiveTab, "辅助技能", currentTab == 1);

        // Cultivation tab
        drawTab(canvas, paint, cultivationTab, "修炼", currentTab == 2);
    }

    /**
     * Draw a single tab
     */
    private void drawTab(Canvas canvas, Paint paint, Rect tab, String label, boolean isSelected) {
        // Tab background
        if (isSelected) {
            paint.setColor(Color.argb(200, 50, 100, 200));
        } else {
            paint.setColor(Color.argb(100, 40, 40, 50));
        }
        canvas.drawRect(tab, paint);

        // Tab border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(isSelected ? Color.rgb(100, 180, 255) : Color.rgb(80, 80, 100));
        canvas.drawRect(tab, paint);
        paint.setStyle(Paint.Style.FILL);

        // Tab text
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = tab.centerY() + 8;
        canvas.drawText(label, tab.centerX(), textY, paint);
    }

    /**
     * Draw skill list
     */
    private void drawSkillList(Canvas canvas, Paint paint) {
        int cols = 3;
        int gap = 15;
        int itemWidth = (skillListArea.width() - gap * (cols - 1)) / cols;
        int itemHeight = 200;

        // Filter skills by current tab
        List<SkillInfo> filteredSkills = new ArrayList<>();
        if (currentTab == 0) {
            filteredSkills.addAll(SkillSystem.getInstance().getMainSkillInfos());
        } else if (currentTab == 1) {
            filteredSkills.addAll(SkillSystem.getInstance().getAssistSkillInfos());
        } else if (currentTab == 2) {
            filteredSkills.addAll(SkillSystem.getInstance().getPractiseSkillInfos());
        }

        if (filteredSkills.isEmpty()) {
            paint.setColor(Color.rgb(150, 150, 150));
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("暂无技能", skillListArea.centerX(), skillListArea.centerY(), paint);
            return;
        }

        // Clip canvas to skill list area
        canvas.save();
        canvas.clipRect(skillListArea);

        for (int i = 0; i < filteredSkills.size(); i++) {
            SkillInfo skill = filteredSkills.get(i);

            int col = i % cols;
            int row = i / cols;

            int xPos = skillListArea.left + col * (itemWidth + gap);
            int yPos = skillListArea.top + row * (itemHeight + gap) - (int) scrollOffset;

            // Skip if completely outside visible area
            if (yPos + itemHeight < skillListArea.top || yPos > skillListArea.bottom) continue;

            // Skill item background
            paint.setColor(Color.argb(80, 40, 40, 50));
            canvas.drawRoundRect(xPos, yPos, xPos + itemWidth, yPos + itemHeight, 8, 8, paint);

            // Border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            switch (currentTab) {
                case 0:
                    paint.setColor(Color.rgb(255, 200, 100));
                    break;
                case 1:
                    paint.setColor(Color.rgb(100, 255, 150));
                    break;
                case 2:
                    paint.setColor(Color.rgb(200, 150, 255));
                    break;
            }

            canvas.drawRoundRect(xPos, yPos, xPos + itemWidth, yPos + itemHeight, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            // Skill name
            paint.setColor(Color.WHITE);
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(skill.getName(), xPos + itemWidth / 2, yPos + 30, paint);

            // Skill level
            paint.setColor(Color.WHITE);
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Lv." + skill.getLevel() + "/" + skill.getMaxLevel(),
                    xPos + 10, yPos + 55, paint);

            // Description (truncated)
            paint.setColor(Color.WHITE);
            paint.setTextSize(18);
            canvas.drawText(skill.getDesc(), xPos + 10, yPos + 85, paint);

            // Draw buttons if available
            if (i < skillButtons.size()) {
                SkillButtonPair pair = skillButtons.get(i);
                drawUpgradeButton(canvas, paint, pair.upgradeButton, skill.canUpgrade());
                drawDowngradeButton(canvas, paint, pair.downgradeButton, skill.canDowngrade());

                if (skill.isMainSkill()) {
                    drawEquipButton(canvas, paint, skillButtons.get(i).equipButton, skill);
                }
            }
        }

        canvas.restore();

        // Draw scrollbar if content is scrollable
        if (maxScrollOffset > 0) {
            drawScrollbar(canvas, paint);
        }
    }

    /**
     * Draw scrollbar indicator
     */
    private void drawScrollbar(Canvas canvas, Paint paint) {
        int scrollbarWidth = 6;
        int scrollbarX = skillListArea.right - 10;

        // Calculate scrollbar position and height
        float totalHeight = skillListArea.height() + maxScrollOffset;
        float scrollbarHeight = Math.max(40, (skillListArea.height() / totalHeight) * skillListArea.height());
        float scrollbarY = skillListArea.top + (scrollOffset / maxScrollOffset) * (skillListArea.height() - scrollbarHeight);

        // Scrollbar track
        paint.setColor(Color.argb(50, 100, 100, 100));
        canvas.drawRoundRect(scrollbarX, skillListArea.top, scrollbarX + scrollbarWidth, skillListArea.bottom, 3, 3, paint);

        // Scrollbar thumb
        paint.setColor(Color.argb(150, 150, 150, 150));
        canvas.drawRoundRect(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 3, 3, paint);
    }

    /**
     * Draw upgrade button
     */
    private void drawUpgradeButton(Canvas canvas, Paint paint, Rect button, boolean enabled) {
        paint.setColor(enabled ? Color.argb(200, 50, 200, 100) : Color.argb(100, 100, 100, 100));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(enabled ? Color.WHITE : Color.rgb(150, 150, 150));
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("+", button.centerX(), button.centerY() + 10, paint);
    }

    /**
     * Draw downgrade button
     */
    private void drawDowngradeButton(Canvas canvas, Paint paint, Rect button, boolean enabled) {
        paint.setColor(enabled ? Color.argb(200, 200, 100, 50) : Color.argb(100, 100, 100, 100));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(enabled ? Color.WHITE : Color.rgb(150, 150, 150));
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("-", button.centerX(), button.centerY() + 10, paint);
    }

    private void drawEquipButton(Canvas canvas, Paint paint, Rect button, SkillInfo skillInfo) {
        boolean isEquipped = SkillSystem.getInstance().getEquippedActiveSkills().contains(skillInfo);
        int index = SkillSystem.getInstance().getEquippedActiveSkills().indexOf(skillInfo) + 1;

        paint.setColor(isEquipped ? Color.argb(200, 50, 200, 100) : Color.argb(200, 255, 80, 80));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(isEquipped ? "装配中 " + index : "未装配", button.centerX(), button.centerY() + 6, paint);
    }

    /**
     * Draw close button
     */
    private void drawCloseButton(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(180, 255, 80, 80));
        canvas.drawRoundRect(closeButton.left, closeButton.top,
                closeButton.right, closeButton.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(closeButton.left, closeButton.top,
                closeButton.right, closeButton.bottom, 8, 8, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);

        float padding = 10;
        canvas.drawLine(closeButton.left + padding, closeButton.top + padding,
                closeButton.right - padding, closeButton.bottom - padding, paint);
        canvas.drawLine(closeButton.right - padding, closeButton.top + padding,
                closeButton.left + padding, closeButton.bottom - padding, paint);
    }

    /**
     * Handle touch event
     */
    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        // Check close button FIRST
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        // Check tabs
        if (TouchUtil.checkIsInTouchRectFloat(activeTab, x, y)) {
            if (currentTab != 0) {
                currentTab = 0;
                scrollOffset = 0; // Reset scroll when switching tabs
                updateSkillButtons();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(passiveTab, x, y)) {
            if (currentTab != 1) {
                currentTab = 1;
                scrollOffset = 0;
                updateSkillButtons();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(cultivationTab, x, y)) {
            if (currentTab != 2) {
                currentTab = 2;
                scrollOffset = 0;
                updateSkillButtons();
            }
            return true;
        }

        // Handle scrolling in skill list area
        if (TouchUtil.checkIsInTouchRectFloat(skillListArea, x, y)) {
            // Check skill buttons first (only if not dragging)
            if (!isDragging) {
                for (SkillButtonPair pair : skillButtons) {
                    // Handle Equip/Unequip for Main Skills
                    if (pair.skill.isMainSkill() && TouchUtil.checkIsInTouchRectFloat(pair.equipButton, x, y)) {
                        boolean isEquipped = SkillSystem.getInstance().getEquippedActiveSkills().contains(pair.skill);
                        if (isEquipped) {
                            if (listener != null) listener.onUnequipSkill(pair.skill);
                        } else {
                            if (listener != null) listener.onEquipSkill(pair.skill);
                        }
                        return true;
                    }

                    if (TouchUtil.checkIsInTouchRectFloat(pair.upgradeButton, x, y)) {
                        if (pair.skill.canUpgrade() && listener != null) {
                            listener.onUpgradeSkill(pair.skill);
                            updateSkillButtons();
                        }
                        return true;
                    }
                    if (TouchUtil.checkIsInTouchRectFloat(pair.downgradeButton, x, y)) {
                        if (pair.skill.canDowngrade() && listener != null) {
                            listener.onDowngradeSkill(pair.skill);
                            updateSkillButtons();
                        }
                        return true;
                    }
                }
            }
            return true; // Consume touch in skill list area
        }

        return false;
    }

    /**
     * Handle scroll/drag events
     */
    public boolean handleScroll(float deltaX, float deltaY) {
        if (!isVisible || maxScrollOffset <= 0) return false;

        scrollOffset -= deltaY;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        updateSkillButtons();
        return true;
    }
}

package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.game.dream.bean.QuestInfo;
import com.game.dream.system.QuestSystem;
import com.game.dream.utils.TouchUtil;

import java.util.List;

/**
 * 任务面板 - 显示已接取的任务列表
 * 两个标签: 进行中 / 已完成
 */
public class QuestPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    // Tabs
    private Rect activeTab;
    private Rect completedTab;
    private int currentTab; // 0=进行中, 1=已完成

    // Quest list display area
    private Rect questListArea;

    // Scrolling support
    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;

    // Quest item rects for touch detection
    private List<QuestItemRect> questItemRects;

    private static class QuestItemRect {
        Rect bounds;
        QuestInfo quest;

        QuestItemRect(QuestInfo quest, Rect bounds) {
            this.quest = quest;
            this.bounds = bounds;
        }
    }

    public QuestPanel() {
        this.isVisible = false;
        this.currentTab = 0;
        this.questItemRects = new java.util.ArrayList<>();

        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.activeTab = new Rect();
        this.completedTab = new Rect();
        this.questListArea = new Rect();
    }

    public void toggleVisibility() {
        isVisible = !isVisible;
        if (isVisible) {
            scrollOffset = 0;
            updateQuestItemRects();
        }
    }

    public void show() {
        isVisible = true;
        scrollOffset = 0;
        updateQuestItemRects();
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

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
        int tabWidth = width / 2;
        int tabY = panelBounds.top + 60;

        activeTab = new Rect(panelBounds.left, tabY, panelBounds.left + tabWidth, tabY + tabHeight);
        completedTab = new Rect(panelBounds.left + tabWidth, tabY, panelBounds.right, tabY + tabHeight);

        // Quest list area (below tabs)
        questListArea = new Rect(
                panelBounds.left + 20,
                tabY + tabHeight + 20,
                panelBounds.right - 20,
                panelBounds.bottom - 30
        );

        updateQuestItemRects();
    }

    private void updateQuestItemRects() {
        questItemRects.clear();

        List<QuestInfo> quests;
        if (currentTab == 0) {
            quests = QuestSystem.getInstance().getActiveQuests();
        } else {
            quests = QuestSystem.getInstance().getCompletedQuests();
        }

        int itemHeight = 180;
        int gap = 15;

        float totalContentHeight = quests.size() * (itemHeight + gap);
        maxScrollOffset = Math.max(0, totalContentHeight - questListArea.height());
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        for (int i = 0; i < quests.size(); i++) {
            QuestInfo quest = quests.get(i);
            int yPos = questListArea.top + i * (itemHeight + gap) - (int) scrollOffset;

            Rect itemRect = new Rect(
                    questListArea.left,
                    yPos,
                    questListArea.right,
                    yPos + itemHeight
            );
            questItemRects.add(new QuestItemRect(quest, itemRect));
        }
    }

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
        canvas.drawText("任务列表", panelBounds.centerX(), panelBounds.top + 45, paint);

        // Close button
        drawCloseButton(canvas, paint);

        // Tabs
        int activeCount = QuestSystem.getInstance().getActiveQuests().size();
        int completedCount = QuestSystem.getInstance().getCompletedQuests().size();
        drawTab(canvas, paint, activeTab, "进行中(" + activeCount + ")", currentTab == 0);
        drawTab(canvas, paint, completedTab, "已完成(" + completedCount + ")", currentTab == 1);

        // Quest list
        drawQuestList(canvas, paint);
    }

    private void drawTab(Canvas canvas, Paint paint, Rect tab, String label, boolean isSelected) {
        paint.setColor(isSelected ? Color.argb(200, 50, 100, 200) : Color.argb(100, 40, 40, 50));
        canvas.drawRect(tab, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(isSelected ? Color.rgb(100, 180, 255) : Color.rgb(80, 80, 100));
        canvas.drawRect(tab, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, tab.centerX(), tab.centerY() + 8, paint);
    }

    private void drawQuestList(Canvas canvas, Paint paint) {
        List<QuestInfo> quests;
        if (currentTab == 0) {
            quests = QuestSystem.getInstance().getActiveQuests();
        } else {
            quests = QuestSystem.getInstance().getCompletedQuests();
        }

        if (quests.isEmpty()) {
            paint.setColor(Color.rgb(150, 150, 150));
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(currentTab == 0 ? "暂无进行中的任务" : "暂无已完成的任务",
                    questListArea.centerX(), questListArea.centerY(), paint);
            return;
        }

        // Clip to quest list area
        canvas.save();
        canvas.clipRect(questListArea);

        int itemHeight = 180;
        int gap = 15;

        for (int i = 0; i < quests.size(); i++) {
            QuestInfo quest = quests.get(i);
            int yPos = questListArea.top + i * (itemHeight + gap) - (int) scrollOffset;

            if (yPos + itemHeight < questListArea.top || yPos > questListArea.bottom) continue;

            drawQuestItem(canvas, paint, quest, questListArea.left, yPos, questListArea.width(), itemHeight);
        }

        canvas.restore();

        // Scrollbar
        if (maxScrollOffset > 0) {
            drawScrollbar(canvas, paint);
        }
    }

    private void drawQuestItem(Canvas canvas, Paint paint, QuestInfo quest, int x, int y, int width, int height) {
        // Background
        paint.setColor(Color.argb(80, 40, 40, 50));
        canvas.drawRoundRect(x, y, x + width, y + height, 8, 8, paint);

        // Border color based on status
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        if (quest.isCompleted()) {
            paint.setColor(Color.rgb(100, 255, 100)); // Green for completed
        } else {
            paint.setColor(Color.rgb(255, 200, 100)); // Gold for active
        }
        canvas.drawRoundRect(x, y, x + width, y + height, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        // Quest name
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(quest.getName(), x + 15, y + 30, paint);

        // Status tag
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (quest.isCompleted()) {
            paint.setColor(Color.rgb(100, 255, 100));
            canvas.drawText("已完成", x + width - 15, y + 30, paint);
        } else {
            paint.setColor(Color.rgb(255, 200, 100));
            canvas.drawText("进行中", x + width - 15, y + 30, paint);
        }

        // Quest description (truncate if too long)
        paint.setColor(Color.rgb(200, 200, 200));
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.LEFT);
        String descText = quest.getDesc();
        if (descText != null && !descText.isEmpty()) {
            descText = truncateText(paint, descText, width - 30);
            canvas.drawText(descText, x + 15, y + 65, paint);
        }

        // Current stage description
        paint.setColor(Color.rgb(200, 220, 255));
        paint.setTextSize(20);
        String stageDesc = quest.getCurrentStageDesc();
        if (!stageDesc.isEmpty()) {
            canvas.drawText("当前: " + stageDesc, x + 15, y + 90, paint);
        }

        // Progress bar
        float barX = x + 15;
        float barY = y + 110;
        float barWidth = width - 30;
        float barHeight = 20;
        float progress = quest.getProgress();

        // Bar background
        paint.setColor(Color.argb(100, 50, 50, 50));
        canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 5, 5, paint);

        // Bar fill
        float fillWidth = barWidth * progress;
        if (fillWidth > 0) {
            if (quest.isCompleted()) {
                paint.setColor(Color.rgb(100, 255, 100));
            } else {
                paint.setColor(Color.rgb(100, 180, 255));
            }
            canvas.drawRoundRect(barX, barY, barX + fillWidth, barY + barHeight, 5, 5, paint);
        }

        // Bar border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(150, 150, 150, 150));
        canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 5, 5, paint);
        paint.setStyle(Paint.Style.FILL);

        // Progress text
        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(quest.getStage() + " / " + quest.getTotalStages(),
                barX + barWidth / 2, barY + 16, paint);

        // Stage list (all stages with completion marks)
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.LEFT);
        List<String> stageDescs = quest.getStageDescs();
        if (stageDescs != null) {
            float stageY = y + 145;
            for (int s = 0; s < stageDescs.size() && s < 2; s++) { // Show max 2 stages
                boolean done = s < quest.getStage();
                boolean current = s == quest.getStage();
                if (done) {
                    paint.setColor(Color.rgb(100, 255, 100));
                    canvas.drawText("✓ " + stageDescs.get(s), x + 15, stageY, paint);
                } else if (current) {
                    paint.setColor(Color.rgb(255, 220, 100));
                    canvas.drawText("▶ " + stageDescs.get(s), x + 15, stageY, paint);
                } else {
                    paint.setColor(Color.rgb(120, 120, 120));
                    canvas.drawText("○ " + stageDescs.get(s), x + 15, stageY, paint);
                }
                stageY += 20;
            }
            if (stageDescs.size() > 2) {
                paint.setColor(Color.rgb(120, 120, 120));
                canvas.drawText("... 还有 " + (stageDescs.size() - 2) + " 个阶段", x + 15, stageY, paint);
            }
        }
    }

    /**
     * 截断文本，如果超过最大宽度则添加...
     */
    private String truncateText(Paint paint, String text, float maxWidth) {
        if (text == null || text.isEmpty()) return "";
        float textWidth = paint.measureText(text);
        if (textWidth <= maxWidth) return text;
        String ellipsis = "...";
        float ellipsisWidth = paint.measureText(ellipsis);
        float availableWidth = maxWidth - ellipsisWidth;
        if (availableWidth <= 0) return ellipsis;
        int end = paint.breakText(text, true, availableWidth, null);
        return text.substring(0, end) + ellipsis;
    }

    private void drawScrollbar(Canvas canvas, Paint paint) {
        int scrollbarWidth = 6;
        int scrollbarX = questListArea.right - 10;

        float totalHeight = questListArea.height() + maxScrollOffset;
        float scrollbarHeight = Math.max(40, (questListArea.height() / totalHeight) * questListArea.height());
        float scrollbarY = questListArea.top + (scrollOffset / maxScrollOffset) * (questListArea.height() - scrollbarHeight);

        paint.setColor(Color.argb(50, 100, 100, 100));
        canvas.drawRoundRect(scrollbarX, questListArea.top, scrollbarX + scrollbarWidth, questListArea.bottom, 3, 3, paint);

        paint.setColor(Color.argb(150, 150, 150, 150));
        canvas.drawRoundRect(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 3, 3, paint);
    }

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

    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        // Close button
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        // Tabs
        if (TouchUtil.checkIsInTouchRectFloat(activeTab, x, y)) {
            if (currentTab != 0) {
                currentTab = 0;
                scrollOffset = 0;
                updateQuestItemRects();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(completedTab, x, y)) {
            if (currentTab != 1) {
                currentTab = 1;
                scrollOffset = 0;
                updateQuestItemRects();
            }
            return true;
        }

        // Quest list area (consume touch for scrolling)
        if (TouchUtil.checkIsInTouchRectFloat(questListArea, x, y)) {
            return true;
        }

        return false;
    }

    public boolean handleScroll(float deltaX, float deltaY) {
        if (!isVisible || maxScrollOffset <= 0) return false;

        scrollOffset -= deltaY;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        updateQuestItemRects();
        return true;
    }
}

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

public class PracticePanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;
    private Rect listClip;

    private static final int ITEM_HEIGHT = 92;
    private static final int ITEM_GAP = 8;

    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;
    private float touchDownY = 0;

    private static class Row {
        SkillInfo skill;
        Rect upgradeBtn;
    }

    private List<Row> rows = new ArrayList<>();

    public PracticePanel() {
        panelBounds = new Rect();
        closeButton = new Rect();
        listClip = new Rect();
    }

    public void show() {
        isVisible = true;
        scrollOffset = 0;
        isDragging = false;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);
        int buttonSize = 40;
        int padding = 10;
        closeButton.set(
                panelBounds.right - buttonSize - padding,
                panelBounds.top + padding,
                panelBounds.right - padding,
                panelBounds.top + padding + buttonSize
        );
        listClip.set(panelBounds.left + 10, panelBounds.top + 120, panelBounds.right - 10, panelBounds.bottom - 16);
    }

    public void draw(Canvas canvas) {
        if (!isVisible) {
            return;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.argb(240, 20, 25, 35));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(180, 150, 255));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        drawCloseButton(canvas, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(34);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("玄机真人 · 修炼", panelBounds.centerX(), panelBounds.top + 46, paint);

        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.LEFT);
        long money = RoleSystem.getInstance().getRoleInfo().getMoney();
        int cap = SkillSystem.getInstance().getPracticeLevelCap();
        canvas.drawText("金钱: " + money + "    当前可修炼上限: " + cap + " 级",
                panelBounds.left + 28, panelBounds.top + 88, paint);

        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 104,
                panelBounds.right - 20, panelBounds.top + 104, paint);

        drawRows(canvas, paint);
    }

    private void drawRows(Canvas canvas, Paint paint) {
        List<SkillInfo> skills = SkillSystem.getInstance().getPractiseSkillInfos();
        int listStartY = listClip.top;
        int listEndY = listClip.bottom;
        int itemWidth = panelBounds.width() - 56;
        int itemX = panelBounds.left + 28;

        int totalHeight = skills.size() * (ITEM_HEIGHT + ITEM_GAP);
        int visibleHeight = listEndY - listStartY;
        maxScrollOffset = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        List<Row> nextRows = new ArrayList<>(skills.size());
        canvas.save();
        canvas.clipRect(listClip);

        int cap = SkillSystem.getInstance().getPracticeLevelCap();
        for (int i = 0; i < skills.size(); i++) {
            SkillInfo skill = skills.get(i);
            int itemY = (int) (listStartY + i * (ITEM_HEIGHT + ITEM_GAP) - scrollOffset);
            if (itemY + ITEM_HEIGHT < listStartY || itemY > listEndY) {
                Row skip = new Row();
                skip.skill = skill;
                skip.upgradeBtn = new Rect();
                nextRows.add(skip);
                continue;
            }

            Rect itemRect = new Rect(itemX, itemY, itemX + itemWidth, itemY + ITEM_HEIGHT);
            paint.setColor(Color.argb(80, 40, 45, 60));
            canvas.drawRoundRect(itemRect.left, itemRect.top, itemRect.right, itemRect.bottom, 8, 8, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(140, 110, 200));
            canvas.drawRoundRect(itemRect.left, itemRect.top, itemRect.right, itemRect.bottom, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.WHITE);
            paint.setTextSize(28);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setFakeBoldText(true);
            canvas.drawText(skill.getName(), itemRect.left + 14, itemRect.top + 32, paint);
            paint.setFakeBoldText(false);

            int nextLevel = skill.getLevel() + 1;
            int cost = SkillSystem.getPracticeUpgradeCost(nextLevel);
            String status;
            if (skill.getLevel() >= skill.getMaxLevel()) {
                status = "Lv." + skill.getLevel() + "  已满级";
            } else {
                status = "Lv." + skill.getLevel() + "/" + skill.getMaxLevel()
                        + "  下级 " + cost + " 金币";
            }
            paint.setColor(Color.rgb(220, 200, 140));
            paint.setTextSize(22);
            canvas.drawText(status, itemRect.left + 14, itemRect.top + 62, paint);

            paint.setColor(Color.rgb(170, 170, 180));
            paint.setTextSize(18);
            canvas.drawText(skill.getDesc(), itemRect.left + 14, itemRect.top + 82, paint);

            Rect upgradeBtn = new Rect(itemRect.right - 110, itemRect.top + 22, itemRect.right - 14, itemRect.top + 70);
            boolean can = nextLevel <= skill.getMaxLevel() && nextLevel <= cap;
            paint.setColor(can ? Color.argb(180, 80, 60, 150) : Color.argb(120, 70, 70, 80));
            canvas.drawRoundRect(upgradeBtn.left, upgradeBtn.top, upgradeBtn.right, upgradeBtn.bottom, 8, 8, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            String btn = skill.getLevel() <= 0 ? "学习" : "升级";
            canvas.drawText(btn, upgradeBtn.centerX(), upgradeBtn.centerY() + 8, paint);

            Row row = new Row();
            row.skill = skill;
            row.upgradeBtn = upgradeBtn;
            nextRows.add(row);
        }
        rows = nextRows;
        canvas.restore();
    }

    private void drawCloseButton(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(180, 255, 80, 80));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        canvas.drawLine(closeButton.left + 10, closeButton.top + 10, closeButton.right - 10, closeButton.bottom - 10, paint);
        canvas.drawLine(closeButton.right - 10, closeButton.top + 10, closeButton.left + 10, closeButton.bottom - 10, paint);
    }

    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) {
            return false;
        }
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }
        for (Row row : rows) {
            if (row.upgradeBtn != null && row.upgradeBtn.contains((int) x, (int) y)) {
                SkillSystem.getInstance().tryUpgradePractice(row.skill);
                return true;
            }
        }
        isDragging = false;
        touchDownY = y;
        lastTouchY = y;
        return true;
    }

    public boolean handleTouchMove(float x, float y) {
        if (!isVisible) {
            return false;
        }
        if (Math.abs(y - touchDownY) > 10) {
            isDragging = true;
        }
        if (isDragging) {
            scrollOffset -= (y - lastTouchY);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            lastTouchY = y;
            return true;
        }
        return false;
    }

    public boolean handleTouchUp(float x, float y) {
        if (!isVisible) {
            return false;
        }
        isDragging = false;
        return false;
    }
}

package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.system.TitleSystem;
import com.game.dream.title.TitleCatalog;
import com.game.dream.title.TitleDef;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 称号列表：已获得 / 未获得，点击佩戴。
 */
public class TitlePanel {

    private boolean visible;
    private Rect panelBounds;
    private Rect closeButton;
    private Rect unequipButton;
    private Rect ownedTab;
    private Rect lockedTab;
    private Rect listArea;
    private int tab;
    private float scrollOffset;
    private float maxScroll;

    private final List<Rect> rowRects = new ArrayList<>();
    private final List<String> rowIds = new ArrayList<>();

    public TitlePanel() {
        panelBounds = new Rect();
        closeButton = new Rect();
        unequipButton = new Rect();
        ownedTab = new Rect();
        lockedTab = new Rect();
        listArea = new Rect();
    }

    public void show() {
        TitleSystem.getInstance().evaluateRules();
        visible = true;
        scrollOffset = 0;
        rebuildRows();
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setBounds(int x, int y, int w, int h) {
        panelBounds.set(x, y, x + w, y + h);
        int pad = 10;
        closeButton.set(panelBounds.right - 50, panelBounds.top + pad, panelBounds.right - pad, panelBounds.top + pad + 40);
        unequipButton.set(panelBounds.left + 16, panelBounds.top + 58, panelBounds.left + 140, panelBounds.top + 108);
        int tabY = panelBounds.top + 118;
        int half = panelBounds.width() / 2;
        ownedTab.set(panelBounds.left, tabY, panelBounds.left + half, tabY + 48);
        lockedTab.set(panelBounds.left + half, tabY, panelBounds.right, tabY + 48);
        listArea.set(panelBounds.left + 12, tabY + 56, panelBounds.right - 12, panelBounds.bottom - 16);
        rebuildRows();
    }

    private void rebuildRows() {
        rowRects.clear();
        rowIds.clear();
        TitleSystem ts = TitleSystem.getInstance();
        List<TitleDef> source = new ArrayList<>();
        for (TitleDef d : TitleCatalog.all()) {
            if (d.hidden && !ts.isUnlocked(d.id)) {
                continue;
            }
            boolean owned = ts.isUnlocked(d.id);
            if (tab == 0 && owned) {
                source.add(d);
            } else if (tab == 1 && !owned && !d.hidden) {
                source.add(d);
            }
        }
        int rowH = 64;
        int gap = 4;
        float totalH = source.size() * (rowH + gap) - gap;
        maxScroll = Math.max(0, totalH - listArea.height());
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
        int y = (int) (listArea.top - scrollOffset);
        for (TitleDef d : source) {
            Rect r = new Rect(listArea.left, y, listArea.right, y + rowH);
            rowRects.add(r);
            rowIds.add(d.id);
            y += rowH + gap;
        }
    }

    public void draw(Canvas canvas) {
        if (!visible) {
            return;
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.argb(245, 20, 26, 36));
        canvas.drawRoundRect(panelBounds.left, panelBounds.top, panelBounds.right, panelBounds.bottom, 14, 14, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(200, 170, 90));
        canvas.drawRoundRect(panelBounds.left, panelBounds.top, panelBounds.right, panelBounds.bottom, 14, 14, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(255, 220, 120));
        paint.setTextSize(34);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("江湖称号", panelBounds.centerX(), panelBounds.top + 44, paint);

        drawClose(canvas, paint);
        drawBtn(canvas, paint, unequipButton, "卸下", Color.argb(200, 80, 90, 110));

        paint.setTextSize(26);
        drawTab(canvas, paint, ownedTab, "已获得", tab == 0);
        drawTab(canvas, paint, lockedTab, "未获得", tab == 1);

        TitleSystem ts = TitleSystem.getInstance();
        String eq = ts.getEquippedTitleId();
        paint.setColor(Color.rgb(180, 200, 220));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(22);
        String eqText = eq == null ? "当前：未佩戴" : "当前：" + ts.getEquippedDisplayName();
        canvas.drawText(eqText, panelBounds.left + 150, panelBounds.top + 92, paint);

        canvas.save();
        canvas.clipRect(listArea);
        paint.setColor(Color.argb(90, 10, 12, 18));
        canvas.drawRect(listArea, paint);
        for (int i = 0; i < rowRects.size(); i++) {
            Rect r = rowRects.get(i);
            if (r.bottom < listArea.top || r.top > listArea.bottom) {
                continue;
            }
            String id = rowIds.get(i);
            TitleDef def = TitleCatalog.get(id);
            if (def == null) {
                continue;
            }
            boolean equipped = id.equals(eq);
            paint.setColor(equipped ? Color.argb(220, 60, 80, 120) : Color.argb(200, 40, 48, 60));
            canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, 8, 8, paint);
            paint.setColor(def.rarity.getColorArgb());
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(28);
            canvas.drawText(def.displayName, r.left + 14, r.top + 36, paint);
            paint.setColor(Color.rgb(160, 170, 185));
            paint.setTextSize(20);
            String sub = tab == 1 ? def.hint : (equipped ? "佩戴中" : "点击佩戴");
            float subY = tab == 1 ? r.top + 56 : r.top + 62;
            canvas.drawText(sub, r.left + 14, subY, paint);
        }
        canvas.restore();
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawTab(Canvas canvas, Paint paint, Rect r, String label, boolean active) {
        paint.setColor(active ? Color.argb(220, 70, 110, 150) : Color.argb(120, 50, 55, 65));
        canvas.drawRect(r, paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, r.centerX(), r.centerY() + 10, paint);
    }

    private void drawBtn(Canvas canvas, Paint paint, Rect r, String label, int bg) {
        paint.setColor(bg);
        canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, 8, 8, paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24);
        canvas.drawText(label, r.centerX(), r.centerY() + 8, paint);
    }

    private void drawClose(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(200, 180, 70, 70));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 6, 6, paint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        float c = closeButton.centerX();
        float m = closeButton.centerY();
        canvas.drawLine(c - 10, m - 10, c + 10, m + 10, paint);
        canvas.drawLine(c + 10, m - 10, c - 10, m + 10, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    public boolean handleTouch(float x, float y) {
        if (!visible) {
            return false;
        }
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(unequipButton, x, y)) {
            TitleSystem.getInstance().unequip();
            rebuildRows();
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(ownedTab, x, y)) {
            tab = 0;
            scrollOffset = 0;
            rebuildRows();
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(lockedTab, x, y)) {
            tab = 1;
            scrollOffset = 0;
            rebuildRows();
            return true;
        }
        if (tab == 0 && listArea.contains((int) x, (int) y)) {
            for (int i = 0; i < rowRects.size(); i++) {
                Rect r = rowRects.get(i);
                if (r.bottom < listArea.top || r.top > listArea.bottom) {
                    continue;
                }
                if (TouchUtil.checkIsInTouchRectFloat(r, x, y)) {
                    TitleSystem.getInstance().equip(rowIds.get(i));
                    rebuildRows();
                    return true;
                }
            }
        }
        return TouchUtil.checkIsInTouchRectFloat(panelBounds, x, y);
    }

    public boolean handleScroll(float deltaY) {
        if (!visible || maxScroll <= 0) {
            return false;
        }
        scrollOffset -= deltaY;
        if (scrollOffset < 0) {
            scrollOffset = 0;
        } else if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
        rebuildRows();
        return true;
    }
}
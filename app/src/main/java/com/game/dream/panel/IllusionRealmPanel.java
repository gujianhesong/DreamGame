package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.system.IllusionRealmSystem;
import com.game.dream.utils.TouchUtil;

/**
 * 幻境选关：凡尘 / 妖狐 / 龙宫，各 1～20 关
 */
public class IllusionRealmPanel {

    private boolean isVisible;
    private int selectedRealm = IllusionRealmSystem.REALM_FANCHEN;
    private Rect panelBounds = new Rect();
    private Rect closeButton = new Rect();
    private final Rect[] realmTabs = new Rect[IllusionRealmSystem.REALM_COUNT];
    private final Rect[] stageButtons = new Rect[IllusionRealmSystem.STAGES_PER_REALM];

    public IllusionRealmPanel() {
        for (int i = 0; i < realmTabs.length; i++) {
            realmTabs[i] = new Rect();
        }
        for (int i = 0; i < stageButtons.length; i++) {
            stageButtons[i] = new Rect();
        }
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

    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);
        int pad = 12;
        closeButton.set(panelBounds.right - 52 - pad, panelBounds.top + pad,
                panelBounds.right - pad, panelBounds.top + pad + 52);

        int tabTop = panelBounds.top + 88;
        int tabLeft = panelBounds.left + 16;
        int tabRight = panelBounds.right - 16;
        int tabW = (tabRight - tabLeft) / IllusionRealmSystem.REALM_COUNT;
        int tabH = 44;
        for (int i = 0; i < IllusionRealmSystem.REALM_COUNT; i++) {
            int left = tabLeft + i * tabW + 4;
            realmTabs[i].set(left, tabTop, left + tabW - 8, tabTop + tabH);
        }

        int cols = 5;
        int rows = 4;
        int top = tabTop + tabH + 28;
        int left = panelBounds.left + 24;
        int right = panelBounds.right - 24;
        int bottom = panelBounds.bottom - 40;
        int cellW = (right - left) / cols;
        int cellH = (bottom - top) / rows;
        int gap = 8;

        for (int i = 0; i < IllusionRealmSystem.STAGES_PER_REALM; i++) {
            int row = i / cols;
            int col = i % cols;
            int bx = left + col * cellW + gap;
            int by = top + row * cellH + gap;
            stageButtons[i].set(bx, by, bx + cellW - gap * 2, by + cellH - gap * 2);
        }
    }

    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.argb(245, 25, 15, 45));
        canvas.drawRect(panelBounds, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(180, 120, 255));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("幻境挑战", panelBounds.centerX(), panelBounds.top + 44, paint);

        IllusionRealmSystem sys = IllusionRealmSystem.getInstance();
        drawRealmTabs(canvas, paint, sys);

        String title = IllusionRealmSystem.getRealmDisplayName(selectedRealm);
        paint.setTextSize(22);
        paint.setColor(Color.rgb(220, 200, 255));
        canvas.drawText(title, panelBounds.centerX(), realmTabs[0].bottom + 22, paint);

        if (!sys.isRealmAccessible(selectedRealm)) {
            paint.setTextSize(18);
            paint.setColor(Color.rgb(255, 180, 120));
            canvas.drawText(sys.getRealmLockHint(selectedRealm), panelBounds.centerX(),
                    realmTabs[0].bottom + 48, paint);
        } else {
            paint.setTextSize(18);
            paint.setColor(Color.rgb(200, 200, 220));
            canvas.drawText("进度 " + sys.getClearedStage(selectedRealm) + " / "
                            + IllusionRealmSystem.STAGES_PER_REALM,
                    panelBounds.centerX(), realmTabs[0].bottom + 48, paint);
        }

        drawClose(canvas, paint);

        int unlocked = sys.getUnlockedStage(selectedRealm);
        int cleared = sys.getClearedStage(selectedRealm);
        boolean realmOpen = sys.isRealmAccessible(selectedRealm);

        for (int i = 0; i < IllusionRealmSystem.STAGES_PER_REALM; i++) {
            int stage = i + 1;
            Rect btn = stageButtons[i];
            boolean stageCleared = stage <= cleared;
            boolean canPlay = realmOpen && stage <= unlocked;

            if (!canPlay) {
                paint.setColor(Color.argb(120, 60, 60, 70));
            } else if (stageCleared) {
                paint.setColor(Color.argb(200, 40, 90, 60));
            } else if (stage == unlocked) {
                paint.setColor(Color.argb(220, 80, 50, 140));
            } else {
                paint.setColor(Color.argb(180, 50, 70, 110));
            }
            canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 10, 10, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(22);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(stage), btn.centerX(), btn.centerY() + 8, paint);

            if (stage == 5 || stage == 10 || stage == 15 || stage == 20) {
                paint.setTextSize(14);
                paint.setColor(Color.rgb(255, 220, 120));
                canvas.drawText("★", btn.right - 18, btn.top + 20, paint);
            }
        }
    }

    private void drawRealmTabs(Canvas canvas, Paint paint, IllusionRealmSystem sys) {
        String[] labels = {"凡尘", "妖狐", "龙宫"};
        for (int i = 0; i < IllusionRealmSystem.REALM_COUNT; i++) {
            Rect tab = realmTabs[i];
            boolean selected = i == selectedRealm;
            boolean accessible = sys.isRealmAccessible(i);

            if (selected) {
                paint.setColor(Color.argb(230, 100, 60, 160));
            } else if (accessible) {
                paint.setColor(Color.argb(160, 55, 45, 90));
            } else {
                paint.setColor(Color.argb(100, 45, 45, 55));
            }
            canvas.drawRoundRect(tab.left, tab.top, tab.right, tab.bottom, 8, 8, paint);

            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(accessible ? Color.WHITE : Color.rgb(140, 140, 150));
            String text = labels[i];
            if (!accessible) {
                text = text + "·锁";
            }
            canvas.drawText(text, tab.centerX(), tab.centerY() + 7, paint);
        }
    }

    private void drawClose(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(200, 200, 60, 60));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(closeButton.left + 12, closeButton.top + 12, closeButton.right - 12, closeButton.bottom - 12, paint);
        canvas.drawLine(closeButton.right - 12, closeButton.top + 12, closeButton.left + 12, closeButton.bottom - 12, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) return false;
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }
        for (int i = 0; i < IllusionRealmSystem.REALM_COUNT; i++) {
            if (TouchUtil.checkIsInTouchRectFloat(realmTabs[i], x, y)) {
                selectedRealm = i;
                return true;
            }
        }
        return panelBounds.contains((int) x, (int) y);
    }

    public boolean handleTouchUp(float x, float y) {
        if (!isVisible) return false;
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            return true;
        }
        IllusionRealmSystem sys = IllusionRealmSystem.getInstance();
        for (int i = 0; i < IllusionRealmSystem.STAGES_PER_REALM; i++) {
            if (TouchUtil.checkIsInTouchRectFloat(stageButtons[i], x, y)) {
                int stage = i + 1;
                if (!sys.isRealmAccessible(selectedRealm)) {
                    GameEngine.getInstance().showCenterToast(sys.getRealmLockHint(selectedRealm));
                    return true;
                }
                if (!sys.isStageUnlocked(selectedRealm, stage)) {
                    return true;
                }
                hide();
                sys.requestEnterStage(selectedRealm, stage);
                return true;
            }
        }
        return false;
    }
}

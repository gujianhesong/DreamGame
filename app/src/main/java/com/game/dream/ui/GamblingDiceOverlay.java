package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.utils.TouchUtil;

/**
 * 赌坊掷骰：摇盅 → 开盅点数
 */
public class GamblingDiceOverlay {

    public interface Listener {
        void onRollFinished();
    }

    private static final long ROLL_MS = 1900;
    private static final long FLICKER_MS = 72;

    private boolean visible;
    private long startTime;
    private long lastFlickerMs;
    private boolean revealed;

    private String dealerName;
    private int finalDie1;
    private int finalDie2;
    private Listener listener;

    private int rollingFace1 = 1;
    private int rollingFace2 = 1;

    private final Rect panel = new Rect();
    private final Rect okButton = new Rect();

    public void show(String dealerName, int die1, int die2, Listener listener) {
        this.dealerName = dealerName != null ? dealerName : "庄家";
        this.finalDie1 = clampDie(die1);
        this.finalDie2 = clampDie(die2);
        this.listener = listener;
        this.startTime = System.currentTimeMillis();
        this.lastFlickerMs = startTime;
        this.revealed = false;
        this.visible = true;
        rollingFace1 = 1 + (int) (Math.random() * 6);
        rollingFace2 = 1 + (int) (Math.random() * 6);
        layoutPanel();
    }

    public void hide() {
        visible = false;
        listener = null;
    }

    public boolean isVisible() {
        return visible;
    }

    private static int clampDie(int v) {
        if (v < 1) {
            return 1;
        }
        if (v > 6) {
            return 6;
        }
        return v;
    }

    private void layoutPanel() {
        int sw = GameEngine.getScreenWidth();
        int sh = GameEngine.getScreenHeight();
        int pw = Math.min(560, sw - 48);
        int ph = Math.min(420, sh - 120);
        int px = (sw - pw) / 2;
        int py = (sh - ph) / 2;
        panel.set(px, py, px + pw, py + ph);
        okButton.set(panel.left + pw / 4, panel.bottom - 76, panel.right - pw / 4, panel.bottom - 20);
    }

    public void update() {
        if (!visible) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!revealed) {
            long elapsed = now - startTime;
            if (elapsed >= ROLL_MS) {
                revealed = true;
            } else if (now - lastFlickerMs >= FLICKER_MS) {
                rollingFace1 = 1 + (int) (Math.random() * 6);
                rollingFace2 = 1 + (int) (Math.random() * 6);
                lastFlickerMs = now;
            }
        }
    }

    public void draw(Canvas canvas) {
        if (!visible) {
            return;
        }
        int sw = GameEngine.getScreenWidth();
        int sh = GameEngine.getScreenHeight();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRect(0, 0, sw, sh, paint);

        paint.setColor(Color.argb(248, 22, 28, 38));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 16, 16, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(180, 140, 70));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 16, 16, paint);
        paint.setStyle(Paint.Style.FILL);

        long elapsed = System.currentTimeMillis() - startTime;
        float t = Math.min(1f, elapsed / (float) ROLL_MS);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(220, 200, 140));
        paint.setTextSize(34);
        canvas.drawText(dealerName + " 摇盅", panel.centerX(), panel.top + 48, paint);

        paint.setColor(Color.rgb(200, 210, 220));
        paint.setTextSize(26);
        if (!revealed) {
            String shake = t < 0.35f ? "骰子在盅里乱跳…" : (t < 0.75f ? "咔嚓咔嚓…" : "要开了…");
            canvas.drawText(shake, panel.centerX(), panel.top + 92, paint);
        } else {
            int sum = finalDie1 + finalDie2;
            canvas.drawText("开盅！", panel.centerX(), panel.top + 92, paint);
            paint.setColor(Color.rgb(255, 220, 120));
            paint.setTextSize(40);
            canvas.drawText(finalDie1 + "  +  " + finalDie2 + "  =  " + sum + " 点",
                    panel.centerX(), panel.top + 148, paint);
        }

        float dieSize = Math.min(100, panel.width() * 0.2f);
        float cx1 = panel.centerX() - dieSize * 0.85f;
        float cx2 = panel.centerX() + dieSize * 0.85f;
        float cy = panel.centerY() + 28;

        float shakeAmp = revealed ? 0 : (1f - t * 0.4f) * 14f;
        float oy1 = (float) Math.sin(elapsed * 0.022) * shakeAmp;
        float oy2 = (float) Math.cos(elapsed * 0.019) * shakeAmp;
        float rot1 = revealed ? 0 : (float) Math.sin(elapsed * 0.031) * 18f;
        float rot2 = revealed ? 0 : (float) Math.cos(elapsed * 0.027) * -16f;

        int face1 = revealed ? finalDie1 : rollingFace1;
        int face2 = revealed ? finalDie2 : rollingFace2;
        drawDie(canvas, paint, cx1, cy + oy1, dieSize, face1, rot1);
        drawDie(canvas, paint, cx2, cy + oy2, dieSize, face2, rot2);

        // 木盅示意
        paint.setColor(Color.argb(90, 60, 45, 35));
        canvas.drawOval(panel.left + 40, cy + dieSize * 0.55f,
                panel.right - 40, cy + dieSize * 1.15f, paint);

        if (revealed) {
            paint.setColor(Color.argb(200, 50, 100, 150));
            canvas.drawRoundRect(okButton.left, okButton.top, okButton.right, okButton.bottom, 10, 10, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(okButton.left, okButton.top, okButton.right, okButton.bottom, 10, 10, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(28);
            canvas.drawText("看结果", okButton.centerX(), okButton.centerY() + 10, paint);
        }
    }

    private void drawDie(Canvas canvas, Paint paint, float cx, float cy, float size, int value, float rotDeg) {
        canvas.save();
        canvas.rotate(rotDeg, cx, cy);
        float half = size * 0.5f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(245, 240, 228));
        canvas.drawRoundRect(cx - half, cy - half, cx + half, cy + half, size * 0.14f, size * 0.14f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(Color.rgb(70, 60, 52));
        canvas.drawRoundRect(cx - half, cy - half, cx + half, cy + half, size * 0.14f, size * 0.14f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(28, 22, 20));
        float pip = size * 0.1f;
        float step = size * 0.28f;
        for (float[] pipOff : pipsForValue(value)) {
            canvas.drawCircle(cx + pipOff[0] * step, cy + pipOff[1] * step, pip, paint);
        }
        canvas.restore();
    }

    private static float[][] pipsForValue(int value) {
        switch (value) {
            case 1:
                return new float[][]{{0, 0}};
            case 2:
                return new float[][]{{-1, -1}, {1, 1}};
            case 3:
                return new float[][]{{-1, -1}, {0, 0}, {1, 1}};
            case 4:
                return new float[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            case 5:
                return new float[][]{{-1, -1}, {1, -1}, {0, 0}, {-1, 1}, {1, 1}};
            default:
                return new float[][]{{-1, -1}, {-1, 0}, {-1, 1}, {1, -1}, {1, 0}, {1, 1}};
        }
    }

    public boolean handleTouch(float x, float y) {
        if (!visible) {
            return false;
        }
        if (!revealed) {
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(okButton, x, y) || panel.contains((int) x, (int) y)) {
            Listener l = listener;
            hide();
            if (l != null) {
                l.onRollFinished();
            }
            return true;
        }
        return true;
    }
}
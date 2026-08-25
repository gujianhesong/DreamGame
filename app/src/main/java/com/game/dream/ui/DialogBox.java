package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A reusable dialog box for text and choices
 */
public class DialogBox {
    public interface DialogListener {
        void onOptionSelected(int optionIndex);
    }

    private boolean isVisible;
    private Rect bounds;
    private Rect closeButton;
    private String title;
    private String message;
    private List<String> options;
    private List<Rect> optionButtons;
    private DialogListener listener;

    // Colors
    private static final int BG_COLOR = Color.argb(240, 20, 25, 35);
    private static final int BORDER_COLOR = Color.rgb(100, 180, 255);
    private static final int TEXT_COLOR = Color.WHITE;
    private static final int BTN_BG_COLOR = Color.argb(180, 50, 100, 150);
    private static final int BTN_PRESSED_COLOR = Color.argb(220, 70, 130, 180);

    public DialogBox() {
        this.isVisible = false;
        this.bounds = new Rect();
        this.closeButton = new Rect();
        this.options = new ArrayList<>();
        this.optionButtons = new ArrayList<>();
    }

    /**
     * Show the dialog with a message and options
     */
    public void show(String title, String message, List<String> options, DialogListener listener) {
        this.title = title;
        this.message = message;
        this.options.clear();
        if(options != null){
            this.options.addAll(options);
        }
        this.listener = listener;
        this.isVisible = true;
        calculateButtonBounds();
    }

    /**
     * Hide the dialog
     */
    public void hide() {
        this.isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set position and size
     */
    public void setBounds(int x, int y, int width, int height) {
        bounds.set(x, y, x + width, y + height);
        if (isVisible) calculateButtonBounds();
    }

    private void calculateButtonBounds() {
        optionButtons.clear();
        if (options.isEmpty()) return;

        int btnHeight = 42;
        int gap = 20;

        // Calculate button widths based on text
        Paint tempPaint = new Paint();
        tempPaint.setAntiAlias(true);
        tempPaint.setTextSize(28);
        int totalBtnWidth = 0;
        int[] btnWidths = new int[options.size()];
        for (int i = 0; i < options.size(); i++) {
            btnWidths[i] = (int) tempPaint.measureText(options.get(i)) + 60;
            totalBtnWidth += btnWidths[i];
        }
        totalBtnWidth += (options.size() - 1) * gap;

        // Center buttons horizontally
        int startX = bounds.left + 30;

        // Calculate starting Y dynamically based on title + message height
        int contentY = bounds.top + 20;
        if (title != null) {
            contentY += 50; // title height (40px font + 10px spacing)
        }
        if (message != null) {
            tempPaint.setTextSize(30);
            String[] lines = message.split("\n");
            contentY += lines.length * 42; // message lines (30px font + 12px line height)
        }
        contentY += 30; // gap before buttons
        int startY = contentY;

        for (int i = 0; i < options.size(); i++) {
            int yPos = startY + i * (btnHeight + gap);
            if (yPos + btnHeight > bounds.bottom - 20) break;
            optionButtons.add(new Rect(startX, yPos, startX + btnWidths[i], yPos + btnHeight));
        }

        // Close button
        int padding = 10;
        int btnSize = 40;
        closeButton.set(bounds.right - btnSize - padding, bounds.top + padding,
                bounds.right - padding, bounds.top + padding + btnSize);
    }

    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Background
        paint.setColor(BG_COLOR);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 15, 15, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(BORDER_COLOR);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 15, 15, paint);
        paint.setStyle(Paint.Style.FILL);

        // Close button
        drawCloseButton(canvas, paint);

        float posY = bounds.top;
        // Title
        if (title != null) {
            paint.setColor(Color.rgb(255, 215, 0)); // Gold title
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            posY += 45;
            canvas.drawText(title, bounds.centerX(), posY, paint);
        }

        if (message != null) {
            paint.setColor(TEXT_COLOR);
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.LEFT);

            posY += 60;

            // Draw message with simple line breaking
            String[] lines = message.split("\n");
            for (String line : lines) {
                canvas.drawText(line, bounds.left + 30, posY, paint);
                posY += 45;
            }
        }

        // Options Buttons
        for (int i = 0; i < optionButtons.size(); i++) {
            Rect btn = optionButtons.get(i);

            // Button background
            paint.setColor(BTN_BG_COLOR);
            canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);

            // Button border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            // Button text
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(options.get(i), btn.centerX(), btn.centerY() + 10, paint);
        }
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

    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        for (int i = 0; i < optionButtons.size(); i++) {
            if (optionButtons.get(i).contains((int) x, (int) y)) {
                hide();
                if (listener != null) {
                    listener.onOptionSelected(i);
                }
                return true;
            }
        }
        return false; // Return false to allow clicking outside to close if desired
    }
}

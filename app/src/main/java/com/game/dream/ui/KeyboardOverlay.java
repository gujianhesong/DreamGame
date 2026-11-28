package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseIntArray;
import android.view.MotionEvent;

import com.game.dream.GameEngine;
import com.game.dream.audio.GmInstruments;
import com.game.dream.audio.KeyboardSynthRouter;
import com.game.dream.utils.TouchUtil;

/** 电子琴：C4–C7（三 octave），GM128 音源，内置 SF2 合成。 */
public class KeyboardOverlay {

    public static final int MIDI_MIN = 60; // C4
    public static final int MIDI_MAX = 96; // C7
    private static final int DEFAULT_VELOCITY = 100;
    private static final float WHITE_KEY_GAP = 2f;
    private static final float BLACK_KEY_WIDTH_RATIO = 0.62f;
    private static final float BLACK_KEY_HEIGHT_RATIO = 0.62f;
    private static final float KEY_TOP_INSET = 2f;
    private static final int INST_ROW_H = 48;
    private static final int INST_BTN_H = 48;
    private static final float PICKER_TAP_SLOP = 16f;

    private boolean visible;
    private boolean loadFailed;
    private boolean loading;
    private String loadHint;
    private boolean pickerOpen;
    private float instrumentScroll;
    private float maxInstrumentScroll;
    private float lastPickerTouchY;
    private float pickerTouchStartX;
    private float pickerTouchStartY;
    private boolean pickerDragging;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int selectedProgram = GmInstruments.DEFAULT_PROGRAM;
    private final SparseIntArray pointerMidi = new SparseIntArray();
    private final boolean[] keyLit = new boolean[128];

    private final Rect panel = new Rect();
    private final Rect body = new Rect();
    private final Rect titleBar = new Rect();
    private final Rect closeButton = new Rect();
    private final Rect instButton = new Rect();
    private final Rect pickerPanel = new Rect();
    private final Rect keyboardArea = new Rect();

    private final int[] keyMidi = new int[64];
    private final RectF[] keyRects = new RectF[64];
    private final boolean[] keyBlack = new boolean[64];
    private int keyCount;

    public void show() {
        visible = true;
        loading = true;
        loadFailed = false;
        pickerOpen = false;
        instrumentScroll = 0f;
        pointerMidi.clear();
        layoutAll();
        KeyboardSynthRouter.getInstance().connectAsync(GameEngine.getInstance().getAppContext(),
            () -> mainHandler.post(() -> {
                loading = false;
                loadFailed = false;
                loadHint = KeyboardSynthRouter.getInstance().getStatusLine();
            }),
            () -> mainHandler.post(() -> {
                loading = false;
                loadFailed = true;
            }));
    }

    public void hide() {
        visible = false;
        pointerMidi.clear();
        for (int i = 0; i < keyLit.length; i++) {
            keyLit[i] = false;
        }
        KeyboardSynthRouter.getInstance().release();
    }

    public boolean isVisible() {
        return visible;
    }

    private static boolean isBlackKey(int midi) {
        int k = midi % 12;
        return k == 1 || k == 3 || k == 6 || k == 8 || k == 10;
    }

    private void layoutAll() {
        int sw = GameEngine.getScreenWidth();
        int sh = GameEngine.getScreenHeight();
        panel.set(4, 4, sw - 4, sh - 4);

        int closeW = Math.max(96, Math.min(132, panel.width() / 7));
        closeButton.set(panel.right - closeW - 10, panel.top + 8,
            panel.right - 10, panel.top + 54);
        titleBar.set(panel.left + 12, panel.top + 4,
            Math.max(panel.left + 80, closeButton.left - 12), panel.top + 56);

        int headerBottom = panel.top + 118;
        body.set(panel.left + 8, headerBottom, panel.right - 8, panel.bottom - 20);

        int btnW = Math.min((int) (body.width() * 0.62f), 420);
        btnW = Math.max(btnW, Math.min(body.width() - 24, 280));
        int btnTop = body.top + 8;
        instButton.set(body.left + 14, btnTop, body.left + 14 + btnW, btnTop + INST_BTN_H);

        int playMargin = Math.max(32, (int) (body.height() * 0.06f));
        int keyPadH = 8;
        int keyBlockH = Math.min(310, Math.max(262, (int) (body.height() * 0.44f)));
        int pickerTop = instButton.bottom + 6;
        int pickerPreferH = Math.min(540, Math.max(400, (int) (body.height() * 0.56f)));
        int keyboardBottom = body.bottom - playMargin;
        int keyboardTop = keyboardBottom - keyBlockH;
        int pickerBottom = keyboardTop - 6;
        int pickerH = pickerBottom - pickerTop;
        if (pickerH < pickerPreferH) {
            int shift = pickerPreferH - pickerH;
            keyboardTop += shift;
            keyboardBottom += shift;
            int maxBottom = body.bottom - playMargin;
            if (keyboardBottom > maxBottom) {
                keyboardBottom = maxBottom;
                keyboardTop = Math.max(pickerTop + pickerPreferH + 6, keyboardBottom - keyBlockH);
            }
            pickerBottom = keyboardTop - 6;
        }
        keyboardArea.set(body.left + keyPadH, keyboardTop, body.right - keyPadH, keyboardBottom);
        pickerPanel.set(instButton.left, pickerTop, instButton.right, pickerBottom);

        layoutKeys();
        maxInstrumentScroll = Math.max(0,
            GmInstruments.COUNT * INST_ROW_H - pickerPanel.height());
    }

    private int countWhiteKeysInRange() {
        int n = 0;
        for (int m = MIDI_MIN; m <= MIDI_MAX; m++) {
            if (!isBlackKey(m)) {
                n++;
            }
        }
        return n;
    }

    /** 音高 m 之前（不含 m）已有多少个白键。 */
    private int whiteKeysBefore(int m) {
        int n = 0;
        for (int i = MIDI_MIN; i < m; i++) {
            if (!isBlackKey(i)) {
                n++;
            }
        }
        return n;
    }

    private void layoutKeys() {
        keyCount = 0;
        int whiteCount = countWhiteKeysInRange();
        if (whiteCount <= 0) {
            return;
        }
        float areaW = keyboardArea.width();
        float totalGaps = WHITE_KEY_GAP * Math.max(0, whiteCount - 1);
        float whiteW = (areaW - totalGaps) / whiteCount;
        float blackW = whiteW * BLACK_KEY_WIDTH_RATIO;
        float yTop = keyboardArea.top + KEY_TOP_INSET;
        float keySpanH = keyboardArea.bottom - yTop;
        float blackH = keySpanH * BLACK_KEY_HEIGHT_RATIO;

        int whiteIndex = 0;
        for (int m = MIDI_MIN; m <= MIDI_MAX; m++) {
            if (isBlackKey(m)) {
                continue;
            }
            float left = keyboardArea.left + whiteIndex * (whiteW + WHITE_KEY_GAP);
            RectF r = new RectF(left, yTop, left + whiteW, keyboardArea.bottom);
            putKey(m, r, false);
            whiteIndex++;
        }

        for (int m = MIDI_MIN; m <= MIDI_MAX; m++) {
            if (!isBlackKey(m)) {
                continue;
            }
            int wIdx = whiteKeysBefore(m);
            if (wIdx <= 0) {
                continue;
            }
            float centerX = keyboardArea.left + wIdx * (whiteW + WHITE_KEY_GAP) - WHITE_KEY_GAP * 0.5f;
            float left = centerX - blackW * 0.5f;
            RectF r = new RectF(left, yTop, left + blackW, yTop + blackH);
            putKey(m, r, true);
        }
    }

    private void putKey(int midi, RectF r, boolean black) {
        if (keyCount >= keyRects.length) {
            return;
        }
        keyMidi[keyCount] = midi;
        keyRects[keyCount] = r;
        keyBlack[keyCount] = black;
        keyCount++;
    }

    public void draw(Canvas canvas) {
        if (!visible) {
            return;
        }
        int sw = GameEngine.getScreenWidth();
        int sh = GameEngine.getScreenHeight();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.argb(200, 14, 12, 28));
        canvas.drawRect(0, 0, sw, sh, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(36, 32, 52));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 16, 16, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(178, 148, 96));
        canvas.drawRoundRect(panel.left + 2, panel.top + 2, panel.right - 2, panel.bottom - 2, 14, 14, paint);
        paint.setStyle(Paint.Style.FILL);

        drawValance(canvas, paint);
        drawTitle(canvas, paint);
        drawClose(canvas, paint);
        drawBody(canvas, paint);
        drawInstBar(canvas, paint);
        if (pickerOpen) {
            drawPicker(canvas, paint);
        }
        drawKeyboard(canvas, paint);
        if (loading) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.rgb(255, 230, 200));
            paint.setTextSize(28);
            canvas.drawText("正在加载 SF2 音色…", body.centerX(), body.centerY(), paint);
        } else if (loadFailed) {
            String err = KeyboardSynthRouter.getLastLoadError();
            if (err == null || err.isEmpty()) {
                err = "音源加载失败，请重新完整编译安装";
            }
            paint.setTextSize(26);
            paint.setColor(Color.rgb(255, 180, 160));
            canvas.drawText("音源加载失败", body.centerX(), body.centerY() - 22, paint);
            paint.setTextSize(20);
            canvas.drawText(err, body.centerX(), body.centerY() + 8, paint);
        }
    }

    private void drawValance(Canvas canvas, Paint paint) {
        int band = panel.top + 84;
        int[] cols = {Color.rgb(48, 40, 72), Color.rgb(64, 52, 88), Color.rgb(118, 96, 56)};
        for (int i = 0; i < 6; i++) {
            paint.setColor(cols[i % cols.length]);
            float h = (band - panel.top - 8) / 6f;
            canvas.drawRect(panel.left + 8, panel.top + 8 + i * h,
                panel.right - 8, panel.top + 8 + (i + 1) * h, paint);
        }
    }

    private void drawTitle(Canvas canvas, Paint paint) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(255, 232, 190));
        paint.setTextSize(34);
        paint.setShadowLayer(5, 0, 2, Color.argb(120, 20, 10, 40));
        canvas.drawText("蔚莱琴案", titleBar.centerX(), titleBar.top + titleBar.height() * 0.72f, paint);
        paint.clearShadowLayer();
        paint.setTextSize(24);
        paint.setColor(Color.argb(220, 230, 220, 255));
        canvas.drawText("C4 – C7 · GM 音源", panel.centerX(), panel.top + 102, paint);
    }

    private void drawClose(Canvas canvas, Paint paint) {
        drawWoodBtn(canvas, paint, closeButton, "离开", 32);
    }

    private void drawBody(Canvas canvas, Paint paint) {
        paint.setColor(Color.rgb(28, 26, 42));
        canvas.drawRoundRect(body.left, body.top, body.right, body.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.argb(100, 196, 164, 110));
        canvas.drawRoundRect(body.left + 2, body.top + 2, body.right - 2, body.bottom - 2, 11, 11, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawInstBar(Canvas canvas, Paint paint) {
        String label;
        if (pickerOpen) {
            label = "离开 ▲";
        } else {
            label = selectedProgram + " · " + GmInstruments.getName(selectedProgram);
        }
        drawWoodBtn(canvas, paint, instButton, label, 24);
        if (loadHint != null && !loadHint.isEmpty() && !pickerOpen) {
            int hintLeft = instButton.right + 12;
            if (hintLeft < body.right - 8) {
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(18);
                paint.setColor(Color.rgb(200, 188, 220));
                float ty = instButton.centerY() - (paint.descent() + paint.ascent()) / 2f;
                float maxW = body.right - 8 - hintLeft;
                canvas.drawText(ellipsize(paint, loadHint, maxW), hintLeft, ty, paint);
            }
        }
    }

    private void drawPicker(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(245, 42, 38, 58));
        canvas.drawRoundRect(pickerPanel.left, pickerPanel.top, pickerPanel.right, pickerPanel.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(196, 164, 110));
        canvas.drawRoundRect(pickerPanel.left, pickerPanel.top, pickerPanel.right, pickerPanel.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.FILL);

        canvas.save();
        canvas.clipRect(pickerPanel);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(24);
        float y = pickerPanel.top + 36 - instrumentScroll;
        for (int p = 0; p < GmInstruments.COUNT; p++) {
            if (y > pickerPanel.bottom) {
                break;
            }
            if (y + INST_ROW_H >= pickerPanel.top) {
                boolean sel = p == selectedProgram;
                if (sel) {
                    paint.setColor(Color.argb(200, 90, 70, 110));
                    canvas.drawRect(pickerPanel.left + 4, y - 26,
                        pickerPanel.right - 4, y + 14, paint);
                }
                paint.setColor(sel ? Color.rgb(255, 240, 210) : Color.rgb(220, 215, 235));
                canvas.drawText(p + " · " + GmInstruments.getName(p),
                    pickerPanel.left + 16, y, paint);
            }
            y += INST_ROW_H;
        }
        canvas.restore();
    }

    private void drawKeyboard(Canvas canvas, Paint paint) {
        paint.setColor(Color.rgb(22, 20, 32));
        canvas.drawRoundRect(keyboardArea.left, keyboardArea.top,
            keyboardArea.right, keyboardArea.bottom, 8, 8, paint);

        for (int i = 0; i < keyCount; i++) {
            if (keyBlack[i]) {
                continue;
            }
            drawOneKey(canvas, paint, i);
        }
        for (int i = 0; i < keyCount; i++) {
            if (!keyBlack[i]) {
                continue;
            }
            drawOneKey(canvas, paint, i);
        }
    }

    private void drawOneKey(Canvas canvas, Paint paint, int i) {
        int midi = keyMidi[i];
        RectF r = keyRects[i];
        boolean on = keyLit[midi];
        boolean black = keyBlack[i];
        if (black) {
            paint.setColor(on ? Color.rgb(72, 120, 180) : Color.rgb(28, 26, 32));
        } else {
            paint.setColor(on ? Color.rgb(210, 228, 255) : Color.rgb(248, 244, 236));
        }
        canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, black ? 6 : 4, black ? 6 : 4, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(black ? 1f : 1.5f);
        paint.setColor(Color.rgb(120, 100, 80));
        canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, black ? 6 : 4, black ? 6 : 4, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawWoodBtn(Canvas canvas, Paint paint, Rect r, String label, float textSize) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(88, 58, 48));
        canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(196, 154, 88));
        canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 236, 200));
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        float ty = r.centerY() - (paint.descent() + paint.ascent()) / 2f;
        float maxW = r.width() - 20;
        canvas.drawText(ellipsize(paint, label, maxW), r.centerX(), ty, paint);
    }

    private static String ellipsize(Paint paint, String text, float maxW) {
        if (text == null || text.isEmpty() || paint.measureText(text) <= maxW) {
            return text == null ? "" : text;
        }
        String suffix = "…";
        for (int i = text.length() - 1; i > 0; i--) {
            String s = text.substring(0, i) + suffix;
            if (paint.measureText(s) <= maxW) {
                return s;
            }
        }
        return suffix;
    }

    public boolean handleTouch(MotionEvent event) {
        if (!visible) {
            return false;
        }
        int action = event.getActionMasked();
        if (loading) {
            return true;
        }
        if (action == MotionEvent.ACTION_UP && TouchUtil.checkIsInTouchRectFloat(closeButton,
            event.getX(), event.getY())) {
            hide();
            return true;
        }
        if (action == MotionEvent.ACTION_UP && TouchUtil.checkIsInTouchRectFloat(instButton,
            event.getX(), event.getY())) {
            pickerOpen = !pickerOpen;
            return true;
        }
        if (pickerOpen) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    lastPickerTouchY = event.getY();
                    pickerTouchStartX = event.getX();
                    pickerTouchStartY = event.getY();
                    pickerDragging = false;
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getX() - pickerTouchStartX;
                    float dyTotal = event.getY() - pickerTouchStartY;
                    if (!pickerDragging
                            && dx * dx + dyTotal * dyTotal > PICKER_TAP_SLOP * PICKER_TAP_SLOP) {
                        pickerDragging = true;
                    }
                    float dy = event.getY() - lastPickerTouchY;
                    lastPickerTouchY = event.getY();
                    if (pickerDragging) {
                        instrumentScroll -= dy;
                        instrumentScroll = clamp(instrumentScroll, 0, maxInstrumentScroll);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    float x = event.getX();
                    float y = event.getY();
                    if (!pickerDragging && pickerPanel.contains((int) x, (int) y)) {
                        float rel = y - pickerPanel.top - 36 + instrumentScroll;
                        int row = (int) (rel / INST_ROW_H);
                        if (row >= 0 && row < GmInstruments.COUNT) {
                            selectedProgram = row;
                            KeyboardSynthRouter.getInstance().setProgram(row);
                            pickerOpen = false;
                        }
                    }
                    pickerDragging = false;
                    return true;
                }
                default:
                    return true;
            }
        }
        if (loadFailed) {
            return true;
        }

        int idx = event.getActionIndex();
        int pid = event.getPointerId(idx);
        float x = event.getX(idx);
        float y = event.getY(idx);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                trackPointerKey(pid, x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                for (int p = 0; p < event.getPointerCount(); p++) {
                    trackPointerKey(event.getPointerId(p), event.getX(p), event.getY(p));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                onKeyUp(pid);
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseAllPointers();
                break;
            default:
                break;
        }
        return true;
    }

    private void trackPointerKey(int pointerId, float x, float y) {
        int midi = hitKey(x, y);
        int old = pointerMidi.get(pointerId, -1);
        if (midi < 0) {
            if (old >= 0) {
                pointerMidi.delete(pointerId);
                releaseKey(old);
            }
            return;
        }
        if (old >= 0 && old != midi) {
            pointerMidi.delete(pointerId);
            releaseKey(old);
        }
        pointerMidi.put(pointerId, midi);
        if (!keyLit[midi]) {
            keyLit[midi] = true;
            KeyboardSynthRouter.getInstance().noteOn(midi, DEFAULT_VELOCITY);
        }
    }

    private void onKeyUp(int pointerId) {
        int midi = pointerMidi.get(pointerId, -1);
        pointerMidi.delete(pointerId);
        if (midi >= 0) {
            releaseKey(midi);
        }
    }

    private void releaseKey(int midi) {
        if (!keyLit[midi]) {
            return;
        }
        boolean held = false;
        for (int i = 0; i < pointerMidi.size(); i++) {
            if (pointerMidi.valueAt(i) == midi) {
                held = true;
                break;
            }
        }
        if (!held) {
            keyLit[midi] = false;
            KeyboardSynthRouter.getInstance().noteOff(midi);
        }
    }

    private void releaseAllPointers() {
        KeyboardSynthRouter.getInstance().allNotesOff();
        pointerMidi.clear();
        for (int i = 0; i < keyLit.length; i++) {
            keyLit[i] = false;
        }
    }

    private int hitKey(float x, float y) {
        for (int i = 0; i < keyCount; i++) {
            if (keyBlack[i] && hitBlackKey(keyRects[i], x, y)) {
                return keyMidi[i];
            }
        }
        for (int i = 0; i < keyCount; i++) {
            if (!keyBlack[i] && keyRects[i].contains(x, y)) {
                return keyMidi[i];
            }
        }
        return -1;
    }

    /** 黑键优先于白键；触控区略放宽便于点按 */
    private static boolean hitBlackKey(RectF r, float x, float y) {
        float slopX = 5f;
        float slopY = 4f;
        return x >= r.left - slopX && x <= r.right + slopX
                && y >= r.top - slopY && y <= r.bottom + slopY;
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
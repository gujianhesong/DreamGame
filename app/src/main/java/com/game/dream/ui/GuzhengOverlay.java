package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;

import com.game.dream.GameEngine;
import com.game.dream.audio.GuzhengSoundPlayer;
import com.game.dream.utils.TouchUtil;

/**
 * 古筝试弹：俯视图 —— 筝首在左、筝尾/岳山在右；21 根弦水平平行，一弦在上（外弦高音）。
 */
public class GuzhengOverlay {

    public interface Listener {
        void onClosed();
    }

    public static final int STRING_COUNT = 21;

    /** 一弦（高音）雁柱靠右，二十一弦（低音）雁柱靠左，近似实物斜列 */
    private static final float BRIDGE_FRAC_HIGH = 0.68f;
    private static final float BRIDGE_FRAC_LOW = 0.36f;

    private boolean visible;
    private Listener listener;

    private final Rect panel = new Rect();
    private final Rect body = new Rect();
    private final Rect closeButton = new Rect();
    private final Rect titleBar = new Rect();
    private final float[] stringY = new float[STRING_COUNT];

  /** 左侧按弦区：每根手指独立按弦（弦号 + 横向深度） */
    private static final class LeftPress {
        int stringNo;
        float x;
    }

    private final SparseArray<LeftPress> leftPressByPointer = new SparseArray<>();
    /** 本轮在拨弦区已发声的弦，同指同弦按住不重复播 */
    private int pluckSoundedString;

    private int highlightString;
    /** 拨弦手指在拨弦区内已发声的弦（多指各记各的） */
    private final SparseIntArray pluckFingerString = new SparseIntArray();

    public void show(Listener listener) {
        this.listener = listener;
        this.visible = true;
        leftPressByPointer.clear();
        pluckSoundedString = 0;
        highlightString = 0;
        layout();
        GuzhengSoundPlayer player = GuzhengSoundPlayer.getInstance();
        player.init(GameEngine.getInstance().getAppContext());
        if (!player.isReady()) {
            GameEngine.getInstance().showCenterToast("古筝音色配置未加载，请重新安装完整包体", 2800);
        }
    }

    public void hide() {
        visible = false;
        GuzhengSoundPlayer.getInstance().release();
        Listener l = listener;
        listener = null;
        if (l != null) {
            l.onClosed();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    private void layout() {
        int sw = GameEngine.getScreenWidth();
        int sh = GameEngine.getScreenHeight();
        panel.set(4, 4, sw - 4, sh - 4);

        int closeW = Math.max(96, Math.min(132, panel.width() / 7));
        closeButton.set(panel.right - closeW - 10, panel.top + 8,
                panel.right - 10, panel.top + 54);

        titleBar.set(panel.left + 12, panel.top + 4,
                Math.max(panel.left + 80, closeButton.left - 12), panel.top + 56);
        int headerBottom = panel.top + 88;
        body.set(panel.left + 8, headerBottom, panel.right - 8, panel.bottom - 28);

        float topPad = Math.max(26f, body.height() * 0.055f);
        float bottomPad = Math.max(20f, body.height() * 0.05f);
        float usable = body.height() - topPad - bottomPad;
        for (int i = 0; i < STRING_COUNT; i++) {
            stringY[i] = body.top + topPad + usable * (i + 0.5f) / STRING_COUNT;
        }
    }

    /** 弦号 1–21 的雁柱 x（弦上有效分隔点） */
    private float bridgeXForString(int stringNo) {
        float t = (stringNo - 1) / (float) (STRING_COUNT - 1);
        float frac = BRIDGE_FRAC_HIGH + (BRIDGE_FRAC_LOW - BRIDGE_FRAC_HIGH) * t;
        return body.left + body.width() * frac;
    }

    public void draw(Canvas canvas) {
        if (!visible) {
            return;
        }
        int sw = GameEngine.getScreenWidth();
        int sh = GameEngine.getScreenHeight();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.argb(195, 12, 10, 8));
        canvas.drawRect(0, 0, sw, sh, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(50, 36, 38));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 16, 16, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.rgb(178, 138, 72));
        canvas.drawRoundRect(panel.left + 2, panel.top + 2, panel.right - 2, panel.bottom - 2, 14, 14, paint);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.argb(120, 220, 190, 120));
        canvas.drawRoundRect(panel.left + 6, panel.top + 6, panel.right - 6, panel.bottom - 6, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        drawBrocadeValance(canvas, paint);
        drawClose(canvas, paint);
        drawHeader(canvas, paint);

        drawZitherBody(canvas, paint);
        drawStrings(canvas, paint);
        drawNotePlaque(canvas, paint);
    }

    /** 红香院锦帘横条（绛紫锦色，较南市红篷更雅） */
    private void drawBrocadeValance(Canvas canvas, Paint paint) {
        int awBottom = panel.top + 84;
        int stripes = 8;
        float stripeH = (awBottom - panel.top - 8) / (float) stripes;
        int[] colors = {
                Color.rgb(92, 48, 72),
                Color.rgb(118, 72, 92),
                Color.rgb(88, 56, 78),
                Color.rgb(128, 92, 58)
        };
        for (int i = 0; i < stripes; i++) {
            paint.setColor(colors[i % colors.length]);
            canvas.drawRect(panel.left + 8, panel.top + 8 + i * stripeH,
                    panel.right - 8, panel.top + 8 + (i + 1) * stripeH, paint);
        }
        paint.setColor(Color.rgb(72, 48, 38));
        canvas.drawRect(panel.left + 8, awBottom - 2, panel.right - 8, awBottom + 5, paint);
        paint.setColor(Color.argb(90, 200, 165, 100));
        canvas.drawRect(panel.left + 8, awBottom + 2, panel.right - 8, awBottom + 4, paint);
    }

    private void drawHeader(Canvas canvas, Paint paint) {
        float titleCx = titleBar.centerX();
        float titleCy = titleBar.top + titleBar.height() * 0.68f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 232, 175));
        paint.setTextSize(fitTextSize(paint, "红香琴案",
                titleBar.width() - 4f, 28f, 38f));
        paint.setShadowLayer(6, 0, 2, Color.argb(160, 50, 20, 30));
        canvas.drawText("红香琴案", titleCx, titleCy, paint);
        paint.clearShadowLayer();

        paint.setColor(Color.argb(90, 255, 220, 150));
        canvas.drawRect(panel.left + 20, panel.top + 84, panel.right - 20, panel.top + 86, paint);

        float chipY = panel.top + 76;
        drawStatChip(canvas, paint, panel.left + panel.width() * 0.28f, chipY, "雁柱左 · 按弦");
        drawStatChip(canvas, paint, panel.right - panel.width() * 0.28f, chipY, "岳山右 · 拨弦");
    }

    private void drawStatChip(Canvas canvas, Paint paint, float cx, float cy, String text) {
        paint.setTextSize(20f);
        float tw = paint.measureText(text);
        float pad = 14f;
        float l = cx - tw / 2f - pad;
        float r = cx + tw / 2f + pad;
        float t = cy - 22;
        float b = cy + 12;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(210, 58, 42, 52));
        canvas.drawRoundRect(l, t, r, b, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.rgb(196, 154, 88));
        canvas.drawRoundRect(l, t, r, b, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 240, 225));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, cx, cy + 2, paint);
    }

    private void drawClose(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(92, 58, 42));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(196, 154, 88));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 236, 200));
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("离开", closeButton.centerX(), closeButton.centerY() + 10, paint);
    }

    private void drawNotePlaque(Canvas canvas, Paint paint) {
        if (highlightString <= 0) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.argb(150, 220, 200, 175));
            paint.setTextSize(17);
            canvas.drawText("二十一弦 · 按弦变音 · 空弦一音", panel.centerX(), panel.bottom - 14, paint);
            return;
        }
        GuzhengSoundPlayer gp = GuzhengSoundPlayer.getInstance();
        String line = gp.formatStringLabel(highlightString) + " · 空弦";
        paint.setTextSize(22);
        float tw = paint.measureText(line);
        float pad = 18f;
        float cx = panel.centerX();
        float cy = panel.bottom - 18;
        float l = cx - tw / 2f - pad;
        float r = cx + tw / 2f + pad;
        float top = cy - 26;
        float bottom = cy + 8;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(230, 58, 42, 48));
        canvas.drawRoundRect(l, top, r, bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.rgb(196, 154, 88));
        canvas.drawRoundRect(l, top, r, bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 228, 160));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(line, cx, cy, paint);
    }

    private static float fitTextSize(Paint paint, String sample, float maxWidth,
                                     float minSize, float maxSize) {
        for (float size = maxSize; size >= minSize; size -= 0.5f) {
            paint.setTextSize(size);
            if (paint.measureText(sample) <= maxWidth) {
                return size;
            }
        }
        return minSize;
    }

    private void drawZitherBody(Canvas canvas, Paint paint) {
        float yueshanX = body.right - body.width() * 0.06f;
        float pressRight = body.left + body.width() * 0.38f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(86, 50, 32));
        canvas.drawRoundRect(body.left, body.top, body.right, body.bottom, 12, 12, paint);
        paint.setColor(Color.argb(28, 120, 90, 110));
        canvas.drawRect(body.left, body.top, pressRight, body.bottom, paint);
        paint.setColor(Color.argb(22, 255, 235, 210));
        canvas.drawRect(yueshanX, body.top, body.right, body.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(140, 196, 154, 88));
        canvas.drawRoundRect(body.left + 1, body.top + 1, body.right - 1, body.bottom - 1, 11, 11, paint);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 6; i++) {
            float fy = body.top + body.height() * (0.18f + i * 0.13f);
            paint.setColor(Color.argb(35, 40, 26, 18));
            canvas.drawLine(body.left + body.width() * 0.06f, fy, body.right - body.width() * 0.08f, fy, paint);
        }

        paint.setColor(Color.rgb(200, 175, 120));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(yueshanX, body.top + 4, yueshanX, body.bottom - 4, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(46, 32, 22));
        canvas.drawRoundRect(body.left, body.top, body.left + body.width() * 0.06f,
                body.bottom, 4, 4, paint);
        paint.setColor(Color.rgb(38, 26, 18));
        canvas.drawRoundRect(body.left - 4, body.bottom - 18, body.right + 4, body.bottom + 8, 6, 6, paint);
        paint.setColor(Color.rgb(32, 22, 16));
        canvas.drawRect(body.left, body.bottom + 4, body.right, body.bottom + 10, paint);

        paint.setTextSize(18);
        paint.setColor(Color.argb(200, 255, 235, 200));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("按", body.left + body.width() * 0.03f, body.top + 24, paint);
        canvas.drawText("拨", yueshanX + body.width() * 0.08f, body.top + 24, paint);
    }

    private void drawStrings(Canvas canvas, Paint paint) {
        float yueshanX = body.right - body.width() * 0.06f;
        float stringLeft = body.left + body.width() * 0.07f;
        float stringRight = yueshanX;

        for (int i = 0; i < STRING_COUNT; i++) {
            int no = i + 1;
            float y = stringY[i];
            float bridgeX = bridgeXForString(no);
            boolean pressed = isStringLeftPressed(no);
            boolean lit = highlightString == no;

            float baseStroke = Math.max(1.6f, body.height() / STRING_COUNT * 0.22f);
            if (lit) {
                paint.setColor(Color.argb(255, 255, 230, 150));
                paint.setStrokeWidth(baseStroke * 1.35f);
            } else if (pressed) {
                paint.setColor(Color.argb(220, 255, 190, 210));
                paint.setStrokeWidth(baseStroke * 1.15f);
            } else {
                paint.setColor(Color.argb(210, 220, 215, 205));
                paint.setStrokeWidth(baseStroke * (0.75f + no / 28f));
            }
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(stringLeft, y, stringRight, y, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(228, 188, 118));
            float bh = Math.max(6f, body.height() / STRING_COUNT * 0.28f);
            float bw = Math.max(5f, bh * 0.55f);
            RectF bridge = new RectF(bridgeX - bw / 2f, y - bh * 0.85f, bridgeX + bw / 2f, y + bh * 0.4f);
            canvas.drawRoundRect(bridge, 2, 2, paint);
            paint.setColor(Color.rgb(120, 95, 60));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.2f);
            canvas.drawLine(bridgeX - bw, y, bridgeX + bw, y, paint);
            paint.setStyle(Paint.Style.FILL);

            if (no == 1 || no == 11 || no == 21 || no % 5 == 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(150, 255, 255, 255));
                paint.setTextSize(Math.max(13f, body.height() / STRING_COUNT * 0.42f));
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(no + "弦", stringLeft - Math.max(8f, body.width() * 0.02f), y + 5, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    public boolean handleTouch(MotionEvent event) {
        if (!visible) {
            return false;
        }
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        int pointerId = event.getPointerId(pointerIndex);

        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
                && TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            clearAllTouchState();
            return true;
        }
        if (action == MotionEvent.ACTION_POINTER_UP) {
            clearLeftPress(pointerId);
            pluckFingerString.delete(pointerId);
            if (leftPressByPointer.size() == 0) {
                pluckSoundedString = 0;
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                updatePressOrPluck(event.getX(i), event.getY(i), event.getPointerId(i));
            }
            return true;
        }

        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) {
            return true;
        }

        if (!body.contains((int) x, (int) y)) {
            return true;
        }

        int stringNo = hitString(y);
        if (stringNo <= 0) {
            return true;
        }
        float bridgeX = bridgeXForString(stringNo);
        boolean inPluck = x > bridgeX;
        boolean inPress = x <= bridgeX;

        if (action == MotionEvent.ACTION_DOWN) {
            pluckSoundedString = 0;
        }

        if (inPress) {
            setLeftPress(pointerId, stringNo, x);
        }
        if (inPluck) {
            clearLeftPress(pointerId);
            tryPluckAt(stringNo, x, bridgeX, pointerId);
        }
        return true;
    }

    private void setLeftPress(int pointerId, int stringNo, float x) {
        LeftPress press = leftPressByPointer.get(pointerId);
        if (press == null) {
            press = new LeftPress();
            leftPressByPointer.put(pointerId, press);
        }
        press.stringNo = stringNo;
        press.x = x;
    }

    private void clearLeftPress(int pointerId) {
        leftPressByPointer.remove(pointerId);
    }

    private boolean isStringLeftPressed(int stringNo) {
        for (int i = 0; i < leftPressByPointer.size(); i++) {
            if (leftPressByPointer.valueAt(i).stringNo == stringNo) {
                return true;
            }
        }
        return false;
    }

    private float pressDepthForPluckedString(int stringNo) {
        for (int i = 0; i < leftPressByPointer.size(); i++) {
            LeftPress p = leftPressByPointer.valueAt(i);
            if (p.stringNo != stringNo) {
                continue;
            }
            float bridgeX = bridgeXForString(stringNo);
            float pressLeft = body.left + body.width() * 0.07f;
            return clamp01((bridgeX - p.x) / (bridgeX - pressLeft));
        }
        return 0f;
    }

    private void updatePressOrPluck(float x, float y, int pointerId) {
        if (!body.contains((int) x, (int) y)) {
            return;
        }
        int stringNo = hitString(y);
        if (stringNo <= 0) {
            return;
        }
        float bridgeX = bridgeXForString(stringNo);
        if (x <= bridgeX) {
            setLeftPress(pointerId, stringNo, x);
        } else {
            clearLeftPress(pointerId);
            applyPluckAlongPointer(x, y, pointerId);
        }
    }

    private void applyPluckAlongPointer(float x, float y, int pointerId) {
        if (!body.contains((int) x, (int) y)) {
            return;
        }
        int stringNo = hitString(y);
        if (stringNo <= 0) {
            return;
        }
        float bridgeX = bridgeXForString(stringNo);
        if (x > bridgeX) {
            tryPluckAt(stringNo, x, bridgeX, pointerId);
        }
    }

    private void tryPluckAt(int stringNo, float x, float bridgeX, int pointerId) {
        float yueshanX = body.right - body.width() * 0.06f;
        float pluckRight = Math.min(yueshanX, body.right - 8);
        boolean handFreePluck = leftPressByPointer.size() > 0
                && leftPressByPointer.indexOfKey(pointerId) < 0;
        if (handFreePluck) {
            if (pluckFingerString.get(pointerId, -1) == stringNo) {
                return;
            }
            pluckFingerString.put(pointerId, stringNo);
        } else if (stringNo == pluckSoundedString) {
            return;
        }
        float pluckT = (x - bridgeX) / (pluckRight - bridgeX);
        triggerPluck(stringNo, pluckT);
        if (!handFreePluck) {
            pluckSoundedString = stringNo;
        }
    }

    private void clearAllTouchState() {
        leftPressByPointer.clear();
        pluckSoundedString = 0;
        pluckFingerString.clear();
        highlightString = 0;
    }

    private void triggerPluck(int stringNo, float pluckT) {
        pluckT = clamp01(pluckT);
        highlightString = stringNo;
        float leftDepth = pressDepthForPluckedString(stringNo);
        int pressString = leftDepth > 0.05f ? stringNo : 0;
        GuzhengSoundPlayer.getInstance().playPluck(stringNo, pluckT, pressString, leftDepth);
    }

    private int hitString(float touchY) {
        int best = 1;
        float bestDist = Float.MAX_VALUE;
        for (int i = 0; i < STRING_COUNT; i++) {
            float d = Math.abs(touchY - stringY[i]);
            if (d < bestDist) {
                bestDist = d;
                best = i + 1;
            }
        }
        float rowH = body.height() / (float) STRING_COUNT;
        return bestDist <= rowH * 0.48f ? best : 0;
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }
}
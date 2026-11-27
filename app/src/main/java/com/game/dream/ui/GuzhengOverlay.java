package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
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
    private final float[] stringY = new float[STRING_COUNT];

    private int leftHoldString;
    private float leftHoldX;
    private int leftPointerId = -1;
    /** 本轮在拨弦区已发声的弦，同指同弦按住不重复播 */
    private int pluckSoundedString;

    private int highlightString;

    public void show(Listener listener) {
        this.listener = listener;
        this.visible = true;
        leftHoldString = 0;
        leftHoldX = 0f;
        pluckSoundedString = 0;
        leftPointerId = -1;
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
        int marginH = 6;
        int marginV = 8;
        int pw = sw - marginH * 2;
        int ph = sh - marginV * 2;
        int px = marginH;
        int py = marginV;
        panel.set(px, py, px + pw, py + ph);
        int header = Math.max(72, (int) (ph * 0.11f));
        int footer = Math.max(24, (int) (ph * 0.04f));
        int side = Math.max(10, (int) (pw * 0.012f));
        body.set(panel.left + side, panel.top + header, panel.right - side, panel.bottom - footer);
        int closeW = Math.max(116, (int) (pw * 0.16f));
        int closeH = Math.max(44, (int) (header * 0.62f));
        closeButton.set(panel.right - closeW - side, panel.top + 10,
                panel.right - side, panel.top + 10 + closeH);

        float topPad = Math.max(28f, body.height() * 0.06f);
        float bottomPad = Math.max(14f, body.height() * 0.03f);
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

        paint.setColor(Color.argb(175, 8, 10, 16));
        canvas.drawRect(0, 0, sw, sh, paint);

        paint.setShader(new LinearGradient(panel.left, panel.top, panel.right, panel.bottom,
                Color.argb(252, 32, 28, 36),
                Color.argb(252, 18, 22, 30),
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 18, 18, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(195, 140, 85));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 18, 18, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(255, 210, 170));
        float titleSize = Math.max(30f, Math.min(40f, panel.width() * 0.042f));
        paint.setTextSize(titleSize);
        canvas.drawText("古筝 · 试弹一曲", panel.centerX(), panel.top + titleSize + 8, paint);

        paint.setColor(Color.rgb(170, 168, 160));
        paint.setTextSize(Math.max(17f, titleSize * 0.58f));
        canvas.drawText("二十一根弦 · 每弦空弦一音（按弦变音）· 雁柱左按右拨",
                panel.centerX(), panel.top + titleSize + 36, paint);

        drawClose(canvas, paint);
        drawZitherBody(canvas, paint);
        drawStrings(canvas, paint);

        if (highlightString > 0) {
            GuzhengSoundPlayer gp = GuzhengSoundPlayer.getInstance();
            paint.setColor(Color.argb(230, 255, 200, 120));
            paint.setTextSize(Math.max(22f, panel.height() * 0.032f));
            canvas.drawText(gp.formatStringLabel(highlightString) + "（空弦）",
                    panel.centerX(), panel.bottom - Math.max(6f, panel.height() * 0.012f), paint);
        }
    }

    private void drawClose(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(200, 90, 55, 75));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom,
                8, 8, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(24f, closeButton.height() * 0.52f));
        canvas.drawText("收起", closeButton.centerX(), closeButton.centerY() + closeButton.height() * 0.18f, paint);
    }

    private void drawZitherBody(Canvas canvas, Paint paint) {
        paint.setColor(Color.rgb(88, 52, 30));
        canvas.drawRoundRect(body.left, body.top, body.right, body.bottom, 12, 12, paint);

        float yueshanX = body.right - body.width() * 0.06f;
        paint.setColor(Color.rgb(200, 175, 120));
        paint.setStrokeWidth(5);
        canvas.drawLine(yueshanX, body.top + 4, yueshanX, body.bottom - 4, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(50, 36, 24));
        canvas.drawRoundRect(body.left, body.top, body.left + body.width() * 0.05f,
                body.bottom, 4, 4, paint);

        paint.setColor(Color.rgb(40, 28, 18));
        canvas.drawRect(body.left, body.bottom - 22, body.right, body.bottom, paint);

        paint.setTextSize(17);
        paint.setColor(Color.argb(180, 255, 235, 200));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("筝首", body.left + body.width() * 0.05f, body.top + 22, paint);
        canvas.drawText("岳山·右手拨弦", yueshanX - body.width() * 0.14f, body.top + 22, paint);
    }

    private void drawStrings(Canvas canvas, Paint paint) {
        float yueshanX = body.right - body.width() * 0.06f;
        float stringLeft = body.left + body.width() * 0.07f;
        float stringRight = yueshanX;

        for (int i = 0; i < STRING_COUNT; i++) {
            int no = i + 1;
            float y = stringY[i];
            float bridgeX = bridgeXForString(no);
            boolean pressed = leftHoldString == no;
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
            paint.setColor(Color.rgb(235, 210, 150));
            float bh = Math.max(6f, body.height() / STRING_COUNT * 0.28f);
            float bw = Math.max(5f, bh * 0.7f);
            canvas.drawRect(bridgeX - bw / 2f, y - bh, bridgeX + bw / 2f, y + bh, paint);
            paint.setColor(Color.rgb(120, 95, 60));
            paint.setStrokeWidth(1.2f);
            canvas.drawLine(bridgeX - bw, y, bridgeX + bw, y, paint);

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
            if (pointerId == leftPointerId) {
                leftPointerId = -1;
                leftHoldString = 0;
                leftHoldX = 0f;
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                int pid = event.getPointerId(i);
                if (pid != leftPointerId) {
                    continue;
                }
                applyPressMove(event.getX(i), event.getY(i));
            }
            if (event.getPointerCount() == 1) {
                applyPluckMove(event.getX(0), event.getY(0));
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
            leftPointerId = pointerId;
            leftHoldString = stringNo;
            leftHoldX = x;
        }
        if (inPluck) {
            tryPluckAt(stringNo, x, bridgeX, pointerId, true);
        }
        return true;
    }

    private void applyPressMove(float x, float y) {
        if (!body.contains((int) x, (int) y)) {
            return;
        }
        int stringNo = hitString(y);
        if (stringNo <= 0) {
            return;
        }
        float bridgeX = bridgeXForString(stringNo);
        if (x <= bridgeX) {
            leftHoldString = stringNo;
            leftHoldX = x;
        }
    }

    private void applyPluckMove(float x, float y) {
        if (!body.contains((int) x, (int) y)) {
            return;
        }
        int stringNo = hitString(y);
        if (stringNo <= 0) {
            return;
        }
        float bridgeX = bridgeXForString(stringNo);
        if (x > bridgeX) {
            tryPluckAt(stringNo, x, bridgeX, -1, false);
        }
    }

    private void tryPluckAt(int stringNo, float x, float bridgeX, int pointerId, boolean onPointerDown) {
        float yueshanX = body.right - body.width() * 0.06f;
        float pluckRight = Math.min(yueshanX, body.right - 8);
        boolean secondFingerPluck = onPointerDown && pointerId != leftPointerId && leftPointerId >= 0;
        if (!secondFingerPluck && stringNo == pluckSoundedString) {
            return;
        }
        float pluckT = (x - bridgeX) / (pluckRight - bridgeX);
        triggerPluck(stringNo, pluckT);
        if (!secondFingerPluck) {
            pluckSoundedString = stringNo;
        }
    }

    private void clearAllTouchState() {
        leftHoldString = 0;
        leftHoldX = 0f;
        leftPointerId = -1;
        pluckSoundedString = 0;
        highlightString = 0;
    }

    private void triggerPluck(int stringNo, float pluckT) {
        pluckT = clamp01(pluckT);
        highlightString = stringNo;
        float leftDepth = 0f;
        if (leftHoldString == stringNo) {
            float bridgeX = bridgeXForString(stringNo);
            float pressLeft = body.left + body.width() * 0.07f;
            leftDepth = clamp01((bridgeX - leftHoldX) / (bridgeX - pressLeft));
        }
        GuzhengSoundPlayer.getInstance().playPluck(stringNo, pluckT, leftHoldString, leftDepth);
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
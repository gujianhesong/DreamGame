package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.game.dream.GameEngine;
import com.game.dream.system.RingTossSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 套圈：左侧拉弓抛圈，右侧上远下近柱靶。
 */
public class RingTossOverlay {

    private static final float LEFT_ZONE_RATIO = 0.28f;
    /** 按 60fps 归一化的重力；实际每帧乘 dtScale */
    private static final float GRAVITY = 0.82f;
    private static final float SPEED_X = 50f;
    private static final float SPEED_Y = 42f;
    private static final float MIN_POWER = 0.38f;
    private static final long RING_ON_GROUND_MS = 900;
    private static final long MAX_FLIGHT_MS = 6000;

    private boolean visible;
    private final android.graphics.Rect panel = new android.graphics.Rect();
    private final android.graphics.Rect body = new android.graphics.Rect();
    private final android.graphics.Rect closeButton = new android.graphics.Rect();
    private final android.graphics.Rect leftZone = new android.graphics.Rect();
    private final android.graphics.Rect prizeZone = new android.graphics.Rect();
    private final android.graphics.Rect titleBar = new android.graphics.Rect();

    private int ringsLeft;
    private int roundHits;
    private int roundSilver;
    private int roundFarHits;

    private float anchorX;
    private float anchorY;
    private boolean aiming;
    private float pullX;
    private float pullY;
    private float maxPull;

    private boolean flying;
    /** 落地后仍绘制一小段时间，避免“半空一闪没” */
    private boolean ringLandedVisible;
    private long ringHideAtMs;
    private float ringX;
    private float ringY;
    private float ringVx;
    private float ringVy;
    private float ringDisplayR;
    private long flightStartMs;
    private long lastPhysicsMs;

    private boolean showSettlement;
    private String settlementText;

    private final List<Target> targets = new ArrayList<>();

    private static final class Target {
        String name;
        int silver;
        boolean farTier;
        int tier;
        boolean claimed;
        float x;
        float y;
        float poleH;
        float hitR;
        float drawScale;
    }

    public void show() {
        visible = true;
        ringsLeft = RingTossSystem.RINGS_PER_ROUND;
        roundHits = 0;
        roundSilver = 0;
        roundFarHits = 0;
        aiming = false;
        flying = false;
        ringLandedVisible = false;
        lastPhysicsMs = 0;
        showSettlement = false;
        layoutAll();
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
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
        int headerBottom = panel.top + 88;
        body.set(panel.left + 8, headerBottom, panel.right - 8, panel.bottom - 32);

        leftZone.set(body.left, body.top, body.left + (int) (body.width() * LEFT_ZONE_RATIO), body.bottom);
        prizeZone.set(leftZone.right, body.top, body.right, body.bottom);

        anchorX = leftZone.left + leftZone.width() * 0.55f;
        anchorY = body.centerY();
        maxPull = leftZone.width() * 0.85f;
        ringDisplayR = Math.max(14f, body.height() * 0.028f);

        layoutTargets();
    }

    private void layoutTargets() {
        targets.clear();
        final String[][] names = {
                {"青瓷小瓶", "玉树挂坠", "银锞摆件"},
                {"绣球", "竹编小马", "檀木扇"},
                {"布老虎", "陶偶", "糖果包"}
        };
        final int[][] silvers = {
                {500, 450, 400},
                {320, 260, 220},
                {180, 140, 100}
        };
        float marginX = Math.max(20f, prizeZone.width() * 0.08f);
        float colStep = (prizeZone.width() - 2f * marginX) / 3f;
        float labelReserve = Math.max(58f, body.height() * 0.18f);
        float maxPoleBottom = body.bottom - labelReserve;

        float[] rowCenterY = {
                body.top + body.height() * 0.28f,
                body.top + body.height() * 0.48f,
                body.top + body.height() * 0.66f
        };

        float uniformPoleH = Math.min(36f, body.height() * 0.055f);
        float uniformHitR = ringDisplayR * 0.96f;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Target t = new Target();
                t.name = names[row][col];
                t.silver = silvers[row][col];
                t.farTier = row == 0;
                t.tier = row;
                t.claimed = false;
                t.x = prizeZone.left + marginX + colStep * (col + 0.5f);
                t.drawScale = 1f;
                t.poleH = uniformPoleH;
                t.hitR = uniformHitR;
                float top = rowCenterY[row] - t.poleH * 0.45f;
                if (top + t.poleH + labelReserve > body.bottom) {
                    top = maxPoleBottom - t.poleH;
                }
                top = Math.max(body.top + 8f, top);
                t.y = top;
                targets.add(t);
            }
        }
    }

    public void update() {
        if (!visible) {
            return;
        }
        long now = System.currentTimeMillis();
        if (ringLandedVisible && now >= ringHideAtMs) {
            ringLandedVisible = false;
        }
        if (!flying) {
            return;
        }
        if (lastPhysicsMs == 0) {
            lastPhysicsMs = now;
        }
        float dtMs = Math.min(48f, now - lastPhysicsMs);
        lastPhysicsMs = now;
        float dt = dtMs / 16.666f;

        int substeps = Math.max(1, (int) Math.ceil(dt));
        float subDt = dt / substeps;
        for (int i = 0; i < substeps; i++) {
            ringX += ringVx * subDt;
            ringY += ringVy * subDt;
            ringVy += GRAVITY * subDt;

            Target midHit = findHitTarget(ringX, ringY);
            if (midHit != null) {
                ringX = midHit.x;
                ringY = prizeTopY(midHit);
                landRing();
                return;
            }

            float ground = groundY();
            boolean hitGround = ringY + ringDisplayR * 0.35f >= ground;
            if (hitGround) {
                ringY = ground - ringDisplayR * 0.35f;
                landRing();
                return;
            }
        }

        long elapsed = now - flightStartMs;
        if (elapsed > MAX_FLIGHT_MS) {
            // 超长飞行：加速下坠直到着地，不在半空强制收圈
            ringVy += GRAVITY * 2.5f;
        }
    }

    private float groundY() {
        return body.bottom - Math.max(14f, body.height() * 0.035f);
    }

    private void landRing() {
        flying = false;
        ringLandedVisible = true;
        ringHideAtMs = System.currentTimeMillis() + RING_ON_GROUND_MS;
        Target hit = findHitTarget(ringX, ringY);
        if (hit != null) {
            roundHits++;
            roundSilver += hit.silver;
            RingTossSystem.getInstance().awardSilver(hit.silver);
            if (hit.farTier) {
                roundFarHits++;
            }
            hit.claimed = true;
            GameEngine.getInstance().showCenterToast(
                    "套中「" + hit.name + "」+" + hit.silver + " 银", 1600);
        }
        if (ringsLeft <= 0) {
            finishRound();
        }
    }

    private float prizeTopY(Target t) {
        return t.y - ringDisplayR * 0.2f;
    }

    private Target findHitTarget(float x, float y) {
        Target best = null;
        float bestD = Float.MAX_VALUE;
        for (Target t : targets) {
            if (t.claimed) {
                continue;
            }
            float dx = x - t.x;
            float dy = y - prizeTopY(t);
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < t.hitR && d < bestD) {
                bestD = d;
                best = t;
            }
        }
        return best;
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
        paint.setColor(Color.rgb(52, 40, 32));
        canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 16, 16, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.rgb(178, 138, 72));
        canvas.drawRoundRect(panel.left + 2, panel.top + 2, panel.right - 2, panel.bottom - 2, 14, 14, paint);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.argb(120, 220, 190, 120));
        canvas.drawRoundRect(panel.left + 6, panel.top + 6, panel.right - 6, panel.bottom - 6, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        drawAwning(canvas, paint);
        drawClose(canvas, paint);
        drawHeader(canvas, paint);

        canvas.save();
        canvas.clipRect(body);
        drawZones(canvas, paint);
        drawTargets(canvas, paint);
        drawLauncher(canvas, paint);
        if (flying || ringLandedVisible || (aiming && ringsLeft > 0)) {
            float rcx = (flying || ringLandedVisible) ? ringX : pullX;
            float rcy = (flying || ringLandedVisible) ? ringY : pullY;
            drawRing(canvas, paint, rcx, rcy);
        }
        canvas.restore();

        if (showSettlement && settlementText != null) {
            drawSettlement(canvas, paint);
        }
    }

    private void drawAwning(Canvas canvas, Paint paint) {
        int awBottom = panel.top + 84;
        int stripes = 7;
        float stripeH = (awBottom - panel.top) / (float) stripes;
        for (int i = 0; i < stripes; i++) {
            paint.setColor(i % 2 == 0 ? Color.rgb(140, 38, 38) : Color.rgb(168, 52, 48));
            canvas.drawRect(panel.left + 8, panel.top + 8 + i * stripeH,
                    panel.right - 8, panel.top + 8 + (i + 1) * stripeH, paint);
        }
        paint.setColor(Color.rgb(88, 62, 38));
        canvas.drawRect(panel.left + 8, awBottom - 3, panel.right - 8, awBottom + 5, paint);
    }

    private void drawHeader(Canvas canvas, Paint paint) {
        float titleCx = titleBar.centerX();
        float titleCy = titleBar.top + titleBar.height() * 0.68f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 232, 175));
        paint.setTextSize(fitTextSize(paint, "南市套圈",
                titleBar.width() - 4f, 28f, 38f));
        paint.setShadowLayer(6, 0, 2, Color.argb(180, 40, 20, 10));
        canvas.drawText("南市套圈", titleCx, titleCy, paint);
        paint.clearShadowLayer();

        paint.setColor(Color.argb(90, 255, 220, 150));
        canvas.drawRect(panel.left + 20, panel.top + 84, panel.right - 20, panel.top + 86, paint);

        float chipY = panel.top + 76;
        drawStatChip(canvas, paint, panel.left + panel.width() * 0.28f, chipY,
                "剩 " + ringsLeft + " 圈");
        drawStatChip(canvas, paint, panel.right - panel.width() * 0.28f, chipY,
                "已得 " + roundSilver + " 银");
    }

    private void drawStatChip(Canvas canvas, Paint paint, float cx, float cy, String text) {
        paint.setTextSize(23f);
        float tw = paint.measureText(text);
        float pad = 16f;
        float l = cx - tw / 2f - pad;
        float r = cx + tw / 2f + pad;
        float t = cy - 22;
        float b = cy + 12;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(210, 68, 50, 38));
        canvas.drawRoundRect(l, t, r, b, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.rgb(196, 154, 88));
        canvas.drawRoundRect(l, t, r, b, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 240, 210));
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

    private void drawZones(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(118, 98, 72));
        canvas.drawRect(leftZone, paint);
        paint.setColor(Color.argb(35, 30, 20, 15));
        float lx = leftZone.left + 12;
        while (lx < leftZone.right) {
            canvas.drawLine(lx, leftZone.top, lx, leftZone.bottom, paint);
            lx += 18;
        }
        paint.setColor(Color.rgb(142, 118, 88));
        canvas.drawRect(prizeZone, paint);
        paint.setColor(Color.argb(40, 90, 70, 50));
        float gy = body.bottom - Math.max(14f, body.height() * 0.04f);
        canvas.drawRect(prizeZone.left, gy, prizeZone.right, body.bottom, paint);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.argb(60, 70, 50, 35));
        for (float gx = prizeZone.left; gx < prizeZone.right; gx += 22) {
            canvas.drawLine(gx, gy, gx + 12, body.bottom, paint);
        }

        int divX = leftZone.right;
        paint.setColor(Color.rgb(96, 68, 44));
        canvas.drawRect(divX - 3, body.top, divX + 3, body.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(100, 220, 190, 120));
        canvas.drawRect(body.left + 1, body.top + 1, body.right - 1, body.bottom - 1, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTextSize(26);
        paint.setColor(Color.rgb(255, 244, 220));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("挽弓抛圈", leftZone.centerX(), body.top + 34, paint);
        paint.setTextSize(20);
        paint.setColor(Color.argb(200, 255, 230, 190));
        canvas.drawText("向右侧虚线圈套", leftZone.centerX(), body.top + 60, paint);
    }

    private void drawTargets(Canvas canvas, Paint paint) {
        for (Target t : targets) {
            if (t.claimed) {
                drawClaimedSlot(canvas, paint, t);
                continue;
            }
            drawPrizeFigure(canvas, paint, t);
        }
    }

    private void drawClaimedSlot(Canvas canvas, Paint paint, Target t) {
        float cy = prizeTopY(t) + t.poleH * 0.35f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 60, 48, 38));
        canvas.drawCircle(t.x, cy, t.hitR * 0.85f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.argb(120, 140, 120, 100));
        canvas.drawCircle(t.x, cy, t.hitR * 0.85f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 120, 100, 85));
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("已兑", t.x, cy + 7, paint);
    }

    private void drawPrizeFigure(Canvas canvas, Paint paint, Target t) {
        TierPalette pal = tierPalette(t.tier);
        float cx = t.x;
        float cy = prizeTopY(t);
        float bodyW = t.hitR * 1.15f;
        float bodyH = t.hitR * 1.35f;
        float poleW = 7f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 0, 0, 0));
        canvas.drawOval(cx - bodyW * 0.45f + 2, cy - bodyH * 0.35f + 3,
                cx + bodyW * 0.55f + 2, cy + bodyH * 0.45f + 3, paint);

        float poleTop = t.y + t.poleH - Math.min(t.poleH, 20f);
        paint.setColor(Color.rgb(100, 68, 42));
        canvas.drawRect(cx - poleW / 2, poleTop, cx + poleW / 2, t.y + t.poleH, paint);

        RectF jar = new RectF(cx - bodyW * 0.5f, cy - bodyH * 0.4f,
                cx + bodyW * 0.5f, cy + bodyH * 0.42f);
        paint.setColor(pal.bodyFill);
        canvas.drawOval(jar, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(pal.bodyStroke);
        canvas.drawOval(jar, paint);
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(cx - bodyW * 0.32f, cy - bodyH * 0.42f,
                cx + bodyW * 0.32f, cy - bodyH * 0.42f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(pal.neck);
        canvas.drawRect(cx - bodyW * 0.18f, cy - bodyH * 0.52f,
                cx + bodyW * 0.18f, cy - bodyH * 0.38f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(150, 255, 245, 210));
        canvas.drawCircle(cx, cy, t.hitR, paint);

        float badgeR = 13f;
        float bx = cx + bodyW * 0.38f;
        float by = cy - bodyH * 0.48f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(pal.badgeFill);
        canvas.drawCircle(bx, by, badgeR, paint);
        paint.setColor(pal.badgeText);
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(pal.badgeLabel, bx, by + 5f, paint);

        float plaqueTop = Math.min(t.y + t.poleH + 8, body.bottom - 52);
        paint.setTextSize(23);
        float silverW = paint.measureText(t.silver + " 银");
        paint.setTextSize(20);
        String name = t.name != null ? t.name : "";
        float nameW = paint.measureText(name);
        float textW = Math.max(silverW, nameW);
        float colCap = prizeZone.width() / 3f - 14f;
        float plaqueW = Math.min(colCap, Math.max(70f, textW + 20f));
        paint.setColor(pal.plaqueFill);
        canvas.drawRoundRect(cx - plaqueW / 2, plaqueTop, cx + plaqueW / 2,
                plaqueTop + 42, 6, 6, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(pal.plaqueStroke);
        canvas.drawRoundRect(cx - plaqueW / 2, plaqueTop, cx + plaqueW / 2,
                plaqueTop + 42, 6, 6, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(pal.silverText);
        paint.setTextSize(23);
        canvas.drawText(t.silver + " 银", cx, plaqueTop + 18, paint);
        paint.setColor(pal.nameText);
        paint.setTextSize(20);
        canvas.drawText(ellipsize(paint, name, plaqueW - 12), cx, plaqueTop + 36, paint);
    }

    private static final class TierPalette {
        final int bodyFill;
        final int bodyStroke;
        final int neck;
        final int badgeFill;
        final int badgeText;
        final String badgeLabel;
        final int plaqueFill;
        final int plaqueStroke;
        final int silverText;
        final int nameText;

        TierPalette(int bodyFill, int bodyStroke, int neck, int badgeFill, int badgeText,
                    String badgeLabel, int plaqueFill, int plaqueStroke, int silverText, int nameText) {
            this.bodyFill = bodyFill;
            this.bodyStroke = bodyStroke;
            this.neck = neck;
            this.badgeFill = badgeFill;
            this.badgeText = badgeText;
            this.badgeLabel = badgeLabel;
            this.plaqueFill = plaqueFill;
            this.plaqueStroke = plaqueStroke;
            this.silverText = silverText;
            this.nameText = nameText;
        }
    }

    private static TierPalette tierPalette(int tier) {
        switch (tier) {
            case 0:
                return new TierPalette(
                        Color.rgb(214, 178, 92),
                        Color.rgb(160, 118, 48),
                        Color.rgb(190, 150, 78),
                        Color.rgb(130, 40, 32),
                        Color.rgb(255, 230, 150),
                        "珍",
                        Color.rgb(78, 58, 36),
                        Color.rgb(196, 154, 88),
                        Color.rgb(255, 232, 140),
                        Color.rgb(255, 248, 228));
            case 1:
                return new TierPalette(
                        Color.rgb(178, 88, 72),
                        Color.rgb(120, 52, 42),
                        Color.rgb(150, 72, 58),
                        Color.rgb(92, 48, 40),
                        Color.rgb(255, 220, 200),
                        "品",
                        Color.rgb(70, 48, 38),
                        Color.rgb(168, 108, 78),
                        Color.rgb(255, 210, 170),
                        Color.rgb(255, 242, 230));
            default:
                return new TierPalette(
                        Color.rgb(118, 152, 128),
                        Color.rgb(72, 98, 82),
                        Color.rgb(98, 128, 108),
                        Color.rgb(56, 72, 62),
                        Color.rgb(220, 245, 230),
                        "常",
                        Color.rgb(62, 56, 46),
                        Color.rgb(130, 118, 96),
                        Color.rgb(210, 235, 210),
                        Color.rgb(242, 250, 242));
        }
    }

    private void drawSettlement(Canvas canvas, Paint paint) {
        float w = panel.width() * 0.62f;
        float l = panel.centerX() - w / 2f;
        float r = panel.centerX() + w / 2f;
        float t = panel.centerY() - 95;
        float b = panel.centerY() + 115;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 48, 38, 30));
        canvas.drawRoundRect(l, t, r, b, 14, 14, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(196, 154, 88));
        canvas.drawRoundRect(l, t, r, b, 14, 14, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 228, 175));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(34);
        canvas.drawText("本局收摊", panel.centerX(), panel.centerY() - 52, paint);
        paint.setTextSize(28);
        paint.setColor(Color.rgb(255, 246, 228));
        canvas.drawText(settlementText, panel.centerX(), panel.centerY() + 8, paint);
        paint.setTextSize(24);
        paint.setColor(Color.argb(220, 200, 180, 150));
        canvas.drawText("点此空白关闭", panel.centerX(), panel.centerY() + 62, paint);
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

    private static String ellipsize(Paint paint, String text, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (paint.measureText(text) <= maxWidth) {
            return text;
        }
        String tail = "…";
        for (int end = text.length(); end > 0; end--) {
            String part = text.substring(0, end) + tail;
            if (paint.measureText(part) <= maxWidth) {
                return part;
            }
        }
        return tail;
    }

    private void drawLauncher(Canvas canvas, Paint paint) {
        float footY = anchorY + 28;
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(108, 78, 52));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(anchorX - 42, anchorY - 52, anchorX + 8, anchorY + 28),
                200, 125, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(140, 100, 62));
        canvas.drawCircle(anchorX, anchorY, 7, paint);
        canvas.drawRect(leftZone.left + 16, footY, leftZone.right - 16, footY + 6, paint);

        float stackY = Math.max(body.top + 72f, anchorY - 52);
        int shown = Math.min(ringsLeft, 10);
        float startX = leftZone.centerX() - (shown - 1) * 7f;
        for (int i = 0; i < shown; i++) {
            float rcx = startX + i * 14f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.5f);
            paint.setColor(Color.rgb(210, 168, 72));
            canvas.drawCircle(rcx, stackY, 7, paint);
        }

        if (aiming && !flying) {
            paint.setColor(Color.argb(90, 255, 240, 160));
            paint.setStrokeWidth(10f);
            canvas.drawLine(anchorX, anchorY, pullX, pullY, paint);
            paint.setColor(Color.argb(200, 255, 235, 140));
            paint.setStrokeWidth(2.5f);
            canvas.drawLine(anchorX, anchorY, pullX, pullY, paint);
            float pull = (float) Math.hypot(anchorX - pullX, anchorY - pullY);
            float power = Math.min(1f, pull / Math.max(1f, maxPull));
            paint.setStyle(Paint.Style.FILL);
            float barW = leftZone.width() * 0.7f;
            float barL = leftZone.centerX() - barW / 2f;
            float barY = footY + 22;
            paint.setColor(Color.argb(140, 40, 32, 24));
            canvas.drawRoundRect(barL, barY, barL + barW, barY + 10, 5, 5, paint);
            paint.setColor(Color.rgb(200, 120, 60));
            canvas.drawRoundRect(barL, barY, barL + barW * power, barY + 10, 5, 5, paint);
        }
    }

    private void drawRing(Canvas canvas, Paint paint, float cx, float cy) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(4f, ringDisplayR * 0.3f));
        paint.setColor(Color.argb(120, 60, 40, 20));
        canvas.drawCircle(cx, cy, ringDisplayR + 2, paint);
        paint.setColor(Color.rgb(218, 178, 88));
        paint.setStrokeWidth(Math.max(3f, ringDisplayR * 0.24f));
        canvas.drawCircle(cx, cy, ringDisplayR, paint);
        paint.setStrokeWidth(Math.max(1.5f, ringDisplayR * 0.12f));
        paint.setColor(Color.rgb(255, 236, 188));
        canvas.drawCircle(cx, cy, ringDisplayR * 0.72f, paint);
    }

    public boolean handleTouch(MotionEvent event) {
        if (!visible) {
            return false;
        }
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN && TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }
        if (showSettlement) {
            if (action == MotionEvent.ACTION_DOWN) {
                hide();
            }
            return true;
        }
        if (flying || ringLandedVisible) {
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            if (leftZone.contains((int) x, (int) y) && ringsLeft > 0) {
                aiming = true;
                pullX = x;
                pullY = y;
                return true;
            }
            return true;
        }
        if (aiming && action == MotionEvent.ACTION_MOVE) {
            pullX = x;
            pullY = y;
            return true;
        }
        if (aiming && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
            float dx = anchorX - pullX;
            float dy = anchorY - pullY;
            float pull = (float) Math.sqrt(dx * dx + dy * dy);
            if (pull < 12f) {
                aiming = false;
                return true;
            }
            pull = Math.min(pull, maxPull);
            float power = Math.max(MIN_POWER, pull / maxPull);
            float nx = dx / pull;
            float ny = dy / pull;
            ringVx = nx * power * SPEED_X;
            ringVy = ny * power * SPEED_Y - 5f;
            ringX = anchorX;
            ringY = anchorY;
            ringDisplayR = Math.max(14f, body.height() * 0.028f);
            ringsLeft--;
            aiming = false;
            flying = true;
            ringLandedVisible = false;
            flightStartMs = System.currentTimeMillis();
            lastPhysicsMs = flightStartMs;
            if (ringsLeft == 0) {
                // last ring — round ends after lands via landRing
            }
            return true;
        }
        return true;
    }

    private void finishRound() {
        if (showSettlement) {
            return;
        }
        showSettlement = true;
        int net = roundSilver - RingTossSystem.ENTRY_FEE;
        settlementText = "命中 " + roundHits + " 次，共得 " + roundSilver + " 银"
                + (net >= 0 ? "（净 +" + net + "）" : "（净 " + net + "）");
        RingTossSystem.getInstance().onRoundFinished(roundHits, roundSilver, roundFarHits);
    }
}
package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.game.dream.GameEngine;
import com.game.dream.bean.RoleInfo;
import com.game.dream.system.GamblingSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.TitleSystem;
import com.game.dream.utils.TouchUtil;

/**
 * 如意赌坊全屏：选筹 → 押注 → 摇盅 → 结算。
 */
public class GamblingDiceOverlay {

  private enum Phase {
    BLOCKED,
    STAKE_SELECT,
    BET_SIDE,
    ROLLING,
    RESULT
  }

  private static final long ROLL_MS = 1900;
  private static final long FLICKER_MS = 72;

  private boolean visible;
  private Phase phase = Phase.STAKE_SELECT;

  private String dealerName;
  private int stake;
  private boolean betBig;
  private int finalDie1;
  private int finalDie2;
  private long rollStartMs;
  private long lastFlickerMs;
  private boolean rollRevealed;
  private int rollingFace1 = 1;
  private int rollingFace2 = 1;

  private String resultHeadline;
  private String resultDetail;
  private int lastNet;
  private String tableHint;

  private final Rect panel = new Rect();
  private final Rect body = new Rect();
  private final Rect titleBar = new Rect();
  private final Rect closeButton = new Rect();

  private final Rect btnStakeLeisure = new Rect();
  private final Rect btnStakeNormal = new Rect();
  private final Rect btnStakeHigh = new Rect();
  private final Rect btnLeave = new Rect();
  private final Rect btnBetBig = new Rect();
  private final Rect btnBetSmall = new Rect();
  private final Rect btnChangeStake = new Rect();
  private final Rect btnAgain = new Rect();
  private final Rect btnChangeAfter = new Rect();
  private final Rect btnDone = new Rect();
  private final Rect btnBlockedFarewell = new Rect();

  public void show(String dealerName) {
    this.dealerName = dealerName != null ? dealerName : "庄家";
    visible = true;
    GamblingSystem g = GamblingSystem.getInstance();
    if (g.isDailyLossCapped()) {
      phase = Phase.BLOCKED;
    } else {
      phase = Phase.STAKE_SELECT;
    }
    stake = 0;
    tableHint = null;
    layoutAll();
  }

  public void hide() {
    visible = false;
    phase = Phase.STAKE_SELECT;
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
    int headerBottom = panel.top + 112;
    body.set(panel.left + 8, headerBottom, panel.right - 8, panel.bottom - 24);

    int gap = 10;
    int footTop = panel.bottom - 90;
    int btnH = 56;
    int stakeCols = 4;
    int colW = (body.width() - gap * (stakeCols - 1)) / stakeCols;
    int x = body.left;
    btnStakeLeisure.set(x, footTop, x + colW, footTop + btnH);
    x += colW + gap;
    btnStakeNormal.set(x, footTop, x + colW, footTop + btnH);
    x += colW + gap;
    btnStakeHigh.set(x, footTop, x + colW, footTop + btnH);
    x += colW + gap;
    btnLeave.set(x, footTop, x + colW, footTop + btnH);

    int betGap = 28;
    int betBtnW = Math.min(240, Math.max(156, body.width() / 3));
    int betBtnH = btnH + 10;
    int betPairW = betBtnW * 2 + betGap;
    int betLeft = body.centerX() - betPairW / 2;
    int betY = body.centerY() + body.height() / 10;
    btnBetBig.set(betLeft, betY, betLeft + betBtnW, betY + betBtnH);
    btnBetSmall.set(betLeft + betBtnW + betGap, betY, betLeft + betPairW, betY + betBtnH);
    btnChangeStake.set(body.left + body.width() / 4, footTop,
        body.right - body.width() / 4, footTop + btnH);

    int barW = (body.width() - gap * 2) / 3;
    btnAgain.set(body.left, footTop, body.left + barW, footTop + btnH);
    btnChangeAfter.set(body.left + barW + gap, footTop,
        body.left + 2 * barW + gap, footTop + btnH);
    btnDone.set(body.left + 2 * (barW + gap), footTop, body.right, footTop + btnH);
    int cy = body.centerY() + 48;
    int halfW = body.width() / 3;
    btnBlockedFarewell.set(body.centerX() - halfW / 2, cy, body.centerX() + halfW / 2, cy + btnH);
  }

  public void update() {
    if (!visible || phase != Phase.ROLLING || rollRevealed) {
      return;
    }
    long now = System.currentTimeMillis();
    long elapsed = now - rollStartMs;
    if (elapsed >= ROLL_MS) {
      rollRevealed = true;
      settleRoll();
      phase = Phase.RESULT;
    } else if (now - lastFlickerMs >= FLICKER_MS) {
      rollingFace1 = 1 + (int) (Math.random() * 6);
      rollingFace2 = 1 + (int) (Math.random() * 6);
      lastFlickerMs = now;
    }
  }

  private void settleRoll() {
    GamblingSystem gambling = GamblingSystem.getInstance();
    int sum = finalDie1 + finalDie2;
    if (sum == 7) {
      lastNet = -stake;
      gambling.recordNetResult(lastNet);
      TitleSystem.getInstance().onGambleBet(betBig, sum, false, lastNet, stake);
      resultHeadline = "七点通杀";
      resultDetail = "恰好七点，庄家通吃。净 " + lastNet + " 银";
    } else {
      boolean win = betBig ? sum >= 8 : sum <= 6;
      if (win) {
        lastNet = stake;
        gambling.depositWinnings(stake * 2L);
        gambling.recordNetResult(lastNet);
        TitleSystem.getInstance().onGambleBet(betBig, sum, true, lastNet, stake);
        resultHeadline = "押中了";
        resultDetail = "净赢 " + stake + " 银，" + (stake * 2) + " 银已入袋";
      } else {
        lastNet = -stake;
        gambling.recordNetResult(lastNet);
        TitleSystem.getInstance().onGambleBet(betBig, sum, false, lastNet, stake);
        resultHeadline = "未中";
        resultDetail = "手气差点，输掉 " + stake + " 银";
      }
    }
    TitleSystem.getInstance().onGambleComeback(gambling.getDailyNetSilver());
    if (gambling.isDailyLossCapped()) {
      tableHint = "今日输额已满，请明日再来";
    }
  }

  private void beginRoll() {
    finalDie1 = 1 + (int) (Math.random() * 6);
    finalDie2 = 1 + (int) (Math.random() * 6);
    rollStartMs = System.currentTimeMillis();
    lastFlickerMs = rollStartMs;
    rollRevealed = false;
    rollingFace1 = 1 + (int) (Math.random() * 6);
    rollingFace2 = 1 + (int) (Math.random() * 6);
    phase = Phase.ROLLING;
    tableHint = null;
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
    paint.setColor(Color.rgb(44, 38, 32));
    canvas.drawRoundRect(panel.left, panel.top, panel.right, panel.bottom, 16, 16, paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(4);
    paint.setColor(Color.rgb(178, 138, 72));
    canvas.drawRoundRect(panel.left + 2, panel.top + 2, panel.right - 2, panel.bottom - 2, 14, 14, paint);
    paint.setStrokeWidth(1.5f);
    paint.setColor(Color.argb(120, 220, 190, 120));
    canvas.drawRoundRect(panel.left + 6, panel.top + 6, panel.right - 6, panel.bottom - 6, 12, 12, paint);
    paint.setStyle(Paint.Style.FILL);

    drawGreenGoldValance(canvas, paint);
    drawHeader(canvas, paint);
    if (phase != Phase.ROLLING) {
      drawClose(canvas, paint);
    }

    drawTable(canvas, paint);
    drawRulesLine(canvas, paint);
    if (tableHint != null) {
      drawTableHint(canvas, paint, tableHint);
    }

    switch (phase) {
      case BLOCKED:
        drawBlocked(canvas, paint);
        break;
      case STAKE_SELECT:
        drawStakeButtons(canvas, paint);
        break;
      case BET_SIDE:
        drawBetButtons(canvas, paint);
        break;
      case ROLLING:
      case RESULT:
        drawDiceScene(canvas, paint);
        break;
      default:
        break;
    }

    if (phase == Phase.RESULT) {
      drawResultPlaque(canvas, paint);
      drawResultButtons(canvas, paint);
    }
  }

  private void drawGreenGoldValance(Canvas canvas, Paint paint) {
    int awBottom = panel.top + 84;
    int stripes = 8;
    float stripeH = (awBottom - panel.top - 8) / (float) stripes;
    int[] colors = {
        Color.rgb(36, 68, 52),
        Color.rgb(52, 88, 64),
        Color.rgb(118, 96, 48),
        Color.rgb(42, 58, 46)
    };
    for (int i = 0; i < stripes; i++) {
      paint.setColor(colors[i % colors.length]);
      canvas.drawRect(panel.left + 8, panel.top + 8 + i * stripeH,
          panel.right - 8, panel.top + 8 + (i + 1) * stripeH, paint);
    }
    paint.setColor(Color.rgb(68, 52, 36));
    canvas.drawRect(panel.left + 8, awBottom - 2, panel.right - 8, awBottom + 5, paint);
  }

  private void drawHeader(Canvas canvas, Paint paint) {
    float titleCx = titleBar.centerX();
    float titleCy = titleBar.top + titleBar.height() * 0.68f;
    paint.setTextAlign(Paint.Align.CENTER);
    paint.setColor(Color.rgb(255, 232, 175));
    paint.setTextSize(fitTextSize(paint, "如意赌坊", titleBar.width() - 4f, 28f, 38f));
    paint.setShadowLayer(6, 0, 2, Color.argb(180, 20, 40, 28));
    canvas.drawText("如意赌坊", titleCx, titleCy, paint);
    paint.clearShadowLayer();

    RoleInfo role = RoleSystem.getInstance().getRoleInfo();
    GamblingSystem g = GamblingSystem.getInstance();
    long dayNet = g.getDailyNetSilver();
    String dayStr = dayNet >= 0 ? "今日 +" + dayNet : "今日 " + dayNet;

    float y = panel.top + 80;
    drawStatChip(canvas, paint, panel.left + panel.width() * 0.2f, y,
        "身上 " + (role != null ? role.getMoney() : 0));
    drawStatChip(canvas, paint, panel.left + panel.width() * 0.44f, y,
        "柜中 " + (role != null ? role.getBankMoney() : 0));
    drawStatChip(canvas, paint, panel.right - panel.width() * 0.44f, y,
        "可用 " + g.getSpendableSilver());
    drawStatChip(canvas, paint, panel.right - panel.width() * 0.2f, y, dayStr);

    float y2 = panel.top + 108;
    drawStatChip(canvas, paint, panel.centerX(), y2, "庄家 · " + dealerName);
  }

  private void drawStatChip(Canvas canvas, Paint paint, float cx, float cy, String text) {
    paint.setTextSize(22f);
    float tw = paint.measureText(text);
    float pad = 12f;
    float l = cx - tw / 2f - pad;
    float r = cx + tw / 2f + pad;
    float t = cy - 24;
    float b = cy + 12;
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.argb(210, 42, 58, 48));
    canvas.drawRoundRect(l, t, r, b, 7, 7, paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(1.5f);
    paint.setColor(Color.rgb(196, 154, 88));
    canvas.drawRoundRect(l, t, r, b, 7, 7, paint);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.rgb(255, 240, 210));
    paint.setTextAlign(Paint.Align.CENTER);
    canvas.drawText(text, cx, cy, paint);
  }

  private void drawClose(Canvas canvas, Paint paint) {
    drawWoodButton(canvas, paint, closeButton, "离开", 31);
  }

  private void drawTable(Canvas canvas, Paint paint) {
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.rgb(32, 72, 52));
    canvas.drawRoundRect(body.left, body.top, body.right, body.bottom, 14, 14, paint);
    paint.setColor(Color.rgb(38, 82, 58));
    canvas.drawRoundRect(body.left + 6, body.top + 6, body.right - 6, body.bottom - 6, 12, 12, paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(2);
    paint.setColor(Color.argb(150, 196, 154, 88));
    canvas.drawRoundRect(body.left + 2, body.top + 2, body.right - 2, body.bottom - 2, 13, 13, paint);
    paint.setStyle(Paint.Style.FILL);
  }

  private void drawRulesLine(Canvas canvas, Paint paint) {
    paint.setTextAlign(Paint.Align.CENTER);
    paint.setColor(Color.argb(200, 235, 240, 220));
    paint.setTextSize(22);
    canvas.drawText("两骰比点 · 大≥8 小≤6 · 七点庄吃", body.centerX(), body.top + 28, paint);
    if (phase == Phase.BET_SIDE && stake > 0) {
      paint.setColor(Color.rgb(255, 228, 150));
      paint.setTextSize(25);
      canvas.drawText("本局筹码 " + stake + " 银 · 中得 " + (stake * 2) + " 银", body.centerX(),
          body.top + 52, paint);
    }
    if (phase == Phase.ROLLING || phase == Phase.RESULT) {
      String side = betBig ? "大" : "小";
      paint.setTextSize(23);
      paint.setColor(Color.rgb(255, 210, 170));
      canvas.drawText("已押 " + side + " · " + stake + " 银", body.centerX(), body.top + 52, paint);
    }
  }

  private void drawTableHint(Canvas canvas, Paint paint, String hint) {
    paint.setTextSize(22);
    paint.setColor(Color.rgb(255, 180, 140));
    paint.setTextAlign(Paint.Align.CENTER);
    canvas.drawText(hint, body.centerX(), body.bottom - 100, paint);
  }

  private void drawBlocked(Canvas canvas, Paint paint) {
    paint.setTextAlign(Paint.Align.CENTER);
    paint.setColor(Color.rgb(255, 220, 180));
    paint.setTextSize(26);
    canvas.drawText("今日输足 " + GamblingSystem.DAILY_MAX_NET_LOSS + " 银", body.centerX(),
        body.centerY() - 20, paint);
    paint.setTextSize(20);
    paint.setColor(Color.argb(220, 220, 200, 180));
    canvas.drawText("钱麻子摆手让你明日再来", body.centerX(), body.centerY() + 16, paint);
    drawWoodButtonFitted(canvas, paint, btnBlockedFarewell, "告辞", 30f, 22f);
  }

  private void drawStakeButtons(Canvas canvas, Paint paint) {
    GamblingSystem g = GamblingSystem.getInstance();
    drawWoodButtonFitted(canvas, paint, btnStakeLeisure,
        "闲玩 " + GamblingSystem.STAKE_LEISURE, 28f, 17f);
    drawWoodButtonFitted(canvas, paint, btnStakeNormal,
        "常态 " + GamblingSystem.STAKE_NORMAL, 28f, 17f);
    String highLabel = g.canUseHighRoller()
        ? ("豪客 " + GamblingSystem.STAKE_HIGH)
        : (GamblingSystem.MIN_LEVEL_HIGH_ROLLER + "级解锁");
    drawWoodButtonFitted(canvas, paint, btnStakeHigh, highLabel, 28f, 15f);
    if (!g.canUseHighRoller()) {
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(2);
      paint.setColor(Color.argb(120, 120, 120, 120));
      canvas.drawRoundRect(btnStakeHigh.left, btnStakeHigh.top, btnStakeHigh.right,
          btnStakeHigh.bottom, 10, 10, paint);
      paint.setStyle(Paint.Style.FILL);
    }
    drawWoodButtonFitted(canvas, paint, btnLeave, "不赌了", 28f, 20f);
  }

  private void drawBetButtons(Canvas canvas, Paint paint) {
    drawWoodButtonFitted(canvas, paint, btnBetBig, "押大", 30f, 22f);
    drawWoodButtonFitted(canvas, paint, btnBetSmall, "押小", 30f, 22f);
    drawWoodButtonFitted(canvas, paint, btnChangeStake, "换筹码", 30f, 22f);
  }

  private void drawResultButtons(Canvas canvas, Paint paint) {
    boolean capped = GamblingSystem.getInstance().isDailyLossCapped();
    drawWoodButtonFitted(canvas, paint, btnAgain, capped ? "今日已满" : "再来一局", 28f, 20f);
    drawWoodButtonFitted(canvas, paint, btnChangeAfter, "换筹码", 28f, 20f);
    drawWoodButtonFitted(canvas, paint, btnDone, "不赌了", 28f, 20f);
  }

  private void drawWoodButtonFitted(Canvas canvas, Paint paint, Rect r, String label,
                                    float maxSize, float minSize) {
    float size = fitTextSize(paint, label, r.width() - 14f, minSize, maxSize);
    drawWoodButton(canvas, paint, r, label, size);
  }

  private void drawWoodButton(Canvas canvas, Paint paint, Rect r, String label, float textSize) {
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.rgb(92, 58, 42));
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
    canvas.drawText(label, r.centerX(), ty, paint);
  }

  private void drawDiceScene(Canvas canvas, Paint paint) {
    long elapsed = phase == Phase.RESULT
        ? ROLL_MS
        : Math.min(ROLL_MS, System.currentTimeMillis() - rollStartMs);
    float t = Math.min(1f, elapsed / (float) ROLL_MS);
    boolean showFinal = phase == Phase.RESULT || rollRevealed;

    float dieSize = Math.min(140f, Math.min(body.width() * 0.2f, body.height() * 0.14f));
    float cx = body.centerX();
    float cy = body.centerY() - body.height() * 0.06f;
    float cx1 = cx - dieSize * 0.92f;
    float cx2 = cx + dieSize * 0.92f;

    float shakeAmp = showFinal ? 0 : (1f - t * 0.4f) * 18f;
    float oy1 = (float) Math.sin(elapsed * 0.022) * shakeAmp;
    float oy2 = (float) Math.cos(elapsed * 0.019) * shakeAmp;
    float rot1 = showFinal ? 0 : (float) Math.sin(elapsed * 0.031) * 18f;
    float rot2 = showFinal ? 0 : (float) Math.cos(elapsed * 0.027) * -16f;

    float bowlL = body.left + body.width() * 0.12f;
    float bowlR = body.right - body.width() * 0.12f;
    float bowlT = cy + dieSize * 0.35f;
    float bowlB = cy + dieSize * 1.25f;
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.argb(showFinal ? 70 : 110, 40, 28, 22));
    canvas.drawOval(bowlL, bowlT, bowlR, bowlB, paint);

    int f1 = showFinal ? finalDie1 : rollingFace1;
    int f2 = showFinal ? finalDie2 : rollingFace2;
    drawDie(canvas, paint, cx1, cy + oy1, dieSize, f1, rot1);
    drawDie(canvas, paint, cx2, cy + oy2, dieSize, f2, rot2);

    if (!showFinal) {
      paint.setTextAlign(Paint.Align.CENTER);
      paint.setColor(Color.rgb(235, 240, 228));
      paint.setTextSize(22);
      String shake = t < 0.35f ? "骰子乱跳…" : (t < 0.75f ? "咔嚓咔嚓…" : "要开了…");
      canvas.drawText(shake, body.centerX(), cy - dieSize * 0.9f, paint);
    }
  }

  private void drawResultPlaque(Canvas canvas, Paint paint) {
    int sum = finalDie1 + finalDie2;
    String points = finalDie1 + " + " + finalDie2 + " = " + sum + " 点";
    float cx = body.centerX();
    float anchorY = body.centerY() + body.height() * 0.30f;

    float headlineSize = 36f;
    float bodySize = 28f;
    float lineGap = 10f;
    float padX = 40f;
    float padY = 18f;

    paint.setTextSize(headlineSize);
    float w1 = paint.measureText(resultHeadline);
    paint.setTextSize(bodySize);
    float w2 = paint.measureText(points);
    float w3 = paint.measureText(resultDetail != null ? resultDetail : "");
    float minW = body.width() * 0.58f;
    float boxW = Math.max(minW, Math.max(w1, Math.max(w2, w3)) + padX * 2);

    paint.setTextSize(headlineSize);
    Paint.FontMetrics fmHead = paint.getFontMetrics();
    paint.setTextSize(bodySize);
    Paint.FontMetrics fmBody = paint.getFontMetrics();

    float y2Probe = fmHead.descent + lineGap - fmBody.ascent;
    float y3Probe = y2Probe + fmBody.descent + lineGap - fmBody.ascent;
    float blockH = (y3Probe + fmBody.descent) - fmHead.ascent;
    float y1 = anchorY - blockH / 2f - fmHead.ascent;
    float y2 = y1 + y2Probe;
    float y3 = y1 + y3Probe;

    float top = y1 + fmHead.ascent - padY;
    float bottom = y3 + fmBody.descent + padY;
    float l = cx - boxW / 2f;
    float r = cx + boxW / 2f;

    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.argb(235, 48, 40, 32));
    canvas.drawRoundRect(l, top, r, bottom, 14, 14, paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(2.5f);
    paint.setColor(Color.rgb(196, 154, 88));
    canvas.drawRoundRect(l, top, r, bottom, 14, 14, paint);
    paint.setStyle(Paint.Style.FILL);
    paint.setTextAlign(Paint.Align.CENTER);

    paint.setColor(Color.rgb(255, 228, 140));
    paint.setTextSize(headlineSize);
    canvas.drawText(resultHeadline, cx, y1, paint);

    paint.setColor(Color.rgb(255, 246, 228));
    paint.setTextSize(bodySize);
    canvas.drawText(points, cx, y2, paint);

    paint.setColor(lastNet >= 0 ? Color.rgb(180, 255, 200) : Color.rgb(255, 200, 180));
    canvas.drawText(resultDetail, cx, y3, paint);
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

  public boolean handleTouch(MotionEvent event) {
    if (!visible) {
      return false;
    }
    if (event.getActionMasked() != MotionEvent.ACTION_UP) {
      return true;
    }
    float x = event.getX();
    float y = event.getY();

    if (phase == Phase.ROLLING) {
      return true;
    }
    if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
      hide();
      return true;
    }

    switch (phase) {
      case BLOCKED:
        if (TouchUtil.checkIsInTouchRectFloat(btnBlockedFarewell, x, y)
            || TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
          hide();
        }
        return true;
      case STAKE_SELECT:
        if (TouchUtil.checkIsInTouchRectFloat(btnLeave, x, y)) {
          hide();
        } else if (pickStakeTouch(x, y)) {
          // stake set in pickStakeTouch
        }
        return true;
      case BET_SIDE:
        if (TouchUtil.checkIsInTouchRectFloat(btnChangeStake, x, y)) {
          phase = Phase.STAKE_SELECT;
          stake = 0;
        } else if (TouchUtil.checkIsInTouchRectFloat(btnBetBig, x, y)) {
          tryStartRoll(true);
        } else if (TouchUtil.checkIsInTouchRectFloat(btnBetSmall, x, y)) {
          tryStartRoll(false);
        }
        return true;
      case RESULT:
        if (TouchUtil.checkIsInTouchRectFloat(btnDone, x, y)) {
          hide();
        } else if (TouchUtil.checkIsInTouchRectFloat(btnChangeAfter, x, y)) {
          if (GamblingSystem.getInstance().isDailyLossCapped()) {
            phase = Phase.BLOCKED;
          } else {
            phase = Phase.STAKE_SELECT;
            stake = 0;
          }
        } else if (TouchUtil.checkIsInTouchRectFloat(btnAgain, x, y)) {
          if (GamblingSystem.getInstance().isDailyLossCapped()) {
            phase = Phase.BLOCKED;
          } else {
            phase = Phase.BET_SIDE;
            tableHint = null;
          }
        }
        return true;
      default:
        return true;
    }
  }

  private boolean pickStakeTouch(float x, float y) {
    GamblingSystem g = GamblingSystem.getInstance();
    int s = 0;
    if (TouchUtil.checkIsInTouchRectFloat(btnStakeLeisure, x, y)) {
      s = GamblingSystem.STAKE_LEISURE;
    } else if (TouchUtil.checkIsInTouchRectFloat(btnStakeNormal, x, y)) {
      s = GamblingSystem.STAKE_NORMAL;
    } else if (TouchUtil.checkIsInTouchRectFloat(btnStakeHigh, x, y)) {
      if (!g.canUseHighRoller()) {
        tableHint = "未满" + GamblingSystem.MIN_LEVEL_HIGH_ROLLER + "级，豪客席暂不接待";
        return false;
      }
      s = GamblingSystem.STAKE_HIGH;
    } else {
      return false;
    }
    if (!g.canAfford(s)) {
      tableHint = "银两不足，需要 " + s + "（可用 " + g.getSpendableSilver() + "）";
      return false;
    }
    if (g.wouldExceedDailyLossCap(s)) {
      tableHint = "再押此筹码恐超今日输额上限";
      return false;
    }
    stake = s;
    tableHint = null;
    phase = Phase.BET_SIDE;
    return true;
  }

  private void tryStartRoll(boolean big) {
    GamblingSystem g = GamblingSystem.getInstance();
    if (g.isDailyLossCapped() || g.wouldExceedDailyLossCap(stake)) {
      tableHint = "今日输额已达或将近上限";
      return;
    }
    if (!g.withdrawStake(stake)) {
      tableHint = "扣款失败，银两不足";
      return;
    }
    betBig = big;
    beginRoll();
  }
}
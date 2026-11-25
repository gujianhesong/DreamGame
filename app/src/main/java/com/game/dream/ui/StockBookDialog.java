package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.bean.StockMarketInfo;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.StockMarketSystem;
import com.game.dream.utils.TouchUtil;

import java.util.List;

/**
 * 金陵股簿交易面板
 */
public class StockBookDialog {

    private static final int ROW_HEIGHT = 88;
    private static final int HOLD_PANEL_MIN_W = 360;
    /** 持股栏宽度，左侧独立一栏 */
    private static final int HOLD_SHARES_COL_W = 128;
    private static final int ROW_GAP = 4;
    private static final int LIST_TOP_OFFSET = 155;
    private static final int ACTION_BAR_RESERVE = 88;
    private static final int BG_COLOR = Color.argb(245, 25, 30, 40);
    private static final int BORDER_COLOR = Color.rgb(210, 175, 90);

    private boolean visible;
    private Rect bounds;
    private Rect closeButton;
    private int selectedIndex;
    private final Rect buy100 = new Rect();
    private final Rect buy1000 = new Rect();
    private final Rect sell100 = new Rect();
    private final Rect sell1000 = new Rect();
    private final Rect sellAll = new Rect();
    private final Rect listArea = new Rect();
    private final Rect[] rowRects = new Rect[StockMarketInfo.STOCK_COUNT];
    private float scrollOffset;
    private float maxScrollOffset;

    public StockBookDialog() {
        bounds = new Rect();
        closeButton = new Rect();
        for (int i = 0; i < rowRects.length; i++) {
            rowRects[i] = new Rect();
        }
    }

    public void show() {
        selectedIndex = 0;
        scrollOffset = 0;
        visible = true;
        recalcButtons();
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setBounds(int x, int y, int width, int height) {
        bounds.set(x, y, x + width, y + height);
        recalcButtons();
    }

    private void recalcButtons() {
        closeButton.set(bounds.right - 58, bounds.top + 8, bounds.right - 8, bounds.top + 48);
        listArea.set(bounds.left + 12, bounds.top + LIST_TOP_OFFSET,
                bounds.right - 12, bounds.bottom - ACTION_BAR_RESERVE);
        layoutRowRects();
        int btnY = bounds.bottom - 80;
        int gap = 6;
        int count = 5;
        int x0 = bounds.left + 10;
        int totalW = bounds.width() - 20;
        int w = (totalW - gap * (count - 1)) / count;
        int btnH = 56;
        buy100.set(x0, btnY, x0 + w, btnY + btnH);
        buy1000.set(x0 + (w + gap), btnY, x0 + 2 * w + gap, btnY + btnH);
        sell100.set(x0 + 2 * (w + gap), btnY, x0 + 3 * w + 2 * gap, btnY + btnH);
        sell1000.set(x0 + 3 * (w + gap), btnY, x0 + 4 * w + 3 * gap, btnY + btnH);
        sellAll.set(x0 + 4 * (w + gap), btnY, bounds.right - 10, btnY + btnH);
    }

    private void layoutRowRects() {
        int count = StockMarketInfo.STOCK_COUNT;
        float totalContent = count * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        maxScrollOffset = Math.max(0, totalContent - listArea.height());
        if (scrollOffset > maxScrollOffset) {
            scrollOffset = maxScrollOffset;
        }
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
        for (int i = 0; i < count; i++) {
            int y = listArea.top + i * (ROW_HEIGHT + ROW_GAP) - (int) scrollOffset;
            rowRects[i].set(listArea.left, y, listArea.right, y + ROW_HEIGHT);
        }
    }

    public void draw(Canvas canvas) {
        if (!visible) {
            return;
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(BG_COLOR);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(BORDER_COLOR);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(255, 215, 120));
        paint.setTextSize(42);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("金陵股簿", bounds.centerX(), bounds.top + 46, paint);
        paint.setFakeBoldText(false);

        RoleSystem role = RoleSystem.getInstance();
        long bank = role.getRoleInfo().getBankMoney();
        long pocket = role.getRoleInfo().getMoney();
        int day = StockMarketSystem.getInstance().getGameDay();
        paint.setColor(Color.rgb(200, 225, 255));
        paint.setTextSize(30);
        String head = "第" + day + "日  身上" + pocket + "  柜中" + bank
                + "  市价每3秒刷新  每" + StockMarketSystem.DIVIDEND_EVERY_DAYS + "游戏日分利";
        canvas.drawText(head, bounds.centerX(), bounds.top + 88, paint);

        String news = StockMarketSystem.getInstance().getMarketNews();
        if (news != null && !news.isEmpty()) {
            paint.setColor(Color.rgb(255, 200, 140));
            paint.setTextSize(28);
            canvas.drawText(truncate(news, 24), bounds.centerX(), bounds.top + 124, paint);
        }

        List<StockMarketSystem.StockQuote> quotes = StockMarketSystem.getInstance().getQuotes();
        long totalProfit = 0;
        boolean hasHoldings = false;
        for (StockMarketSystem.StockQuote q : quotes) {
            if (q.shares > 0) {
                hasHoldings = true;
                totalProfit += q.getUnrealizedProfit();
            }
        }
        int rowLeft = bounds.left + 12;
        int rowRight = bounds.right - 12;
        int holdLeft = Math.max(rowLeft + (int) ((rowRight - rowLeft) * 0.42f), rowRight - HOLD_PANEL_MIN_W);
        float totalProfitX = holdLeft + HOLD_SHARES_COL_W + 14;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(30);
        paint.setFakeBoldText(true);
        if (hasHoldings) {
            String totalLine = (totalProfit >= 0 ? "总收益 +" : "总收益 ") + totalProfit + " 银";
            paint.setColor(totalProfit >= 0 ? Color.rgb(255, 110, 110) : Color.rgb(80, 210, 115));
            canvas.drawText(totalLine, totalProfitX, bounds.top + 142, paint);
        } else {
            paint.setColor(Color.rgb(200, 210, 230));
            canvas.drawText("总收益 —", totalProfitX, bounds.top + 142, paint);
        }
        paint.setFakeBoldText(false);

        layoutRowRects();
        float priceColX = computeAlignedPriceColumnX(paint, quotes, listArea.left);

        int save = canvas.save();
        canvas.clipRect(listArea);
        for (int i = 0; i < quotes.size() && i < StockMarketInfo.STOCK_COUNT; i++) {
            Rect row = rowRects[i];
            if (row.bottom < listArea.top || row.top > listArea.bottom) {
                continue;
            }
            drawRow(canvas, paint, quotes.get(i), i, priceColX);
        }
        canvas.restoreToCount(save);

        if (maxScrollOffset > 0) {
            drawScrollBar(canvas, paint);
        }

        drawActionBar(canvas, paint);
        drawCloseButton(canvas, paint);
    }

    /** 按最长股名计算统一「银/股」列 X，各行对齐。 */
    private float computeAlignedPriceColumnX(Paint paint, List<StockMarketSystem.StockQuote> quotes, int rowLeft) {
        paint.setTextSize(34);
        paint.setFakeBoldText(true);
        float maxNameW = 0;
        for (StockMarketSystem.StockQuote q : quotes) {
            maxNameW = Math.max(maxNameW, paint.measureText(q.name));
        }
        paint.setFakeBoldText(false);
        return rowLeft + 12 + maxNameW + 44;
    }

    private void drawRow(Canvas canvas, Paint paint, StockMarketSystem.StockQuote q, int index, float priceColX) {
        Rect row = rowRects[index];
        boolean selected = index == selectedIndex;
        paint.setColor(selected ? Color.argb(90, 90, 130, 180) : Color.argb(50, 40, 55, 75));
        canvas.drawRoundRect(row.left, row.top, row.right, row.bottom, 6, 6, paint);

        int holdLeft = Math.max(row.left + (int) (row.width() * 0.42f), row.right - HOLD_PANEL_MIN_W);
        paint.setColor(Color.argb(70, 255, 255, 255));
        canvas.drawRect(holdLeft, row.top + 4, row.right - 6, row.bottom - 4, paint);
        paint.setColor(Color.argb(140, 255, 230, 160));
        paint.setStrokeWidth(1.5f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(holdLeft, row.top + 4, row.right - 6, row.bottom - 4, paint);
        paint.setStyle(Paint.Style.FILL);

        int leftPad = 12;
        int cy = (row.top + row.bottom) / 2;
        float valLineY = row.top + 38;
        float profitLineY = row.top + 68;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setTextSize(34);
        paint.setFakeBoldText(true);
        canvas.drawText(q.name, row.left + leftPad, cy - 4, paint);
        paint.setFakeBoldText(false);

        String priceText = q.getPriceText() + " 银/股";
        paint.setColor(Color.rgb(255, 248, 120));
        paint.setTextSize(32);
        canvas.drawText(priceText, priceColX, cy + 12, paint);

        float ch = q.getDayChangePercent();
        String chText = String.format("%+.1f%%", ch);
        paint.setColor(ch >= 0 ? Color.rgb(255, 90, 90) : Color.rgb(80, 220, 120));
        paint.setTextSize(32);
        canvas.drawText(chText, row.left + leftPad, cy + 30, paint);

        int holdSepX = holdLeft + HOLD_SHARES_COL_W;
        paint.setColor(Color.argb(100, 255, 230, 180));
        paint.setStrokeWidth(1.5f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(holdSepX, row.top + 8, holdSepX, row.bottom - 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(230, 245, 255));
        paint.setTextSize(28);
        paint.setFakeBoldText(true);
        paint.setTextSize(26);
        canvas.drawText("持 " + q.shares + " 股", holdLeft + 10, cy + 10, paint);
        paint.setFakeBoldText(false);

        float valProfitX = holdSepX + 14;
        long marketVal = q.getMarketValue();
        long profit = q.getUnrealizedProfit();

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(255, 255, 200));
        paint.setTextSize(28);
        String valLine = q.shares > 0 ? "总值 " + marketVal + " 银" : "总值 —";
        canvas.drawText(valLine, valProfitX, valLineY, paint);

        paint.setTextSize(28);
        paint.setFakeBoldText(true);
        if (q.shares > 0) {
            String profitLine = (profit >= 0 ? "收益 +" : "收益 ") + profit + " 银";
            paint.setColor(profit >= 0 ? Color.rgb(255, 110, 110) : Color.rgb(80, 210, 115));
            canvas.drawText(profitLine, valProfitX, profitLineY, paint);
        } else {
            paint.setColor(Color.rgb(200, 210, 230));
            canvas.drawText("收益 —", valProfitX, profitLineY, paint);
        }
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawActionBar(Canvas canvas, Paint paint) {
        drawBtn(canvas, paint, buy100, "买100", Color.rgb(60, 120, 80));
        drawBtn(canvas, paint, buy1000, "买1000", Color.rgb(50, 100, 70));
        drawBtn(canvas, paint, sell100, "卖100", Color.rgb(140, 70, 70));
        drawBtn(canvas, paint, sell1000, "卖1000", Color.rgb(125, 60, 60));
        drawBtn(canvas, paint, sellAll, "全卖", Color.rgb(120, 55, 55));
    }

    private void drawBtn(Canvas canvas, Paint paint, Rect r, String label, int bg) {
        paint.setColor(bg);
        canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, 8, 8, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(label.length() > 4 ? 24 : 28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, r.centerX(), r.centerY() + 9, paint);
    }

    private void drawScrollBar(Canvas canvas, Paint paint) {
        float trackX = bounds.right - 10;
        float trackTop = listArea.top;
        float trackBottom = listArea.bottom;
        paint.setColor(Color.argb(80, 255, 255, 255));
        canvas.drawRect(trackX, trackTop, trackX + 6, trackBottom, paint);
        float thumbH = Math.max(40, listArea.height() * listArea.height()
                / (listArea.height() + maxScrollOffset));
        float thumbY = trackTop + (scrollOffset / maxScrollOffset) * (trackBottom - trackTop - thumbH);
        paint.setColor(Color.argb(180, 255, 215, 140));
        canvas.drawRoundRect(trackX, thumbY, trackX + 6, thumbY + thumbH, 3, 3, paint);
    }

    private void drawCloseButton(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(200, 180, 60, 60));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        canvas.drawLine(closeButton.left + 12, closeButton.top + 12,
                closeButton.right - 12, closeButton.bottom - 12, paint);
        canvas.drawLine(closeButton.right - 12, closeButton.top + 12,
                closeButton.left + 12, closeButton.bottom - 12, paint);
    }

    public boolean handleTouch(float x, float y) {
        if (!visible) {
            return false;
        }
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }
        for (int i = 0; i < rowRects.length; i++) {
            Rect row = rowRects[i];
            if (row.bottom < listArea.top || row.top > listArea.bottom) {
                continue;
            }
            if (TouchUtil.checkIsInTouchRectFloat(row, x, y)) {
                selectedIndex = i;
                return true;
            }
        }
        if (TouchUtil.checkIsInTouchRectFloat(listArea, x, y)) {
            return true;
        }
        StockMarketSystem market = StockMarketSystem.getInstance();
        if (TouchUtil.checkIsInTouchRectFloat(buy100, x, y)) {
            flashResult(market.tryBuy(selectedIndex, 100));
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(buy1000, x, y)) {
            flashResult(market.tryBuy(selectedIndex, 1000));
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(sell100, x, y)) {
            flashResult(market.trySell(selectedIndex, 100));
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(sell1000, x, y)) {
            flashResult(market.trySell(selectedIndex, 1000));
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(sellAll, x, y)) {
            List<StockMarketSystem.StockQuote> q = market.getQuotes();
            if (selectedIndex < q.size()) {
                int n = q.get(selectedIndex).sellableShares;
                if (n > 0) {
                    flashResult(market.trySell(selectedIndex, n));
                } else {
                    flashResult("无可卖之股。");
                }
            }
            return true;
        }
        return true;
    }

    public boolean handleScroll(float deltaX, float deltaY) {
        if (!visible || maxScrollOffset <= 0) {
            return false;
        }
        scrollOffset -= deltaY;
        if (scrollOffset < 0) {
            scrollOffset = 0;
        } else if (scrollOffset > maxScrollOffset) {
            scrollOffset = maxScrollOffset;
        }
        layoutRowRects();
        return true;
    }

    private void flashResult(String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        GameEngine.getInstance().showCenterToast(msg);
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }

}
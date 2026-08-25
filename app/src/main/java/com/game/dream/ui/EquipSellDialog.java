package com.game.dream.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.panel.EquipInfoPanel;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.utils.ItemsUtil;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 装备出售专用对话框 - 支持分页浏览、查看详情、直接出售
 */
public class EquipSellDialog {

    public interface SellCallback {
        void onItemSold(ItemStack stack, int price);
    }

    // 布局常量
    private static final int PAGE_SIZE = 8;
    private static final int ROW_HEIGHT = 72;
    private static final int ROW_GAP = 6;

    // 颜色
    private static final int BG_COLOR = Color.argb(240, 20, 25, 35);
    private static final int BORDER_COLOR = Color.rgb(100, 180, 255);
    private static final int ROW_BG_EVEN = Color.argb(60, 40, 60, 90);
    private static final int ROW_BG_ODD = Color.argb(40, 30, 45, 70);
    private static final int SELL_BTN_COLOR = Color.argb(200, 180, 60, 60);
    private static final int DETAIL_BTN_COLOR = Color.argb(200, 50, 100, 150);
    private static final int NAV_BTN_COLOR = Color.argb(180, 50, 100, 150);

    // 状态
    private boolean visible;
    private Rect bounds;
    private String title;
    private List<ItemStack> equipList;
    private List<Integer> sellPrices;
    private int currentPage;

    // 按钮区域
    private Rect closeButton;
    private Rect prevButton;
    private Rect nextButton;
    private List<Rect> sellButtons;
    private List<Rect> detailButtons;

    // 详情视图 - 使用 EquipInfoPanel
    private EquipInfoPanel equipInfoPanel;

    private SellCallback callback;

    public EquipSellDialog() {
        bounds = new Rect();
        closeButton = new Rect();
        prevButton = new Rect();
        nextButton = new Rect();
        sellButtons = new ArrayList<>();
        detailButtons = new ArrayList<>();
        equipInfoPanel = new EquipInfoPanel();
    }

    /**
     * 显示装备出售对话框
     */
    public void show(String title, List<ItemStack> equipList, List<Integer> sellPrices, SellCallback callback) {
        this.title = title;
        this.callback = callback;
        this.currentPage = 0;
        equipInfoPanel.hide();
        updateData(equipList, sellPrices);
        this.visible = true;
    }

    /**
     * 更新数据（排序后的装备列表和对应价格）
     */
    public void updateData(List<ItemStack> equipList, List<Integer> sellPrices) {
        this.equipList = new ArrayList<>(equipList);
        this.sellPrices = new ArrayList<>(sellPrices);
        int totalPages = getTotalPages();
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
        recalcButtons();
    }

    public boolean isVisible() {
        return visible;
    }

    public void hide() {
        visible = false;
        equipInfoPanel.hide();
    }

    public void setBounds(int x, int y, int width, int height) {
        bounds.set(x, y, x + width, y + height);
    }

    private int getTotalPages() {
        return Math.max(1, (equipList.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private void recalcButtons() {
        // Close button
        int padding = 10;
        int btnSize = 40;
        closeButton.set(bounds.right - btnSize - padding, bounds.top + padding,
                bounds.right - padding, bounds.top + padding + btnSize);

        // Navigation buttons at bottom
        int navY = bounds.bottom - 60;
        int navBtnW = 160;
        int navBtnH = 42;
        int navGap = 20;

        boolean hasPrev = currentPage > 0;
        boolean hasNext = currentPage < getTotalPages() - 1;

        if (hasPrev && hasNext) {
            int totalW = navBtnW * 2 + navGap;
            int startX = bounds.centerX() - totalW / 2;
            prevButton.set(startX, navY, startX + navBtnW, navY + navBtnH);
            nextButton.set(startX + navBtnW + navGap, navY,
                    startX + navBtnW * 2 + navGap, navY + navBtnH);
        } else if (hasPrev) {
            prevButton.set(bounds.centerX() - navBtnW / 2, navY,
                    bounds.centerX() + navBtnW / 2, navY + navBtnH);
        } else if (hasNext) {
            nextButton.set(bounds.centerX() - navBtnW / 2, navY,
                    bounds.centerX() + navBtnW / 2, navY + navBtnH);
        }

        // Per-row sell and detail buttons
        sellButtons.clear();
        detailButtons.clear();
        int startIdx = currentPage * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, equipList.size());
        int listTop = bounds.top + 105;

        for (int i = startIdx; i < endIdx; i++) {
            int rowIdx = i - startIdx;
            int rowY = listTop + rowIdx * (ROW_HEIGHT + ROW_GAP);

            int btnW = 60;
            int btnH = 30;
            int rightMargin = 15;
            int sellX = bounds.right - rightMargin - btnW;
            int detailX = sellX - btnW - 20;

            sellButtons.add(new Rect(sellX, rowY + 18, sellX + btnW, rowY + 18 + btnH));
            detailButtons.add(new Rect(detailX, rowY + 18, detailX + btnW, rowY + 18 + btnH));
        }
    }

    // ==================== 绘制 ====================

    public void draw(Canvas canvas) {
        if (!visible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 半透明背景
        canvas.drawColor(Color.argb(100, 0, 0, 0));
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 15, 15, paint);
        paint.setColor(BG_COLOR);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 15, 15, paint);

        // 边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(BORDER_COLOR);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, 15, 15, paint);
        paint.setStyle(Paint.Style.FILL);

        // 关闭按钮
        drawCloseButton(canvas, paint);

        // 列表视图
        drawListView(canvas, paint);

        // 装备详情面板（在最上层）
        if (equipInfoPanel.isVisible()) {
            equipInfoPanel.draw(canvas);
        }
    }

    private void drawCloseButton(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(180, 255, 80, 80));
        canvas.drawRoundRect(closeButton.left, closeButton.top,
                closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(closeButton.left, closeButton.top,
                closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        canvas.drawLine(closeButton.left + 10, closeButton.top + 10,
                closeButton.right - 10, closeButton.bottom - 10, paint);
        canvas.drawLine(closeButton.right - 10, closeButton.top + 10,
                closeButton.left + 10, closeButton.bottom - 10, paint);
    }

    private void drawListView(Canvas canvas, Paint paint) {
        // 标题
        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(title, bounds.centerX(), bounds.top + 45, paint);
        paint.setFakeBoldText(false);

        // 信息栏
        paint.setColor(Color.rgb(200, 220, 255));
        paint.setTextSize(22);
        String info = "金钱: " + RoleSystem.getInstance().getRoleInfo().getMoney()
                + "  |  共" + equipList.size() + "件  第" + (currentPage + 1) + "/" + getTotalPages() + "页";
        canvas.drawText(info, bounds.centerX(), bounds.top + 80, paint);

        if (equipList.isEmpty()) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(28);
            canvas.drawText("背包里没有可出售的装备", bounds.centerX(), bounds.centerY(), paint);
            return;
        }

        // 装备列表行
        int startIdx = currentPage * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, equipList.size());
        int listTop = bounds.top + 105;

        for (int i = startIdx; i < endIdx; i++) {
            int rowIdx = i - startIdx;
            int rowY = listTop + rowIdx * (ROW_HEIGHT + ROW_GAP);
            EquipmentItem equip = (EquipmentItem) equipList.get(i).getItem();
            int price = sellPrices.get(i);

            // 行背景（交替色）
            paint.setColor(rowIdx % 2 == 0 ? ROW_BG_EVEN : ROW_BG_ODD);
            canvas.drawRoundRect(bounds.left + 15, rowY, bounds.right - 15, rowY + ROW_HEIGHT, 8, 8, paint);

            // 左侧品质色条
            paint.setColor(equip.getColor());
            paint.setStrokeWidth(4);
            canvas.drawLine(bounds.left + 20, rowY + 8, bounds.left + 20, rowY + ROW_HEIGHT - 8, paint);
            paint.setStrokeWidth(1);

            // 装备名称（品质颜色）
            paint.setColor(equip.getColor());
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.LEFT);
            String rarityText = ItemsUtil.getRarityText(equip.getRarity());
            EquipItemInfo equipItemInfo = equip.getEquipItemInfo();
            String levelText = (equipItemInfo != null && equipItemInfo.getLevel() > 0) ? " Lv." + equipItemInfo.getLevel() : "";
            canvas.drawText(equip.getName(), bounds.left + 30, rowY + 28, paint);

            // 品质 + 等级 + 价格
            paint.setColor(Color.rgb(250, 250, 0));
            paint.setTextSize(18);
            canvas.drawText(rarityText + levelText + "  " + price + "金", bounds.left + 30, rowY + 55, paint);

            // 出售按钮
            if (rowIdx < sellButtons.size()) {
                drawSmallButton(canvas, paint, sellButtons.get(rowIdx), "出售", SELL_BTN_COLOR);
            }
            // 详情按钮
            if (rowIdx < detailButtons.size()) {
                drawSmallButton(canvas, paint, detailButtons.get(rowIdx), "详情", DETAIL_BTN_COLOR);
            }
        }

        // 分页导航
        drawNavButtons(canvas, paint);
    }

    private void drawNavButtons(Canvas canvas, Paint paint) {
        boolean hasPrev = currentPage > 0;
        boolean hasNext = currentPage < getTotalPages() - 1;

        if (hasPrev) {
            String label = "◀ 上一页";
            drawNavButton(canvas, paint, prevButton, label);
        }
        if (hasNext) {
            String label = "下一页 ▶";
            drawNavButton(canvas, paint, nextButton, label);
        }
    }

    private void drawNavButton(Canvas canvas, Paint paint, Rect rect, String text) {
        paint.setColor(NAV_BTN_COLOR);
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(150, 200, 255));
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, rect.centerX(), rect.centerY() + 7, paint);
    }

    private void drawSmallButton(Canvas canvas, Paint paint, Rect rect, String text, int bgColor) {
        paint.setColor(bgColor);
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 6, 6, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 6, 6, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, rect.centerX(), rect.centerY() + 6, paint);
    }

    // ==================== 触摸处理 ====================

    public boolean handleTouch(float x, float y) {
        if (!visible) return false;

        // 装备详情面板优先处理触摸
        if (equipInfoPanel.isVisible()) {
            equipInfoPanel.handleTouch(x, y, bounds.left, bounds.top, bounds.right, bounds.bottom);
            return true;
        }

        // 关闭按钮
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        return handleListTouch(x, y);
    }

    private boolean handleListTouch(float x, float y) {
        if (equipList.isEmpty()) return true;

        // 分页按钮
        if (currentPage > 0 && TouchUtil.checkIsInTouchRectFloat(prevButton, x, y)) {
            currentPage--;
            recalcButtons();
            return true;
        }
        if (currentPage < getTotalPages() - 1 && TouchUtil.checkIsInTouchRectFloat(nextButton, x, y)) {
            currentPage++;
            recalcButtons();
            return true;
        }

        int startIdx = currentPage * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, equipList.size());

        for (int i = startIdx; i < endIdx; i++) {
            int rowIdx = i - startIdx;

            // 出售按钮
            if (rowIdx < sellButtons.size() && TouchUtil.checkIsInTouchRectFloat(sellButtons.get(rowIdx), x, y)) {
                sellItem(i);
                return true;
            }
            // 详情按钮
            if (rowIdx < detailButtons.size() && TouchUtil.checkIsInTouchRectFloat(detailButtons.get(rowIdx), x, y)) {
                showItemDetail(i);
                return true;
            }
        }

        return true; // 对话框区域内消费触摸
    }

    // ==================== 业务逻辑 ====================

    private void sellItem(int index) {
        if (index < 0 || index >= equipList.size()) return;

        ItemStack stack = equipList.get(index);
        int price = sellPrices.get(index);
        String name = stack.getItem().getName();

        ItemSystem.getInstance().removeItems(Collections.singletonList(stack));
        RoleSystem.getInstance().getRoleInfo().setMoney(
                RoleSystem.getInstance().getRoleInfo().getMoney() + price);
        GameEngine.getInstance().showCenterToast("出售 " + name + " 获得 " + price + " 金钱");

        if (callback != null) {
            callback.onItemSold(stack, price);
        }

        // 刷新数据
        equipList.remove(index);
        sellPrices.remove(index);
        int totalPages = getTotalPages();
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
        recalcButtons();
    }

    private void showItemDetail(int index) {
        if (index < 0 || index >= equipList.size()) return;

        EquipmentItem equip = (EquipmentItem) equipList.get(index).getItem();
        equipInfoPanel.showReadOnly(equip, bounds.centerX(), bounds.centerY());
    }
}

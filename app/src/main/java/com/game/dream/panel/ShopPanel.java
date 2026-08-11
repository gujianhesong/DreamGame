package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.enums.FoodType;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 商店面板 - 食物购买
 */
public class ShopPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    private List<ShopItem> shopItems;
    private static final int ITEM_HEIGHT = 120;
    private static final int ITEM_GAP = 10;

    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;
    private float touchDownY = 0;

    public ShopPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.shopItems = new ArrayList<>();
        initShopItems();
    }

    private void initShopItems() {
        shopItems.clear();
        for (FoodType foodType : FoodType.values()) {
            Item foodItem = ItemCreator.createCookFood(foodType);
            shopItems.add(new ShopItem(foodItem, foodItem.getValue()));
        }
    }

    public void showShopItems(List<ShopItem> shopItems) {
        this.shopItems.clear();
        if(shopItems != null){
            this.shopItems.addAll(shopItems);
        }
        isVisible = true;
        scrollOffset = 0;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        int buttonSize = 40;
        int padding = 10;
        closeButton.set(
                panelBounds.right - buttonSize - padding,
                panelBounds.top + padding,
                panelBounds.right - padding,
                panelBounds.top + padding + buttonSize
        );
    }

    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Panel background
        paint.setColor(Color.argb(240, 20, 25, 35));
        canvas.drawRect(panelBounds, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(100, 180, 255));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        // Close button
        drawCloseButton(canvas, paint);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("云游商人", panelBounds.centerX(), panelBounds.top + 50, paint);

        // Money display
        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.LEFT);
        long money = RoleSystem.getInstance().getRoleInfo().getMoney();
        canvas.drawText("金钱: " + money, panelBounds.left + 30, panelBounds.top + 90, paint);

        // Divider
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 105,
                panelBounds.right - 20, panelBounds.top + 105, paint);

        // Draw item list
        drawItemList(canvas, paint);
    }

    private void drawItemList(Canvas canvas, Paint paint) {
        int listStartY = panelBounds.top + 115;
        int listEndY = panelBounds.bottom - 15;
        int itemWidth = panelBounds.width() - 60;
        int itemX = panelBounds.left + 30;

        // Calculate max scroll
        int totalHeight = shopItems.size() * (ITEM_HEIGHT + ITEM_GAP);
        int visibleHeight = listEndY - listStartY;
        maxScrollOffset = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        // Clip to list area
        Rect clipRect = new Rect(panelBounds.left + 10, listStartY, panelBounds.right - 10, listEndY);
        canvas.save();
        canvas.clipRect(clipRect);

        for (int i = 0; i < shopItems.size(); i++) {
            ShopItem shopItem = shopItems.get(i);
            int itemY = (int) (listStartY + i * (ITEM_HEIGHT + ITEM_GAP) - scrollOffset);

            // Skip if outside visible area
            if (itemY + ITEM_HEIGHT < listStartY || itemY > listEndY) continue;

            Rect itemRect = new Rect(itemX, itemY, itemX + itemWidth, itemY + ITEM_HEIGHT);
            shopItem.itemRect = itemRect;

            // Item background
            paint.setColor(Color.argb(80, 40, 45, 60));
            canvas.drawRoundRect(itemRect.left, itemRect.top, itemRect.right, itemRect.bottom, 8, 8, paint);

            // Item border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(80, 120, 160));
            canvas.drawRoundRect(itemRect.left, itemRect.top, itemRect.right, itemRect.bottom, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            // Item name
            paint.setColor(shopItem.item.getColor());
            paint.setTextSize(26);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setFakeBoldText(true);
            canvas.drawText(shopItem.item.getName(), itemRect.left + 15, itemRect.top + 30, paint);
            paint.setFakeBoldText(false);

            // Item description (two lines max)
            paint.setColor(Color.rgb(180, 180, 180));
            paint.setTextSize(18);
            String desc = shopItem.item.getDescription();
            float descMaxWidth = itemWidth - 130; // 留出购买按钮空间
            String[] descLines = splitTextIntoLines(desc, paint, descMaxWidth, 2);
            canvas.drawText(descLines[0], itemRect.left + 15, itemRect.top + 55, paint);
            if (descLines.length > 1) {
                canvas.drawText(descLines[1], itemRect.left + 15, itemRect.top + 75, paint);
            }

            // Price
            paint.setColor(Color.rgb(255, 215, 0));
            paint.setTextSize(24);
            canvas.drawText("💰 " + shopItem.price, itemRect.left + 15, itemRect.top + 105, paint);

            // Buy button
            Rect buyBtn = new Rect(itemRect.right - 100, itemRect.top + 35, itemRect.right - 15, itemRect.top + 85);
            shopItem.buyBtnRect = buyBtn;

            paint.setColor(Color.argb(180, 50, 130, 80));
            canvas.drawRoundRect(buyBtn.left, buyBtn.top, buyBtn.right, buyBtn.bottom, 8, 8, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(100, 255, 100));
            canvas.drawRoundRect(buyBtn.left, buyBtn.top, buyBtn.right, buyBtn.bottom, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.WHITE);
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("购买", buyBtn.centerX(), buyBtn.centerY() + 8, paint);
        }

        canvas.restore();

        // Scrollbar
        if (maxScrollOffset > 0) {
            int scrollbarWidth = 6;
            int scrollbarX = panelBounds.right - 16;
            float scrollbarHeight = Math.max(40, (visibleHeight / (float) (visibleHeight + maxScrollOffset)) * visibleHeight);
            float scrollbarY = listStartY + (scrollOffset / maxScrollOffset) * (visibleHeight - scrollbarHeight);

            paint.setColor(Color.argb(50, 100, 100, 100));
            canvas.drawRoundRect(scrollbarX, listStartY, scrollbarX + scrollbarWidth, listEndY, 3, 3, paint);

            paint.setColor(Color.argb(150, 150, 150, 150));
            canvas.drawRoundRect(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 3, 3, paint);
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
        paint.setColor(Color.WHITE);

        float padding = 10;
        canvas.drawLine(closeButton.left + padding, closeButton.top + padding,
                closeButton.right - padding, closeButton.bottom - padding, paint);
        canvas.drawLine(closeButton.right - padding, closeButton.top + padding,
                closeButton.left + padding, closeButton.bottom - padding, paint);
    }

    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) return false;

        // Close button
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        // Check buy buttons
        for (ShopItem shopItem : shopItems) {
            if (shopItem.buyBtnRect != null && shopItem.buyBtnRect.contains((int) x, (int) y)) {
                buyItem(shopItem);
                return true;
            }
        }

        // Start drag
        isDragging = false;
        touchDownY = y;
        lastTouchY = y;
        return true;
    }

    public boolean handleTouchMove(float x, float y) {
        if (!isVisible) return false;

        float deltaY = y - lastTouchY;
        if (Math.abs(y - touchDownY) > 10) {
            isDragging = true;
        }

        if (isDragging) {
            scrollOffset -= deltaY;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            lastTouchY = y;
            return true;
        }
        return false;
    }

    public boolean handleTouchUp(float x, float y) {
        if (!isVisible) return false;
        isDragging = false;
        return false;
    }

    private void buyItem(ShopItem shopItem) {
        long money = RoleSystem.getInstance().getRoleInfo().getMoney();
        if (money < shopItem.price) {
            GameEngine.getInstance().showCenterToast("金钱不足!");
            return;
        }

        if (!ItemSystem.getInstance().hasSpace()) {
            GameEngine.getInstance().showCenterToast("背包已满!");
            return;
        }

        RoleSystem.getInstance().removeMoney(shopItem.price);
        ItemSystem.getInstance().addItem(shopItem.item, 1);
        GameEngine.getInstance().showCenterToast("购买了 " + shopItem.item.getName());
    }

    /**
     * 将文本按宽度拆分为指定行数
     */
    private String[] splitTextIntoLines(String text, Paint paint, float maxWidth, int maxLines) {
        if (text == null || text.isEmpty()) return new String[]{""};

        List<String> lines = new ArrayList<>();
        int start = 0;

        for (int line = 0; line < maxLines && start < text.length(); line++) {
            boolean isLastLine = (line == maxLines - 1);
            if (isLastLine) {
                // 最后一行，剩余全部放入，超出加...
                String remaining = text.substring(start);
                if (paint.measureText(remaining) <= maxWidth) {
                    lines.add(remaining);
                } else {
                    // 截断并加...
                    int end = start;
                    while (end < text.length() && paint.measureText(text.substring(start, end + 1) + "...") <= maxWidth) {
                        end++;
                    }
                    lines.add(text.substring(start, end) + "...");
                }
            } else {
                // 非最后一行，尽量多放
                int end = start;
                while (end < text.length() && paint.measureText(text.substring(start, end + 1)) <= maxWidth) {
                    end++;
                }
                if (end == start) {
                    // 至少放一个字符
                    end = start + 1;
                }
                lines.add(text.substring(start, end));
                start = end;
            }
        }

        return lines.toArray(new String[0]);
    }

    public static class ShopItem {
        Item item;
        int price;
        Rect itemRect;
        Rect buyBtnRect;

        public ShopItem(Item item, int price) {
            this.item = item;
            this.price = price;
        }
    }
}

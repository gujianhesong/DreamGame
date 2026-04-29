package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;

/**
 * Item information popup panel
 */
public class ItemInfoPanel {
    private ItemStack selectedItem;
    private boolean isVisible;
    private Rect panelBounds;

    // Buttons
    private Rect useButton;
    private Rect dropButton;

    // Callback interface
    public interface ItemActionListener {
        void onUseItem(ItemStack item);

        void onDropItem(ItemStack item);

    }

    private ItemActionListener listener;

    public ItemInfoPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.useButton = new Rect();
        this.dropButton = new Rect();
    }

    /**
     * Show item info panel
     */
    public void show(ItemStack item, int centerX, int centerY, ItemActionListener listener) {
        this.selectedItem = item;
        this.listener = listener;
        this.isVisible = true;

        // Calculate panel size
        int panelWidth = 400;
        int panelHeight = 350;

        // Position panel to the right of the click position
        // Add some offset so it doesn't overlap with the cursor/item
        int offsetX = 50;
        int panelX = centerX + offsetX;
        int panelY = centerY - panelHeight / 2; // Center vertically relative to click

        // Ensure panel stays within reasonable bounds (optional: add boundary checks later)
        panelBounds.set(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight
        );

        // Calculate button positions
        int buttonWidth = 120;
        int buttonHeight = 45;
        int bottomMargin = 30;

        // Use/Equip button (left)
        useButton.set(
                panelBounds.left + 30,
                panelBounds.bottom - bottomMargin - buttonHeight,
                panelBounds.left + 30 + buttonWidth,
                panelBounds.bottom - bottomMargin
        );

        // Drop button (right)
        dropButton.set(
                panelBounds.right - 30 - buttonWidth,
                panelBounds.bottom - bottomMargin - buttonHeight,
                panelBounds.right - 30,
                panelBounds.bottom - bottomMargin
        );
    }

    /**
     * Hide panel
     */
    public void hide() {
        isVisible = false;
        selectedItem = null;
    }

    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Draw the item info panel
     */
    public void draw(Canvas canvas) {
        if (!isVisible || selectedItem == null) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Item item = selectedItem.getItem();

        // Panel background (more transparent - argb 180 instead of 240)
        paint.setColor(Color.argb(180, 25, 30, 40));
        float cornerRadius = 15f; // Round corners
        canvas.drawRoundRect(panelBounds.left, panelBounds.top,
                panelBounds.right, panelBounds.bottom,
                cornerRadius, cornerRadius, paint);

        // Border with rarity color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(item.getColor());
        canvas.drawRoundRect(panelBounds.left, panelBounds.top,
                panelBounds.right, panelBounds.bottom,
                cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        // Item name (with rarity color)
        paint.setColor(item.getColor());
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(item.getName(), panelBounds.centerX(), panelBounds.top + 45, paint);

        // Rarity label
        paint.setTextSize(18);
        paint.setColor(Color.WHITE);
        canvas.drawText(getRarityText(item.getRarity()), panelBounds.centerX(), panelBounds.top + 70, paint);

        // Divider line
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 85,
                panelBounds.right - 20, panelBounds.top + 85, paint);

        // Description
        paint.setColor(Color.rgb(200, 200, 220));
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.LEFT);

        // Wrap description text
        String description = item.getDescription();
        drawWrappedText(canvas, paint, description, panelBounds.left + 25, panelBounds.top + 115,
                panelBounds.width() - 50, 28);

        // Additional info section
        int infoStartY = panelBounds.top + 180;

        paint.setColor(Color.rgb(150, 150, 180));
        paint.setTextSize(18);
        canvas.drawText("类型: " + getTypeText(item.getType()),
                panelBounds.left + 25, infoStartY, paint);

        canvas.drawText("价值: " + item.getValue() + " 金币",
                panelBounds.left + 25, infoStartY + 30, paint);

        if (selectedItem.getQuantity() > 1) {
            canvas.drawText("数量: " + selectedItem.getQuantity(),
                    panelBounds.left + 25, infoStartY + 60, paint);
        }

        // Stackable info
        canvas.drawText("可堆叠: " + (item.getMaxStack() > 1 ? "是 (最大" + item.getMaxStack() + ")" : "否"),
                panelBounds.left + 25, infoStartY + 90, paint);

        // Draw buttons
        drawButtons(canvas, paint, item);
    }

    /**
     * Draw action buttons
     */
    private void drawButtons(Canvas canvas, Paint paint, Item item) {
        drawButton(canvas, paint, useButton, "✨ 使用", Color.rgb(50, 150, 255));
        drawButton(canvas, paint, dropButton, "🗑️ 丢弃", Color.rgb(200, 80, 80));
    }

    /**
     * Draw a single button
     */
    private void drawButton(Canvas canvas, Paint paint, Rect button, String text, int color) {
        // Button background
        paint.setColor(Color.argb(200, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top, button.right, button.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        // Text
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = button.centerY() + 8;
        canvas.drawText(text, button.centerX(), textY, paint);
    }

    /**
     * Draw wrapped text
     */
    private void drawWrappedText(Canvas canvas, Paint paint, String text, float x, float y,
                                 float maxWidth, float lineHeight) {
        String[] words = text.split("");
        StringBuilder line = new StringBuilder();
        float currentY = y;

        for (String word : words) {
            String testLine = line.toString() + word;
            float width = paint.measureText(testLine);

            if (width > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), x, currentY, paint);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line.append(word);
            }
        }

        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, currentY, paint);
        }
    }

    /**
     * Get rarity text
     */
    private String getRarityText(Item.Rarity rarity) {
        switch (rarity) {
            case Rarity_1:
                return "普通";
            case Rarity_2:
                return "优秀";
            case Rarity_3:
                return "稀有";
            case Rarity_4:
                return "史诗";
            case Rarity_5:
                return "传说";
            default:
                return "";
        }
    }

    /**
     * Get type text
     */
    private String getTypeText(Item.Type type) {
        switch (type) {
            case CONSUMABLE:
                return "消耗品";
            case EQUIPMENT:
                return "装备";
            case MATERIAL:
                return "材料";
            case QUEST_ITEM:
                return "任务物品";
            case SPECIAL:
                return "特殊物品";
            default:
                return "未知";
        }
    }

    /**
     * Handle touch event - returns true if touch was handled
     */
    public boolean handleTouch(float x, float y, float parentLeft, float parentTop,
                               float parentRight, float parentBottom) {
        if (!isVisible) return false;

        // Check if touch is outside the panel bounds - close the panel
        if (x < parentLeft || x > parentRight || y < parentTop || y > parentBottom) {
            hide();
            return true;
        }

        // Check if touch is outside the info panel but inside parent - close the panel
        if (!panelBounds.contains((int) x, (int) y)) {
            hide();
            return true;
        }

        // Check use/unequip button
        if (useButton.contains((int) x, (int) y)) {
            if (listener != null && selectedItem != null) {
                listener.onUseItem(selectedItem);
            }
            hide();
            return true;
        }

        // Check drop button (only for non-equipped items)
        if (dropButton.contains((int) x, (int) y)) {
            if (listener != null && selectedItem != null) {
                listener.onDropItem(selectedItem);
            }
            hide();
            return true;
        }

        // Touch inside panel but not on buttons - just consume the event
        return true;
    }

}

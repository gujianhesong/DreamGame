package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.item.ConsumableItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.system.ItemSystem;
import com.game.dream.utils.ItemsUtil;
import com.game.dream.utils.TouchUtil;

import java.util.List;

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
    private Rect defaultPotionButton; // 设为/取消默认药品按钮

    // Whether this item is a default potion
    private boolean isDefaultHp = false;
    private boolean isDefaultMp = false;

    // Callback interface
    public interface ItemActionListener {
        void onUseItem(ItemStack item);

        void onDropItem(ItemStack item);

        void onSetDefaultHp(ItemStack item);

        void onSetDefaultMp(ItemStack item);
    }

    private ItemActionListener listener;

    public ItemInfoPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.useButton = new Rect();
        this.dropButton = new Rect();
        this.defaultPotionButton = new Rect();
    }

    /**
     * Show item info panel
     */
    public void show(ItemStack item, int centerX, int centerY, ItemActionListener listener) {
        show(item, centerX, centerY, false, false, listener);
    }

    /**
     * Show item info panel with default potion state
     */
    public void show(ItemStack item, int centerX, int centerY, boolean isDefaultHp, boolean isDefaultMp, ItemActionListener listener) {
        this.selectedItem = item;
        this.listener = listener;
        this.isVisible = true;
        this.isDefaultHp = isDefaultHp;
        this.isDefaultMp = isDefaultMp;

        // Determine if item is a consumable potion (HP or MP)
        boolean isHealPotion = false;
        if (item.getItem() instanceof ConsumableItem) {
            ConsumableItem ci = (ConsumableItem) item.getItem();
            isHealPotion = ci.getEffectType() == ConsumableItem.EffectType.HEAL_HP
                    || ci.getEffectType() == ConsumableItem.EffectType.HEAL_MP;
        }

        // Calculate panel size
        int panelWidth = 420;
        int panelHeight = isHealPotion ? 480 : 420; // Extra space for default potion button

        // Position panel to the right of the click position
        int offsetX = 70;
        int panelX = centerX + offsetX;
        int panelY = centerY - panelHeight / 2;

        panelBounds.set(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight
        );

        // Calculate button positions
        int buttonWidth = 120;
        int buttonHeight = 45;
        int bottomMargin = 20;

        // Use button (left)
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

        // Default potion button (above use/drop buttons, only for heal potions)
        if (isHealPotion) {
            int defaultBtnY = panelBounds.bottom - bottomMargin - buttonHeight - 60;
            defaultPotionButton.set(
                    panelBounds.left + 30,
                    defaultBtnY,
                    panelBounds.right - 30,
                    defaultBtnY + buttonHeight
            );
        } else {
            defaultPotionButton.setEmpty();
        }
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

        // Use local reference to avoid NPE from concurrent hide() call
        ItemStack localItem = selectedItem;
        if (localItem == null) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Item item = localItem.getItem();
        if (item == null) return;

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
        canvas.drawText(ItemsUtil.getRarityText(item.getRarity()), panelBounds.centerX(), panelBounds.top + 70, paint);

        // Divider line
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(150, 150, 150));
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 85,
                panelBounds.right - 20, panelBounds.top + 85, paint);

        // Description
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.LEFT);

        // Wrap description text
        String description = item.getDescription();
        drawWrappedText(canvas, paint, description, panelBounds.left + 25, panelBounds.top + 115,
                panelBounds.width() - 50, 28);

        // Additional info section
        int infoStartY = panelBounds.top + 180;

        paint.setColor(Color.YELLOW);
        paint.setTextSize(18);
        canvas.drawText("类型: " + getTypeText(item.getType()),
                panelBounds.left + 25, infoStartY, paint);

        canvas.drawText("价值: " + item.getValue() + " 金币",
                panelBounds.left + 25, infoStartY + 30, paint);

        if (localItem.getQuantity() > 1) {
            canvas.drawText("数量: " + localItem.getQuantity(),
                    panelBounds.left + 25, infoStartY + 60, paint);
        }

        // Stackable info
        canvas.drawText("可堆叠: " + (item.getMaxStack() > 1 ? "是 (最大" + item.getMaxStack() + ")" : "否"),
                panelBounds.left + 25, infoStartY + 90, paint);

        // Default potion order info
        int defaultInfoY = infoStartY + 120;
        List<Integer> hpIds = ItemSystem.getInstance().getDefaultHpPotionIds();
        List<Integer> mpIds = ItemSystem.getInstance().getDefaultMpPotionIds();
        int hpIndex = hpIds.indexOf(item.getId());
        int mpIndex = mpIds.indexOf(item.getId());
        if (hpIndex >= 0) {
            paint.setColor(Color.WHITE);
            canvas.drawText("★ 默认气血药 序号: " + (hpIndex + 1),
                    panelBounds.left + 25, defaultInfoY, paint);
        }
        if (mpIndex >= 0) {
            paint.setColor(Color.WHITE);
            canvas.drawText("★ 默认魔法药 序号: " + (mpIndex + 1),
                    panelBounds.left + 25, defaultInfoY + (hpIndex >= 0 ? 28 : 0), paint);
        }

        // Draw buttons
        drawButtons(canvas, paint, item);
    }

    /**
     * Draw action buttons
     */
    private void drawButtons(Canvas canvas, Paint paint, Item item) {
        drawButton(canvas, paint, useButton, "✨ 使用", Color.rgb(50, 150, 255));
        drawButton(canvas, paint, dropButton, "🗑️ 丢弃", Color.rgb(200, 80, 80));

        // Draw default potion button if visible (for consumable HP/MP items)
        if (item instanceof ConsumableItem && defaultPotionButton.width() > 0) {
            ConsumableItem ci = (ConsumableItem) item;
            boolean isHp = ci.getEffectType() == ConsumableItem.EffectType.HEAL_HP;
            boolean isDefault = isHp ? isDefaultHp : isDefaultMp;
            String btnText;
            int btnColor;
            if (isDefault) {
                btnText = "取消默认" + (isHp ? "气血药" : "魔法药");
                btnColor = Color.rgb(200, 100, 50);
            } else {
                btnText = "设为默认" + (isHp ? "气血药" : "魔法药");
                btnColor = Color.rgb(50, 180, 100);
            }
            drawButton(canvas, paint, defaultPotionButton, btnText, btnColor);
        }
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

        // Check if touch is outside the info panel - close the panel
        if (!TouchUtil.checkIsInTouchRectFloat(panelBounds, x, y)) {
            hide();
            return true;
        }

        // Check use/unequip button
        if (TouchUtil.checkIsInTouchRectFloat(useButton, x, y)) {
            if (listener != null && selectedItem != null) {
                listener.onUseItem(selectedItem);
            }
            hide();
            return true;
        }

        // Check drop button (only for non-equipped items)
        if (TouchUtil.checkIsInTouchRectFloat(dropButton, x, y)) {
            if (listener != null && selectedItem != null) {
                listener.onDropItem(selectedItem);
            }
            hide();
            return true;
        }

        // Check default potion button
        if (defaultPotionButton.width() > 0 && TouchUtil.checkIsInTouchRectFloat(defaultPotionButton, x, y)) {
            if (listener != null && selectedItem != null && selectedItem.getItem() instanceof ConsumableItem) {
                ConsumableItem ci = (ConsumableItem) selectedItem.getItem();
                if (ci.getEffectType() == ConsumableItem.EffectType.HEAL_HP) {
                    listener.onSetDefaultHp(selectedItem);
                } else if (ci.getEffectType() == ConsumableItem.EffectType.HEAL_MP) {
                    listener.onSetDefaultMp(selectedItem);
                }
            }
            hide();
            return true;
        }

        // Touch inside panel but not on buttons - just consume the event
        return true;
    }

}

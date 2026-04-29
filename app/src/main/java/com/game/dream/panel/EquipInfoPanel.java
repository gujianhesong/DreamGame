package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;

/**
 * Equipment information panel - displays detailed equipment stats
 */
public class EquipInfoPanel {
    private ItemStack selectedEquipment;
    private boolean isVisible;
    private Rect panelBounds;

    // Buttons
    private Rect actionButton;
    private Rect dropButton;

    // Track if this is an equipped item
    private boolean isEquipped;
    private int inventoryIndex; // For backpack items

    // Callback interface
    public interface EquipActionListener {
        void onUnequip(EquipmentItem equipment);
        void onEquip(EquipmentItem equipment, int index);
        void onDrop(EquipmentItem equipment, int index);
    }

    private EquipActionListener listener;

    public EquipInfoPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.actionButton = new Rect();
        this.dropButton = new Rect();
        this.isEquipped = false;
        this.inventoryIndex = -1;
    }

    /**
     * Show equipment info panel
     */
    public void show(EquipmentItem equipment, boolean isEquipped, int inventoryIndex, int centerX, int centerY, EquipActionListener listener) {
        this.selectedEquipment = new ItemStack(equipment, 1);
        this.listener = listener;
        this.isVisible = true;
        this.isEquipped = isEquipped;
        this.inventoryIndex = inventoryIndex;

        // Calculate panel size (larger than item info for more stats)
        int panelWidth = 420;
        int panelHeight = 450;

        // Position panel to the right of the click position
        int offsetX = 50;
        int panelX = centerX + offsetX;
        int panelY = centerY - panelHeight / 2; // Center vertically relative to click

        panelBounds.set(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight
        );

        // Calculate button positions based on state
        int buttonWidth = 150;
        int buttonHeight = 50;
        int bottomMargin = 35;

        if (isEquipped) {
            // For equipped items: single "Unequip" button centered
            actionButton.set(
                    panelBounds.centerX() - buttonWidth / 2,
                    panelBounds.bottom - bottomMargin - buttonHeight,
                    panelBounds.centerX() + buttonWidth / 2,
                    panelBounds.bottom - bottomMargin
            );
            // Hide drop button
            dropButton.set(0, 0, 0, 0);
        } else {
            // For inventory items: "Equip" (left) and "Drop" (right)
            actionButton.set(
                    panelBounds.left + 30,
                    panelBounds.bottom - bottomMargin - buttonHeight,
                    panelBounds.left + 30 + buttonWidth,
                    panelBounds.bottom - bottomMargin
            );

            dropButton.set(
                    panelBounds.right - 30 - buttonWidth,
                    panelBounds.bottom - bottomMargin - buttonHeight,
                    panelBounds.right - 30,
                    panelBounds.bottom - bottomMargin
            );
        }
    }

    /**
     * Hide panel
     */
    public void hide() {
        isVisible = false;
        selectedEquipment = null;
    }

    public boolean isVisible() { return isVisible; }

    /**
     * Draw the equipment info panel
     */
    public void draw(Canvas canvas) {
        if (!isVisible || selectedEquipment == null) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        EquipmentItem equipment = (EquipmentItem)selectedEquipment.getItem();

        // Panel background (semi-transparent dark)
        paint.setColor(Color.argb(190, 20, 25, 35));
        float cornerRadius = 15f;
        canvas.drawRoundRect(panelBounds.left, panelBounds.top,
                panelBounds.right, panelBounds.bottom,
                cornerRadius, cornerRadius, paint);

        // Border with rarity color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(equipment.getColor());
        canvas.drawRoundRect(panelBounds.left, panelBounds.top,
                panelBounds.right, panelBounds.bottom,
                cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        // Equipment name (with rarity color, larger font)
        paint.setColor(equipment.getColor());
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(equipment.getName(), panelBounds.centerX(), panelBounds.top + 50, paint);
        paint.setFakeBoldText(false);

        // Rarity and slot label
        paint.setTextSize(18);
        paint.setColor(Color.WHITE);
        String slotText = getSlotText(equipment.getSlot());
        canvas.drawText(getRarityText(equipment.getRarity()) + " · " + slotText,
                panelBounds.centerX(), panelBounds.top + 78, paint);

        // Divider line
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawLine(panelBounds.left + 25, panelBounds.top + 95,
                panelBounds.right - 25, panelBounds.top + 95, paint);

        // Stats section
        int statsStartY = panelBounds.top + 130;
        drawStatsSection(canvas, paint, equipment, statsStartY);

        // Description section
        int descStartY = statsStartY + 160;

        paint.setColor(Color.rgb(180, 180, 200));
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("描述:", panelBounds.left + 25, descStartY, paint);

        paint.setColor(Color.rgb(200, 200, 220));
        paint.setTextSize(19);
        drawWrappedText(canvas, paint, equipment.getDescription(),
                panelBounds.left + 25, descStartY + 30,
                panelBounds.width() - 50, 26);

        // Additional info
        int infoStartY = descStartY + 100;

        paint.setColor(Color.rgb(150, 150, 180));
        paint.setTextSize(17);
        canvas.drawText("价值: " + equipment.getValue() + " 金币",
                panelBounds.left + 25, infoStartY, paint);

        // Draw action buttons (Equip/Unequip + Drop)
        drawButtons(canvas, paint, equipment);
    }

    /**
     * Draw equipment stats section
     */
    private void drawStatsSection(Canvas canvas, Paint paint, EquipmentItem equipment, int startY) {
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.LEFT);

        int currentY = startY;
        int lineHeight = 35;

        EquipItemInfo equipItemInfo = equipment.getEquipItemInfo();

        // hp bonus
        if (equipItemInfo.getHp() > 0) {
            paint.setColor(Color.rgb(255, 100, 100)); // Red for attack
            canvas.drawText("气血: +" + equipItemInfo.getHp(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // mp bonus
        if (equipItemInfo.getMp() > 0) {
            paint.setColor(Color.rgb(255, 100, 100)); // Red for attack
            canvas.drawText("魔法: +" + equipItemInfo.getMp(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // Hit bonus
        if (equipItemInfo.getHit() > 0) {
            paint.setColor(Color.rgb(255, 100, 100)); // Red for attack
            canvas.drawText("命中: +" + equipItemInfo.getHit(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // Attack bonus
        if (equipItemInfo.getAttack() > 0) {
            paint.setColor(Color.rgb(255, 100, 100)); // Red for attack
            canvas.drawText("伤害: +" + equipItemInfo.getAttack(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // Defense bonus
        if (equipItemInfo.getDefense() > 0) {
            paint.setColor(Color.rgb(100, 150, 255)); // Blue for defense
            canvas.drawText("防御: +" + equipItemInfo.getDefense(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // Magic bonus
        if (equipItemInfo.getMana() > 0) {
            paint.setColor(Color.rgb(200, 100, 255)); // Purple for magic
            canvas.drawText("灵力: +" + equipItemInfo.getMana(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // Speed bonus
        if (equipItemInfo.getSpeed() > 0) {
            paint.setColor(Color.rgb(100, 255, 100)); // Green for speed
            canvas.drawText("速度: +" + equipItemInfo.getSpeed(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // Dodge bonus
        if (equipItemInfo.getDodge() > 0) {
            paint.setColor(Color.rgb(100, 255, 100)); // Green for speed
            canvas.drawText("闪避: +" + equipItemInfo.getDodge(),
                    panelBounds.left + 25, currentY, paint);
            currentY += lineHeight;
        }

        // If no bonuses, show a message
//        if (equipment.getAttackBonus() == 0 && equipment.getDefenseBonus() == 0 &&
//                equipment.getMagicBonus() == 0 && equipment.getSpeedBonus() == 0) {
//            paint.setColor(Color.rgb(150, 150, 150));
//            canvas.drawText("无额外属性加成", panelBounds.left + 25, currentY, paint);
//        }
    }

    /**
     * Draw action buttons
     */
    private void drawButtons(Canvas canvas, Paint paint, EquipmentItem equipment) {
        if (isEquipped) {
            // For equipped items: show "卸下装备" button
            drawButton(canvas, paint, actionButton, "🔓 卸下装备", Color.rgb(220, 160, 50));
        } else {
            // For inventory items: show "穿戴" and "丢弃"
            drawButton(canvas, paint, actionButton, "🛡️ 穿戴", Color.rgb(50, 150, 255));
            drawButton(canvas, paint, dropButton, "🗑️ 丢弃", Color.rgb(200, 80, 80));
        }
    }

    /**
     * Draw a single button
     */
    private void drawButton(Canvas canvas, Paint paint, Rect button, String text, int color) {
        if (button.width() <= 0 || button.height() <= 0) return; // Skip hidden buttons

        // Button background
        paint.setColor(Color.argb(210, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawRoundRect(button.left, button.top,
                button.right, button.bottom, 10, 10, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(button.left, button.top,
                button.right, button.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.FILL);

        // Text
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = button.centerY() + 9;
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
            case Rarity_1: return "普通";
            case Rarity_2: return "优秀";
            case Rarity_3: return "稀有";
            case Rarity_4: return "史诗";
            case Rarity_5: return "传说";
            default: return "";
        }
    }

    /**
     * Get slot text
     */
    private String getSlotText(EquipmentItem.Slot slot) {
        switch (slot) {
            case HELMET: return "头盔";
            case ACCESSORY: return "饰品";
            case WEAPON: return "武器";
            case ARMOR: return "铠甲";
            case BELT: return "腰带";
            case SHOES: return "鞋子";
            default: return "未知";
        }
    }

    /**
     * Handle touch event
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
        if (!panelBounds.contains((int)x, (int)y)) {
            hide();
            return true;
        }

        // Check action button (Unequip or Equip)
        if (actionButton.contains((int)x, (int)y)) {
            if (listener != null && selectedEquipment != null) {
                EquipmentItem equipment = (EquipmentItem)selectedEquipment.getItem();
                if (isEquipped) {
                    listener.onUnequip(equipment);
                } else {
                    listener.onEquip(equipment, inventoryIndex);
                }
            }
            hide();
            return true;
        }

        // Check drop button (only for inventory items)
        if (!isEquipped && dropButton.contains((int)x, (int)y)) {
            if (listener != null && selectedEquipment != null) {
                EquipmentItem equipment = (EquipmentItem)selectedEquipment.getItem();
                listener.onDrop(equipment, inventoryIndex);
            }
            hide();
            return true;
        }

        // Touch inside panel but not on buttons - just consume the event
        return true;
    }
}

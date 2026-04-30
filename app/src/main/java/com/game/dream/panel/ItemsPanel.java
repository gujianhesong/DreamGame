package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;

import java.util.List;

/**
 * Equipment and Inventory Panel
 */
public class ItemsPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    // Equipment slots (left side)
    private Rect helmetSlot;
    private Rect necklaceSlot;
    private Rect weaponSlot;
    private Rect armorSlot;
    private Rect beltSlot;
    private Rect shoesSlot;

    // Money display area
    private Rect moneyArea;

    // Inventory grid (right side)
    private Rect[][] inventorySlots;
    private static final int INVENTORY_COLS = 6; // Increased from 5 to 6
    private static final int INVENTORY_ROWS = 5; // Reduced from 6 to 5 to fit better
    private static final int SLOT_SIZE = 120; // Slightly larger slots
    private static final int SLOT_GAP = 8;
    private static final int SLOT_GAP_EQUIP = 20;

    // Item info popup
    private ItemInfoPanel itemInfoPanel;
    // Equipment info popup
    private EquipInfoPanel equipInfoPanel;

    public ItemsPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.inventorySlots = new Rect[INVENTORY_ROWS][INVENTORY_COLS];
        this.itemInfoPanel = new ItemInfoPanel();
        this.equipInfoPanel = new EquipInfoPanel();
    }

    /**
     * Toggle panel visibility
     */
    public void toggleVisibility() {
        isVisible = !isVisible;
    }

    public void show() { isVisible = true; }
    public void hide() { isVisible = false; }
    public boolean isVisible() { return isVisible; }

    /**
     * Set panel bounds and calculate slot positions
     */
    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        // Close button (top-right)
        int buttonSize = 40;
        int padding = 10;
        closeButton.set(
                panelBounds.right - buttonSize - padding,
                panelBounds.top + padding,
                panelBounds.right - padding,
                panelBounds.top + padding + buttonSize
        );

        // Calculate equipment slots (left side) - 3 rows x 2 columns
        int leftPanelWidth = (int) (width * 0.28f);
        int slotWidth = SLOT_SIZE;
        int slotHeight = SLOT_SIZE;
        int slotGapX = SLOT_GAP_EQUIP; // Horizontal gap between columns
        int slotGapY = SLOT_GAP_EQUIP; // Vertical gap between rows

        // Calculate starting position to center the 2-column grid
        int totalWidth = slotWidth * 2 + slotGapX;
        int startX = x + (leftPanelWidth - totalWidth) / 2;
        int startY = y + 140;

        // Row 1: Helmet, Necklace
        helmetSlot = new Rect(startX, startY,
                startX + slotWidth, startY + slotHeight);
        necklaceSlot = new Rect(startX + slotWidth + slotGapX, startY,
                startX + slotWidth * 2 + slotGapX, startY + slotHeight);

        // Row 2: Weapon, Armor
        int row2Y = startY + slotHeight + slotGapY;
        weaponSlot = new Rect(startX, row2Y,
                startX + slotWidth, row2Y + slotHeight);
        armorSlot = new Rect(startX + slotWidth + slotGapX, row2Y,
                startX + slotWidth * 2 + slotGapX, row2Y + slotHeight);

        // Row 3: Belt, Shoes
        int row3Y = row2Y + slotHeight + slotGapY;
        beltSlot = new Rect(startX, row3Y,
                startX + slotWidth, row3Y + slotHeight);
        shoesSlot = new Rect(startX + slotWidth + slotGapX, row3Y,
                startX + slotWidth * 2 + slotGapX, row3Y + slotHeight);

        // Money area (right side, top) - moved higher
        int rightPanelStartX = x + leftPanelWidth + 20;
        int moneyHeight = 50;
        moneyArea = new Rect(rightPanelStartX, y + 80,
                x + width - 240, y + 80 + moneyHeight);

        // Inventory grid (right side, below money with more gap)
        int inventoryStartX = rightPanelStartX;
        int inventoryStartY = y + 80 + moneyHeight + 60; // Added 30px gap

        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotX = inventoryStartX + col * (SLOT_SIZE + SLOT_GAP);
                int slotY = inventoryStartY + row * (SLOT_SIZE + SLOT_GAP);
                inventorySlots[row][col] = new Rect(slotX, slotY,
                        slotX + SLOT_SIZE, slotY + SLOT_SIZE);
            }
        }
    }

    /**
     * Draw the equipment panel
     */
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
        canvas.drawText("装备与背包", panelBounds.centerX(), panelBounds.top + 50, paint);

        // Divider line
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 65,
                panelBounds.right - 20, panelBounds.top + 65, paint);

        // Draw equipment section
        drawEquipmentSection(canvas, paint);

        // Draw money
        drawMoneySection(canvas, paint);

        // Draw inventory grid
        drawInventoryGrid(canvas, paint);

        // Draw item info panel (on top of everything)
        if (itemInfoPanel != null && itemInfoPanel.isVisible()) {
            itemInfoPanel.draw(canvas);
        }

        // Draw equipment info panel (on top of everything)
        if (equipInfoPanel != null && equipInfoPanel.isVisible()) {
            equipInfoPanel.draw(canvas);
        }
    }

    /**
     * Draw equipment slots
     */
    private void drawEquipmentSection(Canvas canvas, Paint paint) {
        // Section title
        paint.setColor(Color.rgb(200, 200, 220));
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("装备栏", helmetSlot.centerX() - 20, helmetSlot.top - 20, paint);

        // Draw each equipment slot
        drawEquipmentSlot(canvas, paint, helmetSlot, "头盔",
                ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.HELMET));
        drawEquipmentSlot(canvas, paint, necklaceSlot, "项链",
                ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ACCESSORY));
        drawEquipmentSlot(canvas, paint, weaponSlot, "武器",
                ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON));
        drawEquipmentSlot(canvas, paint, armorSlot, "铠甲",
                ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ARMOR));
        drawEquipmentSlot(canvas, paint, beltSlot, "腰带",
                ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.BELT));
        drawEquipmentSlot(canvas, paint, shoesSlot, "鞋子",
                ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.SHOES));
    }

    /**
     * Draw a single equipment slot
     */
    private void drawEquipmentSlot(Canvas canvas, Paint paint, Rect slot,
                                   String label, EquipmentItem equipped) {
        // Slot background - lighter when empty
        if (equipped != null) {
            paint.setColor(Color.argb(100, 40, 40, 50));
        } else {
            paint.setColor(Color.argb(60, 60, 60, 70)); // Lighter background for empty slots
        }
        canvas.drawRoundRect(slot.left, slot.top, slot.right, slot.bottom, 8, 8, paint);

        // Slot border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        if (equipped != null) {
            // Colored border based on rarity (no fill)
            paint.setColor(equipped.getColor());
            paint.setStrokeWidth(3);
        } else {
            paint.setColor(Color.rgb(150, 150, 170)); // Brighter border for empty slots
            paint.setStrokeWidth(2);
        }

        canvas.drawRoundRect(slot.left, slot.top, slot.right, slot.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        if (equipped != null) {
            // Draw equipped item name (no background box)
            paint.setColor(equipped.getColor());
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.CENTER);

            // Item name (truncated if too long)
            String itemName = equipped.getName();
            if (itemName.length() > 5) {
                itemName = itemName.substring(0, 5) + "..";
            }
            canvas.drawText(itemName, slot.centerX(), slot.centerY() + 5, paint);

            // Rarity indicator
            paint.setTextSize(14);
            paint.setColor(Color.WHITE);
            canvas.drawText(getRarityText(equipped.getRarity()),
                    slot.centerX(), slot.bottom - 8, paint);
        } else {
            // Empty slot - draw label with brighter color
            paint.setColor(Color.argb(220, 200, 200, 220)); // Much brighter text
            paint.setTextSize(18); // Slightly larger font
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(label, slot.centerX(), slot.centerY() + 7, paint);
        }
    }

    /**
     * Draw money section
     */
    private void drawMoneySection(Canvas canvas, Paint paint) {
        // Background
        paint.setColor(Color.argb(120, 40, 40, 50));
        canvas.drawRoundRect(moneyArea.left, moneyArea.top,
                moneyArea.right, moneyArea.bottom, 10, 10, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawRoundRect(moneyArea.left, moneyArea.top,
                moneyArea.right, moneyArea.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.FILL);

        // Money text
        paint.setColor(Color.rgb(255, 215, 0));
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.LEFT);

        long money = RoleSystem.getInstance().getRoleInfo().getMoney();
        canvas.drawText("💰 金钱: " + money, moneyArea.left + 15,
                moneyArea.centerY() + 10, paint);
    }

    /**
     * Draw inventory grid
     */
    private void drawInventoryGrid(Canvas canvas, Paint paint) {
        // Section title - positioned above the grid
        paint.setColor(Color.rgb(200, 200, 220));
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("背包 (" + ItemSystem.getInstance().getSize() + "/" + ItemSystem.getInstance().getMaxSize() + ")",
                inventorySlots[0][0].left, inventorySlots[0][0].top - 15, paint);

        List<ItemStack> items = ItemSystem.getInstance().getItems();

        // Draw each inventory slot
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                Rect slot = inventorySlots[row][col];
                int index = row * INVENTORY_COLS + col;

                // Slot background (always draw)
                paint.setColor(Color.argb(80, 30, 30, 40));
                canvas.drawRoundRect(slot.left, slot.top, slot.right, slot.bottom, 5, 5, paint);

                // Slot border (default)
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setColor(Color.rgb(60, 60, 80));

                // Draw item if exists
                if (index < items.size()) {
                    ItemStack stack = items.get(index);
                    Item item = stack.getItem();

                    // Item border color based on rarity (draw border only, no fill)
                    paint.setColor(item.getColor());
                    paint.setStrokeWidth(3);
                    canvas.drawRoundRect(slot.left + 2, slot.top + 2,
                            slot.right - 2, slot.bottom - 2, 4, 4, paint);

                    // Item name
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(18);
                    paint.setTextAlign(Paint.Align.CENTER);

                    String itemName = item.getName();
                    if (itemName.length() > 5) {
                        itemName = itemName.substring(0, 5) + "..";
                    }
                    canvas.drawText(itemName, slot.centerX(), slot.centerY() - 2, paint);

                    // Quantity
                    if (stack.getQuantity() > 1) {
                        paint.setTextSize(14);
                        paint.setColor(Color.YELLOW);
                        paint.setTextAlign(Paint.Align.RIGHT);
                        canvas.drawText("x" + stack.getQuantity(),
                                slot.right - 5, slot.bottom - 5, paint);
                    }
                } else {
                    // Empty slot - draw default border
                    canvas.drawRoundRect(slot.left, slot.top, slot.right, slot.bottom, 5, 5, paint);
                }

                // Reset paint style
                paint.setStyle(Paint.Style.FILL);
            }
        }
    }

    /**
     * Draw close button
     */
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
     * Handle touch event
     */
    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        // If item info panel is visible, let it handle the touch first
        if (itemInfoPanel != null && itemInfoPanel.isVisible()) {
            // Pass parent panel bounds to detect outside clicks
            return itemInfoPanel.handleTouch(x, y,
                    panelBounds.left, panelBounds.top,
                    panelBounds.right, panelBounds.bottom);
        }
        // If equipment info panel is visible, let it handle the touch first
        if (equipInfoPanel != null && equipInfoPanel.isVisible()) {
            return equipInfoPanel.handleTouch(x, y,
                    panelBounds.left, panelBounds.top,
                    panelBounds.right, panelBounds.bottom);
        }

        // Check close button
        if (closeButton.contains((int)x, (int)y)) {
            hide();
            return true;
        }

        // Check equipment slots - show info or unequip
        if (helmetSlot.contains((int)x, (int)y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.HELMET);
            if (equipped != null) {
                // Show item info for equipped item
                showEquipmentInfo(equipped, true, -1, helmetSlot.centerX(), helmetSlot.centerY());
            } else {
                // Empty slot - do nothing or show hint
            }
            return true;
        }

        if (necklaceSlot.contains((int)x, (int)y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ACCESSORY);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, necklaceSlot.centerX(), necklaceSlot.centerY());
            }
            return true;
        }

        if (weaponSlot.contains((int)x, (int)y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, weaponSlot.centerX(), weaponSlot.centerY());
            }
            return true;
        }

        if (armorSlot.contains((int)x, (int)y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ARMOR);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, armorSlot.centerX(), armorSlot.centerY());
            }
            return true;
        }

        if (beltSlot.contains((int)x, (int)y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.BELT);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, beltSlot.centerX(), beltSlot.centerY());
            }
            return true;
        }

        if (shoesSlot.contains((int)x, (int)y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.SHOES);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, shoesSlot.centerX(), shoesSlot.centerY());
            }
            return true;
        }

        // Check inventory slots (use or equip)
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                if (inventorySlots[row][col].contains((int)x, (int)y)) {
                    int index = row * INVENTORY_COLS + col;
                    if (index < ItemSystem.getInstance().getItems().size()) {
                        // Show appropriate info panel based on item type
                        ItemStack stack = ItemSystem.getInstance().getItems().get(index);
                        int centerX = inventorySlots[row][col].centerX();
                        int centerY = inventorySlots[row][col].centerY();
                        if (stack.getItem().getType() == Item.Type.EQUIPMENT) {
                            // For equipment in inventory, show item info with equip option
                            EquipmentItem equipmentItem = (EquipmentItem) stack.getItem();
                            showEquipmentInfo(equipmentItem, false, index, centerX, centerY);
                        } else {
                            // For non-equipment items, show simple item info
                            showItemInfo(stack, index, centerX, centerY);
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Show equipment info panel for equipped item
     */
    private void showEquipmentInfo(EquipmentItem equipment, boolean isEquiped, int inventoryIndex, int centerX, int centerY) {
        if (equipInfoPanel != null) {
            equipInfoPanel.show(equipment, isEquiped, inventoryIndex, centerX, centerY,
                    new EquipInfoPanel.EquipActionListener() {
                        @Override
                        public void onUnequip(EquipmentItem equipment) {
                            ItemSystem.getInstance().unequipSlot(equipment.getSlot());
                        }

                        @Override
                        public void onEquip(EquipmentItem equipment, int index) {
                            ItemSystem.getInstance().equipItem(index);
                        }

                        @Override
                        public void onDrop(EquipmentItem equipment, int index) {
                            ItemSystem.getInstance().removeItem(equipment.getId(), 1);
                        }
                    }
            );
        }
    }

    /**
     * Show item info panel for equipped item
     */
    private void showItemInfo(ItemStack stack, int index, int centerX, int centerY) {
        if (itemInfoPanel != null) {
            itemInfoPanel.show(stack,
                    centerX,
                    centerY,
                    new ItemInfoPanel.ItemActionListener() {
                        @Override
                        public void onUseItem(ItemStack item) {
                            ItemSystem.getInstance().useItem(index);
                        }

                        @Override
                        public void onDropItem(ItemStack item) {
                            ItemSystem.getInstance().removeItem(item.getItem().getId(), 1);
                        }
                    }
            );
        }
    }
}

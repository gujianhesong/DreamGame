package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

import com.game.dream.GameEngine;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.enums.GemtoneType;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.item.ItemStack;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.DialogBox;
import com.game.dream.utils.EquipUtil;
import com.game.dream.utils.ItemsUtil;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // Category tabs
    private static final String[] TAB_NAMES = {"药品", "装备", "物品", "材料", "任务"};
    private static final int TAB_HEIGHT = 40;
    private static final int TAB_GAP = 4;
    private Rect[][] tabBounds;
    private volatile int selectedTab = 0;

    // Scrolling support for inventory
    private float inventoryScrollOffset = 0;
    private float maxInventoryScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;

    // For detecting click vs scroll
    private float touchDownX = 0;
    private float touchDownY = 0;
    private boolean isTouchingInventory = false;

    // Item info popup
    private ItemInfoPanel itemInfoPanel;
    // Equipment info popup
    private EquipInfoPanel equipInfoPanel;

    // Multi-select mode
    private boolean isMultiSelectMode = false;
    private Set<ItemStack> selectedItems = new HashSet<>();
    private Rect multiSelectButton; // “多选”按钮
    private Rect selectAllButton;   // “全选”按钮
    private Rect discardButton;     // “丢弃”按钮
    private boolean allSelected = false; // 当前是否全选状态

    public ItemsPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.inventorySlots = new Rect[INVENTORY_ROWS][INVENTORY_COLS];
        this.tabBounds = new Rect[1][TAB_NAMES.length];
        this.itemInfoPanel = new ItemInfoPanel();
        this.equipInfoPanel = new EquipInfoPanel();
        this.multiSelectButton = new Rect();
        this.selectAllButton = new Rect();
        this.discardButton = new Rect();
    }

    /**
     * Toggle panel visibility
     */
    public void toggleVisibility() {
        isVisible = !isVisible;
        if (isVisible) {
            inventoryScrollOffset = 0;
        } else {
            exitMultiSelectMode();
        }
    }

    public void show() {
        isVisible = true;
        inventoryScrollOffset = 0;
    }

    public void hide() {
        isVisible = false;
        exitMultiSelectMode();
    }

    private void exitMultiSelectMode() {
        isMultiSelectMode = false;
        selectedItems.clear();
        allSelected = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

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

        // Multi-select button (right of money area)
        int msBtnGap = 10;
        int msBtnWidth = 80;
        multiSelectButton.set(
                moneyArea.right + msBtnGap,
                moneyArea.top,
                moneyArea.right + msBtnGap + msBtnWidth,
                moneyArea.bottom
        );

        // Category tabs (below money area)
        int tabStartY = moneyArea.bottom + 15;
        int totalTabGap = TAB_GAP * (TAB_NAMES.length - 1);
        int availableTabWidth = (x + width - 240) - rightPanelStartX;
        int tabWidth = (availableTabWidth - totalTabGap) / TAB_NAMES.length;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = rightPanelStartX + i * (tabWidth + TAB_GAP);
            tabBounds[0][i] = new Rect(tabX, tabStartY, tabX + tabWidth, tabStartY + TAB_HEIGHT);
        }

        // Inventory grid (right side, below tabs)
        int inventoryStartX = rightPanelStartX;
        int inventoryStartY = tabStartY + TAB_HEIGHT + 20;

        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int slotX = inventoryStartX + col * (SLOT_SIZE + SLOT_GAP);
                int slotY = inventoryStartY + row * (SLOT_SIZE + SLOT_GAP);
                inventorySlots[row][col] = new Rect(slotX, slotY,
                        slotX + SLOT_SIZE, slotY + SLOT_SIZE);
            }
        }

        // Bottom action buttons (below inventory grid, only visible in multi-select mode)
        int bottomY = inventoryStartY + INVENTORY_ROWS * (SLOT_SIZE + SLOT_GAP) + 10;
        int actionBtnWidth = 140;
        int actionBtnHeight = 44;
        int bottomCenterX = (inventoryStartX + inventorySlots[INVENTORY_ROWS - 1][INVENTORY_COLS - 1].right) / 2 + inventoryStartX / 2;
        // Actually center between inventory start and end
        int invCenterX = (inventoryStartX + inventorySlots[0][INVENTORY_COLS - 1].right) / 2;
        selectAllButton.set(
                invCenterX - actionBtnWidth - 10,
                bottomY,
                invCenterX - 10,
                bottomY + actionBtnHeight
        );
        discardButton.set(
                invCenterX + 10,
                bottomY,
                invCenterX + 10 + actionBtnWidth,
                bottomY + actionBtnHeight
        );
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

        // Multi-select button
        drawMultiSelectButton(canvas, paint);

        // Divider line
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(80, 80, 100));
        canvas.drawLine(panelBounds.left + 20, panelBounds.top + 65,
                panelBounds.right - 20, panelBounds.top + 65, paint);

        // Draw equipment section
        drawEquipmentSection(canvas, paint);

        // Draw money
        drawMoneySection(canvas, paint);

        // Draw category tabs
        drawCategoryTabs(canvas, paint);

        // Draw inventory grid
        drawInventoryGrid(canvas, paint);

        // Draw multi-select mode bottom buttons
        if (isMultiSelectMode) {
            drawMultiSelectActions(canvas, paint);
        }

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
            paint.setColor(Color.WHITE);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.CENTER);

            // Item name (truncated if too long)
            String itemName = equipped.getName();
            canvas.drawText(itemName, slot.centerX(), slot.centerY() + 5, paint);

            // Rarity indicator
            paint.setTextSize(16);
            canvas.drawText(ItemsUtil.getRarityText(equipped.getRarity()),
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
        float moneyWidth = paint.measureText("💰 金钱: " + money);
        canvas.drawText("💰 金钱: " + money, moneyArea.left + 15,
                moneyArea.centerY() + 10, paint);

        // Item count after money
        List<ItemStack> displayItems = getCategoryItems(selectedTab);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setTextSize(28);
        canvas.drawText("  物品: " + displayItems.size() + "/" + ItemSystem.getInstance().getMaxSize(),
                moneyArea.left + 15 + moneyWidth + 50, moneyArea.centerY() + 10, paint);
    }

    /**
     * Draw category tabs for inventory filtering
     */
    private void drawCategoryTabs(Canvas canvas, Paint paint) {
        for (int i = 0; i < TAB_NAMES.length; i++) {
            Rect tab = tabBounds[0][i];
            boolean isSelected = (i == selectedTab);

            // Tab background
            paint.setColor(isSelected ? Color.argb(200, 40, 80, 140) : Color.argb(100, 40, 40, 50));
            canvas.drawRoundRect(tab.left, tab.top, tab.right, tab.bottom, 6, 6, paint);

            // Tab border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(isSelected ? 3 : 1);
            paint.setColor(isSelected ? Color.rgb(100, 180, 255) : Color.rgb(200, 200, 200));
            canvas.drawRoundRect(tab.left, tab.top, tab.right, tab.bottom, 6, 6, paint);
            paint.setStyle(Paint.Style.FILL);

            // Tab text
            paint.setColor(Color.WHITE);
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(TAB_NAMES[i], tab.centerX(), tab.centerY() + 7, paint);
        }
    }

    /**
     * Get items matching the given category tab (creates fresh list each call)
     */
    private List<ItemStack> getCategoryItems(int tabIndex) {
        List<ItemStack> allItems = ItemSystem.getInstance().getItems();
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < allItems.size(); i++) {
            try {
                ItemStack stack = allItems.get(i);
                if (isTypeInCategory(stack.getItem().getType(), tabIndex)) {
                    result.add(stack);
                }
            } catch (Exception e) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if item type belongs to the given category
     */
    private boolean isTypeInCategory(Item.Type type, int tabIndex) {
        switch (tabIndex) {
            case 0: // 药品
                return type == Item.Type.CONSUMABLE || type == Item.Type.FOOD;
            case 1: // 装备
                return type == Item.Type.EQUIPMENT;
            case 2: // 物品
                return type == Item.Type.SKILL_BOOK || type == Item.Type.SPECIAL;
            case 3: // 材料
                return type == Item.Type.MATERIAL;
            case 4: // 任务
                return type == Item.Type.QUEST_ITEM;
            default:
                return false;
        }
    }

    /**
     * Draw inventory grid
     */
    private void drawInventoryGrid(Canvas canvas, Paint paint) {
        List<ItemStack> displayItems = getCategoryItems(selectedTab);

        // 1. Calculate total rows needed based on actual item count
        int totalRowsNeeded = (int) Math.ceil((double) displayItems.size() / INVENTORY_COLS);
        if (totalRowsNeeded < INVENTORY_ROWS)
            totalRowsNeeded = INVENTORY_ROWS; // Keep at least the visible area

        // 2. Update max scroll offset
        int totalContentHeight = totalRowsNeeded * (SLOT_SIZE + SLOT_GAP);
        int visibleHeight = INVENTORY_ROWS * (SLOT_SIZE + SLOT_GAP);
        maxInventoryScrollOffset = Math.max(0, totalContentHeight - visibleHeight);

        // Clamp scroll offset
        inventoryScrollOffset = Math.max(0, Math.min(inventoryScrollOffset, maxInventoryScrollOffset));

        // 3. Define the clipping area (the visible inventory box)
        Rect clipRect = new Rect(
                inventorySlots[0][0].left - 10,
                inventorySlots[0][0].top - 30,
                inventorySlots[INVENTORY_ROWS - 1][INVENTORY_COLS - 1].right + 10,
                inventorySlots[INVENTORY_ROWS - 1][INVENTORY_COLS - 1].bottom + 10
        );
        canvas.save();
        canvas.clipRect(clipRect);

        // 4. Loop through ALL rows needed, not just the visible 5
        for (int row = 0; row < totalRowsNeeded; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int index = row * INVENTORY_COLS + col;

                // Optimization: Stop if we've drawn all items
                if (index >= displayItems.size() && row >= INVENTORY_ROWS) break;

                // Calculate the Y position with scroll offset
                // We use the base slot Y as a reference point
                int baseSlotY = inventorySlots[0][0].top + row * (SLOT_SIZE + SLOT_GAP);
                int currentY = (int) (baseSlotY - inventoryScrollOffset);

                Rect drawSlot = new Rect(
                        inventorySlots[0][0].left + col * (SLOT_SIZE + SLOT_GAP),
                        currentY,
                        inventorySlots[0][0].left + col * (SLOT_SIZE + SLOT_GAP) + SLOT_SIZE,
                        currentY + SLOT_SIZE
                );

                // Skip drawing if completely outside the visible area (Optimization)
                if (drawSlot.bottom < clipRect.top || drawSlot.top > clipRect.bottom) continue;

                // Slot background
                paint.setColor(Color.argb(80, 30, 30, 40));
                canvas.drawRoundRect(drawSlot.left, drawSlot.top, drawSlot.right, drawSlot.bottom, 5, 5, paint);

                // Draw item if exists
                if (index < displayItems.size()) {
                    ItemStack stack = displayItems.get(index);
                    Item item = stack.getItem();

                    // Multi-select highlight (draw before item content so it acts as overlay background)
                    boolean isSelected = isMultiSelectMode && selectedItems.contains(stack);
                    if (isMultiSelectMode) {
                        if (isSelected) {
                            // Selected: blue highlight overlay
                            paint.setColor(Color.argb(80, 50, 130, 255));
                            canvas.drawRoundRect(drawSlot.left, drawSlot.top, drawSlot.right, drawSlot.bottom, 5, 5, paint);
                        } else {
                            // Unselected: dim overlay
                            paint.setColor(Color.argb(40, 0, 0, 0));
                            canvas.drawRoundRect(drawSlot.left, drawSlot.top, drawSlot.right, drawSlot.bottom, 5, 5, paint);
                        }
                    }

                    // Item border
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(isSelected ? 4 : 3);
                    paint.setColor(isSelected ? Color.rgb(80, 170, 255) : item.getColor());
                    canvas.drawRoundRect(drawSlot.left + 2, drawSlot.top + 2,
                            drawSlot.right - 2, drawSlot.bottom - 2, 4, 4, paint);

                    // Item name
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(18);
                    paint.setTextAlign(Paint.Align.CENTER);

                    String itemName = item.getName();
                    if (itemName.length() > 6) {
                        canvas.drawText(itemName.substring(0, 6), drawSlot.centerX(), drawSlot.centerY() - 2, paint);
                        canvas.drawText(itemName.substring(6), drawSlot.centerX(), drawSlot.centerY() - 2 + 20, paint);
                    } else {
                        canvas.drawText(itemName, drawSlot.centerX(), drawSlot.centerY() - 2, paint);
                    }

                    // Quantity
                    if (stack.getQuantity() > 1) {
                        paint.setTextSize(18);
                        paint.setColor(Color.YELLOW);
                        paint.setTextAlign(Paint.Align.RIGHT);
                        canvas.drawText("x" + stack.getQuantity(),
                                drawSlot.right - 5, drawSlot.bottom - 5, paint);
                    }

                    // Selected checkmark (top-right corner)
                    if (isSelected) {
                        paint.setColor(Color.rgb(80, 200, 80));
                        paint.setTextSize(28);
                        paint.setTextAlign(Paint.Align.RIGHT);
                        canvas.drawText("✓", drawSlot.right - 4, drawSlot.top + 24, paint);
                    }
                } else {
                    // Empty slot border
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2);
                    paint.setColor(Color.rgb(60, 60, 80));
                    canvas.drawRoundRect(drawSlot.left, drawSlot.top, drawSlot.right, drawSlot.bottom, 5, 5, paint);
                }
                paint.setStyle(Paint.Style.FILL);
            }
        }

        canvas.restore();

        // Draw scrollbar
        if (maxInventoryScrollOffset > 0) {
            drawScrollbar(canvas, paint, clipRect);
        }
    }

    private void drawScrollbar(Canvas canvas, Paint paint, Rect area) {
        int scrollbarWidth = 6;
        int scrollbarX = area.right - 10;

        float totalHeight = area.height() + maxInventoryScrollOffset;
        float scrollbarHeight = Math.max(40, (area.height() / totalHeight) * area.height());
        float scrollbarY = area.top + (inventoryScrollOffset / maxInventoryScrollOffset) * (area.height() - scrollbarHeight);

        paint.setColor(Color.argb(50, 100, 100, 100));
        canvas.drawRoundRect(scrollbarX, area.top, scrollbarX + scrollbarWidth, area.bottom, 3, 3, paint);

        paint.setColor(Color.argb(150, 150, 150, 150));
        canvas.drawRoundRect(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 3, 3, paint);
    }

    /**
     * Draw multi-select mode button (top-left)
     */
    private void drawMultiSelectButton(Canvas canvas, Paint paint) {
        boolean active = isMultiSelectMode;
        int bgColor = active ? Color.argb(200, 50, 120, 200) : Color.argb(150, 80, 80, 100);
        paint.setColor(bgColor);
        canvas.drawRoundRect(multiSelectButton.left, multiSelectButton.top,
                multiSelectButton.right, multiSelectButton.bottom, 6, 6, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(active ? 2 : 1);
        paint.setColor(active ? Color.rgb(100, 180, 255) : Color.rgb(180, 180, 200));
        canvas.drawRoundRect(multiSelectButton.left, multiSelectButton.top,
                multiSelectButton.right, multiSelectButton.bottom, 6, 6, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        String label = active ? "✓ 多选" : "☐ 多选";
        canvas.drawText(label, multiSelectButton.centerX(), multiSelectButton.centerY() + 6, paint);
    }

    /**
     * Draw multi-select action buttons (select all + discard)
     */
    private void drawMultiSelectActions(Canvas canvas, Paint paint) {
        int selectedCount = selectedItems.size();

        // Select all / Deselect all button
        boolean hasSelection = selectedCount > 0;
        paint.setColor(Color.argb(180, 60, 60, 80));
        canvas.drawRoundRect(selectAllButton.left, selectAllButton.top,
                selectAllButton.right, selectAllButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(180, 180, 220));
        canvas.drawRoundRect(selectAllButton.left, selectAllButton.top,
                selectAllButton.right, selectAllButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        String selectLabel = allSelected ? "取消全选" : "全选";
        canvas.drawText(selectLabel, selectAllButton.centerX(), selectAllButton.centerY() + 7, paint);

        // Discard button
        int discardColor = hasSelection ? Color.argb(200, 180, 50, 50) : Color.argb(100, 80, 40, 40);
        paint.setColor(discardColor);
        canvas.drawRoundRect(discardButton.left, discardButton.top,
                discardButton.right, discardButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(hasSelection ? Color.rgb(255, 120, 120) : Color.rgb(150, 100, 100));
        canvas.drawRoundRect(discardButton.left, discardButton.top,
                discardButton.right, discardButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(hasSelection ? Color.WHITE : Color.argb(150, 200, 200, 200));
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        String discardLabel = "丢弃" + (hasSelection ? "(" + selectedCount + ")" : "");
        canvas.drawText(discardLabel, discardButton.centerX(), discardButton.centerY() + 7, paint);
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
     * Handle touch event (Called on ACTION_DOWN)
     */
    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) return false;

        // Check category tabs
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (tabBounds[0][i] != null && tabBounds[0][i].contains((int) x, (int) y)) {
                if (selectedTab != i) {
                    selectedTab = i;
                    inventoryScrollOffset = 0;
                    // Clear selection when switching tabs
                    if (isMultiSelectMode) {
                        selectedItems.clear();
                        allSelected = false;
                    }
                }
                return true;
            }
        }

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
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        // Check multi-select button
        if (TouchUtil.checkIsInTouchRectFloat(multiSelectButton, x, y)) {
            isMultiSelectMode = !isMultiSelectMode;
            selectedItems.clear();
            allSelected = false;
            return true;
        }

        // In multi-select mode, check action buttons
        if (isMultiSelectMode) {
            // Select all / Deselect all
            if (TouchUtil.checkIsInTouchRectFloat(selectAllButton, x, y)) {
                List<ItemStack> displayItems = getCategoryItems(selectedTab);
                if (allSelected) {
                    selectedItems.clear();
                    allSelected = false;
                } else {
                    selectedItems.clear();
                    selectedItems.addAll(displayItems);
                    allSelected = true;
                }
                return true;
            }
            // Discard
            if (TouchUtil.checkIsInTouchRectFloat(discardButton, x, y) && !selectedItems.isEmpty()) {
                int count = ItemSystem.getInstance().removeItems(new ArrayList<>(selectedItems));
                selectedItems.clear();
                allSelected = false;
                GameEngine.getInstance().showCenterToast("已丢弃 " + count + " 个物品");
                return true;
            }
        }

        // Check equipment slots - show info or unequip
        if (helmetSlot.contains((int) x, (int) y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.HELMET);
            if (equipped != null) {
                // Show item info for equipped item
                showEquipmentInfo(equipped, true, -1, helmetSlot.centerX(), helmetSlot.centerY());
            } else {
                // Empty slot - do nothing or show hint
            }
            return true;
        }

        if (necklaceSlot.contains((int) x, (int) y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ACCESSORY);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, necklaceSlot.centerX(), necklaceSlot.centerY());
            }
            return true;
        }

        if (weaponSlot.contains((int) x, (int) y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, weaponSlot.centerX(), weaponSlot.centerY());
            }
            return true;
        }

        if (armorSlot.contains((int) x, (int) y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ARMOR);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, armorSlot.centerX(), armorSlot.centerY());
            }
            return true;
        }

        if (beltSlot.contains((int) x, (int) y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.BELT);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, beltSlot.centerX(), beltSlot.centerY());
            }
            return true;
        }

        if (shoesSlot.contains((int) x, (int) y)) {
            EquipmentItem equipped = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.SHOES);
            if (equipped != null) {
                showEquipmentInfo(equipped, true, -1, shoesSlot.centerX(), shoesSlot.centerY());
            }
            return true;
        }

        // Reset drag state
        isDragging = false;
        touchDownX = x;
        touchDownY = y;

        // Check if touching inventory area
        Rect inventoryArea = new Rect(
                inventorySlots[0][0].left,
                inventorySlots[0][0].top,
                inventorySlots[INVENTORY_ROWS - 1][INVENTORY_COLS - 1].right,
                inventorySlots[INVENTORY_ROWS - 1][INVENTORY_COLS - 1].bottom
        );

        if (inventoryArea.contains((int) x, (int) y)) {
            isTouchingInventory = true;
            // Don't show popup yet, wait for ACTION_UP or confirm it's not a drag
            return true;
        }

        return false;
    }

    /**
     * Handle touch release (Called on ACTION_UP)
     */
    public boolean handleTouchUp(float x, float y) {
        if (!isVisible) return false;
        if (!isTouchingInventory) return false;

        // If it wasn't a drag, treat it as a click
        if (!isDragging) {
            // Perform the item click logic here
            List<ItemStack> displayItems = getCategoryItems(selectedTab);

            // 1. Calculate total rows needed dynamically
            int totalRowsNeeded = (int) Math.ceil((double) displayItems.size() / INVENTORY_COLS);

            // 2. Loop through ALL rows that contain items
            for (int row = 0; row < totalRowsNeeded; row++) {
                for (int col = 0; col < INVENTORY_COLS; col++) {
                    int index = row * INVENTORY_COLS + col;

                    // Safety check: don't go beyond the list size
                    if (index >= displayItems.size()) break;

                    // 3. Calculate the EXACT same position as used in drawInventoryGrid
                    int baseSlotY = inventorySlots[0][0].top + row * (SLOT_SIZE + SLOT_GAP);
                    int currentTop = (int) (baseSlotY - inventoryScrollOffset);

                    Rect hitSlot = new Rect(
                            inventorySlots[0][0].left + col * (SLOT_SIZE + SLOT_GAP),
                            currentTop,
                            inventorySlots[0][0].left + col * (SLOT_SIZE + SLOT_GAP) + SLOT_SIZE,
                            currentTop + SLOT_SIZE
                    );

                    // 4. Check if the touch point is inside this calculated slot
                    if (hitSlot.contains((int) x, (int) y)) {
                        ItemStack stack = displayItems.get(index);

                        if (isMultiSelectMode) {
                            // Toggle selection
                            if (selectedItems.contains(stack)) {
                                selectedItems.remove(stack);
                            } else {
                                selectedItems.add(stack);
                            }
                            // Update allSelected state
                            allSelected = selectedItems.size() >= displayItems.size();
                        } else {
                            // Show the appropriate info panel
                            if (stack.getItem().getType() == Item.Type.EQUIPMENT) {
                                showEquipmentInfo((EquipmentItem) stack.getItem(), false, index, hitSlot.centerX(), hitSlot.centerY());
                            } else {
                                showItemInfo(stack, index, hitSlot.centerX(), hitSlot.centerY());
                            }
                        }

                        isTouchingInventory = false;
                        return true;
                    }
                }
            }
        }

        isTouchingInventory = false;
        return false;
    }

    /**
     * Handle move (Called on ACTION_MOVE)
     */
    public boolean handleTouchMove(float x, float y) {
        if (!isVisible || !isTouchingInventory) return false;

        float deltaY = y - touchDownY;

        // If moved more than 10 pixels, consider it a drag
        if (Math.abs(deltaY) > 10) {
            isDragging = true;
            inventoryScrollOffset -= deltaY;
            inventoryScrollOffset = Math.max(0, Math.min(inventoryScrollOffset, maxInventoryScrollOffset));
            touchDownY = y; // Reset reference for smooth scrolling
            return true;
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
                            ItemSystem.getInstance().equipItem(equipment);
                        }

                        @Override
                        public void onDrop(EquipmentItem equipment, int index) {
                            ItemSystem.getInstance().removeItem(equipment.getId(), 1);
                        }

                        @Override
                        public void onXiangqian(EquipmentItem equipment, int index) {
                            showEquipXiangQianDialog(equipment);
                        }

                        @Override
                        public void onXilian(EquipmentItem equipment, int index) {
                            showEquipXiLianDialog(equipment);
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
            // Check if this item is set as default HP/MP potion
            boolean isDefaultHp = ItemSystem.getInstance().getDefaultHpPotionIds().contains(stack.getItem().getId());
            boolean isDefaultMp = ItemSystem.getInstance().getDefaultMpPotionIds().contains(stack.getItem().getId());

            itemInfoPanel.show(stack,
                    centerX,
                    centerY,
                    isDefaultHp,
                    isDefaultMp,
                    new ItemInfoPanel.ItemActionListener() {
                        @Override
                        public void onUseItem(ItemStack item) {
                            ItemSystem.getInstance().useItem(item);
                        }

                        @Override
                        public void onDropItem(ItemStack item) {
                            ItemSystem.getInstance().removeItem(item.getItem().getId(), 1);
                        }

                        @Override
                        public void onSetDefaultHp(ItemStack item) {
                            int itemId = item.getItem().getId();
                            if (ItemSystem.getInstance().getDefaultHpPotionIds().contains(itemId)) {
                                ItemSystem.getInstance().removeDefaultHpPotion(itemId);
                                GameEngine.getInstance().showCenterToast("已取消默认气血药");
                            } else {
                                ItemSystem.getInstance().addDefaultHpPotion(itemId);
                                GameEngine.getInstance().showCenterToast("已设为默认气血药");
                            }
                        }

                        @Override
                        public void onSetDefaultMp(ItemStack item) {
                            int itemId = item.getItem().getId();
                            if (ItemSystem.getInstance().getDefaultMpPotionIds().contains(itemId)) {
                                ItemSystem.getInstance().removeDefaultMpPotion(itemId);
                                GameEngine.getInstance().showCenterToast("已取消默认魔法药");
                            } else {
                                ItemSystem.getInstance().addDefaultMpPotion(itemId);
                                GameEngine.getInstance().showCenterToast("已设为默认魔法药");
                            }
                        }
                    }
            );
        }
    }

    private void showEquipXiangQianDialog(EquipmentItem equipment) {
        List<String> options = new ArrayList<>();
        EquipItemInfo equipItemInfo = equipment.getEquipItemInfo();
        switch (equipment.getSlot()) {
            case HELMET: {
                // 头盔可镶嵌太阳石，红玛瑙
                options.add((equipItemInfo.getAttackStoneLevel() + 1) + "级太阳石（增加攻击伤害）");
                options.add((equipItemInfo.getHitStoneLevel() + 1) + "级红玛瑙（增加攻击和法术命中）");
            }
            break;
            case ACCESSORY: {
                // 项链可镶嵌舍利子，蓝宝石
                options.add((equipItemInfo.getManaStoneLevel() + 1) + "级舍利子（增加法术伤害）");
                options.add((equipItemInfo.getMpStoneLevel() + 1) + "级蓝宝石（增加魔法上限）");
            }
            break;
            case WEAPON: {
                // 武器可镶嵌太阳石，舍利子
                options.add((equipItemInfo.getAttackStoneLevel() + 1) + "级太阳石（增加攻击伤害）");
                options.add((equipItemInfo.getManaStoneLevel() + 1) + "级舍利子（增加法术伤害）");
            }
            break;
            case ARMOR: {
                // 铠甲可镶嵌月亮石，光芒石
                options.add((equipItemInfo.getDefenseStoneLevel() + 1) + "级月亮石（增加防御）");
                options.add((equipItemInfo.getHpStoneLevel() + 1) + "级光芒石（增加气血上限）");
            }
            break;
            case BELT: {
                // 腰带可镶嵌光芒石，黑宝石
                options.add((equipItemInfo.getHpStoneLevel() + 1) + "级光芒石（增加气血上限）");
                options.add((equipItemInfo.getSpeedStoneLevel() + 1) + "级黑宝石（增加速度）");
            }
            break;
            case SHOES: {
                // 鞋子可镶嵌黑宝石，神秘石
                options.add((equipItemInfo.getSpeedStoneLevel() + 1) + "级黑宝石（增加速度）");
                options.add((equipItemInfo.getDodgeStoneLevel() + 1) + "级神秘石（增加闪避）");
            }
            break;
        }

        String msg = "请选择你要镶嵌的宝石，当前装备 " + EquipUtil.getStoneAddResultText(equipment.getEquipItemInfo());
        GameEngine.getInstance().showDialog("宝石镶嵌", msg, options, new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                String option = options.get(optionIndex);
                GemtoneType gemtoneType = null;
                if (option.startsWith("太阳石")) {
                    gemtoneType = GemtoneType.GT_TaiYangShi;
                } else if (option.startsWith("红玛瑙")) {
                    gemtoneType = GemtoneType.GT_HoneMaNao;
                } else if (option.startsWith("舍利子")) {
                    gemtoneType = GemtoneType.GT_SheLiZi;
                } else if (option.startsWith("蓝宝石")) {
                    gemtoneType = GemtoneType.GT_LanBaoShi;
                } else if (option.startsWith("月亮石")) {
                    gemtoneType = GemtoneType.GT_YueLiangShi;
                } else if (option.startsWith("光芒石")) {
                    gemtoneType = GemtoneType.GT_GuangMangShi;
                } else if (option.startsWith("黑宝石")) {
                    gemtoneType = GemtoneType.GT_HeiBaoShi;
                } else if (option.startsWith("神秘石")) {
                    gemtoneType = GemtoneType.GT_ShenMiShi;
                }

                if (gemtoneType != null) {
                    boolean success = ItemSystem.getInstance().equipXiangQian(equipment, gemtoneType);
                    if (success) {
                        showEquipXiangQianDialog(equipment);
                    }
                }
            }
        });
    }

    private void showEquipXiLianDialog(EquipmentItem equipment) {
        List<String> options = new ArrayList<>();
        EquipItemInfo equipItemInfo = equipment.getEquipItemInfo();
        options.add("使用 " + equipItemInfo.getLevel() + "级洗炼石 洗炼");

        String xilianInfo = EquipUtil.getEquipXiLianPropText(equipItemInfo);
        String msg;
        if (TextUtils.isEmpty(xilianInfo)) {
            msg = "对装备进行洗炼可以获得附加属性加成，当前装备 洗炼属性：无 ";
        } else {
            msg = "对装备进行洗炼可以获得附加属性加成，当前装备 " + xilianInfo;
        }

        GameEngine.getInstance().showDialog("装备洗炼", msg, options, new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                boolean success = ItemSystem.getInstance().equipXiLian(equipment);
                if (success) {
                    showEquipXiLianDialog(equipment);
                }
            }
        });
    }
}

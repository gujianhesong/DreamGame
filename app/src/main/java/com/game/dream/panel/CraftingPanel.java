package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.enums.GemtoneType;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Crafting/Forge Panel
 */
public class CraftingPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    // Tabs
    private Rect equipBookTab;
    private Rect equipIronTab;
    private Rect gemtoneTab;
    private Rect xilianStoneTab;
    private int currentTab;

    // Recipe list area
    private Rect recipeListArea;
    private List<Rect> recipeButtons;

    // Scrolling support
    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;

    public interface CraftingListener {
        void onCraft(Recipe recipe);
    }

    private CraftingListener listener;

    // Sample Recipes (In a real game, this would come from a CraftingSystem)
    private static class Recipe {
        String name;
        int level;
        List<MaterialItem> materialItems;
        int costMoney;

        Recipe(String name, int level, List<MaterialItem> materialItems, int money) {
            this.name = name;
            this.level = level;
            this.materialItems = materialItems;
            this.costMoney = money;
        }
    }

    private static class MaterialItem {
        String materialName;
        int materialCount;

        MaterialItem(String materialName, int materialCount) {
            this.materialName = materialName;
            this.materialCount = materialCount;
        }
    }

    private List<Recipe> equipBookRecipes;
    private List<Recipe> equipIronRecipes;
    private List<Recipe> gemtoneRecipes;
    private List<Recipe> xilianStoneRecipes;

    public CraftingPanel() {
        this.isVisible = false;

        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.equipBookTab = new Rect();
        this.equipIronTab = new Rect();
        this.gemtoneTab = new Rect();
        this.xilianStoneTab = new Rect();
        this.recipeListArea = new Rect();

        this.recipeButtons = new ArrayList<>();

        this.equipBookRecipes = new ArrayList<>();
        this.equipIronRecipes = new ArrayList<>();
        this.gemtoneRecipes = new ArrayList<>();
        this.xilianStoneRecipes = new ArrayList<>();

        initializeRecipes();

        setListener(new CraftingListener() {
            @Override
            public void onCraft(Recipe recipe) {
                doCraftRecipe(recipe);
            }
        });
    }

    private void initializeRecipes() {
        for (int i = 2; i <= 10; i++) {
            int srcLevel = (i - 1) * 10;
            int destLevel = i * 10;
            equipBookRecipes.add(new Recipe(destLevel + "级头盔制造书", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级头盔制造书", 2)), 1000));
            equipBookRecipes.add(new Recipe(destLevel + "级项链制造书", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级项链制造书", 2)), 1000));
            equipBookRecipes.add(new Recipe(destLevel + "级武器制造书", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级武器制造书", 2)), 1000));
            equipBookRecipes.add(new Recipe(destLevel + "级铠甲制造书", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级铠甲制造书", 2)), 1000));
            equipBookRecipes.add(new Recipe(destLevel + "级腰带制造书", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级腰带制造书", 2)), 1000));
            equipBookRecipes.add(new Recipe(destLevel + "级鞋子制造书", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级鞋子制造书", 2)), 1000));
        }

        for (int i = 2; i <= 10; i++) {
            int srcLevel = (i - 1) * 10;
            int destLevel = i * 10;
            equipIronRecipes.add(new Recipe(destLevel + "级精铁", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级精铁", 2)), 1000));
        }

        for (int i = 2; i <= 10; i++) {
            int srcLevel = i - 1;
            int destLevel = i;
            GemtoneType[] gemtoneTypes = GemtoneType.values();
            for (GemtoneType gemtoneType : gemtoneTypes) {
                gemtoneRecipes.add(new Recipe(destLevel + "级" + gemtoneType.getDesc(), destLevel, Arrays.asList(
                        new MaterialItem(srcLevel + "级" + gemtoneType.getDesc(), 2)), 1000));
            }
        }

        for (int i = 2; i <= 10; i++) {
            int srcLevel = (i - 1) * 10;
            int destLevel = i * 10;
            xilianStoneRecipes.add(new Recipe(destLevel + "级洗炼石", destLevel, Arrays.asList(
                    new MaterialItem(srcLevel + "级洗炼石", 2)), 1000));
        }
    }

    public void toggleVisibility() {
        isVisible = !isVisible;
    }

    public void show() {
        isVisible = true;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        // Close button
        int padding = 10;
        int btnSize = 40;
        closeButton.set(panelBounds.right - btnSize - padding, panelBounds.top + padding,
                panelBounds.right - padding, panelBounds.top + padding + btnSize);

        // Tabs (top)
        int tabHeight = 50;
        int tabWidth = width / 4;
        int tabY = panelBounds.top + 60;

        equipBookTab = new Rect(panelBounds.left, tabY, panelBounds.left + tabWidth, tabY + tabHeight);
        equipIronTab = new Rect(panelBounds.left + tabWidth, tabY, panelBounds.left + tabWidth * 2, tabY + tabHeight);
        gemtoneTab = new Rect(panelBounds.left + tabWidth * 2, tabY, panelBounds.left + tabWidth * 3, tabY + tabHeight);
        xilianStoneTab = new Rect(panelBounds.left + tabWidth * 3, tabY, panelBounds.right, tabY + tabHeight);

        // Recipe list area
        recipeListArea.set(panelBounds.left + 20, tabY + tabHeight + 20,
                panelBounds.right - 20, panelBounds.bottom - 30);

        updateRecipeButtons();
    }

    public void setListener(CraftingListener listener) {
        this.listener = listener;
    }

    private void updateRecipeButtons() {
        recipeButtons.clear();
        int itemHeight = 100;
        int gap = 15;

        List<Recipe> recipes = getCurrentTabRecipeList();

        int totalHeight = recipes.size() * (itemHeight + gap);
        maxScrollOffset = Math.max(0, totalHeight - recipeListArea.height());
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        for (int i = 0; i < recipes.size(); i++) {
            int yPos = recipeListArea.top + i * (itemHeight + gap) - (int) scrollOffset;
            Rect btn = new Rect(recipeListArea.left, yPos, recipeListArea.right, yPos + itemHeight);
            recipeButtons.add(btn);
        }
    }

    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Background
        paint.setColor(Color.argb(240, 20, 25, 35));
        canvas.drawRect(panelBounds, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(255, 165, 0)); // Orange border for crafting
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("合成工坊", panelBounds.centerX(), panelBounds.top + 50, paint);

        // Close button
        drawCloseButton(canvas, paint);

        // Draw tabs
        drawTabs(canvas, paint);

        // Clip and draw recipes
        canvas.save();
        canvas.clipRect(recipeListArea);

        List<Recipe> recipes = getCurrentTabRecipeList();
        List<Rect> curButtons = new ArrayList<>(recipeButtons);
        for (int i = 0; i < recipes.size(); i++) {
            Rect btn = curButtons.get(i);
            if (btn.bottom < recipeListArea.top || btn.top > recipeListArea.bottom) continue;

            Recipe recipe = recipes.get(i);

            // Recipe background
            paint.setColor(Color.argb(80, 50, 50, 60));
            canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);

            // Recipe Name
            paint.setColor(Color.rgb(255, 200, 100));
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(recipe.name, btn.left + 15, btn.top + 35, paint);

            // Materials needed
            paint.setColor(Color.rgb(180, 180, 180));
            paint.setTextSize(18);
            String needItemDesc = "";
            for (MaterialItem item : recipe.materialItems) {
                needItemDesc += item.materialName + " x" + item.materialCount;
            }
            canvas.drawText("需要: " + needItemDesc, btn.left + 15, btn.top + 65, paint);
            canvas.drawText("金钱: " + recipe.costMoney, btn.left + 15, btn.top + 90, paint);

            // Check if player has enough materials (Simple check)
            boolean canCraft = checkCanCraft(recipe);

            // Craft Button
            Rect craftBtn = new Rect(btn.right - 100, btn.top + 25, btn.right - 10, btn.bottom - 25);
            paint.setColor(canCraft ? Color.argb(200, 50, 200, 50) : Color.argb(100, 100, 100, 100));
            canvas.drawRoundRect(craftBtn.left, craftBtn.top, craftBtn.right, craftBtn.bottom, 5, 5, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("打造", craftBtn.centerX(), craftBtn.centerY() + 6, paint);
        }

        canvas.restore();

        // Scrollbar
        if (maxScrollOffset > 0) {
            int sbWidth = 6;
            int sbX = recipeListArea.right - 10;
            float totalH = recipeListArea.height() + maxScrollOffset;
            float sbH = Math.max(40, (recipeListArea.height() / totalH) * recipeListArea.height());
            float sbY = recipeListArea.top + (scrollOffset / maxScrollOffset) * (recipeListArea.height() - sbH);

            paint.setColor(Color.argb(50, 100, 100, 100));
            canvas.drawRoundRect(sbX, recipeListArea.top, sbX + sbWidth, recipeListArea.bottom, 3, 3, paint);
            paint.setColor(Color.argb(150, 150, 150, 150));
            canvas.drawRoundRect(sbX, sbY, sbX + sbWidth, sbY + sbH, 3, 3, paint);
        }
    }

    private void drawCloseButton(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(180, 255, 80, 80));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        canvas.drawLine(closeButton.left + 10, closeButton.top + 10, closeButton.right - 10, closeButton.bottom - 10, paint);
        canvas.drawLine(closeButton.right - 10, closeButton.top + 10, closeButton.left + 10, closeButton.bottom - 10, paint);
    }

    /**
     * Draw tabs
     */
    private void drawTabs(Canvas canvas, Paint paint) {
        // equipBook tab
        drawTab(canvas, paint, equipBookTab, "制造书", currentTab == 0);

        // equipIron tab
        drawTab(canvas, paint, equipIronTab, "精铁", currentTab == 1);

        // gemtone tab
        drawTab(canvas, paint, gemtoneTab, "宝石", currentTab == 2);

        // xilianStone tab
        drawTab(canvas, paint, xilianStoneTab, "洗炼石", currentTab == 3);
    }

    /**
     * Draw a single tab
     */
    private void drawTab(Canvas canvas, Paint paint, Rect tab, String label, boolean isSelected) {
        // Tab background
        if (isSelected) {
            paint.setColor(Color.argb(200, 50, 100, 200));
        } else {
            paint.setColor(Color.argb(100, 40, 40, 50));
        }
        canvas.drawRect(tab, paint);

        // Tab border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(isSelected ? Color.rgb(100, 180, 255) : Color.rgb(80, 80, 100));
        canvas.drawRect(tab, paint);
        paint.setStyle(Paint.Style.FILL);

        // Tab text
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = tab.centerY() + 8;
        canvas.drawText(label, tab.centerX(), textY, paint);
    }

    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) return false;
        isDragging = false;
        lastTouchY = y;

        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        // Check tabs
        if (TouchUtil.checkIsInTouchRectFloat(equipBookTab, x, y)) {
            if (currentTab != 0) {
                currentTab = 0;
                scrollOffset = 0;
                updateRecipeButtons();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(equipIronTab, x, y)) {
            if (currentTab != 1) {
                currentTab = 1;
                scrollOffset = 0;
                updateRecipeButtons();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(gemtoneTab, x, y)) {
            if (currentTab != 2) {
                currentTab = 2;
                scrollOffset = 0;
                updateRecipeButtons();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(xilianStoneTab, x, y)) {
            if (currentTab != 3) {
                currentTab = 3;
                scrollOffset = 0;
                updateRecipeButtons();
            }
            return true;
        }

        if (recipeListArea.contains((int) x, (int) y)) {
            return true;
        }
        return false;
    }

    public boolean handleTouchMove(float x, float y) {
        if (!isVisible || !recipeListArea.contains((int) x, (int) y)) return false;

        float deltaY = y - lastTouchY;
        if (Math.abs(deltaY) > 5) {
            isDragging = true;
            scrollOffset -= deltaY;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            lastTouchY = y;
            updateRecipeButtons();
            return true;
        }
        return false;
    }

    public boolean handleTouchUp(float x, float y) {
        if (!isVisible || isDragging) return false;

        List<Recipe> recipes = getCurrentTabRecipeList();
        for (int i = 0; i < recipes.size(); i++) {
            Rect btn = recipeButtons.get(i);
            Rect craftBtn = new Rect(btn.right - 100, btn.top + 25, btn.right - 10, btn.bottom - 25);

            if (craftBtn.contains((int) x, (int) y)) {
                if (listener != null) listener.onCraft(recipes.get(i));
                return true;
            }
        }
        return false;
    }

    private boolean checkCanCraft(Recipe recipe) {
        boolean canCraft = true;
        for (MaterialItem item : recipe.materialItems) {
            int curItemCount = ItemSystem.getInstance().getItemCountByName(item.materialName);
            if (curItemCount < item.materialCount) {
                canCraft = false;
                break;
            }
        }
        if (RoleSystem.getInstance().getRoleInfo().getMoney() < recipe.costMoney) {
            canCraft = false;
        }
        return canCraft;
    }

    private List<Recipe> getCurrentTabRecipeList() {
        List<Recipe> curRecipeList = new ArrayList<>();
        switch (currentTab) {
            case 0: {
                curRecipeList.addAll(equipBookRecipes);
            }
            break;
            case 1: {
                curRecipeList.addAll(equipIronRecipes);
            }
            break;
            case 2: {
                curRecipeList.addAll(gemtoneRecipes);
            }
            break;
            case 3: {
                curRecipeList.addAll(xilianStoneRecipes);
            }
            break;
        }
        return curRecipeList;
    }

    private void doCraftRecipe(Recipe recipe) {
        if (RoleSystem.getInstance().getRoleInfo().getTili() < 20) {
            GameEngine.getInstance().showCenterToast("合成需要20点体力");
            return;
        }
        if (RoleSystem.getInstance().getRoleInfo().getMoney() < recipe.costMoney) {
            GameEngine.getInstance().showCenterToast("合成需要" + recipe.costMoney + "金钱");
            return;
        }
        boolean canCraft = checkCanCraft(recipe);
        if (!canCraft) {
            GameEngine.getInstance().showCenterToast("你缺少合成材料");
            return;
        }

        Item item = null;
        //制造书
        if (recipe.name.contains("头盔制造书")) {
            item = ItemCreator.createBuildEquipBook(recipe.level, EquipmentItem.Slot.HELMET);
        } else if (recipe.name.contains("项链制造书")) {
            item = ItemCreator.createBuildEquipBook(recipe.level, EquipmentItem.Slot.ACCESSORY);
        } else if (recipe.name.contains("武器制造书")) {
            item = ItemCreator.createBuildEquipBook(recipe.level, EquipmentItem.Slot.WEAPON);
        } else if (recipe.name.contains("铠甲制造书")) {
            item = ItemCreator.createBuildEquipBook(recipe.level, EquipmentItem.Slot.ARMOR);
        } else if (recipe.name.contains("腰带制造书")) {
            item = ItemCreator.createBuildEquipBook(recipe.level, EquipmentItem.Slot.BELT);
        } else if (recipe.name.contains("鞋子制造书")) {
            item = ItemCreator.createBuildEquipBook(recipe.level, EquipmentItem.Slot.SHOES);
        }

        //精铁
        if (recipe.name.contains("精铁")) {
            item = ItemCreator.createBuildEquipIron(recipe.level);
        }

        //宝石
        GemtoneType[] gemtoneTypes = GemtoneType.values();
        for (GemtoneType gemtoneType : gemtoneTypes) {
            if (recipe.name.contains(gemtoneType.getDesc())) {
                item = ItemCreator.createGemstone(gemtoneType, recipe.level);
            }
        }

        //洗炼石
        if (recipe.name.contains("洗炼石")) {
            item = ItemCreator.createXiLianStone(recipe.level);
        }

        if (item != null) {
            if (ItemSystem.getInstance().addItem(item, 1)) {
                String msg = "你获得了" + item.getName();
                GameEngine.getInstance().showCenterToast(msg);
            }
            for (MaterialItem materialItem : recipe.materialItems) {
                ItemSystem.getInstance().removeItem(materialItem.materialName, materialItem.materialCount);
            }
            RoleSystem.getInstance().removeMoney(recipe.costMoney);
        }
    }
}

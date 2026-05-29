package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.system.ItemSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Crafting/Forge Panel
 */
public class CraftingPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;

    // Recipe list area
    private Rect recipeListArea;
    private List<Rect> recipeButtons;

    // Scrolling support
    private float scrollOffset = 0;
    private float maxScrollOffset = 0;
    private float lastTouchY = 0;
    private boolean isDragging = false;

    public interface CraftingListener {
        void onCraft(int recipeId);
    }

    private CraftingListener listener;

    // Sample Recipes (In a real game, this would come from a CraftingSystem)
    private static class Recipe {
        int id;
        String name;
        String material;
        int materialCount;
        int costMoney;

        Recipe(int id, String name, String material, int count, int money) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.materialCount = count;
            this.costMoney = money;
        }
    }

    private List<Recipe> recipes;

    public CraftingPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.recipeListArea = new Rect();
        this.recipeButtons = new ArrayList<>();
        this.recipes = new ArrayList<>();
        initializeRecipes();
    }

    private void initializeRecipes() {
        // Example recipes
        recipes.add(new Recipe(1, "铁剑", "铁矿石", 5, 100));
        recipes.add(new Recipe(2, "回血丹", "灵草", 3, 50));
        recipes.add(new Recipe(3, "玄铁甲", "玄铁矿", 10, 500));
        recipes.add(new Recipe(4, "法力药水", "魔核", 2, 200));
    }

    public void toggleVisibility() { isVisible = !isVisible; }
    public void show() { isVisible = true; }
    public void hide() { isVisible = false; }
    public boolean isVisible() { return isVisible; }

    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        // Close button
        int padding = 10;
        int btnSize = 40;
        closeButton.set(panelBounds.right - btnSize - padding, panelBounds.top + padding,
                panelBounds.right - padding, panelBounds.top + padding + btnSize);

        // Recipe list area
        recipeListArea.set(panelBounds.left + 20, panelBounds.top + 80,
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

        int totalHeight = recipes.size() * (itemHeight + gap);
        maxScrollOffset = Math.max(0, totalHeight - recipeListArea.height());
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        for (int i = 0; i < recipes.size(); i++) {
            int yPos = recipeListArea.top + i * (itemHeight + gap) - (int)scrollOffset;
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

        // Clip and draw recipes
        canvas.save();
        canvas.clipRect(recipeListArea);

        for (int i = 0; i < recipes.size(); i++) {
            Rect btn = recipeButtons.get(i);
            if (btn.bottom < recipeListArea.top || btn.top > recipeListArea.bottom) continue;

            Recipe r = recipes.get(i);

            // Recipe background
            paint.setColor(Color.argb(80, 50, 50, 60));
            canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);

            // Recipe Name
            paint.setColor(Color.rgb(255, 200, 100));
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(r.name, btn.left + 15, btn.top + 35, paint);

            // Materials needed
            paint.setColor(Color.rgb(180, 180, 180));
            paint.setTextSize(18);
            canvas.drawText("需要: " + r.material + " x" + r.materialCount, btn.left + 15, btn.top + 65, paint);
            canvas.drawText("金钱: " + r.costMoney, btn.left + 15, btn.top + 90, paint);

            // Check if player has enough materials (Simple check)
            boolean canCraft = ItemSystem.getInstance().getItemCountByName(r.material) >= r.materialCount &&
                    com.game.dream.system.RoleSystem.getInstance().getRoleInfo().getMoney() >= r.costMoney;

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

    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) return false;
        isDragging = false;
        lastTouchY = y;

        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        if (recipeListArea.contains((int)x, (int)y)) {
            return true;
        }
        return false;
    }

    public boolean handleTouchMove(float x, float y) {
        if (!isVisible || !recipeListArea.contains((int)x, (int)y)) return false;

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

        for (int i = 0; i < recipes.size(); i++) {
            Rect btn = recipeButtons.get(i);
            Rect craftBtn = new Rect(btn.right - 100, btn.top + 25, btn.right - 10, btn.bottom - 25);

            if (craftBtn.contains((int)x, (int)y)) {
                if (listener != null) listener.onCraft(recipes.get(i).id);
                return true;
            }
        }
        return false;
    }
}

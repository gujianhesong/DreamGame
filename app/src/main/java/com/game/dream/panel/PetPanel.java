package com.game.dream.panel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.dream.GameEngine;
import com.game.dream.enemy.Enemy;
import com.game.dream.system.Pet;
import com.game.dream.system.PetSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 战宠花名册面板(左列表 + 右详情)。
 * 左侧: 已收服战宠的可滚动列表, 点击某项即选中。
 * 右侧: 选中战宠的详细信息 —— 名称 / 类型 / 等级 / 经验 / 气血，
 *       体魔力耐敏属性加点(+/-、剩余点数、洗点)，
 *       以及 出战-收回 / 疗伤 / 改名 / 洗点 / 放生(二次确认) 操作。
 * 直接操作 {@link PetSystem} 单例；出战/收回由 GameEngine 每帧自动同步到世界。
 *
 * 线程安全: 绘制线程与触摸线程都会访问本面板，故不缓存共享的条目矩形列表，
 * 每次通过 {@link #computeRowRects()} 计算并返回局部快照(遵循项目既有的本地快照约定)。
 * 详情区按钮/属性行位置在 setBounds 中固定计算，绘制与触摸共用同一批矩形。
 */
public class PetPanel {
    private boolean isVisible;
    private Rect panelBounds;
    private Rect closeButton;
    private Rect listArea;    // 左侧列表区(可滚动)
    private Rect detailArea;  // 右侧详情区

    // 详情区操作按钮(位置固定, 在 setBounds 中计算)
    private Rect detailDeployButton;   // 出战 / 收回
    private Rect detailHealButton;     // 疗伤
    private Rect detailRenameButton;   // 改名
    private Rect detailResetButton;    // 洗点
    private Rect detailReleaseButton;  // 放生

    // 属性加点行(体/魔/力/耐/敏)的 +/- 按钮与几何
    private final Rect[] attrMinus = new Rect[Pet.ATTR_COUNT];
    private final Rect[] attrPlus = new Rect[Pet.ATTR_COUNT];
    private float attrRowsTop;
    private float attrRowH;
    private int attrContentLeft;
    private int attrContentRight;

    private static final String[] ATTR_LABELS = {"体质", "魔力", "力量", "耐力", "敏捷"};
    /** 每项基础属性同行展示的派生战斗属性(参照人物属性面板摆放): 体质→气血, 魔力→灵力, 力量→攻击, 耐力→防御, 敏捷→速度。 */
    private static final String[] DERIVED_LABELS = {"气血", "灵力", "攻击", "防御", "速度"};

    // 当前选中的战宠(volatile: 绘制线程与触摸线程共享)
    private volatile Pet selectedPet = null;

    // 滚动支持(volatile: 绘制线程读, 触摸线程写)
    private volatile float scrollOffset = 0;
    private volatile float maxScrollOffset = 0;

    // 放生二次确认
    private Pet pendingRelease = null;
    private final Rect confirmBox = new Rect();
    private final Rect confirmYes = new Rect();
    private final Rect confirmNo = new Rect();

    private static final int ROW_HEIGHT = 96;
    private static final int ROW_GAP = 10;

    /** 战宠种类(Enemy 子类简单名) → 中文类型名，与 MapContentManager 中的命名保持一致。 */
    private static final Map<String, String> SPECIES_CN = new HashMap<>();
    static {
        SPECIES_CN.put("Wolf", "野狼");
        SPECIES_CN.put("Tiger", "猛虎");
        SPECIES_CN.put("WildBoar", "野猪");
        SPECIES_CN.put("Viper", "毒蛇");
        SPECIES_CN.put("Bandit", "强盗");
        SPECIES_CN.put("FoxSpirit", "狐狸精");
        SPECIES_CN.put("ShrimpSoldier", "虾兵");
        SPECIES_CN.put("CrabGeneral", "蟹将");
        SPECIES_CN.put("Yaksha", "夜叉");
        SPECIES_CN.put("LittleGreenDragon", "小青龙");
        SPECIES_CN.put("GiantSeaTurtle", "大海龟");
        SPECIES_CN.put("LonelySpirit", "孤魂");
        SPECIES_CN.put("SavageWraith", "野鬼");
        SPECIES_CN.put("Vampire", "吸血鬼");
        SPECIES_CN.put("ChainBoundWraith", "幽灵");
        SPECIES_CN.put("GhostGeneral", "鬼将");
        SPECIES_CN.put("HellGuardian", "地狱守卫");
        SPECIES_CN.put("KingYanluo", "阎罗王");
        SPECIES_CN.put("YellowSpringGuide", "黄泉引路人");
        SPECIES_CN.put("BloodPoolDemonKing", "血池鬼王");
        SPECIES_CN.put("JudgeCuiYu", "判官崔钰");
    }

    private static class RowRect {
        final Pet pet;
        final Rect bounds;

        RowRect(Pet pet, Rect bounds) {
            this.pet = pet;
            this.bounds = bounds;
        }
    }

    public PetPanel() {
        this.isVisible = false;
        this.panelBounds = new Rect();
        this.closeButton = new Rect();
        this.listArea = new Rect();
        this.detailArea = new Rect();
        this.detailDeployButton = new Rect();
        this.detailHealButton = new Rect();
        this.detailRenameButton = new Rect();
        this.detailResetButton = new Rect();
        this.detailReleaseButton = new Rect();
        for (int i = 0; i < Pet.ATTR_COUNT; i++) {
            attrMinus[i] = new Rect();
            attrPlus[i] = new Rect();
        }
    }

    public void toggleVisibility() {
        isVisible = !isVisible;
        if (isVisible) {
            scrollOffset = 0;
            pendingRelease = null;
        }
    }

    public void show() {
        isVisible = true;
        scrollOffset = 0;
        pendingRelease = null;
    }

    public void hide() {
        isVisible = false;
        pendingRelease = null;
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

        int pad = 20;
        int areaTop = panelBounds.top + 104;
        int contentBottom = panelBounds.bottom - 30;

        int listLeft = panelBounds.left + pad;
        int listWidth = (int) (panelBounds.width() * 0.32f);
        listArea = new Rect(listLeft, areaTop, listLeft + listWidth, contentBottom);

        int detailLeft = listArea.right + 24;
        detailArea = new Rect(detailLeft, areaTop, panelBounds.right - pad, contentBottom);

        int p = 28;
        attrContentLeft = detailArea.left + p;
        attrContentRight = detailArea.right - p;
        int contentW = attrContentRight - attrContentLeft;

        // 详情区底部五个操作按钮
        int btnH = 76;
        int btnGap = 14;
        int btnY = detailArea.bottom - 30 - btnH;
        int bw = (contentW - 4 * btnGap) / 5;
        int bx = attrContentLeft;
        detailDeployButton.set(bx, btnY, bx + bw, btnY + btnH);
        detailHealButton.set(bx + (bw + btnGap), btnY, bx + 2 * bw + btnGap, btnY + btnH);
        detailRenameButton.set(bx + 2 * (bw + btnGap), btnY, bx + 3 * bw + 2 * btnGap, btnY + btnH);
        detailResetButton.set(bx + 3 * (bw + btnGap), btnY, bx + 4 * bw + 3 * btnGap, btnY + btnH);
        detailReleaseButton.set(bx + 4 * (bw + btnGap), btnY, bx + 5 * bw + 4 * btnGap, btnY + btnH);

        // 属性加点行几何(位于血条与底部按钮之间)
        int pmBtn = 44;
        attrRowsTop = detailArea.top + 246;
        float attrRowsBottom = btnY - 74;
        attrRowH = (attrRowsBottom - attrRowsTop) / Pet.ATTR_COUNT;
        if (attrRowH < 40) {
            attrRowH = 40;
        }
        for (int i = 0; i < Pet.ATTR_COUNT; i++) {
            int cy = (int) (attrRowsTop + attrRowH * i + attrRowH / 2);
            int half = pmBtn / 2;
            attrPlus[i].set(attrContentRight - pmBtn, cy - half, attrContentRight, cy + half);
            attrMinus[i].set(attrPlus[i].left - 10 - pmBtn, cy - half, attrPlus[i].left - 10, cy + half);
        }

        // 放生确认框(面板居中)
        int boxW = Math.min(520, width - 60);
        int boxH = 190;
        int boxX = panelBounds.centerX() - boxW / 2;
        int boxY = panelBounds.centerY() - boxH / 2;
        confirmBox.set(boxX, boxY, boxX + boxW, boxY + boxH);
        int cbtnW = (boxW - 40 * 2 - 30) / 2;
        int cbtnTop = boxY + boxH - 74;
        int cbtnBottom = boxY + boxH - 24;
        confirmYes.set(boxX + 40, cbtnTop, boxX + 40 + cbtnW, cbtnBottom);
        confirmNo.set(boxX + 40 + cbtnW + 30, cbtnTop, boxX + 40 + cbtnW + 30 + cbtnW, cbtnBottom);
    }

    /** 解析当前有效选中项; 若失效则回退到出战战宠或列表首项。 */
    private Pet resolveSelected() {
        List<Pet> roster = PetSystem.getInstance().getRoster();
        Pet sel = selectedPet;
        if (sel != null && roster.contains(sel)) {
            return sel;
        }
        Pet active = PetSystem.getInstance().getActivePet();
        if (active != null && roster.contains(active)) {
            selectedPet = active;
            return active;
        }
        if (!roster.isEmpty()) {
            Pet first = roster.get(0);
            selectedPet = first;
            return first;
        }
        selectedPet = null;
        return null;
    }

    /**
     * 依据当前花名册与滚动量计算左侧每行的边界(返回局部快照, 不修改共享状态)。
     * 同时刷新 maxScrollOffset 并夹取 scrollOffset。
     */
    private List<RowRect> computeRowRects() {
        List<Pet> roster = PetSystem.getInstance().getRoster();
        int count = roster.size();

        float totalContentHeight = count * (ROW_HEIGHT + ROW_GAP);
        float newMax = Math.max(0, totalContentHeight - listArea.height());
        maxScrollOffset = newMax;
        float offset = Math.max(0, Math.min(scrollOffset, newMax));
        scrollOffset = offset;

        List<RowRect> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Pet pet = roster.get(i);
            int yPos = listArea.top + i * (ROW_HEIGHT + ROW_GAP) - (int) offset;
            Rect bounds = new Rect(listArea.left, yPos, listArea.right, yPos + ROW_HEIGHT);
            rows.add(new RowRect(pet, bounds));
        }
        return rows;
    }

    public void draw(Canvas canvas) {
        if (!isVisible) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 面板背景
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(240, 20, 28, 24));
        canvas.drawRect(panelBounds, paint);

        // 边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(120, 230, 150));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        // 标题
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        int rosterCount = PetSystem.getInstance().getRoster().size();
        canvas.drawText("战宠花名册 (" + rosterCount + "/" + PetSystem.getInstance().getMaxRoster() + ")",
                panelBounds.centerX(), panelBounds.top + 45, paint);

        // 关闭按钮
        drawCloseButton(canvas, paint);

        // 左右分区小标题
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(24);
        paint.setColor(Color.rgb(150, 220, 165));
        canvas.drawText("战宠列表", listArea.left, listArea.top - 14, paint);
        canvas.drawText("战宠信息", detailArea.left, detailArea.top - 14, paint);

        // 左侧列表
        drawPetList(canvas, paint);
        // 右侧详情
        drawDetail(canvas, paint, resolveSelected());

        // 放生确认(置顶)
        if (pendingRelease != null) {
            drawConfirm(canvas, paint);
        }
    }

    // ==================== 左侧列表 ====================

    private void drawPetList(Canvas canvas, Paint paint) {
        List<RowRect> rows = computeRowRects();
        if (rows.isEmpty()) {
            paint.setColor(Color.rgb(160, 160, 160));
            paint.setTextSize(22);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("还没有战宠", listArea.centerX(), listArea.centerY() - 16, paint);
            canvas.drawText("去野外削弱怪物后", listArea.centerX(), listArea.centerY() + 16, paint);
            canvas.drawText("点击「捕捉」收服它", listArea.centerX(), listArea.centerY() + 48, paint);
            return;
        }

        Pet selected = resolveSelected();

        canvas.save();
        canvas.clipRect(listArea);
        for (RowRect row : rows) {
            Rect b = row.bounds;
            if (b.bottom < listArea.top || b.top > listArea.bottom) continue;
            drawRow(canvas, paint, row.pet, b, row.pet == selected);
        }
        canvas.restore();

        if (maxScrollOffset > 0) {
            drawScrollbar(canvas, paint);
        }
    }

    private void drawRow(Canvas canvas, Paint paint, Pet pet, Rect b, boolean selected) {
        int x = b.left;
        int y = b.top;
        int w = b.width();

        boolean isActive = pet == PetSystem.getInstance().getActivePet();
        boolean downed = pet.isDowned();

        // 背景
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selected ? Color.argb(140, 55, 80, 62) : Color.argb(90, 40, 50, 44));
        canvas.drawRoundRect(x, y, x + w, y + ROW_HEIGHT, 10, 10, paint);

        // 边框(选中=金, 出战=绿, 重伤=红, 待机=灰)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 3 : 2);
        if (selected) {
            paint.setColor(Color.rgb(255, 220, 120));
        } else if (isActive) {
            paint.setColor(Color.rgb(120, 255, 150));
        } else if (downed) {
            paint.setColor(Color.rgb(255, 110, 110));
        } else {
            paint.setColor(Color.rgb(120, 130, 140));
        }
        canvas.drawRoundRect(x, y, x + w, y + ROW_HEIGHT, 10, 10, paint);
        paint.setStyle(Paint.Style.FILL);

        // 名字
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        canvas.drawText(pet.getName(), x + 16, y + 34, paint);

        // 等级(右侧)
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(20);
        paint.setColor(Color.rgb(150, 220, 160));
        canvas.drawText("Lv." + pet.getLevel(), x + w - 16, y + 34, paint);

        // 状态标签
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(18);
        String statusText;
        if (isActive) {
            statusText = "出战中";
            paint.setColor(Color.rgb(120, 255, 150));
        } else if (downed) {
            statusText = "重伤";
            paint.setColor(Color.rgb(255, 120, 120));
        } else {
            statusText = "待机";
            paint.setColor(Color.rgb(180, 180, 190));
        }
        canvas.drawText(statusText, x + 16, y + 58, paint);

        // 迷你血条
        Enemy body = pet.getEntity();
        int curHp = body != null ? body.getHealth() : 0;
        int maxHp = body != null ? body.getMaxHealth() : 1;
        float hpRatio = maxHp <= 0 ? 0f : Math.max(0f, Math.min(1f, curHp / (float) maxHp));
        float barX = x + 16;
        float barW = w - 32;
        float barY = y + 70;
        float barH = 12;
        paint.setColor(Color.argb(120, 50, 50, 50));
        canvas.drawRoundRect(barX, barY, barX + barW, barY + barH, 5, 5, paint);
        paint.setColor(downed ? Color.rgb(180, 90, 90) : Color.rgb(90, 220, 120));
        canvas.drawRoundRect(barX, barY, barX + barW * hpRatio, barY + barH, 5, 5, paint);
    }

    private void drawScrollbar(Canvas canvas, Paint paint) {
        int scrollbarWidth = 6;
        int scrollbarX = listArea.right - 4;
        float totalHeight = listArea.height() + maxScrollOffset;
        float scrollbarHeight = Math.max(40, (listArea.height() / totalHeight) * listArea.height());
        float scrollbarY = listArea.top + (scrollOffset / maxScrollOffset) * (listArea.height() - scrollbarHeight);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 100, 100, 100));
        canvas.drawRoundRect(scrollbarX, listArea.top, scrollbarX + scrollbarWidth, listArea.bottom, 3, 3, paint);
        paint.setColor(Color.argb(160, 150, 220, 160));
        canvas.drawRoundRect(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 3, 3, paint);
    }

    // ==================== 右侧详情 ====================

    private void drawDetail(Canvas canvas, Paint paint, Pet pet) {
        // 详情区背景
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 34, 46, 38));
        canvas.drawRoundRect(detailArea.left, detailArea.top, detailArea.right, detailArea.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(140, 110, 200, 130));
        canvas.drawRoundRect(detailArea.left, detailArea.top, detailArea.right, detailArea.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        if (pet == null) {
            paint.setColor(Color.rgb(160, 160, 160));
            paint.setTextSize(26);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("从左侧列表选择一只战宠查看详情",
                    detailArea.centerX(), detailArea.centerY(), paint);
            return;
        }

        boolean isActive = pet == PetSystem.getInstance().getActivePet();
        boolean downed = pet.isDowned();

        int p = 28;
        float x = detailArea.left + p;
        float contentW = detailArea.width() - 2 * p;

        // 名称(大) + 状态标签
        float nameY = detailArea.top + 56;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        String nm = pet.getName();
        float nameW = paint.measureText(nm);
        canvas.drawText(nm, x, nameY, paint);

        paint.setTextSize(24);
        String statusText;
        if (isActive) {
            statusText = "[出战中]";
            paint.setColor(Color.rgb(120, 255, 150));
        } else if (downed) {
            statusText = "[重伤]";
            paint.setColor(Color.rgb(255, 120, 120));
        } else {
            statusText = "[待机]";
            paint.setColor(Color.rgb(180, 180, 190));
        }
        canvas.drawText(statusText, x + nameW + 22, nameY - 4, paint);

        // 分隔线
        float sepY = detailArea.top + 78;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(70, 255, 255, 255));
        canvas.drawLine(x, sepY, x + contentW, sepY, paint);
        paint.setStyle(Paint.Style.FILL);

        // 类型 / 等级
        float infoY = detailArea.top + 116;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(26);
        paint.setColor(Color.rgb(160, 175, 165));
        canvas.drawText("类型", x, infoY, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText(typeName(pet), x + 60, infoY, paint);
        paint.setColor(Color.rgb(160, 175, 165));
        canvas.drawText("等级", x + contentW * 0.5f, infoY, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Lv." + pet.getLevel(), x + contentW * 0.5f + 60, infoY, paint);

        // 经验条
        int expToNext = Math.max(1, PetSystem.getInstance().expToNext(pet.getLevel()));
        float expY = detailArea.top + 136;
        float expRatio = Math.max(0f, Math.min(1f, pet.getExp() / (float) expToNext));
        drawBar(canvas, paint, x, expY, contentW, 20, expRatio,
                Color.rgb(110, 180, 255), "经验 " + pet.getExp() + " / " + expToNext);

        // 气血条
        Enemy body = pet.getEntity();
        int curHp = body != null ? body.getHealth() : 0;
        int maxHp = body != null ? body.getMaxHealth() : 1;
        float hpRatio = maxHp <= 0 ? 0f : Math.max(0f, Math.min(1f, curHp / (float) maxHp));
        float hpY = detailArea.top + 172;
        drawBar(canvas, paint, x, hpY, contentW, 20, hpRatio,
                downed ? Color.rgb(180, 90, 90) : Color.rgb(90, 220, 120), "气血 " + curHp + " / " + maxHp);

        // 属性加点区(每行同行展示派生战斗属性)
        drawAttributeSection(canvas, paint, pet, x, contentW);

        // 操作按钮
        drawItemButton(canvas, paint, detailDeployButton, isActive ? "收回" : "出战",
                isActive ? Color.argb(200, 90, 120, 200) : Color.argb(200, 60, 160, 90));
        drawItemButton(canvas, paint, detailHealButton, "疗伤", Color.argb(200, 70, 150, 160));
        drawItemButton(canvas, paint, detailRenameButton, "改名", Color.argb(200, 150, 120, 200));
        drawItemButton(canvas, paint, detailResetButton, "洗点", Color.argb(200, 190, 150, 70));
        drawItemButton(canvas, paint, detailReleaseButton, "放生", Color.argb(200, 170, 80, 80));
    }

    private void drawAttributeSection(Canvas canvas, Paint paint, Pet pet, float x, float contentW) {
        // 小标题 + 剩余点数
        float headerY = attrRowsTop - 16;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(26);
        paint.setColor(Color.rgb(150, 220, 165));
        canvas.drawText("属性加点", x, headerY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        int remain = pet.getRemainPoints();
        paint.setColor(remain > 0 ? Color.rgb(255, 220, 120) : Color.rgb(150, 155, 150));
        canvas.drawText("剩余点数: " + remain, x + contentW, headerY, paint);

        boolean canPlus = remain > 0;
        for (int i = 0; i < Pet.ATTR_COUNT; i++) {
            float cy = attrRowsTop + attrRowH * i + attrRowH / 2;
            int val = pet.getAttr(i);
            int floor = pet.getAttrFloor(i);

            // 行底色(隔行)
            if (i % 2 == 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(40, 255, 255, 255));
                canvas.drawRoundRect(x, cy - attrRowH / 2 + 3, x + contentW, cy + attrRowH / 2 - 3, 6, 6, paint);
            }

            // 属性名
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(26);
            paint.setColor(Color.rgb(210, 220, 210));
            canvas.drawText(ATTR_LABELS[i], x + 10, cy + 9, paint);

            // 属性值
            paint.setTextSize(28);
            paint.setColor(val > floor ? Color.rgb(130, 255, 160) : Color.WHITE);
            String vs = String.valueOf(val);
            float vx = x + 120;
            canvas.drawText(vs, vx, cy + 9, paint);
            // 自由加点标注(不含升级固定成长)
            if (val > floor) {
                paint.setTextSize(20);
                paint.setColor(Color.rgb(130, 220, 150));
                canvas.drawText("(+" + (val - floor) + ")", vx + paint.measureText(vs) + 34, cy + 9, paint);
            }

            // 派生战斗属性(与基础属性同行, 名称与数值分别按固定列左对齐, 保证各行对齐)
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(24);
            paint.setColor(Color.rgb(255, 225, 150));
            float derivedLabelX = x + contentW - 330;
            float derivedValueX = x + contentW - 260;
            canvas.drawText(DERIVED_LABELS[i], derivedLabelX, cy + 9, paint);
            canvas.drawText(String.valueOf(derivedValue(pet, i)), derivedValueX, cy + 9, paint);

            // -/+ 按钮
            boolean canMinus = val > floor;
            drawPmButton(canvas, paint, attrMinus[i], "-", canMinus);
            drawPmButton(canvas, paint, attrPlus[i], "+", canPlus);
        }
    }

    /** 按属性行索引取对应派生战斗属性值: 0气血 1灵力 2攻击 3防御 4速度。 */
    private int derivedValue(Pet pet, int index) {
        switch (index) {
            case 0: return pet.getDerivedMaxHealth();
            case 1: return pet.getDerivedMana();
            case 2: return pet.getDerivedAttack();
            case 3: return pet.getDerivedDefense();
            case 4: return pet.getDerivedSpeed();
            default: return 0;
        }
    }

    private void drawPmButton(Canvas canvas, Paint paint, Rect btn, String symbol, boolean enabled) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(enabled ? Color.argb(210, 60, 130, 80) : Color.argb(120, 70, 74, 72));
        canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(enabled ? Color.rgb(190, 255, 205) : Color.rgb(120, 124, 122));
        canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(enabled ? Color.WHITE : Color.rgb(140, 144, 142));
        canvas.drawText(symbol, btn.centerX(), btn.centerY() + 11, paint);
    }

    private void drawBar(Canvas canvas, Paint paint, float bx, float by, float bw, float bh,
                         float ratio, int color, String text) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 50, 50, 50));
        canvas.drawRoundRect(bx, by, bx + bw, by + bh, 6, 6, paint);
        paint.setColor(color);
        canvas.drawRoundRect(bx, by, bx + bw * Math.max(0f, Math.min(1f, ratio)), by + bh, 6, 6, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(150, 160, 160, 160));
        canvas.drawRoundRect(bx, by, bx + bw, by + bh, 6, 6, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(15);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, bx + bw / 2, by + bh - 5, paint);
    }

    /** 宠物类型名称: 优先查种类中文映射，缺失时回退到去掉等级后缀的战宠名。 */
    private String typeName(Pet pet) {
        String sc = pet.getSpeciesClass();
        String simple = null;
        if (sc != null && !sc.isEmpty()) {
            int i = sc.lastIndexOf('.');
            simple = i >= 0 ? sc.substring(i + 1) : sc;
            String cn = SPECIES_CN.get(simple);
            if (cn != null) {
                return cn;
            }
        }
        String nm = pet.getName();
        if (nm != null && !nm.isEmpty()) {
            return nm.replace("BOSS", "").replace("精英", "").replace("首领", "");
        }
        return simple != null ? simple : "未知";
    }

    private void drawItemButton(Canvas canvas, Paint paint, Rect btn, String label, int bgColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(btn.left, btn.top, btn.right, btn.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(26);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, btn.centerX(), btn.centerY() + 9, paint);
    }

    private void drawCloseButton(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 255, 80, 80));
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(closeButton.left, closeButton.top, closeButton.right, closeButton.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        float padding = 10;
        canvas.drawLine(closeButton.left + padding, closeButton.top + padding,
                closeButton.right - padding, closeButton.bottom - padding, paint);
        canvas.drawLine(closeButton.right - padding, closeButton.top + padding,
                closeButton.left + padding, closeButton.bottom - padding, paint);
    }

    private void drawConfirm(Canvas canvas, Paint paint) {
        // 半透明遮罩
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRect(panelBounds, paint);

        // 确认框
        paint.setColor(Color.argb(245, 35, 30, 30));
        canvas.drawRoundRect(confirmBox.left, confirmBox.top, confirmBox.right, confirmBox.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(255, 140, 140));
        canvas.drawRoundRect(confirmBox.left, confirmBox.top, confirmBox.right, confirmBox.bottom, 12, 12, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        String nm = pendingRelease != null ? pendingRelease.getName() : "";
        canvas.drawText("确定要放生「" + nm + "」吗？", confirmBox.centerX(), confirmBox.top + 60, paint);
        paint.setTextSize(22);
        paint.setColor(Color.rgb(220, 190, 190));
        canvas.drawText("放生后将永久离队，无法找回。", confirmBox.centerX(), confirmBox.top + 95, paint);

        drawItemButton(canvas, paint, confirmYes, "确认放生", Color.argb(220, 180, 70, 70));
        drawItemButton(canvas, paint, confirmNo, "取消", Color.argb(200, 90, 100, 110));
    }

    // ==================== 触摸 ====================

    public boolean handleTouch(float x, float y) {
        if (!isVisible) return false;

        // 放生确认优先
        if (pendingRelease != null) {
            if (TouchUtil.checkIsInTouchRectFloat(confirmYes, x, y)) {
                String nm = pendingRelease.getName();
                PetSystem.getInstance().release(pendingRelease);
                GameEngine.getInstance().showCenterToast("已放生「" + nm + "」", 1500);
                pendingRelease = null;
                return true;
            }
            if (TouchUtil.checkIsInTouchRectFloat(confirmNo, x, y)) {
                pendingRelease = null;
                return true;
            }
            // 确认框显示时，吞噬面板内的其它触摸
            return TouchUtil.checkIsInTouchRectFloat(panelBounds, x, y);
        }

        // 关闭按钮
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }

        // 详情区操作(属性加点 + 功能按钮)
        Pet sel = resolveSelected();
        if (sel != null) {
            for (int i = 0; i < Pet.ATTR_COUNT; i++) {
                if (TouchUtil.checkIsInTouchRectFloat(attrPlus[i], x, y)) {
                    if (!PetSystem.getInstance().allocatePoint(sel, i)) {
                        GameEngine.getInstance().showCenterToast("没有可分配的属性点", 1000);
                    }
                    return true;
                }
                if (TouchUtil.checkIsInTouchRectFloat(attrMinus[i], x, y)) {
                    if (!PetSystem.getInstance().deallocatePoint(sel, i)) {
                        GameEngine.getInstance().showCenterToast("该属性已是初始值", 1000);
                    }
                    return true;
                }
            }
            if (TouchUtil.checkIsInTouchRectFloat(detailDeployButton, x, y)) {
                boolean wasActive = sel == PetSystem.getInstance().getActivePet();
                if (wasActive) {
                    PetSystem.getInstance().recallActive();
                    GameEngine.getInstance().showCenterToast("已收回「" + sel.getName() + "」", 1200);
                } else {
                    PetSystem.getInstance().setActive(sel);
                    GameEngine.getInstance().showCenterToast("「" + sel.getName() + "」出战！", 1200);
                }
                return true;
            }
            if (TouchUtil.checkIsInTouchRectFloat(detailHealButton, x, y)) {
                PetSystem.getInstance().heal(sel);
                GameEngine.getInstance().showCenterToast("「" + sel.getName() + "」已疗伤", 1200);
                return true;
            }
            if (TouchUtil.checkIsInTouchRectFloat(detailRenameButton, x, y)) {
                GameEngine.getInstance().promptRenamePet(sel);
                return true;
            }
            if (TouchUtil.checkIsInTouchRectFloat(detailResetButton, x, y)) {
                PetSystem.getInstance().resetPoints(sel);
                GameEngine.getInstance().showCenterToast("已洗点，自由属性点已全部退回", 1200);
                return true;
            }
            if (TouchUtil.checkIsInTouchRectFloat(detailReleaseButton, x, y)) {
                pendingRelease = sel;
                return true;
            }
        }

        // 左侧列表: 点选
        if (TouchUtil.checkIsInTouchRectFloat(listArea, x, y)) {
            List<RowRect> rows = computeRowRects();
            for (RowRect row : rows) {
                if (row.bounds.bottom < listArea.top || row.bounds.top > listArea.bottom) continue;
                if (TouchUtil.checkIsInTouchRectFloat(row.bounds, x, y)) {
                    selectedPet = row.pet;
                    return true;
                }
            }
            return true; // 消费列表区触摸(用于滚动)
        }

        return false;
    }

    public boolean handleScroll(float deltaX, float deltaY) {
        if (!isVisible) return false;
        // 重新计算 maxScrollOffset 后再夹取(空花名册时 maxScrollOffset 为 0, 直接返回)
        computeRowRects();
        if (maxScrollOffset <= 0) return false;
        scrollOffset -= deltaY;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        return true;
    }
}

package com.game.dream.panel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import com.game.dream.GameEngine;
import com.game.dream.bean.MapInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.figure.Player;
import com.game.dream.map.MapOverviewBitmap;
import com.game.dream.npc.AnimalNpc;
import com.game.dream.npc.Npc;
import com.game.dream.system.MapSystem;
import com.game.dream.system.NpcSystem;
import com.game.dream.utils.TouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 大地图查看：左侧地图可拖动，右侧常驻 NPC 列表可选中高亮。
 */
public class WorldMapPanel {

    private static final int MAP_RENDER_SIZE = 768;
    private static final int NPC_LIST_WIDTH = 300;
    private static final int CONTENT_TOP_OFFSET = 64;
    private static final int LEGEND_HEIGHT = 96;
    private static final int LIST_ITEM_HEIGHT = 44;
    private static final int LIST_HEADER_HEIGHT = 36;
    private static final long LIST_NPC_DOUBLE_TAP_MS = 450;
    private static final int ACTION_BTN_W = 112;
    private static final int ACTION_BTN_H = 44;
    private static final int ACTION_BAR_GAP = 12;
    /** 图例中「玩家 / 常驻NPC / 任务 / 夜间」等说明文字 */
    private static final float LEGEND_TEXT_SIZE = 26f;
    /** 右侧列表 NPC 名称 */
    private static final float LIST_NPC_NAME_TEXT_SIZE = 24f;

    private boolean isVisible;
    private final Rect panelBounds = new Rect();
    private final Rect closeButton = new Rect();
    private final Rect mapViewBounds = new Rect();
    private final Rect legendBounds = new Rect();
    private final Rect npcListBounds = new Rect();

    private Bitmap mapBitmap;
    private int mapWidth;
    private int mapHeight;
    private float viewScale;
    private float panWorldX;
    private float panWorldY;
    private String mapTitle = "";

    private final List<Npc> residentNpcs = new ArrayList<>();
    private int selectedNpcId = -1;

    private boolean mapDragActive;
    private float lastDragX;
    private float lastDragY;
    private float dragTotalDist;

    private float listScrollOffset;
    private float maxListScroll;
    private boolean listDragActive;
    private float lastListDragY;
    private float listDragTotalDist;
    private final List<Rect> listItemBounds = new ArrayList<>();
    private int lastListTapNpcId = -1;
    private long lastListTapTime;

    private static final int CHIP_NPC_NORMAL = 1;
    private static final int CHIP_NPC_QUEST = 2;
    private static final int CHIP_NPC_NIGHT = 3;
    private static final int CHIP_MOB_NORMAL = 4;
    private static final int CHIP_MOB_LEADER = 5;
    private static final int CHIP_MOB_ELITE = 6;
    private static final int CHIP_MOB_BOSS = 7;

    private boolean showNpcNormal = true;
    private boolean showNpcQuest = true;
    private boolean showNpcNight = true;
    private boolean showMobNormal = true;
    private boolean showMobLeader = true;
    private boolean showMobElite = true;
    private boolean showMobBoss = true;

    private static class FilterChip {
        final Rect bounds = new Rect();
        final int id;
        final String label;
        final int dotColor;

        FilterChip(int id, String label, int dotColor) {
            this.id = id;
            this.label = label;
            this.dotColor = dotColor;
        }
    }

    private final List<FilterChip> filterChips = new ArrayList<>();

    private boolean hasMapTarget;
    private float mapTargetWorldX;
    private float mapTargetWorldY;
    private float mapTargetScreenX;
    private float mapTargetScreenY;
    private final Rect actionBarBounds = new Rect();
    private final Rect btnNavigate = new Rect();
    private final Rect btnTeleport = new Rect();

    public void show() {
        isVisible = true;
        selectedNpcId = -1;
        mapDragActive = false;
        listDragActive = false;
        listScrollOffset = 0;
        lastListTapNpcId = -1;
        lastListTapTime = 0;
        rebuildMapContent();
        centerOnPlayer();
        updateListItemBounds();
    }

    public void hide() {
        isVisible = false;
        selectedNpcId = -1;
        mapDragActive = false;
        listDragActive = false;
        clearMapTarget();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void invalidateMap() {
        if (mapBitmap != null && !mapBitmap.isRecycled()) {
            mapBitmap.recycle();
            mapBitmap = null;
        }
        if (isVisible) {
            rebuildMapContent();
            centerOnPlayer();
            updateListItemBounds();
        }
    }

    public void cleanup() {
        if (mapBitmap != null && !mapBitmap.isRecycled()) {
            mapBitmap.recycle();
            mapBitmap = null;
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        panelBounds.set(x, y, x + width, y + height);

        int pad = 12;
        closeButton.set(
                panelBounds.right - 52 - pad,
                panelBounds.top + pad,
                panelBounds.right - pad,
                panelBounds.top + pad + 52
        );

        int contentBottom = panelBounds.bottom - 12;
        int inset = 16;
        int gap = 12;

        int listRight = panelBounds.right - inset;
        int listLeft = listRight - NPC_LIST_WIDTH;
        npcListBounds.set(listLeft, panelBounds.top + CONTENT_TOP_OFFSET, listRight, contentBottom);

        int mapLeft = panelBounds.left + inset;
        int mapRight = listLeft - gap;
        int legendBottom = contentBottom;
        int legendTop = legendBottom - LEGEND_HEIGHT;
        legendBounds.set(mapLeft, legendTop, mapRight, legendBottom);

        int mapTop = panelBounds.top + CONTENT_TOP_OFFSET;
        mapViewBounds.set(mapLeft, mapTop, mapRight, legendTop - 8);

        updateViewScale();
        clampPan();
        updateListItemBounds();
        layoutFilterChips();
    }

    private void rebuildMapContent() {
        MapInfo map = GameEngine.getInstance().getMap();
        mapWidth = MapSystem.getInstance().getCurMapInfo().getMapWidth();
        mapHeight = MapSystem.getInstance().getCurMapInfo().getMapHeight();
        mapTitle = MapSystem.getInstance().getCurMapInfo().getMapName();

        if (mapBitmap != null && !mapBitmap.isRecycled()) {
            mapBitmap.recycle();
        }
        mapBitmap = MapOverviewBitmap.render(map, mapWidth, mapHeight,
                com.game.dream.common.Constants.TILE_SIZE, MAP_RENDER_SIZE);
        updateViewScale();
        reloadResidentNpcs();
    }

    private void reloadResidentNpcs() {
        residentNpcs.clear();
        int mapId = MapSystem.getInstance().getCurMapInfo().getMapId();
        for (Npc npc : NpcSystem.getInstance().getMapNpcList(mapId)) {
            if (!(npc instanceof AnimalNpc)) {
                residentNpcs.add(npc);
            }
        }
        listScrollOffset = 0;
        updateListItemBounds();
    }

    private void updateListItemBounds() {
        listItemBounds.clear();
        if (npcListBounds.width() <= 0) {
            maxListScroll = 0;
            return;
        }
        int innerPad = 8;
        int itemGap = 8;
        int itemW = npcListBounds.width() - innerPad * 2;
        int listAreaHeight = npcListBounds.height() - LIST_HEADER_HEIGHT;
        int contentHeight = residentNpcs.size() * (LIST_ITEM_HEIGHT + itemGap);
        maxListScroll = Math.max(0, contentHeight - listAreaHeight + innerPad);

        int listContentTop = npcListBounds.top + LIST_HEADER_HEIGHT;
        for (int i = 0; i < residentNpcs.size(); i++) {
            int left = npcListBounds.left + innerPad;
            int top = listContentTop + innerPad + i * (LIST_ITEM_HEIGHT + itemGap) - (int) listScrollOffset;
            listItemBounds.add(new Rect(left, top, left + itemW, top + LIST_ITEM_HEIGHT));
        }
    }

    private void updateViewScale() {
        if (mapViewBounds.width() <= 0 || mapWidth <= 0) {
            viewScale = 1f;
            return;
        }
        float fitW = (float) mapViewBounds.width() / mapWidth;
        float fitH = (float) mapViewBounds.height() / mapHeight;
        float fit = Math.min(fitW, fitH);
        float zoomFactor;
        int maxDim = Math.max(mapWidth, mapHeight);
        if (maxDim < 12000) {
            zoomFactor = 11f;
        } else if (maxDim > 40000) {
            zoomFactor = 6f;
        } else {
            zoomFactor = 8f;
        }
        viewScale = fit * zoomFactor;
    }

    private void centerOnPlayer() {
        Player player = GameEngine.getInstance().getPlayer();
        if (player == null || viewScale <= 0) {
            return;
        }
        float viewWorldW = mapViewBounds.width() / viewScale;
        float viewWorldH = mapViewBounds.height() / viewScale;
        panWorldX = player.getX() - viewWorldW / 2f;
        panWorldY = player.getY() - viewWorldH / 2f;
        clampPan();
    }

    private void focusOnNpc(Npc npc) {
        if (npc == null || viewScale <= 0) {
            return;
        }
        selectedNpcId = npc.getId();
        float viewWorldW = mapViewBounds.width() / viewScale;
        float viewWorldH = mapViewBounds.height() / viewScale;
        panWorldX = npc.getX() - viewWorldW / 2f;
        panWorldY = npc.getY() - viewWorldH / 2f;
        clampPan();
    }

    private void clampPan() {
        float viewWorldW = mapViewBounds.width() / viewScale;
        float viewWorldH = mapViewBounds.height() / viewScale;

        float maxPanX = Math.max(0, mapWidth - viewWorldW);
        float maxPanY = Math.max(0, mapHeight - viewWorldH);
        panWorldX = Math.max(0, Math.min(panWorldX, maxPanX));
        panWorldY = Math.max(0, Math.min(panWorldY, maxPanY));
    }

    private float worldToScreenX(float worldX) {
        return mapViewBounds.left + (worldX - panWorldX) * viewScale;
    }

    private float worldToScreenY(float worldY) {
        return mapViewBounds.top + (worldY - panWorldY) * viewScale;
    }

    private float screenToWorldX(float screenX) {
        return panWorldX + (screenX - mapViewBounds.left) / viewScale;
    }

    private float screenToWorldY(float screenY) {
        return panWorldY + (screenY - mapViewBounds.top) / viewScale;
    }

    private void clearMapTarget() {
        hasMapTarget = false;
        actionBarBounds.setEmpty();
        btnNavigate.setEmpty();
        btnTeleport.setEmpty();
    }

    private void setMapTarget(float screenX, float screenY) {
        mapTargetScreenX = screenX;
        mapTargetScreenY = screenY;
        mapTargetWorldX = screenToWorldX(screenX);
        mapTargetWorldY = screenToWorldY(screenY);
        mapTargetWorldX = Math.max(0, Math.min(mapTargetWorldX, mapWidth));
        mapTargetWorldY = Math.max(0, Math.min(mapTargetWorldY, mapHeight));
        hasMapTarget = true;
        layoutActionBar();
    }

    private void layoutActionBar() {
        int barW = ACTION_BTN_W * 2 + ACTION_BAR_GAP + 16;
        int barH = ACTION_BTN_H + 12;
        int centerX = (int) mapTargetScreenX;
        int top = (int) mapTargetScreenY - barH - 16;
        int left = centerX - barW / 2;
        if (left < mapViewBounds.left + 4) {
            left = mapViewBounds.left + 4;
        }
        if (left + barW > mapViewBounds.right - 4) {
            left = mapViewBounds.right - 4 - barW;
        }
        if (top < mapViewBounds.top + 4) {
            top = (int) mapTargetScreenY + 20;
        }
        if (top + barH > mapViewBounds.bottom - 4) {
            top = mapViewBounds.bottom - 4 - barH;
        }
        actionBarBounds.set(left, top, left + barW, top + barH);
        int btnTop = actionBarBounds.top + 6;
        int btnBottom = btnTop + ACTION_BTN_H;
        btnNavigate.set(actionBarBounds.left + 8, btnTop,
                actionBarBounds.left + 8 + ACTION_BTN_W, btnBottom);
        btnTeleport.set(btnNavigate.right + ACTION_BAR_GAP, btnTop,
                btnNavigate.right + ACTION_BAR_GAP + ACTION_BTN_W, btnBottom);
    }

    private void drawMapActionBar(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(230, 18, 24, 36));
        canvas.drawRoundRect(actionBarBounds.left, actionBarBounds.top,
                actionBarBounds.right, actionBarBounds.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(100, 150, 210));
        canvas.drawRoundRect(actionBarBounds.left, actionBarBounds.top,
                actionBarBounds.right, actionBarBounds.bottom, 10, 10, paint);
        paint.setStyle(Paint.Style.FILL);

        drawActionButton(canvas, paint, btnNavigate, "导航", Color.rgb(50, 120, 200));
        drawActionButton(canvas, paint, btnTeleport, "传送", Color.rgb(90, 160, 90));
    }

    private void drawActionButton(Canvas canvas, Paint paint, Rect rect, String label, int fillColor) {
        paint.setColor(fillColor);
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.argb(180, 255, 255, 255));
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(24f);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = rect.centerY() - (paint.descent() + paint.ascent()) / 2f;
        canvas.drawText(label, rect.centerX(), textY, paint);
    }

    private boolean handleActionBarTouch(float x, float y) {
        if (!hasMapTarget) {
            return false;
        }
        layoutActionBar();
        if (TouchUtil.checkIsInTouchRectFloat(btnNavigate, x, y)) {
            if (GameEngine.getInstance().startWorldMapNavigation(mapTargetWorldX, mapTargetWorldY)) {
                hide();
            }
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(btnTeleport, x, y)) {
            if (GameEngine.getInstance().tryWorldMapLocalTeleport(mapTargetWorldX, mapTargetWorldY)) {
                hide();
            }
            return true;
        }
        return TouchUtil.checkIsInTouchRectFloat(actionBarBounds, x, y);
    }

    public void draw(Canvas canvas) {
        if (!isVisible) {
            return;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.argb(235, 12, 18, 28));
        canvas.drawRect(panelBounds, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(90, 140, 200));
        canvas.drawRect(panelBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("世界地图 · " + mapTitle, panelBounds.centerX(), panelBounds.top + 44, paint);

        paint.setColor(Color.argb(255, 20, 24, 32));
        canvas.drawRect(mapViewBounds, paint);

        int saveCount = canvas.save();
        canvas.clipRect(mapViewBounds);

        if (mapBitmap != null && !mapBitmap.isRecycled()) {
            RectF dst = new RectF(
                    worldToScreenX(0),
                    worldToScreenY(0),
                    worldToScreenX(mapWidth),
                    worldToScreenY(mapHeight)
            );
            Rect src = new Rect(0, 0, mapBitmap.getWidth(), mapBitmap.getHeight());
            canvas.drawBitmap(mapBitmap, src, dst, null);
        } else {
            paint.setColor(Color.DKGRAY);
            paint.setTextSize(22);
            canvas.drawText("地图加载中…", mapViewBounds.centerX(), mapViewBounds.centerY(), paint);
        }

        Paint npcPaint = new Paint();
        npcPaint.setAntiAlias(true);
        float markerR = markerRadius();
        long now = System.currentTimeMillis();

        for (Npc npc : residentNpcs) {
            if (!shouldShowNpc(npc)) {
                continue;
            }
            float sx = worldToScreenX(npc.getX());
            float sy = worldToScreenY(npc.getY());
            if (!isInMapView(npc.getX(), npc.getY())) {
                continue;
            }
            boolean selected = npc.getId() == selectedNpcId;

            if (selected) {
                float pulse = 10f + 4f * (float) Math.sin(now / 220.0);
                npcPaint.setStyle(Paint.Style.STROKE);
                npcPaint.setStrokeWidth(4f);
                npcPaint.setColor(Color.rgb(255, 255, 120));
                canvas.drawCircle(sx, sy, markerR + pulse, npcPaint);
                npcPaint.setColor(Color.WHITE);
                npcPaint.setStrokeWidth(2f);
                canvas.drawCircle(sx, sy, markerR + pulse - 3f, npcPaint);
            }

            npcPaint.setStyle(Paint.Style.FILL);
            if (npc.isNightOnly()) {
                npcPaint.setColor(Color.rgb(160, 120, 255));
            } else if (npc.hasQuest()) {
                npcPaint.setColor(Color.rgb(255, 210, 60));
            } else {
                npcPaint.setColor(Color.rgb(80, 200, 255));
            }
            float r = selected ? markerR + 3f : markerR;
            canvas.drawCircle(sx, sy, r, npcPaint);
            npcPaint.setStyle(Paint.Style.STROKE);
            npcPaint.setStrokeWidth(selected ? 2.5f : 1.5f);
            npcPaint.setColor(selected ? Color.WHITE : Color.argb(200, 0, 0, 0));
            canvas.drawCircle(sx, sy, r, npcPaint);
        }

        drawEnemyMarkers(canvas, npcPaint);

        Player player = GameEngine.getInstance().getPlayer();
        if (player != null) {
            float px = worldToScreenX(player.getX());
            float py = worldToScreenY(player.getY());
            paint.setColor(Color.RED);
            canvas.drawCircle(px, py, markerR + 1f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(px, py, markerR + 1f, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        if (hasMapTarget) {
            float tx = worldToScreenX(mapTargetWorldX);
            float ty = worldToScreenY(mapTargetWorldY);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setColor(Color.rgb(255, 200, 60));
            canvas.drawCircle(tx, ty, markerR + 5f, paint);
            paint.setColor(Color.argb(90, 255, 200, 60));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(tx, ty, markerR + 3f, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        canvas.restoreToCount(saveCount);

        if (hasMapTarget) {
            drawMapActionBar(canvas, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawRect(mapViewBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        drawLegend(canvas, paint);
        drawNpcList(canvas, paint);

        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(220, 60, 60));
        canvas.drawRect(closeButton, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("×", closeButton.centerX(), closeButton.centerY() + 10, paint);
    }

    private float markerRadius() {
        return 6f;
    }

    private void layoutFilterChips() {
        filterChips.clear();
        filterChips.add(new FilterChip(CHIP_NPC_NORMAL, "常驻NPC", Color.rgb(80, 200, 255)));
        filterChips.add(new FilterChip(CHIP_NPC_QUEST, "任务", Color.rgb(255, 210, 60)));
        filterChips.add(new FilterChip(CHIP_NPC_NIGHT, "夜间", Color.rgb(160, 120, 255)));
        filterChips.add(new FilterChip(CHIP_MOB_NORMAL, "普通怪", Color.rgb(110, 190, 95)));
        filterChips.add(new FilterChip(CHIP_MOB_LEADER, "头领", Color.rgb(255, 150, 50)));
        filterChips.add(new FilterChip(CHIP_MOB_ELITE, "精英", Color.rgb(230, 90, 210)));
        filterChips.add(new FilterChip(CHIP_MOB_BOSS, "BOSS", Color.rgb(255, 55, 55)));

        Paint measure = new Paint();
        measure.setTextSize(LEGEND_TEXT_SIZE);
        int row1Y = legendBounds.top + 34;
        int row2Y = legendBounds.bottom - 14;
        int playerBlockW = (int) (36 + measure.measureText("玩家"));
        layoutChipRow(row1Y, legendBounds.left + 8 + playerBlockW, 0, 3, measure);
        layoutChipRow(row2Y, legendBounds.left + 8, 3, filterChips.size(), measure);
    }

    private int layoutChipRow(int baseY, int startX, int from, int to, Paint measure) {
        int x = startX;
        int chipH = 34;
        int top = baseY - chipH + 10;
        int bottom = baseY + 10;
        for (int i = from; i < to && i < filterChips.size(); i++) {
            FilterChip chip = filterChips.get(i);
            float textW = measure.measureText(chip.label);
            int chipW = (int) (28 + textW + 14);
            chip.bounds.set(x, top, x + chipW, bottom);
            x += chipW + 10;
        }
        return x;
    }

    private boolean isChipEnabled(int chipId) {
        switch (chipId) {
            case CHIP_NPC_NORMAL: return showNpcNormal;
            case CHIP_NPC_QUEST: return showNpcQuest;
            case CHIP_NPC_NIGHT: return showNpcNight;
            case CHIP_MOB_NORMAL: return showMobNormal;
            case CHIP_MOB_LEADER: return showMobLeader;
            case CHIP_MOB_ELITE: return showMobElite;
            case CHIP_MOB_BOSS: return showMobBoss;
            default: return true;
        }
    }

    private void toggleChip(int chipId) {
        switch (chipId) {
            case CHIP_NPC_NORMAL: showNpcNormal = !showNpcNormal; break;
            case CHIP_NPC_QUEST: showNpcQuest = !showNpcQuest; break;
            case CHIP_NPC_NIGHT: showNpcNight = !showNpcNight; break;
            case CHIP_MOB_NORMAL: showMobNormal = !showMobNormal; break;
            case CHIP_MOB_LEADER: showMobLeader = !showMobLeader; break;
            case CHIP_MOB_ELITE: showMobElite = !showMobElite; break;
            case CHIP_MOB_BOSS: showMobBoss = !showMobBoss; break;
            default: break;
        }
    }

    private boolean shouldShowNpc(Npc npc) {
        if (npc.isNightOnly()) {
            return showNpcNight;
        }
        if (npc.hasQuest()) {
            return showNpcQuest;
        }
        return showNpcNormal;
    }

    private boolean shouldShowEnemy(Enemy enemy) {
        if (enemy == null || !enemy.isAlive() || enemy.isPet()) {
            return false;
        }
        switch (enemy.getEnemyLevel()) {
            case NORMAL:
                return showMobNormal;
            case LEADER:
                return showMobLeader;
            case ELITE:
                return showMobElite;
            case BOSS:
            case STAGE_BOSS:
                return showMobBoss;
            default:
                return false;
        }
    }

    private boolean isInMapView(float wx, float wy) {
        float vw = mapViewBounds.width() / viewScale;
        float vh = mapViewBounds.height() / viewScale;
        float pad = 40f;
        return wx >= panWorldX - pad && wx <= panWorldX + vw + pad
                && wy >= panWorldY - pad && wy <= panWorldY + vh + pad;
    }

    private void drawEnemyMarkers(Canvas canvas, Paint paint) {
        List<Enemy> enemies = GameEngine.getInstance().getEnemies();
        if (enemies == null || enemies.isEmpty()) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        for (Enemy enemy : enemies) {
            if (!shouldShowEnemy(enemy)) {
                continue;
            }
            float wx = enemy.getX();
            float wy = enemy.getY();
            if (!isInMapView(wx, wy)) {
                continue;
            }
            float sx = worldToScreenX(wx);
            float sy = worldToScreenY(wy);
            drawEnemyMarker(canvas, paint, enemy.getEnemyLevel(), sx, sy);
        }
    }

    private void drawEnemyMarker(Canvas canvas, Paint paint, Enemy.EnemyLevel level,
                                 float sx, float sy) {
        switch (level) {
            case NORMAL:
                paint.setColor(Color.rgb(110, 190, 95));
                canvas.drawCircle(sx, sy, 3.5f, paint);
                break;
            case LEADER:
                paint.setColor(Color.rgb(255, 150, 50));
                float s = 5f;
                canvas.drawRect(sx - s, sy - s, sx + s, sy + s, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.2f);
                paint.setColor(Color.argb(200, 0, 0, 0));
                canvas.drawRect(sx - s, sy - s, sx + s, sy + s, paint);
                paint.setStyle(Paint.Style.FILL);
                break;
            case ELITE: {
                paint.setColor(Color.rgb(230, 90, 210));
                Path tri = new Path();
                float h = 7f;
                tri.moveTo(sx, sy - h);
                tri.lineTo(sx + h, sy + h * 0.6f);
                tri.lineTo(sx - h, sy + h * 0.6f);
                tri.close();
                canvas.drawPath(tri, paint);
                break;
            }
            case BOSS:
            case STAGE_BOSS:
                paint.setColor(Color.rgb(255, 55, 55));
                canvas.drawCircle(sx, sy, 7f, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.5f);
                paint.setColor(Color.rgb(255, 230, 120));
                canvas.drawCircle(sx, sy, 10f, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(sx, sy, 7f, paint);
                paint.setStyle(Paint.Style.FILL);
                break;
            default:
                break;
        }
    }

    private void drawLegend(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(60, 30, 35, 45));
        canvas.drawRect(legendBounds, paint);

        layoutFilterChips();

        int row1Y = legendBounds.top + 34;
        int row2Y = legendBounds.bottom - 14;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(LEGEND_TEXT_SIZE);

        int x = legendBounds.left + 8;
        paint.setColor(Color.RED);
        canvas.drawCircle(x + 6, row1Y - 6, 6, paint);
        paint.setColor(Color.rgb(220, 220, 220));
        canvas.drawText("玩家", x + 18, row1Y, paint);
        x += (int) paint.measureText("玩家") + 36;

        drawFilterChipRow(canvas, paint, row1Y, 0, 3);
        drawFilterChipRow(canvas, paint, row2Y, 3, filterChips.size());

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.rgb(140, 160, 180));
        paint.setTextSize(LEGEND_TEXT_SIZE);
        canvas.drawText("点选类型开关 · 拖动地图", legendBounds.right - 8, legendBounds.top + 54, paint);
    }

    private void drawFilterChipRow(Canvas canvas, Paint paint, int baseY, int from, int to) {
        for (int i = from; i < to && i < filterChips.size(); i++) {
            FilterChip chip = filterChips.get(i);
            boolean on = isChipEnabled(chip.id);
            int alpha = on ? 255 : 110;
            paint.setAlpha(alpha);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(on ? 50 : 25, 255, 255, 255));
            canvas.drawRoundRect(chip.bounds.left, chip.bounds.top, chip.bounds.right, chip.bounds.bottom,
                    8, 8, paint);

            float iconCx = chip.bounds.left + 14;
            float iconCy = chip.bounds.centerY();
            paint.setColor(chip.dotColor);
            paint.setAlpha(alpha);
            if (chip.id >= CHIP_MOB_NORMAL) {
                drawChipMarkerPreview(canvas, paint, chip.id, iconCx, iconCy);
            } else {
                canvas.drawCircle(iconCx, iconCy, 6, paint);
            }

            paint.setColor(Color.rgb(220, 220, 220));
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(LEGEND_TEXT_SIZE);
            canvas.drawText(chip.label, chip.bounds.left + 26, baseY, paint);
            paint.setAlpha(255);
        }
    }

  /** 图例左侧小图标与地图上标记形状一致 */
    private void drawChipMarkerPreview(Canvas canvas, Paint paint, int chipId, float cx, float cy) {
        paint.setStyle(Paint.Style.FILL);
        switch (chipId) {
            case CHIP_MOB_NORMAL:
                canvas.drawCircle(cx, cy, 4f, paint);
                break;
            case CHIP_MOB_LEADER:
                paint.setStyle(Paint.Style.FILL);
                float s = 4f;
                canvas.drawRect(cx - s, cy - s, cx + s, cy + s, paint);
                break;
            case CHIP_MOB_ELITE: {
                Path tri = new Path();
                float h = 5f;
                tri.moveTo(cx, cy - h);
                tri.lineTo(cx + h, cy + h * 0.5f);
                tri.lineTo(cx - h, cy + h * 0.5f);
                tri.close();
                canvas.drawPath(tri, paint);
                break;
            }
            case CHIP_MOB_BOSS:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f);
                canvas.drawCircle(cx, cy, 6f, paint);
                paint.setStyle(Paint.Style.FILL);
                break;
            default:
                break;
        }
    }

    private boolean handleFilterChipTouch(float x, float y) {
        layoutFilterChips();
        for (FilterChip chip : filterChips) {
            if (TouchUtil.checkIsInTouchRectFloat(chip.bounds, x, y)) {
                toggleChip(chip.id);
                return true;
            }
        }
        return false;
    }

    private void drawNpcList(Canvas canvas, Paint paint) {
        paint.setColor(Color.argb(230, 22, 28, 38));
        canvas.drawRect(npcListBounds, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(70, 95, 130));
        canvas.drawRect(npcListBounds, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(180, 200, 230));
        paint.setTextSize(LEGEND_TEXT_SIZE);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("常驻 NPC", npcListBounds.left + 10, npcListBounds.top + 26, paint);

        if (residentNpcs.isEmpty()) {
            paint.setColor(Color.rgb(150, 150, 150));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(LIST_NPC_NAME_TEXT_SIZE);
            canvas.drawText("本地图暂无常驻 NPC", npcListBounds.centerX(), npcListBounds.centerY() + 12, paint);
            return;
        }

        int clipTop = npcListBounds.top + LIST_HEADER_HEIGHT;
        Rect listClip = new Rect(npcListBounds.left, clipTop, npcListBounds.right, npcListBounds.bottom);
        canvas.save();
        canvas.clipRect(listClip);

        updateListItemBounds();

        for (int i = 0; i < residentNpcs.size(); i++) {
            if (i >= listItemBounds.size()) {
                break;
            }
            Rect item = listItemBounds.get(i);
            if (item.bottom < clipTop || item.top > npcListBounds.bottom) {
                continue;
            }
            Npc npc = residentNpcs.get(i);
            boolean selected = npc.getId() == selectedNpcId;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(selected ? Color.argb(120, 70, 100, 160) : Color.argb(70, 40, 48, 62));
            canvas.drawRoundRect(item.left, item.top, item.right, item.bottom, 8, 8, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(selected ? 3 : 1);
            paint.setColor(selected ? Color.rgb(255, 230, 100) : Color.argb(100, 100, 120, 150));
            canvas.drawRoundRect(item.left, item.top, item.right, item.bottom, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            int dotCx = item.left + 14;
            int dotCy = item.centerY();
            paint.setColor(npcMarkerColor(npc));
            canvas.drawCircle(dotCx, dotCy, 5, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(LIST_NPC_NAME_TEXT_SIZE);
            paint.setTextAlign(Paint.Align.LEFT);
            String name = truncateText(paint, npc.getName(), item.width() - 40);
            float textY = item.centerY() - (paint.descent() + paint.ascent()) / 2f;
            canvas.drawText(name, item.left + 26, textY, paint);
        }

        canvas.restore();

        if (maxListScroll > 0) {
            float barH = npcListBounds.height() - 36;
            float thumbH = Math.max(40, barH * npcListBounds.height() / (barH + maxListScroll));
            float thumbY = clipTop + (listScrollOffset / maxListScroll) * (barH - thumbH);
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawRect(npcListBounds.right - 8, thumbY, npcListBounds.right - 4, thumbY + thumbH, paint);
        }
    }

    private int npcMarkerColor(Npc npc) {
        if (npc.isNightOnly()) {
            return Color.rgb(160, 120, 255);
        }
        if (npc.hasQuest()) {
            return Color.rgb(255, 210, 60);
        }
        return Color.rgb(80, 200, 255);
    }

    private String truncateText(Paint paint, String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (paint.measureText(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String sub = text.substring(0, len) + ellipsis;
            if (paint.measureText(sub) <= maxWidth) {
                return sub;
            }
        }
        return ellipsis;
    }

    public boolean handleTouchDown(float x, float y) {
        if (!isVisible) {
            return false;
        }
        if (TouchUtil.checkIsInTouchRectFloat(closeButton, x, y)) {
            hide();
            return true;
        }
        if (hasMapTarget && handleActionBarTouch(x, y)) {
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(legendBounds, x, y) && handleFilterChipTouch(x, y)) {
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(npcListBounds, x, y)) {
            listDragActive = true;
            lastListDragY = y;
            listDragTotalDist = 0;
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(mapViewBounds, x, y)) {
            mapDragActive = true;
            lastDragX = x;
            lastDragY = y;
            dragTotalDist = 0;
            return true;
        }
        if (TouchUtil.checkIsInTouchRectFloat(panelBounds, x, y)) {
            return true;
        }
        return false;
    }

    public boolean handleTouchMove(float x, float y) {
        if (!isVisible) {
            return false;
        }
        if (listDragActive) {
            float dy = y - lastListDragY;
            listDragTotalDist += Math.abs(dy);
            listScrollOffset = Math.max(0, Math.min(listScrollOffset - dy, maxListScroll));
            lastListDragY = y;
            updateListItemBounds();
            return true;
        }
        if (mapDragActive) {
            float dx = x - lastDragX;
            float dy = y - lastDragY;
            dragTotalDist += Math.abs(dx) + Math.abs(dy);
            panWorldX -= dx / viewScale;
            panWorldY -= dy / viewScale;
            clampPan();
            lastDragX = x;
            lastDragY = y;
            return true;
        }
        return false;
    }

    public boolean handleTouchUp(float x, float y) {
        if (!isVisible) {
            return false;
        }
        boolean handled = false;
        if (listDragActive) {
            listDragActive = false;
            if (listDragTotalDist < 20f) {
                Npc listHit = findListNpcAt(x, y);
                if (listHit != null) {
                    long now = System.currentTimeMillis();
                    if (listHit.getId() == lastListTapNpcId
                            && (now - lastListTapTime) <= LIST_NPC_DOUBLE_TAP_MS) {
                        lastListTapNpcId = -1;
                        lastListTapTime = 0;
                        navigateToNpc(listHit);
                    } else {
                        focusOnNpc(listHit);
                        lastListTapNpcId = listHit.getId();
                        lastListTapTime = now;
                    }
                }
            }
            handled = true;
        }
        if (mapDragActive) {
            mapDragActive = false;
            if (dragTotalDist < 24f && TouchUtil.checkIsInTouchRectFloat(mapViewBounds, x, y)) {
                if (hasMapTarget && handleActionBarTouch(x, y)) {
                    handled = true;
                    return handled;
                }
                Npc hit = findNpcAt(x, y);
                if (hit != null) {
                    focusOnNpc(hit);
                    clearMapTarget();
                } else {
                    setMapTarget(x, y);
                    selectedNpcId = -1;
                }
            }
            handled = true;
        }
        return handled;
    }

    public boolean handleTouch(float x, float y) {
        return handleTouchDown(x, y);
    }

    private void navigateToNpc(Npc npc) {
        if (npc == null) {
            return;
        }
        clearMapTarget();
        if (GameEngine.getInstance().startWorldMapNavigationToNpc(npc)) {
            hide();
        }
    }

    private Npc findListNpcAt(float x, float y) {
        for (int i = 0; i < listItemBounds.size(); i++) {
            if (TouchUtil.checkIsInTouchRectFloat(listItemBounds.get(i), x, y)) {
                return residentNpcs.get(i);
            }
        }
        return null;
    }

    private Npc findNpcAt(float screenX, float screenY) {
        float hitPad = 22f;
        Npc best = null;
        float bestDist = Float.MAX_VALUE;
        float markerR = markerRadius();
        for (Npc npc : residentNpcs) {
            if (!shouldShowNpc(npc)) {
                continue;
            }
            float sx = worldToScreenX(npc.getX());
            float sy = worldToScreenY(npc.getY());
            float dx = screenX - sx;
            float dy = screenY - sy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist <= markerR + hitPad && dist < bestDist) {
                bestDist = dist;
                best = npc;
            }
        }
        return best;
    }
}
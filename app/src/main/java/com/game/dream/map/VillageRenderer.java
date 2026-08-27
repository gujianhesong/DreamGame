package com.game.dream.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillageRenderer {

    // 定义村庄内的物体类型
    public enum ObjectType { HOUSE, TREE, WELL, ROAD, HAY_BALE, STONE_TABLE }

    public static class VillageObject {
        public ObjectType type;
        public int x, y;
        public int width, height;
        public int color;

        public VillageObject(ObjectType type, int x, int y, int w, int h, int color) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.color = color;
        }
    }

    private List<VillageObject> objects = new ArrayList<>();
    private Rect villageBounds;

    /**
     * 初始化村庄布局
     */
    public void initVillage(int mapCenterX, int mapCenterY, int mapWidth, int mapHeight) {
        // 1. 定义村庄范围：地图中间的 1/4 区域
        int vW = mapWidth / 3;
        int vH = mapHeight / 3;
        int vX = mapCenterX - vW / 2;
        int vY = mapCenterY - vH / 2;
        villageBounds = new Rect(vX, vY, vX + vW, vY + vH);

        initVillageObjects(vX, vY, vW, vH);
    }

    /**
     * 使用自定义边界初始化村庄（金陵大地图使用）
     */
    public void initVillageWithBounds(int vx, int vy, int vw, int vh) {
        villageBounds = new Rect(vx, vy, vx + vw, vy + vh);
        initVillageObjects(vx, vy, vw, vh);
    }

    private void initVillageObjects(int vX, int vY, int vW, int vH) {

        Random rand = new Random(42); // 固定种子保证每次生成的村庄一样

        int hw = Math.min(400, vW / 5);
        int hh = Math.min(320, vH / 6);

        // 网格布局放置房屋：3x3 网格，随机跳过部分格子，保证不重叠且不靠边
        int margin = 60; // 距离村庄边缘的最小距离
        int cols = 3;
        int rows = 3;
        int cellW = (vW - margin * 2) / cols;
        int cellH = (vH - margin * 2) / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // 跳过中心格子（水井位置）
                if (row == 1 && col == 1) continue;
                // 随机跳过部分格子（约 70% 概率放房子）
                if (rand.nextFloat() < 0.3f) continue;

                // 格子左上角
                int cellX = vX + margin + col * cellW;
                int cellY = vY + margin + row * cellH;

                // 在格子内随机偏移（保证房子完全在格子内）
                int maxJitterX = Math.max(0, cellW - hw - 20);
                int maxJitterY = Math.max(0, cellH - hh - 20);
                int jx = rand.nextInt(maxJitterX + 1);
                int jy = rand.nextInt(maxJitterY + 1);

                objects.add(new VillageObject(ObjectType.HOUSE,
                        cellX + jx, cellY + jy,
                        hw, hh, Color.rgb(139, 69, 19)));
            }
        }

        // 生成树木和水井 (Decorations)
        for (int i = 0; i < 15; i++) {
            objects.add(new VillageObject(ObjectType.TREE,
                    vX + rand.nextInt(vW), vY + rand.nextInt(vH),
                    Math.min(100, vW / 15), Math.min(140, vH / 10), Color.GREEN));
        }

        // 中心水井
        int wellSize = Math.min(100, vW / 15);
        objects.add(new VillageObject(ObjectType.WELL,
                vX + vW / 2 - wellSize / 2, vY + vH / 2 - wellSize / 2,
                wellSize, wellSize, Color.GRAY));

        // 干草堆（放在网格间距处，不重叠房屋）
        int hayW = Math.min(80, vW / 20);
        int hayH = Math.min(50, vH / 30);
        objects.add(new VillageObject(ObjectType.HAY_BALE,
                vX + vW / 3 - 30, vY + vH / 3 - 20, hayW, hayH, Color.rgb(200, 170, 60)));
        objects.add(new VillageObject(ObjectType.HAY_BALE,
                vX + vW * 2 / 3 - 20, vY + vH * 2 / 3 + 10, hayW, hayH, Color.rgb(190, 160, 50)));
        objects.add(new VillageObject(ObjectType.HAY_BALE,
                vX + vW / 3 + 40, vY + vH * 2 / 3 - 30, hayW, hayH, Color.rgb(195, 165, 55)));

        // 石桌石凳（放在网格间距区域，远离水井）
        int tableW = Math.min(90, vW / 18);
        int tableH = Math.min(60, vH / 24);
        objects.add(new VillageObject(ObjectType.STONE_TABLE,
                vX + vW * 2 / 3 + 20, vY + vH / 3 + 30, tableW, tableH, Color.GRAY));
        objects.add(new VillageObject(ObjectType.STONE_TABLE,
                vX + vW / 3 - 60, vY + vH * 2 / 3 - 20, tableW, tableH, Color.GRAY));
    }

    /**
     * 绘制村庄
     */
    public void draw(Canvas canvas, float cameraX, float cameraY) {
        if (villageBounds == null) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 先绘制地面背景色（区分村庄区域）
        paint.setColor(Color.argb(250, 215, 162, 109)); // 土黄色土地
        canvas.drawRoundRect(new RectF(
                villageBounds.left - cameraX,
                villageBounds.top - cameraY,
                villageBounds.right - cameraX,
                villageBounds.bottom - cameraY),
                100,
                100,
                paint
        );

        // 按 Y 轴排序，实现遮挡关系（近处的物体盖住远处的）
        objects.sort((o1, o2) -> Integer.compare(o1.y, o2.y));

        for (VillageObject obj : objects) {
            // 视锥剔除：树木的 obj.y 是底部，向上延伸 obj.height
            // 其他物体的 obj.y 是顶部，向下延伸 obj.height
            float visTop, visBottom;
            if (obj.type == ObjectType.TREE) {
                visTop = obj.y - obj.height;   // 树顶在上方
                visBottom = obj.y;              // 树底在下方
            } else {
                visTop = obj.y;
                visBottom = obj.y + obj.height;
            }
            if (obj.x + obj.width < cameraX || obj.x > cameraX + canvas.getWidth() ||
                    visBottom < cameraY || visTop > cameraY + canvas.getHeight()) {
                continue;
            }

            float drawX = obj.x - cameraX;
            float drawY = obj.y - cameraY;

            switch (obj.type) {
                case HOUSE:
                    drawHouse(canvas, paint, drawX, drawY, obj.width, obj.height, obj.x, obj.y);
                    break;
                case TREE:
                    drawTree(canvas, paint, drawX, drawY, obj.width, obj.height, obj.x, obj.y);
                    break;
                case WELL:
                    drawWell(canvas, paint, drawX, drawY, obj.width, obj.height);
                    break;
                case ROAD:
                    paint.setColor(obj.color);
                    canvas.drawRect(drawX, drawY, drawX + obj.width, drawY + obj.height, paint);
                    break;
                case HAY_BALE:
                    drawHayBale(canvas, paint, drawX, drawY, obj.width, obj.height);
                    break;
                case STONE_TABLE:
                    drawStoneTable(canvas, paint, drawX, drawY, obj.width, obj.height);
                    break;
            }
        }
    }

    private void drawHouse(Canvas canvas, Paint paint, float x, float y, int w, int h, int worldX, int worldY) {
        Random hRand = new Random(worldX * 53 + worldY * 97);
        int wallStyle = hRand.nextInt(4);
        int roofStyle = hRand.nextInt(3);
        int windowStyle = hRand.nextInt(4);
        boolean hasChimney = hRand.nextBoolean();
        boolean hasFlowerPot = hRand.nextBoolean();
        boolean hasFence = hRand.nextBoolean();

        float wallTop = y + h / 3f;
        float cx = x + w / 2f;

        // --- 墙体 ---
        int[] wallColors = {
                Color.rgb(200, 180, 150), // 米黄
                Color.rgb(210, 195, 170), // 浅驼
                Color.rgb(185, 170, 145), // 土黄
                Color.rgb(195, 185, 175)  // 灰白
        };
        paint.setColor(wallColors[wallStyle]);
        canvas.drawRect(x, wallTop, x + w, y + h, paint);

        // 墙基（底部深色条）
        paint.setColor(Color.rgb(140, 125, 105));
        canvas.drawRect(x, y + h - 8, x + w, y + h, paint);

        // 墙面横线纹理
        paint.setColor(Color.argb(20, 0, 0, 0));
        paint.setStrokeWidth(1);
        for (int i = 1; i < 4; i++) {
            float ly = wallTop + (y + h - wallTop) * i / 4f;
            canvas.drawLine(x + 3, ly, x + w - 3, ly, paint);
        }

        // --- 屋顶 ---
        int[] roofColors = {
                Color.rgb(120, 50, 40),  // 红瓦
                Color.rgb(80, 65, 50),   // 灰瓦
                Color.rgb(100, 75, 45)   // 棕瓦
        };
        paint.setColor(roofColors[roofStyle]);
        android.graphics.Path roofPath = new android.graphics.Path();
        roofPath.moveTo(x - 12, wallTop + 2);
        roofPath.lineTo(cx, y);
        roofPath.lineTo(x + w + 12, wallTop + 2);
        roofPath.close();
        canvas.drawPath(roofPath, paint);
        // 屋檐
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawRect(x - 12, wallTop, x + w + 12, wallTop + 6, paint);

        // --- 烟囱 ---
        if (hasChimney) {
            float chimX = cx + w * 0.2f;
            float chimY = y + h * 0.05f;
            paint.setColor(Color.rgb(110, 90, 75));
            canvas.drawRect(chimX - 6, chimY, chimX + 6, wallTop, paint);
            paint.setColor(Color.rgb(90, 75, 60));
            canvas.drawRect(chimX - 8, chimY - 3, chimX + 8, chimY + 3, paint);
        }

        // --- 窗户 ---
        float winW = w * 0.14f;
        float winH = h * 0.12f;
        float winY = wallTop + (y + h - wallTop) * 0.25f;

        switch (windowStyle) {
            case 0: // 左右各一扇窗
                drawWindow(paint, canvas, x + w * 0.18f, winY, winW, winH);
                drawWindow(paint, canvas, x + w * 0.68f, winY, winW, winH);
                break;
            case 1: // 左双窗 + 右无
                drawWindow(paint, canvas, x + w * 0.15f, winY, winW, winH);
                drawWindow(paint, canvas, x + w * 0.32f, winY, winW, winH);
                break;
            case 2: // 左圆窗 + 右方窗
                paint.setColor(Color.rgb(180, 210, 230));
                canvas.drawCircle(x + w * 0.22f, winY + winH * 0.5f, winW * 0.6f, paint);
                paint.setColor(Color.rgb(80, 55, 35));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                canvas.drawCircle(x + w * 0.22f, winY + winH * 0.5f, winW * 0.6f, paint);
                canvas.drawLine(x + w * 0.22f - winW * 0.6f, winY + winH * 0.5f,
                        x + w * 0.22f + winW * 0.6f, winY + winH * 0.5f, paint);
                canvas.drawLine(x + w * 0.22f, winY + winH * 0.5f - winW * 0.6f,
                        x + w * 0.22f, winY + winH * 0.5f + winW * 0.6f, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                drawWindow(paint, canvas, x + w * 0.68f, winY, winW, winH);
                break;
            case 3: // 无窗户（门两侧挂装饰）
                break;
        }

        // --- 门 ---
        paint.setColor(Color.rgb(80, 40, 20));
        canvas.drawRect(cx - 50, y + h - 80, cx + 50, y + h, paint);
        // 门框
        paint.setColor(Color.rgb(60, 30, 15));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(cx - 50, y + h - 80, cx + 50, y + h, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        // 门环
        paint.setColor(Color.rgb(180, 160, 60));
        canvas.drawCircle(cx - 15, y + h - 40, 4, paint);
        canvas.drawCircle(cx + 15, y + h - 40, 4, paint);

        // --- 花盆 ---
        if (hasFlowerPot) {
            float potX = x + w * (hRand.nextBoolean() ? 0.12f : 0.82f);
            float potY = y + h - 22;
            // 花盆主体（梯形效果：上宽下窄）
            paint.setColor(Color.rgb(160, 80, 40));
            canvas.drawRect(potX - 10, potY, potX + 10, potY + 20, paint);
            // 盆口沿
            paint.setColor(Color.rgb(140, 65, 30));
            canvas.drawRect(potX - 12, potY, potX + 12, potY + 4, paint);
            // 花朵
            int flowerColor = hRand.nextInt(3) == 0 ? Color.rgb(255, 80, 80)
                    : (hRand.nextInt(2) == 0 ? Color.rgb(255, 200, 60) : Color.rgb(220, 100, 200));
            paint.setColor(flowerColor);
            canvas.drawCircle(potX, potY - 8, 9, paint);
            // 花心
            paint.setColor(Color.rgb(255, 230, 100));
            canvas.drawCircle(potX, potY - 8, 4, paint);
            // 茎
            paint.setColor(Color.rgb(40, 130, 40));
            canvas.drawRect(potX - 2, potY - 4, potX + 2, potY + 2, paint);
        }

        // --- 小栅栏 ---
        if (hasFence) {
            paint.setColor(Color.rgb(160, 130, 90));
            float fenceX = x + w + 8;
            float fenceY = y + h - 55;
            float fenceW = 50;
            float postSpacing = fenceW / 5f;
            // 竖向栅栏桩（6根）
            for (int i = 0; i <= 5; i++) {
                canvas.drawRect(fenceX + i * postSpacing, fenceY, fenceX + i * postSpacing + 4, y + h, paint);
            }
            // 横向栏杆（两道）
            canvas.drawRect(fenceX, fenceY + 10, fenceX + fenceW, fenceY + 14, paint);
            canvas.drawRect(fenceX, fenceY + 30, fenceX + fenceW, fenceY + 34, paint);
        }

        // --- 门口灯笼（60%概率，门两侧各一个）---
        if (hRand.nextFloat() < 0.6f) {
            float lw = 8;
            float lh = 12;
            // 左侧灯笼
            drawLantern(canvas, paint, cx - 58, y + h - 75, lw, lh);
            // 右侧灯笼
            drawLantern(canvas, paint, cx + 58, y + h - 75, lw, lh);
        }
    }

    /** 绘制一扇方窗 */
    private void drawWindow(Paint paint, Canvas canvas, float wx, float wy, float ww, float wh) {
        // 窗玻璃
        paint.setColor(Color.rgb(180, 210, 230));
        canvas.drawRect(wx, wy, wx + ww, wy + wh, paint);
        // 窗框
        paint.setColor(Color.rgb(80, 55, 35));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(wx, wy, wx + ww, wy + wh, paint);
        // 十字窗格
        canvas.drawLine(wx + ww / 2, wy, wx + ww / 2, wy + wh, paint);
        canvas.drawLine(wx, wy + wh / 2, wx + ww, wy + wh / 2, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    /** 绘制一个灯笼 */
    private void drawLantern(Canvas canvas, Paint paint, float lx, float ly, float lw, float lh) {
        // 挂绳
        paint.setColor(Color.rgb(60, 30, 15));
        paint.setStrokeWidth(2);
        canvas.drawLine(lx, ly - 10, lx, ly, paint);
        paint.setStrokeWidth(1);

        // 灯笼主体（红色椭圆）
        paint.setColor(Color.rgb(220, 40, 40));
        canvas.drawOval(lx - lw, ly, lx + lw, ly + lh * 2, paint);

        // 金色箍（上下两道）
        paint.setColor(Color.rgb(200, 170, 40));
        canvas.drawRect(lx - lw, ly, lx + lw, ly + 3, paint);
        canvas.drawRect(lx - lw, ly + lh * 2 - 3, lx + lw, ly + lh * 2, paint);

        // 灯笼高光
        paint.setColor(Color.argb(40, 255, 200, 150));
        canvas.drawOval(lx - lw * 0.5f, ly + 3, lx + lw * 0.3f, ly + lh, paint);

        // 底部小流苏
        paint.setColor(Color.rgb(200, 170, 40));
        canvas.drawRect(lx - 1, ly + lh * 2, lx + 1, ly + lh * 2 + 5, paint);
    }

    private void drawTree(Canvas canvas, Paint paint, float x, float y, int w, int h, int worldX, int worldY) {
        // 用世界坐标做种子，保证同一棵树样式固定（不受摄像机移动影响）
        Random treeRand = new Random(worldX * 73 + worldY * 137);
        int style = treeRand.nextInt(6);

        float cx = x + w / 2f;
        float trunkTop = y + h * 0.35f;
        float trunkW = w * 0.08f;

        // 风力摇摆：树冠随风偏移，树干不动
        long windT = System.currentTimeMillis();
        float windX = (float)(Math.sin(windT * 0.001 + worldX * 0.003) * Math.cos(windT * 0.0007 + worldY * 0.004));
        float windY = (float)(Math.cos(windT * 0.0012 + worldX * 0.004) * Math.sin(windT * 0.0008 + worldY * 0.003));
        float wxOff = windX * w * 0.08f;
        float wyOff = windY * w * 0.05f;

        switch (style) {
            case 0: // 樟树/阔叶树：树干不动 + 树冠摇摆
                paint.setColor(Color.rgb(90, 60, 30));
                canvas.drawRect(cx - trunkW * 1.3f, trunkTop, cx + trunkW * 1.3f, y + h, paint);
                float canopyR = w * 0.42f;
                paint.setColor(Color.rgb(30, 120, 30));
                canvas.drawCircle(cx + wxOff - canopyR * 0.4f, trunkTop + wyOff - canopyR * 0.2f, canopyR * 0.75f, paint);
                canvas.drawCircle(cx + wxOff + canopyR * 0.4f, trunkTop + wyOff - canopyR * 0.1f, canopyR * 0.7f, paint);
                paint.setColor(Color.rgb(40, 140, 40));
                canvas.drawCircle(cx + wxOff, trunkTop + wyOff - canopyR * 0.5f, canopyR * 0.8f, paint);
                paint.setColor(Color.argb(35, 120, 220, 80));
                canvas.drawCircle(cx + wxOff - canopyR * 0.2f, trunkTop + wyOff - canopyR * 0.7f, canopyR * 0.35f, paint);
                break;

            case 1: // 松树：树干不动 + 三层三角摇摆
                paint.setColor(Color.rgb(80, 55, 28));
                canvas.drawRect(cx - trunkW, trunkTop, cx + trunkW, y + h, paint);
                float pineW = w * 0.45f;
                float layerH = (trunkTop - y) * 0.4f;
                for (int i = 0; i < 3; i++) {
                    float sway = (i + 1) / 3f;
                    float lx = wxOff * sway, ly = wyOff * sway;
                    float ly2 = y + i * layerH * 0.7f;
                    float lw = pineW * (1f - i * 0.2f);
                    paint.setColor(i == 0 ? Color.rgb(25, 100, 25) : Color.rgb(35, 125, 35));
                    android.graphics.Path tri = new android.graphics.Path();
                    tri.moveTo(cx + lx - lw, ly2 + ly + layerH);
                    tri.lineTo(cx + lx, ly2 + ly);
                    tri.lineTo(cx + lx + lw, ly2 + ly + layerH);
                    tri.close();
                    canvas.drawPath(tri, paint);
                }
                break;

            case 2: // 柳树：树干不动 + 树冠枝条摇摆
                paint.setColor(Color.rgb(85, 65, 35));
                canvas.drawRect(cx - trunkW * 1.2f, trunkTop, cx + trunkW * 1.2f, y + h, paint);
                float willowR = w * 0.48f;
                paint.setColor(Color.rgb(50, 130, 45));
                canvas.drawOval(cx + wxOff - willowR, trunkTop + wyOff - willowR * 0.6f,
                        cx + wxOff + willowR, trunkTop + wyOff + willowR * 0.3f, paint);
                paint.setColor(Color.rgb(60, 140, 50));
                paint.setStrokeWidth(2);
                for (int i = 0; i < 5; i++) {
                    float bx = cx + (treeRand.nextFloat() - 0.5f) * willowR * 1.6f;
                    float by = trunkTop - willowR * 0.3f;
                    canvas.drawLine(bx + wxOff, by + wyOff,
                            bx + wxOff + (treeRand.nextFloat() - 0.5f) * 8, by + wyOff + willowR * 0.8f, paint);
                }
                paint.setStrokeWidth(1);
                break;

            case 3: // 灌木丛：树干不动 + 蓬松树冠摇摆
                paint.setColor(Color.rgb(95, 70, 38));
                canvas.drawRect(cx - trunkW * 1.5f, y + h * 0.5f, cx + trunkW * 1.5f, y + h, paint);
                float bushR = w * 0.35f;
                paint.setColor(Color.rgb(45, 130, 40));
                canvas.drawCircle(cx + wxOff - bushR * 0.5f, y + h * 0.35f + wyOff, bushR * 0.7f, paint);
                canvas.drawCircle(cx + wxOff + bushR * 0.5f, y + h * 0.35f + wyOff, bushR * 0.65f, paint);
                paint.setColor(Color.rgb(55, 150, 50));
                canvas.drawCircle(cx + wxOff, y + h * 0.25f + wyOff, bushR * 0.75f, paint);
                paint.setColor(Color.argb(30, 150, 230, 100));
                canvas.drawCircle(cx + wxOff - bushR * 0.15f, y + h * 0.18f + wyOff, bushR * 0.3f, paint);
                break;

            case 4: // 金秋树：树干不动 + 金黄色树冠摇摆
                paint.setColor(Color.rgb(90, 60, 30));
                canvas.drawRect(cx - trunkW * 1.3f, trunkTop, cx + trunkW * 1.3f, y + h, paint);
                float goldR = w * 0.42f;
                paint.setColor(Color.rgb(200, 170, 30));
                canvas.drawCircle(cx + wxOff - goldR * 0.4f, trunkTop + wyOff - goldR * 0.2f, goldR * 0.75f, paint);
                canvas.drawCircle(cx + wxOff + goldR * 0.4f, trunkTop + wyOff - goldR * 0.1f, goldR * 0.7f, paint);
                paint.setColor(Color.rgb(230, 195, 40));
                canvas.drawCircle(cx + wxOff, trunkTop + wyOff - goldR * 0.5f, goldR * 0.8f, paint);
                paint.setColor(Color.argb(45, 255, 240, 100));
                canvas.drawCircle(cx + wxOff - goldR * 0.2f, trunkTop + wyOff - goldR * 0.7f, goldR * 0.35f, paint);
                break;

            case 5: // 红枫树：树干不动 + 橙红色树冠摇摆
                paint.setColor(Color.rgb(85, 55, 28));
                canvas.drawRect(cx - trunkW * 1.3f, trunkTop, cx + trunkW * 1.3f, y + h, paint);
                float redR = w * 0.42f;
                paint.setColor(Color.rgb(190, 70, 25));
                canvas.drawCircle(cx + wxOff - redR * 0.4f, trunkTop + wyOff - redR * 0.2f, redR * 0.75f, paint);
                canvas.drawCircle(cx + wxOff + redR * 0.4f, trunkTop + wyOff - redR * 0.1f, redR * 0.7f, paint);
                paint.setColor(Color.rgb(220, 90, 35));
                canvas.drawCircle(cx + wxOff, trunkTop + wyOff - redR * 0.5f, redR * 0.8f, paint);
                paint.setColor(Color.argb(40, 255, 160, 80));
                canvas.drawCircle(cx + wxOff - redR * 0.2f, trunkTop + wyOff - redR * 0.7f, redR * 0.35f, paint);
                break;
        }
    }

    private void drawWell(Canvas canvas, Paint paint, float x, float y, int w, int h) {
        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float rx = w * 0.45f;
        float ry = h * 0.4f;

        // 地面阴影
        paint.setColor(Color.argb(30, 0, 0, 0));
        canvas.drawOval(x - 5, y + h * 0.2f, x + w + 5, y + h + 5, paint);

        // 石砌井台（外壁）
        paint.setColor(Color.rgb(140, 135, 125));
        canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, paint);

        // 井台顶面（稍亮）
        paint.setColor(Color.rgb(165, 160, 150));
        canvas.drawOval(cx - rx * 0.85f, cy - ry * 0.85f, cx + rx * 0.85f, cy + ry * 0.85f, paint);

        // 井口（深色空洞）
        paint.setColor(Color.rgb(25, 30, 40));
        canvas.drawOval(cx - rx * 0.55f, cy - ry * 0.55f, cx + rx * 0.55f, cy + ry * 0.55f, paint);

        // 水面反光
        paint.setColor(Color.argb(60, 80, 140, 200));
        canvas.drawOval(cx - rx * 0.3f, cy - ry * 0.15f, cx + rx * 0.1f, cy + ry * 0.15f, paint);

        // 左支柱
        float postTop = cy - ry - h * 0.35f;
        paint.setColor(Color.rgb(90, 60, 35));
        canvas.drawRect(cx - rx * 0.65f - 3, postTop, cx - rx * 0.65f + 3, cy - ry * 0.3f, paint);
        // 右支柱
        canvas.drawRect(cx + rx * 0.65f - 3, postTop, cx + rx * 0.65f + 3, cy - ry * 0.3f, paint);

        // 横梁
        paint.setColor(Color.rgb(70, 45, 25));
        canvas.drawRect(cx - rx * 0.75f, postTop - 4, cx + rx * 0.75f, postTop + 4, paint);

        // 小顶棚（三角）
        paint.setColor(Color.rgb(110, 55, 35));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(cx - rx * 0.9f, postTop);
        roof.lineTo(cx, postTop - h * 0.2f);
        roof.lineTo(cx + rx * 0.9f, postTop);
        roof.close();
        canvas.drawPath(roof, paint);

        // 绳子 + 水桶
        paint.setColor(Color.rgb(160, 140, 100));
        paint.setStrokeWidth(2);
        canvas.drawLine(cx, postTop + 4, cx, cy - ry * 0.2f, paint);
        paint.setStrokeWidth(1);
        // 小水桶
        paint.setColor(Color.rgb(110, 80, 50));
        canvas.drawRect(cx - 5, cy - ry * 0.2f, cx + 5, cy - ry * 0.2f + 8, paint);
    }

    public Rect getVillageBounds(){
        return villageBounds;
    }

    private void drawHayBale(Canvas canvas, Paint paint, float x, float y, int w, int h) {
        float cx = x + w / 2f;
        float cy = y + h / 2f;

        // 阴影
        paint.setColor(Color.argb(25, 0, 0, 0));
        canvas.drawOval(x - 2, y + h * 0.3f, x + w + 2, y + h + 3, paint);

        // 草堆主体（金色椭圆）
        paint.setColor(Color.rgb(200, 170, 60));
        canvas.drawOval(x, y, x + w, y + h, paint);

        // 亮面
        paint.setColor(Color.rgb(220, 195, 90));
        canvas.drawOval(x + w * 0.15f, y + h * 0.1f, x + w * 0.7f, y + h * 0.6f, paint);

        // 稻草纹理线
        paint.setColor(Color.rgb(160, 130, 40));
        paint.setStrokeWidth(1);
        canvas.drawLine(x + w * 0.2f, y + h * 0.3f, x + w * 0.8f, y + h * 0.3f, paint);
        canvas.drawLine(x + w * 0.15f, y + h * 0.55f, x + w * 0.85f, y + h * 0.55f, paint);
        canvas.drawLine(x + w * 0.3f, y + h * 0.75f, x + w * 0.7f, y + h * 0.75f, paint);

        // 顶部散落的稻草（几根斜线）
        paint.setColor(Color.rgb(180, 150, 50));
        paint.setStrokeWidth(2);
        canvas.drawLine(cx - 5, y + 2, cx - 12, y - 5, paint);
        canvas.drawLine(cx + 3, y + 1, cx + 10, y - 4, paint);
        canvas.drawLine(cx + 8, y + 3, cx + 16, y - 2, paint);
        paint.setStrokeWidth(1);
    }

    private void drawStoneTable(Canvas canvas, Paint paint, float x, float y, int w, int h) {
        float cx = x + w / 2f;

        // 石凳1（左侧）
        float benchW = w * 0.35f;
        float benchH = h * 0.5f;
        float bench1X = x - benchW * 0.6f;
        float bench1Y = y + h * 0.4f;
        // 凳腿
        paint.setColor(Color.rgb(120, 115, 105));
        canvas.drawRect(bench1X + 2, bench1Y + benchH * 0.4f, bench1X + 6, bench1Y + benchH, paint);
        canvas.drawRect(bench1X + benchW - 6, bench1Y + benchH * 0.4f, bench1X + benchW - 2, bench1Y + benchH, paint);
        // 凳面
        paint.setColor(Color.rgb(155, 150, 140));
        canvas.drawOval(bench1X, bench1Y, bench1X + benchW, bench1Y + benchH * 0.55f, paint);

        // 石凳2（右侧）
        float bench2X = x + w + benchW * 0.1f;
        float bench2Y = y + h * 0.35f;
        paint.setColor(Color.rgb(120, 115, 105));
        canvas.drawRect(bench2X + 2, bench2Y + benchH * 0.4f, bench2X + 6, bench2Y + benchH, paint);
        canvas.drawRect(bench2X + benchW - 6, bench2Y + benchH * 0.4f, bench2X + benchW - 2, bench2Y + benchH, paint);
        paint.setColor(Color.rgb(155, 150, 140));
        canvas.drawOval(bench2X, bench2Y, bench2X + benchW, bench2Y + benchH * 0.55f, paint);

        // 桌子阴影
        paint.setColor(Color.argb(25, 0, 0, 0));
        canvas.drawOval(x - 3, y + h * 0.2f, x + w + 3, y + h + 4, paint);

        // 桌腿
        paint.setColor(Color.rgb(110, 105, 95));
        canvas.drawRect(x + w * 0.15f, y + h * 0.35f, x + w * 0.15f + 6, y + h, paint);
        canvas.drawRect(x + w * 0.75f, y + h * 0.35f, x + w * 0.75f + 6, y + h, paint);

        // 桌面（石板）
        paint.setColor(Color.rgb(150, 145, 135));
        canvas.drawOval(x, y, x + w, y + h * 0.5f, paint);
        // 桌面高光
        paint.setColor(Color.argb(30, 255, 255, 255));
        canvas.drawOval(x + w * 0.2f, y + h * 0.05f, x + w * 0.6f, y + h * 0.35f, paint);
    }

    /**
     * 获取村庄内所有不可通行的矩形区域（用于碰撞检测）
     */
    public List<Rect> getObstacles() {
        List<Rect> obstacles = new ArrayList<>();
        for (VillageObject obj : objects) {
            if (obj.type == ObjectType.HOUSE || obj.type == ObjectType.WELL) {
                // 稍微缩小一点碰撞箱，让玩家能贴着房子走
                obstacles.add(new Rect(obj.x + 5, obj.y + 5, obj.x + obj.width - 5, obj.y + obj.height - 5));
            }
        }
        return obstacles;
    }
}

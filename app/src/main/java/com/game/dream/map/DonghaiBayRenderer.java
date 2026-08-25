package com.game.dream.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 东海湾地图渲染器
 * 负责绘制: 左侧村庄（复用 VillageRenderer）、中间围墙、右侧沙滩与海面装饰
 */
public class DonghaiBayRenderer {

    private VillageRenderer villageRenderer;
    private int[] gateCenterYs; // 3 道门的中心 y 坐标
    private int gateHeight;

    // 树木装饰列表
    private List<TreeDeco> trees = new ArrayList<>();

    // 海浪动画基准时间（避免 float 精度丢失）
    private long waveBaseTime = 0;

    private static class TreeDeco {
        float x, y;   // 世界坐标（树底部中心）
        float size;    // 树尺寸
        int type;      // 0=椰子树, 1=阔叶树, 2=松树, 3=灌木丛

        TreeDeco(float x, float y, float size, int type) {
            this.x = x; this.y = y; this.size = size; this.type = type;
        }
    }

    // 围墙世界坐标
    private static final int WALL_X = 6000;
    private static final int WALL_WIDTH = 100;
    private static final int MAP_HEIGHT = 10000;

    /**
     * 初始化渲染器
     */
    public void init(int[] villageBounds, int[] gateCenterYs, int gateHeight) {
        this.gateCenterYs = gateCenterYs;
        this.gateHeight = gateHeight;

        // 初始化左侧村庄渲染器
        villageRenderer = new VillageRenderer();
        villageRenderer.initVillageWithBounds(
                villageBounds[0], villageBounds[1],
                villageBounds[2] - villageBounds[0],
                villageBounds[3] - villageBounds[1]
        );

        // 生成树木装饰
        generateTrees();
    }

    /**
     * 绘制东海湾所有附加元素
     */
    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        // 1. 绘制左侧村庄
        if (villageRenderer != null) {
            villageRenderer.draw(canvas, cameraX, cameraY);
        }

        // 2. 绘制围墙
        drawWall(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 3. 绘制树木
        drawTrees(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 4. 绘制沙滩与海面装饰
        drawBeachDeco(canvas, cameraX, cameraY, screenWidth, screenHeight);
    }

    // ==================== 围墙绘制 ====================

    private void drawWall(Canvas canvas, float cameraX, float cameraY, int sw, int sh) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float wallLeft = WALL_X - cameraX;
        float wallRight = WALL_X + WALL_WIDTH - cameraX;

        // 视锥剔除
        if (wallRight < 0 || wallLeft > sw) return;

        // 绘制围墙主体：在门之间分段绘制
        float prevBottom = -cameraY; // 上一段结束位置
        for (int centerY : gateCenterYs) {
            float gateTop = centerY - gateHeight / 2f - cameraY;
            float gateBottom = centerY + gateHeight / 2f - cameraY;
            // 门上方的墙段
            if (gateTop > prevBottom) {
                drawWallSegment(canvas, paint, wallLeft, Math.max(0, prevBottom), wallRight, Math.min(sh, gateTop));
            }
            prevBottom = gateBottom;
        }
        // 最后一道门到地图底部的墙段
        float mapBottom = MAP_HEIGHT - cameraY;
        if (mapBottom > prevBottom) {
            drawWallSegment(canvas, paint, wallLeft, Math.max(0, prevBottom), wallRight, Math.min(sh, mapBottom));
        }

        // 绘制每道门的门框装饰
        for (int centerY : gateCenterYs) {
            drawGateFrame(canvas, paint, centerY, cameraX, cameraY);
        }
    }

    private void drawWallSegment(Canvas canvas, Paint paint, float left, float top, float right, float bottom) {
        float wallW = right - left;

        // 墙体主体（灰色石墙）
        paint.setColor(Color.rgb(120, 115, 105));
        canvas.drawRect(left, top, right, bottom, paint);

        // 左侧暗边
        paint.setColor(Color.rgb(90, 85, 78));
        canvas.drawRect(left, top, left + 4, bottom, paint);

        // 右侧亮边
        paint.setColor(Color.rgb(145, 140, 130));
        canvas.drawRect(right - 4, top, right, bottom, paint);

        // 横向砖缝纹理（每隔 40px）
        paint.setColor(Color.argb(30, 0, 0, 0));
        paint.setStrokeWidth(1);
        float brickH = 40;
        float startRow = ((int) (top / brickH)) * brickH;
        for (float y = startRow; y < bottom; y += brickH) {
            if (y >= top) {
                canvas.drawLine(left + 4, y, right - 4, y, paint);
            }
        }

        // 城垛（顶部锯齿）
        if (top <= 0) { // 只在地图顶部画城垛
            float merlonW = wallW * 0.5f;
            float merlonH = 12;
            paint.setColor(Color.rgb(100, 95, 88));
            for (float mx = left; mx < right; mx += merlonW * 2) {
                canvas.drawRect(mx, top, mx + merlonW, top + merlonH, paint);
            }
        }
    }

    private void drawGateFrame(Canvas canvas, Paint paint, int centerY, float cameraX, float cameraY) {
        float gateTop = centerY - gateHeight / 2f - cameraY;
        float gateBottom = centerY + gateHeight / 2f - cameraY;
        float gateLeft = WALL_X - cameraX;
        float gateRight = WALL_X + WALL_WIDTH - cameraX;

        // 视锥剔除
        if (gateBottom < 0 || gateTop > canvas.getHeight()) return;
        if (gateRight < 0 || gateLeft > canvas.getWidth()) return;

        // 门框（深色木框）
        paint.setColor(Color.rgb(80, 50, 25));
        paint.setStrokeWidth(6);
        canvas.drawLine(gateLeft, gateTop, gateLeft, gateBottom, paint);
        canvas.drawLine(gateRight, gateTop, gateRight, gateBottom, paint);
        canvas.drawLine(gateLeft, gateTop, gateRight, gateTop, paint);

        // 门拱（半圆装饰）
        float cx = (gateLeft + gateRight) / 2f;
        paint.setColor(Color.rgb(100, 65, 30));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawArc(new RectF(gateLeft, gateTop - 20, gateRight, gateTop + 40), 180, 180, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 门两侧小灯笼
        float lanternY = gateTop + 20;
        drawSmallLantern(canvas, paint, gateLeft - 12, lanternY);
        drawSmallLantern(canvas, paint, gateRight + 12, lanternY);
    }

    private void drawSmallLantern(Canvas canvas, Paint paint, float cx, float topY) {
        // 挂绳
        paint.setColor(Color.rgb(60, 30, 15));
        paint.setStrokeWidth(1);
        canvas.drawLine(cx, topY - 8, cx, topY, paint);
        // 灯笼主体
        paint.setColor(Color.rgb(220, 40, 40));
        canvas.drawOval(cx - 5, topY, cx + 5, topY + 14, paint);
        // 金色箍
        paint.setColor(Color.rgb(200, 170, 40));
        canvas.drawRect(cx - 5, topY, cx + 5, topY + 2, paint);
        canvas.drawRect(cx - 5, topY + 12, cx + 5, topY + 14, paint);
    }

    // ==================== 树木系统 ====================

    private void generateTrees() {
        trees.clear();
        Random rng = new Random(42);

        // --- 沙滩区域：椰子树 ---
        // 沙滩范围: x=6100~8100, y=0~10000
        int beachX1 = WALL_X + WALL_WIDTH; // 6100
        int beachX2 = beachX1 + 2000;       // 8100
        // 网格间距 600x600，随机偏移
        for (int gy = 300; gy < MAP_HEIGHT; gy += 600) {
            for (int gx = beachX1 + 150; gx < beachX2 - 50; gx += 600) {
                float tx = gx + rng.nextInt(250) - 125;
                float ty = gy + rng.nextInt(250) - 125;
                float size = 90 + rng.nextFloat() * 40; // 90~130
                trees.add(new TreeDeco(tx, ty, size, 0)); // 椰子树
            }
        }

        // --- 草地区域：阔叶树/松树/灌木 ---
        // 草地范围: x=0~6000, 排除村庄(1500,3500,4500,6500)
        int vX1 = 1500, vY1 = 3500, vX2 = 4500, vY2 = 6500;
        for (int gy = 200; gy < MAP_HEIGHT; gy += 500) {
            for (int gx = 200; gx < WALL_X - 100; gx += 500) {
                // 跳过村庄区域（加 150px 边距）
                if (gx > vX1 - 150 && gx < vX2 + 150 && gy > vY1 - 150 && gy < vY2 + 150) continue;
                float tx = gx + rng.nextInt(200) - 100;
                float ty = gy + rng.nextInt(200) - 100;
                float size = 80 + rng.nextFloat() * 50; // 80~130
                int type = 1 + rng.nextInt(3); // 1=阔叶树, 2=松树, 3=灌木
                trees.add(new TreeDeco(tx, ty, size, type));
            }
        }
    }

    private void drawTrees(Canvas canvas, float cameraX, float cameraY, int sw, int sh) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (TreeDeco t : trees) {
            float sx = t.x - cameraX;
            float sy = t.y - cameraY;
            // 视锥剔除
            // 树从 sy 向上生长，总高约 size*1.6，棕榈叶展开约 size*0.3
            // 树顶屏幕位置 = sy - size*1.9
            // 可见条件：树顶 < sh 且 树底 > 0，即 sy < sh + size*1.9 且 sy > 0
            // 加余量保证平滑过渡
            if (sx < -t.size * 2 || sx > sw + t.size * 2) continue;
            if (sy < -t.size * 2 || sy > sh + t.size * 3) continue;

            switch (t.type) {
                case 0: drawCoconutTree(canvas, paint, sx, sy, t.size); break;
                case 1: drawBroadleafTree(canvas, paint, sx, sy, t.size); break;
                case 2: drawPineTree(canvas, paint, sx, sy, t.size); break;
                case 3: drawBush(canvas, paint, sx, sy, t.size); break;
            }
        }
    }

    /** 椰子树：弯曲锥形树干 + 顶部放射状棕榈叶 + 椰子 */
    private void drawCoconutTree(Canvas canvas, Paint paint, float cx, float bottom, float size) {
        float h = size * 1.6f;
        float topY = bottom - h;
        float bend = size * 0.2f; // 弯曲偏移量
        float baseW = size * 0.12f; // 底部宽度
        float topW = size * 0.06f;  // 顶部宽度

        // 树干：单条平滑曲线填充路径（左边缘 + 右边缘闭合）
        float topCx = cx + bend; // 顶部中心 x
        paint.setColor(Color.rgb(130, 95, 55));
        android.graphics.Path trunk = new android.graphics.Path();
        // 左边缘：从底部左侧到顶部左侧
        trunk.moveTo(cx - baseW, bottom);
        trunk.quadTo(cx + bend * 0.4f - baseW * 0.7f, bottom - h * 0.5f,
                     topCx - topW, topY);
        // 右边缘：从顶部右侧回到底部右侧
        trunk.lineTo(topCx + topW, topY);
        trunk.quadTo(cx + bend * 0.4f + baseW * 0.7f, bottom - h * 0.5f,
                     cx + baseW, bottom);
        trunk.close();
        canvas.drawPath(trunk, paint);

        // 树干横纹（沿曲线分布）
        paint.setColor(Color.argb(35, 0, 0, 0));
        paint.setStrokeWidth(1);
        for (float t = 0.1f; t < 0.95f; t += 0.08f) {
            // 二次贝塞尔插值：P = (1-t)²P0 + 2(1-t)tP1 + t²P2
            float py = bottom + 2 * (1 - t) * t * (bottom - h * 0.5f) + t * t * topY - (1 - t) * (1 - t) * bottom;
            float px = cx + 2 * (1 - t) * t * (cx + bend * 0.4f) + t * t * topCx - (1 - t) * (1 - t) * cx;
            float w = baseW + (topW - baseW) * t; // 当前宽度插值
            canvas.drawLine(px - w, py, px + w, py, paint);
        }

        // 棕榈叶（放射状）
        float leafLen = size * 0.55f;
        paint.setStrokeWidth(3);
        int leafCount = 7;
        for (int i = 0; i < leafCount; i++) {
            double angle = -Math.PI + Math.PI * i / (leafCount - 1);
            float endX = topCx + (float) Math.cos(angle) * leafLen;
            float endY = topY + (float) Math.sin(angle) * leafLen * 0.5f + leafLen * 0.3f;
            float midX = topCx + (float) Math.cos(angle) * leafLen * 0.5f;
            float midY = topY + (float) Math.sin(angle) * leafLen * 0.25f - leafLen * 0.1f;

            paint.setColor(i % 2 == 0 ? Color.rgb(30, 130, 40) : Color.rgb(50, 150, 50));
            android.graphics.Path leaf = new android.graphics.Path();
            leaf.moveTo(topCx, topY);
            leaf.quadTo(midX, midY, endX, endY);
            canvas.drawPath(leaf, paint);
        }
        paint.setStrokeWidth(1);

        // 椰子（3个小圆）
        paint.setColor(Color.rgb(120, 80, 30));
        canvas.drawCircle(topCx - 6, topY + 10, 5, paint);
        canvas.drawCircle(topCx + 6, topY + 12, 5, paint);
        canvas.drawCircle(topCx, topY + 17, 4.5f, paint);
    }

    /** 阔叶树：粗壮树干 + 圆形树冠 */
    private void drawBroadleafTree(Canvas canvas, Paint paint, float cx, float bottom, float size) {
        float h = size * 1.4f;
        float trunkW = size * 0.08f;
        float topY = bottom - h;
        float trunkTop = bottom - h * 0.45f;

        // 树干
        paint.setColor(Color.rgb(90, 60, 30));
        canvas.drawRect(cx - trunkW * 1.3f, trunkTop, cx + trunkW * 1.3f, bottom, paint);

        // 树冠（多层圆）
        float canopyR = size * 0.42f;
        paint.setColor(Color.rgb(30, 120, 30));
        canvas.drawCircle(cx - canopyR * 0.4f, trunkTop - canopyR * 0.2f, canopyR * 0.75f, paint);
        canvas.drawCircle(cx + canopyR * 0.4f, trunkTop - canopyR * 0.1f, canopyR * 0.7f, paint);
        paint.setColor(Color.rgb(40, 140, 40));
        canvas.drawCircle(cx, trunkTop - canopyR * 0.5f, canopyR * 0.8f, paint);
        // 高光
        paint.setColor(Color.argb(35, 120, 220, 80));
        canvas.drawCircle(cx - canopyR * 0.2f, trunkTop - canopyR * 0.7f, canopyR * 0.35f, paint);
    }

    /** 松树：树干 + 三层三角 */
    private void drawPineTree(Canvas canvas, Paint paint, float cx, float bottom, float size) {
        float h = size * 1.5f;
        float trunkW = size * 0.06f;
        float topY = bottom - h;
        float trunkTop = bottom - h * 0.35f;

        // 树干
        paint.setColor(Color.rgb(80, 55, 28));
        canvas.drawRect(cx - trunkW, trunkTop, cx + trunkW, bottom, paint);

        // 三层三角树冠
        float pineW = size * 0.45f;
        float layerH = (trunkTop - topY) * 0.4f;
        for (int i = 0; i < 3; i++) {
            float ly = topY + i * layerH * 0.7f;
            float lw = pineW * (1f - i * 0.2f);
            paint.setColor(i == 0 ? Color.rgb(25, 100, 25) : Color.rgb(35, 125, 35));
            android.graphics.Path tri = new android.graphics.Path();
            tri.moveTo(cx - lw, ly + layerH);
            tri.lineTo(cx, ly);
            tri.lineTo(cx + lw, ly + layerH);
            tri.close();
            canvas.drawPath(tri, paint);
        }
    }

    /** 灌木丛：低矮圆形 */
    private void drawBush(Canvas canvas, Paint paint, float cx, float bottom, float size) {
        float bushR = size * 0.35f;
        float bushH = size * 0.5f;
        // 主丛
        paint.setColor(Color.rgb(45, 130, 45));
        canvas.drawOval(cx - bushR, bottom - bushH, cx + bushR, bottom, paint);
        // 侧丛
        paint.setColor(Color.rgb(55, 145, 55));
        canvas.drawCircle(cx - bushR * 0.6f, bottom - bushH * 0.5f, bushR * 0.6f, paint);
        canvas.drawCircle(cx + bushR * 0.5f, bottom - bushH * 0.4f, bushR * 0.55f, paint);
        // 高光点
        paint.setColor(Color.argb(40, 150, 230, 100));
        canvas.drawCircle(cx - bushR * 0.2f, bottom - bushH * 0.7f, bushR * 0.25f, paint);
    }

    // ==================== 沙滩与海面装饰 ====================

    private void drawBeachDeco(Canvas canvas, float cameraX, float cameraY, int sw, int sh) {
        // 只在右侧沙滩/海面区域绘制额外装饰
        float beachLeft = (WALL_X + WALL_WIDTH) - cameraX;
        if (beachLeft > sw) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 海岸线世界坐标 x = 8100（沙滩右边缘 / 海左边缘）
        int rightStart = WALL_X + WALL_WIDTH;          // 6100
        float coastWorldX = rightStart + 2000f;         // 8100
        float coastScreenX = coastWorldX - cameraX;

        float waveStartY = Math.max(-20, -cameraY);
        float waveEndY = Math.min(sh + 20, DonghaiBayMapGenerator.MAP_HEIGHT - cameraY);
        if (waveStartY >= waveEndY) return;

        // 时间驱动的海浪涨退动画
        // 关键：先减去基准时间，再转 float，避免大数值 float 精度丢失导致动画冻结
        long time = System.currentTimeMillis();
        if (waveBaseTime == 0) waveBaseTime = time;
        float t = (time - waveBaseTime) / 1000f; // 秒（从0开始）

        // === 1. 填充式海浪主体（从海面延伸上沙滩，涨退可见） ===
        // 主波浪：3 层不同相位的叠加，产生自然的涨退感
        // 每层波浪用填充 Path 绘制，左边缘是波浪前进线
        int layerCount = 3;
        float[][] layerX = new float[layerCount][];
        int segCount = (int) ((waveEndY - waveStartY) / 10) + 1;

        for (int layer = 0; layer < layerCount; layer++) {
            layerX[layer] = new float[segCount];
            float speed = 1.2f + layer * 0.3f;
            float phase = layer * 2.1f;
            // 该层基础推进距离（越外层浪越大）
            float baseAdvance = 120 + layer * 60; // 120, 180, 240 px
            float advance = (float) (Math.sin(t * speed + phase) * baseAdvance * 0.5 + baseAdvance * 0.5);

            for (int s = 0; s < segCount; s++) {
                float worldY = waveStartY + s * 10 + cameraY;
                // 沿 y 方向的波浪起伏
                float yWave = (float) Math.sin(worldY * 0.012 + t * 0.6 + layer * 0.8) * 25;
                // 小波纹叠加
                float yDetail = (float) Math.sin(worldY * 0.04 + t * 1.5 + layer) * 8;
                layerX[layer][s] = coastScreenX - advance + yWave + yDetail;
            }
        }

        // 从最外层开始绘制填充波浪（右侧渐变融入海面）
        for (int layer = layerCount - 1; layer >= 0; layer--) {
            int alpha = 70 + layer * 40; // 70, 110, 150
            int r = 80 + layer * 30;     // 80, 110, 140
            int g = 170 + layer * 20;    // 170, 190, 210
            int b = 230 + layer * 8;     // 230, 238, 246

            android.graphics.Path waterPath = new android.graphics.Path();
            // 左边缘：波浪前进线（从上到下）
            waterPath.moveTo(layerX[layer][0], waveStartY);
            for (int s = 1; s < segCount; s++) {
                float py = waveStartY + s * 10;
                waterPath.lineTo(layerX[layer][s], py);
            }
            // 右边缘：延伸到屏幕右侧远处，通过渐变融入海面
            float rightEdge = sw + 50;
            waterPath.lineTo(rightEdge, waveStartY + (segCount - 1) * 10);
            waterPath.lineTo(rightEdge, waveStartY);
            waterPath.close();

            // 渐变：从海岸线处全色 → 向海面方向逐渐消失
            float gradStart = coastScreenX + 20;
            float gradEnd = coastScreenX + 400 + layer * 150;
            paint.setShader(new LinearGradient(
                    gradStart, 0, gradEnd, 0,
                    Color.argb(alpha, r, g, b),
                    Color.argb(0, r, g, b),
                    Shader.TileMode.CLAMP));
            canvas.drawPath(waterPath, paint);
            paint.setShader(null);
        }

        // === 2. 浪花泡沫带（最内层波浪前缘的白色泡沫） ===
        float[] foamX = layerX[0]; // 最内层波浪位置
        paint.setStrokeWidth(4);
        for (int s = 0; s < segCount; s += 1) {
            float py = waveStartY + s * 10;
            float fx = foamX[s];
            if (fx < -30 || fx > sw + 30) continue;

            // 泡沫宽度随波浪位置变化
            float foamWidth = 6 + (float) Math.sin(t * 3 + s * 0.7) * 3;
            // 泡沫透明度：在波峰时更亮
            int foamAlpha = 140 + (int) (Math.sin(t * 2.5 + s * 0.5) * 60);
            foamAlpha = Math.max(60, Math.min(220, foamAlpha));
            paint.setColor(Color.argb(foamAlpha, 255, 255, 255));
            // 绘制泡沫弧线
            if (s + 1 < segCount) {
                float nextFx = foamX[s + 1];
                float nextPy = waveStartY + (s + 1) * 10;
                canvas.drawLine(fx, py, nextFx, nextPy, paint);
            }
            // 随机泡沫点
            if (s % 3 == 0) {
                float dotOff = (float) Math.sin(t * 4 + s * 1.3) * 12;
                float dotR = 2.5f + (float) Math.sin(t * 2 + s) * 1.5f;
                paint.setColor(Color.argb(foamAlpha - 30, 255, 255, 255));
                canvas.drawCircle(fx - 8 + dotOff, py + 3, dotR, paint);
            }
        }
        paint.setStrokeWidth(1);

        // === 3. 湿沙区域（波浪退去后沙滩上的水渍反光） ===
        // 湿沙范围：从最内层波浪最远到达位置到海岸线
        float maxReach = 0;
        for (int s = 0; s < segCount; s++) {
            maxReach = Math.max(maxReach, layerX[0][s]);
        }
        // 不绘制湿沙——已被填充波浪覆盖

        // === 4. 海面波光粼粼效果（海区域内的闪烁光点） ===
        float seaLeft = coastScreenX;
        if (seaLeft < sw) {
            float sparkleSegH = 40;
            for (float wy = waveStartY; wy < waveEndY; wy += sparkleSegH) {
                float worldY = wy + cameraY;
                // 多个不同频率的闪烁
                float sparkle1 = (float) Math.sin(t * 3.7 + worldY * 0.03);
                float sparkle2 = (float) Math.sin(t * 2.1 + worldY * 0.05 + 1.5);
                float sparkle = Math.max(0, sparkle1 * sparkle2);
                if (sparkle > 0.3f) {
                    float sx = seaLeft + 100 + (float) Math.sin(worldY * 0.01 + t * 0.3) * 200;
                    if (sx > 0 && sx < sw) {
                        int sparkAlpha = (int) (sparkle * 180);
                        paint.setColor(Color.argb(sparkAlpha, 255, 255, 240));
                        canvas.drawCircle(sx, wy + 15, 2 + sparkle * 3, paint);
                    }
                }
            }
        }
    }

    // ==================== 碰撞与安全区 ====================

    public Rect getVillageBounds() {
        return villageRenderer != null ? villageRenderer.getVillageBounds() : null;
    }

    public List<Rect> getObstacles() {
        List<Rect> obstacles = new ArrayList<>();
        if (villageRenderer != null) {
            obstacles.addAll(villageRenderer.getObstacles());
        }
        return obstacles;
    }
}

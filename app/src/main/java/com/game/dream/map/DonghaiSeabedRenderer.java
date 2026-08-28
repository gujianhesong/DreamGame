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
 * 东海海底地图渲染器
 * 负责绘制: 龙宫建筑群、海底大型装饰（珊瑚/海藻）、水下视觉效果（滤镜/焦散/气泡）
 */
public class DonghaiSeabedRenderer {

    // ==================== 数据结构 ====================

    private static class CoralDeco {
        float x, y, size;
        int type; // 0=鹿角珊瑚, 1=脑珊瑚, 2=管状珊瑚, 3=蘑菇珊瑚, 4=扇形珊瑚
        int color; // 颜色索引

        CoralDeco(float x, float y, float size, int type, int color) {
            this.x = x; this.y = y; this.size = size; this.type = type; this.color = color;
        }
    }

    private static class KelpDeco {
        float x, y, height, width;
        float phase; // 摇摆相位偏移

        KelpDeco(float x, float y, float height, float width, float phase) {
            this.x = x; this.y = y; this.height = height; this.width = width; this.phase = phase;
        }
    }

    private static class BubbleSource {
        float x, y;
        float rate; // 冒泡频率
        float maxH; // 气泡最大上升高度

        BubbleSource(float x, float y, float rate, float maxH) {
            this.x = x; this.y = y; this.rate = rate; this.maxH = maxH;
        }
    }

    // 海底地面冒出的气泡（从地面缓慢升起变大）
    private static class AmbientBubble {
        float x, y;         // 世界坐标
        float riseDist;     // 已上升距离
        float speed;        // 上升速度
        float maxRise;      // 最大上升距离
        float maxSize;      // 最终半径
        float wobblePhase;  // 左右摇摆相位
        float wobbleFreq;   // 摇摆频率

        AmbientBubble(float x, float y, float speed, float maxSize, float maxRise) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.maxSize = maxSize;
            this.maxRise = maxRise;
            this.riseDist = 0;
            this.wobblePhase = (float) (Math.random() * Math.PI * 2);
            this.wobbleFreq = 1.0f + (float) (Math.random() * 1.5f);
        }
    }

    private List<AmbientBubble> ambientBubbles = new ArrayList<>();
    private float ambientBubbleTimer = 0;
    private static final float AMBIENT_BUBBLE_INTERVAL = 0.6f; // 生成间隔（秒）

    // ==================== 成员变量 ====================

    private List<CoralDeco> corals = new ArrayList<>();
    private List<KelpDeco> kelps = new ArrayList<>();
    private List<BubbleSource> bubbleSources = new ArrayList<>();
    private long animBaseTime = 0;

    // 龙宫建筑障碍物（与 MapGenerator 一致）
    private List<Rect> palaceObstacles = new ArrayList<>();

    // 珊瑚颜色表
    private static final int[][] CORAL_COLORS = {
        {220, 70, 70},   // 红珊瑚
        {255, 130, 50},  // 橙珊瑚
        {180, 70, 190},  // 紫珊瑚
        {255, 190, 70},  // 金珊瑚
        {80, 200, 150},  // 绿珊瑚
        {255, 120, 160}, // 粉珊瑚
    };

    // ==================== 初始化 ====================

    public void init() {
        generateCorals();
        generateKelps();
        generateBubbleSources();
        palaceObstacles = DonghaiSeabedMapGenerator.getPalaceWallObstacles();
    }

    private void generateCorals() {
        corals.clear();
        Random rng = new Random(77777);

        // 珊瑚礁带大量珊瑚 (距中心 0.30~0.40 * MAX_DIST)
        float maxDist = (float) Math.sqrt(10000f * 10000f + 10000f * 10000f);
        for (int i = 0; i < 600; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.30f + rng.nextFloat() * 0.10f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            float size = 40 + rng.nextFloat() * 80;
            int type = rng.nextInt(5);
            int color = rng.nextInt(CORAL_COLORS.length);
            corals.add(new CoralDeco(wx, wy, size, type, color));
        }

        // 龙宫周围装饰性珊瑚 (在龙宫附近散布)
        for (int i = 0; i < 150; i++) {
            float wx = 6000 + rng.nextFloat() * 8000;
            float wy = 6000 + rng.nextFloat() * 8000;
            // 排除龙宫内部
            if (wx >= 7000 && wx <= 13000 && wy >= 7000 && wy <= 13000) continue;
            float size = 30 + rng.nextFloat() * 60;
            int type = rng.nextInt(5);
            int color = rng.nextInt(CORAL_COLORS.length);
            corals.add(new CoralDeco(wx, wy, size, type, color));
        }

        // 海藻森林区域的大型海藻 (0.40~0.55)
        for (int i = 0; i < 200; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.40f + rng.nextFloat() * 0.15f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            float height = 150 + rng.nextFloat() * 250;
            float width = 8 + rng.nextFloat() * 12;
            float phase = rng.nextFloat() * (float) (2 * Math.PI);
            kelps.add(new KelpDeco(wx, wy, height, width, phase));
        }

        // 龙宫周围也散布一些海藻
        for (int i = 0; i < 80; i++) {
            float wx = 5500 + rng.nextFloat() * 9000;
            float wy = 5500 + rng.nextFloat() * 9000;
            if (wx >= 7100 && wx <= 12900 && wy >= 7100 && wy <= 12900) continue;
            float height = 100 + rng.nextFloat() * 200;
            float width = 6 + rng.nextFloat() * 10;
            float phase = rng.nextFloat() * (float) (2 * Math.PI);
            kelps.add(new KelpDeco(wx, wy, height, width, phase));
        }
    }

    private void generateKelps() {
        // 已在 generateCorals 中一起生成
    }

    private void generateBubbleSources() {
        bubbleSources.clear();
        Random rng = new Random(88888);

        // 热液喷口区域密集气泡
        float maxDist = (float) Math.sqrt(10000f * 10000f + 10000f * 10000f);
        for (int i = 0; i < 40; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.75f + rng.nextFloat() * 0.15f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            bubbleSources.add(new BubbleSource(wx, wy, 0.5f + rng.nextFloat(), 200 + rng.nextFloat() * 300));
        }

        // 龙宫周围稀疏气泡
        for (int i = 0; i < 20; i++) {
            float wx = 6500 + rng.nextFloat() * 7000;
            float wy = 6500 + rng.nextFloat() * 7000;
            bubbleSources.add(new BubbleSource(wx, wy, 0.3f + rng.nextFloat() * 0.5f, 150 + rng.nextFloat() * 200));
        }
    }

    // ==================== 主绘制入口 ====================

    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        long time = System.currentTimeMillis();
        if (animBaseTime == 0) animBaseTime = time;
        float t = (time - animBaseTime) / 1000f;

        // 1. 绘制龙宫建筑群
        drawPalaceStructures(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 2. 绘制珊瑚装饰
        drawCorals(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 3. 绘制海藻（带水下摇摆）
        drawKelps(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 4. 绘制气泡
        drawBubbles(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 5. 绘制焦散光斑（浅水区）
        drawCaustics(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 6. 绘制水下滤镜（全局蓝色遮罩）
        drawUnderwaterOverlay(canvas, cameraX, cameraY, screenWidth, screenHeight);
    }

    // ==================== 龙宫建筑群 ====================

    private void drawPalaceStructures(Canvas canvas, float cameraX, float cameraY,
                                       int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 龙宫区域参数
        float px1 = DonghaiSeabedMapGenerator.PALACE_X1;
        float py1 = DonghaiSeabedMapGenerator.PALACE_Y1;
        float px2 = DonghaiSeabedMapGenerator.PALACE_X2;
        float py2 = DonghaiSeabedMapGenerator.PALACE_Y2;

        // 视锥剔除
        if (px2 - cameraX < 0 || px1 - cameraX > sw || py2 - cameraY < 0 || py1 - cameraY > sh) return;

        // --- 绘制主殿 ---
        drawMainHall(canvas, paint, cameraX, cameraY, t);

        // --- 绘制四角明珠塔 ---
        drawPearlTower(canvas, paint, px1 + 300, py1 + 300, cameraX, cameraY, t);
        drawPearlTower(canvas, paint, px2 - 300, py1 + 300, cameraX, cameraY, t);
        drawPearlTower(canvas, paint, px1 + 300, py2 - 300, cameraX, cameraY, t);
        drawPearlTower(canvas, paint, px2 - 300, py2 - 300, cameraX, cameraY, t);

        // --- 绘制南大门 ---
        drawSouthGate(canvas, paint, cameraX, cameraY, t);

        // --- 绘制左右门 ---
        drawSideGate(canvas, paint, DonghaiSeabedMapGenerator.PALACE_X1, cameraX, cameraY, t, false); // 左门
        drawSideGate(canvas, paint, DonghaiSeabedMapGenerator.PALACE_X2, cameraX, cameraY, t, true);  // 右门

        // --- 绘制龙宫围墙装饰（墙顶琉璃瓦） ---
        drawWallDecoration(canvas, paint, cameraX, cameraY, sw, sh);

        // --- 绘制宫殿内珍宝陈列 ---
        drawTreasures(canvas, paint, cameraX, cameraY, sw, sh, t);
    }

    private void drawMainHall(Canvas canvas, Paint paint, float cameraX, float cameraY, float t) {
        // 主殿位置: 龙宫中心偏北
        float hallCX = 10000 - cameraX;
        float hallCY = 9200 - cameraY;
        float hallW = 1600;
        float hallH = 1200;
        float left = hallCX - hallW / 2;
        float top = hallCY - hallH / 2;

        if (left + hallW < 0 || left > canvas.getWidth() || top + hallH < 0 || top > canvas.getHeight()) return;

        // 殿基（玉石台基）
        paint.setColor(Color.rgb(90, 130, 150));
        canvas.drawRect(left - 40, top + hallH - 100, left + hallW + 40, top + hallH + 20, paint);
        paint.setColor(Color.rgb(110, 150, 170));
        canvas.drawRect(left - 20, top + hallH - 80, left + hallW + 20, top + hallH, paint);

        // 殿身（珊瑚红柱 + 翡翠墙）
        paint.setColor(Color.rgb(50, 110, 100));
        canvas.drawRect(left, top + 200, left + hallW, top + hallH - 100, paint);

        // 柱子（6根珊瑚红柱）
        int pillarCount = 6;
        for (int i = 0; i < pillarCount; i++) {
            float px = left + 80 + i * (hallW - 160) / (pillarCount - 1);
            paint.setColor(Color.rgb(180, 60, 50));
            canvas.drawRect(px - 12, top + 200, px + 12, top + hallH - 100, paint);
            // 柱头金箍
            paint.setColor(Color.rgb(200, 170, 50));
            canvas.drawRect(px - 15, top + 195, px + 15, top + 210, paint);
            canvas.drawRect(px - 15, top + hallH - 110, px + 15, top + hallH - 95, paint);
        }

        // 屋顶（翡翠绿琉璃瓦，三层飞檐）
        drawRoof(canvas, paint, left - 60, top, hallW + 120, 220, Color.rgb(30, 130, 90));
        drawRoof(canvas, paint, left - 30, top + 60, hallW + 60, 160, Color.rgb(35, 140, 95));
        drawRoof(canvas, paint, left, top + 110, hallW, 110, Color.rgb(40, 150, 100));

        // 殿顶宝珠（夜明珠，发光效果）
        float pearlX = hallCX;
        float pearlY = top - 10;
        float glow = (float) (Math.sin(t * 2) * 0.3 + 0.7);
        // 光晕
        paint.setColor(Color.argb((int) (60 * glow), 200, 255, 200));
        canvas.drawCircle(pearlX, pearlY, 40, paint);
        paint.setColor(Color.argb((int) (100 * glow), 220, 255, 230));
        canvas.drawCircle(pearlX, pearlY, 22, paint);
        // 珠体
        paint.setColor(Color.rgb(230, 255, 240));
        canvas.drawCircle(pearlX, pearlY, 12, paint);

        // 匾额
        paint.setColor(Color.rgb(60, 40, 20));
        canvas.drawRect(hallCX - 120, top + 210, hallCX + 120, top + 270, paint);
        paint.setColor(Color.rgb(200, 170, 50));
        canvas.drawRect(hallCX - 115, top + 215, hallCX + 115, top + 265, paint);
        // "龙宫" 文字
        paint.setColor(Color.rgb(60, 40, 20));
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("东海龙宫", hallCX, top + 250, paint);
    }

    private void drawRoof(Canvas canvas, Paint paint, float left, float top, float w, float h, int color) {
        // 飞檐屋顶（梯形 + 两端翘起）
        paint.setColor(color);
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(left - 20, top + h);
        roof.lineTo(left + w * 0.1f, top);
        roof.lineTo(left + w * 0.9f, top);
        roof.lineTo(left + w + 20, top + h);
        roof.close();
        canvas.drawPath(roof, paint);

        // 屋脊金边
        paint.setColor(Color.rgb(200, 170, 50));
        paint.setStrokeWidth(2);
        canvas.drawLine(left + w * 0.1f, top, left + w * 0.9f, top, paint);
        paint.setStrokeWidth(1);
    }

    private void drawPearlTower(Canvas canvas, Paint paint, float worldX, float worldY,
                                 float cameraX, float cameraY, float t) {
        float sx = worldX - cameraX;
        float sy = worldY - cameraY;
        if (sx < -100 || sx > canvas.getWidth() + 100 || sy < -150 || sy > canvas.getHeight() + 100) return;

        // 塔基
        paint.setColor(Color.rgb(80, 120, 140));
        canvas.drawRect(sx - 35, sy - 10, sx + 35, sy + 30, paint);

        // 塔身（三层）
        for (int i = 0; i < 3; i++) {
            float ty = sy - 30 - i * 40;
            float tw = 30 - i * 5;
            paint.setColor(Color.rgb(50, 110, 100));
            canvas.drawRect(sx - tw, ty - 30, sx + tw, ty, paint);
            // 小飞檐
            paint.setColor(Color.rgb(35, 130, 90));
            canvas.drawRect(sx - tw - 8, ty - 35, sx + tw + 8, ty - 28, paint);
        }

        // 塔顶夜明珠
        float pearlY = sy - 155;
        float glow = (float) (Math.sin(t * 2.5 + worldX * 0.01) * 0.3 + 0.7);
        paint.setColor(Color.argb((int) (50 * glow), 180, 255, 220));
        canvas.drawCircle(sx, pearlY, 25, paint);
        paint.setColor(Color.rgb(220, 255, 240));
        canvas.drawCircle(sx, pearlY, 10, paint);
    }

    private void drawSouthGate(Canvas canvas, Paint paint, float cameraX, float cameraY, float t) {
        int gateCX = (DonghaiSeabedMapGenerator.PALACE_X1 + DonghaiSeabedMapGenerator.PALACE_X2) / 2;
        float sx = gateCX - cameraX;
        float sy = DonghaiSeabedMapGenerator.PALACE_Y2 - cameraY;

        if (sx < -300 || sx > canvas.getWidth() + 300 || sy < -200 || sy > canvas.getHeight() + 200) return;

        // 门框（两根大柱子 + 横梁）
        float gateW = DonghaiSeabedMapGenerator.GATE_WIDTH;
        float gateH = 300;

        // 左柱
        paint.setColor(Color.rgb(170, 55, 45));
        canvas.drawRect(sx - gateW / 2 - 20, sy - gateH, sx - gateW / 2 + 20, sy, paint);
        // 右柱
        canvas.drawRect(sx + gateW / 2 - 20, sy - gateH, sx + gateW / 2 + 20, sy, paint);

        // 横梁
        paint.setColor(Color.rgb(170, 55, 45));
        canvas.drawRect(sx - gateW / 2 - 30, sy - gateH - 20, sx + gateW / 2 + 30, sy - gateH, paint);

        // 门额（金色匾）
        paint.setColor(Color.rgb(200, 170, 50));
        canvas.drawRect(sx - 100, sy - gateH - 60, sx + 100, sy - gateH - 20, paint);
        paint.setColor(Color.rgb(60, 40, 20));
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("水晶宫", sx, sy - gateH - 32, paint);

        // 门顶飞檐
        paint.setColor(Color.rgb(35, 130, 90));
        canvas.drawRect(sx - gateW / 2 - 50, sy - gateH - 70, sx + gateW / 2 + 50, sy - gateH - 58, paint);

        // 柱头金箍
        paint.setColor(Color.rgb(200, 170, 50));
        canvas.drawRect(sx - gateW / 2 - 25, sy - gateH - 5, sx - gateW / 2 + 25, sy - gateH + 10, paint);
        canvas.drawRect(sx + gateW / 2 - 25, sy - gateH - 5, sx + gateW / 2 + 25, sy - gateH + 10, paint);
    }

    /**
     * 绘制左右侧门（西墙/东墙）
     */
    private void drawSideGate(Canvas canvas, Paint paint, int wallX, float cameraX, float cameraY, float t, boolean isEast) {
        float gateCY = DonghaiSeabedMapGenerator.SIDE_GATE_CENTER_Y - cameraY;
        float sx = wallX - cameraX;

        if (sx < -200 || sx > canvas.getWidth() + 200 || gateCY < -200 || gateCY > canvas.getHeight() + 200) return;

        float gateW = DonghaiSeabedMapGenerator.SIDE_GATE_WIDTH;
        float gateH = 250; // 门高度
        float halfGate = gateW / 2f;

        // 两根柱子（上下排列，门沿垂直方向开启）
        paint.setColor(Color.rgb(170, 55, 45));
        // 上柱
        canvas.drawRect(sx - 20, gateCY - halfGate - 15, sx + 20, gateCY - halfGate + 15, paint);
        // 下柱
        canvas.drawRect(sx - 20, gateCY + halfGate - 15, sx + 20, gateCY + halfGate + 15, paint);

        // 门楣（横向连接）
        paint.setColor(Color.rgb(170, 55, 45));
        canvas.drawRect(sx - 15, gateCY - halfGate, sx + 15, gateCY + halfGate, paint);
        // 门楣内部（通透感，用较浅的颜色）
        paint.setColor(Color.rgb(70, 115, 135));
        canvas.drawRect(sx - 8, gateCY - halfGate + 18, sx + 8, gateCY + halfGate - 18, paint);

        // 门额（金色小匾）
        float plaqueY = gateCY - halfGate - 25;
        paint.setColor(Color.rgb(200, 170, 50));
        canvas.drawRect(sx - 40, plaqueY - 15, sx + 40, plaqueY + 15, paint);
        paint.setColor(Color.rgb(60, 40, 20));
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(isEast ? "东海门" : "西海门", sx, plaqueY + 5, paint);

        // 飞檐装饰
        paint.setColor(Color.rgb(35, 130, 90));
        canvas.drawRect(sx - 50, plaqueY - 22, sx + 50, plaqueY - 14, paint);

        // 柱头金箍
        paint.setColor(Color.rgb(200, 170, 50));
        canvas.drawRect(sx - 22, gateCY - halfGate - 18, sx + 22, gateCY - halfGate - 12, paint);
        canvas.drawRect(sx - 22, gateCY + halfGate + 12, sx + 22, gateCY + halfGate + 18, paint);
    }

    private void drawWallDecoration(Canvas canvas, Paint paint, float cameraX, float cameraY,
                                     int sw, int sh) {
        // 在围墙顶部画琉璃瓦装饰线
        float px1 = DonghaiSeabedMapGenerator.PALACE_X1 - cameraX;
        float py1 = DonghaiSeabedMapGenerator.PALACE_Y1 - cameraY;
        float px2 = DonghaiSeabedMapGenerator.PALACE_X2 - cameraX;
        float py2 = DonghaiSeabedMapGenerator.PALACE_Y2 - cameraY;

        paint.setColor(Color.rgb(35, 130, 90));
        paint.setStrokeWidth(3);

        // 北墙顶
        if (py1 > -10 && py1 < sh + 10) {
            canvas.drawLine(Math.max(0, px1), py1, Math.min(sw, px2), py1, paint);
        }
        // 西墙顶
        if (px1 > -10 && px1 < sw + 10) {
            canvas.drawLine(px1, Math.max(0, py1), px1, Math.min(sh, py2), paint);
        }
        // 东墙顶
        if (px2 > -10 && px2 < sw + 10) {
            canvas.drawLine(px2, Math.max(0, py1), px2, Math.min(sh, py2), paint);
        }
        paint.setStrokeWidth(1);
    }

    private void drawTreasures(Canvas canvas, Paint paint, float cameraX, float cameraY,
                                int sw, int sh, float t) {
        // 龙宫内的珍宝装饰（金元宝堆、珊瑚摆件）
        Random rng = new Random(55555);
        float px1 = 7200, py1 = 7200, px2 = 12800, py2 = 12800;

        // 视锥剔除
        if (px2 - cameraX < 0 || px1 - cameraX > sw || py2 - cameraY < 0 || py1 - cameraY > sh) return;

        for (int i = 0; i < 30; i++) {
            float wx = px1 + rng.nextFloat() * (px2 - px1);
            float wy = py1 + rng.nextFloat() * (py2 - py1);
            float sx = wx - cameraX;
            float sy = wy - cameraY;
            if (sx < -20 || sx > sw + 20 || sy < -20 || sy > sh + 20) continue;

            int type = rng.nextInt(3);
            switch (type) {
                case 0: // 金元宝
                    paint.setColor(Color.rgb(220, 190, 50));
                    canvas.drawOval(sx - 6, sy - 3, sx + 6, sy + 3, paint);
                    paint.setColor(Color.argb(60, 255, 240, 100));
                    canvas.drawCircle(sx, sy - 2, 3, paint);
                    break;
                case 1: // 小珊瑚摆件
                    int ci = rng.nextInt(CORAL_COLORS.length);
                    paint.setColor(Color.rgb(CORAL_COLORS[ci][0], CORAL_COLORS[ci][1], CORAL_COLORS[ci][2]));
                    canvas.drawCircle(sx, sy, 4 + rng.nextFloat() * 3, paint);
                    break;
                case 2: // 发光珠
                    float glow = (float) (Math.sin(t * 3 + i * 0.7) * 0.3 + 0.7);
                    paint.setColor(Color.argb((int) (40 * glow), 200, 255, 220));
                    canvas.drawCircle(sx, sy, 8, paint);
                    paint.setColor(Color.rgb(230, 255, 240));
                    canvas.drawCircle(sx, sy, 3, paint);
                    break;
            }
        }
    }

    // ==================== 珊瑚装饰 ====================

    private void drawCorals(Canvas canvas, float cameraX, float cameraY, int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float margin = 150;

        for (CoralDeco c : corals) {
            float sx = c.x - cameraX;
            float sy = c.y - cameraY;
            if (sx < -margin || sx > sw + margin || sy < -margin || sy > sh + margin) continue;

            int[] color = CORAL_COLORS[c.color];
            float sway = (float) Math.sin(t * 0.5 + c.x * 0.003) * 3;

            switch (c.type) {
                case 0: drawBranchCoral(canvas, paint, sx, sy, c.size, color, sway); break;
                case 1: drawBrainCoral(canvas, paint, sx, sy, c.size, color); break;
                case 2: drawTubeCoral(canvas, paint, sx, sy, c.size, color, sway); break;
                case 3: drawMushroomCoral(canvas, paint, sx, sy, c.size, color); break;
                case 4: drawFanCoral(canvas, paint, sx, sy, c.size, color, sway); break;
            }
        }
    }

    private void drawBranchCoral(Canvas canvas, Paint paint, float cx, float cy, float size,
                                  int[] color, float sway) {
        // 鹿角珊瑚：分支结构
        paint.setColor(Color.rgb(color[0], color[1], color[2]));
        paint.setStrokeWidth(size * 0.08f);
        int branches = 3 + (int) (size / 30);
        for (int i = 0; i < branches; i++) {
            float angle = -0.8f + 1.6f * i / (branches - 1);
            float bx = cx + (float) Math.sin(angle) * size * 0.5f + sway;
            float by = cy - (float) Math.cos(angle) * size * 0.5f;
            canvas.drawLine(cx, cy, bx, by, paint);
            // 小分支
            float mx = cx + (bx - cx) * 0.6f;
            float my = cy + (by - cy) * 0.6f;
            canvas.drawLine(mx, my, mx + sway + (float) Math.sin(angle + 0.5f) * size * 0.2f,
                    my - (float) Math.cos(angle + 0.5f) * size * 0.2f, paint);
        }
        paint.setStrokeWidth(1);
    }

    private void drawBrainCoral(Canvas canvas, Paint paint, float cx, float cy, float size, int[] color) {
        // 脑珊瑚：圆形团状
        float r = size * 0.4f;
        paint.setColor(Color.rgb(color[0], color[1], color[2]));
        canvas.drawCircle(cx, cy, r, paint);
        // 纹路
        paint.setColor(Color.argb(40, 0, 0, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        for (int i = 0; i < 3; i++) {
            float offset = -r * 0.4f + i * r * 0.4f;
            canvas.drawArc(new RectF(cx - r + 2, cy + offset - 3, cx + r - 2, cy + offset + 3), 30, 120, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawTubeCoral(Canvas canvas, Paint paint, float cx, float cy, float size,
                                int[] color, float sway) {
        // 管状珊瑚：多根管子
        int tubes = 3 + (int) (size / 35);
        for (int i = 0; i < tubes; i++) {
            float tx = cx - size * 0.3f + i * size * 0.6f / (tubes - 1);
            float th = size * 0.3f + size * 0.3f * (i % 2 == 0 ? 1 : 0.6f);
            paint.setColor(Color.rgb(color[0], color[1], color[2]));
            canvas.drawRect(tx - 3 + sway * 0.5f, cy - th, tx + 3 + sway * 0.5f, cy, paint);
            // 管口
            paint.setColor(Color.rgb(Math.max(0, color[0] - 40), Math.max(0, color[1] - 40), Math.max(0, color[2] - 40)));
            canvas.drawCircle(tx + sway * 0.5f, cy - th, 4, paint);
        }
    }

    private void drawMushroomCoral(Canvas canvas, Paint paint, float cx, float cy, float size, int[] color) {
        // 蘑菇珊瑚：伞状
        float r = size * 0.35f;
        // 柄
        paint.setColor(Color.rgb(Math.min(255, color[0] + 30), Math.min(255, color[1] + 30), Math.min(255, color[2] + 30)));
        canvas.drawRect(cx - 3, cy - r * 0.5f, cx + 3, cy, paint);
        // 伞盖
        paint.setColor(Color.rgb(color[0], color[1], color[2]));
        canvas.drawOval(cx - r, cy - r * 0.8f, cx + r, cy - r * 0.2f, paint);
        // 高光
        paint.setColor(Color.argb(40, 255, 255, 255));
        canvas.drawCircle(cx - r * 0.2f, cy - r * 0.6f, r * 0.25f, paint);
    }

    private void drawFanCoral(Canvas canvas, Paint paint, float cx, float cy, float size,
                               int[] color, float sway) {
        // 扇形珊瑚
        float w = size * 0.5f;
        float h = size * 0.6f;
        paint.setColor(Color.rgb(color[0], color[1], color[2]));
        android.graphics.Path fan = new android.graphics.Path();
        fan.moveTo(cx, cy);
        fan.quadTo(cx - w + sway, cy - h * 0.7f, cx + sway * 0.5f, cy - h);
        fan.quadTo(cx + w + sway, cy - h * 0.7f, cx, cy);
        fan.close();
        canvas.drawPath(fan, paint);
        // 脉络
        paint.setColor(Color.argb(30, 0, 0, 0));
        paint.setStrokeWidth(1);
        canvas.drawLine(cx, cy, cx + sway * 0.5f, cy - h * 0.8f, paint);
    }

    // ==================== 海藻（带水流摇摆） ====================

    private void drawKelps(Canvas canvas, float cameraX, float cameraY, int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float margin = 200;

        for (KelpDeco k : kelps) {
            float sx = k.x - cameraX;
            float sy = k.y - cameraY;
            if (sx < -margin || sx > sw + margin || sy < -margin || sy > sh + k.height + margin) continue;

            // 水流摇摆：低频正弦，比陆地风力更慢
            float swayAmp = 15 + k.width;
            float swayFreq = 0.4f;

            paint.setStrokeWidth(k.width);
            paint.setColor(Color.rgb(20, 90, 40));

            // 用贝塞尔曲线绘制海藻主体
            android.graphics.Path kelpPath = new android.graphics.Path();
            kelpPath.moveTo(sx, sy);
            float midSway = (float) Math.sin(t * swayFreq + k.phase) * swayAmp * 0.5f;
            float topSway = (float) Math.sin(t * swayFreq + k.phase + 1) * swayAmp;
            kelpPath.quadTo(sx + midSway, sy - k.height * 0.5f, sx + topSway, sy - k.height);
            canvas.drawPath(kelpPath, paint);

            // 叶片
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(30, 110, 50));
            int leafCount = (int) (k.height / 60);
            for (int i = 1; i <= leafCount; i++) {
                float frac = (float) i / (leafCount + 1);
                float lx = sx + topSway * frac * frac;
                float ly = sy - k.height * frac;
                float leafLen = 15 + k.width;
                float dir = (i % 2 == 0) ? 1 : -1;
                float leafSway = (float) Math.sin(t * 0.6 + k.phase + i) * 5;
                canvas.drawLine(lx, ly, lx + dir * leafLen + leafSway, ly - 5, paint);
            }
        }
        paint.setStrokeWidth(1);
    }

    // ==================== 气泡粒子 ====================

    private void drawBubbles(Canvas canvas, float cameraX, float cameraY, int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // --- 更新并绘制海底地面冒出的气泡（从地面升起，逐渐变大）---
        float dt = 0.016f; // 近似帧时间
        ambientBubbleTimer += dt;
        if (ambientBubbleTimer >= AMBIENT_BUBBLE_INTERVAL) {
            ambientBubbleTimer = 0;
            // 在可见区域底部（地面）随机生成气泡
            float bx = cameraX + (float) (Math.random() * sw);
            float by = cameraY + sh - 20 + (float) (Math.random() * 40); // 地面附近
            float speed = 20 + (float) (Math.random() * 40);    // 上升速度 20~60 px/s
            float maxSize = 6 + (float) (Math.random() * 16);   // 最终半径 6~22
            float maxRise = 300 + (float) (Math.random() * 500); // 上升距离 300~800px
            ambientBubbles.add(new AmbientBubble(bx, by, speed, maxSize, maxRise));
        }

        // 限制同时存在的气泡数量
        if (ambientBubbles.size() > 25) {
            ambientBubbles.remove(0);
        }

        java.util.Iterator<AmbientBubble> it = ambientBubbles.iterator();
        while (it.hasNext()) {
            AmbientBubble b = it.next();
            // 更新：向上移动
            b.riseDist += b.speed * dt;
            b.y -= b.speed * dt;
            // 轻微左右摇摆（越往上摆幅越大）
            float progress = b.riseDist / b.maxRise;
            float wobbleAmp = 0.3f + progress * 1.2f;
            b.x += (float) Math.sin(t * b.wobbleFreq + b.wobblePhase) * wobbleAmp;

            if (b.riseDist >= b.maxRise) {
                it.remove();
                continue;
            }

            float sx = b.x - cameraX;
            float sy = b.y - cameraY;
            if (sx < -50 || sx > sw + 50 || sy < -50 || sy > sh + 50) continue;

            // 大小：从 1.5px 逐渐长大到 maxSize（平方曲线，开始慢后面快）
            float size = 1.5f + (b.maxSize - 1.5f) * progress * progress;
            // 透明度：前段渐显，末段渐隐
            int alpha;
            if (progress < 0.15f) {
                alpha = (int) (progress / 0.15f * 180);
            } else if (progress > 0.85f) {
                alpha = (int) ((1 - (progress - 0.85f) / 0.15f) * 180);
            } else {
                alpha = 180;
            }
            if (alpha <= 0) continue;

            // 气泡外圈
            paint.setColor(Color.argb(alpha, 180, 225, 255));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.2f + progress * 0.8f);
            canvas.drawCircle(sx, sy, size, paint);

            // 内部半透明填充
            paint.setColor(Color.argb(alpha / 6, 140, 210, 255));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(sx, sy, size, paint);

            // 高光点（左上，随气泡变大更明显）
            float hlSize = size * (0.2f + progress * 0.1f);
            paint.setColor(Color.argb((int)(alpha * 0.9f), 255, 255, 255));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(sx - size * 0.28f, sy - size * 0.32f, hlSize, paint);

            // 次高光（右下小点）
            paint.setColor(Color.argb(alpha / 3, 255, 255, 255));
            canvas.drawCircle(sx + size * 0.22f, sy + size * 0.28f, size * 0.1f, paint);
        }
        paint.setStyle(Paint.Style.FILL);

        // --- 原有气泡源小气泡 ---
        for (BubbleSource src : bubbleSources) {
            float sx = src.x - cameraX;
            float sy = src.y - cameraY;
            if (sx < -50 || sx > sw + 50 || sy < -src.maxH - 50 || sy > sh + 50) continue;

            // 每个气泡源产生多个气泡（不同相位）
            int bubbleCount = (int) (src.rate * 5);
            for (int i = 0; i < bubbleCount; i++) {
                float phase = (t * src.rate + i * 1.3f) % 3.0f;
                float rise = phase / 3.0f;
                float bx = sx + (float) Math.sin(phase * 4 + src.x * 0.01) * 8;
                float by = sy - rise * src.maxH;
                float bSize = 2 + rise * 5;
                int alpha = (int) ((1 - rise) * 120);
                if (alpha <= 0) continue;

                paint.setColor(Color.argb(alpha, 200, 230, 255));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1);
                canvas.drawCircle(bx, by, bSize, paint);
                // 高光
                paint.setColor(Color.argb(alpha / 2, 255, 255, 255));
                canvas.drawCircle(bx - bSize * 0.3f, by - bSize * 0.3f, bSize * 0.3f, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
    }

    // ==================== 焦散光斑 ====================

    private void drawCaustics(Canvas canvas, float cameraX, float cameraY, int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 焦散光斑只在较浅区域（距中心 < 0.45 * MAX_DIST）绘制
        float cx = 10000, cy = 10000;

        // 用多个缓慢移动变形的椭圆模拟焦散
        int causticCount = 15;
        for (int i = 0; i < causticCount; i++) {
            float phase1 = t * 0.3f + i * 2.1f;
            float phase2 = t * 0.2f + i * 1.7f;
            float worldX = cx + (float) Math.sin(phase1) * 3000 + (float) Math.cos(phase2) * 2000;
            float worldY = cy + (float) Math.cos(phase1) * 3000 + (float) Math.sin(phase2) * 2000;

            float sx = worldX - cameraX;
            float sy = worldY - cameraY;
            if (sx < -200 || sx > sw + 200 || sy < -200 || sy > sh + 200) continue;

            float size = 60 + (float) Math.sin(t * 0.5 + i) * 30;
            int alpha = 15 + (int) (Math.sin(t * 0.8 + i * 1.3) * 10);

            paint.setColor(Color.argb(alpha, 180, 230, 255));
            canvas.drawOval(sx - size, sy - size * 0.6f, sx + size, sy + size * 0.6f, paint);
        }
    }

    // ==================== 水下滤镜 ====================

    private void drawUnderwaterOverlay(Canvas canvas, float cameraX, float cameraY, int sw, int sh) {
        Paint paint = new Paint();

        // 全局蓝绿色遮罩（模拟水下色偏）
        paint.setColor(Color.argb(35, 10, 60, 100));
        canvas.drawRect(0, 0, sw, sh, paint);

        // 从上方打下的渐变光柱（模拟水面透光）
        float lightIntensity = 0.12f;
        paint.setShader(new LinearGradient(0, 0, 0, sh,
                Color.argb((int) (lightIntensity * 255), 80, 180, 220),
                Color.argb(0, 20, 60, 100),
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, sw, sh, paint);
        paint.setShader(null);
    }

    // ==================== 碰撞与安全区 ====================

    public List<Rect> getObstacles() {
        return palaceObstacles;
    }

    /**
     * 获取龙宫区域安全区（龙宫内部为安全区）
     */
    public Rect getPalaceSafeZone() {
        return new Rect(
                DonghaiSeabedMapGenerator.PALACE_X1 - 100,
                DonghaiSeabedMapGenerator.PALACE_Y1 - 100,
                DonghaiSeabedMapGenerator.PALACE_X2 + 100,
                DonghaiSeabedMapGenerator.PALACE_Y2 + 100
        );
    }

    public void cleanup() {
        corals.clear();
        kelps.clear();
        bubbleSources.clear();
    }
}

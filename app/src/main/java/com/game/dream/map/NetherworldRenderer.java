package com.game.dream.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 地府地图渲染器
 * 负责绘制：阎罗殿建筑群、鬼门关、奈何桥、判官府、彼岸花田装饰、
 * 白骨、鬼火、冥雾、纸灰、地狱红光、幽冥滤镜
 */
public class NetherworldRenderer {

    // ==================== 数据结构 ====================

    /** 鬼火（漂浮的幽绿/惨蓝/血红光点） */
    private static class Wisp {
        float x, y;
        float baseX, baseY;      // 锚点，用于布朗运动回归
        float radius;             // 光晕半径
        int colorType;            // 0=幽绿, 1=惨蓝, 2=血红
        float phase;              // 明暗呼吸相位
        float driftSpeed;         // 漂移速度
        float driftAngle;         // 当前漂移方向
        float driftTimer;         // 方向切换计时

        Wisp(float x, float y, float radius, int colorType) {
            this.x = x; this.y = y;
            this.baseX = x; this.baseY = y;
            this.radius = radius;
            this.colorType = colorType;
            this.phase = (float) (Math.random() * Math.PI * 2);
            this.driftSpeed = 8 + (float) (Math.random() * 20);
            this.driftAngle = (float) (Math.random() * Math.PI * 2);
            this.driftTimer = 0;
        }
    }

    /** 冥雾团（缓慢变形漂移的紫灰椭圆） */
    private static class MistPatch {
        float x, y;
        float baseX, baseY;
        float size;
        float phase;
        float driftSpeed;

        MistPatch(float x, float y, float size) {
            this.x = x; this.y = y;
            this.baseX = x; this.baseY = y;
            this.size = size;
            this.phase = (float) (Math.random() * Math.PI * 2);
            this.driftSpeed = 3 + (float) (Math.random() * 6);
        }
    }

    /** 纸灰（缓慢下降的灰白小点） */
    private static class Ash {
        float x, y;
        float speed;
        float wobblePhase;
        float wobbleFreq;
        float size;

        Ash(float x, float y) {
            this.x = x; this.y = y;
            this.speed = 8 + (float) (Math.random() * 14);
            this.wobblePhase = (float) (Math.random() * Math.PI * 2);
            this.wobbleFreq = 0.6f + (float) (Math.random() * 1.2f);
            this.size = 1 + (float) (Math.random() * 1.6f);
        }
    }

    /** 大型白骨装饰（散布在荒原上） */
    private static class BoneDeco {
        float x, y;
        float size;
        int type; // 0=颅骨堆, 1=长骨交叉, 2=肋骨残段, 3=断碑

        BoneDeco(float x, float y, float size, int type) {
            this.x = x; this.y = y; this.size = size; this.type = type;
        }
    }

    /** 大型彼岸花丛（比 tile 装饰更大更醒目） */
    private static class LilyCluster {
        float x, y;
        float size;
        int flowerCount;

        LilyCluster(float x, float y, float size, int flowerCount) {
            this.x = x; this.y = y; this.size = size; this.flowerCount = flowerCount;
        }
    }

    // ==================== 成员变量 ====================

    private List<Wisp> wisps = new ArrayList<>();
    private List<MistPatch> mists = new ArrayList<>();
    private List<Ash> ashes = new ArrayList<>();
    private List<BoneDeco> bones = new ArrayList<>();
    private List<LilyCluster> lilies = new ArrayList<>();
    private float ashTimer = 0;
    private static final float ASH_INTERVAL = 0.15f;
    private long animBaseTime = 0;

    // 阎罗殿围墙障碍
    private List<Rect> palaceObstacles = new ArrayList<>();

    // 鬼火配色（幽绿 / 惨蓝 / 血红）
    private static final int[][] WISP_COLORS = {
        {140, 240, 170},
        {150, 200, 255},
        {255, 100, 90},
    };

    // ==================== 初始化 ====================

    public void init() {
        generateWisps();
        generateMists();
        generateBones();
        generateLilies();
        palaceObstacles = NetherworldMapGenerator.getPalaceWallObstacles();
        // 幽魂牢墙体也加入障碍列表
        palaceObstacles.addAll(NetherworldMapGenerator.getPrisonWallObstacles());
    }

    private void generateWisps() {
        wisps.clear();
        Random rng = new Random(666001);

        // 荒原密集鬼火（0.44~0.86 环带）
        float maxDist = (float) Math.sqrt(10000f * 10000f + 10000f * 10000f);
        for (int i = 0; i < 180; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.44f + rng.nextFloat() * 0.42f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            float r = 20 + rng.nextFloat() * 30;
            int ct = rng.nextInt(3);
            wisps.add(new Wisp(wx, wy, r, ct));
        }

        // 彼岸花田稀疏鬼火（红花映幽光）
        for (int i = 0; i < 40; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.28f + rng.nextFloat() * 0.08f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            float r = 15 + rng.nextFloat() * 20;
            int ct = rng.nextInt(2); // 幽绿或惨蓝，不用血红避免与花冲突
            wisps.add(new Wisp(wx, wy, r, ct));
        }

        // 阎罗殿内长明灯（金色，围绕主殿）
        for (int i = 0; i < 24; i++) {
            float wx = 7500 + rng.nextFloat() * 5000;
            float wy = 7500 + rng.nextFloat() * 5000;
            float r = 12 + rng.nextFloat() * 15;
            wisps.add(new Wisp(wx, wy, r, 1));
        }
    }

    private void generateMists() {
        mists.clear();
        Random rng = new Random(666002);
        // 全地图随机分布 40 团冥雾
        for (int i = 0; i < 40; i++) {
            float wx = 500 + rng.nextFloat() * 19000;
            float wy = 500 + rng.nextFloat() * 19000;
            float size = 200 + rng.nextFloat() * 300;
            mists.add(new MistPatch(wx, wy, size));
        }
    }

    private void generateBones() {
        bones.clear();
        Random rng = new Random(666003);
        float maxDist = (float) Math.sqrt(10000f * 10000f + 10000f * 10000f);
        // 荒原大型白骨（0.50~0.90）
        for (int i = 0; i < 220; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.50f + rng.nextFloat() * 0.40f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            float size = 30 + rng.nextFloat() * 60;
            int type = rng.nextInt(4);
            bones.add(new BoneDeco(wx, wy, size, type));
        }
    }

    private void generateLilies() {
        lilies.clear();
        Random rng = new Random(666004);
        float maxDist = (float) Math.sqrt(10000f * 10000f + 10000f * 10000f);
        // 彼岸花丛（0.28~0.36）
        for (int i = 0; i < 260; i++) {
            float angle = rng.nextFloat() * (float) (2 * Math.PI);
            float dist = (0.28f + rng.nextFloat() * 0.08f) * maxDist;
            float wx = 10000 + (float) Math.cos(angle) * dist;
            float wy = 10000 + (float) Math.sin(angle) * dist;
            // 排除阎罗殿内部
            if (wx >= NetherworldMapGenerator.PALACE_X1 && wx <= NetherworldMapGenerator.PALACE_X2
                && wy >= NetherworldMapGenerator.PALACE_Y1 && wy <= NetherworldMapGenerator.PALACE_Y2) continue;
            float size = 20 + rng.nextFloat() * 30;
            int count = 3 + rng.nextInt(5);
            lilies.add(new LilyCluster(wx, wy, size, count));
        }
    }

    // ==================== 主绘制入口 ====================

    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        long time = System.currentTimeMillis();
        if (animBaseTime == 0) animBaseTime = time;
        float t = (time - animBaseTime) / 1000f;
        float dt = 0.016f;

        // 1. 阎罗殿建筑群（主殿 + 判官府 + 匾额）
        drawPalaceStructures(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 2. 鬼门关（南门） + 侧门
        drawGhostGate(canvas, cameraX, cameraY, t);
        drawSideGate(canvas, NetherworldMapGenerator.PALACE_X1, cameraX, cameraY, false);
        drawSideGate(canvas, NetherworldMapGenerator.PALACE_X2, cameraX, cameraY, true);

        // 3. 围墙顶部装饰（血色琉璃）
        drawWallDecoration(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 4. 奈何桥（石桥 + 灯笼 + 锁链）
        drawNaiheBridge(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 5. 大型彼岸花丛
        drawLilyClusters(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 6. 大型白骨装饰
        drawBoneDecos(canvas, cameraX, cameraY, screenWidth, screenHeight);

        // 6.5 幽魂牢（东南角封闭囚室）
        drawPrison(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 7. 地狱深渊边缘红光
        drawHellGlow(canvas, cameraX, cameraY, screenWidth, screenHeight, t);

        // 8. 鬼火（漂浮光点）
        drawWisps(canvas, cameraX, cameraY, screenWidth, screenHeight, t, dt);

        // 9. 冥雾（大团雾）
        drawMists(canvas, cameraX, cameraY, screenWidth, screenHeight, t, dt);

        // 10. 纸灰（缓慢下降）
        drawAshes(canvas, cameraX, cameraY, screenWidth, screenHeight, dt);

        // 11. 幽冥滤镜（紫黑遮罩 + 暗角）
        drawNetherOverlay(canvas, screenWidth, screenHeight);
    }

    // ==================== 阎罗殿建筑群 ====================

    private void drawPalaceStructures(Canvas canvas, float cameraX, float cameraY,
                                       int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float px1 = NetherworldMapGenerator.PALACE_X1;
        float py1 = NetherworldMapGenerator.PALACE_Y1;
        float px2 = NetherworldMapGenerator.PALACE_X2;
        float py2 = NetherworldMapGenerator.PALACE_Y2;

        if (px2 - cameraX < 0 || px1 - cameraX > sw || py2 - cameraY < 0 || py1 - cameraY > sh) return;

        // 主殿（森罗殿）
        drawMainHall(canvas, paint, cameraX, cameraY, t);

        // 四角判官府（赏善司 / 罚恶司 / 察查司 / 阴律司）
        drawJudgeTower(canvas, paint, px1 + 400, py1 + 400, cameraX, cameraY, t, "赏善司");
        drawJudgeTower(canvas, paint, px2 - 400, py1 + 400, cameraX, cameraY, t, "罚恶司");
        drawJudgeTower(canvas, paint, px1 + 400, py2 - 400, cameraX, cameraY, t, "察查司");
        drawJudgeTower(canvas, paint, px2 - 400, py2 - 400, cameraX, cameraY, t, "阴律司");

        // 十八层地狱入口（殿后北方，红光漩涡）
        drawHellEntrance(canvas, paint, cameraX, cameraY, t);

        // 殿前香炉
        drawIncenseBurner(canvas, paint, 9600 - cameraX, 11500 - cameraY, t);
        drawIncenseBurner(canvas, paint, 10400 - cameraX, 11500 - cameraY, t);
    }

    private void drawMainHall(Canvas canvas, Paint paint, float cameraX, float cameraY, float t) {
        float hallCX = 10000 - cameraX;
        float hallCY = 9500 - cameraY;
        float hallW = 1800;
        float hallH = 1300;
        float left = hallCX - hallW / 2;
        float top = hallCY - hallH / 2;

        if (left + hallW < 0 || left > canvas.getWidth() || top + hallH < 0 || top > canvas.getHeight()) return;

        // 殿基（黑石台基，两层）
        paint.setColor(Color.rgb(35, 28, 45));
        canvas.drawRect(left - 60, top + hallH - 120, left + hallW + 60, top + hallH + 30, paint);
        paint.setColor(Color.rgb(50, 40, 60));
        canvas.drawRect(left - 30, top + hallH - 95, left + hallW + 30, top + hallH - 5, paint);

        // 殿身（玄黑墙）
        paint.setColor(Color.rgb(28, 22, 38));
        canvas.drawRect(left, top + 220, left + hallW, top + hallH - 100, paint);

        // 六根血红木柱
        int pillarCount = 6;
        for (int i = 0; i < pillarCount; i++) {
            float px = left + 90 + i * (hallW - 180) / (pillarCount - 1);
            paint.setColor(Color.rgb(110, 25, 25));
            canvas.drawRect(px - 14, top + 220, px + 14, top + hallH - 100, paint);
            // 柱头金箍
            paint.setColor(Color.rgb(180, 150, 50));
            canvas.drawRect(px - 17, top + 215, px + 17, top + 232, paint);
            canvas.drawRect(px - 17, top + hallH - 110, px + 17, top + hallH - 93, paint);
            // 柱上盘绕的锁链纹（简单横线）
            paint.setColor(Color.argb(160, 200, 180, 80));
            for (int j = 0; j < 4; j++) {
                float ly = top + 260 + j * (hallH - 400) / 4f;
                canvas.drawRect(px - 13, ly, px + 13, ly + 2, paint);
            }
        }

        // 屋顶（三层玄黑琉璃瓦，血红木脊）
        drawRoof(canvas, paint, left - 80, top, hallW + 160, 240, Color.rgb(18, 14, 28));
        drawRoof(canvas, paint, left - 40, top + 70, hallW + 80, 170, Color.rgb(24, 18, 34));
        drawRoof(canvas, paint, left, top + 130, hallW, 110, Color.rgb(30, 22, 40));

        // 殿顶"幽冥眼"（发光宝珠，缓慢眨眼）
        float pearlX = hallCX;
        float pearlY = top - 20;
        // 呼吸式发光（0.3~1.0 循环）
        float glow = (float) (Math.sin(t * 1.2) * 0.35 + 0.65);
        // 外光晕（血红）
        paint.setColor(Color.argb((int) (70 * glow), 220, 60, 60));
        canvas.drawCircle(pearlX, pearlY, 55, paint);
        // 内光晕（幽绿）
        paint.setColor(Color.argb((int) (120 * glow), 140, 240, 180));
        canvas.drawCircle(pearlX, pearlY, 28, paint);
        // 珠体
        paint.setColor(Color.rgb(220, 255, 230));
        canvas.drawCircle(pearlX, pearlY, 14, paint);
        // 瞳孔（黑点，随时间左右移动，营造注视感）
        float pupilOffset = (float) Math.sin(t * 0.5) * 4;
        paint.setColor(Color.rgb(10, 5, 15));
        canvas.drawCircle(pearlX + pupilOffset, pearlY, 5, paint);

        // 匾额：森罗殿
        paint.setColor(Color.rgb(15, 10, 20));
        canvas.drawRect(hallCX - 160, top + 230, hallCX + 160, top + 300, paint);
        paint.setColor(Color.rgb(180, 150, 50));
        canvas.drawRect(hallCX - 155, top + 235, hallCX + 155, top + 295, paint);
        paint.setColor(Color.rgb(20, 10, 10));
        paint.setTextSize(38);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("森罗殿", hallCX, top + 280, paint);

        // 匾额下方对联（血字）
        paint.setColor(Color.argb(200, 160, 30, 30));
        paint.setTextSize(18);
        canvas.drawText("善恶到头终有报", hallCX - 260, top + 360, paint);
        canvas.drawText("是非分明不容情", hallCX + 260, top + 360, paint);
    }

    private void drawRoof(Canvas canvas, Paint paint, float left, float top, float w, float h, int color) {
        // 飞檐屋顶（梯形 + 两端翘起）
        paint.setColor(color);
        Path roof = new Path();
        roof.moveTo(left - 30, top + h);
        roof.lineTo(left + w * 0.1f, top);
        roof.lineTo(left + w * 0.9f, top);
        roof.lineTo(left + w + 30, top + h);
        roof.close();
        canvas.drawPath(roof, paint);

        // 屋脊血色金边
        paint.setColor(Color.rgb(150, 30, 25));
        paint.setStrokeWidth(3);
        canvas.drawLine(left + w * 0.1f, top, left + w * 0.9f, top, paint);

        // 屋脊两端翘起的鸱吻（简化为小三角）
        paint.setColor(Color.rgb(120, 25, 20));
        Path leftHorn = new Path();
        leftHorn.moveTo(left + w * 0.1f, top);
        leftHorn.lineTo(left + w * 0.1f - 15, top - 25);
        leftHorn.lineTo(left + w * 0.1f + 15, top + 5);
        leftHorn.close();
        canvas.drawPath(leftHorn, paint);
        Path rightHorn = new Path();
        rightHorn.moveTo(left + w * 0.9f, top);
        rightHorn.lineTo(left + w * 0.9f + 15, top - 25);
        rightHorn.lineTo(left + w * 0.9f - 15, top + 5);
        rightHorn.close();
        canvas.drawPath(rightHorn, paint);
        paint.setStrokeWidth(1);
    }

    /**
     * 判官府（四角小殿）
     */
    private void drawJudgeTower(Canvas canvas, Paint paint, float worldX, float worldY,
                                 float cameraX, float cameraY, float t, String label) {
        float sx = worldX - cameraX;
        float sy = worldY - cameraY;
        if (sx < -200 || sx > canvas.getWidth() + 200 || sy < -200 || sy > canvas.getHeight() + 200) return;

        // 台基
        paint.setColor(Color.rgb(40, 32, 50));
        canvas.drawRect(sx - 90, sy + 40, sx + 90, sy + 70, paint);

        // 殿身（玄黑墙 + 血柱）
        paint.setColor(Color.rgb(30, 24, 42));
        canvas.drawRect(sx - 80, sy - 40, sx + 80, sy + 40, paint);
        // 四根血柱
        paint.setColor(Color.rgb(110, 25, 25));
        canvas.drawRect(sx - 78, sy - 40, sx - 68, sy + 40, paint);
        canvas.drawRect(sx + 68, sy - 40, sx + 78, sy + 40, paint);
        canvas.drawRect(sx - 6, sy - 40, sx + 6, sy + 40, paint);

        // 屋顶
        drawRoof(canvas, paint, sx - 100, sy - 90, 200, 60, Color.rgb(20, 15, 30));

        // 顶灯（幽绿发光）
        float glow = (float) (Math.sin(t * 2 + worldX * 0.01) * 0.3 + 0.7);
        paint.setColor(Color.argb((int) (60 * glow), 140, 240, 170));
        canvas.drawCircle(sx, sy - 105, 22, paint);
        paint.setColor(Color.rgb(200, 255, 220));
        canvas.drawCircle(sx, sy - 105, 8, paint);

        // 匾额
        paint.setColor(Color.rgb(15, 10, 20));
        canvas.drawRect(sx - 55, sy - 30, sx + 55, sy + 5, paint);
        paint.setColor(Color.rgb(180, 150, 50));
        canvas.drawRect(sx - 52, sy - 27, sx + 52, sy + 2, paint);
        paint.setColor(Color.rgb(20, 10, 10));
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, sx, sy - 5, paint);
    }

    /**
     * 十八层地狱入口（漩涡状黑洞，位于阎罗殿正后方）
     */
    private void drawHellEntrance(Canvas canvas, Paint paint, float cameraX, float cameraY, float t) {
        float sx = 10000 - cameraX;
        float sy = 7500 - cameraY;
        if (sx < -400 || sx > canvas.getWidth() + 400 || sy < -400 || sy > canvas.getHeight() + 400) return;

        // 多层旋转红黑环
        for (int i = 5; i >= 1; i--) {
            float r = i * 45;
            int alpha = 40 + (5 - i) * 30;
            int red = 100 + (5 - i) * 25;
            paint.setColor(Color.argb(alpha, red, 20, 10));
            canvas.drawCircle(sx, sy, r, paint);
        }
        // 中心黑孔
        paint.setColor(Color.rgb(5, 0, 5));
        canvas.drawCircle(sx, sy, 40, paint);
        // 旋转的红光丝（简单模拟漩涡）
        paint.setStrokeWidth(3);
        for (int i = 0; i < 8; i++) {
            float ang = t * 0.8f + i * (float) (Math.PI / 4);
            float r1 = 50 + (float) Math.sin(t * 2 + i) * 15;
            float r2 = 190;
            paint.setColor(Color.argb(120, 220, 60, 20));
            canvas.drawLine(sx + (float) Math.cos(ang) * r1, sy + (float) Math.sin(ang) * r1,
                    sx + (float) Math.cos(ang + 0.5f) * r2, sy + (float) Math.sin(ang + 0.5f) * r2, paint);
        }
        paint.setStrokeWidth(1);

        // 匾额：十八层地狱
        paint.setColor(Color.rgb(15, 10, 20));
        canvas.drawRect(sx - 110, sy - 260, sx + 110, sy - 210, paint);
        paint.setColor(Color.rgb(180, 40, 30));
        canvas.drawRect(sx - 106, sy - 256, sx + 106, sy - 214, paint);
        paint.setColor(Color.rgb(255, 220, 100));
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("十八层地狱", sx, sy - 226, paint);
    }

    /**
     * 殿前香炉（青铜三足，冒青烟）
     */
    private void drawIncenseBurner(Canvas canvas, Paint paint, float sx, float sy, float t) {
        if (sx < -100 || sx > canvas.getWidth() + 100 || sy < -200 || sy > canvas.getHeight() + 100) return;

        // 炉身（青铜）
        paint.setColor(Color.rgb(90, 110, 90));
        canvas.drawRect(sx - 30, sy - 20, sx + 30, sy + 30, paint);
        // 炉沿
        paint.setColor(Color.rgb(120, 140, 110));
        canvas.drawRect(sx - 35, sy - 25, sx + 35, sy - 18, paint);
        // 三足
        paint.setColor(Color.rgb(70, 90, 70));
        canvas.drawRect(sx - 26, sy + 30, sx - 18, sy + 45, paint);
        canvas.drawRect(sx + 18, sy + 30, sx + 26, sy + 45, paint);
        canvas.drawRect(sx - 4, sy + 30, sx + 4, sy + 45, paint);
        // 香（三根红线）
        paint.setColor(Color.rgb(180, 40, 30));
        canvas.drawRect(sx - 8, sy - 60, sx - 6, sy - 20, paint);
        canvas.drawRect(sx - 1, sy - 65, sx + 1, sy - 20, paint);
        canvas.drawRect(sx + 6, sy - 58, sx + 8, sy - 20, paint);
        // 香头火星
        paint.setColor(Color.argb(220, 255, 160, 60));
        canvas.drawCircle(sx - 7, sy - 60, 2, paint);
        canvas.drawCircle(sx, sy - 65, 2, paint);
        canvas.drawCircle(sx + 7, sy - 58, 2, paint);
        // 青烟（缓慢上升的半透明团）
        for (int i = 0; i < 5; i++) {
            float phase = (t * 0.6f + i * 0.7f) % 3f;
            float rise = phase / 3f;
            float smokeY = sy - 65 - rise * 200;
            float smokeX = sx + (float) Math.sin(t * 0.8 + i) * (10 + rise * 25);
            int alpha = (int) ((1 - rise) * 90);
            if (alpha <= 0) continue;
            paint.setColor(Color.argb(alpha, 180, 190, 180));
            canvas.drawCircle(smokeX, smokeY, 12 + rise * 25, paint);
        }
    }

    // ==================== 鬼门关（南门） ====================

    private void drawGhostGate(Canvas canvas, float cameraX, float cameraY, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int gateCX = (NetherworldMapGenerator.PALACE_X1 + NetherworldMapGenerator.PALACE_X2) / 2;
        float sx = gateCX - cameraX;
        float sy = NetherworldMapGenerator.PALACE_Y2 - cameraY;

        if (sx < -400 || sx > canvas.getWidth() + 400 || sy < -300 || sy > canvas.getHeight() + 300) return;

        float gateW = NetherworldMapGenerator.GATE_WIDTH;
        float gateH = 340;

        // 门框（两根黑铁大柱 + 横梁）
        paint.setColor(Color.rgb(25, 20, 32));
        canvas.drawRect(sx - gateW / 2 - 30, sy - gateH, sx - gateW / 2 + 30, sy, paint);
        canvas.drawRect(sx + gateW / 2 - 30, sy - gateH, sx + gateW / 2 + 30, sy, paint);

        // 柱上骷髅门钉（3 层）
        for (int i = 0; i < 3; i++) {
            float ny = sy - gateH + 50 + i * 90;
            drawSkull(canvas, paint, sx - gateW / 2, ny, 14);
            drawSkull(canvas, paint, sx + gateW / 2, ny, 14);
        }

        // 横梁
        paint.setColor(Color.rgb(30, 24, 38));
        canvas.drawRect(sx - gateW / 2 - 40, sy - gateH - 30, sx + gateW / 2 + 40, sy - gateH, paint);

        // 门额（血色匾）
        paint.setColor(Color.rgb(60, 15, 15));
        canvas.drawRect(sx - 130, sy - gateH - 90, sx + 130, sy - gateH - 30, paint);
        paint.setColor(Color.rgb(180, 40, 30));
        canvas.drawRect(sx - 125, sy - gateH - 85, sx + 125, sy - gateH - 35, paint);
        paint.setColor(Color.rgb(255, 220, 100));
        paint.setTextSize(34);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("鬼门关", sx, sy - gateH - 45, paint);

        // 门顶飞檐（玄黑琉璃）
        paint.setColor(Color.rgb(20, 15, 30));
        Path roof = new Path();
        roof.moveTo(sx - gateW / 2 - 60, sy - gateH - 90);
        roof.lineTo(sx - gateW / 2 - 20, sy - gateH - 130);
        roof.lineTo(sx + gateW / 2 + 20, sy - gateH - 130);
        roof.lineTo(sx + gateW / 2 + 60, sy - gateH - 90);
        roof.close();
        canvas.drawPath(roof, paint);
        // 屋脊血红
        paint.setColor(Color.rgb(150, 30, 25));
        paint.setStrokeWidth(3);
        canvas.drawLine(sx - gateW / 2 - 20, sy - gateH - 130, sx + gateW / 2 + 20, sy - gateH - 130, paint);
        paint.setStrokeWidth(1);

        // 门内幽光（透光感）
        float glow = (float) (Math.sin(t * 1.5) * 0.2 + 0.8);
        paint.setColor(Color.argb((int) (60 * glow), 140, 240, 170));
        canvas.drawRect(sx - gateW / 2 + 30, sy - gateH + 20, sx + gateW / 2 - 30, sy, paint);

        // 门口两侧的石狮（简化为牛头马面石像）
        drawStoneGuardian(canvas, paint, sx - gateW / 2 - 100, sy - 40, true);
        drawStoneGuardian(canvas, paint, sx + gateW / 2 + 100, sy - 40, false);
    }

    /**
     * 门口石像（牛头/马面）
     */
    private void drawStoneGuardian(Canvas canvas, Paint paint, float sx, float sy, boolean isLeft) {
        // 基座
        paint.setColor(Color.rgb(50, 45, 55));
        canvas.drawRect(sx - 35, sy + 20, sx + 35, sy + 45, paint);
        // 身体（石雕灰）
        paint.setColor(Color.rgb(75, 70, 80));
        canvas.drawRect(sx - 28, sy - 40, sx + 28, sy + 20, paint);
        // 头（isLeft=牛头/带角，否则=马面/长脸）
        if (isLeft) {
            // 牛头：宽脸 + 双角
            paint.setColor(Color.rgb(85, 78, 88));
            canvas.drawRect(sx - 25, sy - 75, sx + 25, sy - 40, paint);
            // 角
            paint.setColor(Color.rgb(200, 195, 175));
            Path horn1 = new Path();
            horn1.moveTo(sx - 25, sy - 70);
            horn1.lineTo(sx - 40, sy - 90);
            horn1.lineTo(sx - 20, sy - 78);
            horn1.close();
            canvas.drawPath(horn1, paint);
            Path horn2 = new Path();
            horn2.moveTo(sx + 25, sy - 70);
            horn2.lineTo(sx + 40, sy - 90);
            horn2.lineTo(sx + 20, sy - 78);
            horn2.close();
            canvas.drawPath(horn2, paint);
        } else {
            // 马面：长脸
            paint.setColor(Color.rgb(85, 78, 88));
            canvas.drawRect(sx - 18, sy - 85, sx + 18, sy - 40, paint);
            // 马耳
            paint.setColor(Color.rgb(95, 88, 98));
            Path ear1 = new Path();
            ear1.moveTo(sx - 15, sy - 85);
            ear1.lineTo(sx - 20, sy - 100);
            ear1.lineTo(sx - 5, sy - 88);
            ear1.close();
            canvas.drawPath(ear1, paint);
            Path ear2 = new Path();
            ear2.moveTo(sx + 15, sy - 85);
            ear2.lineTo(sx + 20, sy - 100);
            ear2.lineTo(sx + 5, sy - 88);
            ear2.close();
            canvas.drawPath(ear2, paint);
        }
        // 眼（幽绿发光）
        paint.setColor(Color.argb(220, 140, 240, 170));
        canvas.drawCircle(sx - 8, sy - 58, 3, paint);
        canvas.drawCircle(sx + 8, sy - 58, 3, paint);
    }

    /**
     * 绘制小骷髅（门钉装饰）
     */
    private void drawSkull(Canvas canvas, Paint paint, float cx, float cy, float r) {
        // 颅骨
        paint.setColor(Color.rgb(220, 215, 195));
        canvas.drawCircle(cx, cy, r, paint);
        // 下颌
        canvas.drawRect(cx - r * 0.5f, cy + r * 0.5f, cx + r * 0.5f, cy + r * 1.2f, paint);
        // 眼窝
        paint.setColor(Color.rgb(15, 10, 20));
        canvas.drawCircle(cx - r * 0.35f, cy - r * 0.15f, r * 0.25f, paint);
        canvas.drawCircle(cx + r * 0.35f, cy - r * 0.15f, r * 0.25f, paint);
        // 鼻孔
        canvas.drawCircle(cx, cy + r * 0.25f, r * 0.15f, paint);
    }

    /**
     * 侧门（左右）
     */
    private void drawSideGate(Canvas canvas, int wallX, float cameraX, float cameraY, boolean isEast) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float gateCY = NetherworldMapGenerator.SIDE_GATE_CENTER_Y - cameraY;
        float sx = wallX - cameraX;
        if (sx < -300 || sx > canvas.getWidth() + 300 || gateCY < -300 || gateCY > canvas.getHeight() + 300) return;

        float gateW = NetherworldMapGenerator.SIDE_GATE_WIDTH;
        float halfGate = gateW / 2f;

        // 两根柱子（上下）
        paint.setColor(Color.rgb(25, 20, 32));
        canvas.drawRect(sx - 25, gateCY - halfGate - 20, sx + 25, gateCY - halfGate + 20, paint);
        canvas.drawRect(sx - 25, gateCY + halfGate - 20, sx + 25, gateCY + halfGate + 20, paint);

        // 门楣
        paint.setColor(Color.rgb(30, 24, 38));
        canvas.drawRect(sx - 18, gateCY - halfGate, sx + 18, gateCY + halfGate, paint);
        // 门内幽光
        paint.setColor(Color.argb(80, 140, 240, 170));
        canvas.drawRect(sx - 10, gateCY - halfGate + 25, sx + 10, gateCY + halfGate - 25, paint);

        // 门额
        float plaqueY = gateCY - halfGate - 30;
        paint.setColor(Color.rgb(60, 15, 15));
        canvas.drawRect(sx - 55, plaqueY - 20, sx + 55, plaqueY + 20, paint);
        paint.setColor(Color.rgb(180, 40, 30));
        canvas.drawRect(sx - 52, plaqueY - 17, sx + 52, plaqueY + 17, paint);
        paint.setColor(Color.rgb(255, 220, 100));
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(isEast ? "东幽门" : "西冥门", sx, plaqueY + 6, paint);
    }

    /**
     * 围墙顶部血色琉璃装饰
     */
    private void drawWallDecoration(Canvas canvas, float cameraX, float cameraY, int sw, int sh) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float px1 = NetherworldMapGenerator.PALACE_X1 - cameraX;
        float py1 = NetherworldMapGenerator.PALACE_Y1 - cameraY;
        float px2 = NetherworldMapGenerator.PALACE_X2 - cameraX;
        float py2 = NetherworldMapGenerator.PALACE_Y2 - cameraY;

        paint.setColor(Color.rgb(150, 30, 25));
        paint.setStrokeWidth(4);

        if (py1 > -10 && py1 < sh + 10) {
            canvas.drawLine(Math.max(0, px1), py1, Math.min(sw, px2), py1, paint);
        }
        if (px1 > -10 && px1 < sw + 10) {
            canvas.drawLine(px1, Math.max(0, py1), px1, Math.min(sh, py2), paint);
        }
        if (px2 > -10 && px2 < sw + 10) {
            canvas.drawLine(px2, Math.max(0, py1), px2, Math.min(sh, py2), paint);
        }
        paint.setStrokeWidth(1);
    }

    // ==================== 奈何桥 ====================

    private void drawNaiheBridge(Canvas canvas, float cameraX, float cameraY,
                                  int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float bridgeCX = NetherworldMapGenerator.BRIDGE_CENTER_X - cameraX;
        float bridgeW = NetherworldMapGenerator.BRIDGE_WIDTH;
        float yStart = NetherworldMapGenerator.PALACE_Y2 - cameraY;
        float yEnd = yStart + 3200;

        if (bridgeCX + bridgeW / 2 < 0 || bridgeCX - bridgeW / 2 > sw) return;
        if (yEnd < 0 || yStart > sh) return;

        // 桥面石栏杆（两侧）
        paint.setColor(Color.rgb(140, 132, 138));
        canvas.drawRect(bridgeCX - bridgeW / 2 - 20, yStart, bridgeCX - bridgeW / 2 + 10, yEnd, paint);
        canvas.drawRect(bridgeCX + bridgeW / 2 - 10, yStart, bridgeCX + bridgeW / 2 + 20, yEnd, paint);

        // 栏杆柱（每 200px 一根）
        paint.setColor(Color.rgb(170, 162, 168));
        for (float y = yStart; y < yEnd; y += 200) {
            canvas.drawRect(bridgeCX - bridgeW / 2 - 25, y - 15, bridgeCX - bridgeW / 2 + 15, y + 15, paint);
            canvas.drawRect(bridgeCX + bridgeW / 2 - 15, y - 15, bridgeCX + bridgeW / 2 + 25, y + 15, paint);
        }

        // 桥面石板缝
        paint.setColor(Color.argb(120, 60, 55, 65));
        paint.setStrokeWidth(1);
        for (float y = yStart; y < yEnd; y += 80) {
            canvas.drawLine(bridgeCX - bridgeW / 2 + 10, y, bridgeCX + bridgeW / 2 - 10, y, paint);
        }

        // 桥上灯笼（每 400px 一盏，红色幽光）
        for (float y = yStart + 200; y < yEnd; y += 400) {
            drawLantern(canvas, paint, bridgeCX - bridgeW / 2 - 40, y, t);
            drawLantern(canvas, paint, bridgeCX + bridgeW / 2 + 40, y, t);
        }

        // 桥头石碑（孟婆亭提示）
        float steleY = yEnd - 200;
        paint.setColor(Color.rgb(70, 65, 75));
        canvas.drawRect(bridgeCX + bridgeW / 2 + 60, steleY - 60, bridgeCX + bridgeW / 2 + 130, steleY + 60, paint);
        paint.setColor(Color.rgb(180, 40, 30));
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("奈", bridgeCX + bridgeW / 2 + 95, steleY - 25, paint);
        canvas.drawText("何", bridgeCX + bridgeW / 2 + 95, steleY + 5, paint);
        canvas.drawText("桥", bridgeCX + bridgeW / 2 + 95, steleY + 35, paint);
    }

    /**
     * 红灯笼（发光的圆形 + 穗子）
     */
    private void drawLantern(Canvas canvas, Paint paint, float sx, float sy, float t) {
        float glow = (float) (Math.sin(t * 2 + sx * 0.01) * 0.2 + 0.8);
        // 光晕
        paint.setColor(Color.argb((int) (60 * glow), 220, 60, 40));
        canvas.drawCircle(sx, sy, 32, paint);
        // 灯笼主体
        paint.setColor(Color.rgb(180, 40, 30));
        canvas.drawOval(sx - 18, sy - 22, sx + 18, sy + 22, paint);
        // 灯笼骨
        paint.setColor(Color.rgb(60, 15, 10));
        canvas.drawRect(sx - 20, sy - 3, sx + 20, sy + 3, paint);
        // 顶盖和底
        paint.setColor(Color.rgb(180, 150, 50));
        canvas.drawRect(sx - 8, sy - 26, sx + 8, sy - 22, paint);
        canvas.drawRect(sx - 8, sy + 22, sx + 8, sy + 26, paint);
        // 穗子
        paint.setColor(Color.rgb(200, 180, 60));
        canvas.drawLine(sx, sy + 26, sx, sy + 40, paint);
        canvas.drawCircle(sx, sy + 42, 3, paint);
        // 中心亮点
        paint.setColor(Color.argb((int) (200 * glow), 255, 200, 100));
        canvas.drawCircle(sx, sy, 6, paint);
    }

    // ==================== 彼岸花丛 ====================

    private void drawLilyClusters(Canvas canvas, float cameraX, float cameraY,
                                   int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (LilyCluster cluster : lilies) {
            float sx = cluster.x - cameraX;
            float sy = cluster.y - cameraY;
            if (sx < -100 || sx > sw + 100 || sy < -100 || sy > sh + 100) continue;

            // 每朵花独立位置（用 cluster.x 做伪随机种子）
            Random rng = new Random((long) (cluster.x * 100 + cluster.y));
            for (int i = 0; i < cluster.flowerCount; i++) {
                float fx = sx + (rng.nextFloat() - 0.5f) * cluster.size * 2;
                float fy = sy + (rng.nextFloat() - 0.5f) * cluster.size * 2;
                // 花茎
                float stemH = 20 + rng.nextFloat() * 15;
                // 风吹摇摆
                float sway = (float) Math.sin(t * 0.8 + fx * 0.01) * 3;
                paint.setColor(Color.rgb(80, 100, 55));
                paint.setStrokeWidth(2);
                canvas.drawLine(fx, fy, fx + sway, fy - stemH, paint);

                // 花朵（6 瓣细长血红花瓣，蜘蛛百合造型）
                float flowerY = fy - stemH;
                float flowerX = fx + sway;
                int shade = rng.nextInt(40);
                paint.setColor(Color.rgb(210 + shade / 3, 30 + shade / 2, 40));
                paint.setStrokeWidth(2);
                for (int p = 0; p < 6; p++) {
                    float ang = (float) (p * Math.PI / 3) + t * 0.1f;
                    float petalLen = 10 + rng.nextFloat() * 4;
                    float px = flowerX + (float) Math.cos(ang) * petalLen;
                    float py = flowerY + (float) Math.sin(ang) * petalLen;
                    canvas.drawLine(flowerX, flowerY, px, py, paint);
                }
                // 花心（金黄亮点）
                paint.setColor(Color.argb(230, 255, 200, 100));
                canvas.drawCircle(flowerX, flowerY, 2.5f, paint);
            }
            paint.setStrokeWidth(1);
        }
    }

    // ==================== 白骨装饰 ====================

    private void drawBoneDecos(Canvas canvas, float cameraX, float cameraY, int sw, int sh) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (BoneDeco b : bones) {
            float sx = b.x - cameraX;
            float sy = b.y - cameraY;
            if (sx < -100 || sx > sw + 100 || sy < -100 || sy > sh + 100) continue;

            switch (b.type) {
                case 0: drawSkullPile(canvas, paint, sx, sy, b.size); break;
                case 1: drawCrossBones(canvas, paint, sx, sy, b.size); break;
                case 2: drawRibCage(canvas, paint, sx, sy, b.size); break;
                case 3: drawBrokenStele(canvas, paint, sx, sy, b.size); break;
            }
        }
    }

    private void drawSkullPile(Canvas canvas, Paint paint, float sx, float sy, float size) {
        // 3~5 个头骨堆
        Random rng = new Random((long) (sx * 100 + sy));
        int count = 3 + rng.nextInt(3);
        for (int i = 0; i < count; i++) {
            float ox = (rng.nextFloat() - 0.5f) * size;
            float oy = (rng.nextFloat() - 0.5f) * size * 0.5f;
            float r = size * (0.2f + rng.nextFloat() * 0.15f);
            drawSkull(canvas, paint, sx + ox, sy + oy, r);
        }
    }

    private void drawCrossBones(Canvas canvas, Paint paint, float sx, float sy, float size) {
        // 两根长骨交叉
        paint.setColor(Color.rgb(225, 220, 200));
        paint.setStrokeWidth(size * 0.12f);
        canvas.drawLine(sx - size * 0.6f, sy - size * 0.4f, sx + size * 0.6f, sy + size * 0.4f, paint);
        canvas.drawLine(sx - size * 0.6f, sy + size * 0.4f, sx + size * 0.6f, sy - size * 0.4f, paint);
        // 骨端
        paint.setColor(Color.rgb(240, 235, 215));
        float r = size * 0.1f;
        canvas.drawCircle(sx - size * 0.6f, sy - size * 0.4f, r, paint);
        canvas.drawCircle(sx + size * 0.6f, sy - size * 0.4f, r, paint);
        canvas.drawCircle(sx - size * 0.6f, sy + size * 0.4f, r, paint);
        canvas.drawCircle(sx + size * 0.6f, sy + size * 0.4f, r, paint);
        paint.setStrokeWidth(1);
    }

    private void drawRibCage(Canvas canvas, Paint paint, float sx, float sy, float size) {
        // 肋骨（半椭圆弧）
        paint.setColor(Color.rgb(220, 215, 195));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * 0.06f);
        for (int i = 0; i < 5; i++) {
            float rr = size * (0.3f + i * 0.12f);
            RectF oval = new RectF(sx - rr, sy - rr * 0.5f, sx + rr, sy + rr * 0.5f);
            canvas.drawArc(oval, 200, 140, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBrokenStele(Canvas canvas, Paint paint, float sx, float sy, float size) {
        // 断碑（灰白石碑，顶部残缺）
        paint.setColor(Color.rgb(90, 85, 95));
        Path stele = new Path();
        stele.moveTo(sx - size * 0.3f, sy + size * 0.5f);
        stele.lineTo(sx - size * 0.3f, sy - size * 0.4f);
        stele.lineTo(sx - size * 0.1f, sy - size * 0.5f);
        stele.lineTo(sx + size * 0.15f, sy - size * 0.35f);
        stele.lineTo(sx + size * 0.3f, sy - size * 0.2f);
        stele.lineTo(sx + size * 0.3f, sy + size * 0.5f);
        stele.close();
        canvas.drawPath(stele, paint);
        // 苔痕
        paint.setColor(Color.argb(120, 60, 90, 50));
        canvas.drawCircle(sx - size * 0.1f, sy, size * 0.15f, paint);
        // 碑上残字
        paint.setColor(Color.argb(200, 40, 30, 30));
        paint.setTextSize(size * 0.25f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("冥", sx, sy - size * 0.05f, paint);
    }

    // ==================== 地狱深渊红光 ====================

    private void drawHellGlow(Canvas canvas, float cameraX, float cameraY,
                               int sw, int sh, float t) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 只在地图边缘（>0.86）绘制红色光晕
        // 简化：在地图四边各画一条渐变红光
        float centerX = 10000 - cameraX;
        float centerY = 10000 - cameraY;
        float innerR = 0.86f * 14142;

        // 用一个大 radial gradient 从中心透明到边缘红
        float glow = (float) (Math.sin(t * 0.8) * 0.15 + 0.85);
        RadialGradient grad = new RadialGradient(centerX, centerY, innerR,
                Color.argb(0, 200, 60, 20),
                Color.argb((int) (100 * glow), 220, 60, 20),
                Shader.TileMode.CLAMP);
        paint.setShader(grad);
        // 只绘制外圈范围
        canvas.drawRect(centerX - 15000, centerY - 15000, centerX + 15000, centerY + 15000, paint);
        paint.setShader(null);
    }

    // ==================== 鬼火（漂浮光点） ====================

    private void drawWisps(Canvas canvas, float cameraX, float cameraY,
                            int sw, int sh, float t, float dt) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (Wisp w : wisps) {
            // 布朗运动漂移（离锚点越远，回归力越强）
            w.driftTimer += dt;
            if (w.driftTimer > 1.5f) {
                w.driftTimer = 0;
                w.driftAngle = (float) (Math.random() * Math.PI * 2);
            }
            w.x += (float) Math.cos(w.driftAngle) * w.driftSpeed * dt;
            w.y += (float) Math.sin(w.driftAngle) * w.driftSpeed * dt;
            // 回归锚点
            float backX = (w.baseX - w.x) * 0.5f * dt;
            float backY = (w.baseY - w.y) * 0.5f * dt;
            w.x += backX;
            w.y += backY;

            float sx = w.x - cameraX;
            float sy = w.y - cameraY;
            if (sx < -80 || sx > sw + 80 || sy < -80 || sy > sh + 80) continue;

            // 呼吸明暗
            float pulse = (float) (Math.sin(t * 2 + w.phase) * 0.35 + 0.65);
            int[] c = WISP_COLORS[w.colorType];

            // 外层光晕
            paint.setColor(Color.argb((int) (40 * pulse), c[0], c[1], c[2]));
            canvas.drawCircle(sx, sy, w.radius * 2, paint);
            // 中层光晕
            paint.setColor(Color.argb((int) (100 * pulse), c[0], c[1], c[2]));
            canvas.drawCircle(sx, sy, w.radius, paint);
            // 内核亮点
            paint.setColor(Color.argb((int) (220 * pulse),
                    Math.min(255, c[0] + 60),
                    Math.min(255, c[1] + 60),
                    Math.min(255, c[2] + 60)));
            canvas.drawCircle(sx, sy, w.radius * 0.35f, paint);
        }
    }

    // ==================== 冥雾 ====================

    private void drawMists(Canvas canvas, float cameraX, float cameraY,
                            int sw, int sh, float t, float dt) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (MistPatch m : mists) {
            // 缓慢正弦漂移
            m.x = m.baseX + (float) Math.sin(t * 0.1 + m.phase) * m.driftSpeed * 15;
            m.y = m.baseY + (float) Math.cos(t * 0.08 + m.phase) * m.driftSpeed * 10;

            float sx = m.x - cameraX;
            float sy = m.y - cameraY;
            if (sx < -m.size * 2 || sx > sw + m.size * 2
                || sy < -m.size * 2 || sy > sh + m.size * 2) continue;

            // 变形（大小脉动）
            float deform = (float) (Math.sin(t * 0.4 + m.phase) * 0.15 + 1);
            float sizeX = m.size * deform;
            float sizeY = m.size * 0.6f / deform;

            // 双层雾（外淡内稍浓）
            paint.setColor(Color.argb(28, 90, 70, 110));
            canvas.drawOval(sx - sizeX, sy - sizeY, sx + sizeX, sy + sizeY, paint);
            paint.setColor(Color.argb(20, 130, 110, 150));
            canvas.drawOval(sx - sizeX * 0.6f, sy - sizeY * 0.6f, sx + sizeX * 0.6f, sy + sizeY * 0.6f, paint);
        }
    }

    // ==================== 纸灰（下降粒子） ====================

    private void drawAshes(Canvas canvas, float cameraX, float cameraY,
                            int sw, int sh, float dt) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 生成新的纸灰
        ashTimer += dt;
        while (ashTimer >= ASH_INTERVAL) {
            ashTimer -= ASH_INTERVAL;
            if (ashes.size() < 60) {
                float ax = cameraX + (float) (Math.random() * sw);
                float ay = cameraY - 20 - (float) (Math.random() * 60);
                ashes.add(new Ash(ax, ay));
            }
        }

        Iterator<Ash> it = ashes.iterator();
        while (it.hasNext()) {
            Ash a = it.next();
            a.y += a.speed * dt;
            a.wobblePhase += dt * a.wobbleFreq * 3;
            a.x += (float) Math.sin(a.wobblePhase) * 0.6f;

            // 超出屏幕下方移除
            if (a.y - cameraY > sh + 40) {
                it.remove();
                continue;
            }

            float sx = a.x - cameraX;
            float sy = a.y - cameraY;
            if (sx < -20 || sx > sw + 20) continue;

            paint.setColor(Color.argb(180, 210, 205, 190));
            canvas.drawCircle(sx, sy, a.size, paint);
            paint.setColor(Color.argb(80, 255, 250, 240));
            canvas.drawCircle(sx - a.size * 0.3f, sy - a.size * 0.3f, a.size * 0.4f, paint);
        }
    }

    // ==================== 幽魂牢 ====================

    /**
     * 绘制幽魂牢：封闭囚室墙体 + 匾额 + 铁链 + 角落鬼火盆 + 内部白骨
     */
    private void drawPrison(Canvas canvas, float cameraX, float cameraY, int sw, int sh, float t) {
        float px1 = NetherworldMapGenerator.PRISON_X1;
        float py1 = NetherworldMapGenerator.PRISON_Y1;
        float px2 = NetherworldMapGenerator.PRISON_X2;
        float py2 = NetherworldMapGenerator.PRISON_Y2;

        // 视锥剔除
        if (px2 - cameraX < -200 || px1 - cameraX > sw + 200
                || py2 - cameraY < -200 || py1 - cameraY > sh + 200) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float wallT = NetherworldMapGenerator.PRISON_WALL_THICKNESS;

        // 1. 墙体（深黑石砖，比鬼门关墙更暗）
        paint.setColor(Color.rgb(22, 18, 28));
        // 北墙
        canvas.drawRect(px1 - cameraX, py1 - cameraY, px2 - cameraX, py1 + wallT - cameraY, paint);
        // 南墙
        canvas.drawRect(px1 - cameraX, py2 - wallT - cameraY, px2 - cameraX, py2 - cameraY, paint);
        // 西墙
        canvas.drawRect(px1 - cameraX, py1 - cameraY, px1 + wallT - cameraX, py2 - cameraY, paint);
        // 东墙
        canvas.drawRect(px2 - wallT - cameraX, py1 - cameraY, px2 - cameraX, py2 - cameraY, paint);

        // 2. 墙体砖缝（水平线）
        paint.setColor(Color.argb(90, 60, 50, 70));
        paint.setStrokeWidth(1);
        for (float y = py1 + 20; y < py2; y += 40) {
            // 北墙
            if (y < py1 + wallT) {
                canvas.drawLine(px1 - cameraX, y - cameraY, px2 - cameraX, y - cameraY, paint);
            }
            // 南墙
            if (y > py2 - wallT) {
                canvas.drawLine(px1 - cameraX, y - cameraY, px2 - cameraX, y - cameraY, paint);
            }
        }
        for (float x = px1 + 20; x < px2; x += 40) {
            // 西墙
            if (x < px1 + wallT) {
                canvas.drawLine(x - cameraX, py1 - cameraY, x - cameraX, py2 - cameraY, paint);
            }
            // 东墙
            if (x > px2 - wallT) {
                canvas.drawLine(x - cameraX, py1 - cameraY, x - cameraX, py2 - cameraY, paint);
            }
        }
        paint.setStrokeWidth(1);

        // 3. 墙顶血色琉璃（与阎罗殿围墙同风格）
        paint.setColor(Color.rgb(110, 25, 25));
        canvas.drawRect(px1 - cameraX, py1 - cameraY - 8, px2 - cameraX, py1 - cameraY, paint);
        canvas.drawRect(px1 - cameraX, py2 - cameraY, px2 - cameraX, py2 - cameraY + 8, paint);
        canvas.drawRect(px1 - cameraX - 8, py1 - cameraY, px1 - cameraX, py2 - cameraY, paint);
        canvas.drawRect(px2 - cameraX, py1 - cameraY, px2 - cameraX + 8, py2 - cameraY, paint);

        // 4. 匾额"幽魂牢"（挂在北墙内侧）
        float signCX = (px1 + px2) / 2 - cameraX;
        float signCY = py1 + wallT + 40 - cameraY;
        // 匾框
        paint.setColor(Color.rgb(40, 12, 12));
        canvas.drawRect(signCX - 110, signCY - 32, signCX + 110, signCY + 32, paint);
        paint.setColor(Color.rgb(130, 30, 25));
        canvas.drawRect(signCX - 104, signCY - 26, signCX + 104, signCY + 26, paint);
        // 字
        paint.setColor(Color.rgb(255, 210, 90));
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("幽魂牢", signCX, signCY + 10, paint);
        paint.setFakeBoldText(false);

        // 5. 铁链（从北墙垂下，4 根）
        paint.setColor(Color.rgb(70, 65, 75));
        paint.setStrokeWidth(3);
        for (int i = 0; i < 4; i++) {
            float cx = px1 + 200 + i * ((px2 - px1 - 400) / 3f) - cameraX;
            float topY = py1 + wallT - cameraY;
            float len = 120 + (float) Math.sin(t * 0.8 + i) * 15;
            // 链环（小椭圆串联）
            for (float cy = topY; cy < topY + len; cy += 14) {
                canvas.drawOval(cx - 4, cy, cx + 4, cy + 10, paint);
            }
        }
        paint.setStrokeWidth(1);

        // 6. 四角鬼火盆（幽绿火焰）
        float inset = wallT + 60;
        drawBrazier(canvas, paint, px1 + inset - cameraX, py1 + inset - cameraY, t, 0);
        drawBrazier(canvas, paint, px2 - inset - cameraX, py1 + inset - cameraY, t, 1);
        drawBrazier(canvas, paint, px1 + inset - cameraX, py2 - inset - cameraY, t, 2);
        drawBrazier(canvas, paint, px2 - inset - cameraX, py2 - inset - cameraY, t, 3);

        // 7. 地面散落白骨（牢内）
        paint.setColor(Color.argb(180, 200, 195, 175));
        float[][] boneSpots = {
            {0.30f, 0.55f}, {0.65f, 0.40f}, {0.45f, 0.75f},
            {0.72f, 0.68f}, {0.25f, 0.30f}, {0.55f, 0.25f}
        };
        for (float[] spot : boneSpots) {
            float bx = px1 + (px2 - px1) * spot[0] - cameraX;
            float by = py1 + (py2 - py1) * spot[1] - cameraY;
            // 小颅骨
            canvas.drawCircle(bx, by, 6, paint);
            paint.setColor(Color.rgb(20, 15, 25));
            canvas.drawCircle(bx - 2, by - 1, 1.5f, paint);
            canvas.drawCircle(bx + 2, by - 1, 1.5f, paint);
            paint.setColor(Color.argb(180, 200, 195, 175));
            // 交叉骨
            paint.setStrokeWidth(2);
            canvas.drawLine(bx + 10, by - 4, bx + 22, by + 4, paint);
            canvas.drawLine(bx + 10, by + 4, bx + 22, by - 4, paint);
            paint.setStrokeWidth(1);
        }

        // 8. 牢内暗角（比全局滤镜更深，强调封闭感）
        float cx = (px1 + px2) / 2 - cameraX;
        float cy = (py1 + py2) / 2 - cameraY;
        float rw = (px2 - px1) / 2f;
        float rh = (py2 - py1) / 2f;
        float radius = (float) Math.sqrt(rw * rw + rh * rh);
        RadialGradient prisonVignette = new RadialGradient(cx, cy, radius * 0.35f,
                Color.argb(0, 0, 0, 0),
                Color.argb(110, 5, 0, 15),
                Shader.TileMode.CLAMP);
        paint.setShader(prisonVignette);
        canvas.drawRect(px1 - cameraX, py1 - cameraY, px2 - cameraX, py2 - cameraY, paint);
        paint.setShader(null);
    }

    /**
     * 鬼火盆（青铜盆 + 幽绿火焰，随时间摇曳）
     */
    private void drawBrazier(Canvas canvas, Paint paint, float sx, float sy, float t, int idx) {
        // 盆体
        paint.setColor(Color.rgb(55, 70, 60));
        canvas.drawRect(sx - 18, sy, sx + 18, sy + 22, paint);
        paint.setColor(Color.rgb(40, 55, 48));
        canvas.drawRect(sx - 22, sy - 4, sx + 22, sy + 4, paint);
        // 盆脚
        paint.setColor(Color.rgb(35, 45, 40));
        canvas.drawRect(sx - 14, sy + 22, sx - 8, sy + 32, paint);
        canvas.drawRect(sx + 8, sy + 22, sx + 14, sy + 32, paint);

        // 火焰（多层椭圆，幽绿→惨白）
        float flicker = (float) (Math.sin(t * 3.5 + idx * 1.7) * 0.25 + 0.75);
        float fh = 38 * flicker;
        // 外焰（深绿）
        paint.setColor(Color.argb(140, 60, 200, 120));
        canvas.drawOval(sx - 14, sy - fh, sx + 14, sy + 4, paint);
        // 中焰（亮绿）
        paint.setColor(Color.argb(180, 120, 240, 160));
        canvas.drawOval(sx - 9, sy - fh * 0.75f, sx + 9, sy + 2, paint);
        // 内焰（惨白）
        paint.setColor(Color.argb(200, 220, 255, 230));
        canvas.drawOval(sx - 4, sy - fh * 0.45f, sx + 4, sy, paint);

        // 光晕
        RadialGradient glow = new RadialGradient(sx, sy - fh * 0.4f, 60,
                Color.argb((int) (60 * flicker), 100, 220, 150),
                Color.argb(0, 100, 220, 150),
                Shader.TileMode.CLAMP);
        paint.setShader(glow);
        canvas.drawCircle(sx, sy - fh * 0.4f, 60, paint);
        paint.setShader(null);
    }

    // ==================== 幽冥滤镜 ====================

    private void drawNetherOverlay(Canvas canvas, int sw, int sh) {
        Paint paint = new Paint();

        // 紫黑全局遮罩
        paint.setColor(Color.argb(60, 30, 10, 45));
        canvas.drawRect(0, 0, sw, sh, paint);

        // 暗角（RadialGradient 中心透明 → 边缘深黑）
        float cx = sw / 2f;
        float cy = sh / 2f;
        float radius = (float) Math.sqrt(cx * cx + cy * cy);
        RadialGradient vignette = new RadialGradient(cx, cy, radius * 0.4f,
                Color.argb(0, 0, 0, 0),
                Color.argb(140, 5, 0, 10),
                Shader.TileMode.CLAMP);
        paint.setShader(vignette);
        canvas.drawRect(0, 0, sw, sh, paint);
        paint.setShader(null);
    }

    // ==================== 碰撞与安全区 ====================

    public List<Rect> getObstacles() {
        return palaceObstacles;
    }

    /**
     * 获取阎罗殿安全区
     */
    public Rect getPalaceSafeZone() {
        return new Rect(
                NetherworldMapGenerator.PALACE_X1 - 100,
                NetherworldMapGenerator.PALACE_Y1 - 100,
                NetherworldMapGenerator.PALACE_X2 + 100,
                NetherworldMapGenerator.PALACE_Y2 + 100
        );
    }

    public void cleanup() {
        wisps.clear();
        mists.clear();
        ashes.clear();
        bones.clear();
        lilies.clear();
    }
}

package com.game.dream.npc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.game.dream.enums.NpcType;

public class AnimalNpc extends Npc{

    public AnimalNpc(int id, String name, NpcType type, float x, float y) {
        super(id, name, type, x, y);

        // 根据类型设置不同的大小
        if (type == NpcType.COW || type == NpcType.HORSE) {
            this.size = 90; // 大型动物
        } else if (type == NpcType.SHEEP) {
            this.size = 65; // 中型动物
        } else if (type == NpcType.DOG) {
            this.size = 55; // 中型动物
        } else if (type == NpcType.CHICKEN || type == NpcType.DUCK) {
            this.size = 35; // 小型家禽
        } else {
            this.size = 70; // 标准人类
        }
    }

    /**
     * 绘制动物 NPC - 采用真实的动物形态而非人形
     */
    public void draw(Canvas canvas, float cameraX, float cameraY) {
        paint.setAntiAlias(true);
        float cx = x + cameraX + size / 2f;
        float cy = y + cameraY + size / 2f;

        // 1. 阴影
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawOval(cx - size * 0.5f, cy + size * 0.2f, cx + size * 0.5f, cy + size * 0.8f, paint);

        // 2. 根据动物类型绘制完整的身体形态
        drawAnimalBody(canvas, paint, cx, cy);

        // 3. 名字 (绘制在脚下)
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(3, 0, 0, Color.BLACK);
        canvas.drawText(name, cx, cy + size * 1.2f, paint);
        paint.clearShadowLayer();

        // 4. 任务提示
        if (hasQuest && !isInteracting) {
            paint.setColor(Color.YELLOW);
            paint.setTextSize(35);
            canvas.drawText("!", cx, cy - size * 1.2f, paint);
        }
    }

    /**
     * 绘制动物的完整身体形态（包含头、身体、四肢等）
     */
    private void drawAnimalBody(Canvas canvas, Paint paint, float cx, float cy) {
        switch (type) {
            case CHICKEN:
                drawChicken(canvas, paint, cx, cy);
                break;
            case DUCK:
                drawDuck(canvas, paint, cx, cy);
                break;
            case DOG:
                drawDog(canvas, paint, cx, cy);
                break;
            case SHEEP:
                drawSheep(canvas, paint, cx, cy);
                break;
            case COW:
                drawCow(canvas, paint, cx, cy);
                break;
            case HORSE:
                drawHorse(canvas, paint, cx, cy);
                break;
        }
    }

    /**
     * 绘制公鸡 - 椭圆形身体 + 鸡冠 + 尖嘴 + 爪子
     */
    private void drawChicken(Canvas canvas, Paint paint, float cx, float cy) {
        float bodyW = size * 0.7f;
        float bodyH = size * 0.8f;

        // 身体（白色椭圆）
        paint.setColor(Color.WHITE);
        canvas.drawOval(cx - bodyW/2, cy - bodyH/2, cx + bodyW/2, cy + bodyH/2, paint);

        // 头部（小圆圈）
        float headR = size * 0.3f;
        canvas.drawCircle(cx, cy - bodyH/2 - headR/2, headR, paint);

        // 鸡冠（红色锯齿状）
        paint.setColor(Color.RED);
        android.graphics.Path comb = new android.graphics.Path();
        comb.moveTo(cx - headR/2, cy - bodyH/2 - headR);
        comb.lineTo(cx - headR/4, cy - bodyH/2 - headR * 1.5f);
        comb.lineTo(cx, cy - bodyH/2 - headR);
        comb.lineTo(cx + headR/4, cy - bodyH/2 - headR * 1.5f);
        comb.lineTo(cx + headR/2, cy - bodyH/2 - headR);
        canvas.drawPath(comb, paint);

        // 眼睛（小黑点）
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx - headR/3, cy - bodyH/2 - headR/2, 2, paint);
        canvas.drawCircle(cx + headR/3, cy - bodyH/2 - headR/2, 2, paint);

        // 嘴巴（橙色三角形）
        paint.setColor(Color.rgb(255, 165, 0));
        android.graphics.Path beak = new android.graphics.Path();
        beak.moveTo(cx - 3, cy - bodyH/2);
        beak.lineTo(cx + 3, cy - bodyH/2);
        beak.lineTo(cx, cy - bodyH/2 + 8);
        canvas.drawPath(beak, paint);

        // 爪子（两条线）
        paint.setStrokeWidth(2);
        canvas.drawLine(cx - 5, cy + bodyH/2, cx - 10, cy + bodyH/2 + 8, paint);
        canvas.drawLine(cx + 5, cy + bodyH/2, cx + 10, cy + bodyH/2 + 8, paint);

        // 尾巴（几根羽毛）
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        canvas.drawLine(cx + bodyW/2, cy - 5, cx + bodyW/2 + 10, cy - 15, paint);
        canvas.drawLine(cx + bodyW/2, cy, cx + bodyW/2 + 12, cy - 8, paint);
    }

    /**
     * 绘制鸭子 - 黄色椭圆身体 + 扁嘴
     */
    private void drawDuck(Canvas canvas, Paint paint, float cx, float cy) {
        float bodyW = size * 0.8f;
        float bodyH = size * 0.7f;

        // 身体（黄色椭圆）
        paint.setColor(Color.YELLOW);
        canvas.drawOval(cx - bodyW/2, cy - bodyH/2, cx + bodyW/2, cy + bodyH/2, paint);

        // 头部
        float headR = size * 0.3f;
        canvas.drawCircle(cx, cy - bodyH/2 - headR/3, headR, paint);

        // 扁嘴（橙色椭圆）
        paint.setColor(Color.rgb(255, 165, 0));
        canvas.drawOval(cx - 8, cy - bodyH/2, cx + 8, cy - bodyH/2 + 6, paint);

        // 眼睛
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx - headR/3, cy - bodyH/2 - headR/3, 2, paint);
        canvas.drawCircle(cx + headR/3, cy - bodyH/2 - headR/3, 2, paint);

        // 脚蹼（橙色）
        paint.setStrokeWidth(3);
        canvas.drawLine(cx - 6, cy + bodyH/2, cx - 12, cy + bodyH/2 + 6, paint);
        canvas.drawLine(cx + 6, cy + bodyH/2, cx + 12, cy + bodyH/2 + 6, paint);
    }

    /**
     * 绘制狗 - 四足站立姿态
     */
    private void drawDog(Canvas canvas, Paint paint, float cx, float cy) {
        float bodyW = size * 0.8f;
        float bodyH = size * 0.6f;

        // 身体（黄褐色圆角矩形，横向）
        paint.setColor(Color.rgb(210, 180, 140));
        canvas.drawRoundRect(cx - bodyW/2, cy - bodyH/2, cx + bodyW/2, cy + bodyH/3, 10, 10, paint);

        // 头部（圆形）
        float headR = size * 0.3f;
        canvas.drawCircle(cx - bodyW/2 - headR/3, cy - bodyH/2, headR, paint);

        // 耳朵（下垂的三角形）
        paint.setColor(Color.rgb(180, 140, 100));
        drawTriangle(canvas, cx - bodyW/2 - headR, cy - bodyH/2 - headR/2,
                cx - bodyW/2 - headR + 5, cy - bodyH/2 + 5,
                cx - bodyW/2 - headR + 10, cy - bodyH/2 - headR/2, paint);

        // 眼睛
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx - bodyW/2 - headR/2, cy - bodyH/2 - 2, 3, paint);

        // 鼻子
        canvas.drawOval(cx - bodyW/2 - headR - 3, cy - bodyH/2 + 2,
                cx - bodyW/2 - headR + 3, cy - bodyH/2 + 6, paint);

        // 四条腿
        paint.setColor(Color.rgb(210, 180, 140));
        canvas.drawRect(cx - bodyW/2 - 5, cy + bodyH/3, cx - bodyW/2 + 5, cy + bodyH/2 + 8, paint);
        canvas.drawRect(cx - bodyW/4, cy + bodyH/3, cx - bodyW/4 + 8, cy + bodyH/2 + 8, paint);
        canvas.drawRect(cx + bodyW/4 - 5, cy + bodyH/3, cx + bodyW/4 + 5, cy + bodyH/2 + 8, paint);
        canvas.drawRect(cx + bodyW/2 - 10, cy + bodyH/3, cx + bodyW/2, cy + bodyH/2 + 8, paint);

        // 尾巴（翘起）
        paint.setStrokeWidth(4);
        canvas.drawLine(cx + bodyW/2, cy - bodyH/4, cx + bodyW/2 + 10, cy - bodyH/2, paint);
    }

    /**
     * 绘制羊 - 蓬松的白色卷毛，体型调整为比狗更大
     */
    private void drawSheep(Canvas canvas, Paint paint, float cx, float cy) {
        // 身体主体（白色椭圆）- 增加宽度和高度以匹配新的 size
        float bodyW = size * 0.7f;
        float bodyH = size * 0.5f;

        paint.setColor(Color.WHITE);
        canvas.drawOval(cx - bodyW/2, cy - bodyH/2, cx + bodyW/2, cy + bodyH/2, paint);

        // 增加前后的蓬松感（用较大的圆圈修饰边缘，体现厚羊毛）
        canvas.drawCircle(cx - bodyW/2 + 8, cy, bodyH * 0.7f, paint);
        canvas.drawCircle(cx + bodyW/2 - 8, cy, bodyH * 0.7f, paint);
        // 顶部再加一团毛，显得更高大
        canvas.drawCircle(cx, cy - bodyH/2 + 5, bodyH * 0.6f, paint);

        // 头部（黑色或灰色）
        paint.setColor(Color.rgb(50, 50, 50));
        float headR = size * 0.23f; // 头部也相应调大
        float headX = cx - bodyW/2 - headR/2;
        float headY = cy - bodyH/4;
        canvas.drawCircle(headX, headY, headR, paint);

        // 羊角
        paint.setColor(Color.rgb(139, 69, 19));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3); // 加粗羊角
        canvas.drawArc(headX - headR - 5, headY - headR - 3,
                headX - headR + 7, headY - headR + 7, 0, 180, false, paint);
        canvas.drawArc(headX + headR - 10, headY - headR - 3,
                headX + headR - 2, headY - headR + 7, 0, 180, false, paint);
        paint.setStyle(Paint.Style.FILL);

        // 眼睛
        paint.setColor(Color.WHITE);
        canvas.drawCircle(headX - 4, headY - 2, 2, paint);

        // 四条腿（黑色，稍微拉长并加粗）
        paint.setColor(Color.BLACK);
        float legH = size * 0.28f;
        float legW = size * 0.08f;
        canvas.drawRect(cx - bodyW/3, cy + bodyH/2 - 5, cx - bodyW/3 + legW, cy + bodyH/2 + legH, paint);
        canvas.drawRect(cx - bodyW/6, cy + bodyH/2 - 5, cx - bodyW/6 + legW, cy + bodyH/2 + legH, paint);
        canvas.drawRect(cx + bodyW/6, cy + bodyH/2 - 5, cx + bodyW/6 + legW, cy + bodyH/2 + legH, paint);
        canvas.drawRect(cx + bodyW/3 - legW, cy + bodyH/2 - 5, cx + bodyW/3, cy + bodyH/2 + legH, paint);
    }

    /**
     * 绘制牛 - 黑白花色 + 牛角
     */
    private void drawCow(Canvas canvas, Paint paint, float cx, float cy) {
        float bodyW = size * 0.75f;
        float bodyH = size * 0.55f;

        // 身体（白色圆角矩形）
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(cx - bodyW/2, cy - bodyH/2, cx + bodyW/2, cy + bodyH/2, 15, 15, paint);

        // 黑色斑点
        paint.setColor(Color.BLACK);
        canvas.drawOval(cx - bodyW/4, cy - bodyH/4, cx, cy, paint);
        canvas.drawCircle(cx + bodyW/3, cy + bodyH/4, size * 0.08f, paint);

        // 头部
        paint.setColor(Color.WHITE);
        float headR = size * 0.28f;
        canvas.drawCircle(cx - bodyW/2 - headR/2, cy - bodyH/4, headR, paint);

        // 牛角（白色弯曲）
        paint.setColor(Color.rgb(240, 240, 200));
        canvas.drawArc(cx - bodyW/2 - headR - 8, cy - bodyH/4 - headR,
                cx - bodyW/2 - headR + 8, cy - bodyH/4 - headR + 15, 30, 120, false, paint);
        canvas.drawArc(cx - bodyW/2 - headR/2 - 8, cy - bodyH/4 - headR,
                cx - bodyW/2 - headR/2 + 8, cy - bodyH/4 - headR + 15, 30, 120, false, paint);

        // 鼻子（粉色）
        paint.setColor(Color.rgb(255, 180, 180));
        canvas.drawOval(cx - bodyW/2 - headR - 5, cy - bodyH/4 + 5,
                cx - bodyW/2 - headR + 5, cy - bodyH/4 + 12, paint);

        // 眼睛
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx - bodyW/2 - headR/2 - 3, cy - bodyH/4 - 3, 3, paint);

        // 四条腿
        paint.setColor(Color.WHITE);
        canvas.drawRect(cx - bodyW/2 - 5, cy + bodyH/2, cx - bodyW/2 + 8, cy + bodyH/2 + 15, paint);
        canvas.drawRect(cx - bodyW/4, cy + bodyH/2, cx - bodyW/4 + 10, cy + bodyH/2 + 15, paint);
        canvas.drawRect(cx + bodyW/4 - 8, cy + bodyH/2, cx + bodyW/4 + 5, cy + bodyH/2 + 15, paint);
        canvas.drawRect(cx + bodyW/2 - 10, cy + bodyH/2, cx + bodyW/2, cy + bodyH/2 + 15, paint);

        // 尾巴
        paint.setStrokeWidth(3);
        canvas.drawLine(cx + bodyW/2, cy, cx + bodyW/2 + 12, cy + 10, paint);
    }

    /**
     * 绘制马 - 棕色身体 + 鬃毛 + 长脸
     */
    private void drawHorse(Canvas canvas, Paint paint, float cx, float cy) {
        float bodyW = size * 0.8f;
        float bodyH = size * 0.5f;

        // 身体（棕色圆角矩形）
        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawRoundRect(cx - bodyW/2, cy - bodyH/2, cx + bodyW/2, cy + bodyH/2, 12, 12, paint);

        // 【重新设计】头部 - 使用 Path 绘制更真实的马头侧面轮廓
        float headCenterX = cx - bodyW/2 - size * 0.15f;
        float headCenterY = cy - bodyH/2 - size * 0.05f;
        float headScale = size * 0.35f;

        paint.setColor(Color.rgb(139, 69, 19));
        android.graphics.Path horseHead = new android.graphics.Path();

        // 马头侧面轮廓（从后脑勺到鼻尖）
        horseHead.moveTo(headCenterX - headScale * 0.3f, headCenterY - headScale * 0.4f); // 后脑勺顶部
        horseHead.lineTo(headCenterX + headScale * 0.5f, headCenterY - headScale * 0.6f); // 头顶
        horseHead.lineTo(headCenterX + headScale * 0.9f, headCenterY - headScale * 0.3f); // 前额
        horseHead.lineTo(headCenterX + headScale * 1.1f, headCenterY + headScale * 0.1f); // 鼻梁
        horseHead.lineTo(headCenterX + headScale * 1.0f, headCenterY + headScale * 0.4f); // 鼻尖
        horseHead.lineTo(headCenterX + headScale * 0.7f, headCenterY + headScale * 0.5f); // 嘴部
        horseHead.lineTo(headCenterX + headScale * 0.3f, headCenterY + headScale * 0.45f); // 下颚
        horseHead.lineTo(headCenterX - headScale * 0.2f, headCenterY + headScale * 0.3f); // 喉咙
        horseHead.lineTo(headCenterX - headScale * 0.3f, headCenterY - headScale * 0.2f); // 颈部连接
        horseHead.close();

        canvas.drawPath(horseHead, paint);

        // 【修正】鬃毛 - 从头顶到颈部的长发
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        for (int i = 0; i < 5; i++) {
            float startX = headCenterX + headScale * 0.1f + i * 3;
            float startY = headCenterY - headScale * 0.3f + i * 8;
            float endX = startX - 8;
            float endY = startY + 15;
            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        // 【修正】耳朵 - 竖立在头顶
        paint.setColor(Color.rgb(139, 69, 19));
        paint.setStyle(Paint.Style.FILL);
        // 左耳（靠近观察者）
        android.graphics.Path ear1 = new android.graphics.Path();
        ear1.moveTo(headCenterX + headScale * 0.2f, headCenterY - headScale * 0.5f);
        ear1.lineTo(headCenterX + headScale * 0.25f, headCenterY - headScale * 0.85f);
        ear1.lineTo(headCenterX + headScale * 0.35f, headCenterY - headScale * 0.5f);
        canvas.drawPath(ear1, paint);

        // 右耳（远侧）
        android.graphics.Path ear2 = new android.graphics.Path();
        ear2.moveTo(headCenterX + headScale * 0.45f, headCenterY - headScale * 0.55f);
        ear2.lineTo(headCenterX + headScale * 0.5f, headCenterY - headScale * 0.85f);
        ear2.lineTo(headCenterX + headScale * 0.6f, headCenterY - headScale * 0.55f);
        canvas.drawPath(ear2, paint);

        // 【修正】眼睛 - 大而明亮
        paint.setColor(Color.BLACK);
        canvas.drawCircle(headCenterX + headScale * 0.55f, headCenterY - headScale * 0.15f, 3, paint);
        // 眼睛高光
        paint.setColor(Color.WHITE);
        canvas.drawCircle(headCenterX + headScale * 0.57f, headCenterY - headScale * 0.17f, 1, paint);

        // 【修正】鼻孔
        paint.setColor(Color.BLACK);
        canvas.drawCircle(headCenterX + headScale * 0.95f, headCenterY + headScale * 0.15f, 2, paint);

        // 【修正】嘴巴线条
        paint.setStrokeWidth(2);
        canvas.drawLine(headCenterX + headScale * 0.7f, headCenterY + headScale * 0.35f,
                headCenterX + headScale * 0.95f, headCenterY + headScale * 0.38f, paint);

        // 四条腿（修长）
        paint.setColor(Color.rgb(139, 69, 19));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        canvas.drawRect(cx - bodyW/2 - 5, cy + bodyH/2, cx - bodyW/2 + 6, cy + bodyH/2 + 18, paint);
        canvas.drawRect(cx - bodyW/4, cy + bodyH/2, cx - bodyW/4 + 8, cy + bodyH/2 + 18, paint);
        canvas.drawRect(cx + bodyW/4 - 6, cy + bodyH/2, cx + bodyW/4 + 5, cy + bodyH/2 + 18, paint);
        canvas.drawRect(cx + bodyW/2 - 8, cy + bodyH/2, cx + bodyW/2, cy + bodyH/2 + 18, paint);

        // 尾巴 - 更飘逸的长发
        paint.setStrokeWidth(4);
        canvas.drawLine(cx + bodyW/2, cy - bodyH/4, cx + bodyW/2 + 15, cy + bodyH/4, paint);
        paint.setStrokeWidth(3);
        canvas.drawLine(cx + bodyW/2 + 2, cy - bodyH/4 + 5, cx + bodyW/2 + 18, cy + bodyH/4 + 5, paint);
        paint.setStrokeWidth(2);
        canvas.drawLine(cx + bodyW/2 + 4, cy - bodyH/4 + 10, cx + bodyW/2 + 20, cy + bodyH/4 + 10, paint);
    }

    // 辅助方法：画三角形
    private void drawTriangle(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, Paint paint) {
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();
        canvas.drawPath(path, paint);
    }

    /**
     * 检测触摸点是否命中 NPC
     */
    public boolean isTouched(float touchWorldX, float touchWorldY) {
        float dx = touchWorldX - (x + size / 2f);
        float dy = touchWorldY - (y + size / 2f);
        return Math.sqrt(dx * dx + dy * dy) < size;
    }
}
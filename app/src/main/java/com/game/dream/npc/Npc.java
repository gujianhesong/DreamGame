package com.game.dream.npc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.game.dream.enums.NpcType;

public class Npc {
    protected int id;
    protected String name;
    protected NpcType type;
    protected float x, y;
    protected int size;

    protected Paint paint = new Paint();

    // 交互状态
    protected boolean hasQuest;
    protected boolean isInteracting;

    public Npc(int id, String name, NpcType type, float x, float y) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.hasQuest = false; // 默认无任务

        // 根据类型设置不同的大小
        if (type == NpcType.CHILD_BOY || type == NpcType.CHILD_GIRL) {
            this.size = 55;
        } else if (type == NpcType.WOMAN || type == NpcType.MAID || type == NpcType.GIRL || type == NpcType.BEAUTY) {
            this.size = 80;
        } else if (type == NpcType.SOLDIER || type == NpcType.HUNTER || type == NpcType.BANDIT) {
            this.size = 90;
        } else {
            this.size = 85;
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public boolean hasQuest() {
        return hasQuest;
    }

    public void setHasQuest(boolean hasQuest) {
        this.hasQuest = hasQuest;
    }

    /**
     * 绘制 NPC 到画布上
     *
     * @param canvas  画布
     * @param cameraX 摄像机 X 偏移量
     * @param cameraY 摄像机 Y 偏移量
     */
    public void draw(Canvas canvas, float cameraX, float cameraY) {
        paint.setAntiAlias(true);
        float cx = x + cameraX + size / 2f;
        float cy = y + cameraY + size / 2f;

        // 1. 阴影
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawOval(cx - size * 0.5f, cy + size * 0.2f, cx + size * 0.5f, cy + size * 0.8f, paint);
        //canvas.drawRect(cx - size * 0.5f, cy - size * 0.5f, cx + size * 0.5f, cy + size * 0.5f, paint);

        // 2. 根据类型绘制身体
        drawBody(canvas, paint, cx, cy);

        // 3. 头部 (统一肤色，但小孩头大一点)
        paint.setColor(Color.rgb(255, 220, 180));
        float headRadius = (type == NpcType.CHILD_BOY || type == NpcType.CHILD_GIRL) ? size / 3.5f : size / 4f;
        float headY = cy - size / 2f;
        canvas.drawCircle(cx, headY, headRadius, paint);

        // 4. 绘制头发 (在脸部之前画，或者根据发型决定层级)
        drawHair(canvas, paint, cx, headY, headRadius);

        // 5. 绘制脸部和五官
        drawFace(canvas, paint, cx, headY, headRadius);

        // 6. 名字 (绘制在脚下)
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(3, 0, 0, Color.BLACK);
        canvas.drawText(name, cx, cy + size * 1.2f, paint);
        paint.clearShadowLayer();

        // 7. 任务提示
        if (hasQuest && !isInteracting) {
            paint.setColor(Color.YELLOW);
            paint.setTextSize(35);
            canvas.drawText("!", cx, cy - size * 1.2f, paint);
        }
    }

    /**
     * 绘制 NPC 的身体主体和服装
     *
     * @param canvas 画布
     * @param paint  画笔
     * @param cx     屏幕中心 X 坐标
     * @param cy     屏幕中心 Y 坐标
     */
    private void drawBody(Canvas canvas, Paint paint, float cx, float cy) {
        // 定义身体区域的矩形范围
        RectF bodyRect = new RectF(cx - size / 2.5f, cy - size / 2.5f, cx + size / 2.5f, cy + size / 2.5f);

        // 1. 根据 NPC 类型绘制不同颜色和形状的身体（服装）
        switch (type) {
            case OLD_MAN:
                // 老人：灰色长袍，体现朴素和年迈
                paint.setColor(Color.rgb(160, 160, 160));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case CHILD_BOY:
                // 小男孩：鲜艳的红色小袄，体现活泼可爱
                paint.setColor(Color.rgb(255, 100, 100));
                canvas.drawRoundRect(bodyRect, 15, 15, paint);
                break;

            case CHILD_GIRL:
                // 小女孩：嫩黄色的连衣裙，像小鸭子一样可爱
                paint.setColor(Color.rgb(255, 250, 150));
                canvas.drawRoundRect(bodyRect, 12, 12, paint);
                break;

            case WOMAN:
                // 女人：粉色长裙，体现温柔女性化特征
                paint.setColor(Color.rgb(255, 105, 180));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case MAN:
                // 男人：朴素的蓝色方块状布衣，体现普通村民
                paint.setColor(Color.rgb(70, 130, 180));
                canvas.drawRect(bodyRect, paint);
                break;

            case MERCHANT:
                // 商人：耀眼的金色华服，体现富有和地位
                paint.setColor(Color.rgb(255, 215, 0));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case SERVANT:
                // 仆人：干净的白色仆役服，体现整洁和服务身份
                paint.setColor(Color.rgb(240, 240, 240));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case TAOIST:
                // 道人：青色道袍，带有修仙者的仙风道骨
                paint.setColor(Color.rgb(100, 180, 160));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case MONK:
                // 和尚：黄色僧袍，佛教传统颜色
                paint.setColor(Color.rgb(255, 200, 50));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case SOLDIER:
                // 士兵：银色铠甲，体现防御力和军事身份
                paint.setColor(Color.rgb(192, 192, 192));
                canvas.drawRect(bodyRect, paint);
                break;

            case FARMER:
                // 农民：土黄色粗布麻衣，体现劳作和朴实
                paint.setColor(Color.rgb(210, 180, 140));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case HUNTER:
                // 猎户：深绿色伪装服，便于在森林中隐蔽
                paint.setColor(Color.rgb(50, 100, 50));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case BANDIT:
                // 强盗：黑色夜行衣，体现神秘和危险
                paint.setColor(Color.rgb(40, 40, 40));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case BEGGAR:
                // 乞丐：褐色破旧补丁衣，体现贫困潦倒
                paint.setColor(Color.rgb(139, 69, 19));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case GAMBLER:
                // 赌徒：花哨的紫色衣服，体现不务正业和张扬
                paint.setColor(Color.rgb(148, 0, 211));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case SCHOLAR:
                // 书生：儒雅的浅蓝色长衫，体现文人气质
                paint.setColor(Color.rgb(173, 216, 230));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case MAID:
                // 丫鬟：红色系的制服，体现勤快和喜庆
                paint.setColor(Color.rgb(255, 100, 150)); // 调整为粉红色/浅红色
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case GIRL:
                // 少女：活泼的粉色短裙，体现青春活力
                paint.setColor(Color.rgb(255, 182, 193));
                canvas.drawRoundRect(bodyRect, 15, 15, paint);
                break;

            case BEAUTY:
                // 美人：华丽的深红色长裙，体现高贵和妩媚
                paint.setColor(Color.rgb(220, 20, 60));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
            case COACHMAN:
                // 车夫：深褐色耐脏的工装，体现户外劳作的辛苦
                paint.setColor(Color.rgb(100, 80, 60));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case PHARMACIST:
                // 药店老板：干净的青绿色长袍，带有草药气息
                paint.setColor(Color.rgb(120, 180, 140));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case TAVERN_KEEPER:
                // 酒馆老板：暖棕色粗布衣，带着酒香气息
                paint.setColor(Color.rgb(180, 120, 60));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;

            case DOCTOR:
                // 郎中：素净的白色长衫，体现医者仁心
                paint.setColor(Color.rgb(230, 230, 240));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
        }

        // 2. 绘制脚部/鞋子（在所有身体之后，配饰之前）
        drawFeet(canvas, paint, cx, cy);

        // 3. 绘制身体上的装饰细节（如拐杖、钱袋等，保持在最上层）
        drawAccessories(canvas, paint, cx, cy);
    }

    /**
     * 专门绘制脚部的逻辑
     */
    private void drawFeet(Canvas canvas, Paint paint, float cx, float cy) {
        float footY = cy + size / 2.5f; // 脚的位置在身体底部稍上方
        float footWidth = size / 5f;
        float footHeight = size / 6f;

        switch (type) {
            case OLD_MAN:
            case SCHOLAR:
                // 布鞋 (黑色)
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;

            case SOLDIER:
            case HUNTER:
            case BANDIT:
                // 战靴/皮靴 (深褐色)
                paint.setColor(Color.rgb(101, 67, 33));
                canvas.drawRect(cx - footWidth * 1.3f, footY - footHeight / 2, cx - footWidth * 0.3f, footY + footHeight, paint);
                canvas.drawRect(cx + footWidth * 0.3f, footY - footHeight / 2, cx + footWidth * 1.3f, footY + footHeight, paint);
                break;

            case MONK:
                // 僧鞋 (灰色草鞋)
                paint.setColor(Color.GRAY);
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                // 草绳细节
                paint.setStrokeWidth(1);
                paint.setColor(Color.WHITE);
                canvas.drawLine(cx - footWidth, footY + footHeight / 2, cx - footWidth * 0.5f, footY + footHeight / 2, paint);
                canvas.drawLine(cx + footWidth * 0.5f, footY + footHeight / 2, cx + footWidth, footY + footHeight / 2, paint);
                break;

            case WOMAN:
            case BEAUTY:
            case MAID:
            case GIRL:
                // 绣花鞋/小脚 (红色或粉色)
                paint.setColor(type == NpcType.BEAUTY ? Color.RED : Color.rgb(255, 192, 203));
                // 画得小巧一点，且并拢
                canvas.drawOval(cx - footWidth, footY + footHeight / 2, cx, footY + footHeight * 1.2f, paint);
                canvas.drawOval(cx, footY + footHeight / 2, cx + footWidth, footY + footHeight * 1.2f, paint);
                break;

            case BEGGAR:
                // 破鞋或者赤脚 (肤色)
                paint.setColor(Color.rgb(255, 220, 180));
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;
            case COACHMAN:
                // 结实的皮靴，沾点泥土色
                paint.setColor(Color.rgb(80, 50, 30));
                canvas.drawRect(cx - size / 4.5f, cy + size / 3.5f, cx - size / 10f, cy + size / 2.2f, paint);
                canvas.drawRect(cx + size / 10f, cy + size / 3.5f, cx + size / 4.5f, cy + size / 2.2f, paint);
                break;

            case PHARMACIST:
                // 干净的布鞋，朴素整洁
                paint.setColor(Color.rgb(80, 100, 80));
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;

            case TAVERN_KEEPER:
                // 厚实的皮靴，站得稳
                paint.setColor(Color.rgb(120, 80, 40));
                canvas.drawRect(cx - footWidth * 1.3f, footY - footHeight / 2, cx - footWidth * 0.3f, footY + footHeight, paint);
                canvas.drawRect(cx + footWidth * 0.3f, footY - footHeight / 2, cx + footWidth * 1.3f, footY + footHeight, paint);
                break;

            case DOCTOR:
                // 整洁的黑色布鞋，低调稳重
                paint.setColor(Color.rgb(50, 50, 50));
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;

            default:
                // 普通黑鞋
                paint.setColor(Color.DKGRAY);
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;
        }
    }

    /**
     * 绘制 NPC 身上的配饰、武器或特殊装饰细节
     * 这些元素通常绘制在身体和头发的最上层，以确保可见性
     */
    private void drawAccessories(Canvas canvas, Paint paint, float cx, float cy) {
        switch (type) {
            case OLD_MAN:
                // 拐杖：棕色木棍，体现年迈需要支撑
                paint.setStrokeWidth(4);
                paint.setColor(Color.rgb(101, 67, 33));
                canvas.drawLine(cx + size / 2f, cy, cx + size / 2f, cy + size / 1.5f, paint);
                break;

            case MERCHANT:
                // 钱袋：腰间挂着的褐色小袋子，象征富有
                paint.setColor(Color.rgb(139, 69, 19));
                canvas.drawCircle(cx - size / 3f, cy + size / 4f, 8, paint);
                break;

            case WOMAN:
                // 发髻：脑后盘起的黑色头发，体现传统女性发型
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx, cy - size / 1.8f, 8, paint);
                break;

            case SOLDIER:
                // 肩甲：士兵铠甲两侧的红色护肩，体现威严
                paint.setColor(Color.rgb(180, 0, 0));
                canvas.drawRect(cx - size / 2.5f, cy - size / 2.5f, cx - size / 4, cy - size / 4, paint);
                canvas.drawRect(cx + size / 4, cy - size / 2.5f, cx + size / 2.5f, cy - size / 4, paint);
                break;

            case FARMER:
                // 草帽：头顶宽大的黄色草帽，用于田间遮阳
                paint.setColor(Color.rgb(255, 255, 150));
                canvas.drawOval(cx - size / 2, cy - size / 1.8f, cx + size / 2, cy - size / 2.5f, paint);
                break;

            case HUNTER:
                // 箭袋：背后背着的褐色长方形容器，用于装箭矢
                paint.setColor(Color.rgb(139, 69, 19));
                canvas.drawRect(cx + size / 3, cy - size / 2, cx + size / 1.8f, cy + size / 4, paint);
                break;

            case BANDIT:
                // 红头巾：额头上系的红色布条，强盗的典型标志
                paint.setColor(Color.RED);
                canvas.drawRect(cx - size / 2.2f, cy - size / 1.8f, cx + size / 2.2f, cy - size / 2.5f, paint);
                // 红腰带：腰间系的粗犷腰带
                paint.setStrokeWidth(4);
                canvas.drawLine(cx - size / 2.5f, cy, cx + size / 2.5f, cy, paint);
                break;

            case BEGGAR:
                // 破碗：身边放着的灰色讨饭碗
                paint.setColor(Color.GRAY);
                canvas.drawOval(cx - size / 1.8f, cy + size / 3, cx - size / 2.5f, cy + size / 2.5f, paint);
                break;

            case GAMBLER:
                // 骰子：手里捏着的白色小方块，体现赌徒身份
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cx + size / 2.5f, cy, 5, paint);
                break;

            case SCHOLAR:
                // 折扇：手中拿着的白色半圆形扇子，体现文人雅趣
                paint.setColor(Color.WHITE);
                canvas.drawArc(cx - size / 1.8f, cy, cx - size / 2.5f, cy + size / 2, 0, 180, true, paint);
                break;

            case MAID:
                // 围裙：身前的白色工作围裙
                paint.setColor(Color.WHITE);
                canvas.drawRect(cx - 10, cy, cx + 10, cy + size / 2, paint);
                // 双丸子头：丫鬟特有的两侧发髻
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - 10, cy - size / 1.8f, 6, paint);
                canvas.drawCircle(cx + 10, cy - size / 1.8f, 6, paint);
                break;

            case GIRL:
                // 蝴蝶结：头顶红色的可爱发饰
                paint.setColor(Color.RED);
                canvas.drawOval(cx - 5, cy - size / 1.8f, cx + 5, cy - size / 2.2f, paint);
                break;

            case BEAUTY:
                // 金腰带：华丽的金色腰饰，彰显高贵
                paint.setColor(Color.rgb(255, 215, 0));
                canvas.drawRect(cx - size / 2.5f, cy, cx + size / 2.5f, cy + 5, paint);
                // 飘带：随风飘舞的粉色丝带，增加动感
                paint.setStrokeWidth(3);
                paint.setColor(Color.rgb(255, 192, 203));
                canvas.drawLine(cx + size / 2.5f, cy - size / 2, cx + size / 1.5f, cy, paint);
                break;

            case TAOIST:
                // 太极徽章：道袍胸前的黑白太极简化图案
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx, cy - size / 4, 8, paint);
                break;

            case MONK:
                // 佛珠：胸前挂着的红色念珠串
                paint.setColor(Color.RED);
                for (int i = 0; i < 5; i++) {
                    canvas.drawCircle(cx - 15 + i * 8, cy + size / 4, 3, paint);
                }
                break;
            case COACHMAN:
                // 马鞭：手里拿着一根细长的鞭子
                paint.setStrokeWidth(3);
                paint.setColor(Color.rgb(139, 69, 19)); // 棕色皮革
                canvas.drawLine(cx + size / 2f, cy - size / 4, cx + size / 1.2f, cy + size / 4, paint);
                // 鞭梢
                paint.setStrokeWidth(1);
                canvas.drawLine(cx + size / 1.2f, cy + size / 4, cx + size / 1.3f, cy + size / 3, paint);
                break;

            case PHARMACIST:
                // 药葫芦：腰间挂着的褐色小葫芦
                paint.setColor(Color.rgb(139, 90, 43));
                canvas.drawOval(cx - size / 3f, cy + size / 5f, cx - size / 5f, cy + size / 2.5f, paint);
                // 葫芦口
                paint.setColor(Color.rgb(100, 60, 30));
                canvas.drawRect(cx - size / 3.5f, cy + size / 6f, cx - size / 5.5f, cy + size / 5f, paint);
                break;

            case TAVERN_KEEPER:
                // 酒葫芦：手里提着的酒壶
                paint.setColor(Color.rgb(160, 100, 40));
                canvas.drawOval(cx + size / 3f, cy + size / 5f, cx + size / 2f, cy + size / 2.5f, paint);
                // 壶嘴
                paint.setColor(Color.rgb(140, 80, 30));
                canvas.drawRect(cx + size / 2.5f, cy + size / 6f, cx + size / 2.2f, cy + size / 5f, paint);
                break;

            case DOCTOR:
                // 药箱：手里提着的方形药箱
                paint.setColor(Color.rgb(139, 69, 19));
                canvas.drawRect(cx + size / 4f, cy + size / 5f, cx + size / 1.8f, cy + size / 2.5f, paint);
                // 药箱上的十字
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(2);
                float boxCx = (cx + size / 4f + cx + size / 1.8f) / 2;
                float boxCy = (cy + size / 5f + cy + size / 2.5f) / 2;
                canvas.drawLine(boxCx - 4, boxCy, boxCx + 4, boxCy, paint);
                canvas.drawLine(boxCx, boxCy - 4, boxCx, boxCy + 4, paint);
                break;
        }
    }

    /**
     * 绘制各种发型
     */
    private void drawHair(Canvas canvas, Paint paint, float cx, float cy, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK); // 默认黑发

        switch (type) {
            case OLD_MAN:
                // 白色稀疏的头发
                paint.setColor(Color.rgb(220, 220, 220));
                canvas.drawArc(cx - radius, cy - radius * 1.2f, cx + radius, cy, 180, 180, true, paint);
                break;

            case CHILD_BOY:
                canvas.drawCircle(cx - radius, cy - radius / 2, radius / 3f, paint);
                canvas.drawCircle(cx + radius, cy - radius / 2, radius / 3f, paint);
                break;
            case CHILD_GIRL:
                // 两个高高的羊角辫，非常俏皮
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 2, 180, 180, true, paint);
                // 羊角辫翘得更高
                canvas.drawCircle(cx - radius * 0.8f, cy - radius * 0.8f, radius / 2f, paint);
                canvas.drawCircle(cx + radius * 0.8f, cy - radius * 0.8f, radius / 2f, paint);
                // 红色发绳
                paint.setColor(Color.RED);
                canvas.drawCircle(cx - radius * 0.8f, cy - radius * 0.8f, radius / 6f, paint);
                canvas.drawCircle(cx + radius * 0.8f, cy - radius * 0.8f, radius / 6f, paint);
                break;
            case GIRL:
                // 齐刘海或可爱的短发
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 2, 180, 180, true, paint);
                // 两个小辫子
                canvas.drawCircle(cx - radius, cy - radius / 2, radius / 2.5f, paint);
                canvas.drawCircle(cx + radius, cy - radius / 2, radius / 2.5f, paint);
                break;

            case WOMAN:
            case BEAUTY:
                // 长发披肩
                canvas.drawArc(cx - radius, cy - radius * 1.2f, cx + radius, cy, 180, 180, true, paint);
                // 两侧的长发
                canvas.drawRect(cx - radius, cy - radius / 2, cx - radius + 4, cy + radius, paint);
                canvas.drawRect(cx + radius - 4, cy - radius / 2, cx + radius, cy + radius, paint);
                break;

            case MAN:
            case FARMER:
            case BANDIT:
                // 简单的短发
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                break;

            case MERCHANT:
                // 油光锃亮的中分或背头
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 4, 180, 180, true, paint);
                break;

            case SERVANT:
            case MAID:
                // 丫鬟/仆人发髻 (已经在 drawBody 里画了双丸子头，这里补全头顶部分)
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                break;

            case TAOIST:
                // 道士发髻 (头顶一个小圆包)
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                canvas.drawCircle(cx, cy - radius * 1.3f, radius / 2.5f, paint);
                break;

            case MONK:
                // 和尚确实是光头，但我们可以画点戒疤或者留白
                // 这里选择不画头发，保持光头特色
                break;

            case SOLDIER:
                // 军人平头 (方正一点)
                canvas.drawRect(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 2, paint);
                break;

            case HUNTER:
                // 猎户凌乱一点的头发
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                // 几根翘起的头发
                canvas.drawLine(cx + radius / 2, cy - radius, cx + radius, cy - radius * 1.2f, paint);
                break;

            case BEGGAR:
                // 乞丐乱糟糟的头发
                paint.setColor(Color.DKGRAY);
                for (int i = 0; i < 6; i++) {
                    float angle = (float) (Math.PI + (i * Math.PI / 5));
                    float hx = cx + (float) Math.cos(angle) * radius;
                    float hy = cy + (float) Math.sin(angle) * radius - radius / 2;
                    canvas.drawCircle(hx, hy, radius / 4, paint);
                }
                break;

            case GAMBLER:
                // 赌徒夸张的发型
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                // 挑染一缕 (用灰色模拟)
                paint.setColor(Color.GRAY);
                canvas.drawOval(cx + radius / 2, cy - radius, cx + radius, cy - radius / 2, paint);
                break;

            case SCHOLAR:
                // 书生发冠 (用一个方块模拟帽子/发冠)
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                paint.setColor(Color.rgb(50, 50, 100)); // 深蓝色发冠
                canvas.drawRect(cx - radius / 1.5f, cy - radius * 1.3f, cx + radius / 1.5f, cy - radius, paint);
                break;
            case COACHMAN:
                // 车夫帽（平顶帽）：遮阳挡风
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                // 帽檐
                paint.setColor(Color.rgb(60, 40, 20));
                canvas.drawRect(cx - radius * 1.2f, cy - radius * 0.9f, cx + radius * 1.2f, cy - radius * 0.7f, paint);
                break;

            case PHARMACIST:
                // 整洁的发髻，用一根木簪固定
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                canvas.drawCircle(cx, cy - radius * 1.2f, radius / 2.5f, paint);
                // 木簪
                paint.setColor(Color.rgb(139, 90, 43));
                canvas.drawLine(cx - radius / 2, cy - radius * 1.2f, cx + radius / 2, cy - radius * 1.2f, paint);
                break;

            case TAVERN_KEEPER:
                // 随意扎起的头发，略显凌乱
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                // 几缕翘起的头发
                canvas.drawLine(cx - radius / 3, cy - radius, cx - radius / 2, cy - radius * 1.2f, paint);
                canvas.drawLine(cx + radius / 4, cy - radius, cx + radius / 3, cy - radius * 1.15f, paint);
                break;

            case DOCTOR:
                // 传统的书生式发冠，但更简洁
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                // 小发髻
                canvas.drawCircle(cx, cy - radius * 1.25f, radius / 3f, paint);
                break;
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawFace(Canvas canvas, Paint paint, float cx, float cy, float radius) {
        paint.setStyle(Paint.Style.FILL);

        switch (type) {
            case OLD_MAN:
                // 慈祥的眼睛（眯成一条缝）
                paint.setColor(Color.BLACK);
                canvas.drawLine(cx - radius / 2, cy - radius / 4, cx - radius / 6, cy - radius / 4, paint);
                canvas.drawLine(cx + radius / 6, cy - radius / 4, cx + radius / 2, cy - radius / 4, paint);
                // 白胡子
                paint.setColor(Color.WHITE);
                canvas.drawOval(cx - radius / 2, cy + radius / 4, cx + radius / 2, cy + radius / 1.5f, paint);
                break;

            case CHILD_BOY:
                // 大大的圆眼睛
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 6, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 6, paint);
                // 腮红
                paint.setColor(Color.argb(100, 255, 0, 0));
                canvas.drawCircle(cx - radius / 1.8f, cy + radius / 5, radius / 5, paint);
                canvas.drawCircle(cx + radius / 1.8f, cy + radius / 5, radius / 5, paint);
                // 开心的嘴巴
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 2, cy, cx + radius / 2, cy + radius, 0, -180, false, paint);
                break;
            case CHILD_GIRL:
                // 极其天真的大眼睛（占脸部比例很大）
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.2f, cy - radius / 5, radius / 4.5f, paint);
                canvas.drawCircle(cx + radius / 2.2f, cy - radius / 5, radius / 4.5f, paint);
                // 眼睛里的高光
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 4, radius / 8f, paint);
                canvas.drawCircle(cx + radius / 1.9f, cy - radius / 4, radius / 8f, paint);
                // 惊讶的小嘴巴（O型）
                paint.setColor(Color.rgb(255, 105, 180));
                canvas.drawOval(cx - radius / 5, cy + radius / 4, cx + radius / 5, cy + radius / 2.5f, paint);
                break;

            case WOMAN:
                // 漂亮的杏眼
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - radius / 1.8f, cy - radius / 3, cx - radius / 4, cy - radius / 6, paint);
                canvas.drawOval(cx + radius / 4, cy - radius / 3, cx + radius / 1.8f, cy - radius / 6, paint);
                // 红唇
                paint.setColor(Color.RED);
                canvas.drawOval(cx - radius / 4, cy + radius / 4, cx + radius / 4, cy + radius / 2.5f, paint);
                break;

            case MAN:
                // 坚毅的眼神
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 7, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 7, paint);
                // 浓眉
                paint.setStrokeWidth(3);
                canvas.drawLine(cx - radius / 1.5f, cy - radius / 2, cx - radius / 4, cy - radius / 2.5f, paint);
                canvas.drawLine(cx + radius / 4, cy - radius / 2.5f, cx + radius / 1.5f, cy - radius / 2, paint);
                break;

            case MERCHANT: {
                // 精明的三角眼
                paint.setColor(Color.BLACK);
                android.graphics.Path eye1 = new android.graphics.Path();
                eye1.moveTo(cx - radius / 1.5f, cy - radius / 3);
                eye1.lineTo(cx - radius / 4, cy - radius / 3);
                eye1.lineTo(cx - radius / 2, cy - radius / 6);
                canvas.drawPath(eye1, paint);

                android.graphics.Path eye2 = new android.graphics.Path();
                eye2.moveTo(cx + radius / 4, cy - radius / 3);
                eye2.lineTo(cx + radius / 1.5f, cy - radius / 3);
                eye2.lineTo(cx + radius / 2, cy - radius / 6);
                canvas.drawPath(eye2, paint);
                // 八字胡
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 1.5f, cy, cx - radius / 6, cy + radius / 2, 0, -180, false, paint);
                canvas.drawArc(cx + radius / 6, cy, cx + radius / 1.5f, cy + radius / 2, 0, -180, false, paint);
            }
            break;

            case SERVANT:
                // 恭敬的低垂眼
                paint.setColor(Color.GRAY);
                canvas.drawLine(cx - radius / 2, cy - radius / 5, cx - radius / 6, cy - radius / 5, paint);
                canvas.drawLine(cx + radius / 6, cy - radius / 5, cx + radius / 2, cy - radius / 5, paint);
                // 微笑
                paint.setStrokeWidth(2);
                paint.setColor(Color.BLACK);
                canvas.drawArc(cx - radius / 3, cy + radius / 5, cx + radius / 3, cy + radius / 1.5f, 200, 140, false, paint);
                break;
            case TAOIST:
                // 仙风道骨的长眉
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(2);
                canvas.drawLine(cx - radius / 1.2f, cy - radius / 2, cx - radius / 4, cy - radius / 3, paint);
                canvas.drawLine(cx + radius / 1.2f, cy - radius / 2, cx + radius / 4, cy - radius / 3, paint);
                // 山羊胡
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - 5, cy + radius / 2, cx + 5, cy + radius, paint);
                break;

            case MONK:
                // 闭目养神 (两条线)
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(2);
                canvas.drawLine(cx - radius / 2, cy - radius / 5, cx - radius / 6, cy - radius / 5, paint);
                canvas.drawLine(cx + radius / 6, cy - radius / 5, cx + radius / 2, cy - radius / 5, paint);
                // 额头红点 (戒疤)
                paint.setColor(Color.RED);
                canvas.drawCircle(cx, cy - radius / 1.5f, 3, paint);
                break;

            case SOLDIER:
                // 严肃的平头和平视眼
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 8, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 8, paint);
                // 紧抿的嘴唇
                paint.setStrokeWidth(2);
                canvas.drawLine(cx - radius / 3, cy + radius / 3, cx + radius / 3, cy + radius / 3, paint);
                break;

            case FARMER:
                // 朴实的圆眼和憨厚的笑
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 7, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 7, paint);
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 2, cy, cx + radius / 2, cy + radius / 1.5f, 0, -180, false, paint);
                break;

            case HUNTER:
                // 锐利的眼神 (像鹰一样)
                paint.setColor(Color.YELLOW); // 瞳孔颜色
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 8, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 8, paint);
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 12, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 12, paint);
                break;

            case BANDIT:
                // 凶恶的斜眼
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - radius / 1.8f, cy - radius / 3, cx - radius / 4, cy - radius / 6, paint);
                canvas.drawOval(cx + radius / 4, cy - radius / 3, cx + radius / 1.8f, cy - radius / 6, paint);
                // 歪嘴笑
                paint.setStrokeWidth(2);
                canvas.drawLine(cx - radius / 3, cy + radius / 4, cx + radius / 3, cy + radius / 2, paint);
                break;
            case BEGGAR:
                // 凌乱的头发和疲惫的眼神
                paint.setColor(Color.DKGRAY);
                for (int i = 0; i < 5; i++) {
                    canvas.drawLine(cx - radius + i * 5, cy - radius, cx - radius + 2 + i * 5, cy - radius - 5, paint);
                }
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 8, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 8, paint);
                break;

            case GAMBLER:
                // 贼眉鼠眼
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - radius / 1.8f, cy - radius / 3, cx - radius / 4, cy - radius / 6, paint);
                canvas.drawOval(cx + radius / 4, cy - radius / 3, cx + radius / 1.8f, cy - radius / 6, paint);
                // 坏笑
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 2, cy, cx + radius / 2, cy + radius, 30, -120, false, paint);
                break;

            case SCHOLAR:
                // 戴着眼镜（两个圆圈加横梁）
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 4, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 4, paint);
                canvas.drawLine(cx - radius / 4, cy - radius / 5, cx + radius / 4, cy - radius / 5, paint);
                // 文质彬彬的嘴
                canvas.drawOval(cx - radius / 4, cy + radius / 3, cx + radius / 4, cy + radius / 2.5f, paint);
                break;

            case MAID:
                // 乖巧的大眼睛
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 6, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 6, paint);
                // 羞涩的微笑
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 3, cy + radius / 4, cx + radius / 3, cy + radius / 1.5f, 200, 140, false, paint);
                break;

            case GIRL:
                // 天真无邪的表情
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 6, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 6, paint);
                // 惊讶或开心的小嘴
                canvas.drawOval(cx - radius / 6, cy + radius / 3, cx + radius / 6, cy + radius / 2, paint);
                break;

            case BEAUTY: {
                // 妩媚的桃花眼
                paint.setColor(Color.BLACK);
                android.graphics.Path eye1 = new android.graphics.Path();
                eye1.moveTo(cx - radius / 1.5f, cy - radius / 3);
                eye1.quadTo(cx - radius / 2, cy - radius / 5, cx - radius / 4, cy - radius / 3);
                canvas.drawPath(eye1, paint);

                android.graphics.Path eye2 = new android.graphics.Path();
                eye2.moveTo(cx + radius / 4, cy - radius / 3);
                eye2.quadTo(cx + radius / 2, cy - radius / 5, cx + radius / 1.5f, cy - radius / 3);
                canvas.drawPath(eye2, paint);

                // 樱桃小口
                paint.setColor(Color.RED);
                canvas.drawOval(cx - radius / 5, cy + radius / 3, cx + radius / 5, cy + radius / 2.2f, paint);
                // 眉心花钿
                paint.setColor(Color.rgb(255, 192, 203));
                canvas.drawCircle(cx, cy - radius / 1.5f, 3, paint);
            }
            break;
            case COACHMAN:
                // 饱经风霜的脸，眼神专注看路
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 8, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 8, paint);
                // 络腮胡茬
                paint.setStrokeWidth(1);
                paint.setColor(Color.DKGRAY);
                for (int i = 0; i < 5; i++) {
                    canvas.drawLine(cx - radius / 2 + i * 5, cy + radius / 4, cx - radius / 2 + 2 + i * 5, cy + radius / 3, paint);
                }
                break;

            case PHARMACIST:
                // 和善的面容，戴着圆框眼镜
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 4, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 4, paint);
                canvas.drawLine(cx - radius / 4, cy - radius / 5, cx + radius / 4, cy - radius / 5, paint);
                // 温和的微笑
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 3, cy + radius / 5, cx + radius / 3, cy + radius / 1.5f, 200, 140, false, paint);
                break;

            case TAVERN_KEEPER:
                // 红光满面， jovial 的笑容
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 7, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 7, paint);
                // 红扑扑的脸颊
                paint.setColor(Color.argb(80, 255, 80, 80));
                canvas.drawCircle(cx - radius / 2, cy + radius / 6, radius / 4, paint);
                canvas.drawCircle(cx + radius / 2, cy + radius / 6, radius / 4, paint);
                // 开怀大笑
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 2, cy, cx + radius / 2, cy + radius, 0, -180, false, paint);
                break;

            case DOCTOR:
                // 专注而睿智的眼神
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 7, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 7, paint);
                // 淡淡的八字胡
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 1.5f, cy + radius / 6, cx - radius / 6, cy + radius / 2, 0, -180, false, paint);
                canvas.drawArc(cx + radius / 6, cy + radius / 6, cx + radius / 1.5f, cy + radius / 2, 0, -180, false, paint);
                break;
        }
        paint.setStyle(Paint.Style.FILL); // Reset style
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
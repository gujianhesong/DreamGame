package com.game.dream.npc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.GameEngine;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.system.ItemSystem;
import com.game.dream.ui.FloatingText;

import java.util.Random;

/**
 * 宝箱实体 - 迷宫中的可交互宝箱, 点击后获得随机奖励
 */
public class TreasureChest {

    private int id;
    private float x, y;
    private int size = 50;
    private boolean isOpened = false;
    private Paint paint = new Paint();

    private static final Random random = new Random();

    public TreasureChest(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    /**
     * 绘制宝箱
     */
    public void draw(Canvas canvas, float cameraX, float cameraY) {
        paint.setAntiAlias(true);
        float screenX = x + cameraX;
        float screenY = y + cameraY;
        float halfSize = size / 2f;

        if (isOpened) {
            drawOpenedChest(canvas, screenX, screenY, halfSize);
        } else {
            drawClosedChest(canvas, screenX, screenY, halfSize);
        }
    }

    /**
     * 绘制关闭的宝箱
     */
    private void drawClosedChest(Canvas canvas, float cx, float cy, float half) {
        // 阴影
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawOval(cx - half, cy + half * 0.5f, cx + half, cy + half, paint);

        // 箱体底部 (深棕色)
        paint.setColor(Color.rgb(139, 90, 30));
        canvas.drawRoundRect(cx - half, cy - half * 0.2f, cx + half, cy + half * 0.6f, 4, 4, paint);

        // 箱体上部 (金色)
        paint.setColor(Color.rgb(200, 160, 50));
        canvas.drawRoundRect(cx - half, cy - half * 0.7f, cx + half, cy, 6, 6, paint);

        // 箱盖弧形
        paint.setColor(Color.rgb(180, 140, 40));
        canvas.drawArc(cx - half, cy - half, cx + half, cy + half * 0.2f, 180, 180, true, paint);

        // 金属边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(160, 130, 30));
        canvas.drawRoundRect(cx - half, cy - half * 0.7f, cx + half, cy + half * 0.6f, 4, 4, paint);
        paint.setStyle(Paint.Style.FILL);

        // 锁扣
        paint.setColor(Color.rgb(220, 200, 80));
        canvas.drawCircle(cx, cy - half * 0.1f, half * 0.15f, paint);
        paint.setColor(Color.rgb(160, 130, 30));
        canvas.drawCircle(cx, cy - half * 0.1f, half * 0.08f, paint);

        // 高光
        paint.setColor(Color.argb(60, 255, 255, 200));
        canvas.drawRect(cx - half * 0.8f, cy - half * 0.6f, cx - half * 0.3f, cy - half * 0.3f, paint);
    }

    /**
     * 绘制打开的宝箱
     */
    private void drawOpenedChest(Canvas canvas, float cx, float cy, float half) {
        // 阴影
        paint.setColor(Color.argb(30, 0, 0, 0));
        canvas.drawOval(cx - half, cy + half * 0.5f, cx + half, cy + half, paint);

        // 箱体 (暗淡一些)
        paint.setColor(Color.rgb(110, 75, 25));
        canvas.drawRoundRect(cx - half, cy - half * 0.2f, cx + half, cy + half * 0.6f, 4, 4, paint);

        // 打开的箱盖 (向上翻开)
        paint.setColor(Color.rgb(160, 120, 35));
        canvas.drawArc(cx - half, cy - half * 1.4f, cx + half, cy - half * 0.1f, 180, 180, true, paint);

        // 边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(130, 100, 25));
        canvas.drawRoundRect(cx - half, cy - half * 0.2f, cx + half, cy + half * 0.6f, 4, 4, paint);
        paint.setStyle(Paint.Style.FILL);

        // 内部金光
        paint.setColor(Color.argb(80, 255, 215, 0));
        canvas.drawOval(cx - half * 0.5f, cy - half * 0.1f, cx + half * 0.5f, cy + half * 0.3f, paint);
    }

    /**
     * 打开宝箱, 生成随机奖励
     */
    public void open() {
        if (isOpened) return;
        isOpened = true;

        // 随机奖励
        int rewardType = random.nextInt(100);
        Item rewardItem = null;
        int quantity = 1;
        String rewardText = "";

        if (rewardType < 30) {
            // 30% 概率: HP药品
            int[] hpIds = {101011, 101012, 101013};
            int hpId = hpIds[random.nextInt(hpIds.length)];
            rewardItem = ItemCreator.createMedicineHp(hpId);
            quantity = 1 + random.nextInt(3);
        } else if (rewardType < 55) {
            // 25% 概率: MP药品
            int[] mpIds = {102011, 102012, 102013};
            int mpId = mpIds[random.nextInt(mpIds.length)];
            rewardItem = ItemCreator.createMedicineMp(mpId);
            quantity = 1 + random.nextInt(3);
        } else if (rewardType < 75) {
            // 20% 概率: 金钱奖励
            int money = 100 + random.nextInt(400);
            com.game.dream.system.RoleSystem.getInstance().addMoney(money);
            rewardText = "+" + money + " 金钱";
        } else if (rewardType < 90) {
            // 15% 概率: 属性丹药
            int pillType = random.nextInt(5);
            switch (pillType) {
                case 0: rewardItem = ItemCreator.createGain_hp_1(); break;
                case 1: rewardItem = ItemCreator.createGain_attack_1(); break;
                case 2: rewardItem = ItemCreator.createGain_defense_1(); break;
                case 3: rewardItem = ItemCreator.createGain_mana_1(); break;
                case 4: rewardItem = ItemCreator.createGain_speed_1(); break;
            }
            quantity = 1;
        } else {
            // 10% 概率: 稀有奖励 (高级药品)
            int[] rareIds = {101014, 102014};
            int rareId = rareIds[random.nextInt(rareIds.length)];
            if (rareId / 1000 == 101) {
                rewardItem = ItemCreator.createMedicineHp(rareId);
            } else {
                rewardItem = ItemCreator.createMedicineMp(rareId);
            }
            quantity = 1;
        }

        // 发放物品奖励
        if (rewardItem != null) {
            boolean added = ItemSystem.getInstance().addItem(rewardItem, quantity);
            if (added) {
                if (quantity > 1) {
                    rewardText = rewardItem.getName() + " x" + quantity;
                } else {
                    rewardText = rewardItem.getName();
                }
            } else {
                rewardText = "背包已满!";
            }
        }

        // 显示浮动文字
        if (!rewardText.isEmpty()) {
            GameEngine.getInstance().showFloatingText(
                    x, y - size,
                    rewardText,
                    FloatingText.Type.EXPERIENCE
            );
        }

        GameEngine.getInstance().showCenterToast("获得宝箱奖励!");
    }

    /**
     * 检测触摸是否命中宝箱
     */
    public boolean isTouched(float touchWorldX, float touchWorldY) {
        if (isOpened) return false;
        float dx = touchWorldX - (x + size / 2f);
        float dy = touchWorldY - (y + size / 2f);
        return Math.sqrt(dx * dx + dy * dy) < size;
    }

    public int getId() { return id; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }
    public boolean isOpened() { return isOpened; }
}

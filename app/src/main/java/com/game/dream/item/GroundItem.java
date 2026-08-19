package com.game.dream.item;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 地面掉落物品 - 击杀怪物后掉落在地面，玩家靠近自动拾取
 */
public class GroundItem {

    private float x, y;
    private Item item;
    private int quantity;
    private long createdTime;
    private boolean isPickedUp;

    // 拾取范围
    private static final float PICKUP_RADIUS = 60f;

    // 视觉参数
    private static final float ITEM_RADIUS = 20f;
    private static final float BOB_AMPLITUDE = 5f;
    private static final float BOB_SPEED = 0.005f;

    public GroundItem(float x, float y, Item item, int quantity) {
        this.x = x + (float) (Math.random() * 40 - 20); // 随机偏移
        this.y = y + (float) (Math.random() * 40 - 20);
        this.item = item;
        this.quantity = quantity;
        this.createdTime = System.currentTimeMillis();
        this.isPickedUp = false;
    }

    /**
     * 检测玩家是否在拾取范围内
     */
    public boolean isPlayerInRange(float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= PICKUP_RADIUS;
    }

    /**
     * 绘制地面物品
     */
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (isPickedUp) return;

        float screenX = x + offsetX;
        float screenY = y + offsetY;

        // 上下浮动效果
        long elapsed = System.currentTimeMillis() - createdTime;
        float bobOffset = (float) Math.sin(elapsed * BOB_SPEED) * BOB_AMPLITUDE;
        screenY += bobOffset;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 根据物品稀有度获取颜色
        int itemColor = item.getColor();

        // 1. 绘制外圈光晕 (脉冲效果)
        float pulse = 1.0f + 0.2f * (float) Math.sin(elapsed * 0.005);
        paint.setColor(Color.argb(40, Color.red(itemColor), Color.green(itemColor), Color.blue(itemColor)));
        canvas.drawCircle(screenX, screenY, ITEM_RADIUS * 1.6f * pulse, paint);

        // 2. 绘制物品底色圆
        paint.setColor(Color.argb(200, Color.red(itemColor), Color.green(itemColor), Color.blue(itemColor)));
        canvas.drawCircle(screenX, screenY, ITEM_RADIUS, paint);

        // 3. 绘制物品内圈高光
        paint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawCircle(screenX - 2, screenY - 2, ITEM_RADIUS * 0.4f, paint);

        // 4. 如果是装备，绘制边框
        if (item instanceof EquipmentItem) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.argb(220, Color.red(itemColor), Color.green(itemColor), Color.blue(itemColor)));
            canvas.drawCircle(screenX, screenY, ITEM_RADIUS + 1, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // 5. 如果数量 > 1，显示数量文字
        if (quantity > 1) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(quantity), screenX, screenY + ITEM_RADIUS + 16, paint);
        }
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    public boolean isPickedUp() { return isPickedUp; }
    public void setPickedUp(boolean pickedUp) { isPickedUp = pickedUp; }
}

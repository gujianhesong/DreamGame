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
    public enum ObjectType { HOUSE, TREE, WELL, ROAD }

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

        Random rand = new Random(42); // 固定种子保证每次生成的村庄一样

        // 2. 生成一些简单的道路 (Roads)
//        for (int i = 0; i < 5; i++) {
//            int rw = rand.nextInt(100) + 200;
//            int rh = rand.nextInt(20) + 50;
//            objects.add(new VillageObject(ObjectType.ROAD,
//                    vX + rand.nextInt(vW), vY + rand.nextInt(vH), rw, rh, Color.rgb(180, 160, 140)));
//        }

        // 3. 生成房屋 (Houses)
        for (int i = 0; i < 8; i++) {
            int hw = 400;
            int hh = 320;
            // 确保房子不重叠太厉害（简单随机）
            objects.add(new VillageObject(ObjectType.HOUSE,
                    vX + 20 + rand.nextInt(vW - 100),
                    vY + 20 + rand.nextInt(vH - 100),
                    hw, hh, Color.rgb(139, 69, 19))); // 棕色木屋
        }

        // 4. 生成树木和水井 (Decorations)
        for (int i = 0; i < 15; i++) {
            objects.add(new VillageObject(ObjectType.TREE,
                    vX + rand.nextInt(vW), vY + rand.nextInt(vH), 100, 140, Color.GREEN));
        }

        // 中心水井
        objects.add(new VillageObject(ObjectType.WELL, mapCenterX - 200, mapCenterY - 200, 100, 100, Color.GRAY));
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
            // 简单的视锥剔除：如果物体完全不在屏幕内就不画
            if (obj.x + obj.width < cameraX || obj.x > cameraX + canvas.getWidth() ||
                    obj.y + obj.height < cameraY || obj.y > cameraY + canvas.getHeight()) {
                continue;
            }

            float drawX = obj.x - cameraX;
            float drawY = obj.y - cameraY;

            switch (obj.type) {
                case HOUSE:
                    drawHouse(canvas, paint, drawX, drawY, obj.width, obj.height);
                    break;
                case TREE:
                    drawTree(canvas, paint, drawX, drawY, obj.width, obj.height);
                    break;
                case WELL:
                    drawWell(canvas, paint, drawX, drawY, obj.width, obj.height);
                    break;
                case ROAD:
                    paint.setColor(obj.color);
                    canvas.drawRect(drawX, drawY, drawX + obj.width, drawY + obj.height, paint);
                    break;
            }
        }
    }

    private void drawHouse(Canvas canvas, Paint paint, float x, float y, int w, int h) {
        // 墙体
        paint.setColor(Color.rgb(200, 180, 150));
        canvas.drawRect(x, y + h/3, x + w, y + h, paint);

        // 屋顶 (三角形)
        paint.setColor(Color.rgb(100, 50, 50)); // 红瓦
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(x - 10, y + h/3);
        path.lineTo(x + w/2, y);
        path.lineTo(x + w + 10, y + h/3);
        path.close();
        canvas.drawPath(path, paint);

        // 门
        paint.setColor(Color.rgb(80, 40, 20));
        canvas.drawRect(x + w/2 - 30, y + h - 40, x + w/2 + 30, y + h, paint);
    }

    private void drawTree(Canvas canvas, Paint paint, float x, float y, int w, int h) {
        // 树干
        paint.setColor(Color.rgb(101, 67, 33));
        canvas.drawRect(x + w/2 - 5, y + h/2, x + w/2 + 5, y + h, paint);

        // 树叶 (圆形)
        paint.setColor(Color.rgb(34, 139, 34));
        canvas.drawCircle(x + w/2, y + h/3, w/1.5f, paint);
    }

    private void drawWell(Canvas canvas, Paint paint, float x, float y, int w, int h) {
        paint.setColor(Color.GRAY);
        canvas.drawOval(x, y, x + w, y + h, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x + w/2, y + h/2, 10, paint); // 水面反光
    }

    public Rect getVillageBounds(){
        return villageBounds;
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

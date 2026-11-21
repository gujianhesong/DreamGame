package com.game.dream.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.bean.MapInfo;

/**
 * 将整张地图缩略渲染为位图，供小地图与大地图面板共用。
 */
public final class MapOverviewBitmap {

    private MapOverviewBitmap() {
    }

    public static Bitmap render(MapInfo map, int mapWidth, int mapHeight, int tileSize, int outputSize) {
        if (map == null || map.getMapData() == null || outputSize <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);

        float scale = (float) outputSize / Math.max(mapWidth, mapHeight);
        Paint paint = new Paint();
        paint.setAntiAlias(false);

        int mapTilesX = mapWidth / tileSize;
        int mapTilesY = mapHeight / tileSize;
        int step = Math.max(1, Math.max(mapTilesX, mapTilesY) / 200);
        float minimapTileSize = tileSize * scale * step;

        for (int y = 0; y < mapTilesY; y += step) {
            for (int x = 0; x < mapTilesX; x += step) {
                int terrain = map.getMapData()[y][x];
                paint.setColor(terrainColor(terrain));

                float minimapX = x * tileSize * scale;
                float minimapY = y * tileSize * scale;
                float drawWidth = minimapTileSize + 1;
                float drawHeight = minimapTileSize + 1;
                canvas.drawRect(minimapX, minimapY, minimapX + drawWidth, minimapY + drawHeight, paint);
            }
        }
        return bitmap;
    }

    public static int colorForTerrain(int terrain) {
        return terrainColor(terrain);
    }

    private static int terrainColor(int terrain) {
        switch (terrain) {
            case 0:
                return Color.rgb(210, 180, 140);
            case 1:
                return Color.rgb(34, 139, 34);
            case 2:
                return Color.rgb(0, 100, 0);
            case 3:
                return Color.rgb(30, 144, 255);
            case 4:
                return Color.rgb(255, 250, 250);
            case 5:
                return Color.rgb(85, 107, 47);
            case 6:
                return Color.rgb(255, 69, 0);
            case 7:
                return Color.rgb(50, 120, 220);
            case 8:
                return Color.rgb(110, 100, 90);
            case 9:
                return Color.rgb(160, 140, 100);
            case 10:
                return Color.rgb(180, 200, 100);
            case 11:
                return Color.rgb(190, 180, 160);
            case 12:
                return Color.rgb(100, 95, 90);
            case 13:
                return Color.rgb(235, 215, 160);
            case 14:
                return Color.rgb(20, 80, 170);
            case 15:
                return Color.rgb(8, 30, 80);
            case 16:
                return Color.rgb(25, 100, 150);
            case 17:
                return Color.rgb(45, 130, 150);
            case 18:
                return Color.rgb(15, 70, 95);
            case 19:
                return Color.rgb(55, 25, 25);
            case 20:
                return Color.rgb(70, 115, 135);
            case 21:
                return Color.rgb(60, 45, 55);
            case 22:
                return Color.rgb(90, 70, 40);
            case 23:
                return Color.rgb(120, 110, 105);
            case 24:
                return Color.rgb(140, 30, 40);
            case 25:
                return Color.rgb(45, 35, 50);
            case 26:
                return Color.rgb(85, 70, 90);
            case 27:
                return Color.rgb(120, 20, 15);
            case 28:
                return Color.rgb(170, 165, 150);
            case 100:
                return Color.rgb(215, 162, 109);
            case 200:
                return Color.rgb(85, 80, 95);
            case 201:
                return Color.rgb(160, 150, 130);
            case 202:
                return Color.rgb(60, 120, 255);
            case 203:
                return Color.rgb(255, 200, 50);
            default:
                return Color.GRAY;
        }
    }
}
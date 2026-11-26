package com.game.dream.npc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.game.dream.enums.NpcType;

/**
 * 扩展行当 NPC 的服饰与五官绘制（与 {@link Npc} 中经典类型并列）。
 */
public final class ExtendedNpcAppearance {

    private ExtendedNpcAppearance() {
    }

    public static boolean isExtendedType(NpcType type) {
        if (type == null) {
            return false;
        }
        switch (type) {
            case MAGISTRATE_CLERK:
            case PREFECT:
            case YAMEN_RUNNER:
            case BLACKSMITH:
            case CRAFTSMAN:
            case ENTERTAINER:
            case NIGHT_WATCH:
            case BOATMAN:
            case STATION_MASTER:
            case SWORDSMAN:
            case WANDERER:
            case REALM_ATTENDANT:
            case VILLAGE_ELDER:
                return true;
            default:
                return false;
        }
    }

    public static void drawBody(NpcType type, Canvas canvas, Paint paint, RectF bodyRect, float cx, float cy, int size) {
        switch (type) {
            case MAGISTRATE_CLERK:
                paint.setColor(Color.rgb(70, 95, 140));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                paint.setColor(Color.rgb(200, 170, 60));
                canvas.drawRect(cx - size / 8f, cy - size / 10f, cx + size / 8f, cy + size / 10f, paint);
                break;
            case PREFECT:
                paint.setColor(Color.rgb(130, 25, 35));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                paint.setColor(Color.rgb(210, 180, 70));
                canvas.drawRect(bodyRect.left, bodyRect.top + 6, bodyRect.right, bodyRect.top + 11, paint);
                break;
            case YAMEN_RUNNER:
                paint.setColor(Color.rgb(45, 65, 95));
                canvas.drawRoundRect(bodyRect, 8, 8, paint);
                paint.setColor(Color.rgb(200, 50, 40));
                canvas.drawRect(bodyRect.left, cy - 2, bodyRect.right, cy + 3, paint);
                break;
            case BLACKSMITH:
                paint.setColor(Color.rgb(75, 75, 80));
                canvas.drawRoundRect(bodyRect, 8, 8, paint);
                paint.setColor(Color.rgb(160, 100, 50));
                canvas.drawRect(bodyRect.left, cy - 2, bodyRect.right, cy + 4, paint);
                break;
            case CRAFTSMAN:
                paint.setColor(Color.rgb(90, 110, 130));
                canvas.drawRoundRect(bodyRect, 8, 8, paint);
                break;
            case ENTERTAINER:
                paint.setColor(Color.rgb(180, 80, 140));
                canvas.drawRoundRect(bodyRect, 14, 14, paint);
                paint.setColor(Color.rgb(255, 215, 180));
                canvas.drawRect(cx - size / 5f, cy + size / 8f, cx + size / 5f, cy + size / 3f, paint);
                break;
            case NIGHT_WATCH:
                paint.setColor(Color.rgb(35, 40, 65));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
            case BOATMAN:
                paint.setColor(Color.rgb(60, 90, 110));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
            case STATION_MASTER:
                paint.setColor(Color.rgb(120, 95, 70));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
            case SWORDSMAN:
                paint.setColor(Color.rgb(55, 70, 90));
                canvas.drawRoundRect(bodyRect, 8, 8, paint);
                break;
            case WANDERER:
                paint.setColor(Color.rgb(140, 120, 90));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
            case REALM_ATTENDANT:
                paint.setColor(Color.rgb(75, 55, 120));
                canvas.drawRoundRect(bodyRect, 12, 12, paint);
                paint.setColor(Color.rgb(180, 160, 255));
                canvas.drawCircle(cx, cy - size / 6f, 7, paint);
                break;
            case VILLAGE_ELDER:
                paint.setColor(Color.rgb(150, 130, 100));
                canvas.drawRoundRect(bodyRect, 10, 10, paint);
                break;
            default:
                break;
        }
    }

    public static void drawFeet(NpcType type, Canvas canvas, Paint paint, float cx, float cy, int size,
                                float footY, float footWidth, float footHeight) {
        switch (type) {
            case MAGISTRATE_CLERK:
            case PREFECT:
            case VILLAGE_ELDER:
            case WANDERER:
            case STATION_MASTER:
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;
            case YAMEN_RUNNER:
            case SWORDSMAN:
                paint.setColor(Color.rgb(101, 67, 33));
                canvas.drawRect(cx - footWidth * 1.3f, footY - footHeight / 2, cx - footWidth * 0.3f, footY + footHeight, paint);
                canvas.drawRect(cx + footWidth * 0.3f, footY - footHeight / 2, cx + footWidth * 1.3f, footY + footHeight, paint);
                break;
            case BLACKSMITH:
            case CRAFTSMAN:
            case BOATMAN:
                paint.setColor(Color.rgb(80, 50, 30));
                canvas.drawRect(cx - size / 4.5f, cy + size / 3.5f, cx - size / 10f, cy + size / 2.2f, paint);
                canvas.drawRect(cx + size / 10f, cy + size / 3.5f, cx + size / 4.5f, cy + size / 2.2f, paint);
                break;
            case ENTERTAINER:
                paint.setColor(Color.rgb(255, 140, 160));
                canvas.drawOval(cx - footWidth, footY + footHeight / 2, cx, footY + footHeight * 1.2f, paint);
                canvas.drawOval(cx, footY + footHeight / 2, cx + footWidth, footY + footHeight * 1.2f, paint);
                break;
            case NIGHT_WATCH:
            case REALM_ATTENDANT:
                paint.setColor(Color.DKGRAY);
                canvas.drawOval(cx - footWidth * 1.2f, footY, cx - footWidth * 0.2f, footY + footHeight, paint);
                canvas.drawOval(cx + footWidth * 0.2f, footY, cx + footWidth * 1.2f, footY + footHeight, paint);
                break;
            default:
                break;
        }
    }

    public static void drawAccessories(NpcType type, Canvas canvas, Paint paint, float cx, float cy, int size) {
        switch (type) {
            case MAGISTRATE_CLERK:
                paint.setColor(Color.rgb(30, 30, 35));
                canvas.drawRect(cx - size / 3f, cy + size / 6f, cx - size / 5f, cy + size / 3f, paint);
                paint.setColor(Color.rgb(200, 170, 60));
                canvas.drawRect(cx - size / 3.2f, cy + size / 5f, cx - size / 4.8f, cy + size / 4f, paint);
                break;
            case PREFECT:
                paint.setColor(Color.rgb(40, 40, 50));
                canvas.drawRect(cx - size / 4f, cy + size / 8f, cx + size / 4f, cy + size / 6f, paint);
                paint.setColor(Color.rgb(220, 190, 80));
                canvas.drawCircle(cx, cy + size / 5f, 5, paint);
                break;
            case YAMEN_RUNNER:
                paint.setColor(Color.rgb(160, 140, 60));
                canvas.drawRect(cx - size / 3f, cy + size / 12f, cx + size / 3f, cy + size / 8f, paint);
                paint.setStrokeWidth(4);
                paint.setColor(Color.rgb(90, 70, 50));
                canvas.drawLine(cx + size / 2.5f, cy - size / 3f, cx + size / 2.2f, cy + size / 3f, paint);
                paint.setStrokeWidth(1);
                break;
            case BLACKSMITH:
                paint.setStrokeWidth(5);
                paint.setColor(Color.rgb(120, 120, 130));
                canvas.drawLine(cx + size / 3f, cy - size / 4f, cx + size / 1.3f, cy + size / 4f, paint);
                paint.setColor(Color.rgb(80, 80, 85));
                canvas.drawRect(cx + size / 1.5f, cy + size / 6f, cx + size / 1.25f, cy + size / 4f, paint);
                paint.setStrokeWidth(1);
                break;
            case CRAFTSMAN:
                paint.setColor(Color.rgb(130, 130, 140));
                canvas.drawRect(cx + size / 3f, cy, cx + size / 1.6f, cy + size / 3f, paint);
                break;
            case ENTERTAINER:
                paint.setColor(Color.rgb(160, 60, 120));
                canvas.drawOval(cx - size / 2.2f, cy - size / 6f, cx - size / 4f, cy + size / 6f, paint);
                paint.setColor(Color.BLACK);
                for (int i = 0; i < 4; i++) {
                    float sx = cx - size / 2f + i * 6;
                    canvas.drawLine(sx, cy - size / 8f, sx + 2, cy + size / 10f, paint);
                }
                break;
            case NIGHT_WATCH:
                paint.setColor(Color.argb(220, 255, 210, 120));
                canvas.drawCircle(cx - size / 2.5f, cy - size / 4f, 9, paint);
                paint.setColor(Color.rgb(200, 120, 40));
                canvas.drawRect(cx - size / 2.7f, cy - size / 4f, cx - size / 2.2f, cy - size / 6f, paint);
                break;
            case BOATMAN:
                paint.setStrokeWidth(4);
                paint.setColor(Color.rgb(139, 90, 43));
                canvas.drawLine(cx + size / 2.5f, cy - size / 3f, cx + size / 1.4f, cy + size / 3f, paint);
                paint.setColor(Color.rgb(90, 60, 40));
                canvas.drawOval(cx + size / 1.55f, cy + size / 5f, cx + size / 1.25f, cy + size / 3f, paint);
                paint.setStrokeWidth(1);
                break;
            case STATION_MASTER:
                paint.setStrokeWidth(3);
                paint.setColor(Color.rgb(139, 69, 19));
                canvas.drawLine(cx + size / 2f, cy - size / 5f, cx + size / 1.15f, cy + size / 5f, paint);
                break;
            case SWORDSMAN:
                paint.setStrokeWidth(3);
                paint.setColor(Color.rgb(180, 180, 200));
                canvas.drawLine(cx + size / 2.2f, cy + size / 4f, cx + size / 1.5f, cy - size / 3f, paint);
                paint.setColor(Color.rgb(120, 90, 60));
                canvas.drawRect(cx + size / 2.3f, cy + size / 5f, cx + size / 2f, cy + size / 2.5f, paint);
                paint.setStrokeWidth(1);
                break;
            case WANDERER:
                paint.setColor(Color.rgb(139, 69, 19));
                canvas.drawOval(cx - size / 2.5f, cy + size / 6f, cx - size / 4f, cy + size / 2.5f, paint);
                break;
            case REALM_ATTENDANT:
                paint.setColor(Color.argb(100, 160, 120, 255));
                canvas.drawCircle(cx, cy, size / 2.2f, paint);
                paint.setColor(Color.rgb(90, 200, 220));
                canvas.drawCircle(cx + size / 3f, cy - size / 4f, 5, paint);
                break;
            case VILLAGE_ELDER:
                paint.setStrokeWidth(4);
                paint.setColor(Color.rgb(101, 67, 33));
                canvas.drawLine(cx + size / 2f, cy, cx + size / 2f, cy + size / 1.5f, paint);
                paint.setStrokeWidth(1);
                break;
            default:
                break;
        }
    }

    public static void drawHair(NpcType type, Canvas canvas, Paint paint, float cx, float cy, float radius) {
        paint.setColor(Color.BLACK);
        switch (type) {
            case MAGISTRATE_CLERK:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                paint.setColor(Color.rgb(35, 35, 40));
                canvas.drawRect(cx - radius * 1.1f, cy - radius * 1.35f, cx + radius * 1.1f, cy - radius * 0.85f, paint);
                break;
            case PREFECT:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                paint.setColor(Color.rgb(25, 25, 30));
                canvas.drawRect(cx - radius * 1.3f, cy - radius * 1.5f, cx + radius * 1.3f, cy - radius * 1.1f, paint);
                paint.setColor(Color.rgb(210, 180, 70));
                canvas.drawRect(cx - radius * 1.3f, cy - radius * 1.12f, cx + radius * 1.3f, cy - radius * 1.08f, paint);
                break;
            case YAMEN_RUNNER:
            case SWORDSMAN:
                canvas.drawRect(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 2, paint);
                break;
            case BLACKSMITH:
            case CRAFTSMAN:
            case BOATMAN:
            case STATION_MASTER:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                paint.setColor(Color.rgb(60, 40, 20));
                canvas.drawRect(cx - radius * 1.15f, cy - radius * 0.95f, cx + radius * 1.15f, cy - radius * 0.75f, paint);
                break;
            case ENTERTAINER:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                canvas.drawCircle(cx - radius * 0.65f, cy - radius * 1.15f, radius / 3.2f, paint);
                canvas.drawCircle(cx + radius * 0.65f, cy - radius * 1.15f, radius / 3.2f, paint);
                paint.setColor(Color.rgb(255, 100, 150));
                canvas.drawOval(cx - radius / 2, cy - radius * 1.35f, cx + radius / 2, cy - radius * 0.9f, paint);
                break;
            case NIGHT_WATCH:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                paint.setColor(Color.rgb(40, 45, 60));
                canvas.drawRect(cx - radius, cy - radius * 1.05f, cx + radius, cy - radius * 0.7f, paint);
                break;
            case WANDERER:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                canvas.drawLine(cx + radius / 2, cy - radius, cx + radius * 1.1f, cy - radius * 1.15f, paint);
                break;
            case REALM_ATTENDANT:
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                paint.setColor(Color.rgb(130, 100, 200));
                canvas.drawCircle(cx, cy - radius * 1.25f, radius / 2.5f, paint);
                break;
            case VILLAGE_ELDER:
                paint.setColor(Color.rgb(180, 175, 165));
                canvas.drawArc(cx - radius, cy - radius * 1.1f, cx + radius, cy - radius / 3, 180, 180, true, paint);
                break;
            default:
                break;
        }
        paint.setStyle(Paint.Style.FILL);
    }

    public static void drawFace(NpcType type, Canvas canvas, Paint paint, float cx, float cy, float radius) {
        paint.setStyle(Paint.Style.FILL);
        switch (type) {
            case MAGISTRATE_CLERK:
            case WANDERER:
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 4, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 4, paint);
                canvas.drawLine(cx - radius / 4, cy - radius / 5, cx + radius / 4, cy - radius / 5, paint);
                canvas.drawOval(cx - radius / 4, cy + radius / 3, cx + radius / 4, cy + radius / 2.5f, paint);
                break;
            case PREFECT:
                paint.setColor(Color.BLACK);
                canvas.drawOval(cx - radius / 1.8f, cy - radius / 3, cx - radius / 4, cy - radius / 6, paint);
                canvas.drawOval(cx + radius / 4, cy - radius / 3, cx + radius / 1.8f, cy - radius / 6, paint);
                paint.setStrokeWidth(2);
                for (int i = 0; i < 4; i++) {
                    float ox = cx - radius / 2.5f + i * radius / 4f;
                    canvas.drawLine(ox, cy + radius / 2, ox, cy + radius * 1.2f, paint);
                }
                paint.setStrokeWidth(1);
                break;
            case YAMEN_RUNNER:
            case SWORDSMAN:
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 7, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 7, paint);
                paint.setStrokeWidth(2);
                canvas.drawLine(cx - radius / 2, cy + radius / 3, cx + radius / 2, cy + radius / 3, paint);
                paint.setStrokeWidth(1);
                break;
            case BLACKSMITH:
            case CRAFTSMAN:
            case BOATMAN:
            case STATION_MASTER:
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 8, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 8, paint);
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 3, cy + radius / 5, cx + radius / 3, cy + radius / 1.5f, 200, 140, false, paint);
                paint.setStrokeWidth(1);
                break;
            case ENTERTAINER:
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 6, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 6, paint);
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 3, cy + radius / 4, cx + radius / 3, cy + radius / 1.5f, 200, 140, false, paint);
                paint.setStrokeWidth(1);
                break;
            case NIGHT_WATCH:
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 9, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 9, paint);
                break;
            case REALM_ATTENDANT:
                paint.setColor(Color.rgb(80, 60, 160));
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 6, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 6, paint);
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 14, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 14, paint);
                break;
            case VILLAGE_ELDER:
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cx - radius / 2.5f, cy - radius / 5, radius / 8, paint);
                canvas.drawCircle(cx + radius / 2.5f, cy - radius / 5, radius / 8, paint);
                paint.setStrokeWidth(2);
                canvas.drawArc(cx - radius / 3, cy + radius / 5, cx + radius / 3, cy + radius / 1.5f, 210, 120, false, paint);
                paint.setStrokeWidth(1);
                break;
            default:
                break;
        }
    }
}
package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 地狱守卫 - 地狱迷宫第4层阎罗殿的4名守卫BOSS
 * 通过 GuardType 区分牛头、马面、黑无常、白无常，共享 BOSS 属性框架，
 * 攻击方式与视觉根据类型定制。
 *
 * 牛头(COW_HEAD):    巨斧近战，ARC + CHARGE，物理爆发型
 * 马面(HORSE_FACE):  长枪近战，RECT + COMBO，多段连击型
 * 黑无常(BLACK):     锁链法术，投掷通用火球 + 幽绿鬼火光环
 * 白无常(WHITE):     哭丧棒法术，投掷通用火球 + 白雾飘散
 *
 * 属性: 各类型均强制 BOSS 等级，HP高、攻击高、防御中偏高、魔抗分化
 */
public class HellGuardian extends Enemy {

    public enum GuardType {
        COW_HEAD,    // 牛头
        HORSE_FACE,  // 马面
        BLACK,       // 黑无常
        WHITE        // 白无常
    }

    private final GuardType type;
    private float animPhase = (float) (Math.random() * Math.PI * 2);
    private float auraPhase = 0f;

    public HellGuardian(float x, float y, GuardType type) {
        super(x, y, 90);
        this.type = type;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 520;
        prop.rewardExp = 6000;
        prop.rewardMoney = 3000;

        switch (type) {
            case COW_HEAD:
                // 牛头：巨斧近战，物理爆发
                attackCooldown = 2000;
                setAttackShape(AttackShape.ARC);
                addAvailableAttackType(AttackType.MELEE);
                addAvailableAttackType(AttackType.CHARGE);
                chargeSpeedMultiplier = 4.2f;
                windUpDuration = 600;
                prop.attackRange = 220;
                setProperty(3000, 780, 440, 160, 260);
                break;
            case HORSE_FACE:
                // 马面：长枪连刺
                attackCooldown = 1700;
                setAttackShape(AttackShape.RECT);
                addAvailableAttackType(AttackType.COMBO);
                comboHitCount = 4;
                comboHitInterval = 240;
                windUpDuration = 500;
                prop.attackRange = 260;
                setProperty(2600, 620, 400, 190, 300);
                break;
            case BLACK:
                // 黑无常：锁链法术
                attackCooldown = 2400;
                setAttackShape(AttackShape.ARC);
                addAvailableAttackType(AttackType.MELEE);
                windUpDuration = 650;
                prop.attackRange = 300;
                setProperty(2400, 560, 380, 150, 700);
                break;
            case WHITE:
            default:
                // 白无常：哭丧棒法术
                attackCooldown = 2400;
                setAttackShape(AttackShape.ARC);
                addAvailableAttackType(AttackType.MELEE);
                windUpDuration = 650;
                prop.attackRange = 300;
                setProperty(2400, 560, 380, 150, 700);
                break;
        }
        setPropertyExtra(prop);

        // 强制 BOSS
        enemyLevel = EnemyLevel.BOSS;
        size = size * 3;
        setProperty(maxHealth * 28, attackDamage * 4, defense * 5, speed * 4, mana * 4);
    }

    public GuardType getGuardType() {
        return type;
    }

    @Override
    public boolean usesGenericFireball() {
        // 无常使用法术，牛头马面近战为主但也可偶尔投掷
        return type == GuardType.BLACK || type == GuardType.WHITE;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;
        animPhase += 0.003f * deltaTime;
        auraPhase += 0.004f * deltaTime;
    }

    @Override
    protected void performAttack() {
        // 由 GameEngine 处理
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel5(this);
        }
        return possibleDrops;
    }

    // ==================== 绘制 ====================

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        paint.setAntiAlias(true);
        float scale = size / 30.0f;
        long now = System.currentTimeMillis();

        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = now - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 3 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY;
        boolean facingRight = targetX > x;
        float dir = facingRight ? 1f : -1f;

        // 无常专属光环
        if (type == GuardType.BLACK || type == GuardType.WHITE) {
            drawAura(canvas, paint, cx, cy, scale, now);
        }

        drawBody(canvas, paint, cx, cy, scale, now, dir);
        drawHead(canvas, paint, cx, cy, scale, now, dir);
        drawWeapon(canvas, paint, cx, cy, scale, now, dir);

        if (currentState == State.ATTACKING && isWindingUp) {
            drawAttackEffect(canvas, paint, cx, cy, scale, dir);
        }
    }

    private void drawAura(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        boolean isBlack = type == GuardType.BLACK;
        int r = isBlack ? 60 : 220;
        int g = isBlack ? 200 : 220;
        int b = isBlack ? 80 : 230;
        for (int i = 0; i < 4; i++) {
            float angle = auraPhase + i * (float) (Math.PI / 2);
            float ax = cx + (float) Math.cos(angle) * 22 * scale;
            float ay = cy + (float) Math.sin(angle) * 12 * scale;
            float pulse = 0.5f + 0.4f * (float) Math.sin(now / 400.0 + i);
            paint.setColor(Color.argb((int) (80 * pulse), r, g, b));
            canvas.drawCircle(ax, ay, 5 * scale, paint);
            paint.setColor(Color.argb((int) (140 * pulse), r, g, b));
            canvas.drawCircle(ax, ay, 2.5f * scale, paint);
        }
    }

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 躯干配色
        int bodyR, bodyG, bodyB;
        switch (type) {
            case COW_HEAD:   bodyR = 90;  bodyG = 60;  bodyB = 40;  break; // 牛头：棕褐肌肉
            case HORSE_FACE: bodyR = 100; bodyG = 80;  bodyB = 65;  break; // 马面：土黄肌肉
            case BLACK:      bodyR = 40;  bodyG = 40;  bodyB = 55;  break; // 黑无常：黑袍
            case WHITE:
            default:         bodyR = 220; bodyG = 220; bodyB = 225; break; // 白无常：白袍
        }
        paint.setColor(Color.argb(240, bodyR, bodyG, bodyB));
        Path body = new Path();
        body.moveTo(cx - 14 * scale, cy - 10 * scale);
        body.lineTo(cx + 14 * scale, cy - 10 * scale);
        body.lineTo(cx + 16 * scale, cy + 6 * scale);
        body.lineTo(cx + 12 * scale, cy + 20 * scale);
        body.lineTo(cx - 12 * scale, cy + 20 * scale);
        body.lineTo(cx - 16 * scale, cy + 6 * scale);
        body.close();
        canvas.drawPath(body, paint);

        // 腰带/饰带
        if (type == GuardType.BLACK || type == GuardType.WHITE) {
            paint.setColor(Color.argb(240, type == GuardType.BLACK ? 180 : 60,
                    type == GuardType.BLACK ? 40 : 60,
                    type == GuardType.BLACK ? 40 : 70));
            canvas.drawRect(cx - 13 * scale, cy + 4 * scale, cx + 13 * scale, cy + 8 * scale, paint);
        } else {
            // 牛头马面：胸甲皮带
            paint.setColor(Color.argb(220, 60, 45, 30));
            canvas.drawRect(cx - 14 * scale, cy - 2 * scale, cx + 14 * scale, cy + 2 * scale, paint);
        }
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        float headY = cy - 18 * scale;

        switch (type) {
            case COW_HEAD: {
                // 牛头：棕褐头颅 + 双弯角 + 鼻环
                paint.setColor(Color.argb(240, 105, 75, 50));
                canvas.drawOval(cx - 8 * scale, headY - 7 * scale,
                        cx + 8 * scale, headY + 8 * scale, paint);
                // 双弯角
                paint.setColor(Color.argb(245, 230, 220, 190));
                for (int s = -1; s <= 1; s += 2) {
                    Path horn = new Path();
                    horn.moveTo(cx + s * 6 * scale, headY - 5 * scale);
                    horn.quadTo(cx + s * 14 * scale, headY - 12 * scale,
                            cx + s * 12 * scale, headY - 2 * scale);
                    horn.quadTo(cx + s * 10 * scale, headY - 8 * scale,
                            cx + s * 5 * scale, headY - 3 * scale);
                    horn.close();
                    canvas.drawPath(horn, paint);
                }
                // 血红双目
                float glow = 0.7f + 0.3f * (float) Math.sin(now / 400.0);
                paint.setColor(Color.argb((int) (255 * glow), 255, 60, 40));
                canvas.drawCircle(cx - 3 * scale, headY - 1 * scale, 1.5f * scale, paint);
                canvas.drawCircle(cx + 3 * scale, headY - 1 * scale, 1.5f * scale, paint);
                // 鼻孔 + 鼻环
                paint.setColor(Color.argb(240, 30, 20, 15));
                canvas.drawCircle(cx - 2 * scale, headY + 4 * scale, 1 * scale, paint);
                canvas.drawCircle(cx + 2 * scale, headY + 4 * scale, 1 * scale, paint);
                paint.setColor(Color.argb(240, 200, 180, 60));
                paint.setStrokeWidth(1 * scale);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(cx, headY + 6 * scale, 2 * scale, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                break;
            }
            case HORSE_FACE: {
                // 马面：土黄长脸 + 鬃毛 + 长耳
                paint.setColor(Color.argb(240, 130, 105, 80));
                Path face = new Path();
                face.moveTo(cx - 6 * scale, headY - 6 * scale);
                face.lineTo(cx + 6 * scale, headY - 6 * scale);
                face.lineTo(cx + 4 * scale, headY + 10 * scale);
                face.lineTo(cx - 4 * scale, headY + 10 * scale);
                face.close();
                canvas.drawPath(face, paint);
                // 鬃毛（黑褐色）
                paint.setColor(Color.argb(240, 40, 30, 25));
                for (int i = -3; i <= 3; i++) {
                    float wave = (float) Math.sin(now / 500.0 + i) * 2 * scale;
                    canvas.drawLine(cx + i * 1.8f * scale, headY - 6 * scale,
                            cx + i * 2.5f * scale + wave, headY - 14 * scale, paint);
                }
                // 长耳
                for (int s = -1; s <= 1; s += 2) {
                    Path ear = new Path();
                    ear.moveTo(cx + s * 5 * scale, headY - 5 * scale);
                    ear.lineTo(cx + s * 8 * scale, headY - 14 * scale);
                    ear.lineTo(cx + s * 3 * scale, headY - 6 * scale);
                    ear.close();
                    canvas.drawPath(ear, paint);
                }
                // 血红双目
                float glow = 0.7f + 0.3f * (float) Math.sin(now / 400.0);
                paint.setColor(Color.argb((int) (255 * glow), 255, 80, 40));
                canvas.drawCircle(cx - 2.5f * scale, headY - 1 * scale, 1.3f * scale, paint);
                canvas.drawCircle(cx + 2.5f * scale, headY - 1 * scale, 1.3f * scale, paint);
                // 长口鼻
                paint.setColor(Color.argb(240, 30, 20, 15));
                canvas.drawOval(cx - 2.5f * scale, headY + 6 * scale,
                        cx + 2.5f * scale, headY + 9 * scale, paint);
                break;
            }
            case BLACK:
            case WHITE: {
                boolean isBlack = type == GuardType.BLACK;
                // 无常：高瘦苍白面孔 + 长舌（黑）/长帽（白）
                paint.setColor(Color.argb(240, isBlack ? 180 : 235,
                        isBlack ? 175 : 235, isBlack ? 190 : 240));
                canvas.drawOval(cx - 6 * scale, headY - 7 * scale,
                        cx + 6 * scale, headY + 8 * scale, paint);
                // 高帽
                paint.setColor(Color.argb(245, isBlack ? 25 : 240,
                        isBlack ? 25 : 240, isBlack ? 35 : 245));
                Path hat = new Path();
                hat.moveTo(cx - 7 * scale, headY - 7 * scale);
                hat.lineTo(cx + 7 * scale, headY - 7 * scale);
                hat.lineTo(cx + 5 * scale, headY - 22 * scale);
                hat.lineTo(cx - 5 * scale, headY - 22 * scale);
                hat.close();
                canvas.drawPath(hat, paint);
                // 帽上字（黑：一见生财；白：天下太平）简化为一道金/黑横纹
                paint.setColor(Color.argb(240, isBlack ? 220 : 40,
                        isBlack ? 180 : 40, isBlack ? 70 : 50));
                canvas.drawRect(cx - 4 * scale, headY - 16 * scale,
                        cx + 4 * scale, headY - 13 * scale, paint);

                // 幽光双目
                float glow = 0.7f + 0.3f * (float) Math.sin(now / 450.0);
                int eyeR = isBlack ? 100 : 200;
                int eyeG = isBlack ? 240 : 200;
                int eyeB = isBlack ? 100 : 220;
                paint.setColor(Color.argb((int) (240 * glow), eyeR, eyeG, eyeB));
                canvas.drawCircle(cx - 2.5f * scale, headY - 1 * scale, 1.4f * scale, paint);
                canvas.drawCircle(cx + 2.5f * scale, headY - 1 * scale, 1.4f * scale, paint);

                // 长舌（黑无常）/ 微笑（白无常）
                if (isBlack) {
                    paint.setColor(Color.argb(240, 200, 60, 60));
                    Path tongue = new Path();
                    tongue.moveTo(cx - 1.5f * scale, headY + 4 * scale);
                    tongue.quadTo(cx, headY + 12 * scale, cx + 1.5f * scale, headY + 4 * scale);
                    tongue.close();
                    canvas.drawPath(tongue, paint);
                } else {
                    paint.setColor(Color.argb(220, 60, 40, 50));
                    paint.setStrokeWidth(1 * scale);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawArc(cx - 3 * scale, headY + 2 * scale,
                            cx + 3 * scale, headY + 6 * scale, 0, 180, false, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setStrokeWidth(1);
                }
                break;
            }
        }
    }

    private void drawWeapon(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        float swing = 0;
        if (currentState == State.ATTACKING && isWindingUp) {
            swing = -getWindUpProgress() * 20 * scale;
        }
        float wx = cx + dir * 18 * scale;
        float wy = cy - 4 * scale + swing * 0.4f;

        switch (type) {
            case COW_HEAD: {
                // 巨斧（长柄 + 大斧刃）
                paint.setColor(Color.argb(245, 80, 55, 30));
                paint.setStrokeWidth(3 * scale);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(wx, wy + 18 * scale, wx, wy - 14 * scale, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                // 斧刃（银亮）
                paint.setColor(Color.argb(245, 200, 205, 215));
                Path blade = new Path();
                blade.moveTo(wx, wy - 12 * scale);
                blade.lineTo(wx + dir * 14 * scale, wy - 18 * scale);
                blade.lineTo(wx + dir * 16 * scale, wy - 6 * scale);
                blade.lineTo(wx, wy - 4 * scale);
                blade.close();
                canvas.drawPath(blade, paint);
                // 刃上血槽
                paint.setColor(Color.argb(200, 140, 20, 20));
                canvas.drawRect(wx + dir * 3 * scale, wy - 14 * scale,
                        wx + dir * 12 * scale, wy - 12 * scale, paint);
                break;
            }
            case HORSE_FACE: {
                // 长枪（长杆 + 银枪头 + 红缨）
                paint.setColor(Color.argb(245, 90, 65, 35));
                paint.setStrokeWidth(2.2f * scale);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(wx, wy + 22 * scale, wx + dir * 2 * scale, wy - 22 * scale, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                // 枪头
                paint.setColor(Color.argb(245, 220, 225, 235));
                Path spear = new Path();
                spear.moveTo(wx + dir * 2 * scale, wy - 22 * scale);
                spear.lineTo(wx + dir * 5 * scale, wy - 30 * scale);
                spear.lineTo(wx - dir * 1 * scale, wy - 30 * scale);
                spear.close();
                canvas.drawPath(spear, paint);
                // 红缨
                paint.setColor(Color.argb(230, 200, 40, 40));
                for (int i = 0; i < 4; i++) {
                    float wave = (float) Math.sin(now / 400.0 + i) * 2 * scale;
                    canvas.drawLine(wx + dir * 2 * scale, wy - 20 * scale,
                            wx + dir * (4 + i) * scale + wave, wy - 16 * scale, paint);
                }
                break;
            }
            case BLACK: {
                // 锁链（暗铁色节节下垂）
                paint.setColor(Color.argb(240, 60, 60, 70));
                paint.setStrokeWidth(2 * scale);
                paint.setStyle(Paint.Style.STROKE);
                for (int i = 0; i < 5; i++) {
                    float lx = wx + (float) Math.sin(now / 500.0 + i * 0.5f) * 3 * scale;
                    canvas.drawCircle(lx, wy + i * 6 * scale, 2 * scale, paint);
                }
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                // 末端钩爪
                paint.setColor(Color.argb(245, 100, 200, 100));
                Path hook = new Path();
                hook.moveTo(wx, wy + 28 * scale);
                hook.lineTo(wx + 4 * scale, wy + 34 * scale);
                hook.lineTo(wx, wy + 32 * scale);
                hook.close();
                canvas.drawPath(hook, paint);
                break;
            }
            case WHITE:
            default: {
                // 哭丧棒（白木杆 + 白纸穗）
                paint.setColor(Color.argb(245, 230, 230, 235));
                paint.setStrokeWidth(2.5f * scale);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(wx, wy + 18 * scale, wx, wy - 14 * scale, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                // 纸穗（多条飘散白纸条）
                for (int i = 0; i < 5; i++) {
                    float wave = (float) Math.sin(now / 400.0 + i * 0.7f) * 3 * scale;
                    float angle = -0.5f + i * 0.25f;
                    paint.setColor(Color.argb(220, 240, 240, 245));
                    Path strip = new Path();
                    strip.moveTo(wx, wy - 12 * scale);
                    strip.quadTo(wx + wave, wy - 6 * scale + i * 2 * scale,
                            wx + (float) Math.cos(angle) * 10 * scale,
                            wy + (float) Math.sin(angle) * 4 * scale + i * 2 * scale);
                    strip.lineTo(wx + (float) Math.cos(angle) * 10 * scale + 1 * scale,
                            wy + (float) Math.sin(angle) * 4 * scale + i * 2 * scale + 1 * scale);
                    strip.quadTo(wx + wave, wy - 5 * scale + i * 2 * scale,
                            wx + 1 * scale, wy - 12 * scale);
                    strip.close();
                    canvas.drawPath(strip, paint);
                }
                break;
            }
        }
    }

    private void drawAttackEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        float progress = getWindUpProgress();
        float angle = dir > 0 ? 0 : (float) Math.PI;

        // 依据类型使用不同颜色
        int r, g, b;
        switch (type) {
            case COW_HEAD:   r = 255; g = 180; b = 80;  break; // 土黄斧光
            case HORSE_FACE: r = 255; g = 100; b = 100; break; // 红缨枪芒
            case BLACK:      r = 100; g = 240; b = 120; break; // 幽绿鬼火
            case WHITE:
            default:         r = 240; g = 240; b = 255; break; // 惨白雾气
        }

        if (attackShape == AttackShape.RECT) {
            // 矩形枪影
            float rectW = propertyExtra.attackRange * 1.2f;
            float rectH = 60 * scale * progress;
            paint.setColor(Color.argb((int) (progress * 90), r, g, b));
            canvas.save();
            canvas.translate(cx, cy);
            if (dir < 0) canvas.scale(-1, 1);
            canvas.drawRect(0, -rectH / 2, rectW * progress, rectH / 2, paint);
            canvas.restore();
        } else {
            // 扇形
            float arcRadius = propertyExtra.attackRange * progress;
            paint.setColor(Color.argb((int) (progress * 90), r, g, b));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                    (float) Math.toDegrees(angle) - 50, 100, true, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2 * scale * progress);
            for (int i = 1; i <= 3; i++) {
                float rr = arcRadius * (i / 3f);
                paint.setColor(Color.argb((int) (progress * (180 - i * 40)), r, g, b));
                canvas.drawArc(cx - rr, cy - rr, cx + rr, cy + rr,
                        (float) Math.toDegrees(angle) - 45, 90, false, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 阎罗王 - 地狱迷宫第4层最终BOSS，森罗殿之主
 * 掌生死轮回，审判十方亡魂。凡闯至阎罗殿者，皆须受"轮回审判"，
 * 阳寿未尽者魂归幽魂牢，阳寿已尽者永堕轮回。
 *
 * 视觉: 身形魁梧，黑金龙纹冕服，十二旒冕冠垂珠遮面，
 *       面容隐于旒珠之后仅露两点金瞳，双手托生死轮盘（旋转的金黑圆盘），
 *       背后浮现巨大金色轮回法阵。
 *
 * 攻击:
 *  1. 轮回碾压 - 大范围ARC近战，附带金色法阵光效
 *  2. 生死符诏 - 中距离投掷金色符火（复用通用火球）
 *  3. 森罗召魂 - HP<50% 时召唤亡魂大军（父类机制）
 *
 * 属性: HP最高、攻击最高、双抗皆高、移速中
 */
public class KingYanluo extends Enemy {

    private float wheelPhase = 0f;
    private float haloPhase = 0f;
    private float robePhase = 0f;

    public KingYanluo(float x, float y) {
        super(x, y, 105);
        attackCooldown = 2000;
        setAttackShape(AttackShape.ARC);
        addAvailableAttackType(AttackType.MELEE);
        addAvailableAttackType(AttackType.CHARGE);
        chargeSpeedMultiplier = 4.5f;
        windUpDuration = 700;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 620;
        prop.attackRange = 300;
        prop.rewardExp = 20000;
        prop.rewardMoney = 10000;
        setPropertyExtra(prop);

        // 基础属性：全面压制
        setProperty(4000, 900, 520, 160, 800);

        // 强制 BOSS（最终形态）
        enemyLevel = EnemyLevel.BOSS;
        size = size * 3;
        setProperty(maxHealth * 35, attackDamage * 4, defense * 6, speed * 4, mana * 5);
    }

    @Override
    public boolean usesGenericFireball() {
        return true; // 生死符诏
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;
        wheelPhase += 0.002f * deltaTime;
        haloPhase += 0.0015f * deltaTime;
        robePhase += 0.0018f * deltaTime;
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

        // 1. 背后轮回法阵
        drawSamsaraHalo(canvas, paint, cx, cy, scale, now);
        // 2. 龙纹冕服
        drawRobe(canvas, paint, cx, cy, scale, now);
        // 3. 头部与十二旒冕冠
        drawHead(canvas, paint, cx, cy, scale, now);
        // 4. 生死轮盘（双手托举）
        drawSamsaraWheel(canvas, paint, cx, cy, scale, now, dir);
        // 5. 攻击特效
        if (currentState == State.ATTACKING && isWindingUp) {
            drawJudgementEffect(canvas, paint, cx, cy, scale, dir);
        }
    }

    private void drawSamsaraHalo(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 背后金色法阵（多层旋转圆环 + 六道刻痕）
        float haloY = cy - 10 * scale;
        float pulse = 0.6f + 0.3f * (float) Math.sin(now / 700.0);

        for (int ring = 0; ring < 3; ring++) {
            float r = (26 + ring * 6) * scale;
            float spin = haloPhase * (ring % 2 == 0 ? 1 : -1);
            paint.setColor(Color.argb((int) ((90 - ring * 20) * pulse), 255, 210, 90));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((2 - ring * 0.4f) * scale);
            canvas.save();
            canvas.translate(cx, haloY);
            canvas.rotate((float) Math.toDegrees(spin));
            canvas.drawCircle(0, 0, r, paint);
            // 六道刻痕
            for (int i = 0; i < 6; i++) {
                float a = i * (float) (Math.PI / 3);
                canvas.drawLine((float) Math.cos(a) * (r - 3 * scale),
                        (float) Math.sin(a) * (r - 3 * scale),
                        (float) Math.cos(a) * r,
                        (float) Math.sin(a) * r, paint);
            }
            canvas.restore();
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawRobe(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 黑金龙纹冕服
        float wave = (float) Math.sin(robePhase) * 1.5f * scale;
        paint.setColor(Color.argb(245, 15, 15, 25));
        Path robe = new Path();
        robe.moveTo(cx - 16 * scale, cy - 12 * scale);
        robe.lineTo(cx + 16 * scale, cy - 12 * scale);
        robe.lineTo(cx + 22 * scale, cy + 10 * scale);
        robe.lineTo(cx + 18 * scale + wave, cy + 26 * scale);
        robe.lineTo(cx - 18 * scale - wave, cy + 26 * scale);
        robe.lineTo(cx - 22 * scale, cy + 10 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        // 金色龙纹（简化：3道金线波浪 + 圆珠）
        paint.setColor(Color.argb(220, 240, 200, 80));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 3; i++) {
            float oy = cy + (2 + i * 6) * scale;
            Path dragon = new Path();
            dragon.moveTo(cx - 12 * scale, oy);
            dragon.quadTo(cx - 6 * scale, oy - 3 * scale, cx, oy);
            dragon.quadTo(cx + 6 * scale, oy + 3 * scale, cx + 12 * scale, oy);
            canvas.drawPath(dragon, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        // 龙睛（金珠）
        paint.setColor(Color.argb(240, 255, 220, 100));
        canvas.drawCircle(cx, cy + 4 * scale, 1.5f * scale, paint);

        // 红内衬
        paint.setColor(Color.argb(230, 150, 25, 25));
        Path inner = new Path();
        inner.moveTo(cx - 5 * scale, cy - 12 * scale);
        inner.lineTo(cx + 5 * scale, cy - 12 * scale);
        inner.lineTo(cx + 7 * scale, cy + 16 * scale);
        inner.lineTo(cx, cy + 20 * scale);
        inner.lineTo(cx - 7 * scale, cy + 16 * scale);
        inner.close();
        canvas.drawPath(inner, paint);

        // 玉带
        paint.setColor(Color.argb(245, 240, 210, 90));
        canvas.drawRect(cx - 15 * scale, cy + 2 * scale, cx + 15 * scale, cy + 6 * scale, paint);
        paint.setColor(Color.argb(245, 180, 140, 40));
        canvas.drawRect(cx - 3 * scale, cy + 1 * scale, cx + 3 * scale, cy + 7 * scale, paint);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        float headY = cy - 22 * scale;

        // 面部（隐于旒珠后，暗色）
        paint.setColor(Color.argb(240, 60, 50, 50));
        canvas.drawOval(cx - 7 * scale, headY - 6 * scale,
                cx + 7 * scale, headY + 8 * scale, paint);

        // 金瞳
        float glow = 0.8f + 0.2f * (float) Math.sin(now / 350.0);
        paint.setColor(Color.argb((int) (255 * glow), 255, 220, 90));
        canvas.drawCircle(cx - 3 * scale, headY - 1 * scale, 1.8f * scale, paint);
        canvas.drawCircle(cx + 3 * scale, headY - 1 * scale, 1.8f * scale, paint);
        paint.setColor(Color.argb((int) (120 * glow), 255, 240, 150));
        canvas.drawCircle(cx - 3 * scale, headY - 1 * scale, 3.5f * scale, paint);
        canvas.drawCircle(cx + 3 * scale, headY - 1 * scale, 3.5f * scale, paint);

        // 冕冠顶板（黑色 + 金边）
        paint.setColor(Color.argb(245, 20, 20, 30));
        canvas.drawRect(cx - 11 * scale, headY - 12 * scale,
                cx + 11 * scale, headY - 6 * scale, paint);
        paint.setColor(Color.argb(240, 240, 200, 80));
        canvas.drawRect(cx - 11 * scale, headY - 12 * scale,
                cx + 11 * scale, headY - 11 * scale, paint);
        canvas.drawRect(cx - 11 * scale, headY - 7 * scale,
                cx + 11 * scale, headY - 6 * scale, paint);
        // 冠顶金饰
        paint.setColor(Color.argb(245, 255, 230, 110));
        canvas.drawCircle(cx, headY - 13 * scale, 2 * scale, paint);

        // 十二旒珠（前后各6串垂珠）
        paint.setColor(Color.argb(230, 255, 230, 130));
        for (int i = 0; i < 6; i++) {
            float bx = cx - 10 * scale + i * 4 * scale;
            for (int j = 0; j < 4; j++) {
                float by = headY - 5 * scale + j * 3 * scale;
                float sway = (float) Math.sin(now / 500.0 + i * 0.3 + j * 0.2) * 0.8f * scale;
                canvas.drawCircle(bx + sway, by, 1 * scale, paint);
            }
        }
    }

    private void drawSamsaraWheel(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, float dir) {
        // 双手托举的生死轮盘（身前旋转的金黑圆盘）
        float wx = cx + dir * 4 * scale;
        float wy = cy + 8 * scale;
        float r = 10 * scale;

        // 轮盘外圈（金）
        paint.setColor(Color.argb(245, 240, 200, 80));
        canvas.drawCircle(wx, wy, r, paint);
        // 内圈（黑）
        paint.setColor(Color.argb(245, 20, 20, 30));
        canvas.drawCircle(wx, wy, r * 0.82f, paint);

        // 六道分割（旋转）
        canvas.save();
        canvas.translate(wx, wy);
        canvas.rotate((float) Math.toDegrees(wheelPhase));
        paint.setColor(Color.argb(240, 240, 200, 80));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 6; i++) {
            float a = i * (float) (Math.PI / 3);
            canvas.drawLine(0, 0,
                    (float) Math.cos(a) * r * 0.8f,
                    (float) Math.sin(a) * r * 0.8f, paint);
        }
        // 中心太极点
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(250, 255, 230, 110));
        canvas.drawCircle(0, 0, 2 * scale, paint);
        paint.setColor(Color.argb(250, 20, 20, 30));
        canvas.drawCircle(0, 0, 1 * scale, paint);
        canvas.restore();

        // 轮盘光晕
        float pulse = 0.5f + 0.3f * (float) Math.sin(now / 500.0);
        paint.setColor(Color.argb((int) (80 * pulse), 255, 220, 100));
        canvas.drawCircle(wx, wy, r * 1.4f, paint);
    }

    private void drawJudgementEffect(Canvas canvas, Paint paint, float cx, float cy, float scale, float dir) {
        // 轮回审判：巨大金色扇形 + 旋转法阵
        float progress = getWindUpProgress();
        float arcRadius = propertyExtra.attackRange * progress * 1.1f;
        float angle = dir > 0 ? 0 : (float) Math.PI;

        // 金色扇形
        paint.setColor(Color.argb((int) (progress * 100), 255, 220, 90));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius,
                (float) Math.toDegrees(angle) - 60, 120, true, paint);

        // 3道弧线
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i <= 3; i++) {
            float r = arcRadius * (i / 3f);
            paint.setColor(Color.argb((int) (progress * (220 - i * 50)), 255, 230 - i * 20, 100));
            paint.setStrokeWidth((3 - i * 0.5f) * scale * progress);
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r,
                    (float) Math.toDegrees(angle) - 55, 110, false, paint);
        }

        // 中央旋转小法阵
        canvas.save();
        canvas.translate(cx + dir * arcRadius * 0.5f, cy);
        canvas.rotate((float) Math.toDegrees(progress * Math.PI * 2));
        paint.setColor(Color.argb((int) (progress * 180), 255, 240, 140));
        paint.setStrokeWidth(1.5f * scale);
        canvas.drawCircle(0, 0, 12 * scale * progress, paint);
        for (int i = 0; i < 6; i++) {
            float a = i * (float) (Math.PI / 3);
            canvas.drawLine((float) Math.cos(a) * 6 * scale * progress,
                    (float) Math.sin(a) * 6 * scale * progress,
                    (float) Math.cos(a) * 12 * scale * progress,
                    (float) Math.sin(a) * 12 * scale * progress, paint);
        }
        canvas.restore();

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

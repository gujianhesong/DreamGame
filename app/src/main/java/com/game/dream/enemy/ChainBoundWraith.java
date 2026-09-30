package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.GameEngine;
import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 幽灵 - 地狱迷宫第3层枉死城主题怪物
 * 生前含冤被处决的囚魂，死后仍被阴司锁链束缚于枉死城，凡闯入者皆被视为"越狱同党"。
 *
 * 视觉: 半透明青灰人形，穿破烂囚服，脖颈诡异歪斜（暗示被处决）；
 *       全身缠绕生锈铁链，链尾拖地；面部只有两个黑洞眼窝燃烧幽绿鬼火；
 *       胸前有一道暗红致命伤口，偶尔滴落虚影血珠；下半身化为锁链状雾迹。
 *
 * 攻击:
 *  1. 锁链缠绕 - RECT 近战，铁链前甩，命中使玩家定身 1.5 秒（GameEngine 中处理）
 *  2. 怨念凝视 - 远程法术，眼窝幽绿鬼火凝聚投出（复用通用火球）
 *  3. 冤魂共鸣 - 被动，周围 300px 内每只其他缚链怨魂使其法术伤害 +8%（上限 5 层）
 *
 * 属性: HP 低、物理防御极低、魔抗极高、物理攻击低、移速中
 * 出没: 枉死城的宽巷道中，常成群出现
 */
public class ChainBoundWraith extends Enemy {

    // 冤魂共鸣
    private int resonanceStacks = 0;         // 当前层数（0-5）
    private long lastResonanceCheckTime = 0;
    private static final long RESONANCE_CHECK_INTERVAL = 500;
    private static final float RESONANCE_RANGE = 300f;

    // 视觉动画
    private float floatPhase = (float) (Math.random() * Math.PI * 2);
    private float chainSwayPhase = 0f;
    private float eyeFirePhase = 0f;

    public ChainBoundWraith(float x, float y) {
        super(x, y, 78);
        attackCooldown = 2400;
        setAttackShape(AttackShape.RECT);      // 锁链前甩 - 矩形
        addAvailableAttackType(AttackType.MELEE);
        windUpDuration = 550;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 440;
        prop.attackRange = 190;
        prop.rewardExp = 550;
        prop.rewardMoney = 280;
        setPropertyExtra(prop);

        // HP低、物理攻击低、物理防御极低、速度中、法力高(魔抗高)
        setProperty(1700, 260, 250, 250, 700);

        // 等级分布
        resetPropertyWithLevel();
    }

    @Override
    public boolean usesGenericFireball() {
        return true; // 怨念凝视（幽绿鬼火弹）
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive()) return;

        floatPhase += 0.003f * deltaTime;
        chainSwayPhase += 0.004f * deltaTime;
        eyeFirePhase += 0.005f * deltaTime;

        // 冤魂共鸣：每500ms检查一次周围同伴数量
        long now = System.currentTimeMillis();
        if (now - lastResonanceCheckTime >= RESONANCE_CHECK_INTERVAL) {
            lastResonanceCheckTime = now;
            updateResonanceStacks();
        }
    }

    private void updateResonanceStacks() {
        try {
            List<Enemy> enemies = GameEngine.getInstance().getEnemies();
            if (enemies == null) {
                resonanceStacks = 0;
                return;
            }
            int count = 0;
            for (Enemy other : enemies) {
                if (other == this || !other.isAlive()) continue;
                if (!(other instanceof ChainBoundWraith)) continue;
                float dx = other.getX() - x;
                float dy = other.getY() - y;
                if (dx * dx + dy * dy <= RESONANCE_RANGE * RESONANCE_RANGE) {
                    count++;
                    if (count >= 5) break;
                }
            }
            resonanceStacks = Math.min(count, 5);
        } catch (Exception e) {
            resonanceStacks = 0;
        }
    }

    public int getResonanceStacks() {
        return resonanceStacks;
    }

    /**
     * 冤魂共鸣加成后的法术伤害倍率（每层 +8%）
     */
    public float getResonanceDamageMultiplier() {
        return 1.0f + resonanceStacks * 0.08f;
    }

    @Override
    protected void performAttack() {
        // 由 GameEngine 处理
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel4(this);
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

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = now - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 3 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }

        // 飘行浮动
        float floatY = (float) Math.sin(floatPhase) * 4 * scale;
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY + floatY;

        int baseAlpha = 165; // 半透明青灰

        // 冤魂共鸣激活时的绿色外晕（层数越多越明显）
        if (resonanceStacks > 0) {
            drawResonanceAura(canvas, paint, cx, cy, scale, now);
        }

        // 1. 锁链状雾迹（下半身）
        drawChainMist(canvas, paint, cx, cy, scale, now, baseAlpha);
        // 2. 囚服身体
        drawBody(canvas, paint, cx, cy, scale, now, baseAlpha);
        // 3. 缠绕的铁链
        drawChains(canvas, paint, cx, cy, scale, now, baseAlpha);
        // 4. 头部（歪斜 + 幽绿眼窝）
        drawHead(canvas, paint, cx, cy, scale, now, baseAlpha);
        // 5. 手臂（枯瘦前伸）
        drawArms(canvas, paint, cx, cy, scale, now, baseAlpha);
        // 6. 攻击特效：锁链前甩
        if (currentState == State.ATTACKING && isWindingUp) {
            drawChainWhipEffect(canvas, paint, cx, cy, scale);
        }
    }

    private void drawResonanceAura(Canvas canvas, Paint paint, float cx, float cy, float scale, long now) {
        // 冤魂共鸣：环绕身体的幽绿光环，层数越多越亮
        float intensity = 0.3f + resonanceStacks * 0.15f;
        float pulse = 0.7f + 0.3f * (float) Math.sin(now / 400.0);
        paint.setColor(Color.argb((int) (80 * intensity * pulse), 100, 240, 130));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * scale);
        canvas.drawCircle(cx, cy + 4 * scale, 20 * scale, paint);

        paint.setColor(Color.argb((int) (50 * intensity * pulse), 150, 255, 170));
        paint.setStrokeWidth(1 * scale);
        canvas.drawCircle(cx, cy + 4 * scale, 26 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawChainMist(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 下半身锁链状雾迹（3~4条飘散链节）
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (int i = 0; i < 4; i++) {
            float offsetX = (i - 1.5f) * 5 * scale;
            float wave = (float) Math.sin(chainSwayPhase + i * 1.2f) * 6 * scale;
            float tailAlpha = alpha * (0.65f - i * 0.1f);

            paint.setColor(Color.argb((int) tailAlpha, 150, 165, 175));
            paint.setStrokeWidth((3 - i * 0.5f) * scale);

            Path tail = new Path();
            tail.moveTo(cx + offsetX, cy + 10 * scale);
            tail.quadTo(cx + offsetX + wave, cy + 20 * scale,
                    cx + offsetX + wave * 1.5f, cy + 30 * scale);
            canvas.drawPath(tail, paint);

            // 链节高光（几个小圆点模拟铁链）
            paint.setColor(Color.argb((int) (tailAlpha * 0.8f), 100, 105, 115));
            for (int j = 1; j <= 3; j++) {
                float t = j / 4f;
                float nx = cx + offsetX + wave * t * 1.5f;
                float ny = cy + 10 * scale + (20 * scale) * t;
                canvas.drawCircle(nx, ny, (1.5f - i * 0.2f) * scale, paint);
            }
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 破烂囚服（灰褐色，多处撕裂）
        paint.setColor(Color.argb(alpha, 130, 125, 115));
        Path robe = new Path();
        robe.moveTo(cx - 9 * scale, cy - 6 * scale);
        robe.lineTo(cx + 9 * scale, cy - 6 * scale);
        robe.lineTo(cx + 11 * scale, cy + 6 * scale);
        // 撕裂的下摆（锯齿）
        float tear1 = (float) Math.sin(now / 400.0) * 2 * scale;
        float tear2 = (float) Math.sin(now / 400.0 + 1.5f) * 2 * scale;
        robe.lineTo(cx + 8 * scale, cy + 12 * scale + tear1);
        robe.lineTo(cx + 4 * scale, cy + 8 * scale);
        robe.lineTo(cx, cy + 14 * scale + tear2);
        robe.lineTo(cx - 4 * scale, cy + 9 * scale);
        robe.lineTo(cx - 8 * scale, cy + 13 * scale + tear1);
        robe.lineTo(cx - 11 * scale, cy + 6 * scale);
        robe.close();
        canvas.drawPath(robe, paint);

        // 胸前致命伤口（暗红色刀口）
        paint.setColor(Color.argb((int) (alpha * 0.9f), 130, 25, 25));
        Path wound = new Path();
        wound.moveTo(cx - 4 * scale, cy - 3 * scale);
        wound.lineTo(cx + 3 * scale, cy + 1 * scale);
        wound.lineTo(cx + 4 * scale, cy + 4 * scale);
        wound.lineTo(cx - 3 * scale, cy + 1 * scale);
        wound.close();
        canvas.drawPath(wound, paint);
        // 伤口边缘高光
        paint.setColor(Color.argb((int) (alpha * 0.6f), 180, 50, 50));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 4 * scale, cy - 3 * scale, cx + 3 * scale, cy + 1 * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 滴落的虚影血珠
        for (int i = 0; i < 2; i++) {
            float t = ((now / 1200f + i * 0.5f) % 1f);
            float dx = cx + (i == 0 ? -2 : 2) * scale;
            float dy = cy + 4 * scale + t * 16 * scale;
            paint.setColor(Color.argb((int) (alpha * 0.5f * (1 - t)), 150, 30, 30));
            canvas.drawCircle(dx, dy, 1.2f * scale * (1 - t * 0.5f), paint);
        }

        // 囚服上的污渍
        paint.setColor(Color.argb((int) (alpha * 0.35f), 90, 85, 75));
        canvas.drawCircle(cx - 5 * scale, cy + 3 * scale, 2 * scale, paint);
        canvas.drawCircle(cx + 6 * scale, cy - 1 * scale, 1.5f * scale, paint);
    }

    private void drawChains(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 缠绕身体的生锈铁链（3道斜向锁链）
        paint.setColor(Color.argb((int) (alpha * 0.95f), 90, 80, 65));
        paint.setStrokeWidth(2 * scale);
        paint.setStyle(Paint.Style.STROKE);

        // 斜向锁链1（左肩→右腰）
        Path chain1 = new Path();
        chain1.moveTo(cx - 10 * scale, cy - 5 * scale);
        chain1.quadTo(cx, cy + 2 * scale, cx + 10 * scale, cy + 8 * scale);
        canvas.drawPath(chain1, paint);

        // 斜向锁链2（右肩→左腰）
        Path chain2 = new Path();
        chain2.moveTo(cx + 10 * scale, cy - 5 * scale);
        chain2.quadTo(cx, cy + 4 * scale, cx - 10 * scale, cy + 10 * scale);
        canvas.drawPath(chain2, paint);

        // 横向锁链（腰部）
        Path chain3 = new Path();
        chain3.moveTo(cx - 11 * scale, cy + 6 * scale);
        chain3.quadTo(cx, cy + 8 * scale, cx + 11 * scale, cy + 6 * scale);
        canvas.drawPath(chain3, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 链节高光（铁锈色小圆点）
        paint.setColor(Color.argb((int) (alpha * 0.9f), 130, 100, 60));
        for (int i = 0; i < 6; i++) {
            float t = i / 5f;
            // chain1 上的链节
            float cx1 = cx - 10 * scale + 20 * scale * t;
            float cy1 = cy - 5 * scale + (float) Math.sin(t * Math.PI) * 8 * scale + 3 * scale * t;
            canvas.drawCircle(cx1, cy1, 1 * scale, paint);
        }

        // 拖地的锁链尾端
        paint.setColor(Color.argb((int) (alpha * 0.85f), 80, 70, 55));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        float sway = (float) Math.sin(chainSwayPhase) * 3 * scale;
        Path drag = new Path();
        drag.moveTo(cx + 10 * scale, cy + 8 * scale);
        drag.quadTo(cx + 14 * scale + sway, cy + 18 * scale,
                cx + 10 * scale + sway * 1.5f, cy + 28 * scale);
        canvas.drawPath(drag, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawHead(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 头部歪斜（暗示被处决的断颈）
        float tilt = 0.35f; // 弧度
        float headX = cx + 3 * scale; // 略偏右
        float headY = cy - 14 * scale;

        canvas.save();
        canvas.rotate(tilt, headX, headY);

        // 模糊椭圆面孔（青灰）
        paint.setColor(Color.argb(alpha, 170, 185, 190));
        canvas.drawOval(headX - 6 * scale, headY - 7 * scale,
                headX + 6 * scale, headY + 7 * scale, paint);

        // 蓬乱灰白长发（向上飘散）
        paint.setColor(Color.argb((int) (alpha * 0.85f), 160, 165, 170));
        paint.setStrokeWidth(1.3f * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < 6; i++) {
            float hairX = headX + (i - 2.5f) * 2.2f * scale;
            float hairWave = (float) Math.sin(now / 600.0 + i * 0.9f) * 3 * scale;
            float hairLen = (9 + (3 - Math.abs(i - 2.5f)) * 2.5f) * scale;
            Path hair = new Path();
            hair.moveTo(hairX, headY - 5 * scale);
            hair.quadTo(hairX + hairWave, headY - 5 * scale - hairLen * 0.6f,
                    hairX + hairWave * 1.5f, headY - 5 * scale - hairLen);
            canvas.drawPath(hair, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 空洞眼窝（黑色深洞）
        paint.setColor(Color.argb(alpha, 15, 25, 20));
        canvas.drawOval(headX - 4.5f * scale, headY - 3 * scale,
                headX - 1.5f * scale, headY + 1 * scale, paint);
        canvas.drawOval(headX + 1.5f * scale, headY - 3 * scale,
                headX + 4.5f * scale, headY + 1 * scale, paint);

        // 眼窝中燃烧的幽绿鬼火
        float flare = 0.7f + 0.3f * (float) Math.sin(eyeFirePhase);
        paint.setColor(Color.argb((int) (alpha * flare), 100, 240, 130));
        canvas.drawCircle(headX - 3 * scale, headY - 1 * scale, 1.3f * scale, paint);
        canvas.drawCircle(headX + 3 * scale, headY - 1 * scale, 1.3f * scale, paint);
        // 外焰
        paint.setColor(Color.argb((int) (alpha * flare * 0.5f), 150, 255, 170));
        canvas.drawCircle(headX - 3 * scale, headY - 1 * scale, 2.6f * scale, paint);
        canvas.drawCircle(headX + 3 * scale, headY - 1 * scale, 2.6f * scale, paint);

        // 嘴（黑色张开椭圆，愤怒咆哮）
        paint.setColor(Color.argb(alpha, 20, 25, 20));
        canvas.drawOval(headX - 2 * scale, headY + 3 * scale,
                headX + 2 * scale, headY + 5 * scale, paint);

        canvas.restore();

        // 颈部断口（歪斜头下方的暗红痕迹）
        paint.setColor(Color.argb((int) (alpha * 0.7f), 110, 25, 25));
        canvas.drawOval(cx - 3 * scale, cy - 8 * scale, cx + 3 * scale, cy - 5 * scale, paint);
    }

    private void drawArms(Canvas canvas, Paint paint, float cx, float cy, float scale, long now, int alpha) {
        // 枯瘦双手前伸（锁链缠绕）
        paint.setColor(Color.argb(alpha, 170, 180, 185));
        paint.setStrokeWidth(2 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float armWave = (float) Math.sin(now / 700.0) * 2 * scale;
        float reach = (currentState == State.ATTACKING) ? 5 * scale : 0;

        // 左臂
        Path leftArm = new Path();
        leftArm.moveTo(cx - 8 * scale, cy - 3 * scale);
        leftArm.quadTo(cx - 12 * scale, cy - 6 * scale + armWave,
                cx - 15 * scale - reach, cy - 8 * scale + armWave);
        canvas.drawPath(leftArm, paint);

        // 右臂
        Path rightArm = new Path();
        rightArm.moveTo(cx + 8 * scale, cy - 3 * scale);
        rightArm.quadTo(cx + 12 * scale, cy - 6 * scale - armWave,
                cx + 15 * scale + reach, cy - 8 * scale - armWave);
        canvas.drawPath(rightArm, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 手腕铁铐（暗铁色圆环）
        paint.setColor(Color.argb(alpha, 70, 65, 55));
        canvas.drawCircle(cx - 13 * scale - reach, cy - 7 * scale + armWave, 1.8f * scale, paint);
        canvas.drawCircle(cx + 13 * scale + reach, cy - 7 * scale - armWave, 1.8f * scale, paint);

        // 指尖幽绿光点（鬼火外溢）
        float tipGlow = 0.6f + 0.4f * (float) Math.sin(now / 300.0);
        paint.setColor(Color.argb((int) (alpha * tipGlow), 130, 250, 160));
        canvas.drawCircle(cx - 15 * scale - reach, cy - 8 * scale + armWave, 1.5f * scale, paint);
        canvas.drawCircle(cx + 15 * scale + reach, cy - 8 * scale - armWave, 1.5f * scale, paint);
    }

    private void drawChainWhipEffect(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        // 锁链前甩：矩形范围内的铁链轨迹
        float progress = getWindUpProgress();
        float rectW = propertyExtra.attackRange * 1.2f;
        float rectH = 50 * scale * progress;

        // 铁链阴影
        paint.setColor(Color.argb((int) (progress * 80), 100, 90, 70));
        canvas.save();
        canvas.translate(cx, cy);
        if (targetX < x) canvas.scale(-1, 1);
        canvas.drawRect(0, -rectH / 2, rectW * progress, rectH / 2, paint);

        // 3道锁链轨迹（螺旋曲线）
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * scale * progress);
        for (int i = 0; i < 3; i++) {
            float oy = (i - 1) * 12 * scale * progress;
            paint.setColor(Color.argb((int) (progress * (200 - i * 40)), 160, 145, 110));
            Path whip = new Path();
            whip.moveTo(0, oy);
            whip.quadTo(rectW * 0.5f * progress, oy + 6 * scale * progress,
                    rectW * progress, oy - 3 * scale * progress);
            canvas.drawPath(whip, paint);
            // 链节高光
            paint.setColor(Color.argb((int) (progress * 220), 220, 200, 140));
            for (int j = 1; j <= 4; j++) {
                float t = j / 5f;
                float nx = rectW * progress * t;
                float ny = oy + (float) Math.sin(t * Math.PI) * 6 * scale * progress;
                canvas.drawCircle(nx, ny, 1.2f * scale * progress, paint);
            }
        }
        canvas.restore();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }
}

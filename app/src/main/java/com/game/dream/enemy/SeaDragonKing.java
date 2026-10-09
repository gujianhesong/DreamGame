package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 海底迷宫三层关卡 BOSS：东海龙王敖广。独立怪物，不是小青龙。
 * 五发水龙弹、龙雷、大范围龙威，龙尾横扫附带击退。半血连续三道潮柱，并在脚下铺潮池。
 */
public class SeaDragonKing extends Enemy {

    private static final float AURA_RANGE = 400f;
    private static final long AURA_SLOW = 1100;
    private static final float PREFERRED_DISTANCE = 340f;
    private static final float MIN_DISTANCE = 200f;
    private static final int BOLT_COUNT = 5;
    private static final float BOLT_SPREAD = 12f;
    private static final long BOLT_COOLDOWN = 2400;
    private static final long LIGHTNING_COOLDOWN = 4000;

    private long lastWaterBoltTime = 0;
    private long lastLightningTime = 0;
    private boolean pendingWaterBolt = false;
    private boolean pendingLightning = false;

    public SeaDragonKing(float x, float y) {
        super(x, y, 600);
        enemyLevel = EnemyLevel.STAGE_BOSS;
        attackCooldown = 2000;
        setAttackShape(AttackShape.CIRCLE);
        addAvailableAttackType(AttackType.SPIN_ATTACK);
        spinDuration = 1400;
        spinHitInterval = 380;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 720;
        prop.attackRange = 440;
        prop.rewardExp = 10000;
        prop.rewardMoney = 5000;
        setPropertyExtra(prop);
        setProperty(4000*40, 3000, 3000, 600, 4000);
    }

    @Override
    public boolean usesGenericFireball() {
        return false;
    }

    public int getWaterBoltCount() {
        return BOLT_COUNT;
    }

    public float getWaterBoltSpread() {
        return BOLT_SPREAD;
    }

    public float getAuraRange() {
        return AURA_RANGE;
    }

    public long getAuraSlowDuration() {
        return AURA_SLOW;
    }

    public boolean isPendingWaterBolt() {
        return pendingWaterBolt;
    }

    public void consumeWaterBolt() {
        pendingWaterBolt = false;
    }

    public boolean isPendingLightning() {
        return pendingLightning;
    }

    public void consumeLightning() {
        pendingLightning = false;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (!isAlive() || isStunned() || isFrozen()) {
            return;
        }
        long now = System.currentTimeMillis();
        tickSpellWindUp(now, playerX, playerY);
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (currentState == State.CHASING) {
            tryCastSpells(dist, now);
        }
    }

    @Override
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (isRooted() || isSpellWindingUp || dist < 1f) {
            return;
        }
        if (dist < MIN_DISTANCE) {
            targetX = x - dx / dist * 220;
            targetY = y - dy / dist * 220;
        } else if (dist > PREFERRED_DISTANCE * 1.4f) {
            targetX = playerX;
            targetY = playerY;
        } else {
            float perpX = -dy / dist;
            float perpY = dx / dist;
            float circleDir = (Math.sin(System.currentTimeMillis() / 1800.0) > 0) ? 1 : -1;
            targetX = x + perpX * 120 * circleDir;
            targetY = y + perpY * 120 * circleDir;
        }
        moveToTargetWithSpeed(deltaSeconds, speed * 1.05f);
    }

    private void tryCastSpells(float dist, long now) {
        if (isSpellWindingUp || pendingWaterBolt || pendingLightning || dist >= propertyExtra.detectionRange) {
            return;
        }
        if (now - lastSpellCastRollTime < SPELL_CAST_CHECK_INTERVAL) {
            return;
        }
        boolean waterReady = now - lastWaterBoltTime >= BOLT_COOLDOWN;
        boolean lightningReady = now - lastLightningTime >= LIGHTNING_COOLDOWN;
        if (!waterReady && !lightningReady) {
            return;
        }
        lastSpellCastRollTime = now;

        float lightningChance = dist <= MIN_DISTANCE * 1.2f ? 0.35f : 0.25f;
        float waterChance = dist < MIN_DISTANCE ? 0.08f : (dist <= PREFERRED_DISTANCE ? 0.42f : 0.26f);
        if (isBossPhaseTwo()) {
            lightningChance = Math.min(0.70f, lightningChance * 1.6f);
            waterChance = Math.min(0.70f, waterChance * 1.6f);
        }
        if (lightningReady && dist <= propertyExtra.detectionRange * 0.85f && Math.random() < lightningChance) {
            beginSpellWindUp(SPELL_DRAGON_LIGHTNING);
            return;
        }
        if (waterReady && Math.random() < waterChance) {
            beginSpellWindUp(SPELL_WATER_BOLT);
        }
    }

    @Override
    protected void onSpecialSpellWindUpComplete(int spellId, long currentTime) {
        if (spellId == SPELL_WATER_BOLT) {
            pendingWaterBolt = true;
            lastWaterBoltTime = currentTime;
        } else if (spellId == SPELL_DRAGON_LIGHTNING) {
            pendingLightning = true;
            lastLightningTime = currentTime;
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel4(this);
        }
        return possibleDrops;
    }

    @Override
    protected void performAttack() {
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) {
            return;
        }
        float cx = x + offsetX;
        float cy = y + offsetY;
        float wave = (float) Math.sin(System.currentTimeMillis() / 260.0);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.argb(36, 40, 120, 220));
        canvas.drawCircle(cx, cy, AURA_RANGE * 0.2f, paint);

        paint.setColor(Color.rgb(46, 92, 78));
        for (int i = 0; i < 7; i++) {
            float t = i / 6f;
            float px = cx - size * 0.2f + t * size * 0.85f;
            float py = cy + wave * (6f + i) + (float) Math.sin(t * 2.4f + wave) * 10f;
            canvas.drawCircle(px, py, size * (0.26f - t * 0.02f), paint);
        }
        paint.setColor(Color.rgb(212, 168, 64));
        for (int i = 0; i < 4; i++) {
            float t = i / 3f;
            float px = cx - size * 0.05f + t * size * 0.45f;
            float py = cy + wave * 4f;
            canvas.drawCircle(px, py, size * 0.06f, paint);
        }

        float hx = cx - size * 0.32f;
        float hy = cy - size * 0.12f + wave * 3f;
        paint.setColor(Color.rgb(232, 206, 120));
        canvas.drawCircle(hx, hy, size * 0.22f, paint);
        paint.setStrokeWidth(4f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(255, 214, 90));
        canvas.drawLine(hx - size * 0.08f, hy - size * 0.16f, hx - size * 0.28f, hy - size * 0.48f, paint);
        canvas.drawLine(hx + size * 0.06f, hy - size * 0.16f, hx + size * 0.22f, hy - size * 0.46f, paint);
        canvas.drawArc(hx - size * 0.16f, hy - size * 0.42f, hx + size * 0.16f, hy - size * 0.16f, 200, 140, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 230, 240, 255));
        canvas.drawLine(hx - size * 0.2f, hy + size * 0.02f, hx - size * 0.42f, hy + size * 0.16f, paint);
        canvas.drawLine(hx + size * 0.12f, hy + size * 0.04f, hx + size * 0.34f, hy + size * 0.18f, paint);
        paint.setColor(Color.rgb(20, 30, 40));
        canvas.drawCircle(hx - size * 0.06f, hy - size * 0.02f, 4f, paint);
    }
}

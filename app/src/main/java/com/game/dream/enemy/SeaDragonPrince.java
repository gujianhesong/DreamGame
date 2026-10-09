package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.item.Item;
import com.game.dream.utils.ProssibleDropsUtil;

import java.util.List;

/**
 * 海底迷宫二层关卡 BOSS：东海龙太子敖丙。独立怪物，不是小青龙。
 * 三发窄扇水龙弹、龙尾横扫、龙威。半血落一道两段水龙卷，再猛扑一次。
 */
public class SeaDragonPrince extends Enemy {

    private static final float AURA_RANGE = 320f;
    private static final long AURA_SLOW = 700;
    private static final float PREFERRED_DISTANCE = 300f;
    private static final float MIN_DISTANCE = 180f;
    private static final int BOLT_COUNT = 3;
    private static final float BOLT_SPREAD = 12f;
    private static final long BOLT_COOLDOWN = 2000;

    private long lastWaterBoltTime = 0;
    private boolean pendingWaterBolt = false;
    private boolean forcedPounce = false;
    private boolean pounceArmed = false;
    private long forcedPounceDeadline = 0;

    public SeaDragonPrince(float x, float y) {
        super(x, y, 500);
        enemyLevel = EnemyLevel.STAGE_BOSS;
        attackCooldown = 2200;
        pounceFixedSpeed = 780f;
        setAttackShape(AttackShape.CIRCLE);
        addAvailableAttackType(AttackType.SPIN_ATTACK);
        spinDuration = 1200;
        spinHitInterval = 400;

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 640;
        prop.attackRange = 400;
        prop.rewardExp = 7000;
        prop.rewardMoney = 3500;
        setPropertyExtra(prop);
        setProperty(4000*30, 2500, 2500, 400, 3000);
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

    public void beginRagePounce() {
        forcedPounce = true;
        pounceArmed = false;
        interruptBossRetreat();
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        long now = System.currentTimeMillis();
        if (forcedPounce && !pounceArmed && isAlive() && !isStunned() && !isFrozen()) {
            interruptBossRetreat();
            currentState = State.ATTACKING;
            currentAttackType = AttackType.POUNCE;
            isWindingUp = true;
            isPounceRetreating = false;
            windUpStartTime = now;
            pounceArmed = true;
            forcedPounceDeadline = now + 8000;
        }
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);
        if (pounceArmed && (isPounceRetreating || now > forcedPounceDeadline)) {
            forcedPounce = false;
            pounceArmed = false;
        }
        if (!isAlive() || isStunned() || isFrozen()) {
            return;
        }
        tickSpellWindUp(now, playerX, playerY);
        float dx = playerX - x;
        float dy = playerY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (currentState == State.CHASING) {
            tryCastWaterBolt(dist, now);
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
            targetX = x - dx / dist * 200;
            targetY = y - dy / dist * 200;
        } else if (dist > PREFERRED_DISTANCE * 1.5f) {
            targetX = playerX;
            targetY = playerY;
        } else {
            float perpX = -dy / dist;
            float perpY = dx / dist;
            float circleDir = (Math.sin(System.currentTimeMillis() / 2000.0) > 0) ? 1 : -1;
            targetX = x + perpX * 100 * circleDir;
            targetY = y + perpY * 100 * circleDir;
        }
        moveToTargetWithSpeed(deltaSeconds, speed * 1.1f);
    }

    private void tryCastWaterBolt(float dist, long now) {
        if (isSpellWindingUp || pendingWaterBolt || dist >= propertyExtra.detectionRange) {
            return;
        }
        if (now - lastWaterBoltTime < BOLT_COOLDOWN) {
            return;
        }
        if (now - lastSpellCastRollTime < SPELL_CAST_CHECK_INTERVAL) {
            return;
        }
        lastSpellCastRollTime = now;
        float chance = dist < MIN_DISTANCE ? 0.06f : (dist <= PREFERRED_DISTANCE ? 0.45f : 0.28f);
        if (isBossPhaseTwo()) {
            chance = Math.min(0.70f, chance * 1.6f);
        }
        if (Math.random() < chance) {
            beginSpellWindUp(SPELL_WATER_BOLT);
        }
    }

    @Override
    protected void onSpecialSpellWindUpComplete(int spellId, long currentTime) {
        if (spellId == SPELL_WATER_BOLT) {
            pendingWaterBolt = true;
            lastWaterBoltTime = currentTime;
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        if (possibleDrops.isEmpty()) {
            ProssibleDropsUtil.addPossibleDrops_sceneLevel3(this);
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
        float wave = (float) Math.sin(System.currentTimeMillis() / 220.0);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.argb(40, 80, 190, 255));
        canvas.drawCircle(cx, cy, AURA_RANGE * 0.22f, paint);

        paint.setColor(Color.rgb(150, 214, 232));
        for (int i = 0; i < 6; i++) {
            float t = i / 5f;
            float px = cx - size * 0.15f + t * size * 0.7f;
            float py = cy + wave * (4f + i) + (float) Math.sin(t * 3f + wave) * 8f;
            canvas.drawCircle(px, py, size * (0.22f - t * 0.025f), paint);
        }

        float hx = cx - size * 0.28f;
        float hy = cy - size * 0.08f + wave * 3f;
        paint.setColor(Color.rgb(186, 232, 245));
        canvas.drawCircle(hx, hy, size * 0.2f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.rgb(210, 225, 235));
        canvas.drawLine(hx - size * 0.06f, hy - size * 0.16f, hx - size * 0.16f, hy - size * 0.34f, paint);
        canvas.drawLine(hx + size * 0.04f, hy - size * 0.16f, hx + size * 0.02f, hy - size * 0.34f, paint);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(200, 220, 235, 245));
        canvas.drawCircle(hx, hy - size * 0.22f, size * 0.1f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(20, 40, 60));
        canvas.drawCircle(hx - size * 0.06f, hy - size * 0.02f, 3f, paint);
    }
}

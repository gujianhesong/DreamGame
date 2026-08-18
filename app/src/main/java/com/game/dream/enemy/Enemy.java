package com.game.dream.enemy;

import static com.game.dream.common.Constants.TILE_SIZE;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.game.dream.figure.Character;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.ItemCreator;
import com.game.dream.map.MapGenerator;
import com.game.dream.map.MazeGenerator;
import com.game.dream.system.MapSystem;
import com.game.dream.utils.LogUtil;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all enemies
 */
public abstract class Enemy extends Character {
    // AI state
    public enum State {
        IDLE,
        CHASING,
        ATTACKING
    }

    public enum EnemyLevel {
        NORMAL,
        LEADER,
        ELITE,
        BOSS,
    }

    protected int health;
    protected int maxHealth;
    protected int attackDamage;
    protected int magicDamage;
    protected int defense;
    protected int mana;
    protected int speed;

    public class EnemyPropertyExtra{
        // Detection and attack ranges
        protected float detectionRange;
        protected float attackRange;

        // Experience reward when killed
        protected int rewardExp;
        protected int rewardMoney;
    }

    protected State currentState;
    protected EnemyLevel enemyLevel;

    // Movement
    protected float targetX, targetY;
    protected long stateTimer;

    protected EnemyPropertyExtra propertyExtra;

    // Aggro timer - how long enemy stays aggressive after being damaged
    protected long aggroEndTime;
    protected boolean isAggroed;

    // Possible drops
    protected List<Item> possibleDrops;

    protected boolean isCastingSpell;

    // Attack wind-up (攻击前摇)
    protected boolean isWindingUp = false;
    protected long windUpStartTime = 0;
    protected long windUpDuration = 400; // 前摇时长(ms)
    protected boolean attackJustFired = false; // 前摇结束后攻击已触发标记

    // Attack range shape (攻击范围形状)
    public enum AttackShape { CIRCLE, ARC, RECT }
    protected AttackShape attackShape = AttackShape.CIRCLE;

    public Enemy(float x, float y, int size) {
        super(x, y, size);

        this.currentState = State.IDLE;
        this.enemyLevel = EnemyLevel.NORMAL;
        this.targetX = x;
        this.targetY = y;
        this.stateTimer = System.currentTimeMillis();

        propertyExtra = new EnemyPropertyExtra();
        propertyExtra.detectionRange = 400;
        propertyExtra.attackRange = 200;
        propertyExtra.rewardExp = 300;
        propertyExtra.rewardMoney = 100;

        this.attackCooldown = 2000;
        this.lastAttackTime = 0;

        this.aggroEndTime = 0;
        this.isAggroed = false;

        this.possibleDrops = new ArrayList<>();

        this.isCastingSpell = false;
    }

    public void setProperty(int maxHealth, int attackDamage, int defense, int speed, int mana){
        int health = Utils.getWaveValueInt(maxHealth, 0.2f);
        this.maxHealth = health;
        this.health = health;
        this.attackDamage = Utils.getWaveValueInt(attackDamage, 0.2f);
        this.defense = Utils.getWaveValueInt(defense, 0.2f);
        this.speed = Utils.getWaveValueInt(speed, 0.2f);
        this.mana = Utils.getWaveValueInt(mana, 0.2f);
    }

    public void setPropertyExtra(EnemyPropertyExtra enemyPropertyExtra){
        this.propertyExtra = enemyPropertyExtra;
    }

    /**
     * Update enemy behavior - template method
     */
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        if (!isAlive()) return;

        // Update CC state first (clears expired effects)
        updateCCState();

        // Handle knockback movement FIRST (overrides everything, including stun)
        if (isBeingKnockedBack()) {
            float[] kbMove = updateKnockback(deltaTime);
            if (kbMove != null) {
                float newX = x + kbMove[0];
                float newY = y + kbMove[1];
                // Clamp to map bounds
                newX = Math.max(size, Math.min(newX, mapWidth - size));
                newY = Math.max(size, Math.min(newY, mapHeight - size));
                x = newX;
                y = newY;
            }
            // Still update animation during knockback
            updateAnimation(System.currentTimeMillis());
            return;
        }

        // If stunned, skip all AI logic
        if (isStunned()) return;

        long currentTime = System.currentTimeMillis();
        float deltaSeconds = deltaTime / 1000f;

        // Calculate distance to player
        float dx = playerX - x;
        float dy = playerY - y;
        float distanceToPlayer = (float) Math.sqrt(dx * dx + dy * dy);

        // Check if aggro has expired
        if (isAggroed && currentTime > aggroEndTime) {
            isAggroed = false;
        }

        // State machine
        switch (currentState) {
            case IDLE:
                updateIdle(deltaTime, deltaSeconds, map, mapWidth, mapHeight);

                if (!MapSystem.getInstance().isLocationSafe(playerX, playerY) && distanceToPlayer < propertyExtra.detectionRange) {
                    currentState = State.CHASING;
                    stateTimer = currentTime;
                }
                break;

            case CHASING:
                updateChasing(deltaSeconds, playerX, playerY, map, mapWidth, mapHeight);

                // Only return to idle if not aggroed AND player is far away
                if (!isAggroed && distanceToPlayer > propertyExtra.detectionRange * 1.5f || MapSystem.getInstance().isLocationSafe(playerX, playerY)) {
                    currentState = State.IDLE;
                    stateTimer = currentTime;
                } else if (distanceToPlayer < propertyExtra.attackRange) {
                    currentState = State.ATTACKING;
                    stateTimer = currentTime;
                    // 开始攻击前摇
                    isWindingUp = true;
                    windUpStartTime = currentTime;
                }
                break;

            case ATTACKING:
                // 前摇期间不移动，让玩家有机会走出范围
                if (isWindingUp) {
                    // 更新目标方向用于显示攻击方向
                    targetX = playerX;
                    targetY = playerY;
                    if (currentTime - windUpStartTime >= windUpDuration) {
                        // 前摇结束，检查玩家是否还在攻击范围内
                        isWindingUp = false;
                        if (distanceToPlayer < propertyExtra.attackRange) {
                            // 玩家仍在范围内，执行攻击
                            attackJustFired = true;
                            lastAttackTime = currentTime;
                        }
                        // 玩家不在范围内 = 闪避成功，不攻击
                    }
                    // 前摇中不移动
                } else {
                    updateAttacking(deltaSeconds, playerX, playerY);

                    // 攻击冷却后重新开始下一次前摇
                    if (currentTime - lastAttackTime >= attackCooldown) {
                        if (distanceToPlayer < propertyExtra.attackRange) {
                            // 玩家仍在攻击范围内，开始新一轮前摇
                            isWindingUp = true;
                            windUpStartTime = currentTime;
                        } else {
                            // 玩家不在攻击范围，切回追击
                            currentState = State.CHASING;
                            stateTimer = currentTime;
                        }
                    }
                }

                if (distanceToPlayer > propertyExtra.attackRange * 1.5f) {
                    currentState = State.CHASING;
                    isWindingUp = false;
                    stateTimer = currentTime;
                }
                break;
        }

        // Update animation
        updateAnimation(currentTime);
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public int getMagicDamage() {
        return magicDamage;
    }

    public int getDefense() {
        return defense;
    }

    public float getSpeed() {
        return speed;
    }

    public int getMana() {
        return mana;
    }

    public float getAttackRange() {
        return propertyExtra.attackRange;
    }

    /**
     * Update idle behavior
     */
    protected void updateIdle(long deltaTime, float deltaSeconds, int[][] map, int mapWidth, int mapHeight) {
        long currentTime = System.currentTimeMillis();

        // Tigers are more active - change direction every 2-5 seconds
        if (currentTime - stateTimer > 2000 + (int) (Math.random() * 3000)) {
            // Pick a random nearby position
            float angle = (float) (Math.random() * Math.PI * 2);
            float distance = 80 + (float) (Math.random() * 150);

            targetX = x + (float) Math.cos(angle) * distance;
            targetY = y + (float) Math.sin(angle) * distance;

            // Clamp to map bounds
            targetX = Math.max(size, Math.min(targetX, mapWidth - size));
            targetY = Math.max(size, Math.min(targetY, mapHeight - size));

            if (MapSystem.getInstance().isLocationSafe(targetX, targetY)) {
                return;
            }

            stateTimer = currentTime;
        }

        // Move towards target
        moveToTarget(deltaSeconds);
    }

    /**
     * Update chasing behavior - common implementation
     */
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        targetX = playerX;
        targetY = playerY;

        // If rooted, do not move
        if (isRooted()) {
            return;
        }

        // Elite and Leader tigers can cast spells while chasing
        if (enemyLevel == EnemyLevel.BOSS || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.LEADER) {
            // Check if should cast spell (15% chance per update, with cooldown)
            long currentTime = System.currentTimeMillis();
            if (canCastSpell() && (currentTime - lastAttackTime > 3000) && Math.random() < 0.3f) {
                // Will cast spell - this will be handled by GameEngine
                isCastingSpell = true;
                lastAttackTime = currentTime;
                LogUtil.d("Tiger preparing magic spell while chasing!");
            }
        }

        float chaseSpeed = speed * 1.1f;
        moveToTargetWithSpeed(deltaSeconds, chaseSpeed);
    }

    /**
     * Update attacking behavior - common implementation
     */
    protected void updateAttacking(float deltaSeconds, float playerX, float playerY) {
        targetX = playerX;
        targetY = playerY;

        float dx = playerX - x;
        float dy = playerY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float optimalDistance = propertyExtra.attackRange * 0.7f;
            if (distance > optimalDistance) {
                float moveX = (dx / distance) * speed * 0.5f * deltaSeconds;
                float moveY = (dy / distance) * speed * 0.5f * deltaSeconds;

                x += moveX;
                y += moveY;
            }
        }

        /*long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime > attackCooldown) {
            performAttack();
            lastAttackTime = currentTime;
        }*/
    }

    /**
     * Move towards target position
     */
    protected void moveToTarget(float deltaSeconds) {
        moveToTargetWithSpeed(deltaSeconds, speed);
    }

    /**
     * Move towards target with specific speed
     */
    protected void moveToTargetWithSpeed(float deltaSeconds, float speed) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float moveSpeed = (100 + speed * 0.5f);
        float speedRatio = 1f;
        if (isSlowed()) {
            speedRatio = 0.5f;
        }

        if (distance > 5) {
            float moveX = (dx / distance) * moveSpeed * deltaSeconds * speedRatio;
            float moveY = (dy / distance) * moveSpeed * deltaSeconds * speedRatio;

            float newX = x + moveX;
            float newY = y + moveY;

            // 地形检测
            int tileSize = TILE_SIZE;
            int[][] map = MapSystem.getInstance().getCurMapInfo().getMapData();
            if (map != null) {
                int gridX = (int) (newX / tileSize);
                int gridY = (int) (newY / tileSize);
                if (gridX >= 0 && gridX < map[0].length && gridY >= 0 && gridY < map.length) {
                    int terrain = map[gridY][gridX];
                    // 检查是否可通行
                    boolean blocked = (terrain == MapGenerator.LAKE || terrain == MapGenerator.LAVA
                            || terrain == MapGenerator.VILLAGE_NO_PASS || terrain == MazeGenerator.MAZE_WALL);
                    if (!blocked) {
                        x = newX;
                        y = newY;
                    }
                } else {
                    x = newX;
                    y = newY;
                }
            } else {
                x = newX;
                y = newY;
            }
        }
    }

    /**
     * Perform attack - can be overridden by subclass
     */
    protected void performAttack() {
        // Default attack - can be customized by subclasses
    }

    /**
     * Update animation frame
     */
    protected void updateAnimation(long currentTime) {
        if (currentTime - lastAnimUpdate > 150) {
            animFrame = (animFrame + 1) % 4;
            lastAnimUpdate = currentTime;

            bobOffset = (float) Math.sin(animFrame * Math.PI / 2) * 3;
        }
    }

    // Getters
    public State getState() {
        return currentState;
    }

    /**
     * Check if enemy can attack (cooldown expired)
     */
    public boolean canAttack() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAttackTime) >= attackCooldown;
    }

    /**
     * Check if enemy can cast magic spell
     */
    public boolean canCastSpell() {
        return (enemyLevel == EnemyLevel.BOSS || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.LEADER);
    }

    /**
     * Cast magic spell - returns target position for projectile
     */
    public float[] castMagicSpell(float playerX, float playerY) {
        if (!canCastSpell()) return null;

        // Return player position as target
        return new float[]{playerX, playerY};
    }

    /**
     * Set last attack time
     */
    public void setLastAttackTime(long time) {
        this.lastAttackTime = time;
    }

    /**
     * Get last attack time
     */
    public long getLastAttackTime() {
        return lastAttackTime;
    }

    /**
     * Check if attack just fired (consumed once by GameEngine)
     */
    public boolean consumeAttackFired() {
        boolean fired = attackJustFired;
        attackJustFired = false;
        return fired;
    }

    /**
     * Get attack cooldown progress (0-1)
     */
    public float getAttackCooldownProgress() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttack = currentTime - lastAttackTime;

        if (timeSinceLastAttack >= attackCooldown) {
            return 1.0f; // Ready to attack
        }

        return (float) timeSinceLastAttack / attackCooldown;
    }

    /**
     * Get experience reward when this enemy is killed
     */
    public int getExperienceReward() {
        float factor = 1f;
        if (enemyLevel == EnemyLevel.BOSS) {
            factor = 50f;
        } else if (enemyLevel == EnemyLevel.ELITE) {
            factor = 10f;
        } else if (enemyLevel == EnemyLevel.LEADER) {
            factor = 3f;
        }
        return (int) (Utils.getWaveValueInt(propertyExtra.rewardExp, 0.1f) * factor);
    }

    /**
     * Get money reward when this enemy is killed
     */
    public int getMoneyReward() {
        float factor = 1f;
        if (enemyLevel == EnemyLevel.BOSS) {
            factor = 50f;
        } if (enemyLevel == EnemyLevel.ELITE) {
            factor = 10f;
        } else if (enemyLevel == EnemyLevel.LEADER) {
            factor = 3f;
        }
        return (int) (Utils.getWaveValueInt(propertyExtra.rewardMoney, 0.1f) * factor);
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Check if enemy is currently casting a spell
     */
    public boolean isCastingSpell() {
        return isCastingSpell;
    }

    /**
     * Reset casting state
     */
    public void resetCastingState() {
        isCastingSpell = false;
    }

    public boolean takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();

        // Check if invincible
        if (isInvincible && currentTime < invincibleEndTime) {
            return false; // No damage taken
        }

        // Apply damage
        health -= damage;
        lastDamageTime = currentTime;

        // Trigger hit flash (red tint)
        triggerHitFlash();

        if (isJinGangState) {
            health = Math.max(1, health);
        }

        // When damaged, automatically enter CHASING state and set aggro
        if (isAlive()) {
            currentState = State.CHASING;
            stateTimer = currentTime;

            // Set aggro timer - enemy will chase for 10 seconds after being hit
            isAggroed = true;
            aggroEndTime = currentTime + 10000; // 10 seconds

            LogUtil.d("Enemy", getName() + " was damaged! Aggroed for 10 seconds");
        }

        // Check if dead
        if (health <= 0) {
            health = 0;
            return true; // Died
        }

        return false; // Still alive
    }

    @Override
    public void setName(String name) {
        this.name = name;
        if (enemyLevel == EnemyLevel.BOSS) {
            this.name = name + "BOSS";
        } if (enemyLevel == EnemyLevel.ELITE) {
            this.name = name + "精英";
        } else if (enemyLevel == EnemyLevel.LEADER) {
            this.name = name + "首领";
        }
    }

    /**
     * Add possible drop item
     */
    public void addPossibleDrop(Item item) {
        possibleDrops.add(item);
    }

    /**
     * Get dropped items when enemy dies
     */
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();

        getPossibleDropList();

        if (!possibleDrops.isEmpty()) {
            // Drop items count
            int numDrops = (int) (Math.random() * 3);
            if (enemyLevel == EnemyLevel.BOSS) {
                numDrops = 3 + (int) (Math.random() * 3);
            } else if (enemyLevel == EnemyLevel.ELITE) {
                numDrops = 2 + (int) (Math.random() * 3);
            } else if (enemyLevel == EnemyLevel.LEADER) {
                numDrops = 1 + (int) (Math.random() * 3);
            }

            for (int i = 0; i < numDrops && i < possibleDrops.size(); i++) {
                Item item = possibleDrops.get((int) (Math.random() * possibleDrops.size()));
                int quantity = 1 + (int) (Math.random() * 3); // 1-3 quantity

                if (item instanceof EquipmentItem) {
                    quantity = 1;
                }

                drops.add(new ItemStack(item, quantity));
            }
        }

        return drops;
    }

    public abstract List<Item> getPossibleDropList();

    /**
     * Set attack shape
     */
    public void setAttackShape(AttackShape shape) {
        this.attackShape = shape;
    }

    /**
     * Check if enemy is currently in attack wind-up phase
     */
    public boolean isWindingUp() {
        return isWindingUp;
    }

    /**
     * Cancel wind-up (e.g., when stunned by dash)
     */
    public void setWindingUpFalse() {
        isWindingUp = false;
    }

    /**
     * Get wind-up progress (0-1, 1 = about to attack)
     */
    public float getWindUpProgress() {
        if (!isWindingUp) return 0f;
        long elapsed = System.currentTimeMillis() - windUpStartTime;
        return Math.min(1.0f, (float) elapsed / windUpDuration);
    }

    /**
     * Draw attack wind-up warning indicator above enemy
     */
    @Override
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        super.draw(canvas, offsetX, offsetY);

        if (isWindingUp) {
            float screenX = getX() + offsetX;
            float screenY = getY() + offsetY;
            float progress = getWindUpProgress();

            // 绘制警告标记: 红色感叹号，随前摇进度变大变亮
            Paint warnPaint = new Paint();
            warnPaint.setAntiAlias(true);
            int alpha = (int) (80 + progress * 175);
            warnPaint.setColor(Color.argb(alpha, 255, 50, 50));
            warnPaint.setTextSize(16 + progress * 10);
            warnPaint.setTextAlign(Paint.Align.CENTER);
            warnPaint.setFakeBoldText(true);
            canvas.drawText("!", screenX, screenY - getSize() - 25 - (int)(progress * 8), warnPaint);

            // 脚下红色警告圈
            Paint circlePaint = new Paint();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.argb((int)(progress * 60), 255, 0, 0));
            circlePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(screenX, screenY, getSize() * (0.5f + progress * 0.3f), circlePaint);

            // 攻击范围预警: 根据攻击形状绘制不同区域
            Paint rangePaint = new Paint();
            rangePaint.setAntiAlias(true);
            int strokeAlpha = (int) (100 + progress * 155);  // 100~255
            int fillAlpha = (int) (30 + progress * 50);      // 30~80

            switch (attackShape) {
                case ARC: {
                    // 扇形: 朝玩家方向的120度角区域
                    float dx = targetX - getX();
                    float dy = targetY - getY();
                    float angleToTarget = (float) Math.toDegrees(Math.atan2(dy, dx));
                    float arcSpan = 120f;
                    RectF arcRect = new RectF(
                            screenX - propertyExtra.attackRange, screenY - propertyExtra.attackRange,
                            screenX + propertyExtra.attackRange, screenY + propertyExtra.attackRange);
                    // 描边
                    rangePaint.setColor(Color.argb(strokeAlpha, 255, 60, 60));
                    rangePaint.setStyle(Paint.Style.STROKE);
                    rangePaint.setStrokeWidth(3);
                    canvas.drawArc(arcRect, angleToTarget - arcSpan / 2, arcSpan, true, rangePaint);
                    // 填充
                    rangePaint.setColor(Color.argb(fillAlpha, 255, 0, 0));
                    rangePaint.setStyle(Paint.Style.FILL);
                    canvas.drawArc(arcRect, angleToTarget - arcSpan / 2, arcSpan, true, rangePaint);
                    break;
                }
                case RECT: {
                    // 矩形: 朝玩家方向的长方形区域
                    float dx = targetX - getX();
                    float dy = targetY - getY();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float dirX = dist > 0 ? dx / dist : 1;
                    float dirY = dist > 0 ? dy / dist : 0;
                    float perpX = -dirY;
                    float perpY = dirX;
                    float halfW = propertyExtra.attackRange * 0.5f;
                    float len = propertyExtra.attackRange * 1.5f;
                    // 矩形四个顶点 (从敌人前方延伸)
                    Path rectPath = new Path();
                    float x1 = screenX + perpX * halfW;
                    float y1 = screenY + perpY * halfW;
                    float x2 = screenX + dirX * len + perpX * halfW;
                    float y2 = screenY + dirY * len + perpY * halfW;
                    float x3 = screenX + dirX * len - perpX * halfW;
                    float y3 = screenY + dirY * len - perpY * halfW;
                    float x4 = screenX - perpX * halfW;
                    float y4 = screenY - perpY * halfW;
                    rectPath.moveTo(x1, y1);
                    rectPath.lineTo(x2, y2);
                    rectPath.lineTo(x3, y3);
                    rectPath.lineTo(x4, y4);
                    rectPath.close();
                    // 描边
                    rangePaint.setColor(Color.argb(strokeAlpha, 255, 60, 60));
                    rangePaint.setStyle(Paint.Style.STROKE);
                    rangePaint.setStrokeWidth(3);
                    canvas.drawPath(rectPath, rangePaint);
                    // 填充
                    rangePaint.setColor(Color.argb(fillAlpha, 255, 0, 0));
                    rangePaint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(rectPath, rangePaint);
                    break;
                }
                default: {
                    // 圆形: 以敌人为中心的圆
                    rangePaint.setColor(Color.argb(strokeAlpha, 255, 60, 60));
                    rangePaint.setStyle(Paint.Style.STROKE);
                    rangePaint.setStrokeWidth(3);
                    canvas.drawCircle(screenX, screenY, propertyExtra.attackRange, rangePaint);
                    rangePaint.setStyle(Paint.Style.FILL);
                    rangePaint.setColor(Color.argb(fillAlpha, 255, 0, 0));
                    canvas.drawCircle(screenX, screenY, propertyExtra.attackRange, rangePaint);
                    break;
                }
            }
        }
    }
}

package com.game.dream.enemy;

import com.game.dream.Character;
import com.game.dream.LogUtil;
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

    public enum EnemyLevel{
        NORMAL,
        LEADER,
        ELITE
    }

    protected State currentState;
    protected EnemyLevel enemyLevel;

    // Movement
    protected float targetX, targetY;
    protected long stateTimer;

    // Detection and attack ranges
    protected float detectionRange;
    protected float attackRange;

    // Experience reward when killed
    protected int rewardExp;
    protected int rewardMoney;

    protected int health;
    protected int maxHealth;
    protected int attackDamage;
    protected int magicDamage;
    protected int defense;
    protected int mana;
    protected int speed;

    // Aggro timer - how long enemy stays aggressive after being damaged
    protected long aggroEndTime;
    protected boolean isAggroed;

    // Possible drops
    protected List<Item> possibleDrops;
    protected float dropChance;

    public Enemy(float x, float y, int size, float detectionRange, float attackRange, int rewardExp, int rewardMoney) {
        super(x, y, size); // attack=10, defense=0, size=30

        this.currentState = State.IDLE;
        this.enemyLevel = EnemyLevel.NORMAL;
        this.targetX = x;
        this.targetY = y;
        this.stateTimer = System.currentTimeMillis();

        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
        this.attackCooldown = 1500;
        this.lastAttackTime = 0;

        this.rewardExp = rewardExp;
        this.rewardMoney = rewardMoney;

        this.aggroEndTime = 0;
        this.isAggroed = false;

        this.possibleDrops = new ArrayList<>();
        this.dropChance = 0.6f; // 30% chance to drop something
    }

    /**
     * Update enemy behavior - template method
     */
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        if (!isAlive()) return;

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

                if (distanceToPlayer < detectionRange) {
                    currentState = State.CHASING;
                    stateTimer = currentTime;
                }
                break;

            case CHASING:
                updateChasing(deltaSeconds, playerX, playerY, map, mapWidth, mapHeight);

                // Only return to idle if not aggroed AND player is far away
                if (!isAggroed && distanceToPlayer > detectionRange * 1.5f) {
                    currentState = State.IDLE;
                    stateTimer = currentTime;
                } else if (distanceToPlayer < attackRange) {
                    currentState = State.ATTACKING;
                    stateTimer = currentTime;
                }
                break;

            case ATTACKING:
                updateAttacking(deltaSeconds, playerX, playerY);

                if (distanceToPlayer > attackRange * 1.5f) {
                    currentState = State.CHASING;
                    stateTimer = currentTime;
                }
                break;
        }

        // Update animation
        updateAnimation(currentTime);
    }

    public int getAttackDamage() { return attackDamage; }

    public int getMagicDamage() { return magicDamage; }
    public int getDefense() { return defense; }
    public float getSpeed() { return speed; }
    public int getMana() { return mana; }
    public float getAttackRange() { return attackRange; }

    /**
     * Update idle behavior - must be implemented by subclass
     */
    protected abstract void updateIdle(long deltaTime, float deltaSeconds, int[][] map, int mapWidth, int mapHeight);

    /**
     * Update chasing behavior - common implementation
     */
    protected void updateChasing(float deltaSeconds, float playerX, float playerY,
                                 int[][] map, int mapWidth, int mapHeight) {
        targetX = playerX;
        targetY = playerY;

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
            float optimalDistance = attackRange * 0.7f;
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

        if (distance > 5) {
            float moveX = (dx / distance) * moveSpeed * deltaSeconds;
            float moveY = (dy / distance) * moveSpeed * deltaSeconds;

            x += moveX;
            y += moveY;
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
     * Set last attack time
     */
    public void setLastAttackTime(long time) {
        this.lastAttackTime = time;
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

        return (float)timeSinceLastAttack / attackCooldown;
    }

    /**
     * Get experience reward when this enemy is killed
     */
    public int getExperienceReward() {
        float factor = 1f;
        if (enemyLevel == EnemyLevel.ELITE) {
            factor = 10f;
        } else if (enemyLevel == EnemyLevel.LEADER) {
            factor = 3f;
        }
        return (int) (Utils.getWaveValueInt(rewardExp, 0.1f) * factor);
    }

    /**
     * Get money reward when this enemy is killed
     */
    public int getMoneyReward() {
        float factor = 1f;
        if (enemyLevel == EnemyLevel.ELITE) {
            factor = 10f;
        } else if (enemyLevel == EnemyLevel.LEADER) {
            factor = 3f;
        }
        return (int) (Utils.getWaveValueInt(rewardMoney, 0.1f) * factor);
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
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
        if (enemyLevel == EnemyLevel.ELITE) {
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

        if (Math.random() < dropChance && !possibleDrops.isEmpty()) {
            // Drop 1-2 items
            int numDrops = 1 + (int)(Math.random() * 2);

            for (int i = 0; i < numDrops && i < possibleDrops.size(); i++) {
                Item item = possibleDrops.get((int)(Math.random() * possibleDrops.size()));
                int quantity = 1 + (int)(Math.random() * 3); // 1-3 quantity

                if(item instanceof EquipmentItem){
                    quantity = 1;
                }

                drops.add(new ItemStack(item, quantity));
            }
        }

        return drops;
    }

    public abstract List<Item> getPossibleDropList();
}

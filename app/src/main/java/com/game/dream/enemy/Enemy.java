package com.game.dream.enemy;

import android.graphics.Canvas;

import com.game.dream.Character;

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

    protected State currentState;

    // Movement
    protected float targetX, targetY;
    protected long stateTimer;

    // Detection and attack ranges
    protected float detectionRange;
    protected float attackRange;

    public Enemy(float x, float y, int size, float detectionRange, float attackRange) {
        super(x, y, size); // attack=10, defense=0, size=30

        this.currentState = State.IDLE;
        this.targetX = x;
        this.targetY = y;
        this.stateTimer = System.currentTimeMillis();

        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
        this.attackCooldown = 1500;
        this.lastAttackTime = 0;
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

                if (distanceToPlayer > detectionRange * 1.5f) {
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
}

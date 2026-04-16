package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Base class for all enemies
 */
public abstract class Enemy {
    protected float x, y;
    protected float speed;
    protected int size = 60;

    // State
    protected boolean isAlive;
    protected int health;
    protected int maxHealth;

    // AI state
    protected enum State {
        IDLE,
        CHASING,
        ATTACKING
    }
    protected State currentState;

    // Movement
    protected float targetX, targetY;
    protected long stateTimer;
    protected long attackCooldown;
    protected long lastAttackTime;

    // Animation
    protected int animFrame;
    protected long lastAnimUpdate;
    protected float bobOffset;

    // Detection and attack ranges
    protected float detectionRange;
    protected float attackRange;

    public Enemy(float x, float y, int maxHealth, float speed, float detectionRange, float attackRange) {
        this.x = x;
        this.y = y;
        this.speed = speed;

        this.isAlive = true;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

        this.currentState = State.IDLE;
        this.targetX = x;
        this.targetY = y;
        this.stateTimer = System.currentTimeMillis();
        this.attackCooldown = 1500;
        this.lastAttackTime = 0;

        this.animFrame = 0;
        this.lastAnimUpdate = System.currentTimeMillis();
        this.bobOffset = 0;

        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
    }

    /**
     * Update enemy behavior - template method
     */
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        if (!isAlive) return;

        long currentTime = System.currentTimeMillis();
        float deltaSeconds = deltaTime / 1000f;

        // Calculate distance to player
        float dx = playerX - x;
        float dy = playerY - y;
        float distanceToPlayer = (float)Math.sqrt(dx * dx + dy * dy);

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

        float chaseSpeed = speed * 1.3f;
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
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float optimalDistance = attackRange * 0.7f;
            if (distance > optimalDistance) {
                float moveX = (dx / distance) * speed * 0.5f * deltaSeconds;
                float moveY = (dy / distance) * speed * 0.5f * deltaSeconds;

                x += moveX;
                y += moveY;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime > attackCooldown) {
            performAttack();
            lastAttackTime = currentTime;
        }
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
    protected void moveToTargetWithSpeed(float deltaSeconds, float moveSpeed) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

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

            bobOffset = (float)Math.sin(animFrame * Math.PI / 2) * 3;
        }
    }

    /**
     * Take damage
     */
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
    }

    /**
     * Draw the enemy - must be implemented by subclass
     */
    public abstract void draw(Canvas canvas, int offsetX, int offsetY);

    /**
     * Draw health bar - common implementation
     */
    protected void drawHealthBar(Canvas canvas, float cx, float cy, float scale) {
        float barWidth = 30 * scale;
        float barHeight = 4 * scale;
        float barX = cx - barWidth / 2;
        float barY = cy - 22 * scale + bobOffset;

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        canvas.drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, bgPaint);

        Paint healthPaint = new Paint();
        float healthPercent = (float)health / maxHealth;

        if (healthPercent > 0.6f) {
            healthPaint.setColor(Color.GREEN);
        } else if (healthPercent > 0.3f) {
            healthPaint.setColor(Color.YELLOW);
        } else {
            healthPaint.setColor(Color.RED);
        }

        canvas.drawRect(barX, barY, barX + barWidth * healthPercent, barY + barHeight, healthPaint);
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isAlive() { return isAlive; }
    public int getHealth() { return health; }
    public State getState() { return currentState; }
    public int getSize() { return size; }

    // Setters
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}

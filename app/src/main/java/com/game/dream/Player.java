package com.game.dream;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.enemy.Enemy;

public class Player extends Character {
    private static final int LAKE = 3;
    private static final int LAVA = 6;

    // Movement
    private boolean movingUp, movingDown, movingLeft, movingRight;

    // Animation
    private int walkCycle;
    private int facingDirection; // 0=down, 1=up, 2=left, 3=right

    // Magic combat
    private int magicDamage;
    private long lastMagicTime;
    private long magicCooldown;

    // Respawn
    private float respawnX;
    private float respawnY;

    // Renderer
    private PlayerRenderer renderer;

    public Player(float x, float y) {
        super(x, y, 80, 100, 10, 0, 200); // health=100, attack=10, defense=0, speed=150, size=80

        this.walkCycle = 0;
        this.facingDirection = 0;
        this.magicDamage = 15;
        this.lastMagicTime = 0;
        this.magicCooldown = 1200;

        // Respawn point (initial position)
        this.respawnX = x;
        this.respawnY = y;

        attackCooldown = 500; // Melee attack cooldown

        // Initialize renderer
        this.renderer = new PlayerRenderer(this);
    }

    @Override
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (renderer != null) {
            renderer.draw(canvas, offsetX, offsetY);
        }

        // Draw health bar above player (using inherited method)
        float screenX = getX() + offsetX;
        float screenY = getY() + offsetY;
        float scale = getSize() / 40f;
        drawHealthBar(canvas, screenX, screenY, scale);
    }

    public void update(int[][] map, int mapWidth, int mapHeight, int tileSize, long deltaTime) {
        boolean isMoving = false;
        float newX = x;
        float newY = y;

        // Convert speed from pixels/second to pixels/frame
        float deltaSeconds = deltaTime / 1000.0f;
        float moveAmount = speed * deltaSeconds;

        if (movingLeft) {
            newX -= moveAmount;
            isMoving = true;
            facingDirection = 2;
        }
        if (movingRight) {
            newX += moveAmount;
            isMoving = true;
            facingDirection = 3;
        }
        if (movingUp) {
            newY -= moveAmount;
            isMoving = true;
            facingDirection = 1;
        }
        if (movingDown) {
            newY += moveAmount;
            isMoving = true;
            facingDirection = 0;
        }

        // Debug logging
        if (movingUp || movingDown || movingLeft || movingRight) {
            android.util.Log.d("Player", "Moving: U=" + movingUp + " D=" + movingDown +
                    " L=" + movingLeft + " R=" + movingRight +
                    " Pos=(" + (int)x + "," + (int)y + ")");
        }

        // Update animation cycle (both walking and idle)
        if (isMoving) {
            walkCycle = (walkCycle + 2) % 60;
        } else {
            // Idle animation - slower breathing motion
            walkCycle = (walkCycle + 1) % 120;
        }

        // If not moving, return early (but animation still plays)
        if (!isMoving) {
            return;
        }

        // Collision detection with map boundaries
        newX = Math.max(size/2, Math.min(newX, mapWidth * tileSize - size/2));
        newY = Math.max(size/2, Math.min(newY, mapHeight * tileSize - size/2));

        // Check collision with impassable terrain (lake, lava)
        int gridX = (int)(newX / tileSize);
        int gridY = (int)(newY / tileSize);

        if (gridX >= 0 && gridX < mapWidth && gridY >= 0 && gridY < mapHeight) {
            int terrain = map[gridY][gridX];
            if (terrain != LAKE && terrain != LAVA) {
                x = newX;
                y = newY;
            }
        } else {
            x = newX;
            y = newY;
        }
    }

    /**
     * Perform melee attack
     */
    public boolean performMeleeAttack(java.util.List<Enemy> enemies) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackCooldown) {
            return false; // Still on cooldown
        }

        lastAttackTime = currentTime;

        // Attack area in front of player
        float attackRange = size * 1.5f;
        float attackX = x;
        float attackY = y;

        // Determine attack direction based on facing
        switch (facingDirection) {
            case 0: // Down
                attackY += attackRange;
                break;
            case 1: // Up
                attackY -= attackRange;
                break;
            case 2: // Left
                attackX -= attackRange;
                break;
            case 3: // Right
                attackX += attackRange;
                break;
        }

        // Check for enemies in attack range
        boolean hitSomething = false;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - attackX;
            float dy = enemy.getY() - attackY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < attackRange) {
                enemy.takeDamage(attackDamage);
                hitSomething = true;
            }
        }

        return hitSomething;
    }

    /**
     * Cast magic spell
     */
    public Projectile castSpell(float targetX, float targetY, Projectile.Type spellType) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMagicTime < magicCooldown) {
            return null; // Still on cooldown
        }

        lastMagicTime = currentTime;

        // Create projectile from player position to target
        return new Projectile(x, y, targetX, targetY, spellType);
    }

    /**
     * Cast triple spell - fires 3 projectiles at once
     */
    public java.util.List<Projectile> castTripleSpell(Projectile.Type spellType) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMagicTime < magicCooldown) {
            return null; // Still on cooldown
        }

        lastMagicTime = currentTime;

        java.util.List<Projectile> spells = new java.util.ArrayList<>();

        float baseAngle = 0;

        // Determine base angle from facing direction
        switch (getFacingDirection()) {
            case 0:
                baseAngle = 90;
                break;  // Down
            case 1:
                baseAngle = -90;
                break; // Up
            case 2:
                baseAngle = 180;
                break; // Left
            case 3:
                baseAngle = 0;
                break;   // Right
        }

        // Create 3 projectiles with 30 degree separation
        float[] angles = {baseAngle - 30, baseAngle, baseAngle + 30};
        float range = 300;

        for (float angle : angles) {
            // Convert angle to radians
            double rad = Math.toRadians(angle);

            // Calculate target position
            float spellTargetX = getX() + (float) (Math.cos(rad) * range);
            float spellTargetY = getY() + (float) (Math.sin(rad) * range);

            // Cast triple spell (returns list of 3 projectiles)
            spells.add(new Projectile(x, y, spellTargetX, spellTargetY, spellType));
        }

        return spells;
    }

    /**
     * Get attack cooldown progress (0-1)
     */
    public float getAttackCooldownProgress() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastAttackTime;
        return Math.min(1.0f, (float) elapsed / attackCooldown);
    }

    /**
     * Get magic cooldown progress (0-1)
     */
    public float getMagicCooldownProgress() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastMagicTime;
        return Math.min(1.0f, (float) elapsed / magicCooldown);
    }

    public void setRespawnPoint(float respawnX, float respawnY){
        this.respawnX = respawnX;
        this.respawnY = respawnY;
    }

    /**
     * Respawn player at respawn point
     */
    public void respawn() {
        x = respawnX;
        y = respawnY;
        health = maxHealth;
        isInvincible = true;
        invincibleEndTime = System.currentTimeMillis() + 3000; // 3 seconds invincibility after respawn
        android.util.Log.d("Player", "Respawned at (" + (int) x + ", " + (int) y + ")");
    }

    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }
    public void setMovingUp(boolean moving) { this.movingUp = moving; }
    public void setMovingDown(boolean moving) { this.movingDown = moving; }

    public int getFacingDirection() { return facingDirection; }
    public boolean isMoving() { return movingUp || movingDown || movingLeft || movingRight; }
    public int getWalkCycle() { return walkCycle; }
    public long getInvincibleEndTime() { return invincibleEndTime; }
}
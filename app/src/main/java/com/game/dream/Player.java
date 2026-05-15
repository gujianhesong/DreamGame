package com.game.dream;

import android.graphics.Canvas;

import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;
import com.game.dream.item.ConsumableItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;

import java.util.ArrayList;
import java.util.List;

public class Player extends Character {
    private static final int LAKE = 3;
    private static final int LAVA = 6;

    // Movement
    private boolean movingUp, movingDown, movingLeft, movingRight;

    // Animation
    private int walkCycle;
    private int facingDirection; // 0=down, 1=up, 2=left, 3=right

    // Attack animation
    private boolean isAttacking;
    private long attackStartTime;
    private int attackAnimationFrame;
    private static final int ATTACK_ANIMATION_DURATION = 300; // 300ms

    // Magic combat
    private long lastMagicTime;
    private long magicCooldown;

    // Respawn
    private float respawnX;
    private float respawnY;

    // Renderer
    private PlayerRenderer renderer;
    private GameEngine gameEngine;

    public Player(float x, float y) {
        super(x, y, 80);

        this.walkCycle = 0;
        this.facingDirection = 0;
        this.lastMagicTime = 0;
        this.magicCooldown = 1200;

        // Respawn point (initial position)
        this.respawnX = x;
        this.respawnY = y;

        attackCooldown = 500; // Melee attack cooldown

        // Attack animation
        this.isAttacking = false;
        this.attackStartTime = 0;
        this.attackAnimationFrame = 0;

        // Initialize renderer
        this.renderer = new PlayerRenderer(this);
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (renderer != null) {
            renderer.draw(canvas, offsetX, offsetY);
        }
    }

    public void update(int[][] map, int mapWidth, int mapHeight, int tileSize, long deltaTime) {
        // Update CC state
        updateCCState();

        // If stunned or rooted, prevent movement input from taking effect
        if (isStunned() || isRooted()) {
            // Still allow animation updates or mana regen, but skip position change
            //super.update(map, mapWidth, mapHeight, tileSize, deltaTime);
            return;
        }

        // Update attack animation
        updateAttackAnimation();

        boolean isMoving = false;
        float newX = x;
        float newY = y;

        // Convert speed from pixels/second to pixels/frame
        float deltaSeconds = deltaTime / 1000.0f;
        int speed = RoleSystem.getInstance().getRoleInfo().getSpeed();
        float speedRatio = 1f;
        if (isSlowed()) {
            speedRatio = 0.5f;
        }
        float moveAmount = (150 + speed * 0.5f) * deltaSeconds * speedRatio;

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
                    " Pos=(" + (int) x + "," + (int) y + ")");
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
        newX = Math.max(size / 2, Math.min(newX, mapWidth * tileSize - size / 2));
        newY = Math.max(size / 2, Math.min(newY, mapHeight * tileSize - size / 2));

        // Check collision with impassable terrain (lake, lava)
        int gridX = (int) (newX / tileSize);
        int gridY = (int) (newY / tileSize);

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

        RoleSystem.getInstance().getRoleInfo().setMapX((int) x);
        RoleSystem.getInstance().getRoleInfo().setMapY((int) y);
    }

    /**
     * Perform melee attack
     */
    public List<EnemyHitInfo> performMeleeAttack(java.util.List<Enemy> enemies) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackCooldown) {
            return null; // Still on cooldown
        }

        lastAttackTime = currentTime;

        // Attack area in front of player
        float attackRange = size * 2f;
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

        List<EnemyHitInfo> hits = new ArrayList<>();

        // Check for enemies in attack range
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - attackX;
            float dy = enemy.getY() - attackY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < attackRange) {
                AttackResult attackResult = BattleUtil.caculatePlayerAttackDamage(enemy);
                if (attackResult.isHit) {
                    int damage = attackResult.damageValue;
                    enemy.takeDamage(damage);

                    hits.add(new EnemyHitInfo(enemy, damage, attackResult.isCrit));

                    //是否暴击
                } else {
                    //未命中
                    hits.add(new EnemyHitInfo(enemy, -1, false));
                }
            }
        }

        return hits.isEmpty() ? null : hits;
    }

    /**
     * Cast magic spell
     */
    public Projectile castSpell(float targetX, float targetY, SkillType skillType) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMagicTime < magicCooldown) {
            return null; // Still on cooldown
        }

        lastMagicTime = currentTime;

        // Create projectile from player position to target
        return new Projectile(x, y, targetX, targetY, skillType);
    }

    /**
     * Cast triple spell - fires 3 projectiles at once
     */
    public java.util.List<Projectile> castTripleSpell(SkillType skillType) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMagicTime < magicCooldown) {
            return null; // Still on cooldown
        }

        int costMagic = 20;
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        if (roleInfo.getMp() < costMagic) {
            gameEngine.showWarning("魔法不足");
            return null;
        }
        roleInfo.setMp(roleInfo.getMp() - costMagic);

        lastMagicTime = currentTime;

        List<Projectile> spells = new ArrayList<>();

        if (skillType == SkillType.MAIN_ROOT) {
            spells.addAll(castRootSpell());
        } else {
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
            float[] angles = null;
            float range = 300;
            switch (skillType) {
                case MAIN_FIREBALL:
                    angles = new float[]{baseAngle - 60, baseAngle - 40, baseAngle - 20, baseAngle,
                            baseAngle + 20, baseAngle + 40, baseAngle + 60};
                    range = 400;
                    break;
                case MAIN_ICE_BOLT:
                    angles = new float[12];
                    for (int i = 0; i < 12; i++) {
                        angles[i] = baseAngle + 30 * i;
                    }
                    range = 300;
                    break;
                case MAIN_LIGHTNING:
                    angles = new float[]{baseAngle - 90, baseAngle, baseAngle + 90, baseAngle + 180};
                    range = 1000;
                    break;
            }

            for (float angle : angles) {
                // Convert angle to radians
                double rad = Math.toRadians(angle);

                // Calculate target position
                float spellTargetX = getX() + (float) (Math.cos(rad) * range);
                float spellTargetY = getY() + (float) (Math.sin(rad) * range);

                // Cast triple spell (returns list of 3 projectiles)
                spells.add(new Projectile(x, y, spellTargetX, spellTargetY, skillType));
            }
        }

        return spells;
    }

    private List<Projectile> castRootSpell() {
        // Find the nearest enemy within range
        Enemy target = null;
        float minDist = Float.MAX_VALUE;
        float spellRange = 300f;
        List<Projectile> projectiles = new ArrayList<>();

        List<Enemy> enemies = gameEngine.getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                float dx = enemy.getX() - getX();
                float dy = enemy.getY() - getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < spellRange && dist < minDist) {
                    minDist = dist;
                    target = enemy;
                }
            }
        }

        if (target != null) {
            Projectile rootProj = new Projectile(
                    getX(),
                    getY(),
                    target.getX(),
                    target.getY(),
                    SkillType.MAIN_ROOT // Use an existing visual type or add a TALISMAN type
            );

            // Set the effect type to ROOT
            rootProj.setEffectType(Projectile.EffectType.ROOT);
            projectiles.add(rootProj);
        } else {
            gameEngine.showCenterToast("范围内没有目标", 1000);
        }
        return projectiles;
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

    public void setRespawnPoint(float respawnX, float respawnY) {
        this.respawnX = respawnX;
        this.respawnY = respawnY;
    }

    /**
     * Respawn player at respawn point
     */
    public void respawn() {
        x = respawnX;
        y = respawnY;
        RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
        roleInfo.setHp(roleInfo.getBloodCap());
        roleInfo.setMp(roleInfo.getMagicCap());
        isInvincible = true;
        invincibleEndTime = System.currentTimeMillis() + 3000; // 3 seconds invincibility after respawn
        android.util.Log.d("Player", "Respawned at (" + (int) x + ", " + (int) y + ")");
    }

    /**
     * Trigger melee attack animation
     */
    public void triggerAttackAnimation() {
        isAttacking = true;
        attackStartTime = System.currentTimeMillis();
        attackAnimationFrame = 0;
    }

    /**
     * Update attack animation state
     */
    public void updateAttackAnimation() {
        if (!isAttacking) return;

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - attackStartTime;

        if (elapsed >= ATTACK_ANIMATION_DURATION) {
            isAttacking = false;
            attackAnimationFrame = 0;
        } else {
            // Calculate animation frame (0-10)
            attackAnimationFrame = (int) (elapsed * 10 / ATTACK_ANIMATION_DURATION);
        }
    }

    /**
     * Check if currently playing attack animation
     */
    public boolean isAttacking() {
        if (!isAttacking) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - attackStartTime >= ATTACK_ANIMATION_DURATION) {
            isAttacking = false;
            return false;
        }
        return true;
    }

    /**
     * Get attack animation progress (0-1)
     */
    public float getAttackAnimationProgress() {
        if (!isAttacking) return 0f;

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - attackStartTime;
        return Math.min(1.0f, (float) elapsed / ATTACK_ANIMATION_DURATION);
    }

    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
    }

    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
    }

    public void setMovingUp(boolean moving) {
        this.movingUp = moving;
    }

    public void setMovingDown(boolean moving) {
        this.movingDown = moving;
    }

    public int getFacingDirection() {
        return facingDirection;
    }

    public boolean isMoving() {
        return movingUp || movingDown || movingLeft || movingRight;
    }

    public int getWalkCycle() {
        return walkCycle;
    }

    public long getInvincibleEndTime() {
        return invincibleEndTime;
    }

    @Override
    public int getHealth() {
        return RoleSystem.getInstance().getRoleInfo().getHp();
    }

    @Override
    public int getMaxHealth() {
        return RoleSystem.getInstance().getRoleInfo().getBloodCap();
    }

    public boolean takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();

        // Check if invincible
        if (isInvincible && currentTime < invincibleEndTime) {
            return false; // No damage taken
        }

        // Apply damage
        int health = getHealth();
        health -= damage;
        health = Math.max(0, health);
        lastDamageTime = currentTime;

        RoleSystem.getInstance().getRoleInfo().setHp(health);

        // Check if dead
        if (health <= 0) {
            return true; // Died
        }

        return false; // Still alive
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /**
     * Use item from inventory
     */
    public boolean useItem(int index) {
        if (ItemSystem.getInstance().getItems().size() <= index) return false;

        ItemStack stack = ItemSystem.getInstance().getItems().get(index);
        Item item = stack.getItem();

        if (item.getType() != Item.Type.CONSUMABLE) {
            return false;
        }

        ConsumableItem consumable = (ConsumableItem) item;

        // Apply effect based on type
        switch (consumable.getEffectType()) {
            case HEAL_HP:

                break;
            case HEAL_MP:
                break;
            case BUFF_ATTACK:
                break;
            case BUFF_DEFENSE:
                break;
            case BUFF_SPEED:
                break;
        }

        // Consume the item
        return ItemSystem.getInstance().useItem(index);
    }

}
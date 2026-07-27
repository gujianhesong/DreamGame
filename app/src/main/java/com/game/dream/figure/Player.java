package com.game.dream.figure;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.GameEngine;
import com.game.dream.bean.AttackResult;
import com.game.dream.bean.EnemyHitInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.enemy.Enemy;
import com.game.dream.enums.SkillType;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.enums.XiLianType;
import com.game.dream.item.ConsumableItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.map.MapGenerator;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.ui.FloatingText;
import com.game.dream.utils.BattleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Player extends Character {
    private static final int LAKE = 3;
    private static final int LAVA = 6;

    // Movement
    private boolean movingUp, movingDown, movingLeft, movingRight;

    // Dash (冲刺) - 集气系统
    private boolean isDashing;
    private long dashStartTime;
    private int dashDirection; // 0=down, 1=up, 2=left, 3=right
    private float dashDistanceRemaining;
    private static final float DASH_DISTANCE = 400f; // 冲刺总距离(像素)
    private static final float DASH_SPEED = 800f;   // 冲刺速度(像素/秒)
    private static final long DASH_IFRAME_DURATION = 250; // 冲刺无敌帧时长(毫秒)
    // 集气系统
    private static final float DASH_CHARGE_MAX = 100f;   // 集气上限
    private static final float DASH_CHARGE_COST = 20f;   // 每次冲刺消耗
    private static final float DASH_CHARGE_REGEN = 10f;  // 每秒回复量
    private float dashCharge = DASH_CHARGE_MAX;
    private long lastChargeUpdateTime;

    // Dash trail for visual effect
    private java.util.List<DashTrailPoint> dashTrail = new java.util.ArrayList<>();
    private static final int MAX_TRAIL_POINTS = 8;

    // Track enemies hit during current dash (prevent repeated hits)
    private java.util.Set<com.game.dream.enemy.Enemy> dashHitEnemies = new java.util.HashSet<>();

    // Animation
    private int walkCycle;
    private int facingDirection; // 0=down, 1=up, 2=left, 3=right

    // Attack animation
    private boolean isAttacking;
    private long attackStartTime;
    private int attackAnimationFrame;
    private static final int ATTACK_ANIMATION_DURATION = 300; // 300ms

    // Attack lunge (攻击前冲)
    private float attackLungeRemaining = 0;
    private float attackLungeDirX = 0;
    private float attackLungeDirY = 0;
    private static final float ATTACK_LUNGE_DISTANCE = 120f; // 前冲距离(比冲刺400短很多)
    private static final float ATTACK_LUNGE_SPEED = 600f;    // 前冲速度(像素/秒)
    // 前冲过程中已撞过的敌人(防止重复击退)
    private java.util.Set<com.game.dream.enemy.Enemy> lungeHitEnemies = new java.util.HashSet<>();

    // Respawn
    private float respawnX;
    private float respawnY;

    // Renderer
    private PlayerRenderer renderer;

    private HashMap<SkillType, Long> lastCasterTimeHashMap = new HashMap<>();


    public Player(float x, float y) {
        super(x, y, 85);

        this.walkCycle = 0;
        this.facingDirection = 0;

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

        // 集气回复: 每秒回复 DASH_CHARGE_REGEN
        if (dashCharge < DASH_CHARGE_MAX && deltaTime > 0) {
            float deltaSeconds = deltaTime / 1000.0f;
            dashCharge = Math.min(DASH_CHARGE_MAX, dashCharge + DASH_CHARGE_REGEN * deltaSeconds);
        }

        // Handle knockback movement FIRST (overrides everything, including stun/root)
        if (isBeingKnockedBack()) {
            float[] kbMove = updateKnockback(deltaTime);
            if (kbMove != null) {
                float newX = x + kbMove[0];
                float newY = y + kbMove[1];
                // Collision detection with map boundaries
                newX = Math.max(size / 2, Math.min(newX, mapWidth * tileSize - size / 2));
                newY = Math.max(size / 2, Math.min(newY, mapHeight * tileSize - size / 2));
                // Check impassable terrain
                int gridX = (int) (newX / tileSize);
                int gridY = (int) (newY / tileSize);
                if (gridX >= 0 && gridX < mapWidth && gridY >= 0 && gridY < mapHeight) {
                    int terrain = map[gridY][gridX];
                    if (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA && terrain != MapGenerator.VILLAGE_NO_PASS) {
                        x = newX;
                        y = newY;
                    }
                }
                RoleSystem.getInstance().getRoleInfo().setMapX((int) x);
                RoleSystem.getInstance().getRoleInfo().setMapY((int) y);
            }
            return;
        }

        // If stunned or rooted, prevent movement input from taking effect
        if (isStunned() || isRooted()) {
            return;
        }

        // Update attack animation
        updateAttackAnimation();

        // Update dash
        if (isDashing) {
            updateDash(map, mapWidth, mapHeight, tileSize, deltaTime);
            return;
        }

        // 攻击前冲移动
        if (attackLungeRemaining > 0) {
            float deltaSeconds = deltaTime / 1000.0f;
            float lungeMove = ATTACK_LUNGE_SPEED * deltaSeconds;
            lungeMove = Math.min(lungeMove, attackLungeRemaining);

            float newX = x + attackLungeDirX * lungeMove;
            float newY = y + attackLungeDirY * lungeMove;
            // 边界限制
            newX = Math.max(size / 2, Math.min(newX, mapWidth * tileSize - size / 2));
            newY = Math.max(size / 2, Math.min(newY, mapHeight * tileSize - size / 2));
            // 地形检测
            boolean terrainBlocked = false;
            int gridX = (int) (newX / tileSize);
            int gridY = (int) (newY / tileSize);
            if (gridX >= 0 && gridX < mapWidth && gridY >= 0 && gridY < mapHeight) {
                int terrain = map[gridY][gridX];
                if (terrain == MapGenerator.LAKE || terrain == MapGenerator.LAVA || terrain == MapGenerator.VILLAGE_NO_PASS) {
                    terrainBlocked = true;
                }
            }

            if (terrainBlocked) {
                attackLungeRemaining = 0; // 地形阻挡停止前冲
            } else {
                x = newX;
                y = newY;
                attackLungeRemaining -= lungeMove;
            }
            RoleSystem.getInstance().getRoleInfo().setMapX((int) x);
            RoleSystem.getInstance().getRoleInfo().setMapY((int) y);
        }

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
            if (terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA && terrain != MapGenerator.VILLAGE_NO_PASS) {
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
     * Start a dash in the given direction
     * @param direction 0=down, 1=up, 2=left, 3=right
     */
    public void startDash(int direction) {
        long currentTime = System.currentTimeMillis();

        // Check charge (集气系统)
        if (dashCharge < DASH_CHARGE_COST) {
            return;
        }

        // Cannot dash while stunned or rooted
        if (isStunned() || isRooted()) {
            return;
        }

        isDashing = true;
        dashStartTime = currentTime;
        dashDirection = direction;
        dashDistanceRemaining = DASH_DISTANCE;
        facingDirection = direction;

        // 消耗集气
        dashCharge -= DASH_CHARGE_COST;
        lastChargeUpdateTime = currentTime;

        // Clear trail
        dashTrail.clear();
        // Clear dash hit tracking
        dashHitEnemies.clear();

        android.util.Log.d("Player", "Dash started in direction: " + direction);
    }

    /**
     * Update dash movement
     */
    private void updateDash(int[][] map, int mapWidth, int mapHeight, int tileSize, long deltaTime) {
        float deltaSeconds = deltaTime / 1000.0f;
        float dashMoveAmount = DASH_SPEED * deltaSeconds;

        // Add trail point before moving
        if (dashTrail.size() >= MAX_TRAIL_POINTS) {
            dashTrail.remove(0);
        }
        dashTrail.add(new DashTrailPoint(x, y, System.currentTimeMillis()));

        float newX = x;
        float newY = y;

        switch (dashDirection) {
            case 0: newY += dashMoveAmount; break; // Down
            case 1: newY -= dashMoveAmount; break; // Up
            case 2: newX -= dashMoveAmount; break; // Left
            case 3: newX += dashMoveAmount; break; // Right
        }

        dashDistanceRemaining -= dashMoveAmount;

        // Collision detection with map boundaries
        newX = Math.max(size / 2, Math.min(newX, mapWidth * tileSize - size / 2));
        newY = Math.max(size / 2, Math.min(newY, mapHeight * tileSize - size / 2));

        // Check collision with impassable terrain
        int gridX = (int) (newX / tileSize);
        int gridY = (int) (newY / tileSize);

        if (gridX >= 0 && gridX < mapWidth && gridY >= 0 && gridY < mapHeight) {
            int terrain = map[gridY][gridX];
            if (terrain == MapGenerator.LAKE || terrain == MapGenerator.LAVA || terrain == MapGenerator.VILLAGE_NO_PASS) {
                // Hit impassable, stop dash
                dashDistanceRemaining = 0;
            } else {
                x = newX;
                y = newY;
            }
        } else {
            x = newX;
            y = newY;
        }

        // Fast walk cycle during dash
        walkCycle = (walkCycle + 4) % 60;

        RoleSystem.getInstance().getRoleInfo().setMapX((int) x);
        RoleSystem.getInstance().getRoleInfo().setMapY((int) y);

        // Check if dash is complete
        if (dashDistanceRemaining <= 0) {
            isDashing = false;
            // Fade out trail
            dashTrail.clear();
        }
    }

    /**
     * Check if dash has enough charge
     */
    public boolean isDashOnCooldown() {
        return dashCharge < DASH_CHARGE_COST;
    }

    /**
     * Get dash charge progress (0-1, 1 = full charge)
     */
    public float getDashCooldownProgress() {
        return dashCharge / DASH_CHARGE_MAX;
    }

    /**
     * Get current dash charge value
     */
    public float getDashCharge() {
        return dashCharge;
    }

    /**
     * Get max dash charge
     */
    public float getDashChargeMax() {
        return DASH_CHARGE_MAX;
    }

    /**
     * Get dash charge cost per dash
     */
    public float getDashChargeCost() {
        return DASH_CHARGE_COST;
    }

    public boolean isDashing() {
        return isDashing;
    }

    /**
     * Check if player is in dash invincibility window (i-frames)
     */
    public boolean isDashInvincible() {
        return isDashing && (System.currentTimeMillis() - dashStartTime) < DASH_IFRAME_DURATION;
    }

    /**
     * Get current dash direction (0=down, 1=up, 2=left, 3=right)
     */
    public int getDashDirection() {
        return dashDirection;
    }

    /**
     * Get set of enemies hit during current dash
     */
    public java.util.Set<com.game.dream.enemy.Enemy> getDashHitEnemies() {
        return dashHitEnemies;
    }

    public java.util.List<DashTrailPoint> getDashTrail() {
        return dashTrail;
    }

    /**
     * Perform melee attack
     */
    public List<EnemyHitInfo> performMeleeAttack(java.util.List<Enemy> enemies) {
        long currentTime = System.currentTimeMillis();

        long finalAttackCooldown = attackCooldown;
        float attackSpeedRatio = ItemSystem.getInstance().getTotalXiLianPropWithAllEquiped(XiLianType.XL_attackSpeedRatio);
        if (attackSpeedRatio > 0) {
            finalAttackCooldown = (int) (finalAttackCooldown * (1f - attackSpeedRatio));
        }

        if (currentTime - lastAttackTime < finalAttackCooldown) {
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

                    if (ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_Xixue)) {
                        //吸血
                        int takeHp = (int) (damage * 0.1f);
                        if (takeHp > 0) {
                            RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                            roleInfo.setHp(Math.min(roleInfo.getBloodCap(), roleInfo.getHp() + takeHp));
                            GameEngine.getInstance().showFloatText("吸血+" + takeHp, FloatingText.Type.HEAL);
                        }
                    }
                } else {
                    //未命中
                    hits.add(new EnemyHitInfo(enemy, -1, false));
                }
            }
        }

        return hits.isEmpty() ? null : hits;
    }

    /**
     * Get attack cooldown progress (0-1)
     */
    public float getAttackCooldownProgress() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastAttackTime;

        //装备攻击速度加成
        long finalAttackCooldown = attackCooldown;
        float attackSpeedRatio = ItemSystem.getInstance().getTotalXiLianPropWithAllEquiped(XiLianType.XL_attackSpeedRatio);
        if (attackSpeedRatio > 0) {
            finalAttackCooldown = (int) (finalAttackCooldown * (1f - attackSpeedRatio));
        }

        return Math.min(1.0f, (float) elapsed / finalAttackCooldown);
    }

    /**
     * Get magic cooldown progress (0-1)
     */
    public float getMagicCooldownProgress(SkillType skillType) {
        long currentTime = System.currentTimeMillis();
        long lastMagicTime = 0;
        if (lastCasterTimeHashMap.containsKey(skillType)) {
            lastMagicTime = lastCasterTimeHashMap.get(skillType);
        }

        long magicCooldown = 2000;
        SkillInfo skillInfo = SkillSystem.getInstance().getPlayerSkill(skillType);
        if (skillInfo != null) {
            magicCooldown = skillInfo.getCooldownSeconds() * 1000L;

            //装备法术速度加成
            float magicSpeedRatio = ItemSystem.getInstance().getTotalXiLianPropWithAllEquiped(XiLianType.XL_magicSpeedRatio);
            if (magicSpeedRatio > 0) {
                magicCooldown = (int) (magicCooldown * (1f - magicSpeedRatio));
            }
        }
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

        // 设置攻击前冲方向
        attackLungeRemaining = ATTACK_LUNGE_DISTANCE;
        lungeHitEnemies.clear();
        switch (facingDirection) {
            case 0: attackLungeDirX = 0;  attackLungeDirY = 1;  break; // down
            case 1: attackLungeDirX = 0;  attackLungeDirY = -1; break; // up
            case 2: attackLungeDirX = -1; attackLungeDirY = 0;  break; // left
            case 3: attackLungeDirX = 1;  attackLungeDirY = 0;  break; // right
        }
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
     * Check if attack lunge is still in progress
     */
    public boolean isAttackLunging() {
        return attackLungeRemaining > 0;
    }

    /**
     * Get set of enemies already hit by current attack lunge
     */
    public java.util.Set<com.game.dream.enemy.Enemy> getLungeHitEnemies() {
        return lungeHitEnemies;
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
        return isDashing || movingUp || movingDown || movingLeft || movingRight;
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

        updateBuffs(); // Check if buff expired

        // Check if invincible
        if (isInvincible && currentTime < invincibleEndTime) {
            return false; // No damage taken
        }

        // 冲刺无敌帧期间免疫伤害
        if (isDashing && (currentTime - dashStartTime) < DASH_IFRAME_DURATION) {
            return false;
        }

        // Apply damage
        int health = getHealth();
        health -= damage;
        health = Math.max(0, health);
        lastDamageTime = currentTime;

        // Trigger hit flash (red tint)
        triggerHitFlash();

        if (isJinGangState) {
            health = Math.max(1, health);
        }

        if (health <= 0 && ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_ShenYou)) {
            //神佑
            if (Math.random() < 0.2) {
                health = RoleSystem.getInstance().getRoleInfo().getBloodCap();

                GameEngine.getInstance().showCenterToast("神佑复生！");
            }
        }

        RoleSystem.getInstance().getRoleInfo().setHp(health);

        // Check if dead
        if (health <= 0) {
            return true; // Died
        }

        return false; // Still alive
    }

    /**
     * Heal the player blood for a specific amount
     */
    public void healBlood(int amount) {
        if (amount <= 0) return;

        int oldHealth = this.getHealth();
        int newHealth = Math.min(oldHealth + amount, getMaxHealth());
        RoleSystem.getInstance().getRoleInfo().setHp(newHealth);

        if (newHealth > oldHealth) {
            lastHealBloodTime = System.currentTimeMillis(); // Trigger visual effect
        }
    }

    /**
     * Heal the player blood for a specific amount
     */
    public void healMagic(int amount) {
        if (amount <= 0) return;

        int oldMp = RoleSystem.getInstance().getRoleInfo().getMp();
        int newMp = Math.min(oldMp + amount, RoleSystem.getInstance().getRoleInfo().getMagicCap());
        RoleSystem.getInstance().getRoleInfo().setMp(newMp);

        if (newMp > oldMp) {
            lastHealMagicTime = System.currentTimeMillis(); // Trigger visual effect
        }
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

    public long getLastMagicTime(SkillType skillType) {
        long lastMagicTime = 0;
        if (lastCasterTimeHashMap.containsKey(skillType)) {
            lastMagicTime = lastCasterTimeHashMap.get(skillType);
        }
        return lastMagicTime;
    }

    public void setLastMagicTime(SkillType skillType, long lastMagicTime) {
        lastCasterTimeHashMap.put(skillType, lastMagicTime);
    }

    public void setFacingDirection(int facingDirection) {
        this.facingDirection = facingDirection;
    }

    /**
     * Represents a point in the dash trail for visual effect
     */
    public static class DashTrailPoint {
        public final float x;
        public final float y;
        public final long timestamp;

        public DashTrailPoint(float x, float y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }

}
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

    // 攻击类型系统
    public enum AttackType {
        /** 靠近普通攻击: 原地前摇后近战伤害 */
        MELEE,
        /** 猛扑: 固定高速扑向玩家，碰撞触发伤害，命中后随机方向撤退 */
        POUNCE,
        /** 冲击: 锁定方向直线冲锋，撞到玩家触发伤害，持续800ms */
        CHARGE,
        /** 连续爪击: 短时间内多次近战攻击，每击独立判定伤害 */
        COMBO,
        /** 吸血撕咬: 近战攻击命中后回复自身生命 */
        DRAIN_BITE,
        /** 环绕斩击: 原地旋转持续多段伤害，范围内所有方向均判定 */
        SPIN_ATTACK,
        /** 闪现突击: 短暂前摇后高速闪现到玩家身边攻击 */
        BLINK_STRIKE,
        /** 跳跃砸击: 蓄力后跳向玩家位置，落地造成大范围伤害 */
        LEAP_SLAM
    }
    protected List<AttackType> availableAttackTypes = new ArrayList<>();
    protected AttackType currentAttackType = AttackType.MELEE;

    // 冲击(Charge)攻击相关
    protected boolean isCharging = false;
    protected long chargeStartTime = 0;
    protected float chargeDirectionX = 0;
    protected float chargeDirectionY = 0;
    protected float chargeSpeedMultiplier = 3.5f;

    // 猛扑(Pounce)攻击相关
    protected float pounceFixedSpeed = 1000f; // 猛扑固定速度(传入moveToTargetWithSpeed的参数)，实际约600px/s
    protected boolean isPounceRetreating = false;
    protected long pounceRetreatStartTime = 0;
    protected float pounceRetreatAngle = 0;
    protected float pounceRetreatSpeedMultiplier = 2.5f;

    // 连续爪击(Combo)攻击相关
    protected int comboHitCount = 3;        // 总连击数
    protected int comboCurrentHit = 0;      // 当前第几击(0-based)
    protected long comboHitStartTime = 0;   // 当前击的前摇开始时间
    protected long comboHitInterval = 200;  // 每击前摇时长(ms)

    // 吸血撕咬(Drain Bite)攻击相关
    protected float drainHealPercent = 0.5f; // 吸血比例: 造成伤害的50%转化为治疗
    protected int pendingDrainHeal = 0;      // 待处理的吸血回复量

    // 环绕斩击(Spin Attack)攻击相关
    protected boolean isSpinning = false;
    protected long spinStartTime = 0;
    protected long spinDuration = 1500;     // 旋转总时长(ms)
    protected long spinHitInterval = 300;   // 每段伤害间隔(ms)
    protected long spinLastHitTime = 0;     // 上次造成伤害的时间

    // 闪现突击(Blink Strike)攻击相关
    protected boolean isBlinkDashing = false;
    protected float blinkTargetX = 0;        // 闪现目标位置(锁定玩家当时的位置)
    protected float blinkTargetY = 0;
    protected float blinkDashSpeed = 2000f;  // 闪现移动速度(传入moveToTargetWithSpeed的参数)

    // 跳跃砸击(Leap Slam)攻击相关
    protected boolean isSlamLeaping = false;
    protected float slamTargetX = 0;          // 砸击目标位置(锁定玩家当时的位置)
    protected float slamTargetY = 0;
    protected float slamLeapSpeed = 1500f;    // 跳跃移动速度(传入moveToTargetWithSpeed的参数)
    protected float slamLandRange = 150;      // 落地砸击伤害范围(比attackRange大)

    // BOSS召唤小弟相关
    protected boolean hasSummonedMinions = false; // 是否已召唤过小弟
    protected boolean pendingSummon = false;      // 待处理召唤(通知GameEngine生成小弟)

    // 灼烧(DOT)相关
    protected int burnDamagePerSecond = 0;   // 灼烧每秒伤害
    protected long burnEndTime = 0;          // 灼烧结束时间
    protected long burnLastTickTime = 0;     // 上次灼烧伤害时间

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

        // 灼烧DOT伤害
        if (burnDamagePerSecond > 0 && System.currentTimeMillis() < burnEndTime) {
            long currentTime2 = System.currentTimeMillis();
            if (currentTime2 - burnLastTickTime >= 1000) { // 每秒跳一次
                health -= burnDamagePerSecond;
                burnLastTickTime = currentTime2;
                if (health <= 0) health = 0;
            }
        } else {
            burnDamagePerSecond = 0;
        }

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

        // If stunned or frozen, skip all AI logic
        if (isStunned() || isFrozen()) return;

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
                } else if (distanceToPlayer < propertyExtra.attackRange
                        && (currentAttackType != AttackType.POUNCE || currentTime - lastAttackTime >= attackCooldown)) {
                    currentState = State.ATTACKING;
                    stateTimer = currentTime;
                    selectCurrentAttackType();
                    // 开始攻击前摇
                    isWindingUp = true;
                    windUpStartTime = currentTime;
                }
                break;

            case ATTACKING:
                switch (currentAttackType) {
                    case POUNCE:
                        if (isPounceRetreating) {
                            // === 猛扑后撤退: 向随机方向跑开 ===
                            targetX = x + (float) Math.cos(pounceRetreatAngle) * 500;
                            targetY = y + (float) Math.sin(pounceRetreatAngle) * 500;
                            moveToTargetWithSpeed(deltaSeconds, speed * pounceRetreatSpeedMultiplier);

                            long retreatDuration = (long) (attackCooldown * 1.5f);
                            if (currentTime - pounceRetreatStartTime >= retreatDuration) {
                                // 撤退结束，重置攻击计时，确保追击一段时间后才能再次猛扑
                                isPounceRetreating = false;
                                lastAttackTime = currentTime;
                                currentState = State.CHASING;
                                stateTimer = currentTime;
                            }
                        } else if (isWindingUp) {
                            // 猛扑: 前摇期间以固定速度扑向玩家
                            targetX = playerX;
                            targetY = playerY;
                            moveToTargetWithSpeed(deltaSeconds, pounceFixedSpeed);

                            // 碰撞检测: 扑击过程中撞到玩家提前触发攻击
                            if (distanceToPlayer <= size * 0.6f) {
                                isWindingUp = false;
                                attackJustFired = true;
                                lastAttackTime = currentTime;
                            }

                            if (isWindingUp && currentTime - windUpStartTime >= windUpDuration) {
                                // 前摇时间结束，猛扑冲完了，不管有没有碰到都进入撤退
                                isWindingUp = false;
                            }
                        } else {
                            // 猛扑结束，进入撤退阶段
                            isPounceRetreating = true;
                            pounceRetreatStartTime = currentTime;
                            // 随机撤退角度: 360度随机方向
                            pounceRetreatAngle = (float) (Math.random() * Math.PI * 2);
                        }
                        break;

                    case CHARGE:
                        if (isWindingUp) {
                            // 冲击前摇: 锁定玩家方向
                            targetX = playerX;
                            targetY = playerY;

                            if (currentTime - windUpStartTime >= windUpDuration) {
                                // 前摇结束，开始直线冲击
                                isWindingUp = false;
                                isCharging = true;
                                chargeStartTime = currentTime;
                                float cdx = playerX - x;
                                float cdy = playerY - y;
                                float cdist = (float) Math.sqrt(cdx * cdx + cdy * cdy);
                                if (cdist > 0) {
                                    chargeDirectionX = cdx / cdist;
                                    chargeDirectionY = cdy / cdist;
                                }
                            }
                        } else if (isCharging) {
                            // 直线冲击阶段
                            float chargeSpd = speed * chargeSpeedMultiplier;
                            x += chargeDirectionX * chargeSpd * deltaSeconds;
                            y += chargeDirectionY * chargeSpd * deltaSeconds;
                            x = Math.max(size, Math.min(x, mapWidth - size));
                            y = Math.max(size, Math.min(y, mapHeight - size));

                            // 冲击碰撞检测: 撞到玩家触发攻击，但不中断冲击
                            if (distanceToPlayer <= size * 0.7f && !attackJustFired) {
                                attackJustFired = true;
                                lastAttackTime = currentTime;
                            }

                            // 冲击超时结束(800ms)
                            if (currentTime - chargeStartTime >= 800) {
                                isCharging = false;
                                currentState = State.CHASING;
                                stateTimer = currentTime;
                            }
                        }
                        break;

                    case COMBO:
                        if (isWindingUp) {
                            // 连击期间缓慢靠近玩家
                            targetX = playerX;
                            targetY = playerY;
                            moveToTargetWithSpeed(deltaSeconds, speed * 0.5f);

                            if (currentTime - comboHitStartTime >= comboHitInterval) {
                                // 当前击触发
                                if (distanceToPlayer < propertyExtra.attackRange * 1.2f) {
                                    attackJustFired = true;
                                    lastAttackTime = currentTime;
                                }
                                comboCurrentHit++;
                                if (comboCurrentHit >= comboHitCount) {
                                    // 连击结束
                                    isWindingUp = false;
                                    currentState = State.CHASING;
                                    stateTimer = currentTime;
                                } else {
                                    // 开始下一击前摇
                                    comboHitStartTime = currentTime;
                                }
                            }
                        }
                        break;

                    case DRAIN_BITE:
                        if (isWindingUp) {
                            targetX = playerX;
                            targetY = playerY;

                            if (currentTime - windUpStartTime >= windUpDuration) {
                                isWindingUp = false;
                                if (distanceToPlayer < propertyExtra.attackRange * 1.2f) {
                                    attackJustFired = true;
                                    pendingDrainHeal = (int) (attackDamage * drainHealPercent);
                                    lastAttackTime = currentTime;
                                }
                            }
                        } else {
                            updateAttacking(deltaSeconds, playerX, playerY);

                            if (currentTime - lastAttackTime >= attackCooldown) {
                                if (distanceToPlayer < propertyExtra.attackRange) {
                                    isWindingUp = true;
                                    windUpStartTime = currentTime;
                                } else {
                                    currentState = State.CHASING;
                                    stateTimer = currentTime;
                                }
                            }
                        }
                        break;

                    case SPIN_ATTACK:
                        if (isWindingUp) {
                            // 短暂前摇: 蓄力准备旋转
                            targetX = playerX;
                            targetY = playerY;

                            if (currentTime - windUpStartTime >= windUpDuration) {
                                isWindingUp = false;
                                isSpinning = true;
                                spinStartTime = currentTime;
                                spinLastHitTime = currentTime;
                            }
                        } else if (isSpinning) {
                            // 旋转中: 原地旋转，每段间隔触发伤害
                            // 面向玩家方向旋转
                            targetX = x + (playerX - x);
                            targetY = y + (playerY - y);

                            if (currentTime - spinLastHitTime >= spinHitInterval) {
                                if (distanceToPlayer < propertyExtra.attackRange) {
                                    attackJustFired = true;
                                    lastAttackTime = currentTime;
                                }
                                spinLastHitTime = currentTime;
                            }

                            if (currentTime - spinStartTime >= spinDuration) {
                                // 旋转结束
                                isSpinning = false;
                                currentState = State.CHASING;
                                stateTimer = currentTime;
                            }
                        }
                        break;

                    case BLINK_STRIKE:
                        if (isWindingUp) {
                            // 前摇: 原地蓄力面向玩家
                            targetX = playerX;
                            targetY = playerY;

                            if (currentTime - windUpStartTime >= windUpDuration) {
                                isWindingUp = false;
                                isBlinkDashing = true;
                                // 锁定玩家当前位置作为闪现目标
                                blinkTargetX = playerX;
                                blinkTargetY = playerY;
                            }
                        } else if (isBlinkDashing) {
                            // 闪现: 高速冲向锁定位置
                            targetX = blinkTargetX;
                            targetY = blinkTargetY;
                            moveToTargetWithSpeed(deltaSeconds, blinkDashSpeed);

                            float distToTarget = (float) Math.sqrt(
                                    (x - blinkTargetX) * (x - blinkTargetX) +
                                    (y - blinkTargetY) * (y - blinkTargetY));

                            // 到达目标位置或碰到玩家，触发攻击
                            if (distToTarget <= size * 0.5f || distanceToPlayer <= size * 0.7f) {
                                isBlinkDashing = false;
                                if (distanceToPlayer < propertyExtra.attackRange * 1.2f) {
                                    attackJustFired = true;
                                    lastAttackTime = currentTime;
                                }
                            }
                        } else {
                            // 闪现结束，切回追击
                            currentState = State.CHASING;
                            stateTimer = currentTime;
                        }
                        break;

                    case LEAP_SLAM:
                        if (isWindingUp) {
                            // 前摇: 蓄力起跳，面向玩家
                            targetX = playerX;
                            targetY = playerY;

                            if (currentTime - windUpStartTime >= windUpDuration) {
                                isWindingUp = false;
                                isSlamLeaping = true;
                                // 锁定玩家当前位置作为砸击目标
                                slamTargetX = playerX;
                                slamTargetY = playerY;
                            }
                        } else if (isSlamLeaping) {
                            // 跳跃: 高速冲向锁定位置
                            targetX = slamTargetX;
                            targetY = slamTargetY;
                            moveToTargetWithSpeed(deltaSeconds, slamLeapSpeed);

                            float distToTarget = (float) Math.sqrt(
                                    (x - slamTargetX) * (x - slamTargetX) +
                                    (y - slamTargetY) * (y - slamTargetY));

                            // 到达目标位置，落地砸击
                            if (distToTarget <= size * 0.5f) {
                                isSlamLeaping = false;
                                // 落地伤害: 砸击范围内判定
                                if (distanceToPlayer < slamLandRange) {
                                    attackJustFired = true;
                                    lastAttackTime = currentTime;
                                }
                            }
                        } else {
                            // 砸击结束，切回追击
                            currentState = State.CHASING;
                            stateTimer = currentTime;
                        }
                        break;

                    default: // MELEE
                        if (isWindingUp) {
                            targetX = playerX;
                            targetY = playerY;

                            if (currentTime - windUpStartTime >= windUpDuration) {
                                isWindingUp = false;
                                if (distanceToPlayer < propertyExtra.attackRange) {
                                    attackJustFired = true;
                                    lastAttackTime = currentTime;
                                }
                            }
                        } else {
                            updateAttacking(deltaSeconds, playerX, playerY);

                            if (currentTime - lastAttackTime >= attackCooldown) {
                                if (distanceToPlayer < propertyExtra.attackRange) {
                                    isWindingUp = true;
                                    windUpStartTime = currentTime;
                                } else {
                                    currentState = State.CHASING;
                                    stateTimer = currentTime;
                                }
                            }
                        }
                        break;
                }

                if (!isCharging && !isSpinning && !isBlinkDashing && !isSlamLeaping && distanceToPlayer > propertyExtra.attackRange * 1.5f) {
                    currentState = State.CHASING;
                    isWindingUp = false;
                    isCharging = false;
                    isPounceRetreating = false;
                    comboCurrentHit = 0;
                    isSpinning = false;
                    isBlinkDashing = false;
                    isSlamLeaping = false;
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
     * Add available attack type for this enemy
     */
    protected void addAvailableAttackType(AttackType type) {
        availableAttackTypes.add(type);
    }

    /**
     * Randomly select attack type from available types
     */
    protected void selectCurrentAttackType() {
        if (availableAttackTypes.isEmpty()) {
            currentAttackType = AttackType.MELEE;
        } else {
            currentAttackType = availableAttackTypes.get((int) (Math.random() * availableAttackTypes.size()));
        }
        // Reset previous attack state
        isCharging = false;
        comboCurrentHit = 0;
        comboHitStartTime = System.currentTimeMillis();
    }

    /**
     * Get current attack type
     */
    public AttackType getCurrentAttackType() {
        return currentAttackType;
    }

    /**
     * Set current attack type
     */
    public void setCurrentAttackType(AttackType type) {
        this.currentAttackType = type;
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
     * Consume pending drain heal amount (consumed once by GameEngine)
     */
    public int consumeDrainHeal() {
        int heal = pendingDrainHeal;
        pendingDrainHeal = 0;
        return heal;
    }

    /**
     * Heal the enemy, capped at maxHealth
     */
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
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
            isWindingUp = false;
            isCharging = false;
            isPounceRetreating = false;
            comboCurrentHit = 0;
            isSpinning = false;
            isBlinkDashing = false;
            isSlamLeaping = false;

            // Set aggro timer - enemy will chase for 10 seconds after being hit
            isAggroed = true;
            aggroEndTime = currentTime + 10000; // 10 seconds

            LogUtil.d("Enemy", getName() + " was damaged! Aggroed for 10 seconds");

            // BOSS血量低于50%时触发召唤(每只BOSS只能召唤一次)
            if (enemyLevel == EnemyLevel.BOSS && !hasSummonedMinions && health <= maxHealth * 0.5) {
                pendingSummon = true;
            }
        }

        // Check if dead
        if (health <= 0) {
            health = 0;
            return true; // Died
        }

        return false; // Still alive
    }

    /**
     * Check if this BOSS has a pending summon request
     */
    public boolean isPendingSummon() {
        return pendingSummon;
    }

    /**
     * Mark summon as consumed (called by GameEngine after spawning minions)
     */
    public void markSummoned() {
        pendingSummon = false;
        hasSummonedMinions = true;
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
        isCharging = false;
        isPounceRetreating = false;
        comboCurrentHit = 0;
        isSpinning = false;
        isBlinkDashing = false;
        isSlamLeaping = false;
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

    /**
     * Set enemy level (used for summoning minions)
     */
    public void setEnemyLevel(EnemyLevel level) {
        this.enemyLevel = level;
    }

    /**
     * Set aggro state (used for summoning minions to actively chase player)
     * @param durationMs how long to stay aggroed in milliseconds
     */
    public void setAggro(long durationMs) {
        this.isAggroed = true;
        this.aggroEndTime = System.currentTimeMillis() + durationMs;
    }

    /**
     * Apply burn DOT effect
     * @param damagePerSecond burn damage per second
     * @param durationMs burn duration in milliseconds
     */
    public void applyBurn(int damagePerSecond, long durationMs) {
        this.burnDamagePerSecond = damagePerSecond;
        this.burnEndTime = System.currentTimeMillis() + durationMs;
        this.burnLastTickTime = System.currentTimeMillis();
    }

    /**
     * Check if enemy is burning
     */
    public boolean isBurning() {
        return burnDamagePerSecond > 0 && System.currentTimeMillis() < burnEndTime;
    }
}

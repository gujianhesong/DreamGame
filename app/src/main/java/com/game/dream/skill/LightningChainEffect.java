package com.game.dream.skill;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.dream.enemy.Enemy;
import com.game.dream.figure.Player;
import com.game.dream.utils.BattleUtil;
import com.game.dream.bean.AttackResult;
import com.game.dream.enums.SkillType;
import com.game.dream.figure.Character;

import com.game.dream.ui.DamageNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * 链式闪电效果 - 在敌人之间跳跃，伤害递减
 */
public class LightningChainEffect extends SkillEffect {

    public static class LightningHit {
        public Enemy enemy;
        public int damage;
        public boolean isCrit;

        public LightningHit(Enemy enemy, int damage, boolean isCrit) {
            this.enemy = enemy;
            this.damage = damage;
            this.isCrit = isCrit;
        }
    }

    private List<Enemy> targets;
    private List<LightningHit> hits;
    private int currentChainIndex;
    private long chainDelay; // 每次跳跃间隔(ms)
    private long lastJumpTime;
    private float decayRate; // 每次跳跃伤害衰减
    private float jumpRange; // 跳跃范围
    private int skillLevel;
    private boolean noDecayOnLevel10;

    // 视觉相关
    private long startTime;
    private long visualDuration;
    private float playerX, playerY; // 闪电起始点(玩家位置)
    private List<DamageNumber> damageNumbers; // 伤害数字列表(可选)

    // 锯齿闪电视觉数据(缓存，避免每帧重新生成)
    private List<float[][]> cachedBoltPoints;
    private long boltCacheTime;

    public LightningChainEffect(List<Enemy> allEnemies, Player player, int skillLevel, float x, float y) {
        super(Type.HEALING_ZONE, x, y, 0, 1000, 0, 0);
        this.targets = new ArrayList<>();
        this.hits = new ArrayList<>();
        this.currentChainIndex = 0;
        this.chainDelay = 80;
        this.skillLevel = skillLevel;
        this.playerX = x;
        this.playerY = y;
        this.cachedBoltPoints = new ArrayList<>();
        this.boltCacheTime = 0;
        this.damageNumbers = null;

        // 根据等级配置参数
        switch (skillLevel) {
            case 1: case 2:
                this.noDecayOnLevel10 = false;
                this.decayRate = 0.95f;
                this.jumpRange = 200f;
                break;
            case 3: case 4:
                this.noDecayOnLevel10 = false;
                this.decayRate = 0.90f;
                this.jumpRange = 220f;
                break;
            case 5: case 6:
                this.noDecayOnLevel10 = false;
                this.decayRate = 0.85f;
                this.jumpRange = 240f;
                break;
            case 7: case 8:
                this.noDecayOnLevel10 = false;
                this.decayRate = 0.80f;
                this.jumpRange = 260f;
                break;
            case 9:
                this.noDecayOnLevel10 = false;
                this.decayRate = 0.80f;
                this.jumpRange = 280f;
                break;
            case 10:
                this.noDecayOnLevel10 = true;
                this.decayRate = 1.0f;
                this.jumpRange = 300f;
                break;
            default:
                this.noDecayOnLevel10 = false;
                this.decayRate = Math.max(0.75f, 0.95f - (skillLevel - 1) * 0.02f);
                this.jumpRange = 200f + (skillLevel - 1) * 10f;
                break;
        }

        // 计算最大跳跃次数
        int maxJumps;
        if (skillLevel <= 2) maxJumps = 2;
        else if (skillLevel <= 4) maxJumps = 3;
        else if (skillLevel <= 6) maxJumps = 4;
        else if (skillLevel <= 8) maxJumps = 5;
        else maxJumps = 6;

        // 查找初始目标(离玩家最近的敌人)
        Enemy initialTarget = findClosestEnemy(allEnemies, x, y);
        if (initialTarget != null) {
            targets.add(initialTarget);

            // 查找后续链式目标
            Enemy currentTarget = initialTarget;
            for (int i = 1; i < maxJumps; i++) {
                Enemy nextTarget = findNextJumpTarget(allEnemies, currentTarget, targets);
                if (nextTarget != null) {
                    targets.add(nextTarget);
                    currentTarget = nextTarget;
                } else {
                    break;
                }
            }
        }

        this.startTime = System.currentTimeMillis();
        this.visualDuration = this.chainDelay * this.targets.size() + 400;
        this.lastJumpTime = startTime;
    }

    private Enemy findClosestEnemy(List<Enemy> enemies, float x, float y) {
        Enemy closest = null;
        float minDistSq = Float.MAX_VALUE;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float distSq = dx * dx + dy * dy;

            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = enemy;
            }
        }

        return closest;
    }

    private Enemy findNextJumpTarget(List<Enemy> allEnemies, Enemy currentTarget, List<Enemy> alreadyHit) {
        Enemy bestTarget = null;
        float minDistSq = Float.MAX_VALUE;

        for (Enemy enemy : allEnemies) {
            if (!enemy.isAlive() || enemy == currentTarget || alreadyHit.contains(enemy)) {
                continue;
            }

            float dx = enemy.getX() - currentTarget.getX();
            float dy = enemy.getY() - currentTarget.getY();
            float distSq = dx * dx + dy * dy;

            if (distSq <= jumpRange * jumpRange && distSq < minDistSq) {
                minDistSq = distSq;
                bestTarget = enemy;
            }
        }

        return bestTarget;
    }

    @Override
    public void update(List<Enemy> enemies) {
        if (!isActive()) return;

        long currentTime = System.currentTimeMillis();

        // 按间隔处理链式跳跃
        if (currentTime - lastJumpTime >= chainDelay && currentChainIndex < targets.size()) {
            Enemy target = targets.get(currentChainIndex);
            if (target != null && target.isAlive()) {
                // 使用BattleUtil计算基础法术伤害
                AttackResult result = BattleUtil.caculatePlayerCasterDamage(target, SkillType.MAIN_LIGHTNING);

                if (result != null && result.isHit) {
                    int finalDamage = result.damageValue;

                    // 应用链式衰减(第一跳不衰减)
                    if (currentChainIndex > 0 && !noDecayOnLevel10) {
                        float multiplier = 1.0f;
                        for (int i = 0; i < currentChainIndex; i++) {
                            multiplier *= decayRate;
                        }
                        finalDamage = Math.max(1, (int) (finalDamage * multiplier));
                    }

                    // Level 10: 首跳伤害+50%
                    if (skillLevel >= 10 && currentChainIndex == 0) {
                        finalDamage = (int) (finalDamage * 1.5f);
                    }

                    // 应用伤害
                    target.takeDamage(finalDamage);

                    // Level 9+: 30%概率麻痹(减速)
                    if (skillLevel >= 9 && Math.random() < 0.3) {
                        target.applyCC(Character.CrowdControlType.SLOW, 1500);
                    }

                    hits.add(new LightningHit(target, finalDamage, result.isCrit));

                    // 添加伤害数字
                    if (damageNumbers != null) {
                        damageNumbers.add(new DamageNumber(
                            target.getX(), target.getY() - 30,
                            finalDamage, result.isCrit
                        ));
                    }

                    // 缓存闪电视觉数据
                    cacheBoltVisual(currentChainIndex);
                }
            }

            currentChainIndex++;
            lastJumpTime = currentTime;
        }

        // 检查效果是否结束
        if (currentChainIndex >= targets.size() && currentTime - startTime > visualDuration) {
            setActive(false);
        }
    }

    /**
     * 缓存闪电视觉数据(锯齿折线点)
     */
    private void cacheBoltVisual(int chainIndex) {
        // 确保列表足够大
        while (cachedBoltPoints.size() <= chainIndex) {
            cachedBoltPoints.add(null);
        }

        float fromX, fromY, toX, toY;

        if (chainIndex == 0) {
            // 第一跳: 从玩家到第一个目标
            fromX = playerX;
            fromY = playerY;
        } else {
            // 后续跳跃: 从前一个目标到当前目标
            Enemy prev = targets.get(chainIndex - 1);
            fromX = prev.getX();
            fromY = prev.getY();
        }

        Enemy current = targets.get(chainIndex);
        toX = current.getX();
        toY = current.getY();

        // 生成锯齿折线
        cachedBoltPoints.set(chainIndex, generateZigzagPoints(fromX, fromY, toX, toY));
        boltCacheTime = System.currentTimeMillis();
    }

    /**
     * 生成锯齿闪电折线点
     */
    private float[][] generateZigzagPoints(float x1, float y1, float x2, float y2) {
        int segments = 8;
        float[][] points = new float[segments + 1][2];

        float deltaX = (x2 - x1) / segments;
        float deltaY = (y2 - y1) / segments;

        points[0][0] = x1;
        points[0][1] = y1;

        for (int i = 1; i < segments; i++) {
            float ratio = (float) i / segments;
            float perpendicularOffset = (float) (Math.random() - 0.5) * 30 * (1.0f - ratio * 0.3f);

            float normalX = -deltaY;
            float normalY = deltaX;
            float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY);
            if (normalLength > 0) {
                normalX /= normalLength;
                normalY /= normalLength;
            }

            points[i][0] = x1 + deltaX * i + normalX * perpendicularOffset;
            points[i][1] = y1 + deltaY * i + normalY * perpendicularOffset;
        }

        points[segments][0] = x2;
        points[segments][1] = y2;

        return points;
    }

    @Override
    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (!isActive()) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 绘制每个已完成的链式段
        for (int i = 0; i < currentChainIndex && i < cachedBoltPoints.size(); i++) {
            float[][] boltPoints = cachedBoltPoints.get(i);
            if (boltPoints == null) continue;

            // 计算淡出透明度
            long elapsed = System.currentTimeMillis() - startTime;
            long segStartTime = startTime + i * chainDelay;
            long segAge = System.currentTimeMillis() - segStartTime;
            float fadeRatio = Math.max(0, 1.0f - (float) segAge / 500f);
            if (fadeRatio <= 0) continue;

            drawLightningBolt(canvas, paint, boltPoints, offsetX, offsetY, fadeRatio, i == currentChainIndex - 1);
        }

        // 绘制命中点的闪光效果
        for (int i = 0; i < currentChainIndex && i < targets.size(); i++) {
            Enemy target = targets.get(i);
            if (target == null) continue;

            long segStartTime = startTime + i * chainDelay;
            long segAge = System.currentTimeMillis() - segStartTime;
            float flashRatio = Math.max(0, 1.0f - (float) segAge / 300f);
            if (flashRatio <= 0) continue;

            float hitX = target.getX() + offsetX;
            float hitY = target.getY() + offsetY;

            // 命中闪光
            paint.setColor(Color.argb((int) (flashRatio * 150), 255, 255, 200));
            canvas.drawCircle(hitX, hitY, 20 * flashRatio, paint);

            // 内圈白光
            paint.setColor(Color.argb((int) (flashRatio * 200), 255, 255, 255));
            canvas.drawCircle(hitX, hitY, 8 * flashRatio, paint);
        }
    }

    /**
     * 绘制一道锯齿闪电
     */
    private void drawLightningBolt(Canvas canvas, Paint paint, float[][] points,
                                    int offsetX, int offsetY, float fadeRatio, boolean isLatest) {
        int segments = points.length - 1;
        if (segments <= 0) return;

        // 外层辉光
        paint.setStrokeWidth(isLatest ? 8 : 5);
        paint.setColor(Color.argb((int) (fadeRatio * 60), 200, 200, 255));
        for (int i = 0; i < segments; i++) {
            canvas.drawLine(
                points[i][0] + offsetX, points[i][1] + offsetY,
                points[i + 1][0] + offsetX, points[i + 1][1] + offsetY,
                paint);
        }

        // 主闪电线条
        paint.setStrokeWidth(isLatest ? 4 : 2.5f);
        int mainAlpha = (int) (fadeRatio * (isLatest ? 255 : 200));
        paint.setColor(Color.argb(mainAlpha, 255, 255, 100));
        for (int i = 0; i < segments; i++) {
            canvas.drawLine(
                points[i][0] + offsetX, points[i][1] + offsetY,
                points[i + 1][0] + offsetX, points[i + 1][1] + offsetY,
                paint);
        }

        // 内层高亮
        paint.setStrokeWidth(isLatest ? 2 : 1.5f);
        paint.setColor(Color.argb((int) (fadeRatio * 220), 255, 255, 255));
        for (int i = 0; i < segments; i++) {
            canvas.drawLine(
                points[i][0] + offsetX, points[i][1] + offsetY,
                points[i + 1][0] + offsetX, points[i + 1][1] + offsetY,
                paint);
        }

        // 高等级时添加分叉闪电
        if (skillLevel >= 5 && Math.random() < 0.4) {
            int branchSeg = 1 + (int) (Math.random() * (segments - 2));
            float bx = points[branchSeg][0] + offsetX;
            float by = points[branchSeg][1] + offsetY;

            float mainDX = points[branchSeg + 1][0] - points[branchSeg][0];
            float mainDY = points[branchSeg + 1][1] - points[branchSeg][1];
            float normalX = -mainDY;
            float normalY = mainDX;
            float len = (float) Math.sqrt(normalX * normalX + normalY * normalY);
            if (len > 0) {
                normalX /= len;
                normalY /= len;
            }

            float branchLen = 15 + (float) (Math.random() * 15);
            float endX = bx + normalX * branchLen + (float) (Math.random() - 0.5) * 10;
            float endY = by + normalY * branchLen + (float) (Math.random() - 0.5) * 10;

            paint.setStrokeWidth(1.5f);
            paint.setColor(Color.argb((int) (fadeRatio * 150), 255, 255, 180));
            canvas.drawLine(bx, by, endX, endY, paint);
        }
    }

    public List<LightningHit> getHits() {
        return new ArrayList<>(hits);
    }

    public void setDamageNumbers(List<DamageNumber> damageNumbers) {
        this.damageNumbers = damageNumbers;
    }
}

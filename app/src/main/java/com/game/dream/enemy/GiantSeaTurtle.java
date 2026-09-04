package com.game.dream.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.dream.item.EquipCreator;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;

import java.util.List;

/**
 * 大海龟 - 东海湾的千年老龟，防御坦克型怪物
 * 特点: 血量极高、防御极高、速度极慢、缩壳减伤、反伤机制
 * 视觉: 巨大椭圆龟壳、青褐色带深绿斑纹、粗壮四肢、苍老头部、长须
 */
public class GiantSeaTurtle extends Enemy {

    // 缩壳防御机制
    private boolean isShellRetreat = false;
    private long shellRetreatEndTime = 0;
    private static final float SHELL_RETREAT_CHANCE = 0.35f; // 35% 概率触发
    private static final float SHELL_RETREAT_REDUCTION = 0.80f; // 减伤 80%
    private static final long SHELL_RETREAT_DURATION = 1500; // 持续 1.5 秒

    // 反伤机制
    private static final float REFLECT_DAMAGE_RATIO = 0.20f; // 反弹 20% 伤害

    // 龟壳冲撞参数（精英以上）
    private static final float SHELL_CHARGE_SPEED = 600f;

    public GiantSeaTurtle(float x, float y) {
        super(x, y, 95);
        attackCooldown = 3000; // 攻击间隔长（慢速怪）
        setAttackShape(AttackShape.ARC); // 啃咬 - 扇形

        EnemyPropertyExtra prop = new EnemyPropertyExtra();
        prop.detectionRange = 250; // 感知范围小（慢速怪）
        prop.attackRange = 120;
        prop.rewardExp = 500;
        prop.rewardMoney = 250;
        setPropertyExtra(prop);

        // 大海龟：血量极高、防御极高、速度极慢、攻击中等
        setProperty(800, 200, 300, 30, 120);

        // 等级分布
        if (Math.random() < 0.02) {
            // BOSS - 千年玄龟
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 60, attackDamage * 8, defense * 8, speed * 5, mana * 6);
        } else if (Math.random() < 0.07) {
            // 精英 - 百岁灵龟
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 12, attackDamage * 4, defense * 4, speed * 3, mana * 3);
        } else if (Math.random() < 0.30) {
            // 首领 - 老成巨龟
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.4f);
            setProperty(maxHealth * 4, attackDamage * 2, defense * 2, speed * 2, mana * 2);
        }

        // 所有大海龟使用连续爪击（龟爪连击）
        addAvailableAttackType(AttackType.COMBO);
        comboHitCount = (enemyLevel == EnemyLevel.BOSS) ? 4 : (enemyLevel == EnemyLevel.ELITE) ? 3 : 2;
        comboHitInterval = 280;

        // 首领以上可使用龟壳冲撞
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.POUNCE);
            pounceFixedSpeed = SHELL_CHARGE_SPEED;
        }

        // BOSS可使用环绕斩击（龟壳旋风）
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
            spinDuration = 2200;
            spinHitInterval = 350;
        }

        // BOSS可使用跳跃砸击（泰山压顶）
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.LEAP_SLAM);
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        // 缩壳状态额外减伤
        if (isShellRetreat) {
            damage = (int) (damage * (1 - SHELL_RETREAT_REDUCTION));
        } else if (Math.random() < SHELL_RETREAT_CHANCE) {
            // 触发缩壳
            isShellRetreat = true;
            shellRetreatEndTime = System.currentTimeMillis() + SHELL_RETREAT_DURATION;
            damage = (int) (damage * (1 - SHELL_RETREAT_REDUCTION));
        }

        // 反伤：反弹 20% 伤害给攻击者（通过伤害数字显示）
        int reflectDamage = (int) (damage * REFLECT_DAMAGE_RATIO);
        if (reflectDamage > 0) {
            // 反伤通过 GameEngine 处理，这里只记录
            pendingReflectDamage = reflectDamage;
        }

        return super.takeDamage(damage);
    }

    // 待处理的反伤
    private int pendingReflectDamage = 0;

    /**
     * 获取并清除待处理的反伤
     */
    public int consumeReflectDamage() {
        int dmg = pendingReflectDamage;
        pendingReflectDamage = 0;
        return dmg;
    }

    /**
     * 检查是否处于缩壳状态
     */
    public boolean isShellRetreat() {
        return isShellRetreat;
    }

    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive()) return;

        // 更新缩壳状态
        if (isShellRetreat && System.currentTimeMillis() > shellRetreatEndTime) {
            isShellRetreat = false;
        }
    }

    @Override
    public List<Item> getPossibleDropList() {
        possibleDrops.clear();

        if (enemyLevel == EnemyLevel.BOSS) {
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(EquipCreator.createEquip(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            addPossibleDrop(ItemCreator.createGain_hp_3());
            addPossibleDrop(ItemCreator.createGain_attack_3());
            addPossibleDrop(ItemCreator.createGain_defense_3());
            addPossibleDrop(ItemCreator.createGain_mana_3());
            addPossibleDrop(ItemCreator.createGain_speed_3());
            addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
        } else if (enemyLevel == EnemyLevel.ELITE) {
            addPossibleDrop(EquipCreator.createEquip(30, null));
            addPossibleDrop(EquipCreator.createEquip(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            addPossibleDrop(ItemCreator.createGain_hp_2());
            addPossibleDrop(ItemCreator.createGain_attack_2());
            addPossibleDrop(ItemCreator.createGain_defense_2());
            addPossibleDrop(ItemCreator.createGain_mana_2());
            addPossibleDrop(ItemCreator.createGain_speed_2());
            addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
        } else if (enemyLevel == EnemyLevel.LEADER) {
            addPossibleDrop(EquipCreator.createEquip(20, null));
            addPossibleDrop(EquipCreator.createEquip(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        } else {
            addPossibleDrop(EquipCreator.createEquip(20, null));
            addPossibleDrop(ItemCreator.createBuildEquipBook(20, null));
            addPossibleDrop(ItemCreator.createBuildEquipIron(20));
            addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
        }

        return possibleDrops;
    }

    @Override
    protected void performAttack() {
        // GiantSeaTurtle attack - handled by GameEngine
    }

    @Override
    public void onDraw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive()) return;

        paint.setAntiAlias(true);

        float scale = size / 30.0f;

        // 受击震动
        float vibX = 0, vibY = 0;
        if (lastHitFlashTime > 0) {
            long elapsed = System.currentTimeMillis() - lastHitFlashTime;
            if (elapsed < 300) {
                float intensity = (1f - elapsed / 300f) * 4 * scale;
                vibX = (float) (Math.sin(elapsed * 1.5) * intensity);
                vibY = (float) (Math.cos(elapsed * 2.1) * intensity * 0.5f);
            }
        }
        float cx = x + offsetX + vibX;
        float cy = y + offsetY + vibY;

        boolean facingRight = targetX > x;

        if (facingRight) {
            drawTurtle(canvas, paint, cx, cy, scale, true);
        } else {
            drawTurtle(canvas, paint, cx, cy, scale, false);
        }
    }

    private void drawTurtle(Canvas canvas, Paint paint, float cx, float cy, float scale, boolean facingRight) {
        long now = System.currentTimeMillis();
        float dir = facingRight ? 1f : -1f;

        // === 1. 缩壳状态视觉 ===
        if (isShellRetreat) {
            // 缩壳时只画龟壳，头脚缩回
            float shellPulse = (float) Math.sin(now / 200.0) * 0.1f + 0.9f;

            // 龟壳阴影
            paint.setColor(Color.argb(40, 0, 0, 0));
            canvas.drawOval(cx - 18 * scale, cy - 8 * scale + bobOffset,
                    cx + 18 * scale, cy + 12 * scale + bobOffset, paint);

            // 龟壳主体（更大更圆）
            paint.setColor(Color.rgb(80, 100, 70));
            canvas.drawOval(cx - 16 * scale * shellPulse, cy - 10 * scale * shellPulse + bobOffset,
                    cx + 16 * scale * shellPulse, cy + 10 * scale * shellPulse + bobOffset, paint);

            // 龟壳纹路（六角形）
            paint.setColor(Color.rgb(60, 80, 55));
            paint.setStrokeWidth(1.5f * scale);
            paint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < 6; i++) {
                float angle = i * (float) Math.PI / 3;
                float hx = cx + (float) Math.cos(angle) * 8 * scale;
                float hy = cy + (float) Math.sin(angle) * 5 * scale + bobOffset;
                canvas.drawCircle(hx, hy, 4 * scale, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);

            // 防御光环
            paint.setColor(Color.argb(30, 150, 200, 150));
            canvas.drawCircle(cx, cy + bobOffset, 20 * scale, paint);
            return;
        }

        // === 2. 四肢（粗壮象腿）===
        float legSwing1 = (float) Math.sin(animFrame * Math.PI / 2) * 2 * scale;
        float legSwing2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 2 * scale;

        paint.setColor(Color.rgb(90, 110, 80));
        // 左前腿
        canvas.drawOval(cx - 18 * scale + dir * 4 * scale, cy + 2 * scale + bobOffset + legSwing1,
                cx - 10 * scale + dir * 4 * scale, cy + 10 * scale + bobOffset + legSwing1, paint);
        // 右前腿
        canvas.drawOval(cx + 10 * scale + dir * 4 * scale, cy + 2 * scale + bobOffset + legSwing2,
                cx + 18 * scale + dir * 4 * scale, cy + 10 * scale + bobOffset + legSwing2, paint);
        // 左后腿
        canvas.drawOval(cx - 16 * scale - dir * 6 * scale, cy + 4 * scale + bobOffset + legSwing2,
                cx - 8 * scale - dir * 6 * scale, cy + 12 * scale + bobOffset + legSwing2, paint);
        // 右后腿
        canvas.drawOval(cx + 8 * scale - dir * 6 * scale, cy + 4 * scale + bobOffset + legSwing1,
                cx + 16 * scale - dir * 6 * scale, cy + 12 * scale + bobOffset + legSwing1, paint);

        // 爪子
        paint.setColor(Color.rgb(70, 85, 65));
        for (int i = 0; i < 4; i++) {
            float legX = cx + (i < 2 ? 1 : -1) * (i % 2 == 0 ? 14 : -14) * scale + (i < 2 ? dir * 4 : -dir * 6) * scale;
            float legY = cy + (8 + (i % 2) * 2) * scale + bobOffset;
            canvas.drawCircle(legX, legY, 2 * scale, paint);
        }

        // === 3. 龟壳主体 ===
        // 龟壳阴影
        paint.setColor(Color.argb(50, 0, 0, 0));
        canvas.drawOval(cx - 16 * scale, cy - 6 * scale + bobOffset + 2 * scale,
                cx + 16 * scale, cy + 8 * scale + bobOffset + 2 * scale, paint);

        // 龟壳底色（深绿）
        paint.setColor(Color.rgb(70, 90, 65));
        canvas.drawOval(cx - 15 * scale, cy - 8 * scale + bobOffset,
                cx + 15 * scale, cy + 8 * scale + bobOffset, paint);

        // 龟壳主色（青褐）
        paint.setColor(Color.rgb(95, 115, 85));
        canvas.drawOval(cx - 13 * scale, cy - 7 * scale + bobOffset,
                cx + 13 * scale, cy + 7 * scale + bobOffset, paint);

        // 龟壳纹路（六角形网格）
        paint.setColor(Color.rgb(75, 95, 70));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);

        // 中心大六角
        canvas.drawCircle(cx, cy + bobOffset, 5 * scale, paint);

        // 周围小六角
        for (int i = 0; i < 6; i++) {
            float angle = i * (float) Math.PI / 3 + now / 5000f;
            float hx = cx + (float) Math.cos(angle) * 9 * scale;
            float hy = cy + (float) Math.sin(angle) * 5 * scale + bobOffset;
            canvas.drawCircle(hx, hy, 3 * scale, paint);
        }

        // 龟壳高光
        paint.setColor(Color.argb(40, 180, 200, 170));
        canvas.drawOval(cx - 6 * scale, cy - 4 * scale + bobOffset,
                cx + 2 * scale, cy + 2 * scale + bobOffset, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 龟壳边缘装饰（岁月痕迹 - 小贝壳/海藻）
        paint.setColor(Color.rgb(140, 160, 130));
        canvas.drawCircle(cx - 12 * scale, cy - 2 * scale + bobOffset, 1.5f * scale, paint);
        canvas.drawCircle(cx + 10 * scale, cy + 3 * scale + bobOffset, 1.2f * scale, paint);

        // === 4. 头部 ===
        float headX = cx + dir * 16 * scale;
        float headY = cy - 2 * scale + bobOffset;
        float headBob = (float) Math.sin(now / 600.0) * 1 * scale;

        // 头部（椭圆，苍老）
        paint.setColor(Color.rgb(100, 120, 90));
        canvas.drawOval(headX - 6 * scale, headY - 5 * scale + headBob,
                headX + 8 * scale, headY + 5 * scale + headBob, paint);

        // 头部皱纹
        paint.setColor(Color.rgb(80, 100, 75));
        paint.setStrokeWidth(0.8f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(headX - 2 * scale, headY - 3 * scale + headBob,
                headX + 4 * scale, headY - 3 * scale + headBob, paint);
        canvas.drawLine(headX - 1 * scale, headY - 1 * scale + headBob,
                headX + 5 * scale, headY - 1 * scale + headBob, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 眼睛（半眯，苍老感）
        paint.setColor(Color.rgb(40, 50, 35));
        canvas.drawOval(headX + 3 * scale, headY - 2 * scale + headBob,
                headX + 5 * scale, headY + headBob, paint);

        // 眼睛高光
        paint.setColor(Color.WHITE);
        canvas.drawCircle(headX + 4 * scale, headY - 1 * scale + headBob, 0.5f * scale, paint);

        // 长须（飘动）
        paint.setColor(Color.rgb(120, 140, 110));
        paint.setStrokeWidth(1 * scale);
        paint.setStyle(Paint.Style.STROKE);
        float beardWave = (float) Math.sin(now / 400.0) * 2 * scale;
        Path beard = new Path();
        beard.moveTo(headX + 6 * scale, headY + 2 * scale + headBob);
        beard.quadTo(headX + 10 * scale, headY + 4 * scale + headBob + beardWave,
                headX + 12 * scale, headY + 6 * scale + headBob);
        canvas.drawPath(beard, paint);
        Path beard2 = new Path();
        beard2.moveTo(headX + 5 * scale, headY + 3 * scale + headBob);
        beard2.quadTo(headX + 9 * scale, headY + 5 * scale + headBob - beardWave,
                headX + 11 * scale, headY + 7 * scale + headBob);
        canvas.drawPath(beard2, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // === 5. 尾巴（小三角）===
        float tailX = cx - dir * 14 * scale;
        float tailY = cy + 2 * scale + bobOffset;
        paint.setColor(Color.rgb(90, 110, 80));
        Path tail = new Path();
        tail.moveTo(tailX, tailY);
        tail.lineTo(tailX - dir * 4 * scale, tailY - 2 * scale);
        tail.lineTo(tailX - dir * 4 * scale, tailY + 2 * scale);
        tail.close();
        canvas.drawPath(tail, paint);

        // === 6. 攻击特效 ===
        if (currentState == State.ATTACKING && isWindingUp) {
            float progress = getWindUpProgress();
            paint.setColor(Color.argb((int) (progress * 100), 150, 180, 140));
            paint.setStrokeWidth(2 * scale);
            paint.setStyle(Paint.Style.STROKE);
            float arcRadius = 12 * scale * progress;
            canvas.drawArc(cx - arcRadius, cy - arcRadius + bobOffset,
                    cx + arcRadius, cy + arcRadius + bobOffset,
                    facingRight ? -30 : 150, 60 * progress, false, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }
}

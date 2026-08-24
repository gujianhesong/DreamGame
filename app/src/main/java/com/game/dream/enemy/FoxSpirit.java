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
 * 狐狸精 - 美丽妖媚的狐妖，拥有多条尾巴，使用魅惑魔法攻击
 * 高魔法伤害、高速度、较低生命
 */
public class FoxSpirit extends Enemy {

    private long lastSparkleTime = 0; // 魔法粒子动画计时
    private int tailCount = 1;          // 尾巴数量（根据等级决定）

    // 狐媚法术相关
    private long lastFoxCharmTime = 0;          // 上次施放狐媚的时间
    private boolean pendingFoxCharm = false;     // 待处理狐媚花瓣生成(通知GameEngine)

    public FoxSpirit(float x, float y) {
        super(x, y, 80);
        attackCooldown = 1800;
        setAttackShape(AttackShape.ARC); // 狐尾扫击 - 扇形

        EnemyPropertyExtra enemyPropertyExtra = new EnemyPropertyExtra();
        enemyPropertyExtra.detectionRange = 320;
        enemyPropertyExtra.attackRange = 120;
        enemyPropertyExtra.rewardExp = 150;
        enemyPropertyExtra.rewardMoney = 180;
        setPropertyExtra(enemyPropertyExtra);

        // 狐狸精：生命较低，魔法伤害高，速度快
        setProperty(450, 150, 280, 130, 250);

        if (Math.random() < 0.02) {
            //BOSS - 九尾天狐
            enemyLevel = EnemyLevel.BOSS;
            size = size * 3;
            setProperty(maxHealth * 50, attackDamage * 8, defense * 8, speed * 8, mana * 4);
        } else if (Math.random() < 0.07) {
            //精英
            enemyLevel = EnemyLevel.ELITE;
            size = size * 2;
            setProperty(maxHealth * 10, attackDamage * 4, defense * 4, speed * 4, (int) (mana * 2.5));
        } else if (Math.random() < 0.30) {
            //首领
            enemyLevel = EnemyLevel.LEADER;
            size = (int) (size * 1.3f);
            setProperty(maxHealth * 3, attackDamage * 2, defense * 2, speed * 2, (int) (mana * 1.5));
        }

        // 尾巴数量根据等级决定
        if (enemyLevel == EnemyLevel.BOSS) {
            tailCount = 9;  // 九尾天狐
        } else if (enemyLevel == EnemyLevel.ELITE) {
            tailCount = 3;
        } else {
            tailCount = 1;  // 普通和首领只有1条尾巴
        }

        // 首领以上可使用魅惑之吻（吸血）
        if (enemyLevel == EnemyLevel.LEADER || enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.DRAIN_BITE);
        }
        // 精英以上可使用环绕斩击（狐尾旋舞）
        if (enemyLevel == EnemyLevel.ELITE || enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.SPIN_ATTACK);
        }
        // BOSS可使用猛扑（狐跃）
        if (enemyLevel == EnemyLevel.BOSS) {
            addAvailableAttackType(AttackType.POUNCE);
        }
    }

    /**
     * 狐媚法术: 向四周发射8道花瓣，被击中后受到法术伤害，30%概率眩晕1秒
     * 冷却时间: 普通5-8秒，精英4-6秒，BOSS3-5秒
     */
    @Override
    public void update(long deltaTime, float playerX, float playerY, int[][] map, int mapWidth, int mapHeight) {
        super.update(deltaTime, playerX, playerY, map, mapWidth, mapHeight);

        if (!isAlive() || isStunned() || isFrozen()) return;

        // 狐媚法术冷却计时
        long now = System.currentTimeMillis();
        long cooldown;
        if (enemyLevel == EnemyLevel.BOSS) {
            cooldown = 3000 + (long)(Math.random() * 2000); // 3-5秒
        } else if (enemyLevel == EnemyLevel.ELITE) {
            cooldown = 4000 + (long)(Math.random() * 2000); // 4-6秒
        } else {
            cooldown = 5000 + (long)(Math.random() * 3000); // 5-8秒
        }

        if (now - lastFoxCharmTime > cooldown) {
            // 检测玩家是否在感知范围内
            float dx = playerX - x;
            float dy = playerY - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < propertyExtra.detectionRange * 1.5f) {
                pendingFoxCharm = true;
                lastFoxCharmTime = now;
            }
        }
    }

    /**
     * 检查是否有待处理的狐媚花瓣生成请求
     */
    public boolean isPendingFoxCharm() {
        return pendingFoxCharm;
    }

    /**
     * 消费狐媚花瓣请求(由GameEngine调用)
     */
    public void consumeFoxCharm() {
        pendingFoxCharm = false;
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
        // FoxSpirit attack - handled by GameEngine
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

        // === 1. 魔法光环（最底层，淡粉色脉动）===
        long now = System.currentTimeMillis();
        float auraPulse = 1.0f + 0.12f * (float) Math.sin(now / 400.0);
        paint.setColor(Color.argb(35, 255, 150, 200));
        canvas.drawCircle(cx, cy - 2 * scale, size * 0.7f * auraPulse, paint);
        paint.setColor(Color.argb(20, 255, 200, 230));
        canvas.drawCircle(cx, cy - 2 * scale, size * 0.85f * auraPulse, paint);

        // === 2. 尾巴（在身体后面绘制）===
        drawTails(canvas, paint, cx, cy, scale, facingRight);

        // === 3. 身体（白色粉红汉服）===
        drawBody(canvas, paint, cx, cy, scale, facingRight);

        // === 4. 魔法粒子（飘散的粉色光点）===
        drawSparkles(canvas, paint, cx, cy, scale);
    }

    // ==================== 尾巴绘制 ====================

    private void drawTails(Canvas canvas, Paint paint, float cx, float cy, float scale, boolean facingRight) {
        long now = System.currentTimeMillis();

        // 尾巴从腰后位置向背后扇形展开
        float tailBaseX = cx;
        float tailBaseY = cy + 4 * scale + bobOffset;

        // 尾巴朝后的方向偏移
        float dirOffset = facingRight ? -1f : 1f;

        // 根据 tailCount 绘制对应数量的尾巴，扇形展开
        for (int i = 0; i < tailCount; i++) {
            // 每条尾巴的扇形角度（-1 到 1 之间均匀分布）
            float spread;
            if (tailCount == 1) {
                spread = 0f;
            } else {
                spread = (float) i / (tailCount - 1) * 2f - 1f; // -1 到 1
            }

            // 每条尾巴独立的摆动动画
            float swing = (float) Math.sin(now / 600.0 + i * 1.2) * 6 * scale;
            float swing2 = (float) Math.sin(now / 500.0 + i * 0.8) * 4 * scale;

            // 尾巴尖端位置（扇形展开 + 摆动）
            // 中间尾巴最长，两侧渐短（狐尾扇形经典造型）
            float tipSpreadX = spread * 14 * scale; // 水平扇形展开
            float tipLengthBonus = (1f - Math.abs(spread)) * 6 * scale; // 中间尾巴额外加长
            float tipX = tailBaseX + dirOffset * (18 + tipSpreadX) * scale + swing;
            float tipY = tailBaseY + (-22 - tipLengthBonus) * scale + swing2;

            // 弯曲方向随扇形位置变化
            float curl = dirOffset * (8 + spread * 5) * scale;

            // 颜色微调：中间尾巴更亮，两侧略暗
            int r = 235 + (int)(Math.abs(spread) * 10);
            int g = 155 + (int)((1 - Math.abs(spread)) * 25);
            int b = 65 + (int)((1 - Math.abs(spread)) * 25);

            drawSingleTail(canvas, paint, tailBaseX, tailBaseY, scale,
                    tipX, tipY, curl, Color.rgb(r, g, b));
        }
    }

    private void drawSingleTail(Canvas canvas, Paint paint, float baseX, float baseY,
                                float scale, float tipX, float tipY, float curl, int furColor) {
        // 尾巴主体（粗曲线）
        paint.setColor(furColor);
        paint.setStrokeWidth(5 * scale);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        Path tailPath = new Path();
        tailPath.moveTo(baseX, baseY);
        float ctrlX = (baseX + tipX) / 2 + curl;
        float ctrlY = (baseY + tipY) / 2 - 3 * scale;
        tailPath.quadTo(ctrlX, ctrlY, tipX, tipY);
        canvas.drawPath(tailPath, paint);

        // 尾巴尖端（白色）
        paint.setColor(Color.rgb(255, 248, 240));
        paint.setStrokeWidth(3.5f * scale);
        // 尖端在最后 30% 的曲线上
        float midX = ctrlX + (tipX - ctrlX) * 0.6f;
        float midY = ctrlY + (tipY - ctrlY) * 0.6f;
        Path tipPath = new Path();
        tipPath.moveTo(midX, midY);
        tipPath.lineTo(tipX, tipY);
        canvas.drawPath(tipPath, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    // ==================== 身体绘制 ====================

    private void drawBody(Canvas canvas, Paint paint, float cx, float cy, float scale, boolean facingRight) {
        // --- 腿部 ---
        float legSwing1 = (float) Math.sin(animFrame * Math.PI / 2) * 2 * scale;
        float legSwing2 = (float) Math.sin((animFrame + 2) * Math.PI / 2) * 2 * scale;

        paint.setColor(Color.rgb(255, 228, 196));
        canvas.drawRect(cx - 4 * scale, cy + 10 * scale + bobOffset + legSwing1,
                cx - 1 * scale, cy + 17 * scale + bobOffset, paint);
        canvas.drawRect(cx + 1 * scale, cy + 10 * scale + bobOffset + legSwing2,
                cx + 4 * scale, cy + 17 * scale + bobOffset, paint);

        // 绣花鞋（粉色）
        paint.setColor(Color.rgb(219, 112, 147));
        canvas.drawRoundRect(cx - 5 * scale, cy + 15 * scale + bobOffset + legSwing1,
                cx, cy + 18 * scale + bobOffset + legSwing1, 2 * scale, 2 * scale, paint);
        canvas.drawRoundRect(cx, cy + 15 * scale + bobOffset + legSwing2,
                cx + 5 * scale, cy + 18 * scale + bobOffset + legSwing2, 2 * scale, 2 * scale, paint);

        // --- 长裙主体（白色粉红渐变感）---
        paint.setColor(Color.rgb(255, 240, 245));
        Path dress = new Path();
        dress.moveTo(cx - 10 * scale, cy - 6 * scale + bobOffset);
        dress.lineTo(cx + 10 * scale, cy - 6 * scale + bobOffset);
        dress.lineTo(cx + 14 * scale, cy + 12 * scale + bobOffset);
        dress.quadTo(cx, cy + 14 * scale + bobOffset, cx - 14 * scale, cy + 12 * scale + bobOffset);
        dress.close();
        canvas.drawPath(dress, paint);

        // 裙摆粉色花纹边缘
        paint.setColor(Color.argb(60, 255, 182, 193));
        paint.setStrokeWidth(1.5f * scale);
        paint.setStyle(Paint.Style.STROKE);
        Path hemPath = new Path();
        hemPath.moveTo(cx - 13 * scale, cy + 11 * scale + bobOffset);
        hemPath.quadTo(cx, cy + 13.5f * scale + bobOffset, cx + 13 * scale, cy + 11 * scale + bobOffset);
        canvas.drawPath(hemPath, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // 裙子阴影（增加立体感）
        paint.setColor(Color.argb(20, 200, 100, 150));
        Path dressShadow = new Path();
        dressShadow.moveTo(cx - 10 * scale, cy - 6 * scale + bobOffset);
        dressShadow.lineTo(cx, cy - 6 * scale + bobOffset);
        dressShadow.lineTo(cx, cy + 13 * scale + bobOffset);
        dressShadow.lineTo(cx - 14 * scale, cy + 12 * scale + bobOffset);
        dressShadow.close();
        canvas.drawPath(dressShadow, paint);

        // --- 宽袖 ---
        // 左袖
        paint.setColor(Color.rgb(255, 235, 240));
        Path leftSleeve = new Path();
        leftSleeve.moveTo(cx - 10 * scale, cy - 4 * scale + bobOffset);
        leftSleeve.quadTo(cx - 18 * scale, cy + bobOffset, cx - 16 * scale, cy + 6 * scale + bobOffset);
        leftSleeve.lineTo(cx - 11 * scale, cy + 4 * scale + bobOffset);
        leftSleeve.close();
        canvas.drawPath(leftSleeve, paint);

        // 右袖
        Path rightSleeve = new Path();
        rightSleeve.moveTo(cx + 10 * scale, cy - 4 * scale + bobOffset);
        rightSleeve.quadTo(cx + 18 * scale, cy + bobOffset, cx + 16 * scale, cy + 6 * scale + bobOffset);
        rightSleeve.lineTo(cx + 11 * scale, cy + 4 * scale + bobOffset);
        rightSleeve.close();
        canvas.drawPath(rightSleeve, paint);

        // 袖口粉色描边
        paint.setColor(Color.argb(80, 255, 150, 180));
        paint.setStrokeWidth(1.2f * scale);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 16 * scale, cy + 5.5f * scale + bobOffset,
                cx - 11 * scale, cy + 3.5f * scale + bobOffset, paint);
        canvas.drawLine(cx + 16 * scale, cy + 5.5f * scale + bobOffset,
                cx + 11 * scale, cy + 3.5f * scale + bobOffset, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // --- 腰带（粉红色）---
        paint.setColor(Color.rgb(255, 105, 130));
        canvas.drawRect(cx - 11 * scale, cy + 1 * scale + bobOffset,
                cx + 11 * scale, cy + 3.5f * scale + bobOffset, paint);

        // 腰带蝴蝶结
        paint.setColor(Color.rgb(255, 80, 110));
        Path bow = new Path();
        bow.moveTo(cx, cy + 2 * scale + bobOffset);
        bow.lineTo(cx - 4 * scale, cy + bobOffset);
        bow.lineTo(cx, cy + 2.5f * scale + bobOffset);
        bow.lineTo(cx + 4 * scale, cy + bobOffset);
        bow.close();
        canvas.drawPath(bow, paint);

        // --- 衣领（V形粉色）---
        paint.setColor(Color.rgb(255, 200, 210));
        Path collar = new Path();
        collar.moveTo(cx - 6 * scale, cy - 6 * scale + bobOffset);
        collar.lineTo(cx, cy - 2 * scale + bobOffset);
        collar.lineTo(cx + 6 * scale, cy - 6 * scale + bobOffset);
        collar.lineTo(cx + 4 * scale, cy - 6 * scale + bobOffset);
        collar.lineTo(cx, cy - 3.5f * scale + bobOffset);
        collar.lineTo(cx - 4 * scale, cy - 6 * scale + bobOffset);
        collar.close();
        canvas.drawPath(collar, paint);

        // --- 手臂（纤细白皙）---
        paint.setColor(Color.rgb(255, 228, 196));
        paint.setStrokeWidth(2.5f * scale);
        // 左手
        canvas.drawLine(cx - 12 * scale, cy - 2 * scale + bobOffset,
                cx - 14 * scale, cy + 5 * scale + bobOffset, paint);
        // 右手
        canvas.drawLine(cx + 12 * scale, cy - 2 * scale + bobOffset,
                cx + 14 * scale, cy + 5 * scale + bobOffset, paint);

        // --- 头部 ---
        // 脸部（白皙圆润，下巴略尖）
        paint.setColor(Color.rgb(255, 238, 215));
        Path face = new Path();
        face.moveTo(cx - 6.5f * scale, cy - 14 * scale + bobOffset);
        face.quadTo(cx - 7.5f * scale, cy - 10 * scale + bobOffset, cx - 4 * scale, cy - 6.5f * scale + bobOffset);
        face.quadTo(cx, cy - 5.5f * scale + bobOffset, cx + 4 * scale, cy - 6.5f * scale + bobOffset);
        face.quadTo(cx + 7.5f * scale, cy - 10 * scale + bobOffset, cx + 6.5f * scale, cy - 14 * scale + bobOffset);
        face.quadTo(cx + 5 * scale, cy - 19 * scale + bobOffset, cx, cy - 19.5f * scale + bobOffset);
        face.quadTo(cx - 5 * scale, cy - 19 * scale + bobOffset, cx - 6.5f * scale, cy - 14 * scale + bobOffset);
        face.close();
        canvas.drawPath(face, paint);

        // 腮红（柔和的椭圆，更自然）
        paint.setColor(Color.argb(35, 255, 120, 150));
        canvas.drawOval(cx - 6 * scale, cy - 10.5f * scale + bobOffset,
                cx - 3 * scale, cy - 8.5f * scale + bobOffset, paint);
        canvas.drawOval(cx + 3 * scale, cy - 10.5f * scale + bobOffset,
                cx + 6 * scale, cy - 8.5f * scale + bobOffset, paint);

        // --- 头发（乌黑亮丽长发）---
        paint.setColor(Color.rgb(25, 20, 35));
        // 头顶头发
        Path hairTop = new Path();
        hairTop.moveTo(cx - 8 * scale, cy - 12 * scale + bobOffset);
        hairTop.quadTo(cx - 6 * scale, cy - 21 * scale + bobOffset, cx, cy - 20 * scale + bobOffset);
        hairTop.quadTo(cx + 6 * scale, cy - 21 * scale + bobOffset, cx + 8 * scale, cy - 12 * scale + bobOffset);
        hairTop.quadTo(cx + 7 * scale, cy - 14 * scale + bobOffset, cx + 6 * scale, cy - 12 * scale + bobOffset);
        hairTop.lineTo(cx - 6 * scale, cy - 12 * scale + bobOffset);
        hairTop.quadTo(cx - 7 * scale, cy - 14 * scale + bobOffset, cx - 8 * scale, cy - 12 * scale + bobOffset);
        hairTop.close();
        canvas.drawPath(hairTop, paint);

        // 左侧长发
        Path hairLeft = new Path();
        hairLeft.moveTo(cx - 7 * scale, cy - 12 * scale + bobOffset);
        hairLeft.quadTo(cx - 9 * scale, cy - 2 * scale + bobOffset, cx - 8 * scale, cy + 8 * scale + bobOffset);
        hairLeft.lineTo(cx - 6 * scale, cy + 6 * scale + bobOffset);
        hairLeft.quadTo(cx - 7 * scale, cy - 2 * scale + bobOffset, cx - 5.5f * scale, cy - 12 * scale + bobOffset);
        hairLeft.close();
        canvas.drawPath(hairLeft, paint);

        // 右侧长发
        Path hairRight = new Path();
        hairRight.moveTo(cx + 7 * scale, cy - 12 * scale + bobOffset);
        hairRight.quadTo(cx + 9 * scale, cy - 2 * scale + bobOffset, cx + 8 * scale, cy + 8 * scale + bobOffset);
        hairRight.lineTo(cx + 6 * scale, cy + 6 * scale + bobOffset);
        hairRight.quadTo(cx + 7 * scale, cy - 2 * scale + bobOffset, cx + 5.5f * scale, cy - 12 * scale + bobOffset);
        hairRight.close();
        canvas.drawPath(hairRight, paint);

        // 刘海
        paint.setColor(Color.rgb(30, 25, 40));
        Path bangs = new Path();
        bangs.moveTo(cx - 6 * scale, cy - 14 * scale + bobOffset);
        bangs.quadTo(cx - 3 * scale, cy - 11 * scale + bobOffset, cx, cy - 12.5f * scale + bobOffset);
        bangs.quadTo(cx + 3 * scale, cy - 11 * scale + bobOffset, cx + 6 * scale, cy - 14 * scale + bobOffset);
        bangs.quadTo(cx + 4 * scale, cy - 16 * scale + bobOffset, cx, cy - 15.5f * scale + bobOffset);
        bangs.quadTo(cx - 4 * scale, cy - 16 * scale + bobOffset, cx - 6 * scale, cy - 14 * scale + bobOffset);
        bangs.close();
        canvas.drawPath(bangs, paint);

        // --- 狐耳（尖尖三角形，橙色外面+粉色内面）---
        // 左耳
        paint.setColor(Color.rgb(235, 155, 70));
        Path leftEar = new Path();
        leftEar.moveTo(cx - 5 * scale, cy - 17 * scale + bobOffset);
        leftEar.lineTo(cx - 8 * scale, cy - 27 * scale + bobOffset);
        leftEar.lineTo(cx - 1 * scale, cy - 18 * scale + bobOffset);
        leftEar.close();
        canvas.drawPath(leftEar, paint);

        // 左耳内侧（粉色）
        paint.setColor(Color.rgb(255, 182, 193));
        Path leftEarInner = new Path();
        leftEarInner.moveTo(cx - 5 * scale, cy - 18 * scale + bobOffset);
        leftEarInner.lineTo(cx - 7 * scale, cy - 25 * scale + bobOffset);
        leftEarInner.lineTo(cx - 2.5f * scale, cy - 18.5f * scale + bobOffset);
        leftEarInner.close();
        canvas.drawPath(leftEarInner, paint);

        // 右耳
        paint.setColor(Color.rgb(235, 155, 70));
        Path rightEar = new Path();
        rightEar.moveTo(cx + 5 * scale, cy - 17 * scale + bobOffset);
        rightEar.lineTo(cx + 8 * scale, cy - 27 * scale + bobOffset);
        rightEar.lineTo(cx + 1 * scale, cy - 18 * scale + bobOffset);
        rightEar.close();
        canvas.drawPath(rightEar, paint);

        // 右耳内侧
        paint.setColor(Color.rgb(255, 182, 193));
        Path rightEarInner = new Path();
        rightEarInner.moveTo(cx + 5 * scale, cy - 18 * scale + bobOffset);
        rightEarInner.lineTo(cx + 7 * scale, cy - 25 * scale + bobOffset);
        rightEarInner.lineTo(cx + 2.5f * scale, cy - 18.5f * scale + bobOffset);
        rightEarInner.close();
        canvas.drawPath(rightEarInner, paint);

        // --- 眼睛（金色狐眼，妩媚杏仁形）---
        boolean isAggressive = currentState == State.CHASING || currentState == State.ATTACKING;

        // 眼影底色（柔和渐变感）
        paint.setColor(Color.argb(30, 255, 100, 150));
        canvas.drawOval(cx - 6 * scale, cy - 14.5f * scale + bobOffset,
                cx - 1.5f * scale, cy - 11 * scale + bobOffset, paint);
        canvas.drawOval(cx + 1.5f * scale, cy - 14.5f * scale + bobOffset,
                cx + 6 * scale, cy - 11 * scale + bobOffset, paint);

        // 眼白（杏仁形）
        paint.setColor(Color.rgb(255, 252, 248));
        Path leftEyeWhite = new Path();
        leftEyeWhite.moveTo(cx - 5.5f * scale, cy - 12.8f * scale + bobOffset);
        leftEyeWhite.quadTo(cx - 3.5f * scale, cy - 14.5f * scale + bobOffset, cx - 1.5f * scale, cy - 12.8f * scale + bobOffset);
        leftEyeWhite.quadTo(cx - 3.5f * scale, cy - 11.2f * scale + bobOffset, cx - 5.5f * scale, cy - 12.8f * scale + bobOffset);
        leftEyeWhite.close();
        canvas.drawPath(leftEyeWhite, paint);

        Path rightEyeWhite = new Path();
        rightEyeWhite.moveTo(cx + 1.5f * scale, cy - 12.8f * scale + bobOffset);
        rightEyeWhite.quadTo(cx + 3.5f * scale, cy - 14.5f * scale + bobOffset, cx + 5.5f * scale, cy - 12.8f * scale + bobOffset);
        rightEyeWhite.quadTo(cx + 3.5f * scale, cy - 11.2f * scale + bobOffset, cx + 1.5f * scale, cy - 12.8f * scale + bobOffset);
        rightEyeWhite.close();
        canvas.drawPath(rightEyeWhite, paint);

        // 金色虹膜（稍大，更有神）
        if (isAggressive) {
            paint.setColor(Color.rgb(255, 70, 110)); // 攻击时妖艳红粉
        } else {
            paint.setColor(Color.rgb(255, 195, 40)); // 平时琥珀金
        }
        canvas.drawCircle(cx - 3.5f * scale, cy - 12.8f * scale + bobOffset, 1.6f * scale, paint);
        canvas.drawCircle(cx + 3.5f * scale, cy - 12.8f * scale + bobOffset, 1.6f * scale, paint);

        // 虹膜外圈（深色描边增加层次）
        paint.setColor(Color.argb(80, 180, 100, 20));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.4f * scale);
        canvas.drawCircle(cx - 3.5f * scale, cy - 12.8f * scale + bobOffset, 1.6f * scale, paint);
        canvas.drawCircle(cx + 3.5f * scale, cy - 12.8f * scale + bobOffset, 1.6f * scale, paint);
        paint.setStyle(Paint.Style.FILL);

        // 瞳孔（竖椭圆，猫科感）
        paint.setColor(Color.rgb(20, 10, 5));
        canvas.drawOval(cx - 3.9f * scale, cy - 13.6f * scale + bobOffset,
                cx - 3.1f * scale, cy - 12 * scale + bobOffset, paint);
        canvas.drawOval(cx + 3.1f * scale, cy - 13.6f * scale + bobOffset,
                cx + 3.9f * scale, cy - 12 * scale + bobOffset, paint);

        // 眼睛高光（双高光点，更灵动）
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx - 3 * scale, cy - 13.3f * scale + bobOffset, 0.6f * scale, paint);
        canvas.drawCircle(cx - 4 * scale, cy - 12.4f * scale + bobOffset, 0.3f * scale, paint);
        canvas.drawCircle(cx + 4 * scale, cy - 13.3f * scale + bobOffset, 0.6f * scale, paint);
        canvas.drawCircle(cx + 3 * scale, cy - 12.4f * scale + bobOffset, 0.3f * scale, paint);

        // 上眼线（妩媚上挑的弧线）
        paint.setColor(Color.rgb(35, 20, 30));
        paint.setStrokeWidth(1.3f * scale);
        Path leftUpperLid = new Path();
        leftUpperLid.moveTo(cx - 5.8f * scale, cy - 12.5f * scale + bobOffset);
        leftUpperLid.quadTo(cx - 3.5f * scale, cy - 14.8f * scale + bobOffset, cx - 1.2f * scale, cy - 13 * scale + bobOffset);
        canvas.drawPath(leftUpperLid, paint);

        Path rightUpperLid = new Path();
        rightUpperLid.moveTo(cx + 1.2f * scale, cy - 13 * scale + bobOffset);
        rightUpperLid.quadTo(cx + 3.5f * scale, cy - 14.8f * scale + bobOffset, cx + 5.8f * scale, cy - 12.5f * scale + bobOffset);
        canvas.drawPath(rightUpperLid, paint);

        // 下眼线（柔和弧线）
        paint.setStrokeWidth(0.7f * scale);
        paint.setColor(Color.argb(120, 60, 30, 40));
        Path leftLowerLid = new Path();
        leftLowerLid.moveTo(cx - 5.3f * scale, cy - 12.8f * scale + bobOffset);
        leftLowerLid.quadTo(cx - 3.5f * scale, cy - 11 * scale + bobOffset, cx - 1.5f * scale, cy - 12.5f * scale + bobOffset);
        canvas.drawPath(leftLowerLid, paint);

        Path rightLowerLid = new Path();
        rightLowerLid.moveTo(cx + 1.5f * scale, cy - 12.5f * scale + bobOffset);
        rightLowerLid.quadTo(cx + 3.5f * scale, cy - 11 * scale + bobOffset, cx + 5.3f * scale, cy - 12.8f * scale + bobOffset);
        canvas.drawPath(rightLowerLid, paint);

        // 睫毛（眼尾微微上翘）
        paint.setStrokeWidth(0.8f * scale);
        paint.setColor(Color.rgb(30, 15, 25));
        // 左眼睫毛
        canvas.drawLine(cx - 5.5f * scale, cy - 13.2f * scale + bobOffset,
                cx - 6.2f * scale, cy - 14 * scale + bobOffset, paint);
        canvas.drawLine(cx - 5 * scale, cy - 13.8f * scale + bobOffset,
                cx - 5.5f * scale, cy - 14.5f * scale + bobOffset, paint);
        // 右眼睫毛
        canvas.drawLine(cx + 5.5f * scale, cy - 13.2f * scale + bobOffset,
                cx + 6.2f * scale, cy - 14 * scale + bobOffset, paint);
        canvas.drawLine(cx + 5 * scale, cy - 13.8f * scale + bobOffset,
                cx + 5.5f * scale, cy - 14.5f * scale + bobOffset, paint);

        // 眉毛（细长柳叶眉，更柔美）
        paint.setStrokeWidth(0.9f * scale);
        paint.setColor(Color.rgb(55, 35, 45));
        Path leftBrow = new Path();
        leftBrow.moveTo(cx - 5.5f * scale, cy - 15.5f * scale + bobOffset);
        leftBrow.quadTo(cx - 3.5f * scale, cy - 16.2f * scale + bobOffset, cx - 1.5f * scale, cy - 15.2f * scale + bobOffset);
        canvas.drawPath(leftBrow, paint);

        Path rightBrow = new Path();
        rightBrow.moveTo(cx + 1.5f * scale, cy - 15.2f * scale + bobOffset);
        rightBrow.quadTo(cx + 3.5f * scale, cy - 16.2f * scale + bobOffset, cx + 5.5f * scale, cy - 15.5f * scale + bobOffset);
        canvas.drawPath(rightBrow, paint);

        // 鼻子（小巧精致）
        paint.setColor(Color.argb(80, 200, 150, 140));
        canvas.drawCircle(cx, cy - 9.5f * scale + bobOffset, 0.5f * scale, paint);

        // 嘴（精致唇形，攻击时微张）
        if (isAggressive) {
            // 攻击时：微张的唇，露出一点牙齿
            paint.setColor(Color.rgb(230, 90, 110));
            Path mouthOpen = new Path();
            mouthOpen.moveTo(cx - 2.2f * scale, cy - 8.2f * scale + bobOffset);
            mouthOpen.quadTo(cx - 1 * scale, cy - 8.8f * scale + bobOffset, cx, cy - 8.5f * scale + bobOffset);
            mouthOpen.quadTo(cx + 1 * scale, cy - 8.8f * scale + bobOffset, cx + 2.2f * scale, cy - 8.2f * scale + bobOffset);
            mouthOpen.quadTo(cx + 1 * scale, cy - 7.2f * scale + bobOffset, cx, cy - 7 * scale + bobOffset);
            mouthOpen.quadTo(cx - 1 * scale, cy - 7.2f * scale + bobOffset, cx - 2.2f * scale, cy - 8.2f * scale + bobOffset);
            mouthOpen.close();
            canvas.drawPath(mouthOpen, paint);
            // 牙齿
            paint.setColor(Color.rgb(255, 250, 250));
            canvas.drawRect(cx - 1 * scale, cy - 8.2f * scale + bobOffset,
                    cx + 1 * scale, cy - 7.8f * scale + bobOffset, paint);
        } else {
            // 平时：微笑的樱桃小嘴，上唇有唇珠
            paint.setColor(Color.rgb(225, 105, 120));
            // 上唇
            Path upperLip = new Path();
            upperLip.moveTo(cx - 2 * scale, cy - 8.2f * scale + bobOffset);
            upperLip.quadTo(cx - 1 * scale, cy - 8.8f * scale + bobOffset, cx - 0.3f * scale, cy - 8.5f * scale + bobOffset);
            upperLip.quadTo(cx, cy - 8.9f * scale + bobOffset, cx + 0.3f * scale, cy - 8.5f * scale + bobOffset);
            upperLip.quadTo(cx + 1 * scale, cy - 8.8f * scale + bobOffset, cx + 2 * scale, cy - 8.2f * scale + bobOffset);
            upperLip.close();
            canvas.drawPath(upperLip, paint);
            // 下唇
            paint.setColor(Color.rgb(235, 115, 130));
            Path lowerLip = new Path();
            lowerLip.moveTo(cx - 2 * scale, cy - 8.2f * scale + bobOffset);
            lowerLip.quadTo(cx, cy - 7 * scale + bobOffset, cx + 2 * scale, cy - 8.2f * scale + bobOffset);
            lowerLip.close();
            canvas.drawPath(lowerLip, paint);
            // 唇部高光
            paint.setColor(Color.argb(80, 255, 255, 255));
            canvas.drawOval(cx - 0.5f * scale, cy - 8 * scale + bobOffset,
                    cx + 0.5f * scale, cy - 7.6f * scale + bobOffset, paint);
        }

        // 额头花钿（粉色小花，比菱形更精致）
        paint.setColor(Color.rgb(225, 55, 85));
        float fmCx = cx, fmCy = cy - 15.5f * scale + bobOffset;
        float fmR = 0.9f * scale;
        for (int p = 0; p < 5; p++) {
            double a = p * Math.PI * 2 / 5 - Math.PI / 2;
            float px = fmCx + (float) Math.cos(a) * fmR;
            float py = fmCy + (float) Math.sin(a) * fmR;
            canvas.drawCircle(px, py, fmR * 0.55f, paint);
        }
        // 花心
        paint.setColor(Color.rgb(255, 220, 80));
        canvas.drawCircle(fmCx, fmCy, fmR * 0.35f, paint);

        // --- 攻击特效 ---
        if (currentState == State.ATTACKING && isWindingUp) {
            float progress = getWindUpProgress();
            // 蓄力时粉色能量聚集
            paint.setColor(Color.argb((int) (progress * 150), 255, 150, 200));
            paint.setStrokeWidth(2 * scale);
            paint.setStyle(Paint.Style.STROKE);
            float arcRadius = 15 * scale * progress;
            canvas.drawCircle(cx, cy - 5 * scale + bobOffset, arcRadius, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }

    // ==================== 魔法粒子 ====================

    private void drawSparkles(Canvas canvas, Paint paint, float cx, float cy, float scale) {
        long now = System.currentTimeMillis();
        // 每 200ms 更新一次粒子位置
        if (now - lastSparkleTime > 200) {
            lastSparkleTime = now;
        }

        // 绘制 6 个飘散的粉色光点
        for (int i = 0; i < 6; i++) {
            float angle = (float) (now / 1200.0f + i * Math.PI / 3);
            float radius = size * 0.6f + (float) Math.sin(now / 800.0 + i * 1.5) * 5 * scale;
            float sparkleX = cx + (float) Math.cos(angle) * radius;
            float sparkleY = cy - 5 * scale + (float) Math.sin(angle * 0.7 + i) * radius * 0.5f;
            float sparkleAlpha = 0.5f + 0.5f * (float) Math.sin(now / 300.0 + i * 2.0);
            float sparkleSize = (1.0f + (float) Math.sin(now / 400.0 + i) * 0.5f) * scale;

            paint.setColor(Color.argb((int) (sparkleAlpha * 120), 255, 180, 220));
            canvas.drawCircle(sparkleX, sparkleY, sparkleSize, paint);

            // 光点核心（更亮）
            paint.setColor(Color.argb((int) (sparkleAlpha * 180), 255, 220, 240));
            canvas.drawCircle(sparkleX, sparkleY, sparkleSize * 0.5f, paint);
        }
    }
}

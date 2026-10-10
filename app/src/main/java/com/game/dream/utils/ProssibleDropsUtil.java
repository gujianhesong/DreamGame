package com.game.dream.utils;

import com.game.dream.enemy.Enemy;
import com.game.dream.item.ConsumableItem;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ProssibleDropsUtil {

    public enum DropCategory {
        POTION,
        GAIN,
        EQUIP,
        CRAFT,
        GEM,
        STONE,
        TREASURE_MAP,
        OTHER
    }

    /** 普通 / 首领 / 精英 / BOSS：药、增益、装备、制造书铁、宝石、洗练石、藏宝图 */
    private static final int[] WEIGHT_NORMAL = {55, 5, 12, 12, 8, 8, 3};
    private static final int[] WEIGHT_LEADER = {45, 10, 15, 14, 8, 8, 4};
    private static final int[] WEIGHT_ELITE = {35, 15, 18, 15, 10, 7, 7};
    private static final int[] WEIGHT_BOSS = {20, 15, 25, 18, 12, 10, 15};

    public static DropCategory classify(Item item) {
        if (item == null) {
            return DropCategory.OTHER;
        }
        if (item instanceof EquipmentItem) {
            return DropCategory.EQUIP;
        }
        if (item instanceof com.game.dream.item.TreasureMapItem) {
            return DropCategory.TREASURE_MAP;
        }
        if (item instanceof ConsumableItem) {
            ConsumableItem.EffectType effect = ((ConsumableItem) item).getEffectType();
            if (effect == ConsumableItem.EffectType.HEAL_HP
                    || effect == ConsumableItem.EffectType.HEAL_MP) {
                return DropCategory.POTION;
            }
            return DropCategory.GAIN;
        }
        int id = item.getId();
        if (id >= 210000 && id < 230000) {
            return DropCategory.CRAFT;
        }
        if (id >= 230000 && id < 240000) {
            return DropCategory.GEM;
        }
        if (id >= 240000 && id < 250000) {
            return DropCategory.STONE;
        }
        return DropCategory.OTHER;
    }

    public static List<ItemStack> rollDrops(List<Item> possibleDrops, Enemy enemy, int numDrops) {
        List<ItemStack> drops = new ArrayList<>();
        if (possibleDrops == null || possibleDrops.isEmpty() || numDrops <= 0) {
            return drops;
        }

        Map<DropCategory, List<Item>> buckets = new EnumMap<>(DropCategory.class);
        for (Item item : possibleDrops) {
            DropCategory category = classify(item);
            List<Item> bucket = buckets.get(category);
            if (bucket == null) {
                bucket = new ArrayList<>();
                buckets.put(category, bucket);
            }
            bucket.add(item);
        }

        if (!buckets.containsKey(DropCategory.TREASURE_MAP)) {
            Item treasureMap = ItemCreator.createTreasureMap();
            if (treasureMap != null) {
                List<Item> maps = new ArrayList<>();
                maps.add(treasureMap);
                buckets.put(DropCategory.TREASURE_MAP, maps);
            }
        }

        int[] weights = categoryWeights(enemy);
        for (int i = 0; i < numDrops; i++) {
            DropCategory category = pickCategory(buckets, weights);
            if (category == null) {
                break;
            }
            List<Item> bucket = buckets.get(category);
            Item item = bucket.get((int) (Math.random() * bucket.size()));
            int quantity = 1 + (int) (Math.random() * 2);
            if (item instanceof EquipmentItem || category == DropCategory.TREASURE_MAP) {
                quantity = 1;
                bucket.remove(item);
                if (bucket.isEmpty()) {
                    buckets.remove(category);
                }
            }
            drops.add(new ItemStack(item, quantity));
        }
        return drops;
    }

    private static int[] categoryWeights(Enemy enemy) {
        if (enemy != null && enemy.isBossTier()) {
            return WEIGHT_BOSS;
        }
        Enemy.EnemyLevel level = enemy == null ? Enemy.EnemyLevel.NORMAL : enemy.getEnemyLevel();
        if (level == Enemy.EnemyLevel.ELITE) {
            return WEIGHT_ELITE;
        }
        if (level == Enemy.EnemyLevel.LEADER) {
            return WEIGHT_LEADER;
        }
        return WEIGHT_NORMAL;
    }

    private static DropCategory pickCategory(Map<DropCategory, List<Item>> buckets, int[] weights) {
        DropCategory[] ordered = {
                DropCategory.POTION, DropCategory.GAIN, DropCategory.EQUIP,
                DropCategory.CRAFT, DropCategory.GEM, DropCategory.STONE,
                DropCategory.TREASURE_MAP
        };
        int total = 0;
        int otherWeight = 0;
        for (int i = 0; i < ordered.length; i++) {
            List<Item> bucket = buckets.get(ordered[i]);
            if (bucket != null && !bucket.isEmpty()) {
                total += weights[i];
            }
        }
        List<Item> other = buckets.get(DropCategory.OTHER);
        if (other != null && !other.isEmpty()) {
            otherWeight = 1;
            total += otherWeight;
        }
        if (total <= 0) {
            return null;
        }
        int roll = (int) (Math.random() * total);
        int cursor = 0;
        for (int i = 0; i < ordered.length; i++) {
            List<Item> bucket = buckets.get(ordered[i]);
            if (bucket == null || bucket.isEmpty()) {
                continue;
            }
            cursor += weights[i];
            if (roll < cursor) {
                return ordered[i];
            }
        }
        return DropCategory.OTHER;
    }

    /**
     * 场景怪物物品掉落
     * @param enemy
     */
    public static void addPossibleDrops_sceneLevel1(Enemy enemy){
        if(enemy == null) return;

        Enemy.EnemyLevel enemyLevel = enemy.getEnemyLevel();
        if (enemy.isBossTier()) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_2());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_2());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_2());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_2());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_2());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(30, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(40, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(3));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(40));
        } else if (enemyLevel == Enemy.EnemyLevel.ELITE) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            enemy.addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            enemy.addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            enemy.addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_1());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_1());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_1());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_1());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_1());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(10, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(20, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(10, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(20, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(10));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(20));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(2));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(30));
        } else {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp1_2_QiyeLian());
            enemy.addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            enemy.addPossibleDrop(ItemCreator.createMp1_2_Xiangye());
            enemy.addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(0, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(10, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(10, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(10));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(1));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(10));
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(20));
        }
    }

    /**
     * 场景怪物物品掉落
     * @param enemy
     */
    public static void addPossibleDrops_sceneLevel2(Enemy enemy){
        if(enemy == null) return;
        
        Enemy.EnemyLevel enemyLevel = enemy.getEnemyLevel();
        if (enemy.isBossTier()) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_3());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_3());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_3());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_3());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_3());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(40, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(50, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(3));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(50));
        } else if (enemyLevel == Enemy.EnemyLevel.ELITE) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_2());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_2());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_2());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_2());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_2());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(30, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(40, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(2));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(40));
        } else {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            enemy.addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            enemy.addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            enemy.addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(20, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(30, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(20, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(20));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(1));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(20));
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(30));
        }
    }

    /**
     * 场景怪物物品掉落
     * @param enemy
     */
    public static void addPossibleDrops_sceneLevel3(Enemy enemy){
        if(enemy == null) return;

        Enemy.EnemyLevel enemyLevel = enemy.getEnemyLevel();
        if (enemy.isBossTier()) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_3());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_3());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_3());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_3());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_3());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(50, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(60, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(60, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(60));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(3));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(60));
        } else if (enemyLevel == Enemy.EnemyLevel.ELITE) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_2());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_2());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_2());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_2());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_2());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(40, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(50, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(2));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(50));
        } else {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            enemy.addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            enemy.addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            enemy.addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(30, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(40, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(30, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(40, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(30));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(40));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(1));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(30));
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(40));
        }
    }

    /**
     * 场景怪物物品掉落
     * @param enemy
     */
    public static void addPossibleDrops_sceneLevel4(Enemy enemy){
        if(enemy == null) return;

        Enemy.EnemyLevel enemyLevel = enemy.getEnemyLevel();
        if (enemy.isBossTier()) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_3_Fengweicao());
            enemy.addPossibleDrop(ItemCreator.createHp2_4_Longzhixinxie());
            enemy.addPossibleDrop(ItemCreator.createMp2_3_Xueshanhu());
            enemy.addPossibleDrop(ItemCreator.createMp2_4_Canfengyinlu());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_3());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_3());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_3());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_3());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_3());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(70, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(80, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(70, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(80, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(70));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(80));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(4));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(80));
        } else if (enemyLevel == Enemy.EnemyLevel.ELITE) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_2());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_2());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_2());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_2());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_2());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(60, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(70, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(60, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(70, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(60));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(70));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(3));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(70));
        } else {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            enemy.addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            enemy.addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            enemy.addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(50, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(60, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(50, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(60, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(50));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(60));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(2));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(50));
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(60));
        }
    }

    /**
     * 场景怪物物品掉落
     * @param enemy
     */
    public static void addPossibleDrops_sceneLevel5(Enemy enemy){
        if(enemy == null) return;

        Enemy.EnemyLevel enemyLevel = enemy.getEnemyLevel();
        if (enemy.isBossTier()) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_3_Fengweicao());
            enemy.addPossibleDrop(ItemCreator.createHp2_4_Longzhixinxie());
            enemy.addPossibleDrop(ItemCreator.createMp2_3_Xueshanhu());
            enemy.addPossibleDrop(ItemCreator.createMp2_4_Canfengyinlu());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_3());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_3());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_3());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_3());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_3());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(90, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(100, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(90, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(100, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(90));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(100));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(4));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(100));
        } else if (enemyLevel == Enemy.EnemyLevel.ELITE) {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp2_1_Zishiying());
            enemy.addPossibleDrop(ItemCreator.createHp2_2_Liuhuangcao());
            enemy.addPossibleDrop(ItemCreator.createMp2_1_Diyulingzhi());
            enemy.addPossibleDrop(ItemCreator.createMp2_2_Xianhuxian());
            //增益药品
            enemy.addPossibleDrop(ItemCreator.createGain_hp_2());
            enemy.addPossibleDrop(ItemCreator.createGain_attack_2());
            enemy.addPossibleDrop(ItemCreator.createGain_defense_2());
            enemy.addPossibleDrop(ItemCreator.createGain_mana_2());
            enemy.addPossibleDrop(ItemCreator.createGain_speed_2());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(80, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(90, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(80, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(90, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(80));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(90));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(3));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(90));
        } else {
            //药品
            enemy.addPossibleDrop(ItemCreator.createHp1_3_Lurong());
            enemy.addPossibleDrop(ItemCreator.createHp1_4_Xuesechahua());
            enemy.addPossibleDrop(ItemCreator.createMp1_3_Shexiang());
            enemy.addPossibleDrop(ItemCreator.createMp1_4_Dingxiangshui());
            //装备
            enemy.addPossibleDrop(EquipCreator.createEquip(70, null));
            enemy.addPossibleDrop(EquipCreator.createEquip(80, null));
            //制造书铁
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(70, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipBook(80, null));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(70));
            enemy.addPossibleDrop(ItemCreator.createBuildEquipIron(80));
            //宝石
            enemy.addPossibleDrop(ItemCreator.createGemstone(2));
            //洗练石
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(70));
            enemy.addPossibleDrop(ItemCreator.createXiLianStone(80));
        }
    }
}

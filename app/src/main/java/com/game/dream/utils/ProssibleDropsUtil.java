package com.game.dream.utils;

import com.game.dream.enemy.Enemy;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.ItemCreator;

public class ProssibleDropsUtil {

    /**
     * 场景怪物物品掉落
     * @param enemy
     */
    public static void addPossibleDrops_sceneLevel1(Enemy enemy){
        if(enemy == null) return;

        Enemy.EnemyLevel enemyLevel = enemy.getEnemyLevel();
        if (enemyLevel == Enemy.EnemyLevel.BOSS) {
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
        if (enemyLevel == Enemy.EnemyLevel.BOSS) {
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
        if (enemyLevel == Enemy.EnemyLevel.BOSS) {
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
}

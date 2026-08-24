package com.game.dream.item;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.enums.FoodType;
import com.game.dream.enums.GemtoneType;
import com.game.dream.enums.SkillType;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.utils.PriceUtil;
import com.game.dream.utils.Utils;

import java.util.Arrays;
import java.util.List;

public class ItemCreator {
    public static void testAddSomething() {
        //添加宝石
        /*List<GemtoneType> stoneTypes = Arrays.asList(GemtoneType.GT_TaiYangShi, GemtoneType.GT_HoneMaNao, GemtoneType.GT_SheLiZi,
                GemtoneType.GT_YueLiangShi, GemtoneType.GT_GuangMangShi, GemtoneType.GT_HeiBaoShi,
                GemtoneType.GT_LanBaoShi, GemtoneType.GT_ShenMiShi);
        for (int i = 1; i <= 10; i++) {
            for (GemtoneType gemtoneType : stoneTypes) {
                Item item = ItemCreator.createGemstone(gemtoneType, i);
                ItemSystem.getInstance().addItem(item, 50);
            }
        }

        //添加制造书
        List<EquipmentItem.Slot> slots = Arrays.asList(EquipmentItem.Slot.HELMET, EquipmentItem.Slot.ACCESSORY,
                EquipmentItem.Slot.WEAPON, EquipmentItem.Slot.ARMOR,
                EquipmentItem.Slot.BELT, EquipmentItem.Slot.SHOES);
        for (int i = 1; i <= 10; i++) {
            for (EquipmentItem.Slot slot : slots) {
                Item item = ItemCreator.createBuildEquipBook(i * 10, slot);
                ItemSystem.getInstance().addItem(item, 50);
            }
        }

        //添加精铁
        for (int i = 1; i <= 10; i++) {
            Item item = ItemCreator.createBuildEquipIron(i * 10);
            ItemSystem.getInstance().addItem(item, 50);
        }*/

        //添加洗炼石
        for (int i = 1; i <= 10; i++) {
            Item item = ItemCreator.createXiLianStone(i * 10);
            ItemSystem.getInstance().addItem(item, 50);
        }
    }

    public static Item createItemWithInfo(ItemInfo itemInfo) {
        if (itemInfo == null) {
            return null;
        }
        Item item = null;

        int id = itemInfo.getId();
        int type = id / 1000;
        if (itemInfo instanceof EquipItemInfo) {
            return EquipCreator.createEquipWithInfo(itemInfo);
        }

        switch (type) {
            case 101:
                //红药
                item = createMedicineHp(id);
                break;
            case 102:
                //蓝药
                item = createMedicineMp(id);
                break;
            case 103:
                //增益药品
                item = createMedicineGain(id);
                break;
            case 104:
                //解除药品
                item = createMedicineRestoreState(id);
                break;
            case 210:
            case 211: {
                //精铁
                int level = (id - 210000) / 10;
                item = createBuildEquipIron(level);
            }
            break;
            case 220:
            case 221: {
                //制造书
                int level = (id - 220001) / 10;
                int slotIndex = id % 10 - 1;
                EquipmentItem.Slot slot = EquipmentItem.Slot.getSlotWithIndex(slotIndex);
                if (slot != null) {
                    item = createBuildEquipBook(level, slot);
                }
            }
            break;
            case 230:
            case 231: {
                //宝石
                int level = (id - 230001) / 100;
                int stoneIndex = id % 10 - 1;
                GemtoneType gemtoneType = GemtoneType.getGemtoneTypeWithIndex(stoneIndex);
                if (gemtoneType != null) {
                    item = createGemstone(gemtoneType, level);
                }
            }
            break;
            case 240:
            case 241: {
                //洗炼石
                int level = (id - 240000) / 10;
                item = createXiLianStone(level);
            }
            break;
            case 250: {
                //金刚石等特殊材料
                if (id == 250001) {
                    item = createJinGangShi();
                }
            }
            break;
            case 310: {
                //食物
                int ordinal = id - 310000;
                FoodType foodType = FoodType.values()[ordinal];
                item = createCookFood(foodType);
            }
            break;
            case 400: {
                //技能书
                int ordinal = id - 400000;
                SkillType skillType = getSkillTypeByOrdinal(ordinal);
                if (skillType != null) {
                    item = createSkillBook(skillType);
                }
            }
            break;
        }

        return item;
    }

    public static Item createMedicineHp(int id) {
        Item item = null;

        //气血药品
        switch (id) {
            case 101011:
                item = createHp1_1_Siyehua();
                break;
            case 101012:
                item = createHp1_2_QiyeLian();
                break;
            case 101013:
                item = createHp1_3_Lurong();
                break;
            case 101014:
                item = createHp1_4_Xuesechahua();
                break;
            case 101021:
                item = createHp2_1_Zishiying();
                break;
            case 101022:
                item = createHp2_2_Liuhuangcao();
                break;
            case 101023:
                item = createHp2_3_Fengweicao();
                break;
            case 101024:
                item = createHp2_4_Longzhixinxie();
                break;
            case 101025:
                item = createHp2_5_Fenghuozhijing();
                break;
        }
        return item;
    }

    public static Item createMedicineMp(int id) {
        Item item = null;

        //魔法药品
        switch (id) {
            case 102011:
                item = createMp1_1_Foshou();
                break;
            case 102012:
                item = createMp1_2_Xiangye();
                break;
            case 102013:
                item = createMp1_3_Shexiang();
                break;
            case 102014:
                item = createMp1_4_Dingxiangshui();
                break;
            case 102021:
                item = createMp2_1_Diyulingzhi();
                break;
            case 102022:
                item = createMp2_2_Xianhuxian();
                break;
            case 102023:
                item = createMp2_3_Xueshanhu();
                break;
            case 102024:
                item = createMp2_4_Canfengyinlu();
                break;
            case 102025:
                item = createMp2_5_Bailuweishuang();
                break;
        }
        return item;
    }

    private static Item createMedicineGain(int id) {
        Item item = null;
        //增益气血药品
        switch (id) {
            case 103011:
                item = createGain_hp_1();
                break;
            case 103012:
                item = createGain_hp_2();
                break;
            case 103013:
                item = createGain_hp_3();
                break;
            case 103014:
                item = createGain_hp_4();
                break;
            case 103015:
                item = createGain_hp_5();
                break;
        }

        //增益攻击药品
        switch (id) {
            case 103021:
                item = createGain_attack_1();
                break;
            case 103022:
                item = createGain_attack_2();
                break;
            case 103023:
                item = createGain_attack_3();
                break;
            case 103024:
                item = createGain_attack_4();
                break;
            case 103025:
                item = createGain_attack_5();
                break;
        }

        //增益防御药品
        switch (id) {
            case 103031:
                item = createGain_defense_1();
                break;
            case 103032:
                item = createGain_defense_2();
                break;
            case 103033:
                item = createGain_defense_3();
                break;
            case 103034:
                item = createGain_defense_4();
                break;
            case 103035:
                item = createGain_defense_5();
                break;
        }

        //增益灵力药品
        switch (id) {
            case 103041:
                item = createGain_mana_1();
                break;
            case 103042:
                item = createGain_mana_2();
                break;
            case 103043:
                item = createGain_mana_3();
                break;
            case 103044:
                item = createGain_mana_4();
                break;
            case 103045:
                item = createGain_mana_5();
                break;
        }

        //增益速度药品
        switch (id) {
            case 103051:
                item = createGain_speed_1();
                break;
            case 103052:
                item = createGain_speed_2();
                break;
            case 103053:
                item = createGain_speed_3();
                break;
            case 103054:
                item = createGain_speed_4();
                break;
            case 103055:
                item = createGain_speed_5();
                break;
        }
        return item;
    }

    private static Item createMedicineRestoreState(int id) {
        Item item = null;
        //解除状态药品
        return null;
    }

    //------------------ 补充气血药品 ----------------------
    public static Item createHp1_1_Siyehua() {
        return new ConsumableItem(
                101011, "四叶花", "恢复200点气血值",
                Item.Rarity.Rarity_1,
                999, 100,
                ConsumableItem.EffectType.HEAL_HP,
                200, 0
        );
    }

    public static Item createHp1_2_QiyeLian() {
        return new ConsumableItem(
                101012, "七叶莲", "恢复300点气血值",
                Item.Rarity.Rarity_1,
                999, 150,
                ConsumableItem.EffectType.HEAL_HP,
                300, 0
        );
    }

    public static Item createHp1_3_Lurong() {
        return new ConsumableItem(
                101013, "鹿茸", "恢复400点气血值",
                Item.Rarity.Rarity_1,
                999, 200,
                ConsumableItem.EffectType.HEAL_HP,
                400, 0
        );
    }

    public static Item createHp1_4_Xuesechahua() {
        return new ConsumableItem(
                101014, "血色茶花", "恢复500点气血值",
                Item.Rarity.Rarity_2,
                999, 250,
                ConsumableItem.EffectType.HEAL_HP,
                500, 0
        );
    }

    public static Item createHp2_1_Zishiying() {
        return new ConsumableItem(
                101021, "紫石英", "恢复600点气血值",
                Item.Rarity.Rarity_2,
                999, 300,
                ConsumableItem.EffectType.HEAL_HP,
                600, 0
        );
    }

    public static Item createHp2_2_Liuhuangcao() {
        return new ConsumableItem(
                101022, "硫磺草", "恢复800点气血值",
                Item.Rarity.Rarity_2,
                999, 350,
                ConsumableItem.EffectType.HEAL_HP,
                800, 0
        );
    }

    public static Item createHp2_3_Fengweicao() {
        return new ConsumableItem(
                101023, "凤尾草", "恢复1000点气血值",
                Item.Rarity.Rarity_3,
                999, 400,
                ConsumableItem.EffectType.HEAL_HP,
                1000, 0
        );
    }

    public static Item createHp2_4_Longzhixinxie() {
        return new ConsumableItem(
                101024, "龙之心屑", "恢复1500点气血值",
                Item.Rarity.Rarity_3,
                999, 450,
                ConsumableItem.EffectType.HEAL_HP,
                1500, 0
        );
    }

    public static Item createHp2_5_Fenghuozhijing() {
        return new ConsumableItem(
                101025, "火凤之睛", "恢复2000点气血值",
                Item.Rarity.Rarity_3,
                999, 500,
                ConsumableItem.EffectType.HEAL_HP,
                2000, 0
        );
    }

    //------------------ 补充魔法药品 ----------------------

    public static Item createMp1_1_Foshou() {
        return new ConsumableItem(
                102011, "佛手", "恢复200点魔法值",
                Item.Rarity.Rarity_1,
                999, 100,
                ConsumableItem.EffectType.HEAL_MP,
                200, 0
        );
    }

    public static Item createMp1_2_Xiangye() {
        return new ConsumableItem(
                102012, "香叶", "恢复300点魔法值",
                Item.Rarity.Rarity_1,
                999, 150,
                ConsumableItem.EffectType.HEAL_MP,
                300, 0
        );
    }

    public static Item createMp1_3_Shexiang() {
        return new ConsumableItem(
                102013, "麝香", "恢复400点魔法值",
                Item.Rarity.Rarity_1,
                999, 200,
                ConsumableItem.EffectType.HEAL_MP,
                400, 0
        );
    }

    public static Item createMp1_4_Dingxiangshui() {
        return new ConsumableItem(
                102014, "丁香水", "恢复500点魔法值",
                Item.Rarity.Rarity_2,
                999, 250,
                ConsumableItem.EffectType.HEAL_MP,
                500, 0
        );
    }

    public static Item createMp2_1_Diyulingzhi() {
        return new ConsumableItem(
                102021, "地狱灵芝", "恢复600点魔法值",
                Item.Rarity.Rarity_2,
                999, 300,
                ConsumableItem.EffectType.HEAL_MP,
                600, 0
        );
    }

    public static Item createMp2_2_Xianhuxian() {
        return new ConsumableItem(
                102022, "仙狐涎", "恢复800点魔法值",
                Item.Rarity.Rarity_2,
                999, 350,
                ConsumableItem.EffectType.HEAL_MP,
                800, 0
        );
    }

    public static Item createMp2_3_Xueshanhu() {
        return new ConsumableItem(
                102023, "血珊瑚", "恢复1000点魔法值",
                Item.Rarity.Rarity_3,
                999, 400,
                ConsumableItem.EffectType.HEAL_MP,
                1000, 0
        );
    }

    public static Item createMp2_4_Canfengyinlu() {
        return new ConsumableItem(
                102024, "餐风饮露", "恢复1500点魔法值",
                Item.Rarity.Rarity_3,
                999, 450,
                ConsumableItem.EffectType.HEAL_MP,
                1500, 0
        );
    }

    public static Item createMp2_5_Bailuweishuang() {
        return new ConsumableItem(
                102025, "白露为霜", "恢复2000点魔法值",
                Item.Rarity.Rarity_3,
                999, 500,
                ConsumableItem.EffectType.HEAL_MP,
                2000, 0
        );
    }

    //------------------ 增益药品-气血 ----------------------

    public static Item createGain_hp_1() {
        return new ConsumableItem(
                103011, "初级气血丹", "临时增加500点气血，持续30分钟",
                Item.Rarity.Rarity_1,
                999, 1000,
                ConsumableItem.EffectType.BUFF_HP,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_2() {
        return new ConsumableItem(
                103012, "中级气血丹", "临时增加1000点气血，持续30分钟",
                Item.Rarity.Rarity_2,
                999, 2000,
                ConsumableItem.EffectType.BUFF_HP,
                1000, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_3() {
        return new ConsumableItem(
                103013, "高级气血丹", "临时增加1500点气血，持续30分钟",
                Item.Rarity.Rarity_3,
                999, 3000,
                ConsumableItem.EffectType.BUFF_HP,
                1500, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_4() {
        return new ConsumableItem(
                103014, "特级气血丹", "临时增加2000点气血，持续30分钟",
                Item.Rarity.Rarity_4,
                999, 4000,
                ConsumableItem.EffectType.BUFF_HP,
                2000, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_5() {
        return new ConsumableItem(
                103015, "极品气血丹", "临时增加3000点气血，持续30分钟",
                Item.Rarity.Rarity_5,
                999, 6000,
                ConsumableItem.EffectType.BUFF_HP,
                3000, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-伤害 ----------------------

    public static Item createGain_attack_1() {
        return new ConsumableItem(
                103021, "初级攻击丹", "临时增加100点伤害，持续30分钟",
                Item.Rarity.Rarity_1,
                999, 1000,
                ConsumableItem.EffectType.BUFF_HP,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_2() {
        return new ConsumableItem(
                103022, "中级攻击丹", "临时增加200点伤害，持续30分钟",
                Item.Rarity.Rarity_2,
                999, 2000,
                ConsumableItem.EffectType.BUFF_HP,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_3() {
        return new ConsumableItem(
                103023, "高级攻击丹", "临时增加300点伤害，持续30分钟",
                Item.Rarity.Rarity_3,
                999, 3000,
                ConsumableItem.EffectType.BUFF_HP,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_4() {
        return new ConsumableItem(
                103024, "特级攻击丹", "临时增加500点伤害，持续30分钟",
                Item.Rarity.Rarity_4,
                999, 4000,
                ConsumableItem.EffectType.BUFF_HP,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_5() {
        return new ConsumableItem(
                103025, "极品攻击丹", "临时增加800点伤害，持续30分钟",
                Item.Rarity.Rarity_5,
                999, 6000,
                ConsumableItem.EffectType.BUFF_HP,
                800, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-防御 ----------------------

    public static Item createGain_defense_1() {
        return new ConsumableItem(
                103031, "初级防御丹", "临时增加100点防御，持续30分钟",
                Item.Rarity.Rarity_1,
                999, 1000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_2() {
        return new ConsumableItem(
                103032, "中级防御丹", "临时增加200点防御，持续30分钟",
                Item.Rarity.Rarity_2,
                999, 2000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_3() {
        return new ConsumableItem(
                103033, "高级防御丹", "临时增加300点防御，持续30分钟",
                Item.Rarity.Rarity_3,
                999, 3000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_4() {
        return new ConsumableItem(
                103034, "特级防御丹", "临时增加500点防御，持续30分钟",
                Item.Rarity.Rarity_4,
                999, 4000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_5() {
        return new ConsumableItem(
                103035, "极品防御丹", "临时增加800点防御，持续30分钟",
                Item.Rarity.Rarity_5,
                999, 6000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                800, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-灵力 ----------------------

    public static Item createGain_mana_1() {
        return new ConsumableItem(
                103041, "初级灵力丹", "临时增加100点灵力，持续30分钟",
                Item.Rarity.Rarity_1,
                999, 1000,
                ConsumableItem.EffectType.BUFF_MANA,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_2() {
        return new ConsumableItem(
                103042, "中级灵力丹", "临时增加200点灵力，持续30分钟",
                Item.Rarity.Rarity_2,
                999, 2000,
                ConsumableItem.EffectType.BUFF_MANA,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_3() {
        return new ConsumableItem(
                103043, "高级灵力丹", "临时增加300点灵力，持续30分钟",
                Item.Rarity.Rarity_3,
                999, 3000,
                ConsumableItem.EffectType.BUFF_MANA,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_4() {
        return new ConsumableItem(
                103044, "特级灵力丹", "临时增加500点灵力，持续30分钟",
                Item.Rarity.Rarity_4,
                999, 4000,
                ConsumableItem.EffectType.BUFF_MANA,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_5() {
        return new ConsumableItem(
                103045, "极品灵力丹", "临时增加800点灵力，持续30分钟",
                Item.Rarity.Rarity_5,
                999, 6000,
                ConsumableItem.EffectType.BUFF_MANA,
                800, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-速度 ----------------------

    public static Item createGain_speed_1() {
        return new ConsumableItem(
                103051, "初级速度丹", "临时增加50点速度，持续30分钟",
                Item.Rarity.Rarity_1,
                999, 1000,
                ConsumableItem.EffectType.BUFF_SPEED,
                50, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_2() {
        return new ConsumableItem(
                103052, "中级速度丹", "临时增加100点速度，持续30分钟",
                Item.Rarity.Rarity_2,
                999, 2000,
                ConsumableItem.EffectType.BUFF_SPEED,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_3() {
        return new ConsumableItem(
                103053, "高级速度丹", "临时增加200点速度，持续30分钟",
                Item.Rarity.Rarity_3,
                999, 3000,
                ConsumableItem.EffectType.BUFF_SPEED,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_4() {
        return new ConsumableItem(
                103054, "特级速度丹", "临时增加300点速度，持续30分钟",
                Item.Rarity.Rarity_4,
                999, 4000,
                ConsumableItem.EffectType.BUFF_SPEED,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_5() {
        return new ConsumableItem(
                103055, "极品速度丹", "临时增加400点速度，持续30分钟",
                Item.Rarity.Rarity_5,
                999, 6000,
                ConsumableItem.EffectType.BUFF_SPEED,
                400, 30 * 60 * 1000
        );
    }

    public static Item createBuildEquipIron(int level) {
        int id = 210000 + level * 10;
        Item.Rarity rarity;
        if (level <= 20) {
            rarity = Item.Rarity.Rarity_1;
        } else if (level <= 40) {
            rarity = Item.Rarity.Rarity_2;
        } else if (level <= 60) {
            rarity = Item.Rarity.Rarity_3;
        } else if (level <= 90) {
            rarity = Item.Rarity.Rarity_4;
        } else if (level <= 120) {
            rarity = Item.Rarity.Rarity_5;
        } else {
            rarity = Item.Rarity.Rarity_6;
        }
        return new Item(
                id, level + "级精铁", "制造装备的必备材料", Item.Type.MATERIAL,
                rarity,
                999, PriceUtil.getItemPriceWith10Level(2000, level)
        );
    }

    public static Item createBuildEquipBook(int level, EquipmentItem.Slot slot) {
        if (slot == null) {
            slot = Utils.getRandomItem(Arrays.asList(EquipmentItem.Slot.HELMET, EquipmentItem.Slot.ACCESSORY,
                    EquipmentItem.Slot.WEAPON, EquipmentItem.Slot.ARMOR,
                    EquipmentItem.Slot.BELT, EquipmentItem.Slot.SHOES));
        }
        int id = 220001 + level * 10 + slot.ordinal();
        String name = "";
        switch (slot) {
            case HELMET:
                name = level + "级头盔制造书";
                break;
            case ACCESSORY:
                name = level + "级项链制造书";
                break;
            case WEAPON:
                name = level + "级武器制造书";
                break;
            case ARMOR:
                name = level + "级铠甲制造书";
                break;
            case BELT:
                name = level + "级腰带制造书";
                break;
            case SHOES:
                name = level + "级鞋子制造书";
                break;
        }
        Item.Rarity rarity;
        if (level <= 20) {
            rarity = Item.Rarity.Rarity_1;
        } else if (level <= 40) {
            rarity = Item.Rarity.Rarity_2;
        } else if (level <= 60) {
            rarity = Item.Rarity.Rarity_3;
        } else if (level <= 90) {
            rarity = Item.Rarity.Rarity_4;
        } else if (level <= 120) {
            rarity = Item.Rarity.Rarity_5;
        } else {
            rarity = Item.Rarity.Rarity_6;
        }
        return new Item(
                id, name, "制造装备的必备材料", Item.Type.MATERIAL,
                rarity,
                999, PriceUtil.getItemPriceWith10Level(2000, level)
        );
    }

    public static Item createGemstone(GemtoneType gemtoneType, int level) {
        int id = 230001 + level * 100 + gemtoneType.ordinal();

        String desc = "";
        switch (gemtoneType) {
            case GT_TaiYangShi:
                desc = "镶嵌装备的太阳宝石，镶嵌后可增加攻击伤害";
                break;
            case GT_HoneMaNao:
                desc = "镶嵌装备的红玛瑙宝石，镶嵌后可增加攻击和法术命中";
                break;
            case GT_SheLiZi:
                desc = "镶嵌装备的舍利子宝石，镶嵌后可增加灵力";
                break;
            case GT_YueLiangShi:
                desc = "镶嵌装备的月亮宝石，镶嵌后可增加防御";
                break;
            case GT_GuangMangShi:
                desc = "镶嵌装备的光芒宝石，镶嵌后可增加气血";
                break;
            case GT_HeiBaoShi:
                desc = "镶嵌装备的黑色宝石，镶嵌后可增加速度";
                break;
            case GT_LanBaoShi:
                desc = "镶嵌装备的蓝色宝石，镶嵌后可增加魔法";
                break;
            case GT_ShenMiShi:
                desc = "镶嵌装备的神秘宝石，镶嵌后可增加闪避";
                break;
        }
        Item.Rarity rarity;
        if (level <= 2) {
            rarity = Item.Rarity.Rarity_1;
        } else if (level <= 4) {
            rarity = Item.Rarity.Rarity_2;
        } else if (level <= 6) {
            rarity = Item.Rarity.Rarity_3;
        } else if (level <= 9) {
            rarity = Item.Rarity.Rarity_4;
        } else if (level <= 12) {
            rarity = Item.Rarity.Rarity_5;
        } else {
            rarity = Item.Rarity.Rarity_6;
        }
        return new Item(
                id, level + "级" + gemtoneType.getDesc(), desc, Item.Type.MATERIAL,
                rarity,
                999, PriceUtil.getItemPriceWith10Level(2000, level)
        );
    }

    public static Item createXiLianStone(int level) {
        int id = 240000 + level * 10;
        Item.Rarity rarity;
        if (level <= 20) {
            rarity = Item.Rarity.Rarity_1;
        } else if (level <= 40) {
            rarity = Item.Rarity.Rarity_2;
        } else if (level <= 60) {
            rarity = Item.Rarity.Rarity_3;
        } else if (level <= 90) {
            rarity = Item.Rarity.Rarity_4;
        } else if (level <= 120) {
            rarity = Item.Rarity.Rarity_5;
        } else {
            rarity = Item.Rarity.Rarity_6;
        }
        return new Item(
                id, level + "级洗炼石", "洗炼装备附加属性的必备材料", Item.Type.MATERIAL,
                rarity,
                999, PriceUtil.getItemPriceWith10Level(2000, level)
        );
    }

    public static Item createCookFood(FoodType foodType) {
        int id = 310000 + foodType.ordinal();
        Item.Rarity rarity = Item.Rarity.Rarity_0;

        return new Item(
                id, foodType.getName(), foodType.getDesc(), Item.Type.FOOD,
                rarity,
                999, 1000
        );
    }

    /**
     * 创建金刚石 - 任务奖励物品
     */
    public static Item createJinGangShi() {
        return new Item(
                250001, "金刚石", "坚硬无比的宝石，散发着耀眼的光芒，是极其珍贵的材料", Item.Type.MATERIAL,
                Item.Rarity.Rarity_5,
                999, 50000
        );
    }

    //------------------ 技能书 ----------------------

    /**
     * 根据 SkillType 创建对应的技能书
     */
    public static SkillBookItem createSkillBook(SkillType skillType) {
        int id = 400000 + skillType.ordinal();
        String name = getSkillBookName(skillType);
        String desc = "使用后学习或升级技能，学习需要1本，每升一级多消耗1本";

        return new SkillBookItem(
                id, name, desc,
                Item.Rarity.Rarity_5,
                999, 5000,
                skillType
        );
    }

    /**
     * 获取技能书名称
     */
    private static String getSkillBookName(SkillType skillType) {
        List<SkillInfo> skillInfos = SkillSystem.getInstance().getMainSkillInfos();
        for(SkillInfo skillInfo : skillInfos){
            if(skillInfo.getSkillType() == skillType){
                return skillInfo.getName();
            }
        }
        return "";
    }

    /**
     * 根据 ordinal 获取 SkillType (仅返回主技能)
     * 使用 SkillType.values() 保证和 createSkillBook 中 skillType.ordinal() 完全一致
     */
    private static SkillType getSkillTypeByOrdinal(int ordinal) {
        SkillType[] allTypes = SkillType.values();
        if (ordinal >= 0 && ordinal < allTypes.length) {
            return allTypes[ordinal];
        }
        return null;
    }
}

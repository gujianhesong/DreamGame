package com.game.dream.item;

import com.game.dream.bean.ItemInfo;

public class ItemCreator {
    public static Item createItemWithInfo(ItemInfo itemInfo) {
        if (itemInfo == null) {
            return null;
        }
        Item item = null;

        int id = itemInfo.getId();
        int type = id / 1000;

        if (id >= 20000 * 1000 && id < 30000 * 1000) {
            return EquipCreator.createEquipWithInfo(itemInfo);
        }

        switch (type) {
            case 101:
                item = createMedicineHp(id);
                break;
            case 102:
                item = createMedicineMp(id);
                break;
            case 103:
                item = createMedicineGain(id);
                break;
            case 104:
                item = createMedicineRestoreState(id);
                break;
        }

        return item;
    }

    private static Item createMedicineHp(int id) {
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

    private static Item createMedicineMp(int id) {
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
                101011, "四叶花", "恢复100点气血值",
                Item.Rarity.Rarity_1,
                99, 100,
                ConsumableItem.EffectType.HEAL_HP,
                100, 0
        );
    }

    public static Item createHp1_2_QiyeLian() {
        return new ConsumableItem(
                101012, "七叶莲", "恢复150点气血值",
                Item.Rarity.Rarity_1,
                99, 150,
                ConsumableItem.EffectType.HEAL_HP,
                150, 0
        );
    }

    public static Item createHp1_3_Lurong() {
        return new ConsumableItem(
                101013, "鹿茸", "恢复200点气血值",
                Item.Rarity.Rarity_1,
                99, 200,
                ConsumableItem.EffectType.HEAL_HP,
                200, 0
        );
    }

    public static Item createHp1_4_Xuesechahua() {
        return new ConsumableItem(
                101014, "血色茶花", "恢复250点气血值",
                Item.Rarity.Rarity_1,
                99, 250,
                ConsumableItem.EffectType.HEAL_HP,
                250, 0
        );
    }

    public static Item createHp2_1_Zishiying() {
        return new ConsumableItem(
                101021, "紫石英", "恢复300点气血值",
                Item.Rarity.Rarity_2,
                99, 300,
                ConsumableItem.EffectType.HEAL_HP,
                300, 0
        );
    }

    public static Item createHp2_2_Liuhuangcao() {
        return new ConsumableItem(
                101022, "硫磺草", "恢复350点气血值",
                Item.Rarity.Rarity_2,
                99, 350,
                ConsumableItem.EffectType.HEAL_HP,
                350, 0
        );
    }

    public static Item createHp2_3_Fengweicao() {
        return new ConsumableItem(
                101023, "凤尾草", "恢复400点气血值",
                Item.Rarity.Rarity_2,
                99, 400,
                ConsumableItem.EffectType.HEAL_HP,
                400, 0
        );
    }

    public static Item createHp2_4_Longzhixinxie() {
        return new ConsumableItem(
                101024, "龙之心屑", "恢复450点气血值",
                Item.Rarity.Rarity_2,
                99, 450,
                ConsumableItem.EffectType.HEAL_HP,
                450, 0
        );
    }

    public static Item createHp2_5_Fenghuozhijing() {
        return new ConsumableItem(
                101025, "火凤之睛", "恢复500点气血值",
                Item.Rarity.Rarity_2,
                99, 500,
                ConsumableItem.EffectType.HEAL_HP,
                500, 0
        );
    }

    //------------------ 补充魔法药品 ----------------------

    public static Item createMp1_1_Foshou() {
        return new ConsumableItem(
                102011, "佛手", "恢复100点魔法值",
                Item.Rarity.Rarity_1,
                99, 100,
                ConsumableItem.EffectType.HEAL_MP,
                100, 0
        );
    }

    public static Item createMp1_2_Xiangye() {
        return new ConsumableItem(
                102012, "香叶", "恢复150点魔法值",
                Item.Rarity.Rarity_1,
                99, 150,
                ConsumableItem.EffectType.HEAL_MP,
                150, 0
        );
    }

    public static Item createMp1_3_Shexiang() {
        return new ConsumableItem(
                102013, "麝香", "恢复200点魔法值",
                Item.Rarity.Rarity_1,
                99, 200,
                ConsumableItem.EffectType.HEAL_MP,
                200, 0
        );
    }

    public static Item createMp1_4_Dingxiangshui() {
        return new ConsumableItem(
                102014, "丁香水", "恢复250点魔法值",
                Item.Rarity.Rarity_1,
                99, 250,
                ConsumableItem.EffectType.HEAL_MP,
                250, 0
        );
    }

    public static Item createMp2_1_Diyulingzhi() {
        return new ConsumableItem(
                102021, "地狱灵芝", "恢复300点魔法值",
                Item.Rarity.Rarity_2,
                99, 300,
                ConsumableItem.EffectType.HEAL_MP,
                300, 0
        );
    }

    public static Item createMp2_2_Xianhuxian() {
        return new ConsumableItem(
                102022, "仙狐涎", "恢复350点魔法值",
                Item.Rarity.Rarity_2,
                99, 350,
                ConsumableItem.EffectType.HEAL_MP,
                350, 0
        );
    }

    public static Item createMp2_3_Xueshanhu() {
        return new ConsumableItem(
                102023, "血珊瑚", "恢复400点魔法值",
                Item.Rarity.Rarity_2,
                99, 400,
                ConsumableItem.EffectType.HEAL_MP,
                400, 0
        );
    }

    public static Item createMp2_4_Canfengyinlu() {
        return new ConsumableItem(
                102024, "餐风饮露", "恢复450点魔法值",
                Item.Rarity.Rarity_2,
                99, 450,
                ConsumableItem.EffectType.HEAL_MP,
                450, 0
        );
    }

    public static Item createMp2_5_Bailuweishuang() {
        return new ConsumableItem(
                102025, "白露为霜", "恢复500点魔法值",
                Item.Rarity.Rarity_2,
                99, 500,
                ConsumableItem.EffectType.HEAL_MP,
                500, 0
        );
    }

    //------------------ 增益药品-气血 ----------------------

    public static Item createGain_hp_1() {
        return new ConsumableItem(
                103011, "初级气血丹", "临时增加500点气血，持续30分钟",
                Item.Rarity.Rarity_1,
                99, 1000,
                ConsumableItem.EffectType.BUFF_HP,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_2() {
        return new ConsumableItem(
                103012, "中级气血丹", "临时增加1000点气血，持续30分钟",
                Item.Rarity.Rarity_2,
                99, 2000,
                ConsumableItem.EffectType.BUFF_HP,
                1000, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_3() {
        return new ConsumableItem(
                103013, "高级气血丹", "临时增加1500点气血，持续30分钟",
                Item.Rarity.Rarity_3,
                99, 3000,
                ConsumableItem.EffectType.BUFF_HP,
                1500, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_4() {
        return new ConsumableItem(
                103014, "特级气血丹", "临时增加2000点气血，持续30分钟",
                Item.Rarity.Rarity_4,
                99, 4000,
                ConsumableItem.EffectType.BUFF_HP,
                2000, 30 * 60 * 1000
        );
    }

    public static Item createGain_hp_5() {
        return new ConsumableItem(
                103015, "极品气血丹", "临时增加3000点气血，持续30分钟",
                Item.Rarity.Rarity_5,
                99, 6000,
                ConsumableItem.EffectType.BUFF_HP,
                3000, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-伤害 ----------------------

    public static Item createGain_attack_1() {
        return new ConsumableItem(
                103021, "初级攻击丹", "临时增加100点伤害，持续30分钟",
                Item.Rarity.Rarity_1,
                99, 1000,
                ConsumableItem.EffectType.BUFF_HP,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_2() {
        return new ConsumableItem(
                103022, "中级攻击丹", "临时增加200点伤害，持续30分钟",
                Item.Rarity.Rarity_2,
                99, 2000,
                ConsumableItem.EffectType.BUFF_HP,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_3() {
        return new ConsumableItem(
                103023, "高级攻击丹", "临时增加300点伤害，持续30分钟",
                Item.Rarity.Rarity_3,
                99, 3000,
                ConsumableItem.EffectType.BUFF_HP,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_4() {
        return new ConsumableItem(
                103024, "特级攻击丹", "临时增加500点伤害，持续30分钟",
                Item.Rarity.Rarity_4,
                99, 4000,
                ConsumableItem.EffectType.BUFF_HP,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_attack_5() {
        return new ConsumableItem(
                103025, "极品攻击丹", "临时增加800点伤害，持续30分钟",
                Item.Rarity.Rarity_5,
                99, 6000,
                ConsumableItem.EffectType.BUFF_HP,
                800, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-防御 ----------------------

    public static Item createGain_defense_1() {
        return new ConsumableItem(
                103031, "初级防御丹", "临时增加100点防御，持续30分钟",
                Item.Rarity.Rarity_1,
                99, 1000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_2() {
        return new ConsumableItem(
                103032, "中级防御丹", "临时增加200点防御，持续30分钟",
                Item.Rarity.Rarity_2,
                99, 2000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_3() {
        return new ConsumableItem(
                103033, "高级防御丹", "临时增加300点防御，持续30分钟",
                Item.Rarity.Rarity_3,
                99, 3000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_4() {
        return new ConsumableItem(
                103034, "特级防御丹", "临时增加500点防御，持续30分钟",
                Item.Rarity.Rarity_4,
                99, 4000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_defense_5() {
        return new ConsumableItem(
                103035, "极品防御丹", "临时增加800点防御，持续30分钟",
                Item.Rarity.Rarity_5,
                99, 6000,
                ConsumableItem.EffectType.BUFF_DEFENSE,
                800, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-灵力 ----------------------

    public static Item createGain_mana_1() {
        return new ConsumableItem(
                103041, "初级灵力丹", "临时增加100点灵力，持续30分钟",
                Item.Rarity.Rarity_1,
                99, 1000,
                ConsumableItem.EffectType.BUFF_MANA,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_2() {
        return new ConsumableItem(
                103042, "中级灵力丹", "临时增加200点灵力，持续30分钟",
                Item.Rarity.Rarity_2,
                99, 2000,
                ConsumableItem.EffectType.BUFF_MANA,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_3() {
        return new ConsumableItem(
                103043, "高级灵力丹", "临时增加300点灵力，持续30分钟",
                Item.Rarity.Rarity_3,
                99, 3000,
                ConsumableItem.EffectType.BUFF_MANA,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_4() {
        return new ConsumableItem(
                103044, "特级灵力丹", "临时增加500点灵力，持续30分钟",
                Item.Rarity.Rarity_4,
                99, 4000,
                ConsumableItem.EffectType.BUFF_MANA,
                500, 30 * 60 * 1000
        );
    }

    public static Item createGain_mana_5() {
        return new ConsumableItem(
                103045, "极品灵力丹", "临时增加800点灵力，持续30分钟",
                Item.Rarity.Rarity_5,
                99, 6000,
                ConsumableItem.EffectType.BUFF_MANA,
                800, 30 * 60 * 1000
        );
    }

    //------------------ 增益药品-速度 ----------------------

    public static Item createGain_speed_1() {
        return new ConsumableItem(
                103051, "初级速度丹", "临时增加50点速度，持续30分钟",
                Item.Rarity.Rarity_1,
                99, 1000,
                ConsumableItem.EffectType.BUFF_SPEED,
                50, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_2() {
        return new ConsumableItem(
                103052, "中级速度丹", "临时增加100点速度，持续30分钟",
                Item.Rarity.Rarity_2,
                99, 2000,
                ConsumableItem.EffectType.BUFF_SPEED,
                100, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_3() {
        return new ConsumableItem(
                103053, "高级速度丹", "临时增加200点速度，持续30分钟",
                Item.Rarity.Rarity_3,
                99, 3000,
                ConsumableItem.EffectType.BUFF_SPEED,
                200, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_4() {
        return new ConsumableItem(
                103054, "特级速度丹", "临时增加300点速度，持续30分钟",
                Item.Rarity.Rarity_4,
                99, 4000,
                ConsumableItem.EffectType.BUFF_SPEED,
                300, 30 * 60 * 1000
        );
    }

    public static Item createGain_speed_5() {
        return new ConsumableItem(
                103055, "极品速度丹", "临时增加400点速度，持续30分钟",
                Item.Rarity.Rarity_5,
                99, 6000,
                ConsumableItem.EffectType.BUFF_SPEED,
                400, 30 * 60 * 1000
        );
    }

}

package com.game.dream.item;

import android.text.TextUtils;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.utils.EquipUtil;

/**
 * Equipment item (weapons, armor, accessories)
 */
public class EquipmentItem extends Item {
    public enum Slot {
        HELMET,     //头盔
        ACCESSORY,  //饰品
        WEAPON,     //武器
        ARMOR,      //盔甲
        BELT,       //腰带
        SHOES,       //鞋子

        ;

        public static Slot getSlotWithName(String name) {
            for (Slot item : Slot.values()) {
                if (TextUtils.equals(item.name(), name)) {
                    return item;
                }
            }
            return null;
        }

        public static Slot getSlotWithIndex(int index) {
            Slot[] arr = Slot.values();
            if (index < arr.length) {
                return arr[index];
            }
            return null;
        }
    }

    private EquipItemInfo equipItemInfo;

    public EquipmentItem(int id, String name, String description, Rarity rarity,
                         int value, Slot slot, int attackBonus, int defenseBonus,
                         int speedBonus, int magicBonus) {
        super(id, name, description, Type.EQUIPMENT, rarity, 1, value);
    }

    public EquipmentItem(int id, String name) {
        super(id, name, "", Type.EQUIPMENT, Rarity.Rarity_1, 1, 100000);
    }

    public EquipmentItem(EquipItemInfo equipItemInfo) {
        super(equipItemInfo.getId(), equipItemInfo.getName(), "", Type.EQUIPMENT, Rarity.Rarity_1, 1, 100000);
        this.equipItemInfo = equipItemInfo;

        Item.Rarity rarity = EquipUtil.caculateEquipRarity(equipItemInfo);
        equipItemInfo.setRatity(rarity.name());
        this.rarity = rarity;
    }

    public EquipItemInfo getEquipItemInfo() {
        return equipItemInfo;
    }

    public Slot getSlot() {
        Slot slot = null;
        if (equipItemInfo != null) {
            switch (equipItemInfo.getEquipType()) {
                case 1:
                    slot = Slot.HELMET;
                    break;
                case 2:
                    slot = Slot.ACCESSORY;
                    break;
                case 3:
                    slot = Slot.WEAPON;
                    break;
                case 4:
                    slot = Slot.ARMOR;
                    break;
                case 5:
                    slot = Slot.BELT;
                    break;
                case 6:
                    slot = Slot.SHOES;
                    break;
            }
        }
        return slot;
    }
}

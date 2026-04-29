package com.game.dream.item;

import com.game.dream.bean.EquipItemInfo;

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

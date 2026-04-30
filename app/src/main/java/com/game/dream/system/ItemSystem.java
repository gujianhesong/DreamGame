package com.game.dream.system;

import com.game.dream.FloatingText;
import com.game.dream.GameEngine;
import com.game.dream.LogUtil;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.item.ConsumableItem;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ItemSystem {

    private static ItemSystem instance = new ItemSystem();

    public static ItemSystem getInstance() {
        return instance;
    }

    private List<ItemStack> items;
    private int maxSize;

    // Equipment slots
    private EquipmentItem helmet;
    private EquipmentItem accessory;
    private EquipmentItem weapon;
    private EquipmentItem armor;
    private EquipmentItem belt;
    private EquipmentItem shoes;

    private WeakReference<GameEngine> gameEngineWeakReference;

    private ItemSystem() {
        this.maxSize = 500;
        this.items = new ArrayList<>();
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngineWeakReference = new WeakReference<>(gameEngine);
    }

    public List<ItemInfo> getItemInfos() {
        ArrayList<ItemInfo> itemInfos = new ArrayList<>();
        for (ItemStack itemStack : items) {
            Item item = itemStack.getItem();
            if (item instanceof EquipmentItem) {
                EquipmentItem equipmentItem = (EquipmentItem) item;
                itemInfos.add(equipmentItem.getEquipItemInfo());
            } else {
                itemInfos.add(new ItemInfo(item.getId(), item.getName(), item.getType().name(), itemStack.getQuantity()));
            }
        }
        return itemInfos;
    }

    public void setItemInfos(List<ItemInfo> itemInfos) {
        items.clear();
        if (itemInfos != null) {
            for (ItemInfo itemInfo : itemInfos) {
                Item item = ItemCreator.createItemWithInfo(itemInfo);
                if (item != null) {
                    items.add(new ItemStack(item, itemInfo.getAmount()));
                } else {
                    LogUtil.i("无法创建item, itemId:" + itemInfo.getId() + ", " + itemInfo.getName());
                }
            }
        }
    }

    public List<EquipItemInfo> getEquipInfos() {
        ArrayList<EquipItemInfo> itemInfos = new ArrayList<>();
        if (helmet != null) {
            itemInfos.add(helmet.getEquipItemInfo());
        } else {
            itemInfos.add(null);
        }

        if (accessory != null) {
            itemInfos.add(accessory.getEquipItemInfo());
        } else {
            itemInfos.add(null);
        }

        if (weapon != null) {
            itemInfos.add(weapon.getEquipItemInfo());
        } else {
            itemInfos.add(null);
        }

        if (armor != null) {
            itemInfos.add(armor.getEquipItemInfo());
        } else {
            itemInfos.add(null);
        }

        if (belt != null) {
            itemInfos.add(belt.getEquipItemInfo());
        } else {
            itemInfos.add(null);
        }

        if (shoes != null) {
            itemInfos.add(shoes.getEquipItemInfo());
        } else {
            itemInfos.add(null);
        }

        return itemInfos;
    }

    public void setEquipInfos(List<EquipItemInfo> equipInfos) {
        if (equipInfos != null) {
            int equipInfoSize = equipInfos.size();
            if (equipInfoSize > 0 && equipInfos.get(0) != null) {
                helmet = new EquipmentItem(equipInfos.get(0));
            }
            if (equipInfoSize > 1 && equipInfos.get(1) != null) {
                accessory = new EquipmentItem(equipInfos.get(1));
            }
            if (equipInfoSize > 2 && equipInfos.get(2) != null) {
                weapon = new EquipmentItem(equipInfos.get(2));
            }
            if (equipInfoSize > 3 && equipInfos.get(3) != null) {
                armor = new EquipmentItem(equipInfos.get(3));
            }
            if (equipInfoSize > 4 && equipInfos.get(4) != null) {
                belt = new EquipmentItem(equipInfos.get(4));
            }
            if (equipInfoSize > 5 && equipInfos.get(5) != null) {
                shoes = new EquipmentItem(equipInfos.get(5));
            }
        }
    }

    /**
     * Add item to inventory
     *
     * @return true if item was added successfully
     */
    public boolean addItem(Item item, int quantity) {
        // Try to stack with existing items first
        for (ItemStack stack : items) {
            if (stack.getItem().getId() == item.getId() && !stack.isFull()) {
                int remaining = stack.add(quantity);
                if (remaining == 0) {
                    return true; // Fully added
                }
                quantity = remaining;
            }
        }

        // If still have items to add and inventory has space
        if (quantity > 0 && items.size() < maxSize) {
            items.add(new ItemStack(item, quantity));
            return true;
        }

        return false; // Inventory full
    }

    /**
     * Remove item from inventory
     *
     * @return actual amount removed
     */
    public int removeItem(int itemId, int quantity) {
        int totalRemoved = 0;

        for (int i = items.size() - 1; i >= 0; i--) {
            ItemStack stack = items.get(i);
            if (stack.getItem().getId() == itemId) {
                int removed = stack.remove(quantity - totalRemoved);
                totalRemoved += removed;

                if (stack.isEmpty()) {
                    items.remove(i);
                }

                if (totalRemoved >= quantity) {
                    break;
                }
            }
        }

        return totalRemoved;
    }

    /**
     * Use consumable item at index
     */
    public boolean useItem(int index) {
        if (index < 0 || index >= items.size()) return false;

        ItemStack stack = items.get(index);
        Item item = stack.getItem();

        if (item.getType() != Item.Type.CONSUMABLE) return false;

        // Use the item
        boolean isUsed = false;
        if (item instanceof ConsumableItem) {
            ConsumableItem consumableItem = (ConsumableItem) item;
            switch (consumableItem.getEffectType()) {
                case HEAL_HP: {
                    RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                    roleInfo.setHp(Math.min(roleInfo.getBloodCap(), roleInfo.getHp() + consumableItem.getEffectValue()));
                    if (gameEngineWeakReference != null && gameEngineWeakReference.get() != null) {
                        gameEngineWeakReference.get().showFloatText("气血+" + consumableItem.getEffectValue(), FloatingText.Type.HEAL);
                    }
                    isUsed = true;
                }
                break;
                case HEAL_MP: {
                    RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                    roleInfo.setMp(Math.min(roleInfo.getMagicCap(), roleInfo.getMp() + consumableItem.getEffectValue()));
                    if (gameEngineWeakReference != null && gameEngineWeakReference.get() != null) {
                        gameEngineWeakReference.get().showFloatText("魔法+" + consumableItem.getEffectValue(), FloatingText.Type.HEAL_MAGIC);
                    }
                    isUsed = true;
                }
                break;
            }
        }

        if (isUsed) {
            stack.remove(1);
            if (stack.isEmpty()) {
                items.remove(index);
            }
        }

        return false;
    }

    /**
     * Equip an item
     */
    public boolean equipItem(int index) {
        if (index < 0 || index >= items.size()) return false;

        ItemStack stack = items.get(index);
        Item item = stack.getItem();

        if (item.getType() != Item.Type.EQUIPMENT) return false;

        EquipmentItem equipment = (EquipmentItem) item;

        // Unequip current item in that slot first
        unequipSlot(equipment.getSlot());

        // Equip new item
        switch (equipment.getSlot()) {
            case HELMET:
                helmet = equipment;
                break;
            case ACCESSORY:
                accessory = equipment;
                break;
            case WEAPON:
                weapon = equipment;
                break;
            case ARMOR:
                armor = equipment;
                break;
            case BELT:
                belt = equipment;
                break;
            case SHOES:
                shoes = equipment;
                break;
        }

        RoleSystem.getInstance().updateRoleEquipProperty();

        // Remove from inventory
        stack.remove(1);
        if (stack.isEmpty()) {
            items.remove(index);
        }

        return true;
    }

    /**
     * Unequip item from slot
     */
    public void unequipSlot(EquipmentItem.Slot slot) {
        EquipmentItem equipped = getEquippedItem(slot);
        if (equipped != null) {
            addItem(equipped, 1);

            switch (slot) {
                case HELMET:
                    helmet = null;
                    break;
                case ACCESSORY:
                    accessory = null;
                    break;
                case WEAPON:
                    weapon = null;
                    break;
                case ARMOR:
                    armor = null;
                    break;
                case BELT:
                    belt = null;
                    break;
                case SHOES:
                    shoes = null;
                    break;
            }

            RoleSystem.getInstance().updateRoleEquipProperty();
        }
    }

    /**
     * Get equipped item in slot
     */
    public EquipmentItem getEquippedItem(EquipmentItem.Slot slot) {
        switch (slot) {
            case HELMET:
                return helmet;
            case ACCESSORY:
                return accessory;
            case WEAPON:
                return weapon;
            case ARMOR:
                return armor;
            case BELT:
                return belt;
            case SHOES:
                return shoes;
            default:
                return null;
        }
    }

//    /**
//     * Get total attack bonus from equipment
//     */
//    public int getTotalAttackBonus() {
//        int bonus = 0;
//        if (helmet != null) bonus += helmet.getAttackBonus();
//        if (accessory != null) bonus += accessory.getAttackBonus();
//        if (weapon != null) bonus += weapon.getAttackBonus();
//        if (armor != null) bonus += armor.getAttackBonus();
//        if (belt != null) bonus += belt.getAttackBonus();
//        if (shoe != null) bonus += shoe.getAttackBonus();
//        return bonus;
//    }
//
//    /**
//     * Get total defense bonus from equipment
//     */
//    public int getTotalDefenseBonus() {
//        int bonus = 0;
//        if (helmet != null) bonus += helmet.getDefenseBonus();
//        if (accessory != null) bonus += accessory.getDefenseBonus();
//        if (weapon != null) bonus += weapon.getDefenseBonus();
//        if (armor != null) bonus += armor.getDefenseBonus();
//        if (belt != null) bonus += belt.getDefenseBonus();
//        if (shoe != null) bonus += shoe.getDefenseBonus();
//        return bonus;
//    }

    /**
     * Check if inventory has space
     */
    public boolean hasSpace() {
        return items.size() < maxSize;
    }

    /**
     * Get all items
     */
    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

    public int getSize() {
        return items.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

}

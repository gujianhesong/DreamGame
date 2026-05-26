package com.game.dream.system;

import android.text.TextUtils;

import com.game.dream.FloatingText;
import com.game.dream.GameEngine;
import com.game.dream.LogUtil;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.enums.GemtoneType;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.enums.XiLianType;
import com.game.dream.item.ConsumableItem;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.item.ItemStack;

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

    private ItemSystem() {
        this.maxSize = 500;
        this.items = new ArrayList<>();
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
                    LogUtil.i("item " + item.getId() + ", " + itemInfo.getName() + ", " + itemInfo.getAmount());
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

    public int removeItem(String itemName, int quantity) {
        ItemStack itemStack = getItemByName(itemName);
        if (itemStack != null) {
            return removeItem(itemStack.getItem().getId(), quantity);
        }
        return 0;
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
                    int effectValue = consumableItem.getEffectValue();
                    if (ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_ShenNong)) {
                        //神农
                        effectValue = (int) (effectValue * 1.2);
                    }
                    roleInfo.setHp(Math.min(roleInfo.getBloodCap(), roleInfo.getHp() + effectValue));
                    GameEngine.getInstance().showFloatText("气血+" + effectValue, FloatingText.Type.HEAL);
                    isUsed = true;
                }
                break;
                case HEAL_MP: {
                    RoleInfo roleInfo = RoleSystem.getInstance().getRoleInfo();
                    int effectValue = consumableItem.getEffectValue();
                    if (ItemSystem.getInstance().isEquipedSpecialEffect(SpecialEffect.SE_ShenNong)) {
                        //神农
                        effectValue = (int) (effectValue * 1.2);
                    }
                    roleInfo.setMp(Math.min(roleInfo.getMagicCap(), roleInfo.getMp() + effectValue));
                    GameEngine.getInstance().showFloatText("魔法+" + effectValue, FloatingText.Type.HEAL_MAGIC);
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

        //装备等级限制
        if (!isEquipHasSpecialEffect(equipment.getEquipItemInfo(), SpecialEffect.SE_WuJiBieXianZhi)
                && RoleSystem.getInstance().getRoleInfo().getLevel() < equipment.getEquipItemInfo().getLevel()) {
            GameEngine.getInstance().showCenterToast("人物等级不足，无法装备");
            return false;
        }

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

    public boolean xiangqianEquip(EquipmentItem equipment, int index, GemtoneType gemtoneType) {
        if (index < 0 || index >= items.size()) return false;

        ItemStack stack = items.get(index);
        Item item = stack.getItem();

        if (item.getType() != Item.Type.EQUIPMENT) return false;
        if (equipment != item) return false;

        EquipItemInfo equipItemInfo = equipment.getEquipItemInfo();

        if (equipItemInfo.getLevel() == 0) {
            GameEngine.getInstance().showCenterToast("0级装备无法镶嵌宝石");
            return false;
        }

        switch (equipment.getSlot()) {
            case HELMET: {
                // 头盔可镶嵌太阳石，红玛瑙
                if (!(gemtoneType == GemtoneType.GT_TaiYangShi || gemtoneType == GemtoneType.GT_HoneMaNao)) {
                    GameEngine.getInstance().showCenterToast("头盔只能镶嵌太阳石和红玛瑙");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_TaiYangShi
                        && equipItemInfo.getAttackStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级太阳石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_HoneMaNao
                        && equipItemInfo.getHitStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级红玛瑙");
                    return false;
                }
            }
            break;
            case ACCESSORY: {
                // 项链可镶嵌舍利子，蓝宝石
                if (!(gemtoneType == GemtoneType.GT_SheLiZi || gemtoneType == GemtoneType.GT_LanBaoShi)) {
                    GameEngine.getInstance().showCenterToast("头盔只能镶嵌舍利子和蓝宝石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_SheLiZi
                        && equipItemInfo.getManaStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级舍利子");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_LanBaoShi
                        && equipItemInfo.getMpStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级蓝宝石");
                    return false;
                }
            }
            break;
            case WEAPON: {
                // 武器可镶嵌太阳石，舍利子
                if (!(gemtoneType == GemtoneType.GT_TaiYangShi || gemtoneType == GemtoneType.GT_SheLiZi)) {
                    GameEngine.getInstance().showCenterToast("头盔只能镶嵌太阳石和舍利子");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_TaiYangShi
                        && equipItemInfo.getAttackStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级太阳石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_SheLiZi
                        && equipItemInfo.getManaStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级舍利子");
                    return false;
                }
            }
            break;
            case ARMOR: {
                // 铠甲可镶嵌月亮石，光芒石
                if (!(gemtoneType == GemtoneType.GT_YueLiangShi || gemtoneType == GemtoneType.GT_GuangMangShi)) {
                    GameEngine.getInstance().showCenterToast("头盔只能镶嵌月亮石和光芒石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_YueLiangShi
                        && equipItemInfo.getDefenseStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级月亮石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_GuangMangShi
                        && equipItemInfo.getHpStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级光芒石");
                    return false;
                }
            }
            break;
            case BELT: {
                // 腰带可镶嵌光芒石，黑宝石
                if (!(gemtoneType == GemtoneType.GT_GuangMangShi || gemtoneType == GemtoneType.GT_HeiBaoShi)) {
                    GameEngine.getInstance().showCenterToast("头盔只能镶嵌光芒石和黑宝石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_GuangMangShi
                        && equipItemInfo.getHpStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级光芒石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_HeiBaoShi
                        && equipItemInfo.getSpeedStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级黑宝石");
                    return false;
                }
            }
            break;
            case SHOES: {
                // 鞋子可镶嵌黑宝石，神秘石
                if (!(gemtoneType == GemtoneType.GT_HeiBaoShi || gemtoneType == GemtoneType.GT_ShenMiShi)) {
                    GameEngine.getInstance().showCenterToast("头盔只能镶嵌黑宝石和神秘石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_HeiBaoShi
                        && equipItemInfo.getSpeedStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级黑宝石");
                    return false;
                }
                if (!isEquipHasSpecialEffect(equipItemInfo, SpecialEffect.SE_WuJiBieXianZhi)
                        && gemtoneType == GemtoneType.GT_ShenMiShi
                        && equipItemInfo.getDodgeStoneLevel() >= equipItemInfo.getLevel() / 10) {
                    GameEngine.getInstance().showCenterToast("当前非无级别装备最高只能镶嵌" + (equipItemInfo.getLevel() / 10) + "级神秘石");
                    return false;
                }
            }
            break;
        }

        switch (gemtoneType) {
            case GT_TaiYangShi: {
                int needStoneLevel = equipItemInfo.getAttackStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setAttackStoneLevel(equipItemInfo.getAttackStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
            case GT_HoneMaNao: {
                int needStoneLevel = equipItemInfo.getHitStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setHitStoneLevel(equipItemInfo.getHitStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
            case GT_SheLiZi: {
                int needStoneLevel = equipItemInfo.getManaStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setManaStoneLevel(equipItemInfo.getManaStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }

            break;
            case GT_YueLiangShi: {
                int needStoneLevel = equipItemInfo.getDefenseStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setDefenseStoneLevel(equipItemInfo.getDefenseStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
            case GT_GuangMangShi: {
                int needStoneLevel = equipItemInfo.getHpStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setHpStoneLevel(equipItemInfo.getHpStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
            case GT_HeiBaoShi: {
                int needStoneLevel = equipItemInfo.getSpeedStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setSpeedStoneLevel(equipItemInfo.getSpeedStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
            case GT_LanBaoShi: {
                int needStoneLevel = equipItemInfo.getMpStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setMpStoneLevel(equipItemInfo.getMpStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
            case GT_ShenMiShi: {
                int needStoneLevel = equipItemInfo.getDodgeStoneLevel() + 1;
                String stoneName = needStoneLevel + "级" + gemtoneType.getDesc();
                ItemStack itemStack = ItemSystem.getInstance().getItemByName(stoneName);
                if (itemStack != null && itemStack.getQuantity() > 1) {
                    equipItemInfo.setDodgeStoneLevel(equipItemInfo.getDodgeStoneLevel() + 1);
                    ItemSystem.getInstance().removeItem(stoneName, 1);
                    GameEngine.getInstance().showCenterToast(stoneName + "镶嵌成功");
                } else {
                    GameEngine.getInstance().showCenterToast("缺少" + stoneName);
                    return false;
                }
            }
            break;
        }

        return true;
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

    /**
     * 是否装备了指定特效的装备
     *
     * @param specialEffect
     * @return
     */
    public boolean isEquipedSpecialEffect(SpecialEffect specialEffect) {
        List<EquipItemInfo> equipItemInfos = ItemSystem.getInstance().getEquipInfos();
        for (EquipItemInfo item : equipItemInfos) {
            if (item != null && item.getSpecialEffects() != null) {
                for (String specialEffectItem : item.getSpecialEffects()) {
                    if (TextUtils.equals(specialEffectItem, specialEffect.name())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isEquipHasSpecialEffect(EquipItemInfo equipItemInfo, SpecialEffect specialEffect) {
        if (equipItemInfo != null && equipItemInfo.getSpecialEffects() != null) {
            for (String specialEffectItem : equipItemInfo.getSpecialEffects()) {
                if (TextUtils.equals(specialEffectItem, specialEffect.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    public float getTotalXiLianPropWithAllEquiped(XiLianType xiLianType) {
        List<EquipItemInfo> equipItemInfos = ItemSystem.getInstance().getEquipInfos();
        float value = 0f;
        for (EquipItemInfo item : equipItemInfos) {
            if (item != null) {
                switch (xiLianType) {
                    case XL_attackCritRatio:
                        value += item.getAttackCritRatio();
                        break;
                    case XL_magicCritRatio:
                        value += item.getMagicCritRatio();
                        break;
                    case XL_attackSpeedRatio:
                        value += item.getAttackSpeedRatio();
                        break;
                    case XL_magicSpeedRatio:
                        value += item.getMagicSpeedRatio();
                        break;
                    case XL_attackValueRatio:
                        value += item.getAttackValueRatio();
                        break;
                    case XL_magicValueRatio:
                        value += item.getMagicValueRatio();
                        break;
                    case XL_beAttackedValueRatio:
                        value += item.getBeAttackedValueRatio();
                        break;
                    case XL_beMagicedValueRatio:
                        value += item.getBeMagicedValueRatio();
                        break;
                }
            }
        }
        return value;
    }

    public int getItemCountByName(String name) {
        int count = 0;
        for (ItemStack stack : items) {
            if (stack.getItem().getName().equals(name)) {
                count += stack.getQuantity();
            }
        }
        return count;
    }

    public ItemStack getItemByName(String name) {
        for (ItemStack stack : items) {
            if (stack.getItem().getName().equals(name)) {
                return stack;
            }
        }
        return null;
    }
}

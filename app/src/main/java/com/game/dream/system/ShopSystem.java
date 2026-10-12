package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.enums.FoodType;
import com.game.dream.enums.GemtoneType;
import com.game.dream.enums.SkillType;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.Npc;
import com.game.dream.panel.ShopPanel;
import com.game.dream.ui.DialogBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopSystem {

    private static ShopSystem instance = new ShopSystem();

    public static ShopSystem getInstance() {
        return instance;
    }

    private ShopSystem() {
    }

    public boolean handleNpcClick(Npc npc) {
        switch (npc.getId()) {
            case 100151:
                openShop(npc, "客官，你要点什么药", buildQingxiMedicine());
                return true;
            case 100155:
            case 100265:
                openShop(npc, "缺什么杂货尽管开口，捕捉符这里有售。", buildGrocer());
                return true;
            case 100152:
            case 100256:
                openShop(npc, "客官，你想吃点什么，我这里都有", buildFood());
                return true;
            case 100250:
                openShop(npc, "回春堂药材齐全，城里伤药比村里更全。", buildJinlingMedicine());
                return true;
            case 100252:
                openShop(npc, "精铁、洗炼石，打造成套材料这里都有。", buildBlacksmith());
                return true;
            case 100253:
                openShop(npc, "龙泉剑庄，武器制造书与成品兵器。", buildWeaponShop());
                return true;
            case 100254:
                openShop(npc, "铁甲坊，防具制造书与成品铠甲。", buildArmorShop());
                return true;
            case 100255:
                openShop(npc, "璀璨珠宝，镶嵌用的宝石这里有售。", buildGemShop());
                return true;
            case 100263:
                openShop(npc, "贫僧贩卖诸门心法，择一部带回去研习便可。", buildSkillBooks());
                return true;
            default:
                return false;
        }
    }

    private void openShop(Npc npc, String msg, List<ShopPanel.ShopItem> items) {
        GameEngine.getInstance().showDialog(npc.getName(), msg, Arrays.asList("看看都有啥", "离开"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().showShopPanel(items);
                        }
                    }
                });
    }

    private List<ShopPanel.ShopItem> buildQingxiMedicine() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        addMedicines(shopItems, new int[]{101011, 101012, 101013, 101014}, true);
        addMedicines(shopItems, new int[]{102011, 102012, 102013, 102014}, false);
        addItem(shopItems, ItemCreator.createGain_hp_1());
        addItem(shopItems, ItemCreator.createGain_attack_1());
        addItem(shopItems, ItemCreator.createGain_defense_1());
        addItem(shopItems, ItemCreator.createGain_mana_1());
        addItem(shopItems, ItemCreator.createGain_speed_1());
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildJinlingMedicine() {
        List<ShopPanel.ShopItem> shopItems = buildQingxiMedicine();
        addMedicines(shopItems, new int[]{101021, 101022, 101023, 101024, 101025}, true);
        addMedicines(shopItems, new int[]{102021, 102022, 102023, 102024, 102025}, false);
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildFood() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        for (FoodType foodType : FoodType.values()) {
            addItem(shopItems, ItemCreator.createCookFood(foodType));
        }
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildBlacksmith() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        int[] levels = {10, 20, 30, 40, 50, 60};
        for (int level : levels) {
            addItem(shopItems, ItemCreator.createBuildEquipIron(level));
            addItem(shopItems, ItemCreator.createXiLianStone(level));
        }
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildWeaponShop() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        int[] levels = {10, 20, 30, 40, 50};
        for (int level : levels) {
            addItem(shopItems, ItemCreator.createBuildEquipBook(level, EquipmentItem.Slot.WEAPON));
            addItem(shopItems, ItemCreator.createBuildEquipIron(level));
        }
        addItem(shopItems, EquipCreator.createEquip(10, EquipmentItem.Slot.WEAPON));
        addItem(shopItems, EquipCreator.createEquip(20, EquipmentItem.Slot.WEAPON));
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildArmorShop() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        int[] levels = {10, 20, 30, 40};
        for (int level : levels) {
            addItem(shopItems, ItemCreator.createBuildEquipBook(level, EquipmentItem.Slot.ARMOR));
            addItem(shopItems, ItemCreator.createBuildEquipBook(level, EquipmentItem.Slot.HELMET));
            addItem(shopItems, ItemCreator.createBuildEquipBook(level, EquipmentItem.Slot.BELT));
            addItem(shopItems, ItemCreator.createBuildEquipBook(level, EquipmentItem.Slot.SHOES));
        }
        addItem(shopItems, EquipCreator.createEquip(10, EquipmentItem.Slot.ARMOR));
        addItem(shopItems, EquipCreator.createEquip(10, EquipmentItem.Slot.HELMET));
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildGemShop() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        int[] levels = {1, 5, 8};
        for (GemtoneType type : GemtoneType.values()) {
            for (int level : levels) {
                addItem(shopItems, ItemCreator.createGemstone(type, level));
            }
        }
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildSkillBooks() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        SkillType[] types = {
                SkillType.MAIN_FIREBALL, SkillType.MAIN_ICE_BOLT, SkillType.MAIN_LIGHTNING,
                SkillType.MAIN_ChongCi,
                SkillType.MAIN_FeiShaZouShi, SkillType.MAIN_ROOT, SkillType.MAIN_WanJianGuiZong,
                SkillType.MAIN_LianQiHuaShen, SkillType.MAIN_DuWuZhen
        };
        for (SkillType type : types) {
            Item book = ItemCreator.createSkillBook(type);
            int price = type == SkillType.MAIN_ChongCi ? 18000 : Math.max(12000, book.getValue() * 3);
            shopItems.add(new ShopPanel.ShopItem(book, price));
        }
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildGrocer() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        shopItems.add(new ShopPanel.ShopItem(ItemCreator.createCaptureCharm(), 500));
        return shopItems;
    }

    private void addMedicines(List<ShopPanel.ShopItem> shopItems, int[] ids, boolean hp) {
        for (int id : ids) {
            Item item = hp ? ItemCreator.createMedicineHp(id) : ItemCreator.createMedicineMp(id);
            addItem(shopItems, item);
        }
    }

    private void addItem(List<ShopPanel.ShopItem> shopItems, Item item) {
        if (item != null) {
            shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));
        }
    }
}

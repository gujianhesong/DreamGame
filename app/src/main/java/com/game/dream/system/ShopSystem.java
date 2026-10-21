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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSystem {

    private static ShopSystem instance = new ShopSystem();

    public static ShopSystem getInstance() {
        return instance;
    }

    /** 每种技能书商店累计限购。6 本刚好能升到 3 级。 */
    public static final int SKILL_BOOK_SHOP_LIMIT = 6;

    private Map<String, Integer> skillBookShopBought = new HashMap<>();

    private ShopSystem() {
    }

    public Map<String, Integer> getSkillBookShopBought() {
        return new HashMap<>(skillBookShopBought);
    }

    public void setSkillBookShopBought(Map<String, Integer> bought) {
        skillBookShopBought = new HashMap<>();
        if (bought != null) {
            skillBookShopBought.putAll(bought);
        }
    }

    public int getSkillBookBought(SkillType skillType) {
        if (skillType == null) {
            return 0;
        }
        Integer count = skillBookShopBought.get(skillType.name());
        return count == null ? 0 : count;
    }

    public int getSkillBookRemaining(SkillType skillType) {
        return Math.max(0, SKILL_BOOK_SHOP_LIMIT - getSkillBookBought(skillType));
    }

    public boolean tryBuySkillBook(SkillType skillType) {
        if (skillType == null || getSkillBookRemaining(skillType) <= 0) {
            return false;
        }
        skillBookShopBought.put(skillType.name(), getSkillBookBought(skillType) + 1);
        return true;
    }

    public boolean handleNpcClick(Npc npc) {
        switch (npc.getId()) {
            case 100151:
                openShop(npc, "客官，你要点什么药", buildQingxiMedicine());
                return true;
            case 100102:
                openShop(npc, "山野之间也能淘到两本入门心法，每种限购六本。",
                        buildSkillBooks(SkillType.MAIN_ChongCi, SkillType.MAIN_FIREBALL));
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
                openShop(npc, "贫僧贩卖城中心法，每种限购六本，过了这个村便没有这店。",
                        buildSkillBooks(SkillType.MAIN_ICE_BOLT, SkillType.MAIN_ROOT,
                                SkillType.MAIN_MiaoShouHuiChun, SkillType.MAIN_LianQiHuaShen));
                return true;
            case 100307:
                openShop(npc, "海里淘来的残卷，每种限购六本。",
                        buildSkillBooks(SkillType.MAIN_LIGHTNING, SkillType.MAIN_FeiShaZouShi,
                                SkillType.MAIN_JinGangHuTi));
                return true;
            case 100509:
                openShop(npc, "阴司典籍外传，每种限购六本，阳寿未尽也可买。",
                        buildSkillBooks(SkillType.MAIN_DuWuZhen, SkillType.MAIN_WanJianGuiZong,
                                SkillType.MAIN_SwordQi));
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
        int[] materialLevels = {60, 70, 80};
        int[] materialPrices = {30000, 50000, 70000};
        for (int i = 0; i < materialLevels.length; i++) {
            Item iron = ItemCreator.createBuildEquipIron(materialLevels[i]);
            if (iron != null) {
                shopItems.add(new ShopPanel.ShopItem(iron, materialPrices[i]));
            }
        }
        addCraftBooks(shopItems, new EquipmentItem.Slot[]{EquipmentItem.Slot.WEAPON});
        addFinishedEquip(shopItems, 30, EquipmentItem.Slot.WEAPON, 10000);
        addFinishedEquip(shopItems, 40, EquipmentItem.Slot.WEAPON, 20000);
        addFinishedEquip(shopItems, 50, EquipmentItem.Slot.WEAPON, 40000);
        return shopItems;
    }

    private List<ShopPanel.ShopItem> buildArmorShop() {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        EquipmentItem.Slot[] slots = {
                EquipmentItem.Slot.ARMOR,
                EquipmentItem.Slot.HELMET,
                EquipmentItem.Slot.BELT,
                EquipmentItem.Slot.SHOES,
                EquipmentItem.Slot.ACCESSORY
        };
        addCraftBooks(shopItems, slots);
        int[] saleLevels = {30, 40, 50};
        int[] salePrices = {10000, 20000, 40000};
        for (int i = 0; i < saleLevels.length; i++) {
            for (EquipmentItem.Slot slot : slots) {
                addFinishedEquip(shopItems, saleLevels[i], slot, salePrices[i]);
            }
        }
        return shopItems;
    }

    private void addCraftBooks(List<ShopPanel.ShopItem> shopItems, EquipmentItem.Slot[] slots) {
        int[] bookLevels = {60, 70, 80};
        int[] bookPrices = {30000, 50000, 70000};
        for (int i = 0; i < bookLevels.length; i++) {
            for (EquipmentItem.Slot slot : slots) {
                Item book = ItemCreator.createBuildEquipBook(bookLevels[i], slot);
                if (book != null) {
                    shopItems.add(new ShopPanel.ShopItem(book, bookPrices[i]));
                }
            }
        }
    }

    private void addFinishedEquip(List<ShopPanel.ShopItem> shopItems, int level, EquipmentItem.Slot slot, int price) {
        EquipmentItem equip = EquipCreator.createEquip(level, slot);
        if (equip != null) {
            shopItems.add(new ShopPanel.ShopItem(equip, price));
        }
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

    private List<ShopPanel.ShopItem> buildSkillBooks(SkillType... types) {
        List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
        for (SkillType type : types) {
            Item book = ItemCreator.createSkillBook(type);
            if (book == null) {
                continue;
            }
            int price = 30000;
            ShopPanel.ShopItem shopItem = new ShopPanel.ShopItem(book, price);
            shopItem.stockRemaining = getSkillBookRemaining(type);
            shopItems.add(shopItem);
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

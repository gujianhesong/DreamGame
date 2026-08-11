package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.enums.FoodType;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.Npc;
import com.game.dream.panel.ShopPanel;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商店系统
 */
public class ShopSystem {

    private static ShopSystem instance = new ShopSystem();

    public static ShopSystem getInstance() {
        return instance;
    }


    private ShopSystem() {
    }

    public boolean handleNpcClick(Npc npc) {
        //清溪-药店老板
        if (npc.getId() == 100151) {
            handleNpcClick_getMedicine(npc);
            return true;
        }
        //清溪-酒馆老板
        if (npc.getId() == 100152) {
            handleNpcClick_getFood(npc);
            return true;
        }
        return false;
    }

    private void handleNpcClick_getFood(Npc npc) {
        //String msg = "走过路过，不要错过，我这里有好东西咧，快过来瞧瞧";
        String msg = "客官，你想吃点什么，我这里都有";
        GameEngine.getInstance().showDialog(npc.getName(), msg, Arrays.asList("看看都有啥", "离开"), new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                if (optionIndex == 0) {
                    List<ShopPanel.ShopItem> shopItems = new ArrayList<>();
                    for (FoodType foodType : FoodType.values()) {
                        Item foodItem = ItemCreator.createCookFood(foodType);
                        shopItems.add(new ShopPanel.ShopItem(foodItem, foodItem.getValue()));
                    }
                    GameEngine.getInstance().showShopPanel(shopItems);
                }
            }
        });
    }

    private void handleNpcClick_getMedicine(Npc npc) {
        String msg = "客官，你要点什么药";
        GameEngine.getInstance().showDialog(npc.getName(), msg, Arrays.asList("看看都有啥", "离开"), new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                if (optionIndex == 0) {
                    List<ShopPanel.ShopItem> shopItems = new ArrayList<>();

                    //药品
                    int[] hpIds = {101011, 101012, 101013, 101014};
                    int[] mpIds = {102011, 102012, 102013, 102014};

                    for (int id : hpIds) {
                        Item item = ItemCreator.createMedicineHp(id);
                        shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));
                    }
                    for (int id : mpIds) {
                        Item item = ItemCreator.createMedicineMp(id);
                        shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));
                    }

                    Item item = ItemCreator.createGain_hp_1();
                    shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));

                    item = ItemCreator.createGain_attack_1();
                    shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));

                    item = ItemCreator.createGain_defense_1();
                    shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));

                    item = ItemCreator.createGain_mana_1();
                    shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));

                    item = ItemCreator.createGain_speed_1();
                    shopItems.add(new ShopPanel.ShopItem(item, item.getValue()));

                    GameEngine.getInstance().showShopPanel(shopItems);
                }
            }
        });
    }
}

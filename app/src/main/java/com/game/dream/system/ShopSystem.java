package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.npc.Npc;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;

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
        //清溪-云游商人
        if (npc.getId() == 100102) {
            handleNpcClick_getFood(npc);
            return true;
        }
        return false;
    }

    private void handleNpcClick_getFood(Npc npc) {
        String msg = "走过路过，不要错过，我这里有好东西咧，快过来瞧瞧";
        GameEngine.getInstance().showDialog("云游商人", msg, Arrays.asList("看看都有啥", "离开"), new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                if (optionIndex == 0) {
                    GameEngine.getInstance().showShopPanel();
                }
                // optionIndex == 1: 离开，什么都不做
            }
        });
    }
}

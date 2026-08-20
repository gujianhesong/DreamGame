package com.game.dream.npc;

import com.game.dream.GameEngine;
import com.game.dream.system.MapSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;
import java.util.List;

public class FunctionNpcManager {

    private static FunctionNpcManager instance = new FunctionNpcManager();

    public static FunctionNpcManager getInstance() {
        return instance;
    }

    private FunctionNpcManager() {
    }

    public boolean handleNpcClick(Npc npc) {
        switch (npc.getId()) {
            case 100101: {
                // 清溪村村长 - 迷宫入口 / 金陵入口
                List<String> options = Arrays.asList("探索迷宫", "前往金陵", "不了");
                String message = "少侠想去哪里？村外有一处迷雾迷宫，另外南方有一座繁华的大城金陵，也可以去闯荡一番。";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_QING_XI_MAZE);
                        } else if (optionIndex == 1) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_JIN_LING);
                        }
                    }
                });
                return true;
            }
            case 100153: {
                //清溪-妙手郎中
                int costMoney = 500;
                List<String> options = Arrays.asList("我要疗伤", "暂时不用");
                String message = "少侠可要疗伤，诊费" + costMoney;
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {

                        if(RoleSystem.getInstance().getRoleInfo().getMoney() < costMoney){
                            return;
                        }

                        RoleSystem.getInstance().getRoleInfo().setMoney(RoleSystem.getInstance().getRoleInfo().getMoney() - costMoney);
                        RoleSystem.getInstance().getRoleInfo().setHp(RoleSystem.getInstance().getRoleInfo().getBloodCap());
                        RoleSystem.getInstance().getRoleInfo().setMp(RoleSystem.getInstance().getRoleInfo().getMagicCap());

                        GameEngine.getInstance().showDialog(npc.getName(), "好了，少侠已经完全恢复了");
                    }
                });
            }
        }

        return false;
    }
}

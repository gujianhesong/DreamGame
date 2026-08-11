package com.game.dream.npc;

import com.game.dream.GameEngine;
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

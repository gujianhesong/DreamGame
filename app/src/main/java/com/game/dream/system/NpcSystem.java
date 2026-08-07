package com.game.dream.system;

import android.graphics.Color;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.enums.FoodType;
import com.game.dream.enums.NpcType;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.AnimalNpc;
import com.game.dream.npc.Npc;
import com.game.dream.quest.SideQuestManager;
import com.game.dream.system.QuestSystem;
import com.game.dream.ui.DialogBox;
import com.game.dream.ui.CenterNotification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NpcSystem {

    private static NpcSystem instance = new NpcSystem();

    public static NpcSystem getInstance() {
        return instance;
    }

    private HashMap<Integer, List<Npc>> npcMap = new HashMap<>();

    private NpcSystem() {
    }

    public List<Npc> getMapNpcList(int mapId) {
        List<Npc> npcList = npcMap.get(mapId);
        if (npcList == null) {
            npcList = loadMapNpcList(mapId);
            npcMap.put(mapId, npcList);
        }
        return npcList;
    }

    private List<Npc> loadMapNpcList(int mapId) {
        List<Npc> npcList = new ArrayList<>();
        switch (mapId) {
            case 1001: {
                //清溪
                npcList.add(new Npc(100101, "青溪村村长", NpcType.OLD_MAN, 5200, 5300));
                npcList.add(new Npc(100102, "云游商人", NpcType.MERCHANT, 5000, 5300));
                npcList.add(new Npc(100103, "小虎子", NpcType.CHILD_BOY, 4800, 5300));
                // 小虎子有任务
                npcList.get(2).setHasQuest(true);
                npcList.add(new Npc(100104, "小花", NpcType.CHILD_GIRL, 4600, 5300));
                npcList.add(new Npc(100105, "赵大哥", NpcType.MAN, 4400, 5300));
                npcList.add(new Npc(100106, "李婶", NpcType.WOMAN, 4200, 5300));
                npcList.add(new Npc(100107, "小安子", NpcType.SERVANT, 4000, 5300));

                npcList.add(new Npc(100111, "清风道长", NpcType.TAOIST, 5200, 5100));
                npcList.add(new Npc(100112, "慧能大师", NpcType.MONK, 5000, 5100));
                npcList.add(new Npc(100113, "王护卫", NpcType.SOLDIER, 4800, 5100));
                npcList.add(new Npc(100114, "张大伯", NpcType.FARMER, 4600, 5100));
                npcList.add(new Npc(100115, "李猎户", NpcType.HUNTER, 4400, 5100));
                npcList.add(new Npc(100116, "黑风", NpcType.BANDIT, 4200, 5100));
                npcList.add(new Npc(100117, "驿站车夫", NpcType.COACHMAN, 4000, 5100));

                npcList.add(new Npc(100121, "老丐", NpcType.BEGGAR, 5200, 5500));
                npcList.add(new Npc(100122, "赵六", NpcType.GAMBLER, 5000, 5500));
                npcList.add(new Npc(100123, "柳公子", NpcType.SCHOLAR, 4800, 5500));
                npcList.add(new Npc(100124, "小翠", NpcType.MAID, 4600, 5500));
                npcList.add(new Npc(100125, "灵儿", NpcType.GIRL, 4400, 5500));
                npcList.add(new Npc(100126, "苏姑娘", NpcType.BEAUTY, 4200, 5500));

                npcList.add(new AnimalNpc(100191, "大公鸡", NpcType.CHICKEN, 5200, 5700));
                npcList.add(new AnimalNpc(100192, "小鸭子", NpcType.DUCK, 5000, 5700));
                npcList.add(new AnimalNpc(100193, "旺财", NpcType.DOG, 4800, 5700));
                npcList.add(new AnimalNpc(100194, "绵羊", NpcType.SHEEP, 4600, 5700));
                npcList.add(new AnimalNpc(100195, "老黄牛", NpcType.COW, 4400, 5700));
                npcList.add(new AnimalNpc(100196, "骏马", NpcType.HORSE, 4200, 5700));

            }
            break;
        }
        return npcList;
    }

    public void startConversation(Npc npc) {
        boolean handle = SideQuestManager.getInstance().handleQuestConversation(npc);
        if (handle) {
            return;
        }

        handle = ShopSystem.getInstance().handleNpcClick(npc);
        if (handle) {
            return;
        }

        // 默认对话
        /*List<String> options = Arrays.asList("你好", "有什么事吗？", "再见");
        String message = "欢迎来到青溪村！\n最近村外妖兽横行，少侠可要当心。";
        GameEngine.getInstance().showDialog(null, message, options, new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {

            }
        });*/
    }


}

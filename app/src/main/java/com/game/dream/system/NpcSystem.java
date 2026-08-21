package com.game.dream.system;

import com.game.dream.enums.NpcType;
import com.game.dream.npc.AnimalNpc;
import com.game.dream.npc.FunctionNpcManager;
import com.game.dream.npc.Npc;
import com.game.dream.quest.SideQuestManager;

import java.util.ArrayList;
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
            case MapSystem.MAP_ID_QING_XI: {
                //清溪
                npcList.add(new Npc(100151, "药店老板", NpcType.PHARMACIST, 4000, 4000));
                npcList.add(new Npc(100152, "酒馆老板", NpcType.TAVERN_KEEPER, 4200, 4000));
                npcList.add(new Npc(100153, "妙手郎中", NpcType.DOCTOR, 4400, 4000));

                npcList.add(new Npc(100101, "青溪村村长", NpcType.OLD_MAN, 5200, 5300));
                npcList.add(new Npc(100102, "云游商人", NpcType.MERCHANT, 5000, 5300));
                npcList.add(new Npc(100103, "小虎子", NpcType.CHILD_BOY, 4800, 5300));
                npcList.add(new Npc(100104, "小花", NpcType.CHILD_GIRL, 4600, 5300));
                npcList.add(new Npc(100105, "赵大哥", NpcType.MAN, 4400, 5300));
                npcList.add(new Npc(100106, "李婶", NpcType.WOMAN, 4200, 5300));
                npcList.add(new Npc(100107, "小安子", NpcType.SERVANT, 4000, 5300));

                npcList.add(new Npc(100111, "清风道长", NpcType.TAOIST, 5200, 5100));
                npcList.add(new Npc(100112, "慧能大师", NpcType.MONK, 5000, 5100));
                npcList.add(new Npc(100113, "王护卫", NpcType.SOLDIER, 4800, 5100));
                npcList.add(new Npc(100114, "张大伯", NpcType.FARMER, 4600, 5100));
                npcList.add(new Npc(100115, "李猎户", NpcType.HUNTER, 4400, 5100));
                npcList.add(new Npc(100116, "黑风强盗", NpcType.BANDIT, 4200, 5100));
                npcList.add(new Npc(100117, "驿站车夫", NpcType.COACHMAN, 3440, 5220));

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
                npcList.add(new AnimalNpc(100196, "骏马", NpcType.HORSE, 3540, 5160));

            }
            break;
            case MapSystem.MAP_ID_JIN_LING: {
                //金陵
                npcList.add(new Npc(100201, "驿站车夫", NpcType.COACHMAN, 30200, 35400));
                npcList.add(new AnimalNpc(100202, "骏马", NpcType.HORSE, 30300, 35340));

                // 四角村庄车夫
                npcList.add(new Npc(100203, "碧波渡(城东北)车夫", NpcType.COACHMAN, 58860, 2200));
                npcList.add(new Npc(100204, "云岩寨(城东南)车夫", NpcType.COACHMAN, 57150, 57800));
                npcList.add(new Npc(100205, "稻香屯(城西南)车夫", NpcType.COACHMAN, 2850, 58200));
                npcList.add(new Npc(100206, "翠微庄(城西北)车夫", NpcType.COACHMAN, 2840, 2270));

                // 碧波渡(东北)小动物 - 放在房屋网格间距和边缘margin中，避免重叠
                npcList.add(new AnimalNpc(100211, "大公鸡", NpcType.CHICKEN, 57600, 1500));
                npcList.add(new AnimalNpc(100212, "旺财", NpcType.DOG, 58400, 1500));
                npcList.add(new AnimalNpc(100213, "小鸭子", NpcType.DUCK, 57600, 2500));
                npcList.add(new AnimalNpc(100223, "绵羊", NpcType.SHEEP, 57200, 1200));
                npcList.add(new AnimalNpc(100224, "老黄牛", NpcType.COW, 58700, 2000));
                npcList.add(new AnimalNpc(100225, "骏马", NpcType.HORSE, 57200, 2800));

                // 云岩寨(东南)小动物
                npcList.add(new AnimalNpc(100214, "绵羊", NpcType.SHEEP, 57600, 57500));
                npcList.add(new AnimalNpc(100215, "大公鸡", NpcType.CHICKEN, 58400, 57500));
                npcList.add(new AnimalNpc(100216, "小鸭子", NpcType.DUCK, 57600, 58500));
                npcList.add(new AnimalNpc(100226, "旺财", NpcType.DOG, 57200, 57200));
                npcList.add(new AnimalNpc(100227, "老黄牛", NpcType.COW, 58700, 58000));
                npcList.add(new AnimalNpc(100228, "骏马", NpcType.HORSE, 57200, 58800));

                // 稻香屯(西南)小动物
                npcList.add(new AnimalNpc(100217, "老黄牛", NpcType.COW, 1600, 57500));
                npcList.add(new AnimalNpc(100218, "旺财", NpcType.DOG, 2400, 57500));
                npcList.add(new AnimalNpc(100219, "大公鸡", NpcType.CHICKEN, 1600, 58500));
                npcList.add(new AnimalNpc(100229, "绵羊", NpcType.SHEEP, 1200, 57200));
                npcList.add(new AnimalNpc(100230, "骏马", NpcType.HORSE, 2700, 58000));
                npcList.add(new AnimalNpc(100231, "小鸭子", NpcType.DUCK, 1200, 58800));

                // 翠微庄(西北)小动物
                npcList.add(new AnimalNpc(100220, "骏马", NpcType.HORSE, 1600, 1500));
                npcList.add(new AnimalNpc(100221, "绵羊", NpcType.SHEEP, 2400, 1500));
                npcList.add(new AnimalNpc(100222, "旺财", NpcType.DOG, 1600, 2500));
                npcList.add(new AnimalNpc(100232, "大公鸡", NpcType.CHICKEN, 1200, 1200));
                npcList.add(new AnimalNpc(100233, "老黄牛", NpcType.COW, 2700, 2000));
                npcList.add(new AnimalNpc(100234, "小鸭子", NpcType.DUCK, 1200, 2800));
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

        handle = FunctionNpcManager.getInstance().handleNpcClick(npc);
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

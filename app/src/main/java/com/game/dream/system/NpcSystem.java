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
                npcList.add(new Npc(100154, "装备收购商", NpcType.MERCHANT, 4600, 4000));

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

                // 金陵西侧鬼差（仅夜间出现，传送地府）
                Npc ghostClerk = new Npc(100207, "鬼差", NpcType.GHOST_CLERK, 24500, 30000);
                ghostClerk.setNightOnly(true);
                npcList.add(ghostClerk);

                // 金陵东侧车夫（传送东海湾）
                npcList.add(new Npc(100208, "东城车夫", NpcType.COACHMAN, 35500, 30000));

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

                // 城内居民区小动物（放在建筑间隙中，避免重叠）
                // 西区民居间隙：建筑 400x320，间距 200x160，间隙中心 x=25900/26500/..., y=32600/33080/...
                npcList.add(new AnimalNpc(100235, "小公鸡", NpcType.CHICKEN, 27100, 32600));
                npcList.add(new AnimalNpc(100236, "小鸭子", NpcType.DUCK, 28900, 32600));
                npcList.add(new AnimalNpc(100237, "小花狗", NpcType.DOG, 27700, 33080));
                npcList.add(new AnimalNpc(100238, "小绵羊", NpcType.SHEEP, 28300, 33560));
                // 东区民居间隙
                npcList.add(new AnimalNpc(100239, "小公鸡", NpcType.CHICKEN, 32100, 32600));
                npcList.add(new AnimalNpc(100240, "小鸭子", NpcType.DUCK, 33900, 33080));
                npcList.add(new AnimalNpc(100241, "小花狗", NpcType.DOG, 33300, 33560));
                npcList.add(new AnimalNpc(100242, "小绵羊", NpcType.SHEEP, 32700, 34040));
            }
            break;
            case MapSystem.MAP_ID_DONGHAI_BAY: {
                //东海湾
                npcList.add(new Npc(100301, "驿站车夫", NpcType.COACHMAN, 3000, 4800));
                npcList.add(new Npc(100306, "虾兵", NpcType.SOLDIER, 7000, 5000));
                npcList.add(new AnimalNpc(100302, "大公鸡", NpcType.CHICKEN, 1200, 4200));
                npcList.add(new AnimalNpc(100303, "旺财", NpcType.DOG, 800, 5500));
                npcList.add(new AnimalNpc(100304, "小鸭子", NpcType.DUCK, 5000, 5200));
                npcList.add(new AnimalNpc(100305, "绵羊", NpcType.SHEEP, 600, 4400));
            }
            break;
            case MapSystem.MAP_ID_DONGHAI_SEABED: {
                //东海海底
                npcList.add(new Npc(100401, "驿站虾兵", NpcType.SOLDIER, 10000, 13300));
                npcList.add(new Npc(100402, "龟丞相", NpcType.OLD_MAN, 10500, 10500));
                npcList.add(new Npc(100403, "龙女", NpcType.BEAUTY, 9500, 10500));
            }
            break;
            case MapSystem.MAP_ID_NETHERWORLD: {
                //地府
                // 引魂使者（返回人间入口，位于鬼门关外、奈何桥北端）
                npcList.add(new Npc(100501, "引魂使者", NpcType.GHOST_CLERK, 10320, 13400));

                // 森罗殿主殿
                npcList.add(new Npc(100502, "阎罗王", NpcType.JUDGE_YANLUO, 10000, 9500));
                npcList.add(new Npc(100503, "崔判官", NpcType.JUDGE_CUI, 10350, 9800));

                // 鬼门关内两侧（黑白无常）
                npcList.add(new Npc(100504, "黑无常", NpcType.GHOST_OFFICER_BLACK, 9750, 12600));
                npcList.add(new Npc(100505, "白无常", NpcType.GHOST_OFFICER_WHITE, 10250, 12600));

                // 鬼门关外两侧（牛头马面）
                npcList.add(new Npc(100506, "牛头", NpcType.COW_HEAD, 9600, 13250));
                npcList.add(new Npc(100507, "马面", NpcType.HORSE_FACE, 10400, 13250));

                // 奈何桥头（孟婆）
                npcList.add(new Npc(100508, "孟婆", NpcType.MENG_PO, 10000, 15500));

                // 殿前鬼差（巡视）
                npcList.add(new Npc(100511, "鬼差·甲", NpcType.GHOST_CLERK, 8500, 11500));
                npcList.add(new Npc(100512, "鬼差·乙", NpcType.GHOST_CLERK, 11500, 11500));
                npcList.add(new Npc(100513, "鬼差·丙", NpcType.GHOST_CLERK, 8500, 8500));
                npcList.add(new Npc(100514, "鬼差·丁", NpcType.GHOST_CLERK, 11500, 8500));

                // 游魂（散布在彼岸花田）
                npcList.add(new Npc(100521, "游魂", NpcType.LOST_SOUL, 14200, 10200));
                npcList.add(new Npc(100522, "游魂", NpcType.LOST_SOUL, 5800, 9800));
                npcList.add(new Npc(100523, "游魂", NpcType.LOST_SOUL, 10000, 5500));
                npcList.add(new Npc(100524, "游魂", NpcType.LOST_SOUL, 13200, 13800));
                npcList.add(new Npc(100525, "游魂", NpcType.LOST_SOUL, 6800, 13500));
                npcList.add(new Npc(100526, "游魂", NpcType.LOST_SOUL, 12500, 6500));

                // 幽魂牢（东南角封闭囚室，主角死亡后囚禁于此）
                // 牢内中心 (18800, 18800)，黑白无常守在两侧，可送主角返回人间
                npcList.add(new Npc(100531, "黑无常", NpcType.GHOST_OFFICER_BLACK, 18550, 18800));
                npcList.add(new Npc(100532, "白无常", NpcType.GHOST_OFFICER_WHITE, 19050, 18800));

                // 地狱迷宫入口（鬼门关外西侧，守护四层炼狱之门）
                npcList.add(new Npc(100540, "地狱鬼差", NpcType.GHOST_CLERK, 9300, 13400));
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

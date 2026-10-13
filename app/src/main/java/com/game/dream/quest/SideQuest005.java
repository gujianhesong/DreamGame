package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.common.Constants;
import com.game.dream.enums.NpcType;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.map.MapGenerator;
import com.game.dream.npc.AnimalNpc;
import com.game.dream.npc.Npc;
import com.game.dream.panel.MessagePanel;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
import com.game.dream.system.NpcSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 小花的小猫小橘跑到清溪村外靠近地图边缘处，找回后奖励 5 张捕捉符。
 */
public class SideQuest005 implements ISideQuest {

    public static final int QUEST_ID = 8;
    public static final int XIAO_HUA_ID = 100104;
    public static final int XIAO_JU_ID = 100197;
    private static final int CHARM_REWARD = 5;

    @Override
    public String getSideQuestName() {
        return "走失的小橘";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        if (npc.getId() == XIAO_HUA_ID) {
            handleXiaoHuaConversation(npc);
            return true;
        }
        if (npc.getId() == XIAO_JU_ID) {
            handleXiaoJuConversation(npc);
            return true;
        }
        return false;
    }

    /**
     * 读档后按任务进度刷新小橘与任务标记。
     */
    public static void syncWorld() {
        List<Npc> loaded = NpcSystem.getInstance().peekMapNpcList(MapSystem.MAP_ID_QING_XI);
        if (loaded != null) {
            List<Npc> next = new ArrayList<>(loaded);
            decorateQingxiNpcs(next);
            NpcSystem.getInstance().replaceMapNpcList(MapSystem.MAP_ID_QING_XI, next);
        }
    }

    /**
     * 清溪 NPC 列表刚创建或读档同步时，按任务进度放小橘、刷新小花叹号。
     */
    public static void decorateQingxiNpcs(List<Npc> npcList) {
        if (npcList == null) {
            return;
        }
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        boolean shouldShowCat = quest != null && quest.isActive() && quest.getStage() == 0;

        List<Npc> withoutCat = new ArrayList<>();
        Npc xiaoHua = null;
        for (Npc npc : npcList) {
            if (npc.getId() == XIAO_JU_ID) {
                continue;
            }
            if (npc.getId() == XIAO_HUA_ID) {
                xiaoHua = npc;
            }
            withoutCat.add(npc);
        }
        npcList.clear();
        npcList.addAll(withoutCat);

        if (xiaoHua != null) {
            xiaoHua.setHasQuest(quest == null || !quest.isCompleted());
        }
        if (shouldShowCat) {
            long seed = quest != null && quest.getAcceptTime() > 0
                    ? quest.getAcceptTime()
                    : System.currentTimeMillis();
            float[] pos = pickEdgePosition(seed);
            AnimalNpc cat = new AnimalNpc(XIAO_JU_ID, "小橘", NpcType.CAT, pos[0], pos[1]);
            cat.setHasQuest(true);
            npcList.add(cat);
        }
    }

    private void handleXiaoHuaConversation(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);

        if (quest != null && quest.isCompleted()) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "小橘再也不乱跑了。捕捉符要是用完了，可以去杂货商人那里买，拿去驯服村外的怪物当宠物。",
                    Arrays.asList("那就好"), null);
            npc.setHasQuest(false);
            return;
        }

        if (quest == null) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "大哥哥，我家小猫小橘不见了。村子里找遍了都没有，会不会跑到村外去了？"
                            + "我不敢出村，你能不能帮我找找？",
                    Arrays.asList("我去找", "等会儿再说"),
                    new DialogBox.DialogListener() {
                        @Override
                        public void onOptionSelected(int optionIndex) {
                            if (optionIndex == 0) {
                                acceptAndSpawn(npc);
                            }
                        }
                    });
            return;
        }

        if (quest.getStage() == 0) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "小橘还没回来……它最喜欢往远处跑，会不会已经跑到村外很靠边的地方去了？",
                    Arrays.asList("我再去找找"), null);
            return;
        }

        GameEngine.getInstance().showDialog(npc.getName(),
                "真的是小橘！谢谢大哥哥！这是我家的捕捉符，一共五张，送给你。"
                        + "捕捉符可以用来驯服怪物，把它们变成自己的宠物。",
                Arrays.asList("收下捕捉符"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        giveReward(npc);
                    }
                });
    }

    private void acceptAndSpawn(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        if (questSystem.getQuestById(QUEST_ID) == null) {
            questSystem.acceptQuest(QUEST_ID);
        }
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);
        npc.setHasQuest(true);
        spawnXiaoJuIfNeeded(quest);
        GameEngine.getInstance().showNotification("任务接取", "去清溪村外靠近地图边缘的地方寻找小橘",
                CenterNotification.Type.INFO);
        GameEngine.getInstance().showDialog(npc.getName(),
                "谢谢大哥哥！小橘一身橘色，很好认的。村外好大，它大概跑到靠边的地方去了。",
                Arrays.asList("交给我"), null);
    }

    private void handleXiaoJuConversation(Npc npc) {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest == null || quest.isCompleted() || quest.getStage() != 0) {
            GameEngine.getInstance().showDialog("小橘", "喵~", Arrays.asList("离开"), null);
            return;
        }

        GameEngine.getInstance().showDialog("小橘",
                "喵呜……（橘猫往你腿上蹭了蹭，看起来又饿又怕，像是认出来带它回家的人）",
                Arrays.asList("带它回小花身边"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        QuestSystem.getInstance().advanceQuestStage(QUEST_ID);
                        NpcSystem.getInstance().removeNpc(MapSystem.MAP_ID_QING_XI, XIAO_JU_ID);
                        GameEngine.getInstance().showNotification("找到小橘", "快把小橘带回给小花",
                                CenterNotification.Type.INFO);
                    }
                });
    }

    private void giveReward(Npc npc) {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest == null || quest.isCompleted()) {
            return;
        }
        Item charm = ItemCreator.createCaptureCharm();
        boolean added = ItemSystem.getInstance().addItem(charm, CHARM_REWARD);
        if (!added) {
            GameEngine.getInstance().showCenterToast("背包已满，请先整理后再来领取", 2000);
            return;
        }
        QuestSystem.getInstance().advanceQuestStage(QUEST_ID);
        npc.setHasQuest(false);
        GameEngine.getInstance().addMessage("获得捕捉符 x" + CHARM_REWARD, MessagePanel.MessageType.ITEM);
        GameEngine.getInstance().showNotification("任务完成", "走失的小橘完成，获得捕捉符x" + CHARM_REWARD,
                CenterNotification.Type.QUEST_COMPLETE);
        GameEngine.getInstance().showDialog(npc.getName(),
                "拿好哦。先把怪物打得差不多，再用捕捉，小橘以后会给大哥哥加油的。",
                Arrays.asList("谢谢小花"), null);
    }

    private static void spawnXiaoJuIfNeeded(QuestInfo quest) {
        List<Npc> next = new ArrayList<>(NpcSystem.getInstance().getMapNpcList(MapSystem.MAP_ID_QING_XI));
        decorateQingxiNpcs(next);
        NpcSystem.getInstance().replaceMapNpcList(MapSystem.MAP_ID_QING_XI, next);
    }

    /**
     * 在清溪地图靠近四边的地带随机一个可行走点，同一存档（接取时间）位置固定。
     */
    private static float[] pickEdgePosition(long seed) {
        Random random = new Random(seed);
        int mapW = MapSystem.MAP_WIDTH;
        int mapH = MapSystem.MAP_HEIGHT;
        int inset = 120;
        int band = 650;

        for (int attempt = 0; attempt < 48; attempt++) {
            int edge = random.nextInt(4);
            float x;
            float y;
            switch (edge) {
                case 0: // 西
                    x = inset + random.nextFloat() * band;
                    y = inset + random.nextFloat() * (mapH - inset * 2);
                    break;
                case 1: // 东
                    x = mapW - inset - random.nextFloat() * band;
                    y = inset + random.nextFloat() * (mapH - inset * 2);
                    break;
                case 2: // 北
                    x = inset + random.nextFloat() * (mapW - inset * 2);
                    y = inset + random.nextFloat() * band;
                    break;
                default: // 南
                    x = inset + random.nextFloat() * (mapW - inset * 2);
                    y = mapH - inset - random.nextFloat() * band;
                    break;
            }
            if (isWalkable(x, y)) {
                return new float[]{x, y};
            }
        }
        return new float[]{inset + 80, inset + 80};
    }

    private static boolean isWalkable(float x, float y) {
        com.game.dream.bean.MapInfo info = MapSystem.getInstance().getMapInfo(MapSystem.MAP_ID_QING_XI);
        if (info == null || info.getMapData() == null) {
            return true;
        }
        int[][] map = info.getMapData();
        int tile = Constants.TILE_SIZE;
        int tx = (int) (x / tile);
        int ty = (int) (y / tile);
        if (ty < 0 || tx < 0 || ty >= map.length || tx >= map[0].length) {
            return false;
        }
        int terrain = map[ty][tx];
        return terrain != MapGenerator.LAKE && terrain != MapGenerator.LAVA
                && terrain != MapGenerator.RIVER;
    }
}

package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.MazeToken;
import com.game.dream.npc.Npc;
import com.game.dream.panel.MessagePanel;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
import com.game.dream.system.MazeSystem;
import com.game.dream.system.NpcSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;
import java.util.List;

/**
 * 清溪村长迷雾试炼：从迷宫靠近出口的石台取回镇村令。
 */
public class SideQuest007 implements ISideQuest {

    public static final int QUEST_ID = 10;
    public static final int NPC_ID = 100101;
    private static final int EXP_REWARD = 10000;
    private static final int MAP_REWARD = 3;
    private static final String TOKEN_NAME = "镇村令";

    @Override
    public String getSideQuestName() {
        return "迷雾试炼";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        if (npc.getId() != NPC_ID) {
            return false;
        }
        handleChiefConversation(npc);
        return true;
    }

    public static void applyToQingxiNpcs(List<Npc> npcList) {
        if (npcList == null) {
            return;
        }
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        for (Npc npc : npcList) {
            if (npc.getId() == NPC_ID) {
                npc.setHasQuest(quest == null || !quest.isCompleted());
                return;
            }
        }
    }

    public static void syncWorld() {
        Npc chief = NpcSystem.getInstance().findNpc(MapSystem.MAP_ID_QING_XI, NPC_ID);
        if (chief == null) {
            return;
        }
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        chief.setHasQuest(quest == null || !quest.isCompleted());
    }

    public static void onQingxiMazeInitialized(int[][] mapData) {
        if (!shouldPlaceToken()) {
            return;
        }
        MazeSystem.getInstance().placeVillageToken(mapData);
    }

    public static boolean tryCollectToken() {
        MazeToken token = MazeSystem.getInstance().getVillageToken();
        if (token == null || token.isCollected()) {
            return false;
        }
        if (!shouldPlaceToken()) {
            return false;
        }
        Item item = ItemCreator.createZhenCunLing();
        boolean added = ItemSystem.getInstance().addItem(item, 1);
        if (!added) {
            GameEngine.getInstance().showCenterToast("背包已满，整理后再来取镇村令", 2000);
            return true;
        }
        token.setCollected(true);
        GameEngine.getInstance().addMessage("获得 " + TOKEN_NAME, MessagePanel.MessageType.ITEM);
        GameEngine.getInstance().showNotification("获得物品", "获得镇村令，回去交给村长",
                CenterNotification.Type.INFO);
        GameEngine.getInstance().showCenterToast("取得镇村令，回去交给村长", 2200);
        return true;
    }

    private static boolean shouldPlaceToken() {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest == null || !quest.isActive() || quest.isCompleted()) {
            return false;
        }
        return ItemSystem.getInstance().getItemCountByName(TOKEN_NAME) <= 0;
    }

    private void handleChiefConversation(Npc npc) {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        boolean hasToken = ItemSystem.getInstance().getItemCountByName(TOKEN_NAME) > 0;

        if (quest != null && quest.isCompleted()) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "少侠已过迷雾试炼。迷宫仍在，有胆量随时可去。",
                    Arrays.asList("探索迷宫", "告辞"),
                    mazeOrLeaveListener());
            return;
        }

        if (quest == null) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "少侠初入清溪。村外迷雾迷宫危机四伏，深处石台上有我家祖传镇村令。"
                            + "能取回来，便算过了迷雾试炼。",
                    Arrays.asList("接受试炼", "探索迷宫", "不了"),
                    new DialogBox.DialogListener() {
                        @Override
                        public void onOptionSelected(int optionIndex) {
                            if (optionIndex == 0) {
                                acceptQuest(npc);
                            } else if (optionIndex == 1) {
                                enterMaze();
                            }
                        }
                    });
            return;
        }

        if (hasToken) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "这气息……果然是镇村令。少侠把令牌交还于我，试炼便过了。",
                    Arrays.asList("交还镇村令", "探索迷宫", "不了"),
                    new DialogBox.DialogListener() {
                        @Override
                        public void onOptionSelected(int optionIndex) {
                            if (optionIndex == 0) {
                                turnInToken(npc);
                            } else if (optionIndex == 1) {
                                enterMaze();
                            }
                        }
                    });
            return;
        }

        GameEngine.getInstance().showDialog(npc.getName(),
                "镇村令在迷宫深处、靠近出口的石台上。取回再来见我。迷宫可从我这里进入。",
                Arrays.asList("探索迷宫", "我找到镇村令了", "不了"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            enterMaze();
                        } else if (optionIndex == 1) {
                            GameEngine.getInstance().showCenterToast("你还没有镇村令", 2000);
                        }
                    }
                });
    }

    private void acceptQuest(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        if (questSystem.getQuestById(QUEST_ID) == null) {
            questSystem.acceptQuest(QUEST_ID);
        }
        npc.setHasQuest(true);
        GameEngine.getInstance().showNotification("任务接取", "进入迷雾迷宫，取回镇村令",
                CenterNotification.Type.INFO);
        GameEngine.getInstance().showDialog(npc.getName(),
                "好。进迷宫后往深处走，出口附近有一座石台，令牌就在上面。活着回来才算数。",
                Arrays.asList("这就去", "稍后再去"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            enterMaze();
                        }
                    }
                });
    }

    private void turnInToken(Npc npc) {
        if (ItemSystem.getInstance().getItemCountByName(TOKEN_NAME) <= 0) {
            GameEngine.getInstance().showCenterToast("你还没有镇村令", 2000);
            return;
        }
        int freeAfterToken = ItemSystem.getInstance().getMaxSize() - ItemSystem.getInstance().getSize() + 1;
        if (freeAfterToken < MAP_REWARD) {
            GameEngine.getInstance().showCenterToast("背包空间不足，至少再空出两格以放下藏宝图", 2200);
            return;
        }
        ItemSystem.getInstance().removeItem(TOKEN_NAME, 1);
        RoleSystem.getInstance().addExperience(EXP_REWARD);
        int mapsGiven = 0;
        for (int i = 0; i < MAP_REWARD; i++) {
            Item map = ItemCreator.createTreasureMap();
            if (map != null && ItemSystem.getInstance().addItem(map, 1)) {
                mapsGiven++;
            }
        }
        completeQuest(npc);
        GameEngine.getInstance().addMessage("获得经验 " + EXP_REWARD, MessagePanel.MessageType.INFO);
        if (mapsGiven > 0) {
            GameEngine.getInstance().addMessage("获得藏宝图 x" + mapsGiven, MessagePanel.MessageType.ITEM);
        }
        GameEngine.getInstance().showNotification("任务完成",
                "迷雾试炼完成，获得" + EXP_REWARD + "经验、藏宝图x" + mapsGiven,
                CenterNotification.Type.QUEST_COMPLETE);
        GameEngine.getInstance().showDialog(npc.getName(),
                "好！少侠有胆有识。这是本村薄礼：修行心得，再加几张藏宝图。迷宫以后还想闯，随时来找我。",
                Arrays.asList("探索迷宫", "告辞"),
                mazeOrLeaveListener());
    }

    private DialogBox.DialogListener mazeOrLeaveListener() {
        return new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                if (optionIndex == 0) {
                    enterMaze();
                }
            }
        };
    }

    private void enterMaze() {
        GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_QING_XI_MAZE);
    }

    private void completeQuest(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        if (questSystem.getQuestById(QUEST_ID) == null) {
            questSystem.acceptQuest(QUEST_ID);
        }
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);
        if (quest != null && quest.isActive()) {
            while (quest.isActive()) {
                questSystem.advanceQuestStage(QUEST_ID);
            }
        }
        npc.setHasQuest(false);
    }
}

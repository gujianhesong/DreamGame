package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.Npc;
import com.game.dream.panel.MessagePanel;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
import com.game.dream.system.NpcSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;
import java.util.List;

/**
 * 清溪村老丐化缘。给 500 只道谢并继续化缘；给 5000 后推销无法使用的「如来神掌上册」。
 */
public class SideQuest006 implements ISideQuest {

    public static final int QUEST_ID = 9;
    public static final int NPC_ID = 100121;
    private static final int ALMS_SMALL = 500;
    private static final int ALMS_LARGE = 5000;
    private static final int BOOK_PRICE = 38888;

    @Override
    public String getSideQuestName() {
        return "老丐化缘";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        if (npc.getId() != NPC_ID) {
            return false;
        }
        handleBeggarConversation(npc);
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
        Npc beggar = NpcSystem.getInstance().findNpc(MapSystem.MAP_ID_QING_XI, NPC_ID);
        if (beggar == null) {
            return;
        }
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        beggar.setHasQuest(quest == null || !quest.isCompleted());
    }

    private void handleBeggarConversation(Npc npc) {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest != null && quest.isActive() && quest.getStage() >= 1 && !quest.isCompleted()) {
            showBookOffer(npc);
            return;
        }
        showAlmsDialog(npc);
    }

    private void showAlmsDialog(Npc npc) {
        GameEngine.getInstance().showDialog(npc.getName(),
                "这位少侠，行行好，给点钱吧，可怜可怜我这把老骨头。",
                Arrays.asList("给500", "给5000", "不给了"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            giveSmallAlms(npc);
                        } else if (optionIndex == 1) {
                            giveLargeAlms(npc);
                        }
                    }
                });
    }

    private void giveSmallAlms(Npc npc) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role.getMoney() < ALMS_SMALL) {
            GameEngine.getInstance().showCenterToast("金钱不足，需要" + ALMS_SMALL, 2000);
            return;
        }
        RoleSystem.getInstance().removeMoney(ALMS_SMALL);
        ensureAccepted();
        GameEngine.getInstance().showDialog(npc.getName(),
                "多谢少侠！少侠真是大好人，改日一定好好报答。",
                Arrays.asList("不客气"), null);
    }

    private void giveLargeAlms(Npc npc) {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest != null && quest.isCompleted()) {
            RoleInfo role = RoleSystem.getInstance().getRoleInfo();
            if (role.getMoney() < ALMS_LARGE) {
                GameEngine.getInstance().showCenterToast("金钱不足，需要" + ALMS_LARGE, 2000);
                return;
            }
            RoleSystem.getInstance().removeMoney(ALMS_LARGE);
            GameEngine.getInstance().showDialog(npc.getName(),
                    "多谢少侠！少侠真是大好人。",
                    Arrays.asList("不客气"), null);
            return;
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role.getMoney() < ALMS_LARGE) {
            GameEngine.getInstance().showCenterToast("金钱不足，需要" + ALMS_LARGE, 2000);
            return;
        }
        RoleSystem.getInstance().removeMoney(ALMS_LARGE);
        ensureAccepted();
        advanceToBookOffer();
        showBookOffer(npc);
    }

    private void showBookOffer(Npc npc) {
        GameEngine.getInstance().showDialog(npc.getName(),
                "哎呀！少侠这一出手，老汉就知道你绝非凡人。看你这相貌骨骼，便是万中无一的武学奇才！"
                        + "老汉深藏一本《如来神掌》，你我有缘。这无价之宝不要98888，也不要68888，只要38888，就可以带回家！",
                Arrays.asList("38888拿下", "算了我不要了", "我没这么多钱啊"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            buyBookAtPrice(npc);
                        } else if (optionIndex == 1) {
                            showUninterested(npc);
                        } else if (optionIndex == 2) {
                            showGiveAllOffer(npc);
                        }
                    }
                });
    }

    private void buyBookAtPrice(Npc npc) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role.getMoney() < BOOK_PRICE) {
            GameEngine.getInstance().showCenterToast("金钱不足，需要" + BOOK_PRICE, 2000);
            return;
        }
        if (!tryGiveBook()) {
            return;
        }
        RoleSystem.getInstance().removeMoney(BOOK_PRICE);
        completeQuest(npc);
        GameEngine.getInstance().showDialog(npc.getName(),
                "成交！少侠好眼力，这本上册你拿去好好参详。记住，真传只在口授，书里的东西嘛……呵呵。",
                Arrays.asList("我收下了"), null);
    }

    private void showGiveAllOffer(Npc npc) {
        GameEngine.getInstance().showDialog(npc.getName(),
                "没那么多也没关系，你有多少给多少，今日就当交个朋友。",
                Arrays.asList("把身上所有钱给他", "算了我不要了"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            giveAllMoneyForBook(npc);
                        } else {
                            showUninterested(npc);
                        }
                    }
                });
    }

    private void giveAllMoneyForBook(Npc npc) {
        if (!tryGiveBook()) {
            return;
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        role.setMoney(0);
        completeQuest(npc);
        GameEngine.getInstance().showDialog(npc.getName(),
                "痛快！少侠倾囊相授这份缘法，这本《如来神掌》上册便归你了。",
                Arrays.asList("我收下了"), null);
    }

    private void showUninterested(Npc npc) {
        GameEngine.getInstance().showDialog(npc.getName(),
                "小伙子不识货啊……这可是万中无一的机缘，错过可惜。",
                Arrays.asList("告辞"), null);
    }

    private boolean tryGiveBook() {
        Item book = ItemCreator.createRuLaiShenZhangVolume1();
        boolean added = ItemSystem.getInstance().addItem(book, 1);
        if (!added) {
            GameEngine.getInstance().showCenterToast("背包已满，请先整理后再来", 2000);
            return false;
        }
        GameEngine.getInstance().addMessage("获得 " + book.getName(), MessagePanel.MessageType.ITEM);
        GameEngine.getInstance().showNotification("获得物品", "获得" + book.getName(),
                CenterNotification.Type.INFO);
        return true;
    }

    private void ensureAccepted() {
        QuestSystem questSystem = QuestSystem.getInstance();
        if (questSystem.getQuestById(QUEST_ID) == null) {
            questSystem.acceptQuest(QUEST_ID);
        }
    }

    private void advanceToBookOffer() {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest != null && quest.isActive() && quest.getStage() == 0) {
            QuestSystem.getInstance().advanceQuestStage(QUEST_ID);
        }
    }

    private void completeQuest(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        ensureAccepted();
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);
        if (quest != null && quest.isActive()) {
            while (quest.isActive()) {
                questSystem.advanceQuestStage(QUEST_ID);
            }
        }
        npc.setHasQuest(false);
        GameEngine.getInstance().showNotification("任务完成", "老丐化缘完成",
                CenterNotification.Type.QUEST_COMPLETE);
    }
}

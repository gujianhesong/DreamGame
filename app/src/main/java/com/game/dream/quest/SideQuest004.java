package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.enums.SkillType;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.Npc;
import com.game.dream.panel.MessagePanel;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;

/**
 * 清溪村慧能大师化缘 5000 金币，布施后可从金刚护体、妙手回春技能书中二选一。
 */
public class SideQuest004 implements ISideQuest {

    public static final int QUEST_ID = 7;
    public static final int NPC_ID = 100112;
    private static final int ALMS = 5000;

    @Override
    public String getSideQuestName() {
        return "慧能化缘";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        if (npc.getId() != NPC_ID) {
            return false;
        }
        handleHuiNengConversation(npc);
        return true;
    }

    private void handleHuiNengConversation(Npc npc) {
        QuestInfo quest = QuestSystem.getInstance().getQuestById(QUEST_ID);
        if (quest != null && quest.isCompleted()) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "阿弥陀佛。少侠的善缘，贫僧已记下。心法既赠，好生修持。",
                    Arrays.asList("告辞"), null);
            return;
        }

        String message = "阿弥陀佛。贫僧化缘修缮寺庙，少侠可愿布施五千金？";
        GameEngine.getInstance().showDialog(npc.getName(), message,
                Arrays.asList("布施5000金币", "这次就算了"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            RoleInfo role = RoleSystem.getInstance().getRoleInfo();
                            if (role.getMoney() < ALMS) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要" + ALMS, 2000);
                                return;
                            }
                            showBookChoice(npc);
                        }
                    }
                });
    }

    private void showBookChoice(Npc npc) {
        GameEngine.getInstance().showDialog(npc.getName(),
                "善哉善哉，施主与我有缘，贫僧有金刚护体、妙手回春两本心法，可择一相赠。金刚护体可护身减伤，妙手回春可疗伤回血。少侠择一即可。",
                Arrays.asList("金刚护体技能书", "妙手回春技能书"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        SkillType type = optionIndex == 0
                                ? SkillType.MAIN_JinGangHuTi
                                : SkillType.MAIN_MiaoShouHuiChun;
                        giveChosenBook(npc, type);
                    }
                });
    }

    private void giveChosenBook(Npc npc, SkillType skillType) {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role.getMoney() < ALMS) {
            GameEngine.getInstance().showCenterToast("金钱不足，需要" + ALMS, 2000);
            return;
        }
        Item book = ItemCreator.createSkillBook(skillType);
        boolean added = ItemSystem.getInstance().addItem(book, 1);
        if (!added) {
            GameEngine.getInstance().showCenterToast("背包已满，请先整理后再来", 2000);
            return;
        }
        role.setMoney(role.getMoney() - ALMS);
        markCompleted();
        GameEngine.getInstance().addMessage("获得 " + book.getName(), MessagePanel.MessageType.ITEM);
        GameEngine.getInstance().showNotification("获得物品", "获得" + book.getName(), CenterNotification.Type.INFO);
        GameEngine.getInstance().showDialog(npc.getName(),
                "拿去吧。打开背包使用技能书即可学会。阿弥陀佛，少侠珍重。",
                Arrays.asList("多谢大师"), null);
    }

    private void markCompleted() {
        QuestSystem questSystem = QuestSystem.getInstance();
        if (questSystem.getQuestById(QUEST_ID) == null) {
            questSystem.acceptQuest(QUEST_ID);
        }
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);
        if (quest != null && !quest.isCompleted()) {
            questSystem.advanceQuestStage(QUEST_ID);
        }
    }
}

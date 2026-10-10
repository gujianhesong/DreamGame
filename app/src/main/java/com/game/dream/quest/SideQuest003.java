package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.enums.SkillType;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.Npc;
import com.game.dream.panel.MessagePanel;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.system.SkillSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;

/**
 * 清溪村清风道长赠送剑气技能书，方便开局学会剑气。
 */
public class SideQuest003 implements ISideQuest {

    public static final int QUEST_ID = 6;
    public static final int NPC_ID = 100111;

    @Override
    public String getSideQuestName() {
        return "清风赠书";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        if (npc.getId() != NPC_ID) {
            return false;
        }
        handleQingFengConversation(npc);
        return true;
    }

    private void handleQingFengConversation(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);

        if (quest != null && quest.isCompleted()) {
            GameEngine.getInstance().showDialog(npc.getName(),
                    "剑气心法已赠予少侠。佩剑在手，挥剑即可发出剑气，勤加修炼便是。",
                    Arrays.asList("告辞"), null);
            return;
        }

        SkillInfo swordQi = SkillSystem.getInstance().getPlayerSkill(SkillType.MAIN_SwordQi);
        if (swordQi != null && swordQi.getLevel() >= 1) {
            markCompleted(questSystem);
            GameEngine.getInstance().showDialog(npc.getName(),
                    "少侠已通晓剑气，贫道便不多言了。佩剑在手，方可施展。",
                    Arrays.asList("告辞"), null);
            return;
        }

        String message = "少侠初入江湖，村外妖兽横行。贫道这里有一本剑气心法，赠予少侠。"
                + "收入行囊后使用，即可学会剑气。记得先佩上一把剑，无剑则剑气难发。";
        GameEngine.getInstance().showDialog(npc.getName(), message,
                Arrays.asList("领取剑气技能书", "下次再说"),
                new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            giveSwordQiBook(npc);
                        }
                    }
                });
    }

    private void giveSwordQiBook(Npc npc) {
        Item book = ItemCreator.createSkillBook(SkillType.MAIN_SwordQi);
        boolean added = ItemSystem.getInstance().addItem(book, 1);
        if (!added) {
            GameEngine.getInstance().showCenterToast("背包已满，请先整理后再来领取", 2000);
            return;
        }
        String extraMsg = "";
        if (ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON) == null) {
            extraMsg = giveStarterSword();
        }
        markCompleted(QuestSystem.getInstance());
        GameEngine.getInstance().addMessage("获得 " + book.getName(), MessagePanel.MessageType.ITEM);
        GameEngine.getInstance().showNotification("获得物品", "获得" + book.getName() + extraMsg,
                CenterNotification.Type.INFO);
        String thanks = extraMsg.isEmpty()
                ? "拿去吧。打开背包使用技能书，便可学会剑气。少侠珍重。"
                : "少侠尚未佩剑，贫道另赠一把十级无级别剑。佩上剑再用技能书，即可施展剑气。";
        GameEngine.getInstance().showDialog(npc.getName(), thanks, Arrays.asList("多谢道长"), null);
    }

    /** @return 获得剑时的附加提示，失败则为空 */
    private String giveStarterSword() {
        EquipCreator.EquipCreatorExtraInfo extraInfo = new EquipCreator.EquipCreatorExtraInfo();
        extraInfo.isWuJiBie = true;
        EquipmentItem sword = EquipCreator.createEquip(10, EquipmentItem.Slot.WEAPON, extraInfo);
        if (sword == null) {
            return "";
        }
        boolean added = ItemSystem.getInstance().addItem(sword, 1);
        if (!added) {
            GameEngine.getInstance().showCenterToast("背包已满，无级别剑未能放入", 2000);
            return "";
        }
        ItemSystem.getInstance().equipItem(sword);
        GameEngine.getInstance().addMessage("获得 " + sword.getName(), MessagePanel.MessageType.ITEM);
        return "、" + sword.getName();
    }

    private void markCompleted(QuestSystem questSystem) {
        if (questSystem.getQuestById(QUEST_ID) == null) {
            questSystem.acceptQuest(QUEST_ID);
        }
        QuestInfo quest = questSystem.getQuestById(QUEST_ID);
        if (quest != null && !quest.isCompleted()) {
            questSystem.advanceQuestStage(QUEST_ID);
        }
    }
}

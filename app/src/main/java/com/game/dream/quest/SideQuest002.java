package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.npc.Npc;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;

public class SideQuest002 implements ISideQuest {

    @Override
    public String getSideQuestName() {
        return "旺财的馈赠";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        if (npc.getId() == 100193) {
            handleConversation_WangCai(npc);
            return true;
        }

        return false;
    }

    /**
     * 旺财的任务对话逻辑
     */
    private void handleConversation_WangCai(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        QuestInfo quest = questSystem.getQuestById(5);

        // 情况1: 任务未接取 → 接取并显示第一段对话
        if (quest == null) {
            // 接取任务
            questSystem.acceptQuest(5);
            quest = questSystem.getQuestById(5);
        }

        // 情况2: 任务已完成
        if (quest.isCompleted()) {
            String msg = "汪汪！汪汪！（旺财看起来很喜欢你的样子）";
            GameEngine.getInstance().showDialog("旺财", msg, Arrays.asList("再见"), new DialogBox.DialogListener() {
                @Override
                public void onOptionSelected(int optionIndex) {
                    npc.setHasQuest(false);
                }
            });
            return;
        }

        int stage = quest.getStage();

        // Stage 0: 需要芋子排骨
        if (stage == 0) {
            String msg = "汪汪！汪汪！（旺财看起来很久没很饿的样子，给它买点买点芋子排骨吃吧）";
            GameEngine.getInstance().showDialog("旺财", msg, Arrays.asList("给它吃芋子排骨", "离开"), new DialogBox.DialogListener() {
                @Override
                public void onOptionSelected(int optionIndex) {
                    if (optionIndex == 0) {
                        // 检查是否有芋子排骨
                        int count = ItemSystem.getInstance().getItemCountByName("芋子排骨");
                        if (count > 0) {
                            // 消耗芋子排骨
                            ItemSystem.getInstance().removeItem("芋子排骨", 1);
                            // 推进任务
                            questSystem.advanceQuestStage(5);

                            // 奖励一大笔金币
                            String thanksMsg = "汪汪！汪汪！（旺财吃的很开心，并叼给你一个袋子，里面有100000金币）";
                            GameEngine.getInstance().showDialog("旺财", thanksMsg, null, null);

                            RoleSystem.getInstance().addMoney(100000);
                        } else {
                            // 没有芋子排骨
                            GameEngine.getInstance().showNotification("提示", "你没有芋子排骨，先去弄一个吧", CenterNotification.Type.INFO);
                        }
                    }
                    // optionIndex == 1: 离开，什么都不做
                }
            });
        }

    }

}

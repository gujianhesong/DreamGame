package com.game.dream.quest;

import com.game.dream.GameEngine;
import com.game.dream.bean.QuestInfo;
import com.game.dream.item.EquipCreator;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemCreator;
import com.game.dream.npc.Npc;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.QuestSystem;
import com.game.dream.ui.CenterNotification;
import com.game.dream.ui.DialogBox;

import java.util.Arrays;

public class SideQuest001 implements ISideQuest {

    @Override
    public String getSideQuestName() {
        return "馋嘴的小虎子";
    }

    @Override
    public boolean handleQuestConversation(Npc npc) {
        // 小虎子特殊任务对话
        if (npc.getId() == 100103) {
            handleXiaoHuziConversation(npc);
            return true;
        }

        return false;
    }

    /**
     * 小虎子的任务对话逻辑
     */
    private void handleXiaoHuziConversation(Npc npc) {
        QuestSystem questSystem = QuestSystem.getInstance();
        QuestInfo quest = questSystem.getQuestById(4);

        // 情况1: 任务未接取 → 接取并显示第一段对话
        if (quest == null) {
            // 接取任务
            questSystem.acceptQuest(4);
            quest = questSystem.getQuestById(4);
        }

        // 情况2: 任务已完成
        if (quest.isCompleted()) {
            String msg = "谢谢大哥哥！大哥哥最好了！";
            GameEngine.getInstance().showDialog("小虎子", msg, Arrays.asList("再见"), new DialogBox.DialogListener() {
                @Override
                public void onOptionSelected(int optionIndex) {
                    npc.setHasQuest(false);
                }
            });
            return;
        }

        int stage = quest.getStage();

        // Stage 0: 需要绿豆饼
        if (stage == 0) {
            String msg = "小花的绿豆饼好香好甜好好吃啊，要是能再吃一次多好啊";
            GameEngine.getInstance().showDialog("小虎子", msg, Arrays.asList("给他绿豆饼", "离开"), new DialogBox.DialogListener() {
                @Override
                public void onOptionSelected(int optionIndex) {
                    if (optionIndex == 0) {
                        // 检查是否有绿豆饼
                        int count = ItemSystem.getInstance().getItemCountByName("绿豆饼");
                        if (count > 0) {
                            // 消耗绿豆饼
                            ItemSystem.getInstance().removeItem("绿豆饼", 1);
                            // 推进任务
                            questSystem.advanceQuestStage(4);
                            // 显示下一段对话
                            showXiaoHuziStage1Dialog(npc);
                        } else {
                            // 没有绿豆饼
                            GameEngine.getInstance().showNotification("提示", "你没有绿豆饼，先去弄一个吧", CenterNotification.Type.INFO);
                        }
                    }
                    // optionIndex == 1: 离开，什么都不做
                }
            });
        }
        // Stage 1: 需要红糖麻糍
        else if (stage == 1) {
            showXiaoHuziStage1Dialog(npc);
        }
    }

    /**
     * 小虎子第二阶段对话（要红糖麻糍）
     */
    private void showXiaoHuziStage1Dialog(Npc npc) {
        String msg = "哇，是绿豆饼，嗯嗯，好好吃啊。\n大哥哥，我还想吃红糖麻糍，你能给我买吗？";
        GameEngine.getInstance().showDialog("小虎子", msg, Arrays.asList("给他红糖麻糍", "离开"), new DialogBox.DialogListener() {
            @Override
            public void onOptionSelected(int optionIndex) {
                if (optionIndex == 0) {
                    // 检查是否有红糖麻糍
                    int count = ItemSystem.getInstance().getItemCountByName("红糖麻糍");
                    if (count > 0) {
                        // 消耗红糖麻糍
                        ItemSystem.getInstance().removeItem("红糖麻糍", 1);
                        // 推进任务（完成）
                        QuestSystem.getInstance().advanceQuestStage(4);
                        // 奖励60级无级别剑
                        EquipCreator.EquipCreatorExtraInfo extraInfo = new EquipCreator.EquipCreatorExtraInfo();
                        extraInfo.isWuJiBie = true;
                        EquipmentItem equipmentItem = EquipCreator.createEquip(60, EquipmentItem.Slot.WEAPON, extraInfo);
                        ItemSystem.getInstance().addItem(equipmentItem, 1);
                        // 显示感谢对话
                        String thanksMsg = "谢谢大哥哥！这把剑是我在野外捡到的，就送给大哥哥吧，谢谢你给我买好吃的！";
                        GameEngine.getInstance().showDialog("小虎子", thanksMsg, Arrays.asList("收下金刚石"), new DialogBox.DialogListener() {
                            @Override
                            public void onOptionSelected(int optionIndex) {
                                npc.setHasQuest(false);
                                GameEngine.getInstance().showNotification("任务完成", "馋嘴的小虎子任务完成", CenterNotification.Type.QUEST_COMPLETE);
                            }
                        });
                    } else {
                        // 没有红糖麻糍
                        GameEngine.getInstance().showNotification("提示", "你没有红糖麻糍，可别糊弄小孩子哦", CenterNotification.Type.INFO);
                    }
                }
                // optionIndex == 1: 离开
            }
        });
    }

}

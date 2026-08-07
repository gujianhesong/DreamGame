package com.game.dream.system;

import com.game.dream.bean.QuestInfo;
import com.game.dream.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 任务系统 - 单例管理所有任务
 */
public class QuestSystem {

    private static QuestSystem instance = new QuestSystem();

    public static QuestSystem getInstance() {
        return instance;
    }

    // 所有已接取的任务列表
    private List<QuestInfo> acceptedQuests;

    // 任务模板库(所有可接取的任务定义)
    private List<QuestInfo> questTemplates;

    private QuestSystem() {
        acceptedQuests = new ArrayList<>();
        questTemplates = new ArrayList<>();
        initQuestTemplates();
    }

    /**
     * 初始化任务模板(示例任务)
     */
    private void initQuestTemplates() {
        // 任务1: 初出茅庐
        QuestInfo quest1 = new QuestInfo(
                1,
                "初出茅庐",
                "击败野外的怪物，证明你的实力",
                3,
                Arrays.asList("击败5只狼", "击败3只老虎", "击败1只野猪王")
        );
        questTemplates.add(quest1);

        // 任务2: 探索之旅
        QuestInfo quest2 = new QuestInfo(
                2,
                "探索之旅",
                "探索未知的区域，发现隐藏的秘密",
                4,
                Arrays.asList("到达森林深处", "找到神秘遗迹", "解开遗迹谜题", "击败遗迹守护者")
        );
        questTemplates.add(quest2);

        // 任务3: 收集材料
        QuestInfo quest3 = new QuestInfo(
                3,
                "收集材料",
                "为村庄收集所需的资源",
                2,
                Arrays.asList("收集10个狼皮", "将材料交给村长")
        );
        questTemplates.add(quest3);

        // 任务4: 小虎子的馋嘴
        QuestInfo quest4 = new QuestInfo(
                4,
                "小虎子的馋嘴",
                "小虎子想吃小花的绿豆饼和红糖麻糍",
                2,
                Arrays.asList("给小虎子绿豆饼", "给小虎子红糖麻糍")
        );
        questTemplates.add(quest4);

        // 任务5: 旺财的馈赠
        QuestInfo quest5 = new QuestInfo(
                5,
                "旺财的馈赠",
                "旺财看起来很久没很饿的样子，给它买点买点芋子排骨吃吧",
                1,
                Arrays.asList("给旺财吃点芋子排骨", "算了，我自己还想吃呢")
        );
        questTemplates.add(quest5);
    }

    /**
     * 接取任务
     * @param questId 任务ID
     * @return 接取的任务，如果已接取或不存在返回null
     */
    public QuestInfo acceptQuest(int questId) {
        // 检查是否已接取
        for (QuestInfo q : acceptedQuests) {
            if (q.getId() == questId) {
                LogUtil.d("Quest already accepted: " + questId);
                return null;
            }
        }

        // 从模板创建新任务实例
        for (QuestInfo template : questTemplates) {
            if (template.getId() == questId) {
                QuestInfo newQuest = new QuestInfo(
                        template.getId(),
                        template.getName(),
                        template.getDesc(),
                        template.getTotalStages(),
                        new ArrayList<>(template.getStageDescs())
                );
                newQuest.accept();
                acceptedQuests.add(newQuest);
                LogUtil.d("Quest accepted: " + newQuest.getName());
                return newQuest;
            }
        }

        LogUtil.d("Quest not found: " + questId);
        return null;
    }

    /**
     * 推进任务阶段
     * @param questId 任务ID
     * @return true 如果任务完成
     */
    public boolean advanceQuestStage(int questId) {
        for (QuestInfo quest : acceptedQuests) {
            if (quest.getId() == questId && quest.isActive()) {
                boolean completed = quest.advanceStage();
                if (completed) {
                    LogUtil.d("Quest completed: " + quest.getName());
                } else {
                    LogUtil.d("Quest advanced: " + quest.getName() + " stage=" + quest.getStage());
                }
                return completed;
            }
        }
        return false;
    }

    /**
     * 获取所有已接取的任务
     */
    public List<QuestInfo> getAcceptedQuests() {
        return acceptedQuests;
    }

    /**
     * 获取进行中的任务
     */
    public List<QuestInfo> getActiveQuests() {
        List<QuestInfo> active = new ArrayList<>();
        for (QuestInfo q : acceptedQuests) {
            if (q.isActive()) {
                active.add(q);
            }
        }
        return active;
    }

    /**
     * 获取已完成的任务
     */
    public List<QuestInfo> getCompletedQuests() {
        List<QuestInfo> completed = new ArrayList<>();
        for (QuestInfo q : acceptedQuests) {
            if (q.isCompleted()) {
                completed.add(q);
            }
        }
        return completed;
    }

    /**
     * 根据ID获取任务
     */
    public QuestInfo getQuestById(int questId) {
        for (QuestInfo q : acceptedQuests) {
            if (q.getId() == questId) {
                return q;
            }
        }
        return null;
    }

    /**
     * 获取所有任务模板
     */
    public List<QuestInfo> getQuestTemplates() {
        return questTemplates;
    }

    /**
     * 设置已接取的任务列表(用于存档读取)
     */
    public void setAcceptedQuests(List<QuestInfo> quests) {
        this.acceptedQuests = quests != null ? quests : new ArrayList<QuestInfo>();
    }

    /**
     * 移除已完成的任务
     */
    public void removeCompletedQuest(int questId) {
        QuestInfo toRemove = null;
        for (QuestInfo q : acceptedQuests) {
            if (q.getId() == questId && q.isCompleted()) {
                toRemove = q;
                break;
            }
        }
        if (toRemove != null) {
            acceptedQuests.remove(toRemove);
            LogUtil.d("Removed completed quest: " + toRemove.getName());
        }
    }
}

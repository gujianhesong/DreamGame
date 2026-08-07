package com.game.dream.quest;

import com.game.dream.npc.Npc;
import com.game.dream.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 支线任务管理
 */
public class SideQuestManager {

    private static SideQuestManager instance = new SideQuestManager();

    public static SideQuestManager getInstance() {
        return instance;
    }

    private List<ISideQuest> sideQuests;

    private SideQuestManager() {
        sideQuests = new ArrayList<>();
        // 直接注册所有支线任务
        registe(new SideQuest001());
        registe(new SideQuest002());
    }

    public void registe(ISideQuest sideQuest) {
        if (sideQuest == null) return;
        for (ISideQuest item : sideQuests) {
            if (item == sideQuest) {
                return;
            }
        }
        sideQuests.add(sideQuest);
        LogUtil.i("SideQuest registered: " + sideQuest.getClass().getSimpleName());
    }

    public void unRegiste(ISideQuest sideQuest) {
        if (sideQuest == null) return;
        for (ISideQuest item : sideQuests) {
            if (item == sideQuest) {
                sideQuests.remove(sideQuest);
                return;
            }
        }
    }

    public boolean handleQuestConversation(Npc npc) {
        for (ISideQuest item : sideQuests) {
            boolean handle = item.handleQuestConversation(npc);
            if (handle) {
                return true;
            }
        }
        return false;
    }

}

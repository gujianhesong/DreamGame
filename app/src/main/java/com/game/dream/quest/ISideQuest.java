package com.game.dream.quest;

import com.game.dream.npc.Npc;

public interface ISideQuest {

    String getSideQuestName();

    boolean handleQuestConversation(Npc npc);
}

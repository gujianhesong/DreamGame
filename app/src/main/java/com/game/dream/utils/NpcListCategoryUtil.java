package com.game.dream.utils;

import com.game.dream.enums.NpcType;
import com.game.dream.npc.Npc;

/**
 * 大地图右侧 NPC 列表：按玩法功能分档排序。
 */
public final class NpcListCategoryUtil {

    public enum Category {
        QUEST(0, "任务"),
        TRANSPORT(1, "传送"),
        SERVICE(2, "商店·服务"),
        OFFICIAL(3, "官府·剧情"),
        NIGHT(4, "夜间"),
        RESIDENT(5, "居民");

        public final int sortOrder;
        public final String label;

        Category(int sortOrder, String label) {
            this.sortOrder = sortOrder;
            this.label = label;
        }
    }

    private NpcListCategoryUtil() {
    }

    public static Category resolve(Npc npc) {
        if (npc == null) {
            return Category.RESIDENT;
        }
        if (npc.hasQuest()) {
            return Category.QUEST;
        }
        if (npc.isNightOnly()) {
            return Category.NIGHT;
        }
        NpcType type = npc.getNpcType();
        if (type == null) {
            return Category.RESIDENT;
        }
        switch (type) {
            case COACHMAN:
            case GHOST_CLERK:
            case BOATMAN:
            case STATION_MASTER:
                return Category.TRANSPORT;
            case MERCHANT:
            case PHARMACIST:
            case TAVERN_KEEPER:
            case GAMBLER:
            case DOCTOR:
            case MONK:
            case TAOIST:
            case SERVANT:
            case BLACKSMITH:
            case CRAFTSMAN:
            case ENTERTAINER:
                return Category.SERVICE;
            case SCHOLAR:
            case SOLDIER:
            case MAGISTRATE_CLERK:
            case PREFECT:
            case YAMEN_RUNNER:
            case SWORDSMAN:
            case WANDERER:
            case REALM_ATTENDANT:
            case VILLAGE_ELDER:
            case NIGHT_WATCH:
            case JUDGE_YANLUO:
            case JUDGE_CUI:
            case GHOST_OFFICER_BLACK:
            case GHOST_OFFICER_WHITE:
            case COW_HEAD:
            case HORSE_FACE:
            case MENG_PO:
            case LOST_SOUL:
                return Category.OFFICIAL;
            default:
                return Category.RESIDENT;
        }
    }

    public static int compareForWorldMapList(Npc a, Npc b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        int cOrder = Integer.compare(resolve(a).sortOrder, resolve(b).sortOrder);
        if (cOrder != 0) {
            return cOrder;
        }
        String na = a.getName() != null ? a.getName() : "";
        String nb = b.getName() != null ? b.getName() : "";
        return na.compareTo(nb);
    }
}
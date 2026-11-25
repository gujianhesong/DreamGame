package com.game.dream.title;

import com.game.dream.enums.TitleRarity;
import com.game.dream.system.MapSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TitleCatalog {

    private static final List<TitleDef> ALL = new ArrayList<>();
    private static final Map<String, TitleDef> BY_ID = new HashMap<>();

    static {
        registerAll();
        for (TitleDef d : ALL) {
            BY_ID.put(d.id, d);
        }
    }

    private TitleCatalog() {
    }

    public static List<TitleDef> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static TitleDef get(String id) {
        return BY_ID.get(id);
    }

    private static void r(TitleDef def) {
        ALL.add(def);
    }

    private static TitleDef d(String id, String name, TitleRarity rarity, String hint, UnlockRule rule) {
        return new TitleDef(id, name, rarity, false, hint, rule);
    }

    private static TitleDef h(String id, String name, String hint, UnlockRule rule) {
        return new TitleDef(id, name, TitleRarity.HIDDEN, true, hint, rule);
    }

    private static void registerAll() {
        // 凡·地图足迹
        r(d(TitleIds.JIANGHU_NEW, "江湖新人", TitleRarity.FAN, "初入江湖", null));
        r(d(TitleIds.QINGXI_OUT, "清溪出去的", TitleRarity.FAN, "离开清溪村", UnlockRule.builder().map(MapSystem.MAP_ID_JIN_LING).build()));
        r(d(TitleIds.JINLING_GUEST, "金陵过客", TitleRarity.FAN, "踏上金陵", UnlockRule.builder().map(MapSystem.MAP_ID_JIN_LING).build()));
        r(d(TitleIds.DONGHAI_LISTENER, "东海听潮", TitleRarity.FAN, "到东海湾", UnlockRule.builder().map(MapSystem.MAP_ID_DONGHAI_BAY).build()));
        r(d(TitleIds.SEABED_DEEP, "海底漫步", TitleRarity.FAN, "潜入东海海底", UnlockRule.builder().map(MapSystem.MAP_ID_DONGHAI_SEABED).build()));
        r(d(TitleIds.NETHER_STEP, "幽冥一步", TitleRarity.FAN, "入地府", UnlockRule.builder().map(MapSystem.MAP_ID_NETHERWORLD).build()));
        r(d(TitleIds.DAODONG_RICE, "稻香归人", TitleRarity.FAN, "乘驿车抵金陵", UnlockRule.builder().counter(TitleStats.COACH_TRAVEL, 1).build()));
        r(d(TitleIds.QING_MAZE, "竹影迷踪", TitleRarity.FAN, "进清溪迷宫", UnlockRule.builder().map(MapSystem.MAP_ID_QING_XI_MAZE).build()));
        r(d(TitleIds.CORAL_LOST, "珊瑚迷途", TitleRarity.FAN, "海底迷宫一层", UnlockRule.builder().map(MapSystem.MAP_ID_UNDERWATER_MAZE).build()));
        r(d(TitleIds.WATER_MAZE2, "沉船过客", TitleRarity.FAN, "海底迷宫二层", UnlockRule.builder().map(MapSystem.MAP_ID_UNDERWATER_MAZE_2).build()));
        r(d(TitleIds.WATER_MAZE3, "龙宫门外", TitleRarity.FAN, "海底迷宫三层", UnlockRule.builder().map(MapSystem.MAP_ID_UNDERWATER_MAZE_3).build()));
        r(d(TitleIds.YELLOW_SPRING, "黄泉路人", TitleRarity.FAN, "地狱迷宫一层", UnlockRule.builder().map(MapSystem.MAP_ID_HELL_MAZE_1).build()));
        r(d(TitleIds.YANLUO_WALKER, "枉城行客", TitleRarity.QI, "地狱迷宫三层", UnlockRule.builder().map(MapSystem.MAP_ID_HELL_MAZE_3).build()));
        r(d(TitleIds.HELL_4, "阎殿外客", TitleRarity.QI, "地狱迷宫四层", UnlockRule.builder().map(MapSystem.MAP_ID_HELL_MAZE_4).build()));
        r(d(TitleIds.ILLUSION_CUT, "幻境斫玉", TitleRarity.MING, "通关幻境一层", UnlockRule.builder().counter(TitleStats.ILLUSION_WIN, 1).build()));
        r(d(TitleIds.MONSTER_FRIEND, "万妖熟脸", TitleRarity.FAN, "上万妖台", UnlockRule.builder().map(MapSystem.MAP_ID_MONSTER_GALLERY).build()));
        r(d(TitleIds.FOUR_CORNER, "四角归一", TitleRarity.QI, "到访四角村落", UnlockRule.builder().flag("foot_four_corner").build()));
        r(d(TitleIds.EAST_GATE, "东门望海", TitleRarity.FAN, "出金陵东门", UnlockRule.builder().flag("east_gate_sea").build()));

        // 金陵市井
        r(d(TitleIds.ZUI_XIAN_FAN, "醉仙常客", TitleRarity.MING, "与醉仙楼掌柜攀谈", UnlockRule.builder().counter(TitleStats.INN_DAYS, 5).build()));
        r(d(TitleIds.YUELAI_STONE, "悦来枕石", TitleRarity.MING, "客栈歇宿", UnlockRule.builder().counter(TitleStats.INN_REST, 8).build()));
        r(d(TitleIds.HONGXIANG_FRIEND, "红香座上宾", TitleRarity.MING, "红香院略坐", UnlockRule.builder().counter(TitleStats.HONGXIANG, 1).build()));
        r(d(TitleIds.SHUYING_FAN, "疏影弦中人", TitleRarity.MING, "听琴师疏影", UnlockRule.builder().flag("talk_shuying").build()));
        r(d(TitleIds.QIANMA_DEBT, "钱麻子债主", TitleRarity.QI, "赌坊单日净赢银两", UnlockRule.builder().counter(TitleStats.GAMBLE_NET_WIN_DAY, 10000).build()));
        r(d(TitleIds.QIANMA_UNLUCK, "七日倒霉", TitleRarity.QI, "赌坊下注三十局", UnlockRule.builder().counter(TitleStats.GAMBLE_BETS, 30).build()));
        r(d(TitleIds.SEVEN_EATER, "七点通杀活证", TitleRarity.MING, "押大小遇七点", null));
        r(d(TitleIds.BIG_ONLY, "把把押大", TitleRarity.QI, "连押大", UnlockRule.builder().counter(TitleStats.GAMBLE_BIG_ONLY, 10).build()));
        r(d(TitleIds.DICE_EYES, "盅底有神", TitleRarity.FAN, "在如意赌坊下注", UnlockRule.builder().counter(TitleStats.GAMBLE_BETS, 1).build()));
        r(d(TitleIds.SHEN_STUDENT, "沈簿首门生", TitleRarity.MING, "股簿开仓", UnlockRule.builder().counter(TitleStats.STOCK_TRADE, 1).build()));
        r(d(TitleIds.XIBU_CAMEL, "西域驼铃", TitleRarity.MING, "股簿交易", UnlockRule.builder().counter(TitleStats.STOCK_TRADE, 20).build()));
        r(d(TitleIds.SHAREHOLDER, "万年股东", TitleRarity.QI, "股簿买卖", UnlockRule.builder().counter(TitleStats.STOCK_TRADE, 80).build()));
        r(d(TitleIds.CHASE_FALL, "追涨杀跌", TitleRarity.QI, "股簿频繁交易", UnlockRule.builder().counter(TitleStats.STOCK_TRADE, 200).build()));
        r(d(TitleIds.EMPTY_WALLET_WATCH, "空仓看戏", TitleRarity.QI, "解锁股簿暂不交易", UnlockRule.builder().flag("stock_idle").build()));
        r(d(TitleIds.DIVIDEND_LORD, "股息闲人", TitleRarity.MING, "领取股簿分红", UnlockRule.builder().counter(TitleStats.STOCK_DIVIDEND, 1).build()));
        r(d(TitleIds.STOCK_WHALE, "庄门大户", TitleRarity.LEGEND, "股簿一掷千金", UnlockRule.builder().counter(TitleStats.STOCK_TRADE, 500).build()));
        r(d(TitleIds.FIREWORK_JOY, "烟火贺喜", TitleRarity.MING, "金陵彩兑小奖", UnlockRule.builder().counter(TitleStats.LOTTERY_PRIZE, 1).build()));
        r(d(TitleIds.TICKET_STUB, "彩券收集人", TitleRarity.FAN, "购金陵彩", UnlockRule.builder().counter(TitleStats.LOTTERY_BUY, 1).build()));
        r(d(TitleIds.EIGHT_MILLION_DREAM, "八百万梦", TitleRarity.LEGEND, "金陵彩头彩", null));
        r(d(TitleIds.THREE_MIN_HERO, "三分钟内人", TitleRarity.HIDDEN, "临开奖购彩中奖", null));
        r(d(TitleIds.LEGEND_JACKPOT, "烟花散尽", TitleRarity.LEGEND, "命中头彩", null));
        r(d(TitleIds.BANK_NIGHT, "钱庄守夜", TitleRarity.MING, "钱庄存钱", UnlockRule.builder().counter(TitleStats.BANK_DEPOSIT, 1).build()));
        r(d(TitleIds.BAG_HEAVEN, "行囊通天", TitleRarity.QI, "官仓扩容", UnlockRule.builder().counter(TitleStats.WAREHOUSE_EXPAND, 5).build()));
        r(d(TitleIds.HUICHUN_PILL, "回春帖士", TitleRarity.FAN, "回春堂疗伤", UnlockRule.builder().counter(TitleStats.NPC_HEAL, 5).build()));
        r(d(TitleIds.OUYE_HAMMER, "欧冶的锤子", TitleRarity.FAN, "铁铺锻造", UnlockRule.builder().counter(TitleStats.CRAFT, 10).build()));
        r(d(TitleIds.LONGQUAN_GUEST, "龙泉座上客", TitleRarity.FAN, "龙泉剑庄", UnlockRule.builder().flag("talk_longquan").build()));
        r(d(TitleIds.CUICAN_EYE, "宝鉴识光", TitleRarity.FAN, "璀璨珠宝", UnlockRule.builder().flag("talk_cuican").build()));
        r(d(TitleIds.CANGJING_READER, "藏经翻页", TitleRarity.FAN, "藏经商人", UnlockRule.builder().flag("talk_cangjing").build()));
        r(d(TitleIds.HALL_WANDER, "府衙闲人", TitleRarity.FAN, "府衙师爷", UnlockRule.builder().counter(TitleStats.YAMEN_TALK, 1).build()));
        r(d(TitleIds.YAMEN_PEN, "师爷笔友", TitleRarity.MING, "师爷对话", UnlockRule.builder().counter(TitleStats.YAMEN_TALK, 15).build()));
        r(d(TitleIds.GUIDE_KING, "金陵活地图", TitleRarity.QI, "师爷对话", UnlockRule.builder().counter(TitleStats.YAMEN_TALK, 40).build()));
        r(d(TitleIds.GENERAL_STORE, "南市掮客", TitleRarity.FAN, "杂货铺购物", UnlockRule.builder().counter(TitleStats.SHOP_BUY, 1).build()));
        r(d(TitleIds.EQUIP_SELL, "甩手掌柜", TitleRarity.FAN, "收购商卖装备", UnlockRule.builder().flag("equip_sell").build()));
        r(d(TitleIds.RED_LANTERN, "幽绿灯下", TitleRarity.FAN, "如意赌坊门口", UnlockRule.builder().flag("gamble_visit").build()));

        // 套装
        r(d(TitleIds.JINLING_FOUR, "金陵四闯", TitleRarity.QI, "赌股彩庄各一", UnlockRule.builder().flag("jinling_four").build()));
        r(d(TitleIds.NORTH_SOUTH, "南北客", TitleRarity.QI, "清溪金陵东海地府", UnlockRule.builder().flag("foot_north_south").build()));
        r(d(TitleIds.MAZE_THREE, "迷宫三层境", TitleRarity.QI, "三类迷宫各一", UnlockRule.builder().flag("maze_three").build()));
        r(d(TitleIds.SHOP_KING_PIN, "掌柜克星", TitleRarity.QI, "杂货购物达三十次", UnlockRule.builder().flag("shop_king_pin").build()));

        // 战斗与宠
        r(d(TitleIds.CAPTURE_HAND, "符到擒来", TitleRarity.MING, "捕捉成功", UnlockRule.builder().counter(TitleStats.PET_CAPTURE, 1).build()));
        r(d(TitleIds.PET_LEGION, "百兽名册", TitleRarity.QI, "捕捉战宠", UnlockRule.builder().counter(TitleStats.PET_CAPTURE, 20).build()));
        r(d(TitleIds.VET_FRIEND, "兽医老友", TitleRarity.FAN, "兽医师疗伤", UnlockRule.builder().counter(TitleStats.PET_HEAL, 5).build()));
        r(d(TitleIds.SLAYER, "林间斩妖", TitleRarity.FAN, "击败妖物", UnlockRule.builder().counter(TitleStats.KILLS, 50).build()));
        r(d(TitleIds.LEGEND_KILLER, "万妖屠", TitleRarity.LEGEND, "击败妖物", UnlockRule.builder().counter(TitleStats.KILLS, 5000).build()));
        r(d(TitleIds.QI_BOSS_HUNTER, "头领煞星", TitleRarity.QI, "击败精英", UnlockRule.builder().counter(TitleStats.KILLS, 500).build()));
        r(d(TitleIds.DASH_WIND, "追风步", TitleRarity.FAN, "善用冲刺", UnlockRule.builder().counter(TitleStats.DASH_COUNT, 100).build()));
        r(d(TitleIds.POTION_LOVER, "药罐子", TitleRarity.FAN, "快捷喝药", UnlockRule.builder().counter(TitleStats.POTION_USE, 50).build()));

        // 成长
        r(d(TitleIds.LEVEL_TEN, "小有所成", TitleRarity.FAN, "达到10级", UnlockRule.builder().level(10).build()));
        r(d(TitleIds.LEVEL_THIRTY, "风霜满面", TitleRarity.MING, "达到30级", UnlockRule.builder().level(30).build()));
        r(d(TitleIds.LEVEL_FIFTY, "半生江湖", TitleRarity.QI, "达到50级", UnlockRule.builder().level(50).build()));
        r(d(TitleIds.MANUAL_LEVEL, "刻苦修士", TitleRarity.MING, "手动升级", UnlockRule.builder().flag("manual_level").build()));
        r(d(TitleIds.PRACTICE_HARD, "苦修不辍", TitleRarity.MING, "修炼面板", UnlockRule.builder().flag("practice_panel").build()));
        r(d(TitleIds.ASSIST_GURU, "辅技之长", TitleRarity.MING, "辅助技能", UnlockRule.builder().flag("assist_panel").build()));

        // 死亡与地府
        r(d(TitleIds.DEATH_SEEKER, "轻生杀手", TitleRarity.MING, "身死魂归", UnlockRule.builder().counter(TitleStats.DEATHS, 5).build()));
        r(d(TitleIds.SOUL_GUEST, "幽魂牢客", TitleRarity.FAN, "第一次身死", UnlockRule.builder().counter(TitleStats.DEATHS, 1).build()));
        r(d(TitleIds.RETURN_SOUL, "还魂客", TitleRarity.MING, "黑白无常送还阳", UnlockRule.builder().counter(TitleStats.SOUL_RETURN, 1).build()));
        r(d(TitleIds.MENGPO_NO, "孟婆没请", TitleRarity.MING, "地府对话孟婆", UnlockRule.builder().counter(TitleStats.MENGPO_TALK, 1).build()));
        r(d(TitleIds.PRISON_BIRD, "笼中雀", TitleRarity.FAN, "幽魂牢静思", UnlockRule.builder().flag("soul_prison").build()));
        r(d(TitleIds.WEST_GHOST, "鬼差熟人", TitleRarity.MING, "夜间寻鬼差", UnlockRule.builder().flag("west_ghost").build()));
        r(d(TitleIds.GHOST_NIGHT, "夜行金陵", TitleRarity.FAN, "金陵夜行", UnlockRule.builder().flag("jinling_night").build()));
        r(d(TitleIds.DAWN_TRAIN, "鸡鸣起舞", TitleRarity.FAN, "歇至白昼", UnlockRule.builder().flag("inn_dawn").build()));

        // 趣味
        r(d(TitleIds.ONLY_LOOK, "只逛不买", TitleRarity.QI, "逛店不掏钱", UnlockRule.builder().flag("window_shop").build()));
        r(d(TitleIds.HIDDEN_WINDOW_SHOP, "橱窗仙子", TitleRarity.HIDDEN, "打开商店却不买", UnlockRule.builder().flag("window_shop_master").build()));
        r(d(TitleIds.COACH_WANDER, "驿路拾遗", TitleRarity.FAN, "乘车旅行", UnlockRule.builder().counter(TitleStats.COACH_TRAVEL, 10).build()));
        r(d(TitleIds.QI_COACH, "千里驿卒", TitleRarity.QI, "乘车旅行", UnlockRule.builder().counter(TitleStats.COACH_TRAVEL, 50).build()));
        r(d(TitleIds.QI_TELEPORT, "纵横六合", TitleRarity.QI, "多次传送", UnlockRule.builder().counter(TitleStats.TELEPORT, 30).build()));
        r(d(TitleIds.GAMBLE_COMEBACK, "三板回本", TitleRarity.QI, "赌坊翻盘", null));
        r(d(TitleIds.RICH_NO_MORE, "散财童子", TitleRarity.MING, "腰缠万贯", UnlockRule.builder().flag("rich_once").build()));
        r(d(TitleIds.POOR_BRAVE, "手头紧的", TitleRarity.FAN, "盘缠极少", UnlockRule.builder().flag("poor_brave").build()));
        r(d(TitleIds.INN_SNORE, "鼾声震楼", TitleRarity.FAN, "客栈安睡", UnlockRule.builder().counter(TitleStats.INN_REST, 3).build()));
        r(d(TitleIds.ILLUSION_ADDICT, "幻境常客", TitleRarity.QI, "多次幻境", UnlockRule.builder().counter(TitleStats.ILLUSION_WIN, 10).build()));
        r(d(TitleIds.SILVER_TONGUE, "银舌巧辩", TitleRarity.FAN, "与NPC交谈", UnlockRule.builder().counter(TitleStats.YAMEN_TALK, 3).build()));
        r(d(TitleIds.STALL_BROWSE, "摊前伫足", TitleRarity.FAN, "浏览摊位", UnlockRule.builder().counter(TitleStats.SHOP_WINDOW, 30).build()));
        r(d(TitleIds.PET_RENAME, "饲主*", TitleRarity.FAN, "为战宠改名", UnlockRule.builder().flag("pet_rename").build()));
        r(d(TitleIds.CAPTURE_FAIL, "失手一次", TitleRarity.FAN, "捕捉失手", UnlockRule.builder().flag("capture_fail").build()));
        r(d(TitleIds.HUOLI_FULL, "朝气蓬勃", TitleRarity.FAN, "活力满盈", UnlockRule.builder().flag("huoli_full").build()));
        r(d(TitleIds.TILI_BROKEN, "筋疲力尽", TitleRarity.FAN, "体力见底", UnlockRule.builder().flag("tili_empty").build()));
        r(d(TitleIds.BUILD_EQUIP, "炉火映面", TitleRarity.FAN, "打造界面", UnlockRule.builder().flag("build_equip").build()));
        r(d(TitleIds.SKILL_PAGE, "翻页老手", TitleRarity.FAN, "切换技能页", UnlockRule.builder().flag("skill_page").build()));
        r(d(TitleIds.WORLD_MAP, "揽图四顾", TitleRarity.FAN, "打开大地图", UnlockRule.builder().flag("world_map").build()));
        r(d(TitleIds.FAN_DUCK, "鸭栏熟客", TitleRarity.FAN, "村中偶遇鸭", UnlockRule.builder().flag("meet_duck").build()));
        r(d(TitleIds.FAN_ROOSTER, "闻鸡起舞", TitleRarity.FAN, "村中偶遇鸡", UnlockRule.builder().flag("meet_rooster").build()));
        r(d(TitleIds.HEALER, "妙手回春", TitleRarity.MING, "疗伤多次", UnlockRule.builder().counter(TitleStats.NPC_HEAL, 30).build()));
        r(d(TitleIds.LEGEND_TYRANT, "金陵巨贾", TitleRarity.LEGEND, "腰缠千万", UnlockRule.builder().flag("tyrant_silver").build()));
        r(d(TitleIds.TAVERN_BATTLE, "酒肆喧哗", TitleRarity.FAN, "闹事醉仙", UnlockRule.builder().flag("tavern_noise").build()));
        r(d(TitleIds.FOOTPRINT, "步步留痕", TitleRarity.MING, "足迹遍布", UnlockRule.builder().counter(TitleStats.TELEPORT, 100).build()));
        r(d(TitleIds.MIRROR_SHRIMP, "虾兵故人", TitleRarity.FAN, "东海浅滩", UnlockRule.builder().flag("meet_shrimp").build()));
        r(d(TitleIds.UNDERSEA_PEARL, "拾蚌遗珠", TitleRarity.MING, "海底拾贝", UnlockRule.builder().flag("undersea_loot").build()));
        r(d(TitleIds.HELL_RUNNER, "炼狱马拉松", TitleRarity.QI, "地狱迷宫穿行", UnlockRule.builder().flag("hell_runner").build()));
        r(d(TitleIds.MING_GUARD, "城门守卫", TitleRarity.FAN, "与卫兵对话", UnlockRule.builder().flag("talk_guard").build()));
        r(d(TitleIds.MING_MERCHANT, "走南闯北", TitleRarity.MING, "购物达人", UnlockRule.builder().counter(TitleStats.SHOP_BUY, 100).build()));
        r(d(TitleIds.XUANJI_DISCIPLE, "玄机记名", TitleRarity.MING, "玄机真人", UnlockRule.builder().flag("talk_xuanji").build()));
        r(d(TitleIds.PEIYUAN_DISCIPLE, "培元门下", TitleRarity.MING, "培元先生", UnlockRule.builder().flag("talk_peiyuan").build()));
        r(d(TitleIds.BEGGAR_MEMORY, "一碗温情", TitleRarity.FAN, "清溪乞丐", UnlockRule.builder().flag("qingxi_beggar").build()));
        r(d(TitleIds.CHIEF_FRIEND, "村长故交", TitleRarity.FAN, "清溪村长", UnlockRule.builder().flag("qingxi_chief").build()));
        r(d(TitleIds.LOTTERY_LOSING, "彩券攒灰", TitleRarity.MING, "购彩多次未中", UnlockRule.builder().counter(TitleStats.LOTTERY_BUY, 30).build()));

        // 隐藏
        r(h(TitleIds.HIDDEN_DREAMER, "梦中人", "梦醒时分", UnlockRule.builder().flag("dream_start").build()));
        r(h(TitleIds.HIDDEN_WUMING, "江湖无名", "别问我叫什么", null));
        r(h(TitleIds.HIDDEN_JACKPOT_AFTER, "烟花烬冷", "头彩之后", UnlockRule.builder().flag("after_jackpot").build()));
        r(h(TitleIds.HIDDEN_ZERO, "七七四十九", "逢七则止", UnlockRule.builder().counter(TitleStats.GAMBLE_SEVEN, 7).build()));
        r(h(TitleIds.HIDDEN_NEVER_BET, "从不沾赌", "只看不押", UnlockRule.builder().flag("never_gamble").build()));
    }
}
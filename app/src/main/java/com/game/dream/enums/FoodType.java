package com.game.dream.enums;

import android.text.TextUtils;

public enum FoodType {
    FT_LvDouBing("绿豆饼", "由绿豆和面粉烤制的甜香小饼，甜甜糯糯很好吃"),
    FT_ZengGao("甑糕", "甑糕是以糯米、红枣、蜜枣等为原料，用古老炊具“甑”蒸制而成的传统风味小吃，口感绵软粘甜、枣香浓郁"),
    FT_QieGao("切糕", "切糕是以核桃仁、葡萄干、玉米饴等为原料，经传统工艺熬制压制而成的新疆特色坚果糕点，口感香醇酥脆、甜而不腻"),
    FT_JianBingGuoZi("煎饼果子", "哟哟切克闹，煎饼果子来一套"),
    FT_HongTangMaCi("红糖麻糍", "红糖麻糍是用糯米做成的江南小吃，外皮酥脆、内里软糯Q弹，裹上红糖，甜香浓郁而不腻，简直太好吃了"),
    FT_JiaoCongBing("椒葱饼", "椒葱饼是以面粉、椒盐、葱花、花生等为原料，经传统工艺煎烤而成的特色面点，外皮酥脆分层、内里咸香微麻，葱香与椒香交织，是浓浓的乡愁"),

    FT_YouZhaHuaSheng("油炸花生", "油炸花生是以饱满花生米经低温慢炸而成的经典佐酒小食，口感酥脆化渣、咸香适口，最适合下酒"),
    FT_RouWanZi("肉丸子", "肉丸子是以猪肉和红薯粉为主料做成的风味小吃，口感新嫩，肉香浓郁，是家乡的味道"),
    FT_KaoYa("烤鸭", "外皮酥脆油亮、肉质细嫩肥而不腻，加上灵魂酱汁或者酸梅酱，太好吃啦"),
    FT_BaiQieJi("白切鸡", "白切鸡是一道经“三浸三提一冷”古法浸煮而成的经典岭南名菜，皮爽肉滑、骨香本鲜，是鸡有鸡味的至简至鲜的代表"),
    FT_MenZhuJiao("黄豆猪脚", "黄豆猪脚是以饱满黄豆与新鲜猪蹄为主料，经慢火久炖而成的经典滋补佳肴，猪脚软糯脱骨、黄豆绵密吸汁，胶原蛋白与植物蛋白交融，男女老少都爱吃"),
    FT_YuZiPaiGu("芋子排骨", "芋子排骨是以粉糯芋子与鲜嫩排骨为主料，经蒸炖而成的经典家常佳肴，芋吸肉汁绵软入味、排骨酥烂脱骨，肉香与芋香交融，是家的味道"),
    FT_TangCuLiJi("糖醋里脊", "糖醋里脊是以猪里脊为主料，经挂糊炸制并裹以糖醋酱汁而成的经典名菜，外皮酥脆、内里鲜嫩，酸甜适口，小孩子的最爱"),
    FT_SongShuGuiYu("松鼠鳜鱼", "松鼠鳜鱼是以鲜活鳜鱼为主料，经精湛刀工剞花、油炸定型并淋以滚烫糖醋汁而成的经典名菜，形似松鼠、声如鸣叫、外酥里嫩、酸甜适口，好吃停不下来"),
    FT_FoTiaoQiang("佛跳墙", "佛跳墙是以鲍鱼、海参、鱼翅等数十种山珍海味为主料，经分层码放、荷叶封坛、文火慢煨而成，汤色褐亮、荤香浓郁、味中有味，有云“坛启荤香飘四邻，佛闻弃禅跳墙来”"),

    FT_MiJiu("米酒", "米酒是以糯米为主料，经酒曲发酵而成的传统饮品，色泽清亮或微浊，口感清甜醇和、酒香淡雅，喝上一碗，体验浓浓的家乡滋味"),
    FT_ErGuoTou("二锅头", "二锅头是以高粱等粮食为原料，经“掐头去尾、取中段”古法蒸馏而成的传统白酒，酒体清亮、清香纯正、甘冽醇厚"),
    FT_NvErHong("女儿红", "女儿红是以糯米为主料，经古法冬酿并窖藏陈化而成的绍兴黄酒，酒色琥珀澄澈、口感醇厚甘鲜，承载着“生女酿酒、嫁时取饮”的千年民俗与东方含蓄深沉的亲情表达"),
    FT_PuTaoJiu("葡萄酒", "葡萄酒是以葡萄果实或葡萄汁经发酵酿制而成的酒精饮料，色泽瑰丽、香气馥郁、口感层次丰富，正所谓葡萄美酒夜光杯，欲饮琵琶马上催"),

    ;

    private String name;
    private String desc;

    FoodType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public static FoodType getFoodTypeWithName(String name) {
        for (FoodType item : FoodType.values()) {
            if (TextUtils.equals(item.name(), name)) {
                return item;
            }
        }
        return null;
    }
}

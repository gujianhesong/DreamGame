package com.game.dream.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 金陵主城渲染器
 * 主城范围: (25000,25000) ~ (35000,35000)，尺寸 10000x10000
 * 包含: 城墙、城门、皇宫、商业街区、民居、广场、园林等
 */
public class JinlingCityRenderer {

    // ==================== 建筑类型 ====================

    public enum BuildingType {
        PALACE,         // 皇宫/宫殿
        GOVERNMENT,     // 官府
        INN,            // 酒楼/客栈
        TEA_HOUSE,      // 茶馆
        PHARMACY,       // 药店
        WEAPON_SHOP,    // 武器坊
        ARMOR_SHOP,     // 防具店
        BLACKSMITH,     // 铁匠铺
        GENERAL_STORE,  // 杂货铺
        GEM_SHOP,       // 宝石商
        MARKET_STALL,   // 市集摊位
        HOUSE,          // 民居
        TALL_HOUSE,     // 二层民居
        GARDEN_PAVILION,// 园林亭阁
        WELL,           // 水井
        CLOCK_TOWER,    // 钟楼
        STABLE,         // 马厩
        WAREHOUSE       // 仓库
    }

    public static class Building {
        public BuildingType type;
        public int x, y, width, height;
        public String name; // 建筑名称（可选）

        public Building(BuildingType type, int x, int y, int w, int h, String name) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.name = name;
        }
    }

    // ==================== 装饰物类型 ====================

    public enum DecoType { TREE, LANTERN, FLOWER_BED, STATUE, FENCE, WELL, STONE_TABLE, CART, BAMBOO, ROCKERY, BARREL, FLAG_POLE, NOTICE_BOARD }

    public static class Decoration {
        public DecoType type;
        public int x, y, size;

        public Decoration(DecoType type, int x, int y, int size) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }

    // ==================== 字段 ====================

    private List<Building> buildings = new ArrayList<>();
    private List<Decoration> decorations = new ArrayList<>();
    private List<Rect> wallSegments = new ArrayList<>();
    private List<Rect> gateOpenings = new ArrayList<>();
    private Rect cityBounds;
    private Random rand = new Random(99999);

    // 城市坐标常量
    private static final int CX1 = 25000, CY1 = 25000;
    private static final int CX2 = 35000, CY2 = 35000;
    private static final int CCX = 30000, CCY = 30000;

    // 城墙参数
    private static final int WALL_THICK = 200;
    private static final int GATE_WIDTH = 600;

    // 内城参数
    private static final int INNER_X1 = 28000, INNER_Y1 = 28000;
    private static final int INNER_X2 = 32000, INNER_Y2 = 32000;

    // 道路参数
    private static final int ROAD_HALF = 150; // 道路半宽

    // ==================== 初始化 ====================

    public void initCity() {
        cityBounds = new Rect(CX1, CY1, CX2, CY2);
        buildings.clear();
        decorations.clear();
        wallSegments.clear();
        gateOpenings.clear();

        buildOuterWall();
        buildInnerCity();
        buildNorthDistrict();  // 酒楼街 + 客栈区
        buildEastDistrict();   // 武器坊街 + 杂货市集
        buildSouthDistrict();  // 民居区 + 南广场
        buildWestDistrict();   // 药铺街 + 铁匠铺
        addDecorations();
    }

    // ==================== 城墙 ====================

    private void buildOuterWall() {
        int gateHalf = GATE_WIDTH / 2;

        // 北墙 (左段 + 右段，中间是北门)
        wallSegments.add(new Rect(CX1, CY1, CCX - gateHalf, CY1 + WALL_THICK));
        wallSegments.add(new Rect(CCX + gateHalf, CY1, CX2, CY1 + WALL_THICK));
        gateOpenings.add(new Rect(CCX - gateHalf, CY1, CCX + gateHalf, CY1 + WALL_THICK));

        // 南墙
        wallSegments.add(new Rect(CX1, CY2 - WALL_THICK, CCX - gateHalf, CY2));
        wallSegments.add(new Rect(CCX + gateHalf, CY2 - WALL_THICK, CX2, CY2));
        gateOpenings.add(new Rect(CCX - gateHalf, CY2 - WALL_THICK, CCX + gateHalf, CY2));

        // 西墙
        wallSegments.add(new Rect(CX1, CY1, CX1 + WALL_THICK, CCY - gateHalf));
        wallSegments.add(new Rect(CX1, CCY + gateHalf, CX1 + WALL_THICK, CY2));
        gateOpenings.add(new Rect(CX1, CCY - gateHalf, CX1 + WALL_THICK, CCY + gateHalf));

        // 东墙
        wallSegments.add(new Rect(CX2 - WALL_THICK, CY1, CX2, CCY - gateHalf));
        wallSegments.add(new Rect(CX2 - WALL_THICK, CCY + gateHalf, CX2, CY2));
        gateOpenings.add(new Rect(CX2 - WALL_THICK, CCY - gateHalf, CX2, CCY + gateHalf));

        // 四角角楼 (装饰性小塔楼)
        int towerSize = 300;
        buildings.add(new Building(BuildingType.CLOCK_TOWER, CX1, CY1, towerSize, towerSize, "西北角楼"));
        buildings.add(new Building(BuildingType.CLOCK_TOWER, CX2 - towerSize, CY1, towerSize, towerSize, "东北角楼"));
        buildings.add(new Building(BuildingType.CLOCK_TOWER, CX1, CY2 - towerSize, towerSize, towerSize, "西南角楼"));
        buildings.add(new Building(BuildingType.CLOCK_TOWER, CX2 - towerSize, CY2 - towerSize, towerSize, towerSize, "东南角楼"));
    }

    // ==================== 内城（皇城/官府区） ====================

    private void buildInnerCity() {
        // 皇宫大殿 (中心偏北)
        buildings.add(new Building(BuildingType.PALACE, 29000, 28300, 2000, 1400, "金陵府衙"));

        // 左配殿
        buildings.add(new Building(BuildingType.GOVERNMENT, 28200, 28500, 600, 800, "户房"));
        buildings.add(new Building(BuildingType.GOVERNMENT, 28200, 29500, 600, 800, "兵房"));

        // 右配殿
        buildings.add(new Building(BuildingType.GOVERNMENT, 31200, 28500, 600, 800, "礼房"));
        buildings.add(new Building(BuildingType.GOVERNMENT, 31200, 29500, 600, 800, "工房"));

        // 内城花园 (南部区域，用装饰表示)
        // 花园区域不放置建筑碰撞，用装饰物填充

        // 钟楼 (内城中心偏南，标志性建筑)
        buildings.add(new Building(BuildingType.CLOCK_TOWER, CCX - 200, 30200, 400, 400, "金陵钟楼"));

        // 内城花园装饰
        decorations.add(new Decoration(DecoType.STATUE, CCX, 30900, 60));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 28500, 30800, 200));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 31500, 30800, 200));
        decorations.add(new Decoration(DecoType.TREE, 28300, 31200, 120));
        decorations.add(new Decoration(DecoType.TREE, 28600, 31500, 110));
        decorations.add(new Decoration(DecoType.TREE, 31400, 31200, 115));
        decorations.add(new Decoration(DecoType.TREE, 31700, 31500, 120));
        // 内城花园果树（红黄果子）
        decorations.add(new Decoration(DecoType.TREE, 28800, 31000, 140));
        decorations.add(new Decoration(DecoType.TREE, 29200, 31400, 130));
        decorations.add(new Decoration(DecoType.TREE, 30800, 31100, 135));
        decorations.add(new Decoration(DecoType.TREE, 31200, 31400, 140));
        decorations.add(new Decoration(DecoType.TREE, 30000, 31600, 145));
    }

    // ==================== 北区：酒楼街 + 客栈区 ====================

    private void buildNorthDistrict() {
        // 区域: (25200, 25200) ~ (34800, 28000)
        // 主街两侧分布酒楼和客栈

        // 酒楼街 (西侧)
        placeShops(25400, 25400, 27800, 27800,
                400, 320, 200, 160, BuildingType.INN,
                new String[]{"醉仙楼", "金陵酒楼", "望月阁", "春风饭店", "聚贤庄", "太白酒楼", "悦宾楼", "福满园", "香满楼", "八仙楼", "迎宾阁", "状元楼"});

        // 客栈区 (东侧)
        placeShops(30200, 25400, 34600, 27800,
                400, 320, 200, 160, BuildingType.TEA_HOUSE,
                new String[]{"悦来客栈", "如家旅舍", "清风茶馆", "明月茶楼", "听雨轩", "品茗居", "怡心茶庄", "碧云轩", "翠竹茶坊", "松风馆", "兰香居", "杏花村"});

        // 中间小摊位
        placeShops(28200, 25600, 29800, 27600,
                400, 320, 200, 160, BuildingType.MARKET_STALL,
                new String[]{"面摊", "包子铺", "馄饨摊", "粥铺", "烧饼档", "豆腐坊"});
    }

    // ==================== 东区：武器坊 + 杂货市集 ====================

    private void buildEastDistrict() {
        // 区域: (32000, 25200) ~ (34800, 34800)
        // 注意：避开南北向主干道（x=30000附近）和东西向主干道（y=30000附近）

        // 上半区：武器坊街（全部在东西向主干道 y=29850 上方）
        placeShops(32200, 25400, 34600, 27400,
                400, 320, 200, 160, BuildingType.WEAPON_SHOP,
                new String[]{"龙泉剑庄", "霸王枪铺", "倚天锻坊", "寒铁兵器", "神兵阁", "干将剑庐", "莫邪铸坊", "玄铁堂", "碧血武器", "青锋兵器"});

        // 杂货铺 (间隔160px，底部28520 < 道路29850)
        placeShops(32200, 27560, 34600, 28680,
                400, 320, 200, 160, BuildingType.GENERAL_STORE,
                new String[]{"万宝杂货", "百宝箱", "奇珍铺", "聚宝盆", "万象阁", "杂货郎"});

        // 宝石店 (底部29000 < 道路29850，安全)
        placeShops(32200, 28840, 34600, 29640,
                400, 320, 200, 160, BuildingType.GEM_SHOP,
                new String[]{"璀璨宝石", "翡翠轩", "珠玉阁", "碧玺坊", "玛瑙居", "琉璃轩", "钻石斋", "紫晶洞"});

        // 下半区：防具店（全部在东西向主干道 y=30150 下方）
        placeShops(32200, 30600, 34600, 32040,
                400, 320, 200, 160, BuildingType.ARMOR_SHOP,
                new String[]{"铁甲坊", "锦衣卫", "玄甲防具", "金缕衣阁", "银盔铺", "锁子甲坊", "虎威防具", "龙鳞甲铺"});
    }

    // ==================== 南区：民居区 + 南广场 ====================

    private void buildSouthDistrict() {
        // 区域: (25200, 32000) ~ (34800, 34800)

        // 南广场 (城门内侧大广场)
        // 广场区域不放建筑，用装饰物表示

        // 西区民居（建筑 400x320，间距 200x160）
        fillBuildings(25400, 32200, 29600, 34600,
                400, 320, 200, 160, BuildingType.HOUSE,
                new String[]{"民居", "小院", "巷弄人家"});

        // 东区民居（建筑 400x320，间距 200x160）
        fillBuildings(30400, 32200, 34600, 34600,
                400, 320, 200, 160, BuildingType.TALL_HOUSE,
                new String[]{"二层小楼", "民居", "商铺"});

        // 广场装饰
        decorations.add(new Decoration(DecoType.STATUE, CCX, 33500, 80));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 29000, 33800, 200));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 31000, 33800, 200));

        // === 西区民居间隙树木 ===
        // 建筑网格: 400x320, 间距 200x160, 步长 600x480
        // 列间隙中心 x=25900,26500,27100,27700,28300,28900,29500
        // 行间隙中心 y=32600,33080,33560,34040
        // 树高=size*1.4，树冠向上延伸~0.7*size。行间距160，半间距80
        // 向下偏移+30px：树冠顶距上方建筑 80-30=50px 安全距离
        decorations.add(new Decoration(DecoType.TREE, 25900, 32630, 110));
        decorations.add(new Decoration(DecoType.TREE, 27700, 32630, 100));
        decorations.add(new Decoration(DecoType.TREE, 29500, 32630, 115));
        decorations.add(new Decoration(DecoType.TREE, 26500, 33110, 105));
        decorations.add(new Decoration(DecoType.TREE, 28300, 33110, 120));
        decorations.add(new Decoration(DecoType.TREE, 25900, 33590, 100));
        decorations.add(new Decoration(DecoType.TREE, 27100, 33590, 110));
        decorations.add(new Decoration(DecoType.TREE, 28900, 34070, 105));
        decorations.add(new Decoration(DecoType.TREE, 27100, 34070, 100));
        decorations.add(new Decoration(DecoType.TREE, 25900, 34070, 115));

        // === 东区民居间隙树木 ===
        // 列间隙中心 x=30900,31500,32100,32700,33300,33900,34500
        decorations.add(new Decoration(DecoType.TREE, 31500, 32630, 115));
        decorations.add(new Decoration(DecoType.TREE, 33300, 32630, 105));
        decorations.add(new Decoration(DecoType.TREE, 30900, 33110, 120));
        decorations.add(new Decoration(DecoType.TREE, 32700, 33110, 100));
        decorations.add(new Decoration(DecoType.TREE, 34500, 33110, 110));
        decorations.add(new Decoration(DecoType.TREE, 32100, 33590, 110));
        decorations.add(new Decoration(DecoType.TREE, 33900, 33590, 105));
        decorations.add(new Decoration(DecoType.TREE, 31500, 34070, 100));
        decorations.add(new Decoration(DecoType.TREE, 33300, 34070, 115));
        decorations.add(new Decoration(DecoType.TREE, 34500, 34070, 110));

        // === 西区民居间隙石桌石凳（加大） ===
        decorations.add(new Decoration(DecoType.STONE_TABLE, 27100, 32600, 95));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 27700, 33560, 90));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 26500, 34040, 85));

        // === 东区民居间隙石桌石凳（加大） ===
        decorations.add(new Decoration(DecoType.STONE_TABLE, 32700, 32600, 95));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 33900, 34040, 90));

        // === 手推车/板车（加大） ===
        decorations.add(new Decoration(DecoType.CART, 26500, 32600, 90));
        decorations.add(new Decoration(DecoType.CART, 27700, 33000, 85));
        decorations.add(new Decoration(DecoType.CART, 31500, 33080, 90));
        decorations.add(new Decoration(DecoType.CART, 33900, 32600, 85));

        // 南区主路两侧行道树
        decorations.add(new Decoration(DecoType.TREE, CCX - ROAD_HALF - 100, 32500, 110));
        decorations.add(new Decoration(DecoType.TREE, CCX + ROAD_HALF + 100, 32500, 115));
        decorations.add(new Decoration(DecoType.TREE, CCX - ROAD_HALF - 100, 34200, 105));
        decorations.add(new Decoration(DecoType.TREE, CCX + ROAD_HALF + 100, 34200, 110));
    }

    // ==================== 西区：药铺街 + 铁匠铺 ====================

    private void buildWestDistrict() {
        // 区域: (25200, 25200) ~ (28000, 34800)

        // 上半区：药铺街（全部在东西向主干道 y=29850 上方）
        placeShops(25400, 25400, 27800, 27400,
                400, 320, 200, 160, BuildingType.PHARMACY,
                new String[]{"回春堂", "百草堂", "济世药铺", "同仁堂", "妙手药房", "悬壶斋", "杏林春", "活死人堂", "仁心药庐", "万金丹铺"});

        // 仓库 (间隔160px，底部28200 < 道路29850)
        placeShops(25400, 27560, 27800, 28680,
                400, 320, 200, 160, BuildingType.WAREHOUSE,
                new String[]{"官仓", "民仓", "货栈", "粮仓", "储物阁", "集货栈"});

        // 马厩 (底部28680 < 道路29850，安全)
        placeShops(25400, 28840, 27800, 29480,
                400, 320, 200, 160, BuildingType.STABLE,
                new String[]{"马厩", "骡马行", "骏马坊", "奔马场"});

        // 下半区：铁匠铺（全部在东西向主干道 y=30150 下方）
        placeShops(25400, 30440, 27800, 32440,
                400, 320, 200, 160, BuildingType.BLACKSMITH,
                new String[]{"欧冶铁铺", "干将铸坊", "铸剑山庄", "打铁铺", "莫邪剑庐", "龙泉锻坊", "玄铁铺", "赤焰铁坊", "天工锻坊", "百炼堂"});
    }

    // ==================== 装饰物 ====================

    private void addDecorations() {
        // 沿主干道两侧放置灯笼和树木
        Random decoRand = new Random(77777);

        // 北街灯笼
        for (int y = CY1 + WALL_THICK + 50; y < INNER_Y1; y += 400) {
            decorations.add(new Decoration(DecoType.LANTERN, CCX - ROAD_HALF - 50, y, 20));
            decorations.add(new Decoration(DecoType.LANTERN, CCX + ROAD_HALF + 50, y, 20));
        }
        // 南街灯笼
        for (int y = INNER_Y2 + 50; y < CY2 - WALL_THICK; y += 400) {
            decorations.add(new Decoration(DecoType.LANTERN, CCX - ROAD_HALF - 50, y, 20));
            decorations.add(new Decoration(DecoType.LANTERN, CCX + ROAD_HALF + 50, y, 20));
        }
        // 西街灯笼
        for (int x = CX1 + WALL_THICK + 50; x < INNER_X1; x += 400) {
            decorations.add(new Decoration(DecoType.LANTERN, x, CCY - ROAD_HALF - 50, 20));
            decorations.add(new Decoration(DecoType.LANTERN, x, CCY + ROAD_HALF + 50, 20));
        }
        // 东街灯笼
        for (int x = INNER_X2 + 50; x < CX2 - WALL_THICK; x += 400) {
            decorations.add(new Decoration(DecoType.LANTERN, x, CCY - ROAD_HALF - 50, 20));
            decorations.add(new Decoration(DecoType.LANTERN, x, CCY + ROAD_HALF + 50, 20));
        }

        // 内城周围树木 + 各区果树
        for (int i = 0; i < 20; i++) {
            int tx = INNER_X1 + decoRand.nextInt(INNER_X2 - INNER_X1);
            int ty = INNER_Y1 + decoRand.nextInt(INNER_Y2 - INNER_Y1);
            // 避开建筑区域
            if (ty > 30000) { // 花园区域
                decorations.add(new Decoration(DecoType.TREE, tx, ty, 80 + decoRand.nextInt(60)));
            }
        }

        // 北区果树（统一网格400x320+200x160，步长600x480）
        // 酒楼街间隙中心: x=25800/26400/..., y=25720/26200/26680
        decorations.add(new Decoration(DecoType.TREE, 25800, 25720, 130));
        decorations.add(new Decoration(DecoType.TREE, 27600, 26680, 125));
        // 客栈区间隙中心: x=32600/33200/..., y=25720/26200/26680
        decorations.add(new Decoration(DecoType.TREE, 32600, 25720, 135));
        decorations.add(new Decoration(DecoType.TREE, 33600, 27280, 120));

        // 南区果树（民居间隙中，避开建筑）
        // 西区间隙中心: x=25900/26500/.../29500, y=32600/33080/33560/34040
        decorations.add(new Decoration(DecoType.TREE, 25900, 33080, 125));
        decorations.add(new Decoration(DecoType.TREE, 28300, 34550, 130));
        // 东区间隙中心: x=30900/31500/.../34500, y=32600/33080/33560/34040
        decorations.add(new Decoration(DecoType.TREE, 32100, 33080, 135));
        decorations.add(new Decoration(DecoType.TREE, 33900, 34550, 125));

        // 西区果树（统一网格400x320+200x160，步长600x480）
        // 药铺街/仓库间隙中心: x=25800/26400/..., y=28680/29160/...
        decorations.add(new Decoration(DecoType.TREE, 26600, 28760, 130));
        decorations.add(new Decoration(DecoType.TREE, 27600, 29360, 120));

        // 东区果树（统一网格400x320+200x160，步长600x480）
        // 武器坊/杂货铺间隙中心: x=32600/33200/..., y=28680/29160/...
        decorations.add(new Decoration(DecoType.TREE, 33200, 28760, 125));
        decorations.add(new Decoration(DecoType.TREE, 34000, 29400, 130));

        // 主干道两侧行道树
        decorations.add(new Decoration(DecoType.TREE, CCX - ROAD_HALF - 120, 26500, 110));
        decorations.add(new Decoration(DecoType.TREE, CCX + ROAD_HALF + 120, 26500, 115));
        decorations.add(new Decoration(DecoType.TREE, CCX - ROAD_HALF - 120, 33500, 120));
        decorations.add(new Decoration(DecoType.TREE, CCX + ROAD_HALF + 120, 33500, 110));

        // 水井 (各街区)
        decorations.add(new Decoration(DecoType.WELL, 26500, 26500, 120));
        decorations.add(new Decoration(DecoType.WELL, 33500, 26500, 120));
        decorations.add(new Decoration(DecoType.WELL, 26500, 33500, 120));
        decorations.add(new Decoration(DecoType.WELL, 33500, 33500, 120));
        decorations.add(new Decoration(DecoType.WELL, CCX, CCY - 500, 120));

        // === 商铺街道装饰：石桌石凳（歇脚处） ===
        // 东区商铺间隙
        decorations.add(new Decoration(DecoType.STONE_TABLE, 32800, 27200, 90));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 34000, 28300, 85));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 33200, 29200, 90));
        // 西区商铺间隙
        decorations.add(new Decoration(DecoType.STONE_TABLE, 26200, 27200, 85));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 27200, 28300, 90));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 26600, 29200, 85));
        // 北区商铺间隙
        decorations.add(new Decoration(DecoType.STONE_TABLE, 26200, 26200, 90));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 33200, 26200, 85));

        // === 商铺街道装饰：手推车 ===
        decorations.add(new Decoration(DecoType.CART, 33600, 27200, 85));
        decorations.add(new Decoration(DecoType.CART, 32600, 29200, 90));
        decorations.add(new Decoration(DecoType.CART, 27400, 27200, 85));
        decorations.add(new Decoration(DecoType.CART, 25800, 29200, 90));

        // === 商铺街道装饰：花坛 ===
        decorations.add(new Decoration(DecoType.FLOWER_BED, 34200, 26000, 150));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 32600, 28600, 130));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 25800, 26000, 140));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 27400, 28600, 130));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 28600, 26200, 120));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 29400, 26200, 120));

        // === 内城前方广场装饰（内城南门到东西向主干道之间） ===
        decorations.add(new Decoration(DecoType.STATUE, 29000, 30600, 50));
        decorations.add(new Decoration(DecoType.STATUE, 31000, 30600, 50));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 28500, 30400, 160));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 31500, 30400, 160));
        decorations.add(new Decoration(DecoType.TREE, 28200, 30800, 100));
        decorations.add(new Decoration(DecoType.TREE, 31800, 30800, 100));

        // === 东区商铺与南区民居之间空地绿化 ===
        decorations.add(new Decoration(DecoType.TREE, 32600, 31800, 120));
        decorations.add(new Decoration(DecoType.TREE, 33800, 31600, 110));
        decorations.add(new Decoration(DecoType.TREE, 34400, 31900, 115));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 33000, 31800, 160));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 34200, 31600, 85));

        // === 西区商铺与南区民居之间空地绿化 ===
        decorations.add(new Decoration(DecoType.TREE, 25800, 31600, 115));
        decorations.add(new Decoration(DecoType.TREE, 27000, 31800, 120));
        decorations.add(new Decoration(DecoType.TREE, 27600, 31500, 110));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 26400, 31600, 150));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 25800, 31800, 85));

        // === 商铺门前灯笼（额外点缀） ===
        decorations.add(new Decoration(DecoType.LANTERN, 32400, 26200, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 34400, 26200, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 25600, 26200, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 27600, 26200, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 32400, 28200, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 25600, 28200, 22));

        // === 栅栏（内城花园围栏） ===
        decorations.add(new Decoration(DecoType.FENCE, 28200, 30700, 60));
        decorations.add(new Decoration(DecoType.FENCE, 28800, 30700, 60));
        decorations.add(new Decoration(DecoType.FENCE, 31200, 30700, 60));
        decorations.add(new Decoration(DecoType.FENCE, 31800, 30700, 60));

        // === 额外树木填充空旷区域 ===
        // 北区与内城之间空地
        decorations.add(new Decoration(DecoType.TREE, 28200, 27800, 100));
        decorations.add(new Decoration(DecoType.TREE, 31800, 27800, 105));
        // 内城东西两侧
        decorations.add(new Decoration(DecoType.TREE, 27200, 29500, 110));
        decorations.add(new Decoration(DecoType.TREE, 27800, 30200, 100));
        decorations.add(new Decoration(DecoType.TREE, 32200, 29500, 105));
        decorations.add(new Decoration(DecoType.TREE, 32800, 30200, 100));
        // 南广场两侧补充
        decorations.add(new Decoration(DecoType.TREE, 28400, 33200, 115));
        decorations.add(new Decoration(DecoType.TREE, 31600, 33200, 110));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 28000, 33600, 140));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 32000, 33600, 140));

        // =============================================
        // === 新增装饰：竹林、假山、木桶、旗杆、告示牌 ===
        // =============================================

        // === 竹林（内城花园、北区、南区角落） ===
        // 内城花园竹林
        decorations.add(new Decoration(DecoType.BAMBOO, 28400, 31000, 120));
        decorations.add(new Decoration(DecoType.BAMBOO, 31600, 31000, 110));
        decorations.add(new Decoration(DecoType.BAMBOO, 28600, 31600, 100));
        decorations.add(new Decoration(DecoType.BAMBOO, 31400, 31600, 105));
        // 北区酒楼旁竹林（移到行间隙中间，竹竿不侵入建筑）
        decorations.add(new Decoration(DecoType.BAMBOO, 25900, 27400, 110));
        decorations.add(new Decoration(DecoType.BAMBOO, 27100, 27400, 100));
        // 北区客栈旁竹林（移到行间隙中间）
        decorations.add(new Decoration(DecoType.BAMBOO, 30700, 27400, 115));
        decorations.add(new Decoration(DecoType.BAMBOO, 34200, 27400, 105));
        // 南区民居角落竹林（移到民居南侧道路上，避开建筑）
        decorations.add(new Decoration(DecoType.BAMBOO, 25600, 34800, 100));
        decorations.add(new Decoration(DecoType.BAMBOO, 29400, 34700, 110));
        decorations.add(new Decoration(DecoType.BAMBOO, 34400, 34800, 105));
        // 东西主干道旁竹林
        decorations.add(new Decoration(DecoType.BAMBOO, 27000, 30400, 100));
        decorations.add(new Decoration(DecoType.BAMBOO, 33000, 30400, 105));

        // === 假山（内城花园、广场、各区空地） ===
        // 内城花园假山
        decorations.add(new Decoration(DecoType.ROCKERY, 29200, 30800, 180));
        decorations.add(new Decoration(DecoType.ROCKERY, 30800, 30800, 160));
        decorations.add(new Decoration(DecoType.ROCKERY, 28400, 31400, 140));
        decorations.add(new Decoration(DecoType.ROCKERY, 31600, 31400, 150));
        // 南广场假山
        decorations.add(new Decoration(DecoType.ROCKERY, 28600, 33400, 170));
        decorations.add(new Decoration(DecoType.ROCKERY, 31400, 33400, 160));
        // 东区商铺间隙假山
        decorations.add(new Decoration(DecoType.ROCKERY, 34400, 27400, 140));
        // 西区商铺间隙假山
        decorations.add(new Decoration(DecoType.ROCKERY, 25600, 27400, 130));
        // 东区商铺与民居之间
        decorations.add(new Decoration(DecoType.ROCKERY, 32400, 31900, 150));
        // 西区商铺与民居之间
        decorations.add(new Decoration(DecoType.ROCKERY, 27600, 31900, 145));

        // === 木桶（商铺门前、仓库旁、酒楼旁） ===
        // 东区武器坊街门前
        decorations.add(new Decoration(DecoType.BARREL, 32400, 27300, 55));
        decorations.add(new Decoration(DecoType.BARREL, 34200, 27300, 50));
        // 东区杂货铺门前
        decorations.add(new Decoration(DecoType.BARREL, 33000, 28600, 55));
        // 东区宝石店门前
        decorations.add(new Decoration(DecoType.BARREL, 32600, 29500, 50));
        // 西区药铺门前
        decorations.add(new Decoration(DecoType.BARREL, 25600, 27300, 55));
        decorations.add(new Decoration(DecoType.BARREL, 27400, 27300, 50));
        // 西区仓库旁（货物桶，移到药铺与仓库之间的行间隙）
        decorations.add(new Decoration(DecoType.BARREL, 26000, 27400, 60));
        decorations.add(new Decoration(DecoType.BARREL, 27200, 27400, 55));
        // 北区酒楼门前（酒桶）
        decorations.add(new Decoration(DecoType.BARREL, 25600, 27700, 55));
        decorations.add(new Decoration(DecoType.BARREL, 27600, 27700, 50));
        // 北区小摊附近
        decorations.add(new Decoration(DecoType.BARREL, 28400, 27500, 45));
        decorations.add(new Decoration(DecoType.BARREL, 29600, 27500, 45));
        // 铁匠铺旁（移到铁匠铺南侧，避开建筑）
        decorations.add(new Decoration(DecoType.BARREL, 26000, 32500, 55));
        decorations.add(new Decoration(DecoType.BARREL, 27200, 32500, 50));

        // === 旗杆（内城、城门、主要街区入口） ===
        // 内城前方广场两侧大旗
        decorations.add(new Decoration(DecoType.FLAG_POLE, 28600, 30600, 120));
        decorations.add(new Decoration(DecoType.FLAG_POLE, 31400, 30600, 120));
        // 内城配殿旁（移到内城花园东西两侧，避开配殿 28200~28800/31200~31800）
        decorations.add(new Decoration(DecoType.FLAG_POLE, 28000, 29000, 100));
        decorations.add(new Decoration(DecoType.FLAG_POLE, 32000, 29000, 100));
        // 北区酒楼街入口旗（移到建筑列间隙北侧道路上，避开建筑）
        decorations.add(new Decoration(DecoType.FLAG_POLE, 25900, 25200, 110));
        decorations.add(new Decoration(DecoType.FLAG_POLE, 27100, 25200, 110));
        // 北区客栈区入口旗（移到建筑列间隙北侧道路上）
        decorations.add(new Decoration(DecoType.FLAG_POLE, 31000, 25200, 110));
        decorations.add(new Decoration(DecoType.FLAG_POLE, 33400, 25200, 110));
        // 东区武器坊街入口（移到间隙北侧道路上）
        decorations.add(new Decoration(DecoType.FLAG_POLE, 32500, 25200, 105));
        // 西区药铺街入口（移到间隙北侧道路上，与酒楼旗错开）
        decorations.add(new Decoration(DecoType.FLAG_POLE, 26500, 25200, 105));
        // 南广场旗杆（移到民居南侧与城门之间的道路上）
        decorations.add(new Decoration(DecoType.FLAG_POLE, 29200, 34800, 115));
        decorations.add(new Decoration(DecoType.FLAG_POLE, 30800, 34800, 115));

        // === 告示牌（商铺区、民居区、广场） ===
        // 北区小摊市场告示牌（移到小摊北侧道路上）
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 29000, 25300, 80));
        // 东区商铺区告示牌
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 32400, 26800, 80));
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 34500, 29800, 75));
        // 西区商铺区告示牌
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 25600, 26800, 80));
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 27600, 30200, 75));
        // 南区民居告示牌
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 29800, 32200, 85));
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 30200, 34600, 80));
        // 南广场告示牌
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 28800, 33600, 85));
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 31200, 33600, 85));
        // 内城前告示牌
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 29400, 30200, 90));
        decorations.add(new Decoration(DecoType.NOTICE_BOARD, 30600, 30200, 90));

        // === 额外灯笼补充（商铺区、城门附近） ===
        decorations.add(new Decoration(DecoType.LANTERN, 32200, 27400, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 34600, 27400, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 25400, 27400, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 27800, 27400, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 32200, 28800, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 25400, 28800, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 32200, 30600, 22));
        decorations.add(new Decoration(DecoType.LANTERN, 25400, 30400, 22));
        // 南门附近灯笼
        decorations.add(new Decoration(DecoType.LANTERN, CCX - GATE_WIDTH/2 - 50, CY2 - WALL_THICK - 50, 25));
        decorations.add(new Decoration(DecoType.LANTERN, CCX + GATE_WIDTH/2 + 50, CY2 - WALL_THICK - 50, 25));
        // 北门附近灯笼
        decorations.add(new Decoration(DecoType.LANTERN, CCX - GATE_WIDTH/2 - 50, CY1 + WALL_THICK + 50, 25));
        decorations.add(new Decoration(DecoType.LANTERN, CCX + GATE_WIDTH/2 + 50, CY1 + WALL_THICK + 50, 25));
        // 西门附近灯笼
        decorations.add(new Decoration(DecoType.LANTERN, CX1 + WALL_THICK + 50, CCY - GATE_WIDTH/2 - 50, 25));
        decorations.add(new Decoration(DecoType.LANTERN, CX1 + WALL_THICK + 50, CCY + GATE_WIDTH/2 + 50, 25));
        // 东门附近灯笼
        decorations.add(new Decoration(DecoType.LANTERN, CX2 - WALL_THICK - 50, CCY - GATE_WIDTH/2 - 50, 25));
        decorations.add(new Decoration(DecoType.LANTERN, CX2 - WALL_THICK - 50, CCY + GATE_WIDTH/2 + 50, 25));

        // === 城墙内侧行道树（沿墙根，放在建筑列间隙中避免重叠） ===
        // 北墙内侧（酒楼/客栈间隙）
        decorations.add(new Decoration(DecoType.TREE, 25500, 25100, 95));
        decorations.add(new Decoration(DecoType.TREE, 30700, 25200, 100));
        decorations.add(new Decoration(DecoType.TREE, 32500, 25200, 95));
        decorations.add(new Decoration(DecoType.TREE, 33100, 25200, 100));
        // 南墙内侧
        decorations.add(new Decoration(DecoType.TREE, 25600, 34600, 90));
        decorations.add(new Decoration(DecoType.TREE, 34400, 34600, 95));
        // 西墙内侧
        decorations.add(new Decoration(DecoType.TREE, 25400, 30000, 90));
        decorations.add(new Decoration(DecoType.TREE, 25400, 31500, 95));
        // 东墙内侧
        decorations.add(new Decoration(DecoType.TREE, 34600, 30000, 90));
        decorations.add(new Decoration(DecoType.TREE, 34600, 31500, 95));

        // === 内城花园额外装饰 ===
        decorations.add(new Decoration(DecoType.FLOWER_BED, 29000, 31200, 120));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 31000, 31200, 120));
        decorations.add(new Decoration(DecoType.FENCE, 28600, 31000, 55));
        decorations.add(new Decoration(DecoType.FENCE, 31400, 31000, 55));
        decorations.add(new Decoration(DecoType.WELL, 29600, 31200, 120));
        decorations.add(new Decoration(DecoType.STONE_TABLE, 30400, 31200, 80));

        // === 主干道十字路口的装饰 ===
        // 路口四角各放一个花坛
        decorations.add(new Decoration(DecoType.FLOWER_BED, CCX - ROAD_HALF - 200, CCY - ROAD_HALF - 200, 130));
        decorations.add(new Decoration(DecoType.FLOWER_BED, CCX + ROAD_HALF + 200, CCY - ROAD_HALF - 200, 130));
        decorations.add(new Decoration(DecoType.FLOWER_BED, CCX - ROAD_HALF - 200, CCY + ROAD_HALF + 200, 130));
        decorations.add(new Decoration(DecoType.FLOWER_BED, CCX + ROAD_HALF + 200, CCY + ROAD_HALF + 200, 130));
    }

    // ==================== 建筑填充工具 ====================

    /**
     * 在指定矩形区域内按网格排列建筑
     */
    private void fillBuildings(int zoneX1, int zoneY1, int zoneX2, int zoneY2,
                               int bw, int bh, int gapX, int gapY,
                               BuildingType type, String[] names) {
        int idx = 0;
        int curY = zoneY1;
        while (curY + bh <= zoneY2) {
            int curX = zoneX1;
            while (curX + bw <= zoneX2) {
                String name = names[idx % names.length];
                buildings.add(new Building(type, curX, curY, bw, bh, name));
                curX += bw + gapX;
                idx++;
            }
            curY += bh + gapY;
        }
    }

    /** 商铺专用：每个名称只使用一次，使用与民居相同的紧凑间距 */
    private void placeShops(int zoneX1, int zoneY1, int zoneX2, int zoneY2,
                            int bw, int bh, int gapX, int gapY,
                            BuildingType type, String[] names) {
        int count = names.length;
        int zoneW = zoneX2 - zoneX1;
        int cols = Math.max(1, Math.min(count, zoneW / (bw + gapX)));
        int stepX = cols > 1 ? (zoneW - bw) / (cols - 1) : 0;
        int stepY = bh + gapY;
        for (int i = 0; i < count; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = zoneX1 + col * stepX;
            int y = zoneY1 + row * stepY;
            buildings.add(new Building(type, x, y, bw, bh, names[i]));
        }
    }

    // ==================== 渲染 ====================

    public void draw(Canvas canvas, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        if (cityBounds == null) return;

        // 视锥范围
        float viewLeft = cameraX;
        float viewTop = cameraY;
        float viewRight = cameraX + screenWidth;
        float viewBottom = cameraY + screenHeight;

        // 如果完全看不到城市区域就跳过
        if (viewRight < CX1 || viewLeft > CX2 || viewBottom < CY1 || viewTop > CY2) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 1. 绘制城市地面背景
        drawCityGround(canvas, paint, cameraX, cameraY, screenWidth, screenHeight);

        // 2. 绘制城墙
        drawWalls(canvas, paint, cameraX, cameraY);

        // 3. 按 Y 排序绘制建筑
        buildings.sort((a, b) -> Integer.compare(a.y + a.height, b.y + b.height));
        for (Building b : buildings) {
            if (b.x + b.width < viewLeft || b.x > viewRight ||
                    b.y + b.height < viewTop || b.y > viewBottom) continue;
            drawBuilding(canvas, paint, b, b.x - cameraX, b.y - cameraY);
        }

        // 4. 绘制装饰物
        for (Decoration d : decorations) {
            if (d.x + d.size < viewLeft || d.x - d.size > viewRight ||
                    d.y + d.size < viewTop || d.y - d.size > viewBottom) continue;
            drawDecoration(canvas, paint, d, d.x - cameraX, d.y - cameraY);
        }

        // 5. 绘制城门标记
        drawGates(canvas, paint, cameraX, cameraY);
    }

    // ==================== 地面渲染 ====================

    private void drawCityGround(Canvas canvas, Paint paint, float camX, float camY, int sw, int sh) {
        // 城市地面：浅土色
        paint.setColor(Color.rgb(210, 195, 170));
        float gx = Math.max(CX1, camX) - camX;
        float gy = Math.max(CY1, camY) - camY;
        float gx2 = Math.min(CX2, camX + sw) - camX;
        float gy2 = Math.min(CY2, camY + sh) - camY;
        canvas.drawRect(gx, gy, gx2, gy2, paint);

        // 内城地面：青石板色
        paint.setColor(Color.rgb(180, 175, 165));
        canvas.drawRect(INNER_X1 - camX, INNER_Y1 - camY, INNER_X2 - camX, INNER_Y2 - camY, paint);

        // 主干道：石板路色
        paint.setColor(Color.rgb(170, 165, 155));
        // 南北主路
        canvas.drawRect(CCX - ROAD_HALF - camX, CY1 - camY, CCX + ROAD_HALF - camX, CY2 - camY, paint);
        // 东西主路
        canvas.drawRect(CX1 - camX, CCY - ROAD_HALF - camY, CX2 - camX, CCY + ROAD_HALF - camY, paint);
    }

    // ==================== 城墙渲染 ====================

    private void drawWalls(Canvas canvas, Paint paint, float camX, float camY) {
        // 城墙主体：深灰色
        paint.setColor(Color.rgb(100, 95, 90));
        for (Rect wall : wallSegments) {
            if (wall.right < camX || wall.left > camX + canvas.getWidth() ||
                    wall.bottom < camY || wall.top > camY + canvas.getHeight()) continue;
            canvas.drawRect(wall.left - camX, wall.top - camY, wall.right - camX, wall.bottom - camY, paint);
        }

        // 城墙顶部装饰线
        paint.setColor(Color.rgb(120, 110, 100));
        for (Rect wall : wallSegments) {
            if (wall.right < camX || wall.left > camX + canvas.getWidth() ||
                    wall.bottom < camY || wall.top > camY + canvas.getHeight()) continue;
            // 顶部垛口效果
            int crenelW = 30, crenelGap = 20;
            boolean horizontal = (wall.width() > wall.height());
            if (horizontal) {
                for (int cx = wall.left; cx < wall.right; cx += crenelW + crenelGap) {
                    canvas.drawRect(cx - camX, wall.top - camY - 8,
                            cx + crenelW - camX, wall.top - camY, paint);
                }
            } else {
                for (int cy = wall.top; cy < wall.bottom; cy += crenelW + crenelGap) {
                    canvas.drawRect(wall.left - camX - 8, cy - camY,
                            wall.left - camX, cy + crenelW - camY, paint);
                }
            }
        }
    }

    // ==================== 城门渲染 ====================

    private void drawGates(Canvas canvas, Paint paint, float camX, float camY) {
        // 城门地面（可通行区域标记）
        paint.setColor(Color.rgb(190, 180, 160));
        for (Rect gate : gateOpenings) {
            canvas.drawRect(gate.left - camX, gate.top - camY, gate.right - camX, gate.bottom - camY, paint);
        }

        // 城门两侧门柱
        paint.setColor(Color.rgb(80, 70, 60));
        for (Rect gate : gateOpenings) {
            int pillarW = 40;
            if (gate.width() > gate.height()) {
                // 南北墙的门
                canvas.drawRect(gate.left - camX - pillarW, gate.top - camY - 30,
                        gate.left - camX, gate.bottom - camY + 30, paint);
                canvas.drawRect(gate.right - camX, gate.top - camY - 30,
                        gate.right - camX + pillarW, gate.bottom - camY + 30, paint);
            } else {
                // 东西墙的门
                canvas.drawRect(gate.left - camX - 30, gate.top - camY - pillarW,
                        gate.right - camX + 30, gate.top - camY, paint);
                canvas.drawRect(gate.left - camX - 30, gate.bottom - camY,
                        gate.right - camX + 30, gate.bottom - camY + pillarW, paint);
            }
        }
    }

    // ==================== 建筑渲染 ====================

    private void drawBuilding(Canvas canvas, Paint paint, Building b, float dx, float dy) {
        float w = b.width, h = b.height;

        switch (b.type) {
            case PALACE:
                drawPalace(canvas, paint, b, dx, dy, w, h);
                break;
            case GOVERNMENT:
                drawGovernment(canvas, paint, b, dx, dy, w, h);
                break;
            case INN:
                drawInn(canvas, paint, b, dx, dy, w, h);
                break;
            case TEA_HOUSE:
                drawTeaHouse(canvas, paint, b, dx, dy, w, h);
                break;
            case PHARMACY:
                drawPharmacy(canvas, paint, b, dx, dy, w, h);
                break;
            case WEAPON_SHOP:
                drawWeaponShop(canvas, paint, b, dx, dy, w, h);
                break;
            case ARMOR_SHOP:
                drawArmorShop(canvas, paint, b, dx, dy, w, h);
                break;
            case BLACKSMITH:
                drawBlacksmith(canvas, paint, b, dx, dy, w, h);
                break;
            case GENERAL_STORE:
            case GEM_SHOP:
                drawGeneralStore(canvas, paint, b, dx, dy, w, h);
                break;
            case MARKET_STALL:
                drawMarketStall(canvas, paint, b, dx, dy, w, h);
                break;
            case HOUSE:
                drawHouse(canvas, paint, b, dx, dy, w, h, Color.rgb(200, 180, 150));
                break;
            case TALL_HOUSE:
                drawTallHouse(canvas, paint, b, dx, dy, w, h);
                break;
            case CLOCK_TOWER:
                drawClockTower(canvas, paint, b, dx, dy, w, h);
                break;
            case GARDEN_PAVILION:
                drawPavilion(canvas, paint, b, dx, dy, w, h);
                break;
            case STABLE:
                drawStable(canvas, paint, b, dx, dy, w, h);
                break;
            case WAREHOUSE:
                drawWarehouse(canvas, paint, b, dx, dy, w, h);
                break;
        }

        // 建筑名称标签（如果建筑够大且名字不为空）
        if (b.name != null && w > 300) {
            paint.setTextSize(25);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setShadowLayer(2, 1, 1, Color.BLACK);
            canvas.drawText(b.name, dx + w / 2, dy + h / 2 + 5, paint);
            paint.clearShadowLayer();
        }
    }

    // --- 各类型建筑绘制 ---

    private void drawPalace(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 基座
        paint.setColor(Color.rgb(160, 150, 130));
        canvas.drawRect(x - 10, y + h * 0.3f, x + w + 10, y + h, paint);
        // 主体
        paint.setColor(Color.rgb(180, 50, 50));
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        // 屋顶
        // 石基
        paint.setColor(Color.rgb(170, 165, 150));
        canvas.drawRect(x - 15, y + h - 15, x + w + 15, y + h, paint);
        paint.setColor(Color.rgb(155, 150, 135));
        canvas.drawRect(x - 15, y + h - 15, x + w + 15, y + h - 10, paint);
        // 屋顶
        paint.setColor(Color.rgb(200, 170, 50));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 20, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 20, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawRoofTiles(canvas, paint, x - 20, y + h * 0.3f, w + 40);
        // === 气派大门 ===
        float cx = x + w / 2f;
        // 门框（石质大门套）
        paint.setColor(Color.rgb(170, 160, 140));
        canvas.drawRect(cx - 180, y + h - 340, cx + 180, y + h, paint);
        // 门框内凹阴影
        paint.setColor(Color.rgb(140, 130, 110));
        canvas.drawRect(cx - 160, y + h - 320, cx + 160, y + h, paint);
        // 门洞（深色内部）
        paint.setColor(Color.rgb(50, 20, 10));
        canvas.drawRect(cx - 150, y + h - 300, cx + 150, y + h, paint);
        // 左门扇
        paint.setColor(Color.rgb(140, 45, 35));
        canvas.drawRect(cx - 148, y + h - 296, cx - 4, y + h, paint);
        // 右门扇
        canvas.drawRect(cx + 4, y + h - 296, cx + 148, y + h, paint);
        // 门扇中线
        paint.setColor(Color.rgb(100, 30, 20));
        canvas.drawRect(cx - 3, y + h - 296, cx + 3, y + h, paint);
        // 门钉（金色，5行×4列 每扇门）
        paint.setColor(Color.rgb(220, 190, 60));
        for (int row = 0; row < 5; row++) {
            float nailY = y + h - 270 + row * 52;
            // 左门钉
            for (int col = 0; col < 4; col++) {
                float nailX = cx - 130 + col * 34;
                canvas.drawCircle(nailX, nailY, 5, paint);
            }
            // 右门钉
            for (int col = 0; col < 4; col++) {
                float nailX = cx + 22 + col * 34;
                canvas.drawCircle(nailX, nailY, 5, paint);
            }
        }
        // 门环（金色大圆环）
        paint.setColor(Color.rgb(220, 190, 60));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(cx - 40, y + h - 150, 14, paint);
        canvas.drawCircle(cx + 40, y + h - 150, 14, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        // 门环底座
        paint.setColor(Color.rgb(180, 150, 40));
        canvas.drawCircle(cx - 40, y + h - 165, 6, paint);
        canvas.drawCircle(cx + 40, y + h - 165, 6, paint);
        // 门额匾（"金陵府衙"）
        paint.setColor(Color.rgb(160, 130, 40));
        canvas.drawRect(cx - 120, y + h - 380, cx + 120, y + h - 340, paint);
        paint.setColor(Color.rgb(60, 20, 10));
        canvas.drawRect(cx - 116, y + h - 376, cx + 116, y + h - 344, paint);
        paint.setColor(Color.rgb(240, 220, 120));
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("金陵府衙", cx, y + h - 350, paint);
        // 门两侧灯笼
        drawBuildingLantern(canvas, paint, cx - 200, y + h - 300, 12, 18);
        drawBuildingLantern(canvas, paint, cx + 188, y + h - 300, 12, 18);
        // 柱子
        drawPillars(canvas, paint, x, y + h * 0.3f, w, h * 0.7f, 6);
        // 台阶
        drawSteps(canvas, paint, x + w / 2f, y + h, 6);
    }

    private void drawGovernment(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(190, 175, 150));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        // 屋顶
        paint.setColor(Color.rgb(80, 80, 100));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawRoofTiles(canvas, paint, x - 8, y + h * 0.3f, w + 16);
        // 门
        paint.setColor(Color.rgb(100, 60, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
        // 立柱
        drawPillars(canvas, paint, x, y + h * 0.3f, w, h * 0.7f, 4);
        // 台阶
        drawSteps(canvas, paint, x + w / 2f, y + h, 4);
    }

    private void drawInn(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(210, 180, 130));
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.25f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(150, 60, 30));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 10, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 10, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -10, 0.3f);
        drawRoofTiles(canvas, paint, x - 10, y + h * 0.3f, w + 20);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.32f, b.name);
        // 酒旗
        paint.setColor(Color.rgb(200, 50, 50));
        canvas.drawRect(x + w - 30, y + 5, x + w - 20, y + h * 0.4f, paint);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawTeaHouse(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(220, 200, 170));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.3f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(100, 80, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.35f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.35f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -8, 0.35f);
        drawRoofTiles(canvas, paint, x - 8, y + h * 0.35f, w + 16);
        drawCurtain(canvas, paint, x, y + h * 0.35f, w);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.37f, b.name);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawPharmacy(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(200, 190, 170));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.3f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(60, 100, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -8, 0.3f);
        drawRoofTiles(canvas, paint, x - 8, y + h * 0.3f, w + 16);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.32f, b.name);
        // 药十字标志
        paint.setColor(Color.rgb(200, 50, 50));
        float cx = x + w / 2, cy = y + h * 0.55f;
        canvas.drawRect(cx - 15, cy - 5, cx + 15, cy + 5, paint);
        canvas.drawRect(cx - 5, cy - 15, cx + 5, cy + 15, paint);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawWeaponShop(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(170, 160, 145));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.3f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(90, 90, 100));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -5, 0.3f);
        drawRoofTiles(canvas, paint, x - 5, y + h * 0.3f, w + 10);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.32f, b.name);
        // 剑架装饰
        paint.setColor(Color.rgb(150, 150, 160));
        paint.setStrokeWidth(2);
        canvas.drawLine(x + 20, y + h * 0.4f, x + 20, y + h * 0.8f, paint);
        canvas.drawLine(x + 10, y + h * 0.5f, x + 30, y + h * 0.5f, paint);
        paint.setStrokeWidth(1);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawArmorShop(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(180, 175, 160));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.3f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(70, 80, 100));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -5, 0.3f);
        drawRoofTiles(canvas, paint, x - 5, y + h * 0.3f, w + 10);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.32f, b.name);
        // 盾牌装饰
        paint.setColor(Color.rgb(120, 120, 140));
        canvas.drawCircle(x + w / 2, y + h * 0.55f, 20, paint);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawBlacksmith(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(140, 130, 120));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.3f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(60, 55, 50));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -5, 0.3f);
        drawRoofTiles(canvas, paint, x - 5, y + h * 0.3f, w + 10);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.32f, b.name);
        // 烟囱
        paint.setColor(Color.rgb(80, 70, 60));
        canvas.drawRect(x + w - 40, y - 20, x + w - 20, y + h * 0.3f, paint);
        // 火光
        paint.setColor(Color.argb(60, 255, 100, 0));
        canvas.drawCircle(x + w / 2, y + h * 0.7f, 30, paint);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawGeneralStore(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(210, 195, 165));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.3f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(130, 100, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -8, 0.3f);
        drawRoofTiles(canvas, paint, x - 8, y + h * 0.3f, w + 16);
        if (b.name != null) drawSignboard(canvas, paint, x + w / 2f, y + h * 0.32f, b.name);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawMarketStall(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 简易摊位：棚子 + 台面
        paint.setColor(Color.rgb(180, 140, 100));
        canvas.drawRect(x, y + h * 0.5f, x + w, y + h, paint);
        // 棚顶
        int stallColor = Color.rgb(200, 80 + (int) (x % 100), 50);
        paint.setColor(stallColor);
        canvas.drawRect(x - 5, y + h * 0.3f, x + w + 5, y + h * 0.55f, paint);
        // 支柱
        paint.setColor(Color.rgb(120, 80, 40));
        canvas.drawRect(x, y + h * 0.3f, x + 4, y + h, paint);
        canvas.drawRect(x + w - 4, y + h * 0.3f, x + w, y + h, paint);
    }

    // ==================== 建筑装饰辅助方法 ====================

    private void drawWallBase(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(140, 125, 105));
        canvas.drawRect(x, y + h - 8, x + w, y + h, paint);
    }

    private void drawWallLines(Canvas canvas, Paint paint, float x, float wallTop, float w, float h) {
        paint.setColor(Color.argb(20, 0, 0, 0));
        paint.setStrokeWidth(1);
        float wallH = h;
        for (int i = 1; i < 4; i++) {
            float ly = wallTop + wallH * i / 4f;
            canvas.drawLine(x + 3, ly, x + w - 3, ly, paint);
        }
    }

    private void drawEnhancedWindow(Canvas canvas, Paint paint, float wx, float wy, float ww, float wh) {
        paint.setColor(Color.rgb(180, 210, 230));
        canvas.drawRect(wx, wy, wx + ww, wy + wh, paint);
        paint.setColor(Color.rgb(80, 55, 35));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(wx, wy, wx + ww, wy + wh, paint);
        canvas.drawLine(wx + ww / 2, wy, wx + ww / 2, wy + wh, paint);
        canvas.drawLine(wx, wy + wh / 2, wx + ww, wy + wh / 2, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawEnhancedDoor(Canvas canvas, Paint paint, float x, float y, float w, float h, Building b) {
        float cx = x + w / 2f;
        Random bRand = new Random(b.x * 53L + b.y * 97);
        // 门
        paint.setColor(Color.rgb(80, 40, 20));
        canvas.drawRect(cx - 50, y + h - 80, cx + 50, y + h, paint);
        // 门框
        paint.setColor(Color.rgb(60, 30, 15));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(cx - 50, y + h - 80, cx + 50, y + h, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        // 门环
        paint.setColor(Color.rgb(180, 160, 60));
        canvas.drawCircle(cx - 15, y + h - 40, 4, paint);
        canvas.drawCircle(cx + 15, y + h - 40, 4, paint);
        // 窗户
        float winW = w * 0.12f;
        float winH = h * 0.1f;
        float winY = y + h * 0.2f;
        drawEnhancedWindow(canvas, paint, x + w * 0.15f, winY, winW, winH);
        drawEnhancedWindow(canvas, paint, x + w * 0.73f, winY, winW, winH);
        // 灯笼（70%概率）
        if (bRand.nextFloat() < 0.7f) {
            float lw = 7, lh = 10;
            drawBuildingLantern(canvas, paint, cx - 58, y + h - 75, lw, lh);
            drawBuildingLantern(canvas, paint, cx + 58, y + h - 75, lw, lh);
        }
        // 花盆（50%概率）
        if (bRand.nextFloat() < 0.5f) {
            drawBuildingFlowerPot(canvas, paint, x, y, w, h, bRand);
        }
    }

    private void drawBuildingFlowerPot(Canvas canvas, Paint paint, float x, float y, float w, float h, Random rand) {
        float potX = x + w * (rand.nextBoolean() ? 0.1f : 0.88f);
        float potY = y + h - 20;
        paint.setColor(Color.rgb(160, 80, 40));
        canvas.drawRect(potX - 9, potY, potX + 9, potY + 18, paint);
        paint.setColor(Color.rgb(140, 65, 30));
        canvas.drawRect(potX - 11, potY, potX + 11, potY + 4, paint);
        int flowerColor = rand.nextInt(3) == 0 ? Color.rgb(255, 80, 80)
                : (rand.nextInt(2) == 0 ? Color.rgb(255, 200, 60) : Color.rgb(220, 100, 200));
        paint.setColor(flowerColor);
        canvas.drawCircle(potX, potY - 7, 8, paint);
        paint.setColor(Color.rgb(255, 230, 100));
        canvas.drawCircle(potX, potY - 7, 3, paint);
        paint.setColor(Color.rgb(40, 130, 40));
        canvas.drawRect(potX - 2, potY - 3, potX + 2, potY + 2, paint);
    }

    private void drawBuildingLantern(Canvas canvas, Paint paint, float lx, float ly, float lw, float lh) {
        paint.setColor(Color.rgb(60, 30, 15));
        paint.setStrokeWidth(2);
        canvas.drawLine(lx, ly - 10, lx, ly, paint);
        paint.setStrokeWidth(1);
        paint.setColor(Color.rgb(220, 40, 40));
        canvas.drawOval(lx - lw, ly, lx + lw, ly + lh * 2, paint);
        paint.setColor(Color.rgb(200, 170, 40));
        canvas.drawRect(lx - lw, ly, lx + lw, ly + 3, paint);
        canvas.drawRect(lx - lw, ly + lh * 2 - 3, lx + lw, ly + lh * 2, paint);
        paint.setColor(Color.argb(40, 255, 200, 150));
        canvas.drawOval(lx - lw * 0.5f, ly + 3, lx + lw * 0.3f, ly + lh, paint);
        paint.setColor(Color.rgb(200, 170, 40));
        canvas.drawRect(lx - 1, ly + lh * 2, lx + 1, ly + lh * 2 + 5, paint);
    }

    private void drawEaves(Canvas canvas, Paint paint, float x, float y, float w, float h, int overhang, float roofBase) {
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawRect(x + overhang, y + h * roofBase, x + w - overhang, y + h * roofBase + 5, paint);
    }

    // ==================== 豪华装饰辅助方法 ====================

    private void drawRoofTiles(Canvas canvas, Paint paint, float x, float roofBaseY, float w) {
        float tileW = 14;
        paint.setColor(Color.argb(35, 0, 0, 0));
        paint.setStrokeWidth(1);
        for (float tx = x; tx < x + w; tx += tileW) {
            canvas.drawArc(tx, roofBaseY - 3, tx + tileW, roofBaseY + 5, 0, 180, false, paint);
        }
    }

    private void drawPillars(Canvas canvas, Paint paint, float x, float top, float w, float h, int count) {
        for (int i = 0; i < count; i++) {
            float px = x + w * (i + 1) / (count + 1);
            paint.setColor(Color.rgb(140, 45, 35));
            canvas.drawRect(px - 4, top, px + 4, top + h, paint);
            paint.setColor(Color.rgb(170, 155, 55));
            canvas.drawRect(px - 6, top, px + 6, top + 5, paint);
            canvas.drawRect(px - 6, top + h - 5, px + 6, top + h, paint);
        }
    }

    private void drawSignboard(Canvas canvas, Paint paint, float cx, float y, String name) {
        float sw = 70, sh = 22;
        paint.setColor(Color.rgb(90, 50, 25));
        canvas.drawRect(cx - sw / 2, y, cx + sw / 2, y + sh, paint);
        paint.setColor(Color.rgb(70, 35, 15));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(cx - sw / 2, y, cx + sw / 2, y + sh, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setColor(Color.rgb(220, 200, 150));
        paint.setTextSize(14);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(name, cx, y + sh * 0.72f, paint);
    }

    private void drawLatticeWindow(Canvas canvas, Paint paint, float wx, float wy, float ww, float wh) {
        paint.setColor(Color.rgb(180, 210, 230));
        canvas.drawRect(wx, wy, wx + ww, wy + wh, paint);
        paint.setColor(Color.rgb(80, 55, 35));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(wx, wy, wx + ww, wy + wh, paint);
        paint.setStrokeWidth(1);
        float stepX = ww / 3f, stepY = wh / 3f;
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(wx + i * stepX, wy, wx + i * stepX, wy + wh, paint);
            canvas.drawLine(wx, wy + i * stepY, wx + ww, wy + i * stepY, paint);
        }
        paint.setColor(Color.rgb(180, 160, 60));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float dx = wx + (i + 0.5f) * stepX;
                float dy = wy + (j + 0.5f) * stepY;
                canvas.drawCircle(dx, dy, 2, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    private void drawSteps(Canvas canvas, Paint paint, float cx, float bottom, int count) {
        for (int i = 0; i < count; i++) {
            float sw = 50 + i * 12;
            float sy = bottom + i * 5;
            paint.setColor(Color.rgb(155 - i * 10, 150 - i * 10, 140 - i * 10));
            canvas.drawRect(cx - sw / 2, sy, cx + sw / 2, sy + 6, paint);
        }
    }

    private void drawCurtain(Canvas canvas, Paint paint, float x, float roofBaseY, float w) {
        float curtainH = 18;
        paint.setColor(Color.rgb(180, 45, 35));
        canvas.drawRect(x, roofBaseY, x + w, roofBaseY + curtainH, paint);
        paint.setColor(Color.rgb(160, 35, 25));
        float scallopW = 16;
        for (float sx = x; sx < x + w; sx += scallopW) {
            canvas.drawArc(sx, roofBaseY + curtainH - 4, sx + scallopW, roofBaseY + curtainH + 8, 0, 180, false, paint);
        }
    }

    private void drawHouse(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h, int wallColor) {
        // 墙体
        paint.setColor(wallColor);
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.25f, w, h);
        // 屋顶
        paint.setColor(Color.rgb(100, 60, 40));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.2f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.2f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -5, 0.35f);
        drawRoofTiles(canvas, paint, x - 5, y + h * 0.2f, w + 10);
        drawLatticeWindow(canvas, paint, x + w * 0.15f, y + h * 0.2f, w * 0.14f, h * 0.12f);
        drawLatticeWindow(canvas, paint, x + w * 0.71f, y + h * 0.2f, w * 0.14f, h * 0.12f);
        drawSteps(canvas, paint, x + w / 2f, y + h, 3);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawTallHouse(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 二层楼墙体
        paint.setColor(Color.rgb(195, 175, 145));
        canvas.drawRect(x, y + h * 0.15f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.15f, w, h);
        // 楼层分隔线
        paint.setColor(Color.rgb(120, 90, 60));
        canvas.drawRect(x, y + h * 0.5f, x + w, y + h * 0.53f, paint);
        // 屋顶
        paint.setColor(Color.rgb(90, 55, 35));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.2f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.2f);
        roof.close();
        canvas.drawPath(roof, paint);
        drawEaves(canvas, paint, x, y, w, h, -8, 0.2f);
        drawRoofTiles(canvas, paint, x - 8, y + h * 0.2f, w + 16);
        drawLatticeWindow(canvas, paint, x + 15, y + h * 0.18f, w * 0.12f, h * 0.1f);
        drawLatticeWindow(canvas, paint, x + w - 15 - w * 0.12f, y + h * 0.18f, w * 0.12f, h * 0.1f);
        drawSteps(canvas, paint, x + w / 2f, y + h, 4);
        // 窗户
        float winW = w * 0.12f;
        float winH = h * 0.1f;
        drawEnhancedWindow(canvas, paint, x + 15, y + h * 0.18f, winW, winH);
        drawEnhancedWindow(canvas, paint, x + w - 15 - winW, y + h * 0.18f, winW, winH);
        // 门和装饰
        drawEnhancedDoor(canvas, paint, x, y, w, h, b);
    }

    private void drawClockTower(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 塔基
        paint.setColor(Color.rgb(130, 120, 110));
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y + h * 0.25f, w, h * 0.6f);
        drawWallLines(canvas, paint, x, y + h * 0.25f, w, h * 0.6f);
        // 塔身
        paint.setColor(Color.rgb(110, 100, 90));
        canvas.drawRect(x + w * 0.15f, y + h * 0.15f, x + w * 0.85f, y + h * 0.45f, paint);
        // 塔顶
        paint.setColor(Color.rgb(180, 150, 50));
        android.graphics.Path spire = new android.graphics.Path();
        spire.moveTo(x + w * 0.2f, y + h * 0.15f);
        spire.lineTo(x + w / 2, y);
        spire.lineTo(x + w * 0.8f, y + h * 0.15f);
        spire.close();
        canvas.drawPath(spire, paint);
        // 钟面
        paint.setColor(Color.rgb(240, 230, 200));
        canvas.drawCircle(x + w / 2, y + h * 0.18f, Math.min(w, h) * 0.12f, paint);
    }

    private void drawPavilion(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 亭子底座
        paint.setColor(Color.rgb(170, 160, 140));
        canvas.drawRect(x, y + h * 0.6f, x + w, y + h, paint);
        // 柱子
        paint.setColor(Color.rgb(150, 50, 50));
        canvas.drawRect(x + 5, y + h * 0.18f, x + 10, y + h, paint);
        canvas.drawRect(x + w - 10, y + h * 0.18f, x + w - 5, y + h, paint);
        // 顶
        paint.setColor(Color.rgb(60, 100, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 10, y + h * 0.2f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 10, y + h * 0.2f);
        roof.close();
        canvas.drawPath(roof, paint);
    }

    private void drawStable(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(160, 130, 90));
        canvas.drawRect(x, y + h * 0.18f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.18f, w, h);
        // 横梁
        paint.setColor(Color.rgb(120, 90, 60));
        canvas.drawRect(x - 5, y + h * 0.18f, x + w + 5, y + h * 0.2f, paint);
        drawEaves(canvas, paint, x, y, w, h, -5, 0.25f);
        // 门和装饰
        float cx = x + w / 2f;
        paint.setColor(Color.rgb(80, 40, 20));
        canvas.drawRect(cx - 55, y + h - 80, cx + 55, y + h, paint);
        paint.setColor(Color.rgb(60, 30, 15));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(cx - 55, y + h - 80, cx + 55, y + h, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        // 灯笼
        Random sRand = new Random(b.x * 53L + b.y * 97);
        if (sRand.nextFloat() < 0.7f) {
            drawBuildingLantern(canvas, paint, cx - 63, y + h - 75, 7, 10);
            drawBuildingLantern(canvas, paint, cx + 63, y + h - 75, 7, 10);
        }
    }

    private void drawWarehouse(Canvas canvas, Paint paint, Building b, float x, float y, float w, float h) {
        // 墙体
        paint.setColor(Color.rgb(150, 140, 125));
        canvas.drawRect(x, y + h * 0.18f, x + w, y + h, paint);
        drawWallBase(canvas, paint, x, y, w, h);
        drawWallLines(canvas, paint, x, y + h * 0.18f, w, h);
        // 屋顶边缘
        paint.setColor(Color.rgb(100, 90, 80));
        canvas.drawRect(x - 3, y + h * 0.2f, x + w + 3, y + h * 0.18f, paint);
        drawEaves(canvas, paint, x, y, w, h, -3, 0.2f);
        // 门
        float cx = x + w / 2f;
        paint.setColor(Color.rgb(80, 70, 60));
        canvas.drawRect(cx - 60, y + h - 90, cx + 60, y + h, paint);
        paint.setColor(Color.rgb(60, 50, 40));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(cx - 60, y + h - 90, cx + 60, y + h, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    // ==================== 装饰物渲染 ====================

    private void drawDecoration(Canvas canvas, Paint paint, Decoration d, float dx, float dy) {
        switch (d.type) {
            case TREE: {
                // 用世界坐标做种子，保证同一棵树样式固定（不受摄像机移动影响）
                Random treeRand = new Random(d.x * 73 + d.y * 137);
                int tStyle = treeRand.nextInt(6);
                float tW = d.size;
                float tH = d.size * 1.4f;
                float trunkTop = dy + tH * 0.35f;
                float trunkW = tW * 0.08f;

                switch (tStyle) {
                    case 0: // 樟树/阔叶树：粗壮树干 + 多层圆形树冠
                        paint.setColor(Color.rgb(90, 60, 30));
                        canvas.drawRect(dx - trunkW * 1.3f, trunkTop, dx + trunkW * 1.3f, dy + tH, paint);
                        float canopyR = tW * 0.42f;
                        paint.setColor(Color.rgb(30, 120, 30));
                        canvas.drawCircle(dx - canopyR * 0.4f, trunkTop - canopyR * 0.2f, canopyR * 0.75f, paint);
                        canvas.drawCircle(dx + canopyR * 0.4f, trunkTop - canopyR * 0.1f, canopyR * 0.7f, paint);
                        paint.setColor(Color.rgb(40, 140, 40));
                        canvas.drawCircle(dx, trunkTop - canopyR * 0.5f, canopyR * 0.8f, paint);
                        paint.setColor(Color.argb(35, 120, 220, 80));
                        canvas.drawCircle(dx - canopyR * 0.2f, trunkTop - canopyR * 0.7f, canopyR * 0.35f, paint);
                        break;

                    case 1: // 松树：树干 + 三层三角
                        paint.setColor(Color.rgb(80, 55, 28));
                        canvas.drawRect(dx - trunkW, trunkTop, dx + trunkW, dy + tH, paint);
                        float pineW = tW * 0.45f;
                        float layerH = (trunkTop - dy) * 0.4f;
                        for (int i = 0; i < 3; i++) {
                            float ly = dy + i * layerH * 0.7f;
                            float lw = pineW * (1f - i * 0.2f);
                            paint.setColor(i == 0 ? Color.rgb(25, 100, 25) : Color.rgb(35, 125, 35));
                            android.graphics.Path pTri = new android.graphics.Path();
                            pTri.moveTo(dx - lw, ly + layerH);
                            pTri.lineTo(dx, ly);
                            pTri.lineTo(dx + lw, ly + layerH);
                            pTri.close();
                            canvas.drawPath(pTri, paint);
                        }
                        break;

                    case 2: // 柳树：树干 + 垂拱形树冠
                        paint.setColor(Color.rgb(85, 65, 35));
                        canvas.drawRect(dx - trunkW * 1.2f, trunkTop, dx + trunkW * 1.2f, dy + tH, paint);
                        float willowR = tW * 0.48f;
                        paint.setColor(Color.rgb(50, 130, 45));
                        canvas.drawOval(dx - willowR, trunkTop - willowR * 0.6f, dx + willowR, trunkTop + willowR * 0.3f, paint);
                        paint.setColor(Color.rgb(60, 140, 50));
                        paint.setStrokeWidth(2);
                        for (int i = 0; i < 5; i++) {
                            float bx = dx + (treeRand.nextFloat() - 0.5f) * willowR * 1.6f;
                            float by = trunkTop - willowR * 0.3f;
                            canvas.drawLine(bx, by, bx + (treeRand.nextFloat() - 0.5f) * 8, by + willowR * 0.8f, paint);
                        }
                        paint.setStrokeWidth(1);
                        break;

                    case 3: // 灌木丛：矮粗干 + 扁圆蓬松树冠
                        paint.setColor(Color.rgb(95, 70, 38));
                        canvas.drawRect(dx - trunkW * 1.5f, dy + tH * 0.5f, dx + trunkW * 1.5f, dy + tH, paint);
                        float bushR = tW * 0.35f;
                        paint.setColor(Color.rgb(45, 130, 40));
                        canvas.drawCircle(dx - bushR * 0.5f, dy + tH * 0.35f, bushR * 0.7f, paint);
                        canvas.drawCircle(dx + bushR * 0.5f, dy + tH * 0.35f, bushR * 0.65f, paint);
                        paint.setColor(Color.rgb(55, 150, 50));
                        canvas.drawCircle(dx, dy + tH * 0.25f, bushR * 0.75f, paint);
                        paint.setColor(Color.argb(30, 150, 230, 100));
                        canvas.drawCircle(dx - bushR * 0.15f, dy + tH * 0.18f, bushR * 0.3f, paint);
                        break;

                    case 4: // 果树：粗壮树干 + 茂密树冠 + 红黄果子
                        paint.setColor(Color.rgb(85, 55, 25));
                        canvas.drawRect(dx - trunkW * 1.4f, trunkTop, dx + trunkW * 1.4f, dy + tH, paint);
                        // 主枝干
                        paint.setStrokeWidth(3);
                        canvas.drawLine(dx - trunkW * 0.5f, trunkTop + 10, dx - tW * 0.25f, trunkTop - tH * 0.15f, paint);
                        canvas.drawLine(dx + trunkW * 0.5f, trunkTop + 15, dx + tW * 0.22f, trunkTop - tH * 0.12f, paint);
                        paint.setStrokeWidth(1);
                        // 树冠（多层深绿圆）
                        float fR = tW * 0.46f;
                        paint.setColor(Color.rgb(25, 105, 25));
                        canvas.drawCircle(dx - fR * 0.45f, trunkTop - fR * 0.15f, fR * 0.7f, paint);
                        canvas.drawCircle(dx + fR * 0.4f, trunkTop - fR * 0.1f, fR * 0.65f, paint);
                        paint.setColor(Color.rgb(35, 125, 30));
                        canvas.drawCircle(dx, trunkTop - fR * 0.45f, fR * 0.8f, paint);
                        canvas.drawCircle(dx - fR * 0.2f, trunkTop - fR * 0.6f, fR * 0.55f, paint);
                        canvas.drawCircle(dx + fR * 0.25f, trunkTop - fR * 0.55f, fR * 0.5f, paint);
                        // 高光
                        paint.setColor(Color.argb(30, 100, 200, 70));
                        canvas.drawCircle(dx - fR * 0.1f, trunkTop - fR * 0.75f, fR * 0.3f, paint);
                        // 果子（红/黄随机）
                        int fruitColor = treeRand.nextBoolean() ? Color.rgb(220, 40, 30) : Color.rgb(240, 180, 30);
                        int fruitColor2 = treeRand.nextBoolean() ? Color.rgb(200, 30, 20) : Color.rgb(255, 200, 50);
                        for (int fi = 0; fi < 8; fi++) {
                            float fx = dx + (treeRand.nextFloat() - 0.5f) * fR * 1.5f;
                            float fy = trunkTop - fR * 0.7f + treeRand.nextFloat() * fR * 1.1f;
                            float fr = 3 + treeRand.nextFloat() * 3;
                            paint.setColor(fi % 2 == 0 ? fruitColor : fruitColor2);
                            canvas.drawCircle(fx, fy, fr, paint);
                            // 果子高光
                            paint.setColor(Color.argb(60, 255, 255, 200));
                            canvas.drawCircle(fx - fr * 0.25f, fy - fr * 0.25f, fr * 0.35f, paint);
                        }
                        break;

                    case 5: // 红叶大树：粗壮干 + 宽大树冠 + 红/橙色叶片
                        paint.setColor(Color.rgb(75, 50, 28));
                        canvas.drawRect(dx - trunkW * 1.5f, trunkTop, dx + trunkW * 1.5f, dy + tH, paint);
                        // 枝干
                        paint.setStrokeWidth(3);
                        paint.setColor(Color.rgb(80, 55, 30));
                        canvas.drawLine(dx, trunkTop + 5, dx - tW * 0.3f, trunkTop - tH * 0.2f, paint);
                        canvas.drawLine(dx, trunkTop + 10, dx + tW * 0.28f, trunkTop - tH * 0.18f, paint);
                        canvas.drawLine(dx, trunkTop, dx - tW * 0.15f, trunkTop - tH * 0.3f, paint);
                        paint.setStrokeWidth(1);
                        // 大树冠（红/橙/黄渐变）
                        float rR = tW * 0.5f;
                        paint.setColor(Color.rgb(180, 50, 30));
                        canvas.drawCircle(dx - rR * 0.4f, trunkTop - rR * 0.2f, rR * 0.7f, paint);
                        canvas.drawCircle(dx + rR * 0.35f, trunkTop - rR * 0.15f, rR * 0.65f, paint);
                        paint.setColor(Color.rgb(200, 80, 30));
                        canvas.drawCircle(dx, trunkTop - rR * 0.5f, rR * 0.8f, paint);
                        paint.setColor(Color.rgb(220, 120, 40));
                        canvas.drawCircle(dx - rR * 0.25f, trunkTop - rR * 0.65f, rR * 0.5f, paint);
                        canvas.drawCircle(dx + rR * 0.2f, trunkTop - rR * 0.6f, rR * 0.45f, paint);
                        // 高光
                        paint.setColor(Color.argb(35, 255, 200, 100));
                        canvas.drawCircle(dx + rR * 0.1f, trunkTop - rR * 0.8f, rR * 0.3f, paint);
                        // 散落的小红叶点
                        for (int li = 0; li < 6; li++) {
                            float lx = dx + (treeRand.nextFloat() - 0.5f) * rR * 1.6f;
                            float ly = trunkTop - rR * 0.8f + treeRand.nextFloat() * rR * 1.2f;
                            int lc = treeRand.nextInt(3);
                            paint.setColor(lc == 0 ? Color.rgb(230, 60, 30) : (lc == 1 ? Color.rgb(240, 150, 40) : Color.rgb(200, 40, 25)));
                            canvas.drawCircle(lx, ly, 2.5f + treeRand.nextFloat() * 2, paint);
                        }
                        break;
                }
            }
            break;
            case LANTERN:
                // 灯笼柱
                paint.setColor(Color.rgb(100, 80, 60));
                canvas.drawRect(dx - 2, dy - 15, dx + 2, dy + 15, paint);
                // 灯笼
                paint.setColor(Color.rgb(220, 60, 40));
                canvas.drawOval(dx - 8, dy - 25, dx + 8, dy - 10, paint);
                // 光晕
                paint.setColor(Color.argb(30, 255, 200, 100));
                canvas.drawCircle(dx, dy - 17, 20, paint);
                break;
            case FLOWER_BED:
                paint.setColor(Color.argb(60, 34, 120, 34));
                canvas.drawCircle(dx, dy, d.size * 0.5f, paint);
                // 彩色花朵点
                Random flowerRand = new Random((int) (dx * 7 + dy * 13));
                for (int i = 0; i < 6; i++) {
                    float fx = dx + (flowerRand.nextFloat() - 0.5f) * d.size;
                    float fy = dy + (flowerRand.nextFloat() - 0.5f) * d.size;
                    int c = flowerRand.nextInt(3);
                    if (c == 0) paint.setColor(Color.rgb(255, 100, 100));
                    else if (c == 1) paint.setColor(Color.rgb(255, 200, 100));
                    else paint.setColor(Color.rgb(200, 100, 255));
                    canvas.drawCircle(fx, fy, 3, paint);
                }
                break;
            case STATUE:
                paint.setColor(Color.rgb(160, 155, 145));
                // 底座
                canvas.drawRect(dx - 20, dy + 10, dx + 20, dy + 30, paint);
                // 雕像
                canvas.drawCircle(dx, dy - 5, 15, paint);
                canvas.drawRect(dx - 8, dy + 5, dx + 8, dy + 15, paint);
                break;
            case STONE_TABLE: {
                // 石桌石凳：石板桌面 + 两条石腿 + 左右石凳
                float stW = d.size * 0.55f;
                float stH = d.size * 0.15f;
                // 石桌面（灰色椭圆）
                paint.setColor(Color.rgb(160, 155, 145));
                canvas.drawOval(dx - stW, dy - stH, dx + stW, dy + stH, paint);
                // 桌面高光
                paint.setColor(Color.argb(30, 220, 220, 210));
                canvas.drawOval(dx - stW * 0.6f, dy - stH * 0.5f, dx + stW * 0.3f, dy + stH * 0.2f, paint);
                // 两条石腿
                paint.setColor(Color.rgb(130, 125, 115));
                canvas.drawRect(dx - stW * 0.7f, dy + stH * 0.3f, dx - stW * 0.5f, dy + stH * 2.5f, paint);
                canvas.drawRect(dx + stW * 0.5f, dy + stH * 0.3f, dx + stW * 0.7f, dy + stH * 2.5f, paint);
                // 左石凳
                float benchOff = stW + d.size * 0.25f;
                paint.setColor(Color.rgb(145, 140, 130));
                canvas.drawRect(dx - benchOff - d.size * 0.15f, dy - stH * 0.3f, dx - benchOff + d.size * 0.15f, dy + stH * 0.8f, paint);
                // 左凳腿
                paint.setColor(Color.rgb(120, 115, 105));
                canvas.drawRect(dx - benchOff - d.size * 0.1f, dy + stH * 0.8f, dx - benchOff - d.size * 0.02f, dy + stH * 2.2f, paint);
                canvas.drawRect(dx - benchOff + d.size * 0.02f, dy + stH * 0.8f, dx - benchOff + d.size * 0.1f, dy + stH * 2.2f, paint);
                // 右石凳
                paint.setColor(Color.rgb(145, 140, 130));
                canvas.drawRect(dx + benchOff - d.size * 0.15f, dy - stH * 0.3f, dx + benchOff + d.size * 0.15f, dy + stH * 0.8f, paint);
                // 右凳腿
                paint.setColor(Color.rgb(120, 115, 105));
                canvas.drawRect(dx + benchOff - d.size * 0.1f, dy + stH * 0.8f, dx + benchOff - d.size * 0.02f, dy + stH * 2.2f, paint);
                canvas.drawRect(dx + benchOff + d.size * 0.02f, dy + stH * 0.8f, dx + benchOff + d.size * 0.1f, dy + stH * 2.2f, paint);
                // 桌上小物件（茶杯/棋子）
                Random tblRand = new Random((int)(d.x * 17 + d.y * 31));
                if (tblRand.nextBoolean()) {
                    // 茶杯
                    paint.setColor(Color.rgb(200, 190, 170));
                    canvas.drawCircle(dx - stW * 0.3f, dy - stH * 0.1f, d.size * 0.04f, paint);
                    canvas.drawCircle(dx + stW * 0.2f, dy + stH * 0.1f, d.size * 0.04f, paint);
                } else {
                    // 棋子
                    paint.setColor(Color.rgb(40, 40, 40));
                    canvas.drawCircle(dx - stW * 0.2f, dy, d.size * 0.035f, paint);
                    paint.setColor(Color.rgb(220, 215, 200));
                    canvas.drawCircle(dx + stW * 0.25f, dy - stH * 0.15f, d.size * 0.035f, paint);
                }
            }
            break;
            case CART: {
                // 手推车/板车：木质车斗 + 双轮 + 车把 + 货物
                float cW = d.size * 0.7f;
                float cH = d.size * 0.35f;
                float wheelR = d.size * 0.18f;
                // 车斗主体（棕色木板）
                paint.setColor(Color.rgb(150, 100, 50));
                canvas.drawRect(dx - cW, dy - cH, dx + cW * 0.5f, dy + cH * 0.3f, paint);
                // 车斗侧板
                paint.setColor(Color.rgb(130, 85, 40));
                canvas.drawRect(dx - cW, dy - cH - cH * 0.4f, dx - cW + 4, dy + cH * 0.3f, paint);
                canvas.drawRect(dx + cW * 0.5f - 4, dy - cH - cH * 0.4f, dx + cW * 0.5f, dy + cH * 0.3f, paint);
                // 车斗底板纹理线
                paint.setColor(Color.argb(25, 0, 0, 0));
                paint.setStrokeWidth(1);
                for (int i = 1; i < 4; i++) {
                    float ly = dy - cH + (cH * 1.3f) * i / 4f;
                    canvas.drawLine(dx - cW + 4, ly, dx + cW * 0.5f - 4, ly, paint);
                }
                // 车把（向右延伸）
                paint.setColor(Color.rgb(120, 80, 35));
                paint.setStrokeWidth(3);
                canvas.drawLine(dx + cW * 0.5f, dy - cH * 0.2f, dx + cW * 0.5f + cW * 0.5f, dy - cH * 0.5f, paint);
                canvas.drawLine(dx + cW * 0.5f, dy + cH * 0.1f, dx + cW * 0.5f + cW * 0.5f, dy - cH * 0.15f, paint);
                paint.setStrokeWidth(1);
                // 左轮
                paint.setColor(Color.rgb(80, 55, 30));
                canvas.drawCircle(dx - cW * 0.6f, dy + cH * 0.3f + wheelR, wheelR, paint);
                paint.setColor(Color.rgb(110, 80, 45));
                canvas.drawCircle(dx - cW * 0.6f, dy + cH * 0.3f + wheelR, wheelR * 0.5f, paint);
                // 轮辐
                paint.setColor(Color.rgb(80, 55, 30));
                paint.setStrokeWidth(2);
                float w1cx = dx - cW * 0.6f, w1cy = dy + cH * 0.3f + wheelR;
                canvas.drawLine(w1cx - wheelR * 0.7f, w1cy, w1cx + wheelR * 0.7f, w1cy, paint);
                canvas.drawLine(w1cx, w1cy - wheelR * 0.7f, w1cx, w1cy + wheelR * 0.7f, paint);
                // 右轮
                paint.setColor(Color.rgb(80, 55, 30));
                canvas.drawCircle(dx + cW * 0.2f, dy + cH * 0.3f + wheelR, wheelR, paint);
                paint.setColor(Color.rgb(110, 80, 45));
                canvas.drawCircle(dx + cW * 0.2f, dy + cH * 0.3f + wheelR, wheelR * 0.5f, paint);
                float w2cx = dx + cW * 0.2f, w2cy = dy + cH * 0.3f + wheelR;
                canvas.drawLine(w2cx - wheelR * 0.7f, w2cy, w2cx + wheelR * 0.7f, w2cy, paint);
                canvas.drawLine(w2cx, w2cy - wheelR * 0.7f, w2cx, w2cy + wheelR * 0.7f, paint);
                paint.setStrokeWidth(1);
                // 车上货物（随机）
                Random cartRand = new Random((int)(d.x * 23 + d.y * 47));
                int cargoType = cartRand.nextInt(3);
                if (cargoType == 0) {
                    // 麻袋
                    paint.setColor(Color.rgb(180, 160, 120));
                    canvas.drawOval(dx - cW * 0.7f, dy - cH * 1.3f, dx - cW * 0.1f, dy - cH * 0.2f, paint);
                    paint.setColor(Color.rgb(160, 140, 100));
                    canvas.drawOval(dx - cW * 0.4f, dy - cH * 1.1f, dx + cW * 0.2f, dy - cH * 0.3f, paint);
                } else if (cargoType == 1) {
                    // 木箱
                    paint.setColor(Color.rgb(170, 120, 60));
                    canvas.drawRect(dx - cW * 0.8f, dy - cH * 1.4f, dx - cW * 0.2f, dy - cH * 0.3f, paint);
                    paint.setColor(Color.rgb(150, 105, 50));
                    canvas.drawRect(dx - cW * 0.5f, dy - cH * 1.2f, dx + cW * 0.1f, dy - cH * 0.4f, paint);
                    // 箱子纹理
                    paint.setColor(Color.argb(30, 0, 0, 0));
                    canvas.drawLine(dx - cW * 0.8f, dy - cH * 0.85f, dx - cW * 0.2f, dy - cH * 0.85f, paint);
                } else {
                    // 酒坛/菜筐
                    paint.setColor(Color.rgb(140, 90, 50));
                    canvas.drawOval(dx - cW * 0.75f, dy - cH * 1.2f, dx - cW * 0.25f, dy - cH * 0.3f, paint);
                    paint.setColor(Color.rgb(120, 75, 40));
                    canvas.drawOval(dx - cW * 0.45f, dy - cH * 1.0f, dx + cW * 0.15f, dy - cH * 0.4f, paint);
                    // 坛口
                    paint.setColor(Color.rgb(100, 65, 35));
                    canvas.drawRect(dx - cW * 0.55f, dy - cH * 1.25f, dx - cW * 0.45f, dy - cH * 1.1f, paint);
                }
            }
            break;
            case WELL: {
                float wr = d.size * 0.45f;
                float wry = d.size * 0.4f;
                // 地面阴影（与村庄水井一致）
                paint.setColor(Color.argb(30, 0, 0, 0));
                canvas.drawOval(dx - wr - 5, dy + wry * 0.2f, dx + wr + 5, dy + wry + d.size * 0.15f, paint);
                // 石砌井台（外壁）
                paint.setColor(Color.rgb(140, 135, 125));
                canvas.drawOval(dx - wr, dy - wry, dx + wr, dy + wry, paint);
                // 井台顶面（稍亮）
                paint.setColor(Color.rgb(165, 160, 150));
                canvas.drawOval(dx - wr * 0.85f, dy - wry * 0.85f, dx + wr * 0.85f, dy + wry * 0.85f, paint);
                // 井口（深色空洞）
                paint.setColor(Color.rgb(25, 30, 40));
                canvas.drawOval(dx - wr * 0.55f, dy - wry * 0.55f, dx + wr * 0.55f, dy + wry * 0.55f, paint);
                // 水面反光
                paint.setColor(Color.argb(60, 80, 140, 200));
                canvas.drawOval(dx - wr * 0.3f, dy - wry * 0.15f, dx + wr * 0.1f, dy + wry * 0.15f, paint);
                // 左支柱 + 右支柱
                float postTop = dy - wry - d.size * 0.35f;
                paint.setColor(Color.rgb(90, 60, 35));
                canvas.drawRect(dx - wr * 0.65f - 3, postTop, dx - wr * 0.65f + 3, dy - wry * 0.3f, paint);
                canvas.drawRect(dx + wr * 0.65f - 3, postTop, dx + wr * 0.65f + 3, dy - wry * 0.3f, paint);
                // 横梁
                paint.setColor(Color.rgb(70, 45, 25));
                canvas.drawRect(dx - wr * 0.75f, postTop - 4, dx + wr * 0.75f, postTop + 4, paint);
                // 小顶棚（三角）
                paint.setColor(Color.rgb(110, 55, 35));
                android.graphics.Path wRoof = new android.graphics.Path();
                wRoof.moveTo(dx - wr * 0.9f, postTop);
                wRoof.lineTo(dx, postTop - d.size * 0.2f);
                wRoof.lineTo(dx + wr * 0.9f, postTop);
                wRoof.close();
                canvas.drawPath(wRoof, paint);
                // 绳子 + 水桶
                paint.setColor(Color.rgb(160, 140, 100));
                paint.setStrokeWidth(2);
                canvas.drawLine(dx, postTop + 4, dx, dy - wry * 0.2f, paint);
                paint.setStrokeWidth(1);
                paint.setColor(Color.rgb(110, 80, 50));
                canvas.drawRect(dx - 5, dy - wry * 0.2f, dx + 5, dy - wry * 0.2f + 8, paint);
            }
            break;
            case FENCE: {
                // 栅栏：两根立柱 + 上下两根横梁
                float fW = d.size * 0.5f;
                float fH = d.size * 0.5f;
                // 左柱
                paint.setColor(Color.rgb(130, 95, 55));
                canvas.drawRect(dx - fW - 3, dy - fH, dx - fW + 3, dy + 5, paint);
                // 右柱
                canvas.drawRect(dx + fW - 3, dy - fH, dx + fW + 3, dy + 5, paint);
                // 上横梁
                paint.setColor(Color.rgb(150, 110, 65));
                canvas.drawRect(dx - fW, dy - fH + 5, dx + fW, dy - fH + 12, paint);
                // 下横梁
                canvas.drawRect(dx - fW, dy - fH * 0.4f, dx + fW, dy - fH * 0.4f + 7, paint);
                // 中间竖条
                paint.setColor(Color.rgb(140, 100, 60));
                canvas.drawRect(dx - 2, dy - fH + 5, dx + 2, dy - fH * 0.4f, paint);
            }
            break;
            case BAMBOO: {
                // 竹林：多根细长竹竿 + 顶部叶簇
                Random bambooRand = new Random((int)(d.x * 41 + d.y * 67));
                int stalks = 4 + bambooRand.nextInt(4); // 4~7根
                float baseY = dy + d.size * 0.3f;
                for (int i = 0; i < stalks; i++) {
                    float sx = dx + (bambooRand.nextFloat() - 0.5f) * d.size * 0.7f;
                    float stalkH = d.size * (0.9f + bambooRand.nextFloat() * 0.5f);
                    float topY = baseY - stalkH;
                    // 竹竿（绿色，带节）
                    paint.setColor(Color.rgb(60, 140 + bambooRand.nextInt(30), 50));
                    paint.setStrokeWidth(3);
                    canvas.drawLine(sx, baseY, sx, topY, paint);
                    // 竹节（深色横纹）
                    paint.setColor(Color.rgb(40, 110, 35));
                    paint.setStrokeWidth(1);
                    int joints = 2 + bambooRand.nextInt(3);
                    for (int j = 1; j <= joints; j++) {
                        float jy = baseY - stalkH * j / (joints + 1f);
                        canvas.drawLine(sx - 3, jy, sx + 3, jy, paint);
                    }
                    // 顶部叶簇
                    paint.setColor(Color.rgb(45, 150, 40));
                    float leafW = d.size * 0.18f;
                    canvas.drawOval(sx - leafW, topY - leafW * 0.5f, sx + leafW, topY + leafW * 0.3f, paint);
                    paint.setColor(Color.rgb(55, 165, 50));
                    canvas.drawOval(sx - leafW * 0.6f, topY - leafW * 0.8f, sx + leafW * 0.8f, topY, paint);
                }
                paint.setStrokeWidth(1);
            }
            break;
            case ROCKERY: {
                // 假山：多块不规则灰色石头堆叠
                Random rockRand = new Random((int)(d.x * 53 + d.y * 89));
                float rBase = d.size * 0.5f;
                // 底部阴影
                paint.setColor(Color.argb(40, 0, 0, 0));
                canvas.drawOval(dx - rBase * 1.1f, dy + rBase * 0.2f, dx + rBase * 1.1f, dy + rBase * 0.6f, paint);
                // 3~5块石头
                int rocks = 3 + rockRand.nextInt(3);
                for (int i = 0; i < rocks; i++) {
                    float rx = dx + (rockRand.nextFloat() - 0.5f) * rBase * 0.8f;
                    float ry = dy - i * rBase * 0.25f + (rockRand.nextFloat() - 0.5f) * rBase * 0.3f;
                    float rw = rBase * (0.5f + rockRand.nextFloat() * 0.4f) * (1f - i * 0.12f);
                    float rh = rBase * (0.3f + rockRand.nextFloat() * 0.25f);
                    int shade = 120 + rockRand.nextInt(40);
                    paint.setColor(Color.rgb(shade, shade - 5, shade - 15));
                    canvas.drawOval(rx - rw, ry - rh, rx + rw, ry + rh * 0.6f, paint);
                    // 石头高光
                    paint.setColor(Color.argb(25, 220, 220, 210));
                    canvas.drawOval(rx - rw * 0.4f, ry - rh * 0.6f, rx + rw * 0.2f, ry, paint);
                }
                // 顶部小植物
                paint.setColor(Color.rgb(50, 130, 45));
                canvas.drawCircle(dx + (rockRand.nextFloat() - 0.5f) * rBase * 0.3f, dy - rBase * 0.5f, rBase * 0.15f, paint);
            }
            break;
            case BARREL: {
                // 木桶：椭圆桶身 + 铁箍 + 桶口
                float bW = d.size * 0.35f;
                float bH = d.size * 0.45f;
                // 桶身（棕色）
                paint.setColor(Color.rgb(140, 90, 45));
                canvas.drawOval(dx - bW, dy - bH * 0.3f, dx + bW, dy + bH * 0.5f, paint);
                canvas.drawRect(dx - bW, dy - bH * 0.3f, dx + bW, dy + bH * 0.2f, paint);
                // 木板纹理
                paint.setColor(Color.argb(20, 0, 0, 0));
                paint.setStrokeWidth(1);
                for (int i = 0; i < 4; i++) {
                    float lx = dx - bW + bW * 2f * (i + 1) / 5f;
                    canvas.drawLine(lx, dy - bH * 0.3f, lx, dy + bH * 0.2f, paint);
                }
                // 上铁箍
                paint.setColor(Color.rgb(90, 85, 80));
                paint.setStrokeWidth(2);
                canvas.drawLine(dx - bW * 0.95f, dy - bH * 0.15f, dx + bW * 0.95f, dy - bH * 0.15f, paint);
                // 下铁箍
                canvas.drawLine(dx - bW * 0.9f, dy + bH * 0.05f, dx + bW * 0.9f, dy + bH * 0.05f, paint);
                paint.setStrokeWidth(1);
                // 桶口（深色椭圆）
                paint.setColor(Color.rgb(60, 40, 20));
                canvas.drawOval(dx - bW * 0.85f, dy - bH * 0.45f, dx + bW * 0.85f, dy - bH * 0.2f, paint);
            }
            break;
            case FLAG_POLE: {
                // 旗杆：高杆 + 三角旗帜
                float poleH = d.size * 1.8f;
                float poleTop = dy - poleH;
                // 杆影
                paint.setColor(Color.argb(30, 0, 0, 0));
                canvas.drawRect(dx + 2, dy - poleH * 0.1f, dx + 5, dy + 5, paint);
                // 旗杆（深木色）
                paint.setColor(Color.rgb(100, 70, 35));
                canvas.drawRect(dx - 2, poleTop, dx + 2, dy + 5, paint);
                // 杆顶圆球
                paint.setColor(Color.rgb(200, 170, 50));
                canvas.drawCircle(dx, poleTop - 4, 5, paint);
                // 旗帜（红色三角，随风飘）
                Random flagRand = new Random((int)(d.x * 37 + d.y * 59));
                float flagW = d.size * 0.5f;
                float flagH = d.size * 0.7f;
                int flagColor = flagRand.nextInt(3);
                if (flagColor == 0) paint.setColor(Color.rgb(200, 40, 30));      // 红旗
                else if (flagColor == 1) paint.setColor(Color.rgb(40, 80, 180)); // 蓝旗
                else paint.setColor(Color.rgb(220, 180, 40));                     // 黄旗
                android.graphics.Path flagPath = new android.graphics.Path();
                flagPath.moveTo(dx + 2, poleTop + 5);
                flagPath.lineTo(dx + 2 + flagW, poleTop + flagH * 0.4f);
                flagPath.lineTo(dx + 2, poleTop + flagH);
                flagPath.close();
                canvas.drawPath(flagPath, paint);
                // 旗上文字（简化为一个白色方块）
                paint.setColor(Color.argb(180, 255, 255, 255));
                canvas.drawRect(dx + flagW * 0.25f, poleTop + flagH * 0.3f, dx + flagW * 0.55f, poleTop + flagH * 0.55f, paint);
            }
            break;
            case NOTICE_BOARD: {
                // 告示牌：两根木柱 + 木板面 + 文字线条
                float nbW = d.size * 0.5f;
                float nbH = d.size * 0.4f;
                float postH = d.size * 0.8f;
                // 左柱
                paint.setColor(Color.rgb(110, 75, 40));
                canvas.drawRect(dx - nbW - 3, dy - postH, dx - nbW + 3, dy + 5, paint);
                // 右柱
                canvas.drawRect(dx + nbW - 3, dy - postH, dx + nbW + 3, dy + 5, paint);
                // 木板面
                paint.setColor(Color.rgb(180, 150, 100));
                canvas.drawRect(dx - nbW, dy - postH + 5, dx + nbW, dy - postH + 5 + nbH, paint);
                // 木板边框
                paint.setColor(Color.rgb(130, 95, 55));
                paint.setStrokeWidth(2);
                canvas.drawRect(dx - nbW, dy - postH + 5, dx + nbW, dy - postH + 5 + nbH, paint);
                paint.setStrokeWidth(1);
                // 文字线条（横线模拟）
                paint.setColor(Color.argb(60, 40, 30, 20));
                for (int i = 0; i < 4; i++) {
                    float ly = dy - postH + 12 + i * nbH * 0.22f;
                    canvas.drawLine(dx - nbW * 0.75f, ly, dx + nbW * 0.75f, ly, paint);
                }
                // 顶部横木
                paint.setColor(Color.rgb(100, 70, 35));
                canvas.drawRect(dx - nbW - 5, dy - postH, dx + nbW + 5, dy - postH + 6, paint);
            }
            break;
        }
    }

    // ==================== 碰撞检测 ====================

    /**
     * 获取城市内所有不可通行的矩形区域
     */
    public List<Rect> getObstacles() {
        List<Rect> obstacles = new ArrayList<>();

        // 城墙段
        for (Rect wall : wallSegments) {
            obstacles.add(new Rect(wall.left + 5, wall.top + 5, wall.right - 5, wall.bottom - 5));
        }

        // 建筑物（稍微缩小碰撞箱）
        for (Building b : buildings) {
            int pad = 8;
            obstacles.add(new Rect(b.x + pad, b.y + pad, b.x + b.width - pad, b.y + b.height - pad));
        }

        return obstacles;
    }

    public Rect getCityBounds() {
        return cityBounds;
    }

    /**
     * 获取城市内可通行区域（用于标记 mapData）
     */
    public List<Rect> getPassableRoads() {
        List<Rect> roads = new ArrayList<>();
        // 南北主路
        roads.add(new Rect(CCX - ROAD_HALF, CY1, CCX + ROAD_HALF, CY2));
        // 东西主路
        roads.add(new Rect(CX1, CCY - ROAD_HALF, CX2, CCY + ROAD_HALF));
        return roads;
    }
}

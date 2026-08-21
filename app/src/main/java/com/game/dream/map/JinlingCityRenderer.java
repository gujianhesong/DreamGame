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

    public enum DecoType { TREE, LANTERN, FLOWER_BED, STATUE, FENCE, WELL }

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
        decorations.add(new Decoration(DecoType.TREE, 28300, 31200, 80));
        decorations.add(new Decoration(DecoType.TREE, 28600, 31500, 80));
        decorations.add(new Decoration(DecoType.TREE, 31400, 31200, 80));
        decorations.add(new Decoration(DecoType.TREE, 31700, 31500, 80));
    }

    // ==================== 北区：酒楼街 + 客栈区 ====================

    private void buildNorthDistrict() {
        // 区域: (25200, 25200) ~ (34800, 28000)
        // 主街两侧分布酒楼和客栈

        // 酒楼街 (西侧)
        fillBuildings(25400, 25400, 27800, 27800,
                800, 600, 200, 150, BuildingType.INN,
                new String[]{"醉仙楼", "金陵酒楼", "望月阁", "春风饭店", "聚贤庄", "太白酒楼"});

        // 客栈区 (东侧)
        fillBuildings(30200, 25400, 34600, 27800,
                700, 600, 200, 150, BuildingType.TEA_HOUSE,
                new String[]{"悦来客栈", "如家旅舍", "清风茶馆", "明月茶楼", "听雨轩", "品茗居"});

        // 中间小摊位
        fillBuildings(28200, 25600, 29800, 27600,
                400, 300, 150, 100, BuildingType.MARKET_STALL,
                new String[]{"面摊", "包子铺", "馄饨摊"});
    }

    // ==================== 东区：武器坊 + 杂货市集 ====================

    private void buildEastDistrict() {
        // 区域: (32000, 25200) ~ (34800, 34800)
        // 注意：避开南北向主干道（x=30000附近）和东西向主干道（y=30000附近）

        // 上半区：武器坊街
        fillBuildings(32200, 25400, 34600, 29600,
                700, 600, 200, 150, BuildingType.WEAPON_SHOP,
                new String[]{"龙泉剑庄", "霸王枪铺", "倚天锻坊", "寒铁兵器", "神兵阁"});

        // 下半区：防具店 + 杂货
        fillBuildings(32200, 30400, 34600, 34600,
                700, 600, 200, 150, BuildingType.ARMOR_SHOP,
                new String[]{"铁甲坊", "锦衣卫", "玄甲防具", "金缕衣阁"});

        // 杂货铺 (靠近内城)
        fillBuildings(32200, 28200, 34600, 29600,
                600, 500, 200, 150, BuildingType.GENERAL_STORE,
                new String[]{"万宝杂货", "百宝箱", "奇珍铺"});

        fillBuildings(32200, 30400, 34600, 31800,
                600, 500, 200, 150, BuildingType.GEM_SHOP,
                new String[]{"璀璨宝石", "翡翠轩"});
    }

    // ==================== 南区：民居区 + 南广场 ====================

    private void buildSouthDistrict() {
        // 区域: (25200, 32000) ~ (34800, 34800)

        // 南广场 (城门内侧大广场)
        // 广场区域不放建筑，用装饰物表示

        // 西区民居
        fillBuildings(25400, 32200, 29600, 34600,
                500, 400, 150, 120, BuildingType.HOUSE,
                new String[]{"民居", "小院", "巷弄人家"});

        // 东区民居
        fillBuildings(30400, 32200, 34600, 34600,
                500, 400, 150, 120, BuildingType.TALL_HOUSE,
                new String[]{"二层小楼", "民居", "商铺"});

        // 广场装饰
        decorations.add(new Decoration(DecoType.STATUE, CCX, 33500, 80));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 29000, 33800, 200));
        decorations.add(new Decoration(DecoType.FLOWER_BED, 31000, 33800, 200));
    }

    // ==================== 西区：药铺街 + 铁匠铺 ====================

    private void buildWestDistrict() {
        // 区域: (25200, 25200) ~ (28000, 34800)

        // 上半区：药铺街
        fillBuildings(25400, 25400, 27800, 29600,
                700, 600, 200, 150, BuildingType.PHARMACY,
                new String[]{"回春堂", "百草堂", "济世药铺", "同仁堂", "妙手药房"});

        // 下半区：铁匠铺 + 仓库
        fillBuildings(25400, 30400, 27800, 34600,
                700, 600, 200, 150, BuildingType.BLACKSMITH,
                new String[]{"欧冶铁铺", "干将 forge", "铸剑山庄", "打铁铺"});

        // 仓库 (靠近内城)
        fillBuildings(25400, 28200, 27800, 29600,
                600, 500, 200, 150, BuildingType.WAREHOUSE,
                new String[]{"官仓", "民仓", "货栈"});

        fillBuildings(25400, 30400, 27800, 31800,
                600, 500, 200, 150, BuildingType.STABLE,
                new String[]{"马厩", "骡马行"});
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

        // 内城周围树木
        for (int i = 0; i < 20; i++) {
            int tx = INNER_X1 + decoRand.nextInt(INNER_X2 - INNER_X1);
            int ty = INNER_Y1 + decoRand.nextInt(INNER_Y2 - INNER_Y1);
            // 避开建筑区域
            if (ty > 30000) { // 花园区域
                decorations.add(new Decoration(DecoType.TREE, tx, ty, 40 + decoRand.nextInt(40)));
            }
        }

        // 水井 (各街区)
        decorations.add(new Decoration(DecoType.WELL, 26500, 26500, 40));
        decorations.add(new Decoration(DecoType.WELL, 33500, 26500, 40));
        decorations.add(new Decoration(DecoType.WELL, 26500, 33500, 40));
        decorations.add(new Decoration(DecoType.WELL, 33500, 33500, 40));
        decorations.add(new Decoration(DecoType.WELL, CCX, CCY - 500, 40));
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
                drawPalace(canvas, paint, dx, dy, w, h);
                break;
            case GOVERNMENT:
                drawGovernment(canvas, paint, dx, dy, w, h);
                break;
            case INN:
                drawInn(canvas, paint, dx, dy, w, h);
                break;
            case TEA_HOUSE:
                drawTeaHouse(canvas, paint, dx, dy, w, h);
                break;
            case PHARMACY:
                drawPharmacy(canvas, paint, dx, dy, w, h);
                break;
            case WEAPON_SHOP:
                drawWeaponShop(canvas, paint, dx, dy, w, h);
                break;
            case ARMOR_SHOP:
                drawArmorShop(canvas, paint, dx, dy, w, h);
                break;
            case BLACKSMITH:
                drawBlacksmith(canvas, paint, dx, dy, w, h);
                break;
            case GENERAL_STORE:
            case GEM_SHOP:
                drawGeneralStore(canvas, paint, dx, dy, w, h);
                break;
            case MARKET_STALL:
                drawMarketStall(canvas, paint, dx, dy, w, h);
                break;
            case HOUSE:
                drawHouse(canvas, paint, dx, dy, w, h, Color.rgb(200, 180, 150));
                break;
            case TALL_HOUSE:
                drawTallHouse(canvas, paint, dx, dy, w, h);
                break;
            case CLOCK_TOWER:
                drawClockTower(canvas, paint, dx, dy, w, h);
                break;
            case GARDEN_PAVILION:
                drawPavilion(canvas, paint, dx, dy, w, h);
                break;
            case STABLE:
                drawStable(canvas, paint, dx, dy, w, h);
                break;
            case WAREHOUSE:
                drawWarehouse(canvas, paint, dx, dy, w, h);
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

    private void drawPalace(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        // 基座
        paint.setColor(Color.rgb(160, 150, 130));
        canvas.drawRect(x - 10, y + h * 0.3f, x + w + 10, y + h, paint);
        // 主体
        paint.setColor(Color.rgb(180, 50, 50));
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        // 屋顶
        paint.setColor(Color.rgb(200, 170, 50));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 20, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 20, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        // 门
        paint.setColor(Color.rgb(120, 40, 40));
        canvas.drawRect(x + w / 2 - 80, y + h - 120, x + w / 2 + 80, y + h, paint);
        // 柱子
        paint.setColor(Color.rgb(160, 40, 40));
        for (int i = 0; i < 4; i++) {
            float px = x + w * (i + 1) / 5;
            canvas.drawRect(px - 5, y + h * 0.3f, px + 5, y + h, paint);
        }
    }

    private void drawGovernment(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(190, 175, 150));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(80, 80, 100));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        paint.setColor(Color.rgb(100, 60, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawInn(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(210, 180, 130));
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        paint.setColor(Color.rgb(150, 60, 30));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 10, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 10, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        // 酒旗
        paint.setColor(Color.rgb(200, 50, 50));
        canvas.drawRect(x + w - 30, y + 5, x + w - 20, y + h * 0.4f, paint);
        paint.setColor(Color.rgb(100, 60, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawTeaHouse(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(220, 200, 170));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(100, 80, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.35f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.35f);
        roof.close();
        canvas.drawPath(roof, paint);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawPharmacy(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(200, 190, 170));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(60, 100, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        // 药十字标志
        paint.setColor(Color.rgb(200, 50, 50));
        float cx = x + w / 2, cy = y + h * 0.55f;
        canvas.drawRect(cx - 15, cy - 5, cx + 15, cy + 5, paint);
        canvas.drawRect(cx - 5, cy - 15, cx + 5, cy + 15, paint);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawWeaponShop(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(170, 160, 145));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(90, 90, 100));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        // 剑架装饰
        paint.setColor(Color.rgb(150, 150, 160));
        paint.setStrokeWidth(2);
        canvas.drawLine(x + 20, y + h * 0.4f, x + 20, y + h * 0.8f, paint);
        canvas.drawLine(x + 10, y + h * 0.5f, x + 30, y + h * 0.5f, paint);
        paint.setStrokeWidth(1);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawArmorShop(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(180, 175, 160));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(70, 80, 100));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        // 盾牌装饰
        paint.setColor(Color.rgb(120, 120, 140));
        canvas.drawCircle(x + w / 2, y + h * 0.55f, 20, paint);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawBlacksmith(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(140, 130, 120));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(60, 55, 50));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        // 烟囱
        paint.setColor(Color.rgb(80, 70, 60));
        canvas.drawRect(x + w - 40, y - 20, x + w - 20, y + h * 0.3f, paint);
        // 火光
        paint.setColor(Color.argb(60, 255, 100, 0));
        canvas.drawCircle(x + w / 2, y + h * 0.7f, 30, paint);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawGeneralStore(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(210, 195, 165));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(130, 100, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 8, y + h * 0.3f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 8, y + h * 0.3f);
        roof.close();
        canvas.drawPath(roof, paint);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawMarketStall(Canvas canvas, Paint paint, float x, float y, float w, float h) {
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

    private void drawHouse(Canvas canvas, Paint paint, float x, float y, float w, float h, int wallColor) {
        paint.setColor(wallColor);
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(100, 60, 40));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 5, y + h * 0.35f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 5, y + h * 0.35f);
        roof.close();
        canvas.drawPath(roof, paint);
        paint.setColor(Color.rgb(80, 45, 20));
        canvas.drawRect(x + w / 2 - 50, y + h - 100, x + w / 2 + 50, y + h, paint);
    }

    private void drawTallHouse(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        // 二层楼
        paint.setColor(Color.rgb(195, 175, 145));
        canvas.drawRect(x, y + h * 0.15f, x + w, y + h, paint);
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
        // 窗户
        paint.setColor(Color.rgb(180, 200, 220));
        canvas.drawRect(x + 15, y + h * 0.25f, x + 35, y + h * 0.42f, paint);
        canvas.drawRect(x + w - 35, y + h * 0.25f, x + w - 15, y + h * 0.42f, paint);
        // 门
        paint.setColor(Color.rgb(80, 45, 20));
        canvas.drawRect(x + w / 2 - 55, y + h - 110, x + w / 2 + 55, y + h, paint);
    }

    private void drawClockTower(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        // 塔基
        paint.setColor(Color.rgb(130, 120, 110));
        canvas.drawRect(x, y + h * 0.4f, x + w, y + h, paint);
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
        canvas.drawCircle(x + w / 2, y + h * 0.3f, Math.min(w, h) * 0.12f, paint);
    }

    private void drawPavilion(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        // 亭子底座
        paint.setColor(Color.rgb(170, 160, 140));
        canvas.drawRect(x, y + h * 0.6f, x + w, y + h, paint);
        // 柱子
        paint.setColor(Color.rgb(150, 50, 50));
        canvas.drawRect(x + 5, y + h * 0.3f, x + 10, y + h, paint);
        canvas.drawRect(x + w - 10, y + h * 0.3f, x + w - 5, y + h, paint);
        // 顶
        paint.setColor(Color.rgb(60, 100, 60));
        android.graphics.Path roof = new android.graphics.Path();
        roof.moveTo(x - 10, y + h * 0.35f);
        roof.lineTo(x + w / 2, y);
        roof.lineTo(x + w + 10, y + h * 0.35f);
        roof.close();
        canvas.drawPath(roof, paint);
    }

    private void drawStable(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(160, 130, 90));
        canvas.drawRect(x, y + h * 0.3f, x + w, y + h, paint);
        paint.setColor(Color.rgb(120, 90, 60));
        canvas.drawRect(x - 5, y + h * 0.25f, x + w + 5, y + h * 0.35f, paint);
        paint.setColor(Color.rgb(80, 50, 30));
        canvas.drawRect(x + w / 2 - 55, y + h - 100, x + w / 2 + 55, y + h, paint);
    }

    private void drawWarehouse(Canvas canvas, Paint paint, float x, float y, float w, float h) {
        paint.setColor(Color.rgb(150, 140, 125));
        canvas.drawRect(x, y + h * 0.25f, x + w, y + h, paint);
        paint.setColor(Color.rgb(100, 90, 80));
        canvas.drawRect(x - 3, y + h * 0.2f, x + w + 3, y + h * 0.3f, paint);
        paint.setColor(Color.rgb(80, 70, 60));
        canvas.drawRect(x + w / 2 - 60, y + h - 110, x + w / 2 + 60, y + h, paint);
    }

    // ==================== 装饰物渲染 ====================

    private void drawDecoration(Canvas canvas, Paint paint, Decoration d, float dx, float dy) {
        switch (d.type) {
            case TREE: {
                // 用世界坐标做种子，保证同一棵树样式固定（不受摄像机移动影响）
                Random treeRand = new Random(d.x * 73 + d.y * 137);
                int tStyle = treeRand.nextInt(4);
                float tSize = d.size;

                switch (tStyle) {
                    case 0: // 阔叶树
                        paint.setColor(Color.rgb(90, 60, 30));
                        canvas.drawRect(dx - 4, dy, dx + 4, dy + tSize * 0.6f, paint);
                        float cR = tSize * 0.38f;
                        paint.setColor(Color.rgb(30, 120, 30));
                        canvas.drawCircle(dx - cR * 0.35f, dy - cR * 0.15f, cR * 0.7f, paint);
                        canvas.drawCircle(dx + cR * 0.35f, dy - cR * 0.1f, cR * 0.65f, paint);
                        paint.setColor(Color.rgb(40, 140, 40));
                        canvas.drawCircle(dx, dy - cR * 0.45f, cR * 0.75f, paint);
                        paint.setColor(Color.argb(35, 120, 220, 80));
                        canvas.drawCircle(dx - cR * 0.2f, dy - cR * 0.65f, cR * 0.3f, paint);
                        break;

                    case 1: // 松树
                        paint.setColor(Color.rgb(80, 55, 28));
                        canvas.drawRect(dx - 3, dy, dx + 3, dy + tSize * 0.6f, paint);
                        float pW = tSize * 0.35f;
                        float pH = tSize * 0.25f;
                        for (int i = 0; i < 3; i++) {
                            float py = dy - tSize * 0.05f + i * pH * 0.65f;
                            float pw = pW * (1f - i * 0.2f);
                            paint.setColor(i == 0 ? Color.rgb(25, 100, 25) : Color.rgb(35, 125, 35));
                            android.graphics.Path pTri = new android.graphics.Path();
                            pTri.moveTo(dx - pw, py + pH);
                            pTri.lineTo(dx, py);
                            pTri.lineTo(dx + pw, py + pH);
                            pTri.close();
                            canvas.drawPath(pTri, paint);
                        }
                        break;

                    case 2: // 柳树
                        paint.setColor(Color.rgb(85, 65, 35));
                        canvas.drawRect(dx - 3, dy, dx + 3, dy + tSize * 0.55f, paint);
                        float wR = tSize * 0.4f;
                        paint.setColor(Color.rgb(50, 130, 45));
                        canvas.drawOval(dx - wR, dy - wR * 0.5f, dx + wR, dy + wR * 0.25f, paint);
                        paint.setColor(Color.rgb(60, 140, 50));
                        paint.setStrokeWidth(2);
                        for (int i = 0; i < 4; i++) {
                            float bx = dx + (treeRand.nextFloat() - 0.5f) * wR * 1.5f;
                            float by2 = dy - wR * 0.2f;
                            canvas.drawLine(bx, by2, bx + (treeRand.nextFloat() - 0.5f) * 6, by2 + wR * 0.7f, paint);
                        }
                        paint.setStrokeWidth(1);
                        break;

                    case 3: // 灌木
                        paint.setColor(Color.rgb(95, 70, 38));
                        canvas.drawRect(dx - 4, dy + tSize * 0.2f, dx + 4, dy + tSize * 0.6f, paint);
                        float bR = tSize * 0.32f;
                        paint.setColor(Color.rgb(45, 130, 40));
                        canvas.drawCircle(dx - bR * 0.45f, dy + tSize * 0.1f, bR * 0.65f, paint);
                        canvas.drawCircle(dx + bR * 0.45f, dy + tSize * 0.1f, bR * 0.6f, paint);
                        paint.setColor(Color.rgb(55, 150, 50));
                        canvas.drawCircle(dx, dy, bR * 0.7f, paint);
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
            case WELL: {
                float wr = d.size * 0.5f;
                // 石砌井台
                paint.setColor(Color.rgb(140, 135, 125));
                canvas.drawOval(dx - wr, dy - wr * 0.6f, dx + wr, dy + wr * 0.6f, paint);
                // 井台顶面
                paint.setColor(Color.rgb(165, 160, 150));
                canvas.drawOval(dx - wr * 0.8f, dy - wr * 0.45f, dx + wr * 0.8f, dy + wr * 0.45f, paint);
                // 井口
                paint.setColor(Color.rgb(25, 30, 40));
                canvas.drawOval(dx - wr * 0.5f, dy - wr * 0.3f, dx + wr * 0.5f, dy + wr * 0.3f, paint);
                // 水面反光
                paint.setColor(Color.argb(60, 80, 140, 200));
                canvas.drawOval(dx - wr * 0.2f, dy - wr * 0.1f, dx + wr * 0.1f, dy + wr * 0.1f, paint);
                // 支柱 + 横梁
                float wTop = dy - wr * 0.6f - d.size * 0.35f;
                paint.setColor(Color.rgb(90, 60, 35));
                canvas.drawRect(dx - wr * 0.55f - 2, wTop, dx - wr * 0.55f + 2, dy - wr * 0.2f, paint);
                canvas.drawRect(dx + wr * 0.55f - 2, wTop, dx + wr * 0.55f + 2, dy - wr * 0.2f, paint);
                paint.setColor(Color.rgb(70, 45, 25));
                canvas.drawRect(dx - wr * 0.65f, wTop - 3, dx + wr * 0.65f, wTop + 3, paint);
                // 小顶棚
                paint.setColor(Color.rgb(110, 55, 35));
                android.graphics.Path wRoof = new android.graphics.Path();
                wRoof.moveTo(dx - wr * 0.75f, wTop);
                wRoof.lineTo(dx, wTop - d.size * 0.18f);
                wRoof.lineTo(dx + wr * 0.75f, wTop);
                wRoof.close();
                canvas.drawPath(wRoof, paint);
                // 绳子 + 水桶
                paint.setColor(Color.rgb(160, 140, 100));
                paint.setStrokeWidth(2);
                canvas.drawLine(dx, wTop + 3, dx, dy - wr * 0.1f, paint);
                paint.setStrokeWidth(1);
                paint.setColor(Color.rgb(110, 80, 50));
                canvas.drawRect(dx - 4, dy - wr * 0.1f, dx + 4, dy - wr * 0.1f + 7, paint);
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

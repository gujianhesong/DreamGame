package com.game.dream.npc;

import android.util.Pair;

import com.game.dream.GameEngine;
import com.game.dream.item.EquipmentItem;
import com.game.dream.item.Item;
import com.game.dream.item.ItemStack;
import com.game.dream.system.ItemSystem;
import com.game.dream.system.MapSystem;
import com.game.dream.system.RoleSystem;
import com.game.dream.ui.DialogBox;
import com.game.dream.ui.EquipSellDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionNpcManager {

    private static FunctionNpcManager instance = new FunctionNpcManager();

    public static FunctionNpcManager getInstance() {
        return instance;
    }

    private FunctionNpcManager() {
    }

    public boolean handleNpcClick(Npc npc) {
        switch (npc.getId()) {
            case 100101: {
                // 清溪村村长 - 迷宫入口
                List<String> options = Arrays.asList("探索迷宫", "不了");
                String message = "少侠，村外有一处迷雾迷宫，里面危机四伏，若少侠有胆量，可前去闯荡一番。";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_QING_XI_MAZE);
                        }
                    }
                });
                return true;
            }
            case 100153: {
                //清溪-妙手郎中
                int costMoney = 500;
                List<String> options = Arrays.asList("我要疗伤", "暂时不用");
                String message = "少侠可要疗伤，诊费" + costMoney;
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {

                        if(RoleSystem.getInstance().getRoleInfo().getMoney() < costMoney){
                            return;
                        }

                        RoleSystem.getInstance().getRoleInfo().setMoney(RoleSystem.getInstance().getRoleInfo().getMoney() - costMoney);
                        RoleSystem.getInstance().getRoleInfo().setHp(RoleSystem.getInstance().getRoleInfo().getBloodCap());
                        RoleSystem.getInstance().getRoleInfo().setMp(RoleSystem.getInstance().getRoleInfo().getMagicCap());

                        GameEngine.getInstance().showDialog(npc.getName(), "好了，少侠已经完全恢复了");
                    }
                });
                return true;
            }
            case 100154: {
                //清溪-装备收购商
                handleEquipSell(npc);
                return true;
            }
            case 100117: {
                //清溪-驿站车夫
                List<String> options = Arrays.asList("前往金陵稻香屯", "不了");
                String message = "客官想去哪里？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            int cost = 100;
                            if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                                return;
                            }
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            GameEngine.getInstance().teleportToMapWithPosition(MapSystem.MAP_ID_JIN_LING, 2880, 58470);
                        }
                    }
                });
                return true;
            }
            case 100201: {
                //金陵-驿站车夫（主城）
                List<String> options = Arrays.asList("前往碧波渡(城东北)", "前往云岩寨(城东南)", "前往稻香屯(城西南)", "前往翠微庄(城西北)", "不了");
                String message = "客官想去哪里？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        int cost = 100;
                        if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                            GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                            return;
                        }
                        switch (optionIndex) {
                            case 0: // 碧波渡（东北）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(58900);
                                GameEngine.getInstance().getPlayer().setY(2470);
                                break;
                            case 1: // 云岩寨（东南）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(57200);
                                GameEngine.getInstance().getPlayer().setY(58080);
                                break;
                            case 2: // 稻香屯（西南）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(2880);
                                GameEngine.getInstance().getPlayer().setY(58470);
                                break;
                            case 3: // 翠微庄（西北）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(2870);
                                GameEngine.getInstance().getPlayer().setY(2550);
                                break;
                        }
                    }
                });
                return true;
            }
            case 100203: // 碧波渡车夫（东北）
            case 100204: // 云岩寨车夫（东南）
            case 100205: // 稻香屯车夫（西南）
            case 100206: { // 翠微庄车夫（西北）
                // 四角村庄车夫 → 传送到金陵主城
                List<String> options = Arrays.asList("前往金陵主城", "不了");
                String message = "客官想去金陵主城吗？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            int cost = 100;
                            if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                                return;
                            }
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            Pair<Integer, Integer> transPos = MapSystem.getInstance().getCurMapInfo().getTransPos();
                            GameEngine.getInstance().getPlayer().setX(transPos.first);
                            GameEngine.getInstance().getPlayer().setY(transPos.second);
                        }
                    }
                });
                return true;
            }
            case 100207: {
                //金陵西侧-鬼差（仅夜间出现，传送地府）
                List<String> options = Arrays.asList("前往地府", "不了");
                String message = "嘘……阳人莫要张扬。小人奉命在此引导亡魂，若少侠有意前往地府，小人可代为开路……";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_NETHERWORLD);
                        }
                    }
                });
                return true;
            }
            case 100208: {
                //金陵东侧-东城车夫（传送东海湾）
                List<String> options = Arrays.asList("前往东海湾", "不了");
                String message = "客官想去东海湾？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            int cost = 100;
                            if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                                return;
                            }
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_DONGHAI_BAY);
                        }
                    }
                });
                return true;
            }
            case 100301: {
                //东海湾-驿站车夫
                List<String> options = Arrays.asList("前往金陵主城", "不了");
                String message = "客官想去哪里？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        int cost = 100;
                        if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                            GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                            return;
                        }
                        if (optionIndex == 0) {
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            GameEngine.getInstance().teleportToMapWithPosition(MapSystem.MAP_ID_JIN_LING, 35700, 30000);
                        }
                    }
                });
                return true;
            }
            case 100306: {
                //东海湾-海边虾兵（潜入东海海底）
                List<String> options = Arrays.asList("潜入东海海底", "不了");
                String message = "少侠想潜入海底吗？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            int cost = 100;
                            if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                                return;
                            }
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_DONGHAI_SEABED);
                        }
                    }
                });
                return true;
            }
            case 100401: {
                //东海海底-驿站虾兵
                List<String> options = Arrays.asList("返回东海湾海边", "前往海底迷宫", "不了");
                String message = "少侠想去哪里？我只收你100金钱";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            int cost = 100;
                            if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                                return;
                            }
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            GameEngine.getInstance().teleportToMapWithPosition(MapSystem.MAP_ID_DONGHAI_BAY, 7000, 5200);
                        } else if (optionIndex == 1) {
                            int cost = 100;
                            if (RoleSystem.getInstance().getRoleInfo().getMoney() < cost) {
                                GameEngine.getInstance().showCenterToast("金钱不足，需要100金钱");
                                return;
                            }
                            RoleSystem.getInstance().getRoleInfo().setMoney(
                                    RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_UNDERWATER_MAZE);
                        }
                    }
                });
                return true;
            }
            case 100501: {
                //地府-引魂使者（返回金陵西侧）
                List<String> options = Arrays.asList("返回金陵", "不了");
                String message = "阴阳两隔，活人不宜久留于此。少侠若想回返人间，小吽可代为引魂……";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMapWithPosition(MapSystem.MAP_ID_JIN_LING, 24700, 30000);
                        }
                    }
                });
                return true;
            }
            case 100502: {
                //地府-阎罗王
                String message = "大胆！阳人何以闯入森罗殿？……哼，念你初犯，且退下吧。";
                GameEngine.getInstance().showDialog(npc.getName(), message);
                return true;
            }
            case 100508: {
                //地府-孟婆
                String message = "奈何桥上无老少，一碗孟婆汤忘前尘。少侠尚是阳身，这汤……可饮不得。";
                GameEngine.getInstance().showDialog(npc.getName(), message);
                return true;
            }
            case 100531:
            case 100532: {
                //幽魂牢-黑白无常（主角死亡后囚禁于此，可送返回人间）
                List<String> options = Arrays.asList("恳请二位送我回阳间", "不了");
                String message = npc.getId() == 100531
                        ? "嘿嘿……阳寿未尽却已魂归幽魂牢，看来阎王爷也懒得收你。若想还阳，得答应一个条件——回去后多行善事，莫再轻掷性命。"
                        : "阳间寿数未终，本不该拘你魂魄。奈何你已身死，只能暂寄此牢。若愿还阳，我兄弟二人可代为引魂……只是回去后须得惜命。";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_QING_XI);
                        }
                    }
                });
                return true;
            }
            case 100540: {
                //地狱鬼差（地狱迷宫入口）
                List<String> options = Arrays.asList("闯地狱迷宫", "不了");
                String message = "阳人休得靠近！此地乃四层炼狱之门，一层黄泉迷径、二层血池炼狱、三层枉死城、四层阎罗殿，入者九死一生。少侠若自认道行深厂，小吾可代为开门……";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_HELL_MAZE_1);
                        }
                    }
                });
                return true;
            }
        }

        return false;
    }

    /**
     * 装备收购商 - 出售装备功能
     */
    private void handleEquipSell(final Npc npc) {
        // 收集背包所有装备
        List<ItemStack> equipList = new ArrayList<>();
        List<Integer> sellPrices = new ArrayList<>();

        for (ItemStack stack : ItemSystem.getInstance().getItems()) {
            if (stack.getItem().getType() == Item.Type.EQUIPMENT) {
                EquipmentItem equip = (EquipmentItem) stack.getItem();
                equipList.add(stack);
                sellPrices.add(calcSellPrice(equip));
            }
        }

        // 排序：低等级在前，同等级按低品质在前
        java.util.Collections.sort(equipList, (a, b) -> {
            EquipmentItem ea = (EquipmentItem) a.getItem();
            EquipmentItem eb = (EquipmentItem) b.getItem();
            float levelA = ea.getEquipItemInfo() != null ? ea.getEquipItemInfo().getLevel() : 0;
            float levelB = eb.getEquipItemInfo() != null ? eb.getEquipItemInfo().getLevel() : 0;
            if (levelA != levelB) return Float.compare(levelA, levelB);
            return ea.getRarity().ordinal() - eb.getRarity().ordinal();
        });
        // 重新计算排序后的价格
        sellPrices.clear();
        for (ItemStack stack : equipList) {
            sellPrices.add(calcSellPrice((EquipmentItem) stack.getItem()));
        }

        // 使用专用装备出售对话框
        EquipSellDialog dialog = GameEngine.getInstance().getEquipSellDialog();
        if (dialog != null) {
            dialog.show(npc.getName(), equipList, sellPrices, null);
        }
    }

    /**
     * 计算装备出售价格（基础价值 * 等级 * 品质系数）
     */
    private int calcSellPrice(EquipmentItem equip) {
        int baseValue = equip.getValue();
        float levelFloat = equip.getEquipItemInfo().getLevel();
        if (levelFloat == 0) {
            levelFloat = 0.5f;
        }
        float rarityMultiplier;
        switch (equip.getRarity()) {
            case Rarity_1: rarityMultiplier = 1.0f; break;
            case Rarity_2: rarityMultiplier = 1.5f; break;
            case Rarity_3: rarityMultiplier = 2.0f; break;
            case Rarity_4: rarityMultiplier = 3.0f; break;
            case Rarity_5: rarityMultiplier = 5.0f; break;
            case Rarity_6: rarityMultiplier = 8.0f; break;
            default: rarityMultiplier = 0.5f; break;
        }
        return (int) (baseValue * levelFloat * rarityMultiplier);
    }
}

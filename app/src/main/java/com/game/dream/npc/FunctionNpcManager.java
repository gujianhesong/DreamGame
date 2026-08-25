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
                // 清溪村村长 - 迷宫入口 / 金陵入口
                List<String> options = Arrays.asList("探索迷宫", "前往金陵", "不了");
                String message = "少侠想去哪里？村外有一处迷雾迷宫，另外南方有一座繁华的大城金陵，也可以去闯荡一番。";
                GameEngine.getInstance().showDialog(npc.getName(), message, options, new DialogBox.DialogListener() {
                    @Override
                    public void onOptionSelected(int optionIndex) {
                        if (optionIndex == 0) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_QING_XI_MAZE);
                        } else if (optionIndex == 1) {
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_JIN_LING);
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
                List<String> options = Arrays.asList("前往金陵", "不了");
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
                            GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_JIN_LING);
                        }
                    }
                });
                return true;
            }
            case 100201: {
                //金陵-驿站车夫（主城）
                List<String> options = Arrays.asList("前往清溪村", "前往碧波渡", "前往云岩寨", "前往稻香屯", "前往翠微庄", "不了");
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
                            case 0: // 清溪村（跨地图）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().teleportToMap(MapSystem.MAP_ID_QING_XI);
                                break;
                            case 1: // 碧波渡（东北）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(58900);
                                GameEngine.getInstance().getPlayer().setY(2470);
                                break;
                            case 2: // 云岩寨（东南）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(57200);
                                GameEngine.getInstance().getPlayer().setY(58080);
                                break;
                            case 3: // 稻香屯（西南）
                                RoleSystem.getInstance().getRoleInfo().setMoney(
                                        RoleSystem.getInstance().getRoleInfo().getMoney() - cost);
                                GameEngine.getInstance().getPlayer().setX(2880);
                                GameEngine.getInstance().getPlayer().setY(58470);
                                break;
                            case 4: // 翠微庄（西北）
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

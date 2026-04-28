package com.game.dream.item;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.system.ItemSystem;
import com.game.dream.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EquipCreator {

    public static EquipmentItem createEquipWithInfo(ItemInfo itemInfo) {
        if (itemInfo instanceof EquipItemInfo) {
            EquipItemInfo equipItemInfo = (EquipItemInfo) itemInfo;
            EquipmentItem equipmentItem = new EquipmentItem(equipItemInfo);
            return equipmentItem;
        }
        return null;
    }

    public static EquipmentItem createEquip(int level, EquipmentItem.Slot slot) {
        if (slot == null) {
            EquipmentItem.Slot[] slots = {EquipmentItem.Slot.HELMET, EquipmentItem.Slot.ACCESSORY, EquipmentItem.Slot.WEAPON,
                    EquipmentItem.Slot.ARMOR, EquipmentItem.Slot.BELT, EquipmentItem.Slot.SHOES};
            int index = (int) (Math.random() * slots.length);
            slot = slots[index];
        }

        int baseId = 20000 * 1000 + (slot.ordinal() + 1) * 1000 * 1000 + level * 1000;
        List<ItemStack> allItems = ItemSystem.getInstance().getItems();
        HashMap<Integer, Item> itemMap = new HashMap<>();
        for (ItemStack item : allItems) {
            itemMap.put(item.getItem().getId(), item.getItem());
        }
        int newId = 0;
        for (int i = 1; i < 100; i++) {
            int id = baseId + i;
            if (!itemMap.containsKey(id)) {
                newId = id;
                break;
            }
        }

        if (newId <= 0) {
            return null;
        }

        float gradeValue = 0;
        int gradeCount = 0;
        EquipItemInfo equipItemInfo = new EquipItemInfo(newId, "");
        switch (slot) {
            case HELMET: {
                equipItemInfo.setName(level + "头盔");
                equipItemInfo.setLevel(level);
                equipItemInfo.setType(1);

                float waveRatio1 = Utils.getWaveValue(1, 0.3f);
                float waveRatio2 = Utils.getWaveValue(1, 0.3f);
                equipItemInfo.setDefense((int) (30 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setMp((int) (30 * (level / 10 + 1) * waveRatio2));

                gradeValue += waveRatio1;
                gradeValue += waveRatio2;
                gradeCount += 2;
            }
            break;
            case ACCESSORY: {
                equipItemInfo.setName(level + "项链");
                equipItemInfo.setType(2);

                float waveRatio1 = Utils.getWaveValue(1, 0.3f);
                float waveRatio2 = Utils.getWaveValue(1, 0.3f);
                equipItemInfo.setMana((int) (25 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setMp((int) (30 * (level / 10 + 1) * waveRatio2));

                gradeValue += waveRatio1;
                gradeValue += waveRatio2;
                gradeCount += 2;
            }
            break;
            case WEAPON: {
                equipItemInfo.setName(level + "剑");
                equipItemInfo.setType(3);

                float waveRatio1 = Utils.getWaveValue(1, 0.3f);
                float waveRatio2 = Utils.getWaveValue(1, 0.3f);
                equipItemInfo.setHit((int) (45 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setAttack((int) (35 * (level / 10 + 1) * waveRatio2));

                gradeValue += waveRatio1;
                gradeValue += waveRatio2;
                gradeCount += 2;
            }
            break;
            case ARMOR: {
                equipItemInfo.setName(level + "铠甲");
                equipItemInfo.setType(4);

                float waveRatio1 = Utils.getWaveValue(1, 0.3f);
                equipItemInfo.setDefense((int) (45 * (level / 10 + 1) * waveRatio1));

                gradeValue += waveRatio1;
                gradeCount += 1;
            }
            break;
            case BELT: {
                equipItemInfo.setName(level + "腰带");
                equipItemInfo.setType(5);

                float waveRatio1 = Utils.getWaveValue(1, 0.3f);
                float waveRatio2 = Utils.getWaveValue(1, 0.3f);
                equipItemInfo.setDefense((int) (20 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setHp((int) (50 * (level / 10 + 1) * waveRatio2));

                gradeValue += waveRatio1;
                gradeValue += waveRatio2;
                gradeCount += 2;
            }
            break;
            case SHOES: {
                equipItemInfo.setName(level + "鞋子");
                equipItemInfo.setType(6);

                float waveRatio1 = Utils.getWaveValue(1, 0.3f);
                float waveRatio2 = Utils.getWaveValue(1, 0.3f);
                equipItemInfo.setSpeed((int) (25 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setDodge((int) (30 * (level / 10 + 1) * waveRatio2));

                gradeValue += waveRatio1;
                gradeValue += waveRatio2;
                gradeCount += 2;
            }
            break;
        }

        double srcPropRatio = Math.random();
        int baseProp = 10 + 3 * (level / 10);
        if (srcPropRatio < 0.15) {
            //属性双加
            List<Integer> numbers = new ArrayList<>();
            numbers.add(0);
            numbers.add(1);
            numbers.add(2);
            numbers.add(3);
            numbers.add(4);
            Collections.shuffle(numbers);
            int first = numbers.get(0);
            int second = numbers.get(1);

            float waveRatio1 = Utils.getWaveValue(1, 0.25f);
            int value1 = (int) (baseProp * waveRatio1);
            switch (first) {
                case 0:
                    equipItemInfo.setPropTi(value1);
                    break;
                case 1:
                    equipItemInfo.setPropMo(value1);
                    break;
                case 2:
                    equipItemInfo.setPropLi(value1);
                    break;
                case 3:
                    equipItemInfo.setPropNai(value1);
                    break;
                case 4:
                    equipItemInfo.setPropMin(value1);
                    break;
            }

            float waveRatio2 = Utils.getWaveValue(1, 0.25f);
            int value2 = (int) (baseProp * waveRatio2);
            switch (second) {
                case 0:
                    equipItemInfo.setPropTi(value2);
                    break;
                case 1:
                    equipItemInfo.setPropMo(value2);
                    break;
                case 2:
                    equipItemInfo.setPropLi(value2);
                    break;
                case 3:
                    equipItemInfo.setPropNai(value2);
                    break;
                case 4:
                    equipItemInfo.setPropMin(value2);
                    break;
            }

            gradeValue += waveRatio1;
            gradeValue += waveRatio2;
            gradeCount += 2;
        } else if (srcPropRatio < 0.4) {
            //属性单加
            float waveRatio1 = Utils.getWaveValue(1, 0.25f);
            int value1 = (int) (baseProp * waveRatio1);
            int index1 = (int) (Math.random() * 5);
            switch (index1) {
                case 0:
                    equipItemInfo.setPropTi(value1);
                    break;
                case 1:
                    equipItemInfo.setPropMo(value1);
                    break;
                case 2:
                    equipItemInfo.setPropLi(value1);
                    break;
                case 3:
                    equipItemInfo.setPropNai(value1);
                    break;
                case 4:
                    equipItemInfo.setPropMin(value1);
                    break;
            }

            gradeValue += waveRatio1;
            gradeCount += 1;
        } else {
            //无属性加成
        }

        //装备评分
        /*float averageGradeValue = gradeValue / gradeCount;
        if(averageGradeValue < 0.85){

        }*/

        EquipmentItem equipmentItem = new EquipmentItem(equipItemInfo);
        return equipmentItem;
    }
}

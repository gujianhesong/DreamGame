package com.game.dream.item;

import com.game.dream.LogUtil;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.system.ItemSystem;
import com.game.dream.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        float baseGradeValue = 0;
        float srcPropGradeValue = 0;
        float specialEffectGradeValue = 0;

        EquipItemInfo equipItemInfo = new EquipItemInfo(newId, "");
        switch (slot) {
            case HELMET: {
                equipItemInfo.setName(level + "级头盔");
                equipItemInfo.setLevel(level);
                equipItemInfo.setEquipType(1);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setDefense((int) (30 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setMp((int) (30 * (level / 10 + 1) * waveRatio2));

                baseGradeValue = (waveRatio1 + waveRatio2) / 2;
            }
            break;
            case ACCESSORY: {
                equipItemInfo.setName(level + "级项链");
                equipItemInfo.setEquipType(2);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setMana((int) (25 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setMp((int) (30 * (level / 10 + 1) * waveRatio2));

                baseGradeValue = (waveRatio1 + waveRatio2) / 2;
            }
            break;
            case WEAPON: {
                equipItemInfo.setName(level + "级剑");
                equipItemInfo.setEquipType(3);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setHit((int) (45 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setAttack((int) (35 * (level / 10 + 1) * waveRatio2));

                baseGradeValue = (waveRatio1 + waveRatio2) / 2;
            }
            break;
            case ARMOR: {
                equipItemInfo.setName(level + "级铠甲");
                equipItemInfo.setEquipType(4);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setDefense((int) (45 * (level / 10 + 1) * waveRatio1));

                baseGradeValue = waveRatio1;
            }
            break;
            case BELT: {
                equipItemInfo.setName(level + "级腰带");
                equipItemInfo.setEquipType(5);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setDefense((int) (20 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setHp((int) (50 * (level / 10 + 1) * waveRatio2));

                baseGradeValue = (waveRatio1 + waveRatio2) / 2;
            }
            break;
            case SHOES: {
                equipItemInfo.setName(level + "级鞋子");
                equipItemInfo.setEquipType(6);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setSpeed((int) (25 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setDodge((int) (30 * (level / 10 + 1) * waveRatio2));

                baseGradeValue = (waveRatio1 + waveRatio2) / 2;
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

            float waveRatio1 = Utils.getWaveValueFloat(1, 0.25f);
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

            float waveRatio2 = Utils.getWaveValueFloat(1, 0.25f);
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

            srcPropGradeValue = waveRatio1 + waveRatio2;
        } else if (srcPropRatio < 0.4) {
            //属性单加
            float waveRatio1 = Utils.getWaveValueFloat(1, 0.25f);
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

            srcPropGradeValue = waveRatio1;
        } else {
            //无属性加成
        }

        //特效
        float totalRatioValue = 100f;
        LinkedHashMap<SpecialEffect, Integer> specialEffectRatioMap = new LinkedHashMap<>();
        specialEffectRatioMap.put(SpecialEffect.SE_WuJiBieXianZhi, 1);
        specialEffectRatioMap.put(SpecialEffect.SE_ShenNong, 3);
        specialEffectRatioMap.put(SpecialEffect.SE_ZhenBao, 3);
        specialEffectRatioMap.put(SpecialEffect.SE_Xixue, 3);
        specialEffectRatioMap.put(SpecialEffect.SE_ShenYou, 3);
        specialEffectRatioMap.put(SpecialEffect.SE_ZhuanZhu, 3);
        specialEffectRatioMap.put(SpecialEffect.SE_ZaiSheng, 3);
        specialEffectRatioMap.put(SpecialEffect.SE_MingSi, 3);
        if (slot == EquipmentItem.Slot.WEAPON) {
            //武器，有必中
            specialEffectRatioMap.put(SpecialEffect.SE_BiZhong, 3);
        }

        List<SpecialEffect> specialEffects = new ArrayList<>();

        //计算特效1
        double specialEffectRatio = Math.random();
        int addValue = 0;
        for (Map.Entry<SpecialEffect, Integer> entry : specialEffectRatioMap.entrySet()) {
            float startValue = addValue / totalRatioValue;
            float endValue = (addValue + entry.getValue()) / totalRatioValue;
            if (specialEffectRatio >= startValue && specialEffectRatio < endValue) {
                specialEffects.add(entry.getKey());
                specialEffectRatioMap.remove(entry.getKey());
                break;
            }
            addValue += entry.getValue();
        }

        //计算特效2
        specialEffectRatio = Math.random();
        addValue = 0;
        for (Map.Entry<SpecialEffect, Integer> entry : specialEffectRatioMap.entrySet()) {
            float startValue = addValue / totalRatioValue;
            float endValue = (addValue + entry.getValue()) / totalRatioValue;
            if (specialEffectRatio >= startValue && specialEffectRatio < endValue) {
                specialEffects.add(entry.getKey());
                specialEffectRatioMap.remove(entry.getKey());
                break;
            }
            addValue += entry.getValue();
        }

        equipItemInfo.setSpecialEffects(specialEffects.stream().map(new Function<SpecialEffect, String>() {
            @Override
            public String apply(SpecialEffect specialEffect) {
                return specialEffect.name();
            }
        }).collect(Collectors.toList()));
        //特效评分, 因为双特效的概率太小了，因此只用一个最大特效值进行评分，不然评分不太准确
        if (!specialEffects.isEmpty()) {
            for(SpecialEffect specialEffect : specialEffects){
                switch (specialEffect) {
                    case SE_WuJiBieXianZhi:
                        specialEffectGradeValue = Math.max(1.0f, specialEffectGradeValue);
                        break;
                    case SE_ShenNong:
                        specialEffectGradeValue = Math.max(0.4f, specialEffectGradeValue);
                        break;
                    case SE_ZhenBao:
                        specialEffectGradeValue = Math.max(0.1f, specialEffectGradeValue);
                        break;
                    case SE_Xixue:
                        specialEffectGradeValue = Math.max(0.6f, specialEffectGradeValue);
                        break;
                    case SE_ShenYou:
                        specialEffectGradeValue = Math.max(0.4f, specialEffectGradeValue);
                        break;
                    case SE_ZhuanZhu:
                        specialEffectGradeValue = Math.max(0.4f, specialEffectGradeValue);
                        break;
                    case SE_ZaiSheng:
                        specialEffectGradeValue = Math.max(0.4f, specialEffectGradeValue);
                        break;
                    case SE_MingSi:
                        specialEffectGradeValue = Math.max(0.4f, specialEffectGradeValue);
                        break;
                    case SE_BiZhong:
                        specialEffectGradeValue = Math.max(0.4f, specialEffectGradeValue);
                        break;
                }
            }
        }

        //装备评分
        float gradeValue = baseGradeValue + srcPropGradeValue * 0.8f + specialEffectGradeValue;
        float maxGradeValue = 1.3f + 1.3f * 2 * 0.8f + 1.0f;
        float minGradeValue = 0.7f + 0 + 0;
        if (gradeValue - minGradeValue > (maxGradeValue - minGradeValue) * 0.8) {
            equipItemInfo.setRatity(Item.Rarity.Rarity_5.name());
        } else if (gradeValue - minGradeValue > (maxGradeValue - minGradeValue) * 0.6) {
            equipItemInfo.setRatity(Item.Rarity.Rarity_4.name());
        } else if (gradeValue - minGradeValue > (maxGradeValue - minGradeValue) * 0.4) {
            equipItemInfo.setRatity(Item.Rarity.Rarity_3.name());
        } else if (gradeValue - minGradeValue > (maxGradeValue - minGradeValue) * 0.2) {
            equipItemInfo.setRatity(Item.Rarity.Rarity_2.name());
        } else {
            equipItemInfo.setRatity(Item.Rarity.Rarity_1.name());
        }

        return new EquipmentItem(equipItemInfo);
    }
}

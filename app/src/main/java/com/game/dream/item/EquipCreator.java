package com.game.dream.item;

import android.text.TextUtils;

import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.ItemInfo;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.system.ItemSystem;
import com.game.dream.utils.EquipUtil;
import com.game.dream.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EquipCreator {
    public static class EquipCreatorExtraInfo {
        public boolean isWuJiBie;
    }

    public static EquipmentItem createEquipWithInfo(ItemInfo itemInfo) {
        if (itemInfo instanceof EquipItemInfo) {
            EquipItemInfo equipItemInfo = (EquipItemInfo) itemInfo;
            EquipmentItem equipmentItem = new EquipmentItem(equipItemInfo);
            return equipmentItem;
        }
        return null;
    }

    public static EquipmentItem createEquip(int level, EquipmentItem.Slot slot) {
        return createEquip(level, slot, null);
    }

    public static EquipmentItem createEquip(int level, EquipmentItem.Slot slot, EquipCreatorExtraInfo extraInfo) {
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

        EquipItemInfo equipItemInfo = new EquipItemInfo(newId, "");
        equipItemInfo.setLevel(level);
        switch (slot) {
            case HELMET: {
                equipItemInfo.setName(level + "级头盔");
                equipItemInfo.setEquipType(1);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setDefense((int) (30 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setMp((int) (30 * (level / 10 + 1) * waveRatio2));
            }
            break;
            case ACCESSORY: {
                equipItemInfo.setName(level + "级项链");
                equipItemInfo.setEquipType(2);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setMana((int) (25 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setMp((int) (30 * (level / 10 + 1) * waveRatio2));
            }
            break;
            case WEAPON: {
                equipItemInfo.setName(level + "级剑");
                equipItemInfo.setEquipType(3);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setHit((int) (45 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setAttack((int) (35 * (level / 10 + 1) * waveRatio2));
            }
            break;
            case ARMOR: {
                equipItemInfo.setName(level + "级铠甲");
                equipItemInfo.setEquipType(4);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setDefense((int) (50 * (level / 10 + 1) * waveRatio1));
            }
            break;
            case BELT: {
                equipItemInfo.setName(level + "级腰带");
                equipItemInfo.setEquipType(5);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setDefense((int) (20 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setHp((int) (50 * (level / 10 + 1) * waveRatio2));
            }
            break;
            case SHOES: {
                equipItemInfo.setName(level + "级鞋子");
                equipItemInfo.setEquipType(6);

                float waveRatio1 = Utils.getWaveValueFloat(1, 0.3f);
                float waveRatio2 = Utils.getWaveValueFloat(1, 0.3f);
                equipItemInfo.setSpeed((int) (25 * (level / 10 + 1) * waveRatio1));
                equipItemInfo.setDodge((int) (30 * (level / 10 + 1) * waveRatio2));
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
        } else {
            //无属性加成
        }

        //特效
        float totalRatioValue = 100f;
        LinkedHashMap<SpecialEffect, Integer> specialEffectRatioMap = new LinkedHashMap<>();
        specialEffectRatioMap.put(SpecialEffect.SE_WuJiBieXianZhi, 1);
        specialEffectRatioMap.put(SpecialEffect.SE_ShenNong, 3);
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

        if (extraInfo != null && extraInfo.isWuJiBie) {
            List<String> effects = equipItemInfo.getSpecialEffects();
            if (effects == null || effects.isEmpty()) {
                equipItemInfo.setSpecialEffects(Arrays.asList(SpecialEffect.SE_WuJiBieXianZhi.name()));
            } else {
                List<String> newEffects = Arrays.asList(SpecialEffect.SE_WuJiBieXianZhi.name());
                for (String effect : effects) {
                    if (!TextUtils.equals(effect, SpecialEffect.SE_WuJiBieXianZhi.name())) {
                        newEffects.add(effect);
                        break;
                    }
                }
                equipItemInfo.setSpecialEffects(newEffects);
            }
        }

        return new EquipmentItem(equipItemInfo);
    }
}

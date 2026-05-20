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
        double specialEffectRatio = Math.random();
        if (slot == EquipmentItem.Slot.WEAPON) {
            //武器，有必中
            if (specialEffectRatio < 0.01) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_WuJiBieXianZhi.name());
                specialEffectGradeValue = 1.0f;
            } else if (specialEffectRatio < 0.04) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ShenNong.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.07) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ZhenBao.name());
                specialEffectGradeValue = 0.1f;
            } else if (specialEffectRatio < 0.10) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_Xixue.name());
                specialEffectGradeValue = 0.6f;
            } else if (specialEffectRatio < 0.13) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ShenYou.name());
                specialEffectGradeValue = 0.5f;
            } else if (specialEffectRatio < 0.16) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ZhuanZhu.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.19) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ZaiSheng.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.22) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_MingSi.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.25) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_BiZhong.name());
                specialEffectGradeValue = 0.8f;
            }
        } else {
            if (specialEffectRatio < 0.01) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_WuJiBieXianZhi.name());
                specialEffectGradeValue = 1.0f;
            } else if (specialEffectRatio < 0.04) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ShenNong.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.07) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ZhenBao.name());
                specialEffectGradeValue = 0.1f;
            } else if (specialEffectRatio < 0.10) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_Xixue.name());
                specialEffectGradeValue = 0.6f;
            } else if (specialEffectRatio < 0.13) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ShenYou.name());
                specialEffectGradeValue = 0.5f;
            } else if (specialEffectRatio < 0.16) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ZhuanZhu.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.19) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_ZaiSheng.name());
                specialEffectGradeValue = 0.4f;
            } else if (specialEffectRatio < 0.22) {
                equipItemInfo.setSpecialEffect(SpecialEffect.SE_MingSi.name());
                specialEffectGradeValue = 0.4f;
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

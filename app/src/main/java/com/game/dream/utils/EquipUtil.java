package com.game.dream.utils;

import com.game.dream.LogUtil;
import com.game.dream.bean.AddPointResult;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.enums.SpecialEffect;
import com.game.dream.item.Item;

public class EquipUtil {
    private static final int BASE_HP_VALUE = 40;
    private static final int BASE_MP_VALUE = 40;
    private static final int BASE_HIT_VALUE = 30;
    private static final int BASE_ATTACK_VALUE = 10;
    private static final int BASE_DEFENSE_VALUE = 10;
    private static final int BASE_SPEED_VALUE = 8;
    private static final int BASE_MANA_VALUE = 8;
    private static final int BASE_DODGE_VALUE = 12;

    public static String getEquipValueText(EquipItemInfo equipItemInfo) {
        String text = "";
        if (equipItemInfo.getHp() > 0) {
            text += "气血: +" + equipItemInfo.getHp();
        }
        if (equipItemInfo.getMp() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "魔法: +" + equipItemInfo.getMp();
        }
        if (equipItemInfo.getHit() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "命中: +" + equipItemInfo.getHit();
        }
        if (equipItemInfo.getAttack() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "伤害: +" + equipItemInfo.getAttack();
        }
        if (equipItemInfo.getDefense() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "防御: +" + equipItemInfo.getDefense();
        }
        if (equipItemInfo.getMana() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "灵力: +" + equipItemInfo.getMana();
        }
        if (equipItemInfo.getSpeed() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "速度: +" + equipItemInfo.getSpeed();
        }
        if (equipItemInfo.getDodge() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "闪避: +" + equipItemInfo.getDodge();
        }
        return text;
    }

    public static String getEquipPropText(EquipItemInfo equipItemInfo) {
        String text = "";
        if (equipItemInfo.getPropTi() > 0) {
            text += "体质: +" + equipItemInfo.getPropTi();
        }
        if (equipItemInfo.getPropMo() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "魔力: +" + equipItemInfo.getPropMo();
        }
        if (equipItemInfo.getPropLi() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "力量: +" + equipItemInfo.getPropLi();
        }
        if (equipItemInfo.getPropNai() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "耐力: +" + equipItemInfo.getPropNai();
        }
        if (equipItemInfo.getPropMin() > 0) {
            if (!text.isEmpty()) {
                text += "，";
            }
            text += "敏捷: +" + equipItemInfo.getPropMin();
        }
        return text;
    }

    public static String getStoneAddResultText(EquipItemInfo equipItemInfo) {
        String text = "";
        AddPointResult addPointResult = getStoneAddResult(equipItemInfo);
        if (addPointResult != null) {
            switch (equipItemInfo.getEquipType()) {
                case 1: {
                    if (equipItemInfo.getAttackStoneLevel() > 0) {
                        text += "太阳石镶嵌等级：" + equipItemInfo.getAttackStoneLevel() + "级  +" + addPointResult.getAttack() + "伤害";
                    } else {
                        text += "太阳石镶嵌等级：未镶嵌";
                    }
                    text += "\n";
                    if (equipItemInfo.getHitStoneLevel() > 0) {
                        text += "红玛瑙镶嵌等级：" + equipItemInfo.getHitStoneLevel() + "级  +" + addPointResult.getHit() + "命中";
                    } else {
                        text += "红玛瑙镶嵌等级：未镶嵌";
                    }
                }
                break;
                case 2: {
                    if (equipItemInfo.getManaStoneLevel() > 0) {
                        text += "舍利子镶嵌等级：" + equipItemInfo.getManaStoneLevel() + "级  +" + addPointResult.getMana() + "灵力";
                    } else {
                        text += "舍利子镶嵌等级：未镶嵌";
                    }
                    text += "\n";
                    if (equipItemInfo.getMpStoneLevel() > 0) {
                        text += "蓝宝石镶嵌等级：" + equipItemInfo.getMpStoneLevel() + "级  +" + addPointResult.getMagic() + "魔法";
                    } else {
                        text += "蓝宝石镶嵌等级：未镶嵌";
                    }
                }
                break;
                case 3: {
                    if (equipItemInfo.getAttackStoneLevel() > 0) {
                        text += "太阳石镶嵌等级：" + equipItemInfo.getAttackStoneLevel() + "级  +" + addPointResult.getAttack() + "伤害";
                    } else {
                        text += "太阳石镶嵌等级：未镶嵌";
                    }
                    text += "\n";
                    if (equipItemInfo.getManaStoneLevel() > 0) {
                        text += "舍利子镶嵌等级：" + equipItemInfo.getManaStoneLevel() + "级  +" + addPointResult.getMana() + "灵力";
                    } else {
                        text += "舍利子镶嵌等级：未镶嵌";
                    }
                }
                break;
                case 4: {
                    if (equipItemInfo.getDefenseStoneLevel() > 0) {
                        text += "月亮石镶嵌等级：" + equipItemInfo.getDefenseStoneLevel() + "级  +" + addPointResult.getDefense() + "防御";
                    } else {
                        text += "月亮石镶嵌等级：未镶嵌";
                    }
                    text += "\n";
                    if (equipItemInfo.getHpStoneLevel() > 0) {
                        text += "光芒石镶嵌等级：" + equipItemInfo.getHpStoneLevel() + "级  +" + addPointResult.getBlood() + "气血";
                    } else {
                        text += "光芒石镶嵌等级：未镶嵌";
                    }
                }
                break;
                case 5: {
                    if (equipItemInfo.getHpStoneLevel() > 0) {
                        text += "光芒石镶嵌等级：" + equipItemInfo.getHpStoneLevel() + "级  +" + addPointResult.getBlood() + "气血";
                    } else {
                        text += "光芒石镶嵌等级：未镶嵌";
                    }
                    text += "\n";
                    if (equipItemInfo.getSpeedStoneLevel() > 0) {
                        text += "黑宝石镶嵌等级：" + equipItemInfo.getSpeedStoneLevel() + "级  +" + addPointResult.getSpeed() + "速度";
                    } else {
                        text += "黑宝石镶嵌等级：未镶嵌";
                    }
                }
                break;
                case 6: {
                    if (equipItemInfo.getSpeedStoneLevel() > 0) {
                        text += "黑宝石镶嵌等级：" + equipItemInfo.getSpeedStoneLevel() + "级  +" + addPointResult.getSpeed() + "速度";
                    } else {
                        text += "黑宝石镶嵌等级：未镶嵌";
                    }
                    text += "\n";
                    if (equipItemInfo.getDodgeStoneLevel() > 0) {
                        text += "神秘石镶嵌等级：" + equipItemInfo.getDodgeStoneLevel() + "级  +" + addPointResult.getDodge() + "闪避";
                    } else {
                        text += "神秘石镶嵌等级：未镶嵌";
                    }
                }
                break;
            }
        }
        return text;
    }

    public static String getEquipSpecialEffectText(EquipItemInfo equipItemInfo) {
        String text = "";
        if (equipItemInfo != null && equipItemInfo.getSpecialEffects() != null && !equipItemInfo.getSpecialEffects().isEmpty()) {
            boolean isFirst = true;
            for (String specialEffectItem : equipItemInfo.getSpecialEffects()) {
                SpecialEffect specialEffect = SpecialEffect.getSpecialEffectWithName(specialEffectItem);
                if (specialEffect != null) {
                    if (isFirst) {
                        text = "特效：";
                    } else {
                        text = text + "，";
                    }
                    text = text + specialEffect.getDesc();

                    isFirst = false;
                }
            }
        }

        return text;
    }

    public static String getEquipXiLianPropText(EquipItemInfo equipItemInfo) {
        String text = "附加属性：";
        boolean hasXiLianProp = false;
        if (equipItemInfo.getAttackCritRatio() > 0) {
            text += "\n";
            text += "物理暴击几率: +" + equipItemInfo.getAttackCritRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getMagicCritRatio() > 0) {
            text += "\n";
            text += "法术暴击几率: +" + equipItemInfo.getMagicCritRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getAttackSpeedRatio() > 0) {
            text += "\n";
            text += "攻击速度增幅: +" + equipItemInfo.getAttackSpeedRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getMagicSpeedRatio() > 0) {
            text += "\n";
            text += "法术速度增幅: +" + equipItemInfo.getMagicSpeedRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getAttackValueRatio() > 0) {
            text += "\n";
            text += "攻击伤害增幅: +" + equipItemInfo.getAttackValueRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getMagicValueRatio() > 0) {
            text += "\n";
            text += "法术伤害增幅: +" + equipItemInfo.getMagicValueRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getBeAttackedValueRatio() > 0) {
            text += "\n";
            text += "被攻击伤害减伤: +" + equipItemInfo.getBeAttackedValueRatio();
            hasXiLianProp = true;
        }
        if (equipItemInfo.getBeMagicedValueRatio() > 0) {
            text += "\n";
            text += "被法术伤害减伤: +" + equipItemInfo.getBeMagicedValueRatio();
            hasXiLianProp = true;
        }

        if (hasXiLianProp) {
            return text;
        }

        return "";
    }

    public static AddPointResult getStoneAddResult(EquipItemInfo equipItemInfo) {
        int blood = 0, magic = 0, hit = 0, attack = 0, defense = 0, speed = 0, dodge = 0, mana = 0;
        if (equipItemInfo != null) {
            if (equipItemInfo.getHpStoneLevel() > 0) {
                blood = caculateValueWithLevel(BASE_HP_VALUE, equipItemInfo.getHpStoneLevel());
            }
            if (equipItemInfo.getMpStoneLevel() > 0) {
                magic = caculateValueWithLevel(BASE_MP_VALUE, equipItemInfo.getMpStoneLevel());
            }
            if (equipItemInfo.getHitStoneLevel() > 0) {
                hit = caculateValueWithLevel(BASE_HIT_VALUE, equipItemInfo.getHitStoneLevel());
            }
            if (equipItemInfo.getAttackStoneLevel() > 0) {
                attack = caculateValueWithLevel(BASE_ATTACK_VALUE, equipItemInfo.getAttackStoneLevel());
            }
            if (equipItemInfo.getDefenseStoneLevel() > 0) {
                defense = caculateValueWithLevel(BASE_DEFENSE_VALUE, equipItemInfo.getDefenseStoneLevel());
            }
            if (equipItemInfo.getSpeedStoneLevel() > 0) {
                speed = caculateValueWithLevel(BASE_SPEED_VALUE, equipItemInfo.getSpeedStoneLevel());
            }
            if (equipItemInfo.getManaStoneLevel() > 0) {
                mana = caculateValueWithLevel(BASE_MANA_VALUE, equipItemInfo.getManaStoneLevel());
            }
            if (equipItemInfo.getDodgeStoneLevel() > 0) {
                dodge = caculateValueWithLevel(BASE_DODGE_VALUE, equipItemInfo.getDodgeStoneLevel());
            }

            return new AddPointResult(blood, magic, hit, attack, defense, speed, dodge, mana);
        }
        return null;
    }

    private static int caculateValueWithLevel(int baseValue, int level) {
        float value = 0;
        for (int i = 0; i < level; i++) {
            float curValue = baseValue * (1 + 0.1f * i);
            value += curValue;
        }
        return (int) value;
    }

    public static Item.Rarity caculateEquipRarity(EquipItemInfo equipItemInfo) {
        if (equipItemInfo != null) {
            float baseRarityValue = 0;
            float srcPropRarityValue = 0;

            int level = equipItemInfo.getLevel();
            switch (equipItemInfo.getEquipType()) {
                case 1: {
                    float defenseRatio = equipItemInfo.getDefense() * 1f / (30 * (level / 10f + 1));
                    float mpRatio = equipItemInfo.getMp() * 1f / (30 * (level / 10f + 1));

                    baseRarityValue = ((defenseRatio + mpRatio) - 0.7f * 2) / (1.3f * 2 - 0.7f * 2) * 5;
                }
                break;
                case 2: {
                    float manaRatio = equipItemInfo.getMana() * 1f / (25 * (level / 10f + 1));
                    float mpRatio = equipItemInfo.getMp() * 1f / (30 * (level / 10f + 1));

                    baseRarityValue = ((manaRatio + mpRatio) - 0.7f * 2) / (1.3f * 2 - 0.7f * 2) * 5;
                }
                break;
                case 3: {
                    float hitRatio = equipItemInfo.getHit() * 1f / (45 * (level / 10f + 1));
                    float attackRatio = equipItemInfo.getAttack() * 1f / (35 * (level / 10f + 1));

                    baseRarityValue = ((hitRatio + attackRatio) - 0.7f * 2) / (1.3f * 2 - 0.7f * 2) * 5;
                }
                break;
                case 4: {
                    float defenseRatio = equipItemInfo.getDefense() * 1f / (50 * (level / 10f + 1));

                    baseRarityValue = (defenseRatio - 0.7f) / (1.3f - 0.7f) * 5;
                }
                break;
                case 5: {
                    float defenseRatio = equipItemInfo.getDefense() * 1f / (20 * (level / 10f + 1));
                    float hpRatio = equipItemInfo.getHp() * 1f / (50 * (level / 10f + 1));

                    baseRarityValue = ((defenseRatio + hpRatio) - 0.7f * 2) / (1.3f * 2 - 0.7f * 2) * 5;
                }
                break;
                case 6: {
                    float speedRatio = equipItemInfo.getSpeed() * 1f / (25 * (level / 10f + 1));
                    float dodgeRatio = equipItemInfo.getDodge() * 1f / (30 * (level / 10f + 1));

                    baseRarityValue = ((speedRatio + dodgeRatio) - 0.7f * 2) / (1.3f * 2 - 0.7f * 2) * 5;
                }
                break;
            }

            int srcPropValue = equipItemInfo.getPropTi() + equipItemInfo.getPropMo() + equipItemInfo.getPropLi()
                    + equipItemInfo.getPropNai() + equipItemInfo.getPropMin();
            if (srcPropValue > 0) {
                srcPropRarityValue = (srcPropValue / (10f + 3 * (level / 10f)) - 0.75f) / (2.5f - 0.75f) * 5;
            }

            float rarityValue = (baseRarityValue + srcPropRarityValue) / 2;

            //LogUtil.i("aaaaaaaaaaaaaaaaaaa " + equipItemInfo.getName() + ", " + baseRarityValue + ", " + srcPropRarityValue + ", " + rarityValue);

            if (equipItemInfo.getSpecialEffects() != null && !equipItemInfo.getSpecialEffects().isEmpty()) {
                rarityValue += equipItemInfo.getSpecialEffects().size() * 1f;
                //LogUtil.i("aaaaaaaaaaaaaaaaaaa " + rarityValue);
            }

            if (rarityValue < 1) {
                return Item.Rarity.Rarity_1;
            } else if (rarityValue < 2) {
                return Item.Rarity.Rarity_2;
            } else if (rarityValue < 3) {
                return Item.Rarity.Rarity_3;
            } else if (rarityValue < 4) {
                return Item.Rarity.Rarity_4;
            } else if (rarityValue < 5) {
                return Item.Rarity.Rarity_5;
            } else {
                return Item.Rarity.Rarity_6;
            }
        }

        return Item.Rarity.Rarity_1;
    }
}

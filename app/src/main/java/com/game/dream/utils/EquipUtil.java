package com.game.dream.utils;

import com.game.dream.bean.AddPointResult;
import com.game.dream.bean.EquipItemInfo;

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
            if (addPointResult.getBlood() > 0) {
                text += equipItemInfo.getHpStoneLevel() + "级光芒石 +" + addPointResult.getBlood() + "气血";
            }
            if (addPointResult.getMagic() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getMpStoneLevel() + "级蓝宝石 +" + addPointResult.getMagic() + "魔法";
            }
            if (addPointResult.getHit() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getHpStoneLevel() + "级红玛瑙 +" + addPointResult.getHit() + "命中";
            }
            if (addPointResult.getAttack() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getAttackStoneLevel() + "级太阳石 +" + addPointResult.getAttack() + "伤害";
            }
            if (addPointResult.getDefense() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getDefenseStoneLevel() + "级月亮石 +" + addPointResult.getDefense() + "防御";
            }
            if (addPointResult.getSpeed() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getSpeedStoneLevel() + "级黑宝石 +" + addPointResult.getSpeed() + "速度";
            }
            if (addPointResult.getMana() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getManaStoneLevel() + "级舍利子 +" + addPointResult.getMana() + "灵力";
            }
            if (addPointResult.getDodge() > 0) {
                if (!text.isEmpty()) {
                    text += "，";
                }
                text += equipItemInfo.getDodgeStoneLevel() + "级红宝石 +" + addPointResult.getDodge() + "闪避";
            }
        }
        return text;
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
}

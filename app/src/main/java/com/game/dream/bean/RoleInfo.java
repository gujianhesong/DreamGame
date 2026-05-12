package com.game.dream.bean;

import com.game.dream.enums.SkillType;
import com.game.dream.system.RoleSystem;
import com.game.dream.system.SkillSystem;

import java.util.function.Predicate;

public class RoleInfo {

    public static class EquipAddition {
        public int propTi;
        public int propMo;
        public int propLi;
        public int propNai;
        public int propMin;

        public int blood;
        public int magic;
        public int hit;
        public int attack;
        public int defense;
        public int speed;
        public int dodge;
        public int mana;
    }

    public static class SelfProperty {
        public int propTi;
        public int propMo;
        public int propLi;
        public int propNai;
        public int propMin;

        private int blood;
        private int magic;
        private int hit;
        private int attack;
        private int defense;
        private int speed;
        private int dodge;
        private int mana;
    }

    //基础属性
    private String name;
    private int level;
    private String label;
    private long exp;
    private long money;
    private int remainPoints;

    private int hp;
    private int mp;

    private int huoli;
    private int tili;

    //自身属性
    private SelfProperty selfProperty;
    //装备加成
    private EquipAddition equipAddition;

    //人物修炼
    private int practiceAttack;
    private int practiceMagic;
    private int practiceDefense;
    private int practiceMagicDefense;

    //bb修炼
    private int practiceBBAttack;
    private int practiceBBMagic;
    private int practiceBBDefense;
    private int practiceBBMagicDefense;


    //地图信息
    private int mapId;
    private int mapX = -1;
    private int mapY = -1;

    public RoleInfo() {
        selfProperty = new SelfProperty();
        equipAddition = new EquipAddition();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public int getRemainPoints() {
        return remainPoints;
    }

    public void setRemainPoints(int remainPoints) {
        this.remainPoints = remainPoints;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getHuoli() {
        return huoli;
    }

    public void setHuoli(int huoli) {
        this.huoli = huoli;
    }

    public int getTili() {
        return tili;
    }

    public void setTili(int tili) {
        this.tili = tili;
    }

    public SelfProperty getSelfProperty() {
        return selfProperty;
    }

    public void setSelfProperty(SelfProperty selfProperty) {
        this.selfProperty = selfProperty;
    }

    public EquipAddition getEquipAddtion() {
        return equipAddition;
    }

    public void setEquipAddition(EquipAddition equipAddition) {
        this.equipAddition = equipAddition;
    }

    public int getPracticeAttack() {
        practiceAttack = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_AttackPractise) {
                practiceAttack = skillInfo.getLevel();
                break;
            }
        }
        return practiceAttack;
    }

    public int getPracticeMagic() {
        practiceMagic = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_MagicPractise) {
                practiceMagic = skillInfo.getLevel();
                break;
            }
        }
        return practiceMagic;
    }

    public int getPracticeDefense() {
        practiceDefense = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_DefensePractise) {
                practiceDefense = skillInfo.getLevel();
                break;
            }
        }
        return practiceDefense;
    }

    public int getPracticeMagicDefense() {
        practiceMagicDefense = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_MagicDefensePractise) {
                practiceMagicDefense = skillInfo.getLevel();
                break;
            }
        }
        return practiceMagicDefense;
    }

    public int getPracticeBBAttack() {
        practiceBBAttack = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_BB_AttackPractise) {
                practiceBBAttack = skillInfo.getLevel();
                break;
            }
        }
        return practiceBBAttack;
    }

    public int getPracticeBBMagic() {
        practiceBBMagic = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_BB_MagicPractise) {
                practiceBBMagic = skillInfo.getLevel();
                break;
            }
        }
        return practiceBBMagic;
    }

    public int getPracticeBBDefense() {
        practiceBBDefense = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_BB_DefensePractise) {
                practiceBBDefense = skillInfo.getLevel();
                break;
            }
        }
        return practiceBBDefense;
    }

    public int getPracticeBBMagicDefense() {
        practiceBBMagicDefense = 0;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.PST_BB_MagicDefensePractise) {
                practiceBBMagicDefense = skillInfo.getLevel();
                break;
            }
        }
        return practiceBBMagicDefense;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getMapX() {
        return mapX;
    }

    public void setMapX(int mapX) {
        this.mapX = mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void setMapY(int mapY) {
        this.mapY = mapY;
    }

    public int getPropTi() {
        int propTi = selfProperty.propTi + equipAddition.propTi;
        return propTi;
    }

    public int getPropMo() {
        int propMo = selfProperty.propMo + equipAddition.propMo;
        return propMo;
    }

    public int getPropLi() {
        int propLi = selfProperty.propLi + equipAddition.propLi;
        return propLi;
    }

    public int getPropNai() {
        int propNai = selfProperty.propNai + equipAddition.propNai;
        return propNai;
    }

    public int getPropMin() {
        int propMin = selfProperty.propMin + equipAddition.propMin;
        return propMin;
    }

    public int getBloodCap() {
        int baseValue = 100;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getBlood() + equipAddition.blood;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.AST_QiangShen && skillInfo.getLevel() > 0) {
                value = (int) (value * (1 + 0.01 * skillInfo.getLevel()));
                break;
            }
        }
        return value;
    }

    public int getMagicCap() {
        int baseValue = 80;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getMagic() + equipAddition.magic;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.AST_MingXiang && skillInfo.getLevel() > 0) {
                value = (int) (value * (1 + 0.01 * skillInfo.getLevel()));
                break;
            }
        }
        return value;
    }

    public int getHit() {
        int baseValue = 50;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getHit() + equipAddition.hit;
        return value;
    }

    public int getAttack() {
        int baseValue = 40;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getAttack() + equipAddition.attack;
        return value;
    }

    public int getDefense() {
        int baseValue = 40;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getDefense() + equipAddition.defense;
        return value;
    }

    public int getSpeed() {
        int baseValue = 20;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getSpeed() + equipAddition.speed;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.AST_ShenSu && skillInfo.getLevel() > 0) {
                value += skillInfo.getLevel() * 2;
                break;
            }
        }

        return value;
    }

    public int getDodge() {
        int baseValue = 20;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getDodge() + equipAddition.dodge;
        return value;
    }

    public int getMana() {
        int baseValue = 20;
        AddPointResult result = RoleSystem.getInstance().caculateAddPoints(getPropTi(), getPropMo(),
                getPropLi(), getPropNai(), getPropMin());
        int value = baseValue + result.getMana() + equipAddition.mana;
        return value;
    }

    public int getHuoliMax() {
        int value = 200;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.AST_YangShengZhiDao && skillInfo.getLevel() > 0) {
                value += skillInfo.getLevel() * 10;
                break;
            }
        }
        return value;
    }

    public int getTiliMax() {
        int value = 200;
        for (SkillInfo skillInfo : SkillSystem.getInstance().getAssistSkillInfos()) {
            if (skillInfo.getSkillType() == SkillType.AST_JianShen && skillInfo.getLevel() > 0) {
                value += skillInfo.getLevel() * 10;
                break;
            }
        }
        return value;
    }
}

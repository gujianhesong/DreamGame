package com.game.dream.system;

import com.game.dream.bean.AddPointResult;
import com.game.dream.bean.EquipItemInfo;
import com.game.dream.bean.RoleInfo;
import com.game.dream.item.EquipmentItem;
import com.game.dream.utils.EquipUtil;

public class RoleSystem {

    private static RoleSystem instance = new RoleSystem();

    public static RoleSystem getInstance() {
        return instance;
    }

    private RoleInfo roleInfo;

    private RoleSystem() {
    }

    public RoleInfo getRoleInfo() {
        return roleInfo;
    }

    public void setRoleInfo(RoleInfo roleInfo) {
        this.roleInfo = roleInfo;
    }

    public RoleInfo getInitRoleInfo() {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setName("剑侠客");
        roleInfo.setLabel("初出茅庐");
        roleInfo.getSelfProperty().propTi = 10;
        roleInfo.getSelfProperty().propMo = 10;
        roleInfo.getSelfProperty().propLi = 10;
        roleInfo.getSelfProperty().propNai = 10;
        roleInfo.getSelfProperty().propMin = 10;
        roleInfo.setHp(roleInfo.getBloodCap());
        roleInfo.setMp(roleInfo.getMagicCap());
        return roleInfo;
    }

    public AddPointResult caculateAddPoints(int ti, int mo, int li, int nai, int min) {
        int blood = ti * 8;
        int magic = mo * 10;
        int hit = (int) (li * 2 + mo * 1.5f);
        int attack = (int) (li * 1.5f);
        int defense = (int) (nai * 1.5f);
        int speed = (int) (min * 1.1f);
        int dodge = (int) (min * 1.5f);
        int mana = (int) (ti * 0.3f + mo * 0.8f + li * 0.4f + nai * 0.2f);
        return new AddPointResult(blood, magic, hit, attack, defense, speed, dodge, mana);
    }

    /**
     * Add money
     */
    public void addMoney(int money) {
        roleInfo.setMoney(roleInfo.getMoney() + money);
    }

    public int getExpForNextLevel() {
        return calculateExpForLevel(roleInfo.getLevel() + 1);
    }

    /**
     * Calculate experience needed for a given level
     * Formula: base * level^1.5 (increasing difficulty)
     */
    private int calculateExpForLevel(int level) {
        return (int) (100 * Math.pow(level, 1.5));
    }

    /**
     * Add experience points
     */
    public void addExperience(int exp) {
        roleInfo.setExp(roleInfo.getExp() + exp);
        // Check for level up
        while (roleInfo.getExp() >= calculateExpForLevel(roleInfo.getLevel() + 1)) {
            levelUp();
        }
    }

    /**
     * Level up the player
     */
    private void levelUp() {
        int experienceToNextLevel = calculateExpForLevel(roleInfo.getLevel() + 1);
        roleInfo.setExp(roleInfo.getExp() - experienceToNextLevel);
        roleInfo.setLevel(roleInfo.getLevel() + 1);

        // Increase stats on level up
        roleInfo.getSelfProperty().propTi += 1;
        roleInfo.getSelfProperty().propMo += 1;
        roleInfo.getSelfProperty().propLi += 1;
        roleInfo.getSelfProperty().propNai += 1;
        roleInfo.getSelfProperty().propMin += 1;
        roleInfo.setRemainPoints(roleInfo.getRemainPoints() + 5);
    }

    public void updateRoleEquipProperty() {
        RoleInfo.EquipAddition equipAddition = new RoleInfo.EquipAddition();
        EquipmentItem helmet = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.HELMET);
        EquipmentItem accessory = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ACCESSORY);
        EquipmentItem weapon = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.WEAPON);
        EquipmentItem armor = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.ARMOR);
        EquipmentItem belt = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.BELT);
        EquipmentItem shoes = ItemSystem.getInstance().getEquippedItem(EquipmentItem.Slot.SHOES);
        EquipmentItem[] equipments = {helmet, accessory, weapon, armor, belt, shoes};
        for (EquipmentItem equipmentItem : equipments) {
            if (equipmentItem != null) {
                EquipItemInfo equipItemInfo = equipmentItem.getEquipItemInfo();
                equipAddition.propTi += equipItemInfo.getPropTi();
                equipAddition.propMo += equipItemInfo.getPropMo();
                equipAddition.propLi += equipItemInfo.getPropLi();
                equipAddition.propNai += equipItemInfo.getPropNai();
                equipAddition.propMin += equipItemInfo.getPropMin();

                equipAddition.blood += equipItemInfo.getHp();
                equipAddition.magic += equipItemInfo.getMp();
                equipAddition.hit += equipItemInfo.getHit();
                equipAddition.attack += equipItemInfo.getAttack();
                equipAddition.defense += equipItemInfo.getDefense();
                equipAddition.speed += equipItemInfo.getSpeed();
                equipAddition.dodge += equipItemInfo.getDodge();
                equipAddition.mana += equipItemInfo.getMana();

                AddPointResult addPointResult = EquipUtil.getStoneAddResult(equipItemInfo);
                if(addPointResult != null){
                    equipAddition.blood += addPointResult.getBlood();
                    equipAddition.magic += addPointResult.getMagic();
                    equipAddition.hit += addPointResult.getHit();
                    equipAddition.attack += addPointResult.getAttack();
                    equipAddition.defense += addPointResult.getDefense();
                    equipAddition.speed += addPointResult.getSpeed();
                    equipAddition.dodge += addPointResult.getDodge();
                    equipAddition.mana += addPointResult.getMana();
                }
            }
        }
        roleInfo.setEquipAddition(equipAddition);
    }
}

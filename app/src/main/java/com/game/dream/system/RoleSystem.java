package com.game.dream.system;

import com.game.dream.LogUtil;
import com.game.dream.bean.AddPointResult;
import com.game.dream.bean.RoleInfo;

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
        roleInfo.setPropTi(10);
        roleInfo.setPropMo(10);
        roleInfo.setPropLi(10);
        roleInfo.setPropNai(10);
        roleInfo.setPropMin(10);

        updateProperty(roleInfo);

        return roleInfo;
    }

    /**
     * 更新属性
     */
    public void updateProperty(RoleInfo roleInfo) {
        AddPointResult addPointResult = caculateAddPoints(roleInfo.getPropTi(), roleInfo.getPropMo(),
                roleInfo.getPropLi(), roleInfo.getPropNai(), roleInfo.getPropMin());
        int blood = addPointResult.getBlood() + 100;
        int magic = addPointResult.getMagic() + 80;
        int hit = addPointResult.getHit() + 50;
        int attack = addPointResult.getAttack() + 40;
        int defense = addPointResult.getDefense() + 20;
        int speed = addPointResult.getSpeed() + 10;
        int dodge = addPointResult.getDodge() + 20;
        int mana = addPointResult.getMana() + 10;

        roleInfo.setBlood(blood);
        roleInfo.setBloodCap(blood);
        roleInfo.setMagic(magic);
        roleInfo.setMagicCap(magic);
        roleInfo.setHit(hit);
        roleInfo.setAttack(attack);
        roleInfo.setDefense(defense);
        roleInfo.setSpeed(speed);
        roleInfo.setDodge(dodge);
        roleInfo.setMana(mana);

        String[] names = {"ti", "mo", "li", "nai", "min", "hp", "mp",
                "hit", "attack", "defense", "speed", "dodge", "mana"};
        int[] values = {roleInfo.getPropTi(), roleInfo.getPropMo(), roleInfo.getPropLi(),
                roleInfo.getPropNai(), roleInfo.getPropMin(), roleInfo.getBlood(), roleInfo.getMagic(),
                roleInfo.getHit(), roleInfo.getAttack(), roleInfo.getDefense(), roleInfo.getSpeed(),
                roleInfo.getDodge(), roleInfo.getMana()};
        StringBuilder builder = new StringBuilder("角色属性更新: ");
        for (int i = 0; i < names.length; i++) {
            builder.append(names[i]).append(":").append(values[i]).append(",");
        }
        LogUtil.i(builder.toString());
    }

    public AddPointResult caculateAddPoints(int ti, int mo, int li, int nai, int min) {
        int blood = ti * 8;
        int magic = mo * 10;
        int hit = (int) (li * 2 + mo * 1.5f);
        int attack = (int) (li * 1.5f);
        int defense = (int) (nai * 1.5f);
        int speed = (int) (min * 1.1f);
        int dodge = (int) (min * 1.5f);
        int mana = (int) (li * 0.2f + mo * 0.8f + li * 0.3f);
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
        roleInfo.setPropTi(roleInfo.getPropTi() + 1);
        roleInfo.setPropMo(roleInfo.getPropMo() + 1);
        roleInfo.setPropLi(roleInfo.getPropLi() + 1);
        roleInfo.setPropNai(roleInfo.getPropNai() + 1);
        roleInfo.setPropMin(roleInfo.getPropMin() + 1);
        roleInfo.setRemainPoints(roleInfo.getRemainPoints() + 5);

        updateProperty(roleInfo);
    }
}

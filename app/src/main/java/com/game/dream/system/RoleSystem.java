package com.game.dream.system;

import com.game.dream.bean.RoleInfo;

public class RoleSystem {

    private static RoleSystem instance = new RoleSystem();

    public static RoleSystem getInstance(){
        return instance;
    }

    private RoleInfo roleInfo;

    private RoleSystem(){
    }

    public RoleInfo getRoleInfo(){
        return roleInfo;
    }

    public void setRoleInfo(RoleInfo roleInfo){
        this.roleInfo = roleInfo;
    }

    public RoleInfo getInitRoleInfo(){
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setName("事了拂衣去");
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
        int blood = roleInfo.getPropTi() * 5 + 100;
        int magic = roleInfo.getPropMo() * 3 + 80;
        int hit = roleInfo.getPropLi() * 2 + 30;
        int attack = (int) (roleInfo.getPropLi() * 0.7f + 40);
        int defense = (int) (roleInfo.getPropTi() * 1.5f + 10);
        int speed = (int) (roleInfo.getPropMin() * 0.9f + 10);
        int dodge = roleInfo.getPropMin() + 10;
        int mana = (int) (roleInfo.getPropTi() * 0.3 + roleInfo.getPropMo() * 0.7 + roleInfo.getPropLi() * 0.4);

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
    }
}

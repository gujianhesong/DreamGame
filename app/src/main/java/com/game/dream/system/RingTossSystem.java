package com.game.dream.system;

import com.game.dream.GameEngine;
import com.game.dream.bean.RoleInfo;

/**
 * 金陵南市套圈：入场费含 10 个圈，命中按靶位奖银两。
 */
public class RingTossSystem {

    public static final int ENTRY_FEE = 200;
    public static final int RINGS_PER_ROUND = 10;

    private static RingTossSystem instance = new RingTossSystem();

    public static RingTossSystem getInstance() {
        return instance;
    }

    private RingTossSystem() {
    }

    public boolean canAffordEntry() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        return role != null && role.getMoney() >= ENTRY_FEE;
    }

    public boolean payEntry() {
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role == null || role.getMoney() < ENTRY_FEE) {
            return false;
        }
        role.setMoney(role.getMoney() - ENTRY_FEE);
        return true;
    }

    public void awardSilver(int amount) {
        if (amount <= 0) {
            return;
        }
        RoleInfo role = RoleSystem.getInstance().getRoleInfo();
        if (role != null) {
            role.setMoney(role.getMoney() + amount);
        }
    }

    public void onRoundFinished(int hits, int silverWon, int farHits) {
        TitleSystem.getInstance().onRingTossRound(hits, silverWon, farHits);
        if (silverWon > ENTRY_FEE) {
            GameEngine.getInstance().showCenterToast("本局净赚 " + (silverWon - ENTRY_FEE) + " 银", 2200);
        } else if (hits > 0) {
            GameEngine.getInstance().showCenterToast("套中 " + hits + " 次，得 " + silverWon + " 银", 2200);
        }
    }
}
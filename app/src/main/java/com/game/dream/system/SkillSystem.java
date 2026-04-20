package com.game.dream.system;

import com.game.dream.bean.RoleInfo;
import com.game.dream.bean.SkillInfo;
import com.game.dream.enums.SkillType;

import java.util.ArrayList;
import java.util.List;

public class SkillSystem {

    private static SkillSystem instance = new SkillSystem();

    public static SkillSystem getInstance() {
        return instance;
    }

    private SkillSystem() {
        initTotalSkills();
        initPlayerSkills();
    }

    List<SkillInfo> totalSkills = new ArrayList<>();
    List<SkillInfo> playerSkills = new ArrayList<>();

    private void initTotalSkills() {
        totalSkills.add(new SkillInfo(SkillType.FIREBALL, 0, "火球术", ""));
        totalSkills.add(new SkillInfo(SkillType.ICE_BOLT, 0, "寒冰术", ""));
        totalSkills.add(new SkillInfo(SkillType.LIGHTNING, 0, "迅光术", ""));
    }

    private void initPlayerSkills() {
        playerSkills.add(new SkillInfo(SkillType.FIREBALL, 1, "火球术", ""));
        playerSkills.add(new SkillInfo(SkillType.ICE_BOLT, 1, "寒冰术", ""));
        playerSkills.add(new SkillInfo(SkillType.LIGHTNING, 1, "迅光术", ""));
    }

    public List<SkillInfo> getTotalSkills() {
        return totalSkills;
    }

    public List<SkillInfo> getPlayerSkills() {
        return playerSkills;
    }

}

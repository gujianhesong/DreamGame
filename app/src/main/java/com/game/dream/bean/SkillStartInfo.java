package com.game.dream.bean;

import com.game.dream.Projectile;
import com.game.dream.skill.SkillEffect;

import java.util.List;

public class SkillStartInfo {

    private List<Projectile> projectiles;
    private SkillEffect skillEffect;

    public SkillEffect getSkillEffect() {
        return skillEffect;
    }

    public void setSkillEffect(SkillEffect skillEffect) {
        this.skillEffect = skillEffect;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void setProjectiles(List<Projectile> projectiles) {
        this.projectiles = projectiles;
    }
}

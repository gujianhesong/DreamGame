package com.game.dream.title;

import com.game.dream.enums.TitleRarity;

public class TitleDef {

    public final String id;
    public final String displayName;
    public final TitleRarity rarity;
    public final boolean hidden;
    /** 未解锁时展示的条件提示（隐藏称号不含入未获得列表） */
    public final String hint;
    public final UnlockRule rule;

    public TitleDef(String id, String displayName, TitleRarity rarity, boolean hidden,
                    String hint, UnlockRule rule) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.hidden = hidden;
        this.hint = hint;
        this.rule = rule;
    }
}
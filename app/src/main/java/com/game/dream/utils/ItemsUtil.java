package com.game.dream.utils;

import com.game.dream.item.Item;

public class ItemsUtil {

    /**
     * Get rarity text
     */
    public static String getRarityText(Item.Rarity rarity) {
        switch (rarity) {
            case Rarity_1:
                return "白色";
            case Rarity_2:
                return "绿色";
            case Rarity_3:
                return "蓝色";
            case Rarity_4:
                return "紫色";
            case Rarity_5:
                return "金色";
            case Rarity_6:
                return "橙色";
            default:
                return "未知";
        }
    }

}

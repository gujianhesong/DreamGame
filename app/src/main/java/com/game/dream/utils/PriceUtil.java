package com.game.dream.utils;

public class PriceUtil {

    public static int getItemPriceWith10Level(int basePrice, int level){
        level = level / 10;
        int price = (int) (basePrice * Math.pow(2, level));
        return price;
    }

    public static int getItemPriceWithLevel(int basePrice, int level){
        int price = (int) (basePrice * Math.pow(2, level));
        return price;
    }

}

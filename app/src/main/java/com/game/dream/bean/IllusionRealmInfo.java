package com.game.dream.bean;

/**
 * 存档中的幻境挑战进度
 */
public class IllusionRealmInfo {

    /** 凡尘幻境已通关最高关（0～20） */
    private int fanchenCleared;
    /** 妖狐幻境已通关最高关（0～20） */
    private int yaohuCleared;
    /** 龙宫幻境已通关最高关（0～20） */
    private int longgongCleared;

    public int getFanchenCleared() {
        return fanchenCleared;
    }

    public void setFanchenCleared(int fanchenCleared) {
        this.fanchenCleared = fanchenCleared;
    }

    public int getYaohuCleared() {
        return yaohuCleared;
    }

    public void setYaohuCleared(int yaohuCleared) {
        this.yaohuCleared = yaohuCleared;
    }

    public int getLonggongCleared() {
        return longgongCleared;
    }

    public void setLonggongCleared(int longgongCleared) {
        this.longgongCleared = longgongCleared;
    }
}

package com.game.dream.item;

/**
 * Consumable item (potions, food, etc.)
 */
public class ConsumableItem extends Item {
    public enum EffectType {
        HEAL_HP,        // Restore health
        HEAL_MP,        // Restore magic
        BUFF_HP,        // Temporary health boost
        BUFF_ATTACK,    // Temporary attack boost
        BUFF_DEFENSE,   // Temporary defense boost
        BUFF_MANA,      // Temporary mana boost
        BUFF_SPEED      // Temporary speed boost
    }

    private EffectType effectType;
    private int effectValue;
    private int duration; // Duration in milliseconds (0 for instant)

    public ConsumableItem(int id, String name, String description, Rarity rarity,
                          int maxStack, int value, EffectType effectType, int effectValue, int duration) {
        super(id, name, description, Type.CONSUMABLE, rarity, maxStack, value);
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.duration = duration;
    }

    @Override
    public boolean use() {
        // This will be called by the player/inventory system
        return true; // Consumed after use
    }

    public EffectType getEffectType() { return effectType; }
    public int getEffectValue() { return effectValue; }
    public int getDuration() { return duration; }
}

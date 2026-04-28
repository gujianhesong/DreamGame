package com.game.dream.item;

/**
 * Base item class for all items in the game
 */
public class Item {
    public enum Type {
        CONSUMABLE,     // Potions, food, etc.
        EQUIPMENT,      // Weapons, armor, accessories
        MATERIAL,       // Crafting materials
        QUEST_ITEM,     // Quest-related items
        SPECIAL         // Special unique items
    }

    public enum Rarity {
        Rarity_1,           // White - Common
        Rarity_2,           // Green - Uncommon
        Rarity_3,           // Blue - Rare
        Rarity_4,           // Purple - Epic
        Rarity_5            // Orange - Legendary
    }

    protected int id;
    protected String name;
    protected String description;
    protected Type type;
    protected Rarity rarity;
    protected int maxStack;
    protected int value; // Sell/buy value

    public Item(int id, String name, String description, Type type, Rarity rarity, int maxStack, int value) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rarity = rarity;
        this.maxStack = maxStack;
        this.value = value;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public int getMaxStack() { return maxStack; }
    public int getValue() { return value; }

    /**
     * Use the item (for consumables)
     * @return true if item was consumed
     */
    public boolean use() {
        return false; // Default: not usable
    }

    /**
     * Get color based on rarity
     */
    public int getColor() {
        switch (rarity) {
            case Rarity_1:
                return android.graphics.Color.WHITE;
            case Rarity_2:
                return android.graphics.Color.rgb(100, 255, 100); // Green
            case Rarity_3:
                return android.graphics.Color.rgb(100, 181, 246); // Blue
            case Rarity_4:
                return android.graphics.Color.rgb(200, 100, 255); // Purple
            case Rarity_5:
                return android.graphics.Color.rgb(255, 165, 0); // Orange
            default:
                return android.graphics.Color.WHITE;
        }
    }
}

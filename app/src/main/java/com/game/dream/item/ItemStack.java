package com.game.dream.item;

/**
 * Item stack - represents a stack of items in inventory
 */
public class ItemStack {
    private Item item;
    private int quantity;

    public ItemStack(Item item, int quantity) {
        this.item = item;
        this.quantity = Math.min(quantity, item.getMaxStack());
    }

    /**
     * Add items to this stack
     * @return remaining items that couldn't fit
     */
    public int add(int amount) {
        int space = item.getMaxStack() - quantity;
        if (space <= 0) return amount;

        int added = Math.min(amount, space);
        quantity += added;
        return amount - added;
    }

    /**
     * Remove items from this stack
     * @return actual amount removed
     */
    public int remove(int amount) {
        int removed = Math.min(amount, quantity);
        quantity -= removed;
        return removed;
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    public boolean isFull() {
        return quantity >= item.getMaxStack();
    }

    // Getters and Setters
    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, Math.min(quantity, item.getMaxStack()));
    }
}

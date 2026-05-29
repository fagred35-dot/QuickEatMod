package com.quickeat.compat;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for mod compatibility modules.
 */
public interface IModCompat {

    /**
     * Check if this item is consumable according to the compat mod.
     * Called when the base isConsumable check returns false.
     */
    default boolean isConsumable(ItemStack stack) {
        return false;
    }

    /**
     * Check if eating should be prevented for this item.
     * For example, Spice of Life might prevent eating if diversity is too low.
     */
    default boolean shouldPreventEating(ItemStack stack) {
        return false;
    }

    /**
     * Get the mod ID this compat module is for.
     */
    String getModId();
}

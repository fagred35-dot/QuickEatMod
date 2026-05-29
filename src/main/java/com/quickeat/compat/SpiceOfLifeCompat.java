package com.quickeat.compat;

import net.minecraft.world.item.ItemStack;

/**
 * Compatibility with Spice of Life: Carrot Edition.
 * 
 * Spice of Life tracks food diversity and may reduce food value
 * if the player eats the same food too often. Quick Eat respects this
 * by not preventing eating (the mod handles penalties internally via events).
 * 
 * The combo system in Quick Eat actually synergizes well with Spice of Life -
 * both reward eating different foods!
 */
public class SpiceOfLifeCompat implements IModCompat {

    @Override
    public boolean isConsumable(ItemStack stack) {
        // Spice of Life doesn't add new foods, it modifies existing ones
        return false;
    }

    @Override
    public boolean shouldPreventEating(ItemStack stack) {
        // Don't prevent eating - Spice of Life handles penalties via its own events
        // The food will still be eaten, but with reduced effectiveness if diversity is low
        return false;
    }

    @Override
    public String getModId() {
        return ModCompat.SPICE_OF_LIFE;
    }
}

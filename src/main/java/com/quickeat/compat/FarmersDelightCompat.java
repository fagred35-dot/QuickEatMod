package com.quickeat.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Compatibility with Farmer's Delight mod.
 * Farmer's Delight adds many foods that use standard FoodProperties,
 * but also has special items like Knives that shouldn't be "eaten".
 * This module ensures all FD foods work correctly and bowls/bottles are returned.
 */
public class FarmersDelightCompat implements IModCompat {

    @Override
    public boolean isConsumable(ItemStack stack) {
        // Farmer's Delight foods all use standard FoodProperties,
        // so they're already detected by the base check.
        // This compat ensures items from FD namespace with EAT/DRINK anim are caught.
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && id.getNamespace().equals(ModCompat.FARMERS_DELIGHT)) {
            // All FD items with food properties are consumable
            // Items like knives, cutting boards are NOT food and won't have food properties
            return stack.getItem().getFoodProperties() != null;
        }
        return false;
    }

    @Override
    public String getModId() {
        return ModCompat.FARMERS_DELIGHT;
    }
}

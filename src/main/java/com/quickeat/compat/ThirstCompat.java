package com.quickeat.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Compatibility with thirst mods (Tough As Nails, Legendary Survival Overhaul).
 * 
 * These mods add drinkable items that restore thirst.
 * They typically use UseAnim.DRINK, which is already caught by the base check.
 * This module provides extra detection for edge cases where items might use
 * custom use animations or NBT-based thirst properties.
 */
public class ThirstCompat implements IModCompat {

    private final String modId;

    public ThirstCompat(String modId) {
        this.modId = modId;
    }

    @Override
    public boolean isConsumable(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;

        // Check if item belongs to the thirst mod
        if (id.getNamespace().equals(modId)) {
            // Any item from a thirst mod with DRINK animation is consumable
            if (stack.getUseAnimation() == UseAnim.DRINK) return true;
            // Also check for food properties (some thirst mods add food+drink items)
            if (stack.getItem().getFoodProperties() != null) return true;
            // Check if item has a use duration (indicates it can be consumed)
            if (stack.getUseDuration() > 0) return true;
        }

        // Check for items tagged as drinks by thirst mods via NBT
        if (stack.hasTag() && stack.getTag() != null) {
            // Legendary Survival Overhaul uses "Thirst" NBT tag on some items
            if (stack.getTag().contains("ThirstValue") || stack.getTag().contains("Thirst")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getModId() {
        return modId;
    }
}

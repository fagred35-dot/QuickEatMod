package com.quickeat.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * Compatibility with Curios API.
 * 
 * Curios adds extra equipment slots. Some mods put consumable items
 * in curio slots (like food pouches, canteens, etc.).
 * 
 * Since Quick Eat works through container screens, and Curios slots
 * appear in the inventory screen, they're already supported through
 * the container slot system. This module just ensures items from
 * curio-related containers are properly detected as consumable.
 */
public class CuriosCompat implements IModCompat {

    @Override
    public boolean isConsumable(ItemStack stack) {
        // Curios items that are consumable will have standard food properties
        // or EAT/DRINK animation. The base check handles this.
        // This is here for future expansion if Curios adds custom consume mechanics.
        UseAnim anim = stack.getUseAnimation();
        if (anim == UseAnim.EAT || anim == UseAnim.DRINK) {
            return true;
        }
        return false;
    }

    @Override
    public String getModId() {
        return ModCompat.CURIOS;
    }
}

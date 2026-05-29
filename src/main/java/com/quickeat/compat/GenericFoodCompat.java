package com.quickeat.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generic food compatibility for mods that use standard Minecraft food system.
 * Works with: Pam's HarvestCraft, Create, Botania, Tinkers' Construct, etc.
 * 
 * These mods register their foods using standard FoodProperties and UseAnim,
 * so they're already supported by the base isConsumable check.
 * This module provides an extra safety net for edge cases.
 */
public class GenericFoodCompat implements IModCompat {

    private final String modId;

    public GenericFoodCompat(String modId) {
        this.modId = modId;
    }

    @Override
    public boolean isConsumable(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && id.getNamespace().equals(modId)) {
            // If it has food properties or eat/drink animation, it's consumable
            if (stack.getItem().getFoodProperties() != null) return true;
            UseAnim anim = stack.getUseAnimation();
            return anim == UseAnim.EAT || anim == UseAnim.DRINK;
        }
        return false;
    }

    @Override
    public String getModId() {
        return modId;
    }
}

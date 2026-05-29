package com.quickeat.client;

import com.quickeat.QuickEatConfig;
import com.quickeat.network.QuickEatPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FoodEvaluator {

    public static int findBestFoodSlot(LocalPlayer player, boolean requirePanic, boolean isAutoEat) {
        int bestSlot = -1;
        int bestScore = -1;

        int currentFood = player.getFoodData().getFoodLevel();
        int hungerNeeded = 20 - currentFood;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (!QuickEatPacket.isConsumable(stack)) continue;
            if (isBlacklisted(stack)) continue;

            FoodProperties food = stack.getFoodProperties(player);
            if (food == null) food = stack.getItem().getFoodProperties();
            
            // Allow drinks/potions if they are consumable but don't have food properties
            int nutrition = food != null ? food.getNutrition() : 0;
            
            // If it's an automated auto-eat to restore hunger, ignore 0-nutrition items (like potions) 
            // to prevent endless eating loops!
            if (isAutoEat && nutrition == 0) continue;
            
            // Exclude poison/harmful food unless it's a panic and it's our only choice
            if (isHarmful(food) && !requirePanic) continue;

            int score = 0;

            if (requirePanic) {
                // In panic mode, prioritize high saturation/nutrition or golden apples
                if (stack.getItem().getDescriptionId().contains("golden_apple")) {
                    score = 10000;
                } else {
                    score = nutrition * 10;
                }
            } else {
                // Optimal mode: find something that matches hungerNeeded exactly without wasting
                if (nutrition <= hungerNeeded) {
                    score = 100 + nutrition; // Prefer larger foods that still fit
                } else {
                    score = 20 - (nutrition - hungerNeeded); // Penalty for wasting
                }
            }

            // Prefer main hand or offhand if scores are similar
            if (i == player.getInventory().selected) score += 5;
            
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private static boolean isBlacklisted(ItemStack stack) {
        String id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        List<? extends String> blacklist = QuickEatConfig.BLACKLIST.get();
        for (String b : blacklist) {
            if (b.trim().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    private static boolean isHarmful(FoodProperties food) {
        if (food == null) return false;
        for (com.mojang.datafixers.util.Pair<MobEffectInstance, Float> pair : food.getEffects()) {
            MobEffectInstance effect = pair.getFirst();
            if (effect.getEffect() == MobEffects.POISON || 
                effect.getEffect() == MobEffects.HUNGER || 
                effect.getEffect() == MobEffects.HARM || 
                effect.getEffect() == MobEffects.WITHER) {
                return true;
            }
        }
        return false;
    }
}

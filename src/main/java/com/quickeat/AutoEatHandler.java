package com.quickeat;

import com.quickeat.compat.ModCompat;
import com.quickeat.network.EatNotificationPacket;
import com.quickeat.network.QuickEatNetwork;
import com.quickeat.network.QuickEatPacket;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Auto-eat handler. When enabled in config, automatically eats the best food
 * from the player's inventory when hunger drops below threshold.
 * Smartly picks food that won't over-heal (prefers smaller foods when only a little hungry).
 * Works with ALL modded food through the same isConsumable() system.
 */
@Mod.EventBusSubscriber(modid = QuickEatMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutoEatHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!QuickEatConfig.COMMON_SPEC.isLoaded() || !QuickEatConfig.AUTO_EAT_ENABLED.get()) return;

        // Check every 20 ticks (1 second)
        if (player.tickCount % 20 != 0) return;

        int currentFood = player.getFoodData().getFoodLevel();
        int threshold = QuickEatConfig.AUTO_EAT_THRESHOLD.get();

        // Only eat if hunger is at or below threshold and not full
        if (currentFood > threshold || currentFood >= 20) return;

        int hungerNeeded = 20 - currentFood;

        // === Smart food selection ===
        // First pass: find food that best fills without wasting
        int bestSlot = -1;
        int bestNutrition = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !QuickEatPacket.isConsumable(stack)) continue;
            if (ModCompat.shouldPreventEating(stack)) continue;

            FoodProperties food = stack.getFoodProperties(player);
            if (food == null) food = stack.getItem().getFoodProperties();
            if (food == null) continue;

            int nutrition = food.getNutrition();
            // Prefer food that fills exactly what's needed
            if (nutrition <= hungerNeeded && nutrition > bestNutrition) {
                bestNutrition = nutrition;
                bestSlot = i;
            }
        }

        // Second pass: if no perfect fit, use smallest available food
        if (bestSlot == -1) {
            int smallest = Integer.MAX_VALUE;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty() || !QuickEatPacket.isConsumable(stack)) continue;
                if (ModCompat.shouldPreventEating(stack)) continue;

                FoodProperties food = stack.getFoodProperties(player);
                if (food == null) food = stack.getItem().getFoodProperties();
                if (food == null) continue;

                if (food.getNutrition() < smallest) {
                    smallest = food.getNutrition();
                    bestSlot = i;
                }
            }
        }

        if (bestSlot == -1) return; // No food found

        // === Eat the food ===
        ItemStack foodStack = player.getInventory().getItem(bestSlot);
        Item foodItem = foodStack.getItem();
        String foodName = foodStack.getHoverName().getString();

        FoodProperties props = foodStack.getFoodProperties(player);
        if (props == null) props = foodStack.getItem().getFoodProperties();
        int nutrition = props != null ? props.getNutrition() : 0;

        ItemStack originalStack = foodStack.copy();

        // Apply eating
        ItemStack result = foodStack.finishUsingItem(player.level(), player);
        player.getInventory().setItem(bestSlot, result);

        // Sound
        UseAnim anim = originalStack.getUseAnimation();
        if (anim == UseAnim.DRINK) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F,
                    player.level().random.nextFloat() * 0.1F + 0.9F);
        }

        // Particles
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, originalStack),
                    player.getX(), player.getEyeY() - 0.2, player.getZ(),
                    8, 0.2, 0.1, 0.2, 0.05
            );
        }

        // Send notification to client
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(foodItem);
        if (itemId != null) {
            QuickEatNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EatNotificationPacket(itemId.toString(), foodName, nutrition)
            );
        }

        // Combo
        ComboSystem.onFoodEaten(player, foodItem);
    }
}

package com.quickeat;

import com.quickeat.network.QuickEatNetwork;
import com.quickeat.network.QuickEatPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side event handler for Quick Eat.
 * Supports instant eat on key press AND continuous eating while holding the key.
 * No cooldown — hold G on a stack of bread and eat it all in seconds.
 */
@Mod.EventBusSubscriber(modid = QuickEatMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    private static boolean keyHeld = false;
    private static int holdTicks = 0;
    private static final int EAT_INTERVAL = 3; // ticks between eats while holding (150ms)

    /**
     * Instant eat on first key press — immediate feedback.
     */
    @SubscribeEvent
    public static void onKeyPressedInScreen(ScreenEvent.KeyPressed.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) return;
        if (event.getKeyCode() != KeyBindings.QUICK_EAT_KEY.getKey().getValue()) return;

        // Skip if already holding — the tick handler takes over
        if (keyHeld) {
            event.setCanceled(true);
            return;
        }

        keyHeld = true;
        holdTicks = 0;

        tryEat(mc, containerScreen);
        event.setCanceled(true);
    }

    /**
     * Continuous eating while holding the key — tick-based for consistent speed.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!keyHeld) return;

        Minecraft mc = Minecraft.getInstance();

        // Reset if inventory closed
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            keyHeld = false;
            holdTicks = 0;
            return;
        }

        // Check if key is actually still held (direct GLFW state)
        int keyValue = KeyBindings.QUICK_EAT_KEY.getKey().getValue();
        if (!InputConstants.isKeyDown(mc.getWindow().getWindow(), keyValue)) {
            keyHeld = false;
            holdTicks = 0;
            return;
        }

        holdTicks++;
        if (holdTicks >= EAT_INTERVAL) {
            holdTicks = 0;
            tryEat(mc, containerScreen);
        }
    }

    /**
     * Attempt to eat the food under the cursor.
     */
    private static void tryEat(Minecraft mc, AbstractContainerScreen<?> containerScreen) {
        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        if (!QuickEatPacket.isConsumable(stack)) return;

        // Prevent eating when full (client-side hint, server validates too)
        if (QuickEatConfig.COMMON_SPEC.isLoaded() && QuickEatConfig.PREVENT_WHEN_FULL.get()) {
            if (mc.player != null && mc.player.getFoodData().getFoodLevel() >= 20) return;
        }

        boolean isPlayerInventory = (hoveredSlot.container == mc.player.getInventory());
        if (!isPlayerInventory && QuickEatConfig.CLIENT_SPEC.isLoaded()
                && !QuickEatConfig.ALLOW_FROM_CONTAINERS.get()) {
            return;
        }

        // Send eat packet to server
        int containerSlotId = hoveredSlot.index;
        QuickEatNetwork.CHANNEL.sendToServer(new QuickEatPacket(containerSlotId, !isPlayerInventory));

        // Immediate client-side sound feedback
        if (QuickEatConfig.CLIENT_SPEC.isLoaded() && QuickEatConfig.PLAY_SOUND.get()) {
            mc.player.playSound(SoundEvents.GENERIC_EAT, 0.5F,
                    mc.level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    /**
     * Show tooltip hint on food items.
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!QuickEatConfig.CLIENT_SPEC.isLoaded() || !QuickEatConfig.SHOW_TOOLTIP.get()) return;

        ItemStack stack = event.getItemStack();
        if (QuickEatPacket.isConsumable(stack)) {
            String keyName = KeyBindings.QUICK_EAT_KEY.getTranslatedKeyMessage().getString();
            event.getToolTip().add(
                    Component.translatable("tooltip.quickeat.hint", keyName)
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
        }
    }
}

package com.quickeat.client;

import com.quickeat.QuickEatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MacroEatHandler {

    public enum State {
        IDLE, PREPARING, EATING, RESTORING
    }

    private static State currentState = State.IDLE;
    private static int originalSelectedSlot = -1;
    private static int swappedInventorySlot = -1; // -1 if food was already in hotbar
    
    private static int eatTicks = 0;
    private static boolean manualTrigger = false;
    private static boolean hasStartedEating = false;

    public static void startEating(boolean isManual) {
        if (currentState != State.IDLE) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        
        // Prevent eating when full unless manual
        if (!isManual && mc.player.getFoodData().getFoodLevel() >= QuickEatConfig.AUTO_EAT_THRESHOLD.get()) return;
        if (mc.player.getFoodData().getFoodLevel() >= 20 && !isManual) return; // Full

        manualTrigger = isManual;
        originalSelectedSlot = mc.player.getInventory().selected;
        swappedInventorySlot = -1;
        currentState = State.PREPARING;
    }
    
    public static void stopEating() {
        if (currentState == State.IDLE) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.options.keyUse.setDown(false); // Stop eating
        }
        currentState = State.RESTORING;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null) return;

        // Abort if player is hurt (Panic stop)
        if (currentState != State.IDLE && player.hurtTime > 0) {
            stopEating();
        }

        // Abort if user manually presses attack/use (but we are simulating use, so we check attack)
        if (currentState != State.IDLE && mc.options.keyAttack.isDown()) {
            stopEating();
        }

        switch (currentState) {
            case IDLE:
                // Auto-Eat trigger logic
                if (QuickEatConfig.AUTO_EAT_ENABLED.get() && mc.level != null && player.tickCount % 20 == 0) {
                    if (player.getFoodData().getFoodLevel() <= QuickEatConfig.AUTO_EAT_THRESHOLD.get()) {
                        startEating(false);
                    }
                }
                break;

            case PREPARING:
                boolean isPanic = player.getHealth() <= 6.0f; // 3 hearts
                int bestSlot = FoodEvaluator.findBestFoodSlot(player, isPanic, !manualTrigger);
                
                if (bestSlot == -1) {
                    currentState = State.IDLE; // No food found
                    return;
                }

                if (bestSlot < 9) { // It's in the hotbar
                    player.getInventory().selected = bestSlot;
                } else if (bestSlot == 40) { // Offhand
                    // Offhand is fine, we just use it directly, no swap needed
                } else {
                    // It's in main inventory, swap it to current selected hotbar slot
                    swappedInventorySlot = bestSlot;
                    // Slot IDs for main inventory in InventoryMenu are 9 to 35
                    mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, 
                        bestSlot, originalSelectedSlot, ClickType.SWAP, player);
                }

                currentState = State.EATING;
                eatTicks = 0;
                hasStartedEating = false;
                break;

            case EATING:
                eatTicks++;
                mc.options.keyUse.setDown(true); // Simulate holding right click
                
                ItemStack usingItem = player.getUseItem();
                boolean isUsing = !usingItem.isEmpty();
                
                if (isUsing) {
                    hasStartedEating = true;
                }
                
                if (eatTicks > 50 || (hasStartedEating && !isUsing)) {
                    // Finished eating or timed out
                    mc.options.keyUse.setDown(false);
                    hasStartedEating = false;
                    
                    if (manualTrigger && player.getFoodData().getFoodLevel() < 20) {
                        // Keep eating if manual trigger and not full
                        currentState = State.PREPARING;
                    } else {
                        currentState = State.RESTORING;
                    }
                }
                break;

            case RESTORING:
                mc.options.keyUse.setDown(false);
                
                if (swappedInventorySlot != -1) {
                    // Swap back
                    mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, 
                        swappedInventorySlot, originalSelectedSlot, ClickType.SWAP, player);
                    swappedInventorySlot = -1;
                }
                
                if (originalSelectedSlot != -1) {
                    player.getInventory().selected = originalSelectedSlot;
                }
                
                originalSelectedSlot = -1;
                currentState = State.IDLE;
                manualTrigger = false;
                break;
        }
    }
}

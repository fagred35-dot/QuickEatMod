package com.quickeat.network;

import com.quickeat.ComboSystem;
import com.quickeat.QuickEatConfig;
import com.quickeat.compat.ModCompat;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class QuickEatPacket {
    private final int containerSlotId;
    private final boolean fromContainer;

    public QuickEatPacket(int containerSlotId, boolean fromContainer) {
        this.containerSlotId = containerSlotId;
        this.fromContainer = fromContainer;
    }

    public static void encode(QuickEatPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.containerSlotId);
        buf.writeBoolean(msg.fromContainer);
    }

    public static QuickEatPacket decode(FriendlyByteBuf buf) {
        return new QuickEatPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(QuickEatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // Prevent eating when full
            if (QuickEatConfig.COMMON_SPEC.isLoaded() && QuickEatConfig.PREVENT_WHEN_FULL.get()) {
                if (player.getFoodData().getFoodLevel() >= 20) return;
            }

            AbstractContainerMenu menu = player.containerMenu;

            // Validate slot index
            if (msg.containerSlotId < 0 || msg.containerSlotId >= menu.slots.size()) {
                return;
            }

            Slot slot = menu.getSlot(msg.containerSlotId);
            ItemStack stack = slot.getItem();

            if (stack.isEmpty()) return;

            // Check if the item is edible or drinkable
            if (!isConsumable(stack)) return;

            // Check if a compat mod prevents eating
            if (ModCompat.shouldPreventEating(stack)) return;

            // Save info before eating (finishUsingItem may change the stack)
            Item foodItem = stack.getItem();
            String foodName = stack.getHoverName().getString();
            FoodProperties foodProps = stack.getFoodProperties(player);
            if (foodProps == null) foodProps = stack.getItem().getFoodProperties();
            int nutrition = foodProps != null ? foodProps.getNutrition() : 0;
            ItemStack originalStack = stack.copy();

            // Simulate eating: apply food effects server-side
            ItemStack resultStack = stack.finishUsingItem(player.level(), player);

            // Set the result back into the slot
            slot.set(resultStack);
            slot.setChanged();

            // Play eating/drinking sound
            UseAnim anim = originalStack.getUseAnimation();
            if (anim == UseAnim.DRINK) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F,
                        player.level().random.nextFloat() * 0.1F + 0.9F);
            }

            // === NEW: Food particles ===
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, originalStack),
                        player.getX(), player.getEyeY() - 0.2, player.getZ(),
                        8, 0.2, 0.1, 0.2, 0.05
                );
            }

            // === NEW: Send eat notification to client ===
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(foodItem);
            if (itemId != null) {
                QuickEatNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new EatNotificationPacket(itemId.toString(), foodName, nutrition)
                );
            }

            // Process combo system
            ComboSystem.onFoodEaten(player, foodItem);

            // Update the container
            menu.broadcastChanges();
        });
        context.setPacketHandled(true);
    }

    /**
     * Checks if an item is consumable (food or drink).
     * Works with vanilla items, modded food, and thirst/drink items.
     * Also checks mod compatibility modules.
     */
    public static boolean isConsumable(ItemStack stack) {
        // Check if item has food properties (covers all vanilla and most modded food)
        if (stack.getItem().getFoodProperties() != null) {
            return true;
        }

        // Check if the item has food properties on the stack level (some mods use this)
        FoodProperties foodProps = stack.getFoodProperties(null);
        if (foodProps != null) {
            return true;
        }

        // Check use animation - EAT or DRINK covers thirst items,
        // potions, modded drinks (like from Legendary Survival Overhaul)
        UseAnim useAnim = stack.getUseAnimation();
        if (useAnim == UseAnim.EAT || useAnim == UseAnim.DRINK) {
            return true;
        }

        // Check mod compatibility modules for additional consumable detection
        if (ModCompat.isConsumableCompat(stack)) {
            return true;
        }

        return false;
    }
}

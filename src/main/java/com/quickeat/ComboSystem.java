package com.quickeat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;

/**
 * Combo food system - rewards players for eating the SAME food in a row.
 * Eating a different food resets the combo. Eating the same food builds it up.
 * Higher combos give temporary buffs.
 */
public class ComboSystem {

    private static final Map<UUID, ComboData> playerCombos = new HashMap<>();

    public static class ComboData {
        public Item lastFood = null;
        public int comboCount = 0;
        public long lastEatTick = 0;

        public void reset() {
            lastFood = null;
            comboCount = 0;
        }
    }

    /**
     * Called when a player eats food. Returns the new combo count.
     */
    public static int onFoodEaten(ServerPlayer player, Item foodItem) {
        if (!QuickEatConfig.COMBO_ENABLED.get()) return 0;

        UUID playerId = player.getUUID();
        ComboData data = playerCombos.computeIfAbsent(playerId, k -> new ComboData());

        long currentTick = player.level().getGameTime();
        int timeoutTicks = QuickEatConfig.COMBO_TIMEOUT_SECONDS.get() * 20;

        // Check timeout
        if (currentTick - data.lastEatTick > timeoutTicks) {
            data.reset();
        }

        data.lastEatTick = currentTick;

        // Check if same food as last time
        if (data.lastFood == foodItem) {
            // Same food — increase combo!
            data.comboCount++;
            applyComboReward(player, data.comboCount);
            return data.comboCount;
        } else {
            // Different food — reset combo
            data.lastFood = foodItem;
            data.comboCount = 1;
            return 1;
        }
    }

    /**
     * Apply buff rewards based on combo level.
     */
    private static void applyComboReward(ServerPlayer player, int combo) {
        int minCombo = QuickEatConfig.COMBO_MIN_FOR_REWARD.get();
        if (combo < minCombo) return;

        int duration = combo * 100; // 5 sec per combo level
        int amplifier = Math.min((combo - minCombo) / 2, 3);

        if (combo < 5) {
            // Small combo: Regeneration I
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
            sendComboMessage(player, combo, ChatFormatting.GREEN);
        } else if (combo < 8) {
            // Medium combo: Regeneration + Speed
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
            sendComboMessage(player, combo, ChatFormatting.YELLOW);
        } else if (combo < 12) {
            // High combo: Regen II + Speed + Strength
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0));
            sendComboMessage(player, combo, ChatFormatting.GOLD);
        } else {
            // Legendary combo (12+): All buffs + Absorption
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, amplifier));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0));
            sendComboMessage(player, combo, ChatFormatting.LIGHT_PURPLE);
        }
    }

    private static void sendComboMessage(ServerPlayer player, int combo, ChatFormatting color) {
        if (!QuickEatConfig.COMBO_SHOW_MESSAGE.get()) return;
        player.displayClientMessage(
                Component.translatable("quickeat.combo", combo).withStyle(color, ChatFormatting.BOLD),
                true
        );
    }

    public static int getCombo(UUID playerId) {
        ComboData data = playerCombos.get(playerId);
        return data != null ? data.comboCount : 0;
    }

    public static void removePlayer(UUID playerId) {
        playerCombos.remove(playerId);
    }
}

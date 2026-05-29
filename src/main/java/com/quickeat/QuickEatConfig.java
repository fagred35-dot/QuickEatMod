package com.quickeat;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class QuickEatConfig {

    // === Notification position enum ===
    public enum NotificationPosition {
        BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT
    }

    // Client config
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue SHOW_TOOLTIP;
    public static final ForgeConfigSpec.BooleanValue PLAY_SOUND;
    public static final ForgeConfigSpec.BooleanValue ALLOW_FROM_CONTAINERS;
    public static final ForgeConfigSpec.BooleanValue SHOW_NOTIFICATIONS;
    public static final ForgeConfigSpec.EnumValue<NotificationPosition> NOTIFICATION_POSITION;
    public static final ForgeConfigSpec.BooleanValue SHOW_PARTICLES;

    // Server/Common config
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.BooleanValue PREVENT_WHEN_FULL;

    public static final ForgeConfigSpec.BooleanValue AUTO_EAT_ENABLED;
    public static final ForgeConfigSpec.IntValue AUTO_EAT_THRESHOLD;

    public static final ForgeConfigSpec.BooleanValue COMBO_ENABLED;
    public static final ForgeConfigSpec.IntValue COMBO_TIMEOUT_SECONDS;
    public static final ForgeConfigSpec.IntValue COMBO_MIN_FOR_REWARD;
    public static final ForgeConfigSpec.BooleanValue COMBO_SHOW_MESSAGE;

    // Blacklist
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> BLACKLIST;

    static {
        // === CLIENT CONFIG ===
        CLIENT_BUILDER.comment("Quick Eat - Client Settings").push("client");

        SHOW_TOOLTIP = CLIENT_BUILDER
                .comment("Show tooltip hint on food items (Press [key] to eat)")
                .define("showTooltip", true);

        PLAY_SOUND = CLIENT_BUILDER
                .comment("Play eating sound when quick-eating")
                .define("playSound", true);

        ALLOW_FROM_CONTAINERS = CLIENT_BUILDER
                .comment("Allow eating from containers (chests, barrels, etc.)")
                .define("allowFromContainers", true);

        SHOW_NOTIFICATIONS = CLIENT_BUILDER
                .comment("Show toast notification when eating")
                .define("showNotifications", true);

        NOTIFICATION_POSITION = CLIENT_BUILDER
                .comment("Position of eat notifications: BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT")
                .defineEnum("notificationPosition", NotificationPosition.BOTTOM_RIGHT);

        SHOW_PARTICLES = CLIENT_BUILDER
                .comment("Show food particles when eating (visual only, controlled server-side)")
                .define("showParticles", true);

        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();

        // === COMMON CONFIG ===
        COMMON_BUILDER.comment("Quick Eat - Server/Common Settings").push("general");

        PREVENT_WHEN_FULL = COMMON_BUILDER
                .comment("Prevent eating when hunger is full (saves food)")
                .define("preventWhenFull", false);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Auto-Eat Settings").push("autoeat");

        AUTO_EAT_ENABLED = COMMON_BUILDER
                .comment("Enable auto-eat: automatically eat when hunger drops below threshold")
                .define("enabled", false);

        AUTO_EAT_THRESHOLD = COMMON_BUILDER
                .comment("Hunger level at or below which auto-eat triggers (20 = full, 14 = 7 drumsticks)")
                .defineInRange("threshold", 14, 1, 20);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Combo System Settings").push("combo");

        COMBO_ENABLED = COMMON_BUILDER
                .comment("Enable the combo food system (eating same food in a row gives buffs)")
                .define("enabled", true);

        COMBO_TIMEOUT_SECONDS = COMMON_BUILDER
                .comment("Seconds before combo resets if no food is eaten")
                .defineInRange("timeoutSeconds", 30, 5, 300);

        COMBO_MIN_FOR_REWARD = COMMON_BUILDER
                .comment("Minimum combo count to start receiving buff rewards")
                .defineInRange("minForReward", 3, 2, 20);

        COMBO_SHOW_MESSAGE = COMMON_BUILDER
                .comment("Show combo count message in action bar")
                .define("showMessage", true);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Blacklist Settings").push("blacklist");

        BLACKLIST = COMMON_BUILDER
                .comment("List of item IDs (e.g. minecraft:rotten_flesh, minecraft:golden_apple) that should NEVER be auto-eaten")
                .defineList("items", java.util.Arrays.asList("minecraft:rotten_flesh", "minecraft:spider_eye", "minecraft:pufferfish"), o -> o instanceof String);

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}

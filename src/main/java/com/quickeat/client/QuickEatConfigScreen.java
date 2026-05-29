package com.quickeat.client;

import com.quickeat.QuickEatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Built-in config screen using vanilla Minecraft GUI widgets.
 * No external dependencies required — works out of the box.
 * Tabbed interface: General | Auto-Eat | Combo
 */
public class QuickEatConfigScreen extends Screen {

    private final Screen parent;
    private int tab = 0;

    private static final int ROW_H = 24;
    private static final int OPT_W = 310;
    private static final String[] TAB_KEYS = {
            "quickeat.config.category.general",
            "quickeat.config.category.autoeat",
            "quickeat.config.category.combo",
            "quickeat.config.category.blacklist"
    };

    /**
     * Registers the config screen with Forge's mod list config button.
     */
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> new QuickEatConfigScreen(parent)
                )
        );
    }

    public QuickEatConfigScreen(Screen parent) {
        super(Component.translatable("quickeat.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int lx = cx - OPT_W / 2;
        int tabW = 90;
        int tabY = 32;

        // === Tab buttons ===
        for (int i = 0; i < 4; i++) {
            final int tabIdx = i;
            addRenderableWidget(Button.builder(tabLabel(i), b -> switchTab(tabIdx))
                    .bounds(cx - tabW * 3 / 2 - 2 + i * (tabW + 2), tabY, tabW, 20)
                    .build());
        }

        // === Options for current tab ===
        int y = 60;

        switch (tab) {
            case 0 -> { // General
                addToggle(lx, y, "quickeat.config.showTooltip", QuickEatConfig.SHOW_TOOLTIP);
                y += ROW_H;
                addToggle(lx, y, "quickeat.config.playSound", QuickEatConfig.PLAY_SOUND);
                y += ROW_H;
                addToggle(lx, y, "quickeat.config.allowFromContainers", QuickEatConfig.ALLOW_FROM_CONTAINERS);
                y += ROW_H;
                addToggle(lx, y, "quickeat.config.showNotifications", QuickEatConfig.SHOW_NOTIFICATIONS);
                y += ROW_H;

                // Notification position enum selector
                addRenderableWidget(CycleButton.<QuickEatConfig.NotificationPosition>builder(
                                p -> Component.literal(p.name()))
                        .withValues(QuickEatConfig.NotificationPosition.values())
                        .withInitialValue(QuickEatConfig.NOTIFICATION_POSITION.get())
                        .create(lx, y, OPT_W, 20,
                                Component.translatable("quickeat.config.notificationPosition"),
                                (btn, val) -> QuickEatConfig.NOTIFICATION_POSITION.set(val)));
                y += ROW_H;

                addToggle(lx, y, "quickeat.config.showParticles", QuickEatConfig.SHOW_PARTICLES);
                y += ROW_H;
                addToggle(lx, y, "quickeat.config.preventWhenFull", QuickEatConfig.PREVENT_WHEN_FULL);
            }
            case 1 -> { // Auto-Eat
                addToggle(lx, y, "quickeat.config.autoEatEnabled", QuickEatConfig.AUTO_EAT_ENABLED);
                y += ROW_H;
                addSlider(lx, y, "quickeat.config.autoEatThreshold", 1, 20, QuickEatConfig.AUTO_EAT_THRESHOLD);
            }
            case 2 -> { // Combo
                addToggle(lx, y, "quickeat.config.comboEnabled", QuickEatConfig.COMBO_ENABLED);
                y += ROW_H;
                addSlider(lx, y, "quickeat.config.comboTimeout", 5, 300, QuickEatConfig.COMBO_TIMEOUT_SECONDS);
                y += ROW_H;
                addSlider(lx, y, "quickeat.config.comboMinReward", 2, 20, QuickEatConfig.COMBO_MIN_FOR_REWARD);
                y += ROW_H;
                addToggle(lx, y, "quickeat.config.comboShowMessage", QuickEatConfig.COMBO_SHOW_MESSAGE);
            }
            case 3 -> { // Blacklist
                // We display it as a text field or simple button
                addRenderableWidget(Button.builder(Component.translatable("quickeat.config.edit_blacklist"), b -> {
                    // For now we just use a generic warning or link, as complex lists need a whole subscreen.
                    // But we can simply open a text input sub-screen or use Forge's default config GUI for it
                    // Actually, let's just make it a big text field!
                    this.minecraft.setScreen(new BlacklistEditScreen(this));
                }).bounds(lx, y, OPT_W, 20).build());
            }
        }

        // === Done button ===
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(cx - 100, this.height - 28, 200, 20).build());
    }

    private Component tabLabel(int idx) {
        Component text = Component.translatable(TAB_KEYS[idx]);
        if (tab == idx) {
            return text.copy().withStyle(ChatFormatting.UNDERLINE, ChatFormatting.WHITE);
        }
        return text;
    }

    private void switchTab(int t) {
        tab = t;
        rebuildWidgets();
    }

    // --- Widget helpers ---

    private void addToggle(int x, int y, String key, ForgeConfigSpec.BooleanValue config) {
        addRenderableWidget(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                        Component.translatable("options.off").withStyle(ChatFormatting.RED))
                .withInitialValue(config.get())
                .create(x, y, OPT_W, 20,
                        Component.translatable(key),
                        (btn, val) -> config.set(val)));
    }

    private void addSlider(int x, int y, String key, int min, int max, ForgeConfigSpec.IntValue config) {
        Component label = Component.translatable(key);
        int current = config.get();
        double initial = (current - min) / (double) (max - min);

        addRenderableWidget(new AbstractSliderButton(x, y, OPT_W, 20,
                label.copy().append(": " + current), initial) {
            @Override
            protected void updateMessage() {
                int val = min + (int) Math.round(this.value * (max - min));
                this.setMessage(label.copy().append(": " + val));
            }

            @Override
            protected void applyValue() {
                int val = min + (int) Math.round(this.value * (max - min));
                config.set(val);
            }
        });
    }

    // --- Rendering ---

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx);
        gfx.drawCenteredString(this.font, this.title, this.width / 2, 13, 0xFFFFFF);
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}

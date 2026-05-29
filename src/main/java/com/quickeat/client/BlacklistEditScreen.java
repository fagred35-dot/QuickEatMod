package com.quickeat.client;

import com.quickeat.QuickEatConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlacklistEditScreen extends Screen {

    private final Screen parent;
    private EditBox editBox;

    public BlacklistEditScreen(Screen parent) {
        super(Component.translatable("quickeat.config.category.blacklist"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 50;

        List<? extends String> currentList = QuickEatConfig.BLACKLIST.get();
        String currentText = String.join(", ", currentList);

        this.editBox = new EditBox(this.font, cx - 150, y, 300, 20, Component.literal("Blacklist"));
        this.editBox.setMaxLength(1024);
        this.editBox.setValue(currentText);
        this.addRenderableWidget(this.editBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> {
            saveAndClose();
        }).bounds(cx - 100, this.height - 28, 200, 20).build());
    }

    private void saveAndClose() {
        String text = this.editBox.getValue();
        List<String> newList = new ArrayList<>();
        if (!text.trim().isEmpty()) {
            String[] split = text.split(",");
            for (String s : split) {
                if (!s.trim().isEmpty()) {
                    newList.add(s.trim());
                }
            }
        }
        QuickEatConfig.BLACKLIST.set(newList);
        QuickEatConfig.BLACKLIST.save();
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx);
        gfx.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        gfx.drawCenteredString(this.font, Component.literal("Enter item IDs separated by commas:"), this.width / 2, 35, 0xA0A0A0);
        super.render(gfx, mouseX, mouseY, partialTick);
    }
}

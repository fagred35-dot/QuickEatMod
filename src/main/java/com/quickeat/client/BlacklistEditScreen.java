package com.quickeat.client;

import net.minecraft.ChatFormatting;

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
        int y = 70;

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
        
        int cx = this.width / 2;
        int boxW = 350;
        int boxH = 100;
        int lx = cx - boxW / 2;
        int ty = 30;

        // Premium Dark Panel with Orange/Gold Gradient Borders
        gfx.fillGradient(lx, ty, lx + boxW, ty + boxH, 0xEE050505, 0xDD111111);
        gfx.fillGradient(lx - 1, ty - 1, lx + boxW + 1, ty, 0xFFFFAA00, 0xFFFF5500);
        gfx.fillGradient(lx - 1, ty + boxH, lx + boxW + 1, ty + boxH + 1, 0xFFFF5500, 0xFFFFAA00);
        gfx.fillGradient(lx - 1, ty, lx, ty + boxH, 0xFFFFAA00, 0xFFFF5500);
        gfx.fillGradient(lx + boxW, ty, lx + boxW + 1, ty + boxH, 0xFFFF5500, 0xFFFFAA00);

        gfx.drawCenteredString(this.font, Component.literal("✦ ").withStyle(ChatFormatting.GOLD)
            .append(this.title.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .append(Component.literal(" ✦").withStyle(ChatFormatting.GOLD)), cx, 40, 0xFFFFFF);
            
        gfx.drawCenteredString(this.font, Component.literal("Enter item IDs separated by commas").withStyle(ChatFormatting.GRAY), cx, 55, 0xA0A0A0);
        super.render(gfx, mouseX, mouseY, partialTick);
    }
}

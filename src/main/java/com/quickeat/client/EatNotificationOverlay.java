package com.quickeat.client;

import com.quickeat.QuickEatConfig;
import com.quickeat.QuickEatMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom HUD toast overlay for eat notifications.
 * Shows a slide-in notification with food icon, name, and nutrition in a configurable corner.
 */
@Mod.EventBusSubscriber(modid = QuickEatMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EatNotificationOverlay implements IGuiOverlay {

    public static final EatNotificationOverlay INSTANCE = new EatNotificationOverlay();
    private static final List<Notification> notifications = new ArrayList<>();

    private static final int MAX_VISIBLE = 4;
    private static final long SHOW_MS = 2500;
    private static final long FADE_IN_MS = 200;
    private static final long FADE_OUT_MS = 500;
    private static final int BOX_HEIGHT = 26;
    private static final int GAP = 3;
    private static final int MARGIN = 6;
    private static final int ACCENT_W = 3;
    private static final int PAD = 6;
    private static final int ICON_SIZE = 16;

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("eat_notifications", INSTANCE);
    }

    public static void addNotification(ItemStack stack, String name, int nutrition) {
        if (!QuickEatConfig.CLIENT_SPEC.isLoaded() || !QuickEatConfig.SHOW_NOTIFICATIONS.get()) return;
        notifications.add(0, new Notification(stack.copy(), name, nutrition, System.currentTimeMillis()));
        while (notifications.size() > MAX_VISIBLE) {
            notifications.remove(notifications.size() - 1);
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics gfx, float partialTick, int screenWidth, int screenHeight) {
        if (notifications.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;

        QuickEatConfig.NotificationPosition pos = QuickEatConfig.NotificationPosition.BOTTOM_RIGHT;
        if (QuickEatConfig.CLIENT_SPEC.isLoaded()) {
            pos = QuickEatConfig.NOTIFICATION_POSITION.get();
        }

        boolean isRight = pos.name().contains("RIGHT");
        boolean isBottom = pos.name().contains("BOTTOM");

        long now = System.currentTimeMillis();
        int idx = 0;
        Iterator<Notification> it = notifications.iterator();

        while (it.hasNext()) {
            Notification n = it.next();
            long age = now - n.createTime;

            if (age > SHOW_MS + FADE_OUT_MS) {
                it.remove();
                continue;
            }

            float alpha = calcAlpha(age);
            float slide = calcSlide(age);

            // Build display text
            String label = n.foodName;
            String bonus = n.nutrition > 0 ? " +" + n.nutrition : "";
            int textW = font.width(label + bonus);
            int boxW = ACCENT_W + PAD + ICON_SIZE + 4 + textW + PAD;

            // Position with slide animation
            int x;
            if (isRight) {
                x = screenWidth - boxW - MARGIN + (int) ((1f - slide) * (boxW + MARGIN));
            } else {
                x = MARGIN - (int) ((1f - slide) * (boxW + MARGIN));
            }

            int y;
            if (isBottom) {
                y = screenHeight - MARGIN - BOX_HEIGHT - idx * (BOX_HEIGHT + GAP);
            } else {
                y = MARGIN + idx * (BOX_HEIGHT + GAP);
            }

            int a = (int) (alpha * 210);
            int textAlpha = (int) (alpha * 255);

            // Dark semi-transparent background
            gfx.fill(x, y, x + boxW, y + BOX_HEIGHT, (a << 24) | 0x141423);
            // Green accent bar on the left
            gfx.fill(x, y, x + ACCENT_W, y + BOX_HEIGHT, (a << 24) | 0x4ADE80);

            // Food item icon
            if (!n.foodStack.isEmpty() && alpha > 0.3f) {
                gfx.renderItem(n.foodStack, x + ACCENT_W + PAD, y + (BOX_HEIGHT - ICON_SIZE) / 2);
            }

            // Food name text
            int tx = x + ACCENT_W + PAD + ICON_SIZE + 4;
            int ty = y + (BOX_HEIGHT - font.lineHeight) / 2;
            gfx.drawString(font, label, tx, ty, (textAlpha << 24) | 0xE8E8E8);

            // Nutrition bonus text (green)
            if (n.nutrition > 0) {
                gfx.drawString(font, bonus, tx + font.width(label), ty, (textAlpha << 24) | 0x55FF55);
            }

            idx++;
        }
    }

    private float calcAlpha(long age) {
        if (age < FADE_IN_MS) {
            float t = (float) age / FADE_IN_MS;
            return 1f - (1f - t) * (1f - t); // ease-out
        }
        if (age > SHOW_MS) {
            return Math.max(0f, 1f - (float) (age - SHOW_MS) / FADE_OUT_MS);
        }
        return 1f;
    }

    private float calcSlide(long age) {
        if (age < FADE_IN_MS) {
            float t = (float) age / FADE_IN_MS;
            return 1f - (1f - t) * (1f - t); // ease-out
        }
        return 1f;
    }

    static class Notification {
        final ItemStack foodStack;
        final String foodName;
        final int nutrition;
        final long createTime;

        Notification(ItemStack foodStack, String foodName, int nutrition, long createTime) {
            this.foodStack = foodStack;
            this.foodName = foodName;
            this.nutrition = nutrition;
            this.createTime = createTime;
        }
    }
}

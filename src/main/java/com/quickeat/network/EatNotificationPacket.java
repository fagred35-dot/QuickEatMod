package com.quickeat.network;

import com.quickeat.client.EatNotificationOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Server → Client packet: tells the client to show an eat notification toast.
 */
public class EatNotificationPacket {
    private final String itemRegistryName;
    private final String foodName;
    private final int nutrition;

    public EatNotificationPacket(String itemRegistryName, String foodName, int nutrition) {
        this.itemRegistryName = itemRegistryName;
        this.foodName = foodName;
        this.nutrition = nutrition;
    }

    public static void encode(EatNotificationPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemRegistryName);
        buf.writeUtf(msg.foodName);
        buf.writeInt(msg.nutrition);
    }

    public static EatNotificationPacket decode(FriendlyByteBuf buf) {
        return new EatNotificationPacket(buf.readUtf(256), buf.readUtf(256), buf.readInt());
    }

    public static void handle(EatNotificationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(msg.itemRegistryName));
                ItemStack stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
                EatNotificationOverlay.addNotification(stack, msg.foodName, msg.nutrition);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

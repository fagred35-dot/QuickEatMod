package com.quickeat.network;

import com.quickeat.QuickEatMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;

public class QuickEatNetwork {
    private static final String PROTOCOL_VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(QuickEatMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        // Client → Server: eat request
        CHANNEL.messageBuilder(QuickEatPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QuickEatPacket::encode)
                .decoder(QuickEatPacket::decode)
                .consumerMainThread(QuickEatPacket::handle)
                .add();

        // Server → Client: eat notification (for HUD toast)
        CHANNEL.messageBuilder(EatNotificationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EatNotificationPacket::encode)
                .decoder(EatNotificationPacket::decode)
                .consumerMainThread(EatNotificationPacket::handle)
                .add();
    }
}

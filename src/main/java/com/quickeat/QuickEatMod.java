package com.quickeat;

import com.quickeat.compat.ModCompat;
import com.quickeat.network.QuickEatNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(QuickEatMod.MODID)
public class QuickEatMod {
    public static final String MODID = "quickeat";
    public static final Logger LOGGER = LogManager.getLogger();

    public QuickEatMod() {
        // Register config
        QuickEatConfig.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        // Register built-in config screen (client-only, no external deps)
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            com.quickeat.client.QuickEatConfigScreen.register();
        });

        LOGGER.info("Quick Eat mod loaded! Press G on food in inventory to eat instantly.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            QuickEatNetwork.register();
            ModCompat.init();
        });
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up combo data when player disconnects
        ComboSystem.removePlayer(event.getEntity().getUUID());
    }
}

package com.jeict;

import com.jeict.bridge.CelesTechBridge;
import com.jeict.client.JeiCtClient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Temporary fix that lets EMI show CelesTech's recipes, plus a CelesTech-styled, enlarged recipe window.
 *
 * <p>The work happens in the mixins and {@code JeiCtEmiPlugin}. This class registers the client config and makes the
 * bridge decide (and log its decision) during client setup.
 */
@Mod(JeiCt.MOD_ID)
public final class JeiCt {
    public static final String MOD_ID = "jeict";

    public JeiCt(IEventBus modBus, ModContainer container, Dist dist) {
        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
            JeiCtClient.registerConfigScreen(container);
            modBus.addListener(FMLClientSetupEvent.class, event -> CelesTechBridge.isActive());
        }
    }
}

package com.jeict;

import com.jeict.bridge.CelesTechBridge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Temporary fix that lets EMI show CelesTech's recipes.
 *
 * <p>All the work happens in {@code EmiAgnosMixin} and {@code JeiCtEmiPlugin}. This class only makes the
 * bridge decide (and log its decision) during client setup, so the log says early on whether it is active.
 */
@Mod(JeiCt.MOD_ID)
public final class JeiCt {
    public static final String MOD_ID = "jeict";

    public JeiCt(IEventBus modBus, Dist dist) {
        if (dist.isClient()) {
            modBus.addListener(FMLClientSetupEvent.class, event -> CelesTechBridge.isActive());
        }
    }
}

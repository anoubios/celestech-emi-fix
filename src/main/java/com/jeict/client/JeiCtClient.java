package com.jeict.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only setup, kept apart so dedicated servers never load client classes. */
public final class JeiCtClient {
    private JeiCtClient() {}

    /** Adds a Config button for this mod to the Mods screen. */
    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

package com.jeict.emi;

import com.jeict.bridge.CelesTechBridge;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

/**
 * Runs CelesTech's own EMI plugin (which only tells EMI how big CelesTech's custom screens are) under this
 * addon's mod id. EMI then still lays out its panels around those screens, but no longer counts CelesTech as a
 * mod with EMI support of its own, so it keeps CelesTech's JEI recipes.
 *
 * <p>When the bridge is inactive, CelesTech's plugin is not hidden and runs by itself, so this does nothing.
 */
@EmiEntrypoint
public final class JeiCtEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!CelesTechBridge.isActive()) {
            return;
        }
        try {
            EmiPlugin celestech = (EmiPlugin) Class.forName(CelesTechBridge.CELESTECH_EMI_PLUGIN)
                    .getConstructor()
                    .newInstance();
            celestech.register(registry);
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            CelesTechBridge.LOGGER.warn("Could not run CelesTech's EMI screen-size plugin; recipes still work", e);
        }
    }
}

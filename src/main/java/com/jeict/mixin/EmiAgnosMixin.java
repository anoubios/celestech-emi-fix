package com.jeict.mixin;

import java.util.ArrayList;
import java.util.List;

import com.jeict.bridge.CelesTechBridge;

import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * EMI asks {@link EmiAgnos} which EMI plugins exist and which mods have one. EMI then skips the JEI plugin and
 * the JEI categories of every mod on that second list. While the bridge is active, CelesTech's bounds-only
 * plugin is left out of both answers, so EMI runs CelesTech's JEI plugin and imports its recipe categories.
 * {@code JeiCtEmiPlugin} registers the left-out plugin again under this addon's own mod id.
 *
 * <p>The injections are optional ({@code require = 0}): if a future EMI version changes these methods, the
 * game still starts and the bridge simply does nothing.
 */
@Mixin(value = EmiAgnos.class, remap = false)
public abstract class EmiAgnosMixin {
    @Inject(method = "getPlugins", at = @At("RETURN"), cancellable = true, require = 0)
    private static void jeict$hideCelesTechEmiPlugin(CallbackInfoReturnable<List<EmiPluginContainer>> cir) {
        List<EmiPluginContainer> plugins = cir.getReturnValue();
        if (plugins == null || !CelesTechBridge.isActive()) {
            return;
        }
        List<EmiPluginContainer> filtered = new ArrayList<>(plugins);
        if (filtered.removeIf(c -> c.plugin() != null
                && CelesTechBridge.CELESTECH_EMI_PLUGIN.equals(c.plugin().getClass().getName()))) {
            cir.setReturnValue(filtered);
            CelesTechBridge.noteHidden();
        }
    }

    @Inject(method = "getModsWithPlugins", at = @At("RETURN"), cancellable = true, require = 0)
    private static void jeict$unmarkCelesTech(CallbackInfoReturnable<List<String>> cir) {
        List<String> mods = cir.getReturnValue();
        if (mods == null || !CelesTechBridge.isActive()) {
            return;
        }
        List<String> filtered = new ArrayList<>(mods);
        if (filtered.removeIf(CelesTechBridge.CELESTECH_MOD_ID::equals)) {
            cir.setReturnValue(filtered);
        }
    }
}

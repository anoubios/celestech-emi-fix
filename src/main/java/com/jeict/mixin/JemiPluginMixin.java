package com.jeict.mixin;

import com.jeict.bridge.JemiRuntime;

import dev.emi.emi.api.stack.EmiStackInteraction;

import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * EMI 1.1.24 asks JEI for the ingredient under the mouse without checking that JEI is loaded, which logs
 * "Error rendering tooltip" (NullPointerException on JemiPlugin.runtime) while JEI reloads. This skips the
 * question until JEI is back.
 */
@Mixin(targets = "dev.emi.emi.jemi.JemiPlugin", remap = false)
public abstract class JemiPluginMixin {
    @Inject(method = "lambda$register$3", at = @At("HEAD"), cancellable = true, require = 0)
    private static void jeict$noRuntimeYet(Screen screen, int x, int y, CallbackInfoReturnable<EmiStackInteraction> cir) {
        if (JemiRuntime.isMissing()) {
            cir.setReturnValue(EmiStackInteraction.EMPTY);
        }
    }
}

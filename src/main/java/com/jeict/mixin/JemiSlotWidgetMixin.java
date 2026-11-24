package com.jeict.mixin;

import com.jeict.theme.CelesTechTheme;

import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps slot overlays of CelesTech machine panels, such as the press animation, inside the panel's work area.
 * Some of CelesTech's animations reach a pixel past it, which becomes visible once the recipe is enlarged.
 */
@Mixin(targets = "dev.emi.emi.jemi.widget.JemiSlotWidget", remap = false)
public abstract class JemiSlotWidgetMixin {
    @Unique private boolean jeict$clipped;

    @Inject(method = "drawOverlay", at = @At("HEAD"), require = 0)
    private void jeict$clipStart(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        jeict$clipped = CelesTechTheme.clipToPanelBody(graphics);
    }

    @Inject(method = "drawOverlay", at = @At("RETURN"), require = 0)
    private void jeict$clipEnd(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (jeict$clipped) {
            jeict$clipped = false;
            graphics.disableScissor();
        }
    }
}

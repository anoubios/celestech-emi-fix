package com.jeict.mixin;

import com.jeict.theme.CelesTechTheme;

import dev.emi.emi.widget.RecipeBackground;

import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Leaves out EMI's light frame around each recipe on CelesTech pages; CelesTech's panels have their own frame. */
@Mixin(value = RecipeBackground.class, remap = false)
public abstract class RecipeBackgroundMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$hideOnCelesTechPages(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (CelesTechTheme.isRenderingThemed()) {
            ci.cancel();
        }
    }
}

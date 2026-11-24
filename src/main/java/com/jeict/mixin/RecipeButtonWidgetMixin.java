package com.jeict.mixin;

import com.jeict.theme.CelesTechStyle;
import com.jeict.theme.CelesTechTheme;

import dev.emi.emi.widget.RecipeButtonWidget;

import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws EMI's fill, recipe tree, default and screenshot buttons in CelesTech's style on CelesTech pages. */
@Mixin(value = RecipeButtonWidget.class, remap = false)
public abstract class RecipeButtonWidgetMixin {
    @Shadow @Final protected int x;
    @Shadow @Final protected int y;
    @Shadow @Final protected int u;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$themedButton(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!CelesTechTheme.isRenderingThemed()) {
            return;
        }
        RecipeButtonWidget self = (RecipeButtonWidget) (Object) this;
        boolean hovered = self.getBounds().contains(mouseX, mouseY);
        if (CelesTechStyle.recipeButton(graphics, x, y, u, self.getTextureOffset(mouseX, mouseY), hovered)) {
            ci.cancel();
        }
    }
}

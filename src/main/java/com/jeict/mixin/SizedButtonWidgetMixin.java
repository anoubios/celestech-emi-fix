package com.jeict.mixin;

import java.util.function.BooleanSupplier;

import com.jeict.theme.CelesTechStyle;
import com.jeict.theme.CelesTechTheme;

import dev.emi.emi.screen.widget.SizedButtonWidget;

import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the six tab, category and page arrows of a CelesTech recipe page in CelesTech's style. */
@Mixin(value = SizedButtonWidget.class, remap = false)
public abstract class SizedButtonWidgetMixin {
    @Shadow protected int u;
    @Shadow @Final private BooleanSupplier isActive;

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$themedArrow(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SizedButtonWidget self = (SizedButtonWidget) (Object) this;
        if (!CelesTechTheme.isThemedArrow(self)) {
            return;
        }
        self.active = isActive.getAsBoolean();
        boolean hovered = self.active && self.isMouseOver(mouseX, mouseY);
        CelesTechStyle.arrow(graphics, self.getX(), self.getY(), self.getWidth(), self.getHeight(), u == 0, self.active, hovered);
        ci.cancel();
    }
}

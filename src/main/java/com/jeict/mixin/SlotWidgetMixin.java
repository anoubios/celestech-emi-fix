package com.jeict.mixin;

import com.jeict.theme.CelesTechStyle;
import com.jeict.theme.CelesTechTheme;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;

import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws EMI's plain slots (the workstation slots) as CelesTech slots on CelesTech pages. Slot subclasses, such as
 * the ones holding CelesTech's own recipe slots, are left alone.
 */
@Mixin(value = SlotWidget.class, remap = false)
public abstract class SlotWidgetMixin {
    @Shadow protected boolean drawBack;

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$themedSlot(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SlotWidget self = (SlotWidget) (Object) this;
        if (self.getClass() != SlotWidget.class || !drawBack || !CelesTechTheme.isRenderingThemed()) {
            return;
        }
        Bounds b = self.getBounds();
        CelesTechStyle.slot(graphics, b.x(), b.y(), b.width(), b.height());
        ci.cancel();
    }
}

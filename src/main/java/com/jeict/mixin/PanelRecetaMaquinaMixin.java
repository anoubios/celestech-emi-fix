package com.jeict.mixin;

import com.jeict.Config;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * CelesTech's machine panels place fluid tanks flush against the top edge of the work area, so the tank frames run
 * into its border line. With the CelesTech style on, tanks, their frames and their amount labels move down 2 pixels.
 */
@Mixin(targets = "com.crist.celestech.integracion.PanelRecetaMaquina", remap = false)
public abstract class PanelRecetaMaquinaMixin {
    /** Tank slot y in GUI pixels (CelesTech: 22). */
    @ModifyConstant(method = "fluido", constant = @Constant(intValue = 22), require = 0)
    private static int jeict$tankSlotY(int y) {
        return Config.restyle() ? y + 2 : y;
    }

    /** Tank frame y in half-pixels (CelesTech: 44). */
    @ModifyConstant(method = "marcoTanque", constant = @Constant(intValue = 44), require = 0)
    private static int jeict$tankFrameY(int y) {
        return Config.restyle() ? y + 4 : y;
    }

    /** Tank amount label y in half-pixels (CelesTech: 141). */
    @ModifyConstant(method = "etiqueta", constant = @Constant(floatValue = 141.0F), require = 0)
    private static float jeict$tankLabelY(float y) {
        return Config.restyle() ? y + 4.0F : y;
    }
}

package com.jeict.mixin;

import dev.emi.emi.widget.RecipeButtonWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets CelesTech pages move EMI's recipe buttons (fill, tree, default) to the middle of the recipe's height. */
@Mixin(value = RecipeButtonWidget.class, remap = false)
public interface RecipeButtonWidgetAccessor {
    @Accessor("x")
    int jeict$x();

    @Mutable
    @Accessor("x")
    void jeict$setX(int x);

    @Accessor("y")
    int jeict$y();

    @Mutable
    @Accessor("y")
    void jeict$setY(int y);
}

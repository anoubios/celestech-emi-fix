package com.jeict.theme;

/** Added to EMI's {@code RecipeScreen} by {@code RecipeScreenMixin}. */
public interface RecipeScreenAccess {
    /** Index (0 to 5) of one of EMI's own tab, category or page arrows, or -1 for any other widget. */
    int jeict$navigationArrowIndex(Object widget);
}

package com.jeict.theme;

import com.jeict.Config;

import dev.emi.emi.api.recipe.EmiRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Decides when EMI's recipe window is shown in CelesTech's style, and how far CelesTech recipes are enlarged. */
public final class CelesTechTheme {
    private static final String CELESTECH = "celestech";
    /** Height of CelesTech's machine and fabricator panels, whose work area runs from y = 20 to y = 78. */
    private static final int PANEL_HEIGHT = 100;
    private static final int BODY_TOP = 20;
    private static final int BODY_BOTTOM = 78;

    /** The recipe screen currently being drawn in CelesTech's style, or null. */
    @Nullable
    private static Screen themedScreen;
    private static int recipeWidth;
    private static int recipeHeight;

    private CelesTechTheme() {}

    public static boolean isCelesTech(@Nullable EmiRecipeCategory category) {
        if (category == null) {
            return false;
        }
        ResourceLocation id = category.getId();
        return id != null && CELESTECH.equals(id.getNamespace());
    }

    public static void beginRender(Screen screen, boolean themed) {
        themedScreen = themed && Config.restyle() ? screen : null;
        recipeWidth = 0;
        recipeHeight = 0;
    }

    public static void endRender() {
        themedScreen = null;
        recipeWidth = 0;
        recipeHeight = 0;
    }

    /** True only while EMI draws a CelesTech page of the screen that is currently open. */
    public static boolean isRenderingThemed() {
        Screen screen = themedScreen;
        return screen != null && Minecraft.getInstance().screen == screen;
    }

    /** Size of the recipe whose widgets EMI is drawing right now (0 x 0 outside a recipe). */
    public static void setRenderingRecipe(int width, int height) {
        recipeWidth = width;
        recipeHeight = height;
    }

    /**
     * Limits drawing to the work area of a CelesTech machine panel, so slot animations stay inside its frame.
     * Coordinates are the recipe's own; the current pose (including any zoom) maps them to the screen.
     *
     * @return true if a scissor was pushed and must be popped with {@link GuiGraphics#disableScissor()}
     */
    public static boolean clipToPanelBody(GuiGraphics g) {
        if (!isRenderingThemed() || recipeHeight != PANEL_HEIGHT || recipeWidth <= 0) {
            return false;
        }
        Matrix4f pose = g.pose().last().pose();
        Vector3f a = pose.transformPosition(0.0F, BODY_TOP, 0.0F, new Vector3f());
        Vector3f b = pose.transformPosition(recipeWidth, BODY_BOTTOM, 0.0F, new Vector3f());
        g.enableScissor((int) Math.floor(a.x), (int) Math.floor(a.y), (int) Math.ceil(b.x), (int) Math.ceil(b.y));
        return true;
    }

    public static boolean isThemedArrow(Object widget) {
        return isRenderingThemed()
                && Minecraft.getInstance().screen instanceof RecipeScreenAccess access
                && access.jeict$navigationArrowIndex(widget) >= 0;
    }

    /**
     * How much to enlarge a recipe of the given size to fill the given space, never below 1 or above the configured
     * maximum. CelesTech draws in half-pixels, so a zoom that makes each half-pixel a whole number of screen pixels
     * is preferred (sharp lines); otherwise each GUI pixel is at least a whole number of screen pixels (sharp text).
     */
    public static float zoom(int contentWidth, int contentHeight, int availableWidth, int availableHeight) {
        double max = Config.maxScale();
        if (max <= 1.0 || contentWidth <= 0 || contentHeight <= 0) {
            return 1.0F;
        }
        double limit = Math.min(max, Math.min((double) availableWidth / contentWidth, (double) availableHeight / contentHeight));
        double guiScale = Math.max(1.0, Minecraft.getInstance().getWindow().getGuiScale());
        double sharpText = Math.floor(limit * guiScale) / guiScale;
        double sharpLines = Math.floor(limit * guiScale / 2.0) * 2.0 / guiScale;
        double zoom = sharpLines >= 1.0 && sharpLines >= sharpText * 0.75 ? sharpLines : sharpText;
        return (float) Math.max(1.0, zoom);
    }
}

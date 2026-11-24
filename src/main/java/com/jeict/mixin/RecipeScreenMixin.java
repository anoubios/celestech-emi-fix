package com.jeict.mixin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jeict.Config;
import com.jeict.theme.CelesTechStyle;
import com.jeict.theme.CelesTechTheme;
import com.jeict.theme.RecipeScreenAccess;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeDisplay;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import dev.emi.emi.widget.RecipeButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CelesTech pages of EMI's recipe window: one recipe per page, enlarged to fill the free space (mouse input is
 * mapped into the enlarged recipe), and the window drawn in CelesTech's style. Other categories are left alone.
 */
@Mixin(value = RecipeScreen.class, remap = false)
public abstract class RecipeScreenMixin extends Screen implements RecipeScreenAccess {
    @Shadow private List<RecipeTab> tabs;
    @Shadow private int tab;
    @Shadow private int page;
    @Shadow private List<SizedButtonWidget> arrows;
    @Shadow private List<WidgetGroup> currentPage;
    @Shadow int backgroundWidth;
    @Shadow int backgroundHeight;
    @Shadow int x;
    @Shadow int y;

    @Unique private int jeict$emiHeight = -1;
    @Unique private int jeict$emiY;
    @Unique private float jeict$zoom = 1.0F;
    @Unique private WidgetGroup jeict$zoomed;
    @Unique private float jeict$originX;
    @Unique private float jeict$originY;
    @Unique private float jeict$left;
    @Unique private float jeict$right;
    @Unique private float jeict$top;
    @Unique private float jeict$bottom;
    @Unique private boolean jeict$reentry;
    @Unique private int jeict$realMouseX;
    @Unique private int jeict$realMouseY;

    protected RecipeScreenMixin(Component title) {
        super(title);
    }

    // ---- Layout: one enlarged recipe per CelesTech page ----

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/RecipeScreen;setPage(III)V", ordinal = 0), require = 0)
    private void jeict$onePerPage(CallbackInfo ci) {
        jeict$emiHeight = backgroundHeight;
        jeict$emiY = y;
        if (Config.maxScale() <= 1.0) {
            return;
        }
        for (RecipeTab t : tabs) {
            if (CelesTechTheme.isCelesTech(t.category)) {
                t.bakePages(46 + 23 + 1);
            }
        }
    }

    @ModifyArg(method = "setPage", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/RecipeScreen;setRecipePageWidth(I)V"), require = 0)
    private int jeict$zoomedWidth(int width) {
        jeict$zoom = 1.0F;
        jeict$zoomed = null;
        if (jeict$emiHeight < 0) {
            jeict$emiHeight = backgroundHeight;
            jeict$emiY = y;
        }
        // Every tab starts from EMI's own window size; only enlarged CelesTech pages change it below.
        backgroundHeight = jeict$emiHeight;
        y = jeict$emiY;
        RecipeTab current = tabs.get(tab);
        List<RecipeDisplay> displays = current.getPage(page);
        if (!CelesTechTheme.isCelesTech(current.category) || displays.size() != 1) {
            return width;
        }
        int stations = jeict$stationsBelow(current) ? 23 : 0;
        // EMI caps its window at maximumRecipeScreenHeight (256 by default); enlarged CelesTech pages may grow past it.
        int tallest = this.height - 52 - EmiConfig.verticalMargin;
        int availableWidth = Math.round(this.width * 0.72F) - 16;
        int recipeHeight = displays.get(0).getHeight();
        jeict$zoom = CelesTechTheme.zoom(current.getWidth(), recipeHeight, availableWidth, tallest - 46 - stations);
        if (jeict$zoom <= 1.0F) {
            return width;
        }
        backgroundHeight = Math.min(tallest, 46 + stations + Math.round(recipeHeight * jeict$zoom) + 6);
        y = (this.height - backgroundHeight) / 2 + 1;
        // The right padding grows with the zoom, so the enlarged buttons get a side panel of their own.
        return Math.round(current.getWidth() * jeict$zoom) + 8 + Math.round(8 * jeict$zoom);
    }

    @ModifyArg(method = "setPage", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/RecipeTab;constructWidgets(IIIII)Ljava/util/List;"), index = 3, require = 0)
    private int jeict$unzoomedLayoutWidth(int backgroundWidth) {
        return jeict$zoom > 1.0F ? tabs.get(tab).getWidth() + 16 : backgroundWidth;
    }

    @Inject(method = "setPage", at = @At("TAIL"), require = 0)
    private void jeict$afterSetPage(int tabPage, int newTab, int newPage, CallbackInfo ci) {
        jeict$placeZoomed();
        jeict$centerRecipeButtons();
    }

    @Unique
    private void jeict$placeZoomed() {
        jeict$zoomed = null;
        if (jeict$zoom <= 1.0F || tabs.isEmpty()) {
            return;
        }
        RecipeTab current = tabs.get(tab);
        List<RecipeDisplay> displays = current.getPage(page);
        WidgetGroup group = currentPage.stream().filter(g -> g.recipe != null).findFirst().orElse(null);
        if (displays.size() != 1 || group == null) {
            jeict$zoom = 1.0F;
            return;
        }
        RecipeDisplay display = displays.get(0);
        float z = jeict$zoom;
        int availableHeight = backgroundHeight - 46 - (jeict$stationsBelow(current) ? 23 : 0);
        jeict$zoomed = group;
        jeict$left = x + 8 + (current.getWidth() - display.getWidth()) / 2.0F * z;
        // Up to the window's right edge, so the recentered recipe buttons stay clickable.
        jeict$right = x + backgroundWidth;
        jeict$originX = jeict$left + display.getLeftWidth() * z;
        jeict$originY = y + 37 + Math.max(0.0F, (availableHeight - display.getHeight() * z) / 2.0F);
        jeict$top = jeict$originY - 4 * z;
        jeict$bottom = jeict$originY + (display.getHeight() + 4) * z;
    }

    /**
     * EMI stacks a recipe's buttons from its bottom edge, just right of the recipe. On CelesTech pages each column is
     * centered on the recipe's height, and the buttons right of the recipe are centered between the recipe and the
     * window's right edge.
     */
    @Unique
    private void jeict$centerRecipeButtons() {
        if (tabs.isEmpty() || !Config.restyle() || !CelesTechTheme.isCelesTech(tabs.get(tab).category)) {
            return;
        }
        for (WidgetGroup group : currentPage) {
            if (group.recipe == null) {
                continue;
            }
            Map<Integer, List<RecipeButtonWidgetAccessor>> columns = new TreeMap<>();
            for (Widget widget : group.widgets) {
                if (widget instanceof RecipeButtonWidget) {
                    RecipeButtonWidgetAccessor button = (RecipeButtonWidgetAccessor) widget;
                    columns.computeIfAbsent(button.jeict$x(), k -> new ArrayList<>()).add(button);
                }
            }
            for (List<RecipeButtonWidgetAccessor> column : columns.values()) {
                column.sort(Comparator.comparingInt(RecipeButtonWidgetAccessor::jeict$y));
                int top = (group.height - (column.size() * 14 - 2)) / 2;
                for (int i = 0; i < column.size(); i++) {
                    column.get(i).jeict$setY(top + i * 14);
                }
            }
            jeict$centerRightColumns(group, columns);
        }
    }

    @Unique
    private void jeict$centerRightColumns(WidgetGroup group, Map<Integer, List<RecipeButtonWidgetAccessor>> columns) {
        List<Integer> right = columns.keySet().stream().filter(cx -> cx >= group.width).toList();
        if (right.isEmpty()) {
            return;
        }
        boolean zoomed = group == jeict$zoomed;
        float zoom = zoomed ? jeict$zoom : 1.0F;
        float origin = zoomed ? jeict$originX : group.x;
        // Inner right edge of the window (its frame is 3 px thick), in the recipe's own coordinates.
        float windowRight = (x + backgroundWidth - 3 - origin) / zoom;
        int span = right.get(right.size() - 1) + 12 - right.get(0);
        int shift = group.width + Math.round((windowRight - group.width - span) / 2.0F) - right.get(0);
        for (int cx : right) {
            for (RecipeButtonWidgetAccessor button : columns.get(cx)) {
                button.jeict$setX(cx + shift);
            }
        }
    }

    @Unique
    private static boolean jeict$stationsBelow(RecipeTab t) {
        return EmiConfig.workstationLocation == SidebarSide.BOTTOM
                && (!EmiApi.getRecipeManager().getWorkstations(t.category).isEmpty() || RecipeScreen.resolve != null);
    }

    // ---- Mouse: map points inside the enlarged recipe back to its own coordinates ----

    @Unique
    private boolean jeict$inZoomed(double mouseX, double mouseY) {
        return jeict$zoomed != null && mouseX >= jeict$left && mouseX < jeict$right && mouseY >= jeict$top && mouseY < jeict$bottom;
    }

    @Unique
    private double jeict$virtualX(double mouseX) {
        return jeict$zoomed.x + (mouseX - jeict$originX) / jeict$zoom;
    }

    @Unique
    private double jeict$virtualY(double mouseY) {
        return jeict$zoomed.y + (mouseY - jeict$originY) / jeict$zoom;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void jeict$beginRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (jeict$reentry) {
            return;
        }
        boolean celestech = tab >= 0 && tab < tabs.size() && CelesTechTheme.isCelesTech(tabs.get(tab).category);
        CelesTechTheme.beginRender(this, celestech);
        jeict$realMouseX = mouseX;
        jeict$realMouseY = mouseY;
        if (jeict$inZoomed(mouseX, mouseY)) {
            jeict$reentry = true;
            try {
                this.render(graphics, (int) Math.floor(jeict$virtualX(mouseX)), (int) Math.floor(jeict$virtualY(mouseY)), delta);
            } finally {
                jeict$reentry = false;
                CelesTechTheme.endRender();
            }
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void jeict$endRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!jeict$reentry) {
            CelesTechTheme.endRender();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!jeict$reentry && jeict$inZoomed(mouseX, mouseY)) {
            jeict$reentry = true;
            try {
                cir.setReturnValue(this.mouseClicked(jeict$virtualX(mouseX), jeict$virtualY(mouseY), button));
            } finally {
                jeict$reentry = false;
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!jeict$reentry && jeict$inZoomed(mouseX, mouseY)) {
            jeict$reentry = true;
            try {
                cir.setReturnValue(this.mouseReleased(jeict$virtualX(mouseX), jeict$virtualY(mouseY), button));
            } finally {
                jeict$reentry = false;
            }
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true, require = 0)
    private void jeict$mouseDragged(double mouseX, double mouseY, int button, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (!jeict$reentry && jeict$inZoomed(mouseX, mouseY)) {
            jeict$reentry = true;
            try {
                cir.setReturnValue(this.mouseDragged(jeict$virtualX(mouseX), jeict$virtualY(mouseY), button, dx, dy));
            } finally {
                jeict$reentry = false;
            }
        }
    }

    @Redirect(method = "keyPressed", at = @At(value = "FIELD", target = "Ldev/emi/emi/screen/EmiScreenManager;lastMouseX:I", opcode = Opcodes.GETSTATIC), require = 0)
    private int jeict$keyMouseX() {
        int mx = EmiScreenManager.lastMouseX;
        int my = EmiScreenManager.lastMouseY;
        return jeict$inZoomed(mx, my) ? (int) Math.floor(jeict$virtualX(mx)) : mx;
    }

    @Redirect(method = "keyPressed", at = @At(value = "FIELD", target = "Ldev/emi/emi/screen/EmiScreenManager;lastMouseY:I", opcode = Opcodes.GETSTATIC), require = 0)
    private int jeict$keyMouseY() {
        int mx = EmiScreenManager.lastMouseX;
        int my = EmiScreenManager.lastMouseY;
        return jeict$inZoomed(mx, my) ? (int) Math.floor(jeict$virtualY(my)) : my;
    }

    // ---- Drawing: enlarge the recipe; keep tooltips and EMI's side panels at the real cursor ----

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), require = 0)
    private void jeict$translateGroup(PoseStack pose, float tx, float ty, float tz) {
        for (WidgetGroup group : currentPage) {
            if (group.x == (int) tx && group.y == (int) ty) {
                CelesTechTheme.setRenderingRecipe(group.width, group.height);
                break;
            }
        }
        WidgetGroup zoomed = jeict$zoomed;
        if (zoomed != null && (int) tx == zoomed.x && (int) ty == zoomed.y) {
            pose.translate(jeict$originX, jeict$originY, tz);
            pose.scale(jeict$zoom, jeict$zoom, 1.0F);
        } else {
            pose.translate(tx, ty, tz);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/EmiRenderHelper;drawTooltip(Lnet/minecraft/client/gui/screens/Screen;Ldev/emi/emi/runtime/EmiDrawContext;Ljava/util/List;II)V"), require = 0)
    private void jeict$tooltipAtCursor(Screen screen, EmiDrawContext context, List<?> tooltip, int mouseX, int mouseY) {
        jeict$callDrawTooltip(screen, context, tooltip, jeict$reentry ? jeict$realMouseX : mouseX, jeict$reentry ? jeict$realMouseY : mouseY);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Unique
    private static void jeict$callDrawTooltip(Screen screen, EmiDrawContext context, List tooltip, int mouseX, int mouseY) {
        EmiRenderHelper.drawTooltip(screen, context, tooltip, mouseX, mouseY);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/EmiScreenManager;drawBackground(Ldev/emi/emi/runtime/EmiDrawContext;IIF)V"), require = 0)
    private void jeict$panelsBackground(EmiDrawContext context, int mouseX, int mouseY, float delta) {
        EmiScreenManager.drawBackground(context, jeict$reentry ? jeict$realMouseX : mouseX, jeict$reentry ? jeict$realMouseY : mouseY, delta);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/EmiScreenManager;render(Ldev/emi/emi/runtime/EmiDrawContext;IIF)V"), require = 0)
    private void jeict$panels(EmiDrawContext context, int mouseX, int mouseY, float delta) {
        EmiScreenManager.render(context, jeict$reentry ? jeict$realMouseX : mouseX, jeict$reentry ? jeict$realMouseY : mouseY, delta);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/EmiScreenManager;drawForeground(Ldev/emi/emi/runtime/EmiDrawContext;IIF)V"), require = 0)
    private void jeict$panelsForeground(EmiDrawContext context, int mouseX, int mouseY, float delta) {
        EmiScreenManager.drawForeground(context, jeict$reentry ? jeict$realMouseX : mouseX, jeict$reentry ? jeict$realMouseY : mouseY, delta);
    }

    // ---- Drawing: CelesTech-styled window, tabs, header strips and workstation panel ----

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"), require = 0)
    private void jeict$chrome(EmiDrawContext context, ResourceLocation texture, int dx, int dy, int w, int h, int u, int v, int corner, int center) {
        if (!CelesTechTheme.isRenderingThemed()) {
            EmiRenderHelper.drawNinePatch(context, texture, dx, dy, w, h, u, v, corner, center);
            return;
        }
        GuiGraphics g = context.raw();
        if (v == 0 && u == 0) {
            CelesTechStyle.frame(g, dx, dy, w, h);
        } else if (v == 0 && (u == 9 || u == 18)) {
            CelesTechStyle.tab(g, dx, dy, w, y, u == 9);
        } else if (v == 16) {
            if (dy == y + 5) {
                CelesTechStyle.titleStrip(g, dx, dy, w, h);
            } else {
                CelesTechStyle.hole(g, dx, dy, w, h);
            }
        } else if (u == 58) {
            CelesTechStyle.hole(g, dx + 2, dy + 2, w - 4, h - 4);
        } else {
            CelesTechStyle.frame(g, dx, dy, w, h);
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;drawCenteredTextWithShadow(Lnet/minecraft/network/chat/Component;III)V", ordinal = 0), index = 3, require = 0)
    private int jeict$categoryNameColor(int color) {
        if (!CelesTechTheme.isRenderingThemed()) {
            return color;
        }
        return color == 0xFFFFFF ? CelesTechStyle.ORANGE : CelesTechStyle.AMBER;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiDrawContext;drawCenteredTextWithShadow(Lnet/minecraft/network/chat/Component;III)V", ordinal = 1), index = 3, require = 0)
    private int jeict$pageTextColor(int color) {
        return CelesTechTheme.isRenderingThemed() ? CelesTechStyle.TEXT : color;
    }

    @Override
    public int jeict$navigationArrowIndex(Object widget) {
        int index = arrows == null ? -1 : arrows.indexOf(widget);
        return index < 6 ? index : -1;
    }
}

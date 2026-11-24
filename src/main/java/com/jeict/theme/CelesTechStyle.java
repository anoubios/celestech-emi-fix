package com.jeict.theme;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Drawing primitives in CelesTech's own look, so EMI's recipe window matches the recipe panels CelesTech draws.
 *
 * <p>The colors and layering mirror CelesTech's {@code ChasisFino} and {@code PanelRecetaMaquina}. Like CelesTech, the
 * frames are drawn in "fine" space: half-size pixels, so 1 fine pixel is half a GUI pixel. Only Minecraft classes are
 * used here; nothing depends on CelesTech or EMI.
 */
public final class CelesTechStyle {
    public static final int OUTLINE = 0xFF0E1013;
    public static final int BODY_TOP = 0xFF2D323A;
    public static final int BODY_BOTTOM = 0xFF1C2026;
    public static final int BEVEL_LIGHT = 0xFF4F5661;
    public static final int BEVEL_DARK = 0xFF14171B;
    public static final int TITLE_TOP = 0xFF353B44;
    public static final int TITLE_BOTTOM = 0xFF262B33;
    public static final int TITLE_LINE = 0xFF7A3D0C;
    public static final int HOLE = 0xFF07090D;
    public static final int HOLE_LINE = 0xFF3A4048;
    public static final int SLOT_OUTLINE = 0xFF0A0C10;
    public static final int SLOT_FRAME = 0xFF3C424C;
    public static final int SLOT_INSIDE = 0xFF14171C;
    public static final int ORANGE = 0xFFFF9A3A;
    public static final int AMBER = 0xFFFFC24D;
    public static final int TEXT = 0xFFC8CDD3;
    public static final int MUTED = 0xFF59616C;

    private static final String[] ARROW_LEFT = {"...X", "..XX", ".XXX", "XXXX", ".XXX", "..XX", "...X"};
    private static final String[] ARROW_RIGHT = {"X...", "XX..", "XXX.", "XXXX", "XXX.", "XX..", "X..."};
    private static final String[] PLUS = {"..X..", "..X..", "XXXXX", "..X..", "..X.."};
    private static final String[] TREE = {"..XXX..", "..XXX..", "...X...", ".XXXXX.", ".X...X.", "XXX.XXX", "XXX.XXX"};
    private static final String[] HEART_FULL = {".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."};
    private static final String[] HEART_HALF = {".XX.XX.", "XXXX..X", "XXXX..X", ".XXX.X.", "..XXX..", "...X..."};
    private static final String[] HEART_EMPTY = {".XX.XX.", "X..X..X", "X.....X", ".X...X.", "..X.X..", "...X..."};
    private static final String[] CAMERA = {"..XX...", "XXXXXXX", "XX...XX", "XX...XX", "XXXXXXX"};

    private CelesTechStyle() {}

    /** CelesTech's chassis frame ({@code ChasisFino.marco}) filling a GUI rectangle. */
    public static void frame(GuiGraphics g, int x, int y, int w, int h) {
        fine(g, () -> marco(g, x * 2, y * 2, (x + w) * 2, (y + h) * 2));
    }

    /** A recipe-window tab. The selected tab runs 2 px into the window so the two read as one piece. */
    public static void tab(GuiGraphics g, int x, int y, int w, int windowY, boolean selected) {
        fine(g, () -> {
            int x0 = x * 2;
            int y0 = y * 2;
            int x1 = (x + w) * 2;
            int y1 = (selected ? windowY + 2 : windowY) * 2;
            g.fill(x0, y0, x1, y1, OUTLINE);
            if (selected) {
                g.fillGradient(x0 + 2, y0 + 2, x1 - 2, y1, TITLE_TOP, BODY_TOP);
                g.fill(x0 + 2, y0 + 2, x1 - 2, y0 + 4, ORANGE);
                g.fill(x0 + 2, y0 + 4, x0 + 4, y1, BEVEL_LIGHT);
                g.fill(x1 - 4, y0 + 4, x1 - 2, y1, BEVEL_DARK);
            } else {
                g.fillGradient(x0 + 2, y0 + 2, x1 - 2, y1, TITLE_BOTTOM, BEVEL_DARK);
                g.fill(x0 + 2, y0 + 2, x1 - 2, y0 + 4, HOLE_LINE);
            }
        });
    }

    /** A strip styled like the title bar of CelesTech's recipe panels (dark gradient, dark orange underline). */
    public static void titleStrip(GuiGraphics g, int x, int y, int w, int h) {
        fine(g, () -> {
            int x0 = x * 2;
            int y0 = y * 2;
            int x1 = (x + w) * 2;
            int y1 = (y + h) * 2;
            g.fill(x0, y0, x1, y1, OUTLINE);
            g.fillGradient(x0 + 2, y0 + 2, x1 - 2, y1 - 2, TITLE_TOP, TITLE_BOTTOM);
            g.fill(x0 + 2, y1 - 4, x1 - 2, y1 - 2, TITLE_LINE);
        });
    }

    /** A recessed area ({@code ChasisFino.hueco}). */
    public static void hole(GuiGraphics g, int x, int y, int w, int h) {
        fine(g, () -> {
            g.fill(x * 2, y * 2, (x + w) * 2, (y + h) * 2, HOLE);
            g.fill(x * 2, y * 2, (x + w) * 2, y * 2 + 2, HOLE_LINE);
        });
    }

    /** An item slot ({@code ChasisFino.ranura}) covering a GUI rectangle, usually 18 x 18. */
    public static void slot(GuiGraphics g, int x, int y, int w, int h) {
        fine(g, () -> {
            int x0 = x * 2;
            int y0 = y * 2;
            int x1 = (x + w) * 2;
            int y1 = (y + h) * 2;
            g.fill(x0, y0, x1, y1, SLOT_OUTLINE);
            g.fill(x0 + 2, y0 + 2, x1, y1, SLOT_FRAME);
            g.fill(x0 + 2, y0 + 2, x1 - 2, y1 - 2, SLOT_INSIDE);
        });
    }

    /** One of EMI's 12 x 12 navigation arrows. */
    public static void arrow(GuiGraphics g, int x, int y, int w, int h, boolean left, boolean enabled, boolean hovered) {
        button(g, x, y, w, h, enabled, hovered);
        glyph(g, left ? ARROW_LEFT : ARROW_RIGHT, x, y, w, h, glyphColor(enabled, hovered));
    }

    /**
     * One of EMI's 12 x 12 recipe buttons, identified by its texture column {@code u} and state {@code offset}
     * (the value of {@code RecipeButtonWidget.getTextureOffset}). Returns false for buttons it does not know, so the
     * caller can fall back to EMI's own drawing.
     */
    public static boolean recipeButton(GuiGraphics g, int x, int y, int u, int offset, boolean hovered) {
        String[] icon;
        boolean enabled = true;
        boolean lit = false;
        switch (u) {
            case 24 -> {
                icon = PLUS;
                enabled = offset != 24;
            }
            case 36 -> {
                icon = TREE;
                lit = offset >= 36;
            }
            case 48 -> {
                int state = offset - (hovered ? 12 : 0);
                icon = state == 36 ? HEART_FULL : state == 60 ? HEART_HALF : HEART_EMPTY;
            }
            case 60 -> icon = CAMERA;
            default -> {
                return false;
            }
        }
        button(g, x, y, 12, 12, enabled, hovered);
        glyph(g, icon, x, y, 12, 12, lit ? AMBER : glyphColor(enabled, hovered));
        return true;
    }

    private static void button(GuiGraphics g, int x, int y, int w, int h, boolean enabled, boolean hovered) {
        fine(g, () -> {
            int x0 = x * 2;
            int y0 = y * 2;
            int x1 = (x + w) * 2;
            int y1 = (y + h) * 2;
            int top = !enabled ? TITLE_BOTTOM : hovered ? 0xFF434A55 : TITLE_TOP;
            int bottom = !enabled ? BEVEL_DARK : hovered ? BODY_TOP : TITLE_BOTTOM;
            g.fill(x0, y0, x1, y1, OUTLINE);
            g.fillGradient(x0 + 2, y0 + 2, x1 - 2, y1 - 2, top, bottom);
            g.fill(x0 + 2, y0 + 2, x1 - 2, y0 + 4, enabled ? BEVEL_LIGHT : TITLE_TOP);
            if (hovered && enabled) {
                g.fill(x0 + 2, y1 - 4, x1 - 2, y1 - 2, TITLE_LINE);
            }
        });
    }

    private static int glyphColor(boolean enabled, boolean hovered) {
        return !enabled ? MUTED : hovered ? AMBER : ORANGE;
    }

    /**
     * Draws a small bitmap ("X" = one GUI pixel) exactly centered in a box. It is placed in fine space, so a glyph
     * with an odd width or height still sits in the middle of an even-sized button.
     */
    private static void glyph(GuiGraphics g, String[] rows, int x, int y, int w, int h, int color) {
        fine(g, () -> {
            int gx = x * 2 + (w - rows[0].length());
            int gy = y * 2 + (h - rows.length);
            for (int r = 0; r < rows.length; r++) {
                String row = rows[r];
                for (int c = 0; c < row.length(); c++) {
                    if (row.charAt(c) == 'X') {
                        g.fill(gx + c * 2, gy + r * 2, gx + c * 2 + 2, gy + r * 2 + 2, color);
                    }
                }
            }
        });
    }

    private static void marco(GuiGraphics g, int x0, int y0, int x1, int y1) {
        g.fill(x0, y0, x1, y1, OUTLINE);
        g.fillGradient(x0 + 2, y0 + 2, x1 - 2, y1 - 2, BODY_TOP, BODY_BOTTOM);
        g.fill(x0 + 2, y0 + 2, x1 - 2, y0 + 4, BEVEL_LIGHT);
        g.fill(x0 + 2, y0 + 2, x0 + 4, y1 - 2, BEVEL_LIGHT);
        g.fill(x0 + 2, y1 - 4, x1 - 2, y1 - 2, BEVEL_DARK);
        g.fill(x1 - 4, y0 + 2, x1 - 2, y1 - 2, BEVEL_DARK);
    }

    private static void fine(GuiGraphics g, Runnable draw) {
        g.pose().pushPose();
        g.pose().scale(0.5F, 0.5F, 1.0F);
        try {
            draw.run();
        } finally {
            g.pose().popPose();
        }
    }
}

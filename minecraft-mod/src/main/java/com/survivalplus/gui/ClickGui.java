package com.survivalplus.gui;

import com.survivalplus.SurvivalPlus;
import com.survivalplus.features.AutoBridge;
import com.survivalplus.features.CaveLight;
import com.survivalplus.features.OreESP;
import com.survivalplus.features.SprintToggle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Dear-ImGui–style click GUI.
 * Opens on INSERT, unlocks the mouse (Minecraft Screen handles that automatically).
 *
 * Layout
 * ──────
 *  ┌─────────────────────────────────────────────────────┐
 *  │  ◈  SurvivalPlus                          [×] Close │  ← title bar
 *  ├──────────────┬──────────────────────────────────────┤
 *  │ CATEGORIES   │  FEATURES                            │
 *  │  ▸ Render    │   ┌──────────────────────────────┐  │
 *  │    World     │   │ ● Ore ESP          [ENABLED]  │  │
 *  │    Movement  │   │   Show ore outlines …         │  │
 *  │    Misc      │   └──────────────────────────────┘  │
 *  │              │   …                                  │
 *  └──────────────┴──────────────────────────────────────┘
 */
public class ClickGui extends Screen {

    // ── palette ───────────────────────────────────────────────────────────
    private static final int BG          = col(0x0D, 0x0D, 0x15, 230);
    private static final int PANEL       = col(0x16, 0x16, 0x24, 255);
    private static final int PANEL_LIGHT = col(0x1E, 0x1E, 0x30, 255);
    private static final int SIDEBAR_BG  = col(0x10, 0x10, 0x1C, 255);
    private static final int TITLE_BAR   = col(0x0A, 0x0A, 0x14, 255);
    private static final int ACCENT      = col(0x7C, 0x3A, 0xFF, 255);
    private static final int ACCENT_DIM  = col(0x4A, 0x22, 0xAA, 200);
    private static final int SEL_CAT     = col(0x2A, 0x1A, 0x50, 255);
    private static final int DIVIDER     = col(0x2A, 0x2A, 0x40, 255);
    private static final int TEXT_MAIN   = col(0xCD, 0xD6, 0xF4, 255);
    private static final int TEXT_DIM    = col(0x6C, 0x7B, 0xA8, 255);
    private static final int TEXT_CAT    = col(0x89, 0xB4, 0xFA, 255);
    private static final int ON_GREEN    = col(0x4D, 0xD6, 0x8A, 255);
    private static final int OFF_RED     = col(0xF3, 0x8B, 0xA8, 255);
    private static final int HOVER_CARD  = col(0x22, 0x22, 0x38, 255);

    // ── layout ────────────────────────────────────────────────────────────
    private static final int WIN_W       = 440;
    private static final int WIN_H       = 300;
    private static final int TITLE_H     = 22;
    private static final int SIDEBAR_W   = 110;
    private static final int CARD_H      = 52;
    private static final int CARD_PAD    = 6;
    private static final int TOGGLE_W    = 42;
    private static final int TOGGLE_H    = 14;

    // ── window pos ────────────────────────────────────────────────────────
    private int winX, winY;
    private boolean dragging = false;
    private double dragOffX, dragOffY;

    // ── categories & features ─────────────────────────────────────────────
    private record FeatureEntry(String name, String desc, Runnable toggle, BoolSupplier enabled) {}
    private interface BoolSupplier { boolean get(); }
    private record Category(String name, List<FeatureEntry> features) {}

    private final List<Category> categories = new ArrayList<>();
    private int selectedCategory = 0;
    private int scrollOffset = 0;

    public ClickGui() {
        super(Text.empty());
        buildCategories();
    }

    private void buildCategories() {
        OreESP esp = SurvivalPlus.oreESP;
        AutoBridge ab = SurvivalPlus.autoBridge;
        CaveLight cl = SurvivalPlus.caveLight;
        SprintToggle st = SurvivalPlus.sprintToggle;

        List<FeatureEntry> render = new ArrayList<>();
        render.add(new FeatureEntry(
            "Ore ESP",
            "Highlights ores through walls with colored outlines",
            esp::toggle, esp::isEnabled));
        render.add(new FeatureEntry(
            "Cave Light",
            "Fullbright — illuminates dark caves completely",
            cl::toggle, cl::isEnabled));

        List<FeatureEntry> world = new ArrayList<>();
        world.add(new FeatureEntry(
            "Auto Bridge",
            "Places blocks automatically while walking off edges",
            ab::toggle, ab::isEnabled));

        List<FeatureEntry> movement = new ArrayList<>();
        movement.add(new FeatureEntry(
            "Sprint Toggle",
            "Keeps you sprinting whenever you move forward",
            st::toggle, st::isEnabled));

        categories.add(new Category("Render",   render));
        categories.add(new Category("World",    world));
        categories.add(new Category("Movement", movement));
    }

    @Override
    protected void init() {
        winX = (width  - WIN_W) / 2;
        winY = (height - WIN_H) / 2;
    }

    // ── render ────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // Dark tinted full-screen backdrop
        ctx.fill(0, 0, width, height, col(0, 0, 0, 110));

        int wx = winX, wy = winY;

        // Window shadow
        ctx.fill(wx + 4, wy + 4, wx + WIN_W + 4, wy + WIN_H + 4, col(0, 0, 0, 80));

        // Window background
        ctx.fill(wx, wy, wx + WIN_W, wy + WIN_H, PANEL);

        // Title bar
        ctx.fill(wx, wy, wx + WIN_W, wy + TITLE_H, TITLE_BAR);
        // Accent line under title
        ctx.fill(wx, wy + TITLE_H - 2, wx + WIN_W, wy + TITLE_H, ACCENT);

        // Title text  ◈ SurvivalPlus
        ctx.drawTextWithShadow(textRenderer, "◈ SurvivalPlus", wx + 8, wy + 7, ACCENT);
        ctx.drawTextWithShadow(textRenderer, "v1.0", wx + WIN_W - 30, wy + 7, TEXT_DIM);

        // Close button [×]
        boolean closeHover = mx >= wx + WIN_W - 18 && mx < wx + WIN_W - 4
                          && my >= wy + 5 && my < wy + 17;
        ctx.fill(wx + WIN_W - 19, wy + 4, wx + WIN_W - 3, wy + 18,
                 closeHover ? col(0xF3, 0x8B, 0xA8, 180) : col(0x3A, 0x1A, 0x2A, 200));
        ctx.drawTextWithShadow(textRenderer, "×", wx + WIN_W - 14, wy + 6,
                               closeHover ? col(0xFF, 0xFF, 0xFF, 255) : TEXT_DIM);

        // Sidebar
        int sx = wx;
        int sy = wy + TITLE_H;
        int sh = WIN_H - TITLE_H;
        ctx.fill(sx, sy, sx + SIDEBAR_W, sy + sh, SIDEBAR_BG);
        ctx.fill(sx + SIDEBAR_W - 1, sy, sx + SIDEBAR_W, sy + sh, DIVIDER);

        ctx.drawTextWithShadow(textRenderer, "CATEGORIES", sx + 8, sy + 7, TEXT_DIM);
        drawHLine(ctx, sx + 6, sx + SIDEBAR_W - 6, sy + 18, DIVIDER);

        int catY = sy + 24;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            boolean sel = i == selectedCategory;
            boolean hov = mx >= sx + 4 && mx < sx + SIDEBAR_W - 4
                       && my >= catY && my < catY + 18;

            int bg = sel ? SEL_CAT : (hov ? col(0x1A, 0x1A, 0x2C, 200) : 0);
            if (bg != 0) ctx.fill(sx + 4, catY, sx + SIDEBAR_W - 4, catY + 18, bg);

            if (sel) {
                ctx.fill(sx + 4, catY, sx + 6, catY + 18, ACCENT);
            }
            ctx.drawTextWithShadow(textRenderer,
                (sel ? "▶ " : "  ") + cat.name(),
                sx + 12, catY + 5,
                sel ? TEXT_MAIN : TEXT_CAT);
            catY += 20;
        }

        // Feature area
        int fx = wx + SIDEBAR_W + 1;
        int fy = wy + TITLE_H;
        int fw = WIN_W - SIDEBAR_W - 1;
        int fh = WIN_H - TITLE_H;
        ctx.fill(fx, fy, fx + fw, fy + fh, PANEL_LIGHT);

        // Header for feature area
        String catName = categories.get(selectedCategory).name().toUpperCase();
        ctx.drawTextWithShadow(textRenderer, catName, fx + 10, fy + 7, ACCENT);
        drawHLine(ctx, fx + 6, fx + fw - 6, fy + 18, DIVIDER);

        // Feature cards with scissor clipping
        List<FeatureEntry> features = categories.get(selectedCategory).features();
        int cardAreaY = fy + 24;
        int cardAreaH = fh - 24;
        int totalH = features.size() * (CARD_H + CARD_PAD) - CARD_PAD;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, totalH - cardAreaH)));

        ctx.enableScissor(fx, cardAreaY, fx + fw, cardAreaY + cardAreaH);

        int cy = cardAreaY - scrollOffset;
        for (FeatureEntry feat : features) {
            boolean on  = feat.enabled().get();
            boolean hov = mx >= fx + 6 && mx < fx + fw - 6
                       && my >= cy && my < cy + CARD_H
                       && my >= cardAreaY && my < cardAreaY + cardAreaH;

            // Card background
            ctx.fill(fx + 6, cy, fx + fw - 6, cy + CARD_H, hov ? HOVER_CARD : PANEL);
            // Left accent stripe (green if on, dim red if off)
            ctx.fill(fx + 6, cy, fx + 8, cy + CARD_H, on ? ON_GREEN : col(0x44, 0x22, 0x2A, 255));

            // Dot indicator
            int dotColor = on ? ON_GREEN : OFF_RED;
            ctx.fill(fx + 14, cy + 8, fx + 18, cy + 12, dotColor);

            // Feature name
            ctx.drawTextWithShadow(textRenderer, feat.name(), fx + 22, cy + 7, TEXT_MAIN);

            // Toggle button on the right
            drawToggle(ctx, fx + fw - TOGGLE_W - 12, cy + (CARD_H - TOGGLE_H) / 2, on);

            // Description
            ctx.drawTextWithShadow(textRenderer, feat.desc(), fx + 22, cy + 22, TEXT_DIM);

            // Separator
            drawHLine(ctx, fx + 6, fx + fw - 6, cy + CARD_H - 1, DIVIDER);

            cy += CARD_H + CARD_PAD;
        }

        ctx.disableScissor();

        // Scrollbar
        if (totalH > cardAreaH) {
            int sbX = fx + fw - 5;
            float ratio = (float) cardAreaH / totalH;
            int sbH  = (int) (cardAreaH * ratio);
            int sbY  = cardAreaY + (int) ((float) scrollOffset / totalH * cardAreaH);
            ctx.fill(sbX, cardAreaY, sbX + 3, cardAreaY + cardAreaH, col(0x22, 0x22, 0x38, 255));
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, ACCENT_DIM);
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawToggle(DrawContext ctx, int x, int y, boolean on) {
        int track = on ? col(0x3A, 0xAA, 0x60, 255) : col(0x40, 0x30, 0x50, 255);
        ctx.fill(x, y, x + TOGGLE_W, y + TOGGLE_H, track);
        // Knob
        int knobX = on ? x + TOGGLE_W - TOGGLE_H : x;
        ctx.fill(knobX, y, knobX + TOGGLE_H, y + TOGGLE_H, on ? ON_GREEN : col(0x80, 0x60, 0x90, 255));
        // Label
        String label = on ? "ON" : "OFF";
        int lx = on ? x + 4 : x + TOGGLE_H + 3;
        ctx.drawTextWithShadow(textRenderer, label, lx, y + 3, on ? col(0, 0, 0, 200) : TEXT_DIM);
    }

    private static void drawHLine(DrawContext ctx, int x1, int x2, int y, int color) {
        ctx.fill(x1, y, x2, y + 1, color);
    }

    // ── mouse interaction ─────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        // Close button
        if (mx >= winX + WIN_W - 19 && mx < winX + WIN_W - 3
         && my >= winY + 4 && my < winY + 18) {
            close();
            return true;
        }

        // Title bar drag start
        if (mx >= winX && mx < winX + WIN_W && my >= winY && my < winY + TITLE_H) {
            dragging = true;
            dragOffX = mx - winX;
            dragOffY = my - winY;
            return true;
        }

        // Category selection (sidebar)
        int sy = winY + TITLE_H + 24;
        for (int i = 0; i < categories.size(); i++) {
            if (mx >= winX + 4 && mx < winX + SIDEBAR_W - 4
             && my >= sy && my < sy + 18) {
                selectedCategory = i;
                scrollOffset = 0;
                return true;
            }
            sy += 20;
        }

        // Feature toggle click
        int fx = winX + SIDEBAR_W + 1;
        int fw = WIN_W - SIDEBAR_W - 1;
        int cardAreaY = winY + TITLE_H + 24;
        int cardAreaH = WIN_H - TITLE_H - 24;

        if (mx >= fx && mx < fx + fw && my >= cardAreaY && my < cardAreaY + cardAreaH) {
            List<FeatureEntry> features = categories.get(selectedCategory).features();
            int cy = cardAreaY - scrollOffset;
            for (FeatureEntry feat : features) {
                if (my >= cy && my < cy + CARD_H) {
                    feat.toggle().run();
                    return true;
                }
                cy += CARD_H + CARD_PAD;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging && button == 0) {
            winX = (int) (mx - dragOffX);
            winY = (int) (my - dragOffY);
            // Keep window on screen
            winX = Math.max(0, Math.min(width  - WIN_W, winX));
            winY = Math.max(0, Math.min(height - WIN_H, winY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        int fx = winX + SIDEBAR_W + 1;
        int fw = WIN_W - SIDEBAR_W - 1;
        if (mx >= fx && mx < fx + fw && my >= winY + TITLE_H && my < winY + WIN_H) {
            scrollOffset -= (int) (vScroll * 14);
            scrollOffset = Math.max(0, scrollOffset);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    // ── helpers ───────────────────────────────────────────────────────────

    private static int col(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

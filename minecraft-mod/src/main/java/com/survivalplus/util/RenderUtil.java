package com.survivalplus.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class RenderUtil {

    /**
     * Draws a wireframe box outline at the given world coordinates.
     * Call between RenderSystem setup and teardown.
     */
    public static void drawOutlinedBox(BufferBuilder buf, Matrix4f matrix,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       float r, float g, float b, float a) {
        float fx1 = (float) x1, fy1 = (float) y1, fz1 = (float) z1;
        float fx2 = (float) x2, fy2 = (float) y2, fz2 = (float) z2;

        // Bottom face
        line(buf, matrix, fx1, fy1, fz1, fx2, fy1, fz1, r, g, b, a);
        line(buf, matrix, fx2, fy1, fz1, fx2, fy1, fz2, r, g, b, a);
        line(buf, matrix, fx2, fy1, fz2, fx1, fy1, fz2, r, g, b, a);
        line(buf, matrix, fx1, fy1, fz2, fx1, fy1, fz1, r, g, b, a);

        // Top face
        line(buf, matrix, fx1, fy2, fz1, fx2, fy2, fz1, r, g, b, a);
        line(buf, matrix, fx2, fy2, fz1, fx2, fy2, fz2, r, g, b, a);
        line(buf, matrix, fx2, fy2, fz2, fx1, fy2, fz2, r, g, b, a);
        line(buf, matrix, fx1, fy2, fz2, fx1, fy2, fz1, r, g, b, a);

        // Vertical edges
        line(buf, matrix, fx1, fy1, fz1, fx1, fy2, fz1, r, g, b, a);
        line(buf, matrix, fx2, fy1, fz1, fx2, fy2, fz1, r, g, b, a);
        line(buf, matrix, fx2, fy1, fz2, fx2, fy2, fz2, r, g, b, a);
        line(buf, matrix, fx1, fy1, fz2, fx1, fy2, fz2, r, g, b, a);
    }

    private static void line(BufferBuilder buf, Matrix4f matrix,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float r, float g, float b, float a) {
        buf.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buf.vertex(matrix, x2, y2, z2).color(r, g, b, a);
    }

    /**
     * Sets up RenderSystem state for line rendering and returns a started BufferBuilder.
     * Must call endBoxRendering() when done.
     */
    public static BufferBuilder beginBoxRendering(float lineWidth) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(lineWidth);

        Tessellator tess = Tessellator.getInstance();
        return tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
    }

    public static void endBoxRendering(BuiltBuffer built) {
        BufferRenderer.drawWithGlobalProgram(built);
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // ── GUI color helpers ──────────────────────────────────────────────────

    public static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }
}

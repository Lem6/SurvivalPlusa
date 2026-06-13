package com.survivalplus.features;

import com.survivalplus.util.RenderUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.*;

public class OreESP {

    private boolean enabled = false;
    private int scanRadius = 20;

    public record OreColor(float r, float g, float b) {}

    private static final Map<Block, OreColor> ORE_COLORS = new LinkedHashMap<>();

    static {
        ORE_COLORS.put(Blocks.DIAMOND_ORE,           new OreColor(0.0f, 1.0f, 1.0f));
        ORE_COLORS.put(Blocks.DEEPSLATE_DIAMOND_ORE, new OreColor(0.0f, 1.0f, 1.0f));
        ORE_COLORS.put(Blocks.EMERALD_ORE,           new OreColor(0.0f, 1.0f, 0.2f));
        ORE_COLORS.put(Blocks.DEEPSLATE_EMERALD_ORE, new OreColor(0.0f, 1.0f, 0.2f));
        ORE_COLORS.put(Blocks.GOLD_ORE,              new OreColor(1.0f, 0.85f, 0.0f));
        ORE_COLORS.put(Blocks.DEEPSLATE_GOLD_ORE,    new OreColor(1.0f, 0.85f, 0.0f));
        ORE_COLORS.put(Blocks.IRON_ORE,              new OreColor(0.8f, 0.5f, 0.2f));
        ORE_COLORS.put(Blocks.DEEPSLATE_IRON_ORE,    new OreColor(0.8f, 0.5f, 0.2f));
        ORE_COLORS.put(Blocks.COAL_ORE,              new OreColor(0.35f, 0.35f, 0.35f));
        ORE_COLORS.put(Blocks.DEEPSLATE_COAL_ORE,    new OreColor(0.35f, 0.35f, 0.35f));
        ORE_COLORS.put(Blocks.COPPER_ORE,            new OreColor(0.72f, 0.45f, 0.2f));
        ORE_COLORS.put(Blocks.DEEPSLATE_COPPER_ORE,  new OreColor(0.72f, 0.45f, 0.2f));
        ORE_COLORS.put(Blocks.LAPIS_ORE,             new OreColor(0.1f, 0.2f, 0.9f));
        ORE_COLORS.put(Blocks.DEEPSLATE_LAPIS_ORE,   new OreColor(0.1f, 0.2f, 0.9f));
        ORE_COLORS.put(Blocks.REDSTONE_ORE,          new OreColor(1.0f, 0.1f, 0.1f));
        ORE_COLORS.put(Blocks.DEEPSLATE_REDSTONE_ORE,new OreColor(1.0f, 0.1f, 0.1f));
        ORE_COLORS.put(Blocks.ANCIENT_DEBRIS,        new OreColor(0.6f, 0.2f, 0.9f));
        ORE_COLORS.put(Blocks.NETHER_GOLD_ORE,       new OreColor(1.0f, 0.7f, 0.0f));
        ORE_COLORS.put(Blocks.NETHER_QUARTZ_ORE,     new OreColor(0.9f, 0.9f, 0.8f));
    }

    public void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        World world = client.world;
        Vec3d camPos = context.camera().getPos();
        BlockPos center = client.player.getBlockPos();

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder buf = RenderUtil.beginBoxRendering(2.0f);

        int r = scanRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    Block block = world.getBlockState(pos).getBlock();
                    OreColor color = ORE_COLORS.get(block);
                    if (color == null) continue;

                    double x1 = pos.getX() - 0.005;
                    double y1 = pos.getY() - 0.005;
                    double z1 = pos.getZ() - 0.005;
                    double x2 = pos.getX() + 1.005;
                    double y2 = pos.getY() + 1.005;
                    double z2 = pos.getZ() + 1.005;

                    RenderUtil.drawOutlinedBox(buf, matrix, x1, y1, z1, x2, y2, z2,
                        color.r(), color.g(), color.b(), 1.0f);
                }
            }
        }

        RenderUtil.endBoxRendering(buf.end());
        matrices.pop();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { this.enabled = !this.enabled; }

    public int getScanRadius() { return scanRadius; }
    public void setScanRadius(int scanRadius) { this.scanRadius = Math.max(5, Math.min(64, scanRadius)); }
}

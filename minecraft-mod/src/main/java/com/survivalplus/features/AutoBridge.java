package com.survivalplus.features;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoBridge {

    private boolean enabled = false;
    private int prevSelectedSlot = -1;

    public void tick(MinecraftClient client) {
        if (!enabled) return;
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) return;

        // Only trigger when player is moving horizontally
        if (!player.isOnGround()) return;
        double speed = Math.sqrt(player.getVelocity().x * player.getVelocity().x
                               + player.getVelocity().z * player.getVelocity().z);
        if (speed < 0.05) return;

        World world = client.world;
        BlockPos feetPos = player.getBlockPos();
        BlockPos belowFeet = feetPos.down();

        // Check the block directly below is solid (we're standing on something)
        if (world.getBlockState(belowFeet).isAir()) return;

        // Look one block ahead in the player's movement direction
        Direction facing = player.getHorizontalFacing();
        BlockPos aheadPos = feetPos.offset(facing);
        BlockPos aheadBelow = aheadPos.down();

        // If the block ahead at feet level is air and below that is also air → we're about to walk off
        boolean aboutToFallOff = world.getBlockState(aheadPos).isAir()
                              && world.getBlockState(aheadBelow).isAir()
                              && world.getBlockState(belowFeet.offset(facing)).isAir();
        if (!aboutToFallOff) return;

        // Find a placeable block in hotbar
        int targetSlot = findBlockSlot(player.getInventory());
        if (targetSlot == -1) return;

        int savedSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = targetSlot;

        // Determine the surface to click on: the TOP of the block currently below the ahead pos
        // We place against the side of the block at belowFeet facing toward aheadBelow
        BlockPos supportPos = feetPos.offset(facing.getOpposite()).down(); // behind-below
        if (world.getBlockState(supportPos.offset(facing)).isAir()) {
            supportPos = belowFeet; // fall back to the block we're standing on
        }

        BlockState support = world.getBlockState(belowFeet);
        if (!support.isAir()) {
            Vec3d hitVec = Vec3d.ofCenter(aheadBelow).add(0, 0.5, 0);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, aheadBelow, false);
            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
            player.swingHand(Hand.MAIN_HAND);
        }

        player.getInventory().selectedSlot = savedSlot;
    }

    private int findBlockSlot(PlayerInventory inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { this.enabled = !this.enabled; }
}

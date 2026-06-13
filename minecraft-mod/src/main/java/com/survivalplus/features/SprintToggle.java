package com.survivalplus.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Auto-sprint — keeps the player sprinting whenever they are moving forward.
 */
public class SprintToggle {

    private boolean enabled = false;

    public void tick(MinecraftClient client) {
        if (!enabled) return;
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        boolean movingForward = client.options.forwardKey.isPressed();
        if (movingForward && player.isOnGround() && !player.isSprinting()
                && !player.isSubmergedInWater() && !player.isRiding()
                && player.getHungerManager().getFoodLevel() > 6) {
            player.setSprinting(true);
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { this.enabled = !this.enabled; }
}

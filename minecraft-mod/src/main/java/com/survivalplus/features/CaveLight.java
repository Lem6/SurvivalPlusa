package com.survivalplus.features;

import net.minecraft.client.MinecraftClient;

/**
 * Fullbright / Cave Light — cranks the game's gamma to maximum so dark caves
 * are fully lit. Restores the original gamma on disable.
 */
public class CaveLight {

    private boolean enabled = false;
    private double savedGamma = 1.0;
    private static final double FULLBRIGHT_GAMMA = 10.0;

    public void tick(MinecraftClient client) {
        if (client.options == null) return;
        double currentGamma = client.options.gamma.getValue();

        if (enabled) {
            if (currentGamma < FULLBRIGHT_GAMMA) {
                client.options.gamma.setValue(FULLBRIGHT_GAMMA);
            }
        } else {
            // If we just turned off, restore saved gamma
            if (currentGamma >= FULLBRIGHT_GAMMA) {
                client.options.gamma.setValue(savedGamma);
            }
        }
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (enabled && !this.enabled && client.options != null) {
            savedGamma = client.options.gamma.getValue();
        }
        this.enabled = enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }
}

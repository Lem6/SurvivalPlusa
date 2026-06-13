package com.survivalplus;

import com.survivalplus.features.AutoBridge;
import com.survivalplus.features.CaveLight;
import com.survivalplus.features.OreESP;
import com.survivalplus.features.SprintToggle;
import com.survivalplus.gui.ClickGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SurvivalPlus implements ClientModInitializer {

    public static final String MOD_ID = "survivalplus";

    private static KeyBinding guiKey;

    public static final OreESP oreESP = new OreESP();
    public static final AutoBridge autoBridge = new AutoBridge();
    public static final CaveLight caveLight = new CaveLight();
    public static final SprintToggle sprintToggle = new SprintToggle();

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.survivalplus.gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_INSERT,
            "category.survivalplus"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGui());
                }
            }
            if (client.player == null || client.world == null) return;

            autoBridge.tick(client);
            caveLight.tick(client);
            sprintToggle.tick(client);
        });

        WorldRenderEvents.LAST.register(context -> {
            if (oreESP.isEnabled()) {
                oreESP.render(context);
            }
        });
    }
}

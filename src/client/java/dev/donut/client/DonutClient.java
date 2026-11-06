package dev.donut.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.donut.client.config.ClientConfig;
import dev.donut.client.config.ConfigStore;
import dev.donut.client.chunkfinder.ChunkFinderScanner;
import dev.donut.client.amethyst.AmethystScanner;
import dev.donut.client.gui.BaseFinderPanel;
import dev.donut.client.fullbright.FullbrightController;
import dev.donut.client.freecam.FreecamController;
import dev.donut.client.fly.FlyController;
import dev.donut.client.namehider.NameHiderController;
import dev.donut.client.hotbar.HotbarBlur;
import dev.donut.client.gui.DonutScreen;
import dev.donut.client.gui.GlassPanel;
import dev.donut.client.gui.ModuleArrayList;
import dev.donut.client.input.ModuleKeybinds;
import dev.donut.client.render.HighlightRenderer;
import dev.donut.client.render.BlockHighlightScanner;
import dev.donut.client.render.EntityEspRules;
import dev.donut.client.autobuild.AutoBuilderController;
import dev.donut.client.gui.AutoBuilderScreen;
import dev.donut.client.gui.AutoBuilderHud;
import dev.donut.client.gui.ProgressBarHud;
import dev.donut.client.gui.VapeHud;
import dev.donut.client.progress.ProgressTimers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class DonutClient implements ClientModInitializer {
    public static final String MOD_ID = "ultimatepvp";
    private ConfigStore store;
    private ClientConfig config;
    private ChunkFinderScanner chunkScanner;
    private AmethystScanner amethystScanner;
    private dev.donut.client.light.LightDebugScanner lightDebug;

    @Override
    public void onInitializeClient() {
        GlassPanel.initialize();
        ProgressBarHud.initialize();
        store = new ConfigStore(FabricLoader.getInstance().getConfigDir().resolve("donut-client.json"));
        config = store.load();
        new HotbarBlur(config);
        new NameHiderController(config);
        chunkScanner = new ChunkFinderScanner(config);
        BlockHighlightScanner blockHighlights = new BlockHighlightScanner();
        amethystScanner = new AmethystScanner(config);
        lightDebug = new dev.donut.client.light.LightDebugScanner(config);
        ClientTickEvents.END_CLIENT_TICK.register(auto -> {
            dev.donut.client.spectrebuilder.BuilderPlacement.tick(auto);
            dev.donut.client.spectrebuilder.CreativeBuilder.INSTANCE.tick(auto);
            blockHighlights.tick(auto, config.blockHighlights);
            if (config.singleplayerEsp.throughWalls && !EntityEspRules.throughWallsAllowed(auto)) {
                config.singleplayerEsp.throughWalls = false;
            }
        });
        FullbrightController fullbright = new FullbrightController(config);
        ClientTickEvents.START_CLIENT_TICK.register(fullbright::tick);
        FreecamController freecam = new FreecamController(config);
        ClientTickEvents.START_CLIENT_TICK.register(freecam::tick);
        FlyController fly = new FlyController(config);
        ClientTickEvents.START_CLIENT_TICK.register(fly::tick);
        ServerTickEvents.START_SERVER_TICK.register(fly::serverTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(fly::serverStopping);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> fly.serverDisconnected(server, handler.player));
        AutoBuilderController autoBuilder = AutoBuilderController.get();
        ClientTickEvents.START_CLIENT_TICK.register(autoBuilder::tick);
        HighlightRenderer renderer = new HighlightRenderer(config, chunkScanner, amethystScanner, lightDebug, blockHighlights);
        renderer.register();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "client"));
        KeyMapping open = bind("open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, category);
        KeyMapping camera = bind("toggle_freecam", GLFW.GLFW_KEY_F6, category);
        KeyMapping builder = bind("open_autobuilder", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping chunks = bind("toggle_chunk_finder", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping bright = bind("toggle_fullbright", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping flight = bind("toggle_fly", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping progress = bind("toggle_progress_bar", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping names = bind("toggle_name_hider", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping hotbar = bind("toggle_hotbar", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping amethyst = bind("toggle_amethyst", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping light = bind("toggle_light_debug", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping storage = bind("toggle_storage_highlight", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping spawner = bind("toggle_spawner_highlight", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping entityEsp = bind("toggle_singleplayer_esp", InputConstants.UNKNOWN.getValue(), category);
        ModuleKeybinds.initialize(camera, builder, chunks, bright, flight, progress, names, hotbar, amethyst, light, storage, spawner, entityEsp);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ProgressTimers.tick(config);
            boolean inputBlocked = ModuleKeybinds.inputBlocked(client);
            while (open.consumeClick()) {
                if (!inputBlocked && client.screen == null) client.setScreen(new DonutScreen(config, this::save));
            }
            if (ModuleKeybinds.consumePress(client, 2)) {
                if (!inputBlocked && client.screen == null && client.player != null) freecam.setEnabled(!freecam.isActive());
            }
            if (ModuleKeybinds.consumePress(client, 3)) {
                if (!inputBlocked && client.screen == null && client.player != null)
                    client.setScreen(new AutoBuilderScreen(null, config, this::save));
            }
            if (ModuleKeybinds.consumePress(client, 4)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.chunkFinder.enabled = !config.chunkFinder.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 5)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.fullbright.enabled = !config.fullbright.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 6)) {
                if (!inputBlocked && client.screen == null && client.player != null) fly.setEnabled(!fly.isActive());
            }
            if (ModuleKeybinds.consumePress(client, 7)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.progressBar.enabled = !config.progressBar.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 8)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.nameHider.enabled = !config.nameHider.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 9)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.hotbar.enabled = !config.hotbar.enabled;
                    save();
                }
            }
            fly.tick(client);
            chunkScanner.tick(client);
            if (ModuleKeybinds.consumePress(client, 10) && !inputBlocked && client.screen == null && client.player != null) {
                config.amethyst.enabled = !config.amethyst.enabled;
                save();
            }
            amethystScanner.tick(client);
            if (ModuleKeybinds.consumePress(client, 11) && !inputBlocked && client.screen == null && client.player != null) {
                config.lightDebug.enabled = !config.lightDebug.enabled; save();
            }
            lightDebug.tick(client);
            if (ModuleKeybinds.consumePress(client, 12) && !inputBlocked && client.screen == null && client.player != null) {
                config.blockHighlights.toggleStorage();
                save();
            }
            if (ModuleKeybinds.consumePress(client, 13) && !inputBlocked && client.screen == null && client.player != null) {
                config.blockHighlights.toggleSpawner();
                save();
            }
            if (ModuleKeybinds.consumePress(client, 14) && !inputBlocked && client.screen == null && client.player != null) {
                config.singleplayerEsp.enabled = !config.singleplayerEsp.enabled;
                save();
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen) {
                Screens.getButtons(screen).add(Button.builder(Component.literal("Drugs V1"), button ->
                        client.setScreen(new DonutScreen(config, this::save)))
                    .bounds(width - 90, height - 28, 82, 20).build());
            }
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "status"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (!config.showHud || client.player == null || client.screen != null || client.options.hideGui) {
                ModuleArrayList.reset();
                return;
            }
            ModuleArrayList.render(graphics, client.font, client.getWindow().getGuiScaledWidth(), config, freecam.isActive());
            AutoBuilderHud.render(graphics, client.font, client.getWindow().getGuiScaledHeight(), config);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "progress_bar"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (!config.progressBar.enabled || client.player == null || client.screen != null || client.options.hideGui) return;
            ProgressBarHud.render(graphics, config, config.progressBar.completedStages);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "pvp_widgets"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.screen != null || client.options.hideGui) return;
            VapeHud.render(graphics, config.vapeHud);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "base_finder_inspect"),
                (graphics, delta) -> BaseFinderPanel.render(graphics, config, chunkScanner));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { chunkScanner.clear(); amethystScanner.clear(); lightDebug.clear(); freecam.disable(); fly.clientDisconnected(handler, client); autoBuilder.disconnect(); });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ProgressTimers.captureAndPause(config);
            fullbright.close();
            freecam.disable();
            fly.disable();
            autoBuilder.disconnect();
            save();
            lightDebug.close();
            renderer.close();
        });
    }

    private KeyMapping bind(String name, int key, KeyMapping.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping("key.donut." + name, InputConstants.Type.KEYSYM, key, category));
    }

    private void save() { store.save(config); }
}

# Drugs V1

A client-side Fabric mod for Minecraft Java 1.21.11. It includes a configurable ClickGUI, module HUD, schematic AutoBuilder, camera-only Freecam, world visuals, and utility modules.

## Features

- **AutoBuilder:** Loads `.litematic` and `.schem` blueprints, previews placement, tracks materials, and builds using ordinary game interactions. Server rules and permissions still apply.
- **Base Finder:** Scans loaded world data for structural clues only while an integrated single-player server is active. Nearby amethyst geodes are weak clues; containers, redstone, hoppers, and machinery increase a transparent heuristic likelihood score. It is not a calibrated probability or proof of a base. Multiplayer packet data is ignored and cached results are cleared.
- **Freecam:** Detaches and moves the camera locally. It does not move the player or send movement packets.
- **Creative Clone:** Press `C` to copy the held stack into an empty hotbar slot. This uses vanilla creative inventory handling and requires Creative mode granted by the world or server; it does not duplicate items in Survival.
- **Entity ESP:** Highlights nearby living mobs and dropped items with depth-tested outlines in multiplayer or single-player. Through-wall outlines are restricted to private integrated single-player worlds and are disabled if the world is opened to LAN.
- **Storage and spawner highlights:** Marks loaded chests in yellow, shulker boxes in purple, barrels in amber, and monster spawners in black, with matching tracers. Optional through-wall ESP modes are available only in private single-player worlds and turn off if the world is opened to LAN; normal highlights and multiplayer tracers remain depth-tested.
- **HUD and ClickGUI:** Includes a global module search, compact/expanded module browser, keybind editing, favorites, animated module list, a customizable accent color, configurable streaming overlays, and optional CPS, keystrokes, armor durability, coordinates, FPS, ping, potion-effect timers, direction/world-clock, mob-only Target HUD, and mob-only Hit Indicators. These are informational client-side overlays; they do not select targets or automate attacks.

## Build

Use JDK 21 or newer and Gradle 9.2.1 or newer:

```sh
gradle build
```

The standalone mod JAR is written to `build/libs/drugs-v1-1.1.0.jar`. Install it with Fabric Loader and Fabric API for Minecraft 1.21.11.

## Lunar Client

This is a standard Fabric mod and does not patch, inject into, or modify Lunar Client files. Lunar compatibility is only expected when Lunar officially supports loading Fabric 1.21.11 mods and the APIs used here; it is not guaranteed across Lunar versions. Use only a documented mod-loading route supported by Lunar.

## Source and licenses

Compatible Donut Client components are adapted from [NotRaindoots/donut-client](https://github.com/NotRaindoots/donut-client). Original Donut code retains its MIT notice. Component-specific ports and assets retain their separate GPL-3.0-only, CC0-1.0, and OFL-1.1 terms; see `src/main/resources/licenses/` and the bundled asset notices. The combined project includes GPL-licensed components, so preserve those notices and provide corresponding source when redistributing the mod.

The project is not affiliated with Mojang, Microsoft, Lunar Client, or Vape.
package dev.donut.client.gui;

import dev.donut.client.config.VapeHudSettings;
import java.util.ArrayDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

/** Original, compact PvP utility overlays; no Vape code or assets are used. */
public final class VapeHud {
    private static final ArrayDeque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static boolean leftWasDown;
    private static boolean rightWasDown;
    private static LivingEntity observedTarget;
    private static float previousTargetHealth;
    private static float lastDamage;
    private static long lastHitAt;

    private VapeHud() { }

    public static void render(GuiGraphics graphics, VapeHudSettings settings) {
        Minecraft client = Minecraft.getInstance();
        if (settings.cps) renderCps(graphics, client);
        else resetClicks();
        if (settings.keystrokes) renderKeystrokes(graphics, client);
        if (settings.armorStatus) renderArmor(graphics, client);
        renderStats(graphics, client, settings);
        if (settings.statusEffects) renderEffects(graphics, client, settings.cps ? 31 : 7);
        LivingEntity target = currentTarget(client);
        if (settings.targetHud) renderTarget(graphics, client, target, settings.cps ? 31 : 7);
        if (settings.hitIndicators) renderHitIndicator(graphics, client, target);
        else resetHitTracking();
    }

    private static void renderCps(GuiGraphics graphics, Minecraft client) {
        long now = System.currentTimeMillis();
        boolean leftDown = GLFW.glfwGetMouseButton(client.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(client.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (leftDown && !leftWasDown) LEFT_CLICKS.addLast(now);
        if (rightDown && !rightWasDown) RIGHT_CLICKS.addLast(now);
        leftWasDown = leftDown;
        rightWasDown = rightDown;
        trim(LEFT_CLICKS, now);
        trim(RIGHT_CLICKS, now);

        String text = "CPS  " + LEFT_CLICKS.size() + "  /  " + RIGHT_CLICKS.size();
        int width = client.font.width(text) + 14;
        panel(graphics, 7, 7, width, 20);
        graphics.drawString(client.font, text, 14, 13, 0xFFF4F6F8, false);
    }

    private static void renderKeystrokes(GuiGraphics graphics, Minecraft client) {
        int x = 8, y = graphics.guiHeight() - 72, size = 20, gap = 2;
        var options = client.options;
        key(graphics, client, x + size + gap, y, size, "W", options.keyUp.isDown());
        key(graphics, client, x, y + size + gap, size, "A", options.keyLeft.isDown());
        key(graphics, client, x + size + gap, y + size + gap, size, "S", options.keyDown.isDown());
        key(graphics, client, x + (size + gap) * 2, y + size + gap, size, "D", options.keyRight.isDown());
        key(graphics, client, x, y + (size + gap) * 2, (size + gap) * 3 - gap, "SPACE", options.keyJump.isDown());
    }

    private static void renderArmor(GuiGraphics graphics, Minecraft client) {
        var player = client.player;
        if (player == null) return;
        int width = 4 * 20 - 2;
        int x = (graphics.guiWidth() - width) / 2;
        int y = graphics.guiHeight() - 51;
        ItemStack[] armor = {
            player.getItemBySlot(EquipmentSlot.HEAD),
            player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.LEGS),
            player.getItemBySlot(EquipmentSlot.FEET)
        };
        for (int slot = 0; slot < armor.length; slot++) {
            int slotX = x + slot * 20;
            panel(graphics, slotX, y, 18, 22);
            ItemStack stack = armor[slot];
            if (stack == null || stack.isEmpty()) continue;
            graphics.renderItem(stack, slotX + 1, y + 1);
            if (stack.isDamageableItem()) {
                double remaining = 1.0 - (double) stack.getDamageValue() / stack.getMaxDamage();
                int barWidth = (int) Math.round(14 * Math.clamp(remaining, 0.0, 1.0));
                int color = remaining > .5 ? 0xFF72D58A : remaining > .2 ? 0xFFFFC45B : 0xFFFF6B6B;
                graphics.fill(slotX + 2, y + 19, slotX + 16, y + 21, 0xFF25282D);
                if (barWidth > 0) graphics.fill(slotX + 2, y + 19, slotX + 2 + barWidth, y + 21, color);
            }
        }
    }

    private static void renderStats(GuiGraphics graphics, Minecraft client, VapeHudSettings settings) {
        if (client.player == null) return;
        int y = 7;
        if (settings.coordinates) {
            var pos = client.player.blockPosition();
            String dimension = client.level == null ? "" : "  " + client.level.dimension().identifier();
            y = rightBadge(graphics, client, "XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ() + dimension, y);
        }
        if (settings.fps) y = rightBadge(graphics, client, client.getFps() + " FPS", y);
        if (settings.ping) {
            var connection = client.getConnection();
            var info = connection == null ? null : connection.getPlayerInfo(client.player.getUUID());
            String text = info == null ? "Ping  --" : "Ping  " + info.getLatency() + " ms";
            rightBadge(graphics, client, text, y);
        }
        if (settings.worldInfo) {
            int worldInfoY = 7;
            if (settings.coordinates) worldInfoY += 23;
            if (settings.fps) worldInfoY += 23;
            if (settings.ping) worldInfoY += 23;
            String direction = client.player.getDirection().getName().toUpperCase(Locale.ROOT);
            long dayTime = client.level == null ? 0 : Math.floorMod(client.level.getDayTime(), 24000);
            int hours = (int) ((dayTime / 1000 + 6) % 24);
            int minutes = (int) (dayTime % 1000 * 60 / 1000);
            rightBadge(graphics, client, direction + "  " + String.format(Locale.ROOT, "%02d:%02d", hours, minutes), worldInfoY);
        }
    }

    private static void renderEffects(GuiGraphics graphics, Minecraft client, int y) {
        var player = client.player;
        if (player == null) return;
        for (var effect : player.getActiveEffects()) {
            String name = effect.getEffect().value().getDisplayName().getString();
            int seconds = Math.max(0, effect.getDuration() / 20);
            String amplifier = effect.getAmplifier() > 0 ? " " + roman(effect.getAmplifier() + 1) : "";
            String text = name + amplifier + "  " + seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
            int width = Math.min(graphics.guiWidth() - 14, client.font.width(text) + 14);
            panel(graphics, 7, y, width, 20);
            graphics.drawString(client.font, text, 14, y + 6, effectColor(effect.getEffect().value().getColor()), false);
            y += 23;
        }
    }

    private static LivingEntity currentTarget(Minecraft client) {
        return client.crosshairPickEntity instanceof LivingEntity living && !(living instanceof Player)
                && living.isAlive() ? living : null;
    }

    private static void renderTarget(GuiGraphics graphics, Minecraft client, LivingEntity target, int y) {
        if (target == null) return;
        float maxHealth = Math.max(1, target.getMaxHealth());
        float health = Math.clamp(target.getHealth(), 0, maxHealth);
        String title = target.getName().getString() + "  " + String.format(Locale.ROOT, "%.1f", health)
                + "/" + String.format(Locale.ROOT, "%.1f", maxHealth);
        int width = Math.min(graphics.guiWidth() - 16, Math.max(120, client.font.width(title) + 18));
        int x = (graphics.guiWidth() - width) / 2;
        panel(graphics, x, y, width, 32);
        graphics.drawString(client.font, title, x + 8, y + 6, 0xFFF4F6F8, false);
        graphics.fill(x + 8, y + 22, x + width - 8, y + 27, 0xFF25282D);
        int healthWidth = (int) Math.round((width - 16) * health / maxHealth);
        if (healthWidth > 0) graphics.fill(x + 8, y + 22, x + 8 + healthWidth, y + 27, 0xFFE45C66);
    }

    private static void renderHitIndicator(GuiGraphics graphics, Minecraft client, LivingEntity target) {
        long now = System.currentTimeMillis();
        if (target != observedTarget) {
            observedTarget = target;
            previousTargetHealth = target == null ? 0 : target.getHealth();
        } else if (target != null) {
            float health = target.getHealth();
            if (previousTargetHealth - health > .01f) {
                lastDamage = previousTargetHealth - health;
                lastHitAt = now;
            }
            previousTargetHealth = health;
        }
        if (now - lastHitAt > 650 || lastDamage <= 0) return;
        String label = "HIT  -" + String.format(Locale.ROOT, "%.1f", lastDamage);
        int width = client.font.width(label) + 16;
        int x = (graphics.guiWidth() - width) / 2;
        int y = graphics.guiHeight() / 2 + 20;
        panel(graphics, x, y, width, 21);
        graphics.drawString(client.font, label, x + 8, y + 6, 0xFFFF7777, false);
    }

    private static void resetHitTracking() {
        observedTarget = null;
        previousTargetHealth = 0;
        lastDamage = 0;
        lastHitAt = 0;
    }

    private static String roman(int level) {
        return switch (Math.clamp(level, 1, 5)) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "V";
        };
    }

    private static int effectColor(int rgb) { return 0xFF000000 | rgb & 0xFFFFFF; }

    private static int rightBadge(GuiGraphics graphics, Minecraft client, String text, int y) {
        int width = Math.min(graphics.guiWidth() - 8, client.font.width(text) + 14);
        int x = graphics.guiWidth() - width - 7;
        panel(graphics, x, y, width, 20);
        graphics.drawString(client.font, text, x + 7, y + 6, 0xFFF4F6F8, false);
        return y + 23;
    }

    private static void key(GuiGraphics graphics, Minecraft client, int x, int y, int width, String label, boolean pressed) {
        panel(graphics, x, y, width, 20);
        if (pressed) graphics.fill(x + 1, y + 1, x + width - 1, y + 19, 0xFF386C62);
        int textWidth = client.font.width(label);
        graphics.drawString(client.font, label, x + (width - textWidth) / 2, y + 6,
                pressed ? 0xFFFFFFFF : 0xFFD8DDE2, false);
    }

    private static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xC5161B20);
        graphics.fill(x, y, x + width, y + 1, 0xAAFFFFFF);
    }

    private static void trim(ArrayDeque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() >= 1000) clicks.removeFirst();
    }

    private static void resetClicks() {
        LEFT_CLICKS.clear();
        RIGHT_CLICKS.clear();
        leftWasDown = false;
        rightWasDown = false;
    }
}
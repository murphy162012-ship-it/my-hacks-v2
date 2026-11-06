package dev.donut.client.config;

public final class SingleplayerEspSettings {
    public transient boolean enabled;
    public boolean favorite;
    public boolean throughWalls;
    public int range = 48;

    public void sanitize() {
        range = Math.clamp(range, 8, 96);
    }
}
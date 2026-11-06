package dev.donut.client.config;

public final class BlockHighlightSettings {
    public boolean storageEnabled;
    public boolean spawnerEnabled;
    public transient boolean storageEsp;
    public transient boolean spawnerEsp;
    public boolean storageFavorite;
    public boolean spawnerFavorite;
    public int range = 48;

    public boolean storageActive() { return storageEnabled || storageEsp; }
    public boolean spawnerActive() { return spawnerEnabled || spawnerEsp; }

    public void toggleStorage() {
        if (storageActive()) {
            storageEnabled = false;
            storageEsp = false;
        } else storageEnabled = true;
    }

    public void toggleSpawner() {
        if (spawnerActive()) {
            spawnerEnabled = false;
            spawnerEsp = false;
        } else spawnerEnabled = true;
    }

    public void sanitize() {
        range = Math.clamp(range, 8, 96);
    }
}
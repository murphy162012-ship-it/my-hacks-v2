package dev.donut.client.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BlockHighlightSettingsTest {
    @Test void storageEspCountsAsAnActiveHighlight() {
        var settings = new BlockHighlightSettings();
        settings.storageEsp = true;

        assertTrue(settings.storageActive());
        settings.toggleStorage();
        assertFalse(settings.storageActive());
        assertFalse(settings.storageEsp);
    }

    @Test void spawnerEspCountsAsAnActiveHighlight() {
        var settings = new BlockHighlightSettings();
        settings.spawnerEsp = true;

        assertTrue(settings.spawnerActive());
        settings.toggleSpawner();
        assertFalse(settings.spawnerActive());
        assertFalse(settings.spawnerEsp);
    }

    @Test void togglingFromOffEnablesTheVisibleHighlight() {
        var settings = new BlockHighlightSettings();

        settings.toggleStorage();
        settings.toggleSpawner();

        assertTrue(settings.storageEnabled);
        assertTrue(settings.spawnerEnabled);
        assertTrue(settings.storageActive());
        assertTrue(settings.spawnerActive());
    }
}
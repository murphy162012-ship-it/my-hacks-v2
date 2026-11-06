package dev.donut.client.chunkfinder;

import java.util.List;

public final class BaseLikelihood {
    private BaseLikelihood() { }

    public static Estimate estimate(List<SuspectObjects.Marker> markers) {
        int score = 0;
        int geodes = 0;
        int buildClues = 0;
        for (var marker : markers) {
            switch (marker.category()) {
                case GEODES -> { score += 6; geodes++; }
                case CONTAINERS -> { score += 14; buildClues++; }
                case ENDER_STORAGE -> { score += 18; buildClues++; }
                case HOPPERS -> { score += 10; buildClues++; }
                case MACHINES -> { score += 8; buildClues++; }
                case REDSTONE -> { score += 10; buildClues++; }
                case SPAWNERS, DISPLAYS -> score += 5;
                case ANOMALIES -> score += 4;
                case GROWTH -> { }
            }
        }
        if (geodes > 0 && buildClues > 0) score += 10;
        score = Math.clamp(score, 0, 100);
        Level level = score < 15 ? Level.LOW : score < 35 ? Level.POSSIBLE
                : score < 60 ? Level.LIKELY : Level.HIGH;
        String context = geodes == 0 ? "no nearby geode clue"
                : buildClues == 0 ? "natural geode alone is weak evidence"
                : "geode plus nearby build clues";
        return new Estimate(level, score, context);
    }

    public enum Level { LOW, POSSIBLE, LIKELY, HIGH }
    public record Estimate(Level level, int score, String context) { }
}
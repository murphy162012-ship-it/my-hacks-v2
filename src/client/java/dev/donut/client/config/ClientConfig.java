package dev.donut.client.config;

public final class ClientConfig {
    public int schemaVersion = 1;
    public FreecamSettings freecam = new FreecamSettings();
    public AutoBuilderSettings autoBuilder = new AutoBuilderSettings();
    public ChunkFinderSettings chunkFinder = new ChunkFinderSettings();
    public BlockHighlightSettings blockHighlights = new BlockHighlightSettings();
    public SingleplayerEspSettings singleplayerEsp = new SingleplayerEspSettings();
    public LightDebugSettings lightDebug = new LightDebugSettings();
    public AmethystSettings amethyst = new AmethystSettings();
    public FullbrightSettings fullbright = new FullbrightSettings();
    public FlySettings fly = new FlySettings();
    public ProgressBarSettings progressBar = new ProgressBarSettings();
    public NameHiderSettings nameHider = new NameHiderSettings();
    public HotbarSettings hotbar = new HotbarSettings();
    public VapeHudSettings vapeHud = new VapeHudSettings();
    public int accentColor = 0x56DCCB;
    public ClickGuiStyle clickGuiStyle = new ClickGuiStyle();
    public boolean clickGuiParticles = true;
    public boolean showHud = true;


    public void sanitize() {
        if (clickGuiStyle == null) clickGuiStyle = new ClickGuiStyle();
        clickGuiStyle.sanitize();
        if (freecam == null) freecam = new FreecamSettings();
        if (autoBuilder == null) autoBuilder = new AutoBuilderSettings();
        if (chunkFinder == null) chunkFinder = new ChunkFinderSettings();
        if (blockHighlights == null) blockHighlights = new BlockHighlightSettings();
        if (singleplayerEsp == null) singleplayerEsp = new SingleplayerEspSettings();
        if (amethyst == null) amethyst = new AmethystSettings();
        if (fullbright == null) fullbright = new FullbrightSettings();
        if (fly == null) fly = new FlySettings();
        if (progressBar == null) progressBar = new ProgressBarSettings();
        if (nameHider == null) nameHider = new NameHiderSettings();
        if (hotbar == null) hotbar = new HotbarSettings();
        if (vapeHud == null) vapeHud = new VapeHudSettings();
        freecam.sanitize();
        autoBuilder.sanitize();
        chunkFinder.sanitize();
        blockHighlights.sanitize();
        singleplayerEsp.sanitize();
        amethyst.sanitize();
        if (lightDebug == null) lightDebug = new LightDebugSettings();
        lightDebug.sanitize();
        progressBar.sanitize();
        hotbar.sanitize();
        vapeHud.sanitize();
        accentColor &= 0xFFFFFF;
    }
}

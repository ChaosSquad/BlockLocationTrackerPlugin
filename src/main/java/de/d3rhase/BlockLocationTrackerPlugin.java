package de.d3rhase;

import org.bukkit.plugin.java.JavaPlugin;

public class BlockLocationTrackerPlugin extends JavaPlugin {
    private BlockEventListener eventListener;

    @Override
    public void onEnable() {
        // Debug
        getLogger().info("Data folder: " + getDataFolder().getAbsolutePath());
        // Initialize and register the event listener
        eventListener = new BlockEventListener(this);

        getServer().getPluginManager().registerEvents(eventListener, this);

        eventListener.spawnHighlightedBlocks();

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> eventListener.saveBlockList(), 80, 2400L);
    }

    @Override
    public void onDisable() {
        eventListener.removeHighlightedBlocks();
        eventListener.saveBlockList();
    }
}

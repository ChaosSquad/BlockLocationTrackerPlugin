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

        // Start a scheduled task to save the block list every 5 minutes (6000 ticks)
        // Minecraft runs at 20 ticks per second
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                eventListener.saveBlockList();
            }
        }, 0, 6000L); // Adjust the interval as needed
    }

    @Override
    public void onDisable() {
        // Make sure to save the list when the plugin is disabled
        eventListener.saveBlockList();
    }
}
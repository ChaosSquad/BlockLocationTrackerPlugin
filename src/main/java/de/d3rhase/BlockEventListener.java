package de.d3rhase;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockEventListener implements Listener {

    private JavaPlugin plugin;
    private JSONArray blockList;
    private File blockListFile;
    private final List<BlockDisplay> highlightedBlocks;
    private final static String fileName = "interactionAllowed.json";
    private boolean highlightSelectedBlocks;


    public BlockEventListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blockListFile = new File(plugin.getDataFolder(), fileName);
        this.highlightedBlocks= new ArrayList<>();
        this.highlightSelectedBlocks = false;
        loadBlockList();
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        // Debug
        //System.out.println("Bl vor:" + blockList.toJSONString());

        if (item.getType() == Material.CARROT_ON_A_STICK && event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Block targetBlock = player.getTargetBlockExact(5); // Get the exact block up to 5 blocks away

            if (targetBlock != null && !targetBlock.getType().isAir()) {

                JSONObject blockCoords = new JSONObject();
                blockCoords.put("x", targetBlock.getX());
                blockCoords.put("y", targetBlock.getY());
                blockCoords.put("z", targetBlock.getZ());

                if (!this.blockIsOnList(blockCoords)) {

                    blockList.add(blockCoords);

                    if (this.highlightSelectedBlocks) {
                        spawnHighlightedBlock(targetBlock.getLocation());
                    }

                    String message = (ChatColor.GREEN + "Block added to the list");
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1.0F, 1.0F);

                } else {
                    blockList.removeIf(obj -> {
                        JSONObject objCoords = (JSONObject) obj;
                        boolean xMatches = objCoords.get("x").toString().equals(blockCoords.get("x").toString());
                        boolean yMatches = objCoords.get("y").toString().equals(blockCoords.get("y").toString());
                        boolean zMatches = objCoords.get("z").toString().equals(blockCoords.get("z").toString());
                        return xMatches && yMatches && zMatches;
                    });

                    removeHighlightedBlock(targetBlock.getLocation());

                    String message = (ChatColor.RED + "Block removed from the list");
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.AMBIENT, 1.0F, 1.0F);
                }
            }
            event.setCancelled(true);
        }
        // Debug
        // System.out.println("Bl nach:" + blockList.toJSONString());
    }

    private boolean blockIsOnList(JSONObject targetBlock){
        boolean xMatches, yMatches, zMatches;

        for (Object o : blockList) {
            JSONObject listBlock = (JSONObject) o;
            xMatches = listBlock.get("x").toString().equals(targetBlock.get("x").toString());
            yMatches = listBlock.get("y").toString().equals(targetBlock.get("y").toString());
            zMatches = listBlock.get("z").toString().equals(targetBlock.get("z").toString());
            if (xMatches && yMatches && zMatches){
                return true;
            }
        }

        return false;
    }

    private void spawnHighlightedBlock(Location location) {
        BlockDisplay blockDisplay = Objects.requireNonNull(location.getWorld()).spawn(location.clone(), BlockDisplay.class);
        blockDisplay.setBlock(Material.GLASS.createBlockData());
        blockDisplay.setGlowing(true);
        highlightedBlocks.add(blockDisplay);
    }

    private void removeHighlightedBlock(Location location) {
        highlightedBlocks.removeIf(blockDisplay -> {
            if (blockDisplay.getLocation().getBlockX() == location.getBlockX() &&
                    blockDisplay.getLocation().getBlockY() == location.getBlockY() &&
                    blockDisplay.getLocation().getBlockZ() == location.getBlockZ()) {
                blockDisplay.remove();
                return true;
            }
            return false;
        });
    }

    public void spawnHighlightedBlocks() {
        blockList.forEach(obj -> {
            JSONObject blockCoords = (JSONObject) obj;
            World world = plugin.getServer().getWorld("world");
            Location location = new Location(world,
                    Double.parseDouble(blockCoords.get("x").toString()),
                    Double.parseDouble(blockCoords.get("y").toString()),
                    Double.parseDouble(blockCoords.get("z").toString()));

            spawnHighlightedBlock(location);
        });
    }

    public void removeHighlightedBlocks() {
        highlightedBlocks.forEach(BlockDisplay::remove);
        highlightedBlocks.clear();
    }

    public void loadBlockList() {
        try {
            if (!blockListFile.exists()) {
                blockList = new JSONArray();
                return;
            }
            FileReader reader = new FileReader(blockListFile);
            blockList = (JSONArray) new JSONParser().parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
            blockList = new JSONArray();
        }
    }

    public void saveBlockList() {
        try {
            FileWriter file = new FileWriter(blockListFile);
            file.write(blockList.toJSONString());
            file.flush();
            file.close();
            System.out.println("BlockListSaved");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHighlightSelectedBlocks() {
        return highlightSelectedBlocks;
    }

    public void setHighlightSelectedBlocks(boolean highlightSelectedBlocks) {
        this.highlightSelectedBlocks = highlightSelectedBlocks;
    }
}

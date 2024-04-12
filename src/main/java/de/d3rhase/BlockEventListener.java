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
    private final static String fileName = "selectedBlocks.json";
    private boolean highlightSelectedBlocks;
    public static final String BLOCK_DISPLAY_TAG = "BlockLocationTrackerBlockDisplay";
    public static final String BLOCK_DATA_PLACEHOLDER_X = "x";
    public static final String BLOCK_DATA_PLACEHOLDER_Y = "y";
    public static final String BLOCK_DATA_PLACEHOLDER_Z = "z";
    public static final String BLOCK_DATA_PLACEHOLDER_MATERIAL = "material";
    public static final String SERVER_MAP_FOLDER_NAME = "world";


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
                blockCoords.put(BLOCK_DATA_PLACEHOLDER_X, targetBlock.getX());
                blockCoords.put(BLOCK_DATA_PLACEHOLDER_Y, targetBlock.getY());
                blockCoords.put(BLOCK_DATA_PLACEHOLDER_Z, targetBlock.getZ());
                blockCoords.put(BLOCK_DATA_PLACEHOLDER_MATERIAL, targetBlock.getBlockData().getMaterial().toString());

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
                        boolean xMatches = objCoords.get(BLOCK_DATA_PLACEHOLDER_X).toString().equals(blockCoords.get(BLOCK_DATA_PLACEHOLDER_X).toString());
                        boolean yMatches = objCoords.get(BLOCK_DATA_PLACEHOLDER_Y).toString().equals(blockCoords.get(BLOCK_DATA_PLACEHOLDER_Y).toString());
                        boolean zMatches = objCoords.get(BLOCK_DATA_PLACEHOLDER_Z).toString().equals(blockCoords.get(BLOCK_DATA_PLACEHOLDER_Z).toString());
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

        for (Object blockListEntry : blockList) {
            JSONObject blockData = (JSONObject) blockListEntry;
            xMatches = blockData.get(BLOCK_DATA_PLACEHOLDER_X).toString().equals(targetBlock.get(BLOCK_DATA_PLACEHOLDER_X).toString());
            yMatches = blockData.get(BLOCK_DATA_PLACEHOLDER_Y).toString().equals(targetBlock.get(BLOCK_DATA_PLACEHOLDER_Y).toString());
            zMatches = blockData.get(BLOCK_DATA_PLACEHOLDER_Z).toString().equals(targetBlock.get(BLOCK_DATA_PLACEHOLDER_Z).toString());
            if (xMatches && yMatches && zMatches) {
                return true;
            }
        }

        return false;
    }

    private void spawnHighlightedBlock(Location location) {
        BlockDisplay blockDisplay = Objects.requireNonNull(location.getWorld()).spawn(location.clone(), BlockDisplay.class);
        blockDisplay.setBlock(Material.GLASS.createBlockData());
        blockDisplay.setGlowing(true);
        blockDisplay.addScoreboardTag(BLOCK_DISPLAY_TAG);
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
            JSONObject blockData = (JSONObject) obj;
            World world = plugin.getServer().getWorld(SERVER_MAP_FOLDER_NAME);
            Location location = new Location(world,
                    Double.parseDouble(blockData.get(BLOCK_DATA_PLACEHOLDER_X).toString()),
                    Double.parseDouble(blockData.get(BLOCK_DATA_PLACEHOLDER_Y).toString()),
                    Double.parseDouble(blockData.get(BLOCK_DATA_PLACEHOLDER_Z).toString()));

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

    /**
     * This method updates the list data fields for every blocks, so that older lists with only coordinates
     * also get all information that are needed in this version
     */
    public void informationUpdateBlockList(){
        for (int i = 0; i < blockList.size(); i++){
            JSONObject blockListEntry = (JSONObject) blockList.get(i);

            double x, y, z;

            x = Double.parseDouble(blockListEntry.get(BLOCK_DATA_PLACEHOLDER_X).toString());
            y = Double.parseDouble(blockListEntry.get(BLOCK_DATA_PLACEHOLDER_Y).toString());
            z = Double.parseDouble(blockListEntry.get(BLOCK_DATA_PLACEHOLDER_Z).toString());

            World world = plugin.getServer().getWorld(SERVER_MAP_FOLDER_NAME);
            Location location = new Location(world, x, y, z);

            boolean hasMaterialInfo = blockListEntry.containsKey(BLOCK_DATA_PLACEHOLDER_MATERIAL);
            String material = location.getBlock().getBlockData().getMaterial().toString();
            if (!hasMaterialInfo) blockListEntry.put(BLOCK_DATA_PLACEHOLDER_MATERIAL, material);

        }
    }

    public boolean isHighlightSelectedBlocks() {
        return highlightSelectedBlocks;
    }

    public void setHighlightSelectedBlocks(boolean highlightSelectedBlocks) {
        this.highlightSelectedBlocks = highlightSelectedBlocks;
    }
}

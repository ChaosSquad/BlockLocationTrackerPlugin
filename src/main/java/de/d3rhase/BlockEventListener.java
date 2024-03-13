package de.d3rhase;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;

public class BlockEventListener implements Listener {

    private JavaPlugin plugin;
    private JSONArray blockList;
    private File blockListFile;

    public BlockEventListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blockListFile = new File(plugin.getDataFolder(), "blockList.json");
        loadBlockList();
    }

    private void loadBlockList() {
        if (!blockListFile.exists()) {
            blockList = new JSONArray();
            return;
        }
        try (FileReader reader = new FileReader(blockListFile)) {
            blockList = (JSONArray) new org.json.simple.parser.JSONParser().parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
            blockList = new JSONArray();
        }
    }

    void saveBlockList() {
        try (FileWriter file = new FileWriter(blockListFile)) {
            file.write(blockList.toJSONString());
            file.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.CARROT_ON_A_STICK && event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            System.out.println(blockList.toJSONString());
            Block targetBlock = player.getTargetBlockExact(5); // Get the exact block up to 5 blocks away
            if (targetBlock != null) {
                JSONObject blockCoords = new JSONObject();
                blockCoords.put("x", targetBlock.getX());
                blockCoords.put("y", targetBlock.getY());
                blockCoords.put("z", targetBlock.getZ());

                // Check if the block is already in the list
                if (blockList.contains(blockCoords)) {
                    // If it is, remove it
                    blockList.remove(blockCoords);
                    saveBlockList();
                    player.sendMessage("Block removed from the list.");
                } else {
                    // If it's not, add it to the list
                    blockList.add(blockCoords);
                    saveBlockList();
                    player.sendMessage("Block added to the list.");
                }
            }
        }
        System.out.println(blockList.toJSONString());
    }

    public void highlightBlocks() {
        for (Object obj : blockList) {
            JSONObject blockCoords = (JSONObject) obj;
            World world = Bukkit.getWorld("world"); // Replace "worldName" with your actual world name
            double x = ((Long) blockCoords.get("x")).doubleValue();
            double y = ((Long) blockCoords.get("y")).doubleValue();
            double z = ((Long) blockCoords.get("z")).doubleValue();

            Location location = new Location(world, x + 0.5, y + 0.5, z + 0.5); // Center the falling block
            FallingBlock fallingBlock = world.spawnFallingBlock(location, Material.AIR.createBlockData()); // Use BARRIER for invisible blocks

            fallingBlock.setDropItem(false); // Prevent the block from dropping items
            fallingBlock.setGravity(false); // Prevent the block from falling
            fallingBlock.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, false, false)); // Apply glowing effect
            fallingBlock.setSilent(true); // Make the falling block silent
            fallingBlock.setInvulnerable(true); // Make the falling block invulnerable
        }
    }


}
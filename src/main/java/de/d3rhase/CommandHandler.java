package de.d3rhase;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final BlockEventListener listener;

    public CommandHandler(BlockEventListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !args[0].equalsIgnoreCase("showblocks")) {
            sender.sendMessage("Usage: /blt showblocks <true|false>");
            return true;
        }

        boolean show = Boolean.parseBoolean(args[1]);
        listener.setHighlightSelectedBlocks(show);

        if (show) {
            listener.removeHighlightedBlocks(); // Clean up before showing new blocks
            listener.spawnHighlightedBlocks();
            sender.sendMessage("Block highlighting enabled.");
        } else {
            listener.removeHighlightedBlocks();
            sender.sendMessage("Block highlighting disabled.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("blt") && args.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("showblocks");
            return commands;
        } else if (command.getName().equalsIgnoreCase("blt") && args.length == 2 && args[0].equalsIgnoreCase("showblocks")) {
            List<String> states = new ArrayList<>();
            states.add("true");
            states.add("false");
            return states;
        }
        return null; // Return null to default to the command executor's tab completion
    }
}

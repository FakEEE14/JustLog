package fake.justLog.commands;

import fake.justLog.JustLog;
import fake.justLog.manager.ConfigManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static fake.justLog.manager.TextManager.doDebug;

public class CommandManager implements CommandExecutor {

    private final JustLog plugin;
    private final ConfigManager configManager;

    public CommandManager(JustLog plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;

        plugin.getCommand("justlog").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("justlog.admin")) {
            sender.sendMessage("No permission!");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /justlog <filename/player> <message>");
            return true;
        }

        String filename = args[0];
        String message = String.join(" ", args).substring(filename.length() + 1);

        if(plugin.isPapiActive()){
            message = processNestedPlaceholders(filename, message);
            doDebug("Papi is Active",4);
        }
        doDebug("\nisPapiActive="+plugin.isPapiActive(),4);

        if (filename.contains("/")) {
            String[] parts = filename.split("/");
            for (String part : parts) {
                configManager.SaveInFile(part, message);
            }
        } else {
            configManager.SaveInFile(filename, message);
        }
        return false;
    }
    private String processNestedPlaceholders(String playerName, String identifier) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        while (identifier.contains("{") && identifier.contains("}")) {
            int startIndex = identifier.lastIndexOf("{");
            int endIndex = identifier.indexOf("}", startIndex);
            if (startIndex != -1 && endIndex != -1) {
                String placeholderName = identifier.substring(startIndex + 1, endIndex);
                String placeholderValue = PlaceholderAPI.setPlaceholders(player, "%" + placeholderName + "%");
                identifier = identifier.substring(0, startIndex) + placeholderValue + identifier.substring(endIndex + 1);
            } else {
                break;
            }
        }
        return PlaceholderAPI.setPlaceholders(player, identifier);
    }
}
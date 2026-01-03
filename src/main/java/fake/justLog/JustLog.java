package fake.justLog;

import fake.justLog.commands.CommandManager;
import fake.justLog.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static fake.justLog.manager.TextManager.doDebug;

public final class JustLog extends JavaPlugin {

    private ConfigManager configManager;
    private CommandManager commandManager;
    boolean papiActive = false;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        commandManager = new CommandManager(this, configManager);

        Bukkit.getScheduler().runTask(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                papiActive = true;
                doDebug("&aPapi Registered", 1);
            }
        });
    }

    public boolean isPapiActive() {return papiActive;}

    @Override
    public void onDisable() {
        doDebug("&cDisabled!", 1);
    }
}

package zip.sodium.marketplace;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import zip.sodium.marketplace.command.CommandRegistrar;
import zip.sodium.marketplace.config.ConfigHandler;
import zip.sodium.marketplace.database.DatabaseHolder;
import zip.sodium.marketplace.listener.ListenerHandler;

import java.util.logging.Logger;

public final class Entrypoint extends JavaPlugin {
    private static Entrypoint instance = null;

    public static Logger logger() {
        if (instance == null)
            throw new AssertionError("Plugin not initialized");

        return instance.getLogger();
    }

    public static void disable() {
        Bukkit.getPluginManager().disablePlugin(instance);
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigHandler.acknowledge(this);
        ListenerHandler.acknowledge(this);

        DatabaseHolder.acknowledge();
        CommandRegistrar.acknowledge();
    }

    @Override
    public void onDisable() {
        DatabaseHolder.cleanup();
    }
}

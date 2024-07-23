package zip.sodium.marketplace;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import zip.sodium.marketplace.command.CommandRegistrar;
import zip.sodium.marketplace.config.ConfigHandler;
import zip.sodium.marketplace.database.DatabaseHolder;
import zip.sodium.marketplace.listener.ListenerHandler;
import zip.sodium.marketplace.vault.VaultProvider;
import zip.sodium.marketplace.webhook.WebhookProvider;

import java.util.logging.Logger;

public final class Entrypoint extends JavaPlugin {
    private static Entrypoint instance = null;

    public static Logger logger() {
        if (instance == null)
            throw new AssertionError("Plugin not initialized");

        return instance.getLogger();
    }

    private static void disable(final Plugin plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    public static void disable() {
        disable(instance);
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Vault") == null
                || !VaultProvider.acknowledge()) {
            getLogger().severe("Vault dependency not found or not setup correctly!");
            disable(this);

            return;
        }

        instance = this;

        ConfigHandler.acknowledge(this);
        ListenerHandler.acknowledge(this);

        DatabaseHolder.acknowledge();
        CommandRegistrar.acknowledge();
        WebhookProvider.acknowledge();
    }

    @Override
    public void onDisable() {
        DatabaseHolder.cleanup();
        WebhookProvider.cleanup();
    }
}

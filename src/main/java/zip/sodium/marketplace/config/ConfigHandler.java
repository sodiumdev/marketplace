package zip.sodium.marketplace.config;

import org.bukkit.plugin.Plugin;
import zip.sodium.marketplace.config.builtin.*;

public final class ConfigHandler {
    private ConfigHandler() {}

    public static void acknowledge(final Plugin plugin) {
        DatabaseConfig.saveDefaults(plugin, "database.yml");
        MessageConfig.saveDefaults(plugin, "messages.yml");
        PermissionConfig.saveDefaults(plugin, "permissions.yml");
        GuiConfig.saveDefaults(plugin, "gui.yml");
        WebhookConfig.saveDefaults(plugin, "webhook.yml");
    }
}

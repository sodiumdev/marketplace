package zip.sodium.marketplace.config.builtin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import zip.sodium.marketplace.config.EnumConfig;

public enum DatabaseConfig implements EnumConfig {
    CONNECTION_STRING(
            "REPLACE ME"
    ),
    DATABASE_NAME(
            "MarketplaceData"
    ),
    TRANSACTIONS_COLLECTION_NAME(
            "Transactions"
    ),
    ITEMS_COLLECTION_NAME(
            "Items"
    );

    private static YamlConfiguration configFile;
    public static void saveDefaults(final Plugin plugin, final String fileName) {
        EnumConfig.trySaveDefaults(plugin, fileName, values());

        configFile = EnumConfig.tryGetConfigFile(plugin, fileName);
    }

    private Object cache = null;
    private final Object defaultValue;

    DatabaseConfig(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String get() {
        return get(FileConfiguration::getString);
    }

    @Override
    public Object cache() {
        return cache;
    }

    @Override
    public void setCache(final Object cache) {
        this.cache = cache;
    }

    @Override
    public Object defaultValue() {
        return defaultValue;
    }

    @Override
    public FileConfiguration ymlConfiguration() {
        return configFile;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}

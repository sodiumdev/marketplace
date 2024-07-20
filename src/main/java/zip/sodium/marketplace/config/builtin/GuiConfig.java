package zip.sodium.marketplace.config.builtin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.config.EnumConfig;

public enum GuiConfig implements EnumConfig {
    MARKETPLACE_GUI_TITLE(
            "<gray>Marketplace"
    ),
    LOADING_SKULL_NAME(
            "<!i><white>Loading..."
    ),
    REFRESH_ITEM_NAME(
            "<!i><green>Refresh"
    );

    private static YamlConfiguration configFile;
    public static void saveDefaults(final Plugin plugin, final String fileName) {
        EnumConfig.trySaveDefaults(plugin, fileName, values());

        configFile = EnumConfig.tryGetConfigFile(plugin, fileName);
    }

    private Object cache = null;
    private final Object defaultValue;

    GuiConfig(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String get() {
        return LegacyComponentSerializer.legacySection().serialize(get(TagResolver.empty()));
    }

    public @NotNull Component get(final TagResolver... resolver) {
        return MiniMessage.miniMessage().deserialize(
                get(FileConfiguration::getString),
                resolver
        );
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
package zip.sodium.marketplace.config.builtin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import zip.sodium.marketplace.config.EnumConfig;

public enum WebhookConfig implements EnumConfig {
    URL(
            ""
    ),
    EMBED_COLOR(
            "00FF00"
    ),
    PURCHASE_LOG(
            "Listing from `<seller>` with item `<item>` bought by `<buyer>` for $<price>"
    );

    private static YamlConfiguration configFile;
    public static void saveDefaults(final Plugin plugin, final String fileName) {
        EnumConfig.trySaveDefaults(plugin, fileName, values());

        configFile = EnumConfig.tryGetConfigFile(plugin, fileName);
    }

    private Object cache = null;
    private final Object defaultValue;

    WebhookConfig(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getHex() {
        try {
            return Integer.parseInt(
                    get(),
                    16
            );
        } catch (final NumberFormatException e) {
            return Integer.parseInt(
                    defaultValue.toString(),
                    16
            );
        }
    }

    public String getResolved(final TagResolver... resolver) {
        return PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(
                        get(FileConfiguration::getString),
                        resolver
                )
        );
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

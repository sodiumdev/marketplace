package zip.sodium.marketplace.config.builtin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.config.EnumConfig;
import zip.sodium.marketplace.config.data.MessageType;

public enum MessageConfig implements EnumConfig {
    EXPECTED_LESS_ARGUMENTS(
            "<red>Expected less arguments!",
            MessageType.FAIL
    ),
    EXPECTED_ARGUMENT(
            "<red>Expected <gray>`<green><argument><gray>`<red> argument!",
            MessageType.FAIL
    );

    private static YamlConfiguration configFile;
    public static void saveDefaults(final Plugin plugin, final String fileName) {
        EnumConfig.trySaveDefaults(plugin, fileName, EnumConfig.generateDefaultConfigFileContent(values()));

        configFile = EnumConfig.tryGetConfigFile(plugin, fileName);
    }

    private Object cache = null;
    private final Object defaultValue;

    private final MessageType messageType;

    MessageConfig(final String defaultValue, final MessageType messageType) {
        this.defaultValue = defaultValue;
        this.messageType = messageType;
    }

    public MessageType messageType() {
        return messageType;
    }

    public @NotNull Component get() {
        return MiniMessage.miniMessage().deserialize(
                get(FileConfiguration::getString),
                TagResolver.builder().build()
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

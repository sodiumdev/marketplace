package zip.sodium.marketplace.config.builtin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.config.EnumConfig;
import zip.sodium.marketplace.data.message.MessageType;

public enum MessageConfig implements EnumConfig {
    EXPECTED_LESS_ARGUMENTS(
            "<red>Expected less arguments!",
            MessageType.FAIL
    ),
    EXPECTED_ARGUMENT(
            "<red>Expected <gray>`<green><argument><gray>`<red> argument!",
            MessageType.FAIL
    ),
    PLAYER_REQUIRED(
            "<red>Only a player can execute this command!",
            MessageType.FAIL
    ),
    INVALID_ARGUMENT(
            "<red>Expected any of <gray>`<green><expected><gray>`<red> for argument <gray>`<green><argument><gray>`<red> but got <gray>`<green><got><gray>`<red>",
            MessageType.FAIL
    ),
    INTEGER_OUT_OF_BOUNDS(
            "<red>Integer argument with name <gray>`<green><argument><gray>`<red> should be in the range of <min>-<max>",
            MessageType.FAIL
    ),
    INSUFFICIENT_PERMISSIONS(
            "<red>Insufficient permissions! To execute this command you need to have the <gray>`<green><permission><gray>`<red> permission.",
            MessageType.FAIL
    ),
    INVALID_ITEM(
            "<red>Cannot enlist this item on marketplace! Maybe you don't have anything on your main hand?",
            MessageType.FAIL
    ),
    ENLISTING_ITEM(
            "<white>Enlisting item...",
            MessageType.PASS
    ),
    ERROR_ENLISTING_ITEM(
            "<red>Error enlisting item on marketplace!",
            MessageType.FAIL
    ),
    SUCCESSFULLY_ENLISTED_ITEM(
            "<green>Successfully enlisted item on marketplace!",
            MessageType.SUCCESS
    );

    private static YamlConfiguration configFile;
    public static void saveDefaults(final Plugin plugin, final String fileName) {
        EnumConfig.trySaveDefaults(plugin, fileName, values());

        configFile = EnumConfig.tryGetConfigFile(plugin, fileName);
    }

    private Object cache = null;
    private final Object defaultValue;

    private final MessageType messageType;

    MessageConfig(final String defaultValue, final MessageType messageType) {
        this.defaultValue = defaultValue;
        this.messageType = messageType;
    }

    public boolean send(final CommandSender sender, final TagResolver... resolver) {
        sender.sendMessage(
                LegacyComponentSerializer.legacySection()
                        .serialize(get(resolver))
        );

        messageType.trigger(sender);

        return messageType.shouldPass();
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

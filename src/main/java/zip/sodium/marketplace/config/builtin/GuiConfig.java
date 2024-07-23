package zip.sodium.marketplace.config.builtin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
            "<!i><white>Refresh"
    ),
    PREVIOUS_ITEM_NAME(
            "<!i><red>Previous"
    ),
    NEXT_ITEM_NAME(
            "<!i><green>Next"
    ),
    PRICE_ON_LORE(
            "<!i><white>$<price>"
    ),
    CANCEL_ITEM_NAME(
            "<!i><red>Cancel"
    ),
    CONFIRM_ITEM_NAME(
            "<!i><green>Confirm"
    ),
    INSUFFICIENT_FUNDS(
            "<!i><red>Insufficient Funds"
    ),
    UNKNOWN_LISTING(
            "<!><red>Unknown Item Listing"
    ),
    INVENTORY_FULL(
            "<!i><red>Inventory Full"
    ),
    BOUGHT_ITEM(
            "<!i><green>Successfully Bought Item"
    ),
    CURRENT_PAGE_NAME(
            "<!i>Page: <page>"
    ),
    BUY_ACTION_CANCELLED(
            "<!i><red>Buy Action cancelled"
    ),
    UNKNOWN_SELLER(
            "<!i><red>This seller is unknown!"
    ),
    INVALID_LISTING(
            "<!i><red>This listing is invalid!"
    ),
    CANT_GO_BACK_ITEM_NAME(
            "<!i><red>Can't go back!"
    ),
    TRANSACTIONS_GUI_TITLE(
            "<white>Transactions"
    ),
    TRANSACTION_ITEM_NAME(
            "<!i><white>Transaction"
    ),
    BUYER_ON_LORE(
            "<!i><white>Listing got bought by `<buyer>`"
    ),
    SELLER_ON_LORE(
            "<!i><white>Listing bought from `<seller>`"
    ),
    PUT_UP_ON_LORE(
            "<!i><white>Put up by `<me>`"
    ),
    BLACKMARKET_GUI_TITLE(
            "<gray>Blackmarket"
    ),
    BLACKMARKET_REFRESH_INTERVAL(
            1
    );

    private static YamlConfiguration configFile;
    public static void saveDefaults(final Plugin plugin, final String fileName) {
        EnumConfig.trySaveDefaults(plugin, fileName, values());

        configFile = EnumConfig.tryGetConfigFile(plugin, fileName);
    }

    private Object cache = null;
    private final @NotNull Object defaultValue;

    GuiConfig(final @NotNull Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String get() {
        return get(TagResolver.empty());
    }

    public String get(final TagResolver... resolver) {
        return LegacyComponentSerializer.legacySection().serialize(
                MiniMessage.miniMessage().deserialize(
                        get(FileConfiguration::getString),
                        resolver
                )
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
    public @NotNull Object defaultValue() {
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

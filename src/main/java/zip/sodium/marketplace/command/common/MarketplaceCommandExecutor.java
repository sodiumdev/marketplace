package zip.sodium.marketplace.command.common;

import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import zip.sodium.marketplace.Entrypoint;
import zip.sodium.marketplace.config.builtin.GuiConfig;
import zip.sodium.marketplace.config.builtin.MessageConfig;
import zip.sodium.marketplace.database.DatabaseHolder;
import zip.sodium.marketplace.gui.Gui;
import zip.sodium.marketplace.gui.item.GuiItem;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;

public final class MarketplaceCommandExecutor {
    private MarketplaceCommandExecutor() {}

    private static void setupGui(final Gui gui, final Player player) {
        gui.clear();
        gui.surroundWith(
                ItemStackUtil.of(Material.GRAY_STAINED_GLASS_PANE)
        );

        gui.fill(1, 1, 8, 4, ItemStackUtil.of(Material.SKELETON_SKULL, GuiConfig.LOADING_SKULL_NAME.get()));

        DatabaseHolder.findListings(0, 0)
                .thenAccept(listings -> foundListings(player, gui, listings));
    }

    private static void foundListings(final Player player, final Gui gui, final Collection<Document> listings) {
        gui.fill(1, 1, 8, 4, null);

        gui.setItem(22, GuiItem.of(
                ItemStackUtil.of(
                        Material.PLAYER_HEAD,
                        GuiConfig.REFRESH_ITEM_NAME.get()
                ),
                click -> gui.setup(player),
                drag -> gui.setup(player)
        ));

        gui.addItem(
                listings.stream().map(document -> {
                    final var rawData = document.get("item_data");
                    if (!(rawData instanceof Binary binary)) {
                        MessageConfig.SOMETHING_WENT_WRONG.send(player);
                        player.closeInventory();

                        return null;
                    }

                    final var data = binary.getData();

                    final ItemStack itemStack;
                    try {
                        itemStack = ItemStackUtil.deserialize(data);
                    } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        Entrypoint.logger().log(
                                Level.SEVERE,
                                "Error deserializing item while trying to find listings!",
                                e
                        );

                        MessageConfig.SOMETHING_WENT_WRONG.send(player);
                        player.closeInventory();

                        return null;
                    }

                    return itemStack;
                }).toArray(ItemStack[]::new)
        );
    }

    public static boolean execute(final Player player) {
        final var gui = Gui.of(5, MarketplaceCommandExecutor::setupGui);
        gui.open(player, GuiConfig.MARKETPLACE_GUI_TITLE.get());

        return true;
    }
}

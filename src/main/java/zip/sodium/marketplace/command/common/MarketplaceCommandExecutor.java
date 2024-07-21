package zip.sodium.marketplace.command.common;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import zip.sodium.marketplace.Entrypoint;
import zip.sodium.marketplace.config.builtin.GuiConfig;
import zip.sodium.marketplace.config.builtin.MessageConfig;
import zip.sodium.marketplace.data.message.MessageType;
import zip.sodium.marketplace.database.DatabaseHolder;
import zip.sodium.marketplace.gui.Gui;
import zip.sodium.marketplace.gui.item.GuiItem;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;
import zip.sodium.marketplace.vault.VaultProvider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
        gui.clear(1, 1, 8, 4);

        gui.setItem(21, GuiItem.of(
                ItemStackUtil.of(
                        Material.ARROW,
                        GuiConfig.PREVIOUS_ITEM_NAME.get()
                ),
                click -> gui.setup((Player) click.getWhoClicked()),
                drag -> gui.setup((Player) drag.getWhoClicked())
        ));

        gui.setItem(22, GuiItem.of(
                ItemStackUtil.of(
                        Material.PLAYER_HEAD,
                        GuiConfig.REFRESH_ITEM_NAME.get()
                ),
                click -> gui.setup((Player) click.getWhoClicked()),
                drag -> gui.setup((Player) drag.getWhoClicked())
        ));

        gui.setItem(23, GuiItem.of(
                ItemStackUtil.of(
                        Material.ARROW,
                        GuiConfig.NEXT_ITEM_NAME.get()
                ),
                click -> gui.setup((Player) click.getWhoClicked()),
                drag -> gui.setup((Player) drag.getWhoClicked())
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

                    final var showcaseItem = itemStack.clone();

                    final var price = document.get("price");
                    if (!(price instanceof final Integer integerPrice) || integerPrice <= 0) {
                        Entrypoint.logger().severe("Invalid price tag!");

                        MessageConfig.SOMETHING_WENT_WRONG.send(player);
                        player.closeInventory();

                        return null;
                    }

                    ItemStackUtil.editMeta(
                            showcaseItem,
                            meta -> meta.setLore(
                                    List.of(
                                            GuiConfig.LORE_PRICE.getResolved(
                                                    Placeholder.unparsed("price", price.toString())
                                            )
                                    )
                            )
                    );

                    final var sellerId = document.get("seller_id");
                    if (!(sellerId instanceof final String stringSellerId)) {
                        Entrypoint.logger().severe("Seller id tag is not a string!");

                        MessageConfig.UNKNOWN_SELLER.send(player);
                        player.closeInventory();

                        return null;
                    }

                    final UUID sellerUuid;
                    try {
                        sellerUuid = UUID.fromString(stringSellerId);
                    } catch (final IllegalArgumentException e) {
                        Entrypoint.logger().log(
                                Level.SEVERE,
                                "Seller id tag is not a valid UUID!",
                                e
                        );

                        MessageConfig.UNKNOWN_SELLER.send(player);
                        player.closeInventory();

                        return null;
                    }

                    final var seller = Bukkit.getOfflinePlayer(sellerUuid);

                    return GuiItem.of(
                            showcaseItem,
                            click -> setupConfirmation(
                                    gui,
                                    seller,
                                    itemStack,
                                    integerPrice
                            ),
                            drag -> setupConfirmation(
                                    gui,
                                    seller,
                                    itemStack,
                                    integerPrice
                            )
                    );
                }).filter(Objects::nonNull).toArray(GuiItem[]::new)
        );
    }

    private static void setupConfirmation(final Gui gui, final OfflinePlayer seller, final ItemStack buyingItem, final int price) {
        gui.fill(ItemStackUtil.of(Material.GRAY_STAINED_GLASS_PANE, "-"));

        gui.setItem(20, GuiItem.of(
                ItemStackUtil.of(
                        Material.RED_STAINED_GLASS_PANE,
                        GuiConfig.CANCEL_ITEM_NAME.get()
                ),
                click -> gui.setup((Player) click.getWhoClicked()),
                drag -> gui.setup((Player) drag.getWhoClicked())
        ));

        gui.setItem(22, buyingItem);

        gui.setItem(24, GuiItem.of(
                ItemStackUtil.of(
                        Material.GREEN_STAINED_GLASS_PANE,
                        GuiConfig.CONFIRM_ITEM_NAME.get()
                ),
                click -> checkAndBuy(
                        gui,
                        (Player) click.getWhoClicked(),
                        seller,
                        buyingItem,
                        price
                ),
                drag -> checkAndBuy(
                        gui,
                        (Player) drag.getWhoClicked(),
                        seller,
                        buyingItem,
                        price
                )
        ));
    }

    private static void checkAndBuy(final Gui gui, final Player player, final OfflinePlayer seller, final ItemStack buyingItem, final int price) {
        DatabaseHolder.tryFind(seller, buyingItem, price).thenAccept(found -> {
            if (!found) {
                gui.setupAndSurround(
                        player,
                        false,
                        GuiConfig.UNKNOWN_LISTING.get()
                );

                return;
            }

            checkAndBuy$2(gui, player, seller, buyingItem, price);
        });
    }

    private static void checkAndBuy$2(final Gui gui, final Player player, final OfflinePlayer seller, final ItemStack buyingItem, final int price) {
        final var economy = VaultProvider.getEconomy();

        final double balance = economy.getBalance(player);
        if (price > balance) {
            gui.setupAndSurround(
                    player,
                    false,
                    GuiConfig.INSUFFICIENT_FUNDS.get()
            );

            return;
        }

        final int firstEmpty = player.getInventory().firstEmpty();
        if (firstEmpty == -1) {
            gui.setupAndSurround(
                    player,
                    false,
                    GuiConfig.INVENTORY_FULL.get()
            );

            return;
        }

        DatabaseHolder.putDown(seller, player, buyingItem, price).thenAccept(success -> {
            if (!success) {
                gui.setupAndSurround(
                        player,
                        false,
                        GuiConfig.UNKNOWN_LISTING.get()
                );

                return;
            }

            economy.withdrawPlayer(
                    player,
                    price
            );

            economy.depositPlayer(
                    seller,
                    price
            );

            player.getInventory().addItem(buyingItem);

            gui.setupAndSurround(
                    player,
                    true,
                    GuiConfig.BOUGHT_ITEM.getResolved(
                            Placeholder.unparsed("price", Integer.toString(price)),
                            Placeholder.unparsed("seller", Objects.toString(seller.getName()))
                    )
            );
        });
    }

    public static boolean execute(final Player player) {
        final var gui = Gui.of(GuiConfig.MARKETPLACE_GUI_TITLE.get(), 5, MarketplaceCommandExecutor::setupGui);
        gui.open(player);

        return true;
    }
}

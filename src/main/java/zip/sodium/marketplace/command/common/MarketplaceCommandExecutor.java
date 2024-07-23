package zip.sodium.marketplace.command.common;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import zip.sodium.marketplace.config.builtin.GuiConfig;
import zip.sodium.marketplace.data.listing.Listing;
import zip.sodium.marketplace.database.DatabaseHolder;
import zip.sodium.marketplace.gui.Gui;
import zip.sodium.marketplace.gui.item.GuiItem;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;
import zip.sodium.marketplace.vault.VaultProvider;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class MarketplaceCommandExecutor {
    private MarketplaceCommandExecutor() {}

    public static boolean execute(final Player player) {
        Gui.of(GuiConfig.MARKETPLACE_GUI_TITLE.get(), 5, MarketplaceCommandExecutor::setupGui)
                .open(player);

        return true;
    }

    private static void setupGui(final Gui gui, final Player player) {
        gui.clear();
        gui.surroundWith(
                ItemStackUtil.of(Material.GRAY_STAINED_GLASS_PANE)
        );

        gui.fill(1, 1, 8, 4,
                ItemStackUtil.of(Material.SKELETON_SKULL, GuiConfig.LOADING_SKULL_NAME.get()));

        gui.placePageIndicator(4);

        final int maxListings = 18;
        DatabaseHolder.findListings(maxListings * gui.page(), maxListings)
                .thenAccept(listings -> foundListings(gui, listings));
    }

    private static void foundListings(final Gui gui, final Collection<Listing> listings) {
        gui.clear(1, 1, 8, 4);
        gui.placePaginationControls(22);

        gui.addItem(
                listings.stream().map(listing -> {
                    final var stack = listing.stack();
                    final int price = listing.price();
                    final var seller = listing.seller();

                    final var showcaseItem = stack.clone();

                    ItemStackUtil.editMeta(
                            showcaseItem,
                            meta -> editListingMeta(listing, meta)
                    );

                    return GuiItem.of(
                            showcaseItem,
                            ignored -> setupConfirmation(
                                    gui,
                                    seller,
                                    stack,
                                    price
                            )
                    );
                }).toArray(GuiItem[]::new)
        );
    }

    private static void editListingMeta(final Listing listing, final ItemMeta meta) {
        final List<String> lore;
        if (meta.hasLore()) {
            lore = Objects.requireNonNull(meta.getLore());
            lore.add("");
        } else lore = new LinkedList<>();

        lore.add(
                GuiConfig.PUT_UP_ON_LORE.getResolved(
                        Placeholder.unparsed("me", Objects.requireNonNull(listing.seller().getName()))
                )
        );

        lore.add(
                GuiConfig.PRICE_ON_LORE.getResolved(
                        Placeholder.unparsed("price", Integer.toString(listing.price()))
                )
        );

        meta.setLore(lore);
    }

    private static void setupConfirmation(final Gui gui, final OfflinePlayer seller, final ItemStack buyingItem, final int price) {
        gui.fill(ItemStackUtil.of(Material.GRAY_STAINED_GLASS_PANE, "-"));

        gui.setItem(20, GuiItem.of(
                ItemStackUtil.of(
                        Material.RED_STAINED_GLASS_PANE,
                        GuiConfig.CANCEL_ITEM_NAME.get()
                ),
                player -> gui.setupAndSurround(
                        player,
                        false,
                        GuiConfig.BUY_ACTION_CANCELLED.get()
                )
        ));

        gui.setItem(22, buyingItem);

        gui.setItem(24, GuiItem.of(
                ItemStackUtil.of(
                        Material.GREEN_STAINED_GLASS_PANE,
                        GuiConfig.CONFIRM_ITEM_NAME.get()
                ),
                player -> checkAndBuy(
                        gui,
                        player,
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

        DatabaseHolder.purchase(seller, player, buyingItem, price).thenAccept(success -> {
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
}

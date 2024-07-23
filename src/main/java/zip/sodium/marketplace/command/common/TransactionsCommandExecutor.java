package zip.sodium.marketplace.command.common;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import zip.sodium.marketplace.config.builtin.GuiConfig;
import zip.sodium.marketplace.data.transaction.Transaction;
import zip.sodium.marketplace.database.DatabaseHolder;
import zip.sodium.marketplace.gui.Gui;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;

import java.util.*;

public final class TransactionsCommandExecutor {
    public static boolean execute(final Player player) {
        Gui.of(GuiConfig.TRANSACTIONS_GUI_TITLE.get(), 5, TransactionsCommandExecutor::setupGui)
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
        DatabaseHolder.findTransactions(player, maxListings * gui.page(), maxListings)
                .thenAccept(transactions -> foundTransactions(gui, player, transactions));
    }

    private static void foundTransactions(final Gui gui, final Player player, final Collection<Transaction> transactions) {
        gui.clear(1, 1, 8, 4);
        gui.placePaginationControls(22);

        gui.addItem(
                transactions.stream().map(transaction -> {
                    final ItemStack stack = transaction.stack();

                    ItemStackUtil.editMeta(
                            stack,
                            meta -> editTransactionMeta(player, transaction, meta)
                    );

                    return stack;
                }).toArray(ItemStack[]::new)
        );
    }

    private static void editTransactionMeta(final Player player, final Transaction transaction, final ItemMeta meta) {
        final List<String> lore;
        if (meta.hasLore()) {
            lore = Objects.requireNonNull(meta.getLore());
            lore.add("");
        } else lore = new LinkedList<>();

        final Boolean wasBought = transaction.wasBought();
        final var extra = transaction.extra();

        if (wasBought != null && extra != null) {
            if (wasBought) {
                lore.add(
                        GuiConfig.BUYER_ON_LORE.getResolved(
                                Placeholder.unparsed("buyer", Objects.requireNonNull(extra.getName()))
                        )
                );
            } else lore.add(
                    GuiConfig.SELLER_ON_LORE.getResolved(
                            Placeholder.unparsed("seller", Objects.requireNonNull(extra.getName()))
                    )
            );
        } else lore.add(
                GuiConfig.PUT_UP_ON_LORE.getResolved(
                        Placeholder.unparsed("me", player.getName())
                )
        );

        lore.add(
                GuiConfig.PRICE_ON_LORE.getResolved(
                        Placeholder.unparsed("price", Integer.toString(transaction.price()))
                )
        );

        meta.setLore(lore);
    }
}

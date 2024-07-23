package zip.sodium.marketplace.gui;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zip.sodium.marketplace.config.builtin.GuiConfig;
import zip.sodium.marketplace.data.message.MessageType;
import zip.sodium.marketplace.gui.item.GuiItem;
import zip.sodium.marketplace.listener.builtin.GuiListener;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;

import java.util.*;
import java.util.function.BiConsumer;

public class Gui {
    @Contract("_, _, _ -> new")
    public static Gui of(final String title, final int rows, final BiConsumer<Gui, Player> setup) {
        return new Gui(title, rows) {
            @Override
            public void setup(final @NotNull Player player) {
                setup.accept(this, player);
            }
        };
    }

    @Contract("_, _ -> new")
    public static Gui of(final int rows, final BiConsumer<Gui, Player> setup) {
        return of(
                null,
                rows,
                setup
        );
    }

    private final String title;

    private Inventory inventory;

    private final int size;
    private final Map<Integer, GuiItem> items;

    private int page = 0;

    private Gui(final String title, final int rows) {
        size = rows * 9;
        items = new HashMap<>(size);
        this.title = title;
    }

    public void placePageIndicator(final int slot) {
        setItem(slot, ItemStackUtil.of(
                Material.BLACK_STAINED_GLASS_PANE,
                GuiConfig.CURRENT_PAGE_NAME.get(
                        Placeholder.unparsed("page", Integer.toString(page()))
                )
        ));
    }

    public void placePaginationControls(final int centerSlot) {
        if (page() > 0)
            setItem(centerSlot - 1, GuiItem.of(
                    ItemStackUtil.of(
                            Material.ARROW,
                            GuiConfig.PREVIOUS_ITEM_NAME.get()
                    ),
                    clicker -> {
                        if (page() == 0)
                            return;

                        previousPage();
                        setup(clicker);
                    }
            ));
        else setItem(centerSlot - 1, ItemStackUtil.of(
                Material.BARRIER,
                GuiConfig.CANT_GO_BACK_ITEM_NAME.get()
        ));

        setItem(centerSlot, GuiItem.of(
                ItemStackUtil.of(
                        Material.PLAYER_HEAD,
                        GuiConfig.REFRESH_ITEM_NAME.get()
                ),
                this::setup
        ));

        setItem(centerSlot + 1, GuiItem.of(
                ItemStackUtil.of(
                        Material.ARROW,
                        GuiConfig.NEXT_ITEM_NAME.get()
                ),
                clicker -> {
                    nextPage();
                    setup(clicker);
                }
        ));
    }

    private void checkPageBounds() {
        if (page < 0)
            page = 0;
    }

    public final void nextPage() {
        page++;
        checkPageBounds();
    }

    public final void previousPage() {
        page--;
        checkPageBounds();
    }

    public final int page() {
        checkPageBounds();

        return page;
    }

    public final void surroundWith(final ItemStack stack) {
        final int height = inventory.getSize() / 9;

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 9; x++) {
                if (y == 0 || y == height - 1 || x == 0 || x == 8) {
                    final int slot = y * 9 + x;

                    inventory.setItem(slot, stack);
                }
            }
        }
    }

    public final void setItem(final int index, final GuiItem item) {
        items.put(index, item);
        inventory.setItem(index, item.itemStack());
    }

    public final void setItem(final int index, final ItemStack item) {
        items.remove(index);
        inventory.setItem(index, item);
    }

    public final void addItem(final ItemStack... items) {
        for (final var item : items) {
            final int index = inventory.firstEmpty();
            if (index == -1)
                break;

            setItem(index, item);
        }
    }

    public final void addItem(final GuiItem... items) {
        for (final var item : items) {
            final int index = inventory.firstEmpty();
            if (index == -1)
                break;

            setItem(index, item);
        }
    }

    public final void clear() {
        inventory.clear();
        items.clear();
    }

    public final void clear(final int x1, final int y1,
                           final int x2, final int y2) {
        for (int y = Math.min(y1, y2); y < Math.max(y1, y2); y++) {
            for (int x = Math.min(x1, x2); x < Math.max(x1, x2); x++) {
                final int index = y * 9 + x;

                inventory.setItem(index, null);
                items.remove(index);
            }
        }
    }

    public final void fill(final int x1, final int y1,
                           final int x2, final int y2,
                           final ItemStack stack) {
        for (int y = Math.min(y1, y2); y < Math.max(y1, y2); y++) {
            for (int x = Math.min(x1, x2); x < Math.max(x1, x2); x++) {
                inventory.setItem(y * 9 + x, stack);
            }
        }
    }

    public final void fill(final ItemStack stack) {
        items.clear();
        for (int index = 0; index < size; index++) {
            setItem(index, stack);
        }
    }

    public final void fill(final GuiItem item) {
        for (int index = 0; index < size; index++) {
            setItem(index, item);
        }
    }

    public final void setupAndSurround(final Player player, final boolean success, final String message) {
        setup(player);

        surroundWith(
                ItemStackUtil.of(
                        success ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        message
                )
        );

        MessageType.of(success).trigger(player);
    }

    public void setup(final @NotNull Player player) {}
    public void onClose(final @NotNull Player player) {}

    public final void open(final @NotNull Player player) {
        open(player, null);
    }

    public final void open(final @NotNull Player player, final @Nullable String titleOverride) {
        player.closeInventory();

        GuiListener.inventoryOpened(player, this);

        if (titleOverride == null) {
            if (title != null) {
                inventory = Bukkit.createInventory(null, size, title);
            } else inventory = Bukkit.createInventory(null, size);
        } else inventory = Bukkit.createInventory(null, size, titleOverride);

        player.openInventory(inventory);
    }

    public final @Nullable Inventory getInventory() {
        return inventory;
    }

    public final @Nullable GuiItem getGuiItem(final int index) {
        return items.get(index);
    }
}

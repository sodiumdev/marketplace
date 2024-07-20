package zip.sodium.marketplace.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zip.sodium.marketplace.gui.item.GuiItem;
import zip.sodium.marketplace.listener.builtin.GuiListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Gui {
    @Contract("_, _ -> new")
    public static Gui of(final int rows, final BiConsumer<Gui, Player> onOpen) {
        return new Gui(rows) {
            @Override
            public void setup(final @NotNull Player player) {
                onOpen.accept(this, player);
            }
        };
    }

    private Inventory inventory;

    private final int size;
    private final Map<Integer, GuiItem> items;

    private Gui(final int rows) {
        size = rows * 9;
        items = new HashMap<>(size);
    }

    public final void surroundWith(final ItemStack stack) {
        final int height = inventory.getSize() / 9;

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 9; x++) {
                if (y == 0 || y == height - 1 || x == 0 || x == 8) {
                    inventory.setItem(y * 9 + x, stack);
                }
            }
        }
    }

    public final void setItem(final int index, final GuiItem item) {
        items.put(index, item);
        inventory.setItem(index, item.itemStack());
    }

    public final void setItem(final int index, final ItemStack item) {
        inventory.setItem(index, item);
    }

    public final void addItem(final ItemStack... items) {
        inventory.addItem(items);
    }

    public final void clear() {
        inventory.clear();
        items.clear();
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

    public void setup(final @NotNull Player player) {}
    public void onClose(final @NotNull Player player) {}

    public final void open(final @NotNull Player player, final @NotNull String title) {
        GuiListener.inventoryOpened(player, this);

        inventory = Bukkit.createInventory(null, size);

        player.openInventory(inventory).setTitle(title);
    }

    public final @Nullable Inventory getInventory() {
        return inventory;
    }

    public final @Nullable GuiItem getGuiItem(final int index) {
        return items.get(index);
    }
}

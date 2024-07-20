package zip.sodium.marketplace.listener.builtin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import zip.sodium.marketplace.gui.Gui;
import zip.sodium.marketplace.gui.item.GuiItem;

import java.util.*;

public final class GuiListener implements Listener {
    private GuiListener() {}

    private static final Listener INSTANCE = new GuiListener();

    private static final Map<UUID, Gui> openGuis = new HashMap<>();

    @Contract("null -> null")
    public static Gui findOpenGuiFor(final Player player) {
        if (player == null)
            return null;

        return openGuis.get(player.getUniqueId());
    }

    public static void inventoryOpened(final Player player, final Gui gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    public static void register(final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(INSTANCE, plugin);
    }

    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        final Gui openGui = findOpenGuiFor(player);
        if (openGui == null)
            return;

        final int index = event.getRawSlot();

        if (event.getSlot() == index) {
            event.setCancelled(true);
        } else switch (event.getAction()) {
            case UNKNOWN,
                 COLLECT_TO_CURSOR,
                 MOVE_TO_OTHER_INVENTORY -> event.setCancelled(true);
        }

        final var item = openGui.getGuiItem(index);
        if (item == null)
            return;

        item.onClick(event);
    }

    @EventHandler
    public void onDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        final Gui openGui = findOpenGuiFor(player);
        if (openGui == null)
            return;

        for (final int index : event.getRawSlots()) {
            final var item = openGui.getGuiItem(index);
            if (item == null)
                continue;

            item.onDrag(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(final InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        final Gui openGui = findOpenGuiFor(player);
        if (openGui == null)
            return;

        openGui.setup(player);
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        final Gui openGui = findOpenGuiFor(player);
        if (openGui == null)
            return;

        if (!event.getInventory().equals(openGui.getInventory()))
            return;

        openGui.onClose(player);

        openGuis.remove(player.getUniqueId());
    }
}

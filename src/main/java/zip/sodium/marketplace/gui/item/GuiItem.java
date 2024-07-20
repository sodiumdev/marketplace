package zip.sodium.marketplace.gui.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

public final class GuiItem {
    public static GuiItem of(final ItemStack itemStack, final Consumer<InventoryClickEvent> clickAction, final Consumer<InventoryDragEvent> dragAction) {
        return new GuiItem(itemStack, clickAction, dragAction);
    }

    public static GuiItem of(final ItemStack itemStack) {
        return new GuiItem(itemStack, event -> {}, event -> {});
    }

    private final ItemStack itemStack;

    private final Consumer<InventoryClickEvent> clickAction;
    private final Consumer<InventoryDragEvent> dragAction;

    private GuiItem(final ItemStack itemStack, final Consumer<InventoryClickEvent> clickAction, final Consumer<InventoryDragEvent> dragAction) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
        this.dragAction = dragAction;
    }

    public void onClick(final InventoryClickEvent event) {
        clickAction.accept(event);
    }

    public void onDrag(final InventoryDragEvent event) {
        dragAction.accept(event);
    }

    public ItemStack itemStack() {
        return itemStack;
    }
}

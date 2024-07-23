package zip.sodium.marketplace.data.listing;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record Listing(@NotNull OfflinePlayer seller, @NotNull ItemStack stack, int price) { }

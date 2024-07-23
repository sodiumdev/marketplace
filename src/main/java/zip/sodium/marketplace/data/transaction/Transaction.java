package zip.sodium.marketplace.data.transaction;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Transaction(@Nullable OfflinePlayer extra, @Nullable Boolean wasBought, @NotNull ItemStack stack, int price) { }

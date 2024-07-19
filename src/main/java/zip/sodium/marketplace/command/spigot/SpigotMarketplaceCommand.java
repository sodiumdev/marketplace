package zip.sodium.marketplace.command.spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.command.common.MarketplaceCommandExecutor;
import zip.sodium.marketplace.util.spigot.CommandMapUtil;

import java.util.List;

public final class SpigotMarketplaceCommand extends Command {
    private static final SpigotMarketplaceCommand INSTANCE = new SpigotMarketplaceCommand();

    private SpigotMarketplaceCommand() {
        super("marketplace");
    }

    public static void acknowledge(final CommandMap map) {
        CommandMapUtil.register(map, INSTANCE);
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return false;

        return MarketplaceCommandExecutor.execute(
                player
        );
    }

    @Override
    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args) throws IllegalArgumentException {
        return List.of();
    }
}

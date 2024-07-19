package zip.sodium.marketplace.command.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.command.common.SellCommandExecutor;
import zip.sodium.marketplace.util.spigot.CommandMapUtil;

import java.util.List;

public final class SpigotSellCommand extends Command {
    private static final SpigotSellCommand INSTANCE = new SpigotSellCommand();

    private SpigotSellCommand() {
        super("sell");
    }

    public static void acknowledge(final CommandMap map) {
        CommandMapUtil.register(map, INSTANCE);
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return false;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Expected `price` argument!");
            return false;
        }

        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Expected less arguments!");
            return false;
        }

        final Integer price = Integer.getInteger(args[0]);
        if (price == null) {
            player.sendMessage(ChatColor.RED + "Expected less arguments!");
            return false;
        }

        return SellCommandExecutor.execute(
                player,
                price
        );
    }

    @Override
    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1
                && args[0].chars().allMatch(Character::isDigit))
            return List.of(args[0]);

        return List.of();
    }
}

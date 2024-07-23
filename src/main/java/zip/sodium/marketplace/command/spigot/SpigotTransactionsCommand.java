package zip.sodium.marketplace.command.spigot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.command.common.TransactionsCommandExecutor;
import zip.sodium.marketplace.config.builtin.MessageConfig;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.bukkit.CommandMapUtil;

public final class SpigotTransactionsCommand extends Command {
    private static final SpigotTransactionsCommand INSTANCE = new SpigotTransactionsCommand();

    public static void acknowledge(final CommandMap map) {
        CommandMapUtil.register(map, INSTANCE);
    }

    private SpigotTransactionsCommand() {
        super("transactions");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return MessageConfig.PLAYER_REQUIRED.send(sender);
        if (!PermissionConfig.TRANSACTIONS.has(player))
            return MessageConfig.INSUFFICIENT_PERMISSIONS.send(
                    sender,
                    Placeholder.component("permission", Component.text(PermissionConfig.TRANSACTIONS.get()))
            );

        if (args.length > 0)
            return MessageConfig.EXPECTED_LESS_ARGUMENTS.send(player);

        return TransactionsCommandExecutor.execute(
                player
        );
    }
}

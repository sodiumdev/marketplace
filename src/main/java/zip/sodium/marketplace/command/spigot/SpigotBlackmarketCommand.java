package zip.sodium.marketplace.command.spigot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.command.common.BlackmarketCommandExecutor;
import zip.sodium.marketplace.config.builtin.MessageConfig;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.bukkit.CommandMapUtil;

import java.util.List;

public final class SpigotBlackmarketCommand extends Command {
    private static final SpigotBlackmarketCommand INSTANCE = new SpigotBlackmarketCommand();

    private SpigotBlackmarketCommand() {
        super("blackmarket");
    }

    public static void acknowledge(final CommandMap map) {
        CommandMapUtil.register(map, INSTANCE);
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return MessageConfig.PLAYER_REQUIRED.send(sender);
        if (!PermissionConfig.BLACKMARKET.has(player))
            return MessageConfig.INSUFFICIENT_PERMISSIONS.send(
                    sender,
                    Placeholder.component("permission", Component.text(PermissionConfig.BLACKMARKET.get()))
            );

        if (args.length > 1)
            return MessageConfig.EXPECTED_LESS_ARGUMENTS.send(player);

        if (args.length == 1
                && args[0].equals("refresh"))
            return BlackmarketCommandExecutor.executeRefresh(
                    player
            );

        return BlackmarketCommandExecutor.execute(
                player
        );
    }

    @Override
    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1
                && PermissionConfig.REFRESH_BLACKMARKET.has(sender))
            return List.of("refresh");

        return List.of();
    }
}

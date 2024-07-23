package zip.sodium.marketplace.command.spigot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.command.common.SellCommandExecutor;
import zip.sodium.marketplace.config.builtin.MessageConfig;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.bukkit.CommandMapUtil;
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
            return MessageConfig.PLAYER_REQUIRED.send(sender);
        if (!PermissionConfig.SELL.has(player))
            return MessageConfig.INSUFFICIENT_PERMISSIONS.send(
                    sender,
                    Placeholder.unparsed("permission", PermissionConfig.SELL.get())
            );

        if (args.length < 1)
            return MessageConfig.EXPECTED_ARGUMENT.send(
                    player,
                    Placeholder.unparsed("argument", "price")
            );

        if (args.length > 1)
            return MessageConfig.EXPECTED_LESS_ARGUMENTS.send(player);

        final int price;
        try {
            price = Integer.parseInt(args[0]);
        } catch (final NumberFormatException e) {
            return MessageConfig.INVALID_ARGUMENT.send(
                    player,
                    Placeholder.unparsed("argument", "price"),
                    Placeholder.unparsed("expected", "integer"),
                    Placeholder.unparsed("got", args[0])
            );
        }

        if (price < 1)
            return MessageConfig.INTEGER_OUT_OF_BOUNDS.send(
                    player,
                    Placeholder.unparsed("argument", "price"),
                    Placeholder.component("min", Component.text(1)),
                    Placeholder.component("max", Component.text(Integer.MAX_VALUE))
            );

        return SellCommandExecutor.execute(
                player,
                price
        );
    }

    @Override
    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            if (args[0].chars().count() == 0
                    || !args[0].chars().allMatch(Character::isDigit)
                    || Integer.parseInt(args[0]) < 1)
                return List.of("<integer>");
        }

        return List.of();
    }
}

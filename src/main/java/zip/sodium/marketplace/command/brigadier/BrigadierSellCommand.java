package zip.sodium.marketplace.command.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.permissions.Permission;
import zip.sodium.marketplace.command.common.SellCommandExecutor;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.brigadier.CommandSourceStackUtil;

public final class BrigadierSellCommand {
    private BrigadierSellCommand() {}

    public static <T> void acknowledge(final CommandDispatcher<T> dispatcher) {
        final var sellNode = LiteralArgumentBuilder
                .<T>literal("sell")
                .requires(source -> PermissionConfig.SELL.has(CommandSourceStackUtil.getPlayerOrException(source)))
                .build();

        final var priceNode = RequiredArgumentBuilder
                .<T, Integer>argument(
                        "price", IntegerArgumentType.integer(1)
                )
                .executes(BrigadierSellCommand::execute)
                .build();

        dispatcher.getRoot().addChild(sellNode);
        sellNode.addChild(priceNode);
    }

    private static int execute(final CommandContext<?> context) {
        return Boolean.compare(
                SellCommandExecutor.execute(
                        CommandSourceStackUtil.getPlayerOrException(context.getSource()),
                        IntegerArgumentType.getInteger(context, "price")
                ),
                false
        );
    }
}

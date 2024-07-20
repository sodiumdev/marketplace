package zip.sodium.marketplace.command.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import zip.sodium.marketplace.command.common.MarketplaceCommandExecutor;
import zip.sodium.marketplace.command.common.SellCommandExecutor;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.brigadier.CommandSourceStackUtil;

public final class BrigadierMarketplaceCommand {
    private BrigadierMarketplaceCommand() {}

    public static <T> void acknowledge(final CommandDispatcher<T> dispatcher) {
        final var sellNode = LiteralArgumentBuilder
                .<T>literal("marketplace")
                .requires(source -> PermissionConfig.VIEW.has(CommandSourceStackUtil.getPlayerOrException(source)))
                .executes(BrigadierMarketplaceCommand::execute)
                .build();

        dispatcher.getRoot().addChild(sellNode);
    }

    private static int execute(final CommandContext<?> context) {
        return Boolean.compare(
                MarketplaceCommandExecutor.execute(
                        CommandSourceStackUtil.getPlayerOrException(context.getSource())
                ),
                false
        );
    }
}

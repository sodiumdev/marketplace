package zip.sodium.marketplace.command.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import zip.sodium.marketplace.command.common.BlackmarketCommandExecutor;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.brigadier.CommandSourceStackUtil;

public final class BrigadierBlackmarketCommand {
    private BrigadierBlackmarketCommand() {}

    public static <T> void acknowledge(final CommandDispatcher<T> dispatcher) {
        final var blackmarketNode = LiteralArgumentBuilder
                .<T>literal("blackmarket")
                .requires(source -> PermissionConfig.BLACKMARKET.has(CommandSourceStackUtil.getPlayerOrException(source)))
                .executes(BrigadierBlackmarketCommand::execute)
                .build();

        final var refreshNode = LiteralArgumentBuilder
                .<T>literal("refresh")
                .requires(source -> PermissionConfig.REFRESH_BLACKMARKET.has(CommandSourceStackUtil.getPlayerOrException(source)))
                .executes(BrigadierBlackmarketCommand::executeRefresh)
                .build();

        dispatcher.getRoot().addChild(blackmarketNode);
        blackmarketNode.addChild(refreshNode);
    }

    private static int executeRefresh(final CommandContext<?> context) {
        return Boolean.compare(
                BlackmarketCommandExecutor.executeRefresh(
                        CommandSourceStackUtil.getPlayerOrException(context.getSource())
                ),
                false
        );
    }

    private static int execute(final CommandContext<?> context) {
        return Boolean.compare(
                BlackmarketCommandExecutor.execute(
                        CommandSourceStackUtil.getPlayerOrException(context.getSource())
                ),
                false
        );
    }
}

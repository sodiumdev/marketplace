package zip.sodium.marketplace.command.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import zip.sodium.marketplace.command.common.TransactionsCommandExecutor;
import zip.sodium.marketplace.config.builtin.PermissionConfig;
import zip.sodium.marketplace.util.brigadier.CommandSourceStackUtil;

public final class BrigadierTransactionsCommand {
    private BrigadierTransactionsCommand() {}

    public static <T> void acknowledge(final CommandDispatcher<T> dispatcher) {
        final var transactionsNode = LiteralArgumentBuilder
                .<T>literal("transactions")
                .requires(source -> PermissionConfig.TRANSACTIONS.has(CommandSourceStackUtil.getPlayerOrException(source)))
                .executes(BrigadierTransactionsCommand::execute)
                .build();

        dispatcher.getRoot().addChild(transactionsNode);
    }

    private static int execute(final CommandContext<?> context) {
        return Boolean.compare(
                TransactionsCommandExecutor.execute(
                        CommandSourceStackUtil.getPlayerOrException(context.getSource())
                ),
                false
        );
    }
}

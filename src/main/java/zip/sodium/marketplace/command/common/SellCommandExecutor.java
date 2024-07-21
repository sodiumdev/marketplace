package zip.sodium.marketplace.command.common;

import org.bukkit.entity.Player;
import zip.sodium.marketplace.config.builtin.MessageConfig;
import zip.sodium.marketplace.database.DatabaseHolder;

public final class SellCommandExecutor {
    private SellCommandExecutor() {}

    public static boolean execute(final Player player, final int price) {
        final var item = player.getInventory().getItemInMainHand();
        if (item.getAmount() == 0 || item.getType().isAir())
            return MessageConfig.INVALID_ITEM.send(player);

        MessageConfig.ENLISTING_ITEM.send(player);

        DatabaseHolder.putUp(
                player,
                item,
                price
        ).thenApply(success -> {
            if (!success)
                return MessageConfig.ERROR_ENLISTING_ITEM.send(player);

            player.getInventory().setItemInMainHand(null);

            return MessageConfig.SUCCESSFULLY_ENLISTED_ITEM.send(player);
        });

        return true;
    }
}

package zip.sodium.marketplace.data.message;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum MessageType {
    SUCCESS {
        @Override
        public void trigger(final CommandSender sender) {
            if (!(sender instanceof Player player))
                return;

            player.playSound(
                    player,
                    Sound.ENTITY_PLAYER_LEVELUP,
                    1,
                    1
            );
        }
    },
    FAIL {
        @Override
        public void trigger(final CommandSender sender) {
            if (!(sender instanceof Player player))
                return;

            player.playSound(
                    player,
                    Sound.ENTITY_ENDERMAN_TELEPORT,
                    1,
                    0.5f
            );
        }
    },
    PASS {
        @Override
        public void trigger(CommandSender player) {}
    };

    public static MessageType of(final boolean flag) {
        return flag ? SUCCESS : FAIL;
    }

    public abstract void trigger(final CommandSender player);

    public boolean shouldPass() {
        return switch (this) {
            case SUCCESS, PASS -> true;
            case FAIL -> false;
        };
    }
}
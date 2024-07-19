package zip.sodium.marketplace.config.data;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum MessageType {
    SUCCESS {
        @Override
        public void trigger(final Player player) {
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
        public void trigger(final Player player) {
            player.playSound(
                    player,
                    Sound.ENTITY_ENDERMAN_TELEPORT,
                    1,
                    0.5f
            );
        }
    };

    public abstract void trigger(final Player player);
}
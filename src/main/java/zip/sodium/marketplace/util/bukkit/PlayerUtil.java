package zip.sodium.marketplace.util.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import zip.sodium.marketplace.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.UUID;

public final class PlayerUtil {
    private PlayerUtil() {}

    private static final Field BUKKIT_ENTITY_FIELD = getBukkitEntityField();

    private static Field getBukkitEntityField() {
        final var entityClass = ReflectionUtil.tryGetClass("net.minecraft.world.entity.Entity");
        if (entityClass == null)
            return null;

        return ReflectionUtil.tryGetField(entityClass, "bukkitEntity");
    }

    @Contract("null -> null")
    public static Player nmsToBukkit(final Object nmsPlayer) {
        if (BUKKIT_ENTITY_FIELD == null)
            return null;
        if (nmsPlayer == null)
            return null;

        return (Player) ReflectionUtil.tryGetValue(nmsPlayer, BUKKIT_ENTITY_FIELD);
    }

    @Contract("null -> null")
    public static OfflinePlayer tryGetOfflinePlayer(final String uuidString) {
        if (uuidString == null)
            return null;

        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (final IllegalArgumentException e) {
            return null;
        }

        return Bukkit.getOfflinePlayer(uuid);
    }
}

package zip.sodium.marketplace.util.brigadier;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import zip.sodium.marketplace.util.ReflectionUtil;
import zip.sodium.marketplace.util.nms.PlayerUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CommandSourceStackUtil {
    private CommandSourceStackUtil() {}

    private static final Method GET_PLAYER_OR_EXCEPTION_METHOD = ReflectionUtil.trySetAccessible(findGetPlayerOrExceptionMethod());
    private static Method findGetPlayerOrExceptionMethod() {
        var clazz = ReflectionUtil.tryGetClass("net.minecraft.commands.CommandListenerWrapper");
        if (clazz == null) {
            clazz = ReflectionUtil.tryGetClass("net.minecraft.commands.CommandSourceStack");

            return ReflectionUtil.tryGetMethod(clazz, "getPlayerOrException");
        }

        return ReflectionUtil.tryGetMethod(clazz, "h");
    }

    @Contract("null -> null")
    public static Player getPlayerOrException(final Object source) {
        if (GET_PLAYER_OR_EXCEPTION_METHOD == null)
            return null;
        if (source == null)
            return null;

        try {
            return PlayerUtil.nmsToBukkit(GET_PLAYER_OR_EXCEPTION_METHOD.invoke(source));
        } catch (IllegalAccessException | InvocationTargetException ignored) {}

        return null;
    }
}

package zip.sodium.marketplace.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;

public final class ReflectionUtil {
    private ReflectionUtil() {}

    public static final String MINECRAFT_VERSION = getVersion();

    private static String getVersion() {
        String version = Bukkit.getServer().getClass().getName().substring(22);
        version  = version.substring(0, version.length() - 11);

        return version;
    }

    @Contract("null -> null")
    public static @Nullable Class<?> tryGetClass(final String className) {
        if (className == null)
            return null;

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    @Contract("null, _ -> null; _, null -> null")
    public static @Nullable Field tryGetField(final Class<?> clazz, final String fieldName) {
        if (clazz == null)
            return null;
        if (fieldName == null)
            return null;

        try {
            return trySetAccessible(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException ignored) {}

        return null;
    }

    @Contract("null, _ -> null; _, null -> null")
    public static @Nullable Object tryGetValue(final Object base, final Field field) {
        if (base == null)
            return null;
        if (field == null)
            return null;

        try {
            return field.get(base);
        } catch (IllegalAccessException ignored) {}

        return null;
    }

    @Contract("null, _ -> null; _, null -> null")
    public static @Nullable Object tryGetValue(final Object base, final String fieldName) {
        if (base == null)
            return null;

        final var field = tryGetField(base.getClass(), fieldName);
        if (field == null)
            return null;

        return tryGetValue(base, field);
    }

    @Contract("null, _ -> null; _, null -> null")
    public static @Nullable Constructor<?> tryGetConstructor(final Class<?> clazz, final @NotNull Class<?>... parameterTypes) {
        if (clazz == null)
            return null;

        try {
            return trySetAccessible(clazz.getDeclaredConstructor(parameterTypes));
        } catch (NoSuchMethodException ignored) {}

        return null;
    }

    @Contract("null, _, _ -> null; _, null, _ -> null")
    public static @Nullable Method tryGetMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        if (clazz == null)
            return null;
        if (methodName == null)
            return null;

        try {
            return trySetAccessible(clazz.getDeclaredMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException ignored) {}

        return null;
    }

    @Contract("null, _, _ -> null; _, null, _ -> null")
    public static @Nullable Object tryCallMethod(final Object base, final Method method, final Object... parameters) {
        if (base == null)
            return null;
        if (method == null)
            return null;

        try {
            return method.invoke(base, parameters);
        } catch (InvocationTargetException | IllegalAccessException ignored) {}

        return null;
    }

    @Contract("null -> null")
    public static <T extends AccessibleObject> @Nullable T trySetAccessible(final @Nullable T element) {
        if (element != null)
            element.setAccessible(true);

        return element;
    }
}

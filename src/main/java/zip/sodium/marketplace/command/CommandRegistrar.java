package zip.sodium.marketplace.command;

import com.mojang.brigadier.CommandDispatcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import zip.sodium.marketplace.Entrypoint;
import zip.sodium.marketplace.command.brigadier.BrigadierMarketplaceCommand;
import zip.sodium.marketplace.command.brigadier.BrigadierSellCommand;
import zip.sodium.marketplace.command.spigot.SpigotMarketplaceCommand;
import zip.sodium.marketplace.command.spigot.SpigotSellCommand;
import zip.sodium.marketplace.util.ReflectionUtil;

import java.util.Objects;
import java.util.logging.Level;

public final class CommandRegistrar {
    private CommandRegistrar() {}

    private static CommandDispatcher<?> findCommandDispatcher() {
        final var bukkitServer = Bukkit.getServer();
        final var nmsServer = ReflectionUtil.tryCallMethod(
                bukkitServer,
                ReflectionUtil.tryGetMethod(
                        bukkitServer.getClass(),
                        "getServer"
                )
        );

        if (nmsServer == null)
            throw new AssertionError("Unable to get NMS server instance!");

        final var nmsServerClass = ReflectionUtil.tryGetClass("net.minecraft.server.MinecraftServer");
        if (nmsServerClass == null)
            throw new AssertionError("Unable to get NMS server class!");

        final var commands = ReflectionUtil.tryCallMethod(
                nmsServer,
                Objects.requireNonNullElseGet(
                        ReflectionUtil.tryGetMethod(
                                nmsServerClass,
                                "aE"
                        ),
                        () -> ReflectionUtil.tryGetMethod(
                                nmsServerClass,
                                "getCommands"
                        )
                )
        );

        if (commands == null)
            throw new AssertionError("Unable to get command dispatcher!");

        return (CommandDispatcher<?>) ReflectionUtil.tryCallMethod(
                commands,
                Objects.requireNonNullElseGet(
                        ReflectionUtil.tryGetMethod(
                                commands.getClass(),
                                "a"
                        ),
                        () -> ReflectionUtil.tryGetMethod(
                                commands.getClass(),
                                "getDispatcher"
                        )
                )
        );
    }

    private static CommandMap findCommandMap() {
        final var map = ReflectionUtil.tryGetValue(
                Bukkit.getServer(),
                "commandMap"
        );

        if (map == null)
            throw new AssertionError("Unable to get command map!");

        return (CommandMap) map;
    }

    public static void acknowledge() {
        try {
            final var dispatcher = findCommandDispatcher();

            registerViaCommandDispatcher(dispatcher);

            return;
        } catch (final Exception e) {
            Entrypoint.logger().log(
                    Level.WARNING,
                    "Couldn't register commands via brigadier! Registering commands via Spigot instead.",
                    e
            );
        }

        try {
            final var commandMap = findCommandMap();

            registerViaSpigot(commandMap);
        } catch (final Exception e) {
            Entrypoint.logger().log(
                    Level.WARNING,
                    "Couldn't register commands via Spigot! Disabling.",
                    e
            );

            Entrypoint.disable();
        }
    }

    private static void registerViaSpigot(final CommandMap map) {
        SpigotSellCommand.acknowledge(map);
        SpigotMarketplaceCommand.acknowledge(map);
    }

    private static void registerViaCommandDispatcher(CommandDispatcher<?> dispatcher) {
        BrigadierSellCommand.acknowledge(dispatcher);
        BrigadierMarketplaceCommand.acknowledge(dispatcher);
    }
}

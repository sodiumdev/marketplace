package zip.sodium.marketplace.util.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

public final class CommandMapUtil {
    private CommandMapUtil() {}

    public static void register(final CommandMap map, final Command command) {
        map.register(
                command.getName(),
                command
        );

        for (final String alias : command.getAliases()) {
            map.register(
                    alias,
                    command
            );
        }
    }
}

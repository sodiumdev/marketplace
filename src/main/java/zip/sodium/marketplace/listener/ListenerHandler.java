package zip.sodium.marketplace.listener;

import org.bukkit.plugin.Plugin;
import zip.sodium.marketplace.listener.builtin.GuiListener;

public final class ListenerHandler {
    private ListenerHandler() {}

    public static void acknowledge(final Plugin plugin) {
        GuiListener.register(plugin);
    }
}

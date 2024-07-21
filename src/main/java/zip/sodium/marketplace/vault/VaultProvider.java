package zip.sodium.marketplace.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public final class VaultProvider {
    private VaultProvider() {}

    private static Economy economy = null;
    public static Economy getEconomy() {
        return economy;
    }

    public static boolean acknowledge() {
        final var rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        economy = rsp.getProvider();
        return economy != null;
    }
}

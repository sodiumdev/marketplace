package zip.sodium.marketplace.webhook;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import zip.sodium.marketplace.config.builtin.WebhookConfig;

import java.util.Objects;

public final class WebhookProvider {
    private WebhookProvider() {}

    private static WebhookClient client = null;

    public static void acknowledge() {
        final String url = WebhookConfig.URL.get();

        try {
            client = WebhookClient.withUrl(url);
        } catch (final IllegalArgumentException e) {
            client = null;
        }
    }

    public static void logPurchaseToWebhook(final OfflinePlayer seller, final OfflinePlayer buyer, final ItemStack item, final double price) {
        if (client == null)
            return;

        final var embed = new WebhookEmbedBuilder()
                .setColor(WebhookConfig.EMBED_COLOR.getHex())
                .setDescription(WebhookConfig.PURCHASE_LOG.get(
                        Placeholder.unparsed("seller", Objects.requireNonNull(seller.getName())),
                        Placeholder.unparsed("buyer", Objects.requireNonNull(buyer.getName())),
                        Placeholder.unparsed("item", item.toString()),
                        Placeholder.unparsed("price", Double.toString(price))
                ))
                .build();

        client.send(embed);
    }

    public static void cleanup() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}

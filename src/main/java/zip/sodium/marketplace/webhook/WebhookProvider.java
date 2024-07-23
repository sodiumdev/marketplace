package zip.sodium.marketplace.webhook;

import club.minnced.discord.webhook.WebhookClient;
import zip.sodium.marketplace.config.builtin.WebhookConfig;

public final class WebhookProvider {
    private WebhookProvider() {}

    private static WebhookClient client = null;

    public static WebhookClient getClient() {
        return client;
    }

    public static void acknowledge() {
        final String url = WebhookConfig.URL.get();

        try {
            client = WebhookClient.withUrl(url);
        } catch (final IllegalArgumentException e) {
            client = null;
        }
    }

    public static void cleanup() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}

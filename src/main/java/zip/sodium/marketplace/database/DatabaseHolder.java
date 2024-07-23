package zip.sodium.marketplace.database;

import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import zip.sodium.marketplace.Entrypoint;
import zip.sodium.marketplace.config.builtin.DatabaseConfig;
import zip.sodium.marketplace.config.builtin.WebhookConfig;
import zip.sodium.marketplace.data.listing.Listing;
import zip.sodium.marketplace.data.transaction.Transaction;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;
import zip.sodium.marketplace.util.bukkit.PlayerUtil;
import zip.sodium.marketplace.webhook.WebhookProvider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class DatabaseHolder {
    private DatabaseHolder() {}

    private static MongoClient client = null;

    private static MongoCollection<Document> itemsCollection = null;
    private static MongoCollection<Document> transactionsCollection = null;

    private static void tryConnect() {
        final String connectionString = DatabaseConfig.CONNECTION_STRING.get(FileConfiguration::getString);
        if (connectionString == null)
            throw new AssertionError("Connection string not set!");

        client = MongoClients.create(connectionString);
    }

    public static void acknowledge() {
        try {
            tryConnect();
        } catch (final MongoException | AssertionError e) {
            Entrypoint.logger().log(
                    Level.SEVERE,
                    "Couldn't connect to database! Disabling.",
                    e
            );

            Entrypoint.disable();

            return;
        }

        final var marketplaceDataDb = client.getDatabase(DatabaseConfig.DATABASE_NAME.get());

        itemsCollection = marketplaceDataDb.getCollection(DatabaseConfig.ITEMS_COLLECTION_NAME.get());
        transactionsCollection = marketplaceDataDb.getCollection(DatabaseConfig.TRANSACTIONS_COLLECTION_NAME.get());
    }

    public static CompletableFuture<Collection<Transaction>> findTransactions(final OfflinePlayer player, final int skip, final int limit) {
        return CompletableFuture.supplyAsync(() -> {
            final var cursor = transactionsCollection.find(
                    new Document("actor_id", player.getUniqueId().hashCode())
            ).skip(skip).limit(limit);

            final var transactions = new LinkedList<Transaction>();
            try (final var cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    final var document = cursorIterator.next();

                    final var transaction = parseTransaction(document);
                    if (transaction == null)
                        continue;

                    transactions.add(transaction);
                }
            }

            return transactions;
        });
    }

    public static @Nullable Transaction parseTransaction(final Document document) {
        final var objectPrice = document.get("price");
        if (!(objectPrice instanceof final Integer price))
            return null;

        final var objectItemData = document.get("item_data");
        if (!(objectItemData instanceof final Binary binaryItemData))
            return null;

        final var itemData = binaryItemData.getData();

        final ItemStack stack;
        try {
            stack = ItemStackUtil.deserialize(itemData);
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return null;
        }

        final Boolean wasBought;
        final OfflinePlayer extra;

        if (document.get("bought_by") instanceof final String uuidString) {
            extra = PlayerUtil.tryGetOfflinePlayer(uuidString);
            if (extra == null)
                return null;

            wasBought = true;
        } else if (document.get("seller_id") instanceof final String uuidString) {
            extra = PlayerUtil.tryGetOfflinePlayer(uuidString);
            if (extra == null)
                return null;

            wasBought = false;
        } else {
            wasBought = null;
            extra = null;
        }

        return new Transaction(
                extra,
                wasBought,
                stack,
                price
        );
    }

    public static CompletableFuture<Collection<Listing>> findListings(final int skip, final int limit) {
        return CompletableFuture.supplyAsync(() -> {
            final var cursor = itemsCollection.find()
                    .skip(skip)
                    .limit(limit);

            final var listings = new LinkedList<Listing>();
            try (final var cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    final var document = cursorIterator.next();

                    final var listing = parseListing(document);
                    if (listing == null)
                        continue;

                    listings.add(
                            listing
                    );
                }
            }

            return listings;
        });
    }

    public static Listing parseListing(final Document document) {
        final var objectSellerId = document.get("seller_id");
        if (!(objectSellerId instanceof final String stringSellerId))
            return null;

        final var objectPrice = document.get("price");
        if (!(objectPrice instanceof final Integer price))
            return null;

        final var objectItemData = document.get("item_data");
        if (!(objectItemData instanceof final Binary binaryItemData))
            return null;

        final var itemData = binaryItemData.getData();

        final ItemStack stack;
        try {
            stack = ItemStackUtil.deserialize(itemData);
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return null;
        }

        final var seller = PlayerUtil.tryGetOfflinePlayer(stringSellerId);

        return new Listing(
                seller,
                stack,
                price
        );
    }

    public static CompletableFuture<Boolean> putUp(final OfflinePlayer seller, final ItemStack item, final int price) {
        final byte[] serialized;
        try {
            serialized = ItemStackUtil.serialize(item);
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Entrypoint.logger().log(
                    Level.SEVERE,
                    "Error serializing item!",
                    e
            );

            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.runAsync(() -> itemsCollection.insertOne(
                new Document("seller_id", seller.getUniqueId().toString())
                        .append("item_data", serialized)
                        .append("price", price)
        )).thenApplyAsync(result -> {
            transactionsCollection.insertOne(
                    new Document("actor_id", seller.getUniqueId().hashCode())
                            .append("item_data", serialized)
                            .append("price", price)
            );

            return true;
        });
    }

    public static CompletableFuture<Boolean> purchase(final OfflinePlayer seller, final OfflinePlayer buyer, final ItemStack item, final int price) {
        final byte[] serialized;
        try {
            serialized = ItemStackUtil.serialize(item);
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Entrypoint.logger().log(
                    Level.SEVERE,
                    "Error serializing item!",
                    e
            );

            return CompletableFuture.completedFuture(false);
        }

        logPurchaseToWebhook(
                seller,
                buyer,
                item,
                price
        );

        return CompletableFuture.runAsync(() -> itemsCollection.deleteOne(
                new Document("seller_id", seller.getUniqueId().toString())
                        .append("item_data", serialized)
                        .append("price", price)
        )).thenApplyAsync(result -> transactionsCollection.insertOne(
                    new Document("actor_id", seller.getUniqueId().hashCode())
                            .append("bought_by", buyer.getUniqueId().toString())
                            .append("item_data", serialized)
                            .append("price", price)
        )).thenApplyAsync(result -> {
            transactionsCollection.insertOne(
                    new Document("actor_id", buyer.getUniqueId().hashCode())
                            .append("seller_id", seller.getUniqueId().toString())
                            .append("item_data", serialized)
                            .append("price", price)
            );

            return true;
        });
    }

    private static void logPurchaseToWebhook(final OfflinePlayer seller, final OfflinePlayer buyer, final ItemStack item, final int price) {
        final var webhook = WebhookProvider.getClient();
        if (webhook == null)
            return;

        final var embed = new WebhookEmbedBuilder()
                .setColor(WebhookConfig.EMBED_COLOR.getHex())
                .setDescription(WebhookConfig.PURCHASE_LOG.getResolved(
                        Placeholder.unparsed("seller", Objects.requireNonNull(seller.getName())),
                        Placeholder.unparsed("buyer", Objects.requireNonNull(buyer.getName())),
                        Placeholder.unparsed("item", item.toString()),
                        Placeholder.unparsed("price", Integer.toString(price))
                ))
                .build();

        webhook.send(embed);
    }

    public static CompletableFuture<Boolean> tryFind(final OfflinePlayer seller, final ItemStack item, final int price) {
        final byte[] serialized;
        try {
            serialized = ItemStackUtil.serialize(item);
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Entrypoint.logger().log(
                    Level.SEVERE,
                    "Error serializing item!",
                    e
            );

            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> itemsCollection.countDocuments(
                new Document("seller_id", seller.getUniqueId().toString())
                        .append("item_data", serialized)
                        .append("price", price)
        ) >= 1);
    }

    public static void cleanup() {
        if (client != null) {
            client.close();
            client = null;
            itemsCollection = null;
            transactionsCollection = null;
        }
    }
}

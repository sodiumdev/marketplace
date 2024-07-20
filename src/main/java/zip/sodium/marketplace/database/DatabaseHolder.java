package zip.sodium.marketplace.database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import zip.sodium.marketplace.Entrypoint;
import zip.sodium.marketplace.config.builtin.DatabaseConfig;
import zip.sodium.marketplace.data.status.MarketplaceStatus;
import zip.sodium.marketplace.util.bukkit.ItemStackUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
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

    public static CompletableFuture<Collection<Document>> findListings(final int skip, final int limit) {
        return CompletableFuture.supplyAsync(() -> {
            final var cursor = itemsCollection.find()
                    .skip(skip)
                    .limit(limit);

            final var documents = new LinkedList<Document>();
            try (final var cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    documents.add(cursorIterator.next());
                }
            }

            return documents;
        });
    }

    public static CompletableFuture<Boolean> putUpForSale(final Player seller, final ItemStack item, final int price) {
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
                            .append("status", MarketplaceStatus.PUT_UP.ordinal())
                            .append("item_data", serialized)
                            .append("price", price)
            );

            return true;
        });
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

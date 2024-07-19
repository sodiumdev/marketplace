package zip.sodium.marketplace.database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import zip.sodium.marketplace.Entrypoint;
import zip.sodium.marketplace.config.builtin.DatabaseConfig;

import java.util.logging.Level;

public final class DatabaseHolder {
    private DatabaseHolder() {}

    private static MongoClient client = null;

    private static void tryConnect() {
        final String connectionString = DatabaseConfig.CONNECTION_STRING.get(FileConfiguration::getString);
        if (connectionString == null)
            throw new AssertionError("Connection string not set!");

        client = MongoClients.create(connectionString);

        final var database = client.getDatabase("admin");
        database.runCommand(new Document("ping", 1));
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
        }
    }

    public static void cleanup() {
        client.close();
        client = null;
    }
}

package zip.sodium.marketplace.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import zip.sodium.marketplace.Entrypoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.BiFunction;

public interface EnumConfig {
    /**
     * @param t A random instance of the enum
     * @param <T> Type of the enum
     * @return The default config generated via the enum's values
     */
    static <T extends Enum<T> & EnumConfig> @NotNull String generateDefaultConfigFileContent(final @NotNull T[] t) {
        final var defaultConfig = new YamlConfiguration();
        for (final var element : t) {
            element.setCache(null);

            defaultConfig.set(element.toString(), element.defaultValue());
        }

        return defaultConfig.saveToString();
    }

    /**
     * @param plugin The plugin executing the method
     * @param fileName The file to save <code>defaultConfigFileContent</code> to
     * @param defaultConfigFileContent The contents
     */
    static void trySaveDefaults(final @NotNull Plugin plugin, final @NotNull String fileName, final @NotNull String defaultConfigFileContent) {
        final var dataFolder = plugin.getDataFolder();
        if (!dataFolder.isDirectory())
            dataFolder.mkdirs();

        final var configFile = new File(dataFolder, fileName);
        if (configFile.exists())
            return;

        try {
            configFile.createNewFile();

            Files.writeString(
                    configFile.toPath(),
                    defaultConfigFileContent
            );
        } catch (IOException e) {
            Entrypoint.logger().severe("Error saving config file! Will use default values instead.");
        }
    }

    /**
     * @param plugin The plugin executing the method
     * @param fileName The file to load into a <code>YamlConfiguration</code>
     * @return The <code>YamlConfiguration</code> loaded from the provided file name
     */
    static YamlConfiguration tryGetConfigFile(final @NotNull Plugin plugin, final @NotNull String fileName) {
        final var configFile = new YamlConfiguration();
        try {
            configFile.load(
                    new File(
                            plugin.getDataFolder(),
                            fileName
                    )
            );
        } catch (IOException | InvalidConfigurationException e) {
            Entrypoint.logger().severe("Error loading config file! Will use defaults instead.");
        }

        return configFile;
    }

    /**
     * @return The cached value
     */
    Object cache();

    /**
     * @param value The value to set the cached value to
     */
    void setCache(final Object value);

    /**
     * @return The default value defined in the enum instance
     */
    Object defaultValue();

    /**
     * @return YML Configuration Instance
     */
    FileConfiguration ymlConfiguration();

    /**
     * @param operator The method to call when getting the
     * @param <T> Result Type
     * @return Config
     */
    @SuppressWarnings("unchecked")
    default <T> T get(final BiFunction<FileConfiguration, String, T> operator) {
        final var cache = cache();
        if (cache == null) {
            final var configFile = ymlConfiguration();
            final var result = Objects.requireNonNullElseGet(
                    configFile == null ? null : operator.apply(configFile, toString()),
                    () -> (T) defaultValue()
            );

            setCache(result);

            return result;
        }

        return (T) cache;
    }
}
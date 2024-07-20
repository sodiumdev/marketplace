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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Level;

public interface EnumConfig {
    private static void writeString(final @NotNull Path path, final @NotNull String content) {
        try {
            Files.writeString(
                    path,
                    content
            );
        } catch (final IOException e) {
            Entrypoint.logger().log(
                    Level.SEVERE,
                    "Error writing to file `" + path.getFileName() + "`!",
                    e
            );
        }
    }

    /**
     * @param plugin The plugin executing the method
     * @param fileName The file to save <code>defaultConfigFileContent</code> to
     * @param t All the values of the enum
     */
    static <T extends Enum<T> & EnumConfig> void trySaveDefaults(final @NotNull Plugin plugin, final @NotNull String fileName, final @NotNull T @NotNull [] t) {
        final var dataFolder = plugin.getDataFolder();
        if (!dataFolder.isDirectory())
            dataFolder.mkdirs();

        final var defaultConfig = new YamlConfiguration();
        for (final var element : t) {
            element.setCache(null);

            defaultConfig.set(element.toString(), element.defaultValue());
        }

        final var configFile = new File(dataFolder, fileName);
        if (configFile.exists()) {
            final var config = tryGetConfigFile(plugin, fileName);
            final var yaml = config.getKeys(false);
            final var defaultYaml = defaultConfig.getKeys(false);

            if (!yaml.containsAll(defaultYaml)) {
                final var newConfig = new YamlConfiguration();
                for (final String element : defaultYaml) {
                    if (yaml.contains(element)) {
                        newConfig.set(element, config.get(element));
                        continue;
                    }

                    newConfig.set(element, defaultConfig.get(element));
                }

                writeString(
                        configFile.toPath(),
                        newConfig.saveToString()
                );
            }

            return;
        }

        try {
            configFile.createNewFile();
        } catch (final IOException e) {
            Entrypoint.logger().log(
                    Level.SEVERE,
                    "Error creating new config file! Will use default values instead.",
                    e
            );

            return;
        }

        writeString(
                configFile.toPath(),
                defaultConfig.saveToString()
        );
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
        } catch (final IOException | InvalidConfigurationException e) {
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
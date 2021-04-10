package tech.ypsilon.bbbot.settings;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;

public class SettingsController {

    private static final Yaml YAML = new Yaml();
    private static SettingsController instance;

    private final Map<String, Object> DATA;

    /**
     * Registering and loading the settings with a given file
     *
     * @param SETTINGS_FILE the File where the settings are stored in
     * @throws Exception then the file is not found or cannot be read
     */
    public SettingsController(final File SETTINGS_FILE) throws Exception {
        instance = this;
        if (!SETTINGS_FILE.exists())
            throw new FileNotFoundException();
        DATA = YAML.load(new FileInputStream(SETTINGS_FILE));
    }

    /**
     * Get all settings inside the yaml file
     *
     * @return a Map with the settings
     */
    public static Map<String, Object> getData() {
        return Collections.unmodifiableMap(instance.DATA);
    }

    /**
     * Get a setting.
     * Structure depth increment by using the '.'
     *
     * @param key the key from the setting
     * @return the value or null
     * @throws RuntimeException when a structure depth change was requested but cannot be executed
     *                          because of a missing key
     */
    public static Object getValue(String key) {
        return getValue(key, instance.DATA);
    }

    public static <T> T getValue(String key, Class<? extends T> clazz) {
        Object value = getValue(key);
        if (value != null) {
            return clazz.cast(value);
        }
        throw new NullPointerException(String.format("key %s is invalid!", key));
    }

    public static long getLong(String key) {
        return getValue(key, Long.class);
    }

    public static String getString(String key) {
        return getValue(key, String.class);
    }

    public static int getInt(String key) {
        return getValue(key, Integer.class);
    }

    public static boolean getBoolean(String key) {
        return getValue(key, Boolean.class);
    }

    /**
     * Get a setting.
     * Structure depth increment by using the '.'
     *
     * INTERNAL METHOD! NOT MEANT TO BE USED!
     *
     * @param key the key from the setting
     * @return the value or null
     * @throws RuntimeException when a structure depth change was requested but cannot be executed
     *                          because of a missing key
     */
    @SuppressWarnings("unchecked")
    private static Object getValue(String key, Map<String, Object> map) {
        String[] split = key.split("\\.");
        if (split.length == 1) {
            return map.getOrDefault(split[0], null);
        }
        map = (Map<String, Object>) map.getOrDefault(split[0], null);
        if (map == null) throw new RuntimeException();
        return getValue(key.substring(split[0].length() + 1), map);
    }

}

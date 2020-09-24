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

    public SettingsController(final File SETTINGS_FILE) throws Exception {
        instance = this;
        if(!SETTINGS_FILE.exists())
            throw new FileNotFoundException();
        DATA = YAML.load(new FileInputStream(SETTINGS_FILE));
    }

    public static Map<String, Object> getData() {
        return Collections.unmodifiableMap(instance.DATA);
    }

    public static Object getValue(String key) {
        return getValue(key, instance.DATA);
    }

    @SuppressWarnings("unchecked")
    private static Object getValue(String key, Map<String, Object> map) {
        String[] split = key.split("\\.");
        if(split.length == 1) {
            return map.getOrDefault(split[0], null);
        }
        map = (Map<String, Object>) map.getOrDefault(split[0], null);
        if(map == null) throw new RuntimeException();
        return getValue(key.substring(split[0].length()+1), map);
    }

}

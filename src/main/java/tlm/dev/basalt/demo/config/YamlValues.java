package tlm.dev.basalt.demo.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;
import java.util.regex.Pattern;

final class YamlValues {

    private final String file;
    private final ConfigurationSection server;
    private final ConfigurationSection shipped;
    private final Logger logger;

    YamlValues(String file, ConfigurationSection server, ConfigurationSection shipped, Logger logger) {
        this.file = file;
        this.server = server;
        this.shipped = shipped;
        this.logger = logger;
    }

    int integer(String key, int min, int max) {
        if (server.isInt(key) && server.getInt(key) >= min && server.getInt(key) <= max) {
            return server.getInt(key);
        }
        warn(key, "a whole number from " + min + " to " + max);
        return shipped.getInt(key);
    }

    double decimal(String key, double min, double max) {
        if (server.get(key) instanceof Number number && number.doubleValue() >= min && number.doubleValue() <= max) {
            return number.doubleValue();
        }
        warn(key, "a number from " + min + " to " + max);
        return shipped.getDouble(key);
    }

    String word(String key, Pattern pattern) {
        String value = server.getString(key);
        if (server.isString(key) && value != null && pattern.matcher(value).matches()) {
            return value;
        }
        warn(key, "a name matching " + pattern.pattern());
        return shipped.getString(key);
    }

    private void warn(String key, String expected) {
        Object found = server.get(key);
        String what = found == null ? "is missing" : "is " + found;
        logger.warning(file + ": " + key + " " + what + ", expected " + expected + ", using the shipped value " + shipped.get(key));
    }
}

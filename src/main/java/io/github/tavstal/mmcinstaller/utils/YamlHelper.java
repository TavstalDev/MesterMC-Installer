package io.github.tavstal.mmcinstaller.utils;

import io.github.tavstal.mmcinstaller.core.logging.FallbackLogger;
import org.slf4j.event.Level;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * A utility class that extends the FallbackLogger to provide YAML-related helper methods.
 * This class includes functionality for reading and parsing YAML files, as well as retrieving
 * values from nested Maps using dot-separated keys.
 */
public class YamlHelper extends FallbackLogger {
    private static final DumperOptions _dumperOptions = new DumperOptions() {
        {
            setDefaultFlowStyle(FlowStyle.BLOCK); // Forces multi-line formatting.
            setIndent(2); // Sets the indentation level for YAML output.
        }
    };
    private static final Yaml _yaml = new Yaml(_dumperOptions); // YAML parser instance.

    /**
     * Reads a YAML file from the specified resource path and converts it into a Map.
     *
     * @param resourcePath The path to the resource file.
     * @param clazz        The class used to locate the resource.
     * @return A Map containing the parsed YAML data, or null if an error occurs.
     */
    public static Map<String, Object> readFromResource(String resourcePath, Class<?> clazz) {
        InputStream inputStream;
        try {
            Log(Level.DEBUG, String.format("Reading resource file: %s%n", resourcePath));
            inputStream = clazz.getResourceAsStream(resourcePath);
        } catch (NullPointerException ex) {
            Log(Level.ERROR, String.format("Failed to get resource file. Path: %s%n", resourcePath));
            return null;
        } catch (Exception ex) {
            Log(Level.ERROR,"Unknown error happened while reading resource file.");
            Log(Level.ERROR,ex.getMessage());
            return null;
        }

        Log(Level.DEBUG,"Loading yaml file...");
        Object yamlObject = _yaml.load(inputStream);
        Log(Level.DEBUG,"Checking if the yamlObject is a Map...");
        if (!(yamlObject instanceof Map)) {
            System.out.println("The yamlObject is not a Map. Aborting...");
            return null;
        }

        try {
            Log(Level.DEBUG,"Casting yamlObject to Map<String, Object>...");
            @SuppressWarnings("unchecked")
            Map<String, Object> localValue = (Map<String, Object>) yamlObject;
            return localValue;
        } catch (Exception ex) {
            Log(Level.ERROR,"Failed to cast the yamlObject to Map<String, Object>");
            Log(Level.ERROR,ex.getMessage());
            return null;
        }
    }

    //#region Methods for retrieving values from the Map

    /**
     * Retrieves an object from a nested Map using a dot-separated key.
     *
     * @param map          The Map to search.
     * @param key          The dot-separated key to locate the value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved object, or the default value if not found.
     */
    public static Object getObject(Map<String, Object> map, String key, Object defaultValue) {
        try {
            String[] keys = key.split("\\.");
            Object value = map;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    Log(Level.WARN, String.format("Failed to get the object value of the '%s' key.%n", key));
                    return defaultValue;
                }
            }

            return value;
        } catch (Exception ex) {
            Log(Level.ERROR, String.format("Unknown error happened while getting the object value of the '%s' key.%n", key));
            Log(Level.ERROR,ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Overloaded method to retrieve an object from a Map using a dot-separated key.
     * Returns null if the key is not found.
     *
     * @param map The Map to search.
     * @param key The dot-separated key to locate the value.
     * @return The retrieved object, or null if not found.
     */
    @SuppressWarnings("unused")
    public static Object getObject(Map<String, Object> map, String key) {
        return getObject(map, key, null);
    }

    /**
     * Retrieves a String value from a Map using a dot-separated key.
     *
     * @param map          The Map to search.
     * @param key          The dot-separated key to locate the value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved String value, or the default value if not found.
     */
    public static String getString(Map<String, Object> map, String key, String defaultValue) {
        try {
            Object result = getObject(map, key, defaultValue);
            if (result == null) {
                Log(Level.WARN, String.format("The value of the '%s' key is null.%n", key));
                return defaultValue;
            }
            return result.toString();
        } catch (Exception ex) {
            Log(Level.ERROR, String.format("Unknown error happened while getting the string value of the '%s' key.%n", key));
            Log(Level.ERROR, ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Overloaded method to retrieve a String value from a Map using a dot-separated key.
     * Returns an empty string if the key is not found.
     *
     * @param map The Map to search.
     * @param key The dot-separated key to locate the value.
     * @return The retrieved String value, or an empty string if not found.
     */
    public static String getString(Map<String, Object> map, String key) {
        return getString(map, key, "");
    }

    /**
     * Retrieves a Boolean value from a Map using a dot-separated key.
     *
     * @param map          The Map to search.
     * @param key          The dot-separated key to locate the value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved Boolean value, or the default value if not found.
     */
    public static Boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        try {
            Object result = getObject(map, key, defaultValue);
            if (result == null) {
                Log(Level.WARN, String.format("The value of the '%s' key is null.%n", key));
                return defaultValue;
            }
            return Boolean.parseBoolean(result.toString());
        } catch (Exception ex) {
            Log(Level.ERROR, String.format("Unknown error happened while getting the boolean value of the '%s' key.%n", key));
            Log(Level.ERROR,ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Overloaded method to retrieve a Boolean value from a Map using a dot-separated key.
     * Returns false if the key is not found.
     *
     * @param map The Map to search.
     * @param key The dot-separated key to locate the value.
     * @return The retrieved Boolean value, or false if not found.
     */
    @SuppressWarnings("unused")
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        return getBoolean(map, key, false);
    }

    /**
     * Retrieves an Integer value from a Map using a dot-separated key.
     *
     * @param map          The Map to search.
     * @param key          The dot-separated key to locate the value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved Integer value, or the default value if not found.
     */
    public static Integer getInteger(Map<String, Object> map, String key, int defaultValue) {
        try {
            Object result = getObject(map, key, defaultValue);
            if (result == null) {
                Log(Level.WARN, String.format("The value of the '%s' key is null.%n", key));
                return defaultValue;
            }
            return Integer.parseInt(result.toString());
        } catch (Exception ex) {
            Log(Level.ERROR, String.format("Unknown error happened while getting the integer value of the '%s' key.%n", key));
            Log(Level.ERROR,ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Overloaded method to retrieve an Integer value from a Map using a dot-separated key.
     * Returns 0 if the key is not found.
     *
     * @param map The Map to search.
     * @param key The dot-separated key to locate the value.
     * @return The retrieved Integer value, or 0 if not found.
     */
    @SuppressWarnings("unused")
    public static Integer getInteger(Map<String, Object> map, String key) {
        return getInteger(map, key, 0);
    }

    /**
     * Retrieves a Double value from a Map using a dot-separated key.
     *
     * @param map          The Map to search.
     * @param key          The dot-separated key to locate the value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved Double value, or the default value if not found.
     */
    public static Double getDouble(Map<String, Object> map, String key, Double defaultValue) {
        try {
            Object result = getObject(map, key, defaultValue);
            if (result == null) {
                Log(Level.WARN,String.format("The value of the '%s' key is null.%n", key));
                return defaultValue;
            }
            return Double.parseDouble(result.toString());
        } catch (Exception ex) {
            Log(Level.ERROR, String.format("Unknown error happened while getting the double value of the '%s' key.%n", key));
            Log(Level.ERROR, ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Overloaded method to retrieve a Double value from a Map using a dot-separated key.
     * Returns 0.0 if the key is not found.
     *
     * @param map The Map to search.
     * @param key The dot-separated key to locate the value.
     * @return The retrieved Double value, or 0.0 if not found.
     */
    @SuppressWarnings("unused")
    public static Double getDouble(Map<String, Object> map, String key) {
        return getDouble(map, key, 0.0);
    }
    //#endregion
}

package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class responsible for handling localization and translations for the application.
 * It loads localization files, retrieves translations, and supports multiple locales.
 */
public class Translator {
    private final Class _class; // The class used to locate resources.
    private final InstallerLogger _logger; // Logger instance for logging messages.
    private final String[] _locales; // Array of supported locale identifiers.
    private final String _defaultLocale = "hun"; // Default locale identifier.
    private Map<String, Map<String, Object>> _localization; // Map storing localization data.

    /**
     * Constructor for the Translator class.
     *
     * @param locales Array of locale identifiers to load.
     */
    public Translator(String[] locales) {
        _class = InstallerApplication.class;
        _locales = locales;
        _logger = InstallerApplication.getCustomLogger();
    }

    /**
     * Loads localization files for the specified locales.
     *
     * @return True if the localization files are successfully loaded, false otherwise.
     */
    public Boolean Load() {
        InputStream inputStream;
        _localization = new HashMap<>();

        _logger.Debug("Reading lang directory...");
        _logger.Debug("Reading lang files...");
        for (String locale : _locales) {
            try {
                inputStream = _class.getResourceAsStream("lang/" + locale + ".yml");
            } catch (NullPointerException ex) {
                _logger.Error(String.format("Failed to get localization file. Locale: %s", locale));
                return false;
            } catch (Exception ex) {
                _logger.Warn("Unknown error happened while reading locale file.");
                _logger.Error(ex.getMessage());
                return false;
            }

            _logger.Debug("Loading yaml file...");
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Forces multi-line formatting
            options.setIndent(2);
            Yaml yaml = new Yaml(options);
            Object yamlObject = yaml.load(inputStream);
            if (!(yamlObject instanceof Map)) {
                _logger.Error("Failed to cast the yamlObject after reading the localization.");
                return false;
            }

            _logger.Debug("Casting yamlObject to Map...");
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> localValue = (Map<String, Object>) yamlObject;
                _localization.put(locale, localValue); // Warning fix
            } catch (Exception ex) {
                _logger.Warn("Failed to cast the yamlObject to Map.");
                _logger.Error(ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Retrieves a localized string for the given key in the default locale.
     *
     * @param key The translation key.
     * @return The localized string, or an empty string if not found.
     */
    public String Localize(String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Retrieves a localized list of strings for the given key in the default locale.
     *
     * @param key The translation key.
     * @return A list of localized strings, or an empty list if not found.
     */
    public List<String> LocalizeList(String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>) value;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a localized array of strings for the given key in the default locale.
     *
     * @param key The translation key.
     * @return An array of localized strings, or an empty array if not found.
     */
    public String[] LocalizeArray(String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[]) value;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return new String[0];
        }
    }

    /**
     * Retrieves a localized string for the given key in the default locale, with arguments replaced.
     *
     * @param key  The translation key.
     * @param args A map of arguments to replace in the localized string.
     * @return The localized string with arguments replaced, or an empty string if not found.
     */
    public String Localize(String key, Map<String, Object> args) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            // Replace placeholders with arguments
            String result = value.toString();
            var argKeys = args.keySet();
            for (var dirKey : argKeys) {
                String finalKey;
                if (dirKey.startsWith("%"))
                    finalKey = dirKey;
                else
                    finalKey = "%" + dirKey + "%";
                result = result.replace(finalKey, args.get(dirKey).toString());
            }

            return result;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Retrieves a localized string for the given key in the specified locale.
     *
     * @param locale The locale identifier.
     * @param key    The translation key.
     * @return The localized string, or an empty string if not found.
     */
    public String Localize(String locale, String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(locale);
            if (value == null)
                value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Retrieves a localized list of strings for the given key in the specified locale.
     *
     * @param locale The locale identifier.
     * @param key    The translation key.
     * @return A list of localized strings, or an empty list if not found.
     */
    public List<String> LocalizeList(String locale, String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(locale);
            if (value == null)
                value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>) value;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a localized array of strings for the given key in the specified locale.
     *
     * @param locale The locale identifier.
     * @param key    The translation key.
     * @return An array of localized strings, or an empty array if not found.
     */
    public String[] LocalizeArray(String locale, String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(locale);
            if (value == null)
                value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[]) value;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return new String[0];
        }
    }

    /**
     * Retrieves a localized string for the given key in the specified locale, with arguments replaced.
     *
     * @param locale The locale identifier.
     * @param key    The translation key.
     * @param args   A map of arguments to replace in the localized string.
     * @return The localized string with arguments replaced, or an empty string if not found.
     */
    public String Localize(String locale, String key, Map<String, Object> args) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(locale);
            if (value == null)
                value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            // Replace placeholders with arguments
            String result = value.toString();
            var argKeys = args.keySet();
            for (var dirKey : argKeys) {
                String finalKey;
                if (dirKey.startsWith("%"))
                    finalKey = dirKey;
                else
                    finalKey = "%" + dirKey + "%";
                result = result.replace(finalKey, args.get(dirKey).toString());
            }

            return result;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }
}
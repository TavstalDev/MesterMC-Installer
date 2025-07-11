package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.utils.YamlHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * A class responsible for handling localization and translations for the application.
 * It loads localization files, retrieves translations, and supports multiple locales.
 */
public class InstallerTranslator {
    private final InstallerLogger _logger; // Logger instance for logging messages.
    private final String[] _locales; // Array of supported locale identifiers.
    private String _defaultLocale; // Default locale identifier.
    private Map<String, Map<String, Object>> _localization; // Map storing localization data.

    public InstallerTranslator(String[] locales) {
        _locales = locales;
        _logger = InstallerApplication.getLogger();
    }

    public void Load() {
        if (_defaultLocale == null)
            _defaultLocale = ConfigLoader.get().language();

        _localization = new HashMap<>();
        _logger.Debug("Reading lang files...");
        _logger.Debug("# Default locale: " + _defaultLocale);
        for (String locale : _locales) {

            _logger.Debug("Reading localization file for locale: " + locale);
            var result = YamlHelper.readFromResource(String.format("lang/%s.yml", locale), InstallerApplication.class);
            if (result == null) {
                _logger.Error(String.format("Failed to read localization file for locale: %s", locale));
                continue;
            }

            _localization.put(locale, result);
            _logger.Debug("Successfully loaded localization for locale: " + locale);
        }
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
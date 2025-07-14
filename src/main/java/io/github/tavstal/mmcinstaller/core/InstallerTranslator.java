package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.logging.InstallerLogger;
import io.github.tavstal.mmcinstaller.utils.YamlHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles localization and translation for the installer application.
 * Provides methods to load localization files and retrieve translations
 * for specific keys and locales.
 */
public class InstallerTranslator {
    private final InstallerLogger _logger; // Logger instance for logging messages.
    private final String[] _locales; // Array of supported locale identifiers.
    private Map<String, Map<String, Object>> _localization; // Map storing localization data.

    /**
     * Constructs an `InstallerTranslator` with the specified locales.
     *
     * @param locales Array of locale identifiers to support.
     */
    public InstallerTranslator(String[] locales) {
        _locales = locales;
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
    }

    /**
     * Loads localization files for the supported locales.
     * Reads YAML files from the `lang` directory and stores the data in memory.
     */
    public void Load() {
        _localization = new HashMap<>();
        _logger.Debug("Reading lang files...");
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
     * Retrieves a localized string for the current language based on the given key.
     *
     * @param key The translation key to look up.
     * @return The localized string, or an empty string if the key is not found.
     */
    public String Localize(String key) {
        try {
            var localeMap = _localization.get(InstallerState.getLanguage());
            var result = YamlHelper.getString(localeMap, key);
            if (result == null) {
                _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                return "";
            }
            return result;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Retrieves a localized string for the current language and replaces placeholders with arguments.
     *
     * @param key  The translation key to look up.
     * @param args A map of placeholders and their replacement values.
     * @return The localized string with placeholders replaced, or an empty string if the key is not found.
     */
    public String Localize(String key, Map<String, Object> args) {
        try {
            var localeMap = _localization.get(InstallerState.getLanguage());
            var result = YamlHelper.getString(localeMap, key);
            if (result == null) {
                _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                return "";
            }

            var argKeys = args.keySet();
            for (var dirKey : argKeys) {
                String finalKey = dirKey.startsWith("%") ? dirKey : "%" + dirKey + "%";
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
     * Retrieves a localized string for a specific locale based on the given key.
     *
     * @param locale The locale identifier to use for translation.
     * @param key    The translation key to look up.
     * @return The localized string, or an empty string if the key is not found.
     */
    public String Localize(String locale, String key) {
        try {
            var localeMap = _localization.get(locale);
            if (localeMap == null)
                localeMap = _localization.get(InstallerState.getLanguage());
            var result = YamlHelper.getString(localeMap, key);
            if (result == null) {
                _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                return "";
            }
            return result;
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Retrieves a localized string for a specific locale and replaces placeholders with arguments.
     *
     * @param locale The locale identifier to use for translation.
     * @param key    The translation key to look up.
     * @param args   A map of placeholders and their replacement values.
     * @return The localized string with placeholders replaced, or an empty string if the key is not found.
     */
    public String Localize(String locale, String key, Map<String, Object> args) {
        try {
            var localeMap = _localization.get(locale);
            if (localeMap == null)
                localeMap = _localization.get(InstallerState.getLanguage());
            var result = YamlHelper.getString(localeMap, key);
            if (result == null) {
                _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                return "";
            }

            var argKeys = args.keySet();
            for (var dirKey : argKeys) {
                String finalKey = dirKey.startsWith("%") ? dirKey : "%" + dirKey + "%";
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
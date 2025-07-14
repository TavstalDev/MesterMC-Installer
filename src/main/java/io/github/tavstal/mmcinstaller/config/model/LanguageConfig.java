package io.github.tavstal.mmcinstaller.config.model;

/**
 * Represents a language configuration with its key, name, and localization.
 * This record is used to store and manage language-related data.
 *
 * @param key          The unique identifier for the language.
 * @param name         The display name of the language.
 * @param localization The localization string associated with the language.
 */
public record LanguageConfig(String key, String name, String localization) {
}

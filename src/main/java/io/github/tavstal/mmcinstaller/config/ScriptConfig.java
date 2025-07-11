package io.github.tavstal.mmcinstaller.config;

/**
 * Represents a configuration for a script file.
 * <br/>
 * This record is used to store the name and content of a script file.
 * It provides an immutable data structure with built-in methods for accessing
 * the file name and content.
 *
 * @param fileName The name of the script file.
 * @param content The content of the script file.
 */
public record ScriptConfig(String fileName, String content) {
}

package io.github.tavstal.mmcinstaller.config.model;

/**
 * Represents the configuration for a macOS application.
 * <br/>
 * This record is used to store the file name, Info.plist content, and script
 * associated with the macOS application.
 *
 * @param fileName The name of the macOS application file.
 * @param infoList The content of the Info.plist file for the macOS application.
 * @param script The script associated with the macOS application.
 */
public record MacAppConfig(String fileName, String infoList, String script) {
}
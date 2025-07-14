package io.github.tavstal.mmcinstaller.config.model;

/**
 * Represents the default directory configuration.
 * <br/>
 * This record is used to store the paths for the AppData and Start Menu directories.
 * It provides an immutable data structure with built-in methods for accessing these properties.
 *
 * @param appData The path to the AppData directory.
 * @param startMenu The path to the Start Menu directory.
 */
public record DefaultDirsConfig(String appData, String startMenu) {
}

package io.github.tavstal.mmcinstaller.config.model;

/**
 * Represents the configuration for an executable file.
 * <br/>
 * This record is used to store the file name, resource path, and PowerShell script
 * associated with the executable. It provides an immutable data structure with
 * built-in methods for accessing these properties.
 *
 * @param fileName The name of the executable file.
 * @param resourcePath The path to the resource associated with the executable.
 * @param powershell The PowerShell script used for configuring the executable.
 */
public record ExeConfig(String fileName, String resourcePath, String powershell) {
}

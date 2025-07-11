package io.github.tavstal.mmcinstaller.config;

/**
 * Represents the main configuration for the installer.
 * <br/>
 * This record is used to store various configuration settings required for the installation process,
 * including language preferences, debug mode, and configurations for project, download, install, and uninstall operations.
 *
 * @param language The language setting for the installer.
 * @param debug A flag indicating whether debug mode is enabled.
 * @param project The configuration for the project being installed.
 * @param download The configuration for downloading resources.
 * @param install The configuration for the installation process.
 * @param uninstall The configuration for the uninstallation process.
 */
public record InstallerMainConfig(String language, Boolean debug, ProjectConfig project, DownloadConfig download, InstallConfig install, UninstallConfig uninstall) {
}

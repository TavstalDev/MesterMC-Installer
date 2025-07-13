package io.github.tavstal.mmcinstaller.config;

import java.util.List;

/**
 * Represents the main configuration for the installer.
 *
 * @param language           A list of language configurations.
 * @param debug              A flag indicating whether debug mode is enabled.
 * @param project            The project configuration.
 * @param download           The download configuration.
 * @param install            The install configuration.
 * @param uninstall          The uninstall configuration.
 * @param uninstallerConfig  The path or identifier for the uninstaller configuration.
 */
public record InstallerMainConfig(List<LanguageConfig> language, Boolean debug, ProjectConfig project, DownloadConfig download, InstallConfig install, UninstallConfig uninstall, String uninstallerConfig) {
}

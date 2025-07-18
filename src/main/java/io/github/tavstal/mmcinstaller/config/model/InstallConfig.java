package io.github.tavstal.mmcinstaller.config.model;

/**
 * Represents the configuration for the installation process.
 * <br/>
 * This record is used to store various script and executable configurations required during installation,
 * as well as default directory settings and macOS application configuration.
 *
 * @param defaultDirs The configuration for default directories used during installation.
 * @param batch The configuration for the batch script.
 * @param bash The configuration for the bash script.
 * @param zsh The configuration for the zsh script.
 * @param exe The configuration for the executable file.
 * @param linuxDesktop The configuration for the Linux desktop entry script.
 * @param macApp The configuration for the macOS application.
 */
public record InstallConfig(DefaultDirsConfig defaultDirs, ScriptConfig batch, ScriptConfig bash, ScriptConfig zsh, ExeConfig exe, ScriptConfig linuxDesktop, MacAppConfig macApp) {
}

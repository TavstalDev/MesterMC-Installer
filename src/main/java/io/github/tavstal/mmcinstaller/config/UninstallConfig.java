package io.github.tavstal.mmcinstaller.config;

/**
 * Represents the configuration for the uninstallation process.
 * <br/>
 * This record is used to store various script configurations required during uninstallation.
 *
 * @param batch The configuration for the batch script.
 * @param bash The configuration for the bash script.
 * @param zsh The configuration for the zsh script.
 */
public record UninstallConfig(ScriptConfig batch, ScriptConfig bash, ScriptConfig zsh) {
}

package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.*;
import io.github.tavstal.mmcinstaller.utils.YamlHelper;

import java.util.Map;

/**
 * A utility class responsible for loading and initializing the main configuration
 * of the installer application from a YAML file.
 */
public class ConfigLoader {
    // Holds the singleton instance of the main configuration.
    private static InstallerMainConfig _instance;

    /**
     * Initializes the configuration loader by reading and parsing the configuration
     * from the `config.yaml` resource file. If the configuration is already initialized,
     * this method does nothing.
     *
     * @throws RuntimeException if the configuration file cannot be loaded or parsed.
     */
    public static void init() {
        if (_instance != null)
            return;

        Map<String, Object> rawConfigMap = YamlHelper.readFromResource("config.yaml", InstallerApplication.class);
        if (rawConfigMap == null) {
            throw new RuntimeException("Failed to load configuration from config.yaml");
        }

        try {
            // Parse configuration values from the YAML file.
            String lang = YamlHelper.getString(rawConfigMap, "lang", "eng");
            boolean debug = YamlHelper.getBoolean(rawConfigMap, "debug", false);

            String projectName = YamlHelper.getString(rawConfigMap, "project.name", "");
            String projectVersion = YamlHelper.getString(rawConfigMap, "project.version", "1.0.0");
            String projectAuthor = YamlHelper.getString(rawConfigMap, "project.author", "Tavstal");

            String downloadFileName = YamlHelper.getString(rawConfigMap, "download.file_name", "client.jar");
            String downloadUrl = YamlHelper.getString(rawConfigMap, "download.link", "");
            String downloadChecksum = YamlHelper.getString(rawConfigMap, "download.sha256", "");

            String appdataDir = YamlHelper.getString(rawConfigMap, "install.default_dirs.appdata", "app_data");
            String startMenuDir = YamlHelper.getString(rawConfigMap, "install.default_dirs.start_menu", "start_menu_data");
            String batchInstallFileName = YamlHelper.getString(rawConfigMap, "install.batch.file_name", "start.bat");
            String batchInstallContent = YamlHelper.getString(rawConfigMap, "install.batch.content", "");
            String bashInstallFileName = YamlHelper.getString(rawConfigMap, "install.bash.file_name", "start.sh");
            String bashInstallContent = YamlHelper.getString(rawConfigMap, "install.bash.content", "");
            String zshInstallFileName = YamlHelper.getString(rawConfigMap, "install.zsh.file_name", "start.zsh");
            String zshInstallContent = YamlHelper.getString(rawConfigMap, "install.zsh.content", "");
            String exeInstallFileName = YamlHelper.getString(rawConfigMap, "install.exe.file_name", "start.exe");
            String exeResourcePath = YamlHelper.getString(rawConfigMap, "install.exe.resource_path", "");
            String exePowerShellScript = YamlHelper.getString(rawConfigMap, "install.exe.powershell", "");
            String linuxDesktopInstallFileName = YamlHelper.getString(rawConfigMap, "install.linux_desktop.file_name", "start.desktop");
            String linuxDesktopInstallContent = YamlHelper.getString(rawConfigMap, "install.linux_desktop.content", "");
            String macAppInstallFileName = YamlHelper.getString(rawConfigMap, "install.macos_app.file_name", "start.app");
            String macAppInfoList = YamlHelper.getString(rawConfigMap, "install.macos_app.info_list", "");
            String macAppScript = YamlHelper.getString(rawConfigMap, "install.macos_app.script", "");

            String batchUninstallFileName = YamlHelper.getString(rawConfigMap, "uninstall.batch.file_name", "uninstall.bat");
            String batchUninstallContent = YamlHelper.getString(rawConfigMap, "uninstall.batch.content", "");
            String bashUninstallFileName = YamlHelper.getString(rawConfigMap, "uninstall.bash.file_name", "uninstall.sh");
            String bashUninstallContent = YamlHelper.getString(rawConfigMap, "uninstall.bash.content", "");
            String zshUninstallFileName = YamlHelper.getString(rawConfigMap, "uninstall.zsh.file_name", "uninstall.zsh");
            String zshUninstallContent = YamlHelper.getString(rawConfigMap, "uninstall.zsh.content", "");

            // Create the main configuration instance.
            _instance = new InstallerMainConfig(
                    lang,
                    debug,
                    new ProjectConfig(
                            projectName,
                            projectVersion,
                            projectAuthor
                    ),
                    new DownloadConfig(
                            downloadUrl,
                            downloadFileName,
                            downloadChecksum
                    ),
                    new InstallConfig(
                            new DefaultDirsConfig (
                                    appdataDir,
                                    startMenuDir
                            ),
                            // Batch
                            new ScriptConfig(
                                    batchInstallFileName,
                                    batchInstallContent
                            ),
                            // Bash
                            new ScriptConfig(
                                    bashInstallFileName,
                                    bashInstallContent
                            ),
                            // Zsh
                            new ScriptConfig(
                                    zshInstallFileName,
                                    zshInstallContent
                            ),
                            // Exe
                            new ExeConfig(
                                    exeInstallFileName,
                                    exeResourcePath,
                                    exePowerShellScript
                            ),
                            // Linux Desktop
                            new ScriptConfig(
                                    linuxDesktopInstallFileName,
                                    linuxDesktopInstallContent
                            ),
                            // Mac App
                            new MacAppConfig(
                                macAppInstallFileName,
                                macAppInfoList,
                                macAppScript
                            )
                    ),
                    new UninstallConfig(
                            // Batch
                            new ScriptConfig(
                                    batchUninstallFileName,
                                    batchUninstallContent
                            ),
                            // Bash
                            new ScriptConfig(
                                    bashUninstallFileName,
                                    bashUninstallContent
                            ),
                            // Zsh
                            new ScriptConfig(
                                    zshUninstallFileName,
                                    zshUninstallContent
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the singleton instance of the main configuration.
     *
     * @return The initialized `InstallerMainConfig` instance.
     * @throws IllegalStateException if the configuration has not been initialized.
     */
    public static InstallerMainConfig get() {
        if (_instance == null) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call ConfigLoader.init() first.");
        }
        return _instance;
    }
}
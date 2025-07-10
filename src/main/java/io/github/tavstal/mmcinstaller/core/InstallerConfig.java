package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.utils.YamlHelper;

import java.util.Map;

/**
 * Handles the configuration for the installer.
 * Provides methods to initialize and retrieve configuration values from a YAML file.
 */
public class InstallerConfig {
    private boolean _isInitialized; // Indicates whether the configuration has been initialized.
    private Map<String, Object> _config; // Stores the parsed configuration data.

    /**
     * Initializes the configuration by reading from the `config.yaml` resource file.
     * Throws a RuntimeException if the configuration cannot be loaded.
     */
    public void Initialize() {
        if (_isInitialized) {
            return;
        }
        _isInitialized = true;

        _config = YamlHelper.readFromResource("config.yaml", InstallerApplication.class);
        if (_config == null) {
            throw new RuntimeException("Failed to load configuration from config.yaml");
        }
    }

    /**
     * Retrieves the entire configuration map.
     *
     * @return A Map containing the configuration data.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public Map<String, Object> getConfig() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return _config;
    }

    /**
     * Retrieves the default language from the configuration.
     *
     * @return The default language as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getDefaultLanguage() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "lang", "eng");
    }

    /**
     * Retrieves the debug mode flag from the configuration.
     *
     * @return True if debug mode is enabled, false otherwise.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public boolean getDebugMode() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getBoolean(_config, "debug", false);
    }

    /**
     * Retrieves the URL for downloading the JAR file.
     *
     * @return The JAR download URL as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getJarDownloadUrl() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "download.link");
    }

    /**
     * Retrieves the name of the JAR file to be downloaded.
     *
     * @return The JAR file name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getJarDownloadName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "download.file_name");
    }

    /**
     * Retrieves the SHA-256 checksum for the JAR file.
     *
     * @return The JAR file checksum as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getJarDownloadChecksum() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "download.sha256");
    }

    /**
     * Retrieves the default directory name for the Start Menu from the configuration.
     *
     * @return The Start Menu default directory name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getStartMenuDefaultDirName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.dirs.start_menu");
    }

    /**
     * Retrieves the default directory name for the AppData folder from the configuration.
     *
     * @return The AppData default directory name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getAppdataDefaultDirName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.dirs.appdata");
    }

    /**
     * Retrieves the name of the batch file to be created during installation.
     *
     * @return The batch file name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getBatchFileName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.batch.file_name");
    }

    /**
     * Retrieves the content of the batch file to be created during installation.
     *
     * @return The batch file content as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getBatchFileContent() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.batch.content");
    }

    /**
     * Retrieves the name of the bash script file to be created during installation.
     *
     * @return The bash file name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getBashFileName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.bash.file_name");
    }

    /**
     * Retrieves the content of the bash script file to be created during installation.
     *
     * @return The bash file content as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getBashFileContent() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.bash.content");
    }

    /**
     * Retrieves the name of the Zsh script file to be created during installation.
     *
     * @return The Zsh file name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getZshFileName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.zsh.file_name");
    }

    /**
     * Retrieves the content of the Zsh script file to be created during installation.
     *
     * @return The Zsh file content as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getZshFileContent() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.zsh.content");
    }

    /**
     * Retrieves the name of the executable file to be created during installation.
     *
     * @return The executable file name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getExeFileName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.exe.file_name");
    }

    /**
     * Retrieves the resource path for the executable file.
     *
     * @return The executable file resource path as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getExeFileResourcePath() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.exe.resource_path");
    }

    /**
     * Retrieves the name of the Linux desktop file to be created during installation.
     *
     * @return The Linux desktop file name as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getLinuxDesktopFileName() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.linux_desktop.file_name");
    }

    /**
     * Retrieves the content of the Linux desktop file to be created during installation.
     *
     * @return The Linux desktop file content as a String.
     * @throws IllegalStateException if the configuration is not initialized.
     */
    public String getLinuxDesktopFileContent() {
        if (!_isInitialized) {
            throw new IllegalStateException("InstallerConfig is not initialized. Call Initialize() first.");
        }
        return YamlHelper.getString(_config, "install.linux_desktop.content");
    }
}
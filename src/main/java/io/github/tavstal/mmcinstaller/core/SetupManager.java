package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import io.github.tavstal.mmcinstaller.utils.ScriptUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The `SetupManager` class is responsible for managing the setup process of the application.
 * It handles tasks such as logging, localization, and managing installation directories and files.
 */
public class SetupManager {
    private final InstallerLogger _logger; // Logger instance for logging setup-related messages
    private final InstallerTranslator _translator; // Translator instance for localizing messages
    private final File _jarFile; // Reference to the JAR file being installed
    private final File _installDir; // Directory where the application will be installed
    private final File _startMenuDir; // Directory for creating start menu shortcuts (if applicable)
    private final String _os; // Operating system name in lowercase
    private final Consumer<String> _logCallback;

    /**
     * Constructs a new `SetupManager` instance.
     *
     * @param jarFile      The JAR file being installed.
     * @param installDir   The directory where the application will be installed.
     * @param startMenuDir The directory for creating start menu shortcuts (if applicable).
     * @param logCallback  A callback function for logging messages during the setup process.
     */
    public SetupManager(File jarFile, File installDir, File startMenuDir, Consumer<String> logCallback) {
        this._logger = InstallerApplication.getLogger().WithModule(this.getClass());
        this._translator = InstallerApplication.getTranslator();
        this._jarFile = jarFile;
        this._installDir = installDir;
        this._startMenuDir = startMenuDir;
        this._logCallback = logCallback;
        this._os = System.getProperty("os.name").toLowerCase();
    }

    /**
     * Sets up the application environment based on the operating system.
     * This method handles tasks such as creating necessary directories, copying resources,
     * and configuring platform-specific settings (Windows, macOS, or Linux).
     * It also creates an uninstaller configuration file and sets the active scene to "Install Complete".
     */
    public void setup() {
        String installPath = _installDir.getAbsolutePath();

        try {
            // Ensure the shortcut directory exists
            if (_startMenuDir != null && !_startMenuDir.exists()) {
                if (!_startMenuDir.mkdirs()) {
                    // Log and notify if the start menu directory creation fails
                    _logger.Error("Failed to create start menu directory: " + _startMenuDir.getAbsolutePath());
                    _logCallback.accept(_translator.Localize("IO.Directory.CreateError", Map.of(
                            "path", _startMenuDir.getAbsolutePath(),
                            "error", "?"
                    )));
                    return;
                }
            }

            // Copy common resources
            // Moved icons to their own OS-specific setup methods
            // to avoid unnecessary copying and bloating the installation directory
            FileUtils.copyResource(_installDir.getAbsolutePath(),"info.txt", "info.txt");

            // Perform OS-specific setup
            if (_os.contains("win")) { // WINDOWS
                File icoFile = FileUtils.copyResource(_installDir.getAbsolutePath(),"assets/favicon.ico", "icon.ico");
                _logCallback.accept(_translator.Localize("Common.DetectedOS", Map.of("os", "Windows")));
                // Create the batch script file
                ScriptUtils.createFile(
                        _installDir.getAbsolutePath(),
                        ConfigLoader.get().install().batch().fileName(),
                        ConfigLoader.get().install().batch().content()
                                .replaceAll("%dirPath%", installPath.replace("\\", "\\\\"))
                                .replaceAll("%jarPath%", _jarFile.getAbsolutePath().replace("\\", "\\\\"))
                );
                // Setup Windows-specific configurations
                SetupWindowsHelper.setup(_installDir, _startMenuDir, icoFile);
            } else if (_os.contains("mac")) { // MAC OS
                _logCallback.accept(_translator.Localize("Common.DetectedOS", Map.of("os", "MacOS")));
                // Setup macOS-specific configurations
                SetupMacOsHelper.setup(_installDir, _startMenuDir, _jarFile);
            } else {  // LINUX
                if (_os.contains("linux"))
                    _logCallback.accept(_translator.Localize("Common.DetectedOS", Map.of("os", "Linux")));
                else
                    _logCallback.accept(_translator.Localize("Common.UnsupportedOS", Map.of("os", _os)));

                FileUtils.copyResource(_installDir.getAbsolutePath(),"assets/icon.png", "icon.png");

                // Create the bash script file
                File scriptFile = ScriptUtils.createFile(
                        _installDir.getAbsolutePath(),
                        ConfigLoader.get().install().bash().fileName(),
                        ConfigLoader.get().install().bash().content()
                                .replaceAll("%dirPath%", installPath)
                                .replaceAll("%jarPath%", _jarFile.getAbsolutePath())
                );
                // Set the application launch path
                InstallerState.setApplicationToLaunch(scriptFile.getAbsolutePath());
                // Setup Linux-specific configurations
                SetupLinuxHelper.setup(_installDir, _startMenuDir, _jarFile);
            }
            // Create the uninstaller configuration file
            // Depends on the OS specific setup.
            createUninstallerConfig();
        } catch (Exception ex) {
            // Log and notify if the setup process fails
            _logger.Error("Setup failed: " + ex.getMessage());
            _logCallback.accept(ex.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.SetupFailed"));
        }

        // Set the active scene to "Install Complete"
        InstallerApplication.setActiveScene(SceneManager.getInstallCompleteScene());
    }

    /**
     * Creates the uninstaller configuration file for the application.
     * <br/>
     * This method generates the content for the uninstaller configuration file
     * based on the operating system and writes it to the appropriate location.
     * <br/>
     * On Windows, paths are escaped with double backslashes. On other operating systems,
     * paths are used as-is. If the file creation fails, an error is logged.
     */
    private void createUninstallerConfig() {
        String content;
        if (_os.contains("win")) {
            // Generate the uninstaller configuration content for Windows
            content = ConfigLoader.get().uninstallerConfig()
                    .replace("%installDir%", InstallerState.getCurrentPath().replace("\\", "\\\\"))
                    .replace("%startMenuDir%", InstallerState.getStartMenuPath().replace("\\", "\\\\"))
                    .replace("%desktopShortcut%", InstallerState.getShortcutPath().replace("\\", "\\\\"))
                    .replace("%startMenuShortcut%", InstallerState.getStartMenuShortcutPath().replace("\\", "\\\\"));
        } else {
            // Generate the uninstaller configuration content for other operating systems
            content = ConfigLoader.get().uninstallerConfig()
                    .replace("%installDir%", InstallerState.getCurrentPath())
                    .replace("%startMenuDir%", InstallerState.getStartMenuPath())
                    .replace("%desktopShortcut%", InstallerState.getShortcutPath())
                    .replace("%startMenuShortcut%", InstallerState.getStartMenuShortcutPath());
        }
        // Get the file path for the uninstaller configuration
        File uninstallerConfigFile = PathUtils.getUninstallerConfigFile();
        try {
            // Write the content to the uninstaller configuration file
            Files.writeString(uninstallerConfigFile.toPath().toAbsolutePath(), content);
            _logCallback.accept("Uninstaller configuration file created: " + uninstallerConfigFile.getAbsolutePath());
        } catch (Exception ex) {
            // Log an error if the file writing fails
            _logger.Error("Failed to write uninstaller configuration file: " + ex.getMessage());
            _logCallback.accept("Failed to write uninstaller configuration file: " + ex.getMessage());
        }
    }


}

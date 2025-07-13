package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The `SetupManager` class is responsible for managing the setup process of the application.
 * It handles tasks such as creating necessary directories, copying resources, and configuring
 * platform-specific settings (Windows, macOS, or Linux).
 */
public class SetupManager {
    private final InstallerLogger _logger; // Logger instance for logging setup-related messages
    private final InstallerTranslator _translator; // Translator instance for localizing messages
    private final Consumer<String> _logCallback; // Callback function for logging messages to the user interface
    private final File _jarFile; // Reference to the JAR file being installed
    private final File _installDir; // Directory where the application will be installed
    private final File _startMenuDir; // Directory for creating start menu shortcuts (if applicable)
    private final String _os; // Operating system name in lowercase

    /**
     * Constructs a new `SetupManager` instance.
     *
     * @param jarFile The JAR file to be installed.
     * @param installDir The directory where the application will be installed.
     * @param startMenuDir The directory for creating start menu shortcuts (if applicable).
     * @param logCallback A callback function for logging messages to the user interface.
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

    // --- Main Setup Method ---

    /**
     * Sets up the application by creating necessary directories, copying resources,
     * and configuring platform-specific settings (Windows, macOS, or Linux).
     * <br/>
     * This method ensures that the installation directory is properly prepared,
     * resources are copied, and OS-specific setup methods are invoked.
     * <br/>
     * Handles errors during the setup process by logging and notifying the user.
     */
    public void setup() {
        String installPath = _installDir.getAbsolutePath();

        try {
            // Ensure the shortcut directory exists
            if (_startMenuDir != null && !_startMenuDir.exists()) {
                if (!_startMenuDir.mkdirs()) {
                    // Log and notify if the start menu directory creation fails
                    _logger.Error("Failed to create start menu directory: " + _startMenuDir.getAbsolutePath());
                    _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuDirCreationFailed", Map.of("path", _startMenuDir.getAbsolutePath())));
                    return;
                }
            }

            // Copy common resources
            // Moved icons to their own OS-specific setup methods
            // to avoid unnecessary copying and bloating the installation directory
            copyResource("info.txt", "info.txt");

            // Perform OS-specific setup
            if (_os.contains("win")) { // WINDOWS
                File icoFile = copyResource("assets/favicon.ico", "icon.ico");
                _logCallback.accept(_translator.Localize("Progress.Scripts.DetectedOS", Map.of("os", "Windows")));
                // Create the batch script file
                createScriptFile(
                        ConfigLoader.get().install().batch().fileName(),
                        ConfigLoader.get().install().batch().content()
                                .replaceAll("%dirPath%", installPath.replace("\\", "\\\\"))
                                .replaceAll("%jarPath%", _jarFile.getAbsolutePath().replace("\\", "\\\\"))
                );
                // Setup Windows-specific configurations
                setupWindows(icoFile);
            } else if (_os.contains("mac")) { // MAC OS
                _logCallback.accept(_translator.Localize("Progress.Scripts.DetectedOS", Map.of("os", "MacOS")));
                // Create the zsh script file
                /*File scriptFile = createScriptFile(
                        ConfigLoader.get().install().zsh().fileName(),
                        ConfigLoader.get().install().zsh().content()
                                .replaceAll("%dirPath%", installPath)
                                .replaceAll("%jarPath%", _jarFile.getAbsolutePath())
                );*/

                // Setup macOS-specific configurations
                setupMac();
            } else {  // LINUX
                if (_os.contains("linux"))
                    _logCallback.accept(_translator.Localize("Progress.Scripts.DetectedOS", Map.of("os", "Linux")));
                else
                    _logCallback.accept(_translator.Localize("Progress.Scripts.UnsupportedOS", Map.of("os", _os)));

                copyResource("assets/icon.png", "icon.png");

                // Create the bash script file
                File scriptFile = createScriptFile(
                        ConfigLoader.get().install().bash().fileName(),
                        ConfigLoader.get().install().bash().content()
                                .replaceAll("%dirPath%", installPath)
                                .replaceAll("%jarPath%", _jarFile.getAbsolutePath())
                );
                // Set the application launch path
                InstallerState.setApplicationToLaunch(scriptFile.getAbsolutePath());
                // Setup Linux-specific configurations
                setupLinux();
            }
        } catch (Exception ex) {
            // Log and notify if the setup process fails
            _logger.Error("Setup failed: " + ex.getMessage());
            _logCallback.accept(ex.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.SetupFailed"));
        }

        // Set the active scene to "Install Complete"
        InstallerApplication.setActiveScene(SceneManager.getInstallCompleteScene());
    }

    // --- OS Specific Setup Methods ---

    /**
     * Sets up Windows-specific configurations for the application, including creating
     * a shortcut file (`.lnk`) and optionally copying it to the desktop and start menu.
     * <br/>
     * This method handles the creation of a PowerShell script to generate the shortcut,
     * executes the script, and manages error handling and logging during the process.
     *
     * @param iconIcoPath The path to the `.ico` file used as the icon for the shortcut.
     */
    private void setupWindows(File iconIcoPath) {
        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Retrieve the name of the executable file from the configuration
        String exeFileName = ConfigLoader.get().install().exe().fileName();

        // Define the path for the shortcut file
        File shortcutPath = new File(_installDir, "MesterMC.lnk");

        // Copy the executable file from resources to the installation directory
        File exeFile = copyResource(ConfigLoader.get().install().exe().resourcePath(), exeFileName);
        if (exeFile == null) {
            _logger.Warn("Executable file not found: " + exeFileName);
            return;
        }
        // Set the application launch path to the executable file
        InstallerState.setApplicationToLaunch(exeFile.getAbsolutePath());

        // Create the shortcut using a PowerShell script
        try {
            // Generate the PowerShell script content
            String powershellScript = ConfigLoader.get().install().exe().powershell()
                    .replace("%shortcutPath%", shortcutPath.getAbsolutePath().replace("\\", "\\\\"))
                    .replace("%exePath%", exeFile.getAbsolutePath().replace("\\", "\\\\"))
                    .replace("%iconPath%", iconIcoPath.getAbsolutePath().replace("\\", "\\\\"));

            // Save the script to a temporary `.ps1` file
            File ps1File = new File(System.getProperty("java.io.tmpdir"), "create_shortcut.ps1");
            Files.writeString(ps1File.toPath(), powershellScript);

            // Execute the PowerShell script
            Process process = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", ps1File.getAbsolutePath()).start();
            int exitCode = process.waitFor();
            _logger.Debug("PowerShell script executed with exit code: " + exitCode);

            // Clean up the temporary script file
            if (!ps1File.delete())
                _logger.Warn("Failed to delete temporary PowerShell script: " + ps1File.getAbsolutePath());
        } catch (IOException | InterruptedException e) {
            // Log an error if the PowerShell script execution fails
            _logger.Error("Failed to create Windows shortcut: " + e.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.ShortcutCreationError", Map.of("error", e.getMessage())));
            return;
        }

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, "MesterMC.lnk");
        // Define the start menu shortcut file
        File startMenuFile = new File(_startMenuDir, "MesterMC.lnk");

        // Copy the shortcut to the desktop and start menu
        try {
            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                _logger.Debug("Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                // Copy the shortcut file to the desktop directory
                Files.copy(shortcutPath.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Log the creation of the desktop shortcut
                _logCallback.accept(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", Map.of("filePath", desktopShortcutFile.getAbsolutePath())));
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                // Copy the shortcut file to the start menu directory
                Files.copy(shortcutPath.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Log the creation of the start menu shortcut
                _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", Map.of("filePath", startMenuFile.getAbsolutePath())));
            }

            // Delete the original shortcut file in the installation directory
            // This is done to avoid cluttering the installation directory with the shortcut file
            if (shortcutPath.exists() && !shortcutPath.delete()) {
                _logger.Warn("Failed to delete original shortcut file: " + shortcutPath.getAbsolutePath());
            }
        } catch (IOException e) {
            // Log an error if copying the shortcut files fails
            _logger.Error("Failed to copy shortcut files: " + e.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.ShortcutCopyError", Map.of("error", e.getMessage())));
        }

        // Create the uninstallation script file
        createScriptFile(
                ConfigLoader.get().uninstall().batch().fileName(),
                ConfigLoader.get().uninstall().batch().content()
                        .replaceAll("%installDir%", _installDir.getAbsolutePath().replace("\\", "\\\\"))
                        .replaceAll("%desktopShortcut%", desktopShortcutFile.getAbsolutePath().replace("\\", "\\\\"))
                        .replaceAll("%startmenuShortcut%", startMenuFile.getAbsolutePath().replace("\\", "\\\\"))
        );

        createUninstallerConfig();
    }

    /**
     * Sets up Linux-specific configurations for the application, including creating
     * a `.desktop` file and optionally creating desktop and start menu shortcuts.
     * <br/>
     * The `.desktop` file is used to define how the application is launched on Linux systems.
     * This method also handles logging and error reporting during the setup process.
     */
    private void setupLinux() {
        // Get the name and content of the .desktop file from the configuration
        String desktopFileName = ConfigLoader.get().install().linuxDesktop().fileName();
        String desktopFileContent = ConfigLoader.get().install().linuxDesktop().content()
                .replaceAll("%dirPath%", _installDir.getAbsolutePath()) // Replace placeholder with the installation path
                .replaceAll("%jarPath%", _jarFile.getAbsolutePath()); // Replace placeholder with the JAR file path
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Define the .desktop file in the installation directory
        File linuxLaunchFile = new File(_installDir, desktopFileName);

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, desktopFileName);
        // Define the start menu shortcut file
        File startMenuFile = new File(_startMenuDir, desktopFileName);
        try {
            // Log the creation of the .desktop file
            _logger.Debug("Creating .desktop file: " + linuxLaunchFile.getAbsolutePath());
            // Write the content to the .desktop file
            Files.writeString(linuxLaunchFile.toPath(), desktopFileContent);

            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                _logger.Debug("Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                // Copy the .desktop file to the desktop directory
                Files.copy(linuxLaunchFile.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Log the creation of the desktop shortcut
                _logCallback.accept(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", Map.of("filePath", desktopShortcutFile.getAbsolutePath())));
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                // Copy the .desktop file to the start menu directory
                Files.copy(linuxLaunchFile.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Log the creation of the start menu shortcut
                _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", Map.of("filePath", startMenuFile.getAbsolutePath())));
            }
        } catch (IOException e) {
            // Log an error if the .desktop file creation or shortcut creation fails
            _logger.Error("Failed to write .desktop file: " + e.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.DesktopFileCreationError", Map.of("error", e.getMessage())));
        }

        // Create the uninstallation script file
        createScriptFile(
                ConfigLoader.get().uninstall().bash().fileName(),
                ConfigLoader.get().uninstall().bash().content()
                        .replaceAll("%installDir%", _installDir.getAbsolutePath())
                        .replaceAll("%desktopShortcut%", desktopShortcutFile.getAbsolutePath())
                        .replaceAll("%startmenuShortcut%", startMenuFile.getAbsolutePath())
        );

        createUninstallerConfig();
    }

    /**
     * Sets up macOS-specific configurations for the application, including creating
     * a macOS app bundle, `.app` directory structure, and optionally creating desktop
     * and start menu shortcuts.
     * <br/>
     * This method handles the creation of the macOS app bundle, including the `Info.plist` file,
     * launcher script, and icon file. It also ensures the launcher script is executable and
     * manages symlinks for shortcuts.
     * <br/>
     * Logs and handles errors during the setup process.
     */
    private void setupMac() {
        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();
        // Retrieve the name of the macOS app bundle from the configuration
        String desktopFileName = ConfigLoader.get().install().macApp().fileName();

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, desktopFileName);
        // Define the start menu shortcut file
        File startMenuFile = new File(_startMenuDir, desktopFileName);
        // Copy the .icns icon file from resources
        String iconFileName = "icon.icns";
        File icnsFile = copyResource("assets/icon.icns", iconFileName);

        try {
            // Log the creation of the macOS app bundle
            _logger.Debug("Creating macOS app bundle: " + desktopFileName);
            String launcherScriptContent = ConfigLoader.get().install().macApp().script()
                    .replaceAll("%dirPath%", _installDir.getAbsolutePath())
                    .replaceAll("%jarPath%", _jarFile.getAbsolutePath());
            Path launchAppBundlePath = createAppBundle(
                    desktopFileName,
                    iconFileName,
                    icnsFile,
                    launcherScriptContent
            );
            if (launchAppBundlePath != null) {
                // Log the successful creation of the macOS app bundle
                _logger.Debug("Created macOS app bundle at: " + launchAppBundlePath.toAbsolutePath());
                InstallerState.setApplicationToLaunch(launchAppBundlePath.toFile().getAbsolutePath());

                // Check if a desktop shortcut should be created
                if (InstallerState.shouldCreateDesktopShortcut()) {
                    _logger.Debug("Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                    // Create a copy of the original .app bundle
                    //Files.copy(launchAppBundlePath.toAbsolutePath(), desktopShortcutFile.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
                    PathUtils.copyDirectory(launchAppBundlePath.toAbsolutePath(), desktopShortcutFile.toPath().toAbsolutePath());
                    // Log the creation of the desktop shortcut
                    _logCallback.accept(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", Map.of("filePath", desktopShortcutFile.getAbsolutePath())));
                }

                // Check if a start menu shortcut should be created
                if (InstallerState.shouldCreateStartMenuShortcut()) {
                    _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                    // Create a copy of the original .app bundle
                    //Files.copy(launchAppBundlePath.toAbsolutePath(), startMenuFile.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
                    PathUtils.copyDirectory(launchAppBundlePath.toAbsolutePath(), startMenuFile.toPath().toAbsolutePath());
                    // Log the creation of the start menu shortcut
                    _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", Map.of("filePath", startMenuFile.getAbsolutePath())));
                }
            }
            else {
                // Log an error if the app bundle creation failed
                _logger.Error("Failed to create macOS app bundle.");
                _logCallback.accept(_translator.Localize("Progress.Scripts.MacAppBundleCreationError"));
            }

            // Create Uninstaller App Bundle
            Path uninstallAppBundlePath = createAppBundle(
                    ConfigLoader.get().uninstall().zsh().fileName(),
                    iconFileName,
                    icnsFile,
                    ConfigLoader.get().uninstall().zsh().content()
                            .replaceAll("%installDir%", _installDir.getAbsolutePath())
                            .replaceAll("%desktopShortcut%", desktopShortcutFile.getAbsolutePath())
                            .replaceAll("%startmenuShortcut%", startMenuFile.getAbsolutePath())
            );
            if (uninstallAppBundlePath == null) {
                // Log an error if the uninstaller app bundle creation failed
                _logger.Error("Failed to create macOS uninstaller app bundle.");
                _logCallback.accept(_translator.Localize("Progress.Scripts.MacUninstallerBundleCreationError"));
            } else {
                // Log the successful creation of the uninstaller app bundle
                _logger.Debug("Created macOS uninstaller app bundle at: " + uninstallAppBundlePath.toAbsolutePath());
            }
        }
        catch (Exception ex) {
            // Log an error if the macOS app bundle creation fails
            _logger.Error("Failed to create macOS app bundle: " + ex.getMessage());
        }

        createUninstallerConfig();
    }

    // --- Common Methods ---

    private void createUninstallerConfig() {

    }

    /**
     * Copies a resource file from the application's resources to the installation directory.
     * If the resource is not found, logs an error and stops the operation.
     * If the resource is successfully copied, logs the success.
     *
     * @param resourcePath The path to the resource file within the application's resources.
     * @param targetFileName The name of the target file to be created in the installation directory.
     */
    private File copyResource(String resourcePath, String targetFileName) {
        // Define the target file in the installation directory
        File targetFile = new File(_installDir, targetFileName);
        try (InputStream resourceStream = InstallerApplication.class.getResourceAsStream(resourcePath)) {
            // Check if the resource exists
            if (resourceStream == null) {
                _logCallback.accept(_translator.Localize("Progress.Scripts.ResourceNotFound", Map.of("resource", targetFile.getAbsolutePath())));
                return null;
            }
            // Copy the resource to the target file
            Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopied", Map.of("fileName", targetFileName)));
        } catch (IOException e) {
            // Log an error if the copy operation fails
            _logger.Error(String.format("Failed to copy %s: %s", targetFileName, e.getMessage()));
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopyError", Map.of("fileName", targetFileName, "error", e.getMessage())));
        }
        return targetFile;
    }

    /**
     * Creates a script file with the specified name and content in the installation directory.
     * If the operating system is Linux or macOS, the script is made executable.
     * After successful creation, the application switches to the "Install Complete" scene.
     *
     * @param fileName The name of the script file to be created.
     * @param content The content to be written into the script file.
     */
    private File createScriptFile(String fileName, String content) {
        // Define the script file in the installation directory
        File scriptFile = new File(_installDir, fileName);
        try {
            // Write the content to the script file
            Files.writeString(scriptFile.toPath(), content);
            _logger.Debug("Created script: " + scriptFile.getAbsolutePath());
            _logCallback.accept(_translator.Localize("Progress.Scripts.LauncherCreated", Map.of("filePath", scriptFile.getAbsolutePath())));

            // If the OS is Linux or macOS, make the script executable
            if (_os.contains("linux") || _os.contains("mac")) {
                makeScriptExecutable(scriptFile);
            }
        } catch (IOException e) {
            // Log an error if the script file creation fails
            _logger.Error("Failed to write scripts: " + e.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.LauncherCreationError", Map.of("error", e.getMessage())));
        }
        return scriptFile;
    }

    /**
     * Makes a script file executable by using the `chmod` command.
     * This method attempts to set the executable permission on the provided script file
     * and logs the process, including any errors or timeouts.
     *
     * @param scriptFile The script file to make executable.
     */
    private void makeScriptExecutable(File scriptFile) {
        try {
            // Log the start of the process
            _logger.Debug("Attempting to make script executable: " + scriptFile.getAbsolutePath());
            _logCallback.accept("Making launcher executable: " + scriptFile.getAbsolutePath());
            _logCallback.accept(_translator.Localize("Progress.Scripts.MakingExecutable"));

            // Create a ProcessBuilder to execute the chmod command
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", scriptFile.getAbsolutePath());
            Process p = pb.start();

            // Wait for the process to complete, with a timeout of 10 seconds
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);

            if (finished) {
                int exitCode = p.exitValue(); // Get the exit code of the process
                if (exitCode == 0) {
                    // Log success if chmod executed successfully
                    _logger.Info("Script made executable: " + scriptFile.getAbsolutePath());
                    _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableMade", Map.of("filePath", scriptFile.getAbsolutePath())));
                } else {
                    // Read and log the error stream if chmod failed
                    String error = new String(p.getErrorStream().readAllBytes());
                    _logger.Error("chmod failed with exit code " + exitCode + ": " + error);
                    _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableError", Map.of(
                            "filePath", scriptFile.getAbsolutePath(),
                            "error", error
                    )));
                }
            } else {
                // Log a timeout error if the process did not finish within the timeout
                _logger.Error("chmod process timed out.");
                _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableTimeout"));
                p.destroyForcibly(); // Forcefully terminate the process
            }
        } catch (IOException | InterruptedException e) {
            // Log any exceptions that occur during the process
            _logger.Error("Exception while making script executable: " + e.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableException", Map.of("error", e.getMessage())));
        }
    }

    /**
     * Creates a macOS app bundle with the specified structure and content.
     * <br/>
     * This method generates the `.app` directory structure, writes the `Info.plist` file,
     * creates a launcher script, sets its permissions, and copies the icon file to the
     * appropriate location within the bundle.
     * <br/>
     * Logs errors if the app bundle creation fails.
     *
     * @param desktopFileName The name of the macOS app bundle directory.
     * @param icnsFileName The name of the `.icns` icon file to be used in the app bundle.
     * @param icnsFile The `.icns` file to be copied into the app bundle.
     * @param scriptContent The content of the launcher script to be created in the app bundle.
     * @return The path to the created app bundle, or `null` if the creation fails.
     */
    private Path createAppBundle(String desktopFileName, String icnsFileName, File icnsFile, String scriptContent) {
        Path installDir = _installDir.toPath();
        Path appBundlePath = installDir.resolve(desktopFileName);
        try {
            File appBundleFile = appBundlePath.toFile();
            if (appBundleFile.exists()) {
                // If the app bundle already exists, delete it before creating a new one
                if (appBundleFile.isDirectory())
                    PathUtils.deleteDirectory(appBundlePath);
                else
                    Files.delete(appBundlePath);
            }

            // 1. Create the main .app bundle directory
            Files.createDirectories(appBundlePath);

            // 2. Create Contents directory
            Path contentsPath = appBundlePath.resolve("Contents");
            Files.createDirectories(contentsPath);

            // 3. Create MacOS directory
            Path macOSPath = contentsPath.resolve("MacOS");
            Files.createDirectories(macOSPath);

            // 4. Create Resources directory
            Path resourcesPath = contentsPath.resolve("Resources");
            Files.createDirectories(resourcesPath);

            // --- Write Info.plist ---
            // Retrieve the Info.plist content from the configuration
            String infoPlistContent = ConfigLoader.get().install().macApp().infoList();
            if (icnsFile != null) {
                // Replace the icon path placeholder with the actual icon file name
                infoPlistContent = infoPlistContent.replaceAll("%iconPath%", icnsFileName);
            } else {
                // Log a warning if the icon file is not found
                _logger.Warn("No icon file found for macOS app bundle.");
            }

            // Write the Info.plist content to the Contents directory
            Files.writeString(contentsPath.resolve("Info.plist"), infoPlistContent);

            // --- Write execute.sh ---
            // Write the launcher script to the MacOS directory
            Path launcherScriptPath = macOSPath.resolve("execute.sh");
            Files.writeString(launcherScriptPath, scriptContent);

            // --- Make launcher.sh executable ---
            // Define the permissions for the launcher script
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            // Set the permissions on the launcher script
            Files.setPosixFilePermissions(launcherScriptPath, perms);

            // Copy the .icns icon file into the Resources directory
            if (icnsFile != null && icnsFile.exists()) {
                Files.copy(icnsFile.toPath(), resourcesPath.resolve(icnsFileName));
            }
        }
        catch (IOException ex) {
            // Log an error if the app bundle creation fails
            _logger.Error("Failed to create macOS app bundle: " + ex.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.MacAppBundleCreationError", Map.of("error", ex.getMessage())));
            return null;
        }
        return appBundlePath;
    }
}

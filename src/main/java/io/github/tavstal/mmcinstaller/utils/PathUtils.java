package io.github.tavstal.mmcinstaller.utils;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import io.github.tavstal.mmcinstaller.InstallerApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Utility class for handling file paths and directories across different operating systems.
 * Provides methods to retrieve OS-specific directories such as the Start Menu and Desktop.
 */
public class PathUtils {

    /**
     * Determines the default installation path for the application, following OS-specific conventions.
     * This path is typically used for storing application binaries, data, or configuration files.
     *
     * @param appName The name of your application, used as a subfolder name.
     * @return A File object representing the recommended installation directory.
     */
    public static File getDefaultInstallationPath(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File installationDir;
        var logger = InstallerApplication.getLogger();

        if (os.contains("win")) {
            // Windows: C:\Users\[User]\AppData\Roaming\[YourAppName]
            // %APPDATA% (Roaming AppData) is generally not localized.
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                installationDir = new File(appData, appName);
            } else {
                // Fallback if APPDATA env var is not set (unlikely for a typical user)
                logger.Error("APPDATA environment variable not found. Falling back to user home for installation path.");
                installationDir = new File(userHome, appName);
            }
        } else if (os.contains("linux")) {
            // Linux: ~/.local/share/[YourAppName] (XDG_DATA_HOME)
            // This is the standard, non-localized path for user-specific data files.
            String xdgDataHome = getXdgUserDir("XDG_DATA_HOME");
            if (xdgDataHome == null || xdgDataHome.isEmpty()) {
                xdgDataHome = userHome + File.separator + ".local" + File.separator + "share";
            }
            installationDir = new File(xdgDataHome, appName);
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/[YourAppName]
            // This is the standard, non-localized path for application support files and data.
            installationDir = new File(userHome, "Library" + File.separator + "Application Support" + File.separator + appName);
        } else {
            // Generic fallback for other OS types
            logger.Warn("Warning: Unrecognized OS for installation path: " + os + ". Falling back to user home.");
            installationDir = new File(userHome, appName);
        }

        // Ensure the installation directory exists
        if (!installationDir.exists()) {
            if (!installationDir.mkdirs()) {
                logger.Error("Failed to create installation directory: " + installationDir.getAbsolutePath());
                // Consider throwing an IOException or returning null if creation is critical
            }
        }
        return installationDir;
    }

    /**
     * Retrieves the Start Menu directory for the current user, handling OS-specific conventions.
     * On Windows, uses JNA to get the localized "Programs" folder within the Start Menu.
     * On Linux and macOS, falls back to standard application directories.
     *
     * @param shortcutFolderName The name of the folder to create or locate within the Start Menu.
     * @return A File object representing the Start Menu directory, or a fallback directory if unavailable.
     */
    public static File getStartMenuDirectory(String shortcutFolderName) {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File targetDirectory = null;
        var logger = InstallerApplication.getLogger();

        if (os.contains("win")) {
            try {
                // Windows: Use JNA to get the localized "Programs" folder.
                String programsPath = Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Programs);
                if (programsPath != null && !programsPath.isEmpty()) {
                    if (shortcutFolderName == null || shortcutFolderName.isEmpty()) {
                        targetDirectory = new File(programsPath);
                    }
                    else {
                        targetDirectory = new File(programsPath, shortcutFolderName);
                    }
                }
            } catch (Throwable e) {
                // Fallback for Windows if JNA fails.
                logger.Warn("JNA failed to get Windows Programs folder. Falling back to APPDATA. Error: " + e.getMessage());
                String appData = System.getenv("APPDATA");
                if (appData != null && !appData.isEmpty()) {
                    targetDirectory = new File(appData, "Microsoft" + File.separator + "Windows" +
                            File.separator + "Start Menu" + File.separator + "Programs" +
                            File.separator + shortcutFolderName);
                } else {
                    if (shortcutFolderName == null || shortcutFolderName.isEmpty()) {
                        targetDirectory = new File(userHome);
                    } else {
                        targetDirectory = new File(userHome, shortcutFolderName);
                    }
                }
            }
        } else if (os.contains("linux")) {
            // Linux: Standard directory for user-specific .desktop files.
            targetDirectory = new File(userHome, ".local" + File.separator + "share" + File.separator + "applications");
        } else if (os.contains("mac")) {
            // macOS: Standard directory for .app bundles.
            targetDirectory = new File("/Applications");
        } else {
            // Fallback for unknown OS.
            logger.Warn("Unrecognized OS or unable to determine standard launcher directory for " + os + ". Falling back to user home.");
            targetDirectory = new File(userHome, shortcutFolderName);
        }

        // Ensure the target directory exists.
        if (targetDirectory != null && !targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                logger.Error("Failed to create target directory: " + targetDirectory.getAbsolutePath());
            } else {
                logger.Info("Created target directory: " + targetDirectory.getAbsolutePath());
            }
        }
        return targetDirectory;
    }

    /**
     * Retrieves the current user's desktop directory, handling OS-specific conventions and localization.
     * On Linux, attempts to parse the XDG user directories configuration file for the desktop path.
     *
     * @return A File object representing the user's desktop directory, or null if it cannot be determined.
     */
    public static File getUserDesktopDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File desktopDirectory = null;
        var logger = InstallerApplication.getLogger();

        if (os.contains("win")) {
            try {
                // Windows: Use JNA to get the localized "Desktop" folder.
                String desktopPath = Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Desktop);
                if (desktopPath != null && !desktopPath.isEmpty()) {
                    desktopDirectory = new File(desktopPath);
                }
            } catch (Throwable e) {
                logger.Warn("JNA failed to get Windows Desktop folder. Falling back to default. Error: " + e.getMessage());
                desktopDirectory = new File(userHome, "Desktop");
            }
        } else if (os.contains("linux")) {
            // Linux: Parse ~/.config/user-dirs.dirs for the localized desktop path.
            String xdgDesktopPath = getXdgUserDir("XDG_DESKTOP_DIR");
            if (xdgDesktopPath != null) {
                desktopDirectory = new File(xdgDesktopPath);
            } else {
                desktopDirectory = new File(userHome, "Desktop");
            }
        } else if (os.contains("mac")) {
            // macOS: "Desktop" is generally not localized in the file system path.
            desktopDirectory = new File(userHome, "Desktop");
        } else {
            logger.Warn("Unrecognized OS or unable to determine desktop directory for " + os + ". Falling back to user home.");
            desktopDirectory = new File(userHome);
        }

        // Ensure the desktop directory exists.
        if (desktopDirectory != null && !desktopDirectory.exists()) {
            if (!desktopDirectory.mkdirs()) {
                logger.Error("Failed to create desktop directory: " + desktopDirectory.getAbsolutePath());
            }
        }
        return desktopDirectory;
    }

    /**
     * Helper method for Linux to parse XDG user directories from ~/.config/user-dirs.dirs.
     *
     * @param xdgDirName The XDG directory variable name (e.g., "XDG_DESKTOP_DIR").
     * @return The localized path for the XDG directory, or null if not found or an error occurs.
     */
    private static String getXdgUserDir(String xdgDirName) {
        String userHome = System.getProperty("user.home");
        File userDirsFile = new File(userHome, ".config" + File.separator + "user-dirs.dirs");

        if (userDirsFile.exists() && userDirsFile.canRead()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userDirsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(xdgDirName + "=")) {
                        String pathValue = line.substring((xdgDirName + "=").length());
                        // Remove quotes and resolve relative paths.
                        pathValue = pathValue.replaceAll("\"", "");
                        if (pathValue.startsWith("$HOME/")) {
                            return userHome + pathValue.substring("$HOME".length());
                        } else {
                            return Paths.get(userHome, pathValue).normalize().toAbsolutePath().toString();
                        }
                    }
                }
            } catch (IOException e) {
                InstallerApplication.getLogger().Error("Error reading user-dirs.dirs: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Retrieves the path to the uninstaller configuration file based on the operating system.
     * <br/>
     * This method determines the appropriate location for the configuration file
     * (`mestermc_config.yaml`) depending on the OS:
     * <ul>
     *   <li>Windows: Uses the `APPDATA` environment variable or falls back to the user's home directory.</li>
     *   <li>Linux: Places the file in the `.config` directory within the user's home directory.</li>
     *   <li>macOS: Places the file in the `Library/Application Support` directory within the user's home directory.</li>
     *   <li>Other OS: Falls back to the user's home directory.</li>
     * </ul>
     *
     * @return A `File` object representing the path to the uninstaller configuration file.
     */
    public static File getUninstallerConfigFile() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File uninstallerConfigFile;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                uninstallerConfigFile = new File(appData, "mestermc_config.yaml");
            } else {
                uninstallerConfigFile = new File(userHome, "mestermc_config.yaml");
            }
        } else if (os.contains("linux")) {
            uninstallerConfigFile = new File(userHome, ".config" + File.separator + ".mestermc_config.yaml");
        } else if (os.contains("mac")) {
            uninstallerConfigFile = new File(userHome, "Library"+ File.separator +"Application Support"+ File.separator  + ".mestermc_config.yaml");
        } else {
            // Generic fallback for other OS types
            uninstallerConfigFile = new File(userHome, "mestermc_config.yaml");
        }

        return uninstallerConfigFile;
    }
}

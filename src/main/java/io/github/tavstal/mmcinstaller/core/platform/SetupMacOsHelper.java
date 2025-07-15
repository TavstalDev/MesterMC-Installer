package io.github.tavstal.mmcinstaller.core.platform;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.core.logging.FallbackLogger;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Helper class for setting up macOS-specific application environments.
 * Extends the FallbackLogger to provide logging capabilities.
 * This class includes methods for creating macOS app bundles, shortcuts, and uninstallers.
 */
public class SetupMacOsHelper extends FallbackLogger {
    /**
     * Sets up the macOS-specific application environment by creating app bundles, shortcuts,
     * and an uninstaller. This method handles file creation, logging, and error handling.
     *
     * @param installDir   The directory where the application is installed.
     * @param startMenuDir The directory where the start menu shortcut will be created.
     * @param jarFile      The JAR file of the application.
     * @param logCallback  A callback function to log messages during the setup process.
     */
    public static void setup(File installDir, File startMenuDir, File jarFile, Consumer<String> logCallback) {
        InstallerTranslator translator = InstallerApplication.getTranslator();
        String installDirAbPath = installDir.getAbsolutePath();
        Path installDirPath = installDir.toPath();
        var installConfig = ConfigLoader.get().install();
        var uninstallConfig = ConfigLoader.get().uninstall();

        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();
        // Retrieve the name of the macOS app bundle from the configuration
        String desktopFileName = installConfig.macApp().fileName();

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, desktopFileName);
        String desktopShortcutAbPath = desktopShortcutFile.getAbsolutePath();
        InstallerState.setShortcutPath(desktopShortcutAbPath);

        // Define the start menu shortcut file
        File startMenuFile = new File(startMenuDir, desktopFileName);
        String startMenuFileAbPath = startMenuFile.getAbsolutePath();
        InstallerState.setStartMenuShortcutPath(startMenuFileAbPath);

        // Copy the .icns icon file from resources
        String iconFileName = "icon.icns";
        File icnsFile = FileUtils.copyResource(installDirAbPath, "assets/icon.icns", iconFileName);
        if (icnsFile == null) {
            logCallback.accept(translator.Localize("IO.File.CopyError", Map.of(
                    "source", "resources/assets/icon.icns",
                    "destination", installDirAbPath+ File.separator + iconFileName,
                    "error", "?"
            )));
        } else {
            logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                    "source", "resources/assets/icon.icns",
                    "destination", icnsFile.getAbsolutePath()
            )));
        }

        try {
            // Log the creation of the macOS app bundle
            Log(Level.DEBUG, "Creating macOS app bundle: " + desktopFileName);
            String launcherScriptContent = installConfig.macApp().script()
                    .replaceAll("%dirPath%", installDirAbPath)
                    .replaceAll("%jarPath%", jarFile.getAbsolutePath());
            Path launchAppBundlePath = createAppBundle(
                    installDirPath,
                    desktopFileName,
                    iconFileName,
                    icnsFile,
                    launcherScriptContent
            );
            if (launchAppBundlePath != null) {
                // Log the successful creation of the macOS app bundle
                Log(Level.DEBUG, "Created macOS app bundle at: " + launchAppBundlePath.toAbsolutePath());
                InstallerState.setApplicationToLaunch(launchAppBundlePath.toFile().getAbsolutePath());

                // Check if a desktop shortcut should be created
                if (InstallerState.shouldCreateDesktopShortcut()) {
                    Log(Level.DEBUG, "Creating desktop shortcut: " + desktopShortcutAbPath);
                    // Create a copy of the original .app bundle
                    FileUtils.copyDirectory(launchAppBundlePath.toAbsolutePath(), desktopShortcutFile.toPath().toAbsolutePath());
                    logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                            "source", launchAppBundlePath.toAbsolutePath().toString(),
                            "destination", desktopShortcutAbPath
                    )));
                }

                // Check if a start menu shortcut should be created
                if (InstallerState.shouldCreateStartMenuShortcut()) {
                    Log(Level.DEBUG, "Creating start menu shortcut: " + startMenuFileAbPath);
                    // Create a copy of the original .app bundle
                    FileUtils.copyDirectory(launchAppBundlePath.toAbsolutePath(), startMenuFile.toPath().toAbsolutePath());
                    logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                            "source", launchAppBundlePath.toAbsolutePath().toString(),
                            "destination", startMenuFileAbPath
                    )));
                }
            }
            else {
                // Log an error if the app bundle creation failed
                Log(Level.ERROR, "Failed to create macOS app bundle.");
                logCallback.accept(translator.Localize("IO.File.CreateError", Map.of(
                        "path", desktopFileName,
                        "error", "?"
                )));
            }

            // Create Uninstaller App Bundle
            Path uninstallAppBundlePath = createAppBundle(
                    installDirPath,
                    uninstallConfig.zsh().fileName(),
                    iconFileName,
                    icnsFile,
                    uninstallConfig.zsh().content()
                            .replaceAll("%installDir%", installDirAbPath)
                            .replaceAll("%desktopShortcut%", desktopShortcutAbPath)
                            .replaceAll("%startmenuShortcut%", startMenuFileAbPath)
            );
            if (uninstallAppBundlePath == null) {
                // Log an error if the uninstaller app bundle creation failed
                Log(Level.ERROR, "Failed to create macOS uninstaller app bundle.");
                logCallback.accept(translator.Localize("IO.File.CreateError", Map.of(
                        "path", installDirPath + File.separator + uninstallConfig.zsh().fileName(),
                        "error", "?"
                )));

            } else {
                String uninstallAppBundleAbPath = uninstallAppBundlePath.toAbsolutePath().toString();
                // Log the successful creation of the uninstaller app bundle
                Log(Level.DEBUG, "Created macOS uninstaller app bundle at: " + uninstallAppBundleAbPath);
                logCallback.accept(translator.Localize("IO.File.Created", Map.of(
                        "path", uninstallAppBundleAbPath
                )));
            }
        }
        catch (Exception ex) {
            // Log an error if the macOS app bundle creation fails
            Log(Level.ERROR, "Failed to create macOS app bundle: " + ex.getMessage());
            logCallback.accept(ex.getLocalizedMessage());
        }
    }

    /**
     * Creates a macOS app bundle at the specified installation directory.
     * The app bundle includes the necessary structure, an Info.plist file, a launcher script,
     * and an optional icon file. If the app bundle already exists, it is deleted before creation.
     *
     * @param installDir      The directory where the app bundle will be created.
     * @param desktopFileName The name of the app bundle to create.
     * @param icnsFileName    The name of the icon file to include in the app bundle.
     * @param icnsFile        The icon file to copy into the app bundle's Resources directory.
     * @param scriptContent   The content of the launcher script to include in the app bundle.
     * @return The `Path` to the created app bundle, or `null` if the creation fails.
     */
    private static Path createAppBundle(Path installDir, String desktopFileName, String icnsFileName, File icnsFile, String scriptContent) {
        Path appBundlePath = installDir.resolve(desktopFileName);
        try {
            File appBundleFile = appBundlePath.toFile();
            if (appBundleFile.exists()) {
                // If the app bundle already exists, delete it before creating a new one
                if (appBundleFile.isDirectory())
                    FileUtils.deleteDirectory(appBundlePath);
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
                Log(Level.WARN, "No icon file found for macOS app bundle.");
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
            Log(Level.ERROR, "Failed to create macOS app bundle: " + ex.getMessage());
            return null;
        }
        return appBundlePath;
    }
}

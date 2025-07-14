package io.github.tavstal.mmcinstaller.core.platform;

import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
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
import java.util.Set;

/**
 * Helper class for setting up macOS-specific application environments.
 * Extends the FallbackLogger to provide logging capabilities.
 * This class includes methods for creating macOS app bundles, shortcuts, and uninstallers.
 */
public class SetupMacOsHelper extends FallbackLogger {
    /**
     * Sets up the macOS application environment by creating app bundles, shortcuts, and an uninstaller.
     * This method handles the creation of the main app bundle, desktop shortcut, start menu shortcut,
     * and uninstaller app bundle, logging the process and handling errors as needed.
     *
     * @param installDir   The directory where the application is installed.
     * @param startMenuDir The directory where the start menu shortcut will be created.
     * @param jarFile      The JAR file of the application to be included in the app bundle.
     */
    public static void setup(File installDir, File startMenuDir, File jarFile) {
        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();
        // Retrieve the name of the macOS app bundle from the configuration
        String desktopFileName = ConfigLoader.get().install().macApp().fileName();

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, desktopFileName);
        InstallerState.setShortcutPath(desktopShortcutFile.getAbsolutePath());
        // Define the start menu shortcut file
        File startMenuFile = new File(startMenuDir, desktopFileName);
        InstallerState.setStartMenuShortcutPath(startMenuFile.getAbsolutePath());
        // Copy the .icns icon file from resources
        String iconFileName = "icon.icns";
        File icnsFile = FileUtils.copyResource(installDir.getAbsolutePath(), "assets/icon.icns", iconFileName);

        try {
            // Log the creation of the macOS app bundle
            Log(Level.DEBUG, "Creating macOS app bundle: " + desktopFileName);
            String launcherScriptContent = ConfigLoader.get().install().macApp().script()
                    .replaceAll("%dirPath%", installDir.getAbsolutePath())
                    .replaceAll("%jarPath%", jarFile.getAbsolutePath());
            Path launchAppBundlePath = createAppBundle(
                    installDir.toPath(),
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
                    Log(Level.DEBUG, "Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                    // Create a copy of the original .app bundle
                    FileUtils.copyDirectory(launchAppBundlePath.toAbsolutePath(), desktopShortcutFile.toPath().toAbsolutePath());
                }

                // Check if a start menu shortcut should be created
                if (InstallerState.shouldCreateStartMenuShortcut()) {
                    Log(Level.DEBUG, "Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                    // Create a copy of the original .app bundle
                    FileUtils.copyDirectory(launchAppBundlePath.toAbsolutePath(), startMenuFile.toPath().toAbsolutePath());
                }
            }
            else {
                // Log an error if the app bundle creation failed
                Log(Level.ERROR, "Failed to create macOS app bundle.");
            }

            // Create Uninstaller App Bundle
            Path uninstallAppBundlePath = createAppBundle(
                    installDir.toPath(),
                    ConfigLoader.get().uninstall().zsh().fileName(),
                    iconFileName,
                    icnsFile,
                    ConfigLoader.get().uninstall().zsh().content()
                            .replaceAll("%installDir%", installDir.getAbsolutePath())
                            .replaceAll("%desktopShortcut%", desktopShortcutFile.getAbsolutePath())
                            .replaceAll("%startmenuShortcut%", startMenuFile.getAbsolutePath())
            );
            if (uninstallAppBundlePath == null) {
                // Log an error if the uninstaller app bundle creation failed
                Log(Level.ERROR, "Failed to create macOS uninstaller app bundle.");
            } else {
                // Log the successful creation of the uninstaller app bundle
                Log(Level.DEBUG, "Created macOS uninstaller app bundle at: " + uninstallAppBundlePath.toAbsolutePath());
            }
        }
        catch (Exception ex) {
            // Log an error if the macOS app bundle creation fails
            Log(Level.ERROR, "Failed to create macOS app bundle: " + ex.getMessage());
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

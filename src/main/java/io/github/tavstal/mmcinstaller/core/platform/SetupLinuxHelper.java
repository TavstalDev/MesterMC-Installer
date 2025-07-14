package io.github.tavstal.mmcinstaller.core.platform;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.core.logging.FallbackLogger;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.ScriptUtils;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Helper class for setting up the Linux-specific application environment.
 * Extends the FallbackLogger to provide logging capabilities.
 * This class includes methods for creating .desktop files, shortcuts, and uninstallation scripts.
 */
public class SetupLinuxHelper extends FallbackLogger {

    /**
     * Sets up the Linux-specific application environment by creating .desktop files, shortcuts,
     * and an uninstallation script. This method handles file creation, logging, and error handling.
     *
     * @param installDir   The directory where the application is installed.
     * @param startMenuDir The directory where the start menu shortcut will be created.
     * @param jarFile      The JAR file of the application.
     * @param logCallback  A callback function to log messages during the setup process.
     */
    public static void setup(File installDir, File startMenuDir, File jarFile, Consumer<String> logCallback) {
        InstallerTranslator translator = InstallerApplication.getTranslator();
        // Get the name and content of the .desktop file from the configuration
        String desktopFileName = ConfigLoader.get().install().linuxDesktop().fileName();
        String desktopFileContent = ConfigLoader.get().install().linuxDesktop().content()
                .replaceAll("%dirPath%", installDir.getAbsolutePath()) // Replace placeholder with the installation path
                .replaceAll("%jarPath%", jarFile.getAbsolutePath()); // Replace placeholder with the JAR file path
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Define the .desktop file in the installation directory
        File linuxLaunchFile = new File(installDir, desktopFileName);

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, desktopFileName);
        InstallerState.setShortcutPath(desktopShortcutFile.getAbsolutePath());
        // Define the start menu shortcut file
        File startMenuFile = new File(startMenuDir, desktopFileName);
        InstallerState.setStartMenuShortcutPath(startMenuFile.getAbsolutePath());

        try {
            // Log the creation of the .desktop file
            Log(Level.DEBUG, "Creating .desktop file: " + linuxLaunchFile.getAbsolutePath());
            // Write the content to the .desktop file
            Files.writeString(linuxLaunchFile.toPath(), desktopFileContent);
            logCallback.accept(translator.Localize("IO.File.Created", Map.of(
                    "path", linuxLaunchFile.getAbsolutePath()
            )));

            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                Log(Level.DEBUG,"Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                // Copy the .desktop file to the desktop directory
                Files.copy(linuxLaunchFile.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", linuxLaunchFile.getAbsolutePath(),
                        "destination", desktopShortcutFile.getAbsolutePath()
                )));
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                Log(Level.DEBUG,"Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                // Copy the .desktop file to the start menu directory
                Files.copy(linuxLaunchFile.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", linuxLaunchFile.getAbsolutePath(),
                        "destination", startMenuFile.getAbsolutePath()
                )));
            }
        } catch (IOException e) {
            // Log an error if the .desktop file creation or shortcut creation fails
            Log(Level.ERROR,"Failed to write .desktop file: " + e.getMessage());
            logCallback.accept(translator.Localize("IO.File.CreateError", Map.of(
                    "path", linuxLaunchFile.getAbsolutePath(),
                    "error", e.getMessage()
            )));
        }

        // Create the uninstallation script file
        ScriptUtils.createFile(
                installDir.getAbsolutePath(),
                ConfigLoader.get().uninstall().bash().fileName(),
                ConfigLoader.get().uninstall().bash().content()
                        .replaceAll("%installDir%", installDir.getAbsolutePath())
                        .replaceAll("%desktopShortcut%", desktopShortcutFile.getAbsolutePath())
                        .replaceAll("%startmenuShortcut%", startMenuFile.getAbsolutePath())
        );
    }
}

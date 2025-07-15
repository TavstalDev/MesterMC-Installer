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
import java.nio.file.Path;
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
        setLogger(SetupLinuxHelper.class);

        InstallerTranslator translator = InstallerApplication.getTranslator();
        String installDirAbPath = installDir.getAbsolutePath();
        var installConfig = ConfigLoader.get().install();
        var uninstallConfig = ConfigLoader.get().uninstall();

        // Get the name and content of the .desktop file from the configuration
        String desktopFileName = installConfig.linuxDesktop().fileName();
        String desktopFileContent = installConfig.linuxDesktop().content()
                .replaceAll("%dirPath%", installDirAbPath) // Replace placeholder with the installation path
                .replaceAll("%jarPath%", jarFile.getAbsolutePath()); // Replace placeholder with the JAR file path
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Define the .desktop file in the installation directory
        File launchFile = new File(installDir, desktopFileName);
        Path launchFilePath = launchFile.toPath();
        String launchFileAbPath = launchFile.getAbsolutePath();

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, desktopFileName);
        String desktopShortcutAbPath = desktopShortcutFile.getAbsolutePath();
        InstallerState.setShortcutPath(desktopShortcutAbPath);

        // Define the start menu shortcut file
        File startMenuFile = new File(startMenuDir, desktopFileName);
        String startMenuAbPath = startMenuFile.getAbsolutePath();
        InstallerState.setStartMenuShortcutPath(startMenuAbPath);

        try {
            // Log the creation of the .desktop file
            log(Level.DEBUG, "Creating .desktop file: " + launchFileAbPath);
            // Write the content to the .desktop file
            Files.writeString(launchFilePath, desktopFileContent);
            logCallback.accept(translator.Localize("IO.File.Created", Map.of(
                    "path", launchFileAbPath
            )));

            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                log(Level.DEBUG,"Creating desktop shortcut: " + desktopShortcutAbPath);
                // Copy the .desktop file to the desktop directory
                Files.copy(launchFilePath, desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", launchFileAbPath,
                        "destination", desktopShortcutAbPath
                )));
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                log(Level.DEBUG,"Creating start menu shortcut: " + startMenuAbPath);
                // Copy the .desktop file to the start menu directory
                Files.copy(launchFilePath, startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", launchFileAbPath,
                        "destination", startMenuAbPath
                )));
            }
        } catch (IOException e) {
            // Log an error if the .desktop file creation or shortcut creation fails
            log(Level.ERROR,"Failed to write .desktop file: " + e.getMessage());
            logCallback.accept(translator.Localize("IO.File.CreateError", Map.of(
                    "path", launchFileAbPath,
                    "error", e.getMessage()
            )));
        }

        // Create the uninstallation script file
        ScriptUtils.createFile(
                installDirAbPath,
                uninstallConfig.bash().fileName(),
                uninstallConfig.bash().content()
                        .replaceAll("%installDir%", installDirAbPath)
                        .replaceAll("%desktopShortcut%", desktopShortcutAbPath)
                        .replaceAll("%startmenuShortcut%", startMenuAbPath)
        );
    }
}

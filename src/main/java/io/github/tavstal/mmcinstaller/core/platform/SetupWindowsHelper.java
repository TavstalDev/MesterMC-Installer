package io.github.tavstal.mmcinstaller.core.platform;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.core.logging.FallbackLogger;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
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
 * Helper class for setting up the Windows-specific application environment.
 * Extends the FallbackLogger to provide logging capabilities.
 * This class includes methods for creating shortcuts, copying resources, and generating uninstallation scripts.
 */
public class SetupWindowsHelper extends FallbackLogger {
    /**
     * Sets up the Windows-specific application environment by creating shortcuts, copying resources,
     * and generating an uninstallation script. This method handles file operations, PowerShell script execution,
     * and error handling.
     *
     * @param _installDir   The directory where the application is installed.
     * @param _startMenuDir The directory where the start menu shortcut will be created.
     * @param iconIcoPath   The path to the icon file (.ico) used for the shortcut.
     * @param logCallback   A callback function to log messages during the setup process.
     */
    public static void setup(File _installDir, File _startMenuDir, File iconIcoPath, Consumer<String> logCallback) {
        InstallerTranslator translator = InstallerApplication.getTranslator();
        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Retrieve the name of the executable file from the configuration
        String exeFileName = ConfigLoader.get().install().exe().fileName();

        // Define the path for the shortcut file
        File shortcutPath = new File(_installDir, "MesterMC.lnk");

        // Copy the executable file from resources to the installation directory
        var exeResourcePath = ConfigLoader.get().install().exe().resourcePath();
        File exeFile = FileUtils.copyResource(_installDir.getAbsolutePath(), exeResourcePath, exeFileName);
        if (exeFile == null) {
            Log(Level.ERROR, "Executable file not found: " + exeFileName);
            logCallback.accept(translator.Localize("IO.File.CopyError", Map.of(
                    "source", exeResourcePath,
                    "destination",  exeResourcePath + File.separator +  exeFileName,
                    "error", "Not found."
            )));
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
            Log(Level.DEBUG,"PowerShell script executed with exit code: " + exitCode);
            logCallback.accept(translator.Localize("Process.Success", Map.of(
                    "processName", "PowerShell",
                    "exitCode", String.valueOf(exitCode)
            )));

            // Clean up the temporary script file
            if (!ps1File.delete()) {
                Log(Level.WARN, "Failed to delete temporary PowerShell script: " + ps1File.getAbsolutePath());
                logCallback.accept(translator.Localize("IO.File.DeleteError", Map.of(
                        "path", ps1File.getAbsolutePath(),
                        "error", "?"
                )));
            }
        } catch (IOException | InterruptedException e) {
            // Log an error if the PowerShell script execution fails
            Log(Level.ERROR,"Failed to create Windows shortcut: " + e.getMessage());
            logCallback.accept(translator.Localize("IO.File.CreateError", Map.of(
                    "path", shortcutPath.getAbsolutePath(),
                    "error", e.getMessage()
            )));
            return;
        }

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, "MesterMC.lnk");
        InstallerState.setShortcutPath(desktopShortcutFile.getAbsolutePath());

        // Define the start menu shortcut file
        File startMenuFile = new File(_startMenuDir, "MesterMC.lnk");
        InstallerState.setStartMenuShortcutPath(startMenuFile.getAbsolutePath());

        // Copy the shortcut to the desktop and start menu
        try {
            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                Log(Level.DEBUG,"Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                // Copy the shortcut file to the desktop directory
                Files.copy(shortcutPath.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", shortcutPath.getAbsolutePath(),
                        "destination", desktopShortcutFile.getAbsolutePath()
                )));
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                Log(Level.DEBUG,"Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                // Copy the shortcut file to the start menu directory
                Files.copy(shortcutPath.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", shortcutPath.getAbsolutePath(),
                        "destination", startMenuFile.getAbsolutePath()
                )));
            }

            // Delete the original shortcut file in the installation directory
            // This is done to avoid cluttering the installation directory with the shortcut file
            if (shortcutPath.exists() && !shortcutPath.delete()) {
                Log(Level.WARN,"Failed to delete original shortcut file: " + shortcutPath.getAbsolutePath());
            }
        } catch (IOException e) {
            // Log an error if copying the shortcut files fails
            Log(Level.ERROR,"Failed to copy shortcut files: " + e.getMessage());
            logCallback.accept(translator.Localize("IO.File.CopyError", Map.of(
                    "source", shortcutPath.getAbsolutePath(),
                    "destination", desktopShortcutFile.getAbsolutePath() + " or " + startMenuFile.getAbsolutePath(),
                    "error", e.getMessage()
            )));
        }

        // Create the uninstallation script file
        ScriptUtils.createFile(
                _installDir.getAbsolutePath(),
                ConfigLoader.get().uninstall().batch().fileName(),
                ConfigLoader.get().uninstall().batch().content()
                        .replaceAll("%installDir%", _installDir.getAbsolutePath().replace("\\", "\\\\"))
                        .replaceAll("%desktopShortcut%", desktopShortcutFile.getAbsolutePath().replace("\\", "\\\\"))
                        .replaceAll("%startmenuShortcut%", startMenuFile.getAbsolutePath().replace("\\", "\\\\"))
        );
    }
}
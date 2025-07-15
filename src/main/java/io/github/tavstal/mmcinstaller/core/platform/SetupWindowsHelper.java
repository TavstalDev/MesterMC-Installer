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
import java.nio.file.Path;
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
     * Logs a message at the specified log level for this class.
     * Delegates the logging to the `FallbackLogger` with the current class as the module.
     *
     * @param level   The log level (e.g., INFO, WARN, ERROR, DEBUG).
     * @param message The message to be logged.
     */
    private static void log(Level level, String message) {
        FallbackLogger.log(level, message, SetupWindowsHelper.class);
    }

    /**
     * Sets up the Windows-specific application environment by creating shortcuts, copying resources,
     * and generating an uninstallation script. This method handles file operations, PowerShell script execution,
     * and error handling.
     *
     * @param installDir   The directory where the application is installed.
     * @param startMenuDir The directory where the start menu shortcut will be created.
     * @param icoFile   The path to the icon file (.ico) used for the shortcut.
     * @param logCallback   A callback function to log messages during the setup process.
     */
    public static void setup(File installDir, File startMenuDir, File icoFile, Consumer<String> logCallback) {
        InstallerTranslator translator = InstallerApplication.getTranslator();
        String installDirAbPath = installDir.getAbsolutePath();
        var installConfig = ConfigLoader.get().install();
        var uninstallConfig = ConfigLoader.get().uninstall();

        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Retrieve the name of the executable file from the configuration
        String exeFileName = installConfig.exe().fileName();

        // Define the path for the shortcut file
        File shortcutFile = new File(installDir, "MesterMC.lnk");
        String shortcutAbPath = shortcutFile.getAbsolutePath();
        Path shortcutPath = shortcutFile.toPath();

        // Copy the executable file from resources to the installation directory
        var exeResourcePath = installConfig.exe().resourcePath();
        File exeFile = FileUtils.copyResource(installDirAbPath, exeResourcePath, exeFileName);
        if (exeFile == null) {
            log(Level.ERROR, "Executable file not found: " + exeFileName);
            logCallback.accept(translator.Localize("IO.File.CopyError", Map.of(
                    "source", exeResourcePath,
                    "destination",  exeResourcePath + File.separator +  exeFileName,
                    "error", "Not found."
            )));
            return;
        }
        String exeAbPath = exeFile.getAbsolutePath();
        // Set the application launch path to the executable file
        InstallerState.setApplicationToLaunch(exeAbPath);

        // Create the shortcut using a PowerShell script
        try {
            // Generate the PowerShell script content
            String powershellScript = installConfig.exe().powershell()
                    .replace("%shortcutFile%", shortcutAbPath.replace("\\", "\\\\"))
                    .replace("%exePath%", exeAbPath.replace("\\", "\\\\"))
                    .replace("%iconPath%", icoFile.getAbsolutePath().replace("\\", "\\\\"));

            // Save the script to a temporary `.ps1` file
            File ps1File = new File(System.getProperty("java.io.tmpdir"), "create_shortcut.ps1");
            String ps1AbPath = ps1File.getAbsolutePath();
            Files.writeString(ps1File.toPath(), powershellScript);

            // Execute the PowerShell script
            Process process = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", ps1AbPath).start();
            int exitCode = process.waitFor();
            log(Level.DEBUG,"PowerShell script executed with exit code: " + exitCode);
            logCallback.accept(translator.Localize("Process.Success", Map.of(
                    "processName", "PowerShell",
                    "exitCode", String.valueOf(exitCode)
            )));

            // Clean up the temporary script file
            if (!ps1File.delete()) {
                log(Level.WARN, "Failed to delete temporary PowerShell script: " + ps1AbPath);
                logCallback.accept(translator.Localize("IO.File.DeleteError", Map.of(
                        "path", ps1AbPath,
                        "error", "?"
                )));
            }
        } catch (IOException | InterruptedException e) {
            // Log an error if the PowerShell script execution fails
            log(Level.ERROR,"Failed to create Windows shortcut: " + e.getMessage());
            logCallback.accept(translator.Localize("IO.File.CreateError", Map.of(
                    "path", shortcutAbPath,
                    "error", e.getMessage()
            )));
            return;
        }

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, "MesterMC.lnk");
        String desktopShortcutAbPath = desktopShortcutFile.getAbsolutePath();
        InstallerState.setShortcutPath(desktopShortcutAbPath);

        // Define the start menu shortcut file
        File startMenuShortcutFile = new File(startMenuDir, "MesterMC.lnk");
        String startMenuShortcutAbPath = startMenuShortcutFile.getAbsolutePath();
        InstallerState.setStartMenuShortcutPath(startMenuShortcutAbPath);

        // Copy the shortcut to the desktop and start menu
        try {
            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                log(Level.DEBUG,"Creating desktop shortcut: " + desktopShortcutAbPath);
                // Copy the shortcut file to the desktop directory
                Files.copy(shortcutPath, desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", shortcutAbPath,
                        "destination", desktopShortcutAbPath
                )));
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                log(Level.DEBUG,"Creating start menu shortcut: " + startMenuShortcutAbPath);
                // Copy the shortcut file to the start menu directory
                Files.copy(shortcutPath, startMenuShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logCallback.accept(translator.Localize("IO.File.Copied", Map.of(
                        "source", shortcutAbPath,
                        "destination", startMenuShortcutAbPath
                )));
            }

            // Delete the original shortcut file in the installation directory
            // This is done to avoid cluttering the installation directory with the shortcut file
            if (shortcutFile.exists() && !shortcutFile.delete()) {
                log(Level.WARN,"Failed to delete original shortcut file: " + shortcutAbPath);
            }
        } catch (IOException e) {
            // Log an error if copying the shortcut files fails
            log(Level.ERROR,"Failed to copy shortcut files: " + e.getMessage());
            logCallback.accept(translator.Localize("IO.File.CopyError", Map.of(
                    "source", shortcutAbPath,
                    "destination", desktopShortcutAbPath + " or " + startMenuShortcutAbPath,
                    "error", e.getMessage()
            )));
        }

        // Create the uninstallation script file
        ScriptUtils.createFile(
                installDirAbPath,
                uninstallConfig.batch().fileName(),
                uninstallConfig.batch().content()
                        .replaceAll("%installDir%", installDirAbPath.replace("\\", "\\\\"))
                        .replaceAll("%desktopShortcut%", desktopShortcutAbPath.replace("\\", "\\\\"))
                        .replaceAll("%startmenuShortcut%", startMenuShortcutAbPath.replace("\\", "\\\\"))
        );
    }
}
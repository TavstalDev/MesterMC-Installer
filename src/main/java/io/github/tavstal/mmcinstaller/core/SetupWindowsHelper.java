package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.utils.FallbackLogger;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.ScriptUtils;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Helper class for setting up the Windows-specific application environment.
 * Extends the FallbackLogger to provide logging capabilities.
 * This class includes methods for creating shortcuts, copying resources, and generating uninstallation scripts.
 */
public class SetupWindowsHelper extends FallbackLogger {
    /**
     * Sets up the Windows application environment by creating shortcuts, copying executable files,
     * and generating an uninstallation script. This method handles the creation of desktop and start menu shortcuts,
     * logging the process and handling errors as needed.
     *
     * @param _installDir   The directory where the application is installed.
     * @param _startMenuDir The directory where the start menu shortcut will be created.
     * @param iconIcoPath   The path to the icon file to be used for the shortcut.
     */
    public static void setup(File _installDir, File _startMenuDir, File iconIcoPath) {
        // Get the user's desktop directory
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Retrieve the name of the executable file from the configuration
        String exeFileName = ConfigLoader.get().install().exe().fileName();

        // Define the path for the shortcut file
        File shortcutPath = new File(_installDir, "MesterMC.lnk");

        // Copy the executable file from resources to the installation directory
        File exeFile = FileUtils.copyResource(_installDir.getAbsolutePath(),ConfigLoader.get().install().exe().resourcePath(), exeFileName);
        if (exeFile == null) {
            Log(Level.ERROR, "Executable file not found: " + exeFileName);
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

            // Clean up the temporary script file
            if (!ps1File.delete())
                Log(Level.WARN,"Failed to delete temporary PowerShell script: " + ps1File.getAbsolutePath());
        } catch (IOException | InterruptedException e) {
            // Log an error if the PowerShell script execution fails
            Log(Level.ERROR,"Failed to create Windows shortcut: " + e.getMessage());
            return;
        }

        // Define the desktop shortcut file
        File desktopShortcutFile = new File(desktopDir, "MesterMC.lnk");
        InstallerState.setCurrentPath(desktopShortcutFile.getAbsolutePath());

        // Define the start menu shortcut file
        File startMenuFile = new File(_startMenuDir, "MesterMC.lnk");
        InstallerState.setStartMenuPath(startMenuFile.getAbsolutePath());

        // Copy the shortcut to the desktop and start menu
        try {
            // Check if a desktop shortcut should be created
            if (InstallerState.shouldCreateDesktopShortcut()) {
                Log(Level.DEBUG,"Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                // Copy the shortcut file to the desktop directory
                Files.copy(shortcutPath.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Check if a start menu shortcut should be created
            if (InstallerState.shouldCreateStartMenuShortcut()) {
                Log(Level.DEBUG,"Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                // Copy the shortcut file to the start menu directory
                Files.copy(shortcutPath.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Delete the original shortcut file in the installation directory
            // This is done to avoid cluttering the installation directory with the shortcut file
            if (shortcutPath.exists() && !shortcutPath.delete()) {
                Log(Level.WARN,"Failed to delete original shortcut file: " + shortcutPath.getAbsolutePath());
            }
        } catch (IOException e) {
            // Log an error if copying the shortcut files fails
            Log(Level.ERROR,"Failed to copy shortcut files: " + e.getMessage());
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
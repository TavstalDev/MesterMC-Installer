package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages the uninstallation process for the application.
 * Handles the deletion of shortcuts, directories, and configuration files,
 * while providing progress updates and logging messages.
 */
public class UninstallManager {
    private final InstallerLogger _logger; // Logger instance for logging events.
    private final InstallerTranslator _translator; // Translator instance for localization of messages.
    private final Consumer<String> _logCallback; // Callback for logging messages.
    private final Consumer<Double> _progressCallback; // Callback for progress updates.

    /**
     * Constructs an UninstallManager instance.
     *
     * @param logCallback      A callback function for logging messages.
     * @param progressCallback A callback function for updating progress.
     */
    public UninstallManager(Consumer<String> logCallback, Consumer<Double> progressCallback) {
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        _translator = InstallerApplication.getTranslator();
        _logCallback = logCallback;
        _progressCallback = progressCallback;
    }

    /**
     * Starts the uninstallation process.
     * Deletes shortcuts, directories, and configuration files associated with the application.
     * Updates progress and logs messages during each step of the process.
     */
    public void start() {
        _logCallback.accept(_translator.Localize("ProgressUninstall.Deleting"));
        double steps = 5.0;

        // Delete desktop shortcut.
        File desktopShortcut = new File(InstallerState.getShortcutPath());
        if (desktopShortcut.exists()) {
            if (desktopShortcut.delete()) {
                _logCallback.accept(_translator.Localize("IO.File.Deleted", Map.of("path", desktopShortcut.getAbsolutePath())));
            }
            else {
                _logCallback.accept(_translator.Localize("IO.File.DeleteError", Map.of(
                        "path", desktopShortcut.getAbsolutePath(),
                        "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.File.NotFound", Map.of("path",desktopShortcut.getAbsolutePath())));
        }
        _progressCallback.accept(1.0 / steps);

        // Delete start menu shortcut.
        File startMenuShortcut = new File(InstallerState.getStartMenuPath());
        if (startMenuShortcut.exists()) {
            if (startMenuShortcut.delete()) {
                _logCallback.accept(_translator.Localize("IO.File.Deleted", Map.of("path", desktopShortcut.getAbsolutePath())));
            } else {
                _logCallback.accept(_translator.Localize("IO.File.DeleteError", Map.of(
                        "path", desktopShortcut.getAbsolutePath(),
                        "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.File.NotFound", Map.of("path", startMenuShortcut.getAbsolutePath())));
        }
        _progressCallback.accept(2.0 / steps);

        // Delete start menu directory.
        File startMenuDir = new File(InstallerState.getStartMenuPath());
        if (startMenuDir.exists()) {
            if (InstallerState.getStartMenuPath().equals(PathUtils.getStartMenuDirectory("").getAbsolutePath())) {
                _logCallback.accept(_translator.Localize("IO.Directory.NotWritable", Map.of("path", startMenuDir.getAbsolutePath())));
            } else if (startMenuDir.delete()) {
                _logCallback.accept(_translator.Localize("IO.Directory.Deleted", Map.of("path", startMenuDir.getAbsolutePath())));
            } else {
                _logCallback.accept(_translator.Localize("IO.Directory.DeleteError", Map.of(
                        "path", startMenuDir.getAbsolutePath(),
                        "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.Directory.NotFound", Map.of("path", startMenuDir.getAbsolutePath())));
        }
        _progressCallback.accept(3.0 / steps);

        // Delete installation directory.
        File installDir = new File(InstallerState.getCurrentPath());
        if (installDir.exists()) {
            try {
                FileUtils.deleteDirectory(installDir.toPath());
                _logCallback.accept(_translator.Localize("IO.Directory.Deleted", Map.of("path", installDir.getAbsolutePath())));
            } catch (IOException e) {
                _logger.Error("Failed to delete installation directory: " + e.getMessage());
                _logCallback.accept(_translator.Localize("IO.Directory.DeleteError", Map.of(
                        "path", installDir.getAbsolutePath(),
                        "error", e.getMessage()
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.Directory.NotFound", Map.of("path", installDir.getAbsolutePath())));
        }
        _progressCallback.accept(4.0 / steps);

        // Delete the uninstaller config file.
        File configFile = PathUtils.getUninstallerConfigFile();
        if (configFile.exists()) {
            if (configFile.delete()) {
                _logCallback.accept(_translator.Localize("IO.File.Deleted", Map.of("path", configFile.getAbsolutePath())));
            } else {
                _logCallback.accept(_translator.Localize("IO.File.DeleteError", Map.of(
                        "path", configFile.getAbsolutePath(),
                        "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.File.NotFound", Map.of("path", configFile.getAbsolutePath())));
        }

        _progressCallback.accept(1.0); // Set progress to 100% after completion.
        Platform.runLater(() -> { // Small delay to ensure UI is ready.
            InstallerApplication.setActiveScene(SceneManager.getInstallCompleteScene());
        });
    }
}

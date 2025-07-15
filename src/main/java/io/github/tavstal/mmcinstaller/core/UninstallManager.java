package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.logging.InstallerLogger;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

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
        String desktopShortcutAbPath = desktopShortcut.getAbsolutePath();
        if (desktopShortcut.exists()) {
            if (desktopShortcut.isDirectory() && desktopShortcutAbPath.endsWith(".app")) {
                try {
                    FileUtils.deleteDirectory(desktopShortcut.toPath());
                    _logCallback.accept(_translator.Localize("IO.Directory.Deleted", Map.of("path", desktopShortcutAbPath)));
                }
                catch (IOException ex) {
                    _logger.Error("Failed to delete desktop shortcut: " + ex.getMessage());
                    _logCallback.accept(_translator.Localize("IO.Directory.DeleteError", Map.of(
                            "path", desktopShortcutAbPath,
                            "error", ex.getMessage()
                    )));
                }
            } else {
                if (desktopShortcut.delete()) {
                    _logCallback.accept(_translator.Localize("IO.File.Deleted", Map.of("path", desktopShortcutAbPath)));
                } else {
                    _logCallback.accept(_translator.Localize("IO.File.DeleteError", Map.of(
                            "path", desktopShortcutAbPath,
                            "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                    )));
                }
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.File.NotFound", Map.of("path", desktopShortcutAbPath)));
        }
        _progressCallback.accept(1.0 / steps);

        // Delete start menu shortcut.
        File startMenuShortcut = new File(InstallerState.getStartMenuShortcutPath());
        String startMenuShortcutAbPath = startMenuShortcut.getAbsolutePath();
        if (startMenuShortcut.exists()) {
            if (startMenuShortcut.isDirectory() && startMenuShortcutAbPath.endsWith(".app")) {
                try {
                    FileUtils.deleteDirectory(startMenuShortcut.toPath());
                    _logCallback.accept(_translator.Localize("IO.Directory.Deleted", Map.of("path", startMenuShortcutAbPath)));
                }
                catch (IOException ex) {
                    _logger.Error("Failed to delete desktop shortcut: " + ex.getMessage());
                    _logCallback.accept(_translator.Localize("IO.Directory.DeleteError", Map.of(
                            "path", startMenuShortcutAbPath,
                            "error", ex.getMessage()
                    )));
                }
            } else {
                if (startMenuShortcut.delete()) {
                    _logCallback.accept(_translator.Localize("IO.File.Deleted", Map.of("path", startMenuShortcutAbPath)));
                } else {
                    _logCallback.accept(_translator.Localize("IO.File.DeleteError", Map.of(
                            "path", startMenuShortcutAbPath,
                            "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                    )));
                }
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.File.NotFound", Map.of("path", startMenuShortcutAbPath)));
        }
        _progressCallback.accept(2.0 / steps);

        // Delete start menu directory.
        File startMenuDir = new File(InstallerState.getStartMenuPath());
        String startMenuDirAbPath = startMenuDir.getAbsolutePath();
        if (startMenuDir.exists()) {
            if (InstallerState.getStartMenuPath().equals(PathUtils.getStartMenuDirectory("").getAbsolutePath())) {
                _logCallback.accept(_translator.Localize("IO.Directory.NotWritable", Map.of("path", startMenuDirAbPath)));
            } else if (startMenuDir.delete()) {
                _logCallback.accept(_translator.Localize("IO.Directory.Deleted", Map.of("path", startMenuDirAbPath)));
            } else {
                _logCallback.accept(_translator.Localize("IO.Directory.DeleteError", Map.of(
                        "path", startMenuDirAbPath,
                        "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.Directory.NotFound", Map.of("path", startMenuDirAbPath)));
        }
        _progressCallback.accept(3.0 / steps);

        // Delete installation directory.
        File installDir = new File(InstallerState.getCurrentPath());
        String installDirAbPath = installDir.getAbsolutePath();
        if (installDir.exists()) {
            try {
                FileUtils.deleteDirectory(installDir.toPath());
                _logCallback.accept(_translator.Localize("IO.Directory.Deleted", Map.of("path", installDirAbPath)));
            } catch (IOException e) {
                _logger.Error("Failed to delete installation directory: " + e.getMessage());
                _logCallback.accept(_translator.Localize("IO.Directory.DeleteError", Map.of(
                        "path", installDirAbPath,
                        "error", e.getMessage()
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.Directory.NotFound", Map.of("path", installDirAbPath)));
        }
        _progressCallback.accept(4.0 / steps);

        // Delete the uninstaller config file.
        File configFile = PathUtils.getUninstallerConfigFile();
        String configFileAbPath = configFile.getAbsolutePath();
        if (configFile.exists()) {
            if (configFile.delete()) {
                _logCallback.accept(_translator.Localize("IO.File.Deleted", Map.of("path", configFileAbPath)));
            } else {
                _logCallback.accept(_translator.Localize("IO.File.DeleteError", Map.of(
                        "path", configFileAbPath,
                        "error", "?" // Placeholder for actual error message, since we don't have a way to get the actual error in this context
                )));
            }
        } else {
            _logCallback.accept(_translator.Localize("IO.File.NotFound", Map.of("path", configFileAbPath)));
        }

        Platform.runLater(() -> { // Small delay to ensure UI is ready.
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                InstallerApplication.setActiveScene(SceneManager.getInstallCompleteScene());
            });
            pause.play();
        });
    }
}

package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.DownloadManager;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.core.UninstallManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller class for managing the installation progress UI and logic.
 * Handles downloading files, creating shortcuts, and updating the UI.
 */
public class InstallProgressController implements Initializable {
    public Label progressTitle; // Label for the progress title.
    public Label progressDescription; // Label for the progress description.
    public Text progressAction; // Text for the current progress action.
    public ProgressBar progressBar; // Progress bar for visualizing download progress.
    public TextArea logTextArea; // Text area for displaying log messages.
    public Button cancelButton; // Button to cancel the installation process.

    /**
     * Initializes the installation progress controller.
     * Sets up the logger, translator, and UI elements, and starts the appropriate process
     * (installation or uninstallation) based on the current application state.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if not known.
     * @param resources The resources used to localize the root object, or null if not available.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InstallerTranslator _translator = InstallerApplication.getTranslator();

        // Set localized text for UI elements.
        cancelButton.setText(_translator.Localize("Common.Cancel"));
        if (InstallerState.isUninstallModeActive()) {
            progressTitle.setText(_translator.Localize("ProgressUninstall.Title"));
            progressDescription.setText(_translator.Localize("ProgressUninstall.Description"));
            progressAction.setText(_translator.Localize("ProgressUninstall.Action"));

            Platform.runLater(() -> { // Small delay to ensure UI is ready.
                UninstallManager manager = new UninstallManager(this::logStep, this::updateProgressBar);
                manager.start();
            });
            return;
        }

        progressTitle.setText(_translator.Localize("Progress.Title"));
        progressDescription.setText(_translator.Localize("Progress.Description"));
        progressAction.setText(_translator.Localize("Progress.Action"));

        // Get the current installation directory.
        File dir = new File(InstallerState.getCurrentPath());
        if (dir.mkdirs()) {
            logStep(_translator.Localize("IO.Directory.Created", Map.of("path", dir.getAbsolutePath())));
        } else {
            logStep(_translator.Localize("IO.Directory.Exists", Map.of("path", dir.getAbsolutePath())));
        }

        Platform.runLater(() -> { // Small delay to ensure UI is ready.
            DownloadManager manager = new DownloadManager(
                    this::logStep,
                    this::updateProgressBar,
                    this::updateDownloadProgress
            );
            manager.start();
        });
    }

    /**
     * Handles the action when the cancel button is clicked.
     * Exits the application.
     */
    @FXML
    protected void onCancelButtonClick() {
        System.exit(0);
    }

    /**
     * Logs a step in the installation or uninstallation process.
     * Appends the provided message to the log text area and scrolls to the end.
     *
     * @param message The message to log.
     */
    public void logStep(String message) {
        Platform.runLater(() -> {
            // Append the new message
            logTextArea.appendText(message + "\n");
            // Scroll to the end
            logTextArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    /**
     * Updates the progress bar with the specified value.
     * Ensures the update is performed on the JavaFX Application Thread.
     *
     * @param value The progress value to set (between 0.0 and 1.0).
     */
    public void updateProgressBar(double value) {
        Platform.runLater(() -> {
            // Ensure the value is between 0.0 and 1.0
            double result = value;
            if (value > 1.0) {
                result = 1.0;
            } else if (value < 0.0) {
                result = 0.0;
            }
            progressBar.setProgress(result);
        });
    }

    /**
     * Updates the progress bar based on the number of downloaded bytes and total bytes.
     * Calculates the progress as a fraction and ensures the update is performed on the JavaFX Application Thread.
     *
     * @param downloadedBytes The number of bytes downloaded so far.
     * @param totalBytes      The total number of bytes to download.
     */
    public void updateDownloadProgress(long downloadedBytes, long totalBytes) {
        Platform.runLater(() -> {
            if (totalBytes > 0) {
                double value = (double) downloadedBytes / totalBytes;
                progressBar.setProgress(value);
            }
        });
    }
}
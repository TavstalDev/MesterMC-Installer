package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller class for managing the installation progress UI and logic.
 * Handles downloading files, creating shortcuts, and updating the UI.
 */
public class InstallProgressController implements Initializable {

    private InstallerLogger _logger; // Logger instance for logging events.
    private InstallerTranslator _translator; // Translator instance for localization.
    public Label progressTitle; // Label for the progress title.
    public Label progressDescription; // Label for the progress description.
    public Text progressAction; // Text for the current progress action.
    public ProgressBar progressBar; // Progress bar for visualizing download progress.
    public TextArea logTextArea; // Text area for displaying log messages.
    public Button cancelButton; // Button to cancel the installation process.

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up the logger, translator, and UI elements, and starts the download process.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the logger with the current class module.
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        // Initialize the translator for localization.
        _translator = InstallerApplication.getTranslator();

        // Set localized text for UI elements.
        progressTitle.setText(_translator.Localize("Progress.Title"));
        progressDescription.setText(_translator.Localize("Progress.Description"));
        progressAction.setText(_translator.Localize("Progress.Action"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        // Get the current installation directory.
        File dir = new File(InstallerApplication.getCurrentPath());
        if (dir.mkdirs()) {
            logStep(_translator.Localize("Progress.DirectoryCreated", Map.of("path", dir.getAbsolutePath())));
        } else {
            logStep(_translator.Localize("Progress.DirectoryExists", Map.of("path", dir.getAbsolutePath())));
        }

        // Start the download process.
        startDownload();
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
     * Logs a step message to the log text area in the JavaFX application.
     * This method ensures that the UI update is performed on the JavaFX Application Thread.
     *
     * @param message The message to be logged and displayed in the log text area.
     */
    private void logStep(String message) {
        Platform.runLater(() -> {
            // Append the new message
            logTextArea.appendText(message + "\n");

            // Scroll to the end
            logTextArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    /**
     * Updates the download progress in the UI.
     * This method is called by the FileDownloader to update the progress bar and log progress.
     *
     * @param downloadedBytes The number of bytes downloaded so far.
     * @param totalBytes      The total number of bytes to download.
     */
    private void updateDownloadProgress(long downloadedBytes, long totalBytes) {
        Platform.runLater(() -> {
            if (totalBytes > 0) {
                _logger.Debug(String.format("Downloading... %.2f MB / %.2f MB (%.2f%%)",
                        (double) downloadedBytes / (1024 * 1024),
                        (double) totalBytes / (1024 * 1024),
                        (double) downloadedBytes * 100 / totalBytes));
            } else {
                _logger.Debug("Downloading... " + (downloadedBytes / (1024 * 1024)) + " MB (Total unknown)");
            }
        });
    }

    /**
     * Starts the download process by creating a task to download the required file.
     * Updates the UI with progress and handles success, failure, or cancellation of the task.
     */
    private void startDownload() {
        // Define the installation directory, start menu directory, and output file for the download.
        File dir = new File(InstallerApplication.getCurrentPath());
        File startMenuDir = new File(InstallerApplication.getStartMenuPath());
        File outputFile = new File(InstallerApplication.getCurrentPath(), ConfigLoader.get().download().fileName());

        // Create a Task for the download
        FileDownloader downloader = new FileDownloader(_logger, _translator);
        Task<Void> downloadTask = downloader.createDownloadTask(
                ConfigLoader.get().download().link(),
                outputFile,
                this::logStep, // Pass the logStep method reference
                this::updateDownloadProgress // New method to update UI progress
        );

        // TODO: Check file hash

        // Bind the ProgressBar's progress property to the Task's progress property.
        progressBar.progressProperty().bind(downloadTask.progressProperty());

        // Handle the success of the download task.
        downloadTask.setOnSucceeded(event -> {
            _logger.Debug("Download task succeeded.");
            progressBar.progressProperty().unbind(); // Unbind after completion.
            progressBar.setProgress(1.0); // Ensure it shows 100%.
            logStep(_translator.Localize("Progress.Scripts.Creating"));
            SetupManager manager = new SetupManager(outputFile, dir, startMenuDir, this::logStep);
            manager.setup();
        });

        // Handle the failure of the download task.
        downloadTask.setOnFailed(event -> {
            _logger.Error("Download task failed: " + downloadTask.getException().getMessage());
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0.0); // Reset or show error.
        });

        // Handle the cancellation of the download task.
        downloadTask.setOnCancelled(event -> {
            _logger.Debug("Download task cancelled.");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0.0);
            logStep("Download cancelled by user.");
            logStep(_translator.Localize("Progress.Download.Cancelled"));
        });

        // Start the task in a new thread.
        new Thread(downloadTask).start();
    }
}
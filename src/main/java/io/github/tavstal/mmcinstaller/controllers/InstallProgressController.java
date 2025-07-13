package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.core.*;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

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
        File dir = new File(InstallerState.getCurrentPath());
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
                progressBar.setProgress((double) downloadedBytes / totalBytes);
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
        File outputFile = new File(InstallerState.getCurrentPath(), ConfigLoader.get().download().fileName());

        // Create a Task for the download
        System.out.println("Expected size: " + InstallerState.getRequiredSpaceInBytes() + " bytes");
        System.out.println("Existing file size: " + outputFile.length() + " bytes");
        System.out.println("Output file exists: " + outputFile.exists());
        if (outputFile.exists() && outputFile.length() == InstallerState.getRequiredSpaceInBytes() && outputFile.length() > 0) {
            progressBar.setProgress(1.0);
            Platform.runLater(() -> { // Small delay to ensure UI is ready.
                handleDownloadedFile(outputFile);
            });
            return; // Skip download if file already exists and is valid.
        }

        FileDownloader downloader = new FileDownloader(_logger, _translator);
        Task<Void> downloadTask = downloader.createDownloadTask(
                ConfigLoader.get().download().link(),
                outputFile,
                this::logStep, // Pass the logStep method reference
                this::updateDownloadProgress // New method to update UI progress
        );

        // Handle the success of the download task.
        downloadTask.setOnSucceeded(event -> {
            _logger.Debug("Download task succeeded.");
            progressBar.progressProperty().unbind(); // Unbind after completion.
            progressBar.setProgress(1.0); // Ensure it shows 100%.
            logStep(_translator.Localize("Progress.Scripts.Creating"));
            handleDownloadedFile(outputFile); // Handle the downloaded file.
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

    /**
     * Handles the downloaded file by verifying its checksum and performing setup operations.
     * If the checksum validation fails, the file is deleted, and the application exits.
     * If the checksum is valid or the user chooses to proceed despite a mismatch, the setup process is initiated.
     *
     * @param outputFile The downloaded file to be handled.
     */
    private void handleDownloadedFile(File outputFile) {
        // Localized strings for error and warning messages.
        String errorTitle = _translator.Localize("Common.Error");
        String errorHeader = _translator.Localize("Progress.CheckSumError.Header");
        String errorContent = _translator.Localize("Progress.CheckSumError.Content");
        String yesButtonText = _translator.Localize("Common.Next");
        String noButtonText = _translator.Localize("Common.Cancel");
        String outputChecksum = "";
        boolean checksumContinue;

        try {
            // Calculate the checksum of the downloaded file.
            outputChecksum = PathUtils.getFileChecksum(outputFile.getAbsolutePath());
            if (outputChecksum.isEmpty()) {
                // Log an error if the checksum is empty and show an error alert.
                _logger.Error("Checksum is null or empty for file: " + outputFile.getAbsolutePath());
                checksumContinue = showAlert(errorTitle, errorHeader, errorContent, yesButtonText, noButtonText, Alert.AlertType.ERROR);
            } else {
                checksumContinue = true;
            }
        } catch (Exception ex) {
            // Log an error if checksum calculation fails and show an error alert.
            _logger.Error("Failed to calculate checksum: " + ex.getMessage());
            checksumContinue = showAlert(errorTitle, errorHeader, errorContent, yesButtonText, noButtonText, Alert.AlertType.ERROR);
        }

        // If the user chooses not to continue, delete the file and exit the application.
        if (!checksumContinue) {
            if (outputFile.exists())
                outputFile.delete(); // Clean up the file if checksum validation fails.
            System.exit(0);
            return;
        }

        // Retrieve the expected checksum from the configuration.
        String expectedChecksum = ConfigLoader.get().download().hash();
        if (!(expectedChecksum == null || expectedChecksum.isEmpty())) {
            // Compare the calculated checksum with the expected checksum.
            if (!outputChecksum.equals(expectedChecksum)) {
                // Show a warning alert if the checksums do not match.
                String title = _translator.Localize("Common.Warning");
                String header = _translator.Localize("Progress.CheckSumWarning.Header");
                String content = _translator.Localize("Progress.CheckSumWarning.Content", Map.of(
                        "expected", expectedChecksum,
                        "actual", outputChecksum
                ));
                if (!showAlert(title, header, content, yesButtonText, noButtonText, Alert.AlertType.WARNING)) {
                    // If the user chooses not to continue, delete the file and exit the application.
                    if (outputFile.exists())
                        outputFile.delete(); // Clean up the file if checksum validation fails.
                    System.exit(0);
                    return;
                } else {
                    _logger.Warn("Checksum mismatch but user chose to continue.");
                }
            }
        }

        // Initialize the setup manager and perform the setup process.
        File dir = new File(InstallerState.getCurrentPath());
        File startMenuDir = new File(InstallerState.getStartMenuPath());
        SetupManager manager = new SetupManager(outputFile, dir, startMenuDir, this::logStep);
        manager.setup();
    }

    /**
     * Displays a confirmation alert dialog to the user with customizable title, header, content,
     * and button labels. Captures the user's choice and returns whether the user chose to continue.
     *
     * @param title          The title of the alert dialog.
     * @param header         The header text of the alert dialog.
     * @param content        The content text of the alert dialog.
     * @param yesButtonText  The label for the "Yes" button.
     * @param noButtonText   The label for the "No" button.
     * @return true if the user selects the "Yes" button, false otherwise.
     */
    private boolean showAlert(String title, String header, String content, String yesButtonText, String noButtonText, Alert.AlertType alertType) {
        // Show an alert to the user about the checksum error.
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create "Yes" and "No" buttons with the provided labels.
        ButtonType yesButton = new ButtonType(yesButtonText);
        ButtonType noButton = new ButtonType(noButtonText);

        // Set the button types for the alert dialog.
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Display the alert dialog and wait for the user's response.
        Optional<ButtonType> result = alert.showAndWait();

        // Log the user's choice.
        if (result.isPresent() && result.get() == yesButton) {
            _logger.Debug("User chose to continue.");
        } else {
            _logger.Debug("User canceled the operation.");
        }

        // Determine the user's choice and return the result.
        AtomicBoolean choiceResult = new AtomicBoolean(false);
        result.ifPresent(choice -> choiceResult.set(choice == yesButton));
        return choiceResult.get();
    }
}
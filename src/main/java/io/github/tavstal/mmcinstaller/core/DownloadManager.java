package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.core.logging.InstallerLogger;
import io.github.tavstal.mmcinstaller.utils.AlertUtils;
import io.github.tavstal.mmcinstaller.utils.FileUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The `DownloadManager` class is responsible for managing the download process of files.
 * It provides logging, progress updates, and UI callbacks to handle the download lifecycle.
 */
public class DownloadManager {
    private final InstallerLogger _logger; // Logger instance for logging download-related messages.
    private final InstallerTranslator _translator; // Translator instance for localizing messages.
    private final Consumer<String> _logCallback; // Callback for logging messages.
    private final Consumer<Double> _progressCallback; // Callback for progress updates.
    private final BiConsumer<Long, Long> _progressBarCallBack; // Progress bar to update UI.

    /**
     * Constructs a new `DownloadManager` instance.
     *
     * @param logCallback         A callback function for logging messages during the download process.
     * @param progressCallback    A callback function for updating progress values (0.0 to 1.0).
     * @param progressBarCallBack A callback function for updating the progress bar with downloaded and total bytes.
     */
    public DownloadManager(Consumer<String> logCallback, Consumer<Double> progressCallback, BiConsumer<Long, Long> progressBarCallBack) {
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        _translator = InstallerApplication.getTranslator();
        _logCallback = logCallback;
        _progressCallback = progressCallback;
        _progressBarCallBack = progressBarCallBack;
    }

    /**
     * Starts the download process by creating a task to download the required file.
     * Updates the UI with progress and handles success, failure, or cancellation of the task.
     */
    public void start() {
        // Define the installation directory, start menu directory, and output file for the download.
        File outputFile = new File(InstallerState.getCurrentPath(), ConfigLoader.get().download().fileName());

        // Create a Task for the download
        if (outputFile.exists() && outputFile.length() == InstallerState.getRequiredSpaceInBytes() && outputFile.length() > 0) {
            Platform.runLater(() -> { // Small delay to ensure UI is ready.
                _progressCallback.accept(1.0);
                handleDownloadedFile(outputFile);
            });
            return; // Skip download if file already exists and is valid.
        }

        Task<Void> downloadTask = createDownloadTask(
                ConfigLoader.get().download().link(),
                outputFile
        );

        // Handle the success of the download task.
        downloadTask.setOnSucceeded(event -> {
            _logger.Debug("Download task succeeded.");
            //progressBar.progressProperty().unbind(); // Unbind after completion.
            _progressCallback.accept(1.0); // Ensure it shows 100%.
            _logCallback.accept(_translator.Localize("Progress.Scripts.Creating"));
            handleDownloadedFile(outputFile); // Handle the downloaded file.
        });

        // Handle the failure of the download task.
        downloadTask.setOnFailed(event -> {
            _logger.Error("Download task failed: " + downloadTask.getException().getMessage());
            //progressBar.progressProperty().unbind();
            _progressCallback.accept(0.0); // Reset or show error.
        });

        // Handle the cancellation of the download task.
        downloadTask.setOnCancelled(event -> {
            _logger.Debug("Download task cancelled.");
            //progressBar.progressProperty().unbind();
            _progressCallback.accept(0.0);
            _logCallback.accept(_translator.Localize("Progress.Download.Cancelled"));
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
        String errorHeader = _translator.Localize("IO.Checksum.Error");
        String errorContent = _translator.Localize("IO.Checksum.ErrorDetails");
        String yesButtonText = _translator.Localize("Common.Next");
        String noButtonText = _translator.Localize("Common.Cancel");
        String outputChecksum = "";
        boolean checksumContinue;

        String outputFileAbPath = outputFile.getAbsolutePath();

        try {
            // Calculate the checksum of the downloaded file.
            outputChecksum = FileUtils.getFileChecksum(outputFileAbPath);
            if (outputChecksum.isEmpty()) {
                // Log an error if the checksum is empty and show an error alert.
                _logger.Error("Checksum is null or empty for file: " + outputFileAbPath);
                checksumContinue = AlertUtils.show(errorTitle, errorHeader, errorContent, yesButtonText, noButtonText, Alert.AlertType.ERROR);
            } else {
                checksumContinue = true;
            }
        } catch (Exception ex) {
            // Log an error if checksum calculation fails and show an error alert.
            _logger.Error("Failed to calculate checksum: " + ex.getMessage());
            checksumContinue = AlertUtils.show(errorTitle, errorHeader, errorContent, yesButtonText, noButtonText, Alert.AlertType.ERROR);
        }

        // If the user chooses not to continue, delete the file and exit the application.
        if (!checksumContinue) {
            if (outputFile.exists())
                //noinspection ResultOfMethodCallIgnored
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
                String header = _translator.Localize("IO.Checksum.Mismatch");
                String content = _translator.Localize("IO.Checksum.MismatchDetails", Map.of(
                        "expected", expectedChecksum,
                        "actual", outputChecksum
                ));
                if (!AlertUtils.show(title, header, content, yesButtonText, noButtonText, Alert.AlertType.WARNING)) {
                    // If the user chooses not to continue, delete the file and exit the application.
                    if (outputFile.exists())
                        //noinspection ResultOfMethodCallIgnored
                        outputFile.delete(); // Clean up the file if checksum validation fails.
                    System.exit(0);
                    return;
                } else {
                    _logger.Debug("Checksum mismatch but user chose to continue.");
                }
            }
        }

        // Initialize the setup manager and perform the setup process.
        File dir = new File(InstallerState.getCurrentPath());
        File startMenuDir = new File(InstallerState.getStartMenuPath());
        SetupManager manager = new SetupManager(outputFile, dir, startMenuDir, _logCallback);
        manager.setup();
    }

    /**
     * Creates a task to download a file from the specified URL and save it to the given output file.
     * The task handles HTTP requests, progress updates, and error handling.
     *
     * @param url        The URL of the file to download.
     * @param outputFile The file where the downloaded content will be saved.
     * @return A `Task<Void>` that performs the download operation.
     */
    private Task<Void> createDownloadTask(String url, File outputFile) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Log the start of the download process.
                _logCallback.accept(_translator.Localize("Progress.Download.Started", Map.of("file", outputFile.getAbsolutePath())));
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet(url);

                    // Execute the HTTP request and handle the response.
                    httpClient.execute(request, response -> {
                        _logger.Debug("Received response. Status: " + response.getCode());
                        if (response.getCode() != 200) {
                            // Handle non-200 HTTP status codes.
                            String errorMessage = _translator.Localize("Progress.Download.Failed") +
                                    " HTTP Status: " + response.getCode();
                            _logger.Error(errorMessage);
                            _logCallback.accept(errorMessage);
                            throw new IOException("Server returned non-200 status: " + response.getCode());
                        }

                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            long totalBytes = entity.getContentLength(); // Total size of the file.
                            long downloadedBytes = 0; // Bytes downloaded so far.
                            byte[] buffer = new byte[4096]; // Buffer for reading data.

                            // Read the content and write it to the output file.
                            try (InputStream is = entity.getContent();
                                 OutputStream os = new FileOutputStream(outputFile)) {
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    if (isCancelled()) {
                                        // Handle task cancellation.
                                        _logger.Debug("Download cancelled.");
                                        return null;
                                    }
                                    os.write(buffer, 0, bytesRead);
                                    downloadedBytes += bytesRead;
                                    _progressBarCallBack.accept(downloadedBytes, totalBytes); // Report progress.
                                }
                                _logger.Debug("Download complete.");
                                _logCallback.accept(_translator.Localize("Progress.Download.Completed", Map.of("file", outputFile.getAbsolutePath())));
                            } finally {
                                EntityUtils.consume(entity); // Ensure the entity is fully consumed.
                            }
                        } else {
                            // Handle null HTTP entity.
                            _logCallback.accept(_translator.Localize("Progress.Download.Failed"));
                            throw new IOException("HTTP Response entity is null. Cannot download.");
                        }
                        return null;
                    });
                } catch (IOException e) {
                    // Log and handle IO exceptions.
                    _logger.Error(String.format("Failed to download %s: %s", outputFile.getName(), e.getMessage()));
                    _logCallback.accept(_translator.Localize("Progress.Download.Error", Map.of("error", e.getMessage())));
                    throw e;
                } catch (Exception e) {
                    // Log and handle unexpected exceptions.
                    _logger.Error("An unexpected error occurred during download: " + e.getMessage());
                    _logCallback.accept(_translator.Localize("Progress.Download.Error", Map.of("error", e.getMessage())));
                    throw e;
                }
                return null;
            }
        };
    }
}

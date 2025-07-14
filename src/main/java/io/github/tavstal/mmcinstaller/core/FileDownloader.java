package io.github.tavstal.mmcinstaller.core;

import javafx.concurrent.Task;
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
 * Handles file downloading functionality with progress tracking and logging.
 * Provides a method to create a JavaFX Task for downloading a file from a given URL.
 */
public class FileDownloader {
    private final InstallerLogger _logger; // Logger for logging messages and errors.
    private final InstallerTranslator _translator; // Translator for localizing messages.

    /**
     * Constructs a FileDownloader instance with the specified logger and translator.
     *
     * @param logger     The logger to use for logging messages.
     * @param translator The translator to use for localizing messages.
     */
    public FileDownloader(InstallerLogger logger, InstallerTranslator translator) {
        this._logger = logger.WithModule(this.getClass());
        this._translator = translator;
    }

    /**
     * Creates a JavaFX Task to download a file from the specified URL to the given output file.
     * Logs progress and handles errors during the download process.
     *
     * @param url              The URL of the file to download.
     * @param outputFile       The file to save the downloaded content to.
     * @param logCallback      A callback to log messages during the download process.
     * @param progressCallback A callback to report download progress (downloaded bytes, total bytes).
     * @return A JavaFX Task that performs the file download.
     */
    public Task<Void> createDownloadTask(String url, File outputFile, Consumer<String> logCallback, BiConsumer<Long, Long> progressCallback) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Log the start of the download process.
                logCallback.accept(_translator.Localize("Progress.Download.Started", Map.of("file", outputFile.getAbsolutePath())));
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
                            logCallback.accept(errorMessage);
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
                                    progressCallback.accept(downloadedBytes, totalBytes); // Report progress.
                                }
                                _logger.Debug("Download complete.");
                                logCallback.accept(_translator.Localize("Progress.Download.Completed", Map.of("file", outputFile.getAbsolutePath())));
                            } finally {
                                EntityUtils.consume(entity); // Ensure the entity is fully consumed.
                            }
                        } else {
                            // Handle null HTTP entity.
                            logCallback.accept(_translator.Localize("Progress.Download.Failed"));
                            throw new IOException("HTTP Response entity is null. Cannot download.");
                        }
                        return null;
                    });
                } catch (IOException e) {
                    // Log and handle IO exceptions.
                    _logger.Error(String.format("Failed to download %s: %s", outputFile.getName(), e.getMessage()));
                    logCallback.accept(_translator.Localize("Progress.Download.Error", Map.of("error", e.getMessage())));
                    throw e;
                } catch (Exception e) {
                    // Log and handle unexpected exceptions.
                    _logger.Error("An unexpected error occurred during download: " + e.getMessage());
                    logCallback.accept(_translator.Localize("Progress.Download.Error", Map.of("error", e.getMessage())));
                    throw e;
                }
                return null;
            }
        };
    }
}
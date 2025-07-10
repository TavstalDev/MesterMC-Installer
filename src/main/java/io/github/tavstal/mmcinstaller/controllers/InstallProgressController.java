package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerConfig;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.core.SceneManager;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * Controller class for managing the installation progress UI and logic.
 * Handles downloading files, creating shortcuts, and updating the UI.
 */
public class InstallProgressController implements Initializable {

    private InstallerLogger _logger; // Logger instance for logging events.
    private InstallerTranslator _translator; // Translator instance for localization.
    private InstallerConfig _config; // Configuration instance for accessing settings.
    public Label progressTitle; // Label for the progress title.
    public Label progressDescription; // Label for the progress description.
    public Text progressAction; // Text for the current progress action.
    public ProgressBar progressBar; // Progress bar for visualizing download progress.
    public TextArea logTextArea; // Text area for displaying log messages.
    public Button cancelButton; // Button to cancel the installation process.

    /**
     * Initializes the controller after its root element has been completely processed.
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
        _config = InstallerApplication.getConfig();

        // Set localized text for UI elements.
        progressTitle.setText(_translator.Localize("Progress.Title"));
        progressDescription.setText(_translator.Localize("Progress.Description"));
        progressAction.setText(_translator.Localize("Progress.Action"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        // Get the current installation directory.
        File dir = new File(InstallerApplication.getCurrentPath());
        if (!dir.exists()) {
            // Create the directory if it does not exist and log the action.
            dir.mkdirs();
            logStep(_translator.Localize("Progress.DirectoryCreated", new HashMap<>() {
                {
                    put("path", dir.getAbsolutePath());
                }
            }));
        } else {
            // Log that the directory already exists.
            logStep(_translator.Localize("Progress.DirectoryExists", new HashMap<>() {
                {
                    put("path", dir.getAbsolutePath());
                }
            }));
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
     * Starts the download process for the MesterMC.jar file.
     * This method creates a background task to handle the download and updates the UI accordingly.
     */
    private void startDownload() {
        // Define the installation directory, start menu directory, and output file for the download.
        File dir = new File(InstallerApplication.getCurrentPath());
        File startMenuDir = new File(InstallerApplication.getStartMenuPath());
        File outputFile = new File(InstallerApplication.getCurrentPath(), _config.getJarDownloadName());

        // Create a Task for the download
        Task<Void> downloadTask = new Task<>() {
            /**
             * Executes the download process in a background thread.
             *
             * @return null upon completion.
             * @throws Exception if an error occurs during the download.
             */
            @Override
            protected Void call() throws Exception {
                // Log the start of the download process.
                logStep(_translator.Localize("Progress.Download.Started", new HashMap<>() {
                    {
                        put("file", outputFile.getAbsolutePath());
                    }
                }));

                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet(_config.getJarDownloadUrl());

                    // Define a response handler to process the response stream
                    HttpClientResponseHandler<Void> responseHandler = response -> {
                        _logger.Debug("Received response. Status: " + response.getCode());

                        if (response.getCode() != 200) { // Check for successful HTTP status
                            String errorMessage = _translator.Localize("Progress.Download.Failed") +
                                    " HTTP Status: " + response.getCode();
                            _logger.Error(errorMessage);
                            logStep(errorMessage);
                            // Crucially, throw an IOException to propagate the error
                            throw new IOException("Server returned non-200 status: " + response.getCode());
                        }

                        HttpEntity entity = response.getEntity(); // Get the entity for streaming

                        if (entity != null) {
                            long totalBytes = entity.getContentLength();
                            long downloadedBytes = 0;
                            byte[] buffer = new byte[4096]; // Buffer size

                            // Read the response entity and write it to the output file.
                            try (InputStream is = entity.getContent();
                                 OutputStream os = new FileOutputStream(outputFile)) {

                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    // Check for cancellation before writing
                                    if (isCancelled()) {
                                        _logger.Debug("Download cancelled.");
                                        return null; // Exit the handler
                                    }

                                    os.write(buffer, 0, bytesRead);
                                    downloadedBytes += bytesRead;

                                    // Update progress on the JavaFX Application Thread.
                                    if (totalBytes > 0) { // Avoid division by zero.
                                        updateProgress(downloadedBytes, totalBytes);
                                        _logger.Debug(String.format("Downloading... %.2f MB / %.2f MB (%.2f%%)",
                                                (double) downloadedBytes / (1024 * 1024),
                                                (double) totalBytes / (1024 * 1024),
                                                (double) downloadedBytes * 100 / totalBytes));
                                    } else {
                                        // If totalBytes is unknown, report only downloaded amount
                                        updateProgress(downloadedBytes, -1); // Use -1 for unknown total
                                        _logger.Debug("Downloading... " + (downloadedBytes / (1024 * 1024)) + " MB (Total unknown)");
                                    }
                                }
                                // Log the completion of the download.
                                _logger.Debug("Download complete.");
                                logStep(_translator.Localize("Progress.Download.Completed", new HashMap<>() {
                                    {
                                        put("file", outputFile.getAbsolutePath());
                                    }
                                }));
                            } finally {
                                // Ensure the entity's content stream is fully consumed/closed
                                EntityUtils.consume(entity);
                            }
                        } else {
                            // Log and throw an error if the response entity is null.
                            logStep(_translator.Localize("Progress.Download.Failed"));
                            throw new IOException("HTTP Response entity is null. Cannot download.");
                        }
                        return null; // Return Void from the handler
                    };

                    // Execute the GET request using the defined response handler
                    httpClient.execute(request, responseHandler);

                } catch (IOException e) {
                    // Log and handle any IO exceptions during the download.
                    _logger.Error(String.format("Failed to download %s: %s", _config.getJarDownloadName(), e.getMessage()));
                    logStep(_translator.Localize("Progress.Download.Error", new HashMap<>() {
                        {
                            put("error", e.getMessage());
                        }
                    }));
                    // Re-throw the exception to be caught by the Task's exception handling
                    throw e;
                }  catch (Exception e) {
                    // Catch any other unexpected exceptions
                    _logger.Error("An unexpected error occurred during download: " + e.getMessage());
                    logStep(_translator.Localize("Progress.Download.Error", new HashMap<>() {
                        {
                            put("error", e.getMessage());
                        }
                    }));
                    throw e;
                }
                return null; // Task completed successfully
            }
        };

        // Bind the ProgressBar's progress property to the Task's progress property.
        progressBar.progressProperty().bind(downloadTask.progressProperty());

        // Handle the success of the download task.
        downloadTask.setOnSucceeded(event -> {
            _logger.Debug("Download task succeeded.");
            progressBar.progressProperty().unbind(); // Unbind after completion.
            progressBar.setProgress(1.0); // Ensure it shows 100%.
            logStep(_translator.Localize("Progress.Scripts.Creating"));
            createLaunchScripts(outputFile, dir, startMenuDir);
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

    private void createLaunchScripts(File jarFile, File installDir, File startMenuDir) {
        String os = System.getProperty("os.name").toLowerCase();
        String scriptFileName;
        String scriptContent;
        String installPath = installDir.getAbsolutePath();
        File desktopDir = PathUtils.getUserDesktopDirectory();

        // Ensure the shortcut directory exists
        if (!startMenuDir.exists()) {
            if (!startMenuDir.mkdirs()) {
                _logger.Error("Failed to create start menu directory: " + startMenuDir.getAbsolutePath());
                logStep(_translator.Localize("Progress.Scripts.StartMenuDirCreationFailed", new HashMap<>() {
                    {
                        put("path", startMenuDir.getAbsolutePath());
                    }
                }));
                return;
            }
        }

        // Copy icon .png
        File iconImagePath = new File(installDir, "icon.png");
        try (InputStream iconStream = InstallerApplication.class.getResourceAsStream("assets/icon.png")) {

            if (iconStream == null) {
                logStep(_translator.Localize("Progress.Scripts.ResourceNotFound", new HashMap<>() {
                    {
                        put("resource", iconImagePath.getAbsolutePath());
                    }
                }));
                return;
            }

            // Use Files.copy to write the InputStream to the target Path
            Files.copy(iconStream, iconImagePath.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            logStep(_translator.Localize("Progress.Scripts.FileCopied", new HashMap<>() {
                {
                    put("fileName", "icon.png");
                }
            }));
        } catch (IOException e) {
            _logger.Error(String.format("Failed to copy %s: %s", "icon.png", e.getMessage()));
            logStep(_translator.Localize("Progress.Scripts.FileCopyError", new HashMap<>() {
                {
                    put("fileName", "icon.png");
                    put("error", e.getMessage());
                }
            }));
        }

        // Copy icon .ico
        File iconIcoPath = new File(installDir, "icon.ico");
        try (InputStream iconStream = InstallerApplication.class.getResourceAsStream("assets/favicon.ico")) {

            if (iconStream == null) {
                logStep(_translator.Localize("Progress.Scripts.ResourceNotFound", new HashMap<>() {
                    {
                        put("resource", iconIcoPath.getAbsolutePath());
                    }
                }));
                return;
            }

            // Use Files.copy to write the InputStream to the target Path
            Files.copy(iconStream, iconIcoPath.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            logStep(_translator.Localize("Progress.Scripts.FileCopied", new HashMap<>() {
                {
                    put("fileName", "icon.ico");
                }
            }));
        } catch (IOException e) {
            _logger.Error(String.format("Failed to copy %s: %s", "icon.ico", e.getMessage()));
            logStep(_translator.Localize("Progress.Scripts.FileCopyError", new HashMap<>() {
                {
                    put("fileName", "icon.ico");
                    put("error", e.getMessage());
                }
            }));
        }

        if (os.contains("win")) {
            // Windows script creation
            scriptFileName = _config.getBatchFileName();
            scriptContent = _config.getBatchFileContent()
                    .replaceAll("%dirPath%", installPath)
                    .replaceAll("%jarPath%", jarFile.getAbsolutePath());

            // Write .exe
            String exeFileName = _config.getExeFileName();
            File exeFile = new File(installDir, exeFileName);
            InstallerApplication.applicationToLaunch = exeFile.getAbsolutePath();
            File shortcutPath = new File(installDir, "MesterMC.lnk");
            try (InputStream exeStream = InstallerApplication.class.getResourceAsStream(_config.getExeFileResourcePath())) {

                if (exeStream == null) {
                    logStep(_translator.Localize("Progress.Scripts.ResourceNotFound", new HashMap<>() {
                        {
                            put("resource", _config.getExeFileResourcePath());
                        }
                    }));
                    return;
                }

                // Use Files.copy to write the InputStream to the target Path
                Files.copy(exeStream, exeFile.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
                logStep(_translator.Localize("Progress.Scripts.FileCopied", new HashMap<>() {
                    {
                        put("fileName", exeFileName);
                    }
                }));

                // Create windows shortcut
                try {
                    // Create a PowerShell script string
                    String powershellScript = _config.getExePowerShellScript()
                            .replaceAll("%shortcutPath%", shortcutPath.getAbsolutePath().replace("\\", "\\\\"))
                            .replaceAll("%exePath%", exeFile.getAbsolutePath().replace("\\", "\\\\"))
                            .replaceAll("%iconPath%", iconIcoPath.getAbsolutePath().replace("\\", "\\\\"));

                    // Save the script to a temporary .ps1 file
                    String tempDir = System.getProperty("java.io.tmpdir");
                    String ps1FilePath = tempDir + "create_shortcut.ps1";
                    try (FileWriter writer = new FileWriter(ps1FilePath)) {
                        writer.write(powershellScript);
                    }

                    // Execute the PowerShell script
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", ps1FilePath);
                    Process process = pb.start();

                    // Read output/error streams (optional, for debugging)
                    InputStream is = process.getInputStream();
                    InputStream es = process.getErrorStream();

                    // You might want to consume these streams to prevent process deadlock
                    // (e.g., by starting separate threads to read them)
                    // For simplicity, we'll just wait for the process to exit
                    int exitCode = process.waitFor();
                    System.out.println("PowerShell script exited with code: " + exitCode);

                    // Clean up the temporary script file
                    new java.io.File(ps1FilePath).delete();

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (InstallerApplication.shouldCreateDesktopShortcut()) {
                    File desktopShortcutFile = new File(desktopDir, "MesterMC.lnk");
                    _logger.Debug("Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                    Files.copy(shortcutPath.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logStep(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", new HashMap<>() {
                        {
                            put("filePath", desktopShortcutFile.getAbsolutePath());
                        }
                    }));
                }

                if (InstallerApplication.shouldCreateStartMenuShortcut()) {
                    File startMenuFile = new File(startMenuDir, "MesterMC.lnk");
                    _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                    Files.copy(shortcutPath.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logStep(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", new HashMap<>() {
                        {
                            put("filePath", startMenuFile.getAbsolutePath());
                        }
                    }));
                }
            } catch (IOException e) {
                _logger.Error(String.format("Failed to copy %s: %s", exeFileName, e.getMessage()));
                logStep(_translator.Localize("Progress.Scripts.FileCopyError", new HashMap<>() {
                    {
                        put("fileName", exeFileName);
                        put("error", e.getMessage());
                    }
                }));
            }

        } else if (os.contains("linux")) {
            scriptFileName = _config.getBashFileName();
            scriptContent = _config.getBashFileContent()
                    .replaceAll("%dirPath%", installPath)
                    .replaceAll("%jarPath%", jarFile.getAbsolutePath());

            // .desktop file creation for Linux/macOS Start Menu shortcut
            String desktopFileName = _config.getLinuxDesktopFileName();
            String desktopFileContent = _config.getLinuxDesktopFileContent()
                    .replaceAll("%dirPath%", installPath)
                    .replaceAll("%jarPath%", jarFile.getAbsolutePath());

            File linuxLaunchFile = new File(installDir, desktopFileName);
            InstallerApplication.applicationToLaunch = linuxLaunchFile.getAbsolutePath();
            try {
                _logger.Debug("Creating .desktop file: " + linuxLaunchFile.getAbsolutePath());
                Files.writeString(linuxLaunchFile.toPath(), desktopFileContent);

                if (InstallerApplication.shouldCreateDesktopShortcut()) {
                    File desktopShortcutFile = new File(desktopDir, desktopFileName);
                    _logger.Debug("Creating desktop shortcut: " + desktopShortcutFile.getAbsolutePath());
                    Files.copy(linuxLaunchFile.toPath(), desktopShortcutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logStep(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", new HashMap<>() {
                        {
                            put("filePath", desktopShortcutFile.getAbsolutePath());
                        }
                    }));
                }

                if (InstallerApplication.shouldCreateStartMenuShortcut()) {
                    File startMenuFile = new File(startMenuDir, desktopFileName);
                    _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                    Files.copy(linuxLaunchFile.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logStep(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", new HashMap<>() {
                        {
                            put("filePath", startMenuFile.getAbsolutePath());
                        }
                    }));
                }
            }
            catch (IOException e) {
                _logger.Error("Failed to write .desktop file: " + e.getMessage());
            }
        } else if (os.contains("mac")) {
            scriptFileName = _config.getZshFileName();
            scriptContent = _config.getZshFileContent()
                    .replaceAll("%dirPath%", installPath)
                    .replaceAll("%jarPath%", jarFile.getAbsolutePath());

            // TODO: Shortcuts
        }
        else {
            _logger.Error("Unsupported OS for script creation: " + os);
            logStep(_translator.Localize("Progress.Scripts.UnsupportedOS", new HashMap<>() {
                {
                    put("os", os);
                }
            }));
            return;
        }

        File scriptFile = new File(installDir, scriptFileName);
        try {
            Files.writeString(scriptFile.toPath(), scriptContent);
            _logger.Debug("Created launch script: " + scriptFile.getAbsolutePath());
            logStep(_translator.Localize("Progress.Scripts.LauncherCreated", new HashMap<>() {
                {
                    put("filePath", scriptFile.getAbsolutePath());
                }
            }));

            // For Linux/macOS, make the script executable
            if (os.contains("linux") || os.contains("mac")) {
                makeScriptExecutable(scriptFile);
            }

            InstallerApplication.setActiveScene(SceneManager.getInstallCompleteScene());
        } catch (IOException e) {
            _logger.Error("Failed to write launch scripts: " + e.getMessage());
            logStep(_translator.Localize("Progress.Scripts.LauncherCreationError", new HashMap<>() {
                {
                    put("error", e.getMessage());
                }
            }));
        }
    }

    /**
     * Makes a shell script executable using chmod +x.
     * Only for Linux and macOS.
     *
     * @param scriptFile The .sh file to make executable.
     */
    private void makeScriptExecutable(File scriptFile) {
        try {
            _logger.Debug("Attempting to make script executable: " + scriptFile.getAbsolutePath());
            logStep("Making launcher executable: " + scriptFile.getAbsolutePath());
            logStep(_translator.Localize("Progress.Scripts.MakingExecutable"));
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", scriptFile.getAbsolutePath());
            Process p = pb.start();

            // Wait for the process to complete and check exit code
            boolean finished = p.waitFor(10, TimeUnit.SECONDS); // Wait up to 10 seconds

            if (finished) {
                int exitCode = p.exitValue();
                if (exitCode == 0) {
                    _logger.Info("Script made executable: " + scriptFile.getAbsolutePath());
                    logStep(_translator.Localize("Progress.Scripts.ExecutableMade", new HashMap<>() {
                        {
                            put("filePath", scriptFile.getAbsolutePath());
                        }
                    }));
                } else {
                    // Read error stream for more details
                    String error = new String(p.getErrorStream().readAllBytes());
                    _logger.Error("chmod failed with exit code " + exitCode + ": " + error);
                    logStep(_translator.Localize("Progress.Scripts.ExecutableError", new HashMap<>() {
                        {
                            put("filePath", scriptFile.getAbsolutePath());
                            put("error", error);
                        }
                    }));
                }
            } else {
                _logger.Error("chmod process timed out.");
                logStep(_translator.Localize("Progress.Scripts.ExecutableTimeout"));
                p.destroyForcibly(); // Terminate the process
            }
        } catch (IOException | InterruptedException e) {
            _logger.Error("Exception while making script executable: " + e.getMessage());
            logStep(_translator.Localize("Progress.Scripts.ExecutableException", new HashMap<>() {
                {
                    put("error", e.getMessage());
                }
            }));
        }
    }
}
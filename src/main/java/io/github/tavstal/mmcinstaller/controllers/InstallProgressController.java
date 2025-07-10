package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.Translator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class InstallProgressController implements Initializable {

    private static final Log log = LogFactory.getLog(InstallProgressController.class);
    private InstallerLogger _logger; // Logger instance for logging events.
    public Label progressTitle;
    public Label progressDescription;
    public Text progressAction;
    public ProgressBar progressBar;
    public TextArea logTextArea;
    public Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getCustomLogger().WithModule(this.getClass());
        // Translator instance for localization.
        Translator _translator = InstallerApplication.getTranslator();
        String os = System.getProperty("os.name").toLowerCase();

        progressTitle.setText(_translator.Localize("Progress.Title"));
        progressDescription.setText(_translator.Localize("Progress.Description"));
        progressAction.setText(_translator.Localize("Progress.Action"));

        cancelButton.setText(_translator.Localize("Common.Cancel"));

        File dir = new File(InstallerApplication.getCurrentPath());
        if (!dir.exists()) {
            dir.mkdirs(); // Create the directory if it doesn't exist
            logStep("Creating directory: " + dir.getAbsolutePath());
        }
        else
            logStep("Directory already exists: " + dir.getAbsolutePath());

        startDownload();
    }

    @FXML
    protected void onCancelButtonClick() {
        System.exit(0);
    }

    private void logStep(String message) {
        Platform.runLater(() -> {
            // Append the new message
            logTextArea.appendText(message + "\n");

            // Scroll to the end
            logTextArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void startDownload() {
        File dir = new File(InstallerApplication.getCurrentPath());
        File startMenuDir = new File(InstallerApplication.getStartMenuPath());
        File outputFile = new File(InstallerApplication.getCurrentPath(), "MesterMC.jar");

        // Create a Task for the download
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                logStep("Starting download of MesterMC.jar...");
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet("https://mestermc.b-cdn.net/MesterMC.jar");
                    HttpResponse response = httpClient.execute(request);
                    HttpEntity entity = response.getEntity();

                    if (entity != null) {
                        long totalBytes = entity.getContentLength();
                        long downloadedBytes = 0;
                        byte[] buffer = new byte[4096]; // Buffer size

                        try (InputStream is = entity.getContent();
                             OutputStream os = new FileOutputStream(outputFile)) {

                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                                downloadedBytes += bytesRead;

                                // Update progress on the JavaFX Application Thread
                                if (totalBytes > 0) { // Avoid division by zero
                                    updateProgress(downloadedBytes, totalBytes);
                                    _logger.Debug(String.format("Downloading... %.2f MB / %.2f MB",
                                            (double) downloadedBytes / (1024 * 1024),
                                            (double) totalBytes / (1024 * 1024)));
                                } else {
                                    _logger.Debug("Downloading... " + (downloadedBytes / (1024 * 1024)) + " MB");
                                    // If Content-Length is not available, progress bar won't be accurate
                                    // You might set progress to -1 (indeterminate) or just update bytes.
                                }
                            }
                            _logger.Debug("Download complete.");
                            logStep("Download complete!");
                        }
                    } else {
                        logStep("HTTP Response entity is null. Cannot download.");
                        throw new IOException("HTTP Response entity is null. Cannot download.");
                    }
                } catch (IOException e) {
                    _logger.Error("Failed to download .jar: " + e.getMessage());
                    logStep("Download failed: " + e.getMessage());
                    return null;
                }
                return null;
            }
        };

        // Bind ProgressBar's progress property to the Task's progress property
        progressBar.progressProperty().bind(downloadTask.progressProperty());

        // Handle success and failure
        downloadTask.setOnSucceeded(event -> {
            _logger.Debug("Download task succeeded.");
            progressBar.progressProperty().unbind(); // Unbind after completion
            progressBar.setProgress(1.0); // Ensure it shows 100%
            logStep("Creating launch scripts...");
            createLaunchScripts(outputFile, dir, startMenuDir);
        });

        downloadTask.setOnFailed(event -> {
            _logger.Error("Download task failed: " + downloadTask.getException().getMessage());
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0.0); // Reset or show error
            //logTextArea.setText("Download failed: " + downloadTask.getException().getMessage());
            // Show error message to user
        });

        downloadTask.setOnCancelled(event -> {
            _logger.Debug("Download task cancelled.");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0.0);
            logStep("Download cancelled by user.");
        });

        // Start the task in a new thread
        new Thread(downloadTask).start();
    }

    private void createLaunchScripts(File jarFile, File installDir, File startMenuDir) {
        String os = System.getProperty("os.name").toLowerCase();
        String scriptFileName;
        String scriptContent;
        String jarFileName = jarFile.getName(); // MesterMC.jar
        String installPath = installDir.getAbsolutePath();

        // Ensure the shortcut directory exists
        if (!startMenuDir.exists()) {
            if (!startMenuDir.mkdirs()) {
                _logger.Error("Failed to create start menu directory: " + startMenuDir.getAbsolutePath());
                logStep("Error: Cannot create start menu directory!");
                return;
            }
        }

        if (os.contains("win")) {
            scriptFileName = "MesterMC.bat"; // Windows batch file
            // Use 'start "" javaw' to launch without a console window and not block the batch script
            // pushd/popd to temporarily change directory to handle relative paths better
            scriptContent = "@echo off\n" +
                    "set \"INSTALL_DIR=" + installPath + "\"\n" +
                    "pushd \"%INSTALL_DIR%\"\n" +
                    "start \"\" javaw -jar \"" + jarFileName + "\"\n" +
                    "popd\n" +
                    "exit\n";

            // Write .exe
            File exeFile = new File(installDir, "MesterMC.exe");
            try (InputStream exeStream = InstallerApplication.class.getResourceAsStream("exe/MesterMC.exe")) {

                if (exeStream == null) {
                    logStep("Error: MesterMC.exe resource not found!");
                    return;
                }

                // Use Files.copy to write the InputStream to the target Path
                Files.copy(exeStream, exeFile.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);

                logStep("MesterMC.exe copied to: " + exeFile.getAbsolutePath());
            } catch (IOException e) {
                _logger.Error("Failed to copy MesterMC.exe: " + e.getMessage());
                logStep("Error: Failed to copy MesterMC.exe: " + e.getMessage());
            }

        } else if (os.contains("linux") || os.contains("mac")) {
            scriptFileName = "MesterMC.sh"; // Shell script
            // Use "$INSTALL_DIR" to handle spaces in paths
            scriptContent = "#!/bin/bash\n" +
                    "INSTALL_DIR=\"" + installPath + "\"\n" +
                    "cd \"$INSTALL_DIR\" || { echo \"Failed to change directory to $INSTALL_DIR\"; exit 1; }\n" +
                    "java -jar \"" + jarFileName + "\"\n";

            // .desktop file creation for Linux/macOS Start Menu shortcut
        } else {
            _logger.Error("Unsupported OS for script creation: " + os);
            logStep("Warning: Unsupported OS for script creation: " + os);
            return;
        }

        File scriptFile = new File(installDir, scriptFileName);
        try {
            Files.writeString(scriptFile.toPath(), scriptContent);
            _logger.Info("Created launch script: " + scriptFile.getAbsolutePath());
            logStep("Launcher script created: " + scriptFile.getAbsolutePath());

            // For Linux/macOS, make the script executable
            if (os.contains("linux") || os.contains("mac")) {
                makeScriptExecutable(scriptFile);
            }

            InstallerApplication.setActiveScene(InstallerApplication.getInstallCompleteScene());
        } catch (IOException e) {
            _logger.Error("Failed to write launch scripts: " + e.getMessage());
            logStep("Error: Failed to create launcher scripts: " + e.getMessage());
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
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", scriptFile.getAbsolutePath());
            Process p = pb.start();

            // Wait for the process to complete and check exit code
            boolean finished = p.waitFor(10, TimeUnit.SECONDS); // Wait up to 10 seconds

            if (finished) {
                int exitCode = p.exitValue();
                if (exitCode == 0) {
                    _logger.Info("Script made executable: " + scriptFile.getAbsolutePath());
                    logStep("Launcher made executable: " + scriptFile.getAbsolutePath());
                } else {
                    // Read error stream for more details
                    String error = new String(p.getErrorStream().readAllBytes());
                    _logger.Error("chmod failed with exit code " + exitCode + ": " + error);
                    logStep("Error: Failed to make launcher executable: " + error);
                }
            } else {
                _logger.Error("chmod process timed out.");
                logStep("Error: Failed to make launcher executable (timeout)!");
                p.destroyForcibly(); // Terminate the process
            }
        } catch (IOException | InterruptedException e) {
            _logger.Error("Exception while making script executable: " + e.getMessage());
            logStep("Error: Failed to make launcher executable: " + e.getMessage());
        }
    }
}
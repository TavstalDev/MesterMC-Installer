package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import io.github.tavstal.mmcinstaller.utils.SceneManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SetupManager {
    private final InstallerLogger _logger;
    private final InstallerTranslator _translator;
    private final InstallerConfig _config;
    private final Consumer<String> _logCallback;
    private final File jarFile;
    private final File installDir;
    private final File startMenuDir;

    public SetupManager(File jarFile, File installDir, File startMenuDir, Consumer<String> logCallback) {
        this._logger = InstallerApplication.getLogger().WithModule(this.getClass());
        this._translator = InstallerApplication.getTranslator();
        this._config = InstallerApplication.getConfig();
        this.jarFile = jarFile;
        this.installDir = installDir;
        this.startMenuDir = startMenuDir;
        this._logCallback = logCallback;
    }

    public void setup() {
        String os = System.getProperty("os.name").toLowerCase();

        try
        {
            if (os.contains("win")) {


            } else if (os.contains("linux")) {

            } else if (os.contains("mac")) {

            }
        }
        catch (Exception ex) {

        }
    }

    private void setupWindows() {
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
                _logCallback.accept(_translator.Localize("Progress.Scripts.ResourceNotFound", new HashMap<>() {
                    {
                        put("resource", _config.getExeFileResourcePath());
                    }
                }));
                return;
            }

            // Use Files.copy to write the InputStream to the target Path
            Files.copy(exeStream, exeFile.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopied", new HashMap<>() {
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
                _logCallback.accept(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", new HashMap<>() {
                    {
                        put("filePath", desktopShortcutFile.getAbsolutePath());
                    }
                }));
            }

            if (InstallerApplication.shouldCreateStartMenuShortcut()) {
                File startMenuFile = new File(startMenuDir, "MesterMC.lnk");
                _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                Files.copy(shortcutPath.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", new HashMap<>() {
                    {
                        put("filePath", startMenuFile.getAbsolutePath());
                    }
                }));
            }
        } catch (IOException e) {
            _logger.Error(String.format("Failed to copy %s: %s", exeFileName, e.getMessage()));
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopyError", new HashMap<>() {
                {
                    put("fileName", exeFileName);
                    put("error", e.getMessage());
                }
            }));
        }
    }

    private void setupLinux() {
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
                _logCallback.accept(_translator.Localize("Progress.Scripts.DesktopShortcutCreated", new HashMap<>() {
                    {
                        put("filePath", desktopShortcutFile.getAbsolutePath());
                    }
                }));
            }

            if (InstallerApplication.shouldCreateStartMenuShortcut()) {
                File startMenuFile = new File(startMenuDir, desktopFileName);
                _logger.Debug("Creating start menu shortcut: " + startMenuFile.getAbsolutePath());
                Files.copy(linuxLaunchFile.toPath(), startMenuFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuShortcutCreated", new HashMap<>() {
                    {
                        put("filePath", startMenuFile.getAbsolutePath());
                    }
                }));
            }
        }
        catch (IOException e) {
            _logger.Error("Failed to write .desktop file: " + e.getMessage());
        }
    }

    private void setupMac() {
        scriptFileName = _config.getZshFileName();
        scriptContent = _config.getZshFileContent()
                .replaceAll("%dirPath%", installPath)
                .replaceAll("%jarPath%", jarFile.getAbsolutePath());

        // TODO: Shortcuts
    }

    private void makeScriptExecutable(File scriptFile) {
        try {
            _logger.Debug("Attempting to make script executable: " + scriptFile.getAbsolutePath());
            _logCallback.accept("Making launcher executable: " + scriptFile.getAbsolutePath());
            _logCallback.accept(_translator.Localize("Progress.Scripts.MakingExecutable"));
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", scriptFile.getAbsolutePath());
            Process p = pb.start();

            // Wait for the process to complete and check exit code
            boolean finished = p.waitFor(10, TimeUnit.SECONDS); // Wait up to 10 seconds

            if (finished) {
                int exitCode = p.exitValue();
                if (exitCode == 0) {
                    _logger.Info("Script made executable: " + scriptFile.getAbsolutePath());
                    _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableMade", new HashMap<>() {
                        {
                            put("filePath", scriptFile.getAbsolutePath());
                        }
                    }));
                } else {
                    // Read error stream for more details
                    String error = new String(p.getErrorStream().readAllBytes());
                    _logger.Error("chmod failed with exit code " + exitCode + ": " + error);
                    _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableError", new HashMap<>() {
                        {
                            put("filePath", scriptFile.getAbsolutePath());
                            put("error", error);
                        }
                    }));
                }
            } else {
                _logger.Error("chmod process timed out.");
                _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableTimeout"));
                p.destroyForcibly(); // Terminate the process
            }
        } catch (IOException | InterruptedException e) {
            _logger.Error("Exception while making script executable: " + e.getMessage());
            _logCallback.accept(_translator.Localize("Progress.Scripts.ExecutableException", new HashMap<>() {
                {
                    put("error", e.getMessage());
                }
            }));
        }
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
                _logCallback.accept(_translator.Localize("Progress.Scripts.StartMenuDirCreationFailed", new HashMap<>() {
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
                _logCallback.accept(_translator.Localize("Progress.Scripts.ResourceNotFound", new HashMap<>() {
                    {
                        put("resource", iconImagePath.getAbsolutePath());
                    }
                }));
                return;
            }

            // Use Files.copy to write the InputStream to the target Path
            Files.copy(iconStream, iconImagePath.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopied", new HashMap<>() {
                {
                    put("fileName", "icon.png");
                }
            }));
        } catch (IOException e) {
            _logger.Error(String.format("Failed to copy %s: %s", "icon.png", e.getMessage()));
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopyError", new HashMap<>() {
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
                _logCallback.accept(_translator.Localize("Progress.Scripts.ResourceNotFound", new HashMap<>() {
                    {
                        put("resource", iconIcoPath.getAbsolutePath());
                    }
                }));
                return;
            }

            // Use Files.copy to write the InputStream to the target Path
            Files.copy(iconStream, iconIcoPath.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopied", new HashMap<>() {
                {
                    put("fileName", "icon.ico");
                }
            }));
        } catch (IOException e) {
            _logger.Error(String.format("Failed to copy %s: %s", "icon.ico", e.getMessage()));
            _logCallback.accept(_translator.Localize("Progress.Scripts.FileCopyError", new HashMap<>() {
                {
                    put("fileName", "icon.ico");
                    put("error", e.getMessage());
                }
            }));
        }


        else {
            _logger.Error("Unsupported OS for script creation: " + os);
            _logCallback.accept(_translator.Localize("Progress.Scripts.UnsupportedOS", new HashMap<>() {
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
            _logCallback.accept(_translator.Localize("Progress.Scripts.LauncherCreated", new HashMap<>() {
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
            _logCallback.accept(_translator.Localize("Progress.Scripts.LauncherCreationError", new HashMap<>() {
                {
                    put("error", e.getMessage());
                }
            }));
        }
    }
}

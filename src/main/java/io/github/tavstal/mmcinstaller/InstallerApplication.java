package io.github.tavstal.mmcinstaller;

import io.github.tavstal.mmcinstaller.core.InstallerConfig;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;
import java.io.InputStream;

public class InstallerApplication extends Application {
    private static InstallerConfig _config;
    /**
     * Gets the configuration instance.
     *
     * @return The configuration instance.
     */
    public static InstallerConfig getConfig() {
        if (_config == null) {
            _config = new InstallerConfig();
            _config.Initialize();
        }
        return _config;
    }

    private static InstallerLogger _logger;
    /**
     * Gets the custom logger instance.
     *
     * @return The logger instance.
     */
    public static InstallerLogger getCustomLogger() {
        return _logger;
    }

    private static InstallerTranslator _translator;
    /**
     * Gets the translator instance.
     *
     * @return The translator instance.
     */
    public static InstallerTranslator getTranslator() {
        return _translator;
    }

    private static boolean _isLicenseAccepted = false;
    /**
     * Checks if the license is accepted.
     *
     * @return True if the license is accepted, false otherwise.
     */
    public static boolean isLicenseAccepted() {
        return _isLicenseAccepted;
    }
    /**
     * Sets the license acceptance state.
     *
     * @param isLicenseAccepted True if the license is accepted, false otherwise.
     */
    public static void setLicenseAccepted(boolean isLicenseAccepted) {
        _isLicenseAccepted = isLicenseAccepted;
    }

    private static String _currentPath = null;
    /**
     * Gets the current installation path.
     *
     * @return The current installation path.
     */
    public static String getCurrentPath() {
        return _currentPath;
    }
    /**
     * Sets the current installation path.
     *
     * @param currentPath The installation path to set.
     */
    public static void setCurrentPath(String currentPath) {
        _currentPath = currentPath;
    }

    private static boolean _createDesktopShortcut = true;
    /**
     * Checks if a desktop shortcut should be created.
     *
     * @return True if a desktop shortcut should be created, false otherwise.
     */
    public static boolean shouldCreateDesktopShortcut() {
        return _createDesktopShortcut;
    }
    /**
     * Sets the desktop shortcut creation state.
     *
     * @param createDesktopShortcut True to create a desktop shortcut, false otherwise.
     */
    public static void setCreateDesktopShortcut(boolean createDesktopShortcut) {
        _createDesktopShortcut = createDesktopShortcut;
    }

    private static String _startMenuPath = null;
    /**
     * Gets the Start Menu shortcut path.
     *
     * @return The Start Menu shortcut path.
     */
    public static String getStartMenuPath() {
        return _startMenuPath;
    }
    /**
     * Sets the Start Menu shortcut path.
     *
     * @param startMenuPath The Start Menu shortcut path to set.
     */
    public static void setStartMenuPath(String startMenuPath) {
        _startMenuPath = startMenuPath;
    }

    private static boolean _createStartMenuShortcut = true;
    /**
     * Checks if a Start Menu shortcut should be created.
     *
     * @return True if a Start Menu shortcut should be created, false otherwise.
     */
    public static boolean shouldCreateStartMenuShortcut() {
        return _createStartMenuShortcut;
    }
    /**
     * Sets the Start Menu shortcut creation state.
     *
     * @param createStartMenuShortcut True to create a Start Menu shortcut, false otherwise.
     */
    public static void setCreateStartMenuShortcut(boolean createStartMenuShortcut) {
        _createStartMenuShortcut = createStartMenuShortcut;
    }

    private static long _requiredSpace = 0;
    /**
     * Gets the required space for installation in MB.
     *
     * @return The required space as a formatted string.
     */
    public static String getRequiredSpace() {
        return String.format("%d MB", _requiredSpace / (1024 * 1024));
    }

    /**
     * Application entry point. Initializes the application and sets up the primary stage.
     *
     * @param stage The primary stage for the application.
     * @throws IOException If an error occurs during initialization.
     */
    @Override
    public void start(Stage stage) throws IOException {
        _logger = new InstallerLogger(getConfig().getDebugMode());
        _translator = new InstallerTranslator(new String[] {"eng", "hun"});
        _translator.Load();
        _stage = stage;


        _stage.setTitle(_translator.Localize("Window.Title"));
        _stage.setScene(getWelcomeScene());

        try {
            InputStream iconStream = InstallerApplication.class.getResourceAsStream("assets/icon.png");

            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
                stage.setIconified(true);
                _logger.Debug("Icon 'icon.png' loaded successfully.");
            } else {
                _logger.Warn("Icon 'icon.png' not found in resources.");
            }
        } catch (Exception e) {
            _logger.Error("An error occurred while loading the icon: " + e.getMessage());
        }

        _stage.setMinWidth(700);
        _stage.setWidth(700);
        _stage.setMaxWidth(700);
        _stage.setMinHeight(400);
        _stage.setHeight(400);
        _stage.setMaxHeight(400);
        _stage.setResizable(false);

        _stage.centerOnScreen();
        _stage.show();
        _logger.Debug("InstallerApplication started successfully.");

        _logger.Debug("Checking .jar size.");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            _logger.Debug("Sending HTTP request...");
            HttpHead request = new HttpHead(getConfig().getJarDownloadUrl());

            HttpClientResponseHandler<Void> responseHandler = response -> {
                _logger.Debug("Received response. Status: " + response.getCode());

                // We only care about headers for a HEAD request
                Header contentTypeHeader = response.getFirstHeader("Content-Type");
                if (contentTypeHeader != null) {
                    _logger.Debug("Content-Type: " + contentTypeHeader.getValue());
                } else {
                    _logger.Debug("Content-Type header not found.");
                }

                Header contentLengthHeader = response.getFirstHeader("Content-Length");
                if (contentLengthHeader != null) {
                    try {
                        _requiredSpace = Long.parseLong(contentLengthHeader.getValue());
                        _logger.Debug("Content-Length: " + _requiredSpace + " bytes");
                    } catch (NumberFormatException e) {
                        _logger.Error("Content-Length header value is not a valid number: " + contentLengthHeader.getValue());
                    }
                } else {
                    _logger.Error("Content-Length header not found in response.");
                }
                return null;
            };

            httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            _logger.Error("Failed to check jar file.");
        }
    }

    /**
     * Main method. Launches the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch();
    }

    private static Stage _stage;
    /**
     * Sets the active scene for the application.
     *
     * @param scene The scene to set as active.
     */
    public static void setActiveScene(Scene scene) {
        _stage.setScene(scene);
    }

    private static Scene welcomeScene = null;
    /**
     * Retrieves the Welcome scene. Loads it from the FXML file if not already loaded.
     *
     * @return The Welcome scene, or null if an error occurs during loading.
     */
    public static Scene getWelcomeScene() {
        if (welcomeScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/WelcomeView.fxml"));
                welcomeScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading WelcomeView.fxml: " + e.getMessage());
                return null;
            }
        }

        return welcomeScene;
    }

    private static Scene licenseScene = null;
    /**
     * Retrieves the License scene. Loads it from the FXML file if not already loaded.
     *
     * @return The License scene, or null if an error occurs during loading.
     */
    public static Scene getLicenseScene() {
        if (licenseScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/LicenseView.fxml"));
                licenseScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading LicenseView.fxml: " + e.getMessage());
                return null;
            }
        }

        return licenseScene;
    }

    private static Scene installPathScene = null;
    /**
     * Retrieves the Install Path scene. Loads it from the FXML file if not already loaded.
     *
     * @return The Install Path scene, or null if an error occurs during loading.
     */
    public static Scene getInstallPathScene() {
        if (installPathScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/PathSelectView.fxml"));
                installPathScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading PathSelectView.fxml: " + e.getMessage());
                return null;
            }
        }
        return installPathScene;
    }

    private static Scene shortcutScene = null;
    /**
     * Retrieves the Shortcut scene. Loads it from the FXML file if not already loaded.
     *
     * @return The Shortcut scene, or null if an error occurs during loading.
     */
    public static Scene getShortcutScene() {
        if (shortcutScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/ShortcutView.fxml"));
                shortcutScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading ShortcutView.fxml: " + e.getMessage());
                return null;
            }
        }
        return shortcutScene;
    }

    private static Scene reviewScene = null;
    /**
     * Retrieves the Review scene. Loads it from the FXML file if not already loaded.
     *
     * @return The Review scene, or null if an error occurs during loading.
     */
    public static Scene getReviewScene() {
        if (reviewScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/ReviewView.fxml"));
                reviewScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading ReviewView.fxml: " + e.getMessage());
                return null;
            }
        }
        return reviewScene;
    }

    private static Scene installProgressScene = null;
    /**
     * Retrieves the Install Progress scene. Loads it from the FXML file if not already loaded.
     *
     * @return The Install Progress scene, or null if an error occurs during loading.
     */
    public static Scene getInstallProgressScene() {
        if (installProgressScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/InstallProgressView.fxml"));
                installProgressScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading InstallProgressView.fxml: " + e.getMessage());
                return null;
            }
        }
        return installProgressScene;
    }

    private static Scene installCompleteScene = null;
    /**
     * Retrieves the Install Complete scene. Loads it from the FXML file if not already loaded.
     *
     * @return The Install Complete scene, or null if an error occurs during loading.
     */
    public static Scene getInstallCompleteScene() {
        if (installCompleteScene == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource("views/InstallCompleteView.fxml"));
                installCompleteScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                _logger.Error("Error loading InstallCompleteView.fxml: " + e.getMessage());
                return null;
            }
        }
        return installCompleteScene;
    }
}
package io.github.tavstal.mmcinstaller.utils;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the loading and caching of JavaFX scenes for the application.
 * Ensures that scenes are loaded only once and reused when requested.
 */
public class SceneManager {
    // A map to store loaded scenes, keyed by their view names.
    private static final Map<String, Scene> loadedScenes = new HashMap<>();

    /**
     * Retrieves a JavaFX scene by its view name and FXML file path.
     * If the scene is not already loaded, it loads the scene from the specified FXML file
     * and caches it for future use.
     *
     * @param viewName The unique name of the view to identify the scene.
     * @param fxmlPath The path to the FXML file for the scene.
     * @return The loaded JavaFX Scene, or null if an error occurs during loading.
     */
    public static Scene getScene(String viewName, String fxmlPath) {
        return loadedScenes.computeIfAbsent(viewName, k -> {
            try {
                // Load the FXML file and create a new Scene.
                FXMLLoader fxmlLoader = new FXMLLoader(InstallerApplication.class.getResource(fxmlPath));
                return new Scene(fxmlLoader.load());
            } catch (IOException e) {
                // Log an error if the FXML file cannot be loaded.
                InstallerApplication.getLogger().Error("Error loading " + fxmlPath + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Retrieves the JavaFX scene for the Language view.
     * Loads the scene from the "views/LanguageView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Language view.
     */
    public static Scene getLanguageViewScene() {
        return getScene("LanguageView", "views/LanguageView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the Welcome view.
     * Loads the scene from the "views/WelcomeView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Welcome view.
     */
    public static Scene getWelcomeScene() {
        return getScene("WelcomeView", "views/WelcomeView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the License view.
     * Loads the scene from the "views/LicenseView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the License view.
     */
    public static Scene getLicenseScene() {
        return getScene("LicenseView", "views/LicenseView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the Install Path selection view.
     * Loads the scene from the "views/PathSelectView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Install Path selection view.
     */
    public static Scene getInstallPathScene() {
        return getScene("PathSelectView", "views/PathSelectView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the Shortcut creation view.
     * Loads the scene from the "views/ShortcutView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Shortcut creation view.
     */
    public static Scene getShortcutScene() {
        return getScene("ShortcutView", "views/ShortcutView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the Review view.
     * Loads the scene from the "views/ReviewView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Review view.
     */
    public static Scene getReviewScene() {
        return getScene("ReviewView", "views/ReviewView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the Install Progress view.
     * Loads the scene from the "views/InstallProgressView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Install Progress view.
     */
    public static Scene getInstallProgressScene() {
        return getScene("InstallProgressView", "views/InstallProgressView.fxml");
    }

    /**
     * Retrieves the JavaFX scene for the Install Complete view.
     * Loads the scene from the "views/InstallCompleteView.fxml" file if not already cached.
     *
     * @return The JavaFX Scene for the Install Complete view.
     */
    public static Scene getInstallCompleteScene() {
        return getScene("InstallCompleteView", "views/InstallCompleteView.fxml");
    }
}

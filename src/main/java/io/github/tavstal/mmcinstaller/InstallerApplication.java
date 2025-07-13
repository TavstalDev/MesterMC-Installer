package io.github.tavstal.mmcinstaller;

import io.github.tavstal.mmcinstaller.core.ConfigLoader;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import javafx.application.Application;
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
    //#region Constants
    public final static  int LANG_WIDTH = 400;
    public final static int LANG_HEIGHT = 200;
    public final static int WIDTH = 700;
    public final static int HEIGHT = 400;
    //#endregion

    //#region Variables
    private static Stage _stage;
    private static InstallerLogger _logger;
    /**
     * Gets the logger instance.
     *
     * @return The logger instance.
     */
    public static InstallerLogger getLogger() {
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
    //#endregion

    /**
     * Application entry point. Initializes the application and sets up the primary stage.
     *
     * @param stage The primary stage for the application.
     * @throws IOException If an error occurs during initialization.
     */
    @Override
    public void start(Stage stage) throws IOException {
        ConfigLoader.init();
        _logger = new InstallerLogger(ConfigLoader.get().debug());
        _translator = new InstallerTranslator(new String[] {"eng", "hun"});
        _translator.Load();
        _stage = stage;


        _stage.setTitle(_translator.Localize("Window.Title"));
        _stage.setScene(SceneManager.getLanguageViewScene());

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

        changeStageSize(true);
        _stage.setResizable(false);

        _stage.centerOnScreen();
        _stage.show();
        _stage.toFront();
        _stage.requestFocus();
        _logger.Debug("InstallerApplication started successfully.");

        _logger.Debug("Checking .jar size.");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            _logger.Debug("Sending HTTP request...");
            HttpHead request = new HttpHead(ConfigLoader.get().download().link());

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
                        long requiredSpace = Long.parseLong(contentLengthHeader.getValue());
                        InstallerState.setRequiredSpace(requiredSpace);
                        _logger.Debug("Content-Length: " + requiredSpace + " bytes");
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

    //#region Functions
    /**
     * Sets the active scene for the application.
     *
     * @param scene The scene to set as active.
     */
    public static void setActiveScene(Scene scene) {
        _stage.setScene(scene);
    }

    /**
     * Adjusts the size of the application stage based on the specified view type.
     *
     * @param isLanguageView If true, sets the stage size to the dimensions of the language view;
     *                       otherwise, sets it to the default dimensions.
     */
    public static void changeStageSize(boolean isLanguageView) {
        if (isLanguageView) {
            setWidth(LANG_WIDTH);
            setHeight(LANG_HEIGHT);
        } else {
            setWidth(WIDTH);
            setHeight(HEIGHT);
        }
    }

    /**
     * Sets the width of the application stage to the specified value.
     * Ensures that the minimum, current, and maximum widths are all set to the same value.
     *
     * @param width The width to set for the stage.
     */
    public static void setWidth(int width) {
        _stage.setMinWidth(width);
        _stage.setWidth(width);
        _stage.setMaxWidth(width);
    }

    /**
     * Sets the height of the application stage to the specified value.
     * Ensures that the minimum, current, and maximum heights are all set to the same value.
     *
     * @param height The height to set for the stage.
     */
    public static void setHeight(int height) {
        _stage.setMinHeight(height);
        _stage.setHeight(height);
        _stage.setMaxHeight(height);
    }
    //#endregion
}
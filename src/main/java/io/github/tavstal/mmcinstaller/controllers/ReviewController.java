package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Controller class for the Review view of the installer application.
 * Handles user interactions and initializes the review screen.
 */
public class ReviewController implements Initializable {
    public StackPane rootPane;
    private boolean isSceneInitialized = false;

    private InstallerLogger _logger; // Logger instance for logging events.
    private InstallerTranslator _translator; // Translator instance for localization.

    public TextArea reviewTextArea; // Text area displaying the review content.
    public Label reviewTitle; // Label displaying the review title.
    public Label reviewDescription; // Label displaying the review description.
    public Text reviewAction; // Text displaying the review action message.
    public Button backButton; // Button to navigate back to the previous screen.
    public Button nextButton; // Button to proceed to the next step.
    public Button cancelButton; // Button to cancel the installation process.

    /**
     * Initializes the Review view by setting localized text for UI elements
     * and populating the review content.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if not known.
     * @param resources The resources used to localize the root object, or null if not available.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getCustomLogger().WithModule(this.getClass());
        _translator = InstallerApplication.getTranslator();

        updateReviewContent();
        reviewTitle.setText(_translator.Localize("Review.Title"));
        reviewDescription.setText(_translator.Localize("Review.Description"));
        reviewAction.setText(_translator.Localize("Review.Action"));

        backButton.setText(_translator.Localize("Common.Back"));
        nextButton.setText(_translator.Localize("Common.Install"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        rootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && !isSceneInitialized) {
                // Now that the scene is attached, check when it's placed on a window (Stage)
                newScene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
                    if (newWindow instanceof Stage && newWindow.isShowing() && !isSceneInitialized) {
                        updateReviewContent();
                        isSceneInitialized = true;
                    } else if (newWindow == null && isSceneInitialized) {
                        isSceneInitialized = false;
                    }
                });
            }
        });
    }

    /**
     * Updates the content of the review text area with localized and dynamic data.
     */
    public void updateReviewContent() {
        String reviewContent = _translator.Localize("Review.Content", new HashMap<>() {
            {
                put("installPath", InstallerApplication.getCurrentPath());
                put("startMenuPath", InstallerApplication.getStartMenuPath());
                put("desktopShortcut", InstallerApplication.shouldCreateDesktopShortcut() ? InstallerApplication.getTranslator().Localize("Common.YesText") : InstallerApplication.getTranslator().Localize("Common.NoText"));
                put("startMenuShortcut", InstallerApplication.shouldCreateStartMenuShortcut() ? InstallerApplication.getTranslator().Localize("Common.YesText") : InstallerApplication.getTranslator().Localize("Common.NoText"));
            }
        });
        reviewTextArea.setText(reviewContent);
    }

    /**
     * Handles the action when the "Next" button is clicked.
     * Switches the scene to the Install Progress view.
     */
    @FXML
    protected void onNextButtonClick() {
        InstallerApplication.setActiveScene(InstallerApplication.getInstallProgressScene());
        _logger.Debug("Switching to InstallProgressView.fxml");
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * Switches the scene to the Shortcut view.
     */
    @FXML
    protected void onBackButtonClick() {
        InstallerApplication.setActiveScene(InstallerApplication.getShortcutScene());
        _logger.Debug("Switching to ShortcutView.fxml");
    }

    /**
     * Handles the action when the "Cancel" button is clicked.
     * Exits the application.
     */
    @FXML
    protected void onCancelButtonClick() {
        System.exit(0);
    }
}
package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Welcome view of the installer application.
 * Handles user interactions and initializes the welcome screen.
 */
public class WelcomeController implements Initializable {
    private InstallerLogger _logger; // Logger instance for logging events.
    public Button nextButton; // Button to proceed to the next step.
    public Button cancelButton; // Button to cancel the installation process.
    public Label welcomeTitle; // Label displaying the welcome title.
    public Text welcomeDescription; // Text displaying the welcome description.
    public Text welcomeAction; // Text displaying the welcome action message.

    /**
     * Initializes the WelcomeController with localized text for UI elements.
     * This method is called when the FXML file is loaded.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the resources are not known.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        // Translator instance for localization.
        InstallerTranslator _translator = InstallerApplication.getTranslator();

        if (InstallerState.isUninstallModeActive()) {
            welcomeTitle.setText(_translator.Localize("WelcomeUninstall.Title"));
            welcomeDescription.setText(_translator.Localize("WelcomeUninstall.Description"));
            welcomeAction.setText(_translator.Localize("WelcomeUninstall.Action"));
        }
        else {
            welcomeTitle.setText(_translator.Localize("Welcome.Title"));
            welcomeDescription.setText(_translator.Localize("Welcome.Description"));
            welcomeAction.setText(_translator.Localize("Welcome.Action"));
        }

        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));
        _logger.Debug("WelcomeController initialized with localized text.");
    }


    /**
     * Handles the action when the "Next" button is clicked.
     * Loads the License view and switches the scene.
     */
    @FXML
    protected void onNextButtonClick() {
        if (InstallerState.isUninstallModeActive()) {
            _logger.Debug("Uninstall mode is active, switching to ReviewView.fxml.");
            InstallerApplication.setActiveScene(SceneManager.getReviewScene());
            return;
        }
        InstallerApplication.setActiveScene(SceneManager.getLicenseScene());
        _logger.Debug("Switched to LicenseView.fxml");
    }

    /**
     * Handles the action when the "Cancel" button is clicked.
     * Exits the application.
     */
    @FXML
    protected void onCancelButtonClick() {
        _logger.Debug("Cancel button clicked, exiting application.");
        System.exit(0);
    }
}
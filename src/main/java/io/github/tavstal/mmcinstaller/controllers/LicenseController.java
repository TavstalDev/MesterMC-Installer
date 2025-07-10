package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.Translator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the License view of the installer application.
 * Handles user interactions and initializes the license screen.
 */
public class LicenseController implements Initializable {
    private InstallerLogger _logger; // Logger instance for logging events.
    public CheckBox acceptLicenseCheckBox; // Checkbox for accepting the license agreement.
    public Button backButton; // Button to navigate back to the previous screen.
    public Button nextButton; // Button to proceed to the next step.
    public Button cancelButton; // Button to cancel the installation process.
    public Label licenseTitle; // Label displaying the license title.
    public Label licenseDescription; // Label displaying the license description.
    public Text licenseAction; // Text displaying the license action message.
    public TextArea licenseContent; // Text area displaying the license content.

    /**
     * Initializes the License view by setting localized text for UI elements
     * and configuring the initial state of the controls.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if not known.
     * @param resources The resources used to localize the root object, or null if not available.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getCustomLogger().WithModule(this.getClass());
        // Translator instance for localization.
        Translator _translator = InstallerApplication.getTranslator();

        acceptLicenseCheckBox.setSelected(InstallerApplication.isLicenseAccepted());
        nextButton.setDisable(!InstallerApplication.isLicenseAccepted());

        licenseTitle.setText(_translator.Localize("License.Title"));
        licenseDescription.setText(_translator.Localize("License.Description"));
        licenseAction.setText(_translator.Localize("License.Action"));
        licenseContent.setText(_translator.Localize("License.Content"));

        acceptLicenseCheckBox.setText(_translator.Localize("License.Accept"));
        backButton.setText(_translator.Localize("Common.Back"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));
    }

    /**
     * Handles the action when the license checkbox state changes.
     * Updates the application's license acceptance state and enables/disables the "Next" button.
     */
    @FXML
    protected void onLicenseCheckBoxChanged() {
        InstallerApplication.setLicenseAccepted(acceptLicenseCheckBox.isSelected());
        nextButton.setDisable(!acceptLicenseCheckBox.isSelected());
    }

    /**
     * Handles the action when the "Next" button is clicked.
     * Switches the scene to the Install Path view.
     */
    @FXML
    protected void onNextButtonClick() {
        InstallerApplication.setActiveScene(InstallerApplication.getInstallPathScene());
        _logger.Debug("Switched to PathSelectView.fxml");
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * Switches the scene to the Welcome view.
     */
    @FXML
    protected void onBackButtonClick() {
        InstallerApplication.setActiveScene(InstallerApplication.getWelcomeScene());
        _logger.Debug("Switched to WelcomeView.fxml");
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
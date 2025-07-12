package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the installation completion screen.
 * Implements the `Initializable` interface to initialize UI components and handle events.
 */
public class InstallCompleteController implements Initializable {
    private InstallerLogger _logger; // Logger instance for logging events.
    public Button finishButton; // Button to finish the installation process.
    public Label finishedTitle; // Label displaying the title of the completion screen.
    public Text finishedDescription; // Text displaying the description of the completion screen.
    public Text finishedAction; // Text displaying the action message on the completion screen.
    public CheckBox launchGameCheckBox; // CheckBox to allow the user to choose whether to launch the game.

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets localized text for UI components using the `Translator` instance.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        InstallerTranslator _translator = InstallerApplication.getTranslator();

        // Set localized text for UI elements.
        finishedTitle.setText(_translator.Localize("Complete.Title"));
        finishedDescription.setText(_translator.Localize("Complete.Description"));
        finishedAction.setText(_translator.Localize("Complete.Action"));
        launchGameCheckBox.setText(_translator.Localize("Complete.LaunchGameCheckBox"));
        finishButton.setText(_translator.Localize("Common.Finish"));
    }

    /**
     * Handles the action when the finish button is clicked.
     * If the "Launch Game" checkbox is selected, the game will be launched (to be implemented).
     * Exits the application after handling the action.
     */
    @FXML
    protected void onFinishButtonClick() {
        if (launchGameCheckBox.isSelected()) {
            _logger.Debug("Launch game checkbox is selected, launching the game...");
            String osName = System.getProperty("os.name").toLowerCase();
            try {
                Process process;
                if (osName.contains("mac")) {
                    process = new ProcessBuilder("open", InstallerApplication.applicationToLaunch)
                            .inheritIO()
                            .start();
                } else {
                    process = new ProcessBuilder()
                            .command(InstallerApplication.applicationToLaunch)
                            .start();
                }
                _logger.Debug("Game launched successfully.");
            }
            catch (IOException exception) {
                _logger.Error("Failed to launch the game: " + exception.getMessage());
            }
        }
        System.exit(0);
    }
}
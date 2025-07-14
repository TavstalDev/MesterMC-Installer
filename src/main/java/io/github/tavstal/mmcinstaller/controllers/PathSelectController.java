package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
import io.github.tavstal.mmcinstaller.core.logging.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import io.github.tavstal.mmcinstaller.utils.PathUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Controller class for the Install Path view of the installer application.
 * Handles user interactions and initializes the install path screen.
 */
public class PathSelectController implements Initializable {
    private InstallerLogger _logger; // Logger instance for logging events.
    private InstallerTranslator _translator; // Translator instance for localization.
    private String _defaultPath; // Default installation path based on the operating system.
    public Button backButton; // Button to navigate back to the previous screen.
    public Button nextButton; // Button to proceed to the next step.
    public Button cancelButton; // Button to cancel the installation process.
    public Label installPathTitle; // Label displaying the install path title.
    public Label installPathDescription; // Label displaying the install path description.
    public Text installPathAction; // Text displaying the install path action message.
    public TextField directoryTextArea; // Text field for entering the installation directory.
    public Button browseButton; // Button to open the directory chooser.
    public Text freeSpaceText; // Text displaying the free space information.

    /**
     * Initializes the Install Path view by setting localized text for UI elements
     * and configuring the initial state of the controls.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if not known.
     * @param resources The resources used to localize the root object, or null if not available.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        // Translator instance for localization.
        _translator = InstallerApplication.getTranslator();

        installPathTitle.setText(_translator.Localize("InstallPath.Title"));
        installPathDescription.setText(_translator.Localize("InstallPath.Description"));
        installPathAction.setText(_translator.Localize("InstallPath.Action"));
        freeSpaceText.setText(_translator.Localize("InstallPath.FreeSpace", new HashMap<>() {
            {
                put("freeSpace", InstallerState.getRequiredSpace());
            }
        }));

        browseButton.setText(_translator.Localize("Common.Browse"));
        backButton.setText(_translator.Localize("Common.Back"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        _defaultPath = PathUtils.getDefaultInstallationPath(ConfigLoader.get().install().defaultDirs().appData()).getAbsolutePath();

        if (InstallerState.getCurrentPath() != null) {
            directoryTextArea.setText(InstallerState.getCurrentPath());
        } else {
            directoryTextArea.setText(_defaultPath);
            InstallerState.setCurrentPath(_defaultPath);
        }

        // Add a listener to the directory text area to update the current path in InstallerState
        directoryTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            File directory = new File(newValue);
            if (directory.exists() && directory.isDirectory()) {
                InstallerState.setCurrentPath(newValue);
            } else {
                InstallerState.setCurrentPath(_defaultPath);
            }
        });
    }

    /**
     * Handles the action when the "Next" button is clicked.
     * Switches the scene to the Shortcut view.
     */
    @FXML
    protected void onNextButtonClick() {
        InstallerApplication.setActiveScene(SceneManager.getShortcutScene());
        _logger.Debug("Switched to ShortcutView.fxml.");
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * Switches the scene to the License view.
     */
    @FXML
    protected void onBackButtonClick() {
        InstallerApplication.setActiveScene(SceneManager.getLicenseScene());
        _logger.Debug("Switched to LicenseView.fxml.");
    }

    /**
     * Handles the action when the "Cancel" button is clicked.
     * Exits the application.
     */
    @FXML
    protected void onCancelButtonClick() {
        System.exit(0);
    }

    /**
     * Handles the action when the "Browse" button is clicked.
     * Opens a directory chooser dialog to select the installation directory.
     */
    @FXML
    public void onBrowseButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(_translator.Localize("InstallPath.BrowseTitle"));

        // Set initial directory if the text field has a valid path
        File initialDirectory = new File(directoryTextArea.getText());
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        // Get the current stage (window) to make the dialog modal
        Stage stage = (Stage) browseButton.getScene().getWindow();

        // Show the directory chooser dialog
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            directoryTextArea.setText(selectedDirectory.getAbsolutePath());
            InstallerState.setCurrentPath(selectedDirectory.getAbsolutePath());
        }
    }
}
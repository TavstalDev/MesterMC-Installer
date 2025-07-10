package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.Translator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Shortcut view of the installer application.
 * Handles user interactions and initializes the shortcut screen.
 */
public class ShortcutController implements Initializable {
    private InstallerLogger _logger; // Logger instance for logging events.
    private Translator _translator; // Translator instance for localization.
    private String _defaultPath; // Default path for the Start Menu shortcuts.

    public Button backButton; // Button to navigate back to the previous screen.
    public Button nextButton; // Button to proceed to the next step.
    public Button cancelButton; // Button to cancel the installation process.
    public Label shortcutTitle; // Label displaying the shortcut title.
    public Label shortcutDescription; // Label displaying the shortcut description.
    public Text shortcutAction; // Text displaying the shortcut action message.
    public CheckBox desktopCheckBox; // Checkbox for creating a desktop shortcut.
    public CheckBox startMenuCheckBox; // Checkbox for creating a Start Menu shortcut.
    public VBox startMenuDirectoryVBox; // VBox containing Start Menu directory options.
    public TextField directoryTextArea; // Text field for entering the Start Menu directory.
    public Text startMenuNote; // Text displaying a note about the Start Menu shortcut.
    public Button browseButton; // Button to open the directory chooser.

    /**
     * Initializes the Shortcut view by setting localized text for UI elements
     * and configuring the initial state of the controls.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if not known.
     * @param resources The resources used to localize the root object, or null if not available.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getCustomLogger().WithModule(this.getClass());
        _translator = InstallerApplication.getTranslator();

        shortcutTitle.setText(_translator.Localize("Shortcut.Title"));
        shortcutDescription.setText(_translator.Localize("Shortcut.Description"));
        shortcutAction.setText(_translator.Localize("Shortcut.Action"));
        startMenuNote.setText(_translator.Localize("Shortcut.StartMenuNote"));
        desktopCheckBox.setText(_translator.Localize("Shortcut.Desktop"));
        startMenuCheckBox.setText(_translator.Localize("Shortcut.StartMenu"));

        browseButton.setText(_translator.Localize("Common.Browse"));
        backButton.setText(_translator.Localize("Common.Back"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        _defaultPath = getDefaultStartMenuPath().getAbsolutePath();

        if (InstallerApplication.getStartMenuPath() != null) {
            directoryTextArea.setText(InstallerApplication.getStartMenuPath());
        } else {
            directoryTextArea.setText(_defaultPath);
            InstallerApplication.setStartMenuPath(_defaultPath);
        }
        directoryTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            File directory = new File(newValue);
            if (directory.exists() && directory.isDirectory()) {
                InstallerApplication.setStartMenuPath(newValue);
            } else {
                InstallerApplication.setStartMenuPath(_defaultPath);
            }
        });
    }

    /**
     * Handles the action when the "Next" button is clicked.
     * Switches the scene to the Review view.
     */
    @FXML
    protected void onNextButtonClick() {
        InstallerApplication.setActiveScene(InstallerApplication.getReviewScene());
        _logger.Debug("Switched to ReviewView.fxml");
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * Switches the scene to the Install Path view.
     */
    @FXML
    protected void onBackButtonClick() {
        InstallerApplication.setActiveScene(InstallerApplication.getInstallPathScene());
        _logger.Debug("Switched to PathSelectView.fxml");
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
     * Handles the action when the desktop shortcut checkbox state changes.
     * Updates the application's desktop shortcut creation state.
     *
     * @param actionEvent The event triggered by the checkbox state change.
     */
    @FXML
    public void onDesktopCheckBoxChange(ActionEvent actionEvent) {
        InstallerApplication.setCreateDesktopShortcut(desktopCheckBox.isSelected());
    }

    /**
     * Handles the action when the Start Menu shortcut checkbox state changes.
     * Updates the application's Start Menu shortcut creation state and enables/disables related controls.
     *
     * @param actionEvent The event triggered by the checkbox state change.
     */
    @FXML
    public void onStartMenuCheckBoxChange(ActionEvent actionEvent) {
        InstallerApplication.setCreateStartMenuShortcut(startMenuCheckBox.isSelected());
        startMenuDirectoryVBox.setDisable(!startMenuCheckBox.isSelected());
        if (startMenuCheckBox.isSelected()) {
            directoryTextArea.setDisable(false);
            browseButton.setDisable(false);
        } else {
            directoryTextArea.setDisable(true);
            browseButton.setDisable(true);
        }
    }

    /**
     * Handles the action when the "Browse" button is clicked.
     * Opens a directory chooser dialog to select the Start Menu directory.
     *
     * @param actionEvent The event triggered by the button click.
     */
    @FXML
    public void onBrowseButtonClick(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(_translator.Localize("Shortcut.BrowseTitle"));

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
            InstallerApplication.setStartMenuPath(selectedDirectory.getAbsolutePath());
        }
    }

    /**
     * Determines the default Start Menu path based on the operating system.
     * Creates the necessary directory if it does not exist.
     *
     * @return A File object representing the default Start Menu path.
     */
    private File getDefaultStartMenuPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String shortcutFolderName = "MesterMC"; // Folder name within Start Menu/Home for your app's shortcuts

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                // This is the common user-specific Start Menu Programs folder on Windows
                return new File(appData, "Microsoft" + File.separator + "Windows" +
                        File.separator + "Start Menu" + File.separator + "Programs" +
                        File.separator + shortcutFolderName);
            } else {
                return new File(userHome, shortcutFolderName); // Fallback
            }
        } else if (os.contains("linux")) {
            // ~/.local/share/applications first (for .desktop files),
            return new File(userHome, ".local" + File.separator + "share" + File.separator + "applications");
        } else if (os.contains("mac")) {
            // ~/Applications/[YourAppShortcutFolder]
            return new File(userHome, "Applications");
        } else {
            return new File(userHome, shortcutFolderName);
        }
    }
}
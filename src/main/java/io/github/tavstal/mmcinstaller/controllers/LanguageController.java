package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.InstallerState;
import io.github.tavstal.mmcinstaller.config.model.LanguageConfig;
import io.github.tavstal.mmcinstaller.config.ConfigLoader;
import io.github.tavstal.mmcinstaller.core.logging.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for managing the language selection screen of the installer.
 * Handles localization, user interactions, and updates the application state
 * based on the selected language.
 */
public class LanguageController implements Initializable {
    private InstallerTranslator _translator; // Translator for converting text to the selected language.
    public Label selectLabel; // Label prompting the user to select a language.
    public ComboBox<LanguageConfig> languageComboBox; // ComboBox for selecting the language, populated with available languages.
    public Button nextButton; // Button to proceed to the next step of the installation process, enabled after a language is selected.
    public Button cancelButton; // Button to cancel the installation process and exit the application.

    /**
     * Initializes the controller, setting up localization, logging, and populating
     * the language selection ComboBox.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InstallerLogger _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        _translator = InstallerApplication.getTranslator();

        // Populate the ComboBox with available languages.
        languageComboBox.getItems().addAll(
                ConfigLoader.get().language()
        );

        // Set custom cell factory for displaying language options.
        languageComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(LanguageConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : (item.localization() == null || item.localization().isEmpty() ? item.name() : String.format("%s (%s)", item.localization(), item.name())));
            }
        });

        // Set the button cell to display the selected language.
        languageComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LanguageConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : (item.localization() == null || item.localization().isEmpty() ? item.name() : String.format("%s (%s)", item.localization(), item.name())));
            }
        });

        // Localize UI elements.
        selectLabel.setText(_translator.Localize("Language.Title"));
        languageComboBox.setPromptText(_translator.Localize("Language.Action"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        _logger.Debug("LanguageController initialized with localized text.");
    }

    /**
     * Handles the event when the language selection changes.
     * Updates the application state and re-localizes UI elements.
     *
     */
    public void onLanguageChange() {
        if (nextButton.isDisabled())
            nextButton.setDisable(false);
        LanguageConfig selected = languageComboBox.getValue();
        InstallerState.setLanguage(selected.key());
        selectLabel.setText(_translator.Localize("Language.Title"));
        languageComboBox.setPromptText(_translator.Localize("Language.Action"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));
        InstallerApplication.setTitle(_translator.Localize("Window.Title"));
    }

    /**
     * Handles the event when the "Next" button is clicked.
     * Proceeds to the next step of the installer.
     *
     */
    public void onNextButtonClick() {
        InstallerApplication.changeStageSize(false);
        InstallerApplication.attemptCenterOnScreen();
        InstallerApplication.setActiveScene(SceneManager.getWelcomeScene());
    }

    /**
     * Handles the event when the "Cancel" button is clicked.
     * Exits the application.
     *
     */
    public void onCancelButtonClick() {
        System.exit(0);
    }
}
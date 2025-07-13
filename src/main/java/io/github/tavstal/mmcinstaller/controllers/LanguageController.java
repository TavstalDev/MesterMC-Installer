package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.InstallerState;
import io.github.tavstal.mmcinstaller.config.LanguageConfig;
import io.github.tavstal.mmcinstaller.core.ConfigLoader;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.InstallerTranslator;
import io.github.tavstal.mmcinstaller.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.net.URL;
import java.util.ResourceBundle;

public class LanguageController implements Initializable {
    public Label selectLabel;
    private InstallerLogger _logger; // Logger instance for logging events.
    private  InstallerTranslator _translator; // Translator instance for localization.
    public ComboBox<LanguageConfig> languageComboBox;
    public Button nextButton;
    public Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getLogger().WithModule(this.getClass());
        _translator = InstallerApplication.getTranslator();

        languageComboBox.getItems().addAll(
                ConfigLoader.get().language()
        );

        languageComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(LanguageConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : (item.localization() == null || item.localization().isEmpty() ? item.name() : String.format("%s (%s)", item.localization(), item.name())));
            }
        });

        languageComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LanguageConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.name());
            }
        });

        selectLabel.setText(_translator.Localize("Language.Title"));
        languageComboBox.setPromptText(_translator.Localize("Language.Action"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));

        _logger.Debug("LanguageController initialized with localized text.");
    }

    public void onLanguageChange(ActionEvent actionEvent) {
        if (nextButton.isDisabled())
            nextButton.setDisable(false);
        LanguageConfig selected = languageComboBox.getValue();
        InstallerState.setLanguage(selected.key());
        selectLabel.setText(_translator.Localize("Language.Title"));
        languageComboBox.setPromptText(_translator.Localize("Language.Action"));
        nextButton.setText(_translator.Localize("Common.Next"));
        cancelButton.setText(_translator.Localize("Common.Cancel"));
    }

    public void onNextButtonClick(ActionEvent actionEvent) {
        InstallerApplication.changeStageSize(false);
        InstallerApplication.setActiveScene(SceneManager.getWelcomeScene());
    }

    public void onCancelButtonClick(ActionEvent actionEvent) {
        System.exit(0);
    }
}

package io.github.tavstal.mmcinstaller.controllers;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import io.github.tavstal.mmcinstaller.core.Translator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class InstallCompleteController implements Initializable {
    private InstallerLogger _logger; // Logger instance for logging events.
    public Button finishButton;
    public Label finishedTitle;
    public Text finishedDescription;
    public Text finishedAction;
    public CheckBox launchGameCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _logger = InstallerApplication.getCustomLogger().WithModule(this.getClass());
        // Translator instance for localization.
        Translator _translator = InstallerApplication.getTranslator();

        finishButton.setText(_translator.Localize("Common.Finish"));
    }

    @FXML
    protected void onFinishButtonClick() {
        System.exit(0);
    }

    public void onLaunchGameCheckBoxChange(ActionEvent actionEvent) {

    }
}
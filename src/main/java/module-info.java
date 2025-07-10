module io.github.tavstal.mmcinstaller {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;
    requires org.yaml.snakeyaml;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires commons.logging;

    opens io.github.tavstal.mmcinstaller to javafx.fxml;
    exports io.github.tavstal.mmcinstaller;
    exports io.github.tavstal.mmcinstaller.controllers;
    opens io.github.tavstal.mmcinstaller.controllers to javafx.fxml;
}
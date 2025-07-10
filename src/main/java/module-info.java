module io.github.tavstal.mmcinstaller {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;
    requires org.yaml.snakeyaml;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires com.sun.jna.platform;

    opens io.github.tavstal.mmcinstaller to javafx.fxml;
    exports io.github.tavstal.mmcinstaller;
    exports io.github.tavstal.mmcinstaller.controllers;
    opens io.github.tavstal.mmcinstaller.controllers to javafx.fxml;
}
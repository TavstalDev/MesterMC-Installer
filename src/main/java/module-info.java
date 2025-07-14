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
    requires org.slf4j;
    requires kotlin.stdlib;
    requires annotations;

    opens io.github.tavstal.mmcinstaller to javafx.fxml;
    exports io.github.tavstal.mmcinstaller;
    exports io.github.tavstal.mmcinstaller.controllers;
    exports io.github.tavstal.mmcinstaller.core;
    exports io.github.tavstal.mmcinstaller.config;
    opens io.github.tavstal.mmcinstaller.controllers to javafx.fxml;
    exports io.github.tavstal.mmcinstaller.config.model;
    opens io.github.tavstal.mmcinstaller.config to javafx.fxml;
    opens io.github.tavstal.mmcinstaller.config.model to javafx.fxml;
    exports io.github.tavstal.mmcinstaller.core.platform;
    exports io.github.tavstal.mmcinstaller.core.logging;
}
package io.github.tavstal.mmcinstaller.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for displaying alert dialogs in a JavaFX application.
 * Provides methods to create and show customizable alert dialogs
 * with specified titles, headers, content, and button labels.
 */
public class AlertUtils {
    /**
     * Displays an alert dialog with customizable title, header, content, and button labels.
     *
     * @param title         The title of the alert dialog.
     * @param header        The header text of the alert dialog.
     * @param content       The content text of the alert dialog.
     * @param yesButtonText The label for the "Yes" button.
     * @param noButtonText  The label for the "No" button.
     * @param alertType     The type of the alert (e.g., INFORMATION, WARNING, ERROR).
     * @return `true` if the user clicks the "Yes" button, `false` otherwise.
     */
    public static boolean show(String title, String header, String content, String yesButtonText, String noButtonText, Alert.AlertType alertType) {
        // Show an alert to the user about the checksum error.
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create "Yes" and "No" buttons with the provided labels.
        ButtonType yesButton = new ButtonType(yesButtonText);
        ButtonType noButton = new ButtonType(noButtonText);

        // Set the button types for the alert dialog.
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Display the alert dialog and wait for the user's response.
        Optional<ButtonType> result = alert.showAndWait();

        // Determine the user's choice and return the result.
        AtomicBoolean choiceResult = new AtomicBoolean(false);
        result.ifPresent(choice -> choiceResult.set(choice == yesButton));
        return choiceResult.get();
    }
}

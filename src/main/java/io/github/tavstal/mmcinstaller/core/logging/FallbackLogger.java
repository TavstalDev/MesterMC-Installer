package io.github.tavstal.mmcinstaller.core.logging;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import org.slf4j.event.Level;

/**
 * Provides a fallback logging mechanism for the application.
 * If the main logger is unavailable, logs messages to the console.
 */
public abstract class FallbackLogger {
    // Logger instance for logging messages.
    private static InstallerLogger _logger;
    private static boolean _isLoggerSet = false;

    /**
     * Sets the logger instance for the specified class.
     * This method initializes the logger with the module name derived from the given class.
     *
     * @param clazz The class for which the logger is being set.
     */
    protected static void setLogger(Class<?> clazz) {
        if (_isLoggerSet)
            return;
        _logger = InstallerApplication.getLogger().WithModule(clazz);
        _isLoggerSet = true;
    }

    /**
     * Logs a message at the specified log level.
     * If the main logger is unavailable, the message is logged to the console.
     *
     * @param level   The log level (e.g., INFO, WARN, ERROR, DEBUG).
     * @param message The message to be logged.
     */
    protected static void log(Level level, String message) {
        if (InstallerApplication.getLogger() == null) {
            // Log to the console if no logger is available.
            System.out.printf("FallbackLog [%s]: %s%n", level.name(), message);
        } else {
            // Initialize the logger if not already done.
            if (_logger == null) {
                _logger = InstallerApplication.getLogger().WithModule(FallbackLogger.class);
            }

            // Log the message using the appropriate log level.
            switch (level) {
                case Level.INFO:
                    _logger.Info(message);
                    break;
                case Level.WARN:
                    _logger.Warn(message);
                    break;
                case Level.ERROR:
                    _logger.Error(message);
                    break;
                default:
                    _logger.Debug(message);
            }
        }
    }
}
package io.github.tavstal.mmcinstaller.utils;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.InstallerLogger;
import org.slf4j.event.Level;

/**
 * Provides a fallback logging mechanism for the application.
 * If the main logger is unavailable, logs messages to the console.
 */
public abstract class FallbackLogger {
    // Logger instance for logging messages.
    private static InstallerLogger _logger;

    /**
     * Logs a message at the specified log level.
     * If the main logger is unavailable, logs to the console.
     *
     * @param level   The log level (INFO, WARN, ERROR, DEBUG).
     * @param message The message to log.
     */
    protected static void Log(Level level, String message) {
        if (InstallerApplication.getLogger() == null) {
            // Log to the console if no logger is available.
            System.out.printf("FallbackLog [%s]: %s%n", level.name(), message);
        } else {
            // Initialize the logger if not already done.
            if (_logger == null) {
                _logger = InstallerApplication.getLogger().WithModule(YamlHelper.class);
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
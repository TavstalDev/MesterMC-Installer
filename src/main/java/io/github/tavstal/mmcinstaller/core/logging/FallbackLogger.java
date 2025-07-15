package io.github.tavstal.mmcinstaller.core.logging;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import org.slf4j.event.Level;

import java.util.HashMap;

/**
 * Provides a fallback logging mechanism for the application.
 * If the main logger is unavailable, logs messages to the console.
 */
public abstract class FallbackLogger {
    // Logger instance for logging messages.
    private final static HashMap<String, InstallerLogger> _loggers = new HashMap<>();

    /**
     * Logs a message at the specified log level.
     * If the main logger is unavailable, the message is logged to the console.
     *
     * @param level   The log level (e.g., INFO, WARN, ERROR, DEBUG).
     * @param message The message to be logged.
     */
    protected static void log(Level level, String message, Class<?> clazz) {
        if (InstallerApplication.getLogger() == null) {
            // Log to the console if no logger is available.
            System.out.printf("FallbackLog [%s]: %s%n", level.name(), message);
        } else {
            // Initialize the logger if not already done.
            var logger = _loggers.get(clazz.getName());
            if (logger == null) {
                logger = InstallerApplication.getLogger().WithModule(clazz);
                _loggers.put(clazz.getName(), logger);
            }

            // Log the message using the appropriate log level.
            switch (level) {
                case Level.INFO:
                    logger.Info(message);
                    break;
                case Level.WARN:
                    logger.Warn(message);
                    break;
                case Level.ERROR:
                    logger.Error(message);
                    break;
                default:
                    logger.Debug(message);
            }
        }
    }
}
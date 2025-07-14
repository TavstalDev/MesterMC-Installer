package io.github.tavstal.mmcinstaller.core.logging;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.config.InstallerState;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A logger utility class for the Installer application.
 * Provides methods for logging messages at different levels (INFO, WARNING, SEVERE, DEBUG).
 * Supports optional module-specific logging and debug mode.
 */
public class InstallerLogger {
    private final String _module; // The name of the module for module-specific logging.
    private final Logger _logger; // The underlying Java Logger instance.
    private ConsoleHandler _consoleHandler; // ConsoleHandler for logging to the console with color formatting.

    /**
     * Constructs an `InstallerLogger` instance with the specified module name and console handler.
     * <p>
     * If the provided console handler is null, a new `ConsoleHandler` is created with an
     * `AnsiColorFormatter` and added to the logger. Existing console handlers in the root logger
     * are removed to avoid duplicate output.
     * </p>
     *
     * @param module          The name of the module for module-specific logging.
     * @param consoleHandler  The `ConsoleHandler` for logging to the console. If null, a new handler is created.
     */
    public InstallerLogger(String module, ConsoleHandler consoleHandler) {
        _module = module;
        _logger = Logger.getLogger(InstallerApplication.class.getPackage().getName());
        _consoleHandler = consoleHandler;

        if (_consoleHandler == null) {
            Logger rootLogger = Logger.getLogger("");
            // Remove existing ConsoleHandlers to avoid duplicate output
            for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }

            _consoleHandler = new ConsoleHandler();
            _consoleHandler.setFormatter(new AnsiColorFormatter());
            _consoleHandler.setLevel(Level.ALL);

            _logger.addHandler(_consoleHandler);
            _logger.setLevel(Level.ALL);
            _logger.setUseParentHandlers(false); // To prevent the log from going to the root logger's default handler as well
        }
    }

    /**
     * Creates a new logger instance with the specified module and debug mode.
     *
     * @param module The name of the module.
     * @return A new InstallerLogger instance.
     */
    public InstallerLogger WithModule(String module) {
        return new InstallerLogger(module, _consoleHandler);
    }

    /**
     * Creates a new logger instance with the specified module and debug mode.
     * The module is specified as a `Class` object, and its name is used for logging.
     *
     * @param module The `Class` object representing the module.
     * @return A new `InstallerLogger` instance configured with the module name and debug mode.
     */
    public InstallerLogger WithModule(Class<?> module) {
        return new InstallerLogger(module.getSimpleName(), _consoleHandler);
    }

    /**
     * Logs a message at the specified level.
     *
     * @param level The logging level (e.g., INFO, WARNING, SEVERE).
     * @param text  The message to log.
     */
    private void Log(Level level, String text) {
        if (_module != null)
            _logger.log(level, String.format("[%s] %s", _module, text));
        else
            _logger.log(level, text);
    }

    /**
     * Converts an object to a string representation.
     * If the object is an Exception, returns its message.
     * If the object is a String, returns it directly.
     * Otherwise, calls the object's toString() method.
     *
     * @param text The object to convert.
     * @return The string representation of the object.
     */
    private String GetString(Object text) {
        if (text instanceof Exception ex) {
            return ex.getMessage();
        }
        if (text instanceof String str) {
            return str;
        }
        return text.toString();
    }

    /**
     * Logs a message at the INFO level.
     *
     * @param text The message or object to log.
     */
    public void Info(Object text) {
        Log(Level.INFO, GetString(text));
    }

    /**
     * Logs a message at the WARNING level.
     *
     * @param text The message or object to log.
     */
    public void Warn(Object text) {
        Log(Level.WARNING, GetString(text));
    }

    /**
     * Logs a message at the SEVERE level.
     *
     * @param text The message or object to log.
     */
    public void Error(Object text) {
        Log(Level.SEVERE, GetString(text));
    }

    /**
     * Logs a message at the INFO level if debug mode is enabled.
     *
     * @param text The message or object to log.
     */
    public void Debug(Object text) {
        if (InstallerState.isDebugMode())
            Log(Level.FINE, GetString(text));
    }
}

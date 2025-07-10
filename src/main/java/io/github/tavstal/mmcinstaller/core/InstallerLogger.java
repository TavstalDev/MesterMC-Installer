package io.github.tavstal.mmcinstaller.core;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.controllers.WelcomeController;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A logger utility class for the Installer application.
 * Provides methods for logging messages at different levels (INFO, WARNING, SEVERE, DEBUG).
 * Supports optional module-specific logging and debug mode.
 */
public class InstallerLogger {
    private final boolean _debug; // Indicates whether debug mode is enabled.
    private final String _module; // The name of the module for module-specific logging.
    private final Logger _logger; // The underlying Java Logger instance.

    /**
     * Constructor for creating a logger without a specific module.
     *
     * @param debug Whether debug mode is enabled.
     */
    public InstallerLogger(boolean debug) {
        _debug = debug;
        _module = null;
        _logger = Logger.getLogger(InstallerApplication.getProjectName());
    }

    /**
     * Constructor for creating a logger with a specific module.
     *
     * @param module The name of the module.
     * @param debug  Whether debug mode is enabled.
     */
    public InstallerLogger(String module, boolean debug) {
        _debug = debug;
        _module = module;
        _logger = Logger.getLogger(InstallerApplication.getProjectName());
    }

    /**
     * Creates a new logger instance with the specified module and debug mode.
     *
     * @param module The name of the module.
     * @return A new InstallerLogger instance.
     */
    public InstallerLogger WithModule(String module) {
        return new InstallerLogger(module, _debug);
    }

    /**
     * Creates a new logger instance with the specified module and debug mode.
     * The module is specified as a `Class` object, and its name is used for logging.
     *
     * @param module The `Class` object representing the module.
     * @return A new `InstallerLogger` instance configured with the module name and debug mode.
     */
    public InstallerLogger WithModule(Class<?> module) {
        return new InstallerLogger(module.getName(), _debug);
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
        if (_debug)
            Log(Level.INFO, GetString(text));
    }
}

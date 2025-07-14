package io.github.tavstal.mmcinstaller.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A custom log formatter that formats log messages with ANSI color codes
 * and includes a timestamp, log level, and message.
 */
public class AnsiColorFormatter extends Formatter {

    // ANSI escape codes for text formatting
    public static final String ANSI_RESET = "\u001B[0m";
    @SuppressWarnings("unused")
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    @SuppressWarnings("unused")
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    @SuppressWarnings("unused")
    public static final String ANSI_WHITE = "\u001B[37m";

    // Date format for log timestamps
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a log record into a string with ANSI color codes, timestamp, log level, and message.
     * If an exception is included in the log record, its stack trace is appended in red.
     *
     * @param record The log record to format.
     * @return A formatted log message as a string.
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();

        // Determine the color based on the log level
        String levelColor;
        String message = formatMessage(record);
        if (record.getLevel() == Level.SEVERE) {
            levelColor = ANSI_RED;
        } else if (record.getLevel() == Level.WARNING) {
            levelColor = ANSI_YELLOW;
        } else if (record.getLevel() == Level.INFO) {
            levelColor = ANSI_CYAN;
        } else if (record.getLevel() == Level.CONFIG) {
            levelColor = ANSI_GREEN;
        } else if (record.getLevel() == Level.FINE || record.getLevel() == Level.FINER || record.getLevel() == Level.FINEST) {
            levelColor = ANSI_PURPLE;
        } else {
            levelColor = ANSI_RESET;
        }

        // Apply the color
        builder.append(levelColor);

        // Append the timestamp
        builder.append(dateFormat.format(new Date(record.getMillis())));

        // Append the log level
        builder.append(String.format(" [%s] ", record.getLevel().getName()));

        // Append the log message
        builder.append(message);

        // Reset the color
        builder.append(ANSI_RESET);

        // Add a new line
        builder.append(System.lineSeparator());

        // Append the stack trace if an exception is present
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                builder.append(ANSI_RED).append(sw).append(ANSI_RESET);
            } catch (Exception ex) {
                // Ignore exceptions while formatting the stack trace
            }
        }

        return builder.toString();
    }
}
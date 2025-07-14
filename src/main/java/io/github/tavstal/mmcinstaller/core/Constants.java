package io.github.tavstal.mmcinstaller.core;

/**
 * A utility class that defines constants used throughout the application.
 */
public class Constants {
    /** The name of the operating system, retrieved from system properties and converted to lowercase. */
    public final static String OS_NAME = System.getProperty("os.name").toLowerCase();

    /** The width of the language selection window in pixels. */
    public final static int LANG_WIDTH = 400;

    /** The height of the language selection window in pixels. */
    public final static int LANG_HEIGHT = 200;

    /** The default width of the main application window in pixels. */
    public final static int WIDTH = 700;

    /** The default height of the main application window in pixels. */
    public final static int HEIGHT = 400;
}
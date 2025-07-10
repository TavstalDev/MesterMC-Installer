package io.github.tavstal.mmcinstaller;

/**
 * Represents the state of the installer during the installation process.
 * This class contains static fields and methods to manage and retrieve
 * the current state of the installer, such as license acceptance, installation
 * paths, shortcut creation preferences, and required disk space.
 */
public class InstallerState {

    // Indicates whether the license agreement has been accepted.
    private static boolean licenseAccepted = false;

    // Stores the current installation path.
    private static String currentPath = null;

    // Indicates whether a desktop shortcut should be created.
    private static boolean createDesktopShortcut = true;

    // Stores the path for the Start Menu shortcut.
    private static String startMenuPath = null;

    // Indicates whether a Start Menu shortcut should be created.
    private static boolean createStartMenuShortcut = true;

    // Stores the required disk space for the installation in bytes.
    private static long requiredSpace = 0;

    // Stores the application to be launched after installation.
    private static String applicationToLaunch = null;

    /**
     * Checks if the license agreement has been accepted.
     *
     * @return True if the license is accepted, false otherwise.
     */
    public static boolean isLicenseAccepted() {
        return licenseAccepted;
    }

    /**
     * Sets the license acceptance state.
     *
     * @param licenseAccepted True if the license is accepted, false otherwise.
     */
    public static void setLicenseAccepted(boolean licenseAccepted) {
        InstallerState.licenseAccepted = licenseAccepted;
    }

    /**
     * Retrieves the current installation path.
     *
     * @return The current installation path as a String.
     */
    public static String getCurrentPath() {
        return currentPath;
    }

    /**
     * Sets the current installation path.
     *
     * @param currentPath The installation path to set.
     */
    public static void setCurrentPath(String currentPath) {
        InstallerState.currentPath = currentPath;
    }

    /**
     * Checks if a desktop shortcut should be created.
     *
     * @return True if a desktop shortcut should be created, false otherwise.
     */
    public static boolean shouldCreateDesktopShortcut() {
        return createDesktopShortcut;
    }

    /**
     * Sets the desktop shortcut creation preference.
     *
     * @param createDesktopShortcut True to create a desktop shortcut, false otherwise.
     */
    public static void setCreateDesktopShortcut(boolean createDesktopShortcut) {
        InstallerState.createDesktopShortcut = createDesktopShortcut;
    }

    /**
     * Retrieves the path for the Start Menu shortcut.
     *
     * @return The Start Menu shortcut path as a String.
     */
    public static String getStartMenuPath() {
        return startMenuPath;
    }

    /**
     * Sets the path for the Start Menu shortcut.
     *
     * @param startMenuPath The Start Menu shortcut path to set.
     */
    public static void setStartMenuPath(String startMenuPath) {
        InstallerState.startMenuPath = startMenuPath;
    }

    /**
     * Checks if a Start Menu shortcut should be created.
     *
     * @return True if a Start Menu shortcut should be created, false otherwise.
     */
    public static boolean shouldCreateStartMenuShortcut() {
        return createStartMenuShortcut;
    }

    /**
     * Sets the Start Menu shortcut creation preference.
     *
     * @param createStartMenuShortcut True to create a Start Menu shortcut, false otherwise.
     */
    public static void setCreateStartMenuShortcut(boolean createStartMenuShortcut) {
        InstallerState.createStartMenuShortcut = createStartMenuShortcut;
    }

    /**
     * Retrieves the required disk space for the installation.
     *
     * @return The required disk space as a formatted String in MB.
     */
    public static String getRequiredSpace() {
        return String.format("%d MB", requiredSpace / (1024 * 1024));
    }

    /**
     * Sets the required disk space for the installation.
     *
     * @param requiredSpace The required disk space in bytes.
     */
    public static void setRequiredSpace(long requiredSpace) {
        InstallerState.requiredSpace = requiredSpace;
    }

    /**
     * Retrieves the application to be launched after installation.
     *
     * @return The application to launch as a String.
     */
    public static String getApplicationToLaunch() {
        return applicationToLaunch;
    }

    /**
     * Sets the application to be launched after installation.
     *
     * @param appToLaunch The application to launch as a String.
     */
    public static void setApplicationToLaunch(String appToLaunch) {
        InstallerState.applicationToLaunch = appToLaunch;
    }
}
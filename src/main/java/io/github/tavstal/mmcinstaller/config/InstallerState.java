package io.github.tavstal.mmcinstaller.config;

/**
 * Represents the state of the installer during the installation process.
 * This class contains static fields and methods to manage and retrieve
 * the current state of the installer, such as license acceptance, installation
 * paths, shortcut creation preferences, and required disk space.
 */
public class InstallerState {
    //#region Fields
    // Indicates whether the installer is in debug mode.
    private static boolean debugMode = false;
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
    // Stores the selected language for the installer.
    private static String language = "hun";
    // Indicates whether the uninstallation mode is active.
    private static boolean _isUninstallModeActive = false;
    // Stores the path for the shortcut.
    private static String _shortcutPath = null;
    // Stores the path for the Start Menu shortcut.
    private static String _startMenuShortcutPath = null;
    //#endregion

    //#region Getters and Setters
    //#region Debug Mode
    /**
     * Retrieves the current state of the debug mode.
     *
     * @return True if debug mode is enabled, false otherwise.
     */
    public static boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Sets the state of the debug mode.
     *
     * @param debugMode True to enable debug mode, false to disable it.
     */
    public static void setDebugMode(boolean debugMode) {
        InstallerState.debugMode = debugMode;
    }
    //#endregion
    //#region Language
    /**
     * Retrieves the currently selected language for the installer.
     *
     * @return The selected language as a String.
     */
    public static String getLanguage() {
        return language;
    }
    /**
     * Sets the language for the installer.
     *
     * @param language The language to set, represented as a String.
     */
    public static void setLanguage(String language) {
        InstallerState.language = language;
    }
    //#endregion
    //#region Required Disk Space
    /**
     * Retrieves the required disk space for the installation in bytes.
     *
     * @return The required disk space as a long value in bytes.
     */
    public static long getRequiredSpaceInBytes() {
        return requiredSpace;
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
    //#endregion

    //#region License Acceptance
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
    //#endregion
    //#region Uninstallation Mode
    /**
     * Retrieves the current state of the uninstallation mode.
     *
     * @return True if the uninstallation mode is active, false otherwise.
     */
    public static boolean isUninstallModeActive() {
        return _isUninstallModeActive;
    }

    /**
     * Sets the state of the uninstallation mode.
     *
     * @param isActive True to activate the uninstallation mode, false to deactivate it.
     */
    public static void setUninstallMode(boolean isActive) {
        _isUninstallModeActive = isActive;
    }
    //#endregion

    //#region Installation Directory Path
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
    //#endregion
    //#region Start Menu Path
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
    //#endregion
    //#region Application to Launch Path After Installation
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
    //#endregion

    //#region Shortcuts
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
    //#endregion
    //#region Desktop Shortcut Path
    /**
     * Retrieves the path for the shortcut.
     *
     * @return The shortcut path as a String.
     */
    public static String getShortcutPath() {
        return _shortcutPath;
    }

    /**
     * Sets the path for the shortcut.
     *
     * @param shortcutPath The shortcut path to set.
     */
    public static void setShortcutPath(String shortcutPath) {
        _shortcutPath = shortcutPath;
    }
    //#endregion
    //#region Start Menu Shortcut Path
    /**
     * Retrieves the path for the Start Menu shortcut.
     *
     * @return The Start Menu shortcut path as a String.
     */
    public static String getStartMenuShortcutPath() {
        return _startMenuShortcutPath;
    }

    /**
     * Sets the path for the Start Menu shortcut.
     *
     * @param startMenuShortcutPath The Start Menu shortcut path to set.
     */
    public static void setStartMenuShortcutPath(String startMenuShortcutPath) {
        _startMenuShortcutPath = startMenuShortcutPath;
    }
    //#endregion
    //#endregion
}
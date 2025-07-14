package io.github.tavstal.mmcinstaller.utils;

import io.github.tavstal.mmcinstaller.core.Constants;
import io.github.tavstal.mmcinstaller.core.logging.FallbackLogger;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling script-related operations such as creating and making scripts executable.
 * Extends the FallbackLogger to provide logging functionality.
 */
public class ScriptUtils extends FallbackLogger {
    /**
     * Creates a script file with the specified content in the given directory.
     * If the operating system is Linux or macOS, the script is made executable.
     *
     * @param dir      The directory where the script file will be created.
     * @param fileName The name of the script file to create.
     * @param content  The content to write into the script file.
     * @return The created script file.
     */
    public static File createFile(String dir, String fileName, String content) {
        // Define the script file in the installation directory
        File scriptFile = new File(dir, fileName);
        try {
            // Write the content to the script file
            Files.writeString(scriptFile.toPath(), content);
            Log(Level.DEBUG, "Created script: " + scriptFile.getAbsolutePath());

            // If the OS is Linux or macOS, make the script executable
            if (Constants.OS_NAME.contains("linux") || Constants.OS_NAME.contains("mac")) {
                makeExecutable(scriptFile);
            }
        } catch (IOException e) {
            // Log an error if the script file creation fails
            Log(Level.ERROR, "Failed to write scripts: " + e.getMessage());
        }
        return scriptFile;
    }

    /**
     * Makes the specified script file executable.
     * Uses the `chmod +x` command for Linux and macOS systems.
     *
     * @param scriptFile The script file to make executable.
     */
    public static void makeExecutable(File scriptFile) {
        try {
            // Log the start of the process
            Log(Level.DEBUG, "Attempting to make script executable: " + scriptFile.getAbsolutePath());

            // Create a ProcessBuilder to execute the chmod command
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", scriptFile.getAbsolutePath());
            Process p = pb.start();

            // Wait for the process to complete, with a timeout of 10 seconds
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);

            if (finished) {
                int exitCode = p.exitValue(); // Get the exit code of the process
                if (exitCode == 0) {
                    // Log success if chmod executed successfully
                    Log(Level.DEBUG, "Script made executable: " + scriptFile.getAbsolutePath());
                } else {
                    // Read and log the error stream if chmod failed
                    String error = new String(p.getErrorStream().readAllBytes());
                    Log(Level.ERROR, "chmod failed with exit code " + exitCode + ": " + error);
                }
            } else {
                // Log a timeout error if the process did not finish within the timeout
                Log(Level.ERROR, "chmod process timed out.");
                p.destroyForcibly(); // Forcefully terminate the process
            }
        } catch (IOException | InterruptedException e) {
            // Log any exceptions that occur during the process
            Log(Level.ERROR, "Exception while making script executable: " + e.getMessage());
        }
    }
}
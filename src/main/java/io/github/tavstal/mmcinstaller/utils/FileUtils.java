package io.github.tavstal.mmcinstaller.utils;

import io.github.tavstal.mmcinstaller.InstallerApplication;
import io.github.tavstal.mmcinstaller.core.logging.FallbackLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

/**
 * Utility class for file operations such as copying directories,
 * deleting directories, and computing file checksums.
 */
public class FileUtils extends FallbackLogger {
    /**
     * Initializes the logger for the `FileUtils` class.
     * This method sets up the logging mechanism by associating the logger with the `FileUtils` class.
     */
    public static void init() {
        setLogger(FileUtils.class);
    }

    /**
     * Copies the contents of a source directory to a target directory.
     * If the target directory does not exist, it will be created.
     *
     * @param source The path of the source directory.
     * @param target The path of the target directory.
     * @throws IOException If an error occurs during the copy process.
     */
    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            /**
             * Creates the corresponding directory in the target location before visiting its contents.
             *
             * @param dir The current directory being visited.
             * @param attrs The attributes of the directory.
             * @return `FileVisitResult.CONTINUE` to continue the directory traversal.
             * @throws IOException If an error occurs while creating the directory.
             */
            @NotNull
            @Override
            public FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            /**
             * Copies a file from the source directory to the target directory.
             *
             * @param file The file being visited.
             * @param attrs The attributes of the file.
             * @return `FileVisitResult.CONTINUE` to continue the file traversal.
             * @throws IOException If an error occurs while copying the file.
             */
            @NotNull
            @Override
            public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Deletes a directory and all its contents.
     *
     * @param path The path of the directory to delete.
     * @throws IOException If an error occurs during the deletion process.
     */
    public static void deleteDirectory(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            /**
             * Deletes a file during the traversal.
             *
             * @param file The file to be deleted.
             * @param attrs The attributes of the file.
             * @return `FileVisitResult.CONTINUE` to continue the traversal.
             * @throws IOException If an error occurs while deleting the file.
             */
            @NotNull
            @Override
            public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            /**
             * Deletes a directory after all its contents have been visited and deleted.
             *
             * @param dir The directory to be deleted.
             * @param exc An exception thrown during the visit, or `null` if none.
             * @return `FileVisitResult.CONTINUE` to continue the traversal.
             * @throws IOException If an error occurs while deleting the directory.
             */
            @NotNull
            @Override
            public FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Computes the SHA-256 checksum of a file.
     *
     * @param filepath The path of the file to compute the checksum for.
     * @return The SHA-256 checksum as a hexadecimal string.
     * @throws Exception If an error occurs during the checksum computation.
     */
    public static String getFileChecksum(String filepath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(filepath)) {
            byte[] byteBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(byteBuffer)) != -1) {
                digest.update(byteBuffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();

        // Convert bytes to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Copies a resource from the application's classpath to a specified target directory.
     * If the resource does not exist or the copy operation fails, logs an error.
     *
     * @param targetDir      The directory where the resource will be copied.
     * @param resourcePath   The path to the resource within the application's classpath.
     * @param targetFileName The name of the file to create in the target directory.
     * @return The `File` object representing the copied resource, or `null` if the operation fails.
     */
    public static File copyResource(String targetDir, String resourcePath, String targetFileName) {
        // Define the target file in the installation directory
        File targetFile = new File(targetDir, targetFileName);
        try (InputStream resourceStream = InstallerApplication.class.getResourceAsStream(resourcePath)) {
            // Check if the resource exists
            if (resourceStream == null) {
                log(Level.ERROR, "Resource not found: " + resourcePath);
                return null;
            }
            // Copy the resource to the target file
            Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Log an error if the copy operation fails
            log(Level.ERROR, String.format("Failed to copy %s: %s", targetFileName, e.getMessage()));
        }
        return targetFile;
    }
}

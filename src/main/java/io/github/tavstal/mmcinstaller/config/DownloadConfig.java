package io.github.tavstal.mmcinstaller.config;

/**
 * Represents the configuration for downloading a resource.
 * <br/>
 * This record is used to store the download link, file name, and hash of the resource.
 * It provides an immutable data structure with built-in methods for accessing these properties.
 *
 * @param link The URL from which the resource will be downloaded.
 * @param fileName The name of the file to be saved after downloading.
 * @param hash The hash value used to verify the integrity of the downloaded file.
 */
public record DownloadConfig(String link, String fileName, String hash) {
}

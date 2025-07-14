package io.github.tavstal.mmcinstaller.config.model;

/**
 * Represents the configuration for a project.
 * <br/>
 * This record is used to store metadata about a project, including its name, version, and author.
 * It provides an immutable data structure with built-in methods for accessing these properties.
 *
 * @param name The name of the project.
 * @param version The version of the project.
 * @param author The author of the project.
 */
public record ProjectConfig(String name, String version, String author) {
}

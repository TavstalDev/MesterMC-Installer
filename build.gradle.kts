import org.gradle.internal.os.OperatingSystem
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
}

group = "io.github.tavstal"
version = "1.0"


repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        sourceCompatibility = JavaVersion.VERSION_21
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("io.github.tavstal.mmcinstaller")
    mainClass.set("io.github.tavstal.mmcinstaller.InstallerApplication")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}


dependencies {
    implementation("org.controlsfx:controlsfx:11.2.2")
    implementation("net.synedra:validatorfx:0.6.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")

    // HTTP Client
    implementation("org.apache.httpcomponents.client5:httpclient5:5.5")

    // YAML
    implementation("org.yaml:snakeyaml:2.4")

    // JNA Core Library
    implementation("net.java.dev.jna:jna:5.17.0")
    // JNA Platform Library (contains OS-specific mappings like KnownFolders)
    implementation("net.java.dev.jna:jna-platform:5.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val vendorName = "Solymosi 'Tavstal' Zoltán"
val packageOutputDir = layout.buildDirectory.dir("jpackage").get().asFile
val linuxAppIcon = layout.projectDirectory.file("src/main/resources/io/github/tavstal/mmcinstaller/assets/icon.png").asFile
val windowsAppIcon = layout.projectDirectory.file("src/main/resources/io/github/tavstal/mmcinstaller/assets/favicon.ico").asFile
val macAppIcon = layout.projectDirectory.file("src/main/resources/io/github/tavstal/mmcinstaller/assets/icon.icns").asFile

jlink {
    imageZip.set(layout.buildDirectory.file("distributions/${project.name}-${project.version}-${javafx.platform.classifier}.zip"))
    options.addAll(
        listOf(
            "--add-modules", "java.base,java.net.http,jdk.crypto.ec",
            "--strip-debug",
            "--compress", "2",
            "--no-header-files",
            "--no-man-pages",
            "--verbose"
        )
    )
    launcher {
        name = rootProject.name
    }
    jpackage {
        appVersion = project.version.toString()
        vendor = vendorName
        // Output directory
        outputDir = packageOutputDir.absolutePath
        imageName = rootProject.name
        icon = when {
            OperatingSystem.current().isWindows -> windowsAppIcon.absolutePath
            OperatingSystem.current().isMacOsX -> macAppIcon.absolutePath
            else -> linuxAppIcon.absolutePath
        }
    }
}

val createSymlinkLauncher by tasks.registering(DefaultTask::class) {
    group = "build"
    description = "Creates a symbolic link for the application launcher."
    dependsOn(tasks.installDist)
    doLast {
        // The root directory of the installed application (from installDist)
        val installDir = layout.buildDirectory.dir("jpackage/${application.applicationName}").get().asFile.toPath()
        val targetLauncherFullPath = installDir.resolve("bin/${application.applicationName}")
        val symlinkPath = installDir.resolve(application.applicationName)
        // --- Diagnostic messages ---
        logger.lifecycle("--- Starting createSymlinkLauncher task ---")
        logger.lifecycle("# Installation Directory: $installDir")
        logger.lifecycle("# Target Launcher: $targetLauncherFullPath")
        logger.lifecycle("# Symlink Path: $symlinkPath")

        // --- Validate target exists before linking ---
        if (!Files.exists(targetLauncherFullPath)) {
            logger.error("Error: Target launcher script not found at $targetLauncherFullPath. Cannot create symlink.")
            throw GradleException("Target launcher not found for symlink creation.")
        }

        // --- Create the relative symbolic link ---
        // Delete existing symlink if it exists to prevent errors on re-build
        if (Files.exists(symlinkPath)) {
            Files.delete(symlinkPath)
            logger.lifecycle("Removed existing symlink: $symlinkPath")
        }

        // Create a relative symlink from the installDir to the bin/launcher
        Files.createSymbolicLink(symlinkPath, installDir.relativize(targetLauncherFullPath))
        logger.lifecycle("Created relative symlink: $symlinkPath -> ${installDir.relativize(targetLauncherFullPath)}")

        // Make the symlink executable (important for some systems/file managers)
        symlinkPath.toFile().setExecutable(true, false)
        logger.lifecycle("--- Finished createSymlinkLauncher task ---")
    }
}

val buildPackage by tasks.registering(DefaultTask::class) {
    group = "application"
    dependsOn(tasks.jpackageImage)
    dependsOn(createSymlinkLauncher)
}

tasks.named(createSymlinkLauncher.name) {
    mustRunAfter(tasks.jpackageImage)
}
import org.gradle.internal.os.OperatingSystem

plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
    kotlin("jvm")
}

//#region Load properties
val projectVersion: String by project
val projectGroup: String by project
val javaVersion: String by project
val authors: String by project
val linuxAppIconPath: String by project
val windowsAppIconPath: String by project
val macAppIconPath: String by project
val packageOutputDir = layout.buildDirectory.dir("jpackage").get().asFile
val linuxAppIcon = layout.projectDirectory.file(linuxAppIconPath).asFile
val windowsAppIcon = layout.projectDirectory.file(windowsAppIconPath).asFile
val macAppIcon = layout.projectDirectory.file(macAppIconPath).asFile
val projectName = rootProject.name
//#endregion
group = projectGroup
version = projectVersion


repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("${group}.mmcinstaller")
    mainClass.set("${group}.mmcinstaller.InstallerApplication")
}

javafx {
    version = javaVersion
    modules = listOf("javafx.controls", "javafx.fxml")
}


dependencies {
    implementation("org.controlsfx:controlsfx:11.2.2")
    implementation("net.synedra:validatorfx:0.6.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.9")

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
    implementation(kotlin("stdlib-jdk8"))
}

tasks.processResources {
    filteringCharset = "UTF-8"

    val expandProps = mapOf(
        "version" to projectVersion,
        "group" to projectGroup,
        "authors" to authors,
    )

    filesMatching(listOf(
        "config.yaml"
    )) {
        // The 'expand' method directly takes a Map in Kotlin DSL
        expand(expandProps)
    }

}

//#region JLink Configuration
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
        vendor = authors
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
//#endregion

//#region Tasks
tasks.withType<Test> {
    useJUnitPlatform()
}

abstract class WriteFile : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val contents: Property<String>

    @TaskAction
    fun write() {
        val file = outputFile.get().asFile
        if (file.exists()) {
            file.delete()
        }
        file.parentFile.mkdirs()
        file.writeText(contents.get())
        logger.lifecycle("Written file: ${outputFile.asFile.get().absolutePath}")
    }
}

val writeAppRunScript by tasks.registering(WriteFile::class) {
    val appDirRoot = packageOutputDir.resolve("${projectName}.AppDir")
    outputFile.set(appDirRoot.resolve("AppRun"))
    contents.set("""
        #!/bin/bash
        HERE="$(dirname "$(readlink -f "$0")")"
        exec "${'$'}HERE/usr/bin/${projectName}" "$@"
    """.trimIndent())
}

val writeDesktopFile by tasks.registering(WriteFile::class) {
    val appDirRoot = packageOutputDir.resolve("${projectName}.AppDir")
    outputFile.set(appDirRoot.resolve("${projectName}.desktop"))
    contents.set("""
        #!/usr/bin/env xdg-open
        [Desktop Entry]
        Type=Application
        Name=${projectName}
        Exec=${projectName}
        Icon=icon
        Categories=Utility;
        X-AppImage-Integrate=false
    """.trimIndent())
}

val createLinuxAppDir by tasks.registering(Copy::class) {
    group = "application"
    description = "Creates the AppDir structure for Linux."
    dependsOn(tasks.jpackageImage) // Ensure jlinked image is built
    dependsOn(writeAppRunScript)   // Depend on the AppRun script being written
    dependsOn(writeDesktopFile)    // Depend on the Desktop file being written
    onlyIf { OperatingSystem.current().isLinux }

    val appDirRoot = packageOutputDir.resolve("${projectName}.AppDir")
    val jpackageImageDir = packageOutputDir.resolve(projectName)

    // Ensure the root AppDir is created before other files are copied into it
    doFirst {
        if (!appDirRoot.exists()) {
            appDirRoot.mkdirs()
            logger.lifecycle("Created AppDir root: ${appDirRoot.absolutePath}")
        }
    }

    // Configure the Copy operation
    destinationDir = appDirRoot

    // 1. Copy the jpackage bin directory content into usr/bin
    from(jpackageImageDir.resolve("bin")) {
        into("usr/bin/")
    }

    // 2. Copy the jpackage lib directory content into usr/lib
    from(jpackageImageDir.resolve("lib")) {
        into("usr/lib")
    }

    // 3. Copy the icon.png to the root of .AppDir
    from(linuxAppIcon) {
        rename { "icon.png" }
    }

    // This doLast block runs *after* all copying is done
    doLast {
        val appRunFile = appDirRoot.resolve("AppRun")
        appRunFile.setExecutable(true, false) // Make AppRun executable
        logger.lifecycle("Made AppRun executable: ${appRunFile.absolutePath}")
    }
}

val buildPackage by tasks.registering(DefaultTask::class) {
    group = "application"
    if (OperatingSystem.current().isLinux) {
        dependsOn(createLinuxAppDir)
    } else {
        dependsOn(tasks.jpackageImage)
    }
}
//#endregion
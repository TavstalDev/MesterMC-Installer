import org.gradle.internal.os.OperatingSystem

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

val junitVersion = "5.13.3"

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

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Define variables for jpackage task (these are still useful)
val vendorName = "Solymosi 'Tavstal' Zoltán"
val description = "A modified Minecraft client"

// Path to the output directory for packaged app
val packageOutputDir = layout.buildDirectory.dir("jpackage").get().asFile

// Path to your application icon (e.g., in src/main/resources)
val linuxAppIcon = layout.projectDirectory.file("src/main/resources/io/github/tavstal/mmcinstaller/assets/icon.png").asFile
val windowsAppIcon = layout.projectDirectory.file("src/main/resources/io/github/tavstal/mmcinstaller/assets/favicon.ico").asFile

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
        /*installerType = when {
            OperatingSystem.current().isWindows -> "exe"
            OperatingSystem.current().isMacOsX -> "dmg"
            else -> "app-image" // Use app-image for Linux by default, or switch to deb/rpm
        }*/

        appVersion = project.version.toString()
        vendor = vendorName
        //description = description

        // Output directory
        this.outputDir = packageOutputDir.absolutePath
        imageName = rootProject.name

        // Icons
        if (OperatingSystem.current().isWindows) {
            icon = windowsAppIcon.absolutePath
        } else if (OperatingSystem.current().isLinux) {
            icon = linuxAppIcon.absolutePath
        }
    }
}
# Building and Packaging MesterMC-Installer for Linux

This guide explains how to build and package the MesterMC-Installer project as an AppImage on Linux.

## Prerequisites
Ensure you have the following installed:
- **Linux Distribution**: Any modern distribution (e.g., Ubuntu, Fedora, Arch Linux)
- **Java Development Kit (JDK)**: Version 21. Gradle and OpenJDK are recommended, RedHat is not compatible.
- **AppImage Tool**: For creating AppImages
- **AppImageLauncher**: Optional, for easier AppImage testing


## Preparing the Project
Please visit the [Getting Started Guide](https://github.com/TavstalDev/MesterMC-Installer/blob/master/docs/building/getting-started.md) to set up the project in IntelliJ IDEA and synchronize dependencies.

## Building Steps
The building process is mostly straightforward, as jpackage handles the creation of the application image, bundling the Java Runtime Environment (JRE) with your application.

### Run the buildPackage Task:
Once the dependencies are synchronized, you can build and package the application using the buildPackage Gradle task.

#### A. From IntelliJ IDEA:
Open the Gradle tool window (usually on the right side of the IDE).
Navigate to Tasks -> application -> buildPackage.
Double-click buildPackage to execute the task.
#### B. From Terminal (within the project root):
```bash
./gradlew buildPackage
```

This will automatically create the following directories in the build/jpackage folder:
- MesterMC-Installer
- MesterMC-Installer.AppDir

AppDir depends on MesterMC-Installer, so it will be created first. 
The AppDir contains the application files and resources needed to run the installer application.

## Download and Prepare AppImage Tool
Download the AppImage tool and make it executable:
```bash
wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage -O appimagetool
chmod +x appimagetool
./appimagetool --appimage-extract
cd squashfs-root
```

## Create the AppImage
Use the AppImage tool to package your application:
```bash
./AppRun </Relative/Path/To/MesterMC-Installer.AppDir>
```

This will create an AppImage file in the current directory.

## Test the AppImage
To test the generated AppImage, you can run or double-click it in your file manager. 
Ensure that the application runs as expected.:
```bash
./MesterMC-Installer-x86_64.AppImage
```

Depending on your Linux distribution, you might need to install ``appimagelauncher`` or ensure FUSE is available for AppImage execution. 
If the AppImage does not run, install ``appimagelauncher`` using your package manager or refer to your distribution's documentation.
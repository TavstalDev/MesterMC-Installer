# Building and Packaging MesterMC-Installer for Linux

This guide explains how to build and package the MesterMC-Installer project as an AppImage on Linux.

## Build the Project
Run the buildPackage Gradle task (under the application group):
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
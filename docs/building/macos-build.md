# Building and Packaging MesterMC-Installer for macOS

This document outlines the steps required to build and package the MesterMC-Installer application specifically for macOS. The process leverages Gradle and jpackage to create a self-contained .app bundle.
Prerequisites

## Prerequisites
Ensure you have the following installed:
- **macOS Operating System**: At least Big Sur (version 11.0) or newer, as this is the tested environment.
- **Java Development Kit (JDK)**: Version 21. Gradle and OpenJDK are recommended, RedHat is not compatible.
- **Git**: For cloning the project repository.
- **IntelliJ IDEA**: The recommended Integrated Development Environment (IDE) for this project.

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

### Locate the .app File:
After the buildPackage task completes successfully, the .app file will be created in the build/jpackage directory within your project.

The path will typically look like:
```
MesterMC-Installer/build/jpackage/MesterMC-Installer.app
```

You can now double-click the MesterMC-Installer.app file to launch the application.
# Building and Packaging MesterMC-Installer for Windows

This document provides instructions for building and packaging the MesterMC-Installer application for Windows operating systems. 
The process utilizes Gradle and jpackage to create a self-contained application image.

## Prerequisites
- **Windows Operating System**: Any modern version of Windows.
- **Java Development Kit (JDK)**: Version 21. Gradle and OpenJDK are recommended, RedHat is not compatible.
- **Git**: For cloning the project repository.
- **IntelliJ IDEA**: The recommended Integrated Development Environment (IDE) for this project.
- **Pre-built .EXE**: It is optional since I already provided a pre-built .EXE in the resources directory of the repository. 
  - If you want to build your own .EXE, follow the instructions in the [Windows Executable Build Guide](https://github.com/TavstalDev/MesterMC-Installer/blob/master/docs/building/windows-exe-build.md)
  - It is recommended to build your own .EXE if the downloaded .JAR file's name or its path changes. For context at the moment the .JAR file is named `MesterMC.jar` and is located in the installation directory.

### Important Note on Windows Packaging

jpackage creates an application directory (an "app-image") containing the .exe launcher, libraries, and JRE. 
It does not produce a single, monolithic .exe file. 
For a true single .exe, external tools like GraalVM Native Image or Launch4j are required. For distribution, simply zipping the output directory is recommended.

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

This will automatically create the following directory in the build/jpackage folder:
- MesterMC-Installer

The directory contains the application .EXE and resources needed to run the installer application.
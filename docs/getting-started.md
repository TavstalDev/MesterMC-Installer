# Getting Started

## Prerequisites
- **Java Development Kit (JDK)**: Version 21. Gradle and OpenJDK are recommended, RedHat is not compatible.
- **Git**: For cloning the project repository.
- **IntelliJ IDEA**: The recommended Integrated Development Environment (IDE) for this project.
- **Gradle (optional)**: IntelliJ usually uses the Gradle wrapper, but installing Gradle locally can be helpful.

### Clone the Repository:
You can clone the repository using either the command line or directly within IntelliJ IDEA.

#### A) Using Terminal
Open your terminal or command prompt and run:
```bash
git clone https://github.com/TavstalDev/MesterMC-Installer.git
cd MesterMC-Installer
```

#### B) Using IntelliJ IDEA
- Launch IntelliJ IDEA.
- From the Welcome Screen, select Get from VCS (Version Control System).
- In the dialog that appears, paste the repository URL:
- https://github.com/TavstalDev/MesterMC-Installer.git
- Choose the directory where you want to clone the project.
- Click Clone.
- Once cloning completes, you may continue with the next steps.

### Open in IntelliJ IDEA:
Launch IntelliJ IDEA and open the cloned project directory. IntelliJ IDEA should automatically detect the Gradle project.

### Synchronize Dependencies:
Allow IntelliJ IDEA to synchronize the Gradle project and download all necessary dependencies. 
This might take a few moments depending on your internet connection.

### Run the Application
To run the MesterMC-Installer application, you can use the Gradle task provided in the project.
#### A) From IntelliJ IDEA:
- Open the Gradle tool window (usually on the right side of the IDE).
- Navigate to Tasks -> application -> run.
- Double-click run to execute the task.
#### B) From Terminal (within the project root):
```bash
./gradlew run
```

### Building the Application
- [Windows Build Guide](https://github.com/TavstalDev/MesterMC-Installer/blob/master/docs/building/windows-build.md)
- [Linux Build Guide](https://github.com/TavstalDev/MesterMC-Installer/blob/master/docs/building/linux-build.md)
- [macOS Build Guide](https://github.com/TavstalDev/MesterMC-Installer/blob/master/docs/building/macos-build.md)
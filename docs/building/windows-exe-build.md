# Building the Windows Executable

This guide outlines how to create a `MesterMC.exe` file that launches your JavaFX application, complete with a custom icon. This process involves generating an icon file, creating a resource script, and then compiling a small C++ stub that links to these resources and launches your Java JAR.

---

## Prepare Your Icon (`icon.ico`)

First, ensure you have **ImageMagick** installed. It's a powerful tool for converting image formats and creating multi-resolution `.ico` files, which are essential for crisp icons across different sizes in Windows.

* **Install ImageMagick:**
    * **Debian/Ubuntu:**
        ```bash
        sudo apt-get install imagemagick
        ```
    * **Arch Linux:**
        ```bash
        sudo pacman -S imagemagick
        ```
    * **Windows (recommended via package manager):**
        * **Scoop:**
            ```bash
            scoop install imagemagick
            ```
        * **Chocolatey:**
            ```bash
            choco install imagemagick
            ```
        * *(Alternatively, download the official installer from the ImageMagick website.)*

* **Generate `icon.ico` from `icon.png`:**
  This command converts your `icon.png` into a high-quality `.ico` file, embedding multiple common icon sizes (16, 24, 32, 48, 64, and 256 pixels).

```
# Windows
convert.exe icon.png -define icon:auto-resize=16,24,32,48,64,256 icon.ico
```
```
# Linux
convert icon.png -define icon:auto-resize=16,24,32,48,64,256 icon.ico
```

---

## Create a Resource Script File (`.rc`)

This file tells the Windows Resource Compiler which resources (like your icon) to embed into the executable.

* **`mestermc.rc`:**
  Create a file named `mestermc.rc` with the following content. `IDI_ICON1` is a standard identifier for the primary application icon.

    ```c
    IDI_ICON1 ICON "icon.ico"
    ```

---

## Compile the Resource File

Use `windres` (the Windows Resource Compiler) to compile your `.rc` file into an object file (`.o`).

* **Compile `mestermc.rc` to `mestermc_res.o`:**

```
# Windows
x86_64-w64-mingw32-windres mestermc.rc -O coff -o mestermc_res.o
```

```
# Linux
x86_64-w64-mingw32-g++ mestermc_exe.cpp mestermc_res.o -o "MesterMC.exe" -static -lkernel32 -luser32 -lgdi32 -lole32 -loleaut32 -lcomctl32 -lshlwapi -lshfolder -Wl,--subsystem,windows
```

* **Note:** If you're cross-compiling from Linux, use the appropriate prefixed `windres` command from your MinGW-w64 toolchain (e.g., `x86_64-w64-mingw32-windres`). On Windows, `windres` should be directly available if you've installed MinGW/MSYS2.

---

## Create a C++ Stub Executable (`mestermc_exe.cpp`)

This small C++ program acts as the entry point for your Windows executable. Its primary job is to locate and launch your JavaFX application's JAR file using the `javaw.exe` runtime.

* **`mestermc_exe.cpp`:**
  Create a file named `mestermc_exe.cpp` with the following C++ code. Remember to adjust `"MesterMC.jar"` to the actual name and relative path of your compiled application JAR (e.g., `app\\MesterMC.jar` if `jpackage` places it in an `app` subdirectory).

    ```cpp
    #include <windows.h>
    #include <string>
    #include <vector>
    #include <io.h> // For _access (checking file existence)
    
    int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Determine the path to the Launcher.exe itself
    wchar_t exePathBuffer[MAX_PATH];
    GetModuleFileNameW(NULL, exePathBuffer, MAX_PATH);
    std::wstring exeFullPath = exePathBuffer;
    
        // Extract the directory where the EXE is located
        std::wstring exeDir = exeFullPath.substr(0, exeFullPath.find_last_of(L'\\'));
    
        // Assume the JAR is in the same directory as the EXE
        std::wstring jarFileName = L"MesterMC.jar"; // This must match the actual JAR filename
        std::wstring jarPath = exeDir + L"\\" + jarFileName;
    
        // Optional: Check if the JAR file actually exists
        if (_waccess(jarPath.c_str(), 0) == -1) { // 0 means check for existence
            MessageBoxW(NULL, (L"MesterMC.jar not found at:\n" + jarPath + L"\nPlease ensure the application is installed correctly.").c_str(), L"Launch Error", MB_OK | MB_ICONERROR);
            return 1;
        }
    
        // Construct the command to launch Java
        std::wstring command = L"javaw.exe -jar \"";
        command += jarPath;
        command += L"\"";
    
        // Create process (rest of the logic is similar)
        STARTUPINFOW si;
        PROCESS_INFORMATION pi;
        ZeroMemory(&si, sizeof(si));
        si.cb = sizeof(si);
        ZeroMemory(&pi, sizeof(pi));
    
        if (!CreateProcessW(NULL,           // No module name (use command line)
                            &command[0],    // Command line
                            NULL,           // Process handle not inheritable
                            NULL,           // Thread handle not inheritable
                            FALSE,          // Set handle inheritance to FALSE
                            0,              // No creation flags
                            NULL,           // Use parent's environment block
                            exeDir.c_str(), // Working directory for javaw
                            &si,            // Startup info
                            &pi)            // Process info
        ) {
            MessageBoxW(NULL, (L"Failed to launch MesterMC.jar. Command: " + command + L"\nEnsure Java is installed.").c_str(), L"Launch Error", MB_OK | MB_ICONERROR);
            return 1;
        }
    
        CloseHandle(pi.hProcess);
        CloseHandle(pi.hThread);
    
        return 0;
    }
    ```

---

## Compile the Executable

Finally, link your C++ stub with the compiled resource file and the necessary Windows libraries.

### A. Compiling on Windows (using MinGW-w64 via MSYS2 or similar)

If you're building directly on a Windows machine with a MinGW-w64 environment, use the standard `g++` command.

```bash
g++ mestermc_exe.cpp mestermc_res.o -o "MesterMC.exe" -static -lkernel32 -luser32 -lgdi32 -lole32 -loleaut32 -lcomctl32 -lshlwapi -lshfolder -Wl,--subsystem,windows
```

### B. Cross-compiling from Linux

If you're building your Windows executable on a Linux system, you'll use the MinGW-w64 cross-compiler. Ensure you have the mingw-w64 package installed (e.g., sudo apt-get install mingw-w64 on Debian/Ubuntu).

```bash
x86_64-w64-mingw32-g++ mestermc_exe.cpp mestermc_res.o -o "MesterMC.exe" -static -lkernel32 -luser32 -lgdi32 -lole32 -loleaut32 -lcomctl32 -lshlwapi -lshfolder -Wl,--subsystem,windows
```

## Copy the Generated Executable
After compiling, you should have a `MesterMC.exe` file in your current directory.
This executable will launch MesterMC.jar downloaded by the JavaFX application.
Copy this file to the project's resources/io/github/tavstal/mmcinstaller/exe directory.
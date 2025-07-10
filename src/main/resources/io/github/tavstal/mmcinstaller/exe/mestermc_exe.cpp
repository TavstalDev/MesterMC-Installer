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
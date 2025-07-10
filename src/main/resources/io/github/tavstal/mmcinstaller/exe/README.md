Linux:
x86_64-w64-mingw32-g++ mestermc_exe.cpp -o "MesterMC.exe" -static -lkernel32 -luser32 -lgdi32 -lole32 -loleaut32 -lcomctl32 -lshlwapi -lshfolder -Wl,--subsystem,windows

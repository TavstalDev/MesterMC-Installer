# Install ImageMagick if you don't have it
# On Debian/Ubuntu: sudo apt-get install imagemagick
# On Arch Linux: sudo pacman -S imagemagick
# On Windows (with scoop/chocolatey or direct installer): scoop install imagemagick

convert.exe icon.png -define icon:auto-resize=16,24,32,48,64,256 icon.ico

# Create a Resource Script File (.rc)
```mestermc.rc
IDI_ICON1 ICON "icon.ico"
```
# Compile the Resource File using windres

x86_64-w64-mingw32-windres mestermc.rc -O coff -o mestermc_res.o

Linux:
x86_64-w64-mingw32-g++ mestermc_exe.cpp mestermc_res.o -o "MesterMC.exe" -static -lkernel32 -luser32 -lgdi32 -lole32 -loleaut32 -lcomctl32 -lshlwapi -lshfolder -Wl,--subsystem,windows

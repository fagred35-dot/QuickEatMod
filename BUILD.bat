@echo off
echo ========================================
echo   Quick Eat Mod - Build Script
echo ========================================
echo.

set GRADLE_USER_HOME=C:\gradle_home

echo Building mod...
call gradlew.bat build

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo JAR file is in: build\libs\quickeat-1.0.0.jar
    echo Copy it to your Minecraft mods folder.
    echo.
    
    if not exist "mods" mkdir mods
    copy /Y "build\libs\quickeat-1.0.0.jar" "mods\" >nul 2>&1
    echo Also copied to: mods\quickeat-1.0.0.jar
) else (
    echo.
    echo BUILD FAILED! Check errors above.
    echo.
    echo Common fixes:
    echo 1. Make sure you have Java 17+ installed
    echo 2. Make sure you have internet connection
    echo 3. Try running again - sometimes downloads timeout
)

pause

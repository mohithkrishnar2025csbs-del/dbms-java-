@echo off
title Smart Lost-and-Found Management System
color 0B
cls

echo =========================================================
echo  SMART LOST-AND-FOUND MANAGEMENT SYSTEM
echo  Launching Desktop Application...
echo =========================================================
echo.

cd /d "%~dp0"

:: Locate Java runtime
set JAVA_CMD=java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%~dp0jdk-17.0.2\bin\java.exe" (
        set JAVA_CMD="%~dp0jdk-17.0.2\bin\java.exe"
    ) else (
        echo [ERROR] java runtime not found in PATH or portable JDK.
        pause
        exit /b 1
    )
)

:: Verify classes are compiled
if not exist "bin\app\Main.class" (
    echo [INFO] Compiled classes not found. Running compile.bat first...
    call compile.bat
)

echo [INFO] Starting Smart Lost-and-Found Application (app.Main)...
%JAVA_CMD% -cp "bin;lib\mysql-connector-j.jar" app.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application exited with code %ERRORLEVEL%.
    pause
)


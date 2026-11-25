@echo off
title Smart Lost-and-Found - Database Setup and Verification
color 0E
cls

echo =========================================================
echo  SMART LOST-AND-FOUND MANAGEMENT SYSTEM
echo  Database Setup and Verification Wizard
echo =========================================================
echo.

cd /d "%~dp0"

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

echo [INFO] Launching Database Setup Dialog...
%JAVA_CMD% -cp "bin;lib\mysql-connector-j.jar" app.SecureDatabaseSetupDialog

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Setup wizard exited with code %ERRORLEVEL%.
    pause
)

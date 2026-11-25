@echo off
title Smart Lost-and-Found - Project Build and Compiler
color 0A
cls

echo =========================================================
echo  SMART LOST-AND-FOUND MANAGEMENT SYSTEM
echo  Full Project Compilation Script
echo =========================================================
echo.

cd /d "%~dp0"

:: Ensure bin directory exists
if not exist "bin" mkdir "bin"

:: Locate Java compiler
set JAVAC_CMD=javac
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%~dp0jdk-17.0.2\bin\javac.exe" (
        set JAVAC_CMD="%~dp0jdk-17.0.2\bin\javac.exe"
    ) else (
        echo [ERROR] javac compiler not found in PATH or portable JDK.
        pause
        exit /b 1
    )
)

echo [INFO] Compiling all Java source files...
%JAVAC_CMD% -encoding UTF-8 -cp "lib\mysql-connector-j.jar;bin" -d bin ^
    src\model\*.java ^
    src\util\*.java ^
    src\dao\*.java ^
    src\service\*.java ^
    src\ui\*.java ^
    src\app\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================================
    echo  [SUCCESS] All Java sources compiled with 0 errors!
    echo =========================================================
    echo.
) else (
    echo.
    echo [ERROR] Compilation failed. Please check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

pause

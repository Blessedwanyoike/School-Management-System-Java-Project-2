@echo off
setlocal
cd /d "%~dp0"

if not exist ".build" mkdir ".build"

if defined APP_SEED_DATA_PASSWORD (
   rem Start the web portal in a separate window.
   start "Carol Academy Web Portal" /D "%~dp0web-portal" cmd /d /k "mvn spring-boot:run"
) else (
   echo Web portal not started: set APP_SEED_DATA_PASSWORD to enable it.
)

javac -encoding UTF-8 -d ".build" "School management.java"
if errorlevel 1 (
    echo.
    echo Carol Academy could not be compiled. Check that the JDK is installed.
    pause
    exit /b 1
)

java -cp ".build" SchoolManagement
if errorlevel 1 (
    echo.
    echo Carol Academy closed with an error.
    pause
    exit /b 1
)

endlocal

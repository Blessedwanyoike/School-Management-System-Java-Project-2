@echo off
setlocal
cd /d "%~dp0web-portal"

if not exist "pom.xml" (
    echo Could not find the Spring Boot project.
    pause
    exit /b 1
)

if not defined APP_SEED_DATA_PASSWORD (
    echo Set APP_SEED_DATA_PASSWORD before starting the web portal.
    echo Example: set "APP_SEED_DATA_PASSWORD=choose-a-local-password"
    pause
    exit /b 1
)

call mvn spring-boot:run
if errorlevel 1 (
    echo.
    echo The Carol Academy web portal stopped with an error.
    pause
)

endlocal

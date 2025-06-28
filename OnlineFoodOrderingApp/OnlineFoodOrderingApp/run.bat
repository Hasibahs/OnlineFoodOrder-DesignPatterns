@echo off
REM Detect JavaFX version (support 21 or 24.0.1)
setlocal

set "JAR=target\foodorderapp-1.0-SNAPSHOT.jar"
set "JFXLIB=lib\javafx-sdk-21\lib"
if not exist "%JFXLIB%" set "JFXLIB=lib\javafx-sdk-24.0.1\lib"
if not exist "%JFXLIB%" (
    echo JavaFX SDK not found in 'lib\' folder. Please download and extract it.
    pause
    exit /b
)

REM Detect Java (assumes java.exe in PATH)
where java >nul 2>&1
if errorlevel 1 (
    echo Java is not installed or not in PATH. Please install Java 17+ and try again.
    pause
    exit /b
)

REM Run the app!
java --module-path "%JFXLIB%" --add-modules javafx.controls,javafx.fxml -jar "%JAR%"
pause

@echo off
setlocal enabledelayedexpansion
REM Batch script for building and running the JavaFX application on Windows

REM Check for -r flag (run only, skip build)
set RUN_ONLY=false
if "%1"=="-r" set RUN_ONLY=true

REM Directory containing JavaFX JAR files and native libraries
set JAVAFX_DIR=.\lib\javafx

REM Source directories following MVC pattern
set SRC_DIR=.\src
set MAIN_FILE=.\main.java

REM The directory to output compiled classes
set BIN_DIR=.\bin

REM If -r flag is set, skip build and just run
if "%RUN_ONLY%"=="true" (
    REM Check if bin directory exists and has compiled classes
    if not exist "%BIN_DIR%" (
        echo Error: No compiled classes found. Please build first with: run.bat
        exit /b 1
    )
    dir /s /b "%BIN_DIR%\*.class" >nul 2>&1
    if errorlevel 1 (
        echo Error: No compiled classes found. Please build first with: run.bat
        exit /b 1
    )
    goto :run
)

REM Check for zip files that haven't been extracted
echo Checking for unextracted zip files...
set ERROR_FOUND=0
for /r %%f in (*.zip) do (
    set "ZIP_FILE=%%f"
    set "ZIP_DIR=%%~dpf"
    set "ZIP_NAME=%%~nf"
    call :check_zip
)

if !ERROR_FOUND!==1 exit /b 1

REM Create the bin directory if it doesn't exist
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

REM Build the project
echo Building the project...
if exist "%JAVAFX_DIR%" (
    REM Use JavaFX directory if it exists
    set LIBRARY_PATH=
    for %%f in ("%JAVAFX_DIR%\*.jar") do (
        if defined LIBRARY_PATH (
            set "LIBRARY_PATH=!LIBRARY_PATH!;%%f"
        ) else (
            set "LIBRARY_PATH=%%f"
        )
    )
) else (
    REM Fallback to lib directory
    set LIB_DIR=.\lib
    set LIBRARY_PATH=
    for %%f in ("%LIB_DIR%\*.jar") do (
        if defined LIBRARY_PATH (
            set "LIBRARY_PATH=!LIBRARY_PATH!;%%f"
        ) else (
            set "LIBRARY_PATH=%%f"
        )
    )
)

if not defined LIBRARY_PATH (
    echo Error: No JavaFX libraries found in %JAVAFX_DIR% or %LIB_DIR%
    exit /b 1
)

REM Compile all Java files from src subdirectories and main
set JAVA_FILES=%MAIN_FILE%
for /r "%SRC_DIR%" %%f in (*.java) do (
    set "JAVA_FILES=!JAVA_FILES! %%f"
)

javac -cp "!LIBRARY_PATH!" -d "%BIN_DIR%" !JAVA_FILES!

if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo Build successful!
echo.

:run
REM Run the application
echo Running the application...
java --module-path "%JAVAFX_DIR%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -Djava.library.path="%JAVAFX_DIR%" -cp "%BIN_DIR%;.;%SRC_DIR%" main

exit /b %ERRORLEVEL%

:check_zip
REM Check if corresponding directory exists for zip file
set "EXPECTED_DIR=!ZIP_DIR!!ZIP_NAME!"
if not exist "!EXPECTED_DIR!" (
    echo ERROR: Found zip file '!ZIP_FILE!' but no corresponding directory '!EXPECTED_DIR!'
    echo        Please extract the zip file before building.
    set ERROR_FOUND=1
)
goto :eof


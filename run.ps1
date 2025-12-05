# PowerShell script for building and running the JavaFX application on Windows

# Check for -r flag (run only, skip build)
$RunOnly = $false
if ($args[0] -eq "-r") {
    $RunOnly = $true
}

# Directory containing JavaFX JAR files and native libraries
$JavaFxDir = ".\lib\javafx"

# Source directories following MVC pattern
$SrcDir = ".\src"
$MainFile = ".\main.java"

# The directory to output compiled classes
$BinDir = ".\bin"

# Function to run the application
function Run-Application {
    Write-Host "Running the application..."
    $classpath = "$BinDir;.;$SrcDir"
    java --module-path "$JavaFxDir" `
         --add-modules javafx.controls,javafx.fxml `
         --enable-native-access=javafx.graphics `
         "-Djava.library.path=$JavaFxDir" `
         -cp $classpath `
         main
}

# If -r flag is set, skip build and just run
if ($RunOnly) {
    # Check if bin directory exists and has compiled classes
    if (-not (Test-Path $BinDir) -or -not (Get-ChildItem -Path $BinDir -Filter "*.class" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1)) {
        Write-Host "Error: No compiled classes found. Please build first with: .\run.ps1"
        exit 1
    }
    Run-Application
    exit 0
}

# Check for zip files that haven't been extracted
Write-Host "Checking for unextracted zip files..."
$ZipFiles = Get-ChildItem -Path . -Filter "*.zip" -File -Recurse -ErrorAction SilentlyContinue
$ErrorFound = $false

foreach ($zipFile in $ZipFiles) {
    # Get the zip file name without extension
    $zipBasename = [System.IO.Path]::GetFileNameWithoutExtension($zipFile.Name)
    $zipDir = $zipFile.DirectoryName
    
    # Check if there's a corresponding directory with the same name
    $expectedDir = Join-Path $zipDir $zipBasename
    
    if (-not (Test-Path $expectedDir -PathType Container)) {
        Write-Host "ERROR: Found zip file '$($zipFile.FullName)' but no corresponding directory '$expectedDir'"
        Write-Host "       Please extract the zip file before building."
        $ErrorFound = $true
    }
}

if ($ErrorFound) {
    exit 1
}

# Create the bin directory if it doesn't exist
if (-not (Test-Path $BinDir)) {
    New-Item -ItemType Directory -Path $BinDir | Out-Null
}

# Build the project
Write-Host "Building the project..."
if (Test-Path $JavaFxDir) {
    # Use JavaFX directory if it exists
    $jarFiles = Get-ChildItem -Path $JavaFxDir -Filter "*.jar" -File
    $LibraryPath = ($jarFiles.FullName -join ";")
} else {
    # Fallback to lib directory
    $LibDir = ".\lib"
    $jarFiles = Get-ChildItem -Path $LibDir -Filter "*.jar" -File -ErrorAction SilentlyContinue
    if ($jarFiles) {
        $LibraryPath = ($jarFiles.FullName -join ";")
    } else {
        Write-Host "Error: No JavaFX libraries found in $JavaFxDir or $LibDir"
        exit 1
    }
}

# Compile all Java files from src subdirectories and main
$JavaFiles = Get-ChildItem -Path $SrcDir -Filter "*.java" -Recurse -File
$allJavaFiles = @($MainFile) + $JavaFiles.FullName

javac -cp "$LibraryPath" -d "$BinDir" $allJavaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!"
    exit 1
}

Write-Host "Build successful!"
Write-Host ""

# Run the application
Run-Application
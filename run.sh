#!/bin/bash

# Check for -r flag (run only, skip build)
RUN_ONLY=false
if [ "$1" = "-r" ]; then
    RUN_ONLY=true
fi

# Directory containing JavaFX JAR files and native libraries
JAVAFX_DIR="./lib/javafx"

# Source directories following MVC pattern
SRC_DIR="./src"
MAIN_FILE="./main.java"

# The directory to output compiled classes
BIN_DIR="./bin"

# Function to run the application
run_application() {
    echo "Running the application..."
    java --module-path "$JAVAFX_DIR" \
         --add-modules javafx.controls,javafx.fxml \
         --enable-native-access=javafx.graphics \
         -Djava.library.path="$JAVAFX_DIR" \
         -cp "$BIN_DIR:.:$SRC_DIR" \
         main
}

# If -r flag is set, skip build and just run
if [ "$RUN_ONLY" = true ]; then
    # Check if bin directory exists and has compiled classes
    if [ ! -d "$BIN_DIR" ] || [ -z "$(find "$BIN_DIR" -name "*.class" 2>/dev/null | head -1)" ]; then
        echo "Error: No compiled classes found. Please build first with: ./run.sh"
        exit 1
    fi
    run_application
    exit 0
fi

# Check for zip files that haven't been extracted
echo "Checking for unextracted zip files..."
ZIP_FILES=$(find . -name "*.zip" -type f 2>/dev/null)
ERROR_FOUND=0

for zip_file in $ZIP_FILES; do
    # Get the zip file name without extension
    zip_basename=$(basename "$zip_file" .zip)
    zip_dir=$(dirname "$zip_file")
    
    # Check if there's a corresponding directory with the same name
    expected_dir="$zip_dir/$zip_basename"
    
    if [ ! -d "$expected_dir" ]; then
        echo "ERROR: Found zip file '$zip_file' but no corresponding directory '$expected_dir'"
        echo "       Please extract the zip file before building."
        ERROR_FOUND=1
    fi
done

if [ $ERROR_FOUND -eq 1 ]; then
    exit 1
fi

# Create the bin directory if it doesn't exist
mkdir -p "$BIN_DIR"

# Build the project
echo "Building the project..."
if [ -d "$JAVAFX_DIR" ]; then
    # Use JavaFX directory if it exists
    LIBRARY_PATH=$(echo $JAVAFX_DIR/*.jar | tr ' ' ':')
else
    # Fallback to lib directory
    LIB_DIR="./lib"
    LIBRARY_PATH=$(echo $LIB_DIR/*.jar | tr ' ' ':')
fi

# Compile all Java files from src subdirectories and main
JAVA_FILES=$(find "$SRC_DIR" -name "*.java")
javac -cp "$LIBRARY_PATH" -d "$BIN_DIR" "$MAIN_FILE" $JAVA_FILES

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"
echo ""

# Run the application
run_application

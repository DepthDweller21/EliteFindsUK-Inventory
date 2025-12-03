#!/bin/bash

# Directory containing your JAR files
LIB_DIR="./lib"

# Detect all JAR files in the lib directory
LIBRARY_PATH=$(echo $LIB_DIR/*.jar | tr ' ' ':')

# The source files directory
SRC_DIR="."  # Adjust this to the directory where your Java files are

# The directory to output compiled classes
BIN_DIR="./bin"

# Create the bin directory if it doesn't exist
mkdir -p $BIN_DIR

# Compile the Java files and include the JARs in the classpath
echo "Building the project..."
javac -cp "$LIBRARY_PATH" -d "$BIN_DIR" $SRC_DIR/*.java
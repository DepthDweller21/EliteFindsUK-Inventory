#!/bin/bash

# Script to download and set up JavaFX native libraries

echo "JavaFX Native Libraries Setup"
echo "=============================="
echo ""

# Detect architecture
ARCH=$(uname -m)
if [ "$ARCH" = "x86_64" ]; then
    FX_ARCH="x64"
elif [ "$ARCH" = "aarch64" ]; then
    FX_ARCH="aarch64"
else
    echo "Unsupported architecture: $ARCH"
    exit 1
fi

FX_VERSION="25.0.1"
FX_URL="https://download2.gluonhq.com/openjfx/${FX_VERSION}/openjfx-${FX_VERSION}_linux-${FX_ARCH}_bin-sdk.zip"
NATIVE_DIR="./lib/native"
TEMP_ZIP="./javafx-sdk.zip"

echo "Architecture: $FX_ARCH"
echo "JavaFX Version: $FX_VERSION"
echo ""

# Check if native libraries already exist
if [ -d "$NATIVE_DIR" ] && [ -n "$(find "$NATIVE_DIR" -name "*.so" 2>/dev/null | head -1)" ]; then
    echo "Native libraries already exist in $NATIVE_DIR"
    echo "Skipping download."
    exit 0
fi

echo "Downloading JavaFX SDK (this may take a while)..."
echo "URL: $FX_URL"
echo ""

# Download JavaFX SDK
if command -v wget &> /dev/null; then
    wget -O "$TEMP_ZIP" "$FX_URL"
elif command -v curl &> /dev/null; then
    curl -L -o "$TEMP_ZIP" "$FX_URL"
else
    echo "Error: Neither wget nor curl found. Please install one of them."
    exit 1
fi

if [ ! -f "$TEMP_ZIP" ]; then
    echo "Error: Download failed."
    exit 1
fi

echo ""
echo "Extracting native libraries..."

# Create native directory
mkdir -p "$NATIVE_DIR"

# Extract only the native libraries
if command -v unzip &> /dev/null; then
    # Extract the lib directory from the SDK
    unzip -q "$TEMP_ZIP" -d ./temp_javafx 2>/dev/null || {
        echo "Error: Failed to extract. Trying alternative method..."
        # Try to find and extract lib directory
        unzip -q -j "$TEMP_ZIP" "*/lib/*.so" -d "$NATIVE_DIR" 2>/dev/null || {
            echo "Please extract manually:"
            echo "  1. Unzip $TEMP_ZIP"
            echo "  2. Copy the lib/*.so files to $NATIVE_DIR"
            rm -f "$TEMP_ZIP"
            exit 1
        }
    }
    
    # Find and copy native libraries
    if [ -d "./temp_javafx" ]; then
        find ./temp_javafx -name "*.so" -exec cp {} "$NATIVE_DIR/" \;
        rm -rf ./temp_javafx
    fi
else
    echo "Error: unzip not found. Please install unzip."
    rm -f "$TEMP_ZIP"
    exit 1
fi

# Clean up
rm -f "$TEMP_ZIP"

# Verify extraction
if [ -n "$(find "$NATIVE_DIR" -name "*.so" 2>/dev/null | head -1)" ]; then
    echo "✓ Native libraries extracted successfully to $NATIVE_DIR"
    echo ""
    echo "You can now run: ./run.sh"
else
    echo "Error: Native libraries not found after extraction."
    echo "Please download manually from: https://openjfx.io/"
    echo "Extract the .so files to: $NATIVE_DIR"
    exit 1
fi


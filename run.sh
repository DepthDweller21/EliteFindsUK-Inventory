#!/bin/bash

JAVAFX_DIR="./lib/javafx"

BIN_DIR="./bin"

echo "Running the application..."
java --module-path "$JAVAFX_DIR" \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     -Djava.library.path="$JAVAFX_DIR" \
     -cp "$BIN_DIR:." \
     main

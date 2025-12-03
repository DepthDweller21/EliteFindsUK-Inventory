# JavaFX MVC Application

A JavaFX application following the Model-View-Controller (MVC) architectural pattern.

## Project Structure

The project is organized into MVC directories:

```
project/
├── main.java              # Application entry point
├── src/                   # Source code directory
│   ├── model/            # Model layer (business logic and state)
│   │   └── Model.java    # Data model and business logic
│   ├── view/             # View layer (UI definitions)
│   │   └── sample.fxml   # FXML UI layout file
│   └── controller/       # Controller layer (user input handling)
│       └── Controller.java # Handles user interactions
├── bin/                   # Compiled class files (generated)
├── lib/                   # External libraries
│   └── javafx/           # JavaFX SDK (JARs and native libraries)
├── run.sh                # Build and run script
└── Readme.md             # This file
```

## Directory Organization

### `src/model/`

Contains the **Model** classes that represent the application's data and business logic.

-   `Model.java`: Holds application state and implements business logic
-   Uses JavaFX properties for observable data binding
-   Package: `model`

### `src/view/`

Contains the **View** files that define the user interface.

-   `sample.fxml`: FXML file defining the UI layout
-   FXML files are loaded at runtime and connected to controllers

### `src/controller/`

Contains the **Controller** classes that handle user input and coordinate between Model and View.

-   `Controller.java`: Handles UI events and updates the model
-   Implements `Initializable` for FXML initialization
-   Receives model instance from main application
-   Package: `controller`

### Root Directory

-   `main.java`: Application entry point that:
    1. Creates the Model instance
    2. Loads the View (FXML) from `src/view/`
    3. Connects the Controller to the Model
    4. Sets up and displays the JavaFX stage

## Building and Running

### Prerequisites

-   Java 11 or higher
-   JavaFX SDK (included in `lib/javafx/`)
-   Native libraries extracted (no `.zip` files without corresponding directories)

### Build and Run

```bash
./run.sh
```

The script will:

1. Check for unextracted zip files (error if found)
2. Compile all Java source files from MVC directories
3. Run the application

### Manual Build

```bash
# Compile
javac -cp "./lib/javafx/*.jar" -d ./bin \
    ./main.java \
    ./src/model/*.java \
    ./src/controller/*.java

# Run
java --module-path "./lib/javafx" \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     -Djava.library.path="./lib/javafx" \
     -cp "./bin:.:./src" \
     main
```

## MVC Pattern

### Model (`src/model/Model.java`)

-   **Responsibility**: Business logic and data management
-   **Features**:
    -   Uses JavaFX properties for reactive data binding
    -   Contains application state (click count, messages)
    -   Implements business logic methods

### View (`src/view/sample.fxml`)

-   **Responsibility**: UI presentation
-   **Features**:
    -   Defined in FXML (declarative UI)
    -   References controller via `fx:controller` attribute
    -   UI elements bound to controller via `fx:id`

### Controller (`src/controller/Controller.java`)

-   **Responsibility**: User input handling and Model-View coordination
-   **Features**:
    -   Handles UI events (button clicks, etc.)
    -   Receives model instance from main application
    -   Binds view elements to model properties
    -   Delegates business logic to model

### Main Application (`main.java`)

-   **Responsibility**: Application initialization and MVC wiring
-   **Process**:
    1. Creates Model instance
    2. Loads FXML view
    3. Gets Controller from FXMLLoader
    4. Connects Controller to Model
    5. Displays the stage

## File Naming Conventions

-   **Model classes**: PascalCase (e.g., `Model.java`)
-   **Controller classes**: PascalCase (e.g., `Controller.java`)
-   **FXML files**: lowercase with underscores (e.g., `sample.fxml`)
-   **Main class**: lowercase (e.g., `main.java`)

## Adding New Features

1. **New Model**: Add to `src/model/` directory with `package model;`
2. **New View**: Add FXML file to `src/view/` directory
3. **New Controller**: Add to `src/controller/` directory with `package controller;`
4. **Update main.java**: Wire new components together and update FXML path if needed

## Troubleshooting

-   **"Graphics Device initialization failed"**: Ensure JavaFX native libraries are in `lib/javafx/`
-   **"Class not found"**: Run `./run.sh` to rebuild
-   **"FXML not found"**: Ensure FXML files are in `src/view/` directory and path in `main.java` is `src/view/sample.fxml`

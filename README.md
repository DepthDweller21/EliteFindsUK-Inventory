# JavaFX MVC Application

A simple JavaFX application using the Model-View-Controller pattern.

## Quick Start

### Prerequisites

-   Java 11 or higher
-   JavaFX SDK (already included in `lib/javafx/`)

### Run the Application

**On Linux/Mac:**

```bash
./run.sh
```

**On Windows:**

```powershell
# Using PowerShell
.\run.ps1

# Or using Command Prompt
run.bat
```

This will build and run the application. The script checks for unextracted zip files and compiles everything automatically.

**Run without rebuilding:**

```bash
# Linux/Mac
./run.sh -r

# Windows PowerShell
.\run.ps1 -r

# Windows CMD
run.bat -r
```

### Manual Build (if needed)

```bash
# Compile
javac -cp "./lib/javafx/*.jar" -d ./bin ./main.java ./src/*/*.java

# Run
java --module-path "./lib/javafx" \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     -Djava.library.path="./lib/javafx" \
     -cp "./bin:.:./src" \
     main
```

## Project Structure

```
project/
├── main.java              # Application entry point
├── src/
│   ├── model/            # Business logic and data
│   ├── view/             # UI layouts (FXML files)
│   └── controller/       # User input handling
├── bin/                  # Compiled classes (auto-generated)
├── lib/javafx/          # JavaFX libraries
└── docs/                 # Documentation
```

## Documentation

-   [Model Layer](docs/model.md) - How the data and business logic work
-   [View Layer](docs/view.md) - How the UI is defined
-   [Controller Layer](docs/controller.md) - How user interactions are handled
-   [Project Guide](docs/project.md) - Architecture overview and how to extend the project

## Troubleshooting

-   **"Graphics Device initialization failed"**: Make sure JavaFX native libraries are in `lib/javafx/`
-   **"Class not found"**: Run `./run.sh` to rebuild
-   **"FXML not found"**: Check that FXML files are in `src/view/` and the path in `main.java` is correct

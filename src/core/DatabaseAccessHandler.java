package core;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Utility class to handle database access errors
 * Shows popup and redirects to home page when database connection is missing
 */
public class DatabaseAccessHandler {
    /**
     * Execute a database operation, showing popup and redirecting if connection is missing
     * @param operation The database operation to execute
     * @param stage The current stage (for redirect)
     */
    public static void handleDatabaseAccess(Runnable operation, Stage stage) {
        // Check if connection is available before executing
        if (!Connection.getInstance().isConnected()) {
            showNoConnectionAlert(stage);
            return;
        }
        
        try {
            operation.run();
        } catch (NoDatabaseConnectionException e) {
            showNoConnectionAlert(stage);
        } catch (Exception e) {
            // Other exceptions are rethrown
            throw e;
        }
    }
    
    /**
     * Show alert and redirect to home page
     */
    public static void showNoConnectionAlert(Stage stage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Database Connection Required");
            alert.setHeaderText(null);
            alert.setContentText("Database connection required. Please configure connection string in Settings.");
            alert.showAndWait();
            
            // Redirect to home page
            try {
                FXMLLoader loader = new FXMLLoader(
                    DatabaseAccessHandler.class.getResource("../features/homepage/homepage.fxml")
                );
                Parent root = loader.load();
                Scene scene = new Scene(root, 1100, 700);
                stage.setScene(scene);
                stage.setTitle("Home");
            } catch (Exception e) {
                System.err.println("Error redirecting to home page: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}


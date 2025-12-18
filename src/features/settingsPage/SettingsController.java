package features.settingsPage;

import core.ConfigManager;
import core.Connection;
import com.mongodb.client.MongoClients;
import features.revenue.RevenueController;
import features.revenue.RevenueModel;
import features.stock.StockController;
import features.stock.StockModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML private AnchorPane contentArea;
    
    // Settings fields
    @FXML private TextField connectionStringField;
    @FXML private TextField gbpToPkrRateField;
    
    // Buttons
    @FXML private Button saveBtn;
    @FXML private Button testConnectionBtn;
    @FXML private Button resetBtn;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSettings();
    }
    
    /**
     * Load current settings from config and populate fields
     */
    private void loadSettings() {
        // Load connection string
        String connectionString = ConfigManager.getConnectionString();
        if (connectionStringField != null) {
            connectionStringField.setText(connectionString != null ? connectionString : "");
        }
        
        // Load exchange rate
        double exchangeRate = ConfigManager.getGbpToPkrRate();
        if (gbpToPkrRateField != null) {
            gbpToPkrRateField.setText(String.valueOf(exchangeRate));
        }
    }
    
    /**
     * Save settings to config.xml
     */
    @FXML
    private void handleSave() {
        // Validate exchange rate
        double exchangeRate;
        try {
            exchangeRate = Double.parseDouble(gbpToPkrRateField.getText().trim());
            if (exchangeRate <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", 
                    "Exchange rate must be a positive number");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Exchange rate must be a valid number");
            return;
        }
        
        // Save settings
        try {
            String connectionString = connectionStringField.getText().trim();
            ConfigManager.setConnectionString(connectionString.isEmpty() ? null : connectionString);
            ConfigManager.setGbpToPkrRate(exchangeRate);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Settings saved successfully. Restart the application for database connection changes to take effect.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection with entered connection string
     */
    @FXML
    private void handleTestConnection() {
        String connectionString = connectionStringField.getText().trim();
        
        if (connectionString.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Connection string cannot be empty");
            return;
        }
        
        try {
            // Try to create a MongoDB client with the connection string
            com.mongodb.client.MongoClient testClient = MongoClients.create(connectionString);
            testClient.listDatabaseNames().first(); // Try to access database
            testClient.close();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Connection test successful!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Connection Test Failed", 
                "Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reset fields to default values
     */
    @FXML
    private void handleReset() {
        if (connectionStringField != null) {
            connectionStringField.clear();
        }
        if (gbpToPkrRateField != null) {
            gbpToPkrRateField.setText("350.0");
        }
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Navigation handlers
    @FXML
    private void handleStockButton() {
        navigateToPage("../stock/stock.fxml", "Stock");
    }
    
    @FXML
    private void handleRevenueButton() {
        navigateToPage("../revenue/revenue.fxml", "Revenue");
    }
    
    @FXML
    private void handleSettingsButton() {
        // Already on settings page, do nothing
    }
    
    @FXML
    private void handleLogsButton() {
        navigateToPage("../logsPage/log.fxml", "Logs");
    }
    
    @FXML
    private void handleMainMenuButton() {
        navigateToPage("../homepage/homepage.fxml", "Home");
    }
    
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Special handling for stock page - initialize model
            if (fxmlPath.contains("stock.fxml")) {
                StockController controller = loader.getController();
                try {
                    StockModel stockModel = new StockModel();
                    controller.setModel(stockModel);
                } catch (core.NoDatabaseConnectionException e) {
                    core.DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            // Special handling for revenue page - initialize model
            else if (fxmlPath.contains("revenue.fxml")) {
                RevenueController controller = loader.getController();
                try {
                    RevenueModel revenueModel = new RevenueModel();
                    controller.setModel(revenueModel);
                } catch (core.NoDatabaseConnectionException e) {
                    core.DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            // Special handling for logs page - initialize model
            else if (fxmlPath.contains("log.fxml")) {
                features.logsPage.LogsController controller = loader.getController();
                try {
                    features.logsPage.LogModel logModel = new features.logsPage.LogModel();
                    controller.setModel(logModel);
                } catch (core.NoDatabaseConnectionException e) {
                    core.DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            
            Stage stage = getStage();
            if (stage != null) {
                Scene scene = new Scene(root, 1100, 700);
                stage.setScene(scene);
                stage.setTitle(title);
                
                // Force layout recalculation
                Platform.runLater(() -> {
                    stage.sizeToScene();
                    root.requestLayout();
                });
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to " + title + ": " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    
    /**
     * Get the current stage
     */
    private Stage getStage() {
        if (contentArea != null && contentArea.getScene() != null) {
            return (Stage) contentArea.getScene().getWindow();
        }
        return null;
    }
}


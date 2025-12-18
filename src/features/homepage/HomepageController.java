package features.homepage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import core.ClockWidget;
import features.revenue.RevenueController;
import features.revenue.RevenueModel;
import core.DatabaseAccessHandler;
import core.NoDatabaseConnectionException;
import features.logsPage.LogsController;
import features.logsPage.LogModel;
import features.revenue.RevenueController;
import features.revenue.RevenueModel;
import features.stock.StockController;
import features.stock.StockModel;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the homepage
 * Manages the two clocks (Pakistan and UK time)
 */
public class HomepageController implements Initializable {
    
    @FXML
    private HBox clockContainer;
    
    @FXML
    private Button stockBtn;
    
    @FXML
    private Button revenueBtn;
    
    @FXML
    private Button archiveBtn;
    
    @FXML
    private Button logsBtn;
    
    private ClockWidget pakistanClock;
    private ClockWidget ukClock;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Create clocks for both timezones
        pakistanClock = new ClockWidget("Asia/Karachi", "Pakistan Time");
        ukClock = new ClockWidget("Europe/London", "UK Time");
        
        // Add clocks to the container
        clockContainer.getChildren().addAll(pakistanClock, ukClock);
        
        // Start both clocks
        pakistanClock.start();
        ukClock.start();
    }
    
    /**
     * Clean up when the scene is closed
     */
    public void cleanup() {
        if (pakistanClock != null) {
            pakistanClock.stop();
        }
        if (ukClock != null) {
            ukClock.stop();
        }
    }
    
    /**
     * Get the stage from any available node
     */
    private Stage getStage() {
        // Try to get stage from button first (most reliable)
        if (stockBtn != null && stockBtn.getScene() != null) {
            return (Stage) stockBtn.getScene().getWindow();
        }
        if (revenueBtn != null && revenueBtn.getScene() != null) {
            return (Stage) revenueBtn.getScene().getWindow();
        }
        if (archiveBtn != null && archiveBtn.getScene() != null) {
            return (Stage) archiveBtn.getScene().getWindow();
        }
        if (logsBtn != null && logsBtn.getScene() != null) {
            return (Stage) logsBtn.getScene().getWindow();
        }
        // Fallback to clockContainer
        if (clockContainer != null && clockContainer.getScene() != null) {
            return (Stage) clockContainer.getScene().getWindow();
        }
        return null;
    }
    
    @FXML
    private void handleStockButton() {
        try {
            // Cleanup clocks before navigating
            cleanup();
            
            // Load the stock view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../stock/stock.fxml"));
            Parent root = loader.load();
            StockController controller = loader.getController();
            
            // Create and set the model (with database access handling)
            Stage currentStage = getStage();
            try {
                StockModel model = new StockModel();
                controller.setModel(model);
            } catch (NoDatabaseConnectionException e) {
                DatabaseAccessHandler.showNoConnectionAlert(currentStage);
                return;
            }
            
            // Get the stage and switch scene - try multiple methods
            Stage stage = getStage();
            if (stage != null) {
                Scene scene = new Scene(root, 1100, 700);
                stage.setScene(scene);
                stage.setTitle("Stock");
                
                // Force layout recalculation
                Platform.runLater(() -> {
                    stage.sizeToScene();
                    root.requestLayout();
                });
                
                // Cleanup on close
                stage.setOnCloseRequest(event -> {
                    if (controller != null) {
                        controller.cleanup();
                    }
                });
            } else {
                throw new RuntimeException("Could not retrieve stage for navigation");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to Stock: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSettingsButton() {
        navigateToPage("../settingsPage/settings.fxml", "Settings");
    }
    
    @FXML
    private void handleLogsButton() {
        navigateToPage("../logsPage/log.fxml", "Logs");
    }
    
    @FXML
    private void handleRevenueButton() {
        navigateToPage("../revenue/revenue.fxml", "Revenue");
    }
    
    /**
     * Navigate to a different page
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            // Cleanup clocks before navigating
            cleanup();
            
            // Load the new view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Special handling for stock page - initialize model
            if (fxmlPath.contains("stock.fxml")) {
                StockController controller = loader.getController();
                try {
                    StockModel model = new StockModel();
                    controller.setModel(model);
                } catch (NoDatabaseConnectionException e) {
                    DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            // Special handling for revenue page - initialize model
            else if (fxmlPath.contains("revenue.fxml")) {
                RevenueController controller = loader.getController();
                try {
                    RevenueModel revenueModel = new RevenueModel();
                    controller.setModel(revenueModel);
                } catch (NoDatabaseConnectionException e) {
                    DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            // Special handling for logs page - initialize model
            else if (fxmlPath.contains("log.fxml")) {
                LogsController controller = loader.getController();
                try {
                    LogModel logModel = new LogModel();
                    controller.setModel(logModel);
                } catch (NoDatabaseConnectionException e) {
                    DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            
            // Get the stage and switch scene
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
            } else {
                throw new RuntimeException("Could not retrieve stage for navigation");
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
    
    @FXML
    private void handleLogoutButton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Click Yes to logout or No to cancel.");
        
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                try {
                    // Cleanup clocks before navigating
                    cleanup();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../login/login.fxml"));
                    Parent root = loader.load();

                    Stage stage = getStage();
                    if (stage != null) {
                        Scene scene = new Scene(root, 500, 500);
                        stage.setScene(scene);
                        stage.setTitle("Elite Finds UK - Login");
                        stage.setWidth(500);
                        stage.setHeight(500);
                        
                        // Force layout recalculation
                        Platform.runLater(() -> {
                            stage.sizeToScene();
                            root.requestLayout();
                        });
                    } else {
                        throw new RuntimeException("Could not retrieve stage for navigation");
                    }
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Navigation Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to logout: " + e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }
}

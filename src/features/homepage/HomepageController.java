package features.homepage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import core.ClockWidget;
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
    
    @FXML
    private void handleStockButton() {
        try {
            // Load the stock view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../stock/stock.fxml"));
            Parent root = loader.load();
            StockController controller = loader.getController();
            
            // Create and set the model
            StockModel model = new StockModel();
            controller.setModel(model);
            
            // Get the stage and switch scene
            Stage stage = (Stage) clockContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Stock");
            
            // Cleanup on close
            stage.setOnCloseRequest(event -> {
                if (controller != null) {
                    controller.cleanup();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleArchiveButton() {
        System.out.println("Archive button clicked");
        // Add navigation logic here
    }
    
    @FXML
    private void handleFilesButton() {
        System.out.println("Files button clicked");
        // Add navigation logic here
    }
    
    @FXML
    private void handleLogsButton() {
        System.out.println("Logs button clicked");
        // Add navigation logic here
    }
    
    @FXML
    private void handleRevenueButton() {
        System.out.println("Revenue button clicked");
        // Add navigation logic here
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

                    Stage stage = (Stage) clockContainer.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Elite Finds UK - Login");
                    stage.setWidth(500);
                    stage.setHeight(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

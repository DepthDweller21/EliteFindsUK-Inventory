package features.homepage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import core.ClockWidget;

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
        System.out.println("Stock button clicked");
        // Add navigation logic here
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
}

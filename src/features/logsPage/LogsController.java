package features.logsPage;

import features.stock.StockController;
import features.stock.StockModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LogsController implements Initializable {
    @FXML private AnchorPane contentArea;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization if needed
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
    private void handleArchiveButton() {
        navigateToPage("../archivePage/archive.fxml", "Archive");
    }
    
    @FXML
    private void handleLogsButton() {
        // Already on logs page, do nothing
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
                StockModel model = new StockModel();
                controller.setModel(model);
            }
            
            Stage stage = (Stage) (contentArea != null ? contentArea.getScene().getWindow() : null);
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
}


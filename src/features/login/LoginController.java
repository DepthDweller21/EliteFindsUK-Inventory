package features.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import features.homepage.HomepageController;

public class LoginController {
    
    @FXML
    private TextField userName;
    
    @FXML
    private PasswordField pwd;
    
    @FXML
    private Button loginBtn;
    
    @FXML
    private void handleLogin() {
        // Validate credentials
        String username = userName.getText().trim();
        String password = pwd.getText();
        
        // Check if username and password are "admin"
        if (!"admin".equals(username) || !"admin".equals(password)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password. Please try again.");
            alert.showAndWait();
            // Clear password field
            pwd.clear();
            return;
        }
        
        // Credentials are correct, navigate to homepage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../homepage/homepage.fxml"));
            Parent root = loader.load();
            HomepageController controller = loader.getController();
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 700));
            stage.setTitle("Elite Finds UK - Homepage");
            stage.setOnCloseRequest(event -> {
                if (controller != null) {
                    controller.cleanup();
                }
            });
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load homepage: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}

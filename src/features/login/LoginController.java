package features.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        // TODO: Add authentication logic here
        // For now, just navigate to homepage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../homepage/homepage.fxml"));
            Parent root = loader.load();
            HomepageController controller = loader.getController();
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Elite Finds UK - Homepage");
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.setOnCloseRequest(event -> {
                if (controller != null) {
                    controller.cleanup();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class Controller {
    @FXML
    private Button clickButton;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private void handleButtonClick() {
        messageLabel.setText("Button was clicked! 🎉");
    }
}

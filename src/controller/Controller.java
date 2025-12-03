package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.Model;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for MVC pattern
 * Handles user input and updates the view based on model changes
 */
public class Controller implements Initializable {
    private Model model;
    
    @FXML
    private Button clickButton;
    
    @FXML
    private Label messageLabel;
    
    /**
     * Initialize the controller with the model
     */
    public void setModel(Model model) {
        this.model = model;
        // Bind the label to the model's message property
        messageLabel.textProperty().bind(model.messageProperty());
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This is called after FXML loading
        // Model will be set separately by the main application
    }
    
    /**
     * Handle button click event
     * Delegates to the model for business logic
     */
    @FXML
    private void handleButtonClick() {
        if (model != null) {
            model.handleButtonClick();
        }
    }
}

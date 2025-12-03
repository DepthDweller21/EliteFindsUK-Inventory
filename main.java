import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Model;
import controller.Controller;

/**
 * Main application class following MVC pattern
 * Initializes Model, View, and Controller and connects them
 */
public class main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create the Model (business logic and state)
        Model model = new Model();
        
        // Load the View (FXML)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("src/view/sample.fxml"));
        Parent root = loader.load();
        
        // Get the Controller and connect it to the Model
        Controller controller = loader.getController();
        controller.setModel(model);
        
        // Set up the stage
        primaryStage.setTitle("MVC JavaFX Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import core.Connection;
/**
 * Main application class following MVC pattern
 * Initializes Model, View, and Controller and connects them
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database connection (makes it available project-wide)
        Connection connection = Connection.getInstance();
        
        // Load the Login View (FXML)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/features/login/login.fxml"));
        Parent root = loader.load();
        
        // Set up the stage
        primaryStage.setTitle("Elite Finds UK - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(event -> {
            // Close database connection on application shutdown
            Connection.getInstance().close();
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

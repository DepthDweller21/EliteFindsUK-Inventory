package features.welcome;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for the welcome feature
 * Holds the application state and business logic
 */
public class WelcomeModel {
    private StringProperty message;
    private int clickCount;
    
    public WelcomeModel() {
        this.message = new SimpleStringProperty("");
        this.clickCount = 0;
    }
    
    /**
     * Get the message property for binding to UI
     */
    public StringProperty messageProperty() {
        return message;
    }
    
    /**
     * Get the current message
     */
    public String getMessage() {
        return message.get();
    }
    
    /**
     * Set the message
     */
    public void setMessage(String message) {
        this.message.set(message);
    }
    
    /**
     * Get the click count
     */
    public int getClickCount() {
        return clickCount;
    }
    
    /**
     * Handle button click - business logic goes here
     */
    public void handleButtonClick() {
        clickCount++;
        setMessage("Button was clicked " + clickCount + " time(s)! 🎉");
    }
    
    /**
     * Reset the model state
     */
    public void reset() {
        clickCount = 0;
        setMessage("");
    }
}


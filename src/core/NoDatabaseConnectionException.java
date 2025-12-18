package core;

/**
 * Exception thrown when database connection is required but not available
 */
public class NoDatabaseConnectionException extends RuntimeException {
    public NoDatabaseConnectionException() {
        super("Database connection is not configured. Please set the connection string in Settings.");
    }
    
    public NoDatabaseConnectionException(String message) {
        super(message);
    }
}


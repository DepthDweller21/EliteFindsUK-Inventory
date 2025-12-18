package core;

import features.logsPage.Log;
import features.logsPage.LogModel;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility service for logging system activities
 * Provides easy-to-use static methods for logging actions
 */
public class LoggingService {
    private static LogModel logModel;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId PKT_ZONE = ZoneId.of("Asia/Karachi"); // Pakistan (UTC+5, no DST)
    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London"); // UK (GMT/BST, UTC+0/+1, automatic DST)
    
    /**
     * Initialize the logging service with a LogModel
     * Should be called once at application startup
     */
    public static void initialize(LogModel model) {
        logModel = model;
    }
    
    /**
     * Log a system action
     * @param actionType Action type: "Added", "Edited", or "Deleted"
     * @param module Module name: "Stock" or "Revenue"
     * @param entityType Entity type: "Product" or "Sale"
     * @param entityIdentifier Entity identifier: SKU or Transaction ID
     * @param details Brief description of the action
     */
    public static void log(String actionType, String module, String entityType,
                           String entityIdentifier, String details) {
        // Initialize logModel if not already set
        if (logModel == null) {
            logModel = new LogModel();
        }
        
        try {
            // Get current time in UTC
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            
            // Convert to Pakistan time (PKT, UTC+5)
            ZonedDateTime pktTime = now.withZoneSameInstant(PKT_ZONE);
            String timestampPkt = pktTime.format(TIMESTAMP_FORMATTER);
            
            // Convert to UK time (GMT/BST, automatic DST)
            ZonedDateTime ukTime = now.withZoneSameInstant(UK_ZONE);
            String timestampGmt = ukTime.format(TIMESTAMP_FORMATTER);
            
            // ISO format timestamp for sorting
            String timestamp = now.format(DateTimeFormatter.ISO_INSTANT);
            
            // Create log entry
            Log log = new Log(
                actionType,
                module,
                entityType,
                entityIdentifier,
                details,
                timestamp,
                timestampPkt,
                timestampGmt
            );
            
            // Save to database
            logModel.addLog(log);
        } catch (Exception e) {
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


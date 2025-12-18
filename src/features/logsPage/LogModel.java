package features.logsPage;

import core.Connection;
import core.NoDatabaseConnectionException;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Model for the logs feature
 * Handles database operations for log entries
 */
public class LogModel {
    private Datastore datastore;
    private ObservableList<Log> logs;
    
    public LogModel() {
        // Check if database is connected
        if (!Connection.getInstance().isConnected()) {
            throw new NoDatabaseConnectionException();
        }
        
        // Get datastore from Connection singleton
        datastore = Connection.getInstance().getDatastore();
        
        // Register Log entity with Morphia
        Connection.getInstance().mapEntity(Log.class);
        
        // Initialize observable list for logs
        logs = FXCollections.observableArrayList();
        
        // Load initial logs
        loadLogs();
    }
    
    /**
     * Load all logs from MongoDB, sorted by timestamp descending (most recent first)
     */
    public void loadLogs() {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            List<Log> logList = datastore.find(Log.class)
                    .iterator()
                    .toList();
            // Sort by timestamp descending (most recent first)
            logList.sort((a, b) -> {
                if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
                if (a.getTimestamp() == null) return 1;
                if (b.getTimestamp() == null) return -1;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });
            logs.clear();
            logs.addAll(logList);
        } catch (Exception e) {
            System.err.println("Error loading logs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add a new log entry to the database
     * @param log The log to add
     * @return true if successful, false otherwise
     */
    public boolean addLog(Log log) {
        if (datastore == null) {
            // Silently fail for logging - don't throw exception
            return false;
        }
        try {
            datastore.save(log);
            // Reload to update observable list (add to beginning for most recent first)
            logs.add(0, log);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding log: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Search logs by action type, module, entity type, or details
     * @param query The search query string
     * @return List of matching logs
     */
    public ObservableList<Log> searchLogs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return logs;
        }
        
        String searchTerm = query.trim().toLowerCase();
        ObservableList<Log> results = FXCollections.observableArrayList();
        
        for (Log log : logs) {
            if ((log.getActionType() != null && log.getActionType().toLowerCase().contains(searchTerm)) ||
                (log.getModule() != null && log.getModule().toLowerCase().contains(searchTerm)) ||
                (log.getEntityType() != null && log.getEntityType().toLowerCase().contains(searchTerm)) ||
                (log.getDetails() != null && log.getDetails().toLowerCase().contains(searchTerm)) ||
                (log.getEntityIdentifier() != null && log.getEntityIdentifier().toLowerCase().contains(searchTerm))) {
                results.add(log);
            }
        }
        
        return results;
    }
    
    /**
     * Filter logs by date range
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return Filtered list of logs
     */
    public ObservableList<Log> filterByDateRange(LocalDate from, LocalDate to) {
        ObservableList<Log> results = FXCollections.observableArrayList();
        
        if (from == null && to == null) {
            return logs;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Log log : logs) {
            if (log.getTimestampPkt() != null && !log.getTimestampPkt().isEmpty()) {
                try {
                    // Extract date part from timestamp (format: yyyy-MM-dd HH:mm:ss)
                    String datePart = log.getTimestampPkt().substring(0, 10);
                    LocalDate logDate = LocalDate.parse(datePart);
                    
                    boolean matches = true;
                    if (from != null && logDate.isBefore(from)) {
                        matches = false;
                    }
                    if (to != null && logDate.isAfter(to)) {
                        matches = false;
                    }
                    
                    if (matches) {
                        results.add(log);
                    }
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        }
        
        return results;
    }
    
    /**
     * Filter logs by module
     * @param module The module to filter by ("Stock" or "Revenue")
     * @return Filtered list of logs
     */
    public ObservableList<Log> filterByModule(String module) {
        if (module == null || module.trim().isEmpty()) {
            return logs;
        }
        
        ObservableList<Log> results = FXCollections.observableArrayList();
        String searchModule = module.trim();
        
        for (Log log : logs) {
            if (log.getModule() != null && log.getModule().equals(searchModule)) {
                results.add(log);
            }
        }
        
        return results;
    }
    
    /**
     * Filter logs by action type
     * @param actionType The action type to filter by ("Added", "Edited", "Deleted")
     * @return Filtered list of logs
     */
    public ObservableList<Log> filterByActionType(String actionType) {
        if (actionType == null || actionType.trim().isEmpty()) {
            return logs;
        }
        
        ObservableList<Log> results = FXCollections.observableArrayList();
        String searchActionType = actionType.trim();
        
        for (Log log : logs) {
            if (log.getActionType() != null && log.getActionType().equals(searchActionType)) {
                results.add(log);
            }
        }
        
        return results;
    }
    
    /**
     * Delete logs older than specified days
     * @param daysToKeep Number of days to keep (logs older than this will be deleted)
     * @return Number of logs deleted
     */
    public int deleteOldLogs(int daysToKeep) {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String cutoffDateStr = cutoffDate.format(formatter);
            
            int deletedCount = 0;
            ObservableList<Log> logsToDelete = FXCollections.observableArrayList();
            
            for (Log log : logs) {
                if (log.getTimestampPkt() != null && !log.getTimestampPkt().isEmpty()) {
                    try {
                        String datePart = log.getTimestampPkt().substring(0, 10);
                        if (datePart.compareTo(cutoffDateStr) < 0) {
                            logsToDelete.add(log);
                        }
                    } catch (Exception e) {
                        // Skip invalid dates
                    }
                }
            }
            
            for (Log log : logsToDelete) {
                Query<Log> query = datastore.find(Log.class).filter("_id", log.getId());
                datastore.delete(query);
                deletedCount++;
            }
            
            // Reload logs
            loadLogs();
            
            return deletedCount;
        } catch (Exception e) {
            System.err.println("Error deleting old logs: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get all logs
     * @return ObservableList of all logs
     */
    public ObservableList<Log> getLogs() {
        return logs;
    }
    
    /**
     * Get total number of logs
     * @return Total log count
     */
    public int getTotalLogs() {
        return logs.size();
    }
    
    /**
     * Get number of actions today
     * @return Count of logs from today
     */
    public int getActionsToday() {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        int count = 0;
        for (Log log : logs) {
            if (log.getTimestampPkt() != null && log.getTimestampPkt().startsWith(todayStr)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Get most common action type
     * @return Most frequent action type, or "-" if no logs
     */
    public String getMostCommonAction() {
        if (logs.isEmpty()) {
            return "-";
        }
        
        int addedCount = 0;
        int editedCount = 0;
        int deletedCount = 0;
        
        for (Log log : logs) {
            String actionType = log.getActionType();
            if ("Added".equals(actionType)) {
                addedCount++;
            } else if ("Edited".equals(actionType)) {
                editedCount++;
            } else if ("Deleted".equals(actionType)) {
                deletedCount++;
            }
        }
        
        if (addedCount >= editedCount && addedCount >= deletedCount) {
            return "Added";
        } else if (editedCount >= deletedCount) {
            return "Edited";
        } else {
            return "Deleted";
        }
    }
}


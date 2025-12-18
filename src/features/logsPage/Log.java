package features.logsPage;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

/**
 * Log entity class for MongoDB
 * Represents a system activity log entry
 */
@Entity("logs")
public class Log {
    @Id
    private ObjectId id;
    
    @Property("actionType")
    private String actionType; // "Added", "Edited", "Deleted"
    
    @Property("module")
    private String module; // "Stock" or "Revenue"
    
    @Property("entityType")
    private String entityType; // "Product" or "Sale"
    
    @Property("entityIdentifier")
    private String entityIdentifier; // SKU or Transaction ID
    
    @Property("details")
    private String details; // Brief description
    
    @Property("timestamp")
    private String timestamp; // ISO format timestamp
    
    @Property("timestampPkt")
    private String timestampPkt; // Pakistan time (PKT, UTC+5)
    
    @Property("timestampGmt")
    private String timestampGmt; // UK time (GMT/BST, UTC+0/+1)
    
    /**
     * Default constructor required by Morphia
     */
    public Log() {
    }
    
    /**
     * Constructor with all fields
     */
    public Log(String actionType, String module, String entityType,
               String entityIdentifier, String details, String timestamp,
               String timestampPkt, String timestampGmt) {
        this.actionType = actionType;
        this.module = module;
        this.entityType = entityType;
        this.entityIdentifier = entityIdentifier;
        this.details = details;
        this.timestamp = timestamp;
        this.timestampPkt = timestampPkt;
        this.timestampGmt = timestampGmt;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getEntityIdentifier() {
        return entityIdentifier;
    }
    
    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTimestampPkt() {
        return timestampPkt;
    }
    
    public void setTimestampPkt(String timestampPkt) {
        this.timestampPkt = timestampPkt;
    }
    
    public String getTimestampGmt() {
        return timestampGmt;
    }
    
    public void setTimestampGmt(String timestampGmt) {
        this.timestampGmt = timestampGmt;
    }
    
    @Override
    public String toString() {
        return "Log{" +
                "actionType='" + actionType + '\'' +
                ", module='" + module + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}


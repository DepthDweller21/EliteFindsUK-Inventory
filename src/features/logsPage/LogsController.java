package features.logsPage;

import features.revenue.RevenueController;
import features.revenue.RevenueModel;
import features.stock.StockController;
import features.stock.StockModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class LogsController implements Initializable {
    private LogModel model;
    @FXML private AnchorPane contentArea;
    
    // Buttons
    @FXML private Button refreshBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearOldLogsBtn;
    @FXML private Button searchBtn;
    @FXML private Button clearFilterBtn;
    
    // Search and filter
    @FXML private TextField searchBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private ComboBox<String> filterActionType;
    @FXML private ComboBox<String> filterEntity;
    
    // Table
    @FXML private TableView<Log> logsTable;
    @FXML private TableColumn<Log, String> timestampPktColumn;
    @FXML private TableColumn<Log, String> timestampGmtColumn;
    @FXML private TableColumn<Log, String> actionTypeColumn;
    @FXML private TableColumn<Log, String> moduleColumn;
    @FXML private TableColumn<Log, String> entityTypeColumn;
    @FXML private TableColumn<Log, String> detailsColumn;
    
    // Labels
    @FXML private Label totalLogsLabel;
    @FXML private Label actionsTodayLabel;
    @FXML private Label mostCommonActionLabel;
    
    // Dialogs
    @FXML private AnchorPane clearLogsDialog;
    @FXML private Label clearLogsInfoLabel;
    
    /**
     * Initialize the controller with the model
     */
    public void setModel(LogModel model) {
        this.model = model;
        setupTableView();
        bindModelToView();
        updateStatistics();
        initializeFilterComboBoxes();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up table columns
        setupTableColumns();
        
        // Initialize model if not already set (fallback)
        if (model == null) {
            model = new LogModel();
            setModel(model);
        }
    }
    
    /**
     * Set up table column cell value factories
     */
    private void setupTableColumns() {
        if (timestampPktColumn != null) {
            timestampPktColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimestampPkt()));
        }
        if (timestampGmtColumn != null) {
            timestampGmtColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimestampGmt()));
        }
        if (actionTypeColumn != null) {
            actionTypeColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getActionType()));
        }
        if (moduleColumn != null) {
            moduleColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModule()));
        }
        if (entityTypeColumn != null) {
            entityTypeColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEntityType()));
        }
        if (detailsColumn != null) {
            detailsColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDetails()));
        }
    }
    
    /**
     * Set up TableView and bind to model
     */
    private void setupTableView() {
        if (logsTable != null && model != null) {
            logsTable.setItems(model.getLogs());
        }
    }
    
    /**
     * Bind model data to view elements
     */
    private void bindModelToView() {
        if (model != null && logsTable != null) {
            logsTable.setItems(model.getLogs());
        }
    }
    
    /**
     * Initialize filter combo boxes
     */
    private void initializeFilterComboBoxes() {
        if (filterActionType != null) {
            filterActionType.getItems().addAll("Added", "Edited", "Deleted");
        }
        if (filterEntity != null) {
            filterEntity.getItems().addAll("Stock", "Revenue");
        }
    }
    
    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        if (model == null) return;
        
        if (totalLogsLabel != null) {
            totalLogsLabel.setText(String.valueOf(model.getTotalLogs()));
        }
        
        if (actionsTodayLabel != null) {
            actionsTodayLabel.setText(String.valueOf(model.getActionsToday()));
        }
        
        if (mostCommonActionLabel != null) {
            mostCommonActionLabel.setText(model.getMostCommonAction());
        }
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Button handlers
    @FXML
    private void handleRefresh() {
        if (model != null) {
            model.loadLogs();
            updateStatistics();
            if (logsTable != null) {
                logsTable.refresh();
            }
        }
    }
    
    @FXML
    private void handleExport() {
        if (model == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model not initialized");
            return;
        }
        
        try {
            // Get the stage from any node
            Stage stage = (Stage) (exportBtn != null ? exportBtn.getScene().getWindow() : 
                                  (logsTable != null ? logsTable.getScene().getWindow() : null));
            
            if (stage == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not access window");
                return;
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Logs to CSV");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("logs_export_" + LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + ".csv");
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Write CSV file
                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("Time (PKT),Time (GMT/BST),Action,Module,Entity,Details");
                    
                    // Write data
                    for (Log log : model.getLogs()) {
                        writer.printf("%s,%s,%s,%s,%s,%s%n",
                            escapeCsvField(log.getTimestampPkt() != null ? log.getTimestampPkt() : ""),
                            escapeCsvField(log.getTimestampGmt() != null ? log.getTimestampGmt() : ""),
                            escapeCsvField(log.getActionType() != null ? log.getActionType() : ""),
                            escapeCsvField(log.getModule() != null ? log.getModule() : ""),
                            escapeCsvField(log.getEntityType() != null ? log.getEntityType() : ""),
                            escapeCsvField(log.getDetails() != null ? log.getDetails() : "")
                        );
                    }
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Logs exported successfully to:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", 
                "Failed to export logs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClearOldLogs() {
        if (clearLogsDialog != null) {
            if (clearLogsInfoLabel != null) {
                clearLogsInfoLabel.setText("Delete logs older than 30 days?");
            }
            clearLogsDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleClearOldLogsConfirm() {
        if (model == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model not initialized");
            if (clearLogsDialog != null) {
                clearLogsDialog.setVisible(false);
            }
            return;
        }
        
        try {
            int daysToKeep = 30; // Keep logs from last 30 days
            int deletedCount = model.deleteOldLogs(daysToKeep);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Deleted " + deletedCount + " old log entries");
            updateStatistics();
            if (logsTable != null) {
                logsTable.refresh();
            }
            
            if (clearLogsDialog != null) {
                clearLogsDialog.setVisible(false);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to delete old logs: " + e.getMessage());
            e.printStackTrace();
            if (clearLogsDialog != null) {
                clearLogsDialog.setVisible(false);
            }
        }
    }
    
    @FXML
    private void handleClearOldLogsCancel() {
        if (clearLogsDialog != null) {
            clearLogsDialog.setVisible(false);
        }
    }
    
    @FXML
    private void handleSearch() {
        if (model == null) return;
        
        ObservableList<Log> results = model.getLogs();
        
        // Apply text search
        if (searchBox != null && !searchBox.getText().trim().isEmpty()) {
            results = model.searchLogs(searchBox.getText());
        }
        
        // Apply date filter
        LocalDate fromDate = dateFromPicker != null ? dateFromPicker.getValue() : null;
        LocalDate toDate = dateToPicker != null ? dateToPicker.getValue() : null;
        if (fromDate != null || toDate != null) {
            ObservableList<Log> dateFiltered = model.filterByDateRange(fromDate, toDate);
            // Intersect with search results
            if (searchBox != null && !searchBox.getText().trim().isEmpty()) {
                dateFiltered.retainAll(results);
            }
            results = dateFiltered;
        }
        
        // Apply action type filter
        if (filterActionType != null && filterActionType.getSelectionModel().getSelectedItem() != null) {
            String selectedAction = filterActionType.getSelectionModel().getSelectedItem();
            ObservableList<Log> actionFiltered = model.filterByActionType(selectedAction);
            // Intersect with previous results
            actionFiltered.retainAll(results);
            results = actionFiltered;
        }
        
        // Apply module filter
        if (filterEntity != null && filterEntity.getSelectionModel().getSelectedItem() != null) {
            String selectedModule = filterEntity.getSelectionModel().getSelectedItem();
            ObservableList<Log> moduleFiltered = model.filterByModule(selectedModule);
            // Intersect with previous results
            moduleFiltered.retainAll(results);
            results = moduleFiltered;
        }
        
        // Update table
        if (logsTable != null) {
            logsTable.setItems(results);
        }
        
        // Update statistics for filtered results
        if (totalLogsLabel != null) {
            totalLogsLabel.setText(String.valueOf(results.size()));
        }
    }
    
    @FXML
    private void handleClearFilter() {
        if (searchBox != null) {
            searchBox.clear();
        }
        if (dateFromPicker != null) {
            dateFromPicker.setValue(null);
        }
        if (dateToPicker != null) {
            dateToPicker.setValue(null);
        }
        if (filterActionType != null) {
            filterActionType.getSelectionModel().clearSelection();
        }
        if (filterEntity != null) {
            filterEntity.getSelectionModel().clearSelection();
        }
        // Refresh to show all logs
        handleRefresh();
    }
    
    /**
     * Escape CSV field if it contains commas or quotes
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If field contains comma, quote, or newline, wrap in quotes and escape quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    // Navigation handlers
    @FXML
    private void handleStockButton() {
        navigateToPage("../stock/stock.fxml", "Stock");
    }
    
    @FXML
    private void handleRevenueButton() {
        navigateToPage("../revenue/revenue.fxml", "Revenue");
    }
    
    @FXML
    private void handleSettingsButton() {
        navigateToPage("../settingsPage/settings.fxml", "Settings");
    }
    
    @FXML
    private void handleLogsButton() {
        // Already on logs page, do nothing
    }
    
    @FXML
    private void handleMainMenuButton() {
        navigateToPage("../homepage/homepage.fxml", "Home");
    }
    
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Special handling for stock page - initialize model
            if (fxmlPath.contains("stock.fxml")) {
                StockController controller = loader.getController();
                StockModel stockModel = new StockModel();
                controller.setModel(stockModel);
            }
            // Special handling for revenue page - initialize model
            else if (fxmlPath.contains("revenue.fxml")) {
                RevenueController controller = loader.getController();
                RevenueModel revenueModel = new RevenueModel();
                controller.setModel(revenueModel);
            }
            // Special handling for logs page - initialize model
            else if (fxmlPath.contains("log.fxml")) {
                LogsController controller = loader.getController();
                LogModel logModel = new LogModel();
                controller.setModel(logModel);
            }
            
            Stage stage = (Stage) (contentArea != null ? contentArea.getScene().getWindow() : null);
            if (stage != null) {
                Scene scene = new Scene(root, 1100, 700);
                stage.setScene(scene);
                stage.setTitle(title);
                
                // Force layout recalculation
                Platform.runLater(() -> {
                    stage.sizeToScene();
                    root.requestLayout();
                });
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to " + title + ": " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}


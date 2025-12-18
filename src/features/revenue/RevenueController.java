package features.revenue;

import core.DatabaseAccessHandler;
import core.LoggingService;
import core.ConfigManager;
import core.NoDatabaseConnectionException;
import features.revenue.RevenueModel;
import features.revenue.Sale;
import features.stock.Product;
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
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class RevenueController implements Initializable {
    private RevenueModel model;
    @FXML private AnchorPane contentArea;
    
    // Buttons
    @FXML private Button addSaleBtn;
    @FXML private Button editSaleBtn;
    @FXML private Button deleteSaleBtn;
    @FXML private Button refreshBtn;
    @FXML private Button exportBtn;
    @FXML private Button searchBtn;
    @FXML private Button clearFilterBtn;
    
    // Search and filter
    @FXML private TextField searchBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private ComboBox<String> filterProduct;
    
    // Table
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, String> transactionIdColumn;
    @FXML private TableColumn<Sale, String> skuColumn;
    @FXML private TableColumn<Sale, String> productNameColumn;
    @FXML private TableColumn<Sale, Double> salePriceColumn;
    @FXML private TableColumn<Sale, Double> baseCostGbpColumn;
    @FXML private TableColumn<Sale, Double> shippingColumn;
    @FXML private TableColumn<Sale, Double> platformFeePercentColumn;
    @FXML private TableColumn<Sale, Double> platformFeeAmountColumn;
    @FXML private TableColumn<Sale, Double> netProfitColumn;
    @FXML private TableColumn<Sale, Double> profitMarginColumn;
    
    // Labels
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label totalFeesLabel;
    @FXML private Label avgMarginLabel;
    @FXML private Label totalTransactionsLabel;
    
    // Dialogs
    @FXML private AnchorPane addSaleDialog;
    @FXML private AnchorPane editSaleDialog;
    @FXML private AnchorPane deleteConfirmDialog;
    @FXML private AnchorPane exportConfirmDialog;
    
    // Add sale dialog fields
    @FXML private TextField addTransactionIdField;
    @FXML private TextField addSkuField;
    @FXML private TextField addSalePriceField;
    @FXML private TextField addShippingField;
    @FXML private TextField addPlatformFeeField;
    @FXML private DatePicker addSaleDatePicker;
    @FXML private TextField addNetProfitField;
    
    // Edit sale dialog fields
    @FXML private TextField editTransactionIdField;
    @FXML private TextField editSkuField;
    @FXML private TextField editProductNameField;
    @FXML private TextField editSalePriceField;
    @FXML private TextField editShippingField;
    @FXML private ComboBox<String> editPlatformFeeCombo;
    @FXML private DatePicker editSaleDatePicker;
    @FXML private TextField editNetProfitField;
    
    // Delete dialog
    @FXML private Label deleteSaleInfoLabel;
    
    // Export dialog
    @FXML private Label exportInfoLabel;
    
    /**
     * Initialize the controller with the model
     */
    public void setModel(RevenueModel model) {
        this.model = model;
        setupTableView();
        bindModelToView();
        updateStatistics();
        initializePlatformFeeCombo();
        loadProductsForCombo();
        
        // Set up auto-calculation listeners for add dialog
        if (addSalePriceField != null) {
            addSalePriceField.textProperty().addListener((obs, oldVal, newVal) -> recalculateAddProfit());
        }
        if (addShippingField != null) {
            addShippingField.textProperty().addListener((obs, oldVal, newVal) -> recalculateAddProfit());
        }
        if (addPlatformFeeField != null) {
            addPlatformFeeField.textProperty().addListener((obs, oldVal, newVal) -> recalculateAddProfit());
        }
        if (addSkuField != null) {
            addSkuField.textProperty().addListener((obs, oldVal, newVal) -> recalculateAddProfit());
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This is called after FXML loading
        // Set up table columns (model will be set separately by navigation or setModel)
        setupTableColumns();
        
        // Initialize model if not already set (fallback for direct navigation)
        if (model == null) {
            model = new RevenueModel();
            setModel(model);
        }
    }
    
    /**
     * Set up table column cell value factories
     */
    private void setupTableColumns() {
        if (dateColumn != null) {
            dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSaleDate()));
        }
        if (transactionIdColumn != null) {
            transactionIdColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionId()));
        }
        if (skuColumn != null) {
            skuColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSku()));
        }
        if (productNameColumn != null) {
            productNameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        }
        if (salePriceColumn != null) {
            salePriceColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSalePriceGbp()));
            salePriceColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            });
        }
        if (baseCostGbpColumn != null) {
            baseCostGbpColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBaseCostGbp()));
            baseCostGbpColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            });
        }
        if (shippingColumn != null) {
            shippingColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getShippingGbp()));
            shippingColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            });
        }
        if (platformFeePercentColumn != null) {
            platformFeePercentColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPlatformFeePercent()));
            platformFeePercentColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f%%", item));
                    }
                }
            });
        }
        if (platformFeeAmountColumn != null) {
            platformFeeAmountColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPlatformFeeAmount()));
            platformFeeAmountColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            });
        }
        if (netProfitColumn != null) {
            netProfitColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNetProfitGbp()));
            netProfitColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item));
                    }
                }
            });
        }
        if (profitMarginColumn != null) {
            profitMarginColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getProfitMarginPercent()));
            profitMarginColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f%%", item));
                    }
                }
            });
        }
    }
    
    /**
     * Set up TableView and bind to model
     */
    private void setupTableView() {
        if (salesTable != null && model != null) {
            salesTable.setItems(model.getSales());
        }
    }
    
    /**
     * Bind model data to view elements
     */
    private void bindModelToView() {
        if (model != null && salesTable != null) {
            salesTable.setItems(model.getSales());
        }
    }
    
    /**
     * Initialize platform fee combo boxes with values from config
     */
    private void initializePlatformFeeCombo() {
        if (editPlatformFeeCombo != null) {
            String feesStr = ConfigManager.getPlatformFees();
            String[] fees = feesStr.split(",");
            editPlatformFeeCombo.getItems().clear();
            for (String fee : fees) {
                fee = fee.trim();
                if (!fee.isEmpty()) {
                    editPlatformFeeCombo.getItems().add(fee);
                }
            }
        }
    }
    
    /**
     * Load products from stock for filter combo box
     */
    private void loadProductsForCombo() {
        if (model != null) {
            try {
                ObservableList<String> skus = model.getProductsFromStock();
                
                // Populate filter product combo
                if (filterProduct != null) {
                    filterProduct.setItems(skus);
                }
            } catch (NoDatabaseConnectionException e) {
                // Silently fail - don't show popup during initialization
                if (filterProduct != null) {
                    filterProduct.getItems().clear();
                }
            }
        }
        
        // Set up listeners for edit dialog auto-calculation
        if (editSalePriceField != null) {
            editSalePriceField.textProperty().addListener((obs, oldVal, newVal) -> recalculateEditProfit());
        }
        if (editShippingField != null) {
            editShippingField.textProperty().addListener((obs, oldVal, newVal) -> recalculateEditProfit());
        }
        if (editPlatformFeeCombo != null) {
            editPlatformFeeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> recalculateEditProfit());
        }
    }
    
    /**
     * Recalculate profit for add sale dialog
     */
    private void recalculateAddProfit() {
        if (addSalePriceField == null || addSkuField == null || 
            addShippingField == null || addPlatformFeeField == null || model == null) {
            return;
        }
        
        try {
            double salePrice = Double.parseDouble(addSalePriceField.getText().trim());
            String sku = addSkuField.getText().trim();
            
            if (sku.isEmpty()) {
                if (addNetProfitField != null) {
                    addNetProfitField.clear();
                }
                return;
            }
            
            Product product = model.getProductBySku(sku);
            if (product == null) {
                if (addNetProfitField != null) {
                    addNetProfitField.clear();
                }
                return;
            }
            
            double shipping = addShippingField.getText().trim().isEmpty() ? 0.0 : 
                             Double.parseDouble(addShippingField.getText().trim());
            String feePercentStr = addPlatformFeeField.getText().trim();
            
            if (feePercentStr.isEmpty()) {
                if (addNetProfitField != null) {
                    addNetProfitField.clear();
                }
                return;
            }
            
            double feePercent = Double.parseDouble(feePercentStr);
            double baseCostGbp = RevenueModel.convertPkrToGbp(product.getBaseCostPkr());
            double netProfit = RevenueModel.calculateNetProfit(salePrice, baseCostGbp, shipping, feePercent);
            
            if (addNetProfitField != null) {
                addNetProfitField.setText(String.format("%.2f", netProfit));
            }
        } catch (NumberFormatException e) {
            // Invalid input, don't calculate
            if (addNetProfitField != null) {
                addNetProfitField.clear();
            }
        }
    }
    
    /**
     * Recalculate profit for edit sale dialog
     */
    private void recalculateEditProfit() {
        if (editSalePriceField == null || editSkuField == null || 
            editShippingField == null || editPlatformFeeCombo == null || model == null) {
            return;
        }
        
        try {
            double salePrice = Double.parseDouble(editSalePriceField.getText().trim());
            String sku = editSkuField.getText().trim();
            Product product = model.getProductBySku(sku);
            
            if (product != null) {
                double baseCostGbp = RevenueModel.convertPkrToGbp(product.getBaseCostPkr());
                double shipping = editShippingField.getText().trim().isEmpty() ? 0.0 : 
                                 Double.parseDouble(editShippingField.getText().trim());
                String feePercentStr = editPlatformFeeCombo.getSelectionModel().getSelectedItem();
                
                if (feePercentStr != null) {
                    double feePercent = Double.parseDouble(feePercentStr);
                    double netProfit = RevenueModel.calculateNetProfit(salePrice, baseCostGbp, shipping, feePercent);
                    double margin = RevenueModel.calculateProfitMargin(netProfit, salePrice);
                    
                    if (editNetProfitField != null) {
                        editNetProfitField.setText(String.format("%.2f", netProfit));
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Invalid input, don't calculate
        }
    }
    
    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        if (model == null) return;
        
        if (totalTransactionsLabel != null) {
            totalTransactionsLabel.setText(String.valueOf(model.getSales().size()));
        }
        
        if (totalRevenueLabel != null) {
            double totalRevenue = model.calculateTotalRevenue();
            totalRevenueLabel.setText(String.format("%.2f GBP", totalRevenue));
        }
        
        if (totalProfitLabel != null) {
            double totalProfit = model.calculateTotalProfit();
            totalProfitLabel.setText(String.format("%.2f GBP", totalProfit));
        }
        
        if (totalFeesLabel != null) {
            double totalFees = model.calculateTotalFees();
            totalFeesLabel.setText(String.format("%.2f GBP", totalFees));
        }
        
        if (avgMarginLabel != null) {
            double avgMargin = model.calculateAverageMargin();
            avgMarginLabel.setText(String.format("%.2f%%", avgMargin));
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
    
    // Main action handlers
    @FXML
    private void handleAddSale() {
        // Ensure model is initialized
        if (model == null) {
            model = new RevenueModel();
            setModel(model);
        }
        
        if (addSaleDialog != null) {
            // Generate and set transaction ID
            if (addTransactionIdField != null) {
                int nextId = model.getNextTransactionId();
                addTransactionIdField.setText(String.valueOf(nextId));
            }
            
            // Clear fields
            if (addSkuField != null) addSkuField.clear();
            if (addSalePriceField != null) addSalePriceField.clear();
            if (addShippingField != null) addShippingField.clear();
            if (addPlatformFeeField != null) addPlatformFeeField.clear();
            if (addSaleDatePicker != null) addSaleDatePicker.setValue(LocalDate.now());
            if (addNetProfitField != null) addNetProfitField.clear();
            
            addSaleDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleEditSale() {
        if (salesTable == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Table not initialized");
            return;
        }
        
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a sale to edit");
            return;
        }
        
        // Populate edit dialog fields
        if (editTransactionIdField != null) editTransactionIdField.setText(selectedSale.getTransactionId());
        if (editSkuField != null) editSkuField.setText(selectedSale.getSku());
        if (editProductNameField != null) editProductNameField.setText(selectedSale.getProductName());
        if (editSalePriceField != null) editSalePriceField.setText(String.valueOf(selectedSale.getSalePriceGbp()));
        if (editShippingField != null) editShippingField.setText(String.valueOf(selectedSale.getShippingGbp()));
        if (editPlatformFeeCombo != null) {
            editPlatformFeeCombo.getSelectionModel().select(String.valueOf((int)selectedSale.getPlatformFeePercent()));
        }
        if (editSaleDatePicker != null && selectedSale.getSaleDate() != null) {
            try {
                editSaleDatePicker.setValue(LocalDate.parse(selectedSale.getSaleDate()));
            } catch (Exception e) {
                editSaleDatePicker.setValue(LocalDate.now());
            }
        }
        if (editNetProfitField != null) editNetProfitField.setText(String.format("%.2f", selectedSale.getNetProfitGbp()));
        
        if (editSaleDialog != null) {
            editSaleDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleDeleteSale() {
        if (salesTable == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Table not initialized");
            return;
        }
        
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a sale to delete");
            return;
        }
        
        // Show sale info in confirmation dialog
        if (deleteSaleInfoLabel != null) {
            deleteSaleInfoLabel.setText("Are you sure you want to delete sale: " + 
                selectedSale.getTransactionId() + " (" + selectedSale.getProductName() + ")?");
        }
        
        if (deleteConfirmDialog != null) {
            deleteConfirmDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleRefresh() {
        if (model != null) {
            model.loadSales();
            updateStatistics();
            if (salesTable != null) {
                salesTable.refresh();
            }
        }
    }
    
    @FXML
    private void handleExport() {
        if (model == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model not initialized");
            return;
        }
        
        // Update export info label
        if (exportInfoLabel != null) {
            int saleCount = model.getSales().size();
            exportInfoLabel.setText("Total sales: " + saleCount);
        }
        
        if (exportConfirmDialog != null) {
            exportConfirmDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleSearch() {
        if (model == null) return;
        
        ObservableList<Sale> results = model.getSales();
        
        // Apply text search
        if (searchBox != null && !searchBox.getText().trim().isEmpty()) {
            results = model.searchSales(searchBox.getText());
        }
        
        // Apply date filter
        LocalDate fromDate = dateFromPicker != null ? dateFromPicker.getValue() : null;
        LocalDate toDate = dateToPicker != null ? dateToPicker.getValue() : null;
        if (fromDate != null || toDate != null) {
            ObservableList<Sale> dateFiltered = model.filterByDateRange(fromDate, toDate);
            // Intersect with search results
            if (searchBox != null && !searchBox.getText().trim().isEmpty()) {
                dateFiltered.retainAll(results);
            }
            results = dateFiltered;
        }
        
        // Apply product filter
        if (filterProduct != null && filterProduct.getSelectionModel().getSelectedItem() != null) {
            String selectedSku = filterProduct.getSelectionModel().getSelectedItem();
            ObservableList<Sale> productFiltered = model.filterByProduct(selectedSku);
            // Intersect with previous results
            productFiltered.retainAll(results);
            results = productFiltered;
        }
        
        // Update table
        if (salesTable != null) {
            salesTable.setItems(results);
        }
        
        // Update statistics for filtered results
        if (totalTransactionsLabel != null) {
            totalTransactionsLabel.setText(String.valueOf(results.size()));
        }
        if (totalRevenueLabel != null) {
            double totalRevenue = model.calculateTotalRevenue(results);
            totalRevenueLabel.setText(String.format("%.2f GBP", totalRevenue));
        }
        if (totalProfitLabel != null) {
            double totalProfit = model.calculateTotalProfit(results);
            totalProfitLabel.setText(String.format("%.2f GBP", totalProfit));
        }
        if (totalFeesLabel != null) {
            double totalFees = model.calculateTotalFees(results);
            totalFeesLabel.setText(String.format("%.2f GBP", totalFees));
        }
        if (avgMarginLabel != null) {
            double avgMargin = model.calculateAverageMargin(results);
            avgMarginLabel.setText(String.format("%.2f%%", avgMargin));
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
        if (filterProduct != null) {
            filterProduct.getSelectionModel().clearSelection();
        }
        // Refresh to show all sales
        handleRefresh();
    }
    
    // Add sale dialog handlers
    @FXML
    private void handleAddSaleConfirm() {
        if (model == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model not initialized");
            return;
        }
        
        // Validate required fields
        if (addTransactionIdField == null || addTransactionIdField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Transaction ID is required");
            return;
        }
        
        if (addSkuField == null || addSkuField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "SKU is required");
            return;
        }
        
        if (addSalePriceField == null || addSalePriceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Price (GBP) is required");
            return;
        }
        
        if (addPlatformFeeField == null || addPlatformFeeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Platform Fee % is required");
            return;
        }
        
        if (addSaleDatePicker == null || addSaleDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Date is required");
            return;
        }
        
        // Parse values
        String transactionId = addTransactionIdField.getText().trim();
        String sku = addSkuField.getText().trim();
        Product product;
        try {
            product = model.getProductBySku(sku);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (product == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Product not found for SKU: " + sku);
            return;
        }
        
        double salePrice;
        try {
            salePrice = Double.parseDouble(addSalePriceField.getText().trim());
            if (salePrice < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Price must be a positive number");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Price must be a valid number");
            return;
        }
        
        double shipping = 0.0;
        if (addShippingField != null && !addShippingField.getText().trim().isEmpty()) {
            try {
                shipping = Double.parseDouble(addShippingField.getText().trim());
                if (shipping < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Shipping must be a non-negative number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Shipping must be a valid number");
                return;
            }
        }
        
        double feePercent;
        try {
            feePercent = Double.parseDouble(addPlatformFeeField.getText().trim());
            if (feePercent < 0 || feePercent > 100) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Platform Fee % must be between 0 and 100");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Platform Fee % must be a valid number");
            return;
        }
        double baseCostGbp = RevenueModel.convertPkrToGbp(product.getBaseCostPkr());
        double platformFeeAmount = salePrice * (feePercent / 100.0);
        double netProfit = RevenueModel.calculateNetProfit(salePrice, baseCostGbp, shipping, feePercent);
        double profitMargin = RevenueModel.calculateProfitMargin(netProfit, salePrice);
        
        LocalDate saleDate = addSaleDatePicker.getValue();
        String saleDateStr = saleDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Create new sale
        Sale sale = new Sale(
            transactionId,
            sku,
            product.getName(),
            product.getBaseCostPkr(),
            baseCostGbp,
            salePrice,
            shipping,
            feePercent,
            platformFeeAmount,
            netProfit,
            profitMargin,
            saleDateStr
        );
        
        // Add sale to database
        boolean success;
        try {
            success = model.addSale(sale);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (success) {
            // Log the action
            LoggingService.log("Added", "Revenue", "Sale", transactionId, 
                "Sale Transaction ID " + transactionId);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sale added successfully");
            updateStatistics();
            if (addSaleDialog != null) {
                addSaleDialog.setVisible(false);
            }
            // Clear fields (transaction ID will be regenerated on next open)
            if (addSkuField != null) addSkuField.clear();
            if (addSalePriceField != null) addSalePriceField.clear();
            if (addShippingField != null) addShippingField.clear();
            if (addPlatformFeeField != null) addPlatformFeeField.clear();
            if (addSaleDatePicker != null) addSaleDatePicker.setValue(null);
            if (addNetProfitField != null) addNetProfitField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add sale. Transaction ID may already exist.");
        }
    }
    
    @FXML
    private void handleAddSaleCancel() {
        if (addSaleDialog != null) {
            addSaleDialog.setVisible(false);
        }
    }
    
    // Edit sale dialog handlers
    @FXML
    private void handleEditSaleConfirm() {
        if (model == null || salesTable == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model or table not initialized");
            return;
        }
        
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a sale to edit");
            return;
        }
        
        // Validate required fields
        if (editSalePriceField == null || editSalePriceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Price (GBP) is required");
            return;
        }
        
        if (editPlatformFeeCombo == null || editPlatformFeeCombo.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Platform Fee % is required");
            return;
        }
        
        if (editSaleDatePicker == null || editSaleDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Date is required");
            return;
        }
        
        // Parse values
        double salePrice;
        try {
            salePrice = Double.parseDouble(editSalePriceField.getText().trim());
            if (salePrice < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Price must be a positive number");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Sale Price must be a valid number");
            return;
        }
        
        double shipping = 0.0;
        if (editShippingField != null && !editShippingField.getText().trim().isEmpty()) {
            try {
                shipping = Double.parseDouble(editShippingField.getText().trim());
                if (shipping < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Shipping must be a non-negative number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Shipping must be a valid number");
                return;
            }
        }
        
        double feePercent = Double.parseDouble(editPlatformFeeCombo.getSelectionModel().getSelectedItem());
        Product product = model.getProductBySku(selectedSale.getSku());
        
        if (product == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Product not found for SKU");
            return;
        }
        
        double baseCostGbp = RevenueModel.convertPkrToGbp(product.getBaseCostPkr());
        double platformFeeAmount = salePrice * (feePercent / 100.0);
        double netProfit = RevenueModel.calculateNetProfit(salePrice, baseCostGbp, shipping, feePercent);
        double profitMargin = RevenueModel.calculateProfitMargin(netProfit, salePrice);
        
        LocalDate saleDate = editSaleDatePicker.getValue();
        String saleDateStr = saleDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Update sale fields
        selectedSale.setSalePriceGbp(salePrice);
        selectedSale.setShippingGbp(shipping);
        selectedSale.setPlatformFeePercent(feePercent);
        selectedSale.setPlatformFeeAmount(platformFeeAmount);
        selectedSale.setNetProfitGbp(netProfit);
        selectedSale.setProfitMarginPercent(profitMargin);
        selectedSale.setSaleDate(saleDateStr);
        selectedSale.setBaseCostGbp(baseCostGbp);
        
        // Update sale in database
        boolean success;
        try {
            success = model.updateSale(selectedSale);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (success) {
            // Log the action
            LoggingService.log("Edited", "Revenue", "Sale", selectedSale.getTransactionId(), 
                "Sale Transaction ID " + selectedSale.getTransactionId());
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sale updated successfully");
            updateStatistics();
            if (salesTable != null) {
                salesTable.refresh();
            }
            if (editSaleDialog != null) {
                editSaleDialog.setVisible(false);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update sale");
        }
    }
    
    @FXML
    private void handleEditSaleCancel() {
        if (editSaleDialog != null) {
            editSaleDialog.setVisible(false);
        }
    }
    
    // Delete confirmation handlers
    @FXML
    private void handleDeleteConfirm() {
        if (model == null || salesTable == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model or table not initialized");
            if (deleteConfirmDialog != null) {
                deleteConfirmDialog.setVisible(false);
            }
            return;
        }
        
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No sale selected");
            if (deleteConfirmDialog != null) {
                deleteConfirmDialog.setVisible(false);
            }
            return;
        }
        
        String transactionId = selectedSale.getTransactionId();
        boolean success;
        try {
            success = model.deleteSale(transactionId);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (success) {
            // Log the action
            LoggingService.log("Deleted", "Revenue", "Sale", transactionId, 
                "Sale Transaction ID " + transactionId);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sale deleted successfully");
            updateStatistics();
            if (deleteConfirmDialog != null) {
                deleteConfirmDialog.setVisible(false);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sale");
        }
    }
    
    @FXML
    private void handleDeleteCancel() {
        if (deleteConfirmDialog != null) {
            deleteConfirmDialog.setVisible(false);
        }
    }
    
    // Export handlers
    @FXML
    private void handleExportConfirm() {
        if (model == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model not initialized");
            if (exportConfirmDialog != null) {
                exportConfirmDialog.setVisible(false);
            }
            return;
        }
        
        try {
            // Get the stage from any node
            Stage stage = (Stage) (exportBtn != null ? exportBtn.getScene().getWindow() : 
                                  (salesTable != null ? salesTable.getScene().getWindow() : null));
            
            if (stage == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not access window");
                if (exportConfirmDialog != null) {
                    exportConfirmDialog.setVisible(false);
                }
                return;
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Revenue Data to CSV");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("revenue_export_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv");
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Write CSV file
                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("Date,Transaction ID,SKU,Product Name,Sale Price (GBP),Base Cost (GBP),Shipping,Fee %,Fee Amount,Net Profit (GBP),Margin %");
                    
                    // Write data
                    for (Sale sale : model.getSales()) {
                        writer.printf("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.1f,%.2f,%.2f,%.2f%n",
                            escapeCsvField(sale.getSaleDate() != null ? sale.getSaleDate() : ""),
                            escapeCsvField(sale.getTransactionId() != null ? sale.getTransactionId() : ""),
                            escapeCsvField(sale.getSku() != null ? sale.getSku() : ""),
                            escapeCsvField(sale.getProductName() != null ? sale.getProductName() : ""),
                            sale.getSalePriceGbp(),
                            sale.getBaseCostGbp(),
                            sale.getShippingGbp(),
                            sale.getPlatformFeePercent(),
                            sale.getPlatformFeeAmount(),
                            sale.getNetProfitGbp(),
                            sale.getProfitMarginPercent()
                        );
                    }
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Revenue data exported successfully to:\n" + file.getAbsolutePath());
            }
            
            if (exportConfirmDialog != null) {
                exportConfirmDialog.setVisible(false);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", 
                "Failed to export data: " + e.getMessage());
            e.printStackTrace();
            if (exportConfirmDialog != null) {
                exportConfirmDialog.setVisible(false);
            }
        }
    }
    
    @FXML
    private void handleExportCancel() {
        if (exportConfirmDialog != null) {
            exportConfirmDialog.setVisible(false);
        }
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
        // Already on revenue page, do nothing
    }
    
    @FXML
    private void handleSettingsButton() {
        navigateToPage("../settingsPage/settings.fxml", "Settings");
    }
    
    @FXML
    private void handleLogsButton() {
        navigateToPage("../logsPage/log.fxml", "Logs");
    }
    
    @FXML
    private void handleMainMenuButton() {
        navigateToPage("../homepage/homepage.fxml", "Home");
    }
    
    /**
     * Get the current stage
     */
    private Stage getStage() {
        if (contentArea != null && contentArea.getScene() != null) {
            return (Stage) contentArea.getScene().getWindow();
        }
        return null;
    }
    
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Special handling for stock page - initialize model
            if (fxmlPath.contains("stock.fxml")) {
                StockController controller = loader.getController();
                try {
                    StockModel stockModel = new StockModel();
                    controller.setModel(stockModel);
                } catch (NoDatabaseConnectionException e) {
                    DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            // Special handling for revenue page - initialize model
            else if (fxmlPath.contains("revenue.fxml")) {
                RevenueController controller = loader.getController();
                try {
                    RevenueModel revenueModel = new RevenueModel();
                    controller.setModel(revenueModel);
                } catch (NoDatabaseConnectionException e) {
                    DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            // Special handling for logs page - initialize model
            else if (fxmlPath.contains("log.fxml")) {
                features.logsPage.LogsController controller = loader.getController();
                try {
                    features.logsPage.LogModel logModel = new features.logsPage.LogModel();
                    controller.setModel(logModel);
                } catch (NoDatabaseConnectionException e) {
                    DatabaseAccessHandler.showNoConnectionAlert(getStage());
                    return;
                }
            }
            
            Stage stage = getStage();
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


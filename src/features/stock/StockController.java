package features.stock;

import core.DatabaseAccessHandler;
import core.LoggingService;
import core.NoDatabaseConnectionException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the stock feature
 * Handles user input and updates the view based on model changes
 */
public class StockController implements Initializable {
    private StockModel model;
    
    // Main UI elements
    @FXML private VBox sidebar;
    @FXML private AnchorPane contentArea;
    
    // Buttons
    @FXML private Button addProductBtn;
    @FXML private Button editProductBtn;
    @FXML private Button deleteProductBtn;
    @FXML private Button exportBtn;
    @FXML private Button searchBtn;
    @FXML private Button clearFilterBtn;
    
    // Search and filter
    @FXML private TextField searchBox;
    
    // Table
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> sizeColumn;
    @FXML private TableColumn<Product, String> colorColumn;
    @FXML private TableColumn<Product, String> materialColumn;
    @FXML private TableColumn<Product, String> brandColumn;
    @FXML private TableColumn<Product, Double> baseCostColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Integer> quantitySoldColumn;
    @FXML private TableColumn<Product, String> dateColumn;
    
    // Labels
    @FXML private Label totalProductsLabel;
    @FXML private Label selectedProductLabel;
    @FXML private Label totalValuePkrLabel;
    @FXML private Label totalValueGbpLabel;
    
    // Dialogs
    @FXML private AnchorPane addProductDialog;
    @FXML private AnchorPane editProductDialog;
    @FXML private AnchorPane deleteConfirmDialog;
    @FXML private AnchorPane exportConfirmDialog;
    
    // Add product dialog fields
    @FXML private TextField addSkuField;
    @FXML private TextField addNameField;
    @FXML private TextField addSizeField;
    @FXML private TextField addColorField;
    @FXML private TextField addMaterialField;
    @FXML private TextField addBrandField;
    @FXML private TextField addBaseCostField;
    @FXML private TextField addQuantityField;
    @FXML private TextField addQuantitySoldField;
    
    // Edit product dialog fields
    @FXML private TextField editSkuField;
    @FXML private TextField editNameField;
    @FXML private TextField editSizeField;
    @FXML private TextField editColorField;
    @FXML private TextField editMaterialField;
    @FXML private TextField editBrandField;
    @FXML private TextField editBaseCostField;
    @FXML private TextField editQuantityField;
    @FXML private TextField editQuantitySoldField;
    
    // Delete dialog
    @FXML private Label deleteProductNameLabel;
    
    // Export dialog
    @FXML private Label exportInfoLabel;
    
    /**
     * Initialize the controller with the model
     */
    public void setModel(StockModel model) {
        this.model = model;
        setupTableView();
        bindModelToView();
        updateStatistics();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This is called after FXML loading
        // Model will be set separately by the main application
        setupTableColumns();
    }
    
    /**
     * Set up table column cell value factories
     */
    private void setupTableColumns() {
        if (skuColumn != null) {
            skuColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSku()));
        }
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        if (sizeColumn != null) {
            sizeColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSize()));
        }
        if (colorColumn != null) {
            colorColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getColor()));
        }
        if (materialColumn != null) {
            materialColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMaterial()));
        }
        if (brandColumn != null) {
            brandColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBrand()));
        }
        if (baseCostColumn != null) {
            baseCostColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBaseCostPkr()));
            baseCostColumn.setCellFactory(column -> new TableCell<Product, Double>() {
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
        if (quantityColumn != null) {
            quantityColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        }
        if (quantitySoldColumn != null) {
            quantitySoldColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantitySold()));
        }
        if (dateColumn != null) {
            dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateAdded()));
        }
    }
    
    /**
     * Set up TableView and bind to model
     */
    private void setupTableView() {
        if (productsTable != null && model != null) {
            productsTable.setItems(model.getProducts());
            
            // Set up selection listener
            productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    if (selectedProductLabel != null) {
                        selectedProductLabel.setText(newSelection.getName() + " (" + newSelection.getSku() + ")");
                    }
                } else {
                    if (selectedProductLabel != null) {
                        selectedProductLabel.setText("None");
                    }
                }
            });
        }
    }
    
    /**
     * Bind model data to view elements
     */
    private void bindModelToView() {
        if (model != null && productsTable != null) {
            productsTable.setItems(model.getProducts());
        }
    }
    
    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        if (model == null) return;
        
        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.valueOf(model.getProducts().size()));
        }
        
        if (totalValuePkrLabel != null) {
            double totalPkr = model.calculateTotalValuePkr();
            totalValuePkrLabel.setText(String.format("%.2f", totalPkr));
        }
        
        if (totalValueGbpLabel != null) {
            double totalGbp = model.calculateTotalValueGbp();
            totalValueGbpLabel.setText(String.format("%.2f", totalGbp));
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
    
    /**
     * Clean up when the scene is closed
     */
    public void cleanup() {
        // Add cleanup logic here if needed
    }
    
    // Event handlers for main actions
    @FXML
    private void handleAddProduct() {
        if (addProductDialog != null) {
            addProductDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleEditProduct() {
        if (productsTable == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Table not initialized");
            return;
        }
        
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit");
            return;
        }
        
        // Populate edit dialog fields
        if (editSkuField != null) editSkuField.setText(selectedProduct.getSku());
        if (editNameField != null) editNameField.setText(selectedProduct.getName());
        if (editSizeField != null) editSizeField.setText(selectedProduct.getSize() != null ? selectedProduct.getSize() : "");
        if (editColorField != null) editColorField.setText(selectedProduct.getColor() != null ? selectedProduct.getColor() : "");
        if (editMaterialField != null) editMaterialField.setText(selectedProduct.getMaterial() != null ? selectedProduct.getMaterial() : "");
        if (editBrandField != null) editBrandField.setText(selectedProduct.getBrand() != null ? selectedProduct.getBrand() : "");
        if (editBaseCostField != null) editBaseCostField.setText(String.valueOf(selectedProduct.getBaseCostPkr()));
        if (editQuantityField != null) editQuantityField.setText(String.valueOf(selectedProduct.getQuantity()));
        if (editQuantitySoldField != null) editQuantitySoldField.setText(String.valueOf(selectedProduct.getQuantitySold()));
        
        if (editProductDialog != null) {
            editProductDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleDeleteProduct() {
        if (productsTable == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Table not initialized");
            return;
        }
        
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete");
            return;
        }
        
        // Show product name in confirmation dialog
        if (deleteProductNameLabel != null) {
            deleteProductNameLabel.setText("Are you sure you want to delete: " + selectedProduct.getName() + " (" + selectedProduct.getSku() + ")?");
        }
        
        if (deleteConfirmDialog != null) {
            deleteConfirmDialog.setVisible(true);
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
            int productCount = model.getProducts().size();
            exportInfoLabel.setText("Total products: " + productCount);
        }
        
        if (exportConfirmDialog != null) {
            exportConfirmDialog.setVisible(true);
        }
    }
    
    @FXML
    private void handleSearch() {
        // Handle search functionality
        if (model == null || searchBox == null) return;
        
        String searchText = searchBox.getText();
        ObservableList<Product> searchResults = model.searchProducts(searchText);
        
        if (productsTable != null) {
            productsTable.setItems(searchResults);
        }
        
        // Update statistics for filtered results
        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.valueOf(searchResults.size()));
        }
        
        if (totalValuePkrLabel != null) {
            double totalPkr = model.calculateTotalValuePkr(searchResults);
            totalValuePkrLabel.setText(String.format("%.2f", totalPkr));
        }
        
        if (totalValueGbpLabel != null) {
            double totalGbp = model.calculateTotalValueGbp(searchResults);
            totalValueGbpLabel.setText(String.format("%.2f", totalGbp));
        }
    }
    
    @FXML
    private void handleClearFilter() {
        // Clear search box
        if (searchBox != null) {
            searchBox.clear();
        }
        
        // Refresh to show all products
        if (model != null) {
            model.loadProducts();
            updateStatistics();
            if (productsTable != null) {
                productsTable.refresh();
            }
        }
    }
    
    // Add product dialog handlers
    @FXML
    private void handleAddProductConfirm() {
        if (model == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model not initialized");
            return;
        }
        
        // Validate required fields
        if (addSkuField == null || addSkuField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "SKU is required");
            return;
        }
        
        if (addNameField == null || addNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Product Name is required");
            return;
        }
        
        if (addBaseCostField == null || addBaseCostField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Base Cost (PKR) is required");
            return;
        }
        
        // Parse base cost
        double baseCost;
        try {
            baseCost = Double.parseDouble(addBaseCostField.getText().trim());
            if (baseCost < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Base Cost must be a positive number");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Base Cost must be a valid number");
            return;
        }
        
        // Parse quantity
        int quantity = 0;
        if (addQuantityField != null && !addQuantityField.getText().trim().isEmpty()) {
            try {
                quantity = Integer.parseInt(addQuantityField.getText().trim());
                if (quantity < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity must be a non-negative number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity must be a valid number");
                return;
            }
        }
        
        // Parse quantity sold
        int quantitySold = 0;
        if (addQuantitySoldField != null && !addQuantitySoldField.getText().trim().isEmpty()) {
            try {
                quantitySold = Integer.parseInt(addQuantitySoldField.getText().trim());
                if (quantitySold < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity Sold must be a non-negative number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity Sold must be a valid number");
                return;
            }
        }
        
        // Create new product
        Product product = new Product(
            addSkuField.getText().trim(),
            addNameField.getText().trim(),
            addSizeField != null ? addSizeField.getText().trim() : "",
            addColorField != null ? addColorField.getText().trim() : "",
            addMaterialField != null ? addMaterialField.getText().trim() : "",
            addBrandField != null ? addBrandField.getText().trim() : "",
            baseCost,
            quantity,
            quantitySold
        );
        
        // Add product to database
        boolean success;
        try {
            success = model.addProduct(product);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (success) {
            // Log the action
            LoggingService.log("Added", "Stock", "Product", addSkuField.getText().trim(), 
                "Product " + addSkuField.getText().trim());
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully");
            updateStatistics();
            if (addProductDialog != null) {
                addProductDialog.setVisible(false);
            }
            // Clear fields
            if (addSkuField != null) addSkuField.clear();
            if (addNameField != null) addNameField.clear();
            if (addSizeField != null) addSizeField.clear();
            if (addColorField != null) addColorField.clear();
            if (addMaterialField != null) addMaterialField.clear();
            if (addBrandField != null) addBrandField.clear();
            if (addBaseCostField != null) addBaseCostField.clear();
            if (addQuantityField != null) addQuantityField.clear();
            if (addQuantitySoldField != null) addQuantitySoldField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product. SKU may already exist.");
        }
    }
    
    @FXML
    private void handleAddProductCancel() {
        if (addProductDialog != null) {
            addProductDialog.setVisible(false);
        }
        // Clear fields
        if (addSkuField != null) addSkuField.clear();
        if (addNameField != null) addNameField.clear();
        if (addSizeField != null) addSizeField.clear();
        if (addColorField != null) addColorField.clear();
        if (addMaterialField != null) addMaterialField.clear();
        if (addBrandField != null) addBrandField.clear();
        if (addBaseCostField != null) addBaseCostField.clear();
        if (addQuantityField != null) addQuantityField.clear();
        if (addQuantitySoldField != null) addQuantitySoldField.clear();
    }
    
    // Edit product dialog handlers
    @FXML
    private void handleEditProductConfirm() {
        if (model == null || productsTable == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model or table not initialized");
            return;
        }
        
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit");
            return;
        }
        
        // Validate required fields
        if (editNameField == null || editNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Product Name is required");
            return;
        }
        
        if (editBaseCostField == null || editBaseCostField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Base Cost (PKR) is required");
            return;
        }
        
        // Parse base cost
        double baseCost;
        try {
            baseCost = Double.parseDouble(editBaseCostField.getText().trim());
            if (baseCost < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Base Cost must be a positive number");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Base Cost must be a valid number");
            return;
        }
        
        // Parse quantity
        int quantity = 0;
        if (editQuantityField != null && !editQuantityField.getText().trim().isEmpty()) {
            try {
                quantity = Integer.parseInt(editQuantityField.getText().trim());
                if (quantity < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity must be a non-negative number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity must be a valid number");
                return;
            }
        }
        
        // Parse quantity sold
        int quantitySold = 0;
        if (editQuantitySoldField != null && !editQuantitySoldField.getText().trim().isEmpty()) {
            try {
                quantitySold = Integer.parseInt(editQuantitySoldField.getText().trim());
                if (quantitySold < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity Sold must be a non-negative number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity Sold must be a valid number");
                return;
            }
        }
        
        // Update product fields
        selectedProduct.setName(editNameField.getText().trim());
        selectedProduct.setSize(editSizeField != null ? editSizeField.getText().trim() : "");
        selectedProduct.setColor(editColorField != null ? editColorField.getText().trim() : "");
        selectedProduct.setMaterial(editMaterialField != null ? editMaterialField.getText().trim() : "");
        selectedProduct.setBrand(editBrandField != null ? editBrandField.getText().trim() : "");
        selectedProduct.setBaseCostPkr(baseCost);
        selectedProduct.setQuantity(quantity);
        selectedProduct.setQuantitySold(quantitySold);
        
        // Update product in database
        boolean success;
        try {
            success = model.updateProduct(selectedProduct);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (success) {
            // Log the action
            LoggingService.log("Edited", "Stock", "Product", selectedProduct.getSku(), 
                "Product " + selectedProduct.getSku());
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully");
            updateStatistics();
            if (productsTable != null) {
                productsTable.refresh();
            }
            if (editProductDialog != null) {
                editProductDialog.setVisible(false);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product");
        }
    }
    
    @FXML
    private void handleEditProductCancel() {
        if (editProductDialog != null) {
            editProductDialog.setVisible(false);
        }
    }
    
    // Delete confirmation handlers
    @FXML
    private void handleDeleteConfirm() {
        if (model == null || productsTable == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Model or table not initialized");
            if (deleteConfirmDialog != null) {
                deleteConfirmDialog.setVisible(false);
            }
            return;
        }
        
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No product selected");
            if (deleteConfirmDialog != null) {
                deleteConfirmDialog.setVisible(false);
            }
            return;
        }
        
        String sku = selectedProduct.getSku();
        boolean success;
        try {
            success = model.deleteProduct(sku);
        } catch (NoDatabaseConnectionException e) {
            DatabaseAccessHandler.showNoConnectionAlert(getStage());
            return;
        }
        
        if (success) {
            // Log the action
            LoggingService.log("Deleted", "Stock", "Product", sku, 
                "Product " + sku);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully");
            updateStatistics();
            if (deleteConfirmDialog != null) {
                deleteConfirmDialog.setVisible(false);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product");
        }
    }
    
    @FXML
    private void handleDeleteCancel() {
        if (deleteConfirmDialog != null) {
            deleteConfirmDialog.setVisible(false);
        }
    }
    
    // Export confirmation handlers
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
                                  (productsTable != null ? productsTable.getScene().getWindow() : null));
            
            if (stage == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not access window");
                if (exportConfirmDialog != null) {
                    exportConfirmDialog.setVisible(false);
                }
                return;
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Stock Data to CSV");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("stock_export_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv");
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Write CSV file
                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("SKU,Name,Size,Color,Material,Brand,Base Cost (PKR),Quantity,Quantity Sold,Date Added");
                    
                    // Write data
                    for (Product product : model.getProducts()) {
                        writer.printf("%s,%s,%s,%s,%s,%s,%.2f,%d,%d,%s%n",
                            escapeCsvField(product.getSku() != null ? product.getSku() : ""),
                            escapeCsvField(product.getName() != null ? product.getName() : ""),
                            escapeCsvField(product.getSize() != null ? product.getSize() : ""),
                            escapeCsvField(product.getColor() != null ? product.getColor() : ""),
                            escapeCsvField(product.getMaterial() != null ? product.getMaterial() : ""),
                            escapeCsvField(product.getBrand() != null ? product.getBrand() : ""),
                            product.getBaseCostPkr(),
                            product.getQuantity(),
                            product.getQuantitySold(),
                            escapeCsvField(product.getDateAdded() != null ? product.getDateAdded() : "")
                        );
                    }
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Stock data exported successfully to:\n" + file.getAbsolutePath());
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
    
    @FXML
    private void handleExportCancel() {
        if (exportConfirmDialog != null) {
            exportConfirmDialog.setVisible(false);
        }
    }
    
    // Navigation handlers
    @FXML
    private void handleStockButton() {
        // Already on stock page, do nothing
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
    
    /**
     * Navigate to a different page
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            // Cleanup current controller if needed
            cleanup();
            
            // Load the new view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Special handling for stock page - initialize model
            if (fxmlPath.contains("stock.fxml")) {
                StockController controller = loader.getController();
                StockModel model = new StockModel();
                controller.setModel(model);
            }
            
            // Get the stage and switch scene
            Stage stage = (Stage) (productsTable != null ? productsTable.getScene().getWindow() : 
                                  (addProductBtn != null ? addProductBtn.getScene().getWindow() : null));
            
            if (stage != null) {
                // Determine scene dimensions based on page
                double width = 1100;
                double height = 700;
                if (fxmlPath.contains("homepage.fxml")) {
                    width = 1100;
                    height = 700;
                }
                
                Scene scene = new Scene(root, width, height);
                stage.setScene(scene);
                stage.setTitle(title);
                
                // Force layout recalculation
                Platform.runLater(() -> {
                    stage.sizeToScene();
                    root.requestLayout();
                });
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Failed to navigate to " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}


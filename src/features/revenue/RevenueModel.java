package features.revenue;

import core.ConfigManager;
import core.Connection;
import core.NoDatabaseConnectionException;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import features.stock.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model for the revenue feature
 * Holds the application state and business logic
 */
public class RevenueModel {
    private Datastore datastore;
    private ObservableList<Sale> sales;
    
    public RevenueModel() {
        // Check if database is connected
        if (!Connection.getInstance().isConnected()) {
            throw new NoDatabaseConnectionException();
        }
        
        // Get datastore from Connection singleton
        datastore = Connection.getInstance().getDatastore();
        
        // Register Sale entity with Morphia
        Connection.getInstance().mapEntity(Sale.class);
        
        // Initialize observable list for sales
        sales = FXCollections.observableArrayList();
        
        // Load initial sales
        loadSales();
    }
    
    /**
     * Load all sales from MongoDB
     */
    public void loadSales() {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            List<Sale> saleList = datastore.find(Sale.class).iterator().toList();
            sales.clear();
            sales.addAll(saleList);
        } catch (Exception e) {
            System.err.println("Error loading sales: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add a new sale to the database
     * @param sale The sale to add
     * @return true if successful, false otherwise
     */
    public boolean addSale(Sale sale) {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            // Check if transaction ID already exists
            Sale existing = datastore.find(Sale.class)
                    .filter("transactionId", sale.getTransactionId())
                    .first();
            
            if (existing != null) {
                return false; // Transaction ID already exists
            }
            
            datastore.save(sale);
            loadSales(); // Reload to update observable list
            return true;
        } catch (Exception e) {
            System.err.println("Error adding sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing sale in the database
     * @param sale The sale to update
     * @return true if successful, false otherwise
     */
    public boolean updateSale(Sale sale) {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            datastore.save(sale);
            loadSales(); // Reload to update observable list
            return true;
        } catch (Exception e) {
            System.err.println("Error updating sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a sale by transaction ID
     * @param transactionId The transaction ID of the sale to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteSale(String transactionId) {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            Query<Sale> query = datastore.find(Sale.class).filter("transactionId", transactionId);
            datastore.delete(query);
            loadSales(); // Reload to update observable list
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Search sales by SKU, product name, or transaction ID
     * @param query The search query string
     * @return List of matching sales
     */
    public ObservableList<Sale> searchSales(String query) {
        if (query == null || query.trim().isEmpty()) {
            return sales;
        }
        
        String searchTerm = query.trim().toLowerCase();
        ObservableList<Sale> results = FXCollections.observableArrayList();
        
        for (Sale sale : sales) {
            if ((sale.getSku() != null && sale.getSku().toLowerCase().contains(searchTerm)) ||
                (sale.getProductName() != null && sale.getProductName().toLowerCase().contains(searchTerm)) ||
                (sale.getTransactionId() != null && sale.getTransactionId().toLowerCase().contains(searchTerm))) {
                results.add(sale);
            }
        }
        
        return results;
    }
    
    /**
     * Filter sales by date range
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return Filtered list of sales
     */
    public ObservableList<Sale> filterByDateRange(LocalDate from, LocalDate to) {
        ObservableList<Sale> results = FXCollections.observableArrayList();
        
        if (from == null && to == null) {
            return sales;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        
        for (Sale sale : sales) {
            if (sale.getSaleDate() != null && !sale.getSaleDate().isEmpty()) {
                try {
                    LocalDate saleDate = LocalDate.parse(sale.getSaleDate(), formatter);
                    
                    boolean matches = true;
                    if (from != null && saleDate.isBefore(from)) {
                        matches = false;
                    }
                    if (to != null && saleDate.isAfter(to)) {
                        matches = false;
                    }
                    
                    if (matches) {
                        results.add(sale);
                    }
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        }
        
        return results;
    }
    
    /**
     * Filter sales by product SKU
     * @param sku The SKU to filter by
     * @return Filtered list of sales
     */
    public ObservableList<Sale> filterByProduct(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return sales;
        }
        
        ObservableList<Sale> results = FXCollections.observableArrayList();
        String searchSku = sku.trim();
        
        for (Sale sale : sales) {
            if (sale.getSku() != null && sale.getSku().equals(searchSku)) {
                results.add(sale);
            }
        }
        
        return results;
    }
    
    /**
     * Get all products from stock for SKU combo box
     * @return List of product SKUs
     */
    public ObservableList<String> getProductsFromStock() {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            List<Product> products = datastore.find(Product.class).iterator().toList();
            return FXCollections.observableArrayList(
                products.stream()
                    .map(Product::getSku)
                    .filter(sku -> sku != null && !sku.isEmpty())
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            System.err.println("Error loading products from stock: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Get product by SKU
     * @param sku The SKU to search for
     * @return Product if found, null otherwise
     */
    public Product getProductBySku(String sku) {
        if (datastore == null) {
            throw new NoDatabaseConnectionException();
        }
        try {
            return datastore.find(Product.class)
                    .filter("sku", sku)
                    .first();
        } catch (Exception e) {
            System.err.println("Error getting product by SKU: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all sales
     * @return ObservableList of all sales
     */
    public ObservableList<Sale> getSales() {
        return sales;
    }
    
    /**
     * Calculate total revenue
     * @return Total revenue in GBP
     */
    public double calculateTotalRevenue() {
        return sales.stream()
                .mapToDouble(Sale::getSalePriceGbp)
                .sum();
    }
    
    /**
     * Calculate total profit
     * @return Total profit in GBP
     */
    public double calculateTotalProfit() {
        return sales.stream()
                .mapToDouble(Sale::getNetProfitGbp)
                .sum();
    }
    
    /**
     * Calculate total fees
     * @return Total fees in GBP
     */
    public double calculateTotalFees() {
        return sales.stream()
                .mapToDouble(Sale::getPlatformFeeAmount)
                .sum();
    }
    
    /**
     * Calculate average margin
     * @return Average margin percentage
     */
    public double calculateAverageMargin() {
        if (sales.isEmpty()) {
            return 0.0;
        }
        
        return sales.stream()
                .mapToDouble(Sale::getProfitMarginPercent)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Calculate totals for a given list of sales
     */
    public double calculateTotalRevenue(ObservableList<Sale> saleList) {
        return saleList.stream()
                .mapToDouble(Sale::getSalePriceGbp)
                .sum();
    }
    
    public double calculateTotalProfit(ObservableList<Sale> saleList) {
        return saleList.stream()
                .mapToDouble(Sale::getNetProfitGbp)
                .sum();
    }
    
    public double calculateTotalFees(ObservableList<Sale> saleList) {
        return saleList.stream()
                .mapToDouble(Sale::getPlatformFeeAmount)
                .sum();
    }
    
    public double calculateAverageMargin(ObservableList<Sale> saleList) {
        if (saleList.isEmpty()) {
            return 0.0;
        }
        
        return saleList.stream()
                .mapToDouble(Sale::getProfitMarginPercent)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Calculate net profit
     * Net Profit = Sale Price - Base Cost (GBP) - Shipping - Platform Fee Amount
     * Platform Fee Amount = Sale Price × (Platform Fee % / 100)
     */
    public static double calculateNetProfit(double salePrice, double baseCostGbp, double shipping, double feePercent) {
        double platformFeeAmount = salePrice * (feePercent / 100.0);
        return salePrice - baseCostGbp - shipping - platformFeeAmount;
    }
    
    /**
     * Calculate profit margin percentage
     * Profit Margin % = (Net Profit / Sale Price) × 100
     */
    public static double calculateProfitMargin(double netProfit, double salePrice) {
        if (salePrice == 0) {
            return 0.0;
        }
        return (netProfit / salePrice) * 100.0;
    }
    
    /**
     * Convert PKR to GBP using exchange rate from config
     */
    public static double convertPkrToGbp(double pkr) {
        double exchangeRate = ConfigManager.getGbpToPkrRate();
        return pkr / exchangeRate;
    }
    
    /**
     * Get the exchange rate from config
     */
    public static double getExchangeRate() {
        return ConfigManager.getGbpToPkrRate();
    }
    
    /**
     * Get the next sequential transaction ID
     * Returns the highest existing transaction ID + 1, or 1 if no sales exist
     * Transaction IDs are stored as strings but represent sequential numbers
     */
    public int getNextTransactionId() {
        try {
            int maxId = 0;
            for (Sale sale : sales) {
                if (sale.getTransactionId() != null && !sale.getTransactionId().isEmpty()) {
                    try {
                        int id = Integer.parseInt(sale.getTransactionId());
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        // Skip non-numeric transaction IDs
                    }
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            System.err.println("Error getting next transaction ID: " + e.getMessage());
            e.printStackTrace();
            return 1; // Default to 1 if there's an error
        }
    }
}


package features.revenue;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sale entity class for MongoDB
 * Represents a sale transaction in the revenue management system
 */
@Entity("sales")
public class Sale {
    @Id
    private ObjectId id;
    
    @Property("transactionId")
    private String transactionId;
    
    @Property("sku")
    private String sku;
    
    @Property("productName")
    private String productName;
    
    @Property("baseCostPkr")
    private double baseCostPkr;
    
    @Property("baseCostGbp")
    private double baseCostGbp;
    
    @Property("salePriceGbp")
    private double salePriceGbp;
    
    @Property("shippingGbp")
    private double shippingGbp;
    
    @Property("platformFeePercent")
    private double platformFeePercent;
    
    @Property("platformFeeAmount")
    private double platformFeeAmount;
    
    @Property("netProfitGbp")
    private double netProfitGbp;
    
    @Property("profitMarginPercent")
    private double profitMarginPercent;
    
    @Property("saleDate")
    private String saleDate;
    
    /**
     * Default constructor required by Morphia
     */
    public Sale() {
    }
    
    /**
     * Constructor with all fields
     */
    public Sale(String transactionId, String sku, String productName, 
                double baseCostPkr, double baseCostGbp, double salePriceGbp,
                double shippingGbp, double platformFeePercent, 
                double platformFeeAmount, double netProfitGbp,
                double profitMarginPercent, String saleDate) {
        this.transactionId = transactionId;
        this.sku = sku;
        this.productName = productName;
        this.baseCostPkr = baseCostPkr;
        this.baseCostGbp = baseCostGbp;
        this.salePriceGbp = salePriceGbp;
        this.shippingGbp = shippingGbp;
        this.platformFeePercent = platformFeePercent;
        this.platformFeeAmount = platformFeeAmount;
        this.netProfitGbp = netProfitGbp;
        this.profitMarginPercent = profitMarginPercent;
        this.saleDate = saleDate;
    }
    
    /**
     * Constructor that sets saleDate to current date
     */
    public Sale(String transactionId, String sku, String productName,
                double baseCostPkr, double baseCostGbp, double salePriceGbp,
                double shippingGbp, double platformFeePercent,
                double platformFeeAmount, double netProfitGbp,
                double profitMarginPercent) {
        this(transactionId, sku, productName, baseCostPkr, baseCostGbp, salePriceGbp,
             shippingGbp, platformFeePercent, platformFeeAmount, netProfitGbp,
             profitMarginPercent, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public double getBaseCostPkr() {
        return baseCostPkr;
    }
    
    public void setBaseCostPkr(double baseCostPkr) {
        this.baseCostPkr = baseCostPkr;
    }
    
    public double getBaseCostGbp() {
        return baseCostGbp;
    }
    
    public void setBaseCostGbp(double baseCostGbp) {
        this.baseCostGbp = baseCostGbp;
    }
    
    public double getSalePriceGbp() {
        return salePriceGbp;
    }
    
    public void setSalePriceGbp(double salePriceGbp) {
        this.salePriceGbp = salePriceGbp;
    }
    
    public double getShippingGbp() {
        return shippingGbp;
    }
    
    public void setShippingGbp(double shippingGbp) {
        this.shippingGbp = shippingGbp;
    }
    
    public double getPlatformFeePercent() {
        return platformFeePercent;
    }
    
    public void setPlatformFeePercent(double platformFeePercent) {
        this.platformFeePercent = platformFeePercent;
    }
    
    public double getPlatformFeeAmount() {
        return platformFeeAmount;
    }
    
    public void setPlatformFeeAmount(double platformFeeAmount) {
        this.platformFeeAmount = platformFeeAmount;
    }
    
    public double getNetProfitGbp() {
        return netProfitGbp;
    }
    
    public void setNetProfitGbp(double netProfitGbp) {
        this.netProfitGbp = netProfitGbp;
    }
    
    public double getProfitMarginPercent() {
        return profitMarginPercent;
    }
    
    public void setProfitMarginPercent(double profitMarginPercent) {
        this.profitMarginPercent = profitMarginPercent;
    }
    
    public String getSaleDate() {
        return saleDate;
    }
    
    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }
    
    @Override
    public String toString() {
        return "Sale{" +
                "transactionId='" + transactionId + '\'' +
                ", sku='" + sku + '\'' +
                ", productName='" + productName + '\'' +
                ", salePriceGbp=" + salePriceGbp +
                ", netProfitGbp=" + netProfitGbp +
                '}';
    }
}


package features.stock;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Product entity class for MongoDB
 * Represents a product in the stock management system
 */
@Entity("products")
public class Product {
    @Id
    private ObjectId id;
    
    @Property("sku")
    private String sku;
    
    @Property("name")
    private String name;
    
    @Property("size")
    private String size;
    
    @Property("color")
    private String color;
    
    @Property("material")
    private String material;
    
    @Property("brand")
    private String brand;
    
    @Property("baseCostPkr")
    private double baseCostPkr;
    
    @Property("quantity")
    private int quantity;
    
    @Property("quantitySold")
    private int quantitySold;
    
    @Property("dateAdded")
    private String dateAdded;
    
    /**
     * Default constructor required by Morphia
     */
    public Product() {
    }
    
    /**
     * Constructor with all fields
     */
    public Product(String sku, String name, String size, String color, 
                   String material, String brand, double baseCostPkr, 
                   int quantity, int quantitySold, String dateAdded) {
        this.sku = sku;
        this.name = name;
        this.size = size;
        this.color = color;
        this.material = material;
        this.brand = brand;
        this.baseCostPkr = baseCostPkr;
        this.quantity = quantity;
        this.quantitySold = quantitySold;
        this.dateAdded = dateAdded;
    }
    
    /**
     * Constructor that sets dateAdded to current date
     */
    public Product(String sku, String name, String size, String color, 
                   String material, String brand, double baseCostPkr, 
                   int quantity, int quantitySold) {
        this(sku, name, size, color, material, brand, baseCostPkr, quantity, quantitySold,
             LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public double getBaseCostPkr() {
        return baseCostPkr;
    }
    
    public void setBaseCostPkr(double baseCostPkr) {
        this.baseCostPkr = baseCostPkr;
    }
    
    public String getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public int getQuantitySold() {
        return quantitySold;
    }
    
    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", baseCostPkr=" + baseCostPkr +
                '}';
    }
}



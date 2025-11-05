package namnt.vn.coffestore.data.model;

import com.google.gson.annotations.SerializedName;

public class ProductVariant {
    @SerializedName("id")
    private String id;
    
    @SerializedName("size")
    private String size;
    
    @SerializedName("basePrice")
    private double basePrice;

    // Constructor
    public ProductVariant() {
    }

    public ProductVariant(String id, String size, double basePrice) {
        this.id = id;
        this.size = size;
        this.basePrice = basePrice;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
}

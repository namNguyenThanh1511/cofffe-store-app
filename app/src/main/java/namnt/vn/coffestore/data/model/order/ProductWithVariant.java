package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;

public class ProductWithVariant {
    @SerializedName("productId")
    private String productId;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("variantId")
    private String variantId;
    
    @SerializedName("variantSize")
    private String variantSize;
    
    @SerializedName("basePrice")
    private double basePrice;

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
}

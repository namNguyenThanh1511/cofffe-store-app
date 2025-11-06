package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;

public class OrderItemDetail {
    @SerializedName("id")
    private String id;
    
    @SerializedName("productSizeVariantId")
    private String productSizeVariantId;
    
    @SerializedName("productsWithVariant")
    private ProductWithVariant productsWithVariant;
    
    @SerializedName("productId")
    private String productId;
    
    @SerializedName("variantSize")
    private String variantSize;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("unitPrice")
    private double unitPrice;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("temperature")
    private String temperature; // BE returns string: "Hot", "ColdBrew", "Ice"
    
    @SerializedName("sweetness")
    private String sweetness; // BE returns string: "Sweet", "Normal", "Less", "NoSugar"
    
    @SerializedName("milkType")
    private String milkType; // BE returns string: "Dairy", "Condensed", "Plant", "None"
    
    @SerializedName("addons")
    private java.util.List<namnt.vn.coffestore.data.model.Addon> addons;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductSizeVariantId() {
        return productSizeVariantId;
    }

    public void setProductSizeVariantId(String productSizeVariantId) {
        this.productSizeVariantId = productSizeVariantId;
    }

    public ProductWithVariant getProductsWithVariant() {
        return productsWithVariant;
    }

    public void setProductsWithVariant(ProductWithVariant productsWithVariant) {
        this.productsWithVariant = productsWithVariant;
    }

    public String getProductId() {
        // Try to get from productsWithVariant first
        if (productsWithVariant != null && productsWithVariant.getProductId() != null) {
            return productsWithVariant.getProductId();
        }
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getVariantSize() {
        // Try to get from productsWithVariant first
        if (productsWithVariant != null && productsWithVariant.getVariantSize() != null) {
            return productsWithVariant.getVariantSize();
        }
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getSweetness() {
        return sweetness;
    }

    public void setSweetness(String sweetness) {
        this.sweetness = sweetness;
    }

    public String getMilkType() {
        return milkType;
    }

    public void setMilkType(String milkType) {
        this.milkType = milkType;
    }

    public java.util.List<namnt.vn.coffestore.data.model.Addon> getAddons() {
        return addons;
    }

    public void setAddons(java.util.List<namnt.vn.coffestore.data.model.Addon> addons) {
        this.addons = addons;
    }
    
    // Helper methods to convert enum string to Vietnamese
    public String getTemperatureText() {
        if (temperature == null) return "N/A";
        switch (temperature) {
            case "Hot": return "Nóng";
            case "ColdBrew": return "Pha lạnh";
            case "Ice": return "Đá";
            default: return temperature;
        }
    }
    
    public String getSweetnessText() {
        if (sweetness == null) return "N/A";
        switch (sweetness) {
            case "Sweet": return "Ngọt";
            case "Normal": return "Bình thường";
            case "Less": return "Ít ngọt";
            case "NoSugar": return "Không đường";
            default: return sweetness;
        }
    }
    
    public String getMilkTypeText() {
        if (milkType == null) return "N/A";
        switch (milkType) {
            case "Dairy": return "Sữa tươi";
            case "Condensed": return "Sữa đặc";
            case "Plant": return "Sữa thực vật";
            case "None": return "Không sữa";
            default: return milkType;
        }
    }
}

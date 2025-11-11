package namnt.vn.coffestore.data.model;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private String id; // Product ID
    private String variantId; // Variant ID for API
    private String name;
    private double price;
    private String imageUrl;
    private String size;
    private int quantity;
    private String temperature; // "Hot", "ColdBrew", "Ice"
    private String sweetness; // "Sweet", "Normal", "Less", "NoSugar"
    private String milkType; // "Dairy", "Condensed", "Plant", "None"
    private List<String> selectedAddonIds; // List of addon IDs
    private boolean isSelected; // For checkout selection

    public CartItem(String id, String name, double price, String imageUrl, String size, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.size = size;
        this.quantity = quantity;
        this.selectedAddonIds = new ArrayList<>();
    }
    
    public CartItem(String id, String name, double price, String imageUrl, String size, int quantity, 
                    String temperature, String sweetness, String milkType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.size = size;
        this.quantity = quantity;
        this.temperature = temperature;
        this.sweetness = sweetness;
        this.milkType = milkType;
        this.selectedAddonIds = new ArrayList<>();
    }
    
    // Full constructor with all fields
    public CartItem(String id, String variantId, String name, double price, String imageUrl, 
                    String size, int quantity, String temperature, String sweetness, 
                    String milkType, List<String> selectedAddonIds) {
        this.id = id;
        this.variantId = variantId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.size = size;
        this.quantity = quantity;
        this.temperature = temperature;
        this.sweetness = sweetness;
        this.milkType = milkType;
        this.selectedAddonIds = selectedAddonIds != null ? selectedAddonIds : new ArrayList<>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }
    
    public String getTemperature() {
        return temperature;
    }
    
    public String getSweetness() {
        return sweetness;
    }
    
    public String getMilkType() {
        return milkType;
    }
    
    public String getVariantId() {
        return variantId;
    }
    
    public List<String> getSelectedAddonIds() {
        return selectedAddonIds != null ? selectedAddonIds : new ArrayList<>();
    }
    
    public boolean isSelected() {
        return isSelected;
    }

    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }
    
    public void setSelectedAddonIds(List<String> selectedAddonIds) {
        this.selectedAddonIds = selectedAddonIds;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Calculate total for this item
    public double getTotal() {
        return price * quantity;
    }
    
    // Helper methods to convert enum string to Vietnamese
    public String getTemperatureText() {
        if (temperature == null) return "";
        switch (temperature) {
            case "Hot": return "Nóng";
            case "ColdBrew": return "Pha lạnh";
            case "Ice": return "Đá";
            default: return temperature;
        }
    }
    
    public String getSweetnessText() {
        if (sweetness == null) return "";
        switch (sweetness) {
            case "Sweet": return "Ngọt";
            case "Normal": return "Bình thường";
            case "Less": return "Ít ngọt";
            case "NoSugar": return "Không đường";
            default: return sweetness;
        }
    }
    
    public String getMilkTypeText() {
        if (milkType == null) return "";
        switch (milkType) {
            case "Dairy": return "Sữa tươi";
            case "Condensed": return "Sữa đặc";
            case "Plant": return "Sữa thực vật";
            case "None": return "Không sữa";
            default: return milkType;
        }
    }
}

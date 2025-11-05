package namnt.vn.coffestore.data.model;

public class CartItem {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private String size;
    private int quantity;
    private String temperature; // "Hot", "ColdBrew", "Ice"
    private String sweetness; // "Sweet", "Normal", "Less", "NoSugar"
    private String milkType; // "Dairy", "Condensed", "Plant", "None"

    public CartItem(String id, String name, double price, String imageUrl, String size, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.size = size;
        this.quantity = quantity;
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

    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

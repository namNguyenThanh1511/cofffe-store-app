package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderItem {
    @SerializedName("variantId")
    private String variantId;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("temperature")
    private int temperature; // 0=Hot, 1=ColdBrew, 2=Ice
    
    @SerializedName("sweetness")
    private int sweetness; // 0=Sweet, 1=Normal, 2=Less, 3=NoSugar
    
    @SerializedName("milkType")
    private int milkType; // 0=Dairy, 1=Condensed, 2=Plant, 3=None
    
    @SerializedName("addOnIds")
    private List<String> addOnIds;

    public OrderItem(String variantId, int quantity, int temperature, int sweetness, int milkType, List<String> addOnIds) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.temperature = temperature;
        this.sweetness = sweetness;
        this.milkType = milkType;
        this.addOnIds = addOnIds;
    }

    // Getters and Setters
    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getSweetness() {
        return sweetness;
    }

    public void setSweetness(int sweetness) {
        this.sweetness = sweetness;
    }

    public int getMilkType() {
        return milkType;
    }

    public void setMilkType(int milkType) {
        this.milkType = milkType;
    }

    public List<String> getAddOnIds() {
        return addOnIds;
    }

    public void setAddOnIds(List<String> addOnIds) {
        this.addOnIds = addOnIds;
    }
}

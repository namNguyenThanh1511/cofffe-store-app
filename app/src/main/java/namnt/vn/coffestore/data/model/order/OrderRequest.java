package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderRequest {
    @SerializedName("deliveryType")
    private int deliveryType; // Mặc định 0
    
    @SerializedName("orderItems")
    private List<OrderItem> orderItems;

    public OrderRequest(int deliveryType, List<OrderItem> orderItems) {
        this.deliveryType = deliveryType;
        this.orderItems = orderItems;
    }

    // Getters and Setters
    public int getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(int deliveryType) {
        this.deliveryType = deliveryType;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}

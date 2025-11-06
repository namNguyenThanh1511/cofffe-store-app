package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderResponse {
    @SerializedName("id")
    private Object id; // Can be int or String
    
    @SerializedName("orderDate")
    private String orderDate;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("customerId")
    private String customerId;
    
    @SerializedName("orderItems")
    private List<OrderItemDetail> orderItems;

    // Getters and Setters
    public String getId() {
        if (id == null) return null;
        return id.toString();
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDetail> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDetail> orderItems) {
        this.orderItems = orderItems;
    }
    
    // Helper method
    public String getStatusText() {
        if (status == null) return "N/A";
        switch (status.toUpperCase()) {
            case "PROCESSING": return "Đang xử lý";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}

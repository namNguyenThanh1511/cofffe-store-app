package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;

public class UpdateOrderStatusRequest {
    @SerializedName("orderId")
    private String orderId;

    @SerializedName("newStatus")
    private int newStatus;

    public UpdateOrderStatusRequest(String orderId, int newStatus) {
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(int newStatus) {
        this.newStatus = newStatus;
    }
}



package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;

public class PayOrderRequest {
    @SerializedName("orderId")
    private String orderId;

    public PayOrderRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}



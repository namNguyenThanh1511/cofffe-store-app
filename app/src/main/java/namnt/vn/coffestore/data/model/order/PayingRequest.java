package namnt.vn.coffestore.data.model.order;

import com.google.gson.annotations.SerializedName;

public class PayingRequest {
    @SerializedName("orderCode")
    private String orderCode;

    public PayingRequest(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
}

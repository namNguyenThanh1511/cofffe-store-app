package namnt.vn.coffestore.data.model.api;

import com.google.gson.annotations.SerializedName;

public class ApiError {
    @SerializedName("message")
    private String message = "";
    @SerializedName("code")
    private int code;

    // Getters/Setters...
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
}

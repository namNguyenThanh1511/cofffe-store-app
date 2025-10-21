package namnt.vn.coffestore.data.model.auth;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("fullName")
    private String fullName = "";

    @SerializedName("email")
    private String email = "";

    @SerializedName("phoneNumber")
    private String phoneNumber = "";

    @SerializedName("password")
    private String password = "";

    @SerializedName("role")
    private int role = 2;  // Giả sử enum Role serialized thành string; thay bằng int nếu cần

    // Constructor đầy đủ
    public RegisterRequest(String fullName, String email, String phoneNumber, String password, int role) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    // Getters/Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
}

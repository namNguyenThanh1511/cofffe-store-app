package namnt.vn.coffestore.data.model.auth;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("role")
    private String role;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

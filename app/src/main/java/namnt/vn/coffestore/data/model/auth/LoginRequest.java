package namnt.vn.coffestore.data.model.auth;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("keyLogin")
    private String keyLogin = "";

    @SerializedName("password")
    private String password = "";

    // Constructor
    public LoginRequest(String keyLogin, String password) {
        this.keyLogin = keyLogin;
        this.password = password;
    }

    // Getters/Setters
    public String getKeyLogin() { return keyLogin; }
    public void setKeyLogin(String keyLogin) { this.keyLogin = keyLogin; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

package namnt.vn.coffestore.data.model.auth;

import com.google.gson.annotations.SerializedName;
import java.util.Date;  // Để map DateTime

public class TokenResponse {
    @SerializedName("accessToken")
    private String accessToken = "";

    @SerializedName("refreshToken")
    private String refreshToken = "";

    @SerializedName("accessTokenExpiry")
    private Date accessTokenExpiry;

    @SerializedName("refreshTokenExpiry")
    private Date refreshTokenExpiry;

    @SerializedName("tokenType")
    private String tokenType = "Bearer";

    // Constructor (có thể empty, Gson sẽ map)
    public TokenResponse() {}

    // Getters/Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Date getAccessTokenExpiry() { return accessTokenExpiry; }
    public void setAccessTokenExpiry(Date accessTokenExpiry) { this.accessTokenExpiry = accessTokenExpiry; }
    public Date getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(Date refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}

package namnt.vn.coffestore.data.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Participant {
    @SerializedName("userId")
    private String userId;
    @SerializedName("username")
    private String username;
    @SerializedName("role")
    private String role;
    @SerializedName("lastSeen")
    private Date lastSeen;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Date getLastSeen() { return lastSeen; }
    public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }
}



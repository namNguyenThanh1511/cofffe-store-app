package namnt.vn.coffestore.data.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ChatMessage {
    @SerializedName("id")
    private long id;
    @SerializedName("senderId")
    private String senderId;
    @SerializedName("senderName")
    private String senderName;
    @SerializedName("content")
    private String content;
    @SerializedName("messageType")
    private String messageType;
    @SerializedName("isRead")
    private boolean isRead;
    @SerializedName("createdAt")
    private Date createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // For test data helper
    private long conversationId;
    public long getConversationId() { return conversationId; }
    public void setConversationId(long conversationId) { this.conversationId = conversationId; }
}



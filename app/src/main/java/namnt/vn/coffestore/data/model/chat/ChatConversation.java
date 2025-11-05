package namnt.vn.coffestore.data.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class ChatConversation {
    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("updatedAt")
    private Date updatedAt;

    @SerializedName("participants")
    private List<Participant> participants;

    @SerializedName("messages")
    private List<ChatMessage> messages;

    @SerializedName("lastMessage")
    private ChatMessage lastMessage;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }
    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    public ChatMessage getLastMessage() { return lastMessage; }
    public void setLastMessage(ChatMessage lastMessage) { this.lastMessage = lastMessage; }
}



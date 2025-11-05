package namnt.vn.coffestore.data.model.chat;

public class SendMessageRequest {
    private long conversationID;
    private String senderId;
    private String content;
    private String messageType;

    public SendMessageRequest(long conversationID, String senderId, String content, String messageType) {
        this.conversationID = conversationID;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
    }
}



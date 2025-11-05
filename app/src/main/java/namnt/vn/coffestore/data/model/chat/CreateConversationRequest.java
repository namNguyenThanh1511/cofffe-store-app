package namnt.vn.coffestore.data.model.chat;

public class CreateConversationRequest {
    private String customerId;
    private String baristaId;
    private String title;

    public CreateConversationRequest(String customerId, String baristaId, String title) {
        this.customerId = customerId;
        this.baristaId = baristaId;
        this.title = title;
    }
}



package namnt.vn.coffestore.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.chat.ChatConversation;
import namnt.vn.coffestore.data.model.chat.ChatMessage;
import namnt.vn.coffestore.data.repository.ChatRepository;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repository;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
    }

    public void loadUserConversations(String userId) { repository.fetchUserConversations(userId); }
    public void loadConversationMessages(long conversationId) { repository.fetchConversationMessages(conversationId); }

    public LiveData<ApiResponse<List<ChatConversation>>> getConversations() { return repository.getConversationsLive(); }
    public LiveData<ApiResponse<List<ChatMessage>>> getMessages() { return repository.getMessagesLive(); }

    public void sendMessage(long conversationId, String senderId, String content) { repository.sendMessage(conversationId, senderId, content); }
    public LiveData<ApiResponse<ChatMessage>> getSendResult() { return repository.getSendResult(); }

    public void createConversationForCustomer() { repository.createConversationForCustomer(); }
    public LiveData<ApiResponse<ChatConversation>> getCreateResult() { return repository.getCreateResult(); }
}



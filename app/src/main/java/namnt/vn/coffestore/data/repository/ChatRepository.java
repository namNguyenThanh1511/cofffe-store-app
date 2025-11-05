package namnt.vn.coffestore.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.chat.ChatConversation;
import namnt.vn.coffestore.data.model.chat.ChatMessage;
import namnt.vn.coffestore.data.model.chat.SendMessageRequest;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ApiService apiService;
    private final SharedPreferences prefs;

    private final MutableLiveData<ApiResponse<List<ChatConversation>>> conversations = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<ChatMessage>>> messages = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<ChatMessage>> sendResult = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<ChatConversation>> createResult = new MutableLiveData<>();

    public ChatRepository(Context context) {
        this.apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        this.prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    private String bearer() { return "Bearer " + prefs.getString("access_token", ""); }

    public void fetchUserConversations(String userId) {
        android.util.Log.d("ChatRepository", "Fetching conversations for userId: " + userId);
        String authHeader = bearer();
        android.util.Log.d("ChatRepository", "Authorization header: " + (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader));
        
        apiService.getUserConversations(authHeader, userId).enqueue(new Callback<ApiResponse<List<ChatConversation>>>() {
            @Override 
            public void onResponse(Call<ApiResponse<List<ChatConversation>>> call, Response<ApiResponse<List<ChatConversation>>> response) {
                android.util.Log.d("ChatRepository", "Response received - code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChatConversation>> body = response.body();
                    android.util.Log.d("ChatRepository", "Response body: isSuccess=" + body.isSuccess() + ", message=" + body.getMessage());
                    
                    if (body.isSuccess() && body.getData() != null) {
                        android.util.Log.d("ChatRepository", "Conversations loaded: " + body.getData().size() + " items");
                        for (ChatConversation c : body.getData()) {
                            android.util.Log.d("ChatRepository", "Conversation: id=" + c.getId() + ", title=" + c.getTitle());
                        }
                        conversations.postValue(body);
                    } else {
                        android.util.Log.e("ChatRepository", "API returned unsuccessful: " + body.getMessage());
                        // Still post the response so UI can show error message
                        conversations.postValue(body);
                    }
                } else {
                    // Try to parse error body
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("ChatRepository", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ChatRepository", "Failed to read error body: " + e.getMessage());
                    }
                    
                    android.util.Log.e("ChatRepository", "Failed to load conversations - HTTP " + response.code() + ": " + response.message());
                    android.util.Log.e("ChatRepository", "Response body is null: " + (response.body() == null));
                    
                    // Post null to indicate failure
                    conversations.postValue(null);
                }
            }
            @Override 
            public void onFailure(Call<ApiResponse<List<ChatConversation>>> call, Throwable t) {
                android.util.Log.e("ChatRepository", "Network failure: " + t.getMessage(), t);
                conversations.postValue(null);
            }
        });
    }

    public void fetchConversationMessages(long conversationId) {
        apiService.getConversationMessages(bearer(), conversationId).enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
            @Override public void onResponse(Call<ApiResponse<List<ChatMessage>>> call, Response<ApiResponse<List<ChatMessage>>> response) { messages.postValue(response.body()); }
            @Override public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) { messages.postValue(null); }
        });
    }

    public LiveData<ApiResponse<List<ChatConversation>>> getConversationsLive() { return conversations; }
    public LiveData<ApiResponse<List<ChatMessage>>> getMessagesLive() { return messages; }

    public void sendMessage(long conversationId, String senderId, String content) {
        SendMessageRequest req = new SendMessageRequest(conversationId, senderId, content, "text");
        apiService.sendMessage(bearer(), req).enqueue(new Callback<ApiResponse<ChatMessage>>() {
            @Override public void onResponse(Call<ApiResponse<ChatMessage>> call, Response<ApiResponse<ChatMessage>> response) {
                sendResult.postValue(response.body());
            }
            @Override public void onFailure(Call<ApiResponse<ChatMessage>> call, Throwable t) {
                sendResult.postValue(null);
            }
        });
    }

    public LiveData<ApiResponse<ChatMessage>> getSendResult() { return sendResult; }

    public void createConversationForCustomer() {
        apiService.createConversationForCustomer(bearer()).enqueue(new Callback<ApiResponse<ChatConversation>>() {
            @Override public void onResponse(Call<ApiResponse<ChatConversation>> call, Response<ApiResponse<ChatConversation>> response) {
                createResult.postValue(response.body());
            }
            @Override public void onFailure(Call<ApiResponse<ChatConversation>> call, Throwable t) {
                createResult.postValue(null);
            }
        });
    }

    public LiveData<ApiResponse<ChatConversation>> getCreateResult() { return createResult; }
}



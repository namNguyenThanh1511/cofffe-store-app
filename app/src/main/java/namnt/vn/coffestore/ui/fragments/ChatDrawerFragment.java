package namnt.vn.coffestore.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;

/**
 * Right-drawer chat UI for demo/testing.
 * - CUSTOMER: show one conversation with shop
 * - ADMIN or BARISTA: show list of conversations, click to open chat
 */
public class ChatDrawerFragment extends Fragment {

    public enum Role { CUSTOMER, BARISTA, ADMIN }

    private RecyclerView rvConversations;
    private RecyclerView rvMessages;
    private View conversationListContainer;
    private View chatHeader;
    private TextView tvChatTitle;
    private EditText etMessage;
    private ImageView btnSend;
    private View btnStartChat; // visible for customer only
    private ImageView btnBackToConversations; // for Barista/Admin to go back to conversation list

    private namnt.vn.coffestore.ui.adapters.ConversationAdapter conversationAdapter;
    private namnt.vn.coffestore.ui.adapters.MessageAdapter messageAdapter;

    private namnt.vn.coffestore.viewmodel.ChatViewModel chatViewModel;

    private Role role = Role.CUSTOMER;
    private Long currentConversationId = null; // Track current conversation ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat_drawer, container, false);

        conversationListContainer = root.findViewById(R.id.containerConversationList);
        rvConversations = root.findViewById(R.id.rvConversations);
        rvMessages = root.findViewById(R.id.rvMessages);
        chatHeader = root.findViewById(R.id.chatHeader);
        tvChatTitle = root.findViewById(R.id.tvChatTitle);
        etMessage = root.findViewById(R.id.etMessage);
        btnSend = root.findViewById(R.id.btnSend);
        btnStartChat = root.findViewById(R.id.btnStartChat);
        btnBackToConversations = root.findViewById(R.id.btnBackToConversations);

        resolveRole(requireContext());

        chatViewModel = new androidx.lifecycle.ViewModelProvider(this,
                new androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(namnt.vn.coffestore.viewmodel.ChatViewModel.class);

        setupLists();
        
        // Setup observers BEFORE calling API
        setupObservers();
        
        bindRoleUI();
        
        // Back button to return to conversation list
        if (btnBackToConversations != null) {
            btnBackToConversations.setOnClickListener(v -> {
                if (role != Role.CUSTOMER) {
                    // Show conversation list, hide messages
                    if (conversationListContainer != null) conversationListContainer.setVisibility(View.VISIBLE);
                    if (rvMessages != null) rvMessages.setVisibility(View.GONE);
                    if (btnBackToConversations != null) btnBackToConversations.setVisibility(View.GONE);
                    tvChatTitle.setText("Select a conversation");
                    messageAdapter.clear();
                }
            });
        }

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            Long conversationId = getCurrentConversationId();
            if (conversationId == null) {
                android.util.Log.e("ChatFragment", "Cannot send message: conversationId is null");
                android.widget.Toast.makeText(getContext(), "No conversation selected", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            android.util.Log.d("ChatFragment", "Sending message to conversation: " + conversationId);
            chatViewModel.sendMessage(conversationId, getUserId(), text);
        });

        return root;
    }

    private void resolveRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        int roleIdx = prefs.getInt("user_role", -1); // -1 means not set, 0 Customer, 1 Admin, 2 Barista
        
        // If role is not set, try to extract from token
        if (roleIdx == -1) {
            String token = prefs.getString("access_token", "");
            if (!TextUtils.isEmpty(token)) {
                String roleStr = extractRoleFromToken(token);
                if (!TextUtils.isEmpty(roleStr)) {
                    String roleLower = roleStr.toLowerCase();
                    if (roleLower.contains("admin")) {
                        roleIdx = 1;
                    } else if (roleLower.contains("barista")) {
                        roleIdx = 2;
                    } else {
                        roleIdx = 0; // Default to Customer
                    }
                    // Save it for next time
                    prefs.edit().putInt("user_role", roleIdx).apply();
                    android.util.Log.d("ChatFragment", "Extracted and saved role from token: " + roleStr + " -> roleIdx: " + roleIdx);
                } else {
                    roleIdx = 0; // Default to Customer if extraction fails
                }
            } else {
                roleIdx = 0; // Default to Customer if no token
            }
        }
        
        if (roleIdx == 1) {
            role = Role.ADMIN;
        } else if (roleIdx == 2) {
            role = Role.BARISTA;
        } else {
            role = Role.CUSTOMER;
        }
        android.util.Log.d("ChatFragment", "Resolved role: " + role + " from roleIdx: " + roleIdx);
    }
    
    /**
     * Extract role from JWT token payload (fallback method)
     */
    private String extractRoleFromToken(String token) {
        if (TextUtils.isEmpty(token)) return "";
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "";
            
            String payload = parts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            String payloadJson = new String(decodedBytes, "UTF-8");
            
            org.json.JSONObject json = new org.json.JSONObject(payloadJson);
            
            // Try different possible field names
            if (json.has("role")) {
                return json.getString("role");
            } else if (json.has("Role")) {
                return json.getString("Role");
            } else if (json.has("userRole")) {
                return json.getString("userRole");
            } else if (json.has("user_role")) {
                return json.getString("user_role");
            } else if (json.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")) {
                Object roleObj = json.get("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
                if (roleObj instanceof String) {
                    return (String) roleObj;
                } else if (roleObj instanceof org.json.JSONArray) {
                    org.json.JSONArray arr = (org.json.JSONArray) roleObj;
                    if (arr.length() > 0) {
                        return arr.getString(0);
                    }
                }
            }
            
            // Log full payload for debugging
            android.util.Log.d("ChatFragment", "JWT payload: " + payloadJson);
        } catch (Exception e) {
            android.util.Log.e("ChatFragment", "Failed to extract role from token: " + e.getMessage());
        }
        return "";
    }

    private void setupLists() {
        // Conversations
        conversationAdapter = new namnt.vn.coffestore.ui.adapters.ConversationAdapter();
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(conversationAdapter);

        conversationAdapter.setOnConversationClick(conversation -> openConversation(conversation.getId(), conversation.getTitle()));

        // Messages
        messageAdapter = new namnt.vn.coffestore.ui.adapters.MessageAdapter();
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(messageAdapter);
    }

    private void bindRoleUI() {
        String userId = getUserId();
        if (role == Role.CUSTOMER) {
            // Customer: hide conversation list, show button only if no conversation exists
            conversationListContainer.setVisibility(View.GONE);
            if (btnStartChat != null) {
                btnStartChat.setOnClickListener(v -> {
                    // New API creates/returns customer conversation without parameters
                    chatViewModel.createConversationForCustomer();
                });
            }
        } else {
            // Barista/Admin: show conversation list (set visible initially)
            if (conversationListContainer != null) {
                conversationListContainer.setVisibility(View.VISIBLE);
                android.util.Log.d("ChatFragment", "Barista/Admin: Setting conversation list visible");
            }
            if (btnStartChat != null) btnStartChat.setVisibility(View.GONE);
            if (rvMessages != null) rvMessages.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userId)) {
            android.util.Log.d("ChatFragment", "Loading conversations for userId: " + userId + ", role: " + role);
            chatViewModel.loadUserConversations(userId);
        } else {
            android.util.Log.e("ChatFragment", "userId is empty!");
        }
    }
    
    private void setupObservers() {
        chatViewModel.getConversations().observe(getViewLifecycleOwner(), resp -> {
            android.util.Log.d("ChatFragment", "Conversations response: " + (resp != null ? "not null" : "null"));
            if (resp != null) {
                android.util.Log.d("ChatFragment", "isSuccess: " + resp.isSuccess() + ", data: " + (resp.getData() != null ? resp.getData().size() + " items" : "null"));
            }
            
            if (resp != null && resp.isSuccess() && resp.getData() != null) {
                List<namnt.vn.coffestore.data.model.chat.ChatConversation> conversations = resp.getData();
                android.util.Log.d("ChatFragment", "Processing " + conversations.size() + " conversations for role: " + role);
                
                if (role == Role.CUSTOMER) {
                    // Customer: 1 conversation only
                    if (conversations.isEmpty()) {
                        // No conversation yet - show button
                        android.util.Log.d("ChatFragment", "Customer: No conversations, showing button");
                        if (btnStartChat != null) btnStartChat.setVisibility(View.VISIBLE);
                        tvChatTitle.setText("Chat with Barista");
                        messageAdapter.clear();
                    } else {
                        // Has conversation - hide button, load it
                        android.util.Log.d("ChatFragment", "Customer: Has conversation, loading messages");
                        if (btnStartChat != null) btnStartChat.setVisibility(View.GONE);
                        namnt.vn.coffestore.data.model.chat.ChatConversation conv = conversations.get(0);
                        currentConversationId = conv.getId(); // Store conversation ID
                        tvChatTitle.setText(conv.getTitle());
                        // Ensure messages area is visible
                        if (rvMessages != null) rvMessages.setVisibility(View.VISIBLE);
                        chatViewModel.loadConversationMessages(conv.getId());
                    }
                } else {
                    // Barista/Admin: show all conversations (only titles, don't auto-load messages)
                    android.util.Log.d("ChatFragment", "Barista/Admin: Showing " + conversations.size() + " conversations");
                    for (namnt.vn.coffestore.data.model.chat.ChatConversation c : conversations) {
                        android.util.Log.d("ChatFragment", "Conversation: id=" + c.getId() + ", title=" + c.getTitle());
                    }
                    conversationAdapter.submit(conversations);
                    // Don't auto-load first conversation - wait for user to click
                    tvChatTitle.setText("Select a conversation");
                    messageAdapter.clear();
                    // Ensure conversation list is visible, messages hidden
                    if (conversationListContainer != null) {
                        conversationListContainer.setVisibility(View.VISIBLE);
                        android.util.Log.d("ChatFragment", "Conversation list container set to VISIBLE");
                    }
                    if (rvMessages != null) {
                        rvMessages.setVisibility(View.GONE);
                        android.util.Log.d("ChatFragment", "Messages RecyclerView set to GONE");
                    }
                    if (btnBackToConversations != null) btnBackToConversations.setVisibility(View.GONE);
                }
            } else if (resp != null && !resp.isSuccess()) {
                // API call succeeded but response indicates failure
                android.util.Log.e("ChatFragment", "API returned error: " + (resp.getMessage() != null ? resp.getMessage() : "Unknown error"));
                android.widget.Toast.makeText(getContext(), "Failed to load conversations: " + (resp.getMessage() != null ? resp.getMessage() : "Unknown error"), android.widget.Toast.LENGTH_LONG).show();
                
                // Show empty state for Barista/Admin
                if (role != Role.CUSTOMER) {
                    conversationAdapter.submit(new java.util.ArrayList<>());
                    tvChatTitle.setText("No conversations");
                }
            } else {
                // Response is null - network error or parse error
                android.util.Log.e("ChatFragment", "Response is null - check network or API endpoint");
                android.widget.Toast.makeText(getContext(), "Failed to load conversations. Please check your connection.", android.widget.Toast.LENGTH_LONG).show();
                
                // Show empty state for Barista/Admin
                if (role != Role.CUSTOMER) {
                    conversationAdapter.submit(new java.util.ArrayList<>());
                    tvChatTitle.setText("No conversations");
                }
            }
        });

        chatViewModel.getMessages().observe(getViewLifecycleOwner(), resp -> {
            android.util.Log.d("ChatFragment", "Messages response: " + (resp != null ? "not null" : "null"));
            if (resp != null) {
                android.util.Log.d("ChatFragment", "Messages - isSuccess: " + resp.isSuccess() + ", data: " + (resp.getData() != null ? resp.getData().size() + " items" : "null"));
            }
            
            if (resp != null && resp.isSuccess() && resp.getData() != null) {
                messageAdapter.clear();
                for (namnt.vn.coffestore.data.model.chat.ChatMessage m : resp.getData()) {
                    String myId = getUserId();
                    boolean incoming = myId == null || !myId.equals(m.getSenderId());
                    if (incoming) {
                        messageAdapter.addIncoming(m.getContent(), m.getCreatedAt(), m.getSenderName());
                    } else {
                        messageAdapter.addOutgoing(m.getContent(), m.getCreatedAt(), m.getSenderName());
                    }
                }
                // Ensure messages RecyclerView is visible
                if (rvMessages != null) {
                    rvMessages.setVisibility(View.VISIBLE);
                    if (messageAdapter.getItemCount() > 0) {
                        rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
            } else if (resp != null && !resp.isSuccess()) {
                android.util.Log.e("ChatFragment", "Failed to load messages: " + (resp.getMessage() != null ? resp.getMessage() : "Unknown error"));
            } else {
                android.util.Log.e("ChatFragment", "Messages response is null");
            }
        });

        chatViewModel.getSendResult().observe(getViewLifecycleOwner(), resp -> {
            android.util.Log.d("ChatFragment", "Send result: " + (resp != null ? "not null" : "null"));
            if (resp != null) {
                android.util.Log.d("ChatFragment", "Send - isSuccess: " + resp.isSuccess() + ", data: " + (resp.getData() != null ? "not null" : "null"));
            }
            
            if (resp != null && resp.isSuccess() && resp.getData() != null) {
                etMessage.setText("");
                
                // Option 1: Add message directly to adapter (optimistic update)
                // messageAdapter.addOutgoing(resp.getData().getContent(), resp.getData().getCreatedAt(), resp.getData().getSenderName());
                
                // Option 2: Reload messages from server to ensure sync (better approach)
                Long conversationId = getCurrentConversationId();
                if (conversationId != null) {
                    android.util.Log.d("ChatFragment", "Reloading messages after send for conversation: " + conversationId);
                    chatViewModel.loadConversationMessages(conversationId);
                } else {
                    android.util.Log.e("ChatFragment", "Cannot reload messages: conversationId is null");
                    // Fallback: add message directly
                    messageAdapter.addOutgoing(resp.getData().getContent(), resp.getData().getCreatedAt(), resp.getData().getSenderName());
                    if (rvMessages != null && messageAdapter.getItemCount() > 0) {
                        rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
            } else if (resp != null && !resp.isSuccess()) {
                android.util.Log.e("ChatFragment", "Failed to send message: " + (resp.getMessage() != null ? resp.getMessage() : "Unknown error"));
                android.widget.Toast.makeText(getContext(), "Failed to send message: " + (resp.getMessage() != null ? resp.getMessage() : "Unknown error"), android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("ChatFragment", "Send result is null");
            }
        });

        // Observe conversation creation result
        chatViewModel.getCreateResult().observe(getViewLifecycleOwner(), resp -> {
            if (resp != null && resp.isSuccess() && resp.getData() != null) {
                // Reload conversations to update UI
                String userId = getUserId();
                if (!TextUtils.isEmpty(userId)) {
                    chatViewModel.loadUserConversations(userId);
                }
            }
        });
    }

    private void openConversation(long conversationId, String title) {
        android.util.Log.d("ChatFragment", "Opening conversation: " + conversationId + " - " + title);
        currentConversationId = conversationId; // Store current conversation ID
        tvChatTitle.setText(title != null ? title : "Chat");
        
        // Hide conversation list, show messages area
        if (role != Role.CUSTOMER) {
            if (conversationListContainer != null) conversationListContainer.setVisibility(View.GONE);
            if (rvMessages != null) rvMessages.setVisibility(View.VISIBLE);
            if (btnBackToConversations != null) btnBackToConversations.setVisibility(View.VISIBLE);
        } else {
            // For Customer, ensure messages area is visible
            if (rvMessages != null) rvMessages.setVisibility(View.VISIBLE);
        }
        
        // Load messages for this conversation
        chatViewModel.loadConversationMessages(conversationId);
    }

    private String getUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");
        
        // Fallback: if userId is empty, try to extract from token
        if (TextUtils.isEmpty(userId)) {
            String token = prefs.getString("access_token", "");
            if (!TextUtils.isEmpty(token)) {
                userId = extractUserIdFromToken(token);
                if (!TextUtils.isEmpty(userId)) {
                    // Save it for next time
                    prefs.edit().putString("user_id", userId).apply();
                    android.util.Log.d("ChatFragment", "Extracted and saved userId from token: " + userId);
                }
            }
        }
        
        return userId;
    }
    
    /**
     * Extract userId from JWT token payload (fallback method)
     */
    private String extractUserIdFromToken(String token) {
        if (TextUtils.isEmpty(token)) return "";
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "";
            
            String payload = parts[1];
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            String payloadJson = new String(decodedBytes, "UTF-8");
            
            org.json.JSONObject json = new org.json.JSONObject(payloadJson);
            
            // Try different possible field names
            if (json.has("userId")) {
                return json.getString("userId");
            } else if (json.has("user_id")) {
                return json.getString("user_id");
            } else if (json.has("sub")) {
                return json.getString("sub");
            } else if (json.has("nameid")) {
                return json.getString("nameid");
            } else if (json.has("id")) {
                return json.getString("id");
            }
        } catch (Exception e) {
            android.util.Log.e("ChatFragment", "Failed to extract userId from token: " + e.getMessage());
        }
        return "";
    }

    private String getDefaultBaristaId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return prefs.getString("default_barista_id", "");
    }

    /**
     * Get current conversation ID (preferred method)
     */
    private Long getCurrentConversationId() {
        if (currentConversationId != null) {
            return currentConversationId;
        }
        // Fallback: try to get from title (for backward compatibility)
        return getCurrentConversationIdFromTitle();
    }
    
    /**
     * Fallback method: Get conversation ID from title (less reliable)
     */
    private Long getCurrentConversationIdFromTitle() {
        String title = tvChatTitle.getText() != null ? tvChatTitle.getText().toString() : null;
        if (title == null) return null;
        namnt.vn.coffestore.data.model.api.ApiResponse<java.util.List<namnt.vn.coffestore.data.model.chat.ChatConversation>> last = chatViewModel.getConversations().getValue();
        if (last != null && last.getData() != null) {
            for (namnt.vn.coffestore.data.model.chat.ChatConversation c : last.getData()) {
                if (title.equals(c.getTitle())) return c.getId();
            }
        }
        return null;
    }
}



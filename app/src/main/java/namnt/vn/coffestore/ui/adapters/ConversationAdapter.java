package namnt.vn.coffestore.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.chat.ChatConversation;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    public interface OnConversationClickListener { void onConversationClick(ChatConversation c); }
    private OnConversationClickListener listener;

    private final List<ChatConversation> data = new ArrayList<>();

    public void submit(List<ChatConversation> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnConversationClick(OnConversationClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatConversation c = data.get(position);
        holder.title.setText(c.getTitle());
        
        // Last message preview
        if (c.getLastMessage() != null && c.getLastMessage().getContent() != null) {
            holder.lastMessage.setText(c.getLastMessage().getContent());
        } else {
            holder.lastMessage.setText("No messages yet");
        }
        
        // Time
        if (c.getUpdatedAt() != null) {
            holder.time.setText(formatTime(c.getUpdatedAt()));
        } else {
            holder.time.setText("");
        }
        
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onConversationClick(c); });
    }

    @Override
    public int getItemCount() { return data.size(); }

    private String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, lastMessage, time;
        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvConversationTitle);
            lastMessage = itemView.findViewById(R.id.tvLastMessage);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}

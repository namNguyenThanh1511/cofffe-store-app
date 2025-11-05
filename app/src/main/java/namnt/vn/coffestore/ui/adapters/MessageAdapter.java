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

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static class Message { 
        boolean incoming; 
        String text; 
        Date createdAt;
        String senderName;
        
        Message(boolean i, String t, Date d, String s){incoming=i;text=t;createdAt=d;senderName=s;} 
    }
    
    private final List<Message> messages = new ArrayList<>();

    private static final int TYPE_IN = 1;
    private static final int TYPE_OUT = 2;

    public void addIncoming(String text, Date createdAt, String senderName){ 
        messages.add(new Message(true, text, createdAt, senderName)); 
        notifyItemInserted(messages.size()-1); 
    }
    
    public void addOutgoing(String text, Date createdAt, String senderName){ 
        messages.add(new Message(false, text, createdAt, senderName)); 
        notifyItemInserted(messages.size()-1); 
    }
    
    public void clear(){ messages.clear(); notifyDataSetChanged(); }

    @Override
    public int getItemViewType(int position) { return messages.get(position).incoming ? TYPE_IN : TYPE_OUT; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_IN) {
            View v = inf.inflate(R.layout.item_message_incoming, parent, false);
            return new InHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_message_outgoing, parent, false);
            return new OutHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = messages.get(position);
        if (holder instanceof InHolder) {
            InHolder h = (InHolder) holder;
            h.text.setText(m.text);
            if (m.senderName != null) {
                h.senderName.setText(m.senderName);
                h.senderName.setVisibility(View.VISIBLE);
            }
            if (m.createdAt != null) {
                h.time.setText(formatTime(m.createdAt));
            }
        }
        if (holder instanceof OutHolder) {
            OutHolder h = (OutHolder) holder;
            h.text.setText(m.text);
            if (m.createdAt != null) {
                h.time.setText(formatTime(m.createdAt));
            }
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    private String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    static class InHolder extends RecyclerView.ViewHolder {
        TextView text, time, senderName;
        InHolder(@NonNull View itemView){ 
            super(itemView); 
            text = itemView.findViewById(R.id.tvMessageIncoming);
            time = itemView.findViewById(R.id.tvTime);
            senderName = itemView.findViewById(R.id.tvSenderName);
        }
    }

    static class OutHolder extends RecyclerView.ViewHolder {
        TextView text, time;
        OutHolder(@NonNull View itemView){ 
            super(itemView); 
            text = itemView.findViewById(R.id.tvMessageOutgoing);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}

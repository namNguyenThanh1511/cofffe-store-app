package namnt.vn.coffestore.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderResponse> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    public OrderAdapter() {
        this.orders = new ArrayList<>();
    }

    public void setOrders(List<OrderResponse> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
        }

        public void bind(OrderResponse order, OnOrderClickListener listener) {
            // Order ID - Format based on actual id type
            String orderId = order.getId();
            String orderIdText = "Đơn hàng #" + (orderId != null ? orderId : "N/A");
            tvOrderId.setText(orderIdText);
            
            // Order Date - Format: "06/11/2025 10:46"
            if (order.getOrderDate() != null) {
                tvOrderDate.setText("Ngày: " + formatDate(order.getOrderDate()));
            } else {
                tvOrderDate.setText("Ngày: N/A");
            }
            
            // Order Status with color
            tvOrderStatus.setText(order.getStatusText());
            setStatusColor(tvOrderStatus, order.getStatus());
            
            // Order Total
            tvOrderTotal.setText(CurrencyUtils.formatPrice(order.getTotalAmount()));
            
            // Order Items count + total quantity
            int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
            int totalQuantity = 0;
            if (order.getOrderItems() != null) {
                for (namnt.vn.coffestore.data.model.order.OrderItemDetail item : order.getOrderItems()) {
                    totalQuantity += item.getQuantity();
                }
            }
            tvOrderItems.setText(itemCount + " sản phẩm (" + totalQuantity + " món)");
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
        
        private void setStatusColor(TextView statusView, String status) {
            if (status == null) return;
            
            int colorRes;
            switch (status.toUpperCase()) {
                case "PROCESSING":
                    colorRes = android.R.color.holo_orange_dark;
                    break;
                case "CONFIRMED":
                    colorRes = android.R.color.holo_blue_dark;
                    break;
                case "SHIPPING":
                    colorRes = android.R.color.holo_purple;
                    break;
                case "COMPLETED":
                    colorRes = android.R.color.holo_green_dark;
                    break;
                case "CANCELLED":
                    colorRes = android.R.color.holo_red_dark;
                    break;
                default:
                    colorRes = android.R.color.darker_gray;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                statusView.setTextColor(itemView.getContext().getResources().getColor(colorRes, null));
            } else {
                statusView.setTextColor(itemView.getContext().getResources().getColor(colorRes));
            }
        }
        
        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "N/A";
            }
            try {
                // Format: "2025-11-06T03:46:08.6205348" -> "06/11/2025 10:46"
                if (dateString.contains("T")) {
                    String[] parts = dateString.split("T");
                    String datePart = parts[0]; // "2025-11-06"
                    String timePart = parts[1].split("\\.")[0]; // "03:46:08"
                    
                    // Parse date: YYYY-MM-DD -> DD/MM/YYYY
                    String[] dateComponents = datePart.split("-");
                    if (dateComponents.length == 3) {
                        String formattedDate = dateComponents[2] + "/" + dateComponents[1] + "/" + dateComponents[0];
                        // Parse time: HH:MM:SS -> HH:MM
                        String[] timeComponents = timePart.split(":");
                        if (timeComponents.length >= 2) {
                            String formattedTime = timeComponents[0] + ":" + timeComponents[1];
                            return formattedDate + " " + formattedTime;
                        }
                        return formattedDate;
                    }
                }
                return dateString;
            } catch (Exception e) {
                return dateString;
            }
        }
    }
}


package namnt.vn.coffestore.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.ui.activities.OrderBillActivity;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    
    private Context context;
    private List<OrderResponse> orders;
    private SimpleDateFormat dateFormat;
    
    public OrderHistoryAdapter(Context context) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }
    
    public void setOrders(List<OrderResponse> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse order = orders.get(position);
        holder.bind(order);
    }
    
    @Override
    public int getItemCount() {
        return orders.size();
    }
    
    class OrderViewHolder extends RecyclerView.ViewHolder {
        
        TextView tvOrderCode, tvOrderStatus, tvOrderDate;
        TextView tvOrderItems, tvTotalPrice;
        MaterialButton btnViewDetail;
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
        
        public void bind(OrderResponse order) {
            // Order Code
            tvOrderCode.setText("#" + order.getId());
            
            // Order Status with icon and color
            String statusText = getStatusText(order.getStatus());
            String statusIcon = getStatusIcon(order.getStatus());
            tvOrderStatus.setText(statusIcon + " " + statusText);
            tvOrderStatus.setBackgroundResource(getStatusBackground(order.getStatus()));
            
            // Order Date
            try {
                String orderDate = order.getOrderDate();
                if (orderDate != null && !orderDate.isEmpty()) {
                    // Parse ISO 8601 date
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    Date date = isoFormat.parse(orderDate);
                    if (date != null) {
                        tvOrderDate.setText(dateFormat.format(date));
                    } else {
                        tvOrderDate.setText(orderDate);
                    }
                } else {
                    tvOrderDate.setText("--");
                }
            } catch (Exception e) {
                tvOrderDate.setText(order.getOrderDate());
            }
            
            // Order Items Summary
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                StringBuilder itemsSummary = new StringBuilder();
                int count = 0;
                for (namnt.vn.coffestore.data.model.order.OrderItemDetail item : order.getOrderItems()) {
                    if (count > 0) itemsSummary.append(", ");
                    
                    // Get product name from ProductWithVariant
                    String productName = "Product";
                    if (item.getProductsWithVariant() != null && 
                        item.getProductsWithVariant().getProductName() != null) {
                        productName = item.getProductsWithVariant().getProductName();
                    }
                    
                    itemsSummary.append(productName)
                            .append(" (").append(item.getVariantSize()).append(")")
                            .append(" x").append(item.getQuantity());
                    count++;
                    if (count >= 2) break; // Show max 2 items
                }
                if (order.getOrderItems().size() > 2) {
                    itemsSummary.append("...");
                }
                tvOrderItems.setText(itemsSummary.toString());
            } else {
                tvOrderItems.setText("Không có sản phẩm");
            }
            
            // Total Price
            tvTotalPrice.setText(CurrencyUtils.formatPrice(order.getTotalAmount()));
            
            // View Detail Button
            btnViewDetail.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderBillActivity.class);
                // Convert OrderResponse to JSON string
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String orderJson = gson.toJson(order);
                intent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderJson);
                context.startActivity(intent);
            });
        }
        
        private String getStatusText(String status) {
            if (status == null) return "Không xác định";
            
            switch (status.toUpperCase()) {
                case "PROCESSING":
                    return "Đang xử lý";
                case "PENDING":
                    return "Chờ xử lý";
                case "CONFIRMED":
                    return "Đã xác nhận";
                case "PREPARING":
                    return "Đang chuẩn bị";
                case "READY":
                    return "Sẵn sàng";
                case "DELIVERING":
                    return "Đang giao";
                case "COMPLETED":
                    return "Hoàn thành";
                case "CANCELLED":
                    return "Đã hủy";
                default:
                    return status;
            }
        }
        
        private int getStatusBackground(String status) {
            if (status == null) return R.drawable.bg_status_badge;
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    return R.drawable.bg_status_completed; // Green
                case "PROCESSING":
                case "PENDING":
                case "CONFIRMED":
                case "PREPARING":
                case "READY":
                case "DELIVERING":
                    return R.drawable.bg_status_processing; // Orange
                case "CANCELLED":
                    return R.drawable.bg_status_cancelled; // Gray
                default:
                    return R.drawable.bg_status_badge;
            }
        }
        
        private String getStatusIcon(String status) {
            if (status == null) return "•";
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    return "✓";
                case "PROCESSING":
                case "PENDING":
                case "CONFIRMED":
                case "PREPARING":
                    return "⏳";
                case "READY":
                case "DELIVERING":
                    return "🚚";
                case "CANCELLED":
                    return "✗";
                default:
                    return "•";
            }
        }
        
    }
}

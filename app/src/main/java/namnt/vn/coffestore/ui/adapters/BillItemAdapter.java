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
import namnt.vn.coffestore.data.model.order.OrderItemDetail;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class BillItemAdapter extends RecyclerView.Adapter<BillItemAdapter.BillItemViewHolder> {

    private List<OrderItemDetail> items;

    public BillItemAdapter() {
        this.items = new ArrayList<>();
    }

    public void setItems(List<OrderItemDetail> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new BillItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillItemViewHolder holder, int position) {
        OrderItemDetail item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BillItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName, tvItemQuantity, tvItemPrice, tvItemSize, tvItemCustomizations;

        public BillItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemSize = itemView.findViewById(R.id.tvItemSize);
            tvItemCustomizations = itemView.findViewById(R.id.tvItemCustomizations);
        }

        public void bind(OrderItemDetail item) {
            // Product name (you might need to fetch from API or pass from cart)
            tvItemName.setText(item.getProductId() != null ? "Sản phẩm" : "N/A");
            
            // Quantity
            tvItemQuantity.setText("x" + item.getQuantity());
            
            // Price (unit price * quantity)
            double totalPrice = item.getUnitPrice() * item.getQuantity();
            tvItemPrice.setText(CurrencyUtils.formatPrice(totalPrice));
            
            // Size
            String sizeText = getSizeText(item.getVariantSize());
            tvItemSize.setText("Size: " + sizeText);
            
            // Customizations
            StringBuilder customizations = new StringBuilder();
            
            if (item.getTemperature() != null && !item.getTemperature().isEmpty()) {
                customizations.append("• ").append(getTemperatureText(item.getTemperature())).append("\n");
            }
            
            if (item.getSweetness() != null && !item.getSweetness().isEmpty()) {
                customizations.append("• ").append(getSweetnessText(item.getSweetness())).append("\n");
            }
            
            if (item.getMilkType() != null && !item.getMilkType().isEmpty()) {
                customizations.append("• ").append(getMilkTypeText(item.getMilkType()));
            }
            
            if (customizations.length() > 0) {
                tvItemCustomizations.setText(customizations.toString().trim());
                tvItemCustomizations.setVisibility(View.VISIBLE);
            } else {
                tvItemCustomizations.setVisibility(View.GONE);
            }
        }

        private String getSizeText(String size) {
            if (size == null || size.isEmpty()) return "Chưa chọn";
            switch (size.toUpperCase()) {
                case "S":
                case "SMALL":
                    return "Nhỏ";
                case "M":
                case "MEDIUM":
                    return "Vừa";
                case "L":
                case "LARGE":
                    return "Lớn";
                default:
                    return size;
            }
        }

        private String getTemperatureText(String temperature) {
            if (temperature == null) return "";
            switch (temperature) {
                case "Hot": return "Nóng";
                case "ColdBrew": return "Pha lạnh";
                case "Ice": return "Đá";
                default: return temperature;
            }
        }

        private String getSweetnessText(String sweetness) {
            if (sweetness == null) return "";
            switch (sweetness) {
                case "Sweet": return "Ngọt";
                case "Normal": return "Bình thường";
                case "Less": return "Ít ngọt";
                case "NoSugar": return "Không đường";
                default: return sweetness;
            }
        }

        private String getMilkTypeText(String milkType) {
            if (milkType == null) return "";
            switch (milkType) {
                case "Dairy": return "Sữa tươi";
                case "Condensed": return "Sữa đặc";
                case "Plant": return "Sữa thực vật";
                case "None": return "Không sữa";
                default: return milkType;
            }
        }
    }
}

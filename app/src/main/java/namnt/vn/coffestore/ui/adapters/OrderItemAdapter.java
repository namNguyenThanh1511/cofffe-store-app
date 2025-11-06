package namnt.vn.coffestore.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.order.OrderItemDetail;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItemDetail> orderItems;

    public OrderItemAdapter() {
        this.orderItems = new ArrayList<>();
    }

    public void setOrderItems(List<OrderItemDetail> orderItems) {
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItemDetail item = orderItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivItemImage;
        private TextView tvItemName, tvItemSize, tvItemBasePrice, tvItemCustomization, tvItemNotes, tvItemQuantity, tvItemPrice, tvItemAddons;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemSize = itemView.findViewById(R.id.tvItemSize);
            tvItemBasePrice = itemView.findViewById(R.id.tvItemBasePrice);
            tvItemCustomization = itemView.findViewById(R.id.tvItemCustomization);
            tvItemNotes = itemView.findViewById(R.id.tvItemNotes);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemAddons = itemView.findViewById(R.id.tvItemAddons);
        }

        public void bind(OrderItemDetail item) {
            // Item Name
            String productName = "N/A";
            if (item.getProductsWithVariant() != null) {
                if (item.getProductsWithVariant().getProductName() != null) {
                    productName = item.getProductsWithVariant().getProductName();
                }
            }
            tvItemName.setText(productName);
            
            // Item Size
            String size = item.getVariantSize();
            if (size != null && !size.isEmpty()) {
                tvItemSize.setText("Size: " + getSizeText(size));
            } else {
                tvItemSize.setText("Size: N/A");
            }
            
            // Base Price từ productsWithVariant
            if (item.getProductsWithVariant() != null && item.getProductsWithVariant().getBasePrice() > 0) {
                double basePrice = item.getProductsWithVariant().getBasePrice();
                tvItemBasePrice.setText("Giá gốc: " + CurrencyUtils.formatPrice(basePrice));
                tvItemBasePrice.setVisibility(View.VISIBLE);
            } else {
                tvItemBasePrice.setVisibility(View.GONE);
            }
            
            // Customization (Temperature, Sweetness, MilkType)
            StringBuilder customization = new StringBuilder();
            if (item.getTemperature() != null && !item.getTemperature().isEmpty()) {
                customization.append(getTemperatureText(item.getTemperature()));
            }
            if (item.getSweetness() != null && !item.getSweetness().isEmpty()) {
                if (customization.length() > 0) customization.append(" • ");
                customization.append(getSweetnessText(item.getSweetness()));
            }
            if (item.getMilkType() != null && !item.getMilkType().isEmpty()) {
                if (customization.length() > 0) customization.append(" • ");
                customization.append(getMilkTypeText(item.getMilkType()));
            }
            
            if (customization.length() > 0) {
                tvItemCustomization.setVisibility(View.VISIBLE);
                tvItemCustomization.setText(customization.toString());
            } else {
                tvItemCustomization.setVisibility(View.GONE);
            }
            
            // Notes (nếu có)
            if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
                tvItemNotes.setText("Ghi chú: " + item.getNotes());
                tvItemNotes.setVisibility(View.VISIBLE);
            } else {
                tvItemNotes.setVisibility(View.GONE);
            }
            
            // Quantity
            tvItemQuantity.setText("Số lượng: " + item.getQuantity());
            
            // Total Price (unitPrice * quantity + addons)
            double unitPrice = item.getUnitPrice();
            double itemsTotal = unitPrice * item.getQuantity();
            
            // Tính tổng addons
            double addonsTotal = 0;
            if (item.getAddons() != null && !item.getAddons().isEmpty()) {
                for (namnt.vn.coffestore.data.model.Addon addon : item.getAddons()) {
                    addonsTotal += addon.getPrice();
                }
            }
            
            // Total Price = (unitPrice * quantity) + (addonsTotal * quantity)
            double totalPrice = itemsTotal + (addonsTotal * item.getQuantity());
            tvItemPrice.setText(CurrencyUtils.formatPrice(totalPrice));
            
            // Addons với giá
            if (item.getAddons() != null && !item.getAddons().isEmpty()) {
                StringBuilder addonsText = new StringBuilder("Add-ons: ");
                for (int i = 0; i < item.getAddons().size(); i++) {
                    namnt.vn.coffestore.data.model.Addon addon = item.getAddons().get(i);
                    if (i > 0) addonsText.append(", ");
                    addonsText.append(addon.getName())
                              .append(" (+")
                              .append(CurrencyUtils.formatPrice(addon.getPrice()))
                              .append(")");
                }
                tvItemAddons.setVisibility(View.VISIBLE);
                tvItemAddons.setText(addonsText.toString());
            } else {
                tvItemAddons.setVisibility(View.GONE);
            }
            
            // Item Image - placeholder
            ivItemImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        
        private String getSizeText(String size) {
            if (size == null || size.isEmpty()) {
                return "N/A";
            }
            
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
            switch (temperature.toLowerCase()) {
                case "hot":
                    return "Nóng";
                case "ice":
                    return "Đá";
                case "coldbrew":
                    return "Pha lạnh";
                default:
                    return temperature;
            }
        }
        
        private String getSweetnessText(String sweetness) {
            if (sweetness == null) return "";
            switch (sweetness.toLowerCase()) {
                case "sweet":
                    return "Ngọt";
                case "normal":
                    return "Bình thường";
                case "less":
                    return "Ít ngọt";
                case "nosugar":
                    return "Không đường";
                default:
                    return sweetness;
            }
        }
        
        private String getMilkTypeText(String milkType) {
            if (milkType == null) return "";
            switch (milkType.toLowerCase()) {
                case "dairy":
                    return "Sữa tươi";
                case "condensed":
                    return "Sữa đặc";
                case "plant":
                    return "Sữa thực vật";
                case "none":
                    return "Không sữa";
                default:
                    return milkType;
            }
        }
    }
}


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
            // Product name from ProductWithVariant
            String productName = "Sản phẩm";
            if (item.getProductsWithVariant() != null && 
                item.getProductsWithVariant().getProductName() != null) {
                productName = item.getProductsWithVariant().getProductName();
            }
            tvItemName.setText(productName);
            
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
                customizations.append("• ").append(getMilkTypeText(item.getMilkType())).append("\n");
            }
            
            // Add-ons/Toppings
            String addonsText = parseAddons(item.getAddOns());
            if (!addonsText.isEmpty()) {
                customizations.append(addonsText);
            }
            
            if (customizations.length() > 0) {
                tvItemCustomizations.setText(customizations.toString().trim());
                tvItemCustomizations.setVisibility(View.VISIBLE);
            } else {
                tvItemCustomizations.setVisibility(View.GONE);
            }
        }
        
        private String parseAddons(Object addOnsObj) {
            if (addOnsObj == null) {
                android.util.Log.d("BillItemAdapter", "addOnsObj is null");
                return "";
            }
            
            try {
                // Parse addons JSON
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String json = gson.toJson(addOnsObj);
                
                android.util.Log.d("BillItemAdapter", "Addons JSON: " + json);
                
                // Try to parse as array (compatible with older Gson versions)
                com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
                com.google.gson.JsonElement element = parser.parse(json);
                
                if (!element.isJsonArray()) {
                    android.util.Log.d("BillItemAdapter", "Not a JSON array");
                    return "";
                }
                
                com.google.gson.JsonArray jsonArray = element.getAsJsonArray();
                
                android.util.Log.d("BillItemAdapter", "Array size: " + jsonArray.size());
                
                if (jsonArray.size() == 0) return "";
                
                StringBuilder addonsText = new StringBuilder();
                for (int i = 0; i < jsonArray.size(); i++) {
                    com.google.gson.JsonObject addonObj = jsonArray.get(i).getAsJsonObject();
                    
                    String name = addonObj.has("name") ? addonObj.get("name").getAsString() : "";
                    double price = addonObj.has("price") ? addonObj.get("price").getAsDouble() : 0;
                    
                    android.util.Log.d("BillItemAdapter", "Addon: " + name + " - " + price);
                    
                    if (!name.isEmpty()) {
                        addonsText.append("• ").append(name);
                        if (price > 0) {
                            addonsText.append(" (+").append(CurrencyUtils.formatPrice(price)).append(")");
                        }
                        addonsText.append("\n");
                    }
                }
                
                android.util.Log.d("BillItemAdapter", "Final addons text: " + addonsText.toString());
                
                return addonsText.toString();
            } catch (Exception e) {
                // If parsing fails, return empty
                android.util.Log.e("BillItemAdapter", "Error parsing addons: " + e.getMessage());
                e.printStackTrace();
                return "";
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

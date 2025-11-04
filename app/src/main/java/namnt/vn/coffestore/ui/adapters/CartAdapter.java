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
import namnt.vn.coffestore.data.model.CartItem;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter() {
        this.cartItems = new ArrayList<>();
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    public void setOnCartItemListener(OnCartItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCartItemImage, btnDecreaseQty, btnIncreaseQty, btnRemoveItem;
        private TextView tvCartItemName, tvCartItemSize, tvCartItemCustomization, tvCartItemPrice, tvCartItemQty;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivCartItemImage = itemView.findViewById(R.id.ivCartItemImage);
            btnDecreaseQty = itemView.findViewById(R.id.btnDecreaseQty);
            btnIncreaseQty = itemView.findViewById(R.id.btnIncreaseQty);
            btnRemoveItem = itemView.findViewById(R.id.btnRemoveItem);
            
            tvCartItemName = itemView.findViewById(R.id.tvCartItemName);
            tvCartItemSize = itemView.findViewById(R.id.tvCartItemSize);
            tvCartItemCustomization = itemView.findViewById(R.id.tvCartItemCustomization);
            tvCartItemPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvCartItemQty = itemView.findViewById(R.id.tvCartItemQty);
        }

        public void bind(CartItem item, OnCartItemListener listener) {
            tvCartItemName.setText(item.getName());
            
            // Format size display
            String rawSize = item.getSize();
            android.util.Log.d("CartAdapter", "Raw size: '" + rawSize + "' (null=" + (rawSize == null) + ")");
            String sizeText = getSizeText(rawSize);
            tvCartItemSize.setText("Size: " + sizeText);
            
            tvCartItemPrice.setText(CurrencyUtils.formatPrice(item.getTotal()));
            tvCartItemQty.setText(String.valueOf(item.getQuantity()));

            // Build customization text
            StringBuilder customization = new StringBuilder();
            if (item.getTemperatureText() != null && !item.getTemperatureText().isEmpty()) {
                customization.append(item.getTemperatureText());
            }
            if (item.getSweetnessText() != null && !item.getSweetnessText().isEmpty()) {
                if (customization.length() > 0) customization.append(" • ");
                customization.append(item.getSweetnessText());
            }
            if (item.getMilkTypeText() != null && !item.getMilkTypeText().isEmpty()) {
                if (customization.length() > 0) customization.append(" • ");
                customization.append(item.getMilkTypeText());
            }
            
            if (customization.length() > 0) {
                tvCartItemCustomization.setText(customization.toString());
                tvCartItemCustomization.setVisibility(View.VISIBLE);
            } else {
                tvCartItemCustomization.setVisibility(View.GONE);
            }

            // Load image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .centerCrop()
                    .into(ivCartItemImage);
            }

            // Decrease quantity
            btnDecreaseQty.setOnClickListener(v -> {
                if (listener != null) {
                    int newQty = item.getQuantity() - 1;
                    if (newQty > 0) {
                        listener.onQuantityChanged(item, newQty);
                    } else {
                        listener.onItemRemoved(item);
                    }
                }
            });

            // Increase quantity
            btnIncreaseQty.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            });

            // Remove item
            btnRemoveItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item);
                }
            });
        }
        
        private String getSizeText(String size) {
            if (size == null || size.isEmpty()) {
                return "Chưa chọn";
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
    }
}

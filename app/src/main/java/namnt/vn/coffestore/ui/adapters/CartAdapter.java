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
        private TextView tvCartItemName, tvCartItemSize, tvCartItemPrice, tvCartItemQty;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivCartItemImage = itemView.findViewById(R.id.ivCartItemImage);
            btnDecreaseQty = itemView.findViewById(R.id.btnDecreaseQty);
            btnIncreaseQty = itemView.findViewById(R.id.btnIncreaseQty);
            btnRemoveItem = itemView.findViewById(R.id.btnRemoveItem);
            
            tvCartItemName = itemView.findViewById(R.id.tvCartItemName);
            tvCartItemSize = itemView.findViewById(R.id.tvCartItemSize);
            tvCartItemPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvCartItemQty = itemView.findViewById(R.id.tvCartItemQty);
        }

        public void bind(CartItem item, OnCartItemListener listener) {
            tvCartItemName.setText(item.getName());
            tvCartItemSize.setText(item.getSize());
            tvCartItemPrice.setText(String.format("$%.2f", item.getTotal()));
            tvCartItemQty.setText(String.valueOf(item.getQuantity()));

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
    }
}

package namnt.vn.coffestore.ui.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.CoffeeItem;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.CoffeeViewHolder> {
    
    private List<CoffeeItem> coffeeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CoffeeItem item);
    }

    public MenuAdapter() {
        this.coffeeList = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCoffeeList(List<CoffeeItem> coffeeList) {
        this.coffeeList = coffeeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoffeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coffee, parent, false);
        return new CoffeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoffeeViewHolder holder, int position) {
        CoffeeItem item = coffeeList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return coffeeList.size();
    }

    static class CoffeeViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCoffeeImage;
        private TextView tvNoImage;
        private TextView tvCoffeeName;
        private TextView tvPrice;
        private TextView tvOldPrice;

        public CoffeeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCoffeeImage = itemView.findViewById(R.id.ivCoffeeImage);
            tvNoImage = itemView.findViewById(R.id.tvNoImage);
            tvCoffeeName = itemView.findViewById(R.id.tvCoffeeName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
        }

        public void bind(CoffeeItem item, OnItemClickListener listener) {
            tvCoffeeName.setText(item.getName());
            tvPrice.setText(String.format("$%.2f", item.getPrice()));

            // Show/hide old price with strikethrough
            if (item.getOldPrice() != null && item.getOldPrice() > 0) {
                tvOldPrice.setVisibility(View.VISIBLE);
                tvOldPrice.setText(String.format("$%.2f", item.getOldPrice()));
                tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvOldPrice.setVisibility(View.GONE);
            }

            // Handle image - Load from URL with Glide
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                tvNoImage.setVisibility(View.GONE);
                ivCoffeeImage.setVisibility(View.VISIBLE);
                
                Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(ivCoffeeImage);
            } else {
                tvNoImage.setVisibility(View.VISIBLE);
                ivCoffeeImage.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}

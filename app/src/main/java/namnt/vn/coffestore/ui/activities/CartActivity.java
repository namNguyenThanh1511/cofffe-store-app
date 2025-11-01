package namnt.vn.coffestore.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.CartItem;
import namnt.vn.coffestore.ui.adapters.CartAdapter;

public class CartActivity extends AppCompatActivity {

    private ImageView btnBack, btnClearCart;
    private RecyclerView rvCartItems;
    private LinearLayout emptyCartLayout, summaryLayout;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private MaterialButton btnCheckout;
    
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    
    private static final double DELIVERY_FEE = 2.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        loadSampleData();
        setupClickListeners();
        updateCartSummary();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnClearCart = findViewById(R.id.btnClearCart);
        rvCartItems = findViewById(R.id.rvCartItems);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        summaryLayout = findViewById(R.id.summaryLayout);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter();
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        cartAdapter.setOnCartItemListener(new CartAdapter.OnCartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                item.setQuantity(newQuantity);
                cartAdapter.notifyDataSetChanged();
                updateCartSummary();
                Toast.makeText(CartActivity.this, "Quantity updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemRemoved(CartItem item) {
                new AlertDialog.Builder(CartActivity.this)
                    .setTitle("Remove Item")
                    .setMessage("Remove " + item.getName() + " from cart?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        cartItems.remove(item);
                        cartAdapter.setCartItems(cartItems);
                        updateCartSummary();
                        Toast.makeText(CartActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
    }

    private void loadSampleData() {
        cartItems = new ArrayList<>();
        
        // Sample cart items - Replace with actual cart data later
        cartItems.add(new CartItem("1", "Creamy Coffee", 4.00,
            "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400", 
            "Medium", 2));
        cartItems.add(new CartItem("2", "Hot Creamy", 5.80,
            "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400",
            "Large", 1));
        cartItems.add(new CartItem("3", "Cappuccino", 4.30,
            "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400",
            "Small", 3));
        
        cartAdapter.setCartItems(cartItems);
    }

    private void updateCartSummary() {
        if (cartItems.isEmpty()) {
            // Show empty cart
            emptyCartLayout.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            summaryLayout.setVisibility(View.GONE);
        } else {
            // Show cart items
            emptyCartLayout.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            summaryLayout.setVisibility(View.VISIBLE);
            
            // Calculate totals
            double subtotal = 0;
            for (CartItem item : cartItems) {
                subtotal += item.getTotal();
            }
            
            double total = subtotal + DELIVERY_FEE;
            
            // Update UI
            tvSubtotal.setText(String.format("$%.2f", subtotal));
            tvDeliveryFee.setText(String.format("$%.2f", DELIVERY_FEE));
            tvTotal.setText(String.format("$%.2f", total));
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearCart.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                new AlertDialog.Builder(this)
                    .setTitle("Clear Cart")
                    .setMessage("Remove all items from cart?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        cartItems.clear();
                        cartAdapter.setCartItems(cartItems);
                        updateCartSummary();
                        Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        btnCheckout.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
                // TODO: Implement checkout flow
            }
        });
    }
}

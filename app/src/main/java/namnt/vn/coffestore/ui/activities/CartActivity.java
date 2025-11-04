package namnt.vn.coffestore.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.CartItem;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.OrderItemDetail;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.CartAdapter;
import namnt.vn.coffestore.utils.CurrencyUtils;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";

    private ImageView btnBack, btnClearCart;
    private RecyclerView rvCartItems;
    private LinearLayout emptyCartLayout, summaryLayout;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private MaterialButton btnCheckout;
    
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private List<OrderResponse> orders;
    
    private ApiService apiService;
    private AuthViewModel authViewModel;
    
    private static final double DELIVERY_FEE = 0; // Free delivery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initAuthViewModel();
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        
        // Load orders from API
        loadOrdersFromApi();
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

    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AuthViewModel(getApplication());
            }
        }).get(AuthViewModel.class);
    }
    
    private void loadOrdersFromApi() {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String bearerToken = "Bearer " + accessToken;
        Log.d(TAG, "Loading orders from API...");
        
        Call<ApiResponse<List<OrderResponse>>> call = apiService.getOrders(bearerToken);
        call.enqueue(new Callback<ApiResponse<List<OrderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrderResponse>>> call, Response<ApiResponse<List<OrderResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<OrderResponse>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        orders = apiResponse.getData();
                        Log.d(TAG, "Loaded " + orders.size() + " orders");
                        
                        // Convert orders to cart items
                        convertOrdersToCartItems();
                        updateCartSummary();
                    } else {
                        Log.e(TAG, "API response not successful");
                        showEmptyCart();
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    Toast.makeText(CartActivity.this, "Lỗi tải đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmptyCart();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<OrderResponse>>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCart();
            }
        });
    }
    
    private void convertOrdersToCartItems() {
        cartItems = new ArrayList<>();
        
        for (OrderResponse order : orders) {
            if (order.getOrderItems() != null) {
                for (OrderItemDetail item : order.getOrderItems()) {
                    // Debug log
                    Log.d(TAG, "OrderItem - variantSize: " + item.getVariantSize() + 
                          ", productId: " + item.getProductId());
                    
                    // Create CartItem from OrderItemDetail with customization
                    CartItem cartItem = new CartItem(
                        item.getId(),
                        "Loading...", // Product name - will be loaded from API
                        item.getUnitPrice(),
                        null, // Image URL - will be loaded from API
                        item.getVariantSize(),
                        item.getQuantity(),
                        item.getTemperature(),
                        item.getSweetness(),
                        item.getMilkType()
                    );
                    cartItems.add(cartItem);
                    
                    // Load product details asynchronously
                    loadProductDetails(item.getProductId(), cartItem);
                }
            }
        }
        
        cartAdapter.setCartItems(cartItems);
        Log.d(TAG, "Converted to " + cartItems.size() + " cart items");
    }
    
    private void loadProductDetails(String productId, CartItem cartItem) {
        if (productId == null || productId.isEmpty()) {
            cartItem.setName("Unknown Product");
            cartAdapter.notifyDataSetChanged();
            return;
        }
        
        Call<ApiResponse<namnt.vn.coffestore.data.model.Product>> call = apiService.getProductById(productId);
        call.enqueue(new Callback<ApiResponse<namnt.vn.coffestore.data.model.Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<namnt.vn.coffestore.data.model.Product>> call, 
                                 Response<ApiResponse<namnt.vn.coffestore.data.model.Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<namnt.vn.coffestore.data.model.Product> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        namnt.vn.coffestore.data.model.Product product = apiResponse.getData();
                        cartItem.setName(product.getName());
                        cartItem.setImageUrl(product.getImageUrl());
                        cartAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded product: " + product.getName());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<namnt.vn.coffestore.data.model.Product>> call, Throwable t) {
                Log.e(TAG, "Failed to load product " + productId + ": " + t.getMessage());
                cartItem.setName("Product #" + productId.substring(0, Math.min(8, productId.length())));
                cartAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private void showEmptyCart() {
        cartItems = new ArrayList<>();
        cartAdapter.setCartItems(cartItems);
        updateCartSummary();
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
            tvSubtotal.setText(CurrencyUtils.formatPrice(subtotal));
            tvDeliveryFee.setText(CurrencyUtils.formatPrice(DELIVERY_FEE));
            tvTotal.setText(CurrencyUtils.formatPrice(total));
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
                Toast.makeText(this, "Chức năng thanh toán đang phát triển...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

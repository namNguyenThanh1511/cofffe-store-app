package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.CartItem;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.CartAdapter;
import namnt.vn.coffestore.utils.CartManager;
import namnt.vn.coffestore.utils.CurrencyUtils;
import namnt.vn.coffestore.utils.NotificationHelper;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import namnt.vn.coffestore.data.model.order.OrderItem;
import namnt.vn.coffestore.data.model.order.OrderRequest;
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
        
        // Load cart from local storage
        loadCartFromLocal();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart when returning to this activity
        loadCartFromLocal();
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
                // Update in local storage
                int position = cartItems.indexOf(item);
                if (position >= 0) {
                    CartManager.getInstance(CartActivity.this).updateItemQuantity(position, newQuantity);
                    loadCartFromLocal(); // Refresh from local
                    Toast.makeText(CartActivity.this, "Đã cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemRemoved(CartItem item) {
                new AlertDialog.Builder(CartActivity.this)
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Xóa " + item.getName() + " khỏi giỏ hàng?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        int position = cartItems.indexOf(item);
                        if (position >= 0) {
                            CartManager.getInstance(CartActivity.this).removeItem(position);
                            loadCartFromLocal(); // Refresh from local
                            Toast.makeText(CartActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
            
            @Override
            public void onItemSelectionChanged(CartItem item, boolean isSelected) {
                // Update selection in local storage
                CartManager.getInstance(CartActivity.this).addItem(item);
                // Recalculate summary for selected items only
                updateCartSummary();
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
    
    private void loadCartFromLocal() {
        Log.d(TAG, "Loading cart from local storage...");
        
        // Load from CartManager
        cartItems = CartManager.getInstance(this).getCartItems();
        
        Log.d(TAG, "Loaded " + cartItems.size() + " items from local cart");
        
        // Update adapter
        cartAdapter.setCartItems(cartItems);
        
        // Update summary
        updateCartSummary();
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
            
            // Calculate totals for SELECTED items only
            double subtotal = 0;
            int selectedCount = 0;
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    subtotal += item.getTotal();
                    selectedCount++;
                }
            }
            
            double total = subtotal + DELIVERY_FEE;
            
            // Update UI
            tvSubtotal.setText(CurrencyUtils.formatPrice(subtotal));
            tvDeliveryFee.setText(CurrencyUtils.formatPrice(DELIVERY_FEE));
            tvTotal.setText(CurrencyUtils.formatPrice(total));
            
            Log.d(TAG, "Selected items: " + selectedCount + "/" + cartItems.size());
            Log.d(TAG, "Selected subtotal: " + subtotal);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearCart.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                new AlertDialog.Builder(this)
                    .setTitle("Xóa giỏ hàng")
                    .setMessage("Xóa tất cả sản phẩm khỏi giỏ hàng?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        CartManager.getInstance(this).clearCart();
                        loadCartFromLocal();
                        Toast.makeText(this, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        });

        btnCheckout.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (CartItem item : cartItems) {
                if (item.isSelected()) selectedCount++;
            }
            
            if (selectedCount > 0) {
                checkout();
            } else {
                Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void checkout() {
        // Check authentication
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create OrderItems from SELECTED CartItems only
        List<OrderItem> orderItems = new ArrayList<>();
        final List<Integer> selectedPositions = new ArrayList<>();
        
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            if (cartItem.isSelected()) {
                OrderItem orderItem = new OrderItem(
                    cartItem.getVariantId(),
                    cartItem.getQuantity(),
                    convertTemperatureToInt(cartItem.getTemperature()),
                    convertSweetnessToInt(cartItem.getSweetness()),
                    convertMilkTypeToInt(cartItem.getMilkType()),
                    cartItem.getSelectedAddonIds()
                );
                orderItems.add(orderItem);
                selectedPositions.add(i);
            }
        }
        
        // Create OrderRequest
        OrderRequest orderRequest = new OrderRequest(
            0, // deliveryType = 0
            orderItems
        );
        
        // Log request
        Log.d(TAG, "=== CHECKOUT REQUEST ===");
        Log.d(TAG, "Total items: " + orderItems.size());
        Log.d(TAG, "Request JSON: " + new com.google.gson.Gson().toJson(orderRequest));
        
        // Disable button
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");
        
        // Call API
        String bearerToken = "Bearer " + accessToken;
        Call<ApiResponse<OrderResponse>> call = apiService.createOrder(bearerToken, orderRequest);
        call.enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call, Response<ApiResponse<OrderResponse>> response) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Thanh toán");
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OrderResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Checkout successful!");
                        
                        OrderResponse orderResponse = apiResponse.getData();
                        
                        // Log all response headers for debugging
                        Log.d(TAG, "Response Headers:");
                        for (String headerName : response.headers().names()) {
                            Log.d(TAG, headerName + ": " + response.headers().get(headerName));
                        }
                        
                        // Get payment URL from response header (try multiple variations)
                        String paymentUrl = response.headers().get("x-forward-payment");
                        if (paymentUrl == null) paymentUrl = response.headers().get("X-Forward-Payment");
                        if (paymentUrl == null) paymentUrl = response.headers().get("X-Foraward-Payment");
                        if (paymentUrl == null) paymentUrl = response.headers().get("x-foraward-payment");
                        
                        Log.d(TAG, "Payment URL: " + paymentUrl);
                        
                        if (paymentUrl == null || paymentUrl.isEmpty()) {
                            Toast.makeText(CartActivity.this, "Không tìm thấy URL thanh toán. Check logs!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // Remove only SELECTED items from local cart
                        // Sort in descending order to avoid index shifting
                        Collections.sort(selectedPositions, Collections.reverseOrder());
                        for (int position : selectedPositions) {
                            CartManager.getInstance(CartActivity.this).removeItem(position);
                        }
                        
                        // Check remaining items
                        int remainingCount = CartManager.getInstance(CartActivity.this).getCartItemCount();
                        if (remainingCount > 0) {
                            // Still have items - Show stage 2
                            NotificationHelper.updateNotificationStage(CartActivity.this, 2, remainingCount);
                        } else {
                            // No items left - Show stage 3, then hide
                            NotificationHelper.updateNotificationStage(CartActivity.this, 3, 0);
                        }
                        
                        // Navigate to payment WebView
                        showPaymentWebView(paymentUrl, orderResponse);
                    } else {
                        Toast.makeText(CartActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Checkout failed: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        Log.e(TAG, "Error body: " + errorBody);
                        Toast.makeText(CartActivity.this, "Thanh toán thất bại: " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(CartActivity.this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Thanh toán");
                
                Log.e(TAG, "Checkout failed: " + t.getMessage(), t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showPaymentWebView(String paymentUrl, OrderResponse orderResponse) {
        // Navigate to PaymentWebViewActivity with payment URL
        Intent intent = new Intent(this, PaymentWebViewActivity.class);
        
        // Pass payment URL and order data as JSON
        Gson gson = new Gson();
        String orderJson = gson.toJson(orderResponse);
        intent.putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl);
        intent.putExtra(PaymentWebViewActivity.EXTRA_ORDER_RESPONSE, orderJson);
        
        startActivity(intent);
        finish();
    }
    
    // Helper methods to convert String to int for API
    private int convertTemperatureToInt(String temperature) {
        if (temperature == null) return 0;
        switch (temperature) {
            case "Hot": return 0;
            case "ColdBrew": return 1;
            case "Ice": return 2;
            default: return 0;
        }
    }
    
    private int convertSweetnessToInt(String sweetness) {
        if (sweetness == null) return 1;
        switch (sweetness) {
            case "Sweet": return 0;
            case "Normal": return 1;
            case "Less": return 2;
            case "NoSugar": return 3;
            default: return 1;
        }
    }
    
    private int convertMilkTypeToInt(String milkType) {
        if (milkType == null) return 3;
        switch (milkType) {
            case "Dairy": return 0;
            case "Condensed": return 1;
            case "Plant": return 2;
            case "None": return 3;
            default: return 3;
        }
    }
}

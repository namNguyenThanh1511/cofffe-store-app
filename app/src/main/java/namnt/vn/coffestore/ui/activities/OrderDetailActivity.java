package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.OrderItemDetail;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.OrderItemAdapter;
import namnt.vn.coffestore.utils.CurrencyUtils;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private static final String TAG = "OrderDetailActivity";

    private ImageView btnBack;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvCustomerId;
    private RecyclerView rvOrderItems;
    private LinearLayout emptyStateLayout;
    
    private OrderItemAdapter orderItemAdapter;
    private List<OrderItemDetail> orderItems;
    
    private ApiService apiService;
    private AuthViewModel authViewModel;
    private String orderId;
    private String currentRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initAuthViewModel();
        if (authViewModel.getAccessToken().isEmpty()) {
            redirectToLogin();
            return;
        }
        
        // Get order ID from intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        
        // Load order details (không cần check role, endpoint dùng chung)
        loadOrderDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvCustomerId = findViewById(R.id.tvCustomerId);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter();
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private String getUserRole() {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        int roleIdx = prefs.getInt("user_role", 0); // 0=Customer, 1=Admin, 2=Barista
        
        switch (roleIdx) {
            case 1: return "ADMIN";
            case 2: return "BARISTA";
            default: return "CUSTOMER";
        }
    }
    
    private String formatOrderId(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            return orderId;
        }
        
        // Nếu orderId có dạng "2.0" hoặc số thực, convert về số nguyên
        try {
            // Thử parse như double trước
            double doubleValue = Double.parseDouble(orderId);
            int intValue = (int) doubleValue;
            return String.valueOf(intValue);
        } catch (NumberFormatException e) {
            // Nếu không phải số, trả về nguyên bản
            return orderId;
        }
    }

    private void loadOrderDetails() {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Log.e(TAG, "Access token is empty");
            return;
        }
        
        String bearerToken = "Bearer " + accessToken;
        
        // Format orderId - đảm bảo là số nguyên (không có .0)
        final String formattedOrderId = formatOrderId(orderId);
        Log.d(TAG, "Loading order details for ID: " + formattedOrderId + " (original: " + orderId + ")");

        // Dùng endpoint chung /api/orders/{id} cho tất cả roles
        Call<ApiResponse<OrderResponse>> call = apiService.getOrderById(bearerToken, formattedOrderId);
        
        call.enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call,
                                 Response<ApiResponse<OrderResponse>> response) {
                Log.d(TAG, "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OrderResponse> apiResponse = response.body();
                    Log.d(TAG, "API success: " + apiResponse.isSuccess());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OrderResponse order = apiResponse.getData();
                        Log.d(TAG, "Order loaded successfully: ID=" + order.getId());
                        displayOrderDetails(order);
                    } else {
                        Log.e(TAG, "API response not successful: " + apiResponse.getMessage());
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Không thể tải chi tiết đơn hàng";
                        Toast.makeText(OrderDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    // Log chi tiết lỗi
                    String errorBody = null;
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    
                    Log.e(TAG, "Response not successful: " + response.code());
                    String errorMsg = "Lỗi server: " + response.code();
                    Toast.makeText(OrderDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                }
                Toast.makeText(OrderDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void displayOrderDetails(OrderResponse order) {
        // Order ID
        String orderId = order.getId();
        String orderIdText = "Đơn hàng #" + (orderId != null ? orderId : "N/A");
        tvOrderId.setText(orderIdText);
        
        // Order Date - Format: "📅 Ngày: 06/11/2025 10:46"
        if (order.getOrderDate() != null) {
            tvOrderDate.setText("📅 Ngày: " + formatDate(order.getOrderDate()));
        } else {
            tvOrderDate.setText("📅 Ngày: N/A");
        }
        
        // Order Status
        tvOrderStatus.setText(order.getStatusText());
        setStatusColor(tvOrderStatus, order.getStatus());
        
        // Order Total
        tvOrderTotal.setText(CurrencyUtils.formatPrice(order.getTotalAmount()));
        
        // Customer ID - Chỉ hiển thị một phần (không cần full GUID)
        if (order.getCustomerId() != null) {
            String customerId = order.getCustomerId();
            // Nếu là GUID quá dài, chỉ hiển thị 8 ký tự đầu
            if (customerId.length() > 8) {
                customerId = customerId.substring(0, 8) + "...";
            }
            tvCustomerId.setText("👤 Khách hàng: " + customerId);
        } else {
            tvCustomerId.setText("👤 Khách hàng: N/A");
        }
        
        // Order Items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            hideEmptyState();
            orderItems = order.getOrderItems();
            orderItemAdapter.setOrderItems(orderItems);
        } else {
            showEmptyState();
        }
    }

    private void setStatusColor(TextView statusView, String status) {
        if (status == null) return;
        
        int colorRes;
        switch (status.toUpperCase()) {
            case "PROCESSING":
                colorRes = android.R.color.holo_orange_dark;
                break;
            case "CONFIRMED":
                colorRes = android.R.color.holo_blue_dark;
                break;
            case "SHIPPING":
                colorRes = android.R.color.holo_purple;
                break;
            case "COMPLETED":
                colorRes = android.R.color.holo_green_dark;
                break;
            case "CANCELLED":
                colorRes = android.R.color.holo_red_dark;
                break;
            default:
                colorRes = android.R.color.darker_gray;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            statusView.setTextColor(getResources().getColor(colorRes, null));
        } else {
            statusView.setTextColor(getResources().getColor(colorRes));
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "N/A";
        }
        try {
            // Format: "2025-11-06T03:46:08.6205348" -> "06/11/2025 10:46"
            if (dateString.contains("T")) {
                String[] parts = dateString.split("T");
                String datePart = parts[0]; // "2025-11-06"
                String timePart = parts[1].split("\\.")[0]; // "03:46:08"
                
                // Parse date: YYYY-MM-DD -> DD/MM/YYYY
                String[] dateComponents = datePart.split("-");
                if (dateComponents.length == 3) {
                    String formattedDate = dateComponents[2] + "/" + dateComponents[1] + "/" + dateComponents[0];
                    // Parse time: HH:MM:SS -> HH:MM
                    String[] timeComponents = timePart.split(":");
                    if (timeComponents.length >= 2) {
                        String formattedTime = timeComponents[0] + ":" + timeComponents[1];
                        return formattedDate + " " + formattedTime;
                    }
                    return formattedDate;
                }
            }
            return dateString;
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        rvOrderItems.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateLayout.setVisibility(View.GONE);
        rvOrderItems.setVisibility(View.VISIBLE);
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

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


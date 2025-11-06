package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.OrderAdapter;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderManagementActivity extends AppCompatActivity {
    private static final String TAG = "OrderManagementActivity";

    private ImageView btnBack;
    private RecyclerView rvOrders;
    private LinearLayout emptyStateLayout;
    private Spinner spinnerStatus, spinnerPaymentStatus;
    private TextView tvTitle;
    
    private OrderAdapter orderAdapter;
    private List<OrderResponse> allOrders;
    private List<OrderResponse> filteredOrders;
    
    private ApiService apiService;
    private AuthViewModel authViewModel;
    private String currentRole;
    private String currentStatusFilter = null;
    private String currentPaymentStatusFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initAuthViewModel();
        if (authViewModel.getAccessToken().isEmpty()) {
            redirectToLogin();
            return;
        }
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSpinners();
        
        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        
        // Get user role
        currentRole = getUserRole();
        
        // Load orders based on role
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerPaymentStatus = findViewById(R.id.spinnerPaymentStatus);
        tvTitle = findViewById(R.id.tvTitle);
        
        // Set title based on role (will be updated after loading role)
        tvTitle.setText("Quản lý đơn hàng");
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
        
        orderAdapter.setOnOrderClickListener(order -> {
            // Navigate to order detail
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Status spinner
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Tất cả trạng thái");
        statusOptions.add("Đang xử lý");
        statusOptions.add("Đã xác nhận");
        statusOptions.add("Đang giao");
        statusOptions.add("Hoàn thành");
        statusOptions.add("Đã hủy");
        
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentStatusFilter = null;
                } else {
                    // Map Vietnamese to English status
                    currentStatusFilter = mapVietnameseToStatus(statusOptions.get(position));
                }
                filterOrders();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentStatusFilter = null;
            }
        });
        
        // Payment status spinner
        List<String> paymentOptions = new ArrayList<>();
        paymentOptions.add("Tất cả thanh toán");
        paymentOptions.add("Chưa thanh toán");
        paymentOptions.add("Đã thanh toán");
        
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, paymentOptions);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentStatus.setAdapter(paymentAdapter);
        
        spinnerPaymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentPaymentStatusFilter = null;
                } else {
                    // Map Vietnamese to English payment status
                    currentPaymentStatusFilter = mapVietnameseToPaymentStatus(paymentOptions.get(position));
                }
                filterOrders();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentPaymentStatusFilter = null;
            }
        });
    }

    private String mapVietnameseToStatus(String vietnamese) {
        switch (vietnamese) {
            case "Đang xử lý": return "PROCESSING";
            case "Đã xác nhận": return "CONFIRMED";
            case "Đang giao": return "SHIPPING";
            case "Hoàn thành": return "COMPLETED";
            case "Đã hủy": return "CANCELLED";
            default: return vietnamese;
        }
    }

    private String mapVietnameseToPaymentStatus(String vietnamese) {
        switch (vietnamese) {
            case "Chưa thanh toán": return "UNPAID";
            case "Đã thanh toán": return "PAID";
            default: return vietnamese;
        }
    }

    private String getUserRole() {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        int roleIdx = prefs.getInt("user_role", 0); // 0=Customer, 1=Admin, 2=Barista
        
        // Also try to get from UserProfile
        // For now, return based on roleIdx
        switch (roleIdx) {
            case 1: return "ADMIN";
            case 2: return "BARISTA";
            default: return "CUSTOMER";
        }
    }

    private void loadOrders() {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Log.e(TAG, "Access token is empty");
            return;
        }
        
        String bearerToken = "Bearer " + accessToken;
        Log.d(TAG, "Loading orders for role: " + currentRole);

        // Build filters - chỉ thêm vào query nếu có giá trị
        List<String> statuses = null;
        if (currentStatusFilter != null && !currentStatusFilter.isEmpty()) {
            statuses = new ArrayList<>();
            statuses.add(currentStatusFilter);
            Log.d(TAG, "Filter by status: " + currentStatusFilter);
        }
        
        List<String> paymentStatuses = null;
        if (currentPaymentStatusFilter != null && !currentPaymentStatusFilter.isEmpty()) {
            paymentStatuses = new ArrayList<>();
            paymentStatuses.add(currentPaymentStatusFilter);
            Log.d(TAG, "Filter by payment status: " + currentPaymentStatusFilter);
        }

        Call<ApiResponse<List<OrderResponse>>> call;
        try {
            if ("ADMIN".equalsIgnoreCase(currentRole) || "BARISTA".equalsIgnoreCase(currentRole)) {
                Log.d(TAG, "Calling admin-barista endpoint");
                call = apiService.getOrdersForAdminBarista(
                    bearerToken,
                    null, // Search
                    null, // SortBy
                    null, // SortOrder
                    null, // Field
                    statuses, // Statuses
                    null, // DeliveryTypes
                    paymentStatuses, // PaymentStatuses
                    null, // SelectFields
                    null, // PageNumber
                    null  // PageSize
                );
            } else {
                Log.d(TAG, "Calling customer endpoint");
                call = apiService.getOrders(
                    bearerToken,
                    null, // Search
                    null, // SortBy
                    null, // SortOrder
                    null, // Field
                    statuses, // Statuses
                    null, // DeliveryTypes
                    paymentStatuses, // PaymentStatuses
                    null, // SelectFields
                    null, // PageNumber
                    null  // PageSize
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating API call: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tạo request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            showEmptyState();
            return;
        }
        call.enqueue(new Callback<ApiResponse<List<OrderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrderResponse>>> call,
                                 Response<ApiResponse<List<OrderResponse>>> response) {
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<OrderResponse>> apiResponse = response.body();
                    Log.d(TAG, "API success: " + apiResponse.isSuccess());
                    Log.d(TAG, "API message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        allOrders = apiResponse.getData();
                        Log.d(TAG, "Loaded " + allOrders.size() + " orders");
                        filterOrders();
                    } else {
                        Log.e(TAG, "API response not successful: " + apiResponse.getMessage());
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Không thể tải danh sách đơn hàng";
                        Toast.makeText(OrderManagementActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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
                    if (errorBody != null && errorBody.contains("message")) {
                        // Try to extract message from error body if possible
                        errorMsg = "Lỗi: " + response.code();
                    }
                    Toast.makeText(OrderManagementActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<OrderResponse>>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                }
                Toast.makeText(OrderManagementActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void filterOrders() {
        if (allOrders == null) {
            allOrders = new ArrayList<>();
        }
        
        filteredOrders = new ArrayList<>();
        
        for (OrderResponse order : allOrders) {
            boolean matchStatus = true;
            boolean matchPayment = true;
            
            // Filter by status
            if (currentStatusFilter != null) {
                matchStatus = order.getStatus() != null &&
                    order.getStatus().equalsIgnoreCase(currentStatusFilter);
            }
            
            // Filter by payment status
            // TODO: Add paymentStatus field to OrderResponse when API provides it
            // For now, skip payment status filter
            if (currentPaymentStatusFilter != null) {
                // matchPayment = order.getPaymentStatus() != null &&
                //     order.getPaymentStatus().equalsIgnoreCase(currentPaymentStatusFilter);
                matchPayment = true; // Temporary: skip payment filter until API provides it
            }
            
            if (matchStatus && matchPayment) {
                filteredOrders.add(order);
            }
        }
        
        // Update UI
        if (filteredOrders.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            orderAdapter.setOrders(filteredOrders);
        }
        
        Log.d(TAG, "Filtered orders: " + filteredOrders.size() + " / " + allOrders.size());
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateLayout.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);
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


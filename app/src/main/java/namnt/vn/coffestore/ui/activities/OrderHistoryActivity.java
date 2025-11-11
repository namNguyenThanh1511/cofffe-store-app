package namnt.vn.coffestore.ui.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.OrderHistoryAdapter;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {
    
    private static final String TAG = "OrderHistoryActivity";
    
    // Views
    private ImageView btnBack;
    private TextView btnFilterAll, btnFilterToday, btnFilterWeek, btnFilterMonth;
    private TextView btnFilterCompleted, btnFilterProcessing;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private RecyclerView recyclerOrders;
    
    // Data
    private OrderHistoryAdapter adapter;
    private ApiService apiService;
    private AuthViewModel authViewModel;
    
    // Current filter
    private String currentFilter = "ALL";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        
        initViews();
        initServices();
        setupRecyclerView();
        setupFilterButtons();
        
        loadOrders(null); // Load all orders initially
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterToday = findViewById(R.id.btnFilterToday);
        btnFilterWeek = findViewById(R.id.btnFilterWeek);
        btnFilterMonth = findViewById(R.id.btnFilterMonth);
        btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        btnFilterProcessing = findViewById(R.id.btnFilterProcessing);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void initServices() {
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        authViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AuthViewModel(getApplication());
            }
        }).get(AuthViewModel.class);
    }
    
    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(this);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);
    }
    
    private void setupFilterButtons() {
        // Time filters
        btnFilterAll.setOnClickListener(v -> {
            selectFilter(btnFilterAll, "ALL");
            loadOrders(null);
        });
        
        btnFilterToday.setOnClickListener(v -> {
            selectFilter(btnFilterToday, "TODAY");
            loadOrdersFilteredByTime("today", null);
        });
        
        btnFilterWeek.setOnClickListener(v -> {
            selectFilter(btnFilterWeek, "WEEK");
            loadOrdersFilteredByTime("week", null);
        });
        
        btnFilterMonth.setOnClickListener(v -> {
            selectFilter(btnFilterMonth, "MONTH");
            loadOrdersFilteredByTime("month", null);
        });
        
        // Status filters
        btnFilterCompleted.setOnClickListener(v -> {
            selectFilter(btnFilterCompleted, "COMPLETED");
            loadOrders("COMPLETED");
        });
        
        btnFilterProcessing.setOnClickListener(v -> {
            selectFilter(btnFilterProcessing, "PROCESSING");
            loadOrders("PROCESSING");
        });
    }
    
    private void selectFilter(TextView selectedButton, String filter) {
        currentFilter = filter;
        
        // Reset all filters
        resetFilterStyle(btnFilterAll);
        resetFilterStyle(btnFilterToday);
        resetFilterStyle(btnFilterWeek);
        resetFilterStyle(btnFilterMonth);
        resetFilterStyle(btnFilterCompleted);
        resetFilterStyle(btnFilterProcessing);
        
        // Set selected style
        setSelectedFilterStyle(selectedButton);
    }
    
    private void resetFilterStyle(TextView button) {
        button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        button.setBackgroundResource(R.drawable.bg_filter_unselected);
    }
    
    private void setSelectedFilterStyle(TextView button) {
        button.setTextColor(ContextCompat.getColor(this, R.color.text_white));
        button.setBackgroundResource(R.drawable.bg_filter_selected);
    }
    
    private void loadOrders(String statuses) {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String bearerToken = "Bearer " + accessToken;
        
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        
        Log.d(TAG, "Loading orders with statuses: " + statuses);
        
        Call<ApiResponse<List<OrderResponse>>> call = apiService.getOrders(
            bearerToken,
            null,        // search
            "orderDate", // sortBy
            "desc",      // sortOrder (newest first)
            statuses     // statuses filter
        );
        
        call.enqueue(new Callback<ApiResponse<List<OrderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrderResponse>>> call, 
                                 Response<ApiResponse<List<OrderResponse>>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<OrderResponse>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<OrderResponse> orders = apiResponse.getData();
                        Log.d(TAG, "✓ Loaded " + orders.size() + " orders");
                        
                        if (orders.isEmpty()) {
                            showEmptyState();
                        } else {
                            adapter.setOrders(orders);
                            recyclerOrders.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "API response not successful: " + apiResponse.getMessage());
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code());
                    showError("Không thể tải lịch sử đơn hàng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<OrderResponse>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call error: " + t.getMessage());
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    private void showEmptyState() {
        recyclerOrders.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        showEmptyState();
    }
    
    private void loadOrdersFilteredByTime(String timeFilter, String statuses) {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String bearerToken = "Bearer " + accessToken;
        
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        
        Log.d(TAG, "Loading orders filtered by time: " + timeFilter);
        
        Call<ApiResponse<List<OrderResponse>>> call = apiService.getOrders(
            bearerToken,
            null,        // search
            "orderDate", // sortBy
            "desc",      // sortOrder (newest first)
            statuses     // statuses filter
        );
        
        call.enqueue(new Callback<ApiResponse<List<OrderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrderResponse>>> call, 
                                 Response<ApiResponse<List<OrderResponse>>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<OrderResponse>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<OrderResponse> orders = apiResponse.getData();
                        
                        // Filter by time period
                        List<OrderResponse> filteredOrders = filterOrdersByTime(orders, timeFilter);
                        
                        Log.d(TAG, "✓ Loaded " + filteredOrders.size() + " orders for " + timeFilter);
                        
                        if (filteredOrders.isEmpty()) {
                            showEmptyState();
                        } else {
                            adapter.setOrders(filteredOrders);
                            recyclerOrders.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "API response not successful: " + apiResponse.getMessage());
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code());
                    showError("Không thể tải lịch sử đơn hàng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<OrderResponse>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call error: " + t.getMessage());
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    private List<OrderResponse> filterOrdersByTime(List<OrderResponse> orders, String timeFilter) {
        if (orders == null || orders.isEmpty()) return new java.util.ArrayList<>();
        
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar startOfPeriod = java.util.Calendar.getInstance();
        
        // Set start of period based on filter
        switch (timeFilter.toLowerCase()) {
            case "today":
                startOfPeriod.set(java.util.Calendar.HOUR_OF_DAY, 0);
                startOfPeriod.set(java.util.Calendar.MINUTE, 0);
                startOfPeriod.set(java.util.Calendar.SECOND, 0);
                break;
            case "week":
                startOfPeriod.set(java.util.Calendar.DAY_OF_WEEK, startOfPeriod.getFirstDayOfWeek());
                startOfPeriod.set(java.util.Calendar.HOUR_OF_DAY, 0);
                startOfPeriod.set(java.util.Calendar.MINUTE, 0);
                startOfPeriod.set(java.util.Calendar.SECOND, 0);
                break;
            case "month":
                startOfPeriod.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startOfPeriod.set(java.util.Calendar.HOUR_OF_DAY, 0);
                startOfPeriod.set(java.util.Calendar.MINUTE, 0);
                startOfPeriod.set(java.util.Calendar.SECOND, 0);
                break;
        }
        
        java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        List<OrderResponse> filtered = new java.util.ArrayList<>();
        
        for (OrderResponse order : orders) {
            try {
                String dateStr = order.getOrderDate();
                if (dateStr != null) {
                    java.util.Date orderDate = isoFormat.parse(dateStr);
                    if (orderDate != null && orderDate.after(startOfPeriod.getTime())) {
                        filtered.add(order);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
            }
        }
        
        return filtered;
    }
}

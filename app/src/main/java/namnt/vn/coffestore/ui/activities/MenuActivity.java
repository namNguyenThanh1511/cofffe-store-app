package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.CoffeeItem;
import namnt.vn.coffestore.data.model.Product;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.MenuAdapter;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";

    private DrawerLayout drawerLayout;
    private ImageView ivMenu, ivAvatar, ivCart;
    private TextView tvGreeting, tvUserName;
    private EditText etSearch;
    private TextView btnBrewedCoffee, btnColdBrew, btnBeverages;
    private TextView btnPriceAll, btnPriceLow, btnPriceMedium, btnPriceHigh;
    private RecyclerView rvCoffeeList;
    private com.google.android.material.navigation.NavigationView navigationView;
    
    private MenuAdapter menuAdapter;
    private List<CoffeeItem> allCoffeeItems;
    private List<Product> allProducts; // Lưu products gốc để truyền variants
    private String currentCategory = "Brewed Coffee";
    private String currentSearchQuery = "";
    private String currentPriceRange = "ALL"; // ALL, LOW (<30k), MEDIUM (30-50k), HIGH (>50k)
    private ApiService apiService;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initAuthViewModel();
        if (authViewModel.getAccessToken().isEmpty()) {
            redirectToLogin();
            return;
        }

        initViews();
        setupGreeting();
        setupCategoryButtons();
        setupRecyclerView();
        setupClickListeners();
        setupNavigationDrawer();
        observeLogout();
        
        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        
        // Load products from API
        loadProductsFromApi();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        ivMenu = findViewById(R.id.ivMenu);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivCart = findViewById(R.id.ivCart);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        etSearch = findViewById(R.id.etSearch);
        btnBrewedCoffee = findViewById(R.id.btnBrewedCoffee);
        btnColdBrew = findViewById(R.id.btnColdBrew);
        btnBeverages = findViewById(R.id.btnBeverages);
        btnPriceAll = findViewById(R.id.btnPriceAll);
        btnPriceLow = findViewById(R.id.btnPriceLow);
        btnPriceMedium = findViewById(R.id.btnPriceMedium);
        btnPriceHigh = findViewById(R.id.btnPriceHigh);
        rvCoffeeList = findViewById(R.id.rvCoffeeList);
        navigationView = findViewById(R.id.navigationView);
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (hour >= 0 && hour < 12) {
            greeting = getString(R.string.greeting_morning);
        } else {
            greeting = getString(R.string.greeting_evening);
        }
        
        tvGreeting.setText("Hi, " + greeting);
        // You can set user name from SharedPreferences or Firebase Auth later
        tvUserName.setText(getString(R.string.user_name));
    }

    private void setupCategoryButtons() {
        btnBrewedCoffee.setSelected(true);
        btnPriceAll.setSelected(true);
        
        btnBrewedCoffee.setOnClickListener(v -> selectCategory("Brewed Coffee", btnBrewedCoffee));
        btnColdBrew.setOnClickListener(v -> selectCategory("Cold Brew", btnColdBrew));
        btnBeverages.setOnClickListener(v -> selectCategory("Beverages", btnBeverages));
        
        // Price filter buttons
        btnPriceAll.setOnClickListener(v -> selectPriceRange("ALL", btnPriceAll));
        btnPriceLow.setOnClickListener(v -> selectPriceRange("LOW", btnPriceLow));
        btnPriceMedium.setOnClickListener(v -> selectPriceRange("MEDIUM", btnPriceMedium));
        btnPriceHigh.setOnClickListener(v -> selectPriceRange("HIGH", btnPriceHigh));
    }

    private void selectCategory(String category, TextView selectedButton) {
        currentCategory = category;
        
        // Reset all buttons
        btnBrewedCoffee.setSelected(false);
        btnColdBrew.setSelected(false);
        btnBeverages.setSelected(false);
        
        // Select clicked button
        selectedButton.setSelected(true);
        
        // Filter coffee list by category
        filterCoffeeByCategory(category);
    }

    private void setupRecyclerView() {
        menuAdapter = new MenuAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvCoffeeList.setLayoutManager(gridLayoutManager);
        rvCoffeeList.setAdapter(menuAdapter);
        
        menuAdapter.setOnItemClickListener(item -> {
            // Tìm product gốc để lấy variants
            Product selectedProduct = findProductById(item.getId());
            
            // Navigate to ProductDetailActivity
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", item.getId());
            intent.putExtra("PRODUCT_NAME", item.getName());
            intent.putExtra("PRODUCT_DESCRIPTION", item.getDescription());
            intent.putExtra("PRODUCT_PRICE", item.getPrice());
            if (item.getOldPrice() != null) {
                intent.putExtra("PRODUCT_OLD_PRICE", item.getOldPrice());
            }
            intent.putExtra("PRODUCT_IMAGE", item.getImageUrl());
            intent.putExtra("PRODUCT_CATEGORY", item.getCategory());
            
            // Truyền variants dưới dạng JSON string
            if (selectedProduct != null && selectedProduct.getVariants() != null) {
                Gson gson = new Gson();
                String variantsJson = gson.toJson(selectedProduct.getVariants());
                intent.putExtra("PRODUCT_VARIANTS", variantsJson);
            }
            
            startActivity(intent);
        });
    }

    private void loadProductsFromApi() {
        allCoffeeItems = new ArrayList<>();
        allProducts = new ArrayList<>();
        
        Log.d(TAG, "Bắt đầu gọi API /api/products");
        Call<ApiResponse<List<Product>>> call = apiService.getProducts();
        call.enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                Log.d(TAG, "Nhận response từ API: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Product>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Product> products = apiResponse.getData();
                        allProducts = products; // Lưu lại products gốc
                        Log.d(TAG, "Số lượng products: " + products.size());
                        
                        // Convert Product to CoffeeItem và phân loại theo alias
                        for (Product product : products) {
                            String category = mapAliasToCategory(product.getAlias());
                            CoffeeItem coffeeItem = new CoffeeItem(
                                product.getId(),
                                product.getName(),
                                product.getDescription(),
                                product.getMinPrice(), // Lấy giá thấp nhất từ variants
                                null, // oldPrice
                                product.getImageUrl(),
                                category
                            );
                            allCoffeeItems.add(coffeeItem);
                        }
                        
                        // Show initial category
                        filterCoffeeByCategory(currentCategory);
                        Log.d(TAG, "Đã load products thành công từ API");
                    } else {
                        Log.e(TAG, "API response không thành công hoặc data null");
                        Toast.makeText(MenuActivity.this, "Không thể tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                        loadSampleData(); // Fallback to sample data
                    }
                } else {
                    Log.e(TAG, "Response không successful: " + response.code());
                    Toast.makeText(MenuActivity.this, "Lỗi kết nối API: " + response.code(), Toast.LENGTH_SHORT).show();
                    loadSampleData(); // Fallback to sample data
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(MenuActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadSampleData(); // Fallback to sample data
            }
        });
    }
    
    private Product findProductById(String productId) {
        if (allProducts == null || productId == null) return null;
        
        for (Product product : allProducts) {
            if (productId.equals(product.getId())) {
                return product;
            }
        }
        return null;
    }
    
    private String mapAliasToCategory(String alias) {
        if (alias == null) return "Brewed Coffee";
        
        switch (alias.toLowerCase()) {
            case "espresso":
            case "cappuccino":
            case "latte":
            case "americano":
                return "Brewed Coffee";
            case "iced":
            case "cold":
            case "frappe":
                return "Cold Brew";
            case "tea":
            case "chocolate":
            case "juice":
                return "Beverages";
            default:
                return "Brewed Coffee";
        }
    }
    
    private void loadSampleData() {
        allCoffeeItems = new ArrayList<>();
        
        // Sample data as fallback
        allCoffeeItems.add(new CoffeeItem("1", "Creamy Coffee", 4.00, null, 
            "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400", "Brewed Coffee"));
        allCoffeeItems.add(new CoffeeItem("2", "Hot Creamy", 5.80, 6.50, 
            "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400", "Brewed Coffee"));
        allCoffeeItems.add(new CoffeeItem("3", "Mocha Coffee", 5.50, null, 
            "https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=400", "Brewed Coffee"));
        allCoffeeItems.add(new CoffeeItem("4", "Cappuccino", 4.30, null, 
            "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400", "Brewed Coffee"));
        
        allCoffeeItems.add(new CoffeeItem("5", "Iced Latte", 4.50, null, 
            "https://images.unsplash.com/photo-1517487881594-2787fef5ebf7?w=400", "Cold Brew"));
        allCoffeeItems.add(new CoffeeItem("6", "Cold Brew Classic", 5.00, null, 
            "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400", "Cold Brew"));
        
        allCoffeeItems.add(new CoffeeItem("7", "Hot Chocolate", 3.80, null, 
            "https://images.unsplash.com/photo-1542990253-0d0f5be5f0ed?w=400", "Beverages"));
        allCoffeeItems.add(new CoffeeItem("8", "Green Tea", 3.50, null, 
            "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=400", "Beverages"));
        
        // Show initial category
        filterCoffeeByCategory(currentCategory);
    }

    private void filterCoffeeByCategory(String category) {
        currentCategory = category;
        filterCoffee();
    }

    private void selectPriceRange(String priceRange, TextView button) {
        currentPriceRange = priceRange;
        
        // Reset all price buttons
        btnPriceAll.setSelected(false);
        btnPriceLow.setSelected(false);
        btnPriceMedium.setSelected(false);
        btnPriceHigh.setSelected(false);
        
        // Select clicked button
        button.setSelected(true);
        
        // Apply filter
        filterCoffee();
    }
    
    private void filterCoffee() {
        List<CoffeeItem> filteredList = new ArrayList<>();
        
        for (CoffeeItem item : allCoffeeItems) {
            // Filter by category
            boolean matchCategory = item.getCategory().equals(currentCategory);
            
            // Filter by search query
            boolean matchSearch = currentSearchQuery.isEmpty() || 
                item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            // Filter by price range
            boolean matchPrice = matchPriceRange(item.getPrice());
            
            if (matchCategory && matchSearch && matchPrice) {
                filteredList.add(item);
            }
        }
        
        menuAdapter.setCoffeeList(filteredList);
    }
    
    private boolean matchPriceRange(double price) {
        switch (currentPriceRange) {
            case "LOW":
                return price < 30000;
            case "MEDIUM":
                return price >= 30000 && price <= 50000;
            case "HIGH":
                return price > 50000;
            case "ALL":
            default:
                return true;
        }
    }

    private void setupClickListeners() {
        ivMenu.setOnClickListener(v -> {
            // Open drawer from right side
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });
        
        // Cart button - Navigate to CartActivity
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
        
        // Live search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterCoffee();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
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

    private void setupNavigationDrawer() {
        if (navigationView == null) {
            return;
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                drawerLayout.closeDrawers();
                authViewModel.logout();
                return true;
            }
            if (id == R.id.nav_home) {
                drawerLayout.closeDrawers();
                return true;
            }
            return false;
        });
    }

    private void observeLogout() {
        authViewModel.getLogoutResult().observe(this, new Observer<AuthViewModel.AuthResult>() {
            @Override
            public void onChanged(AuthViewModel.AuthResult result) {
                if (result == null) {
                    return;
                }
                Toast.makeText(MenuActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                if (result.isSuccess()) {
                    redirectToLogin();
                }
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

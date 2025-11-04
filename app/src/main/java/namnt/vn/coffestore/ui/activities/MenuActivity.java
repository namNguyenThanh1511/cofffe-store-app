package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import namnt.vn.coffestore.data.model.BrewMethod;
import namnt.vn.coffestore.data.model.CoffeeItem;
import namnt.vn.coffestore.data.model.Product;
import namnt.vn.coffestore.data.model.RoastLevel;
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
    private Spinner spinnerOrigin, spinnerRoastLevel, spinnerBrewMethod;
    private RecyclerView rvCoffeeList;
    private com.google.android.material.navigation.NavigationView navigationView;
    
    private MenuAdapter menuAdapter;
    private List<CoffeeItem> allCoffeeItems;
    private List<Product> allProducts; // Lưu products gốc để truyền variants
    private String currentSearchQuery = "";
    private String currentOriginFilter = null; // null = all origins
    private String currentRoastLevelFilter = null; // null = all roast levels
    private String currentBrewMethodFilter = null; // null = all brew methods
    private List<String> availableOrigins = new ArrayList<>();
    private List<String> availableRoastLevels = new ArrayList<>();
    private List<String> availableBrewMethods = new ArrayList<>();
    private int loadedProductDetailsCount = 0;
    private int totalProductsToLoad = 0;
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
        spinnerOrigin = findViewById(R.id.spinnerOrigin);
        spinnerRoastLevel = findViewById(R.id.spinnerRoastLevel);
        spinnerBrewMethod = findViewById(R.id.spinnerBrewMethod);
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
                        Log.d(TAG, "Số lượng products từ /api/products: " + products.size());
                        
                        // Khởi tạo danh sách rỗng
                        allProducts = new ArrayList<>();
                        allCoffeeItems = new ArrayList<>();
                        totalProductsToLoad = products.size();
                        loadedProductDetailsCount = 0;
                        
                        // Gọi API chi tiết cho từng product
                        for (Product product : products) {
                            loadProductDetails(product.getId());
                        }
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
        filterCoffee();
    }

    private void filterCoffee() {
        List<CoffeeItem> filteredList = new ArrayList<>();
        
        for (CoffeeItem item : allCoffeeItems) {
            boolean matchSearch = item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            boolean matchOrigin = matchOrigin(item.getId());
            boolean matchRoast = matchRoastLevel(item.getId());
            boolean matchBrew = matchBrewMethod(item.getId());
            
            if (matchSearch && matchOrigin && matchRoast && matchBrew) {
                filteredList.add(item);
            }
        }
        
        menuAdapter.setCoffeeList(filteredList);
    }
    
    
    private boolean matchOrigin(String productId) {
        if (currentOriginFilter == null) return true; // Show all if no filter
        
        Product product = findProductById(productId);
        if (product == null || product.getOrigin() == null) return false;
        
        return product.getOrigin().equals(currentOriginFilter);
    }
    
    private boolean matchRoastLevel(String productId) {
        if (currentRoastLevelFilter == null) return true; // Show all if no filter
        
        Product product = findProductById(productId);
        if (product == null || product.getRoastLevel() == null) return false;
        
        return product.getRoastLevel().equals(currentRoastLevelFilter);
    }
    
    private boolean matchBrewMethod(String productId) {
        if (currentBrewMethodFilter == null) return true; // Show all if no filter
        
        Product product = findProductById(productId);
        if (product == null || product.getBrewMethod() == null) return false;
        
        return product.getBrewMethod().equals(currentBrewMethodFilter);
    }

    private void loadProductDetails(String productId) {
        Log.d(TAG, "Đang gọi API /api/products/" + productId);
        Call<ApiResponse<Product>> call = apiService.getProductById(productId);
        call.enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Product> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Product product = apiResponse.getData();
                        Log.d(TAG, "Nhận chi tiết product: " + product.getName() + 
                            ", origin: " + product.getOrigin() + 
                            ", roastLevel: " + product.getRoastLevel() + 
                            ", brewMethod: " + product.getBrewMethod());
                        
                        // Lưu product với đầy đủ thông tin
                        allProducts.add(product);
                        
                        // Convert to CoffeeItem
                        String category = mapAliasToCategory(product.getAlias());
                        CoffeeItem coffeeItem = new CoffeeItem(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getMinPrice(),
                            null,
                            product.getImageUrl(),
                            category
                        );
                        allCoffeeItems.add(coffeeItem);
                        
                        // Kiểm tra đã load xong tất cả chưa
                        loadedProductDetailsCount++;
                        if (loadedProductDetailsCount == totalProductsToLoad) {
                            onAllProductsLoaded();
                        }
                    }
                } else {
                    Log.e(TAG, "Lỗi load chi tiết product " + productId + ": " + response.code());
                    loadedProductDetailsCount++;
                    if (loadedProductDetailsCount == totalProductsToLoad) {
                        onAllProductsLoaded();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                Log.e(TAG, "API call failed cho product " + productId + ": " + t.getMessage());
                loadedProductDetailsCount++;
                if (loadedProductDetailsCount == totalProductsToLoad) {
                    onAllProductsLoaded();
                }
            }
        });
    }
    
    private String mapAliasToCategory(String alias) {
        if (alias == null) return "Brewed Coffee";
        
        switch (alias.toLowerCase()) {
            case "brewed-coffee":
            case "brewed_coffee":
                return "Brewed Coffee";
            case "cold-brew":
            case "cold_brew":
                return "Cold Brew";
            case "beverages":
                return "Beverages";
            default:
                return "Brewed Coffee";
        }
    }
    
    private void onAllProductsLoaded() {
        Log.d(TAG, "Đã load xong " + allProducts.size() + " products với chi tiết đầy đủ");
        
        // Extract unique values từ products đã có đầy đủ thông tin
        extractUniqueOrigins(allProducts);
        extractUniqueRoastLevels(allProducts);
        extractUniqueBrewMethods(allProducts);
        
        // Show all products
        filterCoffee();
    }
    
    private void extractUniqueOrigins(List<Product> products) {
        availableOrigins.clear();
        for (Product product : products) {
            String origin = product.getOrigin();
            if (origin != null && !origin.trim().isEmpty() && !availableOrigins.contains(origin)) {
                availableOrigins.add(origin);
            }
        }
        // Sort alphabetically
        java.util.Collections.sort(availableOrigins);
        Log.d(TAG, "Found " + availableOrigins.size() + " unique origins: " + availableOrigins);
        
        // Setup origin spinner after extracting
        setupOriginSpinner();
    }
    
    private void extractUniqueRoastLevels(List<Product> products) {
        availableRoastLevels.clear();
        for (Product product : products) {
            String roastLevel = product.getRoastLevel();
            if (roastLevel != null && !roastLevel.trim().isEmpty() && !availableRoastLevels.contains(roastLevel)) {
                availableRoastLevels.add(roastLevel);
            }
        }
        // Sort alphabetically
        java.util.Collections.sort(availableRoastLevels);
        Log.d(TAG, "Found " + availableRoastLevels.size() + " unique roast levels: " + availableRoastLevels);
        
        // Setup roast level spinner after extracting
        setupRoastLevelSpinner();
    }
    
    private void extractUniqueBrewMethods(List<Product> products) {
        availableBrewMethods.clear();
        for (Product product : products) {
            String brewMethod = product.getBrewMethod();
            if (brewMethod != null && !brewMethod.trim().isEmpty() && !availableBrewMethods.contains(brewMethod)) {
                availableBrewMethods.add(brewMethod);
            }
        }
        // Sort alphabetically
        java.util.Collections.sort(availableBrewMethods);
        Log.d(TAG, "Found " + availableBrewMethods.size() + " unique brew methods: " + availableBrewMethods);
        
        // Setup brew method spinner after extracting
        setupBrewMethodSpinner();
    }
    
    private void setupOriginSpinner() {
        List<String> originOptions = new ArrayList<>();
        originOptions.add("Tất cả xuất xứ");
        originOptions.addAll(availableOrigins);
        
        ArrayAdapter<String> originAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, originOptions);
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(originAdapter);
        
        spinnerOrigin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentOriginFilter = null; // All origins
                } else {
                    currentOriginFilter = availableOrigins.get(position - 1);
                }
                Log.d(TAG, "Origin filter changed to: " + currentOriginFilter);
                filterCoffee();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentOriginFilter = null;
            }
        });
    }
    
    private void setupRoastLevelSpinner() {
        List<String> roastLevelOptions = new ArrayList<>();
        roastLevelOptions.add("Tất cả độ rang");
        roastLevelOptions.addAll(availableRoastLevels);
        
        ArrayAdapter<String> roastAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, roastLevelOptions);
        roastAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoastLevel.setAdapter(roastAdapter);
        
        spinnerRoastLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentRoastLevelFilter = null; // All roast levels
                } else {
                    currentRoastLevelFilter = availableRoastLevels.get(position - 1);
                }
                Log.d(TAG, "Roast level filter changed to: " + currentRoastLevelFilter);
                filterCoffee();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentRoastLevelFilter = null;
            }
        });
    }
    
    private void setupBrewMethodSpinner() {
        List<String> brewMethodOptions = new ArrayList<>();
        brewMethodOptions.add("Tất cả phương pháp");
        brewMethodOptions.addAll(availableBrewMethods);
        
        ArrayAdapter<String> brewAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, brewMethodOptions);
        brewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrewMethod.setAdapter(brewAdapter);
        
        spinnerBrewMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentBrewMethodFilter = null; // All brew methods
                } else {
                    currentBrewMethodFilter = availableBrewMethods.get(position - 1);
                }
                Log.d(TAG, "Brew method filter changed to: " + currentBrewMethodFilter);
                filterCoffee();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentBrewMethodFilter = null;
            }
        });
    }
    
    private void setupSpinners() {
        // Spinners sẽ được setup sau khi load xong products từ API
        // Xem extractUniqueRoastLevels() và extractUniqueBrewMethods()
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

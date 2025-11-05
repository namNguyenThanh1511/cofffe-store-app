package namnt.vn.coffestore.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.BrewMethod;
import namnt.vn.coffestore.data.model.CoffeeItem;
import namnt.vn.coffestore.data.model.Product;
import namnt.vn.coffestore.data.model.RoastLevel;
import namnt.vn.coffestore.data.model.UserProfile;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.OrderItemDetail;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.ui.adapters.MenuAdapter;
import namnt.vn.coffestore.utils.NotificationHelper;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    // Drawer & header
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView ivMenu, ivAvatar, ivCart;
    private TextView tvGreeting, tvUserName, tvCartBadge;

    // Search & filters
    private EditText etSearch;
    private Spinner spinnerOrigin, spinnerRoastLevel, spinnerBrewMethod;
    private LinearLayout emptyStateLayout;

    // List
    private RecyclerView rvCoffeeList;
    private MenuAdapter menuAdapter;

    // Data
    private List<CoffeeItem> allCoffeeItems;
    private List<Product> allProducts;
    private String currentSearchQuery = "";
    private String currentOriginFilter = null;
    private String currentRoastLevelFilter = null;
    private String currentBrewMethodFilter = null;
    private final List<String> availableOrigins = new ArrayList<>();
    private final List<String> availableRoastLevels = new ArrayList<>();
    private final List<String> availableBrewMethods = new ArrayList<>();
    private int loadedProductDetailsCount = 0;
    private int totalProductsToLoad = 0;

    // API & auth
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

        // Notification channel & permission
        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        initViews();
        setupGreeting();
        setupRecyclerView();
        setupClickListeners();
        setupNavigationDrawer();
        observeLogout();

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        loadProductsFromApi();
        loadUserProfile();
        loadCartCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartCount();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ivMenu = findViewById(R.id.ivMenu);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivCart = findViewById(R.id.ivCart);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        etSearch = findViewById(R.id.etSearch);

        spinnerOrigin = findViewById(R.id.spinnerOrigin);
        spinnerRoastLevel = findViewById(R.id.spinnerRoastLevel);
        spinnerBrewMethod = findViewById(R.id.spinnerBrewMethod);

        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        rvCoffeeList = findViewById(R.id.rvCoffeeList);
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting = (hour >= 0 && hour < 12)
                ? getString(R.string.greeting_morning)
                : getString(R.string.greeting_evening);
        tvGreeting.setText("Hi, " + greeting);
        tvUserName.setText(getString(R.string.user_name)); // sẽ cập nhật lại bằng API
    }

    private void setupRecyclerView() {
        menuAdapter = new MenuAdapter();
        rvCoffeeList.setLayoutManager(new GridLayoutManager(this, 2));
        rvCoffeeList.setAdapter(menuAdapter);

        menuAdapter.setOnItemClickListener(item -> {
            Product selectedProduct = findProductById(item.getId());

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

            if (selectedProduct != null && selectedProduct.getVariants() != null) {
                Gson gson = new Gson();
                String variantsJson = gson.toJson(selectedProduct.getVariants());
                intent.putExtra("PRODUCT_VARIANTS", variantsJson);
            }
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        ivMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        ivCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterCoffee();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupNavigationDrawer() {
        if (navigationView == null) return;

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            }

            if (id == R.id.nav_profile) {
                drawerLayout.closeDrawer(GravityCompat.END);
                // Giữ màn UserProfileActivity của bạn
                startActivity(new Intent(MenuActivity.this, UserProfileActivity.class));
                return true;
            }

            if (id == R.id.nav_map) {
                drawerLayout.closeDrawer(GravityCompat.END);
                startActivity(new Intent(MenuActivity.this, OsmMapActivity.class));
                return true;
            }

            if (id == R.id.nav_logout) {
                drawerLayout.closeDrawer(GravityCompat.END);
                authViewModel.logout();
                return true;
            }

            return false;
        });
    }

    private void observeLogout() {
        authViewModel.getLogoutResult().observe(this, new Observer<AuthViewModel.AuthResult>() {
            @Override
            public void onChanged(AuthViewModel.AuthResult result) {
                if (result == null) return;
                Toast.makeText(MenuActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                if (result.isSuccess()) redirectToLogin();
            }
        });
    }

    // ==================== API PRODUCTS ====================

    private void loadProductsFromApi() {
        allCoffeeItems = new ArrayList<>();
        allProducts = new ArrayList<>();

        Call<ApiResponse<List<Product>>> call = apiService.getProducts();
        call.enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {

                    List<Product> products = response.body().getData();
                    totalProductsToLoad = products.size();
                    loadedProductDetailsCount = 0;

                    for (Product product : products) {
                        loadProductDetails(product.getId());
                    }
                } else {
                    Toast.makeText(MenuActivity.this, "Không thể tải sản phẩm, dùng dữ liệu mẫu", Toast.LENGTH_SHORT).show();
                    loadSampleData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                Toast.makeText(MenuActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadSampleData();
            }
        });
    }

    private void loadProductDetails(String productId) {
        Call<ApiResponse<Product>> call = apiService.getProductById(productId);
        call.enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {

                    Product product = response.body().getData();
                    allProducts.add(product);

                    String category = mapAliasToCategory(product.getAlias());

                    // LƯU Ý: CoffeeItem ở code mới có trường description trong constructor.
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
                }
                loadedProductDetailsCount++;
                if (loadedProductDetailsCount == totalProductsToLoad) {
                    onAllProductsLoaded();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                loadedProductDetailsCount++;
                if (loadedProductDetailsCount == totalProductsToLoad) {
                    onAllProductsLoaded();
                }
            }
        });
    }

    private void onAllProductsLoaded() {
        extractUniqueOrigins(allProducts);
        extractUniqueRoastLevels(allProducts);
        extractUniqueBrewMethods(allProducts);
        filterCoffee();
    }

    private Product findProductById(String productId) {
        if (allProducts == null || productId == null) return null;
        for (Product p : allProducts) if (productId.equals(p.getId())) return p;
        return null;
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

    // ==================== FILTER ====================

    private void filterCoffee() {
        List<CoffeeItem> filtered = new ArrayList<>();
        if (allCoffeeItems == null) allCoffeeItems = new ArrayList<>();

        for (CoffeeItem item : allCoffeeItems) {
            boolean matchSearch = currentSearchQuery.isEmpty()
                    || item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());

            boolean matchOrigin = matchOrigin(item.getId());
            boolean matchRoast = matchRoastLevel(item.getId());
            boolean matchBrew = matchBrewMethod(item.getId());

            if (matchSearch && matchOrigin && matchRoast && matchBrew) {
                filtered.add(item);
            }
        }

        if (emptyStateLayout != null) {
            if (filtered.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                rvCoffeeList.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                rvCoffeeList.setVisibility(View.VISIBLE);
            }
        }

        menuAdapter.setCoffeeList(filtered);
    }

    private boolean matchOrigin(String productId) {
        if (currentOriginFilter == null) return true;
        Product p = findProductById(productId);
        return p != null && currentOriginFilter.equals(p.getOrigin());
    }

    private boolean matchRoastLevel(String productId) {
        if (currentRoastLevelFilter == null) return true;
        Product p = findProductById(productId);
        return p != null && currentRoastLevelFilter.equals(p.getRoastLevel());
    }

    private boolean matchBrewMethod(String productId) {
        if (currentBrewMethodFilter == null) return true;
        Product p = findProductById(productId);
        return p != null && currentBrewMethodFilter.equals(p.getBrewMethod());
    }

    // ==================== SAMPLE FALLBACK ====================

    private void loadSampleData() {
        allCoffeeItems = new ArrayList<>();

        allCoffeeItems.add(new CoffeeItem("1", "Creamy Coffee", "Sample Brewed", 4.00, null,
                "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400", "Brewed Coffee"));
        allCoffeeItems.add(new CoffeeItem("2", "Hot Creamy", "Sample Brewed", 5.80, 6.50,
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400", "Brewed Coffee"));
        allCoffeeItems.add(new CoffeeItem("3", "Mocha Coffee", "Sample Brewed", 5.50, null,
                "https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=400", "Brewed Coffee"));
        allCoffeeItems.add(new CoffeeItem("4", "Cappuccino", "Sample Brewed", 4.30, null,
                "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400", "Brewed Coffee"));

        allCoffeeItems.add(new CoffeeItem("5", "Iced Latte", "Sample Cold Brew", 4.50, null,
                "https://images.unsplash.com/photo-1517487881594-2787fef5ebf7?w=400", "Cold Brew"));
        allCoffeeItems.add(new CoffeeItem("6", "Cold Brew Classic", "Sample Cold Brew", 5.00, null,
                "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400", "Cold Brew"));

        allCoffeeItems.add(new CoffeeItem("7", "Hot Chocolate", "Sample Beverage", 3.80, null,
                "https://images.unsplash.com/photo-1542990253-0d0f5be5f0ed?w=400", "Beverages"));
        allCoffeeItems.add(new CoffeeItem("8", "Green Tea", "Sample Beverage", 3.50, null,
                "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=400", "Beverages"));

        filterCoffee();
    }

    // ==================== EXTRACT & SPINNERS ====================

    private void extractUniqueOrigins(List<Product> products) {
        availableOrigins.clear();
        for (Product p : products) {
            String v = p.getOrigin();
            if (v != null && !v.trim().isEmpty() && !availableOrigins.contains(v)) availableOrigins.add(v);
        }
        java.util.Collections.sort(availableOrigins);
        setupOriginSpinner();
    }

    private void extractUniqueRoastLevels(List<Product> products) {
        availableRoastLevels.clear();
        for (Product p : products) {
            String v = p.getRoastLevel();
            if (v != null && !v.trim().isEmpty() && !availableRoastLevels.contains(v)) availableRoastLevels.add(v);
        }
        java.util.Collections.sort(availableRoastLevels);
        setupRoastLevelSpinner();
    }

    private void extractUniqueBrewMethods(List<Product> products) {
        availableBrewMethods.clear();
        for (Product p : products) {
            String v = p.getBrewMethod();
            if (v != null && !v.trim().isEmpty() && !availableBrewMethods.contains(v)) availableBrewMethods.add(v);
        }
        java.util.Collections.sort(availableBrewMethods);
        setupBrewMethodSpinner();
    }

    private void setupOriginSpinner() {
        if (spinnerOrigin == null) return;
        List<String> options = new ArrayList<>();
        options.add("Tất cả xuất xứ");
        options.addAll(availableOrigins);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(adapter);

        spinnerOrigin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentOriginFilter = (position == 0) ? null : availableOrigins.get(position - 1);
                filterCoffee();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { currentOriginFilter = null; }
        });
    }

    private void setupRoastLevelSpinner() {
        if (spinnerRoastLevel == null) return;

        List<String> enumNames = getRoastLevelEnumValues();
        List<String> viNames = new ArrayList<>();
        for (String e : enumNames) viNames.add(roastLevelToVietnamese(e));

        List<String> options = new ArrayList<>();
        options.add("Tất cả độ rang");
        options.addAll(viNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoastLevel.setAdapter(adapter);

        spinnerRoastLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRoastLevelFilter = (position == 0) ? null : enumNames.get(position - 1);
                filterCoffee();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { currentRoastLevelFilter = null; }
        });
    }

    private void setupBrewMethodSpinner() {
        if (spinnerBrewMethod == null) return;

        List<String> enumNames = getBrewMethodEnumValues();
        List<String> viNames = new ArrayList<>();
        for (String e : enumNames) viNames.add(brewMethodToVietnamese(e));

        List<String> options = new ArrayList<>();
        options.add("Tất cả phương pháp");
        options.addAll(viNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrewMethod.setAdapter(adapter);

        spinnerBrewMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentBrewMethodFilter = (position == 0) ? null : enumNames.get(position - 1);
                filterCoffee();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { currentBrewMethodFilter = null; }
        });
    }

    // ==================== ENUM HELPERS ====================

    private List<String> getRoastLevelEnumValues() {
        List<String> values = new ArrayList<>();
        for (RoastLevel level : RoastLevel.values()) values.add(level.name());
        return values;
    }

    private List<String> getBrewMethodEnumValues() {
        List<String> values = new ArrayList<>();
        for (BrewMethod method : BrewMethod.values()) values.add(method.name());
        return values;
    }

    private String roastLevelToVietnamese(String enumName) {
        RoastLevel level = RoastLevel.fromString(enumName);
        return level != null ? level.getVietnameseName() : enumName;
    }

    private String brewMethodToVietnamese(String enumName) {
        BrewMethod method = BrewMethod.fromString(enumName);
        return method != null ? method.getVietnameseName() : enumName;
    }

    // ==================== USER PROFILE & CART ====================

    private void loadUserProfile() {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) return;

        String bearerToken = "Bearer " + accessToken;
        Call<ApiResponse<UserProfile>> call = apiService.getUserProfile(bearerToken);
        call.enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserProfile profile = response.body().getData();
                    if (profile != null && profile.getFullName() != null) {
                        tvUserName.setText(profile.getFullName());
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) { /* ignore */ }
        });
    }

    private void loadCartCount() {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) return;

        String bearerToken = "Bearer " + accessToken;
        Call<ApiResponse<List<OrderResponse>>> call = apiService.getOrders(bearerToken);
        call.enqueue(new Callback<ApiResponse<List<OrderResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrderResponse>>> call, Response<ApiResponse<List<OrderResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int totalQuantity = 0;
                    List<OrderResponse> orders = response.body().getData();
                    if (orders != null) {
                        for (OrderResponse order : orders) {
                            if (order.getOrderItems() != null) {
                                for (OrderItemDetail item : order.getOrderItems()) {
                                    totalQuantity += item.getQuantity();
                                }
                            }
                        }
                    }
                    final int finalTotal = totalQuantity;
                    runOnUiThread(() -> updateCartBadge(finalTotal));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<OrderResponse>>> call, Throwable t) { /* ignore */ }
        });
    }

    private void updateCartBadge(int count) {
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
                NotificationHelper.updateNotificationStage(this, 2, count);
            } else {
                tvCartBadge.setVisibility(View.GONE);
                NotificationHelper.updateNotificationStage(this, 1, 0);
            }
        }
    }

    // ==================== PERMISSION & AUTH ====================

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Vui lòng bật thông báo để nhận thông tin về đơn hàng", Toast.LENGTH_LONG).show();
            }
        }
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

package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.CoffeeItem;
import namnt.vn.coffestore.ui.adapters.MenuAdapter;

public class MenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView ivMenu, ivAvatar, ivCart;
    private TextView tvGreeting, tvUserName;
    private EditText etSearch;
    private TextView btnBrewedCoffee, btnColdBrew, btnBeverages;
    private RecyclerView rvCoffeeList;
    
    private MenuAdapter menuAdapter;
    private List<CoffeeItem> allCoffeeItems;
    private String currentCategory = "Brewed Coffee";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setupGreeting();
        setupCategoryButtons();
        setupRecyclerView();
        loadSampleData();
        setupClickListeners();
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
        rvCoffeeList = findViewById(R.id.rvCoffeeList);
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
        
        btnBrewedCoffee.setOnClickListener(v -> selectCategory("Brewed Coffee", btnBrewedCoffee));
        btnColdBrew.setOnClickListener(v -> selectCategory("Cold Brew", btnColdBrew));
        btnBeverages.setOnClickListener(v -> selectCategory("Beverages", btnBeverages));
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
            // Navigate to ProductDetailActivity
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", item.getId());
            intent.putExtra("PRODUCT_NAME", item.getName());
            intent.putExtra("PRODUCT_PRICE", item.getPrice());
            if (item.getOldPrice() != null) {
                intent.putExtra("PRODUCT_OLD_PRICE", item.getOldPrice());
            }
            intent.putExtra("PRODUCT_IMAGE", item.getImageUrl());
            intent.putExtra("PRODUCT_CATEGORY", item.getCategory());
            startActivity(intent);
        });
    }

    private void loadSampleData() {
        allCoffeeItems = new ArrayList<>();
        
        // Sample data with images from internet - Replace with API call later
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

    private void filterCoffee() {
        List<CoffeeItem> filteredList = new ArrayList<>();
        
        for (CoffeeItem item : allCoffeeItems) {
            // Filter by category
            boolean matchCategory = item.getCategory().equals(currentCategory);
            
            // Filter by search query
            boolean matchSearch = currentSearchQuery.isEmpty() || 
                item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            if (matchCategory && matchSearch) {
                filteredList.add(item);
            }
        }
        
        menuAdapter.setCoffeeList(filteredList);
    }

    private void setupClickListeners() {
        ivMenu.setOnClickListener(v -> {
            // Open drawer from right side
            if (drawerLayout.isDrawerOpen(findViewById(R.id.navigationView))) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(findViewById(R.id.navigationView));
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
}

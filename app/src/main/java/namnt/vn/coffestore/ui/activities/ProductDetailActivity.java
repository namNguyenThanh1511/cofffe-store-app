package namnt.vn.coffestore.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.Addon;
import namnt.vn.coffestore.data.model.CartItem;
import namnt.vn.coffestore.data.model.ProductVariant;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.OrderItem;
import namnt.vn.coffestore.data.model.order.OrderRequest;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.utils.CartManager;
import namnt.vn.coffestore.utils.CurrencyUtils;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    private ImageView ivProductImage, btnBack, btnFavorite;
    private ImageView btnDecreaseQty, btnIncreaseQty;
    private TextView tvProductName, tvDescription, tvAbout;
    private TextView tvPrice, tvOldPrice, tvQuantity, tvTotalPrice, tvSelectedAddons;
    private TextView btnTempHot, btnTempColdBrew, btnTempIce;
    private TextView btnSweetnessSweet, btnSweetnessNormal, btnSweetnessLess, btnSweetnessNoSugar;
    private TextView btnMilkDairy, btnMilkCondensed, btnMilkPlant, btnMilkNone;
    private MaterialButton btnAddToCart;
    
    private ApiService apiService;
    private AuthViewModel authViewModel;

    private String productId, productName, productImage, productCategory, productDescription;
    private double productPrice;
    private Double productOldPrice;
    
    private int quantity = 1;
    private String selectedSize = "M"; // Default size
    private int selectedTemperature = 0; // Default: 0=Hot
    private int selectedSweetness = 1; // Default: 1=Normal
    private int selectedMilkType = 0; // Default: 0=Dairy
    private List<Addon> availableAddons = new ArrayList<>();
    private List<String> selectedAddonIds = new ArrayList<>();
    private List<ProductVariant> variants;
    private Map<String, ProductVariant> variantMap; // Map size -> variant

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initAuthViewModel();
        initViews();
        getProductData();
        setupQuantityControls();
        setupClickListeners();
        updateTotalPrice();
        
        // Initialize API service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        
        tvProductName = findViewById(R.id.tvProductName);
        tvDescription = findViewById(R.id.tvDescription);
        tvAbout = findViewById(R.id.tvAbout);
        tvPrice = findViewById(R.id.tvPrice);
        tvOldPrice = findViewById(R.id.tvOldPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvSelectedAddons = findViewById(R.id.tvSelectedAddons);
        
        View btnOpenCustomize = findViewById(R.id.btnOpenCustomize);
        btnOpenCustomize.setOnClickListener(v -> showCustomizationDialog());
        
        btnDecreaseQty = findViewById(R.id.btnDecreaseQty);
        btnIncreaseQty = findViewById(R.id.btnIncreaseQty);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        
        // Temperature buttons
        btnTempHot = findViewById(R.id.btnTempHot);
        btnTempColdBrew = findViewById(R.id.btnTempColdBrew);
        btnTempIce = findViewById(R.id.btnTempIce);
        
        // Sweetness buttons
        btnSweetnessSweet = findViewById(R.id.btnSweetnessSweet);
        btnSweetnessNormal = findViewById(R.id.btnSweetnessNormal);
        btnSweetnessLess = findViewById(R.id.btnSweetnessLess);
        btnSweetnessNoSugar = findViewById(R.id.btnSweetnessNoSugar);
        
        // Milk type buttons
        btnMilkDairy = findViewById(R.id.btnMilkDairy);
        btnMilkCondensed = findViewById(R.id.btnMilkCondensed);
        btnMilkPlant = findViewById(R.id.btnMilkPlant);
        btnMilkNone = findViewById(R.id.btnMilkNone);
    }

    private void getProductData() {
        // Get data from intent
        productId = getIntent().getStringExtra("PRODUCT_ID");
        productName = getIntent().getStringExtra("PRODUCT_NAME");
        productDescription = getIntent().getStringExtra("PRODUCT_DESCRIPTION");
        productPrice = getIntent().getDoubleExtra("PRODUCT_PRICE", 0);
        productOldPrice = getIntent().hasExtra("PRODUCT_OLD_PRICE") ? 
            getIntent().getDoubleExtra("PRODUCT_OLD_PRICE", 0) : null;
        productImage = getIntent().getStringExtra("PRODUCT_IMAGE");
        productCategory = getIntent().getStringExtra("PRODUCT_CATEGORY");
        
        // Parse variants from JSON
        String variantsJson = getIntent().getStringExtra("PRODUCT_VARIANTS");
        if (variantsJson != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ProductVariant>>(){}.getType();
            variants = gson.fromJson(variantsJson, listType);
            
            // Create map for quick lookup
            variantMap = new HashMap<>();
            for (ProductVariant variant : variants) {
                variantMap.put(variant.getSize(), variant);
            }
            
            Log.d(TAG, "Loaded " + variants.size() + " variants");
        }

        // Set data to views
        tvProductName.setText(productName);

        if (!TextUtils.isEmpty(productDescription)) {
            tvDescription.setText(productDescription);
        }

        // Set initial price from selected size variant
        if (variantMap != null && variantMap.containsKey(selectedSize)) {
            productPrice = variantMap.get(selectedSize).getBasePrice();
        }
        tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
        
        if (productOldPrice != null && productOldPrice > 0) {
            tvOldPrice.setVisibility(TextView.VISIBLE);
            tvOldPrice.setText(CurrencyUtils.formatPrice(productOldPrice));
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOldPrice.setVisibility(TextView.GONE);
        }

        // Load image
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this)
                .load(productImage)
                .centerCrop()
                .into(ivProductImage);
        }
    }

    
    private void setupTemperatureButtons() {
        // Set Hot as default
        btnTempHot.setSelected(true);
        
        btnTempHot.setOnClickListener(v -> selectTemperature(0, btnTempHot)); // 0=Hot
        btnTempColdBrew.setOnClickListener(v -> selectTemperature(1, btnTempColdBrew)); // 1=ColdBrew
        btnTempIce.setOnClickListener(v -> selectTemperature(2, btnTempIce)); // 2=Ice
    }
    
    private void selectTemperature(int temperature, TextView button) {
        selectedTemperature = temperature;
        
        // Reset all buttons
        btnTempHot.setSelected(false);
        btnTempColdBrew.setSelected(false);
        btnTempIce.setSelected(false);
        
        // Select clicked button
        button.setSelected(true);
        Log.d(TAG, "Selected temperature: " + temperature);
    }
    
    private void setupSweetnessButtons() {
        // Set Normal as default
        btnSweetnessNormal.setSelected(true);
        
        btnSweetnessSweet.setOnClickListener(v -> selectSweetness(0, btnSweetnessSweet)); // 0=Sweet
        btnSweetnessNormal.setOnClickListener(v -> selectSweetness(1, btnSweetnessNormal)); // 1=Normal
        btnSweetnessLess.setOnClickListener(v -> selectSweetness(2, btnSweetnessLess)); // 2=Less
        btnSweetnessNoSugar.setOnClickListener(v -> selectSweetness(3, btnSweetnessNoSugar)); // 3=NoSugar
    }
    
    private void selectSweetness(int sweetness, TextView button) {
        selectedSweetness = sweetness;
        
        // Reset all buttons
        btnSweetnessSweet.setSelected(false);
        btnSweetnessNormal.setSelected(false);
        btnSweetnessLess.setSelected(false);
        btnSweetnessNoSugar.setSelected(false);
        
        // Select clicked button
        button.setSelected(true);
        Log.d(TAG, "Selected sweetness: " + sweetness);
    }
    
    private void setupMilkTypeButtons() {
        // Set Dairy as default
        btnMilkDairy.setSelected(true);
        
        btnMilkDairy.setOnClickListener(v -> selectMilkType(0, btnMilkDairy)); // 0=Dairy
        btnMilkCondensed.setOnClickListener(v -> selectMilkType(1, btnMilkCondensed)); // 1=Condensed
        btnMilkPlant.setOnClickListener(v -> selectMilkType(2, btnMilkPlant)); // 2=Plant
        btnMilkNone.setOnClickListener(v -> selectMilkType(3, btnMilkNone)); // 3=None
    }
    
    private void selectMilkType(int milkType, TextView button) {
        selectedMilkType = milkType;
        
        // Reset all buttons
        btnMilkDairy.setSelected(false);
        btnMilkCondensed.setSelected(false);
        btnMilkPlant.setSelected(false);
        btnMilkNone.setSelected(false);
        
        // Select clicked button
        button.setSelected(true);
        Log.d(TAG, "Selected milk type: " + milkType);
    }

    
    private void showCustomizationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_customize_order);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Set dialog width to 90% of screen width
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(params);
        
        // Get dialog views - Size
        View btnDialogSizeSmall = dialog.findViewById(R.id.btnDialogSizeSmall);
        View btnDialogSizeMedium = dialog.findViewById(R.id.btnDialogSizeMedium);
        View btnDialogSizeLarge = dialog.findViewById(R.id.btnDialogSizeLarge);
        ImageView checkSizeSmall = dialog.findViewById(R.id.checkSizeSmall);
        ImageView checkSizeMedium = dialog.findViewById(R.id.checkSizeMedium);
        ImageView checkSizeLarge = dialog.findViewById(R.id.checkSizeLarge);
        
        // Temperature
        View btnDialogTempHot = dialog.findViewById(R.id.btnDialogTempHot);
        View btnDialogTempColdBrew = dialog.findViewById(R.id.btnDialogTempColdBrew);
        View btnDialogTempIce = dialog.findViewById(R.id.btnDialogTempIce);
        ImageView checkTempHot = dialog.findViewById(R.id.checkTempHot);
        ImageView checkTempColdBrew = dialog.findViewById(R.id.checkTempColdBrew);
        ImageView checkTempIce = dialog.findViewById(R.id.checkTempIce);
        
        View btnDialogSweetnessSweet = dialog.findViewById(R.id.btnDialogSweetnessSweet);
        View btnDialogSweetnessNormal = dialog.findViewById(R.id.btnDialogSweetnessNormal);
        View btnDialogSweetnessLess = dialog.findViewById(R.id.btnDialogSweetnessLess);
        View btnDialogSweetnessNoSugar = dialog.findViewById(R.id.btnDialogSweetnessNoSugar);
        ImageView checkSweetnessSweet = dialog.findViewById(R.id.checkSweetnessSweet);
        ImageView checkSweetnessNormal = dialog.findViewById(R.id.checkSweetnessNormal);
        ImageView checkSweetnessLess = dialog.findViewById(R.id.checkSweetnessLess);
        ImageView checkSweetnessNoSugar = dialog.findViewById(R.id.checkSweetnessNoSugar);
        
        View btnDialogMilkDairy = dialog.findViewById(R.id.btnDialogMilkDairy);
        View btnDialogMilkCondensed = dialog.findViewById(R.id.btnDialogMilkCondensed);
        View btnDialogMilkPlant = dialog.findViewById(R.id.btnDialogMilkPlant);
        View btnDialogMilkNone = dialog.findViewById(R.id.btnDialogMilkNone);
        ImageView checkMilkDairy = dialog.findViewById(R.id.checkMilkDairy);
        ImageView checkMilkCondensed = dialog.findViewById(R.id.checkMilkCondensed);
        ImageView checkMilkPlant = dialog.findViewById(R.id.checkMilkPlant);
        ImageView checkMilkNone = dialog.findViewById(R.id.checkMilkNone);
        
        TextView tvDialogQuantity = dialog.findViewById(R.id.tvDialogQuantity);
        ImageView btnDialogDecreaseQty = dialog.findViewById(R.id.btnDialogDecreaseQty);
        ImageView btnDialogIncreaseQty = dialog.findViewById(R.id.btnDialogIncreaseQty);
        
        // Addons
        LinearLayout layoutAddons = dialog.findViewById(R.id.layoutAddons);
        TextView tvLoadingAddons = dialog.findViewById(R.id.tvLoadingAddons);
        
        com.google.android.material.button.MaterialButton btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        com.google.android.material.button.MaterialButton btnDialogConfirm = dialog.findViewById(R.id.btnDialogConfirm);
        
        // Set current selections - Size
        checkSizeSmall.setSelected(selectedSize.equals("S"));
        checkSizeMedium.setSelected(selectedSize.equals("M"));
        checkSizeLarge.setSelected(selectedSize.equals("L"));
        
        // Temperature
        checkTempHot.setSelected(selectedTemperature == 0);
        checkTempColdBrew.setSelected(selectedTemperature == 1);
        checkTempIce.setSelected(selectedTemperature == 2);
        
        checkSweetnessSweet.setSelected(selectedSweetness == 0);
        checkSweetnessNormal.setSelected(selectedSweetness == 1);
        checkSweetnessLess.setSelected(selectedSweetness == 2);
        checkSweetnessNoSugar.setSelected(selectedSweetness == 3);
        
        checkMilkDairy.setSelected(selectedMilkType == 0);
        checkMilkCondensed.setSelected(selectedMilkType == 1);
        checkMilkPlant.setSelected(selectedMilkType == 2);
        checkMilkNone.setSelected(selectedMilkType == 3);
        
        tvDialogQuantity.setText(String.valueOf(quantity));
        
        // Size listeners
        btnDialogSizeSmall.setOnClickListener(v -> {
            selectedSize = "S";
            checkSizeSmall.setSelected(true);
            checkSizeMedium.setSelected(false);
            checkSizeLarge.setSelected(false);
            if (variantMap != null && variantMap.containsKey("S")) {
                productPrice = variantMap.get("S").getBasePrice();
                tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
                updateTotalPrice();
            }
        });
        btnDialogSizeMedium.setOnClickListener(v -> {
            selectedSize = "M";
            checkSizeSmall.setSelected(false);
            checkSizeMedium.setSelected(true);
            checkSizeLarge.setSelected(false);
            if (variantMap != null && variantMap.containsKey("M")) {
                productPrice = variantMap.get("M").getBasePrice();
                tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
                updateTotalPrice();
            }
        });
        btnDialogSizeLarge.setOnClickListener(v -> {
            selectedSize = "L";
            checkSizeSmall.setSelected(false);
            checkSizeMedium.setSelected(false);
            checkSizeLarge.setSelected(true);
            if (variantMap != null && variantMap.containsKey("L")) {
                productPrice = variantMap.get("L").getBasePrice();
                tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
                updateTotalPrice();
            }
        });
        
        // Temperature listeners
        btnDialogTempHot.setOnClickListener(v -> {
            selectedTemperature = 0;
            checkTempHot.setSelected(true);
            checkTempColdBrew.setSelected(false);
            checkTempIce.setSelected(false);
        });
        btnDialogTempColdBrew.setOnClickListener(v -> {
            selectedTemperature = 1;
            checkTempHot.setSelected(false);
            checkTempColdBrew.setSelected(true);
            checkTempIce.setSelected(false);
        });
        btnDialogTempIce.setOnClickListener(v -> {
            selectedTemperature = 2;
            checkTempHot.setSelected(false);
            checkTempColdBrew.setSelected(false);
            checkTempIce.setSelected(true);
        });
        
        // Sweetness listeners
        btnDialogSweetnessSweet.setOnClickListener(v -> {
            selectedSweetness = 0;
            checkSweetnessSweet.setSelected(true);
            checkSweetnessNormal.setSelected(false);
            checkSweetnessLess.setSelected(false);
            checkSweetnessNoSugar.setSelected(false);
        });
        btnDialogSweetnessNormal.setOnClickListener(v -> {
            selectedSweetness = 1;
            checkSweetnessSweet.setSelected(false);
            checkSweetnessNormal.setSelected(true);
            checkSweetnessLess.setSelected(false);
            checkSweetnessNoSugar.setSelected(false);
        });
        btnDialogSweetnessLess.setOnClickListener(v -> {
            selectedSweetness = 2;
            checkSweetnessSweet.setSelected(false);
            checkSweetnessNormal.setSelected(false);
            checkSweetnessLess.setSelected(true);
            checkSweetnessNoSugar.setSelected(false);
        });
        btnDialogSweetnessNoSugar.setOnClickListener(v -> {
            selectedSweetness = 3;
            checkSweetnessSweet.setSelected(false);
            checkSweetnessNormal.setSelected(false);
            checkSweetnessLess.setSelected(false);
            checkSweetnessNoSugar.setSelected(true);
        });
        
        // Milk type listeners
        btnDialogMilkDairy.setOnClickListener(v -> {
            selectedMilkType = 0;
            checkMilkDairy.setSelected(true);
            checkMilkCondensed.setSelected(false);
            checkMilkPlant.setSelected(false);
            checkMilkNone.setSelected(false);
        });
        btnDialogMilkCondensed.setOnClickListener(v -> {
            selectedMilkType = 1;
            checkMilkDairy.setSelected(false);
            checkMilkCondensed.setSelected(true);
            checkMilkPlant.setSelected(false);
            checkMilkNone.setSelected(false);
        });
        btnDialogMilkPlant.setOnClickListener(v -> {
            selectedMilkType = 2;
            checkMilkDairy.setSelected(false);
            checkMilkCondensed.setSelected(false);
            checkMilkPlant.setSelected(true);
            checkMilkNone.setSelected(false);
        });
        btnDialogMilkNone.setOnClickListener(v -> {
            selectedMilkType = 3;
            checkMilkDairy.setSelected(false);
            checkMilkCondensed.setSelected(false);
            checkMilkPlant.setSelected(false);
            checkMilkNone.setSelected(true);
        });
        
        // Quantity listeners
        final int[] dialogQuantity = {quantity};
        btnDialogDecreaseQty.setOnClickListener(v -> {
            if (dialogQuantity[0] > 1) {
                dialogQuantity[0]--;
                tvDialogQuantity.setText(String.valueOf(dialogQuantity[0]));
            }
        });
        btnDialogIncreaseQty.setOnClickListener(v -> {
            dialogQuantity[0]++;
            tvDialogQuantity.setText(String.valueOf(dialogQuantity[0]));
        });
        
        // Cancel button
        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Confirm button
        btnDialogConfirm.setOnClickListener(v -> {
            quantity = dialogQuantity[0];
            tvQuantity.setText(String.valueOf(quantity));
            
            // Collect selected addons
            selectedAddonIds.clear();
            List<String> addonNames = new ArrayList<>();
            for (Addon addon : availableAddons) {
                if (addon.isSelected()) {
                    selectedAddonIds.add(addon.getId());
                    addonNames.add(addon.getName());
                }
            }
            Log.d(TAG, "Selected addons: " + selectedAddonIds.size());
            
            // Display selected addons
            if (!addonNames.isEmpty()) {
                tvSelectedAddons.setText("🧋 Topping: " + String.join(", ", addonNames));
                tvSelectedAddons.setVisibility(View.VISIBLE);
            } else {
                tvSelectedAddons.setVisibility(View.GONE);
            }
            
            updateTotalPrice();
            dialog.dismiss();
        });
        
        // Load addons from API
        loadAddons(layoutAddons, tvLoadingAddons);
        
        dialog.show();
    }
    
    private void loadAddons(LinearLayout layoutAddons, TextView tvLoadingAddons) {
        Call<ApiResponse<List<Addon>>> call = apiService.getAddons();
        call.enqueue(new Callback<ApiResponse<List<Addon>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Addon>>> call, Response<ApiResponse<List<Addon>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    availableAddons = response.body().getData();
                    tvLoadingAddons.setVisibility(View.GONE);
                    
                    // Display addons
                    for (Addon addon : availableAddons) {
                        if (addon.isActive()) {
                            View addonView = createAddonView(addon, layoutAddons.getContext());
                            layoutAddons.addView(addonView);
                        }
                    }
                    
                    Log.d(TAG, "Loaded " + availableAddons.size() + " addons");
                } else {
                    tvLoadingAddons.setText("Không có topping");
                    Log.e(TAG, "Failed to load addons");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Addon>>> call, Throwable t) {
                tvLoadingAddons.setText("Lỗi tải topping");
                Log.e(TAG, "Error loading addons: " + t.getMessage());
            }
        });
    }
    
    private View createAddonView(Addon addon, android.content.Context context) {
        LinearLayout addonLayout = new LinearLayout(context);
        addonLayout.setOrientation(LinearLayout.HORIZONTAL);
        addonLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        addonLayout.setPadding(0, 12, 0, 12);
        addonLayout.setClickable(true);
        addonLayout.setFocusable(true);
        
        // Checkbox
        ImageView checkbox = new ImageView(context);
        LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(32, 32);
        checkboxParams.setMarginEnd(16);
        checkbox.setLayoutParams(checkboxParams);
        checkbox.setBackground(getResources().getDrawable(R.drawable.bg_checkbox_selector));
        checkbox.setSelected(addon.isSelected());
        
        // Text
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textView.setLayoutParams(textParams);
        textView.setText(addon.getName() + " (+" + CurrencyUtils.formatPrice(addon.getPrice()) + ")");
        textView.setTextColor(getResources().getColor(R.color.text_white));
        textView.setTextSize(16);
        
        addonLayout.addView(checkbox);
        addonLayout.addView(textView);
        
        // Click listener
        addonLayout.setOnClickListener(v -> {
            addon.setSelected(!addon.isSelected());
            checkbox.setSelected(addon.isSelected());
        });
        
        return addonLayout;
    }

    private void setupQuantityControls() {
        tvQuantity.setText(String.valueOf(quantity));
        
        btnDecreaseQty.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });
        
        btnIncreaseQty.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        });
    }

    private void updateTotalPrice() {
        // Base price
        double total = productPrice * quantity;
        
        // Add addons price
        double addonsTotal = 0;
        for (Addon addon : availableAddons) {
            if (addon.isSelected()) {
                addonsTotal += addon.getPrice();
            }
        }
        
        // Total with addons
        total += (addonsTotal * quantity);
        
        Log.d(TAG, "Price breakdown - Base: " + productPrice + 
                   ", Addons: " + addonsTotal + 
                   ", Quantity: " + quantity + 
                   ", Total: " + total);
        
        tvTotalPrice.setText(CurrencyUtils.formatPrice(total));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        });
        
        btnAddToCart.setOnClickListener(v -> {
            if (variantMap != null && variantMap.containsKey(selectedSize)) {
                addToCart();
            } else {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
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
    
    private void addToCart() {
        if (variantMap == null || !variantMap.containsKey(selectedSize)) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ProductVariant selectedVariant = variantMap.get(selectedSize);
        String variantId = selectedVariant.getId();
        
        // Create CartItem
        CartItem cartItem = new CartItem(
            productId,
            variantId,
            productName,
            productPrice,
            productImage,
            selectedSize,
            quantity,
            convertTemperatureToString(selectedTemperature),
            convertSweetnessToString(selectedSweetness),
            convertMilkTypeToString(selectedMilkType),
            new ArrayList<>(selectedAddonIds)
        );
        
        // Add to local cart
        CartManager.getInstance(this).addItem(cartItem);
        
        Log.d(TAG, "=== ADDED TO LOCAL CART ===");
        Log.d(TAG, "Product: " + productName);
        Log.d(TAG, "Variant ID: " + variantId);
        Log.d(TAG, "Quantity: " + quantity);
        Log.d(TAG, "Total items in cart: " + CartManager.getInstance(this).getCartItemCount());
        
        // Show success message
        Toast.makeText(this, "✓ Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        
        // Show success dialog
        showSuccessDialog();
    }
    
    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(this);
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialog_success);
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Set dialog width and animation
        android.view.WindowManager.LayoutParams params = successDialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        successDialog.getWindow().setAttributes(params);
        successDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        
        // Get views
        ImageView ivSuccessIcon = successDialog.findViewById(R.id.ivSuccessIcon);
        com.google.android.material.button.MaterialButton btnOk = successDialog.findViewById(R.id.btnOk);
        
        // Start animations
        android.view.animation.Animation scaleUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_up);
        ivSuccessIcon.startAnimation(scaleUp);
        
        // OK button listener
        btnOk.setOnClickListener(v -> {
            successDialog.dismiss();
            // Navigate to MenuActivity (Home)
            Intent intent = new Intent(ProductDetailActivity.this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        // Auto dismiss after 2 seconds
        new android.os.Handler().postDelayed(() -> {
            if (successDialog.isShowing()) {
                successDialog.dismiss();
                // Navigate to MenuActivity (Home)
                Intent intent = new Intent(ProductDetailActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        }, 2000);
        
        successDialog.show();
    }
    
    // Helper methods to convert int to String for CartItem
    private String convertTemperatureToString(int temperature) {
        switch (temperature) {
            case 0: return "Hot";
            case 1: return "ColdBrew";
            case 2: return "Ice";
            default: return "Hot";
        }
    }
    
    private String convertSweetnessToString(int sweetness) {
        switch (sweetness) {
            case 0: return "Sweet";
            case 1: return "Normal";
            case 2: return "Less";
            case 3: return "NoSugar";
            default: return "Normal";
        }
    }
    
    private String convertMilkTypeToString(int milkType) {
        switch (milkType) {
            case 0: return "Dairy";
            case 1: return "Condensed";
            case 2: return "Plant";
            case 3: return "None";
            default: return "Dairy";
        }
    }
}

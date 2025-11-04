package namnt.vn.coffestore.ui.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.ProductVariant;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    private ImageView ivProductImage, btnBack, btnFavorite;
    private ImageView btnDecreaseQty, btnIncreaseQty;
    private TextView tvProductName, tvDescription, tvAbout;
    private TextView tvPrice, tvOldPrice, tvQuantity, tvTotalPrice;
    private TextView btnSizeSmall, btnSizeMedium, btnSizeLarge;
    private MaterialButton btnAddToCart;

    private String productId, productName, productImage, productCategory, productDescription;
    private double productPrice;
    private Double productOldPrice;
    
    private int quantity = 1;
    private String selectedSize = "M"; // Default size
    private List<ProductVariant> variants;
    private Map<String, ProductVariant> variantMap; // Map size -> variant

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        getProductData();
        setupSizeButtons();
        setupQuantityControls();
        setupClickListeners();
        updateTotalPrice();
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
        
        btnSizeSmall = findViewById(R.id.btnSizeSmall);
        btnSizeMedium = findViewById(R.id.btnSizeMedium);
        btnSizeLarge = findViewById(R.id.btnSizeLarge);
        
        btnDecreaseQty = findViewById(R.id.btnDecreaseQty);
        btnIncreaseQty = findViewById(R.id.btnIncreaseQty);
        btnAddToCart = findViewById(R.id.btnAddToCart);
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

    private void setupSizeButtons() {
        // Set Medium as default
        btnSizeMedium.setSelected(true);
        
        // Enable/disable buttons based on available variants
        if (variantMap != null) {
            btnSizeSmall.setEnabled(variantMap.containsKey("S"));
            btnSizeMedium.setEnabled(variantMap.containsKey("M"));
            btnSizeLarge.setEnabled(variantMap.containsKey("L"));
        }
        
        btnSizeSmall.setOnClickListener(v -> selectSize("S", btnSizeSmall));
        btnSizeMedium.setOnClickListener(v -> selectSize("M", btnSizeMedium));
        btnSizeLarge.setOnClickListener(v -> selectSize("L", btnSizeLarge));
    }

    private void selectSize(String size, TextView button) {
        selectedSize = size;
        
        // Reset all buttons
        btnSizeSmall.setSelected(false);
        btnSizeMedium.setSelected(false);
        btnSizeLarge.setSelected(false);
        
        // Select clicked button
        button.setSelected(true);
        
        // Update price based on selected variant
        if (variantMap != null && variantMap.containsKey(size)) {
            productPrice = variantMap.get(size).getBasePrice();
            tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
            Log.d(TAG, "Selected size " + size + " with price: " + productPrice);
        }
        
        updateTotalPrice();
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
        double total = productPrice * quantity;
        tvTotalPrice.setText(CurrencyUtils.formatPrice(total));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        });
        
        btnAddToCart.setOnClickListener(v -> {
            if (variantMap != null && variantMap.containsKey(selectedSize)) {
                ProductVariant selectedVariant = variantMap.get(selectedSize);
                String message = String.format("Added %d x %s (Size %s - %s) to cart", 
                    quantity, productName, selectedSize, CurrencyUtils.formatPrice(productPrice));
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                
                Log.d(TAG, "Add to cart - Variant ID: " + selectedVariant.getId() + 
                    ", Size: " + selectedSize + ", Quantity: " + quantity + 
                    ", Price: " + productPrice);
                
                // TODO: Add to cart logic with variant ID
                // CartItem item = new CartItem(selectedVariant.getId(), quantity);
            } else {
                Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

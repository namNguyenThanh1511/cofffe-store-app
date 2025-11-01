package namnt.vn.coffestore.ui.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage, btnBack, btnFavorite;
    private ImageView btnDecreaseQty, btnIncreaseQty;
    private TextView tvProductName, tvDescription, tvAbout;
    private TextView tvPrice, tvOldPrice, tvQuantity, tvTotalPrice;
    private TextView btnSizeSmall, btnSizeMedium, btnSizeLarge;
    private MaterialButton btnAddToCart;

    private String productId, productName, productImage, productCategory;
    private double productPrice;
    private Double productOldPrice;
    
    private int quantity = 1;
    private String selectedSize = "Medium";
    private double basePrice = 0;

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
        productPrice = getIntent().getDoubleExtra("PRODUCT_PRICE", 0);
        productOldPrice = getIntent().hasExtra("PRODUCT_OLD_PRICE") ? 
            getIntent().getDoubleExtra("PRODUCT_OLD_PRICE", 0) : null;
        productImage = getIntent().getStringExtra("PRODUCT_IMAGE");
        productCategory = getIntent().getStringExtra("PRODUCT_CATEGORY");

        basePrice = productPrice;

        // Set data to views
        tvProductName.setText(productName);
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
        
        btnSizeSmall.setOnClickListener(v -> selectSize("Small", btnSizeSmall, 0.8));
        btnSizeMedium.setOnClickListener(v -> selectSize("Medium", btnSizeMedium, 1.0));
        btnSizeLarge.setOnClickListener(v -> selectSize("Large", btnSizeLarge, 1.2));
    }

    private void selectSize(String size, TextView button, double priceMultiplier) {
        selectedSize = size;
        
        // Reset all buttons
        btnSizeSmall.setSelected(false);
        btnSizeMedium.setSelected(false);
        btnSizeLarge.setSelected(false);
        
        // Select clicked button
        button.setSelected(true);
        
        // Update price based on size
        productPrice = basePrice * priceMultiplier;
        tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
        
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
            String message = String.format("Added %d x %s (%s) to cart", 
                quantity, productName, selectedSize);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // TODO: Add to cart logic
        });
    }
}

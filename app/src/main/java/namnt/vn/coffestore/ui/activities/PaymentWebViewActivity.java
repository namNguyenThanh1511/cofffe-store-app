package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.api.ApiResponse;
import namnt.vn.coffestore.data.model.order.PayingRequest;
import namnt.vn.coffestore.network.ApiService;
import namnt.vn.coffestore.network.RetrofitClient;
import namnt.vn.coffestore.viewmodel.AuthViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class PaymentWebViewActivity extends AppCompatActivity {
    private static final String TAG = "PaymentWebViewActivity";
    public static final String EXTRA_PAYMENT_URL = "extra_payment_url";
    public static final String EXTRA_ORDER_RESPONSE = "extra_order_response";

    private WebView webView;
    private ProgressBar progressBar;
    private ImageView btnBack;
    
    private String orderResponseJson;
    private ApiService apiService;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);

        // Initialize API service and AuthViewModel
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        authViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AuthViewModel(getApplication());
            }
        }).get(AuthViewModel.class);

        initViews();
        
        // Check if opened via deep link
        handleIntent(getIntent());
        
        // If not deep link, load payment URL
        if (getIntent().getData() == null) {
            loadPaymentUrl();
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        setupWebView();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                
                // Check if URL is payment return from PayOS (localhost redirect)
                if (url != null && url.contains("/api/payment/payos/return")) {
                    handlePaymentReturn(url);
                    return;
                }
                
                // Check if URL is a cancel redirect (localhost/payos/cancel)
                if (url != null && url.contains("/payos/cancel")) {
                    handlePaymentCancel(url);
                    return;
                }
                
                // Check if URL is a redirect back from PayOS
                if (url != null && (url.contains("paymentStatus=") || url.contains("transactionType="))) {
                    handlePaymentRedirect(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Check if this is payment return
                if (url != null && url.contains("/api/payment/payos/return")) {
                    handlePaymentReturn(url);
                    return true;
                }
                
                // Check if this is a cancel redirect
                if (url != null && url.contains("/payos/cancel")) {
                    handlePaymentCancel(url);
                    return true;
                }
                
                // Check if this is payment result redirect
                if (url != null && (url.contains("paymentStatus=") || url.contains("transactionType="))) {
                    handlePaymentRedirect(url);
                    return true;
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "mycoffeeapp".equals(data.getScheme())) {
            // Deep link from PayOS: mycoffeeapp://payos/return?status=...&orderCode=...&cancel=...
            String status = data.getQueryParameter("status");
            String orderCode = data.getQueryParameter("orderCode");
            String cancel = data.getQueryParameter("cancel");
            
            // Get order response from intent extras or SharedPreferences
            orderResponseJson = intent.getStringExtra(EXTRA_ORDER_RESPONSE);
            if (orderResponseJson == null) {
                // Retrieve from SharedPreferences (saved during payment initiation)
                orderResponseJson = getSharedPreferences("payment_prefs", MODE_PRIVATE)
                    .getString("pending_order_response", null);
            }
            
            // Navigate to OrderBillActivity with payment result
            Intent billIntent = new Intent(this, OrderBillActivity.class);
            billIntent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
            billIntent.putExtra("payment_status", status);
            billIntent.putExtra("order_code", orderCode);
            billIntent.putExtra("payment_cancelled", cancel);
            startActivity(billIntent);
            
            // Clear saved order data
            getSharedPreferences("payment_prefs", MODE_PRIVATE)
                .edit()
                .remove("pending_order_response")
                .apply();
            
            finish();
        }
    }
    
    private void loadPaymentUrl() {
        String paymentUrl = getIntent().getStringExtra(EXTRA_PAYMENT_URL);
        orderResponseJson = getIntent().getStringExtra(EXTRA_ORDER_RESPONSE);
        
        // Save order data to SharedPreferences for deep link recovery
        if (orderResponseJson != null) {
            getSharedPreferences("payment_prefs", MODE_PRIVATE)
                .edit()
                .putString("pending_order_response", orderResponseJson)
                .apply();
        }
        
        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            Toast.makeText(this, "URL thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handlePaymentRedirect(String url) {
        try {
            Uri uri = Uri.parse(url);
            
            // PayOS params:
            // - PaymentStatus: PAID, UNPAID
            // - PaymentTransactionType: PENDING, SUCCESS, FAILED
            String paymentStatus = uri.getQueryParameter("paymentStatus");
            String transactionType = uri.getQueryParameter("transactionType");
            String orderCode = uri.getQueryParameter("orderCode");
            String cancel = uri.getQueryParameter("cancel");

            // Navigate to OrderBillActivity with payment result
            Intent intent = new Intent(this, OrderBillActivity.class);
            intent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
            intent.putExtra("payment_status", paymentStatus);
            intent.putExtra("transaction_type", transactionType);
            intent.putExtra("order_code", orderCode);
            intent.putExtra("payment_cancelled", cancel);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý kết quả thanh toán", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handlePaymentReturn(String url) {
        try {
            Uri uri = Uri.parse(url);
            
            // Parse return URL params from backend
            // URL format: http://localhost:7000/api/payment/payos/return?code=00&id=...
            String code = uri.getQueryParameter("code");
            String id = uri.getQueryParameter("id");
            String orderCode = uri.getQueryParameter("orderCode");
            Log.d(TAG,"Payment return url : " + url);
            Log.d(TAG, "Payment return - code: " + code + ", id: " + id);

            // Map code to payment status
            // code "00" = success, other = failed
            String paymentStatus;
            if ("00".equals(code)) {
                paymentStatus = "PAID";
                
                // Call API to confirm payment
                Log.d(TAG, "Payment successful, calling /api/orders/paying...");
                callConfirmPaymentApi(orderCode, paymentStatus);
            } else {
                paymentStatus = "UNPAID";
                // Navigate immediately for failed payment
                navigateToOrderBill(paymentStatus, id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling payment return: " + e.getMessage());
            Toast.makeText(this, "Lỗi xử lý kết quả thanh toán", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void callConfirmPaymentApi(String orderCode, String paymentStatus) {
        String accessToken = authViewModel.getAccessToken();
        if (accessToken.isEmpty()) {
            Log.e(TAG, "Access token is empty, navigating without API call");
            navigateToOrderBill(paymentStatus, orderCode);
            return;
        }
        
        String bearerToken = "Bearer " + accessToken;
        PayingRequest request = new PayingRequest(orderCode);
        
        Log.d(TAG, "Calling confirmPayment API with orderCode: " + orderCode);
        Call<ApiResponse<String>> call = apiService.confirmPayment(bearerToken, request);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✓ Payment confirmation API success: " + response.body().getMessage());
                } else {
                    Log.e(TAG, "Payment confirmation API failed: " + response.code());
                }
                
                // Navigate regardless of API result
                navigateToOrderBill(paymentStatus, orderCode);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Payment confirmation API error: " + t.getMessage());
                
                // Navigate even if API fails
                navigateToOrderBill(paymentStatus, orderCode);
            }
        });
    }
    
    private void navigateToOrderBill(String paymentStatus, String orderCode) {
        // Navigate to OrderBillActivity with payment result
        Intent intent = new Intent(this, OrderBillActivity.class);
        intent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
        intent.putExtra("payment_status", paymentStatus);
        intent.putExtra("order_code", orderCode);
        intent.putExtra("payment_cancelled", "false");
        startActivity(intent);
        
        // Clear saved order data
        getSharedPreferences("payment_prefs", MODE_PRIVATE)
            .edit()
            .remove("pending_order_response")
            .apply();
        
        finish();
    }
    
    private void handlePaymentCancel(String url) {
        try {
            Uri uri = Uri.parse(url);
            
            // Parse cancel URL params
            String code = uri.getQueryParameter("code");
            String id = uri.getQueryParameter("id");
            
            // Navigate to OrderBillActivity with cancel status
            Intent intent = new Intent(this, OrderBillActivity.class);
            intent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
            intent.putExtra("payment_status", "CANCELLED");
            intent.putExtra("transaction_type", "CANCELLED");
            intent.putExtra("order_code", id);
            intent.putExtra("payment_cancelled", "true");
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý hủy thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // User pressed back at root page - treat as cancel
            Intent intent = new Intent(this, OrderBillActivity.class);
            intent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
            intent.putExtra("payment_status", "CANCELLED");
            intent.putExtra("transaction_type", "CANCELLED");
            intent.putExtra("payment_cancelled", "true");
            startActivity(intent);
            finish();
        }
    }
}

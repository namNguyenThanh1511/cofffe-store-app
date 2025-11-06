package namnt.vn.coffestore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.data.model.order.OrderResponse;
import namnt.vn.coffestore.ui.adapters.BillItemAdapter;
import namnt.vn.coffestore.utils.CurrencyUtils;

public class OrderBillActivity extends AppCompatActivity {
    private static final String TAG = "OrderBillActivity";
    public static final String EXTRA_ORDER_RESPONSE = "extra_order_response";

    private ImageView btnBack;
    private TextView tvPaymentStatus, tvPaymentMessage, tvOrderId, tvOrderDate, tvSubtotal, tvDeliveryFee, tvTotal;
    private RecyclerView rvOrderItems;
    private MaterialButton btnViewOrders;

    private BillItemAdapter billItemAdapter;
    private OrderResponse orderResponse;
    private String paymentStatus;
    private String transactionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_bill);

        initViews();
        loadOrderData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMessage = findViewById(R.id.tvPaymentMessage);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnViewOrders = findViewById(R.id.btnViewOrders);

        // Setup RecyclerView
        billItemAdapter = new BillItemAdapter();
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(billItemAdapter);
    }

    private void loadOrderData() {
        // Get order data from intent
        String orderJson = getIntent().getStringExtra(EXTRA_ORDER_RESPONSE);
        paymentStatus = getIntent().getStringExtra("payment_status");
        transactionType = getIntent().getStringExtra("transaction_type");
        
        if (orderJson != null) {
            orderResponse = new Gson().fromJson(orderJson, OrderResponse.class);
            displayPaymentStatus();
            displayOrderInfo();
        }
    }
    
    private void displayPaymentStatus() {
        // PayOS Status from deep link: PAID, UNPAID, CANCELLED
        // Or TransactionType: PENDING, SUCCESS, FAILED, CANCELLED
        
        // Check from deep link status field
        boolean isPaid = "PAID".equalsIgnoreCase(paymentStatus);
        boolean isUnpaid = "UNPAID".equalsIgnoreCase(paymentStatus);
        boolean isCancelled = "CANCELLED".equalsIgnoreCase(paymentStatus);
        
        // Check from transaction type (fallback)
        boolean isSuccess = "SUCCESS".equalsIgnoreCase(transactionType);
        boolean isPending = "PENDING".equalsIgnoreCase(transactionType);
        boolean isFailed = "FAILED".equalsIgnoreCase(transactionType);
        boolean isTransactionCancelled = "CANCELLED".equalsIgnoreCase(transactionType);
        
        if (isPaid || isSuccess) {
            // Payment successful
            tvPaymentStatus.setText("✓");
            tvPaymentStatus.setBackgroundResource(R.drawable.bg_success_circle);
            tvPaymentMessage.setText("Thanh toán thành công!");
            tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_white));
        } else if (isCancelled || isTransactionCancelled) {
            // Payment cancelled
            tvPaymentStatus.setText("⊗");
            tvPaymentStatus.setBackgroundResource(R.drawable.bg_cancelled_circle);
            tvPaymentMessage.setText("Đã hủy đơn hàng thành công!");
            tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_gray));
        } else if (isPending) {
            // Payment pending
            tvPaymentStatus.setText("⏳");
            tvPaymentStatus.setBackgroundResource(R.drawable.bg_pending_circle);
            tvPaymentMessage.setText("Đang chờ thanh toán...");
            tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_gray));
        } else if (isUnpaid || isFailed) {
            // Payment failed or unpaid
            tvPaymentStatus.setText("✗");
            tvPaymentStatus.setBackgroundResource(R.drawable.bg_failed_circle);
            tvPaymentMessage.setText("Thanh toán thất bại!");
            tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_gray));
        } else {
            // Default - show as unpaid/failed
            tvPaymentStatus.setText("✗");
            tvPaymentStatus.setBackgroundResource(R.drawable.bg_failed_circle);
            tvPaymentMessage.setText("Thanh toán thất bại!");
            tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_gray));
        }
    }

    private void displayOrderInfo() {
        if (orderResponse == null) return;

        // Order ID
        tvOrderId.setText("Mã đơn: #" + orderResponse.getId());

        // Order Date
        String dateStr = formatDate(orderResponse.getOrderDate());
        tvOrderDate.setText(dateStr);

        // Items
        if (orderResponse.getOrderItems() != null) {
            billItemAdapter.setItems(orderResponse.getOrderItems());
        }

        // Calculate totals
        double subtotal = 0;
        if (orderResponse.getOrderItems() != null) {
            for (var item : orderResponse.getOrderItems()) {
                subtotal += item.getUnitPrice() * item.getQuantity();
            }
        }

        double deliveryFee = 0; // Free delivery
        double total = subtotal + deliveryFee;

        // Display totals
        tvSubtotal.setText(CurrencyUtils.formatPrice(subtotal));
        tvDeliveryFee.setText(deliveryFee == 0 ? "Miễn phí" : CurrencyUtils.formatPrice(deliveryFee));
        tvTotal.setText(CurrencyUtils.formatPrice(total));
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            // Return current date if no date provided
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(new Date());
        }
        
        try {
            // Try to parse ISO format or other common formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            // If parsing fails, return the original string or current date
            try {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return outputFormat.format(new Date());
            } catch (Exception ex) {
                return dateString;
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnViewOrders.setOnClickListener(v -> {
            // TODO: Navigate to OrderManagementActivity when it's created
            // For now, go back to MenuActivity
            Intent intent = new Intent(this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}

# 🔗 Deep Link Integration for PayOS

## 📋 Overview
Sau khi user quét mã QR PayOS và thanh toán, PayOS sẽ redirect về app qua deep link với payment result.

---

## ✨ Deep Link Format:

```
mycoffeeapp://payos/return?status=PAID&orderCode=123456&cancel=false
```

### **Parameters:**
- **status**: Payment status (PAID, UNPAID, CANCELLED)
- **orderCode**: Order ID từ hệ thống
- **cancel**: Boolean string (true/false)

---

## 🔧 Implementation:

### **1. AndroidManifest.xml** ✅

**Intent Filter for Deep Link:**
```xml
<activity 
    android:name=".ui.activities.PaymentWebViewActivity"
    android:theme="@style/Theme.CoffeStore.NoActionBar"
    android:launchMode="singleTask"
    android:parentActivityName=".ui.activities.CartActivity">
    
    <!-- Deep Link for PayOS redirect -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="mycoffeeapp"
            android:host="payos"
            android:pathPrefix="/return" />
    </intent-filter>
</activity>
```

**Key Points:**
- `launchMode="singleTask"` - Reuse existing activity instance
- `scheme="mycoffeeapp"` - Custom app scheme
- `host="payos"` - Host name
- `pathPrefix="/return"` - Path for payment return

---

### **2. PaymentWebViewActivity.java** ✅

**Handle Deep Link in onCreate:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_payment_webview);

    initViews();
    
    // Check if opened via deep link
    handleIntent(getIntent());
    
    // If not deep link, load payment URL
    if (getIntent().getData() == null) {
        loadPaymentUrl();
    }
}
```

**Handle New Intent (when activity already running):**
```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntent(intent);
}
```

**Process Deep Link:**
```java
private void handleIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null && "mycoffeeapp".equals(data.getScheme())) {
        // Deep link from PayOS
        String status = data.getQueryParameter("status");
        String orderCode = data.getQueryParameter("orderCode");
        String cancel = data.getQueryParameter("cancel");
        
        // Get order response from SharedPreferences
        orderResponseJson = getSharedPreferences("payment_prefs", MODE_PRIVATE)
            .getString("pending_order_response", null);
        
        // Navigate to OrderBillActivity with result
        Intent billIntent = new Intent(this, OrderBillActivity.class);
        billIntent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
        billIntent.putExtra("payment_status", status);
        billIntent.putExtra("order_code", orderCode);
        billIntent.putExtra("payment_cancelled", cancel);
        startActivity(billIntent);
        
        // Clear saved data
        getSharedPreferences("payment_prefs", MODE_PRIVATE)
            .edit()
            .remove("pending_order_response")
            .apply();
        
        finish();
    }
}
```

**Save Order Data for Deep Link Recovery:**
```java
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
    }
}
```

---

### **3. OrderBillActivity.java** ✅

**Handle Status from Deep Link:**
```java
private void displayPaymentStatus() {
    // Check from deep link status field
    boolean isPaid = "PAID".equalsIgnoreCase(paymentStatus);
    boolean isUnpaid = "UNPAID".equalsIgnoreCase(paymentStatus);
    boolean isCancelled = "CANCELLED".equalsIgnoreCase(paymentStatus);
    
    if (isPaid) {
        // Success: ✓ "Thanh toán thành công!"
        tvPaymentStatus.setText("✓");
        tvPaymentStatus.setBackgroundResource(R.drawable.bg_success_circle);
        tvPaymentMessage.setText("Thanh toán thành công!");
    } else if (isCancelled) {
        // Cancelled: ⊗ "Đã hủy đơn hàng thành công!"
        tvPaymentStatus.setText("⊗");
        tvPaymentStatus.setBackgroundResource(R.drawable.bg_cancelled_circle);
        tvPaymentMessage.setText("Đã hủy đơn hàng thành công!");
    } else if (isUnpaid) {
        // Failed: ✗ "Thanh toán thất bại!"
        tvPaymentStatus.setText("✗");
        tvPaymentStatus.setBackgroundResource(R.drawable.bg_failed_circle);
        tvPaymentMessage.setText("Thanh toán thất bại!");
    }
}
```

---

## 🔄 Complete Flow:

```
1. User ở CartActivity
   ↓
2. Checkout → POST /api/orders
   ↓
3. Get payment URL from header
   ↓
4. Navigate to PaymentWebViewActivity
   ↓
5. Save OrderResponse to SharedPreferences
   ↓
6. Load PayOS URL in WebView
   ↓
7. User scans QR & pays on PayOS
   ↓
8. PayOS redirects to deep link:
   mycoffeeapp://payos/return?status=PAID&orderCode=123&cancel=false
   ↓
9. Android opens PaymentWebViewActivity via deep link
   ↓
10. handleIntent extracts params:
    - status
    - orderCode
    - cancel
   ↓
11. Retrieve OrderResponse from SharedPreferences
   ↓
12. Navigate to OrderBillActivity
   ↓
13. Display payment result based on status:
    - PAID → ✓ Success
    - UNPAID → ✗ Failed
    - CANCELLED → ⊗ Cancelled
   ↓
14. Clear SharedPreferences
   ↓
15. Show order details + button "Quản lý đơn hàng"
```

---

## 📊 Status Values:

### **From Deep Link `status` parameter:**

| Status | Display | Icon | Color |
|--------|---------|------|-------|
| `PAID` | "Thanh toán thành công!" | ✓ | Green (#4CAF50) |
| `UNPAID` | "Thanh toán thất bại!" | ✗ | Red (#F44336) |
| `CANCELLED` | "Đá hủy đơn hàng thành công!" | ⊗ | Gray (#9E9E9E) |

---

## 🔐 Data Persistence:

**Why SharedPreferences?**
- Deep link opens app from external browser
- Activity may be destroyed/recreated
- Need to preserve OrderResponse data

**Storage:**
```java
SharedPreferences: "payment_prefs"
Key: "pending_order_response"
Value: OrderResponse JSON string
```

**Lifecycle:**
1. **Save**: When loading payment URL
2. **Retrieve**: When deep link received
3. **Clear**: After navigating to OrderBillActivity

---

## 🧪 Testing Deep Link:

### **Via ADB Command:**
```bash
adb shell am start -W -a android.intent.action.VIEW -d "mycoffeeapp://payos/return?status=PAID&orderCode=12345&cancel=false"
```

### **Test Cases:**

**1. Success:**
```bash
adb shell am start -W -a android.intent.action.VIEW -d "mycoffeeapp://payos/return?status=PAID&orderCode=12345&cancel=false"
```
Expected: ✓ Green "Thanh toán thành công!"

**2. Failed:**
```bash
adb shell am start -W -a android.intent.action.VIEW -d "mycoffeeapp://payos/return?status=UNPAID&orderCode=12345&cancel=false"
```
Expected: ✗ Red "Thanh toán thất bại!"

**3. Cancelled:**
```bash
adb shell am start -W -a android.intent.action.VIEW -d "mycoffeeapp://payos/return?status=CANCELLED&orderCode=12345&cancel=true"
```
Expected: ⊗ Gray "Đã hủy đơn hàng thành công!"

---

## 🚀 Backend Configuration:

**PayOS Deep Link Config:**
```csharp
var deepLink = $"mycoffeeapp://payos/return?" +
               $"status={payos.Status}&" +
               $"orderCode={payos.OrderCode}&" +
               $"cancel={payos.Cancel.ToString().ToLower()}";
```

**Status Values from Backend:**
- `PAID` - Payment successful
- `UNPAID` - Payment failed
- `CANCELLED` - User cancelled

---

## ✅ Features:

- [x] Deep link intent filter in manifest
- [x] Handle deep link in onCreate
- [x] Handle deep link in onNewIntent
- [x] Extract params from deep link URI
- [x] Save OrderResponse to SharedPreferences
- [x] Retrieve OrderResponse when deep link received
- [x] Navigate to OrderBillActivity with result
- [x] Display payment status based on params
- [x] Clear SharedPreferences after use
- [x] Support launchMode="singleTask"

---

## 🔍 Troubleshooting:

### **Deep Link Not Working:**
1. Check AndroidManifest.xml intent-filter
2. Verify scheme: `mycoffeeapp`
3. Test with ADB command
4. Check logcat for errors

### **Order Data Lost:**
1. Check SharedPreferences save/retrieve
2. Verify key: `"pending_order_response"`
3. Check if data cleared too early

### **Wrong Status Display:**
1. Log received status value
2. Verify case-insensitive comparison
3. Check status mapping in OrderBillActivity

---

**🎉 Deep link integration complete! App có thể nhận payment result từ PayOS và hiển thị đúng trạng thái!**

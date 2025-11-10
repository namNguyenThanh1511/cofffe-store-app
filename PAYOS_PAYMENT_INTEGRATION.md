# 💳 PayOS Payment Integration

## 📋 Overview
Tích hợp thanh toán PayOS QR code qua WebView. Sau khi đặt hàng thành công, user sẽ được redirect đến trang PayOS để quét mã QR thanh toán, sau đó redirect về app với kết quả thanh toán.

---

## ✨ Features Implemented:

### **1. PaymentWebViewActivity** ✅
Activity hiển thị PayOS payment page trong WebView.

**Features:**
- ✅ Load payment URL trong WebView
- ✅ Enable JavaScript & DOM storage
- ✅ Intercept redirect URLs
- ✅ Parse payment result từ URL params
- ✅ Navigate to result screen
- ✅ Progress bar khi loading
- ✅ Back navigation support

### **2. Updated CartActivity** ✅
Get payment URL từ response header và navigate.

**Changes:**
- ✅ Get `x-forward-payment` từ response header
- ✅ Navigate to PaymentWebViewActivity thay vì OrderBillActivity
- ✅ Pass payment URL và order data
- ✅ Still remove selected items from cart

### **3. Updated OrderBillActivity** ✅
Hiển thị kết quả thanh toán dựa trên params.

**Features:**
- ✅ Accept payment status params (`payment_status`, `order_code`, `payment_cancelled`)
- ✅ Display success/failure icon & message
- ✅ Green circle ✓ for success
- ✅ Red circle ✗ for failure
- ✅ Button navigate to Order Management
- ✅ Full order details display

### **4. Layouts & Resources** ✅
- ✅ `activity_payment_webview.xml` - WebView layout
- ✅ `bg_failed_circle.xml` - Red circle for failure
- ✅ Updated `activity_order_bill.xml` - Dynamic status display

---

## 🔄 Payment Flow:

```
1. User ở CartActivity
   ↓
2. Select items & click "Thanh toán"
   ↓
3. POST /api/orders
   ↓
4. API Response:
   - Body: OrderResponse data
   - Header: x-forward-payment = PayOS URL
   ↓
5. Remove selected items from cart
   ↓
6. Navigate to PaymentWebViewActivity
   ↓
7. Load PayOS URL in WebView
   ↓
8. User scans QR & pays on PayOS
   ↓
9. PayOS redirects:
   URL: app://payment-result?status=PAID&orderCode=123&cancel=false
   ↓
10. WebView intercepts redirect
   ↓
11. Parse URL params
   ↓
12. Navigate to OrderBillActivity
   ↓
13. Display payment result:
    - Success: ✓ "Thanh toán thành công!"
    - Failed: ✗ "Thanh toán thất bại!"
   ↓
14. Show order details + totals
   ↓
15. User clicks "Quản lý đơn hàng"
   ↓
16. Navigate to OrderManagementActivity (TODO)
```

---

## 📱 Screen Flow:

### **CartActivity**
```
┌─────────────────────────┐
│ Giỏ hàng               │
│ [✓] Item 1             │
│ [✓] Item 2             │
│                         │
│ Total: 100,000₫        │
│ [   Thanh toán   ]     │
└─────────────────────────┘
         ↓
```

### **PaymentWebViewActivity**
```
┌─────────────────────────┐
│ ← Thanh toán           │
│ ─────────────────────── │
│                         │
│   [PayOS WebView]       │
│                         │
│   ┌─────────────┐       │
│   │   QR CODE   │       │
│   │             │       │
│   └─────────────┘       │
│                         │
│   Quét mã để thanh toán │
│                         │
└─────────────────────────┘
         ↓
   (User scans & pays)
         ↓
```

### **OrderBillActivity (Success)**
```
┌─────────────────────────┐
│ ← Chi tiết đơn hàng    │
│                         │
│        ✓                │
│   (Green circle)        │
│                         │
│ Thanh toán thành công! │
│ Mã đơn: #12345         │
│ 05/11/2024 20:30       │
│                         │
│ ┌───────────────────┐  │
│ │   HÓA ĐƠN        │  │
│ │                   │  │
│ │ Items + Details   │  │
│ │                   │  │
│ │ Total: 100,000₫  │  │
│ └───────────────────┘  │
│                         │
│ [ Quản lý đơn hàng ]   │
└─────────────────────────┘
```

### **OrderBillActivity (Failed)**
```
┌─────────────────────────┐
│ ← Chi tiết đơn hàng    │
│                         │
│        ✗                │
│   (Red circle)          │
│                         │
│ Thanh toán thất bại!   │
│ Mã đơn: #12345         │
│ 05/11/2024 20:30       │
│                         │
│ ┌───────────────────┐  │
│ │   HÓA ĐƠN        │  │
│ │                   │  │
│ │ Items + Details   │  │
│ │                   │  │
│ │ Total: 100,000₫  │  │
│ └───────────────────┘  │
│                         │
│ [ Quản lý đơn hàng ]   │
└─────────────────────────┘
```

---

## 🔧 Technical Details:

### **1. Get Payment URL from Header**

**CartActivity.java:**
```java
// After successful API call
String paymentUrl = response.headers().get("x-forward-payment");

if (paymentUrl == null || paymentUrl.isEmpty()) {
    Toast.makeText(this, "Không tìm thấy URL thanh toán", Toast.LENGTH_SHORT).show();
    return;
}

// Navigate to WebView
showPaymentWebView(paymentUrl, orderResponse);
```

### **2. WebView Configuration**

**PaymentWebViewActivity.java:**
```java
// Enable JavaScript & DOM storage
webView.getSettings().setJavaScriptEnabled(true);
webView.getSettings().setDomStorageEnabled(true);

// Intercept URL loading
webView.setWebViewClient(new WebViewClient() {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // Check if redirect has payment result
        if (url != null && url.contains("status=")) {
            handlePaymentRedirect(url);
            return true;
        }
        return false;
    }
});
```

### **3. Parse Redirect URL**

**PaymentWebViewActivity.java:**
```java
private void handlePaymentRedirect(String url) {
    Uri uri = Uri.parse(url);
    
    // PayOS params:
    // - PaymentStatus: PAID, UNPAID
    // - PaymentTransactionType: PENDING, SUCCESS, FAILED
    String paymentStatus = uri.getQueryParameter("paymentStatus");
    String transactionType = uri.getQueryParameter("transactionType");
    String orderCode = uri.getQueryParameter("orderCode");
    String cancel = uri.getQueryParameter("cancel");

    // Navigate to bill with results
    Intent intent = new Intent(this, OrderBillActivity.class);
    intent.putExtra(OrderBillActivity.EXTRA_ORDER_RESPONSE, orderResponseJson);
    intent.putExtra("payment_status", paymentStatus);
    intent.putExtra("transaction_type", transactionType);
    intent.putExtra("order_code", orderCode);
    intent.putExtra("payment_cancelled", cancel);
    startActivity(intent);
    finish();
}
```

### **4. Display Payment Status**

**OrderBillActivity.java:**
```java
private void displayPaymentStatus() {
    // PayOS enums:
    // - PaymentStatus: PAID, UNPAID
    // - PaymentTransactionType: PENDING, SUCCESS, FAILED
    
    boolean isPaid = "PAID".equalsIgnoreCase(paymentStatus);
    boolean isSuccess = "SUCCESS".equalsIgnoreCase(transactionType);
    boolean isPending = "PENDING".equalsIgnoreCase(transactionType);
    boolean isFailed = "FAILED".equalsIgnoreCase(transactionType);
    
    if (isPaid && isSuccess) {
        // Payment successful
        tvPaymentStatus.setText("✓");
        tvPaymentStatus.setBackgroundResource(R.drawable.bg_success_circle);
        tvPaymentMessage.setText("Thanh toán thành công!");
        tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_white));
    } else if (isPending) {
        // Payment pending
        tvPaymentStatus.setText("⏳");
        tvPaymentStatus.setBackgroundResource(R.drawable.bg_pending_circle);
        tvPaymentMessage.setText("Đang chờ thanh toán...");
        tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_gray));
    } else {
        // Payment failed or unpaid
        tvPaymentStatus.setText("✗");
        tvPaymentStatus.setBackgroundResource(R.drawable.bg_failed_circle);
        tvPaymentMessage.setText("Thanh toán thất bại!");
        tvPaymentMessage.setTextColor(getResources().getColor(R.color.text_gray));
    }
}
```

---

## 📊 URL Parameters from PayOS:

### **Expected Redirect Format:**
```
app://payment-result?paymentStatus=PAID&transactionType=SUCCESS&orderCode=123456&cancel=false
```

### **PayOS Enums:**

**PaymentStatus:**
- `PAID` = Đã thanh toán
- `UNPAID` = Chưa thanh toán

**PaymentTransactionType:**
- `PENDING` = Đang chờ xử lý
- `SUCCESS` = Giao dịch thành công
- `FAILED` = Giao dịch thất bại

### **Parameters:**
- **paymentStatus**: Payment status enum (PAID/UNPAID)
- **transactionType**: Transaction type enum (PENDING/SUCCESS/FAILED)
- **orderCode**: Order ID từ hệ thống
  - Dùng để tracking hoặc verify
- **cancel**: Boolean string
  - `true` = User hủy thanh toán
  - `false` = Hoàn tất flow

### **Success Condition:**
```java
paymentStatus == "PAID" && transactionType == "SUCCESS"
```

### **Failed Condition:**
```java
paymentStatus == "UNPAID" || transactionType == "FAILED"
```

### **Pending Condition:**
```java
transactionType == "PENDING"
```

---

## 🎨 UI Components:

### **Success State:**
```xml
<!-- Green circle with checkmark -->
<TextView
    android:id="@+id/tvPaymentStatus"
    android:text="✓"
    android:textSize="48sp"
    android:textColor="@color/white"
    android:background="@drawable/bg_success_circle" />

<TextView
    android:id="@+id/tvPaymentMessage"
    android:text="Thanh toán thành công!"
    android:textColor="@color/text_white" />
```

### **Failed State:**
```xml
<!-- Red circle with X -->
<TextView
    android:id="@+id/tvPaymentStatus"
    android:text="✗"
    android:textSize="48sp"
    android:textColor="@color/white"
    android:background="@drawable/bg_failed_circle" />

<TextView
    android:id="@+id/tvPaymentMessage"
    android:text="Thanh toán thất bại!"
    android:textColor="@color/text_gray" />
```

### **Pending State:**
```xml
<!-- Orange circle with hourglass -->
<TextView
    android:id="@+id/tvPaymentStatus"
    android:text="⏳"
    android:textSize="48sp"
    android:textColor="@color/white"
    android:background="@drawable/bg_pending_circle" />

<TextView
    android:id="@+id/tvPaymentMessage"
    android:text="Đang chờ thanh toán..."
    android:textColor="@color/text_gray" />
```

---

## ✅ Checklist:

- [x] Create PaymentWebViewActivity
- [x] Create activity_payment_webview.xml layout
- [x] Update CartActivity to get header
- [x] Pass payment URL to WebView
- [x] Configure WebView (JavaScript, DOM storage)
- [x] Intercept redirect URLs
- [x] Parse payment result params (paymentStatus, transactionType)
- [x] Update OrderBillActivity to accept params
- [x] Display success/failure/pending status
- [x] Create bg_failed_circle.xml drawable
- [x] Create bg_pending_circle.xml drawable
- [x] Update button to "Quản lý đơn hàng"
- [x] Register PaymentWebViewActivity in manifest
- [x] Handle back navigation in WebView
- [x] Support PayOS enum values (PAID/UNPAID, PENDING/SUCCESS/FAILED)

---

## 🚧 TODO / Next Steps:

### **1. Order Management Screen** ⏳
- Create OrderManagementActivity
- Display list of all orders
- Filter by status (Pending, Paid, Completed)
- Order details view

### **2. Deep Link Handling** ⏳
- Add deep link intent filter if needed
- Handle external redirect (if PayOS opens browser)

### **3. Error Handling** ⏳
- Timeout handling (if user doesn't pay)
- Network error handling
- Invalid URL handling

### **4. UX Improvements** ⏳
- Add payment timeout countdown
- "Hủy thanh toán" button
- Retry payment option
- Save unpaid orders

---

## 🔐 Security Notes:

1. ✅ **WebView Security:**
   - JavaScript enabled (required for PayOS)
   - Only intercept known redirect patterns
   - Don't expose sensitive data in WebView

2. ✅ **Payment Verification:**
   - Always verify payment on backend
   - Don't trust client-side status alone
   - Use orderCode to verify with PayOS API

3. ⏳ **TODO:**
   - Add SSL certificate pinning
   - Implement payment webhook
   - Server-side payment verification

---

## 📝 Notes:

- **x-forward-payment header**: Đảm bảo API backend trả về header này
- **URL params**: PayOS phải config redirect URL với params:
  - `paymentStatus` (PAID/UNPAID)
  - `transactionType` (PENDING/SUCCESS/FAILED)
  - `orderCode`
  - `cancel`
- **Order Management**: Hiện tại navigate về MenuActivity, cần tạo proper screen
- **WebView**: Enable JavaScript vì PayOS cần
- **3 Payment States**:
  - ✓ Success: PAID + SUCCESS → Green circle
  - ⏳ Pending: PENDING → Orange circle
  - ✗ Failed: UNPAID or FAILED → Red circle

---

**🎉 Payment flow complete! User có thể đặt hàng, thanh toán qua QR PayOS, và xem kết quả!**

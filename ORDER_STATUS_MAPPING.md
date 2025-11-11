# 📊 Order Status Mapping

## 🔍 **Actual API Status Values**

Based on the actual API response from `/api/orders`:

```json
{
  "status": "PROCESSING"  // or "COMPLETED"
}
```

---

## ✅ **Status Mapping**

### **API Status → Display Text:**

| API Status | Vietnamese | Filter Tab |
|------------|-----------|-----------|
| `PROCESSING` | Đang xử lý | Chờ xử lý |
| `COMPLETED` | Hoàn thành | Hoàn thành |
| `CANCELLED` | Đã hủy | Đã hủy |

### **Additional Statuses (for future):**

| API Status | Vietnamese | Notes |
|------------|-----------|-------|
| `PENDING` | Chờ xử lý | Not in current API |
| `CONFIRMED` | Đã xác nhận | Not in current API |
| `PREPARING` | Đang chuẩn bị | Not in current API |
| `READY` | Sẵn sàng | Not in current API |
| `DELIVERING` | Đang giao | Not in current API |

---

## 📱 **Filter Implementation**

### **OrderHistoryActivity.java:**

```java
// Tất cả
loadOrders(null)

// Chờ xử lý (PROCESSING)
loadOrders("PROCESSING")

// Hoàn thành (COMPLETED)
loadOrders("COMPLETED")

// Đã hủy (CANCELLED)
loadOrders("CANCELLED")
```

---

## 🎨 **UI Display:**

### **Status Badge:**

```
┌──────────────────────────────┐
│ #80002    [Hoàn thành]      │  ← status="COMPLETED"
│ 11/11/2024 08:15            │
└──────────────────────────────┘

┌──────────────────────────────┐
│ #70005    [Đang xử lý]      │  ← status="PROCESSING"
│ 10/11/2024 12:55            │
└──────────────────────────────┘
```

---

## 📊 **Sample API Response:**

```json
{
  "data": [
    {
      "id": 80002,
      "orderDate": "2025-11-11T08:15:21.3391708",
      "totalAmount": 2000,
      "status": "COMPLETED",
      "customerId": "a561b487-16a7-46dc-936d-e755c2343b71",
      "orderItems": [...]
    },
    {
      "id": 70005,
      "orderDate": "2025-11-10T12:55:49.7852555",
      "totalAmount": 25000,
      "status": "PROCESSING",
      "customerId": "a561b487-16a7-46dc-936d-e755c2343b71",
      "orderItems": [...]
    }
  ],
  "isSuccess": true,
  "message": "Thao tác thành công"
}
```

---

## 🔧 **Implementation:**

### **1. OrderHistoryAdapter.java**

```java
private String getStatusText(String status) {
    if (status == null) return "Không xác định";
    
    switch (status.toUpperCase()) {
        case "PROCESSING":
            return "Đang xử lý";
        case "COMPLETED":
            return "Hoàn thành";
        case "CANCELLED":
            return "Đã hủy";
        // ... other statuses for future
        default:
            return status;
    }
}
```

### **2. OrderHistoryActivity.java**

```java
// Filter: Chờ xử lý
btnFilterPending.setOnClickListener(v -> {
    selectFilter(btnFilterPending, "PENDING");
    loadOrders("PROCESSING");  // API uses PROCESSING
});
```

---

## ✅ **Verified API Statuses:**

From actual API response:
- ✅ **PROCESSING** - Orders being processed
- ✅ **COMPLETED** - Orders completed successfully
- ⚠️ **CANCELLED** - Not in sample data but expected

---

## 🎯 **Filter Logic:**

```
Filter Tab         API Query Parameter
────────────────   ───────────────────
Tất cả            null (show all)
Chờ xử lý         PROCESSING
Hoàn thành        COMPLETED
Đã hủy            CANCELLED
```

---

## 📝 **Notes:**

1. **Current API only returns 2 statuses:**
   - `PROCESSING` - Active orders
   - `COMPLETED` - Finished orders

2. **Future statuses may include:**
   - `PENDING` - Awaiting confirmation
   - `CONFIRMED` - Order confirmed
   - `PREPARING` - Being prepared
   - `READY` - Ready for pickup/delivery
   - `DELIVERING` - Out for delivery
   - `CANCELLED` - Order cancelled

3. **Filter "Chờ xử lý" maps to PROCESSING**
   - User-friendly name for "orders being processed"

---

**Perfect! Status mapping matches actual API! ✅📊🎯**

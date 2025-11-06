# ✅ Selective Checkout - Chọn sản phẩm để thanh toán

## 📋 Overview
User có thể tick chọn từng sản phẩm trong giỏ hàng và chỉ thanh toán những sản phẩm đã chọn.

---

## ✨ Features Implemented:

### **1. Checkbox cho mỗi CartItem** ✅
- ✅ Thêm `isSelected` field vào CartItem model
- ✅ Thêm CheckBox vào `item_cart.xml`
- ✅ CheckBox màu accent (brown)
- ✅ State persist khi scroll RecyclerView

### **2. Cart Summary** ✅
- ✅ **Tính tổng chỉ selected items**
- ✅ Subtotal = Tổng của items đã tick
- ✅ Total = Subtotal + Delivery fee
- ✅ Real-time update khi tick/untick

### **3. Checkout Logic** ✅
- ✅ **Chỉ POST selected items** lên API
- ✅ Validation: Phải chọn ít nhất 1 item
- ✅ Toast warning: "Vui lòng chọn sản phẩm để thanh toán"
- ✅ Log số lượng selected items

### **4. After Checkout** ✅
- ✅ **Xóa chỉ selected items** khỏi cart
- ✅ **Giữ lại unselected items** trong cart
- ✅ Smart notification:
  - Còn items → Stage 2 (50%)
  - Hết items → Stage 3 (100%)

---

## 🎯 User Flow:

```
1. Mở Cart
   ↓
2. Thấy danh sách items với checkbox
   ↓
3. Tick chọn items muốn thanh toán
   ↓
4. Xem tổng tiền (chỉ của selected items)
   ↓
5. Click "Thanh toán"
   ↓
6. POST chỉ selected items lên API
   ↓
7. Success:
   - Xóa selected items khỏi cart
   - Giữ lại unselected items
   - Update notification
   - Show success dialog
```

---

## 📊 Example Scenarios:

### **Scenario 1: Checkout một phần**
```
Cart có:
☑ Cà phê sữa - 35,000₫
☐ Cà phê đen - 30,000₫
☑ Trà sữa - 40,000₫

Subtotal: 75,000₫ (chỉ 2 items đã tick)
Total: 75,000₫

Sau checkout thành công:
- Xóa: Cà phê sữa, Trà sữa
- Giữ lại: Cà phê đen
- Notification: Stage 2 (1 item còn lại)
```

### **Scenario 2: Checkout tất cả**
```
Cart có:
☑ Cà phê sữa - 35,000₫
☑ Cà phê đen - 30,000₫
☑ Trà sữa - 40,000₫

Subtotal: 105,000₫ (tất cả 3 items)
Total: 105,000₫

Sau checkout thành công:
- Xóa: Tất cả 3 items
- Cart: Trống
- Notification: Stage 3 → Stage 1 (ẩn sau 3s)
```

### **Scenario 3: Không chọn gì**
```
Cart có:
☐ Cà phê sữa - 35,000₫
☐ Cà phê đen - 30,000₫

Subtotal: 0₫
Total: 0₫

Click "Thanh toán":
→ Toast: "Vui lòng chọn sản phẩm để thanh toán"
→ Không gọi API
```

---

## 🔧 Technical Implementation:

### **1. CartItem Model:**
```java
private boolean isSelected; // For checkout selection

public boolean isSelected() {
    return isSelected;
}

public void setSelected(boolean selected) {
    isSelected = selected;
}
```

### **2. item_cart.xml:**
```xml
<CheckBox
    android:id="@+id/cbSelectItem"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginEnd="8dp"
    android:buttonTint="@color/brown_button_selected" />
```

### **3. CartAdapter:**
```java
public interface OnCartItemListener {
    void onQuantityChanged(CartItem item, int newQuantity);
    void onItemRemoved(CartItem item);
    void onItemSelectionChanged(CartItem item, boolean isSelected); // NEW
}

// In ViewHolder.bind()
cbSelectItem.setOnCheckedChangeListener(null); // Prevent trigger
cbSelectItem.setChecked(item.isSelected());
cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
    item.setSelected(isChecked);
    listener.onItemSelectionChanged(item, isChecked);
});
```

### **4. CartActivity - Summary:**
```java
private void updateCartSummary() {
    // Calculate totals for SELECTED items only
    double subtotal = 0;
    int selectedCount = 0;
    
    for (CartItem item : cartItems) {
        if (item.isSelected()) {
            subtotal += item.getTotal();
            selectedCount++;
        }
    }
    
    tvSubtotal.setText(CurrencyUtils.formatPrice(subtotal));
}
```

### **5. CartActivity - Checkout:**
```java
private void checkout() {
    // Create OrderItems from SELECTED CartItems only
    List<OrderItem> orderItems = new ArrayList<>();
    final List<Integer> selectedPositions = new ArrayList<>();
    
    for (int i = 0; i < cartItems.size(); i++) {
        CartItem cartItem = cartItems.get(i);
        if (cartItem.isSelected()) {
            orderItems.add(createOrderItem(cartItem));
            selectedPositions.add(i);
        }
    }
    
    // POST to API
    apiService.createOrder(bearerToken, orderRequest);
}
```

### **6. After Success:**
```java
// Remove only SELECTED items
Collections.sort(selectedPositions, Collections.reverseOrder());
for (int position : selectedPositions) {
    CartManager.getInstance(this).removeItem(position);
}

// Update notification based on remaining items
int remainingCount = CartManager.getInstance(this).getCartItemCount();
if (remainingCount > 0) {
    NotificationHelper.updateNotificationStage(this, 2, remainingCount);
} else {
    NotificationHelper.updateNotificationStage(this, 3, 0);
}
```

---

## 🎨 UI/UX Details:

### **CheckBox:**
- **Position**: Bên trái image sản phẩm
- **Color**: Brown accent (matching app theme)
- **Size**: Standard checkbox size
- **Touch target**: Đủ lớn để dễ tap

### **Summary Display:**
```
┌─────────────────────────────┐
│ Subtotal:    [Selected only]│
│ Delivery:    0₫             │
│ ─────────────────────────── │
│ Total:       [Selected only]│
└─────────────────────────────┘
```

### **Checkout Button:**
- **Enabled**: Always (có validation internal)
- **Text**: "Thanh toán"
- **Behavior**: 
  - No selection → Toast warning
  - Has selection → Process checkout

---

## 📝 Edge Cases Handled:

1. ✅ **Không chọn item nào** → Toast warning
2. ✅ **Chọn tất cả** → Checkout hết, cart trống
3. ✅ **Chọn một phần** → Giữ lại unselected items
4. ✅ **Scroll RecyclerView** → Checkbox state giữ nguyên
5. ✅ **Update quantity** → Selection state không đổi
6. ✅ **Remove item** → Xóa bất kể selected hay không
7. ✅ **Clear cart** → Xóa tất cả bất kể selection

---

## 🚀 Benefits:

1. ✅ **Flexible checkout** - Chọn thanh toán từng phần
2. ✅ **Save for later** - Giữ lại items không mua ngay
3. ✅ **Better UX** - Control chi tiết đơn hàng
4. ✅ **Smart cart** - Không force thanh toán tất cả
5. ✅ **Clear pricing** - Thấy rõ tổng tiền của selected items

---

## ✅ Checklist:

- [x] Thêm isSelected field vào CartItem
- [x] Thêm CheckBox vào item_cart.xml
- [x] Update CartAdapter với checkbox handling
- [x] Tính tổng chỉ selected items
- [x] Validate có ít nhất 1 item selected
- [x] Checkout chỉ selected items
- [x] Xóa chỉ selected items sau success
- [x] Giữ unselected items trong cart
- [x] Smart notification based on remaining items
- [x] Toast warning khi không chọn item
- [x] Persist selection state trong local storage

---

**🎉 Feature Complete! Ready to test selective checkout!**

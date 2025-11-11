# 🐛 Addon Price Calculation Debugging

## 📋 Issue:
Giá không cập nhật đúng khi chọn topping/addon:
- Base price: 25,000₫
- Chọn Extra Shot (+5,000₫)
- **Expected:** 30,000₫
- **Actual:** Vẫn 25,000₫

---

## ✅ Fixed:

### **1. Show Total Price** ✅
```xml
<!-- activity_product_detail.xml -->
<TextView
    android:id="@+id/tvTotalPrice"
    android:visibility="visible" />  <!-- Was: gone -->
```

### **2. Added Debug Logging** ✅

**When addon clicked:**
```java
addonLayout.setOnClickListener(v -> {
    addon.setSelected(!addon.isSelected());
    checkbox.setSelected(addon.isSelected());
    Log.d(TAG, "Addon clicked: " + addon.getName() + 
               " -> selected=" + addon.isSelected() + 
               ", price=" + addon.getPrice());
});
```

**When dialog confirmed:**
```java
btnDialogConfirm.setOnClickListener(v -> {
    Log.d(TAG, "=== CONFIRM DIALOG ===");
    Log.d(TAG, "Quantity: " + quantity);
    
    for (Addon addon : availableAddons) {
        Log.d(TAG, "Checking addon: " + addon.getName() + 
                   " -> selected=" + addon.isSelected() + 
                   ", price=" + addon.getPrice());
    }
    
    updateTotalPrice();
});
```

**In updateTotalPrice():**
```java
private void updateTotalPrice() {
    Log.d(TAG, "=== UPDATE TOTAL PRICE ===");
    Log.d(TAG, "Base price: " + productPrice);
    Log.d(TAG, "Quantity: " + quantity);
    Log.d(TAG, "Checking " + availableAddons.size() + " addons:");
    
    for (Addon addon : availableAddons) {
        Log.d(TAG, "  - " + addon.getName() + 
                   ": price=" + addon.getPrice() + 
                   ", selected=" + addon.isSelected());
        if (addon.isSelected()) {
            addonsTotal += addon.getPrice();
            Log.d(TAG, "    ✓ Added to total!");
        }
    }
    
    Log.d(TAG, "=== RESULT ===");
    Log.d(TAG, "Base total: " + (productPrice * quantity));
    Log.d(TAG, "Addons total: " + (addonsTotal * quantity));
    Log.d(TAG, "Final total: " + total);
}
```

---

## 🔍 How to Debug:

### **Step 1: Open Logcat**
1. In Android Studio: **View → Tool Windows → Logcat**
2. Filter by: `ProductDetailActivity`

### **Step 2: Test Flow**
1. Open product detail
2. Click "Tùy chỉnh đơn hàng"
3. Click addon (e.g., Extra Shot)
4. Click "Xác nhận"
5. Check logs

### **Step 3: Expected Logs**

**When clicking addon:**
```
ProductDetailActivity: Addon clicked: Extra Shot -> selected=true, price=5000.0
```

**When confirming:**
```
ProductDetailActivity: === CONFIRM DIALOG ===
ProductDetailActivity: Quantity: 1
ProductDetailActivity: Checking addon: Extra Shot -> selected=true, price=5000.0
ProductDetailActivity: Total selected addons: 1
```

**When updating price:**
```
ProductDetailActivity: === UPDATE TOTAL PRICE ===
ProductDetailActivity: Base price: 25000.0
ProductDetailActivity: Quantity: 1
ProductDetailActivity: Checking 8 addons:
ProductDetailActivity:   - Whipped Cream: price=10000.0, selected=false
ProductDetailActivity:   - Extra Shot: price=5000.0, selected=true
ProductDetailActivity:     ✓ Added to total!
ProductDetailActivity: === RESULT ===
ProductDetailActivity: Base total: 25000.0
ProductDetailActivity: Addons total: 5000.0
ProductDetailActivity: Final total: 30000.0
ProductDetailActivity: ================
```

---

## 🐛 Possible Issues:

### **Issue 1: Addon price = 0**
**Symptom:**
```
ProductDetailActivity: Extra Shot: price=0.0, selected=true
```

**Cause:** API không trả về giá addon đúng

**Solution:** Check backend API `/api/addons`

---

### **Issue 2: Addon selected = false**
**Symptom:**
```
ProductDetailActivity: Extra Shot: price=5000.0, selected=false
```

**Cause:** 
- Addon state bị reset
- Click không hoạt động

**Solution:** 
- Check `addon.isSelected()` trong Addon model
- Verify click listener hoạt động

---

### **Issue 3: Price not updated in UI**
**Symptom:**
- Logs show correct total (30,000)
- UI still shows 25,000

**Cause:** 
- `tvTotalPrice` bị hidden
- Layout cache

**Solution:**
- ✅ Already fixed: `android:visibility="visible"`
- Rebuild app: **Build → Clean Project → Rebuild Project**

---

### **Issue 4: Empty availableAddons**
**Symptom:**
```
ProductDetailActivity: Checking 0 addons:
```

**Cause:** API load addons failed

**Solution:** Check API response

---

## 📊 Test Case:

### **Test 1: Single Addon**
1. Product: Latte (25,000₫)
2. Select: Extra Shot (+5,000₫)
3. Quantity: 1
4. **Expected total:** 30,000₫

### **Test 2: Multiple Addons**
1. Product: Latte (25,000₫)
2. Select: 
   - Extra Shot (+5,000₫)
   - Whipped Cream (+10,000₫)
3. Quantity: 1
4. **Expected total:** 40,000₫

### **Test 3: With Quantity**
1. Product: Latte (25,000₫)
2. Select: Extra Shot (+5,000₫)
3. Quantity: 2
4. **Expected total:** 60,000₫
   - Base: 25,000 × 2 = 50,000
   - Addons: 5,000 × 2 = 10,000
   - Total: 60,000

---

## 🔧 Calculation Formula:

```java
Total = (Base Price × Quantity) + (Sum of Selected Addons × Quantity)
```

**Example:**
```
Base Price: 25,000₫
Addons: Extra Shot (5,000₫) + Vanilla Syrup (5,000₫)
Quantity: 2

Total = (25,000 × 2) + ((5,000 + 5,000) × 2)
      = 50,000 + 20,000
      = 70,000₫
```

---

## ✅ Verification Steps:

1. **Build & Install App**
   ```bash
   Build → Clean Project
   Build → Rebuild Project
   Run app
   ```

2. **Open Product Detail**
   - Select any product
   - Check base price displays correctly

3. **Add Addon**
   - Click "Tùy chỉnh đơn hàng"
   - Select addon (checkbox should fill)
   - Click "Xác nhận"

4. **Check Price**
   - Price should update immediately
   - Format: "30,000₫" (not "30000")

5. **Check Logcat**
   - See detailed logs
   - Verify calculations

---

## 🎯 Checklist:

- [x] `tvTotalPrice` visible
- [x] Logging added to addon click
- [x] Logging added to confirm dialog
- [x] Logging added to updateTotalPrice
- [ ] Test with 1 addon
- [ ] Test with multiple addons
- [ ] Test with quantity > 1
- [ ] Verify Logcat shows correct values
- [ ] Verify UI shows correct price

---

## 💡 Quick Fix:

**If price still not updating:**

1. **Uninstall app completely**
2. **Clean build:**
   ```bash
   Build → Clean Project
   Build → Rebuild Project
   ```
3. **Install fresh:**
   ```bash
   Run → Run 'app'
   ```
4. **Check Logcat for errors**

---

**Debug logs will tell us exactly what's happening! 🐛🔍📊**

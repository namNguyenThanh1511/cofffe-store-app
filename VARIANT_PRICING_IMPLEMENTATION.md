# 💰 Variant Pricing Implementation

## 📋 Overview
Hiển thị giá variant đúng trong toàn bộ app flow:
1. **Menu (Trang chủ):** Show giá thấp nhất (minPrice)
2. **Product Detail:** Default = giá thấp nhất, show giá từng size trong dialog
3. **Price Updates:** Giá thay đổi theo size đã chọn

---

## ✨ Implementation:

### **1. MenuActivity.java** ✅

**Load minPrice from API:**
```java
CoffeeItem coffeeItem = new CoffeeItem(
    product.getId(),
    product.getName(),
    product.getDescription(),
    product.getMinPrice(),  // ← Lowest price from variants
    null,
    product.getImageUrl(),
    category
);
```

**Pass to Detail:**
```java
intent.putExtra("PRODUCT_PRICE", item.getPrice());  // minPrice
```

---

### **2. ProductDetailActivity.java** ✅

**Default Size Selection:**
```java
// selectedSize = null (not hardcoded "M")

// Find variant with minPrice
if (variantMap != null && !variantMap.isEmpty()) {
    // Try to match minPrice from menu
    for (Map.Entry<String, ProductVariant> entry : variantMap.entrySet()) {
        if (Math.abs(entry.getValue().getBasePrice() - productPrice) < 0.01) {
            selectedSize = entry.getKey();  // S, M, or L
            break;
        }
    }
    
    // Fallback: find cheapest variant
    if (selectedSize == null) {
        double minPrice = Double.MAX_VALUE;
        for (Map.Entry<String, ProductVariant> entry : variantMap.entrySet()) {
            if (entry.getValue().getBasePrice() < minPrice) {
                minPrice = entry.getValue().getBasePrice();
                selectedSize = entry.getKey();
            }
        }
    }
}
```

**Result:** Default size = size có giá thấp nhất (matching menu price)

---

### **3. Dialog Layout** ✅

**Added Price TextViews:**
```xml
<!-- dialog_customize_order.xml -->

<!-- Small Size -->
<TextView
    android:layout_width="0dp"
    android:layout_weight="1"
    android:text="Nhỏ" />
<TextView
    android:id="@+id/tvPriceSmall"
    android:textColor="@color/coffee_accent"
    android:textStyle="bold" />

<!-- Medium Size -->
<TextView
    android:layout_width="0dp"
    android:layout_weight="1"
    android:text="Vừa" />
<TextView
    android:id="@+id/tvPriceMedium"
    android:textColor="@color/coffee_accent"
    android:textStyle="bold" />

<!-- Large Size -->
<TextView
    android:layout_width="0dp"
    android:layout_weight="1"
    android:text="Lớn" />
<TextView
    android:id="@+id/tvPriceLarge"
    android:textColor="@color/coffee_accent"
    android:textStyle="bold" />
```

---

### **4. Display Prices in Dialog** ✅

```java
TextView tvPriceSmall = dialog.findViewById(R.id.tvPriceSmall);
TextView tvPriceMedium = dialog.findViewById(R.id.tvPriceMedium);
TextView tvPriceLarge = dialog.findViewById(R.id.tvPriceLarge);

// Display prices for each size from variants
if (variantMap != null) {
    if (variantMap.containsKey("S")) {
        tvPriceSmall.setText(CurrencyUtils.formatPrice(variantMap.get("S").getBasePrice()));
    } else {
        btnDialogSizeSmall.setVisibility(View.GONE);  // Hide if not available
    }
    
    if (variantMap.containsKey("M")) {
        tvPriceMedium.setText(CurrencyUtils.formatPrice(variantMap.get("M").getBasePrice()));
    } else {
        btnDialogSizeMedium.setVisibility(View.GONE);
    }
    
    if (variantMap.containsKey("L")) {
        tvPriceLarge.setText(CurrencyUtils.formatPrice(variantMap.get("L").getBasePrice()));
    } else {
        btnDialogSizeLarge.setVisibility(View.GONE);
    }
}
```

---

### **5. Update Price on Size Change** ✅ (Already implemented)

```java
btnDialogSizeSmall.setOnClickListener(v -> {
    selectedSize = "S";
    // Update checkboxes
    checkSizeSmall.setSelected(true);
    checkSizeMedium.setSelected(false);
    checkSizeLarge.setSelected(false);
    
    // Update price
    if (variantMap != null && variantMap.containsKey("S")) {
        productPrice = variantMap.get("S").getBasePrice();
        tvPrice.setText(CurrencyUtils.formatPrice(productPrice));
        updateTotalPrice();
    }
});
```

---

## 🔄 Complete Flow:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. API → Product with Variants                              │
│    - Small: 35,000₫                                         │
│    - Medium: 45,000₫                                        │
│    - Large: 55,000₫                                         │
│    → minPrice = 35,000₫                                     │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. MenuActivity (Home Screen)                               │
│    Card displays: "35,000₫"  ← minPrice                    │
│    (Cheapest variant)                                       │
└─────────────────────────────────────────────────────────────┘
                         ↓ (User clicks)
┌─────────────────────────────────────────────────────────────┐
│ 3. ProductDetailActivity                                    │
│    - Receives minPrice: 35,000₫                            │
│    - Finds variant with matching price → Size S            │
│    - selectedSize = "S" (default)                          │
│    - Displays: "35,000₫"  ← Same as menu!                 │
└─────────────────────────────────────────────────────────────┘
                         ↓ (User opens customize dialog)
┌─────────────────────────────────────────────────────────────┐
│ 4. Customize Dialog                                         │
│    ☑ Nhỏ      35,000₫  ← Default selected                 │
│    ☐ Vừa      45,000₫                                      │
│    ☐ Lớn      55,000₫                                      │
│                                                             │
│    User can see all prices and choose!                     │
└─────────────────────────────────────────────────────────────┘
                         ↓ (User selects "Large")
┌─────────────────────────────────────────────────────────────┐
│ 5. Price Updates                                            │
│    - selectedSize = "L"                                     │
│    - productPrice = 55,000₫                                │
│    - Display updates: "55,000₫"                            │
│    - Total price recalculated                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Features:

### ✅ **1. Consistent Pricing**
- Menu shows minPrice
- Detail defaults to minPrice
- No confusion between pages

### ✅ **2. Transparent Pricing**
- All variant prices visible in dialog
- User knows exact price before selecting
- No surprises

### ✅ **3. Smart Default**
- Auto-selects cheapest variant
- Matches menu price
- User can change if needed

### ✅ **4. Dynamic Updates**
- Price changes when size changes
- Total price updates automatically
- Real-time feedback

### ✅ **5. Unavailable Variants**
- Hides sizes not in stock
- Only shows available options
- Clean UI

---

## 📊 Example Data Flow:

**Product: "Cà Phê Đen Đá"**
```json
{
  "id": "prod-123",
  "name": "Cà Phê Đen Đá",
  "minPrice": 25000,
  "variants": [
    { "size": "S", "basePrice": 25000 },
    { "size": "M", "basePrice": 30000 },
    { "size": "L", "basePrice": 35000 }
  ]
}
```

**Display:**

| Screen | Size | Price | Notes |
|--------|------|-------|-------|
| Menu | - | 25,000₫ | minPrice shown |
| Detail (initial) | S | 25,000₫ | Default = cheapest |
| Dialog - Small | S | 25,000₫ | ☑ Selected |
| Dialog - Medium | M | 30,000₫ | ☐ Available |
| Dialog - Large | L | 35,000₫ | ☐ Available |
| Detail (after select M) | M | 30,000₫ | Updated |

---

## 🔍 Debugging:

**Added logs to verify:**
```java
Log.d(TAG, "Finding default variant. Product price (minPrice): " + productPrice);
Log.d(TAG, "  Variant " + entry.getKey() + " price: " + entry.getValue().getBasePrice());
Log.d(TAG, "  ✓ Matched! Default size: " + selectedSize);
Log.d(TAG, "Final selected size: " + selectedSize + ", price: " + productPrice);
```

**Check Logcat:**
```
ProductDetailActivity: Finding default variant. Product price (minPrice): 25000.0
ProductDetailActivity:   Variant S price: 25000.0
ProductDetailActivity:   ✓ Matched! Default size: S
ProductDetailActivity: Final selected size: S, price: 25000.0
```

---

## ✅ Testing Checklist:

- [ ] Menu shows minPrice (lowest variant)
- [ ] Click product → Detail shows same price
- [ ] Default size = size with minPrice
- [ ] Open dialog → all prices visible
- [ ] Default size checked in dialog
- [ ] Select different size → price updates
- [ ] Total price recalculates
- [ ] Add to cart with correct price
- [ ] Unavailable sizes hidden

---

## 🎉 Result:

**Before:**
- ❌ Menu shows minPrice (35k)
- ❌ Detail defaults to M (45k)
- ❌ User confused: "Price changed?"

**After:**
- ✅ Menu shows minPrice (35k)
- ✅ Detail defaults to S (35k)
- ✅ Dialog shows all prices clearly
- ✅ User can choose with full transparency
- ✅ Consistent experience across app

---

**Perfect pricing flow! User-friendly & transparent! 💰✨🎯**

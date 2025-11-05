# 📱 Notification Stages - Hệ thống thông báo 3 giai đoạn

## 🎯 Tổng quan
Hệ thống notification được chia thành 3 giai đoạn thanh toán với progress bar trực quan.

---

## 📊 3 Giai đoạn

### 🏁 Giai đoạn 1: Chưa có đơn hàng (0%)
**Status:** Không có sản phẩm trong giỏ
**Action:** Không hiển thị notification
**Progress:** 0%
**Icon:** Không có

```java
NotificationHelper.updateNotificationStage(context, 1, 0);
```

---

### 🏃 Giai đoạn 2: Đã có đơn nhưng chưa thanh toán (50%)
**Status:** Có sản phẩm trong giỏ, đang chờ thanh toán
**Action:** Hiển thị notification persistent
**Progress:** 50%
**Icon:** 🏃 (người đang chạy)

**Nội dung notification:**
- **Title:** 🛒 Giỏ hàng của bạn
- **Text:** [X] sản phẩm đang chờ thanh toán
- **SubText:** 🏃 Đang chờ thanh toán
- **Expanded:**
  ```
  🏃 Giai đoạn 2/3: Đã có đơn hàng
  
  ✓ Bước 1: Đã thêm [X] sản phẩm vào giỏ
  ▶ Bước 2: Đang chờ thanh toán
  ○ Bước 3: Hoàn tất đơn hàng
  
  Nhấn để tiếp tục thanh toán!
  ```

**Tính năng:**
- ✅ Persistent notification (không thể swipe dismiss)
- ✅ Badge hiển thị số lượng sản phẩm
- ✅ Progress bar 50%
- ✅ Click để mở CartActivity

```java
NotificationHelper.updateNotificationStage(context, 2, itemCount);
```

---

### 🏁 Giai đoạn 3: Đã thanh toán thành công (100%)
**Status:** Đơn hàng đã được thanh toán
**Action:** Hiển thị notification thành công, tự động ẩn sau 3 giây
**Progress:** 100%
**Icon:** 🏁 (cờ đích)

**Nội dung notification:**
- **Title:** ✅ Thanh toán thành công!
- **Text:** Đơn hàng của bạn đã được xác nhận
- **SubText:** 🏁 Hoàn thành
- **Expanded:**
  ```
  🏁 Giai đoạn 3/3: Hoàn tất
  
  ✓ Bước 1: Đã thêm sản phẩm vào giỏ
  ✓ Bước 2: Đã thanh toán
  ✓ Bước 3: Hoàn tất đơn hàng
  
  Cảm ơn bạn đã đặt hàng!
  ```

**Tính năng:**
- ✅ Can dismiss notification
- ✅ Progress bar 100%
- ✅ High priority
- ✅ Auto dismiss sau 3 giây

```java
NotificationHelper.updateNotificationStage(context, 3, 0);
```

---

## 🔄 Flow thanh toán

```
[Giai đoạn 1]     →     [Giai đoạn 2]     →     [Giai đoạn 3]
Chưa có đơn            Chờ thanh toán          Hoàn thành
    0%                      50%                    100%
    ○                       🏃                      🏁
 No notification       Show notification      Success notification
                      (Persistent)            (Auto dismiss 3s)
```

---

## 📝 Usage Examples

### Khi thêm sản phẩm vào giỏ:
```java
// Chuyển từ Stage 1 → Stage 2
int itemCount = 3;
NotificationHelper.updateNotificationStage(this, 2, itemCount);
```

### Khi xóa hết sản phẩm:
```java
// Chuyển từ Stage 2 → Stage 1
NotificationHelper.updateNotificationStage(this, 1, 0);
```

### Khi thanh toán thành công:
```java
// Chuyển từ Stage 2 → Stage 3
NotificationHelper.updateNotificationStage(this, 3, 0);
// Notification sẽ tự động ẩn sau 3 giây
```

---

## 🎨 Visual Design

### Progress Bar
- **Stage 1:** 0% - Không hiển thị
- **Stage 2:** 50% - Vàng/Cam (đang xử lý)
- **Stage 3:** 100% - Xanh (thành công)

### Icons
- **Stage 1:** Không có icon
- **Stage 2:** 🏃 Runner (đang chạy về đích)
- **Stage 3:** 🏁 Finish flag (đã về đích)

---

## 📱 Android Requirements
- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 33+ (Android 13+)
- **Permissions:** `POST_NOTIFICATIONS` (Android 13+)

---

## 🔔 Notification Channel
- **Channel ID:** `coffee_cart_channel`
- **Channel Name:** Giỏ hàng
- **Importance:** Default
- **Show badge:** Yes

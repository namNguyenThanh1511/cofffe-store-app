package namnt.vn.coffestore.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import namnt.vn.coffestore.R;
import namnt.vn.coffestore.ui.activities.CartActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "coffee_cart_channel";
    private static final String CHANNEL_NAME = "Giỏ hàng";
    private static final String CHANNEL_DESCRIPTION = "Thông báo về đơn hàng trong giỏ";
    private static final int NOTIFICATION_ID = 1001;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW  // Changed to LOW to disable sound
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.setShowBadge(true);
            channel.setSound(null, null);  // Disable notification sound
            channel.enableVibration(false);  // Disable vibration

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showCartNotification(Context context, int itemCount) {
        // Create intent to open CartActivity when notification is clicked
        Intent intent = new Intent(context, CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Determine payment stage (3 stages)
        // Stage 1: No items (0% - hidden notification)
        // Stage 2: Has unpaid items (50% - show notification)
        // Stage 3: All paid (100% - show success, then hide)
        
        int maxProgress = 100;
        int currentProgress = 50; // Stage 2: Has unpaid items
        String stageIcon = "🏃";
        String stageText = "Đang chờ thanh toán";
        String title = "🛒 Giỏ hàng của bạn";
        String content = itemCount + " sản phẩm đang chờ thanh toán";
        String bigText = "🏃 Giai đoạn 2/3: Đã có đơn hàng\n\n" +
                        "✓ Bước 1: Đã thêm " + itemCount + " sản phẩm vào giỏ\n" +
                        "▶ Bước 2: Đang chờ thanh toán\n" +
                        "○ Bước 3: Hoàn tất đơn hàng\n\n" +
                        "Nhấn để tiếp tục thanh toán!";
        
        // Build notification with progress bar
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cart)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(stageIcon + " " + stageText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true) // Make it persistent
            .setNumber(itemCount)
            .setProgress(maxProgress, currentProgress, false) // 50% progress
            .setSound(null)  // No sound
            .setVibrate(null)  // No vibration
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(bigText)
                .setBigContentTitle(title)
                .setSummaryText(stageIcon + " Tiến độ: " + currentProgress + "%"));

        // Show notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
    
    public static void showPaymentSuccessNotification(Context context) {
        // Show completion notification (Stage 3: 100%)
        Intent intent = new Intent(context, CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int maxProgress = 100;
        int currentProgress = 100; // Stage 3: Payment complete
        String stageIcon = "🏁";
        String stageText = "Hoàn thành";
        String title = "✅ Thanh toán thành công!";
        String content = "Đơn hàng của bạn đã được xác nhận";
        String bigText = "🏁 Giai đoạn 3/3: Hoàn tất\n\n" +
                        "✓ Bước 1: Đã thêm sản phẩm vào giỏ\n" +
                        "✓ Bước 2: Đã thanh toán\n" +
                        "✓ Bước 3: Hoàn tất đơn hàng\n\n" +
                        "Cảm ơn bạn đã đặt hàng!";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cart)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(stageIcon + " " + stageText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Can be dismissed
            .setOngoing(false)
            .setProgress(maxProgress, currentProgress, false) // 100% progress
            .setSound(null)  // No sound
            .setVibrate(null)  // No vibration
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(bigText)
                .setBigContentTitle(title)
                .setSummaryText(stageIcon + " Tiến độ: " + currentProgress + "%"));

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Update notification based on payment stage
     * @param context Application context
     * @param stage Payment stage (1: No orders, 2: Unpaid orders, 3: Paid)
     * @param itemCount Number of items in cart
     */
    public static void updateNotificationStage(Context context, int stage, int itemCount) {
        switch (stage) {
            case 1:
                // Stage 1: No orders - Cancel notification
                cancelCartNotification(context);
                break;
            case 2:
                // Stage 2: Has unpaid orders - Show cart notification
                showCartNotification(context, itemCount);
                break;
            case 3:
                // Stage 3: Payment completed - Show success notification
                showPaymentSuccessNotification(context);
                // Auto dismiss after 3 seconds
                new android.os.Handler().postDelayed(() -> {
                    cancelCartNotification(context);
                }, 3000);
                break;
        }
    }
    
    public static void cancelCartNotification(Context context) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}

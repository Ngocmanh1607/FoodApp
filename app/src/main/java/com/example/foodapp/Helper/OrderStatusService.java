package com.example.foodapp.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.foodapp.Activity.ListUserOrderActivity;
import com.example.foodapp.Activity.MainActivity;
import com.example.foodapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;

public class OrderStatusService extends Service {

    // Key để lấy user ID từ Intent
    private static final String USER_ID_KEY = "USER_ID";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Lấy user ID từ Intent
        String userId = intent.getStringExtra(USER_ID_KEY);
        if (userId != null) {
            // Bắt đầu lắng nghe thay đổi trạng thái đơn hàng
            startListeningForOrderStatus(userId);
        }
        return START_STICKY;
    }

    // Phương thức để bắt đầu lắng nghe thay đổi trạng thái đơn hàng từ Firebase
    private void startListeningForOrderStatus(String userId) {
        // Truy vấn Firebase để lấy các đơn hàng của người dùng
        Query orderQuery = FirebaseDatabase.getInstance().getReference("Orders").orderByChild("key").equalTo(userId);
        orderQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Duyệt qua tất cả các đơn hàng
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    // Lấy trạng thái đơn hàng
                    String status = orderSnapshot.child("status").getValue(String.class);

                    // Kiểm tra trạng thái và hiển thị thông báo tương ứng
                    if ("rejected".equalsIgnoreCase(status)) {
                        showNotification(getApplicationContext(), "Order Update", "Your order has been rejected.");
                    } else if ("accepted".equalsIgnoreCase(status)) {
                        showNotification(getApplicationContext(), "Order Update", "Your order has been accepted.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi truy vấn Firebase bị hủy
            }
        });
    }

    // Phương thức để hiển thị thông báo
    private void showNotification(Context context, String title, String message) {
        // Lấy NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo kênh thông báo nếu cần (cho Android Oreo trở lên)
        String channelId = "order_status_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Order Status Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo Intent để mở MainActivity khi người dùng nhấp vào thông báo
        Intent intent = new Intent(context, ListUserOrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Tạo PendingIntent với Intent đã tạo
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo)  // Đặt icon cho thông báo
                .setContentIntent(pendingIntent) // Đặt PendingIntent để mở MainActivity
                .setAutoCancel(true); // Tự động hủy thông báo khi người dùng nhấp vào

        // Hiển thị thông báo
        notificationManager.notify(0, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Dịch vụ không hỗ trợ bind
    }
}
